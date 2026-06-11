package com.mqttsnet.thinglinks.video.vo.query.gateway;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * 表单查询条件VO
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
@Schema(title = "VideoGatewayMappingPageQuery", description = "视频网关映射")
public class VideoGatewayMappingPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

    /**
     * 源协议
     */
    @Schema(description = "源协议")
    private String srcProtocol;
    /**
     * 源设备标识
     */
    @Schema(description = "源设备标识")
    private String srcDeviceIdentification;
    /**
     * 源通道标识
     */
    @Schema(description = "源通道标识")
    private String srcChannelIdentification;
    /**
     * 映射GB28181设备编号
     */
    @Schema(description = "映射GB28181设备编号")
    private String gbDeviceId;
    /**
     * 映射GB28181通道编号
     */
    @Schema(description = "映射GB28181通道编号")
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
     * 注册状态
     */
    @Schema(description = "注册状态")
    private Boolean registerStatus;
    /**
     * 最后注册时间
     */
    @Schema(description = "最后注册时间")
    private String lastRegisterTime;
    /**
     * 映射配置
     */
    @Schema(description = "映射配置")
    private JSONObject mappingConfig;
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
