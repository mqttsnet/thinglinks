package com.mqttsnet.thinglinks.video.vo.update.device;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * 表单修改方法VO
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
@Schema(title = "VideoChannelUpdateVO", description = "视频通道")
public class VideoChannelUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    @NotNull(message = "请填写唯一标识符", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 所属设备标识
     */
    @Schema(description = "所属设备标识")
    @NotEmpty(message = "请填写所属设备标识")
    @Size(max = 255, message = "所属设备标识长度不能超过{max}")
    private String deviceIdentification;
    /**
     * 通道标识
     */
    @Schema(description = "通道标识")
    @NotEmpty(message = "请填写通道标识")
    @Size(max = 255, message = "通道标识长度不能超过{max}")
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
    @NotNull(message = "请填写通道类型")
    private Integer channelType;
    /**
     * 通道名称
     */
    @Schema(description = "通道名称")
    @Size(max = 255, message = "通道名称长度不能超过{max}")
    private String channelName;
    /**
     * 流标识
     */
    @Schema(description = "流标识")
    @Size(max = 255, message = "流标识长度不能超过{max}")
    private String streamIdentification;
    /**
     * 流类型
     */
    @Schema(description = "流类型")
    @Size(max = 50, message = "流类型长度不能超过{max}")
    private String streamType;
    /**
     * 厂商
     */
    @Schema(description = "厂商")
    @Size(max = 255, message = "厂商长度不能超过{max}")
    private String manufacturer;
    /**
     * 型号
     */
    @Schema(description = "型号")
    @Size(max = 255, message = "型号长度不能超过{max}")
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
    @Size(max = 50, message = "通道地址长度不能超过{max}")
    private String host;
    /**
     * 端口
     */
    @Schema(description = "端口")
    private Integer port;
    /**
     * 设备口令
     */
    @Schema(description = "设备口令（仅写入；不传或空白时保留原值）", accessMode = Schema.AccessMode.WRITE_ONLY)
    @Size(max = 255, message = "设备口令长度不能超过{max}")
    @ToString.Exclude
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
    @Size(max = 500, message = "安装地址长度不能超过{max}")
    private String fullAddress;
    /**
     * 省级编码
     */
    @Schema(description = "省级编码")
    @Size(max = 50, message = "省级编码长度不能超过{max}")
    private String provinceCode;
    /**
     * 市级编码
     */
    @Schema(description = "市级编码")
    @Size(max = 50, message = "市级编码长度不能超过{max}")
    private String cityCode;
    /**
     * 行政区划编码
     */
    @Schema(description = "行政区划编码")
    @Size(max = 50, message = "行政区划编码长度不能超过{max}")
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
    @Size(max = 2147483647, message = "扩展参数长度不能超过{max}")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
