package com.mqttsnet.thinglinks.video.onvif;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 极简 ONVIF SOAP 客户端。
 *
 * <p>不引入 CXF / JAX-WS，直接手写 SOAP envelope，原因：
 * <ul>
 *   <li>ONVIF 实际只用 5-6 个核心方法（GetDeviceInformation / GetCapabilities / GetProfiles / GetStreamUri / 几个 PTZ）</li>
 *   <li>WSDL/SOAP 工具链生成的代码量是手写版的 50 倍以上，几乎都是没用的 stub</li>
 *   <li>WS-Security 的 UsernameToken 在自动生成代码里反而要打额外补丁，手写更直接</li>
 * </ul>
 *
 * <p>使用：注入此 Bean，按方法名调用即可，所有方法都是无状态线程安全的。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Component
public class OnvifSoapClient {

    /** Device 服务 SOAP 命名空间。 */
    private static final String NS_DEVICE = "http://www.onvif.org/ver10/device/wsdl";
    /** Media 服务 SOAP 命名空间（ver10 兼容大多数设备；ver20 部分新设备需要）。 */
    private static final String NS_MEDIA = "http://www.onvif.org/ver10/media/wsdl";
    /** 公共 schema 命名空间，用于 GetStreamUri 的 StreamSetup. */
    private static final String NS_TT = "http://www.onvif.org/ver10/schema";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * 调用 {@code GetDeviceInformation}，返回 manufacturer / model / firmware / serialNumber 等。
     * <p>失败返回空 Map（不抛异常），避免设备发现流程因单台设备超时被打断。
     */
    public Map<String, String> getDeviceInformation(String xaddr, String username, String password) {
        String body = "<tds:GetDeviceInformation xmlns:tds=\"" + NS_DEVICE + "\"/>";
        try {
            Document doc = invoke(xaddr, body, username, password);
            Map<String, String> info = new LinkedHashMap<>();
            putText(doc, "Manufacturer", v -> info.put("manufacturer", v));
            putText(doc, "Model", v -> info.put("model", v));
            putText(doc, "FirmwareVersion", v -> info.put("firmware", v));
            putText(doc, "SerialNumber", v -> info.put("serialNumber", v));
            putText(doc, "HardwareId", v -> info.put("hardwareId", v));
            return info;
        } catch (Exception e) {
            log.warn("[ONVIF] GetDeviceInformation 失败: xaddr={}, error={}", xaddr, e.getMessage());
            return Map.of();
        }
    }

    /**
     * 列出设备所有 Media Profile（主码流 / 子码流 / ...）。
     */
    public List<OnvifProfile> getProfiles(String xaddr, String username, String password) {
        String body = "<trt:GetProfiles xmlns:trt=\"" + NS_MEDIA + "\"/>";
        Document doc;
        try {
            doc = invoke(xaddr, body, username, password);
        } catch (Exception e) {
            log.warn("[ONVIF] GetProfiles 失败: xaddr={}, error={}", xaddr, e.getMessage());
            return List.of();
        }
        List<OnvifProfile> profiles = new ArrayList<>();
        NodeList profileNodes = doc.getElementsByTagNameNS("*", "Profiles");
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Element p = (Element) profileNodes.item(i);
            OnvifProfile.OnvifProfileBuilder b = OnvifProfile.builder()
                    .token(p.getAttribute("token"))
                    .name(textOf(p, "Name"));
            // VideoEncoderConfiguration → Encoding / Resolution / RateControl
            Element venc = firstChild(p, "VideoEncoderConfiguration");
            if (venc != null) {
                b.videoEncoding(textOf(venc, "Encoding"));
                Element resolution = firstChild(venc, "Resolution");
                if (resolution != null) {
                    parseInt(textOf(resolution, "Width"), b::width);
                    parseInt(textOf(resolution, "Height"), b::height);
                }
                Element rate = firstChild(venc, "RateControl");
                if (rate != null) {
                    parseInt(textOf(rate, "FrameRateLimit"), b::frameRate);
                    parseInt(textOf(rate, "BitrateLimit"), b::bitrate);
                }
            }
            profiles.add(b.build());
        }
        return profiles;
    }

    /**
     * 取指定 Profile 的 RTSP 拉流 URL。{@code StreamSetup.Stream=RTP-Unicast, Transport.Protocol=RTSP}。
     */
    public Optional<String> getStreamUri(String xaddr, String username, String password, String profileToken) {
        String body = "<trt:GetStreamUri xmlns:trt=\"" + NS_MEDIA + "\">"
                + "<trt:StreamSetup>"
                + "<tt:Stream xmlns:tt=\"" + NS_TT + "\">RTP-Unicast</tt:Stream>"
                + "<tt:Transport xmlns:tt=\"" + NS_TT + "\"><tt:Protocol>RTSP</tt:Protocol></tt:Transport>"
                + "</trt:StreamSetup>"
                + "<trt:ProfileToken>" + escapeXml(profileToken) + "</trt:ProfileToken>"
                + "</trt:GetStreamUri>";
        try {
            Document doc = invoke(xaddr, body, username, password);
            // 成功响应里只有一个 Uri 节点
            NodeList uri = doc.getElementsByTagNameNS("*", "Uri");
            if (uri.getLength() == 0) {
                return Optional.empty();
            }
            return Optional.ofNullable(uri.item(0).getTextContent()).map(String::trim).filter(s -> !s.isEmpty());
        } catch (Exception e) {
            log.warn("[ONVIF] GetStreamUri 失败: xaddr={}, profileToken={}, error={}",
                    xaddr, profileToken, e.getMessage());
            return Optional.empty();
        }
    }

    // ============== SOAP 调用通用流程 ==============

    /**
     * 向 ONVIF 设备发起 SOAP 调用并解析响应。
     * <p>UserName/Password 留空（null/blank）时不附加 WS-Security Header，便于免认证设备。
     *
     * @param xaddr      设备 SOAP 端点
     * @param soapAction SOAP body 内的请求元素 XML（不包含 envelope）
     * @return 解析后的响应 Document
     */
    private Document invoke(String xaddr, String soapAction, String username, String password) throws Exception {
        String envelope = buildEnvelope(soapAction, username, password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xaddr))
                .header("Content-Type", "application/soap+xml; charset=utf-8")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            // 把 SOAP Fault 解析出来给上层
            String reason = parseSoapFaultReason(response.body()).orElse(response.body());
            throw BizException.wrap("ONVIF 请求失败 status=" + response.statusCode() + ": " + reason);
        }
        return parseXml(response.body());
    }

    private String buildEnvelope(String soapAction, String username, String password) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
        sb.append("<s:Header>");
        if (username != null && !username.isBlank() && password != null) {
            sb.append(OnvifAuthenticator.buildSecurityHeader(username, password));
        }
        sb.append("</s:Header>");
        sb.append("<s:Body>").append(soapAction).append("</s:Body>");
        sb.append("</s:Envelope>");
        return sb.toString();
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // XXE 防御：禁用外部实体解析
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private Optional<String> parseSoapFaultReason(String body) {
        try {
            Document doc = parseXml(body);
            NodeList reason = doc.getElementsByTagNameNS("*", "Text");
            if (reason.getLength() > 0) {
                return Optional.ofNullable(reason.item(0).getTextContent());
            }
        } catch (Exception ignored) {
            // body 不是合法 XML，原样返回
        }
        return Optional.empty();
    }

    // ============== DOM 辅助 ==============

    private void putText(Document doc, String localName, java.util.function.Consumer<String> setter) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            if (text != null && !text.isBlank()) {
                setter.accept(text.trim());
            }
        }
    }

    private String textOf(Element parent, String localName) {
        Element child = firstChild(parent, localName);
        return child == null ? null : child.getTextContent();
    }

    private Element firstChild(Element parent, String localName) {
        NodeList children = parent.getChildNodes();
        AtomicReference<Element> ref = new AtomicReference<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && localName.equals(n.getLocalName())) {
                ref.set((Element) n);
                break;
            }
        }
        return ref.get();
    }

    private void parseInt(String value, java.util.function.Consumer<Integer> setter) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            setter.accept(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            // 设备返回非数字（如某些山寨设备会写浮点），忽略
        }
    }

    private static String escapeXml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
