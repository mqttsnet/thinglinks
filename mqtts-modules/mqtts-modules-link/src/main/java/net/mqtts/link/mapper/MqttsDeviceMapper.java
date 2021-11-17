package net.mqtts.link.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import net.mqtts.link.domain.MqttsDevice;

/**
 * 设备管理Mapper接口
 *
 * @author mqtts
 * @date 2021-10-22
 */
public interface MqttsDeviceMapper {
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
     * 删除设备管理
     *
     * @param id 设备管理主键
     * @return 结果
     */
    public int deleteMqttsDeviceById(Long id);

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMqttsDeviceByIds(Long[] ids);

    MqttsDevice findOneByClientIdAndUserNameAndPassword(@Param("clientId") String clientId, @Param("userName") String userName, @Param("password") String password);

    MqttsDevice findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@Param("clientId") String clientId, @Param("userName") String userName, @Param("password") String password, @Param("deviceStatus") String deviceStatus, @Param("protocolType") String protocolType);

    /**
     * 更新设备在线状态
     *
     * @param updatedConnectStatus 设备状态值
     * @param clientId             客户端ID
     * @return 返回结果
     */
    int updateConnectStatusByClientId(@Param("updatedConnectStatus") String updatedConnectStatus, @Param("clientId") String clientId);

}
