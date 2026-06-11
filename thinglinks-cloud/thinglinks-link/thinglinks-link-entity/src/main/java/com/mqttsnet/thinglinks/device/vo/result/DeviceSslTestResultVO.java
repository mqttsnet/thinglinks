package com.mqttsnet.thinglinks.device.vo.result;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSL 证书认证测试整体响应。前端按 steps 顺序渲染分步卡片,顶部展示 summary。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "DeviceSslTestResultVO", description = "SSL 测试整体响应")
public class DeviceSslTestResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 整体是否通过(全部步骤 PASS 才 true) */
    @Schema(description = "整体是否通过")
    private Boolean success;

    /** 分步结果,按 {@code DeviceSslTestStepEnum} 顺序 */
    @Schema(description = "分步结果列表")
    private List<DeviceSslTestStepVO> steps;

    /** 顶部摘要,如 "测试通过" 或 "签名验证失败 ── client 证书不是该 CA 签发的" */
    @Schema(description = "摘要")
    private String summary;

    /** 测试总耗时毫秒 */
    @Schema(description = "总耗时 ms")
    private Long totalCostMs;
}
