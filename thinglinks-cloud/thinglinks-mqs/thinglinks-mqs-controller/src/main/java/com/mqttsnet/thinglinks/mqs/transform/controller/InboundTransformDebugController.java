package com.mqttsnet.thinglinks.mqs.transform.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.mqs.transform.InboundTransformDebugService;
import com.mqttsnet.thinglinks.mqs.transform.dto.TransformDebugParam;
import com.mqttsnet.thinglinks.mqs.transform.dto.TransformDebugResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备上行前置转换 ── 在线调试接口。
 *
 * <p>选一个真实设备 + 一条源原始报文,后端按**运行时同一套逻辑**(共用 {@code ScriptBindingAssembler})
 * 组装 {@code device}/{@code product} 绑定后试跑脚本,返回执行结果 + 实际注入的绑定快照
 * (供前端「变量检视器」展示 device.* / product.* 在该设备/产品上的真实取值)。
 *
 * @author mqttsnet
 */
@RestController
@RequestMapping("/transform")
@RequiredArgsConstructor
@Tag(name = "前置转换-在线调试", description = "选真实设备 + 原始报文,试跑设备上行前置转换脚本")
@Slf4j
public class InboundTransformDebugController {

    private final InboundTransformDebugService inboundTransformDebugService;

    @Operation(summary = "在线调试前置转换脚本",
        description = "传 deviceIdentification + originTopic + originBody + scriptContent,返回执行结果与实际注入的绑定变量")
    @PostMapping("/debug")
    public R<TransformDebugResultVO> debug(@RequestBody TransformDebugParam param) {
        return R.success(inboundTransformDebugService.debug(param));
    }
}
