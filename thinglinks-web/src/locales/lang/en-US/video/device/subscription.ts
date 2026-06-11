export default {
  table: { title: 'Alarm Notification Subscriptions' },
  pageTitle: 'Alarm Subscription',

  subscriptionName: 'Subscription Name',
  channelType: 'Channel Type',
  messageTemplate: 'Message Template',
  eventType: 'Event Type',
  eventTypes: 'Event Types',
  priorityFilter: 'Priority Filter',
  priorityFilterAll: 'All',
  receiverScope: 'Receiver Scope',
  atAll: '＠All',
  status: 'Status',
  createdTime: 'Created',

  event: {
    ALARM: 'Alarm',
    DEVICE_ONLINE: 'Device Online',
    DEVICE_OFFLINE: 'Device Offline',
    STREAM_CLOSE: 'Stream Closed',
  },

  scope: {
    SELF: 'Creator',
    ORG: 'Creator Org',
    CUSTOM: 'Custom',
  },

  yes: 'Yes',
  no: 'No',
  enabled: 'Enabled',
  disabled: 'Disabled',

  editTitleAdd: 'Add Subscription',
  editTitleEdit: 'Edit Subscription',

  messageTemplateCode: 'Template Code',
  messageTemplateCodeHelp: 'Referenced from base ExtendMsgTemplate.code',
  priorityFilterHelp: 'Comma-separated, e.g. 1,2,3,4 (empty = all)',
  recipientIds: 'Recipient IDs',
  recipientIdsHelp: 'User IDs, comma-separated (within your dept only)',
  atAllLabel: '＠All',
  jumpUrlTemplate: 'Jump URL Template',
  jumpUrlTemplateHelp: 'Supports variables: [sys.domain], [bizId] etc.',
  msgTemplate: 'Message Template',
  msgTemplatePlaceholder: 'Supports [var] placeholders, Markdown for DingTalk/WeChat',
  msgTemplateHelp: 'Leave empty to use base template default content',
  remark: 'Remark',

  rule: {
    subscriptionName: 'Please input subscription name',
    channelType: 'Please select channel type',
    templateCode: 'Please input template code',
    eventTypes: 'Please select event types',
    token: 'Please input Token',
    secret: 'Please input Secret',
    appId: 'Please input AppId',
    appSecret: 'Please input AppSecret',
  },
};
