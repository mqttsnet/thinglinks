package com.mqttsnet.thinglinks.common.cache.rule.groovy;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备上行前置转换桶的「值」对象 ── Redis HASH 里每个 {@code topic 模式 → 该值的 JSON}。
 *
 * <p>由原来的「topic → 纯脚本内容」升级为「topic → {脚本内容 + 扩展参数}」,使脚本除了 device/product
 * 之外还能拿到用户在 {@code extend_params} 里配置的自定义变量(运行时注入为 {@code config} 绑定)。
 *
 * <p>rule 侧({@code RuleCacheDataHelper})写入、mqs 侧({@code InboundScriptTransformer})读取,
 * 故下沉到 common 共享。读取端对非 JSON 旧值做兼容(整串当 {@link #content})。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformScriptEntry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 脚本内容(Groovy) */
    private String content;

    /** 扩展参数(JSON 字符串)── 运行时解析后注入脚本的 {@code config} 绑定 */
    private String extendParams;
}
