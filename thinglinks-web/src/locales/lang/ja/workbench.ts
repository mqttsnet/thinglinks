export default {
  pageTitle: 'ホーム',

  brand: {
    slogan: 'クラウド · 接続 · インテリジェント',
    tagline: 'ThingLinks IoT · マルチテナント SaaS プラットフォーム',
  },

  system: {
    operational: 'すべてのシステム正常稼働',
    degraded: '一部サービス低下',
    outage: 'サービス停止中',
  },

  greeting: {
    earlyMorning: 'おはよう早朝',
    morning: 'おはようございます',
    forenoon: 'おはようございます',
    noon: 'お昼です',
    afternoon: 'こんにちは',
    evening: 'こんばんは',
    night: '夜分遅く',
  },

  week: {
    sun: '日',
    mon: '月',
    tue: '火',
    wed: '水',
    thu: '木',
    fri: '金',
    sat: '土',
  },

  hero: {
    dashboardTitle: 'ワークベンチ',
    roleTitle: 'ロール',
    roleFallback: '一般ユーザー',
    orgTitle: '所属組織',
    orgFallback: '未割当',
    appsTitle: 'アプリ',
    appsSub: '全て利用可能',
    onlineTitle: '最終ログイン',
    ago: {
      just: 'たった今',
      min: '{n} 分前',
      hour: '{n} 時間前',
      day: '{n} 日前',
    },
  },

  apps: {
    title: 'アプリケーション',
    search: 'アプリを検索',
    empty: '利用可能なアプリがありません。管理者にお問い合わせください。',
    current: '現在',
    expireSoon: '期限間近',
    expired: '期限切れ',
    newTabTip: '新しいタブで開きます',
    typeSelfBuilt: '自社アプリ',
    typeThirdParty: 'サードパーティ',
  },

  shortcuts: {
    title: 'クイックアクセス',
    recent: '最近のアクセス',
    pinned: 'お気に入り',
    manage: '管理',
    empty: 'ショートカットがありません',
    add: '追加',
    remove: '削除',
    switchingApp: '「{app}」に切替中...',
    menuNotInCurrentApp:
      '「{name}」は現在のアプリ「{app}」に含まれていません。右上のアプリ切替えから対応アプリに切り替えてからアクセスしてください',
    permissionRevoked:
      '「{name}」は現在アクセスできません。メニューが廃止されたか、権限が変更された可能性があります',
    appNotAccessible:
      'アプリ「{app}」はアクセス権限外になりました。管理者により権限が調整された可能性があります',
    appExpired: 'アプリ「{app}」は有効期限切れです。管理者に延長を依頼してください',
    appCheckFailed: 'アプリ状態の確認に失敗しました。しばらくしてから再試行してください',
  },

  shortcut: {
    profile: 'プロフィール',
    tenant: 'テナントセンター',
    application: 'アプリ管理',
    videoDevice: 'ビデオデバイス',
  },

  messages: {
    title: 'メッセージ',
    empty: 'メッセージなし',
    viewAll: 'すべて表示',
    unreadCount: '未読 {count} 件',
  },

  announcement: {
    title: 'お知らせ',
    empty: 'お知らせなし',
    viewAll: 'すべて表示',
  },

  activity: {
    title: '最近のアクティビティ',
    loginTitle: 'ログイン履歴',
    operationTitle: '操作履歴',
    loginAction: 'ログイン',
    operationAction: '操作',
    empty: '記録がありません',
    viewAllLogins: 'ログイン履歴をすべて表示',
    viewAllOps: '操作履歴をすべて表示',
  },

  banner: {
    title: 'ThingLinks プラットフォーム',
    subtitle: '{count} 個のアプリが利用可能',
    cta: '始める',
  },

  platform: {
    title: 'プラットフォーム情報',
    version: {
      title: 'コンソールバージョン',
    },
    docs: {
      title: 'ドキュメント',
      subtitle: 'クイックスタートとベストプラクティス',
    },
    github: {
      title: 'GitHub コミュニティ',
      subtitle: 'ソース · Issue · ディスカッション',
    },
    support: {
      title: 'カスタマーサポート',
      subtitle: 'オンラインサポートとフィードバック',
    },
  },

  launcher: {
    invalidApp: 'アプリ情報が不完全です',
    openedInNewTab: '新しいタブで開きました',
    switchFailed: 'アプリの切替に失敗しました',
  },

  dropdown: {
    switchApp: 'アプリ切替',
    current: '現在のアプリ',
    searchPlaceholder: 'アプリ検索',
  },

  emptyTenant: {
    title: 'まだ企業に所属していません',
    registerTenant: '企業を登録',
    becomeEmployee: '従業員になる',
    contactAdmin: '所属企業の管理者に招待を依頼してください。',
  },
};
