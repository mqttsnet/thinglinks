package com.mqttsnet.thinglinks.link.service.device.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.api.domain.enumeration.QosEnum;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceCommandStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceCommandTypeEnum;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.CommandIssueRequestParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.PublishMessageRequestParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceCommandMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceCommandService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final CacheDataHelper cacheDataHelper;

    private final ProtocolMessageAdapter protocolMessageAdapter;

    @Resource
    private RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceCommandMapper deviceCommandMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceCommandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceCommand record) {
        return deviceCommandMapper.insert(record);
    }

    @Override
    public int insertSelective(DeviceCommand record) {
        return deviceCommandMapper.insertSelective(record);
    }

    @Override
    public DeviceCommand selectByPrimaryKey(Long id) {
        return deviceCommandMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceCommand record) {
        return deviceCommandMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceCommand record) {
        return deviceCommandMapper.updateByPrimaryKey(record);
    }

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
            this.insertSelective(deviceCommand);
            return deviceCommand;
        }).orElseThrow(() -> new IllegalArgumentException("Invalid DeviceCommandSaveVO input"));
    }

    /**
     * Fetch a list of device command result VOs.
     *
     * @param query the query parameters
     * @return a list of DeviceCommandResultVOs
     */
    /*@Override
    public List<DeviceCommandResultVO> getDeviceCommandResultVOList(DeviceCommandPageQuery query) {
        return superManager.getDeviceCommandResultVOList(query);
    }*/

    /**
     * Processes both serial and parallel device command requests.
     *
     * @param commandWrapper wrapper containing both serial and parallel command requests
     */
    @Override
    public void processDeviceCommands(DeviceCommandWrapperParam commandWrapper) {
        // Process serial commands
        Optional.ofNullable(commandWrapper.getSerial()).orElseGet(Collections::emptyList).forEach(this::processSingleCommand);

        // Process parallel commands concurrently
        Optional.ofNullable(commandWrapper.getParallel()).orElseGet(Collections::emptyList).parallelStream().forEach(this::processSingleCommand);
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
     */
    private Boolean checkDeviceCommandSaveVO(DeviceCommandSaveVO deviceCommandSaveVO) {

        if (StrUtil.isBlank(deviceCommandSaveVO.getDeviceIdentification())) {
            log.warn("Device identification is null");
            return false;
        }

        return true;
    }

    /**
     * Processes a single command request for a device or all devices.
     *
     * @param commandRequest The command request parameters.
     */
    @Async("linkAsync-command")
    protected void processSingleCommand(CommandIssueRequestParam commandRequest) {
        String productIdentification = commandRequest.getProductIdentification();
        String deviceIdentification = commandRequest.getDeviceIdentification();

        List<DeviceResultVO> deviceResultVOList;

        if (Constants.ALL.equals(deviceIdentification)) {
            // 获取所有设备的结果列表
            deviceResultVOList = getAllDeviceResultVOs(productIdentification);
        } else {
            // 获取单个设备的结果列表
            deviceResultVOList = getSingleDeviceResultVO(deviceIdentification);
        }

        // Process each device command.
        deviceResultVOList.forEach(deviceResultVO -> {
            // Build and send the command message.
            R response = buildAndSendMessage(deviceResultVO, commandRequest);

            DeviceCommandSaveVO deviceCommandSaveVO = createDeviceCommandSaveVO(deviceResultVO, response);

            // Save the command for record keeping.
            saveDeviceCommand(deviceCommandSaveVO);
        });
    }

    /**
     * Retrieves a list of all device result value objects for a specific product.
     *
     * @param productIdentification a product identification string used to find devices.
     * @return A list of DeviceResultVO objects, each representing a device linked to the product.
     */
    private List<DeviceResultVO> getAllDeviceResultVOs(String productIdentification) {
        List<Device> allByProductIdentification = deviceService.findAllByProductIdentification(productIdentification);
        return BeanPlusUtil.copyToList(allByProductIdentification, DeviceResultVO.class);
    }

    /**
     * Retrieves the device result value object for a single device.
     *
     * @param deviceIdentification The device's unique identifier.
     * @return A list containing the single DeviceResultVO.
     */
    private List<DeviceResultVO> getSingleDeviceResultVO(String deviceIdentification) {
        // 获取设备缓存对象
        DeviceCacheVO deviceCacheVO = cacheDataHelper.getDeviceCacheVO(deviceIdentification);

        // 若设备缓存对象为null，则直接返回空列表
        if (deviceCacheVO == null) {
            return Collections.emptyList();
        }

        // 转换设备缓存对象到设备结果VO
        DeviceResultVO deviceResultVO = BeanPlusUtil.toBeanIgnoreError(deviceCacheVO, DeviceResultVO.class);

        // 尝试获取产品缓存VO并转换，然后设置到设备结果VO中
        Optional.ofNullable(deviceCacheVO.getProductCacheVO())
                .map(productCacheVO -> BeanPlusUtil.toBeanIgnoreError(productCacheVO, ProductResultVO.class))
                .ifPresent(deviceResultVO::setProductResultVO);

        // 返回包含一个元素的列表
        return Collections.singletonList(deviceResultVO);
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
        if (response.isSuccess()) {
            deviceCommandSaveVO.setStatus(DeviceCommandStatusEnum.SUCCESS.getValue());
            deviceCommandSaveVO.setContent(response.getData().toString());
        } else {
            deviceCommandSaveVO.setStatus(DeviceCommandStatusEnum.FAILURE.getValue());
            deviceCommandSaveVO.setContent(response.getMsg());
        }
        return deviceCommandSaveVO;
    }


    /**
     * Builds and sends a command message to the device.
     *
     * @param deviceResultVO The device result value object.
     * @param commandRequest The command issue request parameters.
     * @return The response from the MQTT broker.
     */
    private R buildAndSendMessage(DeviceResultVO deviceResultVO, CommandIssueRequestParam commandRequest) {
        // Build the encryption details if all necessary information is present
        Optional<EncryptionDetailsDTO> encryptionDetailsOpt = Optional.ofNullable(deviceResultVO).map(drv -> EncryptionDetailsDTO.builder().signKey(drv.getSignKey()).encryptKey(drv.getEncryptKey()).encryptVector(drv.getEncryptVector()).cipherFlag(Integer.valueOf(drv.getEncryptMethod())).build());

        // Construct the command message JSON string
        String commandMessageJson = Optional.ofNullable(commandRequest).map(cr -> buildCommandMessage(deviceResultVO, cr)).map(JSONUtil::toJsonStr).orElse("{}"); // Fallback to an empty JSON object if commandRequest is null

        // Try to build the response using the encryption details
        Optional<ProtocolDataMessageDTO> handleResultOpt = encryptionDetailsOpt.flatMap(encryptionDetails -> {
            try {
                // Attempt to build the response with encryption details and return as an Optional
                return Optional.ofNullable(protocolMessageAdapter.buildResponse(commandMessageJson, encryptionDetails));
            } catch (Exception e) {
                // Log and handle any exceptions that occur during response building
                log.error("Failed to build the response due to an exception.", e);
                return Optional.empty();
            }
        });

        // Prepare the MQTT message content with a default response if handleResult is absent
        String messageContent = handleResultOpt.map(JSONUtil::toJsonStr).orElseGet(() -> {
            // Log the absence of handleResult and use a default empty message
            log.warn("No response object was constructed; using default empty message.");
            return "{}";
        });

        // Generate the response topic string
        String responseTopic = generateResponseTopic(deviceResultVO.getDeviceSdkVersion(), deviceResultVO.getDeviceIdentification());

        // Send the constructed message to the MQTT broker and return the result
        return sendMessage(responseTopic, QosEnum.EXACTLY_ONCE.getValue().toString(), messageContent, deviceResultVO.getAppId());
    }


    /**
     * Generates a response topic string using the provided version and device ID.
     *
     * @param version  The version number.
     * @param deviceId The unique identifier of the device.
     * @return A complete response topic string.
     */
    protected String generateResponseTopic(String version, String deviceId) {
        return String.format("/%s/devices/%s%s", version, deviceId, "/command");
    }


    /**
     * Build command message.
     *
     * @param deviceResultVO device result VO
     * @param commandRequest command request
     * @return command message
     */
    private String buildCommandMessage(DeviceResultVO deviceResultVO, CommandIssueRequestParam commandRequest) {
        // Adapter logic to build the command message should be placed here.
        commandRequest.setDeviceIdentification(deviceResultVO.getDeviceIdentification());
        return JSONUtil.toJsonStr(commandRequest);
    }

    /**
     * Sends a message to the specified MQTT topic with the provided QoS and payload.
     *
     * @param topic    The MQTT topic to publish the message to.
     * @param qos      The quality of service for the message.
     * @param message  The payload of the message.
     * @param tenantId The tenant ID.
     * @return The response from the MQTT brokelr.
     */
    @Override
    public R sendMessage(String topic, String qos, String message, String tenantId) {
        PublishMessageRequestVO publishMessageRequestVO = new PublishMessageRequestVO();
        publishMessageRequestVO.setReqId(Long.valueOf(SnowflakeIdUtil.nextId()));
        publishMessageRequestVO.setTenantId(tenantId);
        publishMessageRequestVO.setTopic(topic);
        publishMessageRequestVO.setQos(qos);
        publishMessageRequestVO.setClientType("web");
        publishMessageRequestVO.setPayload(message);
        publishMessageRequestVO.setExpirySeconds("3600");

        R response = remoteMqttBrokerOpenApi.sendMessage(publishMessageRequestVO);
        if (!response.isSuccess()) {
            log.warn("Failed to send MQTT message: " + response.getMsg());
            //  throw BizException.wrap("Failed to send MQTT message: " + response.getMsg());
        }
        return response;
    }

    @Override
    public R sendCustomMessage(PublishMessageRequestParam publishMessageRequestParam) {
        PublishMessageRequestVO publishMessageRequestVO = new PublishMessageRequestVO();
        publishMessageRequestVO.setReqId(Long.valueOf(SnowflakeIdUtil.nextId()));
        publishMessageRequestVO.setTenantId(publishMessageRequestParam.getTenantId());
        publishMessageRequestVO.setTopic(publishMessageRequestParam.getTopic());
        publishMessageRequestVO.setQos(publishMessageRequestParam.getQos());
        publishMessageRequestVO.setClientType("web");
        publishMessageRequestVO.setPayload(publishMessageRequestParam.getPayload());
        publishMessageRequestVO.setExpirySeconds(publishMessageRequestParam.getExpirySeconds());
        publishMessageRequestVO.setClientMetadata(publishMessageRequestParam.getMetadata());

        try {
            R response = remoteMqttBrokerOpenApi.sendMessage(publishMessageRequestVO);
            if (!response.isSuccess()) {
                log.warn("Failed to send MQTT message: " + response.getMsg());
                //  throw BizException.wrap("Failed to send MQTT message: " + response.getMsg());
            }
            return response;
        } catch (Exception e) {
            log.warn("Failed to send MQTT message: " + e.getMessage());
            return R.fail("Failed to send MQTT message: " + e.getMessage());
        }
    }


}


