export default {
  modalTitle: '製品バージョン公開',
  currentVersion: '現在のバージョン',
  hint: 'システムは現在の物モデルを基に不変スナップショットを生成し、選択した戦略に従ってデバイスデータを振り分けます。',

  // ステップタイトル
  stepStrategy: '公開戦略を選択',
  stepConfig: '戦略の設定',
  stepRemark: '公開メモ',

  // 戦略
  strategyLabel: {
    0: '全量公開',
    1: 'カナリア公開',
    2: 'シャドウ公開',
  },
  strategyDesc: {
    0: '一括で全デバイスに展開',
    1: 'デバイスホワイトリストで段階展開',
    2: '並行検証、本番に影響なし',
  },

  // FULL / SHADOW ヒント
  fullTipTitle: '全量切替',
  fullTipDesc: '新バージョンは本製品配下の全オンラインデバイスに直ちに反映されます。影響範囲をご確認ください。',
  shadowTipTitle: 'シャドウモード',
  shadowTipDesc:
    '新バージョンはシャドウ書き込みのみで、デバイスの実トラフィックは現バージョンを使用します。リリース前の検証に最適です。',

  // カナリア設定
  canaryConfig: 'カナリア設定',
  canarySourceGroup: 'グループから',
  canarySourceManual: 'デバイス識別子から',
  canarySourcePercent: '比率指定',
  canarySourceGroupTip:
    'デバイスグループを選択すると、そのグループ配下の全デバイスを自動的にカナリアホワイトリストにします。',
  canarySourceManualTip: 'デバイス識別子を貼り付けてください。改行またはカンマで区切ります。',
  canarySourcePercentTip:
    'デバイス全数の比率で一貫性ハッシュにより抽出します(同一デバイス+同一比率なら結果が安定し、段階的展開でも揺らぎません)。',

  // グループパネル
  groupSelectPlaceholder: 'デバイスグループを選択してください',
  groupDevicePreview: 'このグループ内の {count} 台のデバイスに展開されます',
  groupEmpty: 'このグループにはデバイスがありません。別のグループを選択してください',
  groupLoadFailed: '選択したグループのデバイス取得に失敗しました',

  // 手動パネル
  deviceWhitelist: 'デバイスホワイトリスト',
  deviceWhitelistTip: 'デバイス識別子は改行またはカンマで区切ります',

  // サマリ
  canarySummary: '{count} 台のデバイスを選択中',
  canaryPercentSummary: '製品配下のデバイスを {percent}% 一貫性ハッシュで抽出します',
  canaryPercentQuick: 'クイック',
  canaryEmpty: 'まだカナリアデバイスを選択していません',

  // メモ
  publishRemark: '公開メモ',
  publishRemarkPlaceholder: '任意。変更内容を記述します',

  // バリデーション / フィードバック
  noStrategy: '先に公開戦略を選択してください',
  noCanaryDevice: 'カナリア公開には少なくとも 1 台のデバイスが必要です',
  noCanaryPercent: 'カナリア比率は 1% ~ 99% の間で指定してください',
  success: '公開しました',
  failed: '公開に失敗しました',
};
