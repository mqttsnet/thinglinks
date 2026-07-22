package com.mqttsnet.thinglinks.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mqttsnet.thinglinks.entity.config.PluginConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigUtil {
    public static PluginConfig getPluginConfig() {
        File configFile;
        String resource = "/config.yaml";
        URL res = ConfigUtil.class.getResource(resource);
        if (res == null) {
            log.error("Config file not found at path: {}", resource);
            throw new RuntimeException("Config file not found");
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            if (res.getProtocol().equals("jar")) {
                InputStream input = ConfigUtil.class.getResourceAsStream(resource);
                configFile = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(configFile);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                configFile.deleteOnExit();
            } else {
                configFile = new File(res.getFile());
            }

            if (configFile != null && !configFile.exists()) {
                throw new RuntimeException("Error: File " + configFile + " not found");
            }
            PluginConfig source = getOverwriteConfig();
            PluginConfig dest = mapper.readValue(configFile, PluginConfig.class);
            if (source != null) {
                // 过滤 source 中的空值属性
                Map<String, Object> nonNullProperties = BeanUtil.beanToMap(source).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // 复制非空属性到 dest
                nonNullProperties.forEach((key, value) -> BeanUtil.setProperty(dest, key, value));
            }
            return dest;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read starter config file: ", e);
        }
    }

    private static PluginConfig getOverwriteConfig() {
        try {
            File file = new File("./conf/standalone.yml");
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            PluginConfig pluginConfig = mapper.readValue(file, PluginConfig.class);
            return pluginConfig;
        } catch (Exception exception) {
            return null;
        }
    }
}
