package com.mqttsnet.thinglinks.config.groovy.helper;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.groovy.entity.ScriptEntry;
import com.mqttsnet.basic.groovy.entity.ScriptQuery;
import com.mqttsnet.basic.groovy.helper.RegisterScriptHelper;
import com.mqttsnet.basic.groovy.loader.ScriptLoader;
import com.mqttsnet.basic.groovy.registry.ScriptRegistry;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.config.groovy.properties.GroovyRedisLoaderProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 手动注册脚本助手
 *
 * @author mqttsnet
 */
@Slf4j
public class ManualRegisterScriptHelper implements RegisterScriptHelper {

    private ScriptRegistry scriptRegistry;

    private ScriptLoader redisScriptLoader;

    private RuleCacheDataHelper ruleCacheDataHelper;

    private GroovyRedisLoaderProperties groovyRedisLoaderProperties;

    public ManualRegisterScriptHelper(ScriptRegistry scriptRegistry,
                                      ScriptLoader redisScriptLoader,
                                      RuleCacheDataHelper ruleCacheDataHelper,
                                      GroovyRedisLoaderProperties groovyRedisLoaderProperties) {
        this.scriptRegistry = scriptRegistry;
        this.redisScriptLoader = redisScriptLoader;
        this.ruleCacheDataHelper = ruleCacheDataHelper;
        this.groovyRedisLoaderProperties = groovyRedisLoaderProperties;
    }

    @Override
    public boolean flushGroovyScriptCache(CacheKey cacheKey, String content, boolean allowCover) throws Exception {
        log.info("start manual register script, uniqueKey is : [{}], script content is : {}", cacheKey, content);
        if (StringUtils.isBlank(cacheKey.getKey()) || StringUtils.isBlank(content)) {
            throw BizException.wrap("uniqueKey and content can not be null.");
        }
        // 查找脚本是否存在
        // 脚本存在且不允许覆盖时终止操作，其他情况允许创建/覆盖，则写入数据源然后注册到registry
        if (ruleCacheDataHelper.getScriptContent(cacheKey).isPresent() && !allowCover) {
            log.warn("script already exists, uniqueKey is : [{}], script content is : {}", cacheKey, content);
            return false;
        }
        // 脚本放入Redis缓存
        ruleCacheDataHelper.setScriptContent(cacheKey, content);
        log.info("key:[{}] script store to redis successfully.", cacheKey.getKey());
        return true;
    }

    @Override
    public boolean registerScript(@NonNull CacheKey cacheKey, @NonNull String content, boolean allowCover) throws Exception {
        log.info("start manual register script, uniqueKey is : [{}], script content is : {}", cacheKey, content);
        if (StringUtils.isBlank(cacheKey.getKey()) || StringUtils.isBlank(content)) {
            throw BizException.wrap("uniqueKey and content can not be null.");
        }
        // 查找脚本是否存在
        // 脚本存在且不允许覆盖时终止操作，其他情况允许创建/覆盖，则写入数据源然后注册到registry
        if (ruleCacheDataHelper.getScriptContent(cacheKey).isPresent() && !allowCover) {
            log.warn("script already exists, uniqueKey is : [{}], script content is : {}", cacheKey, content);
            return false;
        }
        // 脚本放入Redis缓存
        ruleCacheDataHelper.setScriptContent(cacheKey, content);
        log.info("key:[{}]  script store to redis successfully.", cacheKey.getKey());
        // 从Redis加载
        ScriptEntry scriptEntry = redisScriptLoader.load(cacheKey);
        // 注册到脚本注册中心
        Boolean success = scriptRegistry.register(scriptEntry);
        log.info("[{}] script register to registry result is : [{}].", cacheKey.getKey(), success);
        return success;
    }

    /**
     * 批量注册脚本
     *
     * @param scriptMap  脚本信息map key:脚本名称 value:脚本内容
     * @param allowCover 是否允许覆盖
     * @return true: 批量注册成功 false: 批量注册失败
     * @throws Exception
     */
    @Override
    public boolean batchRegisterScript(@NonNull Map<CacheKey, String> scriptMap, boolean allowCover) throws Exception {
        log.info("start batch register script, scriptMap is : [{}]", scriptMap);
        scriptMap.forEach((cacheKey, content) -> {
            try {
                registerScript(cacheKey, content, allowCover);
            } catch (Exception e) {
                log.error("register script failed, uniqueKey is : [{}], script content is : {}", cacheKey.getKey(), content, e);
                throw BizException.wrap("register failed，please retry.", e);
            }
        });
        log.info("batch register script successfully.");
        return true;
    }


    @Override
    public Boolean clear(@NonNull CacheKey cacheKey) {
        ruleCacheDataHelper.delScriptContent(cacheKey);
        ScriptQuery scriptQuery = new ScriptQuery(cacheKey.getKey());
        // 注册到脚本注册中心
        return scriptRegistry.clear(scriptQuery);
    }
}
