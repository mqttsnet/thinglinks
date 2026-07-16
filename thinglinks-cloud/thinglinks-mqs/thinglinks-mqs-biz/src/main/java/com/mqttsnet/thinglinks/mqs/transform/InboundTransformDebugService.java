package com.mqttsnet.thinglinks.mqs.transform;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.enumeration.script.ExecutionStatusEnum;
import com.mqttsnet.thinglinks.mqs.transform.dto.TransformDebugParam;
import com.mqttsnet.thinglinks.mqs.transform.dto.TransformDebugResultVO;
import com.mqttsnet.thinglinks.rule.facade.RuleOpenInnerFacade;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 前置转换脚本「在线调试」服务。
 *
 * <p>选真实设备 + 源原始报文 → 取该设备及其绑定产品的缓存 → 用 {@link ScriptBindingAssembler}
 * 按**运行时同一套逻辑**组装绑定 → 调 rule 执行脚本 → 回执行结果 + 绑定快照。
 * 与 {@link InboundScriptTransformer} 共用组装器,保证调试看到的 device/product 变量与真实运行时一致(零漂移)。
 *
 * @author mqttsnet
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InboundTransformDebugService {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final RuleOpenInnerFacade ruleOpenInnerFacade;
    private final ScriptBindingAssembler bindingAssembler;

    public TransformDebugResultVO debug(TransformDebugParam param) {
        TransformDebugResultVO vo = new TransformDebugResultVO();

        // 1) 取真实设备 + 其绑定产品缓存
        DeviceCacheVO deviceVO = StrUtil.isBlank(param.getDeviceIdentification()) ? null
            : linkCacheDataHelper.getDeviceCacheVO(param.getDeviceIdentification()).orElse(null);
        ProductCacheVO productVO = (deviceVO == null || StrUtil.isBlank(deviceVO.getProductIdentification())) ? null
            : linkCacheDataHelper.getProductCacheVO(deviceVO.getProductIdentification()).orElse(null);
        vo.setDeviceResolved(deviceVO != null);
        vo.setProductResolved(productVO != null);

        // 2) 非 JSON 支持:0x 前缀视为十六进制原始报文,解出无损字节供脚本拿 originBodyHex;否则按文本处理
        String rawInput = param.getOriginBody();
        String originBody;
        String originBodyHex;
        if (StrUtil.startWithIgnoreCase(rawInput, "0x")) {
            byte[] bytes = HexUtil.decodeHex(rawInput.substring(2).replaceAll("\\s", ""));
            originBody = new String(bytes, StandardCharsets.UTF_8);
            originBodyHex = HexUtil.encodeHexStr(bytes);
        } else {
            originBody = rawInput;
            originBodyHex = StrUtil.isEmpty(rawInput) ? null
                : HexUtil.encodeHexStr(rawInput.getBytes(StandardCharsets.UTF_8));
        }

        // 3) 组装绑定(与运行时同一套组装器)
        //    设备已解析则取缓存上的真实标识;未解析时 clientId/productIdentification 留空、deviceIdentification 回退入参
        String clientId = null;
        String deviceIdentification = param.getDeviceIdentification();
        String productIdentification = null;
        if (deviceVO != null) {
            clientId = deviceVO.getClientId();
            deviceIdentification = deviceVO.getDeviceIdentification();
            productIdentification = deviceVO.getProductIdentification();
        }
        // 调试:优先用前端选中的版本(可调当前 / 下个版本逻辑),回退设备绑定版本 / 产品生效版本
        String modelVersionNo = StrUtil.isNotBlank(param.getObjectVersion()) ? param.getObjectVersion()
            : (deviceVO != null && StrUtil.isNotBlank(deviceVO.getBoundProductVersionNo())
                ? deviceVO.getBoundProductVersionNo()
                : (productVO != null ? productVO.getActiveVersionNo() : null));
        ProductModelCacheVO productModel = (StrUtil.isBlank(modelVersionNo) || StrUtil.isBlank(productIdentification))
            ? null
            : linkCacheDataHelper.resolveProductModelByVersionNo(productIdentification, modelVersionNo).orElse(null);

        Map<String, Object> binding = bindingAssembler.assemble(deviceVO, productVO, productModel,
            param.getOriginTopic(), originBody, originBodyHex,
            clientId, deviceIdentification, productIdentification,
            param.getExtendParams());
        vo.setBinding(binding);

        // 4) 调 rule 按脚本内容执行
        RuleGroovyScriptDirectCompileParam compileParam = new RuleGroovyScriptDirectCompileParam();
        compileParam.setScriptContent(param.getScriptContent());
        compileParam.setExecuteParams(JSON.toJSONString(binding));
        // 调试运行也计入脚本执行统计(scriptUniqueKey 由前端详情页拼好传入,空则不计)
        compileParam.setScriptUniqueKey(param.getScriptUniqueKey());

        GroovyScriptEngineExecutorResultVO result;
        try {
            R<GroovyScriptEngineExecutorResultVO> r = ruleOpenInnerFacade.executeScriptContent(compileParam);
            if (r != null && Boolean.TRUE.equals(r.getIsSuccess()) && r.getData() != null) {
                result = r.getData();
            } else {
                result = new GroovyScriptEngineExecutorResultVO();
                result.setExecutionStatus(ExecutionStatusEnum.FAILED);
                result.setErrorMessage(r == null ? "脚本执行无响应" : r.getMsg());
            }
        } catch (Exception e) {
            log.warn("[InboundTransformDebug] exec failed device={} err={}", param.getDeviceIdentification(), e.getMessage());
            result = new GroovyScriptEngineExecutorResultVO();
            result.setExecutionStatus(ExecutionStatusEnum.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        vo.setResult(result);
        return vo;
    }
}
