package com.mqttsnet.thinglinks.mqs.bus.core.stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stage 注册表,启动时按 phase 分组 + order 排序索引所有 {@link DeviceEventStage}。
 * 启动时一次性写入,运行时只读。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
public class DeviceEventStageRegistry {

    private final Map<StagePhaseEnum, List<DeviceEventStage>> phaseToStages = new EnumMap<>(StagePhaseEnum.class);

    public DeviceEventStageRegistry(List<DeviceEventStage> stages) {
        Arrays.stream(StagePhaseEnum.values()).forEach(p -> phaseToStages.put(p, Collections.emptyList()));
        if (CollUtil.isEmpty(stages)) {
            log.info("[bus.stage.registry] no DeviceEventStage bean found");
            return;
        }
        stages.stream()
            .collect(Collectors.groupingBy(DeviceEventStage::getPhase))
            .forEach((phase, list) -> {
                list.sort(Comparator.comparingInt(DeviceEventStage::getOrder));
                phaseToStages.put(phase, List.copyOf(list));
                log.info("[bus.stage.registry] phase={} stages={}", phase,
                    list.stream().map(DeviceEventStage::getName).toList());
            });
    }

    /**
     * 取指定 phase 的 stage 列表(按 order 升序);空 phase 返空列表。
     */
    public List<DeviceEventStage> byPhase(StagePhaseEnum phase) {
        return phaseToStages.getOrDefault(phase, Collections.emptyList());
    }
}
