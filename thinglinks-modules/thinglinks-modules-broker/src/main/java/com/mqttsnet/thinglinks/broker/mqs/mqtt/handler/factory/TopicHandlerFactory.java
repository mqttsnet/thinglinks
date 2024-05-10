package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory;


import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: thinglinks
 * @description: MQTT系统Topic 处理工厂类
 * @packagename: com.mqttsnet.thinglinks.handler.factory
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:00
 **/
@Component
public class TopicHandlerFactory {

    private final DefaultHandler defaultHandler;
    private final SecretKeyHandler secretKeyHandler;
    private final AddSubDeviceHandler addSubDeviceHandler;
    private final DeleteSubDeviceHandler deleteSubDeviceHandler;
    private final UpdateSubDeviceHandler updateSubDeviceHandler;
    private final QueryDeviceHandler queryDeviceHandler;
    private final DeviceDatasHandler deviceDatasHandler;
    private final CommandResponseHandler commandResponseHandler;

    private final OtaCommandResponseHandler otaCommandResponseHandler;

    // Define patterns as constants
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("");
    private static final Pattern SECRET_KEY_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/topo/secretKey");
    private static final Pattern ADD_SUB_DEVICE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/topo/add");
    private static final Pattern DELETE_SUB_DEVICE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/topo/delete");
    private static final Pattern UPDATE_SUB_DEVICE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/topo/update");
    private static final Pattern QUERY_DEVICE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/topo/query");
    private static final Pattern DEVICE_DATAS_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/datas");
    private static final Pattern COMMAND_RESPONSE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/commandResponse");
    private static final Pattern OTA_COMMAND_RESPONSE_PATTERN = Pattern.compile("/([^/]+)/devices/([^/]+)/otaCommandResponse");

    public TopicHandlerFactory(DefaultHandler defaultHandler, SecretKeyHandler secretKeyHandler,
                               AddSubDeviceHandler addSubDeviceHandler, DeleteSubDeviceHandler deleteSubDeviceHandler,
                               UpdateSubDeviceHandler updateSubDeviceHandler, QueryDeviceHandler queryDeviceHandler, DeviceDatasHandler deviceDatasHandler,
                               CommandResponseHandler commandResponseHandler, OtaCommandResponseHandler otaCommandResponseHandler) {
        this.defaultHandler = defaultHandler;
        this.secretKeyHandler = secretKeyHandler;
        this.addSubDeviceHandler = addSubDeviceHandler;
        this.deleteSubDeviceHandler = deleteSubDeviceHandler;
        this.updateSubDeviceHandler = updateSubDeviceHandler;
        this.queryDeviceHandler = queryDeviceHandler;
        this.deviceDatasHandler = deviceDatasHandler;
        this.commandResponseHandler = commandResponseHandler;
        this.otaCommandResponseHandler = otaCommandResponseHandler;
    }

    private static final class TopicHandlerEntry {
        private final Pattern pattern;
        private final TopicHandler handler;

        public TopicHandlerEntry(Pattern pattern, TopicHandler handler) {
            this.pattern = pattern;
            this.handler = handler;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public TopicHandler getHandler() {
            return handler;
        }
    }

    private List<TopicHandlerEntry> topicHandlerEntries;

    // Use the @PostConstruct annotation to initialize the handler entries list after the dependencies are injected.
    @PostConstruct
    public void initTopicHandlerEntries() {
        topicHandlerEntries = new ArrayList<>();
        topicHandlerEntries.add(new TopicHandlerEntry(DEFAULT_PATTERN, defaultHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(SECRET_KEY_PATTERN, secretKeyHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(ADD_SUB_DEVICE_PATTERN, addSubDeviceHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(DELETE_SUB_DEVICE_PATTERN, deleteSubDeviceHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(UPDATE_SUB_DEVICE_PATTERN, updateSubDeviceHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(QUERY_DEVICE_PATTERN, queryDeviceHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(DEVICE_DATAS_PATTERN, deviceDatasHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(COMMAND_RESPONSE_PATTERN, commandResponseHandler));
        topicHandlerEntries.add(new TopicHandlerEntry(OTA_COMMAND_RESPONSE_PATTERN, otaCommandResponseHandler));
    }

    // This method searches for a matching handler based on the topic.
    public TopicHandler findMatchingHandler(String topic) {
        for (TopicHandlerEntry handlerEntry : topicHandlerEntries) {
            Matcher matcher = handlerEntry.getPattern().matcher(topic);
            if (matcher.matches()) {
                return handlerEntry.getHandler();
            }
        }
        return defaultHandler;
    }
}