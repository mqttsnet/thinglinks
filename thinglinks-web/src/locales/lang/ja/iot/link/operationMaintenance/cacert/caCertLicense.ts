export default {
  table: {
    title: 'CA 許可証 証明書リスト',
  },
  id: 'id',
  certName: '証明書名',
  serialNumber: 'シリアル番号',
  commonName: 'コモンネーム',
  organization: '組織名',
  organizationalUnit: '組織単位名',
  countryName: '国',
  provinceName: '都道府県',
  localityName: '市区町村',
  email: 'メールアドレス',
  licenseBase64: '証明書本体 (Base64)',
  businessLicenseFileid: '営業許可証ファイル ID',
  authorizationCertFileid: '認可証明書ファイル ID',
  algorithm: '署名アルゴリズム',
  param1: 'RSA 公開鍵 n または ECC Point x',
  param2: 'RSA 公開鍵 e または ECC Point y',
  extendParams: '拡張情報',
  notBefore: '発行日時',
  notAfter: '有効期限',
  revokeTime: '失効日時',
  expirationDate: '有効期間',
  state: 'ステータス',
  remark: '備考',
  createdTime: '作成日時',
  createdBy: '作成者',
  updatedTime: '更新日時',
  updatedBy: '更新者',
  createdOrgId: '作成者組織',
  detailTitle: 'CA 許可証 詳細',
  importTitle: '証明書インポート',
  caCertPem: 'CA 証明書 PEM',
  thumbprint: 'フィンガープリント (SHA-256)',
  validityRemaining: '残り {days} 日',
  expired: '失効済み',
  boundDeviceCount: '紐付けデバイス数',
  onlineDeviceCount: 'うちオンライン',
  switchView: '表示切替',
  contentTip: 'この CA 証明書の PEM 内容。クライアント側検証用にコピーできます',

  card: {
    nameFallback: '無名証明書',
  },

  status: {
    pending: '発行待ち',
    issued: '発行済み',
    revoked: '失効済み',
    other: '利用不可',
  },

  placeholder: {
    certName: '例: Aliyun IoT CA',
    caCertPem: 'CA 証明書 PEM を貼り付け、-----BEGIN CERTIFICATE----- から始まる内容',
    remark: '任意。用途や関連アプリなどのメモ',
  },

  action: {
    issue: '発行',
    revoke: '失効',
    downloadPack: 'クライアントパッケージダウンロード',
    testSsl: 'SSL テスト',
    viewImpact: '影響範囲を表示',
  },

  import: {
    tip: '標準 PEM 形式の CA ルート証明書を貼り付け。証明書名・シリアル番号・有効期間・署名アルゴリズム等は自動抽出されます',
    confirm: 'インポート',
    success: '証明書のインポートに成功しました',
    failed: '証明書のインポートに失敗しました',
  },

  edit: {
    title: '証明書情報の編集',
    tip: 'PEM 解析で得られる項目(シリアル番号・CN・アルゴリズム等)は読み取り専用。表示名と備考のみ変更可能',
    success: '保存に成功しました',
    failed: '保存に失敗しました',
  },

  detail: {
    refresh: '更新',
    loadFailed: '証明書詳細の読み込みに失敗しました',
  },

  impact: {
    title: '失効影響範囲',
    warning:
      'この操作は {count} 台のデバイス(うち {online} 台オンライン)に即座に影響し、SSL 認証が失敗します',
    confirm: '了解しました。失効を実行',
    boundDevices: '紐付けデバイス (上位 50 件)',
    deviceIdentification: 'デバイス ID',
    deviceName: 'デバイス名',
    online: '接続状態',
    connectStatusOnline: 'オンライン',
    connectStatusOffline: 'オフライン',
    revokeReason: '失効理由',
    revokeReasonPh: '監査追跡のため推奨',
    revokeSuccess: '証明書を失効しました',
    revokeFailed: '失効に失敗しました',
    loadFailed: '影響範囲データの読み込みに失敗しました',
    top50Tip: '紐付けデバイス計 {count} 台、上位 50 件のみ表示',
  },

  downloadPack: {
    tip: 'この CA を根として署名したクライアント証明書を ZIP パッケージとして発行・ダウンロードします',
    caInfo: '署名 CA',
    notAfter: 'クライアント証明書の有効期限',
    notAfterPh: '有効期限を選択(推奨 1 年)',
    confirm: '発行 & ダウンロード',
    success: 'クライアントパッケージのダウンロードを開始しました',
    failed: '発行またはダウンロードに失敗しました',
  },

  audit: {
    tabTitle: '操作タイムライン',
    type: 'アクション',
    detail: '詳細',
    operator: '実行者',
    time: '日時',
    empty: '監査ログはまだありません',
  },

  tabs: {
    basic: '基本情報',
    content: '証明書内容',
    devices: '紐付けデバイス',
    audit: '監査',
  },
};
