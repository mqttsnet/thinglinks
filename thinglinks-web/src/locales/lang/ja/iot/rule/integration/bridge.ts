export default {
  table: { title: 'ブリッジルール一覧' },
  id: 'ID',
  appId: 'アプリケーションシーン',
  ruleName: 'ルール名',
  ruleCode: 'ルールコード',
  direction: '方向',
  dataSourceId: '関連データソース',
  matchConfigJson: 'マッチ条件',
  actionConfigJson: 'アクション設定',
  qos: '信頼性レベル',
  rateLimitQps: 'QPS制限',
  retryMaxTimes: '最大リトライ回数',
  retryBackoffMs: '初期バックオフ(ms)',
  timeoutMs: '送信タイムアウト(ms)',
  deadLetterDataSourceId: 'デッドレターソース',
  enable: '有効',
  priority: '優先度',
  extendParams: '拡張パラメータ',
  remark: '備考',
  createdTime: '作成時間',
  createdBy: '作成者',
  updatedTime: '更新時間',
  updatedBy: '更新者',
  switchView: 'ビュー切替',
  group: {
    basic: '基本情報',
    match: 'マッチ条件',
    action: 'アクション設定',
    fallback: 'フロー制御/リトライ上書き',
    extras: '高度な設定',
  },
  helpMessage: {
    ruleName: '一覧 / カードで表示されるルールの表示名',
    ruleCode: '外部システムから参照する業務コード。空欄なら自動生成',
    appId: 'マルチテナント環境でのルール所属アプリケーション分離単位',
    direction: '送信=プラットフォーム→外部 / 受信=外部→プラットフォーム',
    dataSourceId: '既存のデータソースを選択。下の「アクション設定」が選択した protocol に応じて動的変化',
    qos: 'ルール単位の QoS 上書き。NULL = データソースの default_qos を継承',
    rateLimitQps: 'ルール単位の QPS 制限上書き。NULL = 継承 / 0 = 無制限',
    retryMaxTimes: 'ルール単位の最大リトライ上書き。NULL = データソース default を継承',
    retryBackoffMs: 'ルール単位の初回バックオフ上書き。NULL = データソース default を継承',
    timeoutMs: 'ルール単位の送信タイムアウト上書き。NULL = データソース default を継承',
    priority: '値が小さいほど先にマッチング。同イベント命中時はこの順',
    remark: '任意。業務上の用途識別用',
    overrideTip: 'NULL のフィールドはデータソースの default を継承。「実際の有効値」列は実行時の値',
  },
  placeholder: {
    ruleName: '例：温度警報 → 業務AのKafka',
    ruleCode: '空欄なら自動生成',
    fallbackNullDefault: 'NULL = データソースの default を継承',
    remark: '任意。業務上の用途識別用',
  },
  card: {
    nameFallback: '名称未設定',
  },
  detail: {
    title: 'ブリッジルール詳細',
    metric: {
      direction: '方向',
      dataSource: 'データソース',
      dataSourceName: 'ソース名',
      priority: '優先度',
      enable: '状態',
    },
    tabs: {
      match: 'マッチ条件',
      action: 'アクション設定',
      override: 'フロー上書き',
      stats: '24h 統計',
      log: '最近のログ',
      meta: 'メタデータ',
    },
    runTest: 'テスト送信',
    refresh: '更新',
    edit: '編集',
    enableSuccess: '有効化しました',
    disableSuccess: '無効化しました',
    overrideHint: 'NULL = データソースの既定値を継承、非空で上書き',
    actualEffective: '実効値',
    statsEmpty: '24時間の統計がありません',
    stats: {
      title: '実行結果分布（時間別）',
      success: '成功',
      failed: '失敗',
      deadLetter: 'デッドレター',
      avgLatency: '平均所要時間',
      count: '回数',
    },
    logEmpty: 'ログがありません',
    viewAllLogs: 'すべてのログを見る',
    encryptedHint: 'アクション設定は暗号化保存。本番環境では機密フィールドが自動マスク',
  },
  action: {
    add: 'ルール追加',
    edit: 'ルール編集',
    delete: 'ルール削除',
    enable: '有効化',
    disable: '無効化',
    test: 'テスト送信',
    copy: 'コピー',
    detail: '詳細',
  },
  status: {
    enabled: '有効',
    disabled: '無効',
  },
  tips: {
    enableMustTestPass: '「テスト送信」が成功した後に有効化できます',
    enableSourceRequired: '関連データソースが無効です。先にデータソースを有効化してください',
    deleteEnabled: '有効中のルールは削除できません。先に無効化してください',
    actionConfigEncrypted:
      '注：アクション設定は暗号化保存され、リストAPIではマスクされます（インライントークン漏洩防止）',
    testSinkSuccess: '送信成功',
    testSinkFailed: '送信失敗',
  },
  testSink: {
    sampleLabel: 'サンプル envelope（JSON 形式、byte[] ボディとしてシンクに送信）',
    samplePlaceholder: 'JSON envelope を入力。tenantId / productIdentification / deviceIdentification / actionType / topic / payload / ts フィールドを含めてください',
    result: '送信結果',
    latency: '所要時間',
    unknownError: '不明なエラー',
  },
  // ============================== match config schema 段 ==============================
  matchConfig: {
    outbound: {
      productIdentifications: {
        label: 'プロダクト識別',
        placeholder: 'プロダクト識別子を入力し Enter で確定；all で全プロダクト',
        help: '少なくとも 1 つ；["all"] = 全プロダクト（"*" も可）；カンマまたは空白区切り',
      },
      actionTypes: {
        label: 'イベントタイプ',
        placeholder: '複数選択；all または未選択 = 全件マッチ',
        help: 'PUBLISH / CONNECT / CLOSE / DISCONNECT 等；最低 1 つまたは all',
      },
      topicPatterns: {
        label: 'MQTT トピックパターン',
        // ${} プレースホルダの字面値;関数式メッセージで vue-i18n の補間を回避
        help: () => '複数選択可;「トピック選択」ボタンで製品の基本 topic から選ぶか、+/# ワイルドカードや ${app_id}/${device_identification} 等のプレースホルダー含む独自テンプレートを記述可能。空 = topic 制約なし、全 topic マッチ。',
      },
      payloadKinds: {
        label: 'データ形態',
        placeholder: '複数選択；未選択 = デフォルトで生メッセージのみ消費',
        help: 'ルールが消費するデータ形態を制御。未選択 = デフォルトで生メッセージ(RAW)のみ消費；物モデルデータを橋渡しするには「物モデルデータ」(THING_MODEL)を選択。',
      },
      deviceIdentifications: {
        label: 'デバイス識別フィルタ',
        placeholder: '指定デバイス識別子；空欄 = 制限なし',
        help: 'オプション；指定したデバイスのみマッチ',
      },
      deviceTagsAny: {
        label: 'デバイスタグ（いずれか）',
        placeholder: 'タグのいずれか一致でマッチ',
        help: 'オプション；少なくとも 1 つのタグを持つデバイスのみ',
      },
      payloadFilterType: {
        label: 'コンテンツフィルタタイプ',
        help: '高度；payload 内容によるイベント絞り込み',
      },
      payloadFilterExpression: {
        label: 'コンテンツフィルタ式',
        placeholder: '$.temperature > 80',
        help: 'JSON_PATH / SpEL / Groovy 式',
      },
      timeWindowCronExpr: {
        label: '時間ウィンドウ (cron)',
        placeholder: '0 0 9-18 ? * MON-FRI',
        help: 'オプション；cron 条件を満たす時間帯のみマッチ',
      },
    },
    inbound: {
      subscriptionSourceIds: {
        label: '関連サブスクリプションソース',
        placeholder: 'このルールが消費するサブスクリプションソース（複数選択）',
        help: '複数選択；ルールはこれらのソースから取得したメッセージを消費',
      },
      messageFilterType: { label: 'メッセージフィルタタイプ' },
      messageFilterExpression: {
        label: 'フィルタ式',
        placeholder: "$.event_type == 'data_report'",
      },
    },
    payloadFilterOpts: {
      NONE: 'NONE（フィルタなし）',
      JSON_PATH: 'JSON_PATH',
      SPEL: 'SPEL',
      GROOVY: 'GROOVY',
    },
  },
  // ============================== action config schema 段 ==============================
  actionForm: {
    overrideSuffix: '（上書き）',
    dsDefaultPlaceholder: '空欄 = データソースデフォルトを継承',
    enums: {
      INSERT: 'INSERT',
      UPDATE: 'UPDATE',
      UPSERT: 'UPSERT',
      IGNORE: 'IGNORE',
      SYNC: 'SYNC（同期）',
      ASYNC: 'ASYNC（非同期）',
      LPUSH: 'LPUSH',
      RPUSH: 'RPUSH',
      XADD: 'XADD',
      PUBLISH: 'PUBLISH',
      SET: 'SET',
    },
    common: {
      payloadFormat: {
        label: 'ペイロード形式',
        opt_JSON: 'JSON',
        opt_STRING: 'STRING',
        opt_HEX: 'HEX',
        opt_RAW: 'RAW',
      },
      payloadTemplate: {
        label: 'ペイロードテンプレート',
        placeholder: '[event.payload]（デフォルトはそのまま透過）',
        help: 'プレースホルダーテンプレート；空欄 = envelope そのまま；例：[event.payload] / [tenantId] / [productId]',
      },
    },
    inbound: {
      targetHandler: {
        label: '処理方式',
        help: '受信メッセージの下流リンクを決定',
        opt_MQTT_FORWARD: 'MQTT_FORWARD（デバイス publish に偽装）',
        opt_RAW_INSERT: 'RAW_INSERT（DeviceAction に直接書き込み）',
        opt_RULE_TRIGGER: 'RULE_TRIGGER（シナリオ連携をトリガー）',
      },
      mqttForward: {
        targetProductIdentification: {
          label: 'ターゲットプロダクト識別',
          placeholder: 'productA',
          help: '偽装デバイスが属するプロダクト',
        },
        targetTopicTemplate: {
          label: 'ターゲット Topic テンプレート',
          placeholder: '$thing/up/property/[productId]/[msg.deviceId]',
          help: 'プレースホルダー：[productId] [msg.xxx]',
        },
        fieldMapping: {
          label: 'フィールドマッピング',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification","sourceField":"ts","targetField":"timestamp","transform":"TIMESTAMP_TO_LONG"]',
          help: 'JSON 配列：サードパーティフィールド → プラットフォームフィールド',
        },
      },
      rawInsert: {
        fieldMapping: {
          label: 'フィールドマッピング',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification"]',
          help: 'JSON 配列；DeviceAction テーブルへのマッピング',
        },
      },
      ruleTrigger: {
        triggerRuleId: {
          label: 'トリガールール ID',
          placeholder: 'シナリオ連携ルール ID',
          help: '受信メッセージをルールエンジンに渡す；ルール主キー ID',
        },
      },
    },
    proto: {
      kafka: {
        partitionKey: { label: 'パーティションキーテンプレート', help: 'デバイス ID で一貫性ハッシュパーティショニング' },
        headers: { label: 'カスタム Headers', help: 'JSON 形式' },
      },
      redis: {
        command: { label: '書き込みコマンド' },
        keyTemplate: { label: 'Key テンプレート' },
        ttlSeconds: { label: 'TTL 秒', placeholder: 'SET / XADD 用；0 = 期限なし' },
      },
      rocketmq: {
        tag: { label: 'Tag テンプレート' },
        hashKey: { label: '順序メッセージ hashKey', help: '同じ hashKey は同じキューへ；順序消費保証' },
      },
      rabbitmq: {
        routingKey: { label: 'Routing Key' },
        properties: { label: 'カスタム Properties', help: 'AMQP message properties JSON' },
      },
      mysql: {
        columnMapping: {
          label: 'フィールドマッピング',
          placeholder: '"device_id":"[deviceIdentification]","ts":"[timestamp]","payload":"[payload]"',
          help: 'JSON 形式；空欄 = データソースデフォルト',
        },
        onDuplicate: { label: '競合戦略' },
      },
      http: {
        headers: { label: 'カスタム Headers' },
        queryParams: { label: 'Query Params' },
        bodyWrap: { label: 'Body ラップ', help: 'false = payload を直接送信；true = data: payload でラップ' },
      },
      webhook: {
        headers: { label: 'カスタム Headers' },
      },
      mqtt: {
        topicTemplate: { label: 'Topic テンプレート' },
        qos: {
          label: 'QoS',
          opt_0: '0 - fire-forget',
          opt_1: '1 - at-least-once',
          opt_2: '2 - exactly-once',
        },
        retained: { label: '保持メッセージ' },
      },
      tdengine: {
        superTableOverride: { label: 'スーパーテーブル' },
        childTableTemplate: { label: '子テーブル名テンプレート' },
        tagsMapping: { label: 'TAGS マッピング' },
        columnMapping: { label: 'フィールドマッピング' },
      },
      clickhouse: {
        tableOverride: { label: 'ターゲットテーブル' },
        columnMapping: { label: 'フィールドマッピング' },
      },
      influxdb: {
        measurementOverride: { label: 'Measurement' },
        tagsMapping: { label: 'Tags マッピング' },
        fieldsMapping: { label: 'Fields マッピング' },
      },
      iotdb: {
        timeseriesTemplateOverride: { label: '時系列パステンプレート' },
        columnMapping: { label: 'フィールドマッピング' },
      },
      postgresql: {
        tableOverride: { label: 'ターゲットテーブル', placeholder: '空欄 = データソースデフォルト（schema.table）' },
        columnMapping: {
          label: 'フィールドマッピング',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: '競合戦略' },
      },
      mongodb: {
        collectionOverride: { label: 'Collection' },
        writeModeOverride: { label: '書き込みモード' },
      },
      pulsar: {
        topicOverride: { label: 'Topic' },
        messageKey: { label: 'メッセージ Key', help: 'Pulsar ルーティング / Key_Shared 購読に影響' },
        messageProperties: { label: 'Message Properties' },
        sendModeOverride: { label: '送信モード' },
      },
      dm: {
        tableOverride: {
          label: 'ターゲットテーブル',
          placeholder: '空欄 = データソースデフォルト（schema.table、例 IOT.iot_data）',
        },
        columnMapping: {
          label: 'フィールドマッピング',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onDuplicate: { label: '競合戦略' },
      },
      kingbase: {
        tableOverride: { label: 'ターゲットテーブル', placeholder: '空欄 = データソースデフォルト（schema.table）' },
        columnMapping: {
          label: 'フィールドマッピング',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: '競合戦略' },
      },
    },
  },
};
