package com.mqttsnet.thinglinks.video.gb28181;

import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.config.gb28181.DefaultProperties;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.gb28181.factory.GbStringMsgParserFactory;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.ISIPProcessorObserver;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIP 层初始化
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@Order(value = 10)
public class SipLayer implements CommandLineRunner {

    private final Map<String, SipProviderImpl> tcpSipProviderMap = new ConcurrentHashMap<>();
    private final Map<String, SipProviderImpl> udpSipProviderMap = new ConcurrentHashMap<>();
    private final List<String> monitorIps = new ArrayList<>();
    @Autowired
    private SipConfig sipConfig;
    @Autowired
    private ISIPProcessorObserver sipProcessorObserver;
    @Autowired
    private UserSetting userSetting;

    @Override
    public void run(String... args) {
        log.info("SIP服务初始化开始.......");
        if (ObjectUtils.isEmpty(sipConfig.getIp())) {
            try {
                // 获得本机的所有网络接口
                Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
                while (nifs.hasMoreElements()) {
                    NetworkInterface nif = nifs.nextElement();
                    // 获得与该网络接口绑定的 IP 地址，一般只有一个
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            if ("127.0.0.1".equals(addr.getHostAddress())) {
                                continue;
                            }
                            if (nif.getName().startsWith("docker")) {
                                continue;
                            }
                            // 只关心 IPv4 地址
                            log.info("[自动配置SIP监听网卡] 网卡接口地址： {}", addr.getHostAddress());
                            monitorIps.add(addr.getHostAddress());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[读取网卡信息失败]", e);
            }
            if (monitorIps.isEmpty()) {
                log.error("[自动配置SIP监听网卡信息失败]， 请手动配置SIP.IP后重新启动");
                System.exit(1);
            }
        } else {
            // 使用逗号分割多个ip
            String separator = StrPool.COMMA;
            if (sipConfig.getIp().indexOf(separator) > 0) {
                String[] split = sipConfig.getIp().split(separator);
                monitorIps.addAll(Arrays.asList(split));
            } else {
                monitorIps.add(sipConfig.getIp());
            }
        }
        sipConfig.setMonitorIps(monitorIps);
        if (ObjectUtils.isEmpty(sipConfig.getShowIp())) {
            sipConfig.setShowIp(String.join(StrPool.COMMA, monitorIps));
        }
        SipFactory.getInstance().setPathName("gov.nist");
        if (!monitorIps.isEmpty()) {
            for (String monitorIp : monitorIps) {
                addListeningPoint(monitorIp, sipConfig.getPort());
            }
            // TODO 存储SIP 服务信息

            if (udpSipProviderMap.size() + tcpSipProviderMap.size() == 0) {
                // 没有启动成功、终止程序
                System.exit(1);
            }
        }
    }

    private void addListeningPoint(String monitorIp, int port) {
        SipStackImpl sipStack;
        try {
            sipStack = (SipStackImpl) SipFactory.getInstance().createSipStack(DefaultProperties.getProperties("GB28181_SIP", userSetting.getSipLog(), userSetting.isSipCacheServerConnections()));
            sipStack.setMessageParserFactory(new GbStringMsgParserFactory());
        } catch (PeerUnavailableException e) {
            log.error("[SIP SERVER] SIP服务启动失败， 监听地址{}失败,请检查ip是否正确", monitorIp, e);
            return;
        }

        try {
            ListeningPoint tcpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "TCP");
            SipProviderImpl tcpSipProvider = (SipProviderImpl) sipStack.createSipProvider(tcpListeningPoint);

            tcpSipProvider.setDialogErrorsAutomaticallyHandled();
            tcpSipProvider.addSipListener(sipProcessorObserver);
            tcpSipProviderMap.put(monitorIp, tcpSipProvider);
            log.info("[SIP SERVER] tcp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[SIP SERVER] tcp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }

        try {
            ListeningPoint udpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "UDP");

            SipProviderImpl udpSipProvider = (SipProviderImpl) sipStack.createSipProvider(udpListeningPoint);
            udpSipProvider.addSipListener(sipProcessorObserver);
            udpSipProvider.setDialogErrorsAutomaticallyHandled();
            udpSipProviderMap.put(monitorIp, udpSipProvider);

            log.info("[SIP SERVER] udp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[SIP SERVER] udp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }
    }

    public SipProviderImpl getUdpSipProvider(String ip) {
        if (udpSipProviderMap.size() == 1) {
            return udpSipProviderMap.values().stream().findFirst().get();
        }
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return udpSipProviderMap.get(ip);
    }

    public SipProviderImpl getUdpSipProvider() {
        if (udpSipProviderMap.size() != 1) {
            return null;
        }
        return udpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider() {
        if (tcpSipProviderMap.size() != 1) {
            return null;
        }
        return tcpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider(String ip) {
        if (tcpSipProviderMap.size() == 1) {
            return tcpSipProviderMap.values().stream().findFirst().get();
        }
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return tcpSipProviderMap.get(ip);
    }

    public String getLocalIp(String deviceLocalIp) {
        if (monitorIps.size() == 1) {
            return monitorIps.get(0);
        }
        if (!ObjectUtils.isEmpty(deviceLocalIp)) {
            return deviceLocalIp;
        }
        return getUdpSipProvider().getListeningPoint().getIPAddress();
    }
}
