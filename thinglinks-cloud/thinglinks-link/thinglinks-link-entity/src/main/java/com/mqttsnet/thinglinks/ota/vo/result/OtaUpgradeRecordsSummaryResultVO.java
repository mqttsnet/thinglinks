package com.mqttsnet.thinglinks.ota.vo.result;

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
 * File Name: OtaUpgradeRecordsSummaryResultVO
 * -----------------------------------------------------------------------------
 * Description:
 * OTA升级记录统计VO
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/5/19       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/5/19 20:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "OtaUpgradeRecordsSummaryResultVO", description = "OTA升级记录统计VO")
public class OtaUpgradeRecordsSummaryResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "总数量")
    private Long totalCount;

    @Schema(description = "待升级数量")
    private Long pendingUpgradeCount;

    @Schema(description = "升级中数量")
    private Long upgradingCount;

    @Schema(description = "升级成功数量")
    private Long upgradeSuccessCount;

    @Schema(description = "升级失败数量")
    private Long upgradeFailureCount;

}
