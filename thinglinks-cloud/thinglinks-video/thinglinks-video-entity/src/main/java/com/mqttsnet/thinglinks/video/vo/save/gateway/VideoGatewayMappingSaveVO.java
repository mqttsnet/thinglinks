package com.mqttsnet.thinglinks.video.vo.save.gateway;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 表单保存方法VO
 * 视频网关映射表
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
@Schema(title = "VideoGatewayMappingSaveVO", description = "视频网关映射")
public class VideoGatewayMappingSaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 源协议
     */
    @Schema(description = "源协议")
    @NotEmpty(message = "请填写源协议")
    @Size(max = 50, message = "源协议长度不能超过{max}")
    private String srcProtocol;
    /**
     * 源设备标识
     */
    @Schema(description = "源设备标识")
    @NotEmpty(message = "请填写源设备标识")
    @Size(max = 255, message = "源设备标识长度不能超过{max}")
    private String srcDeviceIdentification;
    /**
     * 源通道标识
     */
    @Schema(description = "源通道标识")
    @Size(max = 255, message = "源通道标识长度不能超过{max}")
    private String srcChannelIdentification;
    /**
     * 映射GB28181设备编号
     */
    @Schema(description = "映射GB28181设备编号")
    @NotEmpty(message = "请填写映射GB28181设备编号")
    @Size(max = 50, message = "映射GB28181设备编号长度不能超过{max}")
    private String gbDeviceId;
    /**
     * 映射GB28181通道编号
     */
    @Schema(description = "映射GB28181通道编号")
    @NotEmpty(message = "请填写映射GB28181通道编号")
    @Size(max = 50, message = "映射GB28181通道编号长度不能超过{max}")
    private String gbChannelId;
    /**
     * 目标上级平台ID
     */
    @Schema(description = "目标上级平台ID")
    private Long gbPlatformId;
    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enable;
    /**
     * 自动注册
     */
    @Schema(description = "自动注册")
    private Boolean autoPush;
    /**
     * 映射配置
     */
    @Schema(description = "映射配置")
    private JSONObject mappingConfig;
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
