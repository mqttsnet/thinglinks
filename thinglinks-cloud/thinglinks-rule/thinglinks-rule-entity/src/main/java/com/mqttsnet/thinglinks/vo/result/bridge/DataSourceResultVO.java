package com.mqttsnet.thinglinks.vo.result.bridge;

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
 * 表单查询返回值 VO
 * 数据桥接-数据源
 * </p>
 *
 * <p>注：connection_json / credential_json 在 entity 层已 EncryptTypeHandler 解密；
 * 但为安全起见，<b>列表查询接口 Service 层应主动屏蔽 credential_json</b>（仅在编辑详情接口时返回）。</p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DataSourceResultVO", description = "数据桥接-数据源")
public class DataSourceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "数据源名称")
    private String dataSourceName;

    @Schema(description = "业务唯一编码")
    private String dataSourceCode;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "协议类型")
    private String sourceType;

    @Schema(description = "连接参数 JSON（已解密）")
    private String connectionJson;

    @Schema(description = "凭证 JSON（已解密；列表接口建议屏蔽）")
    private String credentialJson;

    @Schema(description = "序列化策略")
    private String serialization;

    @Schema(description = "默认可靠性级别")
    private Integer defaultQos;

    @Schema(description = "默认 QPS 限流")
    private Integer defaultRateLimitQps;

    @Schema(description = "默认最大重试次数")
    private Integer defaultRetryMaxTimes;

    @Schema(description = "默认初始退避时长 ms")
    private Integer defaultRetryBackoffMs;

    @Schema(description = "默认单次发送超时 ms")
    private Integer defaultTimeoutMs;

    @Schema(description = "默认死信投递的数据源 FK")
    private Long defaultDeadLetterDataSourceId;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "健康状态")
    private String healthStatus;

    @Schema(description = "上次健康检查时间")
    private LocalDateTime lastHealthCheckTime;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;
}
