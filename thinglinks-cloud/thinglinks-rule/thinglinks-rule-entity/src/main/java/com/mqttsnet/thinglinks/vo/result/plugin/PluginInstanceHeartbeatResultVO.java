package com.mqttsnet.thinglinks.vo.result.plugin;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 插件实例心跳表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-27 16:31:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "插件实例心跳表")
public class PluginInstanceHeartbeatResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 实例唯一标识
     */
    @Schema(description = "实例唯一标识")
    private String instanceIdentification;
    /**
     * 插件运行所在的机器 IP 地址
     */
    @Schema(description = "插件运行所在的机器 IP 地址")
    private String machineIp;
    /**
     * 上次心跳时间
     */
    @Schema(description = "上次心跳时间")
    private LocalDateTime lastHeartbeatTime;
    /**
     * 心跳间隔时间（秒）
     */
    @Schema(description = "心跳间隔时间（秒）")
    private Integer heartbeatInterval;
    /**
     * 心跳状态：0-正常，1-异常
     */
    @Schema(description = "心跳状态：0-正常，1-异常")
    private Integer status;
    /**
     * 心跳详细信息
     */
    @Schema(description = "心跳详细信息")
    private String heartbeatMessage;
    /**
     * 扩展参数（预留）
     */
    @Schema(description = "扩展参数（预留）")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
