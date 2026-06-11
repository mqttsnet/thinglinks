package com.mqttsnet.thinglinks.productversion.canary;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.productversion.vo.canary.CanaryConfigDTO;
import org.springframework.stereotype.Component;

/**
 * 灰度发布算法工具(无状态)── 一致性哈希挑设备。JSON 反序列化由 {@link CanaryConfigDTO#parse} 负责,
 * 调用方先 parse 拿 typed DTO 再调本类做集合筛选。
 *
 * @author mqttsnet
 */
@Component
public class CanaryRuleMatcher {

    /**
     * 按百分比一致性哈希挑选设备子集:Murmur3({@link HashUtil#murmur32(byte[])})对 deviceIdentification
     * 做 32 位哈希与 100 取模,桶号小于 percent 即命中。选 Murmur3 而非 {@link String#hashCode()} 是因雪崩
     * 效应好,对模式化 ID(DEV_001..DEV_999)分布也均匀;deterministic + 跨 JVM 一致,30%→50% 渐进放量时
     * 已命中设备不会被踢出。all 允许 null / 含空串(自动过滤);percent 取 1~99(0 / 100 视为非法返空集合)。
     */
    public List<String> pickByPercent(List<String> all, int percent) {
        if (all == null || all.isEmpty() || percent <= 0 || percent >= 100) {
            return Collections.emptyList();
        }
        List<String> hit = new ArrayList<>(all.size() * percent / 100 + 1);
        for (String identification : all) {
            if (StrUtil.isBlank(identification)) {
                continue;
            }
            int bucket = (HashUtil.murmur32(identification.getBytes(StandardCharsets.UTF_8)) & 0x7FFFFFFF) % 100;
            if (bucket < percent) {
                hit.add(identification);
            }
        }
        return hit;
    }
}
