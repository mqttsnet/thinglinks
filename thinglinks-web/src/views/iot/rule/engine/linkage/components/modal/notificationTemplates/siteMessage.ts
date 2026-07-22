import { NOTIFICATION_CHANNEL_TYPE } from './types';
import type { NotificationTemplatePreset } from './types';

export const SITE_MESSAGE_NOTIFICATION_TEMPLATES: NotificationTemplatePreset[] = [
  {
    id: 'site-message-core',
    nameKey: 'iot.link.engine.linkage.notificationPresets.siteMessageCore.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.siteMessageCore.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.SITE_MESSAGE],
    format: 'NOTICE',
    titleTemplateKey: 'iot.link.engine.linkage.notificationPresets.siteMessageCore.titleTemplate',
    contentTemplateKey:
      'iot.link.engine.linkage.notificationPresets.siteMessageCore.contentTemplate',
    urlTemplate: '${link.executionLogUrl}',
  },
  {
    id: 'site-message-compact',
    nameKey: 'iot.link.engine.linkage.notificationPresets.siteMessageCompact.name',
    descriptionKey: 'iot.link.engine.linkage.notificationPresets.siteMessageCompact.description',
    channelTypes: [NOTIFICATION_CHANNEL_TYPE.SITE_MESSAGE],
    format: 'NOTICE',
    titleTemplateKey:
      'iot.link.engine.linkage.notificationPresets.siteMessageCompact.titleTemplate',
    contentTemplateKey:
      'iot.link.engine.linkage.notificationPresets.siteMessageCompact.contentTemplate',
    urlTemplate: '${link.executionLogUrl}',
  },
];
