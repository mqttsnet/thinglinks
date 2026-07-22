package com.mqttsnet.thinglinks.productpublishrecord.vo.query;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 产品发布记录分页查询参数。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Schema(title = "ProductPublishRecordPageQuery", description = "发布记录分页查询参数")
public class ProductPublishRecordPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @Schema(description = "产品标识")
    private String productIdentification;

    /**
     * 目标版本号。
     */
    @Schema(description = "目标版本号")
    private String targetVersion;

    /**
     * 操作意图(0-发布,1-回滚,2-历史清理)。
     */
    @Schema(description = "操作意图")
    private Integer intent;

    /**
     * 执行状态(0-执行中,1-成功,2-失败)。
     */
    @Schema(description = "执行状态")
    private Integer status;
}
