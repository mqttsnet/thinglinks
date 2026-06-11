package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.dto.common.RemoteAddressInfo;
import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.GbSipDate;
import com.mqttsnet.thinglinks.video.empowerment.device.TransportProtocolEnum;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.auth.DigestServerAuthenticationHelper;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceInfoService;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoDeviceInfoSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceInfoUpdateVO;
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
    private SipConfig sipConfig;

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private VideoDeviceInfoService deviceInfoService;

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
     * @param deviceId     设备ID
     * @param addressInfo  地址信息
     */
    private void logProcessingStart(boolean registerFlag, String deviceId, RemoteAddressInfo addressInfo) {
        String action = registerFlag ? "注册" : "注销";
        log.info("[设备{}请求] 设备: {}, 地址: {}:{}", action, deviceId, addressInfo.getIp(), addressInfo.getPort());
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

        VideoDeviceInfoResultDTO device = deviceInfoService.getVideoDeviceInfoResultDTO(deviceIdentification);
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
     * @param request              SIP响应对象
     * @param registerFlag         注册标志(true表示注册，false表示注销)
     * @param deviceIdentification 设备唯一标识
     * @param addressInfo          网络地址信息
     */
    private void handleNewDeviceRegistration(SIPRequest request,
                                             Response response,
                                             boolean registerFlag,
                                             String deviceIdentification,
                                             RemoteAddressInfo addressInfo) throws ParseException, SipException, NoSuchAlgorithmException {
        VideoDeviceInfoResultDTO newDevice = buildNewDevice(request, deviceIdentification, addressInfo);

        // 注销逻辑
        if (!registerFlag) {
            handleNewDeviceLogout(newDevice);
        } else {
            // 注册逻辑
            // 进行密码验证
            verifyDevicePassword(request, response, newDevice);
            // 保存设备信息
            VideoDeviceInfoSaveVO saveVO = BeanPlusUtil.toBeanIgnoreError(newDevice, VideoDeviceInfoSaveVO.class);
            deviceInfoService.saveDeviceInfo(saveVO);

            // 构建成功响应体，并回复给设备
            response = buildSuccessResponse(request);
            sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
            SipTransactionInfo sipTransactionInfo = new SipTransactionInfo((SIPResponse) response);
            sipEventPublisher.deviceInfoOnlineEventPublish(newDevice, sipTransactionInfo);
        }
    }

    /**
     * 构建新设备实体
     */
    private VideoDeviceInfoResultDTO buildNewDevice(SIPRequest request,
                                                    String deviceIdentification,
                                                    RemoteAddressInfo addressInfo) {
        return VideoDeviceInfoResultDTO.builder()
                .deviceName("TCP-PASSIVE")
                .charset("GB2312")
                .geoCoordSys("WGS84")
                .mediaIdentification("auto")
                .password(sipConfig.getPassword())
                .deviceIdentification(deviceIdentification)
                .ip(addressInfo.getIp())
                .port(addressInfo.getPort())
                .hostAddress(addressInfo.getIp() + StrPool.COLON + addressInfo.getPort())
                .localIp(request.getLocalAddress().getHostAddress())
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
    private void handleExistingDeviceLogout(VideoDeviceInfoResultDTO device) {
        try {
            device.setOnlineStatus(false);
            device.setRegisterTime(DateUtil.now());
            // 更新设备信息
            VideoDeviceInfoUpdateVO updateVO = BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceInfoUpdateVO.class);
            deviceInfoService.updateDeviceInfo(updateVO);
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
    private void handleNewDeviceLogout(VideoDeviceInfoResultDTO device) {
        log.warn("收到新设备的立即注销请求: {}", device.getDeviceIdentification());
        device.setOnlineStatus(false);
        device.setRegisterTime(DateUtil.now());
        // 保存设备信息
        VideoDeviceInfoSaveVO saveVO = BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceInfoSaveVO.class);
        deviceInfoService.saveDeviceInfo(saveVO);
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
                                      @NonNull VideoDeviceInfoResultDTO device, RemoteAddressInfo addressInfo) throws ParseException, SipException, NoSuchAlgorithmException {

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


    private void handleExistingDeviceLogin(SIPRequest request, Response response, VideoDeviceInfoResultDTO device) throws ParseException, SipException {
        device.setOnlineStatus(true);
        device.setRegisterTime(DateUtil.now());
        // 构建成功响应体，并回复给设备
        response = buildSuccessResponse(request);
        sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
        SipTransactionInfo sipTransactionInfo = new SipTransactionInfo((SIPResponse) response);
        log.info("设备：{}, 注册成功", device.getDeviceIdentification());
        // 更新设备信息
        VideoDeviceInfoUpdateVO updateVO = BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceInfoUpdateVO.class);
        deviceInfoService.updateDeviceInfo(updateVO);
        sipEventPublisher.deviceInfoOnlineEventPublish(device, sipTransactionInfo);
    }


    private void verifyDevicePassword(SIPRequest request, Response response, VideoDeviceInfoResultDTO deviceInfoResultDTO) throws NoSuchAlgorithmException, ParseException, SipException {
        AuthorizationHeader authHead = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        String password = (deviceInfoResultDTO != null && StrUtil.isNotBlank(deviceInfoResultDTO.getPassword())) ? deviceInfoResultDTO.getPassword() : sipConfig.getPassword();
        if (authHead == null && StrUtil.isNotBlank(password)) {
            // 回复401
            response = getMessageFactory().createResponse(Response.UNAUTHORIZED, request);
            new DigestServerAuthenticationHelper().generateChallenge(getHeaderFactory(), response, sipConfig.getDomain());
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
    private void fullUpdateDeviceInfo(VideoDeviceInfoResultDTO device, SIPRequest request,
                                      RemoteAddressInfo addressInfo) {

        device.toBuilder()
                .expires(request.getExpires().getExpires())
                .ip(addressInfo.getIp())
                .port(addressInfo.getPort())
                .hostAddress(addressInfo.getIp() + StrPool.COLON + addressInfo.getPort())
                .localIp(request.getLocalAddress().getHostAddress())
                .transport(extractTransportProtocol(request).getValue())
                .registerTime(DateUtil.now())
                .build();
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
