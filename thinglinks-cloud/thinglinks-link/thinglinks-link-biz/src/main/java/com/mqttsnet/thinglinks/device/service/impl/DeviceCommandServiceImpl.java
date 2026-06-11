package com.mqttsnet.thinglinks.device.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.broker.WebSocketBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.broker.DeviceDownlinkFacade;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceNodeTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceStatusEnum;
import com.mqttsnet.thinglinks.device.manager.DeviceCommandManager;
import com.mqttsnet.thinglinks.device.service.DeviceCommandService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceCommandPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.enumeration.QosEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.CommandIssueRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.protocol.vo.param.PublishMqttMessageRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.param.PublishWebSocketMessageRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceCommandResultVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * 设备命令下发及响应表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-20 17:27:25
 * @create [2023-10-20 17:27:25] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceCommandServiceImpl extends SuperServiceImpl<DeviceCommandManager, Long, DeviceCommand> implements DeviceCommandService {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyUserFacade;
    private final WebSocketBrokerOpenAnyUserFacade webSocketBrokerOpenAnyUserFacade;
    private final DeviceDownlinkFacade deviceDownlinkFacade;
    private final DeviceService deviceService;
    private final ProtocolMessageAdapter protocolMessageAdapter;


    /**
     * Saves a device command to the database after validation.
     *
     * @param deviceCommandSaveVO The device command data transfer object.
     * @return The saved DeviceCommand entity.
     * @throws IllegalArgumentException if input validation fails.
     */
    @Override
    public DeviceCommand saveDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO) {
        // Validate the input, build the DeviceCommand object, and save it to the database.
        return Optional.of(deviceCommandSaveVO).filter(this::checkDeviceCommandSaveVO).map(this::buildDeviceCommand).map(deviceCommand -> {
            deviceCommand.setCommandIdentification(SnowflakeIdUtil.nextId());
            return superManager.save(deviceCommand) ? deviceCommand : null;
        }).orElseThrow(() -> new IllegalArgumentException("Invalid DeviceCommandSaveVO input"));
    }

    /**
     * Fetch a list of device command result VOs.
     *
     * @param query the query parameters
     * @return a list of DeviceCommandResultVOs
     */
    @Override
    public List<DeviceCommandResultVO> getDeviceCommandResultVOList(DeviceCommandPageQuery query) {
        return BeanPlusUtil.toBeanList(superManager.getDeviceCommandResultVOList(query), DeviceCommandResultVO.class);
    }

    /**
     * Processes both serial and parallel device command requests.
     *
     * @param commandWrapper wrapper containing both serial and parallel command requests
     * @return list of device command results
     */
    @Override
    public List<DeviceCommandResultVO> processDeviceCommands(DeviceCommandWrapperParam commandWrapper) {
        List<DeviceCommandResultVO> results = new ArrayList<>();

        // Process serial commands
        Optional.ofNullable(commandWrapper.getSerial()).orElseGet(Collections::emptyList)
                .stream()
                .map(this::processSingleCommand)
                .forEach(results::addAll);

        // Process parallel commands（不使用 parallelStream，避免 @DS 数据源上下文在 ForkJoinPool 线程中丢失）
        Optional.ofNullable(commandWrapper.getParallel()).orElseGet(Collections::emptyList)
                .stream()
                .map(this::processSingleCommand)
                .forEach(results::addAll);

        return results;
    }

    @Override
    public void sendMqttCustomMessage(PublishMqttMessageRequestParam publishMqttMessageRequestParam) {
        log.info("发送MQTT消息 - Topic: {}, 租户: {}, 负载类型: {}, 是否为Base64: {}",
                publishMqttMessageRequestParam.getTopic(),
                publishMqttMessageRequestParam.getTenantId(),
                publishMqttMessageRequestParam.getPayload() != null ? publishMqttMessageRequestParam.getPayload().getClass().getSimpleName() : "null",
                publishMqttMessageRequestParam.isPayloadBase64());

        PublishMessageRequestVO publishMessageRequestVO = PublishMessageRequestVO.builder()
                .reqId(Long.valueOf(SnowflakeIdUtil.nextId()))
                .tenantId(publishMqttMessageRequestParam.getTenantId())
                .topic(publishMqttMessageRequestParam.getTopic())
                .qos(publishMqttMessageRequestParam.getQos())
                .clientType("web")
                .payload(publishMqttMessageRequestParam.getPayloadAsSmartString())
                .forceBase64Decode(publishMqttMessageRequestParam.isPayloadBase64())
                .expirySeconds(publishMqttMessageRequestParam.getExpirySeconds())
                .build();

        log.info("发送MQTT消息 - 最终负载长度: {}, 强制解码: {}", publishMessageRequestVO.getPayload() != null ?
                publishMessageRequestVO.getPayload().length() : 0, publishMessageRequestVO.getForceBase64Decode());

        long startTime = System.currentTimeMillis();

        // 执行发送
        R response = mqttBrokerOpenAnyUserFacade.sendMessage(publishMessageRequestVO);

        long costTime = System.currentTimeMillis() - startTime;

        // 处理响应结果
        if (!response.getIsSuccess()) {
            log.error("【MQTT消息发送失败】耗时: {}ms, 错误信息: {}", costTime, response.getMsg());
            throw BizException.wrap("MQTT message sending failed. Please try again! Time consumed: {}ms", costTime);
        } else {
            log.info("【MQTT消息发送成功】<<< 耗时: {}ms, 响应信息: {}", costTime, response.getMsg());
        }
    }


    @Override
    public void sendWebSocketCustomMessage(PublishWebSocketMessageRequestParam publishWebSocketMessageRequestParam) {
        PublishWebSocketMessageRequestVO publishMessageRequestVO = new PublishWebSocketMessageRequestVO();
        publishMessageRequestVO.setReqId(Long.valueOf(SnowflakeIdUtil.nextId()));
        publishMessageRequestVO.setTenantId(publishWebSocketMessageRequestParam.getTenantId());
        publishMessageRequestVO.setTopic(publishWebSocketMessageRequestParam.getTopic());
        publishMessageRequestVO.setClientId(publishWebSocketMessageRequestParam.getClientId());
        publishMessageRequestVO.setClientType("web");
        publishMessageRequestVO.setPayload(publishWebSocketMessageRequestParam.getPayload());

        R response = webSocketBrokerOpenAnyUserFacade.sendMessage(publishMessageRequestVO);
        if (!response.getIsSuccess()) {
            log.warn("Failed to send WebSocket message: {}", response.getMsg());
        }
    }

    /**
     * Build DeviceCommand from DeviceCommandSaveVO.
     *
     * @param deviceCommandSaveVO input VO object
     * @return DeviceCommand object
     */
    private DeviceCommand buildDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO) {
        return BeanPlusUtil.toBeanIgnoreError(deviceCommandSaveVO, DeviceCommand.class);
    }

    /**
     * Validate the DeviceCommandSaveVO object.
     *
     * @param deviceCommandSaveVO the input VO to validate
     * @return true if validation passes
     */
    private Boolean checkDeviceCommandSaveVO(DeviceCommandSaveVO deviceCommandSaveVO) {
        ArgumentAssert.notNull(deviceCommandSaveVO, "deviceCommandSaveVO cannot be null");
        ArgumentAssert.notBlank(deviceCommandSaveVO.getDeviceIdentification(), "deviceIdentification cannot be null");
        // Add other validation checks as required...
        return true;
    }

    /**
     * Processes a single command request for a device or all devices.
     *
     * @param commandRequest The command request parameters.
     * @return List of device command results
     */
    protected List<DeviceCommandResultVO> processSingleCommand(CommandIssueRequestParam commandRequest) {
        List<DeviceCommandResultVO> results = new ArrayList<>();

        // Retrieve the list of devices based on the identification provided.
        String productIdentification = commandRequest.getProductIdentification();
        String deviceIdentification = commandRequest.getDeviceIdentification();

        List<DeviceResultVO> deviceResultVOList;

        if (BizConstant.ALL.equals(deviceIdentification)) {
            // 获取所有设备的结果列表
            deviceResultVOList = getAllDeviceResultVOs(productIdentification);
        } else {
            // 获取单个设备的结果列表
            deviceResultVOList = getSingleDeviceResultVO(deviceIdentification);
        }
        // Process each device command.
        deviceResultVOList.forEach(deviceResultVO -> {
            // Build and send the command message.
            R response = buildAndSendMessage(deviceResultVO.getDeviceIdentification(), commandRequest);

            DeviceCommandSaveVO deviceCommandSaveVO = createDeviceCommandSaveVO(deviceResultVO, response);

            // Save the command for record keeping.
            DeviceCommand savedCommand = saveDeviceCommand(deviceCommandSaveVO);

            // Convert to result VO and add to results
            DeviceCommandResultVO resultVO = BeanPlusUtil.toBean(savedCommand, DeviceCommandResultVO.class);
            results.add(resultVO);
        });

        return results;
    }

    /**
     * Retrieves a list of all device result value objects for a specific product.
     *
     * @param productIdentification a product identification string used to find devices.
     * @return A list of DeviceResultVO objects, each representing a device linked to the product.
     */
    private List<DeviceResultVO> getAllDeviceResultVOs(String productIdentification) {
        DevicePageQuery devicePageQuery = new DevicePageQuery();
        devicePageQuery.setProductIdentification(productIdentification);
        devicePageQuery.setDeviceStatus(DeviceStatusEnum.ACTIVATED.getValue());
        return deviceService.getDeviceResultVOList(devicePageQuery);
    }

    /**
     * Retrieves the device result value object for a single device.
     *
     * @param deviceIdentification The device's unique identifier.
     * @return A list containing the single DeviceResultVO.
     */
    private List<DeviceResultVO> getSingleDeviceResultVO(String deviceIdentification) {
        Optional<DeviceCacheVO> deviceCacheVOOptional = linkCacheDataHelper.getDeviceCacheVO(deviceIdentification);
        return deviceCacheVOOptional.map(deviceCacheVO -> Collections.singletonList(BeanPlusUtil.toBeanIgnoreError(deviceCacheVO, DeviceResultVO.class))).orElse(Collections.emptyList());
    }

    /**
     * Creates a DeviceCommandSaveVO object based on the command request and response.
     *
     * @param deviceResultVO The device result value object.
     * @param response       The response from the MQTT broker.
     * @return A populated DeviceCommandSaveVO object.
     */
    private DeviceCommandSaveVO createDeviceCommandSaveVO(DeviceResultVO deviceResultVO, R response) {
        DeviceCommandSaveVO deviceCommandSaveVO = new DeviceCommandSaveVO();
        deviceCommandSaveVO.setDeviceIdentification(deviceResultVO.getDeviceIdentification());
        deviceCommandSaveVO.setCommandType(DeviceCommandTypeEnum.COMMAND_ISSUE.getValue());
        if (response.getIsSuccess()) {
            deviceCommandSaveVO.setStatus(DeviceCommandStatusEnum.SUCCESS.getValue());
            deviceCommandSaveVO.setContent(response.getData().toString());
        } else {
            deviceCommandSaveVO.setStatus(DeviceCommandStatusEnum.FAILURE.getValue());
            deviceCommandSaveVO.setContent(response.getMsg());
        }
        deviceCommandSaveVO.setRemark(response.toString());
        return deviceCommandSaveVO;
    }


    /**
     * Builds and sends a command message to the device.
     *
     * @param deviceIdentification The deviceIdentification value object.
     * @param commandRequest       The command issue request parameters.
     * @return The response from the MQTT Or WebSocket broker.
     */
    private R buildAndSendMessage(String deviceIdentification, CommandIssueRequestParam commandRequest) {
        // Retrieve the device cache VO from the cache
        Optional<DeviceCacheVO> deviceCacheVOOptional = linkCacheDataHelper.getDeviceCacheVO(deviceIdentification);
        ArgumentAssert.isTrue(deviceCacheVOOptional.isPresent(), "Device does not exist!");
        DeviceCacheVO deviceCacheVO = deviceCacheVOOptional.get();
        // Build the encryption details if all necessary information is present
        Optional<EncryptionDetailsDTO> encryptionDetailsOpt = Optional.of(deviceCacheVO).map(drv -> EncryptionDetailsDTO.builder().mId(Long.valueOf(SnowflakeIdUtil.nextId())).signKey(drv.getSignKey()).encryptKey(drv.getEncryptKey()).encryptVector(drv.getEncryptVector()).cipherFlag(drv.getEncryptMethod()).build());

        // 构造命令业务体 JSON 串。buildCommandMessage 内部已 JSON.toJSONString 一次,
        // 这里不能再 .map(JSON::toJSONString)(会把 JSON 串当对象再序列化 → dataBody 多重转义)。
        // 单次序列化的 JSON 串交给 buildResponse,明文时其内部会还原成对象塞进 dataBody。
        String commandMessageJson = Optional.ofNullable(commandRequest).map(cr -> buildCommandMessage(deviceCacheVO, cr)).orElse("{}");

        // Try to build the response using the encryption details
        Optional<ProtocolDataMessageDTO> handleResultOpt = encryptionDetailsOpt.flatMap(encryptionDetails -> {
            log.info("处理报文加密....commandMessageJson:{},encryptionDetails:{}", commandMessageJson, JSON.toJSONString(encryptionDetails));
            try {
                // Attempt to build the response with encryption details and return as an Optional
                return Optional.ofNullable(protocolMessageAdapter.buildResponse(commandMessageJson, encryptionDetails));
            } catch (Exception e) {
                // Log and handle any exceptions that occur during response building
                log.error("Failed to build the response due to an exception....commandMessageJson:{},encryptionDetails:{}", commandMessageJson, JSON.toJSONString(encryptionDetails), e);
                return Optional.empty();
            }
        });

        // Prepare the MQTT message content with a default response if handleResult is absent
        String messageContent = handleResultOpt.map(JSON::toJSONString).orElseGet(() -> {
            // Log the absence of handleResult and use a default empty message
            log.warn("No response object was constructed; using default empty message.");
            return "{}";
        });

        // Generate the response topic string
        String responseTopic = generateResponseTopic(deviceCacheVO);

        // 按产品协议类型分流下行,收敛到共享派发器;协议解析不出由派发器兜底 MQTT。
        // protocolType 取值同 ProtocolTypeEnum.getValue():MQTT 走 topic,WebSocket 走 clientId。
        String protocolType = linkCacheDataHelper
                .resolveProtocolType(deviceCacheVO.getProductIdentification(),
                        deviceCacheVO.getBoundProductVersionNo())
                .orElse(null);
        return deviceDownlinkFacade.dispatch(DownlinkCommand.builder()
                .protocolType(protocolType)
                .tenantId(String.valueOf(ContextUtil.getTenantId()))
                .clientId(deviceCacheVO.getClientId())
                .deviceIdentification(deviceCacheVO.getDeviceIdentification())
                .topic(responseTopic)
                .qos(QosEnum.EXACTLY_ONCE.getValue().toString())
                .payload(messageContent)
                .build());
    }


    /**
     * Generates a response topic string using the provided version and device ID.
     *
     * @param deviceCacheVO The device result value object.
     * @return A complete response topic string.
     */
    protected String generateResponseTopic(DeviceCacheVO deviceCacheVO) {
        // Determine the device node type using Optional and the enum's fromValue method
        DeviceNodeTypeEnum deviceNodeTypeEnum = Optional.ofNullable(deviceCacheVO.getNodeType())
                .flatMap(DeviceNodeTypeEnum::fromValue)
                .orElse(DeviceNodeTypeEnum.ORDINARY);

        // Get the SDK version, defaulting to "defaultSdkVersion" if not present
        String sdkVersion = Optional.ofNullable(deviceCacheVO.getDeviceSdkVersion()).orElse("defaultSdkVersion");

        // Determine the device ID based on the node type
        String deviceId;
        if (DeviceNodeTypeEnum.SUBDEVICE.equals(deviceNodeTypeEnum)) {
            // Use gatewayId if the device is a subdevice or gateway
            deviceId = Optional.ofNullable(deviceCacheVO.getGatewayId()).orElse("defaultGatewayId");
        } else {
            // Use deviceIdentification for ordinary devices
            deviceId = Optional.ofNullable(deviceCacheVO.getDeviceIdentification()).orElse("defaultDeviceIdentification");
        }

        // Construct the response topic string
        return String.format("/%s/devices/%s%s", sdkVersion, deviceId, "/command");
    }


    /**
     * Build command message.
     *
     * @param deviceCacheVO  device result VO
     * @param commandRequest command request
     * @return command message
     */
    private String buildCommandMessage(DeviceCacheVO deviceCacheVO, CommandIssueRequestParam commandRequest) {
        // Adapter logic to build the command message should be placed here.
        commandRequest.setDeviceIdentification(deviceCacheVO.getDeviceIdentification());
        return JSON.toJSONString(commandRequest);
    }

}


