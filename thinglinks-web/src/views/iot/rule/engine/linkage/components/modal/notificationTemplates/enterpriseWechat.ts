import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

const docUrl = 'https://developer.work.weixin.qq.com/document/path/91770';

export const ENTERPRISE_WECHAT_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'enterprise-wechat-core',
    nameKey: 'iot.link.engine.linkage.notificationPresets.wecomCore.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.wecomCore.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.ENTERPRISE_WECHAT],
    format: 'MARKDOWN',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.wecomCore.titleTemplate',
    contentTemplateKey: 'iot.link.engine.linkage.notificationPresets.wecomCore.contentTemplate',
    docUrl,
  },
  {
    id: 'enterprise-wechat-maintenance',
    nameKey: 'iot.link.engine.linkage.notificationPresets.wecomMaintenance.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.wecomMaintenance.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.ENTERPRISE_WECHAT],
    format: 'MARKDOWN',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.wecomMaintenance.titleTemplate',
    contentTemplateKey:
      'iot.link.engine.linkage.notificationPresets.wecomMaintenance.contentTemplate',
    docUrl,
  },
];
