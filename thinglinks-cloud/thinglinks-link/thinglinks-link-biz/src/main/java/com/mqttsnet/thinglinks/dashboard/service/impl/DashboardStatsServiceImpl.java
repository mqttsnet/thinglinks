package com.mqttsnet.thinglinks.dashboard.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.common.cache.link.counter.DownLinkDataCounterCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.link.counter.UpLinkDataCounterCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.dashboard.enumeration.LinkDataTypeEnum;
import com.mqttsnet.thinglinks.dashboard.enumeration.TimeUnitEnum;
import com.mqttsnet.thinglinks.dashboard.service.DashboardStatsService;
import com.mqttsnet.thinglinks.dashboard.vo.query.DashboardDetailsQuery;
import com.mqttsnet.thinglinks.dashboard.vo.result.DashboardDetailsResultVO;
import com.mqttsnet.thinglinks.dashboard.vo.result.DashboardSummaryResultVO;
import com.mqttsnet.thinglinks.dashboard.vo.result.DashboardTopologySummaryResultVO;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * -----------------------------------------------------------------------------
 * File Name: DashboardStatsServiceImpl.java
 * -----------------------------------------------------------------------------
 * Description:
 * 仪表盘数据业务层接口实现类
 * 提供设备、产品、链路数据等统计信息的查询功能
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
 * @date 2023-11-25 17:02
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardStatsServiceImpl implements DashboardStatsService {
    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.YYYYMMDD_FORMAT);

    private final DeviceService deviceService;
    private final ProductQueryService productQueryService;
    private final CachePlusOps cachePlusOps;
    private final ContextAwareExecutor contextAwareExecutor;

    @Qualifier("linkDefaultExecutor")
    private final ThreadPoolExecutor linkDefaultExecutor;

    /**
     * 获取仪表盘资产统计概要统计信息
     * 包括设备概览和产品概览数据
     *
     * @return {@link DashboardSummaryResultVO} 仪表盘资产概要统计信息
     */
    @Override
    public DashboardSummaryResultVO getDashboardAssetSummary() {
        DashboardSummaryResultVO dashboardSummary = new DashboardSummaryResultVO();

        Optional.ofNullable(deviceService.getDeviceOverview())
                .ifPresent(dashboardSummary::setDeviceOverviewResultVO);

        Optional.ofNullable(productQueryService.getProductOverview())
                .ifPresent(dashboardSummary::setProductOverviewResultVO);

        return dashboardSummary;
    }

    /**
     * 获取拓扑摘要统计信息
     * 包括最近3天的上下行数据总量和详细统计信息
     *
     * @return {@link DashboardTopologySummaryResultVO} 拓扑摘要统计信息
     */
    @Override
    public DashboardTopologySummaryResultVO getTopologySummary() {
        DashboardTopologySummaryResultVO result = new DashboardTopologySummaryResultVO();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(2);
        DashboardDetailsQuery detailsQuery = new DashboardDetailsQuery();
        detailsQuery.setStartTime(startTime.format(DateUtils.YYYYMMDDHHMM_FORMATTER));
        detailsQuery.setEndTime(endTime.format(DateUtils.YYYYMMDDHHMM_FORMATTER));
        detailsQuery.setTime("1h");
        detailsQuery.setLimit(72L);

        try {
            CompletableFuture<DashboardDetailsResultVO> detailsFuture = contextAwareExecutor.executeWithContext(
                    () -> getAssetStatsDetails(detailsQuery), linkDefaultExecutor
            );

            // 计算上下行数据总量
            CompletableFuture<Long> totalUplinkFuture = detailsFuture.thenApplyAsync(details ->
                            details.getUplinkDetails().stream()
                                    .map(DashboardDetailsResultVO.LinkDataDetail::getValue)
                                    .filter(Objects::nonNull)
                                    .mapToLong(Integer::longValue)
                                    .sum(),
                    linkDefaultExecutor
            );

            CompletableFuture<Long> totalDownlinkFuture = detailsFuture.thenApplyAsync(details ->
                            details.getDownlinkDetails().stream()
                                    .map(DashboardDetailsResultVO.LinkDataDetail::getValue)
                                    .filter(Objects::nonNull)
                                    .mapToLong(Integer::longValue)
                                    .sum(),
                    linkDefaultExecutor
            );

            CompletableFuture.allOf(detailsFuture, totalUplinkFuture, totalDownlinkFuture)
                    .exceptionally(ex -> {
                        log.error("异步计算拓扑摘要信息时发生异常", ex);
                        throw new RuntimeException("异步计算拓扑摘要信息失败", ex);
                    })
                    .join();

            DashboardDetailsResultVO detailsResult = detailsFuture.getNow(null);
            Long totalUplink = totalUplinkFuture.getNow(0L);
            Long totalDownlink = totalDownlinkFuture.getNow(0L);

            if (Objects.nonNull(detailsResult)) {
                result.setDashboardDetailsResultVo(detailsResult);
                result.setTotalUplinkData(totalUplink);
                result.setTotalDownlinkData(totalDownlink);
            }

        } catch (Exception e) {
            log.error("获取拓扑摘要信息时发生异常", e);
            throw new RuntimeException("获取拓扑摘要信息失败", e);
        }

        return result;
    }

    /**
     * 获取仪表盘资产统计数据详细信息
     *
     * @param detailsQuery 查询条件对象，包含以下参数：
     *                     - startTime: 开始时间，格式为 yyyyMMddHHmm
     *                     - endTime: 结束时间，格式为 yyyyMMddHHmm
     *                     - time: 时间单位代码（1m, 1h, 1d）
     *                     - limit: 最大返回数据条数
     * @return {@link DashboardDetailsResultVO} 仪表盘资产统计数据详细信息
     */
    @Override
    public DashboardDetailsResultVO getAssetStatsDetails(DashboardDetailsQuery detailsQuery) {
        log.info("获取仪表盘资产统计数据详细信息, query={}", JSON.toJSONString(detailsQuery));
        return this.getDetails(detailsQuery);
    }

    /**
     * 根据查询参数获取指定时间范围内的详细数据
     * 数据按指定的时间单位进行分组统计
     *
     * @param detailsQuery 详细查询参数对象，包含：
     *                     - startTime: 开始时间字符串，格式为 yyyyMMddHHmm
     *                     - endTime: 结束时间字符串，格式为 yyyyMMddHHmm
     *                     - time: 时间单位代码，支持 1m(分钟)、1h(小时)、1d(天)
     *                     - limit: 最大数据条数限制
     * @return {@link DashboardDetailsResultVO} 包含上下行数据详细统计结果的对象
     * @throws BizException             当开始时间晚于结束时间时抛出
     * @throws IllegalArgumentException 当时间单位代码无效时抛出
     */
    public DashboardDetailsResultVO getDetails(DashboardDetailsQuery detailsQuery) {
        LocalDateTime startDateTime = LocalDateTime.parse(detailsQuery.getStartTime(), DateUtils.YYYYMMDDHHMM_FORMATTER);
        LocalDateTime endDateTime = LocalDateTime.parse(detailsQuery.getEndTime(), DateUtils.YYYYMMDDHHMM_FORMATTER);

        if (startDateTime.isAfter(endDateTime)) {
            throw BizException.wrap("开始时间必须早于结束时间");
        }

        TimeUnitEnum timeUnit = TimeUnitEnum.fromCode(detailsQuery.getTime())
                .orElseThrow(() -> new IllegalArgumentException("无效的时间单位代码: " + detailsQuery.getTime()));

        // 获取所有需要查询的日期
        List<String> dateKeys = getDateKeysInRange(startDateTime, endDateTime);

        // 并行获取上下行数据
        Map<String, Map<String, Integer>> uplinkData = batchGetHashData(dateKeys, LinkDataTypeEnum.UPLINK);
        Map<String, Map<String, Integer>> downlinkData = batchGetHashData(dateKeys, LinkDataTypeEnum.DOWNLINK);

        // 生成时间模式列表
        List<String> patterns = generateTimePatterns(startDateTime, endDateTime, timeUnit, detailsQuery.getLimit());

        // 计算每个时间单位的数据
        List<DashboardDetailsResultVO.LinkDataDetail> uplinkDetails = patterns.stream()
                .map(pattern -> createLinkDataDetail(pattern, calculateSumForPattern(pattern, uplinkData, timeUnit)))
                .collect(Collectors.toList());

        List<DashboardDetailsResultVO.LinkDataDetail> downlinkDetails = patterns.stream()
                .map(pattern -> createLinkDataDetail(pattern, calculateSumForPattern(pattern, downlinkData, timeUnit)))
                .collect(Collectors.toList());

        DashboardDetailsResultVO resultVo = new DashboardDetailsResultVO();
        resultVo.setUplinkDetails(sortLinkDataDetails(uplinkDetails));
        resultVo.setDownlinkDetails(sortLinkDataDetails(downlinkDetails));
        return resultVo;
    }

    /**
     * 生成指定时间单位的时间模式列表
     * 根据时间范围和时间单位生成对应的模式列表
     *
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @param timeUnit      时间单位枚举
     * @param limit         最大数据条数限制
     * @return 时间模式字符串列表，格式根据时间单位而定
     */
    private List<String> generateTimePatterns(LocalDateTime startDateTime, LocalDateTime endDateTime,
                                              TimeUnitEnum timeUnit, long limit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeUnit.getPattern());

        return switch (timeUnit) {
            case MINUTE -> Stream.iterate(startDateTime, dt -> !dt.isAfter(endDateTime), dt -> dt.plusMinutes(1))
                    .limit(limit)
                    .map(dt -> dt.format(formatter))
                    .collect(Collectors.toList());

            case HOUR -> Stream.iterate(startDateTime.withMinute(0).withSecond(0).withNano(0),
                            dt -> !dt.isAfter(endDateTime), dt -> dt.plusHours(1))
                    .limit(limit)
                    .map(dt -> dt.format(formatter))
                    .collect(Collectors.toList());

            case DAY -> Stream.iterate(startDateTime.toLocalDate().atStartOfDay(),
                            dt -> !dt.isAfter(endDateTime), dt -> dt.plusDays(1))
                    .limit(limit)
                    .map(dt -> dt.format(formatter))
                    .collect(Collectors.toList());
        };
    }

    /**
     * 获取指定时间范围内的所有日期键列表
     * 包含开始日期和结束日期
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 日期键字符串列表，格式为 yyyyMMdd
     */
    private List<String> getDateKeysInRange(LocalDateTime start, LocalDateTime end) {
        return start.toLocalDate().datesUntil(end.toLocalDate().plusDays(1))
                .map(date -> date.format(YYYYMMDD_FORMATTER))
                .collect(Collectors.toList());
    }

    /**
     * 批量获取Redis Hash数据
     * 使用顺序流保证数据顺序性
     *
     * @param dateKeys         日期键列表，格式为 yyyyMMdd
     * @param linkDataTypeEnum 链路数据类型枚举（上行或下行）
     * @return 包含日期键和对应Hash数据的映射表，
     * 外层Key: 日期字符串(yyyyMMdd)，
     * 内层Key: 时间字段字符串，
     * 内层Value: 数据值
     */
    private Map<String, Map<String, Integer>> batchGetHashData(List<String> dateKeys, LinkDataTypeEnum linkDataTypeEnum) {
        return dateKeys.stream()
                .collect(Collectors.toMap(
                        dateKey -> dateKey,
                        dateKey -> {
                            CacheHashKey hashKey = linkDataTypeEnum == LinkDataTypeEnum.UPLINK
                                    ? UpLinkDataCounterCacheKeyBuilder.build(dateKey)
                                    : DownLinkDataCounterCacheKeyBuilder.build(dateKey);

                            return Optional.ofNullable(cachePlusOps.hGetAll(hashKey))
                                    .orElse(Collections.emptyMap())
                                    .entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(
                                            entry -> entry.getKey().toString(),
                                            entry -> parseCacheResultValue(entry.getValue()),
                                            (existing, replacement) -> existing,
                                            LinkedHashMap::new
                                    ));
                        },
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 解析CacheResult对象的值
     *
     * @param cacheResult CacheResult对象，可能包含各种类型的值
     * @return 解析后的整数值，如果值为空或无效则返回0
     */
    private int parseCacheResultValue(CacheResult<Object> cacheResult) {
        return Optional.ofNullable(cacheResult)
                .filter(result -> !result.isNull() && !result.isNullVal())
                .map(CacheResult::getValue)
                .map(this::safeParseInt)
                .orElse(0);
    }

    /**
     * 安全解析对象为整数值
     * 支持多种数据类型转换：Integer、Long、String、Number等
     *
     * @param value 需要解析的对象值
     * @return 解析后的整数值，如果解析失败则返回0
     */
    private int safeParseInt(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            if (value instanceof Integer integerValue) {
                return integerValue;
            } else if (value instanceof Long longValue) {
                return longValue.intValue();
            } else if (value instanceof String stringValue) {
                if (!stringValue.isBlank()) {
                    return Integer.parseInt(stringValue.trim());
                }
            } else if (value instanceof Number numberValue) {
                return numberValue.intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析整数值: {}, 使用默认值0", value);
            return 0;
        }
    }

    /**
     * 计算特定时间模式的数据总和
     * 根据不同的时间单位采用不同的计算策略
     *
     * @param pattern  时间模式字符串，格式由TimeUnitEnum定义
     * @param hashData Hash数据映射表，包含日期键和对应的时间字段数据
     * @param timeUnit 时间单位枚举
     * @return 该时间模式下的数据总和
     */
    private int calculateSumForPattern(String pattern, Map<String, Map<String, Integer>> hashData, TimeUnitEnum timeUnit) {
        return switch (timeUnit) {
            case MINUTE -> {
                // 分钟级别：pattern为yyyyMMddHHmm，前8位是日期，后4位是时间
                String minuteDateKey = timeUnit.extractDateKey(pattern);
                String minuteTimeField = timeUnit.extractTimeField(pattern);
                yield Optional.ofNullable(hashData.get(minuteDateKey))
                        .map(dateData -> dateData.getOrDefault(minuteTimeField, 0))
                        .orElse(0);
            }
            case HOUR -> {
                // 小时级别：pattern为yyyyMMddHH，前8位是日期，后2位是小时
                String hourDateKey = timeUnit.extractDateKey(pattern);
                String hourPrefix = timeUnit.extractTimeField(pattern);
                yield Optional.ofNullable(hashData.get(hourDateKey))
                        .map(dateData -> dateData.entrySet().stream()
                                .filter(entry -> entry.getKey().startsWith(hourPrefix))
                                .mapToInt(Map.Entry::getValue)
                                .sum())
                        .orElse(0);
            }
            case DAY -> {
                // 天级别：pattern为yyyyMMdd，直接作为日期key
                yield Optional.ofNullable(hashData.get(pattern))
                        .map(dateData -> dateData.values().stream()
                                .mapToInt(Integer::intValue)
                                .sum())
                        .orElse(0);
            }
        };
    }

    /**
     * 对链路数据详情按时间字符串进行排序
     *
     * @param details 需要排序的链路数据详情列表
     * @return 按时间字符串升序排列后的列表
     */
    private List<DashboardDetailsResultVO.LinkDataDetail> sortLinkDataDetails(List<DashboardDetailsResultVO.LinkDataDetail> details) {
        return details.stream()
                .sorted(Comparator.comparing(DashboardDetailsResultVO.LinkDataDetail::getTimeString))
                .collect(Collectors.toList());
    }

    /**
     * 创建链路数据详情对象
     *
     * @param dateTime 时间字符串
     * @param count    数据计数
     * @return 新创建的链路数据详情对象
     */
    private DashboardDetailsResultVO.LinkDataDetail createLinkDataDetail(String dateTime, int count) {
        return new DashboardDetailsResultVO.LinkDataDetail(count, dateTime);
    }

}