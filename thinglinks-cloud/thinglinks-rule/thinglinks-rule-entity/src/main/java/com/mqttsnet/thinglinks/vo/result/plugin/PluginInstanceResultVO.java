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

/**
 * <p>
 * 表单查询方法返回值VO
 * 插件实例信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-27 16:02:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "插件实例信息表")
public class PluginInstanceResultVO extends AuditableResultVO {

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
     * 实例名称，用于标识实例的友好名称
     */
    @Schema(description = "实例名称，用于标识实例的友好名称")
    private String instanceName;
    /**
     * 应用名称，SpringBoot应用名称
     */
    @Schema(description = "应用名称，SpringBoot应用名称")
    private String applicationName;
    /**
     * 实例运行所在的机器 IP 地址
     */
    @Schema(description = "实例运行所在的机器 IP 地址")
    private String machineIp;
    /**
     * 实例可用端口范围起始值
     */
    @Schema(description = "实例可用端口范围起始值")
    private Integer portRangeStart;
    /**
     * 实例可用端口范围结束值
     */
    @Schema(description = "实例可用端口范围结束值")
    private Integer portRangeEnd;
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
    /**
     * 实例的权重
     */
    @Schema(description = "实例的权重")
    private Integer weight;
    /**
     * 实例的健康状态
     */
    @Schema(description = "实例的健康状态")
    private Boolean healthy;
    /**
     * 实例是否启用
     */
    @Schema(description = "实例是否启用")
    private Boolean enabled;
    /**
     * 实例是否为临时实例
     */
    @Schema(description = "实例是否为临时实例")
    private Boolean ephemeral;
    /**
     * 实例所在集群名称
     */
    @Schema(description = "实例所在集群名称")
    private String clusterName;
    /**
     * 实例心跳间隔时间(毫秒)
     */
    @Schema(description = "实例心跳间隔时间(毫秒)")
    private Long heartBeatInterval;
    /**
     * 实例心跳超时时间(毫秒)
     */
    @Schema(description = "实例心跳超时时间(毫秒)")
    private Long heartBeatTimeOut;
    /**
     * 实例IP删除超时时间(毫秒)
     */
    @Schema(description = "实例IP删除超时时间(毫秒)")
    private Long ipDeleteTimeOut;
    /**
     * 实例机器端口
     */
    @Schema(description = "实例机器端口")
    private String machinePort;

    /**
     * 获取实例的详细地址（IP:端口）。
     *
     * @return 返回实例的 IP 和端口的字符串表示
     */
    @Schema(description = "获取实例的地址，格式为 IP:端口")
    public String toInetAddr() {
        return machineIp + ":" + machinePort;
    }


    /**
     * 获取实例的详细地址（带 http:// 的 IP:端口）。
     *
     * @return 返回实例的 IP 和端口的字符串表示，带 http:// 前缀
     */
    @Schema(description = "获取实例的地址，格式为 http://IP:端口")
    public String toInetAddrWithHttp() {
        return "http://" + machineIp + ":" + machinePort;
    }


}
