import type { RuleAlarmChannelTemplate } from '/@/api/iot/rule/engine/linkage/notification';
import { DING_TALK_NOTIFICATION_TEMPLATES } from './dingTalk';
import { ENTERPRISE_WECHAT_NOTIFICATION_TEMPLATES } from './enterpriseWechat';
import { FEISHU_NOTIFICATION_TEMPLATES } from './feishu';
import { SITE_MESSAGE_NOTIFICATION_TEMPLATES } from './siteMessage';
import { NOTIFICATION_CHANNEL_TYPE } from './types';
import { resolveNotificationTemplateMessages } from './notificationConfig.mjs';
import type { NotificationChannelType, NotificationTemplatePreset } from './types';

export { NOTIFICATION_CHANNEL_TYPE };
export type { NotificationChannelType, NotificationTemplatePreset };
export type NotificationTemplateMessageResolver = (key: string) => unknown;

export const NOTIFICATION_TEMPLATE_PRESETS: NotificationTemplatePreset[] = [
  ...DING_TALK_NOTIFICATION_TEMPLATES,
  ...ENTERPRISE_WECHAT_NOTIFICATION_TEMPLATES,
  ...FEISHU_NOTIFICATION_TEMPLATES,
  ...SITE_MESSAGE_NOTIFICATION_TEMPLATES,
];

const DEFAULT_TEMPLATE_ID_BY_CHANNEL: Record<NotificationChannelType, string> = {
  [NOTIFICATION_CHANNEL_TYPE.DING_TALK]: 'ding-talk-core',
  [NOTIFICATION_CHANNEL_TYPE.ENTERPRISE_WECHAT]: 'enterprise-wechat-core',
  [NOTIFICATION_CHANNEL_TYPE.FEISHU]: 'feishu-core',
  [NOTIFICATION_CHANNEL_TYPE.SITE_MESSAGE]: 'site-message-core',
};

export function getNotificationTemplatePresets(channelType: number) {
  return NOTIFICATION_TEMPLATE_PRESETS.filter((item) =>
    item.channelTypes.includes(channelType as NotificationChannelType),
  );
}

export function getNotificationTemplatePreset(templateId?: string) {
  if (!templateId) return undefined;
  return NOTIFICATION_TEMPLATE_PRESETS.find((item) => item.id === templateId);
}

export function getDefaultNotificationTemplatePreset(channelType: number) {
  const presetId = DEFAULT_TEMPLATE_ID_BY_CHANNEL[channelType as NotificationChannelType];
  return (
    getNotificationTemplatePreset(presetId) ||
    getNotificationTemplatePresets(channelType)[0] ||
    SITE_MESSAGE_NOTIFICATION_TEMPLATES[0]
  );
}

export function applyNotificationTemplatePreset(
  base: RuleAlarmChannelTemplate,
  preset: NotificationTemplatePreset,
  resolveMessage: NotificationTemplateMessageResolver,
): RuleAlarmChannelTemplate {
  const localizedMessages = resolveNotificationTemplateMessages(preset, resolveMessage);
  return {
    ...base,
    templateId: preset.id,
    format: preset.format,
    ...localizedMessages,
    urlTemplate: preset.urlTemplate || '',
    atAll: preset.atAll ?? base.atAll ?? false,
  };
}

export function createNotificationChannelTemplate(
  channelType: number,
  resolveMessage: NotificationTemplateMessageResolver,
  templateId?: string,
) {
  const preset =
    getNotificationTemplatePreset(templateId) || getDefaultNotificationTemplatePreset(channelType);
  return applyNotificationTemplatePreset(
    {
      channelType,
      enabled: false,
      atAll: false,
    },
    preset,
    resolveMessage,
  );
}
