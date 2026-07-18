import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

export const SITE_MESSAGE_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'site-message-core',
    name: '站内信',
    description: '系统内提醒，内容短、可跳执行日志。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.SITE_MESSAGE],
    format: 'NOTICE',
    titleTemplate: '告警：${alarm.name}',
    contentTemplate: [
      '${alarm.name}',
      '规则：${rule.name}',
      '设备：${device.name}（${device.identification}）',
      '事件：${trigger.actionType}',
      '时间：${trigger.time}',
      '执行流水：${rule.executionId}',
    ].join('\n'),
    urlTemplate: '${link.executionLogUrl}',
  },
  {
    id: 'site-message-compact',
    name: '站内短提醒',
    description: '列表页阅读更省空间。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.SITE_MESSAGE],
    format: 'NOTICE',
    titleTemplate: '${device.name} ${trigger.actionType}',
    contentTemplate: '${alarm.name}，${trigger.time} 触发，规则：${rule.name}',
    urlTemplate: '${link.executionLogUrl}',
  },
];
