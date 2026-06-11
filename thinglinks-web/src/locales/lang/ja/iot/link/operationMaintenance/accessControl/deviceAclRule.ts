export default {
  table: { title: 'ACL ルール' },
  id: 'ID',
  ruleName: 'ルール名称',
  deviceIdentification: 'デバイス識別',
  actionType: 'アクションタイプ',
  priority: '優先度',
  topicPattern: 'MQTT トピックパターン',
  ipWhitelist: 'IP ホワイトリスト',
  decision: '判断',
  enabled: '有効',
  remark: '備考',
  createdTime: '作成日時',
  createdBy: '作成者',
  updatedTime: '更新日時',
  updatedBy: '更新者',
  createdOrgId: '作成者組織',
  productIdentification: '製品識別',
  ruleLevel: 'ルールレベル',
  allow: '許可',
  deny: '拒否',

  /** カードビュー */
  card: {
    nameFallback: '名前なしルール',
  },

  /** ステータス */
  status: {
    enabled: '有効',
    disabled: '無効',
  },

  /** 詳細ページ */
  detail: {
    title: 'ACL ルール詳細',
    tabBasic: '基本情報',
    tabAudit: '監査情報',
    deviceLevelHint: '製品レベルルール、製品配下のすべてのデバイスに適用',
    priorityLevel: {
      high: '高優先度',
      normal: '通常優先度',
      low: '低優先度',
    },
  },

  /** 編集フォームセクション */
  section: {
    base: '基本情報',
    scope: '範囲マッチング',
    decision: '権限判断',
  },

  /** 操作ボタン */
  action: {
    pickTopic: 'トピックを選択',
    pickProduct: '製品を選択',
    pickDevice: 'デバイスを選択',
    copy: 'ルールをコピー',
  },

  /** ダイアログタイトル */
  dialog: {
    pickTopic: 'MQTT トピックを選択',
    pickProduct: '製品を選択',
    pickDevice: 'デバイスを選択',
  },

  /** プレースホルダー */
  placeholder: {
    ruleName: '例:工場温度センサー購読権限',
    priority: '0-1000、小さいほど優先度が高い',
    productIdentification: '製品を選択',
    deviceIdentification: 'ルールレベルがデバイスの場合は必須',
    topicPattern: '例:device/+/data または home/room/#',
    ipWhitelist: 'Enter で IP を追加、CIDR 対応 (例:192.168.1.0/24)',
    searchTopic: 'トピックを検索',
  },

  /** 提示 */
  tips: {
    pickProductFirst: 'トピックを選択する前に製品を選択してください',
    ipWhitelistFormat: '単一 IP / CIDR 対応;Enter またはカンマで区切り',
    noTopicData: 'この製品にトピックがありません、製品管理で設定してください',
    deviceCount: 'この製品配下に {n} 台のデバイス。リストから選択または入力で絞り込み',
  },

  helpMessage: {
    ruleName: [
      '説明:ACL ルールに識別しやすい名前を付ける',
      '例:工場温度センサー購読権限',
      '注意:ビジネス関連の命名を推奨、例 [エリア]+[デバイスタイプ]+[権限タイプ]',
    ],
    ruleLevel: [
      '説明:ルールの適用範囲を制御',
      '値:0-製品レベル:この製品配下のすべてのデバイスに適用(デバイス識別は空)',
      '1-デバイスレベル:特定デバイスのみ(デバイス識別必須)',
      '注意:製品レベルは汎用、デバイスレベルは特殊用途',
    ],
    productIdentification: [
      '説明:ルールが属する製品の一意識別',
      'フォーマット:製品管理モジュールの識別と一致',
      '例:"PROD_TEMP_2023"',
    ],
    deviceIdentification: [
      '説明:"デバイスレベル" ルールのみ必須、特定デバイスに適用',
      '製品配下の登録デバイスから選択、または入力で絞り込み(未登録 ID も可)',
      '製品レベルルールでは空欄のまま',
    ],
    priority: [
      '説明:複数ルール衝突時、値が小さいほど優先度が高い',
      '範囲:0-1000(デフォルト 500)',
      '推奨:',
      'システムデフォルトルール:1000',
      '重要ルール:0-100',
      '通常ルール:101-500',
    ],
    actionType: [
      '説明:ルールが適用される MQTT 操作タイプ',
      '値',
      '0-すべて:発行/購読/購読解除を含む',
      '1-発行:デバイスのメッセージ発行権限',
      '2-購読:デバイスのトピック購読権限',
      '3-購読解除:購読解除権限',
    ],
    topicPattern: [
      '説明:ワイルドカード対応の MQTT トピックフィルタリング',
      '例:',
      '完全一致:device/001/temperature',
      '単層一致:factory/+/status',
      '多層一致:home/room/#',
    ],
    ipWhitelist: [
      '説明:アクセス許可された IP リスト(CIDR 対応)',
      'フォーマット:',
      '複数 IP はカンマで区切る',
      'IP セグメント対応:192.168.1.0/24',
    ],
    decision: [
      '説明:ルールが許可か拒否かを定義',
      '値:',
      '0-拒否:マッチングリクエストをブロック',
      '1-許可:マッチングリクエストを許可',
      '注意:拒否ルールは許可ルールより優先度を高くすべき',
    ],
  },

  /** テスター入口 */
  testerEntry: {
    button: 'ルールテスト',
    tooltip: '実際の製品 / デバイスを選択し、データ送信をシミュレートして判定結果を確認',
  },
};
