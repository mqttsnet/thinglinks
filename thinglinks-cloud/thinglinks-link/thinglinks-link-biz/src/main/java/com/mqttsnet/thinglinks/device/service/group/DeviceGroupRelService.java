package com.mqttsnet.thinglinks.device.service.group;

import java.util.Collection;
import java.util.List;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.device.entity.group.DeviceGroupRel;


/**
 * <p>
 * 业务接口
 * 设备分组关系表
 * </p>
 *
 * @author mqttsnet
 * @since 2025-06-23 14:06:46
 */
public interface DeviceGroupRelService extends SuperService<Long, DeviceGroupRel> {

    /**
     * 根据分组ID删除设备分组关系
     *
     * @param groupIdList 分组ID列表
     */
    void removeByGroupIds(Collection<Long> groupIdList);


    /**
     * 根据分组ID查询设备标识列表
     *
     * @param groupIdList 分组ID列表
     * @return {@link List<String>} 设备标识列表
     */
    List<String> getDeviceIdentificationsByGroupIds(Collection<Long> groupIdList);

    /**
     * 根据设备标识删除设备分组关系。
     * <p>设备删除时由 {@link com.mqttsnet.thinglinks.device.event.DeviceDeletedEvent} 监听器调用，
     * 同步清理本表对该设备的所有引用，避免出现"设备已删除但分组仍持有指针"的孤儿数据。
     *
     * @param deviceIdentification 设备标识，空值或空白串安全跳过
     */
    void removeByDeviceIdentification(String deviceIdentification);
}


