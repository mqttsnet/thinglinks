package com.mqttsnet.thinglinks.video.gb28181;

import com.mqttsnet.thinglinks.video.cache.SipServerInfoCacheVO;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.config.gb28181.DefaultProperties;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.gb28181.factory.GbStringMsgParserFactory;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.ISIPProcessorObserver;
import com.mqttsnet.thinglinks.video.manager.sip.SipServerInfoManager;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.TransportNotSupportedException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIP 层初始化。
 * <p>
 * 始终自动扫描所有非 docker/localhost 的 IPv4 网卡，不再依赖 Nacos sip.ip 配置。
 * domain/id/password 已移到租户级 video_sip_config 表。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@Order(value = 10)
@RequiredArgsConstructor
public class SipLayer implements CommandLineRunner {

    private final Map<String, SipProviderImpl> tcpSipProviderMap = new ConcurrentHashMap<>();
    private final Map<String, SipProviderImpl> udpSipProviderMap = new ConcurrentHashMap<>();
    private final List<String> monitorIps = new ArrayList<>();
    private final SipConfig sipConfig;
    private final ISIPProcessorObserver sipProcessorObserver;
    private final UserSetting userSetting;
    private final SipServerInfoManager sipServerInfoManager;

    @Override
    public void run(String... args) {
        log.info("[SIP] 服务初始化开始...");

        // 自动扫描所有非 docker/localhost 的 IPv4 网卡
        scanNetworkInterfaces();

        if (monitorIps.isEmpty()) {
            log.error("[SIP] 未扫描到可用网卡，请检查网络配置");
            System.exit(1);
        }

        log.info("[SIP] 监听网卡: {}", monitorIps);

        SipFactory.getInstance().setPathName("gov.nist");
        for (String monitorIp : monitorIps) {
            addListeningPoint(monitorIp, sipConfig.getPort());
        }

        if (udpSipProviderMap.size() + tcpSipProviderMap.size() == 0) {
            log.error("[SIP] 所有监听点启动失败，终止程序");
            System.exit(1);
        }

        registerToRedis();
    }

    /**
     * 网卡名前缀黑名单：一律跳过这些虚拟/隧道/桥接/容器卡。
     * <ul>
     *   <li>docker / br- / veth — Docker 容器桥</li>
     *   <li>utun / tun / tap — 用户态隧道（Tailscale、WireGuard、iCloud Private Relay、VPN 工具）</li>
     *   <li>awdl / llw — macOS Apple Wireless Direct Link / Low-latency Wi-Fi</li>
     *   <li>bridge — 各平台桥接卡</li>
     *   <li>vmnet / vboxnet — VMware / VirtualBox 虚拟网络</li>
     * </ul>
     */
    private static final String[] VIRTUAL_NIC_NAME_PREFIXES = {
            "docker", "br-", "veth",
            "utun", "tun", "tap",
            "awdl", "llw",
            "bridge",
            "vmnet", "vboxnet"
    };

    /**
     * 自动扫描所有合适的 IPv4 网卡（过滤虚拟/保留段）。
     * <p>
     * 过滤规则：
     * <ul>
     *   <li>跳过 loopback / 未 up / 标记为 virtual 的网卡</li>
     *   <li>跳过名字前缀命中 {@link #VIRTUAL_NIC_NAME_PREFIXES} 的网卡</li>
     *   <li>跳过 {@code 127.0.0.0/8}（loopback）</li>
     *   <li>跳过 {@code 169.254.0.0/16}（link-local, RFC 3927）</li>
     *   <li>跳过 {@code 198.18.0.0/15}（RFC 2544 基准测试段，macOS iCloud Private Relay 常用）</li>
     *   <li>跳过 {@code 100.64.0.0/10}（CGNAT，Tailscale 默认段）</li>
     * </ul>
     * <p>
     * 排序策略：过滤后按 IP 字符串自然序稳定排序，让同一台机器每次启动 monitorIps 顺序一致，
     * 方便运维按第一张卡判断"默认出口"。
     */
    private void scanNetworkInterfaces() {
        List<String> discovered = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();
                if (isExcludedNic(nif)) {
                    continue;
                }
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if (isExcludedIpv4(ip)) {
                        log.debug("[SIP] 跳过网卡 {} → {}（虚拟/保留段）", nif.getName(), ip);
                        continue;
                    }
                    log.info("[SIP] 发现网卡: {} → {}", nif.getName(), ip);
                    discovered.add(ip);
                }
            }
        } catch (Exception e) {
            log.error("[SIP] 扫描网卡失败", e);
        }
        // 稳定排序：让启动时 monitorIps 顺序可预期，避免 OS 返回顺序抖动
        discovered.sort(String::compareTo);
        monitorIps.addAll(discovered);
    }

    /**
     * 网卡级别的过滤：跳过 loopback / down / virtual / 名字命中黑名单前缀。
     */
    private static boolean isExcludedNic(NetworkInterface nif) {
        try {
            if (nif.isLoopback() || !nif.isUp() || nif.isVirtual()) {
                return true;
            }
        } catch (Exception e) {
            // 某些平台 getHardwareAddress 等内部调用可能抛异常，保守跳过
            return true;
        }
        String name = nif.getName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        for (String prefix : VIRTUAL_NIC_NAME_PREFIXES) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * IP 段级别的过滤：127/8、169.254/16、198.18/15、100.64/10 一律跳过。
     */
    private static boolean isExcludedIpv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        // 127/8 loopback
        if (ip.startsWith("127.")) {
            return true;
        }
        // 169.254/16 link-local
        if (ip.startsWith("169.254.")) {
            return true;
        }
        // 198.18/15 RFC 2544（198.18.x.x + 198.19.x.x）
        if (ip.startsWith("198.18.") || ip.startsWith("198.19.")) {
            return true;
        }
        // 100.64/10 CGNAT：100.64.0.0 — 100.127.255.255
        if (ip.startsWith("100.")) {
            int dot2 = ip.indexOf('.', 4);
            if (dot2 > 4) {
                try {
                    int second = Integer.parseInt(ip.substring(4, dot2));
                    if (second >= 64 && second <= 127) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // 非法 IP 字符串，保守放行（后续阶段会报错）
                }
            }
        }
        return false;
    }

    private void addListeningPoint(String monitorIp, int port) {
        SipStackImpl sipStack;
        try {
            sipStack = (SipStackImpl) SipFactory.getInstance().createSipStack(
                    DefaultProperties.getProperties("GB28181_SIP", userSetting.getSipLog(), userSetting.isSipCacheServerConnections()));
            sipStack.setMessageParserFactory(new GbStringMsgParserFactory());
        } catch (PeerUnavailableException e) {
            log.error("[SIP] 监听 {} 失败, 请检查 IP 是否正确", monitorIp, e);
            return;
        }

        try {
            ListeningPoint tcpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "TCP");
            SipProviderImpl tcpSipProvider = (SipProviderImpl) sipStack.createSipProvider(tcpListeningPoint);
            tcpSipProvider.setDialogErrorsAutomaticallyHandled();
            tcpSipProvider.addSipListener(sipProcessorObserver);
            tcpSipProviderMap.put(monitorIp, tcpSipProvider);
            log.info("[SIP] tcp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException | TooManyListenersException
                 | ObjectInUseException | InvalidArgumentException e) {
            log.error("[SIP] tcp://{}:{} 启动失败", monitorIp, port);
        }

        try {
            ListeningPoint udpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "UDP");
            SipProviderImpl udpSipProvider = (SipProviderImpl) sipStack.createSipProvider(udpListeningPoint);
            udpSipProvider.addSipListener(sipProcessorObserver);
            udpSipProvider.setDialogErrorsAutomaticallyHandled();
            udpSipProviderMap.put(monitorIp, udpSipProvider);
            log.info("[SIP] udp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException | TooManyListenersException
                 | ObjectInUseException | InvalidArgumentException e) {
            log.error("[SIP] udp://{}:{} 启动失败", monitorIp, port);
        }
    }

    // ===== Provider 获取方法 =====

    public SipProviderImpl getUdpSipProvider(String ip) {
        if (udpSipProviderMap.size() == 1) {
            return udpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        if (ObjectUtils.isEmpty(ip)) {
            return udpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        SipProviderImpl provider = udpSipProviderMap.get(ip);
        if (provider == null) {
            // 云/NAT/容器部署场景下，sipServerAddress 通常是公网 IP / LB IP / 宿主 IP，
            // 本机网卡列表里不会有这个 IP（它由云 NAT 映射反向挂到内网卡）—— 这是预期行为。
            // 此时任意监听点发包即可，出包经 OS 路由到默认网关，云 NAT 会把源 IP 改写为公网 IP。
            // 故 fallback 不是错误，只是 debug 级别告知。
            log.debug("[SIP] udp://{} 在本机网卡列表中不存在（云/NAT/容器场景正常），使用任意可用监听点发包", ip);
            provider = udpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        return provider;
    }

    public SipProviderImpl getUdpSipProvider() {
        if (udpSipProviderMap.size() != 1) {
            return null;
        }
        return udpSipProviderMap.values().stream().findFirst().orElse(null);
    }

    public SipProviderImpl getTcpSipProvider() {
        if (tcpSipProviderMap.size() != 1) {
            return null;
        }
        return tcpSipProviderMap.values().stream().findFirst().orElse(null);
    }

    public SipProviderImpl getTcpSipProvider(String ip) {
        if (tcpSipProviderMap.size() == 1) {
            return tcpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        if (ObjectUtils.isEmpty(ip)) {
            return tcpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        SipProviderImpl provider = tcpSipProviderMap.get(ip);
        if (provider == null) {
            // 同 UDP 说明：云/NAT/容器场景下 sipServerAddress 常常不在本机网卡列表里。
            log.debug("[SIP] tcp://{} 在本机网卡列表中不存在（云/NAT/容器场景正常），使用任意可用监听点发包", ip);
            provider = tcpSipProviderMap.values().stream().findFirst().orElse(null);
        }
        return provider;
    }

    public Map<String, SipProviderImpl> getUdpSipProviderMap() {
        return udpSipProviderMap;
    }

    public Map<String, SipProviderImpl> getTcpSipProviderMap() {
        return tcpSipProviderMap;
    }

    /**
     * 获取第一个监听 IP（供 TenantSipConfigProvider 使用）
     */
    public String getMonitorIp() {
        return monitorIps.isEmpty() ? "0.0.0.0" : monitorIps.get(0);
    }

    /**
     * 获取所有监听 IP
     */
    public List<String> getMonitorIps() {
        return monitorIps;
    }

    /**
     * 获取本地 IP（优先用设备连接进来的本地地址）
     */
    public String getLocalIp(String deviceLocalIp) {
        if (monitorIps.size() == 1) {
            return monitorIps.get(0);
        }
        if (!ObjectUtils.isEmpty(deviceLocalIp)) {
            return deviceLocalIp;
        }
        return getUdpSipProvider().getListeningPoint().getIPAddress();
    }

    // ===== Redis 注册 =====

    /**
     * 注册/续期 SIP 物理节点信息到 Redis（不含业务域/ID，纯物理层）。
     */
    public void registerToRedis() {
        try {
            String instanceId = getMonitorIp() + ":" + sipConfig.getPort();
            SipServerInfoCacheVO cacheVO = SipServerInfoCacheVO.builder()
                    .instanceId(instanceId)
                    .monitorIps(monitorIps)
                    .port(sipConfig.getPort())
                    .onlineStatus(true)
                    .registerTime(LocalDateTime.now())
                    .build();
            sipServerInfoManager.register(cacheVO);
        } catch (Exception e) {
            log.error("[SIP Redis] 节点注册/续期失败", e);
        }
    }

    @Scheduled(fixedRate = 60 * 1000, initialDelay = 60 * 1000)
    public void sipHeartbeatRenewal() {
        if (!monitorIps.isEmpty()) {
            registerToRedis();
        }
    }
}
