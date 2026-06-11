package com.mqttsnet.thinglinks.vo.update.bridge;

import com.mqttsnet.basic.base.entity.SuperEntity;
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

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 表单修改方法 VO
 * 数据桥接-数据源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "DataSourceUpdateVO", description = "数据桥接-数据源")
public class DataSourceUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @NotNull(message = "请填写主键", groups = SuperEntity.Update.class)
    private Long id;

    @Schema(description = "应用ID")
    @NotEmpty(message = "请选择应用ID")
    @Size(max = 128, message = "应用ID长度不能超过{max}")
    private String appId;

    @Schema(description = "数据源名称")
    @NotEmpty(message = "请填写数据源名称")
    @Size(max = 255, message = "数据源名称长度不能超过{max}")
    private String dataSourceName;

    @Schema(description = "方向")
    @NotEmpty(message = "请选择方向")
    private String direction;

    @Schema(description = "协议类型")
    @NotEmpty(message = "请选择协议类型")
    private String sourceType;

    @Schema(description = "连接参数 JSON")
    @NotEmpty(message = "请填写连接参数")
    private String connectionJson;

    @Schema(description = "凭证 JSON")
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

    @Schema(description = "扩展参数")
    @Size(max = 2048, message = "扩展参数长度不能超过{max}")
    private String extendParams;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过{max}")
    private String remark;
}
