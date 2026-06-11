package com.mqttsnet.thinglinks.ota.service.statemachine.event.handler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.broker.WebSocketBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandTypeEnum;
import com.mqttsnet.thinglinks.device.service.DeviceCommandService;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.enumeration.QosEnum;
import com.mqttsnet.thinglinks.ota.converter.OtaUpgradeCommandConverter;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeFileResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaTaskRecordCommandSendStatusEnum;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeRecordsService;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeRecordsResultVO;
import com.mqttsnet.thinglinks.ota.vo.update.OtaUpgradeRecordsUpdateVO;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandRequestParam;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * OTA任务执行处理器
 * <p>
 * 负责处理具体的设备升级逻辑
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/10/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtaTaskExecutionHandler {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyUserFacade;
    private final WebSocketBrokerOpenAnyUserFacade webSocketBrokerOpenAnyUserFacade;
    private final ProtocolMessageAdapter protocolMessageAdapter;
    private final OtaUpgradeRecordsService otaUpgradeRecordsService;
    private final DeviceCommandService deviceCommandService;

    /**
     * 处理设备升级
     *
     * @param deviceIdentification 设备标识
     * @param upgradeTask          升级任务
     * @param upgradePackage       升级包
     * @param fileInfoMap          文件信息映射
     */
    public void processDeviceUpgrade(String deviceIdentification, OtaUpgradeTasksResultDTO upgradeTask, OtaUpgradesResultDTO upgradePackage, Map<Long, OtaUpgradeFileResultDTO> fileInfoMap) {
        try {
            log.info("开始执行设备OTA升级指令 - 设备标识: {}, 任务ID: {}", deviceIdentification, upgradeTask.getTaskName());
            // 构建OTA命令请求
            TopoOtaCommandRequestParam commandRequest = OtaUpgradeCommandConverter.buildOtaCommandRequestParam(deviceIdentification, upgradeTask, upgradePackage, fileInfoMap);
            // 发送升级命令
            boolean sendSuccess = sendOtaUpgradeCommand(deviceIdentification, commandRequest, upgradeTask);
            // 更新升级记录的命令下发状态和相关信息
            updateUpgradeRecordCommandStatus(upgradeTask.getId(), deviceIdentification, commandRequest, sendSuccess ? OtaTaskRecordCommandSendStatusEnum.SENT_SUCCESS : OtaTaskRecordCommandSendStatusEnum.SENT_FAILED);
            log.info("设备OTA升级指令执行完成 - 设备标识: {}, 任务ID: {}, 结果: {}", deviceIdentification, upgradeTask.getTaskName(), sendSuccess ? "成功" : "失败");
        } catch (Exception e) {
            log.error("设备OTA升级指令执行异常 - 设备标识: {}, 任务ID: {}", deviceIdentification, upgradeTask.getTaskName(), e);
        }
    }


    /**
     * 发送OTA升级命令
     *
     * @param deviceIdentification 设备标识
     * @param commandRequest       命令请求
     * @param upgradeTask          升级任务
     * @return 是否发送成功
     */
    private boolean sendOtaUpgradeCommand(String deviceIdentification,
                                          TopoOtaCommandRequestParam commandRequest,
                                          OtaUpgradeTasksResultDTO upgradeTask) {
        try {
            // 获取设备缓存信息
            Optional<DeviceCacheVO> deviceCacheOpt = linkCacheDataHelper.getDeviceCacheVO(deviceIdentification);
            if (deviceCacheOpt.isEmpty()) {
                log.warn("设备档案信息不存在...设备标识: {}", deviceIdentification);
                return false;
            }

            DeviceCacheVO deviceCache = deviceCacheOpt.get();

            // 构建加密信息
            EncryptionDetailsDTO encryptionDetails = EncryptionDetailsDTO.builder()
                    .signKey(deviceCache.getSignKey())
                    .encryptKey(deviceCache.getEncryptKey())
                    .encryptVector(deviceCache.getEncryptVector())
                    .cipherFlag(deviceCache.getEncryptMethod())
                    .build();

            // 构建命令消息
            String commandMessageJson = JSON.toJSONString(commandRequest);
            ProtocolDataMessageDTO protocolMessage = protocolMessageAdapter.buildResponse(commandMessageJson, encryptionDetails);

            // 发送消息
            String messageContent = JSON.toJSONString(protocolMessage);
            String responseTopic = generateResponseTopic(deviceCache.getDeviceSdkVersion(), deviceIdentification);

            R result;
            // 产品 protocolType 不再内嵌在 deviceCacheVO ── 走 LinkCacheDataHelper 共享解析入口,
            // 两者都拿不到默认走 MQTT。
            ProtocolTypeEnum protocolType = linkCacheDataHelper
                    .resolveProtocolType(deviceCache.getProductIdentification(),
                            deviceCache.getBoundProductVersionNo())
                    .flatMap(ProtocolTypeEnum::fromValue)
                    .orElse(ProtocolTypeEnum.MQTT);

            if (ProtocolTypeEnum.WEBSOCKET.equals(protocolType)) {
                result = sendWebSocketMessage(responseTopic, deviceCache.getClientId(), messageContent);
            } else {
                result = sendMessage(responseTopic, QosEnum.EXACTLY_ONCE.getValue().toString(), messageContent);
            }

            // 保存设备命令记录
            saveDeviceCommand(deviceIdentification, result, upgradeTask);

            return result.getIsSuccess();

        } catch (Exception e) {
            log.error("发送OTA升级指令失败 - 设备标识 {}", deviceIdentification, e);
            return false;
        }
    }

    /**
     * 生成响应主题
     *
     * @param version  设备SDK版本
     * @param deviceId 设备标识
     * @return 响应主题
     */
    private String generateResponseTopic(String version, String deviceId) {
        return String.format("/%s/devices/%s/topo/otaCommand", version, deviceId);
    }

    /**
     * 发送MQTT消息
     *
     * @param topic   主题
     * @param qos     服务质量
     * @param message 消息内容
     * @return 发送结果
     */
    private R sendMessage(String topic, String qos, String message) {
        PublishMessageRequestVO request = new PublishMessageRequestVO()
                .setReqId(Long.valueOf(SnowflakeIdUtil.nextId()))
                .setTenantId(ContextUtil.getTenantIdStr())
                .setTopic(topic)
                .setQos(qos)
                .setClientType("web")
                .setPayload(message)
                .setExpirySeconds("3600");

        return mqttBrokerOpenAnyUserFacade.sendMessage(request);
    }

    /**
     * 发送WebSocket消息
     *
     * @param topic    主题
     * @param clientId 客户端ID
     * @param message  消息内容
     * @return 发送结果
     */
    private R sendWebSocketMessage(String topic, String clientId, String message) {
        PublishWebSocketMessageRequestVO request = new PublishWebSocketMessageRequestVO()
                .setReqId(Long.valueOf(SnowflakeIdUtil.nextId()))
                .setTenantId(ContextUtil.getTenantIdStr())
                .setTopic(topic)
                .setClientId(clientId)
                .setClientType("web")
                .setPayload(message);

        return webSocketBrokerOpenAnyUserFacade.sendMessage(request);
    }

    /**
     * 保存设备命令记录
     *
     * @param deviceIdentification 设备标识
     * @param response             响应结果
     * @param upgradeTask          升级任务
     */
    private void saveDeviceCommand(String deviceIdentification, R response, OtaUpgradeTasksResultDTO upgradeTask) {
        try {
            DeviceCommandSaveVO commandSaveVO = new DeviceCommandSaveVO()
                    .setDeviceIdentification(deviceIdentification)
                    .setCommandType(DeviceCommandTypeEnum.OTA_COMMAND_RESPONSE.getValue())
                    .setStatus(response.getIsSuccess() ?
                            DeviceCommandStatusEnum.SUCCESS.getValue() :
                            DeviceCommandStatusEnum.FAILURE.getValue())
                    .setContent(response.getIsSuccess() ?
                            response.getData().toString() : response.getMsg())
                    .setRemark("OTA升级任务ID: [" + upgradeTask.getId() + "],OTA升级任务名称: [" + upgradeTask.getTaskName() + "]");

            deviceCommandService.saveDeviceCommand(commandSaveVO);
        } catch (Exception e) {
            log.error("保存设备命令记录失败 - 设备标识: {}", deviceIdentification, e);
        }
    }

    /**
     * 更新升级记录的命令下发状态和相关信息
     *
     * @param taskId                      任务ID
     * @param deviceIdentification        设备标识
     * @param commandRequest              命令请求
     * @param recordCommandSendStatusEnum 命令发送状态枚举
     */
    private void updateUpgradeRecordCommandStatus(Long taskId, String deviceIdentification, TopoOtaCommandRequestParam commandRequest, OtaTaskRecordCommandSendStatusEnum recordCommandSendStatusEnum) {
        try {
            Optional<OtaUpgradeRecordsResultVO> recordOptional = otaUpgradeRecordsService.getByTaskIdAndDeviceIdentification(taskId, deviceIdentification);
            if (recordOptional.isEmpty()) {
                log.warn("更新升级记录命令状态 - 设备标识: {}, 任务ID: {}, 升级记录不存在", deviceIdentification, taskId);
                return;
            }
            OtaUpgradeRecordsResultVO otaUpgradeRecordsResultVO = recordOptional.get();
            OtaUpgradeRecordsUpdateVO otaUpgradeRecordsUpdateVO = OtaUpgradeRecordsUpdateVO.builder()
                    .id(otaUpgradeRecordsResultVO.getId())
                    .commandSendStatus(recordCommandSendStatusEnum.getValue())
                    .commandContent(JSON.toJSONString(commandRequest))
                    .lastCommandSendTime(LocalDateTime.now())
                    .build();
            otaUpgradeRecordsService.updateOtaUpgradeRecord(otaUpgradeRecordsUpdateVO);
            log.info("更新升级记录命令状态完成 - 设备标识: {}, 任务ID: {}, 状态: {}", deviceIdentification, taskId, recordCommandSendStatusEnum.getDesc());
        } catch (Exception e) {
            log.error("更新升级记录命令状态失败 - 设备标识: {}, 任务ID: {}", deviceIdentification, taskId, e);
        }
    }

}