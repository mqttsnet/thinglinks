package com.mqttsnet.thinglinks.dto.bus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.enumeration.bus.PipelineStatusEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StageStatusEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * Pipeline 执行结果摘要,dispatcher 传给 callback 用于 trace 落库 / 指标 / 报警。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@Builder
public class DeviceEventOutcome {

    private final PipelineStatusEnum status;
    private final List<StageRecord> stages;
    private final long totalLatencyMs;
    /**
     * 失败 / 丢弃 / 未路由时填,成功为 null。
     */
    private final String failureReason;

    /**
     * 有效 stage 数(去 SKIPPED)。
     */
    public int effectiveStageCount() {
        return CollUtil.isEmpty(stages) ? 0
            : (int) stages.stream().filter(s -> s.getStatus() != StageStatusEnum.SKIPPED).count();
    }

    /**
     * 首个失败 stage 名(日志摘要 / 指标 label 用)。
     */
    public Optional<String> firstFailedStage() {
        return CollUtil.isEmpty(stages) ? Optional.empty()
            : stages.stream()
              .filter(s -> s.getStatus() == StageStatusEnum.FAILED)
              .findFirst()
              .map(StageRecord::getStageName);
    }

    /**
     * 防御性不可变视图。
     */
    public List<StageRecord> immutableStages() {
        return CollUtil.isEmpty(stages) ? Collections.emptyList() : Collections.unmodifiableList(stages);
    }
}
