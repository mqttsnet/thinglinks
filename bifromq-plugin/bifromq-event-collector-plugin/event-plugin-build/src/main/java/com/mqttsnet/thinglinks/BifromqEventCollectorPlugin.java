/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.baidu.bifromq.plugin.BifroMQPlugin;
import com.baidu.bifromq.plugin.BifroMQPluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BifromqEventCollectorPlugin extends BifroMQPlugin<BifromqEventCollectorContext> {
    private static final Logger log = LoggerFactory.getLogger(BifromqEventCollectorPlugin.class);
    private static final String LOGBACK_CONFIG_FILE = "conf/logback.xml";
    private static final String PLUGIN_CONFIG_FILE = "conf/config.yaml";


    public BifromqEventCollectorPlugin(BifroMQPluginDescriptor descriptor) {
        super(descriptor);
        // setup logger context using plugin's logback.xml
        configureLoggerContext(descriptor.getPluginRoot());
        try {
            log.info("TODO: Initialize your plugin using config: {}", findConfigFile(descriptor.getPluginRoot()));
            log.info("---config.yaml start---");
            for (String line : Files.readAllLines(findConfigFile(descriptor.getPluginRoot()))) {
                log.info("{}", line);
            }
            log.info("---config.yaml end---");
        } catch (Exception e) {
            log.error("Failed to initialize plugin", e);
        }
    }

    protected void doStart() {
        log.info("TODO: Start your plugin");
    }

    protected void doStop() {
        log.info("TODO: Stop your plugin");
    }

    private void configureLoggerContext(Path rootPath) {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            File logbackConfig = new File(rootPath.resolve(LOGBACK_CONFIG_FILE).toAbsolutePath().toString());
            if (logbackConfig.exists()) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                configurator.doConfigure(logbackConfig);
            } else {
                log.warn("logback.xml not found for {}", getClass().getName());
            }
        } catch (Exception e) {
            log.error("Failed to configure logging for {}", getClass().getName(), e);
        }
    }

    private Path findConfigFile(Path rootPath) {
        return rootPath.resolve(PLUGIN_CONFIG_FILE);
    }
}
