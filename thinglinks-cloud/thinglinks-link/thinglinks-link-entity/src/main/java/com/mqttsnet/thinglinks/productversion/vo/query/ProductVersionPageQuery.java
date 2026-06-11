package com.mqttsnet.thinglinks.productversion.vo.query;

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
 * 产品物模型版本分页查询参数。
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
@Schema(title = "ProductVersionPageQuery", description = "版本分页查询参数")
public class ProductVersionPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    @Schema(description = "产品标识")
    private String productIdentification;

    /**
     * 版本序号(过滤指定快照)。
     */
    @Schema(description = "版本序号")
    private String versionNo;

    /**
     * 版本状态(0-草稿,1-已发布,2-灰度中,3-影子,4-已回滚,5-已归档)。
     */
    @Schema(description = "版本状态")
    private Integer versionStatus;

    /**
     * 发布策略(0-全量,1-灰度,2-影子)。
     */
    @Schema(description = "发布策略")
    private Integer publishStrategy;
}
