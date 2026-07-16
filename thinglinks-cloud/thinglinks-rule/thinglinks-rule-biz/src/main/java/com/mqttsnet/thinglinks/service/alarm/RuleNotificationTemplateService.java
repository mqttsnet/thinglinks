package com.mqttsnet.thinglinks.service.alarm;

import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmChannelTemplateDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRenderedNotificationDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleNotificationPreviewParamVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationPreviewResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleNotificationVariableGroupResultVO;

import java.util.List;
import java.util.Map;

/**
 * 规则告警通知模板服务。
 */
public interface RuleNotificationTemplateService {

    /**
     * 返回后端维护的模板变量清单。
     */
    List<RuleNotificationVariableGroupResultVO> listVariables();

    /**
     * 解析场景联动触发告警动作配置,兼容历史 actionContent。
     */
    RuleAlarmActionConfigDTO parseActionConfig(String actionContent);

    /**
     * 构建规则触发时可用于模板渲染的变量。
     */
    Map<String, Object> buildRuntimeVariables(PolicyContext policyContext,
                                              RuleAlarmDetailsResultVO alarmDetails);

    /**
     * 按渠道解析最终模板配置;未配置时返回默认模板。
     */
    RuleAlarmChannelTemplateDTO resolveChannelTemplate(RuleAlarmActionConfigDTO actionConfig,
                                                       Integer channelType,
                                                       RuleAlarmDetailsResultVO alarmDetails);

    /**
     * 渲染单个渠道通知。
     */
    RuleAlarmRenderedNotificationDTO render(RuleAlarmActionConfigDTO actionConfig,
                                            RuleAlarmChannelTemplateDTO template,
                                            RuleAlarmDetailsResultVO alarmDetails,
                                            Map<String, Object> variables);

    /**
     * 前端预览。
     */
    RuleNotificationPreviewResultVO preview(RuleNotificationPreviewParamVO param);
}
