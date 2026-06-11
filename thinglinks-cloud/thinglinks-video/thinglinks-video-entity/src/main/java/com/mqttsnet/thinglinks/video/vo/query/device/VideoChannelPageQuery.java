package com.mqttsnet.thinglinks.video.vo.query.device;

import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 表单查询条件VO
 * 视频通道表
 * </p>
 *
 * @author mqttsnet
 * @date 2026-03-31 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "VideoChannelPageQuery", description = "视频通道")
public class VideoChannelPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

    /**
     * 所属设备标识
     */
    @Schema(description = "所属设备标识")
    private String deviceIdentification;
    /**
     * 通道标识
     */
    @Schema(description = "通道标识")
    private String channelIdentification;
    /**
     * 逻辑通道号
     */
    @Schema(description = "逻辑通道号")
    private Integer channelNo;
    /**
     * 通道类型
     */
    @Schema(description = "通道类型")
    private Integer channelType;
    /**
     * 通道名称
     */
    @Schema(description = "通道名称")
    private String channelName;
    /**
     * 流标识
     */
    @Schema(description = "流标识")
    private String streamIdentification;
    /**
     * 流类型
     */
    @Schema(description = "流类型")
    private String streamType;
    /**
     * 厂商
     */
    @Schema(description = "厂商")
    private String manufacturer;
    /**
     * 型号
     */
    @Schema(description = "型号")
    private String model;
    /**
     * 在线状态
     */
    @Schema(description = "在线状态")
    private Boolean onlineStatus;
    /**
     * 通道地址(IP/域名)
     */
    @Schema(description = "通道地址(IP/域名)")
    private String host;
    /**
     * 端口
     */
    @Schema(description = "端口")
    private Integer port;
    /**
     * 设备口令
     */
    @Schema(description = "设备口令")
    private String password;
    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;
    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;
    /**
     * 安装地址
     */
    @Schema(description = "安装地址")
    private String fullAddress;
    /**
     * 省级编码
     */
    @Schema(description = "省级编码")
    private String provinceCode;
    /**
     * 市级编码
     */
    @Schema(description = "市级编码")
    private String cityCode;
    /**
     * 行政区划编码
     */
    @Schema(description = "行政区划编码")
    private String regionCode;
    /**
     * 支持音频
     */
    @Schema(description = "支持音频")
    private Boolean hasAudio;
    /**
     * 云台类型
     */
    @Schema(description = "云台类型")
    private Integer ptzType;
    /**
     * 支持云台控制
     */
    @Schema(description = "支持云台控制")
    private Boolean ptzCapability;
    /**
     * 支持对讲
     */
    @Schema(description = "支持对讲")
    private Boolean talkCapability;
    /**
     * 保密属性
     */
    @Schema(description = "保密属性")
    private Integer secrecy;
    /**
     * 通道专属配置
     */
    @Schema(description = "通道专属配置")
    private VideoChannelConfig channelConfig;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
