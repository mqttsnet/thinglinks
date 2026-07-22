import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot';

export const FEISHU_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'feishu-core',
    nameKey: 'iot.link.engine.linkage.notificationPresets.feishuCore.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.feishuCore.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.FEISHU],
    format: 'TEXT',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.feishuCore.titleTemplate',
    contentTemplateKey: 'iot.link.engine.linkage.notificationPresets.feishuCore.contentTemplate',
    docUrl,
  },
  {
    id: 'feishu-brief',
    nameKey: 'iot.link.engine.linkage.notificationPresets.feishuBrief.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.feishuBrief.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.FEISHU],
    format: 'TEXT',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.feishuBrief.titleTemplate',
    contentTemplateKey: 'iot.link.engine.linkage.notificationPresets.feishuBrief.contentTemplate',
    docUrl,
  },
];
