package com.mqttsnet.thinglinks.dashboard.vo.result;

import java.io.Serial;

import com.mqttsnet.thinglinks.device.vo.result.DeviceOverviewResultVO;
import com.mqttsnet.thinglinks.device.vo.result.ProductOverviewResultVO;
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
 * -----------------------------------------------------------------------------
 * File Name: DashboardSummaryResultVO.java
 * -----------------------------------------------------------------------------
 * Description:
 * 仪表板概要统计VO
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
 * @date 2023-11-25 16:58
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DashboardSummaryResultVO", description = "仪表板概要统计VO")
public class DashboardSummaryResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备概况统计")
    private DeviceOverviewResultVO deviceOverviewResultVO;

    @Schema(description = "产品概况统计")
    private ProductOverviewResultVO productOverviewResultVO;

}
