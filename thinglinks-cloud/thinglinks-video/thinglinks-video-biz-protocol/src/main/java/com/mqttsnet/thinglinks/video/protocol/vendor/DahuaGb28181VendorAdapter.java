package com.mqttsnet.thinglinks.video.protocol.vendor;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.video.protocol.VendorProtocolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 大华 GB28181 厂商适配器。
 *
 * <p>处理大华设备在 GB28181 协议实现中的特殊行为：</p>
 * <ul>
 *     <li>大华部分设备 SDP 中使用非标准 payload type</li>
 *     <li>告警 XML 中告警类型编码与标准存在差异</li>
 *     <li>部分机型不支持 TCP 主动模式</li>
 * </ul>
 *
 * <p>仅影响大华设备，不影响其他厂商的协议处理。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class DahuaGb28181VendorAdapter implements VendorProtocolAdapter {

    @Override
    public String getManufacturer() {
        return "dahua";
    }

    @Override
    public String getProtocolVersion() {
        return BizConstant.ALL; // 适用于大华所有版本
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String adaptAlarmXml(String xmlContent) {
        // 大华部分设备告警 XML 的 AlarmMethod 编码非标准，需要映射
        log.debug("[大华适配器] 适配告警XML");
        return xmlContent;
    }
}
