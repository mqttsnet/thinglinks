package com.mqttsnet.thinglinks.device.entity;

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

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;


/**
 * <p>
 * 实体类
 * 设备动作数据
 * </p>
 *
 * @author mqttsnet
 * @date 2023-06-10 16:38:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "device_action", autoResultMap = true)
public class DeviceAction extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备标识
     */
    @TableField(value = "device_identification", condition = EQUAL)
    private String deviceIdentification;
    /**
     * 动作类型
     */
    @TableField(value = "action_type", condition = EQUAL)
    private String actionType;
    /**
     * 内容信息
     */
    @TableField(value = "message", condition = LIKE)
    private String message;
    /**
     * 状态
     */
    @TableField(value = "status", condition = EQUAL)
    private Integer status;
    /**
     * 备注
     */
    @TableField(value = "remark", condition = LIKE)
    private String remark;
    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;

}
