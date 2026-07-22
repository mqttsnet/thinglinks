package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigHolder;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.dto.common.RemoteAddressInfo;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.GbSipDate;
import com.mqttsnet.thinglinks.video.enumeration.device.TransportProtocolEnum;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.auth.DigestServerAuthenticationHelper;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.SIPDateHeader;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * SIP命令类型： REGISTER请求
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class RegisterRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    public final String method = SipCommandTypeEnum.REGISTER.getValue();

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Autowired
    private SIPSender sipSender;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private SipEventPublisher sipEventPublisher;

    @Override
    public void afterPropertiesSet() {
        // 添加消息处理的订阅
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    /**
     * 收到注册请求 处理
     */
    @Override
    public void process(RequestEvent evt) {
        MDC.put(ContextConstants.TRACE_ID_HEADER, IdUtil.fastSimpleUUID());
        log.info("接收到设备[{}]消息：{}", method, evt.getRequest());
        SIPRequest request = (SIPRequest) evt.getRequest();
        handlerTenantId(request);
        Response response = null;
        final boolean registerFlag = request.getExpires().getExpires() != 0;
        final String deviceIdentification = extractDeviceIdentification(request);
        final RemoteAddressInfo remoteAddressInfo = getRemoteAddressInfo(request);

        try {
            logProcessingStart(registerFlag, deviceIdentification, remoteAddressInfo);

            //处理设备注册
            processDeviceRegistration(request, response, registerFlag, deviceIdentification, remoteAddressInfo);
        } catch (BizException bizException) {
            log.info("处理SIP请求时发生业务异常: {}", request.getRequestURI(), bizException);
        } catch (SipException | NoSuchAlgorithmException | ParseException e) {
            log.warn("处理SIP请求时发生系统异常: {}", request.getRequestURI(), e);
        }
    }


    /**
     * 从请求中提取设备标识
     *
     * @param request SIP请求对象
     * @return 设备唯一标识
     */
    private String extractDeviceIdentification(SIPRequest request) {
        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        AddressImpl address = (AddressImpl) fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();
        return uri.getUser();
    }

    /**
     * 获取远程地址信息
     *
     * @param request SIP请求对象
     * @return 远程地址信息对象
     */
    private RemoteAddressInfo getRemoteAddressInfo(SIPRequest request) {
        return SipUtils.getRemoteAddressFromRequest(request, userSetting.getSipUseSourceIpAsRemoteAddress());
    }

    /**
     * 记录处理开始日志
     *
     * @param registerFlag 注册标志
     * @param deviceIdentification     设备ID
     * @param addressInfo  地址信息
     */
    private void logProcessingStart(boolean registerFlag, String deviceIdentification, RemoteAddressInfo addressInfo) {
        String action = registerFlag ? "注册" : "注销";
        log.info("[设备{}请求] 设备: {}, 地址: {}:{}", action, deviceIdentification, addressInfo.getHost(), addressInfo.getPort());
    }

    /**
     * 处理设备注册核心逻辑
     *
     * @param request              SIP请求对象
     * @param response             SIP响应对象
     * @param registerFlag         注册标志
     * @param deviceIdentification 设备ID
     * @param addressInfo          地址信息
     */
    private void processDeviceRegistration(SIPRequest request, Response response, boolean registerFlag,
                                           String deviceIdentification, RemoteAddressInfo addressInfo) throws ParseException, NoSuchAlgorithmException, SipException {

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (Objects.nonNull(device)) {
            // 设备已存在场景的处理
            handleExistingDevice(request, response, registerFlag, device, addressInfo);
        } else {
            // 新设备注册场景处理
            handleNewDeviceRegistration(request, response, registerFlag, deviceIdentification, addressInfo);
        }
    }

    /**
     * 处理新设备自动注册逻辑
     *
     * @param request              SIP请求对象
     * @param response             SIP响应对象
     * @param registerFlag         注册标志(true表示注册，false表示注销)
     * @param deviceIdentification 设备唯一标识
     * @param addressInfo          网络地址信息
     */
    private void handleNewDeviceRegistration(SIPRequest request,
                                             Response response,
                                             boolean registerFlag,
                                             String deviceIdentification,
                                             RemoteAddressInfo addressInfo) throws ParseException, SipException, NoSuchAlgorithmException {
        VideoDeviceResultVO newDevice = buildNewDevice(request, deviceIdentification, addressInfo);

        // 注销逻辑
        if (!registerFlag) {
            handleNewDeviceLogout(newDevice);
        } else {
            // 注册逻辑
            // 进行密码验证
            verifyDevicePassword(request, response, newDevice);
            // 保存设备信息
            VideoDeviceSaveVO saveVO = BeanPlusUtil.toBeanIgnoreError(newDevice, VideoDeviceSaveVO.class);
            videoDeviceService.saveDeviceInfo(saveVO);

            // 写入 SIP 层注册特征（User-Agent / Contact）到 protocol_config，便于后续厂商识别 & 排障
            patchSipProtocolConfig(deviceIdentification, request);

            // 构建成功响应体，并回复给设备
            response = buildSuccessResponse(request);
            sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
            SipTransactionInfo sipTransactionInfo = new SipTransactionInfo((SIPResponse) response);
            sipEventPublisher.deviceInfoOnlineEventPublish(newDevice, sipTransactionInfo);
            // 新注册设备也发一次心跳事件，初始化 lastKeepaliveTime
            sipEventPublisher.deviceKeepaliveEventPublish(deviceIdentification, "REGISTER");
        }
    }

    /**
     * 从 REGISTER 请求头提取 User-Agent / Contact，按需 merge 到设备 {@code protocol_config}。
     * <p>
     * 字段语义：
     * <ul>
     *   <li>{@code sipUserAgent} - 设备厂商/固件特征串，可用于 DeviceAdapterFactory 指纹识别</li>
     *   <li>{@code sipContact} - 设备回调地址原文（含传输参数）</li>
     * </ul>
     * 本方法吞异常，失败不影响注册主流程。
     */
    private void patchSipProtocolConfig(String deviceIdentification, SIPRequest request) {
        try {
            String userAgent = extractHeaderValue(request, "User-Agent");
            String contact = extractHeaderValue(request, ContactHeader.NAME);
            if (StrUtil.isAllBlank(userAgent, contact)) {
                return;
            }
            VideoDeviceProtocolConfig patch = VideoDeviceProtocolConfig.builder()
                    .sipUserAgent(userAgent)
                    .sipContact(contact)
                    .build();
            videoDeviceService.patchProtocolConfig(deviceIdentification, patch);
        } catch (Exception e) {
            log.warn("[REGISTER] 写入 protocol_config 失败 deviceIdentification={} error={}",
                    deviceIdentification, e.getMessage());
        }
    }

    /** 读取 SIP 头 value 部分（去掉 "HeaderName:" 前缀），头缺失或 value 为空返回 null。 */
    private static String extractHeaderValue(SIPRequest request, String headerName) {
        Header header = request.getHeader(headerName);
        // JAIN-SIP 部分 Header 的 equals(null) 实现会 NPE，Hutool ObjectUtil.isNull 会触发 equals，故此处用 == null
        if (header == null) {
            return null;
        }
        // SIP 头 toString 形如 "User-Agent: SIP UAS V3.x"，用 StrUtil.subAfter 切掉第一个冒号前的部分
        String value = StrUtil.trimToNull(StrUtil.subAfter(header.toString(), ':', false));
        return value != null ? value : StrUtil.trimToNull(header.toString());
    }

    /**
     * 构建新设备实体
     */
    private VideoDeviceResultVO buildNewDevice(SIPRequest request,
                                                    String deviceIdentification,
                                                    RemoteAddressInfo addressInfo) {
        return VideoDeviceResultVO.builder()
                .accessProtocol("GB28181")
                .onlineStatus(true)
                .deviceName(deviceIdentification)
                .authSecret(TenantSipConfigHolder.get().getPassword())
                .mediaIdentification("auto")
                .deviceIdentification(deviceIdentification)
                .host(addressInfo.getHost())
                .port(addressInfo.getPort())
                .accessEndpoint(addressInfo.getHost() + StrPool.COLON + addressInfo.getPort())
                .localHost(request.getLocalAddress().getHostAddress())
                .transport(extractTransportProtocol(request).getValue())
                .expires(request.getExpires().getExpires())
                .registerTime(DateUtil.now())
                .build();
    }

    /**
     * 处理已存在设备注销逻辑
     *
     * @param device 设备信息对象
     */
    private void handleExistingDeviceLogout(VideoDeviceResultVO device) {
        try {
            // 注销只翻转 onlineStatus + registerTime，其他字段保留 DB 原值，避免覆盖用户 UI 维护配置；按 id 更新。
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setId(device.getId());
            updateVO.setOnlineStatus(false);
            updateVO.setRegisterTime(DateUtil.now());
            videoDeviceService.updateDeviceInfo(updateVO);
            device.setOnlineStatus(false);
            sipEventPublisher.deviceInfoOfflineEventPublish(device);
            log.info("设备注销完成: {}", device.getDeviceIdentification());
        } catch (Exception e) {
            log.error("设备注销过程中发生异常: {}", device.getDeviceIdentification(), e);
            throw new BizException("设备注销处理失败");
        }
    }

    /**
     * 处理新设备立即注销情况（注册请求中Expires=0）
     */
    private void handleNewDeviceLogout(VideoDeviceResultVO device) {
        log.warn("收到新设备的立即注销请求: {}", device.getDeviceIdentification());
        device.setOnlineStatus(false);
        device.setRegisterTime(DateUtil.now());
        // 保存设备信息
        VideoDeviceSaveVO saveVO = BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceSaveVO.class);
        videoDeviceService.saveDeviceInfo(saveVO);
        sipEventPublisher.deviceInfoOfflineEventPublish(device);
    }


    /**
     * 处理已存在设备的注册/注销
     *
     * @param request      请求
     * @param response     响应
     * @param registerFlag 注册标识(true:注册, false:注销)
     * @param device       设备信息
     * @param addressInfo  远程地址信息
     * @throws ParseException 异常
     * @throws SipException   Sip异常
     */
    private void handleExistingDevice(SIPRequest request, Response response, boolean registerFlag,
                                      @NonNull VideoDeviceResultVO device, RemoteAddressInfo addressInfo) throws ParseException, SipException, NoSuchAlgorithmException {

        // 注册
        if (registerFlag) {
            // 进行密码验证
            verifyDevicePassword(request, response, device);
            fullUpdateDeviceInfo(device, request, addressInfo);
            handleExistingDeviceLogin(request, response, device);
        } else {
            // 注销
            handleExistingDeviceLogout(device);
        }
    }


    private void handleExistingDeviceLogin(SIPRequest request, Response response, VideoDeviceResultVO device) throws ParseException, SipException {
        final boolean wasOffline = !Boolean.TRUE.equals(device.getOnlineStatus());
        device.setOnlineStatus(true);
        device.setRegisterTime(DateUtil.now());
        // 构建成功响应体，并回复给设备
        response = buildSuccessResponse(request);
        sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
        SipTransactionInfo sipTransactionInfo = new SipTransactionInfo((SIPResponse) response);
        log.info("设备：{}, 注册成功", device.getDeviceIdentification());

        // 触发完整上线流程（updateDeviceInfo + 上线事件 → DeviceInfo / Catalog 自动查询）的条件：
        //   1. wasOffline：常规的离线→在线翻转
        //   2. needFirstTimeSync：之前从未成功同步过 catalog / deviceinfo（例如 video-server 重启后
        //      老设备 REGISTER 上来时 wasOffline=false，但 protocol_config 里 CatalogSyncTime 为 null，
        //      通道表也是空的 —— 必须补一次，否则永远拿不到通道数据）
        final boolean needFirstTimeSync = needFirstTimeSync(device);
        if (wasOffline || needFirstTimeSync) {
            // 仅写 REGISTER 真正带过来 / 服务端自身决定的字段，其他用户 UI 维护字段保留 DB 原值；按 id 更新。
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setId(device.getId());
            updateVO.setOnlineStatus(true);
            updateVO.setRegisterTime(device.getRegisterTime());
            updateVO.setHost(device.getHost());
            updateVO.setPort(device.getPort());
            updateVO.setAccessEndpoint(device.getAccessEndpoint());
            updateVO.setLocalHost(device.getLocalHost());
            updateVO.setTransport(device.getTransport());
            updateVO.setExpires(device.getExpires());
            videoDeviceService.updateDeviceInfo(updateVO);
            sipEventPublisher.deviceInfoOnlineEventPublish(device, sipTransactionInfo);
            if (!wasOffline) {
                log.info("[首次同步补偿] 设备: {} 之前在线但未同步过 catalog/deviceInfo，触发一次上线事件",
                        device.getDeviceIdentification());
            }
        }

        // protocol_config 每次 REGISTER 都 patch（不受 wasOffline 限制）：
        //   - 服务重启后老设备首次 REGISTER 时，DB onlineStatus 往往是上次的 true，
        //     wasOffline 为 false，早先的"仅翻转时 patch"会丢掉这一次写入机会
        //   - patchProtocolConfig 内部是 merge，字段无变化时 update 一次的代价可以接受
        //   - User-Agent / Contact 设备重启或固件升级后可能换值，每次 REGISTER 都对齐最新
        patchSipProtocolConfig(device.getDeviceIdentification(), request);

        // 每次 REGISTER 都发心跳事件（不论之前在线/离线），刷新 lastKeepaliveTime
        sipEventPublisher.deviceKeepaliveEventPublish(device.getDeviceIdentification(), "REGISTER");
    }

    /**
     * 判断设备是否需要一次首次同步（DeviceInfo + Catalog）。
     * <p>只要 {@code protocol_config.catalogSyncTime} 为 null 或通道计数为 0，就认为没成功同步过，
     * 需要补触发一次上线事件让 {@code DeviceInfoOnlineEventListener} 调用 autoQuery。
     */
    private boolean needFirstTimeSync(VideoDeviceResultVO device) {
        if (device == null) {
            return false;
        }
        var cfg = device.getProtocolConfig();
        if (cfg == null || cfg.getCatalogSyncTime() == null) {
            return true;
        }
        // channelCount 为 null 或 0 视为从未成功拉过 catalog（新设备注册但 catalog 响应丢失等场景）
        Integer count = device.getChannelCount();
        return count == null || count == 0;
    }


    private void verifyDevicePassword(SIPRequest request, Response response, VideoDeviceResultVO deviceResultVO) throws NoSuchAlgorithmException, ParseException, SipException {
        AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        TenantSipConfig tenantConfig = TenantSipConfigHolder.get();
        String password = (deviceResultVO != null && StrUtil.isNotBlank(deviceResultVO.getAuthSecret())) ? deviceResultVO.getAuthSecret() : tenantConfig.getPassword();
        if (authHead == null && StrUtil.isNotBlank(password)) {
            // 回复401
            response = getMessageFactory().createResponse(Response.UNAUTHORIZED, request);
            new DigestServerAuthenticationHelper().generateChallenge(getHeaderFactory(), response, tenantConfig.getDomain());
            sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
            throw BizException.wrap("Invalid password, 401 unauthorized");
        }

        // 校验密码是否正确
        boolean passwordCorrect = new DigestServerAuthenticationHelper().doAuthenticatePlainTextPassword(request, password);
        if (!passwordCorrect) {
            // 回复403
            response = getMessageFactory().createResponse(Response.FORBIDDEN, request);
            response.setReasonPhrase("wrong password");
            sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
            throw BizException.wrap("Wrong password,密码/SIP服务器ID错误, 回复403");
        }
    }

    /**
     * 补充更新设备信息
     */
    private void fullUpdateDeviceInfo(VideoDeviceResultVO device, SIPRequest request,
                                      RemoteAddressInfo addressInfo) {

        device.setExpires(request.getExpires().getExpires())
                .setHost(addressInfo.getHost())
                .setPort(addressInfo.getPort())
                .setAccessEndpoint(addressInfo.getHost() + StrPool.COLON + addressInfo.getPort())
                .setLocalHost(request.getLocalAddress().getHostAddress())
                .setTransport(extractTransportProtocol(request).getValue())
                .setRegisterTime(DateUtil.now());
    }


    /**
     * 提取传输协议
     */
    private TransportProtocolEnum extractTransportProtocol(SIPRequest request) {
        return Optional.ofNullable((ViaHeader) request.getHeader(ViaHeader.NAME))
                .map(ViaHeader::getTransport)
                .flatMap(TransportProtocolEnum::fromValue)
                .orElse(TransportProtocolEnum.UDP);
    }

    /**
     * 构建注册成功的响应
     *
     * @param request 请求
     * @return {@link Response} 响应
     */
    private Response buildSuccessResponse(Request request) throws ParseException {
        // 携带授权头并且密码正确
        Response response = getMessageFactory().createResponse(Response.OK, request);
        // 添加date头
        SIPDateHeader dateHeader = new SIPDateHeader();
        // 使用自己修改的
        GbSipDate gbSipDate = new GbSipDate(Calendar.getInstance(Locale.ENGLISH).getTimeInMillis());
        dateHeader.setDate(gbSipDate);
        response.addHeader(dateHeader);

        // 添加Contact头
        response.addHeader(request.getHeader(ContactHeader.NAME));
        // 添加Expires头
        response.addHeader(request.getExpires());

        return response;
    }

}
