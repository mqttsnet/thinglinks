package com.mqttsnet.thinglinks.video.onvif;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifDevice;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifProfile;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceSaveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ONVIF 业务 facade：扫描 → 取详情 → 取 RTSP URL → 注册成 RTSP 设备。
 *
 * <p>本类把 ONVIF 协议特性收口在内，对外提供"局域网扫描"和"导入为可点播设备"两个高层操作。
 * 真正的拉流流程在导入后复用 {@link com.mqttsnet.thinglinks.video.service.stream.RtspPlayService}，
 * 因为 ONVIF 标准就是 SOAP 控制 + RTSP 取流——把 RTSP URL 抓到手以后，二者完全一致。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class OnvifService {

    private final OnvifDiscoveryService onvifDiscoveryService;
    private final OnvifSoapClient onvifSoapClient;
    private final VideoDeviceService videoDeviceService;

    /**
     * 扫描局域网内 ONVIF 设备，返回列表（不写库，前端展示给用户挑选导入）。
     *
     * @param timeoutSeconds 扫描超时秒数（建议 3-5）
     */
    public List<OnvifDevice> discover(int timeoutSeconds) {
        return onvifDiscoveryService.scan(timeoutSeconds);
    }

    /**
     * 拉取指定设备的 Profile 列表（含主码流 / 子码流的分辨率、编码等）。
     */
    public List<OnvifProfile> getProfiles(String xaddr, String username, String password) {
        if (StrUtil.isBlank(xaddr)) {
            throw BizException.wrap("xaddr 不能为空");
        }
        return onvifSoapClient.getProfiles(xaddr, username, password);
    }

    /**
     * 把 ONVIF 设备的某个 Profile 导入成平台 RTSP 设备。
     *
     * <p>动作链：
     * <ol>
     *   <li>SOAP {@code GetStreamUri(profileToken)} 取实际 RTSP 地址</li>
     *   <li>SOAP {@code GetDeviceInformation} 拿厂商 / 型号</li>
     *   <li>构造 VideoDeviceSaveVO（accessProtocol = ONVIF，protocolConfig.streamSource 写入完整 URL）</li>
     *   <li>调用 {@code videoDeviceService.save} 走通用 CRUD 入口（含 RTSP 校验 + 默认通道）</li>
     * </ol>
     *
     * <p>同 host:port 重复导入会被自动幂等：默认设备 ID 是 {@code ONVIF_<host>_<port>}，唯一通道复用。
     */
    public String importDevice(String xaddr, String username, String password, String profileToken,
                               String mediaIdentification, String customName) {
        if (StrUtil.isBlank(xaddr)) {
            throw BizException.wrap("xaddr 不能为空");
        }
        if (StrUtil.isBlank(profileToken)) {
            throw BizException.wrap("profileToken 不能为空");
        }
        if (StrUtil.isBlank(mediaIdentification)) {
            throw BizException.wrap("mediaIdentification（关联流媒体）不能为空");
        }

        String streamUri = onvifSoapClient.getStreamUri(xaddr, username, password, profileToken)
                .orElseThrow(() -> BizException.wrap("ONVIF 设备未返回 RTSP 地址"));
        log.info("[ONVIF 导入] xaddr={}, profileToken={}, streamUri={}", xaddr, profileToken, streamUri);

        Map<String, String> info = onvifSoapClient.getDeviceInformation(xaddr, username, password);

        // 把 username/password 注入到 RTSP URL 让 ZLM 拉流时携带凭据
        String rtspWithAuth = injectCredentials(streamUri, username, password);

        VideoDeviceProtocolConfig.StreamSource source = VideoDeviceProtocolConfig.StreamSource.builder()
                .url(rtspWithAuth)
                .username(username)
                .onvifProfileToken(profileToken)
                .rtpType("0")
                .build();
        VideoDeviceProtocolConfig protocolConfig = VideoDeviceProtocolConfig.builder()
                .streamSource(source)
                .build();

        // 从 xaddr 里提取 host / port，落库供前端展示（实际拉流走 streamSource.url，不依赖此处端口）
        String host = parseHost(xaddr);
        Integer port = parsePort(xaddr);
        VideoDeviceSaveVO saveVO = VideoDeviceSaveVO.builder()
                .accessProtocol(AccessProtocolEnum.ONVIF.getValue())
                .deviceName(StrUtil.blankToDefault(customName, info.getOrDefault("model", "ONVIF Camera")))
                .customName(customName)
                .manufacturer(info.get("manufacturer"))
                .model(info.get("model"))
                .firmware(info.get("firmware"))
                .host(host)
                .port(port)
                .authSecret(password)
                .mediaIdentification(mediaIdentification)
                .protocolConfig(protocolConfig)
                .onlineStatus(Boolean.TRUE)
                .build();

        VideoDevice entity = videoDeviceService.save(saveVO);
        return entity.getDeviceIdentification();
    }

    /**
     * 从 SOAP 端点里提取 host（如 {@code http://192.168.1.108/onvif/device_service} → {@code 192.168.1.108}）。
     */
    private String parseHost(String xaddr) {
        try {
            return Optional.ofNullable(java.net.URI.create(xaddr).getHost())
                    .filter(StrUtil::isNotBlank)
                    .orElse("0.0.0.0");
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    /**
     * 从 SOAP 端点里提取端口；URI 没显式端口时按 scheme 默认（http=80, https=443）。
     */
    private Integer parsePort(String xaddr) {
        try {
            java.net.URI uri = java.net.URI.create(xaddr);
            int p = uri.getPort();
            if (p > 0) {
                return p;
            }
            return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        } catch (Exception e) {
            return 80;
        }
    }

    /**
     * 把 username:password 注入 RTSP URL。多数 ONVIF 设备返回的 URL 不带凭据，
     * 但 ZLM addStreamProxy 需要完整 URL 才能携带认证。
     */
    private String injectCredentials(String url, String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password) || url == null) {
            return url;
        }
        if (url.contains("@")) {
            // 已含凭据，不重复注入
            return url;
        }
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }
        return url.substring(0, schemeEnd + 3)
                + username + ":" + password + "@"
                + url.substring(schemeEnd + 3);
    }
}
