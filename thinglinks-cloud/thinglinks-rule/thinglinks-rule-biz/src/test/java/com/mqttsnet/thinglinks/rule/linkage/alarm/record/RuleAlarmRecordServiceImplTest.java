package com.mqttsnet.thinglinks.rule.linkage.alarm.record;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmChannelTemplateDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRecipientDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRenderedNotificationDTO;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarmRecord;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelStatusEnum;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelTypeEnum;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmRecordHandledStatusEnum;
import com.mqttsnet.thinglinks.msg.enumeration.NoticeRemindModeEnum;
import com.mqttsnet.thinglinks.msg.facade.MsgFacade;
import com.mqttsnet.thinglinks.msg.vo.save.ExtendMsgRecipientSaveVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgPublishVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgSendVO;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceAlarmNotificationRequestParam;
import com.mqttsnet.thinglinks.manager.alarm.RuleAlarmRecordManager;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmService;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.service.alarm.impl.RuleAlarmRecordServiceImpl;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleAlarmRecordHandleParamVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmChannelDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordResultVO;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmRecordSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmRecordUpdateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("告警中心记录与通知发送")
class RuleAlarmRecordServiceImplTest {

    @Mock
    private RuleAlarmService ruleAlarmService;
    @Mock
    private RuleNotificationTemplateService ruleNotificationTemplateService;
    @Mock
    private MsgFacade msgApi;
    @Mock
    private RuleAlarmRecordManager ruleAlarmRecordManager;

    private RuleAlarmRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new RuleAlarmRecordServiceImpl());
        ReflectionTestUtils.setField(service, "ruleAlarmService", ruleAlarmService);
        ReflectionTestUtils.setField(service, "ruleNotificationTemplateService", ruleNotificationTemplateService);
        ReflectionTestUtils.setField(service, "msgApi", msgApi);
        ReflectionTestUtils.setField(service, "superManager", ruleAlarmRecordManager);
    }

    @Test
    @DisplayName("站内信渠道使用员工接收人发布系统消息，并落一条待处理告警记录")
    void triggerDeviceAlarmShouldPublishSiteNoticeAndSaveAlarmRecord() {
        stubSaveAlarmRecord();
        PolicyContext context = new PolicyContext();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(siteChannel());
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.SITE_MESSAGE,
                Collections.singletonList(recipient("EMPLOYEE", "1001")),
                false);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(actionConfig,
                AlarmChannelTypeEnum.SITE_MESSAGE.getValue(), alarmDetails)).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(context, alarmDetails))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(actionConfig, template, alarmDetails, Map.of("device.name", "测试设备")))
                .thenReturn(rendered);
        when(msgApi.publish(any(ExtendMsgPublishVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, context));

        ArgumentCaptor<ExtendMsgPublishVO> publishCaptor = ArgumentCaptor.forClass(ExtendMsgPublishVO.class);
        verify(msgApi).publish(publishCaptor.capture());
        ExtendMsgPublishVO publishVO = publishCaptor.getValue();
        assertEquals("设备离线告警", publishVO.getTitle());
        assertEquals("设备 device-1 已离线", publishVO.getContent());
        assertEquals(Collections.singletonList("1001"), publishVO.getRecipientList());
        assertEquals("规则引擎", publishVO.getAuthor());
        assertEquals(NoticeRemindModeEnum.EARLY_WARNING.getCode(), publishVO.getRemindMode());
        assertFalse(publishVO.getDraft());
        assertFalse(publishVO.getAutoRead());

        ArgumentCaptor<RuleAlarmRecordSaveVO> recordCaptor = ArgumentCaptor.forClass(RuleAlarmRecordSaveVO.class);
        verify(service).saveAlarmRecord(recordCaptor.capture());
        RuleAlarmRecordSaveVO record = recordCaptor.getValue();
        assertEquals("app-1", record.getAppId());
        assertEquals("alarm-1", record.getAlarmIdentification());
        assertEquals("设备 device-1 已离线", record.getContentData());
        assertEquals(AlarmRecordHandledStatusEnum.PENDING.getValue(), record.getHandledStatus());
        assertEquals(101L, record.getCreatedBy());
        assertEquals(200L, record.getCreatedOrgId());
        verify(msgApi, never()).sendByTemplate(any());
    }

    @Test
    @DisplayName("站内信动作未单独配置员工接收人时，会使用渠道配置里的默认接收人")
    void triggerDeviceAlarmShouldApplySiteChannelConfig() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmChannelDetailsResultVO siteChannel = siteChannel();
        siteChannel.setChannelConfig("""
                {
                  "remindMode":"03",
                  "target":"03",
                  "autoRead":true,
                  "url":"/#/basic/msg/extendNotice",
                  "recipientList":["1001","1002","1001",""]
                }
                """);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(siteChannel);
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.SITE_MESSAGE,
                Collections.emptyList(),
                false);
        rendered.setUrl("");
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(actionConfig,
                AlarmChannelTypeEnum.SITE_MESSAGE.getValue(), alarmDetails)).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(isNull(), eq(alarmDetails)))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(eq(actionConfig), eq(template), eq(alarmDetails), anyMap()))
                .thenReturn(rendered);
        when(msgApi.publish(any(ExtendMsgPublishVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        ArgumentCaptor<ExtendMsgPublishVO> publishCaptor = ArgumentCaptor.forClass(ExtendMsgPublishVO.class);
        verify(msgApi).publish(publishCaptor.capture());
        ExtendMsgPublishVO publishVO = publishCaptor.getValue();
        assertEquals(NoticeRemindModeEnum.NOTICE.getCode(), publishVO.getRemindMode());
        assertEquals("03", publishVO.getTarget());
        assertTrue(publishVO.getAutoRead());
        assertEquals("/#/basic/msg/extendNotice", publishVO.getUrl());
        assertEquals(List.of("1001", "1002"), publishVO.getRecipientList());
    }

    @Test
    @DisplayName("机器人渠道带渠道配置和接收人发送模板消息，atAll 自动追加 all 接收人")
    void triggerDeviceAlarmShouldSendRobotNotificationWithAtAllRecipient() {
        stubSaveAlarmRecord();
        PolicyContext context = new PolicyContext();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.DING_TALK);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(dingTalkChannel());
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.DING_TALK);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.DING_TALK,
                List.of(recipient("PHONE", "13800001111, 13900001111")),
                true);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(actionConfig,
                AlarmChannelTypeEnum.DING_TALK.getValue(), alarmDetails)).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(context, alarmDetails))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(actionConfig, template, alarmDetails, Map.of("device.name", "测试设备")))
                .thenReturn(rendered);
        when(msgApi.sendByTemplate(any(ExtendMsgSendVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, context));

        ArgumentCaptor<ExtendMsgSendVO> sendCaptor = ArgumentCaptor.forClass(ExtendMsgSendVO.class);
        verify(msgApi).sendByTemplate(sendCaptor.capture());
        ExtendMsgSendVO sendVO = sendCaptor.getValue();
        assertEquals(AlarmChannelTypeEnum.DING_TALK.getChannelTemplateCode(), sendVO.getCode());
        assertEquals("设备离线告警", sendVO.getTitle());
        assertEquals("设备 device-1 已离线", sendVO.getContent());
        assertTrue(sendVO.getConfigList().stream()
                .anyMatch(kv -> "msgType".equals(kv.getKey()) && "MARKDOWN".equals(kv.getValue())));
        assertTrue(sendVO.getConfigList().stream()
                .anyMatch(kv -> "secret".equals(kv.getKey()) && "sec-1".equals(kv.getValue())));
        assertTrue(sendVO.getConfigList().stream()
                .anyMatch(kv -> "token".equals(kv.getKey()) && "token-1".equals(kv.getValue())));
        assertTrue(sendVO.getConfigList().stream()
                .anyMatch(kv -> "isAtAll".equals(kv.getKey())));
        assertEquals(List.of("13800001111", "13900001111", BizConstant.ALL),
                sendVO.getRecipientList().stream().map(ExtendMsgRecipientSaveVO::getRecipient).toList());
        verify(msgApi, never()).publish(any());
    }

    @Test
    @DisplayName("飞书渠道按当前底层策略固定为文本消息，避免 Markdown 内容被错误渲染")
    void triggerDeviceAlarmShouldSendFeishuAsTextMessage() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.FS);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(feishuChannel());
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.FS);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.FS,
                Collections.singletonList(recipient("PHONE", "13800001111")),
                false);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(actionConfig,
                AlarmChannelTypeEnum.FS.getValue(), alarmDetails)).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(isNull(), eq(alarmDetails)))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(eq(actionConfig), eq(template), eq(alarmDetails), anyMap()))
                .thenReturn(rendered);
        when(msgApi.sendByTemplate(any(ExtendMsgSendVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        ArgumentCaptor<ExtendMsgSendVO> sendCaptor = ArgumentCaptor.forClass(ExtendMsgSendVO.class);
        verify(msgApi).sendByTemplate(sendCaptor.capture());
        assertTrue(sendCaptor.getValue().getConfigList().stream()
                .anyMatch(kv -> "msgType".equals(kv.getKey()) && "TEXT".equals(kv.getValue())));
    }

    @Test
    @DisplayName("渠道模板只启用钉钉时，告警规则绑定的其它启用渠道不会被发送")
    void triggerDeviceAlarmShouldFilterByEnabledActionChannelTemplates() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.DING_TALK);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(dingTalkChannel(), siteChannel());
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.DING_TALK);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.DING_TALK,
                Collections.singletonList(recipient("PHONE", "13800001111")),
                false);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(eq(actionConfig),
                eq(AlarmChannelTypeEnum.DING_TALK.getValue()), eq(alarmDetails))).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(isNull(), eq(alarmDetails)))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(eq(actionConfig), eq(template), eq(alarmDetails), anyMap()))
                .thenReturn(rendered);
        when(msgApi.sendByTemplate(any(ExtendMsgSendVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        verify(ruleNotificationTemplateService, never()).resolveChannelTemplate(eq(actionConfig),
                eq(AlarmChannelTypeEnum.SITE_MESSAGE.getValue()), eq(alarmDetails));
        verify(msgApi).sendByTemplate(any(ExtendMsgSendVO.class));
        verify(msgApi, never()).publish(any());
    }

    @Test
    @DisplayName("渠道模板全部显式禁用时只落告警记录，不回退为默认渠道发送")
    void triggerDeviceAlarmShouldNotFallbackToDefaultChannelsWhenAllTemplatesDisabled() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .channelTemplates(List.of(
                        RuleAlarmChannelTemplateDTO.builder()
                                .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                                .enabled(false)
                                .build(),
                        RuleAlarmChannelTemplateDTO.builder()
                                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                                .enabled(false)
                                .build()))
                .build();
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(dingTalkChannel(), siteChannel());
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        verify(service).saveAlarmRecord(any(RuleAlarmRecordSaveVO.class));
        verify(ruleNotificationTemplateService, never()).resolveChannelTemplate(
                eq(actionConfig), anyInt(), eq(alarmDetails));
        verify(msgApi, never()).sendByTemplate(any());
        verify(msgApi, never()).publish(any());
    }

    @Test
    @DisplayName("单个渠道渲染或发送失败不会阻断其它渠道，也不会阻断告警记录落库")
    void triggerDeviceAlarmShouldContinueWhenOneChannelFails() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .build();
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(dingTalkChannel(), siteChannel());
        RuleAlarmChannelTemplateDTO siteTemplate = channelTemplate(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmRenderedNotificationDTO siteRendered = rendered(AlarmChannelTypeEnum.SITE_MESSAGE,
                Collections.singletonList(recipient("EMPLOYEE", "1001")),
                false);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(eq(actionConfig),
                eq(AlarmChannelTypeEnum.DING_TALK.getValue()), eq(alarmDetails)))
                .thenThrow(new IllegalStateException("template broken"));
        when(ruleNotificationTemplateService.resolveChannelTemplate(eq(actionConfig),
                eq(AlarmChannelTypeEnum.SITE_MESSAGE.getValue()), eq(alarmDetails))).thenReturn(siteTemplate);
        when(ruleNotificationTemplateService.buildRuntimeVariables(isNull(), eq(alarmDetails)))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(eq(actionConfig), eq(siteTemplate), eq(alarmDetails), anyMap()))
                .thenReturn(siteRendered);
        when(msgApi.publish(any(ExtendMsgPublishVO.class))).thenReturn(true);

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        verify(msgApi).publish(any(ExtendMsgPublishVO.class));
        ArgumentCaptor<RuleAlarmRecordSaveVO> recordCaptor = ArgumentCaptor.forClass(RuleAlarmRecordSaveVO.class);
        verify(service).saveAlarmRecord(recordCaptor.capture());
        assertEquals("设备 device-1 已离线", recordCaptor.getValue().getContentData());
    }

    @Test
    @DisplayName("告警记录会先于渠道通知落库，通知发送失败也能在列表中追溯到记录")
    void triggerDeviceAlarmShouldSaveRecordBeforeSendingNotification() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = actionConfig(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(siteChannel());
        RuleAlarmChannelTemplateDTO template = channelTemplate(AlarmChannelTypeEnum.SITE_MESSAGE);
        RuleAlarmRenderedNotificationDTO rendered = rendered(AlarmChannelTypeEnum.SITE_MESSAGE,
                Collections.singletonList(recipient("EMPLOYEE", "1001")),
                false);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);
        when(ruleNotificationTemplateService.resolveChannelTemplate(actionConfig,
                AlarmChannelTypeEnum.SITE_MESSAGE.getValue(), alarmDetails)).thenReturn(template);
        when(ruleNotificationTemplateService.buildRuntimeVariables(isNull(), eq(alarmDetails)))
                .thenReturn(Map.of("device.name", "测试设备"));
        when(ruleNotificationTemplateService.render(eq(actionConfig), eq(template), eq(alarmDetails), anyMap()))
                .thenReturn(rendered);
        when(msgApi.publish(any(ExtendMsgPublishVO.class))).thenThrow(new IllegalStateException("notice service unavailable"));

        assertTrue(service.triggerDeviceAlarm(actionConfig, null));

        InOrder inOrder = inOrder(service, msgApi);
        inOrder.verify(service).saveAlarmRecord(any(RuleAlarmRecordSaveVO.class));
        inOrder.verify(msgApi).publish(any(ExtendMsgPublishVO.class));
        ArgumentCaptor<RuleAlarmRecordSaveVO> recordCaptor = ArgumentCaptor.forClass(RuleAlarmRecordSaveVO.class);
        verify(service).saveAlarmRecord(recordCaptor.capture());
        assertEquals(101L, recordCaptor.getValue().getCreatedBy());
        assertEquals(200L, recordCaptor.getValue().getCreatedOrgId());
    }

    @Test
    @DisplayName("MQ 触发告警时，告警记录会从规则上下文补齐应用 ID，并沿用告警规则所属组织")
    void triggerDeviceAlarmShouldFallbackAppIdAndKeepAlarmRuleOrgForMqContext() {
        stubSaveAlarmRecord();
        RuleAlarmActionConfigDTO actionConfig = RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .contentData("设备触发告警")
                .build();
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails();
        alarmDetails.setAppId("");
        alarmDetails.setCreatedBy(101L);
        alarmDetails.setCreatedOrgId(200L);
        PolicyContext context = new PolicyContext();
        context.setRulePolicyDTO(RulePolicyDTO.builder()
                .appId("link-app")
                .createdOrgId(300L)
                .build());
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);

        assertTrue(service.triggerDeviceAlarm(actionConfig, context));

        ArgumentCaptor<RuleAlarmRecordSaveVO> recordCaptor = ArgumentCaptor.forClass(RuleAlarmRecordSaveVO.class);
        verify(service).saveAlarmRecord(recordCaptor.capture());
        RuleAlarmRecordSaveVO record = recordCaptor.getValue();
        assertEquals("link-app", record.getAppId());
        assertEquals(101L, record.getCreatedBy());
        assertEquals(200L, record.getCreatedOrgId());
        assertEquals("设备触发告警", record.getContentData());
    }

    @Test
    @DisplayName("异步线程没有当前部门时，保存告警记录不会把预置组织覆盖为空")
    void saveAlarmRecordShouldPreservePresetOrgWhenThreadContextMissing() {
        RuleAlarmRecordSaveVO saveVO = RuleAlarmRecordSaveVO.builder()
                .appId("app-1")
                .alarmIdentification("alarm-1")
                .occurredTime(java.time.LocalDateTime.now())
                .contentData("设备触发告警")
                .handledStatus(AlarmRecordHandledStatusEnum.PENDING.getValue())
                .createdBy(101L)
                .createdOrgId(200L)
                .build();

        service.saveAlarmRecord(saveVO);

        ArgumentCaptor<RuleAlarmRecord> recordCaptor = ArgumentCaptor.forClass(RuleAlarmRecord.class);
        verify(ruleAlarmRecordManager).save(recordCaptor.capture());
        assertEquals(101L, recordCaptor.getValue().getCreatedBy());
        assertEquals(200L, recordCaptor.getValue().getCreatedOrgId());
    }

    @Test
    @DisplayName("历史告警请求会转换成新版动作配置后复用同一条触发链路")
    void legacyDeviceAlarmRequestShouldReuseActionConfigPath() {
        RuleAlarmRecordServiceImpl legacyService = spy(new RuleAlarmRecordServiceImpl());
        doReturn(true).when(legacyService).triggerDeviceAlarm(any(RuleAlarmActionConfigDTO.class), isNull());
        DeviceAlarmNotificationRequestParam requestParam = new DeviceAlarmNotificationRequestParam();
        requestParam.setAlarmIdentification("alarm-old");
        requestParam.setAtPhone("13800001111");
        requestParam.setContentData("历史内容");

        assertTrue(legacyService.triggerDeviceAlarm(requestParam));

        ArgumentCaptor<RuleAlarmActionConfigDTO> configCaptor = ArgumentCaptor.forClass(RuleAlarmActionConfigDTO.class);
        verify(legacyService).triggerDeviceAlarm(configCaptor.capture(), isNull());
        RuleAlarmActionConfigDTO config = configCaptor.getValue();
        assertEquals(2, config.getVersion());
        assertEquals("alarm-old", config.getAlarmIdentification());
        assertEquals("13800001111", config.getAtPhone());
        assertEquals("历史内容", config.getContentData());
    }

    @Test
    @DisplayName("告警记录标记处理中时写入处理时间和处理备注")
    void handleAlarmRecordShouldMarkInProgress() {
        RuleAlarmRecord existingRecord = alarmRecord();
        when(ruleAlarmRecordManager.getById(1L)).thenReturn(existingRecord);
        when(ruleAlarmRecordManager.updateById(any(RuleAlarmRecord.class))).thenReturn(true);

        RuleAlarmRecordUpdateVO result = service.handleOrSolveAlarmRecord(RuleAlarmRecordHandleParamVO.builder()
                .id(1L)
                .handledStatus(AlarmRecordHandledStatusEnum.IN_PROGRESS.getValue())
                .handleNotes("正在排查")
                .build());

        assertEquals(AlarmRecordHandledStatusEnum.IN_PROGRESS.getValue(), result.getHandledStatus());
        assertEquals("正在排查", existingRecord.getHandlingNotes());
        assertNotNull(existingRecord.getHandledTime());
        verify(ruleAlarmRecordManager).updateById(existingRecord);
    }

    @Test
    @DisplayName("告警记录标记已解决时写入解决时间和解决备注")
    void handleAlarmRecordShouldMarkResolved() {
        RuleAlarmRecord existingRecord = alarmRecord();
        when(ruleAlarmRecordManager.getById(1L)).thenReturn(existingRecord);
        when(ruleAlarmRecordManager.updateById(any(RuleAlarmRecord.class))).thenReturn(true);

        RuleAlarmRecordUpdateVO result = service.handleOrSolveAlarmRecord(RuleAlarmRecordHandleParamVO.builder()
                .id(1L)
                .handledStatus(AlarmRecordHandledStatusEnum.RESOLVED.getValue())
                .handleNotes("设备已恢复")
                .build());

        assertEquals(AlarmRecordHandledStatusEnum.RESOLVED.getValue(), result.getHandledStatus());
        assertEquals("设备已恢复", existingRecord.getResolutionNotes());
        assertNotNull(existingRecord.getResolvedTime());
        verify(ruleAlarmRecordManager).updateById(existingRecord);
    }

    @Test
    @DisplayName("告警记录详情会合并告警规则详情，支撑详情页展示")
    void getAlarmRecordDetailsShouldAttachAlarmRuleDetails() {
        RuleAlarmRecord existingRecord = alarmRecord();
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(siteChannel());
        when(ruleAlarmRecordManager.getById(1L)).thenReturn(existingRecord);
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);

        RuleAlarmRecordDetailsResultVO result = service.getAlarmRecordDetails(1L);

        assertEquals("alarm-1", result.getAlarmIdentification());
        assertEquals("设备 device-1 已离线", result.getContentData());
        assertEquals(alarmDetails, result.getRuleAlarmDetailsResultVO());
    }

    @Test
    @DisplayName("分页告警记录会按告警编码合并规则与渠道详情，支撑卡片列表展示")
    void fillAlarmRuleDetailsShouldAttachAlarmRuleDetailsForListPage() {
        RuleAlarmDetailsResultVO alarmDetails = alarmDetails(siteChannel(), dingTalkChannel());
        RuleAlarmRecordResultVO first = new RuleAlarmRecordResultVO()
                .setAlarmIdentification("alarm-1");
        RuleAlarmRecordResultVO second = new RuleAlarmRecordResultVO()
                .setAlarmIdentification("alarm-1");
        when(ruleAlarmService.getRuleAlarmDetailsByAlarmIdentification("alarm-1")).thenReturn(alarmDetails);

        service.fillAlarmRuleDetails(List.of(first, second));

        assertEquals(alarmDetails, first.getRuleAlarmDetailsResultVO());
        assertEquals(alarmDetails, second.getRuleAlarmDetailsResultVO());
        assertEquals(2, first.getRuleAlarmDetailsResultVO()
                .getRuleAlarmChannelDetailsResultVOList()
                .size());
        verify(ruleAlarmService).getRuleAlarmDetailsByAlarmIdentification("alarm-1");
    }

    private void stubSaveAlarmRecord() {
        doAnswer(invocation -> invocation.getArgument(0))
                .when(service).saveAlarmRecord(any(RuleAlarmRecordSaveVO.class));
    }

    private RuleAlarmActionConfigDTO actionConfig(AlarmChannelTypeEnum channelType) {
        return RuleAlarmActionConfigDTO.builder()
                .version(2)
                .alarmIdentification("alarm-1")
                .channelTemplates(Collections.singletonList(RuleAlarmChannelTemplateDTO.builder()
                        .channelType(channelType.getValue())
                        .enabled(true)
                        .format(channelType == AlarmChannelTypeEnum.SITE_MESSAGE ? "NOTICE" : "MARKDOWN")
                        .build()))
                .build();
    }

    private RuleAlarmDetailsResultVO alarmDetails(RuleAlarmChannelDetailsResultVO... channels) {
        RuleAlarmDetailsResultVO result = RuleAlarmDetailsResultVO.builder()
                .appId("app-1")
                .alarmIdentification("alarm-1")
                .alarmName("设备离线告警")
                .ruleAlarmChannelDetailsResultVOList(List.of(channels))
                .build();
        result.setCreatedBy(101L);
        result.setCreatedOrgId(200L);
        return result;
    }

    private RuleAlarmChannelTemplateDTO channelTemplate(AlarmChannelTypeEnum channelType) {
        return RuleAlarmChannelTemplateDTO.builder()
                .channelType(channelType.getValue())
                .enabled(true)
                .format(channelType == AlarmChannelTypeEnum.SITE_MESSAGE ? "NOTICE" : "MARKDOWN")
                .titleTemplate("${alarm.name}")
                .contentTemplate("设备 ${device.identification} 已离线")
                .build();
    }

    private RuleAlarmRenderedNotificationDTO rendered(AlarmChannelTypeEnum channelType,
                                                      List<RuleAlarmRecipientDTO> recipients,
                                                      boolean atAll) {
        return RuleAlarmRenderedNotificationDTO.builder()
                .channelType(channelType.getValue())
                .channelName(channelType.getDesc())
                .format(channelType == AlarmChannelTypeEnum.SITE_MESSAGE ? "NOTICE" : "MARKDOWN")
                .title("设备离线告警")
                .content("设备 device-1 已离线")
                .url("/#/alarm/record")
                .recipients(recipients)
                .atAll(atAll)
                .build();
    }

    private RuleAlarmRecipientDTO recipient(String type, String value) {
        return RuleAlarmRecipientDTO.builder()
                .type(type)
                .value(value)
                .label(value)
                .build();
    }

    private RuleAlarmChannelDetailsResultVO siteChannel() {
        return RuleAlarmChannelDetailsResultVO.builder()
                .channelName("站内信")
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build();
    }

    private RuleAlarmChannelDetailsResultVO dingTalkChannel() {
        return RuleAlarmChannelDetailsResultVO.builder()
                .channelName("钉钉")
                .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                .channelConfig("{\"secret\":\"sec-1\",\"token\":\"token-1\"}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build();
    }

    private RuleAlarmChannelDetailsResultVO feishuChannel() {
        return RuleAlarmChannelDetailsResultVO.builder()
                .channelName("飞书")
                .channelType(AlarmChannelTypeEnum.FS.getValue())
                .channelConfig("{\"appSecret\":\"secret-1\",\"appId\":\"app-1\",\"token\":\"token-1\"}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build();
    }

    private RuleAlarmRecord alarmRecord() {
        RuleAlarmRecord record = RuleAlarmRecord.builder()
                .appId("app-1")
                .alarmIdentification("alarm-1")
                .contentData("设备 device-1 已离线")
                .handledStatus(AlarmRecordHandledStatusEnum.PENDING.getValue())
                .build();
        record.setId(1L);
        return record;
    }
}
