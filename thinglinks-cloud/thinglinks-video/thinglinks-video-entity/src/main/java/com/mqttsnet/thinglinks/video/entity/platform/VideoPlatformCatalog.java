package com.mqttsnet.thinglinks.video.entity.platform;

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
 * 级联平台目录实体。
 * 自定义虚拟目录，用于组织共享给上级平台的通道。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_platform_catalog", autoResultMap = true)
public class VideoPlatformCatalog extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "platform_id", condition = EQUAL)
    private Long platformId;

    @TableField(value = "name", condition = LIKE)
    private String name;

    @TableField(value = "gb_id", condition = LIKE)
    private String gbId;

    @TableField(value = "parent_id", condition = EQUAL)
    private Long parentId;

    @TableField(value = "catalog_type", condition = EQUAL)
    private Integer catalogType;

    @TableField(value = "civil_code", condition = LIKE)
    private String civilCode;

    @TableField(value = "sort_order", condition = EQUAL)
    private Integer sortOrder;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
