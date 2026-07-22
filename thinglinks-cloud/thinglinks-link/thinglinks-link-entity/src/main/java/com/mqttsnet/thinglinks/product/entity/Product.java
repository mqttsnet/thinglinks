package com.mqttsnet.thinglinks.product.entity;

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

import java.io.Serial;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;


/**
 * 产品模型实体。
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "product", autoResultMap = true)
public class Product extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 模板ID
     */
    @TableField(value = "template_id", condition = EQUAL)
    private Long templateId;
    /**
     * 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
     */
    @TableField(value = "product_name", condition = LIKE)
    private String productName;
    /**
     * 产品标识
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;
    /**
     * 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
     */
    @TableField(value = "product_type", condition = EQUAL)
    private Integer productType;
    /**
     * 厂商ID:支持英文大小写，数字，下划线和中划线
     */
    @TableField(value = "manufacturer_id", condition = LIKE)
    private String manufacturerId;
    /**
     * 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
     */
    @TableField(value = "manufacturer_name", condition = LIKE)
    private String manufacturerName;
    /**
     * 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
     */
    @TableField(value = "model", condition = LIKE)
    private String model;
    /**
     * 数据格式，默认为JSON无需修改。
     */
    @TableField(value = "data_format", condition = LIKE)
    private String dataFormat;
    /**
     * 设备类型:支持英文大小写、数字、下划线和中划线
     */
    @TableField(value = "device_type", condition = LIKE)
    private String deviceType;
    /**
     * 设备接入平台的协议类型，默认为MQTT无需修改。
     */
    @TableField(value = "protocol_type", condition = LIKE)
    private String protocolType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @TableField(value = "product_status", condition = EQUAL)
    private Integer productStatus;
    /**
     * 产品当前激活的版本序号(系统发布时生成的快照标识,16 位短雪花字符串)。
     */
    @TableField(value = "active_version_no", condition = EQUAL)
    private String activeVersionNo;
    /**
     * 灰度期稳定版本序号(仅灰度态有值,晋升 / 回滚后清空),用于灰度回退定位。
     */
    @TableField(value = "previous_full_version_no", condition = EQUAL)
    private String previousFullVersionNo;
    /**
     * 图标
     */
    @TableField(value = "icon", condition = LIKE)
    private String icon;
    /**
     * 产品描述
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
