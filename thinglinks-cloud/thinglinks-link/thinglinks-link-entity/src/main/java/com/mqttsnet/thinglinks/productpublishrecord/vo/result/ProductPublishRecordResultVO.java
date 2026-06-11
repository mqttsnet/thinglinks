package com.mqttsnet.thinglinks.productpublishrecord.vo.result;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 产品发布记录返回 VO。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "ProductPublishRecordResultVO", description = "产品发布记录")
public class ProductPublishRecordResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "源版本号")
    private String sourceVersion;

    @Schema(description = "目标版本号")
    private String targetVersion;

    @Schema(description = "操作意图(字典 PRODUCT_PUBLISH_RECORD_INTENT)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_PUBLISH_RECORD_INTENT)
    private Integer intent;

    @Schema(description = "执行状态(字典 PRODUCT_PUBLISH_RECORD_STATUS)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_PUBLISH_RECORD_STATUS)
    private Integer status;

    @Schema(description = "DDL 执行明细列表(typed,与后端 ddl_summary JSON 列对齐)")
    private List<PublishDdlItemVO> ddlItems;

    /**
     * 发布策略(字典 PRODUCT_PUBLISH_STRATEGY,0=全量 / 1=灰度 / 2=影子)。本表无此字段,
     * Controller 在 handlerResult 阶段按 (productIdentification, targetVersion) 反查 product_version 富化;
     * 仅发布(intent=0)记录有意义,回滚/历史清理为 null。
     */
    @Schema(description = "发布策略(字典 PRODUCT_PUBLISH_STRATEGY)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_PUBLISH_STRATEGY)
    private Integer publishStrategy;

    /** 灰度配置 JSON(仅 publishStrategy=灰度 时非空),结构同 product_version.canary_config_json。 */
    @Schema(description = "灰度配置 JSON(仅灰度策略非空)")
    private String canaryConfigJson;

    @Schema(description = "失败原因")
    private String failedReason;

    @Schema(description = "开始时间")
    private LocalDateTime startedTime;

    @Schema(description = "结束时间")
    private LocalDateTime finishedTime;

    @Schema(description = "备注")
    private String remark;
}
