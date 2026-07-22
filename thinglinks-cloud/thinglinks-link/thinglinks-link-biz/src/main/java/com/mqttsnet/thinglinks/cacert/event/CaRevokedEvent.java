package com.mqttsnet.thinglinks.cacert.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * CA 证书吊销事件 ── 由 {@code CaCertLicenseServiceImpl.revokeCertificate} 发布,
 * 监听方负责后续动作(如清除关联设备 cache、推送告警等)。
 *
 * @author mqttsnet
 */
@Getter
public class CaRevokedEvent extends ApplicationEvent {

    private final Long caId;
    private final String caSerialNumber;
    private final String reason;

    public CaRevokedEvent(Object source, Long caId, String caSerialNumber, String reason) {
        super(source);
        this.caId = caId;
        this.caSerialNumber = caSerialNumber;
        this.reason = reason;
    }
}
