package com.mqttsnet.thinglinks.device.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSL 测试分步状态:PASS=通过 / FAIL=失败(终止)/ SKIP=跳过(前置步骤已失败)。
 *
 * @author mqttsnet
 */
@Getter
@AllArgsConstructor
@Schema(title = "DeviceSslTestStepStatusEnum", description = "SSL 测试分步状态")
public enum DeviceSslTestStepStatusEnum {

    PASS("PASS", "通过"),
    FAIL("FAIL", "失败"),
    SKIP("SKIP", "跳过");

    private final String value;
    private final String desc;
}
