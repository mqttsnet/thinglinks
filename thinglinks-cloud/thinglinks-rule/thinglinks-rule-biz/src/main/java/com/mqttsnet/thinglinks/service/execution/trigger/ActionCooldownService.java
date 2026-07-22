package com.mqttsnet.thinglinks.service.execution.trigger;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.condition.model.dto.AppointEffectiveTimeDTO;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.rule.trigger.RuleTriggerCacheKeys;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 动作冷却闸 ── 条件满足、执行动作前的最小间隔控制。
 *
 * <p><b>为什么必须有</b>:事件驱动下条件持续满足时每条上报都会评估通过,不加闸会造成
 * 动作风暴(告警轰炸/命令重复下发);轮询时代的节奏由 cron 周期隐式限定,事件化后
 * 需要显式冷却。冷却窗口复用规则 {@code appointContent} 的 {@code cooldownSeconds}
 * (缺省回落 {@code frequency},再缺省 60s)—— 存量规则的"执行频率"语义无缝转为
 * "动作最小间隔",迁移后行为节奏与原轮询接近。
 *
 * <p><b>事件/定时双触发防重</b>:冷却 key 按 (rule, condition, device) 维度,事件路径与
 * 定时任务路径共用同一把闸 —— 存量规则的 XXL-Job 未停用期间,先到者执行、后到者被闸住,
 * 天然防重复动作。
 *
 * <p>实现:INCR 返回 1 = 拿到执行权(补 EXPIRE);>1 = 冷却中(顺带自愈无 TTL 的孤儿键,
 * 防止 INCR 与 EXPIRE 之间进程崩溃导致永久闸死)。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionCooldownService {

    private final CachePlusOps cachePlusOps;

    /** 冷却窗口缺省值(秒) */
    private static final long DEFAULT_COOLDOWN_SECONDS = 60L;

    /**
     * 尝试获取动作执行权。
     *
     * @param context            执行上下文(取 appointContent 冷却配置与事件设备)
     * @param conditionPolicyDTO 当前条件(uuid 维度隔离)
     * @return true=可执行动作;false=冷却中,跳过
     */
    public boolean tryAcquire(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        long cooldownSeconds = resolveCooldownSeconds(context);
        if (cooldownSeconds <= 0) {
            return true;
        }
        String device = context.getTriggerEvent() != null
                && StrUtil.isNotBlank(context.getTriggerEvent().getDeviceIdentification())
                ? context.getTriggerEvent().getDeviceIdentification() : "GLOBAL";
        // 条件维度用 conditionType 区分(条件 DTO 无 uuid;控制台每规则每类型一条条件,粒度足够)
        CacheKey key = RuleTriggerCacheKeys.actionCooldown(context.getRuleIdentification(),
                String.valueOf(conditionPolicyDTO.getConditionType()), device, cooldownSeconds);
        Long count = cachePlusOps.incr(key);
        if (count != null && count == 1L) {
            cachePlusOps.expire(key);
            return true;
        }
        // 自愈:INCR 后进程崩溃可能留下无 TTL 的孤儿键,永久闸死该规则 —— 检测到即补 TTL
        Long ttl = cachePlusOps.ttl(key);
        if (ttl != null && ttl < 0) {
            cachePlusOps.expire(key);
        }
        log.info("[ActionCooldown] suppressed rule={} conditionType={} device={} cooldown={}s",
                context.getRuleIdentification(), conditionPolicyDTO.getConditionType(), device, cooldownSeconds);
        return false;
    }

    /**
     * 冷却窗口解析(强类型 {@link AppointEffectiveTimeDTO},与保存侧同一结构):
     * cooldownSeconds > frequency > 60s,非正值逐级跳过。
     * cooldownSeconds 是较新的 util 字段,这里用反射兼容尚未发布该 getter 的依赖版本。
     */
    private long resolveCooldownSeconds(PolicyContext context) {
        try {
            String appointContent = context.getRulePolicyDTO() == null
                    ? null : context.getRulePolicyDTO().getAppointContent();
            if (StrUtil.isNotBlank(appointContent)) {
                AppointEffectiveTimeDTO appoint = JsonUtil.parse(appointContent, AppointEffectiveTimeDTO.class);
                if (appoint != null) {
                    Long cooldownSeconds = readCooldownSeconds(appoint);
                    if (cooldownSeconds != null && cooldownSeconds > 0) {
                        return cooldownSeconds;
                    }
                    if (appoint.getFrequency() != null && appoint.getFrequency() > 0) {
                        return appoint.getFrequency();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[ActionCooldown] parse appointContent failed rule={} err={}",
                    context.getRuleIdentification(), e.getMessage());
        }
        return DEFAULT_COOLDOWN_SECONDS;
    }

    private Long readCooldownSeconds(AppointEffectiveTimeDTO appoint) {
        try {
            Object value = appoint.getClass().getMethod("getCooldownSeconds").invoke(appoint);
            return value instanceof Number number ? number.longValue() : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
