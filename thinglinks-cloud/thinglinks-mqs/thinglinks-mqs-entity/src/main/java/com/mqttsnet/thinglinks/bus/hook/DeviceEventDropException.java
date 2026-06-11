package com.mqttsnet.thinglinks.bus.hook;

import lombok.Getter;

/**
 * 主动丢弃事件,由 {@link DeviceEventInterceptor} 抛出。
 * 非异常错误,INFO 日志,不进 DLT,不触发失败钩子。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class DeviceEventDropException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String reason;

    public DeviceEventDropException(String reason) {
        super(reason);
        this.reason = reason;
    }
}
