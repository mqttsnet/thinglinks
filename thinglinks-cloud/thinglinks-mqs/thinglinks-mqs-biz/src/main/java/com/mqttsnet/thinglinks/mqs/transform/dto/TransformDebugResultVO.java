package com.mqttsnet.thinglinks.mqs.transform.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.Data;

/**
 * 前置转换脚本「在线调试」结果。
 *
 * @author mqttsnet
 */
@Data
public class TransformDebugResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 脚本执行结果(复用 rule 结构:executionStatus / context / exception / errorMessage)
     */
    private GroovyScriptEngineExecutorResultVO result;

    /**
     * 实际注入脚本的绑定快照 ── 供前端「变量检视器」展示 device.* / product.* 在当前设备/产品上的真实取值。
     * 与运行时注入完全一致(同一个 {@code ScriptBindingAssembler});device 不含 password。
     */
    private Map<String, Object> binding;

    /**
     * 调试设备是否命中缓存(未命中则 device 绑定为 null,提示用户)
     */
    private boolean deviceResolved;
    /**
     * 绑定产品是否命中缓存(未命中则 product 绑定为 null)
     */
    private boolean productResolved;
}
