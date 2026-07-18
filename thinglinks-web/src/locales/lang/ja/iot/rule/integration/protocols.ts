/**
 * ブリッジ / データソース - プロトコルモジュール i18n（日本語）。
 *
 * @author mqttsnet
 */
export default {
  common: {
    saslMechanism_PLAIN: 'PLAIN（平文）',
    saslMechanism_SCRAM_256: 'SCRAM-SHA-256（推奨）',
    saslMechanism_SCRAM_512: 'SCRAM-SHA-512',
  },

  kafka: {
    name: 'Kafka',
    bootstrapServers: {
      label: 'Broker アドレス',
      help: 'Kafka broker アドレスリスト、カンマ区切り。本番環境では 2 台以上推奨',
      placeholder: '127.0.0.1:9092 または host1:9092,host2:9092',
    },
    topic: {
      label: 'ターゲット Topic',
      help: 'データ配信先 topic 名。テンプレート変数 [tenantId] [productId] 対応',
      placeholder: 'iot-out-topic',
    },
    clientId: {
      label: 'Client ID',
      help: 'Kafka クライアント ID。空欄なら自動生成（推奨）',
      placeholder: 'thinglinks-bridge-[dsId]',
    },
    partitionStrategy: {
      label: 'パーティション戦略',
      opt_hash: 'hash（デバイス ID 一貫性ハッシュ、推奨）',
      opt_roundRobin: 'roundRobin（ラウンドロビン）',
      opt_sticky: 'sticky',
      opt_manual: 'manual',
    },
    useTls: {
      label: 'TLS 有効化',
      help: '有効化すると下の「認証情報」に SASL 設定が表示',
    },
    saslMechanism: { label: 'SASL メカニズム' },
    saslUsername: {
      label: 'SASL ユーザー名',
      help: 'Kafka クラスタ管理者から発行',
      placeholder: 'kafka_user',
    },
    saslPassword: {
      label: 'SASL パスワード',
      help: '⚠ 落盤時 AES 暗号化；一覧 API は平文を返さない',
    },
    presets: {
      local_label: 'ローカル開発環境',
      local_description: 'ローカル Kafka broker、SASL なし；開発デバッグ用',
      aliyun_sasl_label: 'Aliyun Kafka (SASL_SSL)',
      aliyun_sasl_description: 'Aliyun VPC Kafka インスタンス、SASL/PLAIN 認証',
    },
  },

  redis: {
    name: 'Redis',
    mode: {
      label: 'デプロイメントモード',
      help: 'Redis デプロイメントモード選択',
      opt_STANDALONE: 'STANDALONE（単一ノード）',
      opt_SENTINEL: 'SENTINEL',
      opt_CLUSTER: 'CLUSTER',
    },
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: 'ポート' },
    sentinels: {
      label: 'Sentinels',
      help: 'Sentinel ノードリスト、カンマ区切り',
      placeholder: 'host1:26379,host2:26379',
    },
    masterName: { label: 'Master 名', placeholder: 'mymaster' },
    clusterNodes: { label: 'クラスタノード', placeholder: 'host1:7000,host2:7000,host3:7000' },
    db: {
      label: 'DB インデックス',
      help: 'DB インデックス；CLUSTER モードは未対応',
    },
    command: {
      label: '書き込みコマンド',
      help: 'List は LPUSH/RPUSH、Stream は XADD、Pub/Sub は PUBLISH、KV は SET',
      opt_LPUSH: 'LPUSH（List 先頭）',
      opt_RPUSH: 'RPUSH（List 末尾）',
      opt_XADD: 'XADD（Stream）',
      opt_PUBLISH: 'PUBLISH（Pub/Sub）',
      opt_SET: 'SET（KV）',
    },
    keyTemplate: {
      label: 'Key テンプレート',
      help: 'Key テンプレート、[tenantId] [productId] [deviceId] プレースホルダ対応',
      placeholder: 'iot:[tenantId]:[productId]:[deviceId]',
    },
    ttlSeconds: {
      label: 'TTL（秒）',
      help: 'SET / XADD 時の TTL 秒数；0=永続',
    },
    password: {
      label: 'パスワード',
      help: 'Redis AUTH パスワード（任意）；落盤時 AES 暗号化',
    },
    presets: {
      local_standalone_label: 'ローカル STANDALONE',
      local_standalone_description: 'ローカル Redis 単一ノード + LPUSH 書き込み',
      aliyun_cluster_label: 'Aliyun Redis CLUSTER',
      aliyun_cluster_description: 'Aliyun Redis クラスタ + Stream(XADD) + 1d TTL',
    },
    validate: {
      sentinel_required: 'Redis SENTINEL モードでは sentinels と masterName が必須',
      cluster_required: 'Redis CLUSTER モードではクラスタノードリストが必須',
    },
  },

  rocketmq: {
    name: 'RocketMQ',
    nameServer: {
      label: 'NameServer',
      help: 'NameServer アドレスリスト、セミコロン区切り；Aliyun は endpoint URL',
      placeholder: 'rmq-namesrv-1:9876;rmq-namesrv-2:9876',
    },
    topic: {
      label: 'ターゲット Topic',
      placeholder: 'iot-out-topic',
      // これらの help は @ / | / % / ${} の字面値を含むため、関数式メッセージで vue-i18n のコンパイルを回避
      help: () => 'RocketMQ topic 文字仕様：英字 / 数字 / アンダースコア / ハイフンのみ（^[%|a-zA-Z0-9_-]+$）、長さ ≤ 255；. : @ などを含むと broker に拒否されます',
    },
    tag: {
      label: 'Tag',
      help: () => 'Tag フィルタ；プレースホルダ ${actionType} 対応（実行時にイベントタイプで置換）。Tag の文字仕様は topic と同じ',
      placeholder: '*',
    },
    producerGroup: {
      label: 'Producer Group',
      help: () => '空欄の場合は starter が identifier で自動生成。手入力時は RocketMQ 文字仕様 ^[%|a-zA-Z0-9_-]+$ かつ長さ ≤ 255 を満たす必要（. を含むドメイン形式は broker に拒否されます）',
      placeholder: 'PG_BRIDGE_BIZ01',
    },
    accessChannel: {
      label: 'デプロイメントモード',
      help: 'LOCAL は Apache OSS client；CLOUD は Aliyun ONS client + AK/SK 認証',
      opt_LOCAL: 'LOCAL（自社運用 Apache RocketMQ）',
      opt_CLOUD: 'CLOUD（Aliyun RocketMQ）',
    },
    namespace: {
      label: 'Instance ID',
      help: 'Aliyun RocketMQ instanceId',
      placeholder: 'MQ_INST_xxxxxx',
    },
    vipChannelEnabled: {
      label: 'VIPチャネル',
      on: 'オン',
      off: 'オフ',
      help: 'VIP 高速チャネル（fastListenPort = brokerPort − 2、デフォルト 10909）。RocketMQ 5.x は無効化を推奨：コンテナ/NAT 環境では fastListenPort が見落とされ、producer が 10909 の TCP ハンドシェイクで停止し send が全タイムアウトする。fastListenPort が確実に公開されている場合のみ有効化。',
    },
    accessKey: {
      label: 'AccessKey',
      help: 'Aliyun AccessKey ID（CLOUD モードのみ必須）',
      placeholder: 'LTAI_xxx',
    },
    secretKey: {
      label: 'SecretKey',
      help: 'Aliyun AccessKey Secret；落盤時 AES 暗号化',
    },
    presets: {
      apache_local_label: '自社運用 Apache RocketMQ',
      apache_local_description: 'ローカル / 内網 NameServer、ACL なし',
      aliyun_label: 'Aliyun RocketMQ',
      aliyun_description: 'Aliyun professional + instanceId + AK/SK 認証',
    },
    validate: {
      cloud_namespace_required: 'RocketMQ CLOUD モードでは Aliyun instanceId が必須',
    },
  },

  rabbitmq: {
    name: 'RabbitMQ',
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: 'ポート', help: 'AMQP ポート；TLS は通常 5671' },
    virtualHost: { label: 'Virtual Host', help: 'Virtual host（マルチ vhost デプロイ用）' },
    exchangeName: {
      label: 'Exchange',
      help: 'Exchange 名（バックエンド RabbitmqSink.exchangeName と整合）',
      placeholder: 'iot.events',
    },
    exchangeType: {
      label: 'Exchange タイプ',
      opt_direct: 'direct',
      opt_topic: 'topic',
      opt_fanout: 'fanout',
      opt_headers: 'headers',
    },
    routingKey: {
      label: 'Routing Key',
      help: 'Routing key テンプレート、プレースホルダ対応',
      placeholder: 'device.[productId].[actionType]',
    },
    useTls: { label: 'TLS 有効化', help: 'TLS 時はポートを 5671 に推奨' },
    username: { label: 'ユーザー名', placeholder: 'guest' },
    password: { label: 'パスワード', help: 'AMQP パスワード；落盤時 AES 暗号化' },
    presets: {
      local_topic_label: 'ローカル RabbitMQ topic exchange',
      local_topic_description: 'ローカル RabbitMQ + topic exchange + デバイス単位 routingKey',
      fanout_label: 'fanout ブロードキャスト',
      fanout_description: 'バインドされた全 queue が受信（routingKey 不要）',
    },
  },

  mysql: {
    name: 'MySQL',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'JDBC URL；useSSL/serverTimezone/characterEncoding パラメータ推奨',
      placeholder: 'jdbc:mysql://host:3306/db?useSSL=false&serverTimezone=UTC',
    },
    username: {
      label: 'ユーザー名',
      help: 'DB ユーザー名（MysqlConnectionDto.username と整合、connection セクション所属）',
      placeholder: 'iot_writer',
    },
    table: {
      label: 'ターゲットテーブル',
      help: 'ターゲットテーブル名；テンプレート [productId] 対応',
      placeholder: 'iot_data',
    },
    columnMapping: {
      label: 'カラムマッピング',
      help: 'JSON 形式：テーブルカラム: プレースホルダ；envelope 任意フィールド対応',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: '競合戦略',
      help: '競合処理',
      opt_INSERT: 'INSERT（失敗時例外）',
      opt_UPDATE: 'UPDATE（ON DUPLICATE KEY UPDATE）',
      opt_IGNORE: 'IGNORE（INSERT IGNORE）',
    },
    password: { label: 'パスワード', help: 'DB パスワード；落盤時 AES 暗号化' },
    presets: {
      local_label: 'ローカル MySQL',
      local_description: 'ローカル MySQL + INSERT モード',
      aliyun_rds_label: 'Aliyun RDS for MySQL',
      aliyun_rds_description: 'Aliyun RDS + UPDATE モード（(device_id, ts) 主キーによる）',
    },
  },

  http: {
    name: 'HTTP',
    url: {
      label: 'URL',
      help: 'ターゲット API URL；[tenantId] [productId] テンプレート対応',
      placeholder: 'https://api.business.com/iot/data',
    },
    method: { label: 'Method' },
    contentType: { label: 'Content-Type', placeholder: 'application/json' },
    bearerToken: { label: 'Bearer Token', help: 'Bearer 認証トークン（任意）；落盤時 AES 暗号化' },
    basicUsername: { label: 'Basic ユーザー名', help: 'Basic Auth ユーザー名（任意）' },
    basicPassword: { label: 'Basic パスワード', help: 'Basic Auth パスワード（任意）；暗号化' },
    presets: {
      simple_post_label: 'シンプル POST',
      simple_post_description: '認証なし POST application/json',
      bearer_auth_label: 'Bearer Token 認証',
      bearer_auth_description: 'OAuth2 / JWT Bearer 認証 + JSON body',
    },
  },

  webhook: {
    name: 'WebHook',
    url: { label: 'Webhook URL', help: 'Webhook 受信側 URL', placeholder: 'https://webhook.example.com/iot' },
    contentType: { label: 'Content-Type' },
    signMethod: {
      label: '署名アルゴリズム',
      help: '署名アルゴリズム；HMAC_SHA256 推奨',
      opt_HMAC_SHA1: 'HMAC_SHA1',
      opt_HMAC_SHA256: 'HMAC_SHA256（推奨）',
      opt_HMAC_SHA512: 'HMAC_SHA512',
    },
    signHeaderName: { label: '署名ヘッダー名', help: '署名ヘッダー名' },
    timestampHeaderName: { label: 'タイムスタンプヘッダー名', help: 'タイムスタンプヘッダー名（リプレイ防止）' },
    secretKey: {
      label: '署名鍵',
      help: '署名鍵；落盤時 AES 暗号化',
      placeholder: '32 バイト以上のランダム文字列',
    },
    presets: {
      hmac_sha256_label: 'HMAC-SHA256 + リプレイ防止',
      hmac_sha256_description: '推奨本番設定：SHA256 署名 + X-Timestamp リプレイ防止',
    },
  },

  mqtt: {
    name: 'MQTT',
    broker: {
      label: 'Broker',
      help: 'MQTT broker アドレス（tcp:// または ssl://）',
      placeholder: 'tcp://broker.example.com:1883 または ssl://broker.example.com:8883',
    },
    clientId: { label: 'Client ID', help: 'MQTT clientId；空欄なら自動生成', placeholder: 'thinglinks-bridge-[dsId]' },
    topicTemplate: {
      label: 'Topic テンプレート',
      help: 'ターゲット topic テンプレート、プレースホルダ対応',
      placeholder: 'out/[productId]/[deviceId]',
    },
    qos: {
      label: 'QoS',
      help: '0=fire-forget / 1=at-least-once（推奨）/ 2=exactly-once',
      opt_0: '0 - fire-forget',
      opt_1: '1 - at-least-once（推奨）',
      opt_2: '2 - exactly-once',
    },
    retained: { label: '保持メッセージ', help: 'MQTT retained フラグ' },
    username: { label: 'ユーザー名', help: 'MQTT 認証ユーザー名（任意）' },
    password: { label: 'パスワード', help: 'MQTT 認証パスワード；落盤時 AES 暗号化' },
    caCert: {
      label: 'TLS CA 証明書',
      help: 'PEM 形式 CA 証明書（ssl:// モードのみ）；落盤時 AES 暗号化',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    presets: {
      tcp_no_auth_label: 'TCP 認証なし',
      tcp_no_auth_description: '内網外部 broker、TLS / 認証なし',
      ssl_with_auth_label: 'SSL + ユーザー名/パスワード',
      ssl_with_auth_description: '推奨本番：SSL + ユーザー名/パスワード認証',
    },
  },

  tdengine: {
    name: 'TDengine',
    host: { label: 'Host', placeholder: 'tdengine.example.com' },
    port: { label: 'RESTful ポート', help: 'TDengine RESTful ポート（taosadapter デフォルト 6041）' },
    database: { label: 'データベース', placeholder: 'iot' },
    superTable: {
      label: 'スーパーテーブル（STable）',
      help: 'STable 名；ブリッジメッセージは自動管理サブテーブルへ INSERT',
      placeholder: 'device_data',
    },
    childTableTemplate: {
      label: 'サブテーブル名テンプレート',
      help: '任意；デバイス単位サブテーブル用',
      placeholder: 'd_[deviceIdentification]',
    },
    tagsMapping: {
      label: 'TAGS マッピング',
      help: 'TAGS マッピング JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    columnMapping: {
      label: 'カラムマッピング（タイムスタンプ含む）',
      help: 'カラムマッピング JSON、タイムスタンプ必須',
      placeholder: ' "ts": "[timestamp]", "value": "[payload]" ',
    },
    username: { label: 'ユーザー名' },
    password: { label: 'パスワード', help: 'TDengine デフォルト root/taosdata；落盤時 AES 暗号化' },
    presets: {
      local_default_label: 'ローカル TDengine',
      local_default_description: 'ローカル TDengine + デフォルト root/taosdata + iot DB + device_data STable',
    },
  },

  clickhouse: {
    name: 'ClickHouse',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'ClickHouse JDBC URL（HTTP ポート 8123、TCP ポート 9000）',
      placeholder: 'jdbc:clickhouse://host:8123/db',
    },
    database: { label: 'データベース', placeholder: 'iot' },
    table: { label: 'ターゲットテーブル', placeholder: 'device_data_wide' },
    columnMapping: {
      label: 'カラムマッピング',
      help: 'JSON 形式：カラム: プレースホルダ',
      placeholder: ' "ts": "[timestamp]", "device_id": "[deviceIdentification]", "payload": "[payload]" ',
    },
    useAsyncInsert: {
      label: '非同期 INSERT',
      help: 'ClickHouse 非同期 INSERT 機能を有効化',
    },
    username: { label: 'ユーザー名' },
    password: { label: 'パスワード', help: 'ClickHouse パスワード（任意）；落盤時 AES 暗号化' },
    presets: {
      local_label: 'ローカル ClickHouse',
      local_description: 'ローカル ClickHouse + default ユーザー + 非同期 INSERT',
    },
  },

  influxdb: {
    name: 'InfluxDB',
    apiVersion: {
      label: 'API バージョン',
      help: 'V2 は token 認証、V1 はユーザー名/パスワード',
      opt_V2: 'V2（推奨）',
      opt_V1: 'V1（互換）',
    },
    url: { label: 'URL', placeholder: 'http://influx.example.com:8086' },
    org: { label: 'Org', help: 'V2 必須；V1 任意', placeholder: 'iot-org' },
    bucket: { label: 'Bucket / Database', help: 'V2: bucket / V1: database', placeholder: 'device_metrics' },
    measurement: {
      label: 'Measurement',
      help: '書き込み measurement 名；[productId] テンプレート対応',
      placeholder: 'device_data',
    },
    tagsMapping: {
      label: 'Tags マッピング',
      help: 'Tags マッピング JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    fieldsMapping: { label: 'Fields マッピング', placeholder: ' "value": "[payload]" ' },
    token: { label: 'API Token', help: 'V2 API token；落盤時 AES 暗号化' },
    username: { label: 'V1 ユーザー名', help: 'V1 モード用' },
    password: { label: 'V1 パスワード', help: 'V1 モード用；暗号化' },
    presets: {
      v2_local_label: 'ローカル InfluxDB v2',
      v2_local_description: 'ローカル InfluxDB v2 + token 認証',
    },
    validate: {
      v1_username_required: 'InfluxDB V1 モードではユーザー名が必須',
      v2_org_required: 'InfluxDB V2 モードでは Org が必須',
    },
  },

  iotdb: {
    name: 'Apache IoTDB',
    host: { label: 'Host', placeholder: 'iotdb.example.com' },
    port: { label: 'ポート' },
    storageGroup: { label: 'ストレージグループ', help: 'IoTDB storage group、例：root.iot', placeholder: 'root.iot' },
    timeseriesTemplate: {
      label: '時系列パステンプレート',
      help: '時系列パステンプレート；デバイス単位の時系列を生成',
      placeholder: 'root.iot.[routingKey]',
    },
    columnMapping: { label: 'カラムマッピング', placeholder: ' "value": "[payload]" ' },
    username: { label: 'ユーザー名' },
    password: { label: 'パスワード', help: 'IoTDB デフォルト root/root；落盤時 AES 暗号化' },
    presets: {
      local_default_label: 'ローカル IoTDB',
      local_default_description: 'ローカル IoTDB + デフォルト root/root + root.iot ストレージグループ',
    },
  },

  postgresql: {
    name: 'PostgreSQL',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:postgresql://host:5432/db' },
    username: { label: 'ユーザー名', placeholder: 'postgres' },
    schemaName: { label: 'Schema', help: 'デフォルト public' },
    table: { label: 'ターゲットテーブル', placeholder: 'iot_data' },
    columnMapping: {
      label: 'カラムマッピング',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: '競合戦略',
      opt_INSERT: 'INSERT（失敗時例外）',
      opt_UPSERT: 'UPSERT（ON CONFLICT DO UPDATE）',
      opt_IGNORE: 'IGNORE（ON CONFLICT DO NOTHING）',
    },
    conflictKeys: {
      label: 'UPSERT 競合キー',
      help: 'UPSERT 時必須；カンマ区切りカラム名',
      placeholder: 'device_id,ts',
    },
    password: { label: 'パスワード', help: 'DB パスワード；落盤時 AES 暗号化' },
    presets: {
      local_label: 'ローカル PostgreSQL',
      local_description: 'ローカル PostgreSQL + public schema + INSERT',
      upsert_label: 'UPSERT モード',
      upsert_description: '(device_id, ts) 主キーによる UPSERT；リプレイ・重複メッセージで冪等',
    },
  },

  mongodb: {
    name: 'MongoDB',
    uri: {
      label: 'Connection URI',
      help: 'MongoDB 接続 URI；ユーザー名/パスワード含可（mongodb://user:pass＠host:port）',
      placeholder: 'mongodb://host:27017 または mongodb+srv://...',
    },
    database: { label: 'データベース', placeholder: 'iot' },
    collection: { label: 'Collection', help: 'コレクション名；[productId] テンプレート対応', placeholder: 'device_data' },
    writeMode: {
      label: '書き込みモード',
      opt_INSERT: 'INSERT（メッセージ毎に新ドキュメント）',
      opt_UPSERT: 'UPSERT（キーで更新または挿入）',
    },
    upsertKey: { label: 'UPSERT キー', help: 'UPSERT 時必須', placeholder: 'deviceId' },
    writeConcern: {
      label: 'Write Concern',
      opt_UNACKNOWLEDGED: 'UNACKNOWLEDGED（最速、ACK なし）',
      opt_ACKNOWLEDGED: 'ACKNOWLEDGED（推奨）',
      opt_MAJORITY: 'MAJORITY（過半数書き込み）',
    },
    username: { label: 'ユーザー名', help: 'URI に含まれる場合は任意' },
    password: { label: 'パスワード', help: '落盤時 AES 暗号化' },
    authDatabase: { label: '認証データベース' },
    presets: {
      local_label: 'ローカル MongoDB',
      local_description: 'ローカル MongoDB + iot DB + device_data collection',
      upsert_label: 'UPSERT（デバイス単位冪等）',
      upsert_description: 'deviceId による UPSERT；リプレイ時に重複しない',
    },
  },

  pulsar: {
    name: 'Apache Pulsar',
    serviceUrl: {
      label: 'Service URL',
      help: 'Pulsar broker service URL',
      placeholder: 'pulsar://host:6650 または pulsar+ssl://host:6651',
    },
    topic: {
      label: 'Topic',
      help: 'Pulsar 完全 topic パス；persistent/non-persistent 対応',
      placeholder: 'persistent://tenant/namespace/topic',
    },
    producerName: { label: 'Producer 名', help: '任意；空欄なら自動生成', placeholder: 'iot-bridge' },
    sendMode: {
      label: '送信モード',
      opt_ASYNC: 'ASYNC（非同期、高スループット）',
      opt_SYNC: 'SYNC（同期、強整合）',
    },
    compressionType: {
      label: '圧縮タイプ',
      opt_NONE: 'NONE',
      opt_LZ4: 'LZ4（推奨）',
      opt_ZLIB: 'ZLIB',
      opt_ZSTD: 'ZSTD（高圧縮率）',
      opt_SNAPPY: 'SNAPPY',
    },
    enableBatching: { label: 'Batching 有効化', help: '高スループット；単発レイテンシ若干増' },
    schemaType: {
      label: 'Schema タイプ',
      opt_BYTES: 'BYTES（生バイト）',
      opt_STRING: 'STRING',
      opt_JSON: 'JSON',
      opt_AVRO: 'AVRO',
    },
    authToken: { label: 'JWT Auth Token', help: 'Pulsar JWT 認証 token；落盤時 AES 暗号化' },
    tlsCert: {
      label: 'TLS クライアント証明書',
      help: 'TLS モード必須；PEM 形式；暗号化',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    tlsKey: {
      label: 'TLS クライアント秘密鍵',
      help: 'TLS モード必須；PEM 形式；暗号化',
      placeholder: '-----BEGIN PRIVATE KEY-----\n...',
    },
    presets: {
      local_no_auth_label: 'ローカル Pulsar 認証なし',
      local_no_auth_description: 'Pulsar standalone + LZ4 圧縮 + ASYNC 送信',
      jwt_prod_label: '本番 JWT 認証',
      jwt_prod_description: '本番 Pulsar クラスタ + JWT token + ZSTD 圧縮',
    },
  },

  // ============================== DM 達夢 ==============================
  dm: {
    name: '達夢 DM',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:dm://host:5236/DAMENG' },
    username: { label: 'ユーザー名', placeholder: 'SYSDBA' },
    schemaName: { label: 'Schema', help: 'デフォルトはユーザー名同名 schema、空欄可' },
    table: { label: 'ターゲットテーブル', placeholder: 'iot_data または IOT.iot_data' },
    columnMapping: {
      label: 'フィールドマッピング',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: '競合戦略',
      opt_INSERT: 'INSERT（競合時エラー）',
      opt_UPDATE: 'UPDATE（MERGE INTO）',
      opt_IGNORE: 'IGNORE（競合スキップ）',
    },
    password: { label: 'パスワード', help: 'DB パスワード；ストレージ AES 暗号化' },
    presets: {
      local_label: 'ローカル DM',
      local_description: 'ローカル DM + SYSDBA + INSERT モード',
    },
  },

  // ============================== KingBase 人大金倉 ==============================
  kingbase: {
    name: '人大金倉 KingBase',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:kingbase8://host:54321/dbname' },
    username: { label: 'ユーザー名', placeholder: 'system' },
    schemaName: { label: 'Schema', help: 'デフォルト public（PG 互換）' },
    table: { label: 'ターゲットテーブル', placeholder: 'iot_data' },
    columnMapping: {
      label: 'フィールドマッピング',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: '競合戦略',
      opt_INSERT: 'INSERT（競合時エラー）',
      opt_UPSERT: 'UPSERT（ON CONFLICT DO UPDATE）',
      opt_IGNORE: 'IGNORE（ON CONFLICT DO NOTHING）',
    },
    conflictKeys: {
      label: 'UPSERT 競合キー',
      help: 'onConflict=UPSERT 時必須；カンマ区切りカラム名',
      placeholder: 'device_id,ts',
    },
    password: { label: 'パスワード', help: 'DB パスワード；ストレージ AES 暗号化' },
    presets: {
      local_label: 'ローカル KingBase',
      local_description: 'ローカル KingBase + public schema + INSERT モード',
    },
  },
};
