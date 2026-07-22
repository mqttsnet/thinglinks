package com.mqttsnet.thinglinks.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.utils.topic.TopicPlaceholders;
import com.mqttsnet.thinglinks.entity.acl.DeviceAclRule;
import com.mqttsnet.thinglinks.entity.device.DeviceInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * ACL 主题占位符替换 ── 把 {@link DeviceInfo} 适配成 {@link TopicPlaceholders} 的 resolver,
 * 批量处理 {@link DeviceAclRule} 列表。
 *
 * @author mqttsnet
 * @since 2025-09-29
 */
@Slf4j
public class AclTopicPatternPlaceholderReplacer {

    private AclTopicPatternPlaceholderReplacer() {
        throw new UnsupportedOperationException("ACL主题模式占位符替换工具类不允许实例化");
    }

    /**
     * 批量替换 ACL 规则列表中的主题模式占位符(就地修改 {@code rule.topicPattern})。
     */
    public static void replacePlaceholders(List<DeviceAclRule> rules, Optional<DeviceInfo> deviceInfoOpt) {
        if (CollectionUtil.isEmpty(rules)) {
            return;
        }
        Function<String, String> resolver = buildResolver(deviceInfoOpt.orElse(null));
        rules.stream()
            .filter(rule -> StrUtil.isNotBlank(rule.getTopicPattern()))
            .forEach(rule -> replaceOne(rule, resolver));
    }

    /**
     * 替换单个字符串中的占位符(不修改任何对象)。
     */
    public static String replacePlaceholders(String pattern, Optional<DeviceInfo> deviceInfoOpt) {
        return TopicPlaceholders.replace(pattern, buildResolver(deviceInfoOpt.orElse(null)));
    }

    private static void replaceOne(DeviceAclRule rule, Function<String, String> resolver) {
        if (Objects.isNull(rule)) {
            return;
        }
        String original = rule.getTopicPattern();
        if (!TopicPlaceholders.containsPlaceholders(original)) {
            return;
        }
        String replaced = TopicPlaceholders.replace(original, resolver);
        if (!original.equals(replaced)) {
            rule.setTopicPattern(replaced);
        }
    }

    /**
     * 把 DeviceInfo 适配成 resolver Function。设备信息为空时返回空串(与原行为一致)。
     */
    private static Function<String, String> buildResolver(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return key -> "";
        }
        return key -> switch (key) {
            case TopicPlaceholders.KEY_APP_ID -> deviceInfo.getAppId();
            case TopicPlaceholders.KEY_USER_NAME -> deviceInfo.getUserName();
            case TopicPlaceholders.KEY_DEVICE_IDENTIFICATION -> deviceInfo.getDeviceIdentification();
            case TopicPlaceholders.KEY_PRODUCT_IDENTIFICATION -> deviceInfo.getProductIdentification();
            case TopicPlaceholders.KEY_DEVICE_SDK_VERSION -> deviceInfo.getDeviceSdkVersion();
            default -> null;
        };
    }
}
