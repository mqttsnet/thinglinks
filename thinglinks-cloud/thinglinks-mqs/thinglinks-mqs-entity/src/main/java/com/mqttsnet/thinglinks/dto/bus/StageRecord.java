package com.mqttsnet.thinglinks.dto.bus;

import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StageStatusEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 单 stage 执行记录,Dispatcher 在每个 stage 执行前后填充,聚合为 {@link DeviceEventOutcome}。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Data
@Builder
public class StageRecord {

    private String stageName;
    private StagePhaseEnum phase;
    /**
     * Pipeline 内自增,从 1 起。
     */
    private int sequence;
    private StageStatusEnum status;
    private long latencyMs;
    /**
     * 失败错误,成功为 null。
     */
    private String errorMsg;
    /**
     * 跳过原因(SKIPPED 时填,如 supports=false / guard=false)。
     */
    private String skipReason;
}
