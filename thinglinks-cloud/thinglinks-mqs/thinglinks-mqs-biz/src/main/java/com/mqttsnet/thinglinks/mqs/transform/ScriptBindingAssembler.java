package com.mqttsnet.thinglinks.mqs.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.mqs.transform.dto.ScriptDeviceContextDTO;
import com.mqttsnet.thinglinks.mqs.transform.dto.ScriptProductContextDTO;
import org.springframework.stereotype.Component;

/**
 * 脚本绑定上下文组装器 ── 运行时({@link InboundScriptTransformer})与在线调试
 * ({@link InboundTransformDebugService})共用同一份组装逻辑,保证脚本看到的绑定变量两边**完全一致(零漂移)**。
 *
 * <p>组装出的绑定(executeParams):
 * <ul>
 *   <li>扁平:{@code originTopic / originBody / clientId / deviceIdentification / productIdentification}</li>
 *   <li>{@code device}:{@link ScriptDeviceContextDTO}(设备基础信息,**不含 password**)</li>
 *   <li>{@code product}:{@link ScriptProductContextDTO}(产品基础信息)</li>
 * </ul>
 *
 * @author mqttsnet
 */
@Component
public class ScriptBindingAssembler {

    /**
     * 组装脚本绑定上下文。device/product 映射为独立 DTO 注入,绝不在原缓存 VO 上抠字段。
     *
     * @param deviceVO               设备缓存(可空 ── 调试时设备未命中缓存则 device 绑定为 null)
     * @param productVO              产品缓存(可空)
     * @param originTopic            源上行 topic
     * @param originBody             源原始报文(文本/JSON 直接用;二进制为 UTF-8 解码串,可能有损)
     * @param originBodyHex          源原始报文十六进制(二进制/非 JSON 报文用它拿无损原始字节)
     * @param clientId               客户端标识
     * @param deviceIdentification   设备标识
     * @param productIdentification  产品标识
     * @param extendParams           脚本扩展参数 JSON(解析为 config 绑定;空/非法则 config 为空 Map)
     * @return 可作为 executeParams 的绑定 Map
     */
    public Map<String, Object> assemble(DeviceCacheVO deviceVO, ProductCacheVO productVO, ProductModelCacheVO productModel,
                                        String originTopic, String originBody, String originBodyHex,
                                        String clientId, String deviceIdentification, String productIdentification,
                                        String extendParams) {
        Map<String, Object> params = new HashMap<>(10);
        params.put("originTopic", originTopic);
        params.put("originBody", originBody);
        params.put("originBodyHex", originBodyHex);
        params.put("clientId", clientId);
        params.put("deviceIdentification", deviceIdentification);
        params.put("productIdentification", productIdentification);
        params.put("device", deviceVO == null ? null
            : BeanPlusUtil.toBeanIgnoreError(deviceVO, ScriptDeviceContextDTO.class));
        params.put("product", productVO == null ? null
            : BeanPlusUtil.toBeanIgnoreError(productVO, ScriptProductContextDTO.class));
        // productModel:按版本解析的物模型(运行时=设备绑定版本 / 调试=选中版本),含 services[].properties[],
        // 脚本可 productModel.services 据此做字段映射 / 校验。版本未解析则为 null。
        params.put("productModel", productModel);
        // config:脚本 extend_params 解析出的用户自定义变量(如 SERVICE_CODE、阈值…),脚本里 config.xxx 取用
        params.put("config", parseConfig(extendParams));
        return params;
    }

    /**
     * 解析 extend_params(JSON 对象)为 config 绑定。空 / 非 JSON 对象一律降级为空 Map,绝不抛错阻断。
     */
    private Map<String, Object> parseConfig(String extendParams) {
        if (StrUtil.isBlank(extendParams)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> config = JSON.parseObject(extendParams);
            return config == null ? Collections.emptyMap() : config;
        } catch (Exception ignore) {
            return Collections.emptyMap();
        }
    }
}
