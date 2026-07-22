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

/**
 * <p>
 * 表单查询返回值 VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "SubscriptionSourceResultVO", description = "数据桥接-订阅源")
public class SubscriptionSourceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "业务唯一编码")
    private String sourceCode;

    @Schema(description = "关联数据源 ID")
    private Long dataSourceId;

    @Schema(description = "关联数据源业务编码(service join 反查;列表/卡片/详情友好展示用)")
    private String dataSourceCode;

    @Schema(description = "关联数据源名称(service join 反查;列表/卡片/详情友好展示用)")
    private String dataSourceName;

    @Schema(description = "入站处理方式")
    private String targetHandler;

    @Schema(description = "字段映射 JSON")
    private String mappingJson;

    @Schema(description = "目标产品标识")
    private String targetProductIdentification;

    @Schema(description = "目标 topic 模板")
    private String targetTopicTemplate;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "上次消费位点")
    private String lastConsumeOffset;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;
}
