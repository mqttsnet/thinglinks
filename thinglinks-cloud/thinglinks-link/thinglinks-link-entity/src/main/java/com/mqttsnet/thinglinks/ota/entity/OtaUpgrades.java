package com.mqttsnet.thinglinks.ota.entity;

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
 * <p>
 * 实体类
 * OTA升级包
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:36:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName(value = "ota_upgrades", autoResultMap = true)
public class OtaUpgrades extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 包名称
     */
    @TableField(value = "package_name", condition = LIKE)
    private String packageName;
    /**
     * 升级包类型(0:软件包、1:固件包)
     */
    @TableField(value = "package_type", condition = EQUAL)
    private Integer packageType;
    /**
     * 产品标识
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;
    /**
     * 产品版本序号
     */
    @TableField(value = "product_version_no", condition = EQUAL)
    private String productVersionNo;
    /**
     * 升级包版本号
     */
    @TableField(value = "version", condition = LIKE)
    private String version;
    /**
     * 升级包的位置
     */
    @TableField(value = "file_location", condition = LIKE)
    private String fileLocation;

    /**
     * 签名方法
     */
    @TableField(value = "sign_method", condition = EQUAL)
    private Integer signMethod;
    /**
     * 状态
     */
    @TableField(value = "status", condition = EQUAL)
    private Integer status;
    /**
     * 升级包功能描述
     */
    @TableField(value = "description", condition = LIKE)
    private String description;
    /**
     * 自定义信息
     */
    @TableField(value = "custom_info", condition = LIKE)
    private String customInfo;
    /**
     * 描述
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
