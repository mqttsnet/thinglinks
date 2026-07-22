package com.mqttsnet.thinglinks.config.groovy.loader;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.groovy.compiler.DynamicCodeCompiler;
import com.mqttsnet.basic.groovy.entity.ScriptEntry;
import com.mqttsnet.basic.groovy.entity.ScriptQuery;
import com.mqttsnet.basic.groovy.loader.ScriptLoader;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import com.mqttsnet.thinglinks.manager.script.RuleGroovyScriptManager;
import com.mqttsnet.thinglinks.record.script.ScriptIdentifier;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptPageQuery;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从Redis里加载脚本loader(支持 cache-aside 溯源:cache miss → 查 DB → 自动回填缓存)
 *
 * @author mqttsnet 2022/09/25 13:09
 */
@Slf4j
public class RedisScriptLoader implements ScriptLoader {


    private final RuleCacheDataHelper ruleCacheDataHelper;

    private final DynamicCodeCompiler dynamicCodeCompiler;

    /**
     * DB 回源用 ── cache miss 时按 4 字段身份查脚本。
     */
    private final RuleGroovyScriptManager ruleGroovyScriptManager;

    public RedisScriptLoader(RuleCacheDataHelper ruleCacheDataHelper,
                             DynamicCodeCompiler dynamicCodeCompiler,
                             RuleGroovyScriptManager ruleGroovyScriptManager) {
        this.ruleCacheDataHelper = ruleCacheDataHelper;
        this.dynamicCodeCompiler = dynamicCodeCompiler;
        this.ruleGroovyScriptManager = ruleGroovyScriptManager;
    }


    /**
     * 从Redis加载脚本(cache-aside ── miss 时回源 DB 并自动回填缓存)
     *
     * @param query 查询对象(uniqueKey 解析为 4 字段身份)
     * @return {@link ScriptEntry} 脚本对象
     * @throws Exception
     */
    @Override
    public ScriptEntry load(ScriptQuery query) throws Exception {
        if (StrUtil.isBlank(query.getUniqueKey())) {
            throw BizException.wrap("脚本唯一键不能为空");
        }
        RuleGroovyScriptQuery ruleGroovyScriptQuery = new RuleGroovyScriptQuery(query.getUniqueKey());
        CacheKey cacheKey = ScriptIdentifier.buildCacheKey(ruleGroovyScriptQuery);
        // cache-aside:miss 时按 4 字段身份回源 DB(只取 enable=true 的脚本) + 自动回填缓存
        String script = ruleCacheDataHelper.getScriptContent(cacheKey,
                k -> loadScriptContentFromDb(ruleGroovyScriptQuery))
                .map(Object::toString)
                .orElseThrow(() -> BizException.wrap("脚本不存在"));
        // 获取脚本指纹
        String fingerprint = DigestUtils.md5DigestAsHex(script.getBytes());
        // 创建脚本对象
        ScriptEntry scriptEntry = new ScriptEntry(cacheKey.getKey(), script, fingerprint, System.currentTimeMillis());
        // 动态加载脚本为Class
        Class<?> aClass = dynamicCodeCompiler.compile(scriptEntry);
        scriptEntry.setClazz(aClass);
        return scriptEntry;
    }

    /**
     * 从Redis中加载脚本
     *
     * @param cacheKey 缓存Key
     * @return {@link ScriptEntry} 脚本对象
     * @throws Exception
     */
    @Override
    public ScriptEntry load(@NonNull CacheKey cacheKey) throws Exception {
        if (StrUtil.isBlank(cacheKey.getKey())) {
            throw BizException.wrap("脚本唯一键不能为空");
        }
        // 从Redis中根据key查找脚本
        String script = ruleCacheDataHelper.getScriptContent(cacheKey)
                .map(Object::toString)
                .orElseThrow(() -> BizException.wrap("脚本不存在"));
        // 获取脚本指纹
        String fingerprint = DigestUtils.md5DigestAsHex(script.getBytes());
        // 创建脚本对象
        ScriptEntry scriptEntry = new ScriptEntry(cacheKey.getKey(), script, fingerprint, System.currentTimeMillis());
        // 动态加载脚本为Class
        Class<?> aClass = dynamicCodeCompiler.compile(scriptEntry);
        scriptEntry.setClazz(aClass);
        return scriptEntry;
    }


    /**
     * 启动期批量预加载 ── 当前**未启用**(走 lazy load 模式,见 {@link #load(ScriptQuery)}).
     *
     * <p>历史遗留批量预热实现已删除(40+ 行死代码 + 注释引用了已不存在的 {@code ruleCacheDataHelper.getScriptContentSet()}).
     * 如未来要恢复"启动期扫 hash 全部 key 一次性编译"逻辑,实现要点:
     * <ul>
     *   <li>用 {@code cachePlusOps.scan(pattern)} 而不是 {@code keys}(线上 keys 全表扫易卡死)</li>
     *   <li>预热失败的 entry 不抛异常,日志收口</li>
     *   <li>考虑分租户分批,避免单 tenant 大量脚本压崩 JVM</li>
     * </ul>
     *
     * @return 当前固定返空列表,触发 lazy load
     */
    @Override
    public List<ScriptEntry> load() {
        return new ArrayList<>();
    }


    /**
     * 编译脚本内容
     *
     * @param scriptContent 不可为空的脚本内容
     * @return 包含编译后类的脚本实体
     * @throws BizException 当内容为空或编译失败时抛出
     */
    public ScriptEntry compileScript(@NonNull String scriptContent) throws Exception {
        if (StrUtil.isBlank(scriptContent)) {
            throw BizException.wrap("脚本内容不能为空");
        }

        // 生成内容指纹
        String fingerprint = DigestUtils.md5DigestAsHex(scriptContent.getBytes());
        // 创建脚本实体
        ScriptEntry entry = new ScriptEntry(SnowflakeIdUtil.nextId(), scriptContent, fingerprint, System.currentTimeMillis());

        // 动态编译
        Class<?> clazz = dynamicCodeCompiler.compile(entry);
        entry.setClazz(clazz);
        return entry;
    }

    /**
     * 按 4 字段身份从 DB 拉取 enable=true 的脚本内容(cache miss 回源用)。
     *
     * @param query 含 4 字段身份的查询体
     * @return 脚本内容明文;DB 不存在或被禁用时返回 null
     */
    private String loadScriptContentFromDb(RuleGroovyScriptQuery query) {
        RuleGroovyScriptPageQuery pageQuery = BeanPlusUtil.toBeanIgnoreError(query, RuleGroovyScriptPageQuery.class);
        pageQuery.setEnable(true);
        List<RuleGroovyScript> list = ruleGroovyScriptManager.getRuleGroovyScriptList(pageQuery);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0).getScriptContent();
    }
}
