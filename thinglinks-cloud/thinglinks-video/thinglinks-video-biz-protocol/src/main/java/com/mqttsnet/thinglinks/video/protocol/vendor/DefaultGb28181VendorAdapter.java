package com.mqttsnet.thinglinks.video.protocol.vendor;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.video.protocol.VendorProtocolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * GB28181 默认厂商适配器。
 *
 * <p>实现标准 GB/T 28181 协议行为，作为所有未匹配到特定厂商适配器的默认回退。
 * 同时兼容 2016 和 2022 两个版本的基本行为。</p>
 *
 * <p>当需要支持特定厂商（如海康、大华、宇视）的非标协议扩展时，
 * 新建对应的适配器类即可，无需修改此默认实现。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see VendorProtocolAdapter
 * @since 2026-03-30
 */
@Slf4j
@Component
public class DefaultGb28181VendorAdapter implements VendorProtocolAdapter {

    @Override
    public String getManufacturer() {
        return BizConstant.ALL;
    }

    @Override
    public String getProtocolVersion() {
        return BizConstant.ALL;
    }

    @Override
    public int getPriority() {
        return 999; // 最低优先级，作为兜底
    }

    @Override
    public String buildInviteSdp(SdpBuildContext context) {
        log.debug("[默认GB28181适配器] 使用标准SDP构建: device={}, channel={}",
            context.deviceIdentification(), context.channelIdentification());
        return context.getDefaultSdp();
    }

    @Override
    public SdpParseResult parseSdpResponse(String sdpContent) {
        log.debug("[默认GB28181适配器] 使用标准SDP解析");
        return SdpParseResult.defaultParse(sdpContent);
    }
}
