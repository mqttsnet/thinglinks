import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://open.dingtalk.com/document/orgapp/custom-bot-send-message-type';

export const DING_TALK_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'ding-talk-core',
    nameKey: 'iot.link.engine.linkage.notificationPresets.dingTalkCore.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.dingTalkCore.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.DING_TALK],
    format: 'MARKDOWN',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.dingTalkCore.titleTemplate',
    contentTemplateKey: 'iot.link.engine.linkage.notificationPresets.dingTalkCore.contentTemplate',
    docUrl,
  },
  {
    id: 'ding-talk-ops',
    nameKey: 'iot.link.engine.linkage.notificationPresets.dingTalkOps.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.dingTalkOps.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.DING_TALK],
    format: 'MARKDOWN',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.dingTalkOps.titleTemplate',
    contentTemplateKey: 'iot.link.engine.linkage.notificationPresets.dingTalkOps.contentTemplate',
    docUrl,
  },
];
