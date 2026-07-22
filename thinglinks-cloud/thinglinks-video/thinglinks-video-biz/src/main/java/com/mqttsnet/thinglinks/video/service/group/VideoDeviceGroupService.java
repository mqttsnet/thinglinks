package com.mqttsnet.thinglinks.video.service.group;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroup;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupResultVO;

import java.util.List;

/**
 * 设备分组业务接口。
 *
 * <p>提供设备分组的层级查询与状态筛选等业务操作，
 * 分组支持树形结构（parent-child）。
 * 继承 {@link SuperService} 获得通用 CRUD 能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoDeviceGroup
 * @see VideoDeviceGroupRelationService
 * @see SuperService
 */
public interface VideoDeviceGroupService extends SuperService<Long, VideoDeviceGroup> {

    /**
     * 查询指定父分组下的直接子分组列表。
     *
     * <p>用于构建分组树的下一层级节点。</p>
     *
     * @param parentId 父分组主键 ID，传 {@code null} 或 {@code 0L} 时查询顶级分组
     * @return 直接子分组的 {@link VideoDeviceGroup} 列表，若无子分组则返回空列表
     */
    List<VideoDeviceGroup> listByParentId(Long parentId);

    /**
     * 查询所有处于启用状态的分组。
     *
     * <p>返回全部启用的 {@link VideoDeviceGroup} 记录，
     * 通常用于前端构建完整的分组树（Tree）。</p>
     *
     * @return 所有启用状态的 {@link VideoDeviceGroup} 列表，若无记录则返回空列表
     */
    List<VideoDeviceGroup> listAllEnabled();

    /**
     * 构建分组树形结构。
     *
     * <p>查询所有启用的分组，按 parentId 组装为嵌套树形结构返回。</p>
     *
     * @return 树形结构的 {@link VideoDeviceGroupResultVO} 列表（仅包含顶层节点，子节点在 children 中）
     */
    List<VideoDeviceGroupResultVO> buildTree();

    /**
     * 保存分组并自动计算 groupPath 和 groupLevel。
     *
     * @param entity 分组实体
     */
    void saveGroup(VideoDeviceGroup entity);

    /**
     * 更新分组并自动重新计算 groupPath 和 groupLevel。
     *
     * @param entity 分组实体
     */
    void updateGroup(VideoDeviceGroup entity);
}
