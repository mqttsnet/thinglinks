package com.mqttsnet.thinglinks;

import java.io.InputStream;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import cn.hutool.json.JSONUtil;
import com.baidu.bifromq.plugin.BifroMQPluginContext;
import com.baidu.bifromq.plugin.BifroMQPluginDescriptor;
import com.mqttsnet.thinglinks.entity.config.PluginConfig;
import com.mqttsnet.thinglinks.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
public final class BifromqAuthProviderContext extends BifroMQPluginContext {

    private static final String LOGBACK_CONFIG_FILE = "conf/logback.xml";
    private final BifroMQPluginDescriptor bifroMQPluginDescriptor;
    private final PluginConfig pluginConfig;
    private LoggerContext pluginLoggerContext;

    public BifromqAuthProviderContext(BifroMQPluginDescriptor descriptor) {
        super(descriptor);
        this.bifroMQPluginDescriptor = descriptor;

        // 初始化独立日志配置
        initPluginLogger();

        // 使用ConfigUtil加载配置
        pluginConfig = ConfigUtil.getPluginConfig();
        log.info("Initialized BifromqAuthProviderContext with descriptor: {}", bifroMQPluginDescriptor.getDescriptor().toString());
    }

    @Override
    protected void init() {
        log.info("Starting initialization of BifromqAuthProviderContext for plugin: {}", bifroMQPluginDescriptor.getDescriptor().toString());
        log.debug("Plugin root path: {}", bifroMQPluginDescriptor.getPluginRoot());
        log.debug("Development mode: {}", bifroMQPluginDescriptor.isDevelopment());

        try {
            log.debug("Loading configurations for plugin...");
            // TODO 插入配置加载代码
            log.info("Configurations loaded successfully for {}", bifroMQPluginDescriptor.getDescriptor().toString());
        } catch (Exception e) {
            log.error("Initialization failed for plugin {}: {}", bifroMQPluginDescriptor.getDescriptor().toString(), e.getMessage(), e);
        }
    }

    @Override
    protected void close() {
        log.info("Closing BifromqAuthProviderContext for plugin: {}", bifroMQPluginDescriptor.getDescriptor().toString());

        try {
            // 1. 关闭独立日志上下文
            if (pluginLoggerContext != null) {
                pluginLoggerContext.stop();
            }

            log.debug("Releasing resources for plugin...");
            // TODO 插入资源释放代码
            log.info("Resources released successfully for {}", bifroMQPluginDescriptor.getDescriptor().toString());
        } catch (Exception e) {
            log.error("Failed to close BifromqAuthProviderContext for plugin {}: {}", bifroMQPluginDescriptor.getDescriptor().toString(), e.getMessage(), e);
        }
    }

    /**
     * 初始化插件独立日志
     */
    private void initPluginLogger() {
        try {
            // 创建独立上下文
            pluginLoggerContext = new LoggerContext();
            pluginLoggerContext.setName("thinglinks_" + bifroMQPluginDescriptor.getDescriptor().getPluginId());

            // 从插件JAR中加载logback配置
            try (InputStream configStream = getClass().getResourceAsStream(LOGBACK_CONFIG_FILE)) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(pluginLoggerContext);
                pluginLoggerContext.reset();

                // 注入插件ID到系统属性，供logback.xml使用
                System.setProperty("pluginId", bifroMQPluginDescriptor.getDescriptor().getPluginId());

                configurator.doConfigure(configStream);
                LoggerFactory.getLogger(getClass()).info("Plugin logger configured successfully");
            }
        } catch (Exception e) {
            // 紧急回退到控制台日志
            LoggerFactory.getLogger(getClass()).error("Failed to init plugin logger, using fallback", e);
        }
    }

    public PluginConfig getPluginConfig() {
        // 添加调试日志，打印插件描述符信息
        log.info("Attempting to load config file from /conf/config.yaml for plugin: {}", bifroMQPluginDescriptor.getDescriptor().toString());

        if (this.pluginConfig != null) {
            // 打印配置信息
            log.info("Loaded PluginConfig: {}", JSONUtil.toJsonStr(this.pluginConfig));
        } else {
            log.warn("PluginConfig is null. Please check if the configuration file is loaded correctly.");
        }

        return this.pluginConfig;
    }

}
