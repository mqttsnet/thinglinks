package com.mqttsnet.thinglinks.video.gb28181.platform;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.cmd.SipMessageBuilder;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.header.CallIdHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;

/**
 * Description:
 * 平台级联 SIP 指令发送器。
 * <p>
 * 作为 UAC 向上级平台发送 REGISTER/MESSAGE 等指令。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformCascadeCommander {

    private final SipConfig sipConfig;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final SipMessageBuilder sipMessageBuilder;
    private final SIPSender sipSender;

    /**
     * 向上级平台发送 REGISTER（首次注册，无认证）
     *
     * @param platform 上级平台配置
     * @param callId   CallIdHeader
     * @return callId 字符串（用于跟踪响应）
     */
    public String register(VideoPlatform platform, CallIdHeader callId) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            String localIp = resolveLocalIp(platform, tenantConfig);
            int localPort = resolveLocalPort(platform);
            int expires = ObjectUtil.defaultIfNull(platform.getRegisterExpires(), sipConfig.getRegisterTimeInterval());

            Request request = sipMessageBuilder.buildRegisterRequest(
                    platform.getServerGbId(), platform.getServerIp(), platform.getServerPort(),
                    platform.getDeviceGbId(), StrUtil.blankToDefault(platform.getServerGbDomain(), tenantConfig.getDomain()),
                    localIp, localPort,
                    StrUtil.blankToDefault(platform.getTransport(), "UDP"),
                    expires, callId);

            sipSender.transmitRequest(localIp, request);
            log.info("[平台级联] REGISTER 发送: serverGbId={}, serverIp={}:{}, expires={}",
                    platform.getServerGbId(), platform.getServerIp(), platform.getServerPort(), expires);

            return callId.getCallId();
        } catch (Exception e) {
            log.error("[平台级联] REGISTER 发送失败: serverGbId={}, error={}",
                    platform.getServerGbId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 向上级平台发送带认证的 REGISTER（收到 401 后重试）
     *
     * @param platform  上级平台配置
     * @param callId    CallIdHeader
     * @param wwwAuth   401 响应中的 WWW-Authenticate 头
     */
    public String registerWithAuth(VideoPlatform platform, CallIdHeader callId, WWWAuthenticateHeader wwwAuth) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            String localIp = resolveLocalIp(platform, tenantConfig);
            int localPort = resolveLocalPort(platform);
            int expires = ObjectUtil.defaultIfNull(platform.getRegisterExpires(), sipConfig.getRegisterTimeInterval());

            Request request = sipMessageBuilder.buildRegisterRequest(
                    platform.getServerGbId(), platform.getServerIp(), platform.getServerPort(),
                    platform.getDeviceGbId(), StrUtil.blankToDefault(platform.getServerGbDomain(), tenantConfig.getDomain()),
                    localIp, localPort,
                    StrUtil.blankToDefault(platform.getTransport(), "UDP"),
                    expires, callId);

            // 添加认证头
            String realm = wwwAuth.getRealm();
            String nonce = wwwAuth.getNonce();
            String username = StrUtil.blankToDefault(platform.getUsername(), platform.getDeviceGbId());
            String password = StrUtil.blankToDefault(platform.getPassword(), tenantConfig.getPassword());
            String requestUriStr = "sip:" + platform.getServerGbId() + "@" + platform.getServerIp() + ":" + platform.getServerPort();

            sipMessageBuilder.addAuthorizationHeader(request, username, password, realm, nonce, requestUriStr);

            sipSender.transmitRequest(localIp, request);
            log.info("[平台级联] REGISTER(Auth) 发送: serverGbId={}, realm={}", platform.getServerGbId(), realm);

            return callId.getCallId();
        } catch (Exception e) {
            log.error("[平台级联] REGISTER(Auth) 发送失败: serverGbId={}, error={}",
                    platform.getServerGbId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 向上级平台发送注销请求（Expires=0）
     */
    public void unregister(VideoPlatform platform, CallIdHeader callId) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            String localIp = resolveLocalIp(platform, tenantConfig);
            int localPort = resolveLocalPort(platform);

            Request request = sipMessageBuilder.buildRegisterRequest(
                    platform.getServerGbId(), platform.getServerIp(), platform.getServerPort(),
                    platform.getDeviceGbId(), StrUtil.blankToDefault(platform.getServerGbDomain(), tenantConfig.getDomain()),
                    localIp, localPort,
                    StrUtil.blankToDefault(platform.getTransport(), "UDP"),
                    0, callId);

            sipSender.transmitRequest(localIp, request);
            log.info("[平台级联] UNREGISTER 发送: serverGbId={}", platform.getServerGbId());
        } catch (Exception e) {
            log.error("[平台级联] UNREGISTER 发送失败: serverGbId={}, error={}",
                    platform.getServerGbId(), e.getMessage(), e);
        }
    }

    /**
     * 发送 Keepalive MESSAGE（心跳保活）
     */
    public void keepalive(VideoPlatform platform) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            String localIp = resolveLocalIp(platform, tenantConfig);
            int localPort = resolveLocalPort(platform);
            CallIdHeader callId = sipSender.getNewCallIdHeader(localIp,
                    StrUtil.blankToDefault(platform.getTransport(), "UDP"));

            String keepaliveXml = buildKeepaliveXml(platform.getDeviceGbId());
            Request request = sipMessageBuilder.buildMessageRequest(
                    platform.getServerGbId(), platform.getServerIp(), platform.getServerPort(),
                    StrUtil.blankToDefault(platform.getTransport(), "UDP"),
                    keepaliveXml, callId, tenantConfig);

            sipSender.transmitRequest(localIp, request);
            log.debug("[平台级联] Keepalive 发送: serverGbId={}", platform.getServerGbId());
        } catch (Exception e) {
            log.warn("[平台级联] Keepalive 发送失败: serverGbId={}, error={}",
                    platform.getServerGbId(), e.getMessage());
        }
    }

    private String resolveLocalIp(VideoPlatform platform, TenantSipConfig tenantConfig) {
        return StrUtil.isNotBlank(platform.getSipIp()) ? platform.getSipIp() : tenantConfig.getEffectiveHost();
    }

    private int resolveLocalPort(VideoPlatform platform) {
        return ObjectUtil.isNotNull(platform.getSipPort()) ? platform.getSipPort() : sipConfig.getPort();
    }

    private String buildKeepaliveXml(String deviceGbId) {
        return "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                "<Notify>\r\n" +
                "<CmdType>Keepalive</CmdType>\r\n" +
                "<SN>1</SN>\r\n" +
                "<DeviceID>" + deviceGbId + "</DeviceID>\r\n" +
                "<Status>OK</Status>\r\n" +
                "</Notify>\r\n";
    }
}
