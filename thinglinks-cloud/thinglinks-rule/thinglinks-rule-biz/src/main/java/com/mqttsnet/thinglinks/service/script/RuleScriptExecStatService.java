package com.mqttsnet.thinglinks.service.script;

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.RuleScriptExecStatCacheKeyBuilder;
import com.mqttsnet.thinglinks.vo.result.script.RuleScriptExecStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 规则脚本执行统计 ── 按脚本唯一键累计 total / success / fail(类似 mqs 数据上报统计)。
 *
 * <p>计数为旁路统计:失败仅告警不抛,绝不影响脚本执行主链路。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleScriptExecStatService {

    private static final String FIELD_TOTAL = "total";
    private static final String FIELD_SUCCESS = "success";
    private static final String FIELD_FAIL = "fail";

    private final CachePlusOps cachePlusOps;

    /**
     * 记一次执行 ── total +1,成功/失败各自 +1。scriptUniqueKey 为空则跳过(如纯调试无身份)。
     *
     * @param scriptUniqueKey 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)
     * @param success         本次执行是否成功
     */
    public void record(String scriptUniqueKey, boolean success) {
        if (StrUtil.isBlank(scriptUniqueKey)) {
            return;
        }
        try {
            cachePlusOps.hIncrBy(RuleScriptExecStatCacheKeyBuilder.buildHashField(scriptUniqueKey, FIELD_TOTAL), 1L);
            cachePlusOps.hIncrBy(RuleScriptExecStatCacheKeyBuilder.buildHashField(
                scriptUniqueKey, success ? FIELD_SUCCESS : FIELD_FAIL), 1L);
        } catch (Exception e) {
            log.warn("[rule.script.stat] record failed key={} err={}", scriptUniqueKey, e.getMessage());
        }
    }

    /**
     * 查某脚本的累计执行统计。
     *
     * @param scriptUniqueKey 脚本唯一键
     * @return 统计 VO(读不到则全 0)
     */
    public RuleScriptExecStatVO query(String scriptUniqueKey) {
        RuleScriptExecStatVO vo = new RuleScriptExecStatVO();
        if (StrUtil.isBlank(scriptUniqueKey)) {
            return vo;
        }
        try {
            Map<Object, CacheResult<Object>> raw = cachePlusOps.hGetAll(RuleScriptExecStatCacheKeyBuilder.build(scriptUniqueKey));
            vo.setTotal(parse(raw, FIELD_TOTAL));
            vo.setSuccess(parse(raw, FIELD_SUCCESS));
            vo.setFail(parse(raw, FIELD_FAIL));
        } catch (Exception e) {
            log.warn("[rule.script.stat] query failed key={} err={}", scriptUniqueKey, e.getMessage());
        }
        return vo;
    }

    private long parse(Map<Object, CacheResult<Object>> raw, String field) {
        if (raw == null) {
            return 0L;
        }
        CacheResult<Object> result = raw.get(field);
        Object value = result == null ? null : result.getRawValue();
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
