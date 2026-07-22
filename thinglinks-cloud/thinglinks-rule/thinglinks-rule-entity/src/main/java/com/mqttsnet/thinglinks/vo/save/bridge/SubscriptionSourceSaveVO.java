package com.mqttsnet.thinglinks.vo.save.bridge;

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
 * 表单保存方法 VO
 * 数据桥接-订阅源
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
@Schema(title = "SubscriptionSourceSaveVO", description = "数据桥接-订阅源")
public class SubscriptionSourceSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    @Size(max = 128, message = "应用ID长度不能超过{max}")
    private String appId;

    @Schema(description = "订阅源名称")
    @NotEmpty(message = "请填写订阅源名称")
    @Size(max = 255, message = "订阅源名称长度不能超过{max}")
    private String sourceName;

    @Schema(description = "业务唯一编码（snowflake，不传则自动生成）")
    @Size(max = 128, message = "编码长度不能超过{max}")
    private String sourceCode;

    @Schema(description = "复用数据源 FK（direction 须为 20-入站 或 30-双向）")
    @NotNull(message = "请选择数据源")
    private Long dataSourceId;

    @Schema(description = "入站处理方式：MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER")
    @NotEmpty(message = "请选择处理方式")
    @Size(max = 50, message = "处理方式长度不能超过{max}")
    private String targetHandler;

    @Schema(description = "字段映射 JSON")
    @NotEmpty(message = "请填写字段映射")
    private String mappingJson;

    @Schema(description = "target_handler=MQTT_FORWARD 时的目标产品标识")
    @Size(max = 128, message = "产品标识长度不能超过{max}")
    private String targetProductIdentification;

    @Schema(description = "目标 topic 模板")
    @Size(max = 500, message = "topic 模板长度不能超过{max}")
    private String targetTopicTemplate;

    @Schema(description = "扩展参数")
    @Size(max = 2048, message = "扩展参数长度不能超过{max}")
    private String extendParams;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过{max}")
    private String remark;
}
