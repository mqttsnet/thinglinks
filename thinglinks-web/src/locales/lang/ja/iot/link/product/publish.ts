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
    1: 'グループ / 指定デバイスで段階展開',
    2: 'OTA アップグレード後に自動切替',
  },

  // FULL / SHADOW ヒント
  fullTipTitle: '全量切替',
  fullTipDesc:
    '新バージョンは本製品配下の全オンラインデバイスに直ちに反映されます。影響範囲をご確認ください。',
  shadowTipTitle: 'シャドウ版:OTA アップグレードと併用',
  shadowTipDesc:
    'シャドウ公開は新バージョンの物モデルテーブルを事前作成するだけで、デバイスの現行バージョンは変更しません。' +
    'デバイスが OTA で対応ファームウェア / ソフトウェアにアップグレードすると、自動的にこのシャドウ版へ切り替わります。' +
    '先にモデルを用意し、ファームの段階展開に合わせて1台ずつ移行します。',
  // シャドウ版の使い方(公開で建表 → OTA 連携 → アップグレード後に自動反映)
  shadowFlowTitle: 'シャドウ版の使い方',
  shadowFlowStep1:
    '公開:シャドウ版を生成しデータテーブルを事前作成。どのデバイスも触らず、既存デバイスはこれまで通り動きます。',
  shadowFlowStep2:
    'OTA 連携:「OTA リソース」で、対応ファーム / ソフトのアップグレードパッケージの「対象製品バージョン」をこのシャドウ版に設定します。',
  shadowFlowStep3:
    '自動反映:デバイスが OTA でアップグレード成功(または対応ファーム / ソフトバージョンを報告)すると、自動的にこのシャドウ版へ切り替わり、データが新テーブルに保存されます。',

  // 戦略の実行結果(公開記録 / バージョン一覧のレポート)
  result: {
    fullSwitched: '台を全量切替',
    canaryHit: '台に再バインド · 目標 {target} 台',
    shadowPreBuilt: '個の超テーブルを事前作成 · 再バインドなし',
    ofTotalLabel: '公開時の全デバイスに対して',
    sourceGroup: '{n} 個のグループから',
    sourceManual: 'デバイスリストから',
    sourcePercent: '比率 {percent}%',
    expandGroups: '{n} 個のグループを展開',
  },

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
  groupMultiPreview: '{groups} 個のグループを選択、重複除外 {count} 台',
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

  // カナリアの影響範囲(公開前に影響を明確化;ヒット→新バージョン、残り + 期間中の新規→安定版)
  canaryImpactTitle: 'カナリアの影響範囲',
  canaryImpactHitDevices: 'ヒットした {count} 台が新バージョンへ',
  canaryImpactHitPercent: '約 {percent}% のデバイスが新バージョンへ',
  canaryImpactRest: '残りのデバイスは現在の安定版 {version} を維持',
  canaryImpactNewDevice:
    'カナリア期間中に作成・インポートされたデバイスは安定版に自動バインドされ、カナリア対象には入りません',

  // 手動ホワイトリストの解析フィードバック
  manualParsed: '{count} 台のデバイスを認識',
  manualDedupedSuffix: '、重複 {dup} 件を自動的に除去',

  // メモ
  publishRemark: '公開メモ',
  publishRemarkPlaceholder: '任意。変更内容を記述します',

  // 最大フォールバック再試行回数
  maxRetryLabel: '最大フォールバック再試行回数',
  maxRetryHint: '非同期実行が失敗した場合の自動再試行上限(既定 3、最大 10)',

  // バリデーション / フィードバック
  noStrategy: '先に公開戦略を選択してください',
  noCanaryDevice: 'カナリア公開には少なくとも 1 台のデバイスが必要です',
  noCanaryPercent: 'カナリア比率は 1% ~ 99% の間で指定してください',
  success: '公開しました',
  failed: '公開に失敗しました',
};
