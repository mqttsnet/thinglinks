package com.mqttsnet.thinglinks.productversion.vo.result;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物模型版本管理总览统计 VO。
 *
 * <p>用于跨产品版本管理总览页头部统计卡片(4 个数 + 1 个时间窗指标)。</p>
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ProductVersionStatisticsResultVO", description = "物模型版本管理总览统计")
public class ProductVersionStatisticsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品总数(deleted=0)。
     */
    @Schema(description = "产品总数")
    private Long productTotal;

    /**
     * 已发布过的产品数(product.active_version_no 非空)。
     */
    @Schema(description = "已发布产品数")
    private Long publishedProductCount;

    /**
     * 灰度切换中的产品数(product.previous_full_version_no 非空,表示当前生效版本是灰度版本)。
     */
    @Schema(description = "灰度中产品数")
    private Long canaryProductCount;

    /**
     * 未发布产品数(从未发布过 → product.active_version_no 为空)。
     */
    @Schema(description = "未发布产品数")
    private Long unpublishedProductCount;

    /**
     * 近 7 天发布记录数(product_publish_record 表 intent=0 status=1)。
     */
    @Schema(description = "近 7 天发布次数")
    private Long recentPublishCount7d;

    /**
     * 物模型服务总数(product_service 表 deleted=0)── 衡量平台建模深度。
     */
    @Schema(description = "物模型服务数")
    private Long thingModelServiceCount;

    /**
     * 已发布版本总量(product_version 表 version_status=PUBLISHED)── 跨产品累计发布版本数。
     */
    @Schema(description = "发布版本总量")
    private Long publishedVersionTotal;
}
