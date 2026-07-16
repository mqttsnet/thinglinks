package com.mqttsnet.thinglinks.rule.linkage.execution.action;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionActionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.link.facade.DeviceCommandFacade;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmRecordService;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.service.execution.event.action.AlertActionEvent;
import com.mqttsnet.thinglinks.service.execution.event.action.CommandActionEvent;
import com.mqttsnet.thinglinks.service.execution.event.action.ForwardActionEvent;
import com.mqttsnet.thinglinks.service.execution.event.action.listener.RuleConditionActionEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("场景联动动作事件监听")
class RuleConditionActionEventListenerTest {

    @Mock
    private LinkCacheDataHelper linkCacheDataHelper;
    @Mock
    private DeviceCommandFacade deviceCommandApi;
    @Mock
    private RuleAlarmRecordService ruleAlarmRecordService;
    @Mock
    private RuleNotificationTemplateService ruleNotificationTemplateService;

    private RuleConditionActionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new RuleConditionActionEventListener(linkCacheDataHelper);
        ReflectionTestUtils.setField(listener, "deviceCommandApi", deviceCommandApi);
        ReflectionTestUtils.setField(listener, "ruleAlarmRecordService", ruleAlarmRecordService);
        ReflectionTestUtils.setField(listener, "ruleNotificationTemplateService", ruleNotificationTemplateService);
    }

    @Test
    @DisplayName("告警动作先解析新版配置，再携带执行上下文触发告警")
    void onAlertActionEventShouldParseConfigAndTriggerAlarmWithContext() {
        PolicyContext context = new PolicyContext();
        context.setRuleIdentification("rule-1");
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{\"version\":2,\"alarmIdentification\":\"alarm-1\"}")
                .build();
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .build();
        when(ruleNotificationTemplateService.parseActionConfig(action.getActionContent())).thenReturn(actionConfig);

        listener.onAlertActionEvent(new AlertActionEvent(this, context, action));

        verify(ruleNotificationTemplateService).parseActionConfig(action.getActionContent());
        verify(ruleAlarmRecordService).triggerDeviceAlarm(actionConfig, context);
    }

    @Test
    @DisplayName("告警动作配置异常会向上抛出，让动作执行日志标记失败")
    void onAlertActionEventShouldPropagateParseException() {
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("bad-json")
                .build();
        when(ruleNotificationTemplateService.parseActionConfig(action.getActionContent()))
                .thenThrow(new IllegalArgumentException("bad config"));

        assertThrows(IllegalArgumentException.class,
                () -> listener.onAlertActionEvent(new AlertActionEvent(this, new PolicyContext(), action)));

        verify(ruleAlarmRecordService, never()).triggerDeviceAlarm(any(), any());
    }

    @Test
    @DisplayName("告警记录落库或通知链路异常会向上抛出，避免执行日志误判成功")
    void onAlertActionEventShouldPropagateAlarmServiceException() {
        PolicyContext context = new PolicyContext();
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{\"version\":2,\"alarmIdentification\":\"alarm-1\"}")
                .build();
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .build();
        when(ruleNotificationTemplateService.parseActionConfig(action.getActionContent())).thenReturn(actionConfig);
        org.mockito.Mockito.doThrow(new IllegalStateException("alarm record not saved"))
                .when(ruleAlarmRecordService).triggerDeviceAlarm(actionConfig, context);

        assertThrows(IllegalStateException.class,
                () -> listener.onAlertActionEvent(new AlertActionEvent(this, context, action)));
    }

    @Test
    @DisplayName("命令动作将 actionContent 解析为设备命令包装对象后下发")
    void onCommandActionEventShouldIssueDeviceCommand() {
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{\"serial\":[{\"deviceIdentification\":\"device-1\","
                        + "\"productIdentification\":\"product-1\",\"msgType\":\"cloudReq\","
                        + "\"serviceCode\":\"default_attributes_controls\",\"cmd\":\"setBrightness\","
                        + "\"params\":{\"brightness\":1}}]}")
                .build();
        org.mockito.Mockito.doReturn(R.success("ok"))
                .when(deviceCommandApi).issueCommands(any(DeviceCommandWrapperParam.class));

        listener.onCommandActionEvent(new CommandActionEvent(this, new PolicyContext(), action));

        ArgumentCaptor<DeviceCommandWrapperParam> captor = ArgumentCaptor.forClass(DeviceCommandWrapperParam.class);
        verify(deviceCommandApi).issueCommands(captor.capture());
        assertEquals("device-1", captor.getValue().getSerial().get(0).getDeviceIdentification());
        assertEquals("setBrightness", captor.getValue().getSerial().get(0).getCmd());
    }

    @Test
    @DisplayName("命令动作 JSON 非法时只记录错误，不调用设备命令下发")
    void onCommandActionEventShouldSkipInvalidJson() {
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{bad-json")
                .build();

        assertDoesNotThrow(() -> listener.onCommandActionEvent(new CommandActionEvent(this, new PolicyContext(), action)));

        verify(deviceCommandApi, never()).issueCommands(any());
    }

    @Test
    @DisplayName("命令下发接口异常时监听器吞掉异常，避免中断规则动作链路")
    void onCommandActionEventShouldSwallowIssueException() {
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{\"serial\":[{\"deviceIdentification\":\"device-1\","
                        + "\"productIdentification\":\"product-1\",\"msgType\":\"cloudReq\","
                        + "\"serviceCode\":\"default_attributes_controls\",\"cmd\":\"setBrightness\","
                        + "\"params\":{\"brightness\":1}}]}")
                .build();
        org.mockito.Mockito.doThrow(new IllegalStateException("mqtt offline"))
                .when(deviceCommandApi).issueCommands(any(DeviceCommandWrapperParam.class));

        assertDoesNotThrow(() -> listener.onCommandActionEvent(new CommandActionEvent(this, new PolicyContext(), action)));
    }

    @Test
    @DisplayName("转发动作入口保持可调用，为后续北向转发扩展预留")
    void onForwardActionEventShouldBeCallable() {
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .actionContent("{\"target\":\"bridge\"}")
                .build();

        assertDoesNotThrow(() -> listener.onForwardActionEvent(new ForwardActionEvent(this, new PolicyContext(), action)));
    }
}
