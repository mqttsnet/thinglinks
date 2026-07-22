package com.mqttsnet.thinglinks.device.vo.result;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSL 测试分步结果 ── 一个步骤一条,前端按 steps 顺序渲染卡片。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "DeviceSslTestStepVO", description = "SSL 测试分步结果")
public class DeviceSslTestStepVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 步骤枚举值,见 {@code DeviceSslTestStepEnum} */
    @Schema(description = "步骤枚举")
    private String step;

    /** 步骤名(中文/前端 i18n 展示) */
    @Schema(description = "步骤名")
    private String name;

    /** 步骤状态 PASS / FAIL / SKIP */
    @Schema(description = "步骤状态")
    private String status;

    /** 步骤额外元数据(如证书 subject/issuer/serial/notBefore/notAfter) */
    @Schema(description = "步骤元数据")
    private Map<String, Object> detail;

    /** 失败时的具体原因(脱敏后,可展示给前端) */
    @Schema(description = "失败原因")
    private String reason;

    /** 步骤耗时毫秒 */
    @Schema(description = "耗时 ms")
    private Long costMs;
}
