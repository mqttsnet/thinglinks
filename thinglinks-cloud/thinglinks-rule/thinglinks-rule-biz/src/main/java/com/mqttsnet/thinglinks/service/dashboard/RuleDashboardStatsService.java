package com.mqttsnet.thinglinks.service.dashboard;

import com.mqttsnet.thinglinks.vo.result.dashboard.RuleBridgeSummaryResultVO;
import com.mqttsnet.thinglinks.vo.result.dashboard.RuleDashboardSummaryResultVO;

/**
 * -----------------------------------------------------------------------------
 * File Name: RuleDashboardStatsService.java
 * -----------------------------------------------------------------------------
 * Description:
 * Rule仪表盘数据业务层接口
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
 * @date 2024-10-22 17:01
 */
public interface RuleDashboardStatsService {

    /**
     * 获取仪表盘概要统计信息
     *
     * @return {@link RuleDashboardSummaryResultVO} 仪表盘统计信息
     */
    RuleDashboardSummaryResultVO getDashboardAssetSummary();

    /**
     * 获取北向集成 / 数据桥接统计信息。
     *
     * @return {@link RuleBridgeSummaryResultVO} 数据源 / 桥接规则 / 订阅源总量与启用数 + 今日执行成功/失败分布
     */
    RuleBridgeSummaryResultVO getBridgeSummary();
}
