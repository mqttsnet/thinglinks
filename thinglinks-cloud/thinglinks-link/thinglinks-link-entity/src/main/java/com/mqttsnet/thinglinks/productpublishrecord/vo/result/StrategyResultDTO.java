package com.mqttsnet.thinglinks.productpublishrecord.vo.result;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.mqttsnet.thinglinks.productversion.vo.canary.CanaryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发布策略执行结果快照 ── 存 {@code product_publish_record.canary_result_json}(发布那一刻冻结,不随后续手动切流 /
 * 晋升 / 新设备而变)。全量只用通用字段;灰度填 {@link CanaryResult};影子填 {@link ShadowResult}。
 *
 * @author mqttsnet
 */
@Data
@Schema(title = "StrategyResultDTO", description = "发布策略执行结果快照")
public class StrategyResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "发布策略(0=全量 / 1=灰度 / 2=影子)── 快照自包含,展示不依赖反查版本表")
    private Integer strategy;

    @Schema(description = "本次实际改绑设备数(去重;命中网关会连带子设备)")
    private Integer affectedDeviceCount;

    @Schema(description = "发布那一刻该产品设备总数(占比基数)")
    private Integer productTotalAtPublish;

    @Schema(description = "灰度执行结果(仅灰度策略)")
    private CanaryResult canary;

    @Schema(description = "影子执行结果(仅影子策略)")
    private ShadowResult shadow;

    /**
     * 灰度执行结果快照 ── 记录发布那一刻的来源与命中规模。
     */
    @Data
    @Schema(title = "CanaryResult", description = "灰度执行结果快照")
    public static class CanaryResult implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "灰度来源:group(分组) / manual(名单) / percent(比例)")
        private String source;

        @Schema(description = "所选分组明细(source=group;含当时各组设备数)")
        private List<CanaryGroup> groups;

        @Schema(description = "设备名单(source=manual)")
        private List<String> deviceIdentifications;

        @Schema(description = "灰度比例 1~99(source=percent)")
        private Integer percent;

        @Schema(description = "规则目标设备数(percent=按比例预期数;group/manual 看 groups/名单)")
        private Integer targetCount;
    }

    /**
     * 影子执行结果快照 ── 影子只预建表、不改绑设备。
     */
    @Data
    @Schema(title = "ShadowResult", description = "影子执行结果快照")
    public static class ShadowResult implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "本次成功预建的超表数")
        private Integer preBuiltStableCount;
    }
}
