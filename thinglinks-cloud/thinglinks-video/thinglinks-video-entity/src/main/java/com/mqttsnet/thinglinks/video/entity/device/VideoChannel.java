package com.mqttsnet.thinglinks.video.entity.device;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
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
 * 统一通道表
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
@TableName(value = "video_channel", autoResultMap = true)
public class VideoChannel extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属设备标识
     */
    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;
    /**
     * 通道标识
     */
    @TableField(value = "channel_identification", condition = LIKE)
    private String channelIdentification;
    /**
     * 逻辑通道号
     */
    @TableField(value = "channel_no", condition = EQUAL)
    private Integer channelNo;
    /**
     * 通道类型
     */
    @TableField(value = "channel_type", condition = EQUAL)
    private Integer channelType;
    /**
     * 通道名称
     */
    @TableField(value = "channel_name", condition = LIKE)
    private String channelName;
    /**
     * 流标识
     */
    @TableField(value = "stream_identification", condition = LIKE)
    private String streamIdentification;
    /**
     * 流类型
     */
    @TableField(value = "stream_type", condition = LIKE)
    private String streamType;
    /**
     * 厂商
     */
    @TableField(value = "manufacturer", condition = LIKE)
    private String manufacturer;
    /**
     * 型号
     */
    @TableField(value = "model", condition = LIKE)
    private String model;
    /**
     * 在线状态
     */
    @TableField(value = "online_status", condition = EQUAL)
    private Boolean onlineStatus;
    /**
     * 通道地址(IP/域名)
     */
    @TableField(value = "host", condition = LIKE)
    private String host;
    /**
     * 端口
     */
    @TableField(value = "port", condition = EQUAL)
    private Integer port;
    /**
     * 设备口令
     */
    @TableField(value = "password", condition = LIKE)
    private String password;
    /**
     * 经度
     */
    @TableField(value = "longitude", condition = EQUAL)
    private BigDecimal longitude;
    /**
     * 纬度
     */
    @TableField(value = "latitude", condition = EQUAL)
    private BigDecimal latitude;
    /**
     * 安装地址
     */
    @TableField(value = "full_address", condition = LIKE)
    private String fullAddress;
    /**
     * 省级编码
     */
    @TableField(value = "province_code", condition = LIKE)
    private String provinceCode;
    /**
     * 市级编码
     */
    @TableField(value = "city_code", condition = LIKE)
    private String cityCode;
    /**
     * 行政区划编码
     */
    @TableField(value = "region_code", condition = LIKE)
    private String regionCode;
    /**
     * 支持音频
     */
    @TableField(value = "has_audio", condition = EQUAL)
    private Boolean hasAudio;
    /**
     * 云台类型
     */
    @TableField(value = "ptz_type", condition = EQUAL)
    private Integer ptzType;
    /**
     * 支持云台控制
     */
    @TableField(value = "ptz_capability", condition = EQUAL)
    private Boolean ptzCapability;
    /**
     * 支持对讲
     */
    @TableField(value = "talk_capability", condition = EQUAL)
    private Boolean talkCapability;
    /**
     * 保密属性
     */
    @TableField(value = "secrecy", condition = EQUAL)
    private Integer secrecy;
    /**
     * 通道专属配置（类型安全视图，落库为 JSON 字符串）。
     * <p>前端可读字段（{@code info} 节点由 GB28181 Catalog 全权维护）。业务自定义配置
     * 请加到 {@link VideoChannelConfig} 其他字段，{@code CatalogChannelParser} 只覆盖 info 节点。
     */
    @TableField(value = "channel_config", typeHandler = Fastjson2TypeHandler.class)
    private VideoChannelConfig channelConfig;
    /**
     * 扩展参数（GB28181 协议兜底字段的 JSON 字符串）。
     * <p>类型保持 {@link String} 以兼容 SQL {@code LIKE} 查询与系统内其他 entity 的 extend_params 约定；
     * 业务代码操作时请用 {@link com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelExtendParams}
     * 的 {@code fromJson/toJsonString} 做视图转换，获得字段级类型安全。
     */
    @TableField(value = "extend_params", condition = LIKE)
    private String extendParams;
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
