import type { RuleAlarmChannelTemplate } from '/@/api/iot/rule/engine/linkage/notification';

export const NOTIFICATION_CHANNEL_TYPE = {
  DING_TALK: 0,
  ENTERPRISE_WECHAT: 1,
  FEISHU: 2,
  SITE_MESSAGE: 3,
} as const;

export type NotificationChannelType =
  (typeof NOTIFICATION_CHANNEL_TYPE)[keyof typeof NOTIFICATION_CHANNEL_TYPE];

export interface NotificationTemplatePreset {
  id: string;
  name: string;
  description: string;
  channelTypes: NotificationChannelType[];
  format: RuleAlarmChannelTemplate['format'];
  titleTemplate: string;
  contentTemplate: string;
  urlTemplate?: string;
  atAll?: boolean;
  docUrl?: string;
}
