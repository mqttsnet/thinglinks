package com.mqttsnet.thinglinks.service.execution.trigger;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.condition.model.dto.AppointEffectiveTimeDTO;
import com.mqttsnet.basic.jackson.JsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 规则生效时间窗校验 ── {@code effectiveType=1}(指定时间)时按 {@code appointContent}
 * 的周几勾选 + 时段判断当前时刻是否在窗口内。
 *
 * <p>反序列化走 {@link AppointEffectiveTimeDTO} 强类型结构(与保存侧同一 DTO,字段增减编译期可见),
 * 不做手工 JSON 取值。解析失败一律放行(宁多评估不漏触发),只记 warn。
 *
 * @author mqttsnet
 */
@Slf4j
public final class RuleEffectiveWindowChecker {

    /** 生效类型:指定时间段 */
    private static final int EFFECTIVE_TYPE_APPOINT = 1;

    private RuleEffectiveWindowChecker() {
    }

    /**
     * 当前时刻是否在规则生效窗口内。
     *
     * @param effectiveType  规则生效类型(0/null=永久生效)
     * @param appointContent 指定内容 JSON(结构见 {@link AppointEffectiveTimeDTO})
     * @return true=生效(含永久/解析失败放行)
     */
    public static boolean isEffectiveNow(Integer effectiveType, String appointContent) {
        if (effectiveType == null || effectiveType != EFFECTIVE_TYPE_APPOINT || StrUtil.isBlank(appointContent)) {
            return true;
        }
        AppointEffectiveTimeDTO appoint;
        try {
            appoint = JsonUtil.parse(appointContent, AppointEffectiveTimeDTO.class);
        } catch (Exception e) {
            log.warn("[EffectiveWindow] parse appointContent failed, allow by default. err={}", e.getMessage());
            return true;
        }
        if (appoint == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!weekMatches(appoint.getWeek(), now.getDayOfWeek())) {
            return false;
        }
        return timeframeMatches(appoint.getTimeframe(), now.toLocalTime());
    }

    /**
     * 周几匹配:未配置周信息视为全周生效;已配置则当前周几必须被勾选。
     */
    private static boolean weekMatches(List<AppointEffectiveTimeDTO.WeekDay> week, DayOfWeek today) {
        if (CollUtil.isEmpty(week)) {
            return true;
        }
        String todayName = today.name().toLowerCase(Locale.ROOT);
        return week.stream()
                .filter(day -> day != null && Boolean.TRUE.equals(day.getChecked()))
                .anyMatch(day -> todayName.equalsIgnoreCase(day.getEg()));
    }

    /**
     * 时段匹配:未配置/起止缺失视为全天生效;支持跨天时段(如 22:00-06:00)。
     * 时间格式非法(非 HH:mm)放行并 warn。
     */
    private static boolean timeframeMatches(AppointEffectiveTimeDTO.TimeFrame timeframe, LocalTime nowTime) {
        if (timeframe == null || StrUtil.hasBlank(timeframe.getStartTime(), timeframe.getEndTime())) {
            return true;
        }
        try {
            LocalTime startTime = LocalTime.parse(timeframe.getStartTime().trim());
            LocalTime endTime = LocalTime.parse(timeframe.getEndTime().trim());
            return startTime.isAfter(endTime)
                    // 跨天时段:今天 start 之后 或 明晨 end 之前
                    ? !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime)
                    : !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
        } catch (Exception e) {
            log.warn("[EffectiveWindow] invalid timeframe [{} - {}], allow by default. err={}",
                    timeframe.getStartTime(), timeframe.getEndTime(), e.getMessage());
            return true;
        }
    }
}
