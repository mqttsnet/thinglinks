package com.mqttsnet.thinglinks.video.dto.device.event;

/**
 * 设备信息更新事件。
 * <p>无论是 UI 维护还是 SIP 链路（注册 / 心跳）写库，写完都通过本事件通知监听方刷新缓存，
 * 避免缓存与 DB 长时间脱节，下次读到陈旧字段后再被链路回写覆盖。
 *
 * @author mqttsnet
 */
public class DeviceInfoUpdatedEvent extends DeviceInfoBaseEventAbstract<String> {

    /**
     * @param deviceIdentification 设备国标编号；监听方据此回查 DB 后写缓存
     */
    public DeviceInfoUpdatedEvent(String deviceIdentification) {
        super(deviceIdentification);
    }

    public DeviceInfoUpdatedEvent(String deviceIdentification, Long tenantId) {
        super(deviceIdentification, tenantId);
    }
}
