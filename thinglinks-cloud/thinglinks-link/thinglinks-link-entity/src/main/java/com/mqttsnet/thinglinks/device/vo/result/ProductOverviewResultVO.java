package com.mqttsnet.thinglinks.device.vo.result;

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
 * -----------------------------------------------------------------------------
 * File Name: ProductOverviewResultVO.java
 * -----------------------------------------------------------------------------
 * Description:
 * 产品概况统计
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-25 17:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProductOverviewResultVO", description = "产品概况统计")
public class ProductOverviewResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品总量")
    private Integer productsTotalCount;

    @Schema(description = "普通产品量")
    private Integer ordinaryProductsCount;

    @Schema(description = "网关产品量")
    private Integer gatewayProductsCount;

    @Schema(description = "未知产品量")
    private Integer unknownProductsCount;

    @Schema(description = "启用量")
    private Integer enabledCount;

    @Schema(description = "停用量")
    private Integer disabledCount;

    @Schema(description = "今日新增产品量")
    private Integer todayNewCount;

    @Schema(description = "近7天新增产品量")
    private Integer weekNewCount;

    @Schema(description = "近30天新增产品量")
    private Integer monthNewCount;

}
