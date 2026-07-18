import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://open.dingtalk.com/document/orgapp/custom-bot-send-message-type';

export const DING_TALK_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'ding-talk-core',
    name: '核心告警',
    description: '适合值班群，先看设备、事件和时间。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.DING_TALK],
    format: 'MARKDOWN',
    titleTemplate: '告警：${alarm.name}',
    contentTemplate: [
      '### ${alarm.name}',
      '',
      '> ${trigger.time} · ${trigger.actionType}',
      '',
      '- 规则：${rule.name}',
      '- 产品：${product.name}',
      '- 设备：${device.name}（${device.identification}）',
      '- 执行流水：${rule.executionId}',
      '',
      '[查看执行日志](${link.executionLogUrl})',
    ].join('\n'),
    docUrl,
  },
  {
    id: 'ding-talk-ops',
    name: '排查摘要',
    description: '适合运维排查，保留关键标识和原始事件。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.DING_TALK],
    format: 'MARKDOWN',
    titleTemplate: '规则执行异常：${rule.name}',
    contentTemplate: [
      '#### ${alarm.name}',
      '',
      '- 告警标识：${alarm.identification}',
      '- 规则标识：${rule.identification}',
      '- 产品标识：${product.identification}',
      '- 设备标识：${device.identification}',
      '- 触发动作：${trigger.actionType}',
      '- 触发时间：${trigger.time}',
      '',
      '> 原始事件：${trigger.rawMessage}',
      '',
      '[打开规则](${link.ruleUrl})',
    ].join('\n'),
    docUrl,
  },
];
