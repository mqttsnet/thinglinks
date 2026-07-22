package com.mqttsnet.thinglinks.service.script.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.groovy.entity.EngineExecutorResult;
import com.mqttsnet.basic.groovy.entity.ExecuteParams;
import com.mqttsnet.basic.groovy.entity.ScriptEntry;
import com.mqttsnet.basic.groovy.entity.ScriptQuery;
import com.mqttsnet.basic.groovy.executor.EngineExecutor;
import com.mqttsnet.basic.groovy.loader.ScriptLoader;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.bridge.event.publisher.BridgeEventPublisher;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import com.mqttsnet.thinglinks.enumeration.script.ExecutionStatusEnum;
import com.mqttsnet.thinglinks.manager.script.RuleGroovyScriptManager;
import com.mqttsnet.thinglinks.service.script.RuleGroovyScriptService;
import com.mqttsnet.thinglinks.service.script.RuleScriptExecStatService;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptPageQuery;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import com.mqttsnet.thinglinks.vo.result.script.RuleGroovyScriptResultVO;
import com.mqttsnet.thinglinks.vo.result.script.RuleScriptExecStatVO;
import com.mqttsnet.thinglinks.vo.save.script.RuleGroovyScriptSaveVO;
import com.mqttsnet.thinglinks.vo.update.script.RuleGroovyScriptUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 规则脚本表业务实现类。
 *
 * @author mqttsnet
 * @date 2025-03-24 09:54:10
 * @create [2025-03-24 09:54:10] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleGroovyScriptServiceImpl extends SuperServiceImpl<RuleGroovyScriptManager, Long, RuleGroovyScript> implements RuleGroovyScriptService {


    private final EngineExecutor engineExecutor;

    private final ScriptLoader scriptLoader;

    /**
     * 桥接领域事件发布器:Service 只调 publisher,不直接持有 ApplicationEventPublisher / CachePlusOps,缓存失效是 Listener/Handler 的职责。
     */
    private final BridgeEventPublisher bridgeEventPublisher;

    /**
     * 脚本执行统计(旁路 Redis 计数)。
     */
    private final RuleScriptExecStatService ruleScriptExecStatService;


    @Override
    public List<RuleGroovyScript> listEnabledTransformScripts(String channelCode, String productIdentification, String productVersionNo) {
        if (StrUtil.hasBlank(channelCode, productIdentification, productVersionNo)) {
            return Collections.emptyList();
        }
        return list(Wrappers.<RuleGroovyScript>lambdaQuery()
            .eq(RuleGroovyScript::getScriptType, GroovyScriptCacheKeyBuilder.TRANSFORM_SCRIPT_TYPE)
            .eq(RuleGroovyScript::getChannelCode, channelCode)
            .eq(RuleGroovyScript::getProductIdentification, productIdentification)
            .eq(RuleGroovyScript::getObjectVersion, productVersionNo)
            .eq(RuleGroovyScript::getEnable, Boolean.TRUE));
    }

    @Override
    public List<RuleGroovyScriptResultVO> getRuleGroovyScriptResultVOList(RuleGroovyScriptPageQuery query) {
        List<RuleGroovyScript> scriptList = superManager.getRuleGroovyScriptList(query);
        return BeanPlusUtil.copyToList(scriptList, RuleGroovyScriptResultVO.class);
    }


    @Override
    public Boolean flushGroovyScriptCache() {
        // 全量刷新:查所有启用脚本,逐条发变更事件 ── 与新增/编辑/删除走同一套机制,
        // 由 Listener 按脚本身份路由到对应缓存(转换脚本→HASH 桶 / 其余→String key),job 不做任何类型定制。
        // 同时覆盖 Redis 冷启动/清空后的缓存预热。
        List<RuleGroovyScript> scripts = getSuperManager().list(Wrappers.<RuleGroovyScript>lambdaQuery()
            .eq(RuleGroovyScript::getEnable, Boolean.TRUE));
        if (CollUtil.isEmpty(scripts)) {
            log.info("刷新规则脚本缓存,TenantId:{} 无启用脚本", ContextUtil.getTenantIdStr());
            return true;
        }
        scripts.forEach(script -> {
            try {
                bridgeEventPublisher.publishGroovyScriptChangedEvent(script);
            } catch (Exception e) {
                log.error("刷新规则脚本缓存失败,TenantId:{},规则脚本ID:{}", ContextUtil.getTenantIdStr(), script.getId(), e);
            }
        });
        log.info("刷新规则脚本缓存完成,TenantId:{} 脚本数={}", ContextUtil.getTenantIdStr(), scripts.size());
        return true;
    }


    /**
     * 保存规则脚本 ── 持久化 + 发事件,缓存写入由 BridgeRuleChangedEventListener.onGroovyScriptChanged 处理。
     *
     * @param saveVO 保存参数
     * @return 保存后的脚本数据
     */
    @Override
    public RuleGroovyScriptSaveVO saveGroovyScript(RuleGroovyScriptSaveVO saveVO) {
        log.info("保存规则脚本参数: {}", saveVO);

        // 参数校验
        checkSaveParams(saveVO);

        // 构建实体
        RuleGroovyScript script = buildScriptEntity(saveVO);

        // 保存操作
        superManager.save(script);

        // 发事件 → Listener 按 enable 决定 register / clear
        bridgeEventPublisher.publishGroovyScriptChangedEvent(script);

        return BeanPlusUtil.toBeanIgnoreError(script, RuleGroovyScriptSaveVO.class);
    }


    /**
     * 更新规则脚本 ── 持久化 + 发事件,缓存刷新由 Listener 处理。
     * ENABLE / DISABLE 也走 update 路径(前端 toggle 也是 PUT 全量更新),复用 Changed 事件。
     *
     * @param updateVO 更新参数
     * @return 更新后的脚本数据
     */
    @Override
    public RuleGroovyScriptUpdateVO updateGroovyScript(RuleGroovyScriptUpdateVO updateVO) {
        log.info("更新规则脚本参数: {}", updateVO);

        // 参数校验
        checkUpdateParams(updateVO);

        // 验证记录存在性
        RuleGroovyScript existingScript = superManager.getById(updateVO.getId());
        if (existingScript == null) {
            throw BizException.wrap("规则脚本不存在");
        }

        // 构建更新实体(身份字段允许编辑)
        RuleGroovyScript script = buildUpdateEntity(updateVO);

        // 执行更新
        superManager.updateById(script);

        // 新身份:刷新新缓存(转换脚本→新桶 / 其余→新 String key)
        bridgeEventPublisher.publishGroovyScriptChangedEvent(script);
        // 身份字段被改过(渠道/产品/版本/主题/类型)→ 旧身份缓存要清掉,否则旧桶/旧 key 残留 = 幽灵脚本
        if (isIdentityChanged(existingScript, script)) {
            bridgeEventPublisher.publishGroovyScriptDeletedEvent(existingScript);
        }

        return updateVO;
    }

    @Override
    public Boolean deleteGroovyScript(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        RuleGroovyScript script = superManager.getById(id);
        if (Objects.isNull(script)) {
            throw BizException.wrap("脚本信息不存在!");
        }

        boolean removed = superManager.removeById(id);
        if (removed) {
            // 发事件 → Listener 用事件源的 4 个 identity 字段重建 cacheKey 清理缓存(DB 行已删除)
            bridgeEventPublisher.publishGroovyScriptDeletedEvent(script);
        }
        return removed;
    }

    @Override
    public GroovyScriptEngineExecutorResultVO executeScript(RuleGroovyScriptExecuteScriptParam param) throws Exception {
        boolean success = false;
        try {
            ExecuteParams executeParams = JSON.parseObject(param.getExecuteParams(), ExecuteParams.class);
            // 编译并执行
            ScriptQuery scriptQuery = new ScriptQuery(param.getScriptUniqueKey());
            ScriptEntry scriptEntry = scriptLoader.load(scriptQuery);
            EngineExecutorResult result = engineExecutor.execute(scriptEntry, executeParams);
            GroovyScriptEngineExecutorResultVO resultVO = BeanPlusUtil.toBeanIgnoreError(result, GroovyScriptEngineExecutorResultVO.class);
            resultVO.setExecutionStatus(
                ExecutionStatusEnum.fromValue(Integer.valueOf(result.getExecutionStatus().getCode())).orElse(ExecutionStatusEnum.FAILED)
            );
            // 脚本返回里的 Groovy GString 等递归转普通 String,避免 JSON 序列化抖出内部结构
            resultVO.setContext(normalizeGroovyTypes(resultVO.getContext()));
            success = resultVO.getExecutionStatus() == ExecutionStatusEnum.SUCCESS;
            return resultVO;
        } finally {
            // 执行统计(旁路):按 scriptUniqueKey 计数,空则不计
            ruleScriptExecStatService.record(param.getScriptUniqueKey(), success);
        }
    }

    @Override
    public GroovyScriptEngineExecutorResultVO runDirectCompile(RuleGroovyScriptDirectCompileParam param) throws Exception {
        boolean success = false;
        try {
            ExecuteParams executeParams = JSON.parseObject(param.getExecuteParams(), ExecuteParams.class);
            if (executeParams == null) {
                executeParams = new ExecuteParams();
            }
            // 注入 log 绑定:脚本里 log.info(...) 既进 rule 服务日志(运行时可观测),又收集回显给在线调试(写脚本不再黑盒)
            ScriptLogCollector logCollector = new ScriptLogCollector();
            executeParams.put("log", logCollector);
            // 编译并执行
            ScriptEntry scriptEntry = scriptLoader.compileScript(param.getScriptContent());
            EngineExecutorResult result = engineExecutor.execute(scriptEntry, executeParams);
            GroovyScriptEngineExecutorResultVO resultVO = BeanPlusUtil.toBeanIgnoreError(result, GroovyScriptEngineExecutorResultVO.class);
            resultVO.setExecutionStatus(
                ExecutionStatusEnum.fromValue(Integer.valueOf(result.getExecutionStatus().getCode())).orElse(ExecutionStatusEnum.FAILED)
            );
            resultVO.setLogs(logCollector.getLogs());
            // 脚本返回里的 Groovy GString 等递归转普通 String,避免 JSON 序列化抖出内部结构(如 topic 用 "...${x}..." 拼接)
            resultVO.setContext(normalizeGroovyTypes(resultVO.getContext()));
            success = resultVO.getExecutionStatus() == ExecutionStatusEnum.SUCCESS;
            return resultVO;
        } finally {
            // 执行统计(旁路):scriptUniqueKey 由调用方(mqs 运行时 / 调试)传入,空则不计
            ruleScriptExecStatService.record(param.getScriptUniqueKey(), success);
        }
    }

    @Override
    public RuleScriptExecStatVO getExecStat(Long id) {
        RuleGroovyScript script = superManager.getById(id);
        if (script == null) {
            return new RuleScriptExecStatVO();
        }
        // 与执行侧 / RuleGroovyScriptResultVO#buildOnlyKey 一致:scriptType:channelCode:productIdentification:topicPattern
        String scriptUniqueKey = String.join(StrPool.COLON,
            script.getScriptType(), script.getChannelCode(), script.getProductIdentification(), script.getTopicPattern());
        return ruleScriptExecStatService.query(scriptUniqueKey);
    }

    /**
     * 递归把脚本返回值里的 Groovy {@code GString}(及其它非 {@code String} 的 {@code CharSequence})转成普通 String。
     * 否则带插值的字符串(如 {@code topic: "/v1/devices/${id}/datas"})会被 JSON 序列化成 {@code {values,strings,bytes}} 内部结构。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object normalizeGroovyTypes(Object value) {
        if (value instanceof CharSequence && !(value instanceof String)) {
            return value.toString();
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            map.replaceAll((k, v) -> normalizeGroovyTypes(v));
            return map;
        }
        if (value instanceof List) {
            List list = (List) value;
            list.replaceAll(this::normalizeGroovyTypes);
            return list;
        }
        return value;
    }


    /**
     * 校验保存参数
     *
     * @param saveVO 保存参数
     */
    private void checkSaveParams(RuleGroovyScriptSaveVO saveVO) {
        // 校验唯一
        checkScriptUnique(saveVO.getScriptType(),
            saveVO.getChannelCode(), saveVO.getProductIdentification(), saveVO.getTopicPattern(),
            saveVO.getObjectVersion(), null);


    }

    /**
     * 校验更新参数
     *
     * @param updateVO 更新参数
     */
    private void checkUpdateParams(RuleGroovyScriptUpdateVO updateVO) {
        // 校验唯一
        checkScriptUnique(updateVO.getScriptType(),
            updateVO.getChannelCode(), updateVO.getProductIdentification(), updateVO.getTopicPattern(),
            updateVO.getObjectVersion(), updateVO.getId());
    }

    /**
     * 校验脚本唯一性(4 个业务身份字段组合 + 排除 excludeId)。
     *
     * @param scriptType            脚本类型
     * @param channelCode           渠道编码
     * @param productIdentification 产品标识
     * @param topicPattern          主题模式
     * @param excludeId             需排除的脚本 ID(更新时排除自身,可为 null)
     */
    private void checkScriptUnique(String scriptType,
                                   String channelCode, String productIdentification, String topicPattern,
                                   String objectVersion, Long excludeId) {
        // 设备上行前置转换脚本(topicInboundTransform)以「产品 + 产品发布版本」为维度,版本参与唯一性判定
        // (同一产品不同发布版本可配置不同转换脚本);存量类型仍按 4 字段业务身份判重,版本仅元数据不参与。
        boolean versioned = GroovyScriptCacheKeyBuilder.TRANSFORM_SCRIPT_TYPE.equals(scriptType);
        long count = superManager.count(Wraps.<RuleGroovyScript>lbQ()
            .eq(RuleGroovyScript::getScriptType, scriptType)
            .eq(RuleGroovyScript::getChannelCode, channelCode)
            .eq(RuleGroovyScript::getProductIdentification, productIdentification)
            .eq(RuleGroovyScript::getTopicPattern, topicPattern)
            .eq(versioned, RuleGroovyScript::getObjectVersion, objectVersion)
            .ne(excludeId != null, RuleGroovyScript::getId, excludeId));

        if (count > 0) {
            throw BizException.wrap("该组合配置已存在!");
        }
    }


    /**
     * 构建保存实体
     *
     * @param saveVO 保存参数
     * @return 构建好的脚本实体
     */
    private RuleGroovyScript buildScriptEntity(RuleGroovyScriptSaveVO saveVO) {
        return Builder.of(RuleGroovyScript::new)
            .with(RuleGroovyScript::setName, saveVO.getName())
            .with(RuleGroovyScript::setAppId, saveVO.getAppId())
            .with(RuleGroovyScript::setScriptType, saveVO.getScriptType())
            .with(RuleGroovyScript::setChannelCode, saveVO.getChannelCode())
            .with(RuleGroovyScript::setProductIdentification, saveVO.getProductIdentification())
            .with(RuleGroovyScript::setTopicPattern, saveVO.getTopicPattern())
            .with(RuleGroovyScript::setObjectVersion, saveVO.getObjectVersion())
            .with(RuleGroovyScript::setEnable, saveVO.getEnable())
            .with(RuleGroovyScript::setScriptContent, saveVO.getScriptContent())
            .with(RuleGroovyScript::setExtendParams, saveVO.getExtendParams())
            .with(RuleGroovyScript::setRemark, saveVO.getRemark())
            .with(RuleGroovyScript::setCreatedOrgId, ContextUtil.getCurrentDeptId())
            .build();
    }

    /**
     * 构建更新实体。脚本身份字段(script_type / channel_code / product_identification / topic_pattern / object_version)
     * 全部允许编辑:唯一性由 {@link #checkUpdateParams}→{@link #checkScriptUnique}(排除自身)校验;
     * 身份变更后的缓存换桶(旧身份清理 + 新身份刷新)由 {@link #updateGroovyScript} 通过
     * 「新身份 Changed + 旧身份 Deleted」两个事件交给 Listener 处理。
     *
     * @param updateVO 更新参数
     * @return 构建好的更新脚本实体
     */
    private RuleGroovyScript buildUpdateEntity(RuleGroovyScriptUpdateVO updateVO) {
        return Builder.of(RuleGroovyScript::new)
            .with(RuleGroovyScript::setId, updateVO.getId())
            .with(RuleGroovyScript::setName, updateVO.getName())
            .with(RuleGroovyScript::setAppId, updateVO.getAppId())
            // ===== 身份字段:全部允许编辑(验重见 checkUpdateParams,换桶见 updateGroovyScript) =====
            .with(RuleGroovyScript::setScriptType, updateVO.getScriptType())
            .with(RuleGroovyScript::setChannelCode, updateVO.getChannelCode())
            .with(RuleGroovyScript::setProductIdentification, updateVO.getProductIdentification())
            .with(RuleGroovyScript::setTopicPattern, updateVO.getTopicPattern())
            .with(RuleGroovyScript::setObjectVersion, updateVO.getObjectVersion())
            // ===== 其它字段 =====
            .with(RuleGroovyScript::setEnable, updateVO.getEnable())
            .with(RuleGroovyScript::setScriptContent, updateVO.getScriptContent())
            .with(RuleGroovyScript::setExtendParams, updateVO.getExtendParams())
            .with(RuleGroovyScript::setRemark, updateVO.getRemark())
            .with(RuleGroovyScript::setCreatedOrgId, ContextUtil.getCurrentDeptId())
            .build();
    }

    /**
     * 身份字段是否变化(脚本类型/渠道/产品/主题/版本)。变化意味着编辑后落到新桶/新 key,
     * 旧桶/旧 key 需靠旧身份 Deleted 事件清理,避免残留成幽灵脚本。
     */
    private boolean isIdentityChanged(RuleGroovyScript before, RuleGroovyScript after) {
        return !StrUtil.equals(before.getScriptType(), after.getScriptType())
            || !StrUtil.equals(before.getChannelCode(), after.getChannelCode())
            || !StrUtil.equals(before.getProductIdentification(), after.getProductIdentification())
            || !StrUtil.equals(before.getTopicPattern(), after.getTopicPattern())
            || !StrUtil.equals(before.getObjectVersion(), after.getObjectVersion());
    }

}


