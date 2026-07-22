package com.mqttsnet.thinglinks.video.manager.group;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroup;

import java.util.List;

/**
 * 设备分组 Manager 接口。
 *
 * <p>封装设备分组的数据访问逻辑，为
 * {@link com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupService}
 * 提供底层数据操作支持。继承 {@link SuperManager} 获得通用持久化能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoDeviceGroup
 * @see SuperManager
 * @see com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupService
 */
public interface VideoDeviceGroupManager extends SuperManager<VideoDeviceGroup> {

    /**
     * 查询指定父分组下的直接子分组列表。
     *
     * @param parentId 父分组主键 ID，传 {@code null} 或 {@code 0L} 时查询顶级分组
     * @return 直接子分组的 {@link VideoDeviceGroup} 列表，若无子分组则返回空列表
     */
    List<VideoDeviceGroup> listByParentId(Long parentId);

    /**
     * 查询所有处于启用状态的分组。
     *
     * <p>返回全部启用的 {@link VideoDeviceGroup} 记录，
     * 通常用于构建完整的分组树。</p>
     *
     * @return 所有启用状态的 {@link VideoDeviceGroup} 列表，若无记录则返回空列表
     */
    List<VideoDeviceGroup> listAllEnabled();
}
