package com.mqttsnet.thinglinks.productversion.vo.canary;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本灰度发布配置 ── 对应 product_version.canary_config_json 列(JSON 文本)的 typed 视图,
 * JSON 字段名集中管控在本类,业务代码零字符串硬编码,序列化/反序列化走内置 parse / toJson。
 * JSON 形态:{"mode":"whitelist","deviceIdentifications":[...]}(灰度按明确设备集合,前端分组 / 指定设备两种来源拍平成此形态)。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CanaryConfigDTO", description = "产品版本灰度发布配置")
public class CanaryConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 灰度来源:精确白名单(走 deviceIdentifications)。 */
    public static final String MODE_WHITELIST = "whitelist";

    @Schema(description = "灰度模式:whitelist(白名单)")
    private String mode;

    @Schema(description = "白名单设备识别码列表")
    private List<String> deviceIdentifications;

    @Schema(description = "灰度来源:group(分组) / manual(指定设备)── 纯展示用,灰度匹配按 deviceIdentifications 名单")
    private String source;

    @Schema(description = "所选分组明细(source=group;冻结发布那一刻所选分组的 id/名称/设备数)")
    private List<CanaryGroup> groups;

    /**
     * 反序列化:从 canary_config_json 解析为 typed DTO。null/blank 返 {@link Optional#empty()};
     * JSON 非法时故意不 try-catch,让 fastjson2 JSONException 冒泡,避免静默掩盖脏数据。
     * 选 fastjson2 不选 hutool JSONUtil:hutool 5.x 子包 CopyOptions.setFormatIfDate 有 ABI 漂移,运行时易出 NoSuchMethodError。
     *
     * @param canaryConfigJson canary_config_json 列的 JSON 文本
     * @return typed DTO;字符串为 null/blank 返 {@link Optional#empty()}
     */
    public static Optional<CanaryConfigDTO> parse(String canaryConfigJson) {
        if (StrUtil.isBlank(canaryConfigJson)) {
            return Optional.empty();
        }
        return Optional.of(JSON.parseObject(canaryConfigJson, CanaryConfigDTO.class));
    }

    /**
     * 序列化:把当前 DTO 转回 JSON 字符串(落库 canary_config_json 用)。
     *
     * @return DTO 的 JSON 字符串
     */
    public String toJson() {
        return JSON.toJSONString(this);
    }

    /**
     * Null-safe 取白名单 ── 调用方不必额外判 null,直接用于 IN 改绑。
     *
     * @return 白名单设备识别码列表;为 null 时返空列表
     */
    public List<String> safeDeviceIdentifications() {
        return deviceIdentifications == null ? Collections.emptyList() : deviceIdentifications;
    }
}
