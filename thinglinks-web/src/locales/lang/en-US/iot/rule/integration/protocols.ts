/**
 * Bridge / Data Source - Protocol module i18n (English).
 *
 * @author mqttsnet
 */
export default {
  common: {
    saslMechanism_PLAIN: 'PLAIN (clear text)',
    saslMechanism_SCRAM_256: 'SCRAM-SHA-256 (recommended)',
    saslMechanism_SCRAM_512: 'SCRAM-SHA-512',
  },

  kafka: {
    name: 'Kafka',
    bootstrapServers: {
      label: 'Broker Servers',
      help: 'Kafka broker address list, comma separated. At least 2 brokers in production.',
      placeholder: '127.0.0.1:9092 or host1:9092,host2:9092',
    },
    topic: {
      label: 'Target Topic',
      help: 'Target topic for data delivery. Supports template vars [tenantId] [productId]',
      placeholder: 'iot-out-topic',
    },
    clientId: {
      label: 'Client ID',
      help: 'Kafka client identifier. Leave empty for auto-generation (recommended)',
      placeholder: 'thinglinks-bridge-[dsId]',
    },
    partitionStrategy: {
      label: 'Partition Strategy',
      opt_hash: 'hash (consistent hash by device ID, recommended)',
      opt_roundRobin: 'roundRobin',
      opt_sticky: 'sticky',
      opt_manual: 'manual',
    },
    useTls: {
      label: 'Enable TLS',
      help: 'When enabled, SASL configs appear in "Credential" section',
    },
    saslMechanism: { label: 'SASL Mechanism' },
    saslUsername: {
      label: 'SASL Username',
      help: 'Issued by Kafka cluster admin',
      placeholder: 'kafka_user',
    },
    saslPassword: {
      label: 'SASL Password',
      help: '⚠ AES encrypted at rest; list API masks plaintext',
    },
    presets: {
      local_label: 'Local Dev',
      local_description: 'Local Kafka broker, no SASL; for development',
      aliyun_sasl_label: 'Aliyun Kafka (SASL_SSL)',
      aliyun_sasl_description: 'Aliyun VPC Kafka instance with SASL/PLAIN',
    },
  },

  redis: {
    name: 'Redis',
    mode: {
      label: 'Deployment Mode',
      help: 'Choose Redis deployment mode',
      opt_STANDALONE: 'STANDALONE',
      opt_SENTINEL: 'SENTINEL',
      opt_CLUSTER: 'CLUSTER',
    },
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: 'Port' },
    sentinels: {
      label: 'Sentinels',
      help: 'Sentinel node list, comma separated',
      placeholder: 'host1:26379,host2:26379',
    },
    masterName: { label: 'Master Name', placeholder: 'mymaster' },
    clusterNodes: { label: 'Cluster Nodes', placeholder: 'host1:7000,host2:7000,host3:7000' },
    db: {
      label: 'DB Index',
      help: 'DB index (not supported in CLUSTER mode)',
    },
    command: {
      label: 'Write Command',
      help: 'LPUSH/RPUSH for list, XADD for stream, PUBLISH for pubsub, SET for KV',
      opt_LPUSH: 'LPUSH (List head)',
      opt_RPUSH: 'RPUSH (List tail)',
      opt_XADD: 'XADD (Stream)',
      opt_PUBLISH: 'PUBLISH (Pub/Sub)',
      opt_SET: 'SET (KV)',
    },
    keyTemplate: {
      label: 'Key Template',
      help: 'Key template, supports [tenantId] [productId] [deviceId]',
      placeholder: 'iot:[tenantId]:[productId]:[deviceId]',
    },
    ttlSeconds: {
      label: 'TTL (sec)',
      help: 'TTL in seconds for SET / XADD; 0 = never expire',
    },
    password: {
      label: 'Password',
      help: 'Redis AUTH password (optional); AES encrypted',
    },
    presets: {
      local_standalone_label: 'Local STANDALONE',
      local_standalone_description: 'Local Redis standalone + LPUSH',
      aliyun_cluster_label: 'Aliyun Redis CLUSTER',
      aliyun_cluster_description: 'Aliyun Redis cluster + Stream(XADD) + 1d TTL',
    },
    validate: {
      sentinel_required: 'Redis SENTINEL mode requires sentinels and masterName',
      cluster_required: 'Redis CLUSTER mode requires cluster nodes',
    },
  },

  rocketmq: {
    name: 'RocketMQ',
    nameServer: {
      label: 'NameServer',
      help: 'NameServer addresses semicolon separated; Aliyun uses endpoint URL',
      placeholder: 'rmq-namesrv-1:9876;rmq-namesrv-2:9876',
    },
    topic: {
      label: 'Target Topic',
      placeholder: 'iot-out-topic',
      // help texts here contain literal @ / | / % / ${} — message functions avoid vue-i18n compilation
      help: () => 'RocketMQ topic charset: only letters / digits / underscore / hyphen (^[%|a-zA-Z0-9_-]+$), length ≤ 255; chars like . : @ will be rejected by broker',
    },
    tag: {
      label: 'Tag',
      help: () => 'Tag filter; supports placeholder ${actionType} (replaced at runtime by event type). Tag charset is same as topic',
      placeholder: '*',
    },
    producerGroup: {
      label: 'Producer Group',
      help: () => 'Leave empty: starter auto-generates by identifier. If filled by user, must conform to RocketMQ charset ^[%|a-zA-Z0-9_-]+$ and length ≤ 255 (domain-like values containing . will be rejected by broker)',
      placeholder: 'PG_BRIDGE_BIZ01',
    },
    accessChannel: {
      label: 'Deployment Mode',
      help: 'LOCAL = Apache OSS client; CLOUD = Aliyun ONS client + AK/SK',
      opt_LOCAL: 'LOCAL (Apache RocketMQ)',
      opt_CLOUD: 'CLOUD (Aliyun RocketMQ)',
    },
    namespace: {
      label: 'Instance ID',
      help: 'Aliyun RocketMQ instanceId',
      placeholder: 'MQ_INST_xxxxxx',
    },
    vipChannelEnabled: {
      label: 'VIP Channel',
      on: 'On',
      off: 'Off',
      help: 'VIP fast channel (fastListenPort = brokerPort − 2, default 10909). RocketMQ 5.x officially recommends disabling: in container/NAT environments fastListenPort is often missed, causing producer to stall on 10909 SYN → all sends timeout. Only enable if you have confirmed fastListenPort is exposed.',
    },
    accessKey: {
      label: 'AccessKey',
      help: 'Aliyun AccessKey ID (required for CLOUD)',
      placeholder: 'LTAI_xxx',
    },
    secretKey: {
      label: 'SecretKey',
      help: 'Aliyun AccessKey Secret; AES encrypted',
    },
    presets: {
      apache_local_label: 'Apache RocketMQ (self-hosted)',
      apache_local_description: 'Local / VPC NameServer, no ACL',
      aliyun_label: 'Aliyun RocketMQ',
      aliyun_description: 'Aliyun professional + instanceId + AK/SK auth',
    },
    validate: {
      cloud_namespace_required: 'RocketMQ CLOUD mode requires Aliyun instanceId',
    },
  },

  rabbitmq: {
    name: 'RabbitMQ',
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: 'Port', help: 'AMQP port; usually 5671 for TLS' },
    virtualHost: { label: 'Virtual Host', help: 'Virtual host (multi-vhost deployments)' },
    exchangeName: {
      label: 'Exchange',
      help: 'Exchange name (aligned with backend RabbitmqSink.exchangeName)',
      placeholder: 'iot.events',
    },
    exchangeType: {
      label: 'Exchange Type',
      opt_direct: 'direct',
      opt_topic: 'topic',
      opt_fanout: 'fanout',
      opt_headers: 'headers',
    },
    routingKey: {
      label: 'Routing Key',
      help: 'Routing key template, supports placeholders',
      placeholder: 'device.[productId].[actionType]',
    },
    useTls: { label: 'Enable TLS', help: 'Recommend port 5671 with TLS' },
    username: { label: 'Username', placeholder: 'guest' },
    password: { label: 'Password', help: 'AMQP password; AES encrypted' },
    presets: {
      local_topic_label: 'Local RabbitMQ topic exchange',
      local_topic_description: 'Local RabbitMQ + topic exchange + per-device routingKey',
      fanout_label: 'fanout broadcast',
      fanout_description: 'All bound queues receive (routingKey unused)',
    },
  },

  mysql: {
    name: 'MySQL',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'JDBC URL; recommend useSSL/serverTimezone/characterEncoding params',
      placeholder: 'jdbc:mysql://host:3306/db?useSSL=false&serverTimezone=UTC',
    },
    username: {
      label: 'Username',
      help: 'DB username (aligned with MysqlConnectionDto.username, lives in connection section)',
      placeholder: 'iot_writer',
    },
    table: {
      label: 'Target Table',
      help: 'Target table; supports template [productId]',
      placeholder: 'iot_data',
    },
    columnMapping: {
      label: 'Column Mapping',
      help: 'JSON: table_column: placeholder; placeholders accept any envelope field',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: 'Conflict Strategy',
      help: 'Conflict handling',
      opt_INSERT: 'INSERT (throw on conflict)',
      opt_UPDATE: 'UPDATE (ON DUPLICATE KEY UPDATE)',
      opt_IGNORE: 'IGNORE (INSERT IGNORE)',
    },
    password: { label: 'Password', help: 'DB password; AES encrypted' },
    presets: {
      local_label: 'Local MySQL',
      local_description: 'Local MySQL + INSERT mode',
      aliyun_rds_label: 'Aliyun RDS for MySQL',
      aliyun_rds_description: 'Aliyun RDS + UPDATE (by (device_id, ts) primary key)',
    },
  },

  http: {
    name: 'HTTP',
    url: {
      label: 'URL',
      help: 'Target API URL; supports [tenantId] [productId]',
      placeholder: 'https://api.business.com/iot/data',
    },
    method: { label: 'Method' },
    contentType: { label: 'Content-Type', placeholder: 'application/json' },
    bearerToken: { label: 'Bearer Token', help: 'Bearer auth token (optional); AES encrypted' },
    basicUsername: { label: 'Basic Username', help: 'Basic Auth username (optional)' },
    basicPassword: { label: 'Basic Password', help: 'Basic Auth password (optional); encrypted' },
    presets: {
      simple_post_label: 'Simple POST',
      simple_post_description: 'No auth POST application/json',
      bearer_auth_label: 'Bearer Token Auth',
      bearer_auth_description: 'OAuth2 / JWT Bearer + JSON body',
    },
  },

  webhook: {
    name: 'WebHook',
    url: { label: 'Webhook URL', help: 'Webhook receiver URL', placeholder: 'https://webhook.example.com/iot' },
    contentType: { label: 'Content-Type' },
    signMethod: {
      label: 'Sign Method',
      help: 'HMAC algorithm; HMAC_SHA256 recommended',
      opt_HMAC_SHA1: 'HMAC_SHA1',
      opt_HMAC_SHA256: 'HMAC_SHA256 (recommended)',
      opt_HMAC_SHA512: 'HMAC_SHA512',
    },
    signHeaderName: { label: 'Sign Header', help: 'Signature header name' },
    timestampHeaderName: { label: 'Timestamp Header', help: 'Timestamp header name (anti-replay)' },
    secretKey: {
      label: 'Sign Secret',
      help: 'HMAC sign secret; AES encrypted',
      placeholder: 'At least 32-byte random string',
    },
    presets: {
      hmac_sha256_label: 'HMAC-SHA256 + Anti-Replay',
      hmac_sha256_description: 'Recommended production: SHA256 + X-Timestamp anti-replay',
    },
  },

  mqtt: {
    name: 'MQTT',
    broker: {
      label: 'Broker',
      help: 'MQTT broker URL (tcp:// or ssl://)',
      placeholder: 'tcp://broker.example.com:1883 or ssl://broker.example.com:8883',
    },
    clientId: { label: 'Client ID', help: 'MQTT clientId; auto-generated if empty', placeholder: 'thinglinks-bridge-[dsId]' },
    topicTemplate: {
      label: 'Topic Template',
      help: 'Target topic template, supports placeholders',
      placeholder: 'out/[productId]/[deviceId]',
    },
    qos: {
      label: 'QoS',
      help: '0=fire-forget / 1=at-least-once (recommended) / 2=exactly-once',
      opt_0: '0 - fire-forget',
      opt_1: '1 - at-least-once (recommended)',
      opt_2: '2 - exactly-once',
    },
    retained: { label: 'Retained', help: 'MQTT retained flag' },
    username: { label: 'Username', help: 'MQTT auth username (optional)' },
    password: { label: 'Password', help: 'MQTT auth password; AES encrypted' },
    caCert: {
      label: 'TLS CA Cert',
      help: 'PEM CA cert (only for ssl://); AES encrypted',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    presets: {
      tcp_no_auth_label: 'TCP no auth',
      tcp_no_auth_description: 'VPC external broker, no TLS / no auth',
      ssl_with_auth_label: 'SSL + username/password',
      ssl_with_auth_description: 'Recommended production: SSL + username/password',
    },
  },

  tdengine: {
    name: 'TDengine',
    host: { label: 'Host', placeholder: 'tdengine.example.com' },
    port: { label: 'RESTful Port', help: 'TDengine RESTful port (taosadapter default 6041)' },
    database: { label: 'Database', placeholder: 'iot' },
    superTable: {
      label: 'Super Table (STable)',
      help: 'STable name; bridge messages INSERT into auto-managed sub-tables',
      placeholder: 'device_data',
    },
    childTableTemplate: {
      label: 'Child Table Template',
      help: 'Optional; for per-device sub-tables',
      placeholder: 'd_[deviceIdentification]',
    },
    tagsMapping: {
      label: 'Tags Mapping',
      help: 'TAGS mapping JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    columnMapping: {
      label: 'Column Mapping (incl. timestamp)',
      help: 'Column mapping JSON; timestamp field required',
      placeholder: ' "ts": "[timestamp]", "value": "[payload]" ',
    },
    username: { label: 'Username' },
    password: { label: 'Password', help: 'TDengine default root/taosdata; AES encrypted' },
    presets: {
      local_default_label: 'Local TDengine',
      local_default_description: 'Local TDengine + default root/taosdata + iot db + device_data STable',
    },
  },

  clickhouse: {
    name: 'ClickHouse',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'ClickHouse JDBC URL (HTTP port 8123, TCP port 9000)',
      placeholder: 'jdbc:clickhouse://host:8123/db',
    },
    database: { label: 'Database', placeholder: 'iot' },
    table: { label: 'Target Table', placeholder: 'device_data_wide' },
    columnMapping: {
      label: 'Column Mapping',
      help: 'JSON: column: placeholder',
      placeholder: ' "ts": "[timestamp]", "device_id": "[deviceIdentification]", "payload": "[payload]" ',
    },
    useAsyncInsert: {
      label: 'Async Insert',
      help: 'Enable ClickHouse async insert feature',
    },
    username: { label: 'Username' },
    password: { label: 'Password', help: 'ClickHouse user password (optional); AES encrypted' },
    presets: {
      local_label: 'Local ClickHouse',
      local_description: 'Local ClickHouse + default user + async insert',
    },
  },

  influxdb: {
    name: 'InfluxDB',
    apiVersion: {
      label: 'API Version',
      help: 'V2 uses token; V1 uses username/password',
      opt_V2: 'V2 (recommended)',
      opt_V1: 'V1 (legacy)',
    },
    url: { label: 'URL', placeholder: 'http://influx.example.com:8086' },
    org: { label: 'Org', help: 'Required for V2; optional for V1', placeholder: 'iot-org' },
    bucket: { label: 'Bucket / Database', help: 'V2: bucket / V1: database', placeholder: 'device_metrics' },
    measurement: {
      label: 'Measurement',
      help: 'Target measurement; supports [productId]',
      placeholder: 'device_data',
    },
    tagsMapping: {
      label: 'Tags Mapping',
      help: 'Tags mapping JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    fieldsMapping: { label: 'Fields Mapping', placeholder: ' "value": "[payload]" ' },
    token: { label: 'API Token', help: 'V2 API token; AES encrypted' },
    username: { label: 'V1 Username', help: 'V1 mode' },
    password: { label: 'V1 Password', help: 'V1 mode; encrypted' },
    presets: {
      v2_local_label: 'Local InfluxDB v2',
      v2_local_description: 'Local InfluxDB v2 + token auth',
    },
    validate: {
      v1_username_required: 'InfluxDB V1 mode requires username',
      v2_org_required: 'InfluxDB V2 mode requires Org',
    },
  },

  iotdb: {
    name: 'Apache IoTDB',
    host: { label: 'Host', placeholder: 'iotdb.example.com' },
    port: { label: 'Port' },
    storageGroup: { label: 'Storage Group', help: 'IoTDB storage group, e.g. root.iot', placeholder: 'root.iot' },
    timeseriesTemplate: {
      label: 'Timeseries Template',
      help: 'Timeseries path template; per-device timeseries',
      placeholder: 'root.iot.[routingKey]',
    },
    columnMapping: { label: 'Column Mapping', placeholder: ' "value": "[payload]" ' },
    username: { label: 'Username' },
    password: { label: 'Password', help: 'IoTDB default root/root; AES encrypted' },
    presets: {
      local_default_label: 'Local IoTDB',
      local_default_description: 'Local IoTDB + default root/root + root.iot storage group',
    },
  },

  postgresql: {
    name: 'PostgreSQL',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:postgresql://host:5432/db' },
    username: { label: 'Username', placeholder: 'postgres' },
    schemaName: { label: 'Schema', help: 'Default public' },
    table: { label: 'Target Table', placeholder: 'iot_data' },
    columnMapping: {
      label: 'Column Mapping',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: 'Conflict Strategy',
      opt_INSERT: 'INSERT (throw)',
      opt_UPSERT: 'UPSERT (ON CONFLICT DO UPDATE)',
      opt_IGNORE: 'IGNORE (ON CONFLICT DO NOTHING)',
    },
    conflictKeys: {
      label: 'UPSERT Conflict Keys',
      help: 'Required for UPSERT; comma-separated columns',
      placeholder: 'device_id,ts',
    },
    password: { label: 'Password', help: 'DB password; AES encrypted' },
    presets: {
      local_label: 'Local PostgreSQL',
      local_description: 'Local PostgreSQL + public schema + INSERT',
      upsert_label: 'UPSERT Mode',
      upsert_description: 'UPSERT by (device_id, ts) primary key; idempotent for replays',
    },
  },

  mongodb: {
    name: 'MongoDB',
    uri: {
      label: 'Connection URI',
      help: 'MongoDB URI; can include user:pass＠ inline',
      placeholder: 'mongodb://host:27017 or mongodb+srv://...',
    },
    database: { label: 'Database', placeholder: 'iot' },
    collection: { label: 'Collection', help: 'Collection; supports [productId]', placeholder: 'device_data' },
    writeMode: {
      label: 'Write Mode',
      opt_INSERT: 'INSERT (new doc per message)',
      opt_UPSERT: 'UPSERT (update or insert by key)',
    },
    upsertKey: { label: 'UPSERT Key', help: 'Required for UPSERT', placeholder: 'deviceId' },
    writeConcern: {
      label: 'Write Concern',
      opt_UNACKNOWLEDGED: 'UNACKNOWLEDGED (fastest, no ack)',
      opt_ACKNOWLEDGED: 'ACKNOWLEDGED (recommended)',
      opt_MAJORITY: 'MAJORITY (majority write)',
    },
    username: { label: 'Username', help: 'Optional if URI already has it' },
    password: { label: 'Password', help: 'AES encrypted' },
    authDatabase: { label: 'Auth Database' },
    presets: {
      local_label: 'Local MongoDB',
      local_description: 'Local MongoDB + iot db + device_data collection',
      upsert_label: 'UPSERT (idempotent per device)',
      upsert_description: 'UPSERT by deviceId; replayed messages do not duplicate',
    },
  },

  pulsar: {
    name: 'Apache Pulsar',
    serviceUrl: {
      label: 'Service URL',
      help: 'Pulsar broker service URL',
      placeholder: 'pulsar://host:6650 or pulsar+ssl://host:6651',
    },
    topic: {
      label: 'Topic',
      help: 'Full Pulsar topic path; supports persistent/non-persistent',
      placeholder: 'persistent://tenant/namespace/topic',
    },
    producerName: { label: 'Producer Name', help: 'Optional; auto-generated if empty', placeholder: 'iot-bridge' },
    sendMode: {
      label: 'Send Mode',
      opt_ASYNC: 'ASYNC (high throughput)',
      opt_SYNC: 'SYNC (strong consistency)',
    },
    compressionType: {
      label: 'Compression',
      opt_NONE: 'NONE',
      opt_LZ4: 'LZ4 (recommended)',
      opt_ZLIB: 'ZLIB',
      opt_ZSTD: 'ZSTD (high ratio)',
      opt_SNAPPY: 'SNAPPY',
    },
    enableBatching: { label: 'Enable Batching', help: 'Higher throughput; slight latency' },
    schemaType: {
      label: 'Schema Type',
      opt_BYTES: 'BYTES (raw)',
      opt_STRING: 'STRING',
      opt_JSON: 'JSON',
      opt_AVRO: 'AVRO',
    },
    authToken: { label: 'JWT Auth Token', help: 'Pulsar JWT auth token; AES encrypted' },
    tlsCert: {
      label: 'TLS Client Cert',
      help: 'Required for TLS; PEM; encrypted',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    tlsKey: {
      label: 'TLS Client Key',
      help: 'Required for TLS; PEM; encrypted',
      placeholder: '-----BEGIN PRIVATE KEY-----\n...',
    },
    presets: {
      local_no_auth_label: 'Local Pulsar (no auth)',
      local_no_auth_description: 'Pulsar standalone + LZ4 + ASYNC',
      jwt_prod_label: 'Production JWT auth',
      jwt_prod_description: 'Production Pulsar cluster + JWT token + ZSTD compression',
    },
  },

  // ============================== DM (DaMeng) ==============================
  dm: {
    name: 'DM (DaMeng)',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:dm://host:5236/DAMENG' },
    username: { label: 'Username', placeholder: 'SYSDBA' },
    schemaName: { label: 'Schema', help: 'Defaults to user-named schema; optional' },
    table: { label: 'Target table', placeholder: 'iot_data or IOT.iot_data' },
    columnMapping: {
      label: 'Column mapping',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: 'Conflict strategy',
      opt_INSERT: 'INSERT (throw on conflict)',
      opt_UPDATE: 'UPDATE (MERGE INTO)',
      opt_IGNORE: 'IGNORE (skip on conflict)',
    },
    password: { label: 'Password', help: 'DB password; encrypted at rest (AES)' },
    presets: {
      local_label: 'Local DM',
      local_description: 'Local DM + SYSDBA + INSERT mode',
    },
  },

  // ============================== KingBase ==============================
  kingbase: {
    name: 'KingBase',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:kingbase8://host:54321/dbname' },
    username: { label: 'Username', placeholder: 'system' },
    schemaName: { label: 'Schema', help: 'Defaults to public (PG-compatible)' },
    table: { label: 'Target table', placeholder: 'iot_data' },
    columnMapping: {
      label: 'Column mapping',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: 'Conflict strategy',
      opt_INSERT: 'INSERT (throw on conflict)',
      opt_UPSERT: 'UPSERT (ON CONFLICT DO UPDATE)',
      opt_IGNORE: 'IGNORE (ON CONFLICT DO NOTHING)',
    },
    conflictKeys: {
      label: 'UPSERT conflict keys',
      help: 'Required when onConflict=UPSERT; comma-separated column names',
      placeholder: 'device_id,ts',
    },
    password: { label: 'Password', help: 'DB password; encrypted at rest (AES)' },
    presets: {
      local_label: 'Local KingBase',
      local_description: 'Local KingBase + public schema + INSERT mode',
    },
  },
};
