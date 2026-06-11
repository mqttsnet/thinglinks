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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 插件信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-25 19:05:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "插件信息表")
public class PluginInfoResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 应用ID，所属应用场景
     */
    @Schema(description = "应用ID，所属应用场景")
    private String appId;
    /**
     * 插件唯一标识，自动生成：plugin_code + version
     */
    @Schema(description = "插件唯一标识，自动生成：plugin_code + version")
    private String pluginIdentification;
    /**
     * 插件代码标识，取自 pluginMeta.properties
     */
    @Schema(description = "插件代码标识，取自 pluginMeta.properties")
    private String pluginCode;
    /**
     * 插件名称
     */
    @Schema(description = "插件名称")
    private String pluginName;
    /**
     * 插件版本，取自 pluginMeta.properties
     */
    @Schema(description = "插件版本，取自 pluginMeta.properties")
    private String version;
    /**
     * 插件描述，取自 pluginMeta.properties
     */
    @Schema(description = "插件描述，取自 pluginMeta.properties")
    private String description;
    /**
     * 文件在服务器上的唯一标识，用于查询文件临时路径
     */
    @Schema(description = "文件在服务器上的唯一标识，用于查询文件临时路径")
    private String fileId;
    /**
     * 文件大小（MB）
     */
    @Schema(description = "文件大小（MB）")
    private BigDecimal fileSize;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;
    /**
     * 插件级别：0-系统级，1-用户级
     */
    @Schema(description = "插件级别：0-系统级，1-用户级")
    private Integer level;
    /**
     * 插件类型：0-设备协议插件，1-业务插件
     */
    @Schema(description = "插件类型：0-设备协议插件，1-业务插件")
    private Integer type;
    /**
     * 运行模式：0-单节点，1-集群
     */
    @Schema(description = "运行模式：0-单节点，1-集群")
    private Integer runMode;
    /**
     * 许可证类型（如GPL, MIT, 商业等）
     */
    @Schema(description = "许可证类型（如GPL, MIT, 商业等）")
    private Integer licenseType;
    /**
     * 许可证密钥或证书
     */
    @Schema(description = "许可证密钥或证书")
    private String licenseKey;
    /**
     * 许可证有效期
     */
    @Schema(description = "许可证有效期")
    private LocalDate validUntil;
    /**
     * 文件的哈希值，用于验证文件的完整性（如 SHA-256）
     */
    @Schema(description = "文件的哈希值，用于验证文件的完整性（如 SHA-256）")
    private String fileHash;
    /**
     * 扫描状态：PENDING, SUCCESS, FAILED
     */
    @Schema(description = "扫描状态：PENDING, SUCCESS, FAILED")
    private String scanStatus;
    /**
     * 扫描报告的文件ID
     */
    @Schema(description = "扫描报告的文件ID")
    private String scanReportFileId;
    /**
     * 最后一次扫描的日期
     */
    @Schema(description = "最后一次扫描的日期")
    private LocalDateTime scanDate;
    /**
     * 扫描摘要（如发现的漏洞数目等）
     */
    @Schema(description = "扫描摘要（如发现的漏洞数目等）")
    private String scanSummary;
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
