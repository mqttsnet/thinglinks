package com.mqttsnet.thinglinks.video.service.group;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroupRelation;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupRelationResultVO;

import java.util.List;

/**
 * 设备分组关联业务接口。
 *
 * <p>管理设备/通道与分组之间的多对多绑定关系，
 * 提供绑定、解绑及按维度查询等操作。
 * 继承 {@link SuperService} 获得通用 CRUD 能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoDeviceGroupRelation
 * @see VideoDeviceGroupService
 * @see SuperService
 */
public interface VideoDeviceGroupRelationService extends SuperService<Long, VideoDeviceGroupRelation> {

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
     * 将设备/通道绑定到指定分组。
     *
     * <p>创建一条 {@link VideoDeviceGroupRelation} 记录，
     * 建立设备通道与分组的关联关系。</p>
     *
     * @param groupId               目标分组主键 ID，不能为 {@code null}
     * @param deviceIdentification  设备国标编号（GB/T 28181 编码），不能为 {@code null}
     * @param channelIdentification 通道国标编号（GB/T 28181 编码），不能为 {@code null}
     */
    void bindDevice(Long groupId, String deviceIdentification, String channelIdentification);

    /**
     * 解除指定的设备/通道与分组的绑定关系。
     *
     * <p>根据关联记录主键 ID 删除对应的 {@link VideoDeviceGroupRelation}。</p>
     *
     * @param id 关联记录主键 ID，不能为 {@code null}
     */
    void unbindDevice(Long id);

    /**
     * 删除指定分组下的所有关联记录。
     *
     * <p>通常在删除分组前调用，用于清理该分组下所有的
     * {@link VideoDeviceGroupRelation} 绑定关系。</p>
     *
     * @param groupId 分组主键 ID，不能为 {@code null}
     * @see VideoDeviceGroupService
     */
    void removeByGroupId(Long groupId);

    /**
     * 查询指定分组下的所有关联记录（返回ResultVO）。
     */
    List<VideoDeviceGroupRelationResultVO> listResultVOByGroupId(Long groupId);

    /**
     * 查询指定设备关联的所有分组记录（返回ResultVO）。
     */
    List<VideoDeviceGroupRelationResultVO> listResultVOByDeviceIdentification(String deviceIdentification);
}
