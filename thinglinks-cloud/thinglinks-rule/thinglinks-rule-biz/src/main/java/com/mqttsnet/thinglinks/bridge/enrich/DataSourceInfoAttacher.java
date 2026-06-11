package com.mqttsnet.thinglinks.bridge.enrich;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据源信息批量反填工具(桥接规则 / 订阅源 / trace 列表展示用)。
 * N 个 VO → 1 次 IN 查询(去重),无 N+1。空列表直接 return。
 *
 * <p>走 {@link DataSourceService#listByIds},Service 自带 {@code @DS(BASE_TENANT)} AOP 切库。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceInfoAttacher {

    private final DataSourceService dataSourceService;

    /**
     * 批量反填数据源字段(原地修改)。
     *
     * @param idExtractor 从 VO 提 dataSourceId,如 {@code DataBridgeResultVO::getDataSourceId}
     * @param filler      DataSource → VO 反填消费者,由调用方决定填哪些字段
     */
    public <VO> void attach(List<VO> voList,
                            Function<VO, Long> idExtractor,
                            BiConsumer<VO, DataSource> filler) {
        if (CollUtil.isEmpty(voList) || idExtractor == null || filler == null) {
            return;
        }
        Set<Long> ids = voList.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        try {
            Map<Long, DataSource> dsMap = dataSourceService.listByIds(new ArrayList<>(ids)).stream()
                    .collect(Collectors.toMap(DataSource::getId, ds -> ds));
            voList.forEach(vo -> Optional.ofNullable(idExtractor.apply(vo))
                    .map(dsMap::get)
                    .ifPresent(ds -> filler.accept(vo, ds)));
        } catch (Exception e) {
            // 反填失败仅 warn,不阻塞主返回 ── 业务列表仍能展示其它字段
            log.warn("[DataSourceInfoAttacher] attach failed (non-blocking) sample={}: {}",
                    voList.size(), e.getMessage());
        }
    }

    /**
     * 分页场景:转发到 List 版本(PageController.handlerResult 钩子直接调)。
     */
    public <VO> void attachPage(IPage<VO> voPage,
                                Function<VO, Long> idExtractor,
                                BiConsumer<VO, DataSource> filler) {
        Optional.ofNullable(voPage).ifPresent(p -> attach(p.getRecords(), idExtractor, filler));
    }
}
