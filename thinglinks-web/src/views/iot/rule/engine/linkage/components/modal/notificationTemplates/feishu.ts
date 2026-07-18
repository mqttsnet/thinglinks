import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot';

export const FEISHU_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'feishu-core',
    name: '值班提醒',
    description: '适配当前飞书文本机器人，短行展示核心信息。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.FEISHU],
    format: 'TEXT',
    titleTemplate: '告警：${alarm.name}',
    contentTemplate: [
      '【${alarm.name}】',
      '设备：${device.name}（${device.identification}）',
      '事件：${trigger.actionType}',
      '时间：${trigger.time}',
      '规则：${rule.name}',
      '流水：${rule.executionId}',
      '执行日志：${link.executionLogUrl}',
    ].join('\n'),
    docUrl,
  },
  {
    id: 'feishu-brief',
    name: '短通知',
    description: '适合高频状态类通知，避免刷屏。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.FEISHU],
    format: 'TEXT',
    titleTemplate: '${device.name} ${trigger.actionType}',
    contentTemplate: [
      '${alarm.name} · ${trigger.actionType}',
      '设备：${device.name}',
      '标识：${device.identification}',
      '时间：${trigger.time}',
      '日志：${link.executionLogUrl}',
    ].join('\n'),
    docUrl,
  },
];
