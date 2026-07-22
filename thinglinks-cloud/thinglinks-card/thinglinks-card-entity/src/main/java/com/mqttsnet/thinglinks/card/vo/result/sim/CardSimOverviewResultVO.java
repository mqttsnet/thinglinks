package com.mqttsnet.thinglinks.card.vo.result.sim;

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
 * File Name: CardSimOverviewResultVO
 * -----------------------------------------------------------------------------
 * Description:
 * 物联网卡概况统计
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/13       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/13 22:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "CardSimOverviewResultVO", description = "物联网卡概况统计")
public class CardSimOverviewResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "物联网卡总量")
    private Integer totalCardsCount;

    @Schema(description = "插拔卡数量")
    private Integer cardTypePlugInCount;

    @Schema(description = "贴片IC卡数量")
    private Integer cardTypePatchCount;

    @Schema(description = "移动运营商卡数量")
    private Integer carrierTypeMobileCount;

    @Schema(description = "电信运营商卡数量")
    private Integer carrierTypeTelecomCount;

    @Schema(description = "联通运营商卡数量")
    private Integer carrierTypeUnicomCount;

    @Schema(description = "待激活卡数量")
    private Integer statusPendingActivationCount;

    @Schema(description = "已激活卡数量")
    private Integer statusActivatedCount;

    @Schema(description = "停机卡数量")
    private Integer statusDeactivatedCount;

    @Schema(description = "普卡数量")
    private Integer useTypeNormalCount;

    @Schema(description = "共享池卡数量")
    private Integer useTypeSharedPoolCount;

    @Schema(description = "流量池卡数量")
    private Integer useTypeTrafficPoolCount;

    @Schema(description = "在线卡数量")
    private Integer onlineFlagOnlineCount;

    @Schema(description = "离线卡数量")
    private Integer onlineFlagOfflineCount;
}
