import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://developer.work.weixin.qq.com/document/path/91770';

export const ENTERPRISE_WECHAT_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'enterprise-wechat-core',
    name: '核心告警',
    description: '企微机器人 Markdown 摘要，适合日常告警群。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.ENTERPRISE_WECHAT],
    format: 'MARKDOWN',
    titleTemplate: '告警：${alarm.name}',
    contentTemplate: [
      '## ${alarm.name}',
      '',
      '> ${trigger.time}',
      '',
      '- 规则：${rule.name}',
      '- 设备：${device.name}（${device.identification}）',
      '- 产品：${product.name}',
      '- 事件：${trigger.actionType}',
      '',
      '[查看执行日志](${link.executionLogUrl})',
    ].join('\n'),
    docUrl,
  },
  {
    id: 'enterprise-wechat-maintenance',
    name: '维护工单',
    description: '把现场处理需要的信息放在前面。',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.ENTERPRISE_WECHAT],
    format: 'MARKDOWN',
    titleTemplate: '待处理：${device.name} ${trigger.actionType}',
    contentTemplate: [
      '### ${alarm.name}',
      '',
      '- 处理对象：${device.name}',
      '- 设备标识：${device.identification}',
      '- 产品标识：${product.identification}',
      '- 告警等级：${alarm.level}',
      '- 执行流水：${rule.executionId}',
      '',
      '> 触发时间：${trigger.time}',
    ].join('\n'),
    docUrl,
  },
];
