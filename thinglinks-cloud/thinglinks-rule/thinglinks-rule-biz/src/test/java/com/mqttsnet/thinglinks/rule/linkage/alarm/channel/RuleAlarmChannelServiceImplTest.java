package com.mqttsnet.thinglinks.rule.linkage.alarm.channel;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.dto.alarm.channel.site.SiteMessageParamDTO;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarmChannel;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelStatusEnum;
import com.mqttsnet.thinglinks.enumeration.alarm.AlarmChannelTypeEnum;
import com.mqttsnet.thinglinks.manager.alarm.RuleAlarmChannelManager;
import com.mqttsnet.thinglinks.msg.enumeration.NoticeRemindModeEnum;
import com.mqttsnet.thinglinks.service.alarm.impl.RuleAlarmChannelServiceImpl;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmChannelSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmChannelUpdateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("告警渠道配置")
class RuleAlarmChannelServiceImplTest {

    @Mock
    private RuleAlarmChannelManager ruleAlarmChannelManager;

    private RuleAlarmChannelServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RuleAlarmChannelServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", ruleAlarmChannelManager);
    }

    @Test
    @DisplayName("新增站内信渠道时归一化提醒方式、打开方式、自动已读、默认跳转和员工接收人")
    void saveSiteMessageChannelShouldNormalizeConfig() {
        when(ruleAlarmChannelManager.save(any(RuleAlarmChannel.class))).thenReturn(true);

        RuleAlarmChannelSaveVO result = service.saveAlarmChannel(RuleAlarmChannelSaveVO.builder()
                .channelName("站内预警")
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .channelConfig("""
                        {
                          "remindMode":"03",
                          "target":"03",
                          "autoRead":true,
                          "url":"/#/basic/msg/extendNotice",
                          "recipientList":["1001"," 1002 ","1001",""]
                        }
                        """)
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build());

        ArgumentCaptor<RuleAlarmChannel> captor = ArgumentCaptor.forClass(RuleAlarmChannel.class);
        verify(ruleAlarmChannelManager).save(captor.capture());
        SiteMessageParamDTO config = JSON.parseObject(captor.getValue().getChannelConfig(), SiteMessageParamDTO.class);
        assertEquals("站内预警", result.getChannelName());
        assertEquals(NoticeRemindModeEnum.NOTICE.getCode(), config.getRemindMode());
        assertEquals("03", config.getTarget());
        assertTrue(config.getAutoRead());
        assertEquals("/#/basic/msg/extendNotice", config.getUrl());
        assertEquals(List.of("1001", "1002"), config.getRecipientList());
    }

    @Test
    @DisplayName("新增站内信渠道缺省配置时使用预警、页面打开、非自动已读")
    void saveSiteMessageChannelShouldFillDefaultConfig() {
        when(ruleAlarmChannelManager.save(any(RuleAlarmChannel.class))).thenReturn(true);

        service.saveAlarmChannel(RuleAlarmChannelSaveVO.builder()
                .channelName("站内默认")
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .channelConfig("{}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build());

        ArgumentCaptor<RuleAlarmChannel> captor = ArgumentCaptor.forClass(RuleAlarmChannel.class);
        verify(ruleAlarmChannelManager).save(captor.capture());
        SiteMessageParamDTO config = JSON.parseObject(captor.getValue().getChannelConfig(), SiteMessageParamDTO.class);
        assertEquals(NoticeRemindModeEnum.EARLY_WARNING.getCode(), config.getRemindMode());
        assertEquals("01", config.getTarget());
        assertEquals(Boolean.FALSE, config.getAutoRead());
        assertEquals(List.of(), config.getRecipientList());
    }

    @Test
    @DisplayName("机器人渠道缺少必要凭证时不允许保存，避免规则执行时才失败")
    void saveRobotChannelShouldRejectIncompleteConfig() {
        assertThrows(RuntimeException.class, () -> service.saveAlarmChannel(RuleAlarmChannelSaveVO.builder()
                .channelName("钉钉缺签名")
                .channelType(AlarmChannelTypeEnum.DING_TALK.getValue())
                .channelConfig("{\"token\":\"token-1\"}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build()));

        verify(ruleAlarmChannelManager, never()).save(any(RuleAlarmChannel.class));
    }

    @Test
    @DisplayName("更新站内信渠道时同样归一化渠道配置")
    void updateSiteMessageChannelShouldNormalizeConfig() {
        RuleAlarmChannel existing = RuleAlarmChannel.builder()
                .channelName("旧站内信")
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .channelConfig("{}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build();
        existing.setId(1L);
        when(ruleAlarmChannelManager.getById(1L)).thenReturn(existing);
        when(ruleAlarmChannelManager.updateById(any(RuleAlarmChannel.class))).thenReturn(true);

        service.updateAlarmChannel(RuleAlarmChannelUpdateVO.builder()
                .id(1L)
                .channelName("新站内信")
                .channelType(AlarmChannelTypeEnum.SITE_MESSAGE.getValue())
                .channelConfig("{\"remindMode\":\"01\",\"recipientList\":[\"2001\"]}")
                .status(AlarmChannelStatusEnum.ACTIVATED.getValue())
                .build());

        ArgumentCaptor<RuleAlarmChannel> captor = ArgumentCaptor.forClass(RuleAlarmChannel.class);
        verify(ruleAlarmChannelManager).updateById(captor.capture());
        SiteMessageParamDTO config = JSON.parseObject(captor.getValue().getChannelConfig(), SiteMessageParamDTO.class);
        assertEquals(NoticeRemindModeEnum.TO_DO.getCode(), config.getRemindMode());
        assertEquals("01", config.getTarget());
        assertEquals(Boolean.FALSE, config.getAutoRead());
        assertEquals(List.of("2001"), config.getRecipientList());
    }
}
