package com.mqttsnet.thinglinks.link.service.device;


import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.PublishMessageRequestParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.save.DeviceCommandSaveVO;


/**
 * <p>
 * 业务接口
 * 设备命令下发及响应表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-20 17:27:25
 * @create [2023-10-20 17:27:25] [mqttsnet]
 */
public interface DeviceCommandService {

    int deleteByPrimaryKey(Long id);

    int insert(DeviceCommand record);

    int insertSelective(DeviceCommand record);

    DeviceCommand selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceCommand record);

    int updateByPrimaryKey(DeviceCommand record);


    /**
     * Save device command data.
     *
     * @param deviceCommandSaveVO device command data
     * @return DeviceCommand saved device command data
     */
    DeviceCommand saveDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO);

    /**
     * Fetch a list of device command result VOs.
     *
     * @param query the query parameters
     * @return a list of DeviceCommandResultVOs
     */
//    List<DeviceCommandResultVO> getDeviceCommandResultVOList(DeviceCommandPageQuery query);


    /**
     * Processes both serial and parallel device command requests.
     *
     * @param commandWrapper wrapper containing both serial and parallel command requests
     */
    void processDeviceCommands(DeviceCommandWrapperParam commandWrapper);

    /**
     * Send a message to a topic.
     *
     * @param topic The topic to send the message to.
     * @param qos The quality of service to use.
     * @param message The message to send.
     * @param tenantId The tenant ID.
     * @return The result of the message send operation.
     */
    R sendMessage(String topic, String qos, String message, String tenantId);

    /**
     * Send a custom message to a specified MQTT topic with the provided details.
     *
     * @param publishMessageRequestParam The message details.
     * @return The result of the message sending operation.
     */
    R sendCustomMessage(PublishMessageRequestParam publishMessageRequestParam);
}


