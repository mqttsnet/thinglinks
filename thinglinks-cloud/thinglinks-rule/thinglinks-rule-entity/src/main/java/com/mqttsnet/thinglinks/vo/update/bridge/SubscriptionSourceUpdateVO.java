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
@Schema(title = "SubscriptionSourceUpdateVO", description = "数据桥接-订阅源")
public class SubscriptionSourceUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @NotNull(message = "请填写主键", groups = SuperEntity.Update.class)
    private Long id;

    @Schema(description = "应用ID")
    @Size(max = 128, message = "应用ID长度不能超过{max}")
    private String appId;

    @Schema(description = "订阅源名称")
    @NotEmpty(message = "请填写订阅源名称")
    private String sourceName;

    @Schema(description = "复用数据源 FK")
    @NotNull(message = "请选择数据源")
    private Long dataSourceId;

    @Schema(description = "入站处理方式")
    @NotEmpty(message = "请选择处理方式")
    private String targetHandler;

    @Schema(description = "字段映射 JSON")
    @NotEmpty(message = "请填写字段映射")
    private String mappingJson;

    @Schema(description = "目标产品标识")
    private String targetProductIdentification;

    @Schema(description = "目标 topic 模板")
    private String targetTopicTemplate;

    @Schema(description = "扩展参数")
    @Size(max = 2048, message = "扩展参数长度不能超过{max}")
    private String extendParams;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过{max}")
    private String remark;
}
