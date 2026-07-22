package com.mqttsnet.thinglinks.video.onvif;

import cn.hutool.core.util.IdUtil;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifDevice;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ONVIF WS-Discovery 服务。
 *
 * <p>实现 WS-Discovery 1.1（OASIS）发现协议——通过 UDP 组播 239.255.255.250:3702
 * 发送 Probe 消息，接收设备 ProbeMatch 响应。
 *
 * <p>不做常驻监听，避免端口占用与多线程复杂度；每次调用 {@link #scan(int)} 触发一次
 * 发现，超时返回当下收集到的所有设备。典型耗时 3-5 秒。
 *
 * <p>多网卡处理：默认让 OS 选默认接口；要扫描特定网段，可在生产环境扩展支持
 * {@code NetworkInterface} 参数（当前不实现，避免过度设计）。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Service
@DS(DsConstant.BASE_TENANT)
public class OnvifDiscoveryService {

    /** WS-Discovery 标准组播地址（IPv4）。 */
    private static final String MULTICAST_ADDRESS = "239.255.255.250";
    /** WS-Discovery 标准端口。 */
    private static final int MULTICAST_PORT = 3702;
    /** 单次 receive 的最大等待毫秒，影响响应延迟设备的捕获。 */
    private static final int SOCKET_RECEIVE_TIMEOUT_MS = 800;
    /** 报文最大长度，足够装 5KB 以内的 ProbeMatch（典型设备约 1-2KB）。 */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 扫描局域网内 ONVIF 设备。
     *
     * @param overallTimeoutSeconds 整体超时秒数（建议 3-5），超时后立即返回已收集到的设备
     * @return 去重（按 EndpointReference）后的设备列表；扫描失败返回空列表，不抛异常
     */
    public List<OnvifDevice> scan(int overallTimeoutSeconds) {
        long deadline = System.currentTimeMillis() + Math.max(1, overallTimeoutSeconds) * 1000L;
        Map<String, OnvifDevice> uniq = new LinkedHashMap<>();

        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT_MS);
            // 不需要 joinGroup（我们是发送方，接收单播响应到本地端口即可）

            String messageId = "uuid:" + IdUtil.fastUUID();
            String probe = buildProbeMessage(messageId);
            byte[] probeBytes = probe.getBytes(StandardCharsets.UTF_8);

            DatagramPacket out = new DatagramPacket(probeBytes, probeBytes.length,
                    new InetSocketAddress(InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT));
            socket.send(out);
            log.info("[ONVIF Discovery] 发送 Probe 到 {}:{}, MessageID={}, deadline={}",
                    MULTICAST_ADDRESS, MULTICAST_PORT, messageId, Instant.ofEpochMilli(deadline));

            // 循环接收响应直到超时；setSoTimeout 用 800ms 让循环可以频繁检查 deadline
            byte[] buf = new byte[BUFFER_SIZE];
            while (System.currentTimeMillis() < deadline) {
                DatagramPacket in = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(in);
                } catch (java.net.SocketTimeoutException e) {
                    continue;
                }
                String body = new String(in.getData(), in.getOffset(), in.getLength(), StandardCharsets.UTF_8);
                OnvifDevice device = parseProbeMatch(body);
                if (device != null && device.getXaddr() != null) {
                    String key = device.getEndpointReference() != null
                            ? device.getEndpointReference() : device.getXaddr();
                    uniq.putIfAbsent(key, device);
                }
            }
        } catch (Exception e) {
            log.error("[ONVIF Discovery] 扫描异常", e);
        }
        log.info("[ONVIF Discovery] 扫描结束，发现设备 {} 台", uniq.size());
        return new ArrayList<>(uniq.values());
    }

    /**
     * 构造标准 WS-Discovery Probe SOAP 报文。
     * <p>Types 用 {@code dn:NetworkVideoTransmitter}（ONVIF 摄像头），过滤掉非视频设备。
     */
    private String buildProbeMessage(String messageId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<e:Envelope xmlns:e=\"http://www.w3.org/2003/05/soap-envelope\""
                + " xmlns:w=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\""
                + " xmlns:d=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\""
                + " xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\">"
                + "<e:Header>"
                + "<w:MessageID>" + messageId + "</w:MessageID>"
                + "<w:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>"
                + "<w:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>"
                + "</e:Header>"
                + "<e:Body>"
                + "<d:Probe>"
                + "<d:Types>dn:NetworkVideoTransmitter</d:Types>"
                + "</d:Probe>"
                + "</e:Body>"
                + "</e:Envelope>";
    }

    /**
     * 解析单条 ProbeMatch 响应。多 Match 并存的设备返回第一个；异常时返回 null。
     */
    private OnvifDevice parseProbeMatch(String body) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(body)));

            NodeList matches = doc.getElementsByTagNameNS("*", "ProbeMatch");
            if (matches.getLength() == 0) {
                return null;
            }
            Element match = (Element) matches.item(0);
            OnvifDevice.OnvifDeviceBuilder b = OnvifDevice.builder();

            NodeList xaddrs = match.getElementsByTagNameNS("*", "XAddrs");
            if (xaddrs.getLength() > 0) {
                String addrs = xaddrs.item(0).getTextContent();
                // XAddrs 可能是多个空格分隔的 URL，取第一个
                if (addrs != null && !addrs.isBlank()) {
                    b.xaddr(addrs.trim().split("\\s+")[0]);
                }
            }
            NodeList epRef = match.getElementsByTagNameNS("*", "Address");
            if (epRef.getLength() > 0) {
                b.endpointReference(epRef.item(0).getTextContent());
            }
            NodeList types = match.getElementsByTagNameNS("*", "Types");
            if (types.getLength() > 0) {
                b.types(types.item(0).getTextContent());
            }
            NodeList scopes = match.getElementsByTagNameNS("*", "Scopes");
            if (scopes.getLength() > 0) {
                String raw = scopes.item(0).getTextContent();
                if (raw != null) {
                    b.scopes(Arrays.stream(raw.trim().split("\\s+"))
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList()));
                }
            }
            return b.build();
        } catch (Exception e) {
            log.debug("[ONVIF Discovery] 解析 ProbeMatch 失败: {}", e.getMessage());
            return null;
        }
    }
}
