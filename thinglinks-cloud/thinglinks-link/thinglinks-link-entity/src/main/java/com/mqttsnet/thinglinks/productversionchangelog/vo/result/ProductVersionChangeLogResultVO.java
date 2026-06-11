package com.mqttsnet.thinglinks.productversionchangelog.vo.result;

import java.io.Serial;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 产品物模型版本变更日志返回 VO。
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
@Schema(title = "ProductVersionChangeLogResultVO", description = "产品物模型版本变更日志")
public class ProductVersionChangeLogResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "版本序号(本次变更所属的快照序号)")
    private String versionNo;

    @Schema(description = "变更类型(字典 PRODUCT_VERSION_CHANGE_TYPE)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.PRODUCT_VERSION_CHANGE_TYPE)
    private Integer changeType;

    @Schema(description = "变更维度")
    private Integer targetType;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "字段级变更明细 JSON")
    private String changeDetailJson;
}
