package com.mqttsnet.thinglinks.video.manager.group;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroupRelation;

import java.util.List;

/**
 * 设备分组关联 Manager 接口。
 *
 * <p>封装设备分组关联的数据访问逻辑，为
 * {@link com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupRelationService}
 * 提供底层数据操作支持。继承 {@link SuperManager} 获得通用持久化能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoDeviceGroupRelation
 * @see SuperManager
 * @see com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupRelationService
 */
public interface VideoDeviceGroupRelationManager extends SuperManager<VideoDeviceGroupRelation> {

    /**
     * 查询指定分组下的所有设备/通道关联记录。
     *
     * @param groupId 分组主键 ID，不能为 {@code null}
     * @return 该分组下的 {@link VideoDeviceGroupRelation} 列表，若无关联则返回空列表
     */
    List<VideoDeviceGroupRelation> listByGroupId(Long groupId);

    /**
     * 查询指定设备所关联的所有分组记录。
     *
     * @param deviceIdentification 设备国标编号（GB/T 28181 编码），不能为 {@code null}
     * @return 该设备关联的 {@link VideoDeviceGroupRelation} 列表，若无关联则返回空列表
     */
    List<VideoDeviceGroupRelation> listByDeviceIdentification(String deviceIdentification);

    /**
     * 删除指定分组下的所有关联记录。
     *
     * <p>通常在删除分组前调用，用于清理该分组下所有的
     * {@link VideoDeviceGroupRelation} 绑定关系。</p>
     *
     * @param groupId 分组主键 ID，不能为 {@code null}
     */
    void removeByGroupId(Long groupId);
}
