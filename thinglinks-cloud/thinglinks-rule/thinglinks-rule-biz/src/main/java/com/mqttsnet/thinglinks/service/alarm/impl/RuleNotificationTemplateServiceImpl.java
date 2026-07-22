package com.mqttsnet.thinglinks.service.alarm.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmChannelTemplateDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRecipientDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRenderedNotificationDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceAlarmNotificationRequestParam;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmService;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleNotificationPreviewParamVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationPreviewResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationVariableGroupResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationVariableResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则告警通知模板服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleNotificationTemplateServiceImpl implements RuleNotificationTemplateService {

    private static final int ACTION_CONFIG_VERSION = 2;
    private static final String FORMAT_MARKDOWN = "MARKDOWN";
    private static final String FORMAT_NOTICE = "NOTICE";
    private static final String FORMAT_TEXT = "TEXT";
    private static final String RECIPIENT_PHONE = "PHONE";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final RuleAlarmService ruleAlarmService;

    @Value("${thinglinks.console-url:}")
    private String consoleUrl;

    @Override
    public List<RuleNotificationVariableGroupResultVO> listVariables() {
        return Arrays.asList(
                group("rule", "规则", Arrays.asList(
                        variable("rule.name", "规则名称", "当前场景联动规则名称", "设备离线告警联动"),
                        variable("rule.identification", "规则标识", "当前场景联动规则标识", "691884008387597318"),
                        variable("rule.executionId", "执行流水号", "本次规则执行流水号", "764352343040583682")
                )),
                group("alarm", "告警", Arrays.asList(
                        variable("alarm.name", "告警名称", "所选告警规则名称", "设备离线告警"),
                        variable("alarm.identification", "告警标识", "所选告警规则标识", "ALARM_DEVICE_OFFLINE"),
                        variable("alarm.scene", "告警场景", "告警规则所属场景", "设备状态"),
                        variable("alarm.level", "告警等级", "告警等级编码", "2")
                )),
                group("device", "设备", Arrays.asList(
                        variable("product.name", "产品名称", "触发设备所属产品名称", "全志F135水晶球"),
                        variable("product.identification", "产品标识", "触发设备所属产品标识", "2992007290322944"),
                        variable("device.name", "设备名称", "触发设备名称", "测试普通设备01"),
                        variable("device.identification", "设备标识", "触发设备标识", "3752151419551745")
                )),
                group("trigger", "触发事件", Arrays.asList(
                        variable("trigger.time", "触发时间", "设备事件触发时间", DateUtil.now()),
                        variable("trigger.actionType", "设备动作", "MQS 设备动作类型", "DISCONNECT"),
                        variable("trigger.payloadKind", "报文形态", "RAW 或 THING_MODEL", "THING_MODEL"),
                        variable("trigger.rawMessage", "原始报文", "触发规则的原始报文", "{\"status\":\"offline\"}")
                )),
                group("link", "链接", Arrays.asList(
                        variable("link.ruleUrl", "规则详情链接", "场景联动规则详情地址",
                                buildRuleDetailUrl("691884008387597318")),
                        variable("link.executionLogUrl", "执行日志链接", "规则执行日志地址",
                                buildRuleDetailUrl("691884008387597318"))
                ))
        );
    }

    @Override
    public RuleAlarmActionConfigDTO parseActionConfig(String actionContent) {
        if (StrUtil.isBlank(actionContent)) {
            return RuleAlarmActionConfigDTO.builder().version(ACTION_CONFIG_VERSION).build();
        }
        try {
            JSONObject object = JSON.parseObject(actionContent);
            if (Objects.equals(object.getInteger("version"), ACTION_CONFIG_VERSION)
                    || object.containsKey("channelTemplates")
                    || object.containsKey("recipients")) {
                RuleAlarmActionConfigDTO config = object.toJavaObject(RuleAlarmActionConfigDTO.class);
                normalizeActionConfig(config);
                return config;
            }
            DeviceAlarmNotificationRequestParam legacy =
                    object.toJavaObject(DeviceAlarmNotificationRequestParam.class);
            return legacyToActionConfig(legacy);
        } catch (Exception e) {
            log.warn("Parse alarm action config failed, fallback to raw content. actionContent={}, reason={}",
                    actionContent, e.getMessage());
            return RuleAlarmActionConfigDTO.builder()
                    .version(ACTION_CONFIG_VERSION)
                    .contentData(actionContent)
                    .build();
        }
    }

    @Override
    public Map<String, Object> buildRuntimeVariables(PolicyContext policyContext,
                                                     RuleAlarmDetailsResultVO alarmDetails) {
        Map<String, Object> variables = defaultPreviewVariables();
        Optional.ofNullable(alarmDetails).ifPresent(alarm -> {
            put(variables, "alarm.name", alarm.getAlarmName());
            put(variables, "alarm.identification", alarm.getAlarmIdentification());
            put(variables, "alarm.scene", alarm.getAlarmScene());
            put(variables, "alarm.level", alarm.getLevel());
        });

        Optional.ofNullable(policyContext).ifPresent(context -> {
            put(variables, "tenant.id", context.getTenantId());
            put(variables, "rule.name", context.getRuleName());
            put(variables, "rule.identification", context.getRuleIdentification());
            put(variables, "rule.executionId", context.getRuleExecutionId());
            put(variables, "link.ruleUrl", buildRuleDetailUrl(context.getRuleIdentification()));
            put(variables, "link.executionLogUrl", buildRuleDetailUrl(context.getRuleIdentification()));

            TriggerEventDTO triggerEvent = context.getTriggerEvent();
            if (triggerEvent != null) {
                put(variables, "product.identification", triggerEvent.getProductIdentification());
                put(variables, "device.identification", triggerEvent.getDeviceIdentification());
                put(variables, "trigger.actionType", triggerEvent.getActionType());
                put(variables, "trigger.payloadKind", triggerEvent.getPayloadKind());
                put(variables, "trigger.rawMessage", triggerEvent.getRawMessage());
                put(variables, "trigger.time", formatEventTime(triggerEvent.getEventUtc()));
                fillDeviceAndProductNames(variables, triggerEvent);
            }
        });
        return variables;
    }

    @Override
    public RuleAlarmChannelTemplateDTO resolveChannelTemplate(RuleAlarmActionConfigDTO actionConfig,
                                                              Integer channelType,
                                                              RuleAlarmDetailsResultVO alarmDetails) {
        RuleAlarmChannelTemplateDTO configured = Optional.ofNullable(actionConfig)
                .map(RuleAlarmActionConfigDTO::getChannelTemplates)
                .orElse(Collections.emptyList())
                .stream()
                .filter(item -> Objects.equals(item.getChannelType(), channelType))
                .filter(item -> !Boolean.FALSE.equals(item.getEnabled()))
                .findFirst()
                .orElse(null);
        if (configured != null) {
            return configured;
        }
        return defaultTemplate(channelType, alarmDetails,
                Optional.ofNullable(actionConfig).map(RuleAlarmActionConfigDTO::getContentData).orElse(null));
    }

    @Override
    public RuleAlarmRenderedNotificationDTO render(RuleAlarmActionConfigDTO actionConfig,
                                                   RuleAlarmChannelTemplateDTO template,
                                                   RuleAlarmDetailsResultVO alarmDetails,
                                                   Map<String, Object> variables) {
        Integer channelType = Optional.ofNullable(template)
                .map(RuleAlarmChannelTemplateDTO::getChannelType)
                .orElse(null);
        AlarmChannelTypeEnum channelTypeEnum = AlarmChannelTypeEnum.fromValue(channelType).orElse(null);
        RuleAlarmChannelTemplateDTO actualTemplate = template != null
                ? template
                : defaultTemplate(channelType, alarmDetails, null);
        List<RuleAlarmRecipientDTO> recipients = CollUtil.isNotEmpty(actualTemplate.getRecipients())
                ? actualTemplate.getRecipients()
                : Optional.ofNullable(actionConfig).map(RuleAlarmActionConfigDTO::getRecipients).orElse(Collections.emptyList());

        return RuleAlarmRenderedNotificationDTO.builder()
                .channelType(channelType)
                .channelName(channelTypeEnum == null ? "未知渠道" : channelTypeEnum.getDesc())
                .format(StrUtil.blankToDefault(actualTemplate.getFormat(), defaultFormat(channelType)))
                .title(renderTemplate(actualTemplate.getTitleTemplate(), variables))
                .content(renderTemplate(actualTemplate.getContentTemplate(), variables))
                .url(renderTemplate(actualTemplate.getUrlTemplate(), variables))
                .atAll(Boolean.TRUE.equals(actualTemplate.getAtAll()))
                .recipients(recipients)
                .build();
    }

    @Override
    public RuleNotificationPreviewResultVO preview(RuleNotificationPreviewParamVO param) {
        RuleAlarmDetailsResultVO alarmDetails = null;
        if (param != null && StrUtil.isNotBlank(param.getAlarmIdentification())) {
            alarmDetails = ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification(param.getAlarmIdentification());
        }
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(ACTION_CONFIG_VERSION)
                .alarmIdentification(param == null ? null : param.getAlarmIdentification())
                .recipients(param == null ? Collections.emptyList() : param.getRecipients())
                .channelTemplates(param == null ? Collections.emptyList() : param.getChannelTemplates())
                .build();
        Map<String, Object> variables = defaultPreviewVariables();
        Optional.ofNullable(alarmDetails).ifPresent(alarm -> {
            put(variables, "alarm.name", alarm.getAlarmName());
            put(variables, "alarm.identification", alarm.getAlarmIdentification());
            put(variables, "alarm.scene", alarm.getAlarmScene());
            put(variables, "alarm.level", alarm.getLevel());
        });
        if (param != null && param.getSampleVariables() != null) {
            variables.putAll(param.getSampleVariables());
        }
        List<RuleAlarmRenderedNotificationDTO> renderedList = new ArrayList<>();
        final RuleAlarmDetailsResultVO previewAlarmDetails = alarmDetails;
        List<RuleAlarmChannelTemplateDTO> templates = Optional.ofNullable(actionConfig.getChannelTemplates())
                .orElse(Collections.emptyList());
        if (CollUtil.isEmpty(templates)) {
            for (AlarmChannelTypeEnum channelTypeEnum : AlarmChannelTypeEnum.values()) {
                RuleAlarmChannelTemplateDTO template =
                        resolveChannelTemplate(actionConfig, channelTypeEnum.getValue(), previewAlarmDetails);
                renderedList.add(render(actionConfig, template, previewAlarmDetails, variables));
            }
        } else {
            templates.stream()
                    .filter(item -> !Boolean.FALSE.equals(item.getEnabled()))
                    .forEach(template -> renderedList.add(render(actionConfig, template, previewAlarmDetails, variables)));
        }
        return RuleNotificationPreviewResultVO.builder().channels(renderedList).build();
    }

    private RuleAlarmActionConfigDTO legacyToActionConfig(DeviceAlarmNotificationRequestParam legacy) {
        RuleAlarmActionConfigDTO config = RuleAlarmActionConfigDTO.builder()
                .version(ACTION_CONFIG_VERSION)
                .alarmIdentification(legacy == null ? null : legacy.getAlarmIdentification())
                .atPhone(legacy == null ? null : legacy.getAtPhone())
                .contentData(legacy == null ? null : normalizeLegacyContent(legacy.getContentData()))
                .build();
        normalizeActionConfig(config);
        return config;
    }

    private void normalizeActionConfig(RuleAlarmActionConfigDTO config) {
        if (config == null) {
            return;
        }
        config.setVersion(ACTION_CONFIG_VERSION);
        if (CollUtil.isEmpty(config.getRecipients()) && StrUtil.isNotBlank(config.getAtPhone())) {
            List<RuleAlarmRecipientDTO> recipients = new ArrayList<>();
            Arrays.stream(config.getAtPhone().split(StrUtil.COMMA))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .forEach(phone -> recipients.add(RuleAlarmRecipientDTO.builder()
                            .type(RECIPIENT_PHONE)
                            .value(phone)
                            .label(phone)
                            .build()));
            config.setRecipients(recipients);
        }
    }

    private void fillDeviceAndProductNames(Map<String, Object> variables, TriggerEventDTO triggerEvent) {
        String deviceIdentification = triggerEvent.getDeviceIdentification();
        String productIdentification = triggerEvent.getProductIdentification();
        if (StrUtil.isNotBlank(deviceIdentification)) {
            Optional<DeviceCacheVO> deviceCache = linkCacheDataHelper.getDeviceCacheVO(deviceIdentification);
            final String fallbackProductIdentification = productIdentification;
            deviceCache.ifPresent(device -> {
                put(variables, "device.name", device.getDeviceName());
                put(variables, "device.identification", device.getDeviceIdentification());
                put(variables, "product.identification",
                        StrUtil.blankToDefault(device.getProductIdentification(), fallbackProductIdentification));
            });
            productIdentification = deviceCache.map(DeviceCacheVO::getProductIdentification).orElse(productIdentification);
        }
        if (StrUtil.isNotBlank(productIdentification)) {
            Optional<ProductCacheVO> productCache = linkCacheDataHelper.getProductCacheVO(productIdentification);
            productCache.ifPresent(product -> {
                put(variables, "product.name", product.getProductName());
                put(variables, "product.identification", product.getProductIdentification());
            });
        }
    }

    private RuleAlarmChannelTemplateDTO defaultTemplate(Integer channelType,
                                                        RuleAlarmDetailsResultVO alarmDetails,
                                                        String legacyContent) {
        String title = "【告警】${alarm.name}";
        String content = StrUtil.isNotBlank(legacyContent)
                ? normalizeLegacyContent(legacyContent)
                : defaultContent(channelType);
        return RuleAlarmChannelTemplateDTO.builder()
                .channelType(channelType)
                .enabled(true)
                .format(defaultFormat(channelType))
                .titleTemplate(title)
                .contentTemplate(content)
                .urlTemplate(defaultUrl(channelType))
                .build();
    }

    private String defaultContent(Integer channelType) {
        if (AlarmChannelTypeEnum.SITE_MESSAGE.getValue().equals(channelType)) {
            return "${alarm.name}\n"
                    + "规则：${rule.name}\n"
                    + "产品：${product.name}\n"
                    + "设备：${device.name}（${device.identification}）\n"
                    + "事件：${trigger.actionType}\n"
                    + "时间：${trigger.time}\n"
                    + "执行流水：${rule.executionId}";
        }
        if (AlarmChannelTypeEnum.FS.getValue().equals(channelType)) {
            return "【${alarm.name}】\n"
                    + "设备：${device.name}（${device.identification}）\n"
                    + "事件：${trigger.actionType}\n"
                    + "时间：${trigger.time}\n"
                    + "规则：${rule.name}\n"
                    + "流水：${rule.executionId}\n"
                    + "执行日志：${link.executionLogUrl}";
        }
        return "### ${alarm.name}\n\n"
                + "- **规则**：${rule.name}\n"
                + "- **产品**：${product.name}\n"
                + "- **设备**：${device.name}（`${device.identification}`）\n"
                + "- **事件**：${trigger.actionType}\n"
                + "- **时间**：${trigger.time}\n"
                + "- **执行流水**：`${rule.executionId}`\n\n"
                + "[查看执行日志](${link.executionLogUrl})";
    }

    private String defaultFormat(Integer channelType) {
        if (AlarmChannelTypeEnum.SITE_MESSAGE.getValue().equals(channelType)) {
            return FORMAT_NOTICE;
        }
        if (AlarmChannelTypeEnum.FS.getValue().equals(channelType)) {
            return FORMAT_TEXT;
        }
        return FORMAT_MARKDOWN;
    }

    private String defaultUrl(Integer channelType) {
        return AlarmChannelTypeEnum.SITE_MESSAGE.getValue().equals(channelType) ? "${link.executionLogUrl}" : "";
    }

    private Map<String, Object> defaultPreviewVariables() {
        Map<String, Object> variables = new LinkedHashMap<>();
        put(variables, "tenant.id", "default");
        put(variables, "rule.name", "设备离线告警联动");
        put(variables, "rule.identification", "691884008387597318");
        put(variables, "rule.executionId", "764352343040583682");
        put(variables, "alarm.name", "设备离线告警");
        put(variables, "alarm.identification", "ALARM_DEVICE_OFFLINE");
        put(variables, "alarm.scene", "设备状态");
        put(variables, "alarm.level", "2");
        put(variables, "product.name", "全志F135水晶球");
        put(variables, "product.identification", "2992007290322944");
        put(variables, "device.name", "测试普通设备01");
        put(variables, "device.identification", "3752151419551745");
        put(variables, "trigger.time", DateUtil.now());
        put(variables, "trigger.actionType", "DISCONNECT");
        put(variables, "trigger.payloadKind", "THING_MODEL");
        put(variables, "trigger.rawMessage", "{\"status\":\"offline\"}");
        put(variables, "link.ruleUrl", buildRuleDetailUrl("691884008387597318"));
        put(variables, "link.executionLogUrl", buildRuleDetailUrl("691884008387597318"));
        return variables;
    }

    private String buildRuleDetailUrl(String ruleIdentification) {
        String path = StrUtil.isBlank(ruleIdentification)
                ? "/#/engine/linkage"
                : "/#/engine/linkage/save?id=" + ruleIdentification + "&type=handleView";
        return buildConsoleUrl(path);
    }

    private String buildConsoleUrl(String path) {
        if (StrUtil.isBlank(consoleUrl)) {
            return path;
        }
        String cleanBase = StrUtil.removeSuffix(consoleUrl.trim(), "/");
        String cleanPath = StrUtil.blankToDefault(path, "/#/engine/linkage");
        if (cleanPath.startsWith("/")) {
            return cleanBase + cleanPath;
        }
        return cleanBase + "/" + cleanPath;
    }

    private RuleNotificationVariableGroupResultVO group(String groupCode,
                                                        String groupName,
                                                        List<RuleNotificationVariableResultVO> variables) {
        return RuleNotificationVariableGroupResultVO.builder()
                .groupCode(groupCode)
                .groupName(groupName)
                .variables(variables)
                .build();
    }

    private RuleNotificationVariableResultVO variable(String key,
                                                      String label,
                                                      String description,
                                                      String sample) {
        return RuleNotificationVariableResultVO.builder()
                .key(key)
                .placeholder("${" + key + "}")
                .label(label)
                .description(description)
                .sample(sample)
                .build();
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (StrUtil.isBlank(template)) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value == null ? matcher.group(0) : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String normalizeLegacyContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return content.trim()
                .replaceAll("(?i)^\\s*<p[^>]*>\\s*", "")
                .replaceAll("(?i)\\s*</p>\\s*$", "")
                .replace("<br/>", "\n")
                .replace("<br>", "\n")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String formatEventTime(Long eventUtc) {
        if (eventUtc == null) {
            return DateUtil.now();
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(eventUtc), ZoneId.systemDefault());
        return localDateTime.format(TIME_FORMATTER);
    }

    private void put(Map<String, Object> variables, String key, Object value) {
        if (value != null && StrUtil.isNotBlank(String.valueOf(value))) {
            variables.put(key, value);
        }
    }
}
