package com.mqttsnet.thinglinks.video.entity.gateway;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * <p>
 * 实体类
 * 网关协议映射表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-05-15 17:00:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_gateway_mapping", autoResultMap = true)
public class VideoGatewayMapping extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 源协议
     */
    @TableField(value = "src_protocol", condition = LIKE)
    private String srcProtocol;
    /**
     * 源设备标识
     */
    @TableField(value = "src_device_identification", condition = LIKE)
    private String srcDeviceIdentification;
    /**
     * 源通道标识
     */
    @TableField(value = "src_channel_identification", condition = LIKE)
    private String srcChannelIdentification;
    /**
     * 国标设备编号
     */
    @TableField(value = "gb_device_id", condition = LIKE)
    private String gbDeviceId;
    /**
     * 国标通道编号
     */
    @TableField(value = "gb_channel_id", condition = LIKE)
    private String gbChannelId;
    /**
     * 国标平台ID
     */
    @TableField(value = "gb_platform_id", condition = EQUAL)
    private Long gbPlatformId;
    /**
     * 是否启用
     */
    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;
    /**
     * 自动推流
     */
    @TableField(value = "auto_push", condition = EQUAL)
    private Boolean autoPush;
    /**
     * 映射配置(JSON)
     */
    @TableField(value = "mapping_config", typeHandler = FastjsonTypeHandler.class)
    private JSONObject mappingConfig;
    /**
     * 注册状态
     */
    @TableField(value = "register_status", condition = EQUAL)
    private Boolean registerStatus;
    /**
     * 最后注册时间
     */
    @TableField(value = "last_register_time", condition = LIKE)
    private String lastRegisterTime;
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
