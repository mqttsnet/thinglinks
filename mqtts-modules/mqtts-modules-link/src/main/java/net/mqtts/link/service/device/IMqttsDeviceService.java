package net.mqtts.link.service.device;

import java.util.List;

import net.mqtts.link.domain.device.MqttsDevice;

/**
 * 设备管理Service接口
 *
 * @author mqtts
 * @date 2021-10-22
 */
public interface IMqttsDeviceService<updatateDeviceStacus> {
    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    public MqttsDevice selectMqttsDeviceById(Long id);

    /**
     * 查询设备管理列表
     *
     * @param mqttsDevice 设备管理
     * @return 设备管理集合
     */
    public List<MqttsDevice> selectMqttsDeviceList(MqttsDevice mqttsDevice);

    /**
     * 新增设备管理
     *
     * @param mqttsDevice 设备管理
     * @return 结果
     */
    public int insertMqttsDevice(MqttsDevice mqttsDevice);

    /**
     * 修改设备管理
     *
     * @param mqttsDevice 设备管理
     * @return 结果
     */
    public int updateMqttsDevice(MqttsDevice mqttsDevice);

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的设备管理主键集合
     * @return 结果
     */
    public int deleteMqttsDeviceByIds(Long[] ids);

    /**
     * 删除设备管理信息
     *
     * @param id 设备管理主键
     * @return 结果
     */
    public int deleteMqttsDeviceById(Long id);

    MqttsDevice findOneByClientIdAndUserNameAndPassword(String clientId, String userName, String password);


    MqttsDevice findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId, String userName, String password, String deviceStatus, String protocolType);

    /**
     * 更新设备在线状态
     *
     * @param updatedConnectStatus 设备状态
     * @param clientId             客户端ID
     * @return
     */
    int updateConnectStatusByClientId(String updatedConnectStatus, String clientId);

}
