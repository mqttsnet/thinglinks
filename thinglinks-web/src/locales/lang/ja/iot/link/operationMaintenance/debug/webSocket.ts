export default {
  title: 'WebSocket デバッグ',
  subtitle:
    'デバイス接続エンドポイント(ゲートウェイ経由)で WS の上り下りをデバッグ ── デバイス報告のシミュレーション、プラットフォームからの命令受信',

  // 接続設定
  connectConfig: '接続設定',
  tenantId: 'テナント ID',
  clientId: 'クライアント ID(clientId)',
  username: 'ユーザー名',
  password: 'パスワード',
  wsUrl: 'WS アドレス(ゲートウェイ経由、手動変更可)',
  wsUrlHint:
    'デフォルトでデバイス接続エンドポイントに接続;上記項目を変更すると自動で再生成、直接編集も可',
  resetDefault: 'デフォルトに戻す',
  connect: '接続',
  disconnect: '切断',
  connected: '接続済み',
  disconnected: '未接続',
  modeManual: '手動入力',
  modeDevice: 'デバイス指定',
  device: 'デバイス選択',
  devicePh: 'デバイス名で検索…',
  deviceHint:
    '先に製品を選び、その配下のデバイスを選択。clientId / ユーザー名 / パスワード / テナントを自動入力(編集可)',

  // 送信
  sendFrame: 'メッセージ送信',
  frameDatas: 'データ報告(datas)',
  framePing: 'ハートビート(PING)',
  messageHint:
    '業務フレームは topic・payload フィールド、ハートビートは type が PING。デフォルトはデバイスデータ報告(変更可)',
  sendBtn: '送信',

  // メッセージウィンドウ
  receiveWindow: 'メッセージウィンドウ',
  clear: 'クリア',
  empty: 'メッセージなし',
  dirSent: '送信',
  dirRecv: '受信',
  dirSys: 'システム',
  sysConnecting: '接続中…',
  sysOpen: '接続が確立しました',
  sysClose: '接続が閉じました',
  sysError: '接続エラー',
  prettyJson: '整形',
  copy: 'コピー',

  // 旧キー(mqtt デバッグページで使用中、保持)
  send: '送信',
  connectionStatus: '接続ステータス',
  closeConnection: '閉じる接続',
  openConnection: '开启接続',
  server: 'サービス地址',
  serverPlaceholder: '需要发送到サービス器的内容',
  message: 'メッセージ',
  record: 'メッセージ记录',
  receivedMsg: '收到メッセージ',
  setting: '設定',
  mqttTitle: 'MQTTコマンド下发',
  payload: '// 二進制データは0xで始まる16進数入力、文字列データ入力元の文字列',
  pleaseEnter: '请输入',
};
