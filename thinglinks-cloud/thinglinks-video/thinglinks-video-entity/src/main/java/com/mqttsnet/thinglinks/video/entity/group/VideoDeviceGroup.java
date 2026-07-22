package com.mqttsnet.thinglinks.video.entity.group;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Description:
 * 设备分组实体。
 * 对应数据库表 {@code video_device_group}，用于分屏展示时的层级 Tree 结构，
 * 支持拖拽播放时关联设备/通道。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code groupType} — 分组类型（0=自定义，1=行政区划，2=业务分组）</li>
 *   <li>{@code groupPath} — 层级路径（如 /1/2/3），便于快速查询子孙节点</li>
 *   <li>{@code groupLevel} — 层级深度（从 1 开始）</li>
 *   <li>{@code parentId} — 上级分组ID，顶层为 null</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see VideoDeviceGroupRelation
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_device_group", autoResultMap = true)
public class VideoDeviceGroup extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "group_name", condition = LIKE)
    private String groupName;

    @TableField(value = "parent_id", condition = EQUAL)
    private Long parentId;

    @TableField(value = "group_type", condition = EQUAL)
    private Integer groupType;

    @TableField(value = "sort_order", condition = EQUAL)
    private Integer sortOrder;

    @TableField(value = "group_path", condition = LIKE)
    private String groupPath;

    @TableField(value = "group_level", condition = EQUAL)
    private Integer groupLevel;

    @TableField(value = "icon", condition = LIKE)
    private String icon;

    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;

    @TableField(value = "extend_params", condition = EQUAL)
    private String extendParams;

    @TableField(value = "remark", condition = LIKE)
    private String remark;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
