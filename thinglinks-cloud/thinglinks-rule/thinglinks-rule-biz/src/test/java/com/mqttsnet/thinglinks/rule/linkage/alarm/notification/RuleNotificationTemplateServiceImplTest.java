package com.mqttsnet.thinglinks.rule.linkage.alarm.notification;

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
import com.mqttsnet.thinglinks.service.alarm.impl.RuleNotificationTemplateServiceImpl;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleNotificationPreviewParamVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationPreviewResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("场景联动告警通知模板")
class RuleNotificationTemplateServiceImplTest {

    private LinkCacheDataHelper linkCacheDataHelper;
    private RuleAlarmService ruleAlarmService;
    private RuleNotificationTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        linkCacheDataHelper = mock(LinkCacheDataHelper.class);
        ruleAlarmService = mock(RuleAlarmService.class);
        service = new RuleNotificationTemplateServiceImpl(linkCacheDataHelper, ruleAlarmService);
    }

    @Test
    @DisplayName("变量清单由后端统一维护并返回可插入占位符")
    void listVariablesShouldExposeMaintainedPlaceholders() {
        assertFalse(service.listVariables().isEmpty());
        assertTrue(service.listVariables().stream()
                .flatMap(group -> group.getVariables().stream())
                .anyMatch(variable -> "device.identification".equals(variable.getKey())
                        && "${device.identification}".equals(variable.getPlaceholder())));
        assertTrue(service.listVariables().stream()
                .flatMap(group -> group.getVariables().stream())
                .anyMatch(variable -> "link.executionLogUrl".equals(variable.getKey())));
    }

    @Test
    @DisplayName("空动作配置按新版配置兜底，避免历史空值阻断保存")
    void parseBlankActionConfigShouldReturnVersion2Config() {
        RuleAlarmActionConfigDTO config = service.parseActionConfig(" ");

        assertEquals(2, config.getVersion());
        assertTrue(config.getRecipients() == null || config.getRecipients().isEmpty());
    }

    @Test
    @DisplayName("新版动作配置保留接收人和渠道模板并强制版本号")
    void parseVersion2ActionConfigShouldKeepRecipientsAndTemplates() {
        String json = "{\"version\":2,\"alarmIdentification\":\"alarm-1\","
                + "\"recipients\":[{\"type\":\"PHONE\",\"value\":\"13800001111\"}],"
                + "\"channelTemplates\":[{\"channelType\":0,\"enabled\":true,\"format\":\"MARKDOWN\","
                + "\"titleTemplate\":\"${alarm.name}\",\"contentTemplate\":\"${device.name}\"}]}";

        RuleAlarmActionConfigDTO config = service.parseActionConfig(json);

        assertEquals(2, config.getVersion());
        assertEquals("alarm-1", config.getAlarmIdentification());
        assertEquals("13800001111", config.getRecipients().get(0).getValue());
        assertEquals(0, config.getChannelTemplates().get(0).getChannelType());
    }

    @Test
    @DisplayName("新版动作配置即使没有版本号，也能根据渠道模板字段识别")
    void parseChannelTemplateActionConfigShouldWorkWithoutVersion() {
        String json = "{\"channelTemplates\":[{\"channelType\":3,\"enabled\":true,"
                + "\"format\":\"NOTICE\",\"titleTemplate\":\"站内信\"}]}";

        RuleAlarmActionConfigDTO config = service.parseActionConfig(json);

        assertEquals(2, config.getVersion());
        assertEquals(AlarmChannelTypeEnum.SITE_MESSAGE.getValue(),
                config.getChannelTemplates().get(0).getChannelType());
    }

    @Test
    @DisplayName("新版动作配置即使没有版本号，也能根据接收人字段识别")
    void parseRecipientActionConfigShouldWorkWithoutVersion() {
        String json = "{\"recipients\":[{\"type\":\"EMPLOYEE\",\"value\":\"1001\"}]}";

        RuleAlarmActionConfigDTO config = service.parseActionConfig(json);

        assertEquals(2, config.getVersion());
        assertEquals("1001", config.getRecipients().get(0).getValue());
    }

    @Test
    @DisplayName("历史动作配置兼容 atPhone 并清洗富文本内容")
    void parseLegacyActionConfigShouldNormalizeRecipientsAndContent() {
        String json = "{\"alarmIdentification\":\"alarm-old\","
                + "\"atPhone\":\"13800001111, 13800001111,13900001111\","
                + "\"contentData\":\"<p>设备离线<br/>请处理&nbsp;</p>\"}";

        RuleAlarmActionConfigDTO config = service.parseActionConfig(json);

        assertEquals(2, config.getVersion());
        assertEquals("alarm-old", config.getAlarmIdentification());
        assertEquals(2, config.getRecipients().size());
        assertEquals("13800001111", config.getRecipients().get(0).getValue());
        assertEquals("设备离线 请处理", config.getContentData());
    }

    @Test
    @DisplayName("历史动作配置内容为空时清洗为空串，不影响规则保存")
    void parseLegacyActionConfigShouldNormalizeBlankContent() {
        RuleAlarmActionConfigDTO config = service.parseActionConfig("{\"contentData\":\"\"}");

        assertEquals(2, config.getVersion());
        assertEquals("", config.getContentData());
    }

    @Test
    @DisplayName("历史空请求配置仍按新版结构兜底")
    void legacyNullRequestShouldFallbackToVersion2Config() throws Exception {
        Method method = RuleNotificationTemplateServiceImpl.class
                .getDeclaredMethod("legacyToActionConfig", DeviceAlarmNotificationRequestParam.class);
        method.setAccessible(true);

        RuleAlarmActionConfigDTO config =
                (RuleAlarmActionConfigDTO) method.invoke(service, new Object[]{null});

        assertEquals(2, config.getVersion());
        assertTrue(config.getRecipients() == null || config.getRecipients().isEmpty());
    }

    @Test
    @DisplayName("内部配置归一化遇到空对象时直接跳过")
    void normalizeNullActionConfigShouldBeNoop() throws Exception {
        Method method = RuleNotificationTemplateServiceImpl.class
                .getDeclaredMethod("normalizeActionConfig", RuleAlarmActionConfigDTO.class);
        method.setAccessible(true);

        method.invoke(service, new Object[]{null});
    }

    @Test
    @DisplayName("非法动作内容按原文降级为历史内容，规则执行不抛错")
    void parseInvalidActionConfigShouldFallbackToRawContent() {
        RuleAlarmActionConfigDTO config = service.parseActionConfig("not-json-content");

        assertEquals(2, config.getVersion());
        assertEquals("not-json-content", config.getContentData());
    }

    @Test
    @DisplayName("运行时变量合并规则、告警、触发事件，并用缓存补齐设备和产品名称")
    void buildRuntimeVariablesShouldMergeContextAlarmAndCacheNames() {
        PolicyContext context = new PolicyContext();
        context.setTenantId("tenant-a");
        context.setRuleName("离线联动");
        context.setRuleIdentification("rule-1");
        context.setRuleExecutionId(10001L);
        context.setTriggerEvent(TriggerEventDTO.builder()
                .productIdentification("product-1")
                .deviceIdentification("device-1")
                .actionType("DISCONNECT")
                .payloadKind("RAW")
                .rawMessage("{\"event\":\"disconnect\"}")
                .eventUtc(0L)
                .build());
        when(linkCacheDataHelper.getDeviceCacheVO("device-1")).thenReturn(Optional.of(DeviceCacheVO.builder()
                .deviceIdentification("device-1")
                .deviceName("测试设备")
                .productIdentification("product-cache")
                .build()));
        when(linkCacheDataHelper.getProductCacheVO("product-cache")).thenReturn(Optional.of(ProductCacheVO.builder()
                .productIdentification("product-cache")
                .productName("测试产品")
                .build()));

        Map<String, Object> variables = service.buildRuntimeVariables(context, alarmDetails());

        assertEquals("tenant-a", variables.get("tenant.id"));
        assertEquals("离线联动", variables.get("rule.name"));
        assertEquals("设备离线告警", variables.get("alarm.name"));
        assertEquals("测试设备", variables.get("device.name"));
        assertEquals("测试产品", variables.get("product.name"));
        assertEquals("DISCONNECT", variables.get("trigger.actionType"));
        assertEquals("/#/engine/linkage/save?id=rule-1&type=handleView", variables.get("link.ruleUrl"));
        assertTrue(String.valueOf(variables.get("trigger.time")).matches("\\d{4}-\\d{2}-\\d{2} .*"));
    }

    @Test
    @DisplayName("运行时变量没有触发事件时保留默认预览变量")
    void buildRuntimeVariablesShouldKeepDefaultsWhenTriggerEventMissing() {
        PolicyContext context = new PolicyContext();
        context.setTenantId("");

        Map<String, Object> variables = service.buildRuntimeVariables(context, null);

        assertEquals("设备离线告警联动", variables.get("rule.name"));
        assertEquals("测试普通设备01", variables.get("device.name"));
    }

    @Test
    @DisplayName("运行时变量仅有产品标识时只补齐产品名称，不查询设备缓存")
    void buildRuntimeVariablesShouldFillProductNameWhenOnlyProductIdentifierExists() {
        PolicyContext context = new PolicyContext();
        context.setTriggerEvent(TriggerEventDTO.builder()
                .productIdentification("product-only")
                .deviceIdentification("")
                .eventUtc(0L)
                .build());
        when(linkCacheDataHelper.getProductCacheVO("product-only")).thenReturn(Optional.of(ProductCacheVO.builder()
                .productIdentification("product-only")
                .productName("仅产品规则")
                .build()));

        Map<String, Object> variables = service.buildRuntimeVariables(context, null);

        assertEquals("仅产品规则", variables.get("product.name"));
        assertTrue(String.valueOf(variables.get("trigger.time")).matches("\\d{4}-\\d{2}-\\d{2} .*"));
    }

    @Test
    @DisplayName("运行时变量没有产品和设备标识时不访问缓存并保留默认名称")
    void buildRuntimeVariablesShouldSkipCacheWhenIdentifiersMissing() {
        PolicyContext context = new PolicyContext();
        context.setTriggerEvent(TriggerEventDTO.builder()
                .productIdentification("")
                .deviceIdentification("")
                .eventUtc(0L)
                .build());

        Map<String, Object> variables = service.buildRuntimeVariables(context, null);

        assertEquals("全志F135水晶球", variables.get("product.name"));
        assertEquals("测试普通设备01", variables.get("device.name"));
    }

    @Test
    @DisplayName("运行时变量在缓存未命中时保留触发消息里的产品和设备标识")
    void buildRuntimeVariablesShouldKeepTriggerIdentifiersWhenCacheMissed() {
        PolicyContext context = new PolicyContext();
        context.setTriggerEvent(TriggerEventDTO.builder()
                .productIdentification("product-raw")
                .deviceIdentification("device-raw")
                .build());
        when(linkCacheDataHelper.getDeviceCacheVO("device-raw")).thenReturn(Optional.empty());
        when(linkCacheDataHelper.getProductCacheVO("product-raw")).thenReturn(Optional.empty());

        Map<String, Object> variables = service.buildRuntimeVariables(context, null);

        assertEquals("product-raw", variables.get("product.identification"));
        assertEquals("device-raw", variables.get("device.identification"));
    }

    @Test
    @DisplayName("渠道模板命中启用配置时直接返回该配置")
    void resolveChannelTemplateShouldReturnEnabledConfiguredTemplate() {
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .channelTemplates(Collections.singletonList(RuleAlarmChannelTemplateDTO.builder()
                        .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                        .enabled(true)
                        .format("MARKDOWN")
                        .contentTemplate("命中配置")
                        .build()))
                .build();

        RuleAlarmChannelTemplateDTO template = service.resolveChannelTemplate(
                actionConfig,
                AlarmChannelTypeEnum.DING_TALK.getValue(),
                alarmDetails());

        assertEquals("命中配置", template.getContentTemplate());
    }

    @Test
    @DisplayName("渠道模板优先使用启用配置，禁用配置回退默认模板")
    void resolveChannelTemplateShouldRespectEnabledFlag() {
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .contentData("历史告警内容")
                .channelTemplates(Collections.singletonList(RuleAlarmChannelTemplateDTO.builder()
                        .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                        .enabled(false)
                        .contentTemplate("不会发送")
                        .build()))
                .build();

        RuleAlarmChannelTemplateDTO template = service.resolveChannelTemplate(
                actionConfig,
                AlarmChannelTypeEnum.DING_TALK.getValue(),
                alarmDetails());

        assertEquals("历史告警内容", template.getContentTemplate());
        assertEquals("MARKDOWN", template.getFormat());
    }

    @Test
    @DisplayName("渲染空模板时使用默认模板并标记未知渠道")
    void renderShouldFallbackToDefaultTemplateWhenTemplateMissing() {
        RuleAlarmRenderedNotificationDTO rendered = service.render(
                RuleAlarmActionConfigDTO.builder().build(),
                null,
                alarmDetails(),
                service.buildRuntimeVariables(null, alarmDetails()));

        assertEquals("未知渠道", rendered.getChannelName());
        assertEquals("MARKDOWN", rendered.getFormat());
        assertTrue(rendered.getContent().contains("设备离线告警"));
    }

    @Test
    @DisplayName("渲染时渠道级接收人覆盖全局接收人，未知变量保留占位符")
    void renderShouldUseChannelRecipientsAndKeepUnknownPlaceholder() {
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .recipients(Collections.singletonList(phone("13800001111")))
                .build();
        RuleAlarmChannelTemplateDTO template = RuleAlarmChannelTemplateDTO.builder()
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .format("NOTICE")
                .titleTemplate("${alarm.name}")
                .contentTemplate("${device.name} ${missing.value}")
                .urlTemplate("${link.executionLogUrl}")
                .recipients(Collections.singletonList(employee("1001")))
                .build();

        RuleAlarmRenderedNotificationDTO rendered = service.render(
                actionConfig,
                template,
                alarmDetails(),
                service.buildRuntimeVariables(null, alarmDetails()));

        assertEquals("设备离线告警", rendered.getTitle());
        assertTrue(rendered.getContent().contains("${missing.value}"));
        assertEquals("1001", rendered.getRecipients().get(0).getValue());
    }

    @Test
    @DisplayName("预览只渲染启用渠道，并允许前端样例变量覆盖默认值")
    void previewShouldSkipDisabledTemplatesAndApplySampleVariables() {
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails());
        RuleNotificationPreviewParamVO param = RuleNotificationPreviewParamVO.builder()
                .alarmIdentification("alarm-1")
                .recipients(Collections.singletonList(phone("13800001111")))
                .sampleVariables(Collections.singletonMap("device.name", "预览设备"))
                .channelTemplates(Arrays.asList(
                        RuleAlarmChannelTemplateDTO.builder()
                                .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                                .enabled(true)
                                .format("MARKDOWN")
                                .titleTemplate("${alarm.name}")
                                .contentTemplate("${device.name}")
                                .build(),
                        RuleAlarmChannelTemplateDTO.builder()
                                .channelType(AlarmChannelTypeEnum.FS.getValue())
                                .enabled(false)
                                .contentTemplate("disabled")
                                .build()))
                .build();

        RuleNotificationPreviewResultVO preview = service.preview(param);

        assertEquals(1, preview.getChannels().size());
        assertEquals("预览设备", preview.getChannels().get(0).getContent());
        verify(ruleAlarmService).getRuleAlarmDetailsByAlarmIdentification("alarm-1");
    }

    @Test
    @DisplayName("未配置渠道模板时预览为每个告警渠道生成默认模板")
    void previewShouldCreateDefaultTemplateForEveryChannelWhenMissingConfig() {
        RuleNotificationPreviewResultVO preview = service.preview(RuleNotificationPreviewParamVO.builder().build());

        assertEquals(AlarmChannelTypeEnum.values().length, preview.getChannels().size());
        assertTrue(preview.getChannels().stream()
                .anyMatch(item -> AlarmChannelTypeEnum.SITE_MESSAGE.getValue().equals(item.getChannelType())
                        && "NOTICE".equals(item.getFormat())
                        && item.getUrl().contains("/#/engine/linkage/save")));
        assertTrue(preview.getChannels().stream()
                .anyMatch(item -> AlarmChannelTypeEnum.FS.getValue().equals(item.getChannelType())
                        && "TEXT".equals(item.getFormat())
                        && item.getContent().contains("执行日志：/#/engine/linkage/save")));
    }

    @Test
    @DisplayName("空预览参数仍返回默认渠道预览，前端不会白屏")
    void previewShouldHandleNullParam() {
        RuleNotificationPreviewResultVO preview = service.preview(null);

        assertNotNull(preview);
        assertEquals(AlarmChannelTypeEnum.values().length, preview.getChannels().size());
    }

    private RuleAlarmDetailsResultVO alarmDetails() {
        return RuleAlarmDetailsResultVO.builder()
                .alarmName("设备离线告警")
                .alarmIdentification("alarm-1")
                .alarmScene("设备状态")
                .level(2)
                .build();
    }

    private RuleAlarmRecipientDTO phone(String value) {
        return RuleAlarmRecipientDTO.builder()
                .type("PHONE")
                .value(value)
                .label(value)
                .build();
    }

    private RuleAlarmRecipientDTO employee(String value) {
        return RuleAlarmRecipientDTO.builder()
                .type("EMPLOYEE")
                .value(value)
                .label(value)
                .build();
    }
}
