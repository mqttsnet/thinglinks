export default {
  table: { title: 'アラーム通知購読一覧' },
  pageTitle: 'アラーム購読',

  subscriptionName: '購読名',
  channelType: 'チャネル種別',
  messageTemplate: 'メッセージテンプレート',
  eventType: 'イベント種別',
  eventTypes: 'イベント種別',
  priorityFilter: 'レベルフィルター',
  priorityFilterAll: 'すべて',
  receiverScope: '受信範囲',
  atAll: '＠全員',
  status: '状態',
  createdTime: '作成時間',

  event: {
    ALARM: 'アラーム',
    DEVICE_ONLINE: 'デバイスオンライン',
    DEVICE_OFFLINE: 'デバイスオフライン',
    STREAM_CLOSE: 'ストリーム切断',
  },

  scope: {
    SELF: '作成者',
    ORG: '作成者組織',
    CUSTOM: 'カスタム',
  },

  yes: 'はい',
  no: 'いいえ',
  enabled: '有効',
  disabled: '無効',

  editTitleAdd: '購読を追加',
  editTitleEdit: '購読を編集',

  messageTemplateCode: 'テンプレートコード',
  messageTemplateCodeHelp: 'base ExtendMsgTemplate.code を参照',
  priorityFilterHelp: 'カンマ区切り, 例: 1,2,3,4 (空=すべて)',
  recipientIds: '受信者ID',
  recipientIdsHelp: 'ユーザーID, カンマ区切り (所属部署内のみ)',
  atAllLabel: '＠全員',
  jumpUrlTemplate: 'ジャンプURLテンプレート',
  jumpUrlTemplateHelp: '変数サポート: [sys.domain], [bizId] など',
  msgTemplate: 'メッセージテンプレート',
  msgTemplatePlaceholder: '[変数] プレースホルダ対応、DingTalk/WeChatはMarkdown対応',
  msgTemplateHelp: '空の場合はベーステンプレートを使用',
  remark: '備考',

  rule: {
    subscriptionName: '購読名を入力してください',
    channelType: 'チャネル種別を選択してください',
    templateCode: 'テンプレートコードを入力してください',
    eventTypes: 'イベント種別を選択してください',
    token: 'Tokenを入力してください',
    secret: 'Secretを入力してください',
    appId: 'AppIdを入力してください',
    appSecret: 'AppSecretを入力してください',
  },
};
