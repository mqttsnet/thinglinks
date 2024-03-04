package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.event;

import org.springframework.context.ApplicationEvent;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: MessageEvent
 * @packagename: com.mqttsnet.thinglinks.mqtt.handle.event
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 14:42
 **/
public class MqttMessageEvent extends ApplicationEvent {
    private final String topic;
    private final String qos;
    private final String message;
    private final String time;

    public MqttMessageEvent(Object source, String topic, String qos, String message, String time) {
        super(source);
        this.topic = topic;
        this.qos = qos;
        this.message = message;
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public String getQos() {
        return qos;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }


    public class AddSubDeviceEvent extends MqttMessageEvent {
        public AddSubDeviceEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class AddSubDeviceResponseEvent extends MqttMessageEvent {
        public AddSubDeviceResponseEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceStatusEvent extends MqttMessageEvent {
        public DeviceStatusEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceStatusResponseEvent extends MqttMessageEvent {
        public DeviceStatusResponseEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceControlEvent extends MqttMessageEvent {
        public DeviceControlEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceControlResponseEvent extends MqttMessageEvent {
        public DeviceControlResponseEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceDataReportEvent extends MqttMessageEvent {
        public DeviceDataReportEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }

    public class DeviceDataReportResponseEvent extends MqttMessageEvent {
        public DeviceDataReportResponseEvent(Object source, String topic, String qos, String body, String time) {
            super(source, topic, qos, body, time);
        }
    }
}
