package com.mqttsnet.thinglinks.config.groovy;

import com.mqttsnet.basic.groovy.annotation.ConditionalOnExistingProperty;
import com.mqttsnet.basic.groovy.compiler.DynamicCodeCompiler;
import com.mqttsnet.basic.groovy.loader.ScriptLoader;
import com.mqttsnet.basic.groovy.registry.ScriptRegistry;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.config.groovy.helper.ManualRegisterScriptHelper;
import com.mqttsnet.thinglinks.config.groovy.loader.RedisScriptLoader;
import com.mqttsnet.thinglinks.config.groovy.properties.GroovyRedisLoaderProperties;
import com.mqttsnet.thinglinks.manager.script.RuleGroovyScriptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 * @author mqttsnet
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(value = {GroovyRedisLoaderProperties.class})
@ConditionalOnExistingProperty(property = GroovyRedisLoaderProperties.PREFIX + ".enable", value = "true")
public class GroovyRedisLoaderAutoConfiguration {


    /**
     * <p>
     * 注册基于Redis的ScriptLoader，配置里必须要显示开启该加载器时才注入 {@code enhance.groovy.engine.redis-loader.enable}
     * 需要依赖于RedisTemplate，所以项目里必须要配置redis
     * </p>
     */
    @Bean
    public ScriptLoader redisScriptLoader(RuleCacheDataHelper ruleCacheDataHelper,
                                          DynamicCodeCompiler dynamicCodeCompiler,
                                          RuleGroovyScriptManager ruleGroovyScriptManager) {
        log.info("loading ScriptLoader type is [{}]", RedisScriptLoader.class);
        return new RedisScriptLoader(ruleCacheDataHelper, dynamicCodeCompiler, ruleGroovyScriptManager);
    }

    /**
     * 注入手动注册脚本助手
     */
    @Bean
    public ManualRegisterScriptHelper registerScriptHelper(ScriptRegistry scriptRegistry,
                                                           ScriptLoader scriptLoader,
                                                           RuleCacheDataHelper ruleCacheDataHelper,
                                                           GroovyRedisLoaderProperties groovyRedisLoaderProperties) {
        return new ManualRegisterScriptHelper(scriptRegistry, scriptLoader, ruleCacheDataHelper, groovyRedisLoaderProperties);
    }

}
