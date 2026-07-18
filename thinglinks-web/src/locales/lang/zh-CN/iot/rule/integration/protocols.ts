/**
 * 桥接 / 数据源 - 协议模块 i18n（中文）。
 *
 * <p>对应 {@code views/iot/rule/integration/datasource/protocols/} 下 15 个协议模块的
 * label / helpMessage / placeholder / select option / preset 文案。
 *
 * <p>访问路径：{@code iot.rule.integration.protocols.<protocol>.<field>.<key>}
 *
 * @author mqttsnet
 */
export default {
  // 通用结构片段
  common: {
    saslMechanism_PLAIN: 'PLAIN（明文）',
    saslMechanism_SCRAM_256: 'SCRAM-SHA-256（推荐）',
    saslMechanism_SCRAM_512: 'SCRAM-SHA-512',
  },

  // ============================== Kafka ==============================
  kafka: {
    name: 'Kafka',
    bootstrapServers: {
      label: 'Broker 地址',
      help: 'Kafka broker 地址列表，多个用英文逗号分隔。生产环境至少配 2 个 broker',
      placeholder: '127.0.0.1:9092 或 host1:9092,host2:9092',
    },
    topic: {
      label: '目标 Topic',
      help: '数据投递到的 topic 名称。支持模板变量 [tenantId] [productId]',
      placeholder: 'iot-out-topic',
    },
    clientId: {
      label: 'Client ID',
      help: 'Kafka 客户端标识。留空自动生成（推荐）',
      placeholder: 'thinglinks-bridge-[dsId]',
    },
    partitionStrategy: {
      label: '分区策略',
      opt_hash: 'hash（按设备 ID 一致性 hash，推荐）',
      opt_roundRobin: 'roundRobin（轮询）',
      opt_sticky: 'sticky（粘性）',
      opt_manual: 'manual（手动指定）',
    },
    useTls: {
      label: '启用 TLS',
      help: '启用后下方"凭证密钥"会显示 SASL 配置',
    },
    saslMechanism: { label: 'SASL 机制' },
    saslUsername: {
      label: 'SASL 用户名',
      help: '由 Kafka 集群管理员分配',
      placeholder: 'kafka_user',
    },
    saslPassword: {
      label: 'SASL 密码',
      help: '⚠ 落盘 AES 加密；列表接口不返回明文',
    },
    presets: {
      local_label: '本地开发环境',
      local_description: '本机 Kafka broker，无 SASL；适合开发调试',
      aliyun_sasl_label: '阿里云 Kafka (SASL_SSL)',
      aliyun_sasl_description: '阿里云 VPC Kafka 实例，SASL/PLAIN 鉴权',
    },
  },

  // ============================== Redis ==============================
  redis: {
    name: 'Redis',
    mode: {
      label: '部署模式',
      help: '选择 Redis 部署模式',
      opt_STANDALONE: 'STANDALONE（单机）',
      opt_SENTINEL: 'SENTINEL（哨兵）',
      opt_CLUSTER: 'CLUSTER（集群）',
    },
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: '端口' },
    sentinels: {
      label: 'Sentinels',
      help: 'Sentinel 节点列表，多个用逗号分隔',
      placeholder: 'host1:26379,host2:26379',
    },
    masterName: { label: 'Master 名称', placeholder: 'mymaster' },
    clusterNodes: { label: '集群节点', placeholder: 'host1:7000,host2:7000,host3:7000' },
    db: {
      label: '数据库索引',
      help: '数据库索引；CLUSTER 模式不支持',
    },
    command: {
      label: '写入命令',
      help: '数据写入命令：列表用 LPUSH/RPUSH；流用 XADD；发布订阅用 PUBLISH；KV 用 SET',
      opt_LPUSH: 'LPUSH（List 头插）',
      opt_RPUSH: 'RPUSH（List 尾插）',
      opt_XADD: 'XADD（Stream）',
      opt_PUBLISH: 'PUBLISH（发布订阅）',
      opt_SET: 'SET（KV）',
    },
    keyTemplate: {
      label: 'Key 模板',
      help: 'Key 模板，支持占位符 [tenantId] [productId] [deviceId]',
      placeholder: 'iot:[tenantId]:[productId]:[deviceId]',
    },
    ttlSeconds: {
      label: 'TTL（秒）',
      help: 'SET / XADD 时 TTL 秒；0=永不过期',
    },
    password: {
      label: '密码',
      help: 'Redis AUTH 密码（无密码可空）；落盘 AES 加密',
    },
    presets: {
      local_standalone_label: '本地 STANDALONE',
      local_standalone_description: '本机 Redis 单机 + LPUSH 写入',
      aliyun_cluster_label: '阿里云 Redis CLUSTER',
      aliyun_cluster_description: '阿里云 Redis 集群版 + Stream(XADD) 写入 + 1d TTL',
    },
    validate: {
      sentinel_required: 'Redis SENTINEL 模式必须填 sentinels 和 masterName',
      cluster_required: 'Redis CLUSTER 模式必须填集群节点列表',
    },
  },

  // ============================== RocketMQ ==============================
  rocketmq: {
    name: 'RocketMQ',
    nameServer: {
      label: 'NameServer',
      help: 'RocketMQ NameServer 地址列表，多个用分号分隔；阿里云填 endpoint URL',
      placeholder: 'rmq-namesrv-1:9876;rmq-namesrv-2:9876',
    },
    topic: {
      label: '目标 Topic',
      placeholder: 'iot-out-topic',
      // 本段 help 含 @ / | / % / ${} 字面量,用函数式消息避免被 vue-i18n 当语法编译
      help: () => 'RocketMQ topic 字符规约：仅支持字母 / 数字 / 下划线 / 中划线（^[%|a-zA-Z0-9_-]+$），长度 ≤ 255；含 . : @ 等字符会被 broker 拒绝',
    },
    tag: {
      label: 'Tag',
      help: () => 'Tag 过滤；支持占位符 ${actionType}（运行时按事件类型动态替换）。Tag 字符规约同 topic',
      placeholder: '*',
    },
    producerGroup: {
      label: 'Producer Group',
      help: () => '留空时由 starter 自动按 identifier 生成；用户自填请遵守 RocketMQ 字符规约 ^[%|a-zA-Z0-9_-]+$ 且长度 ≤ 255（含点 . 的域名形式会被 broker 拒绝）',
      placeholder: 'PG_BRIDGE_BIZ01',
    },
    accessChannel: {
      label: '部署模式',
      help: 'LOCAL 走开源 client；CLOUD 走 ONS client + AK/SK 鉴权',
      opt_LOCAL: 'LOCAL（自建 Apache RocketMQ）',
      opt_CLOUD: 'CLOUD（阿里云 RocketMQ）',
    },
    namespace: {
      label: 'Instance ID',
      help: '阿里云 RocketMQ instanceId',
      placeholder: 'MQ_INST_xxxxxx',
    },
    vipChannelEnabled: {
      label: 'VIP 通道',
      on: '开启',
      off: '关闭',
      help: 'VIP 快速通道（fastListenPort = brokerPort − 2，默认 10909）。RocketMQ 5.x 官方建议关闭：容器/NAT 环境下 fastListenPort 经常被漏掉，producer 卡在 10909 三次握手 → send 全超时。仅当确认 broker fastListenPort 已对外开放时才打开。',
    },
    accessKey: {
      label: 'AccessKey',
      help: '阿里云 AccessKey ID（仅 CLOUD 模式必填）',
      placeholder: 'LTAI_xxx',
    },
    secretKey: {
      label: 'SecretKey',
      help: '阿里云 AccessKey Secret；落盘 AES 加密',
    },
    presets: {
      apache_local_label: '自建 Apache RocketMQ',
      apache_local_description: '本机 / 内网 NameServer，无 ACL',
      aliyun_label: '阿里云 RocketMQ',
      aliyun_description: '阿里云专业版，含 instanceId + AK/SK 鉴权',
    },
    validate: {
      cloud_namespace_required: 'RocketMQ CLOUD 模式必须填阿里云 instanceId',
    },
  },

  // ============================== RabbitMQ ==============================
  rabbitmq: {
    name: 'RabbitMQ',
    host: { label: 'Host', placeholder: '127.0.0.1' },
    port: { label: '端口', help: 'AMQP 端口；TLS 通常 5671' },
    virtualHost: { label: 'Virtual Host', help: 'Virtual host；多 vhost 部署用' },
    exchangeName: {
      label: 'Exchange',
      help: 'Exchange 名称（与后端 RabbitmqSink.exchangeName 字段对齐）',
      placeholder: 'iot.events',
    },
    exchangeType: {
      label: 'Exchange 类型',
      opt_direct: 'direct（精确路由）',
      opt_topic: 'topic（通配符）',
      opt_fanout: 'fanout（广播）',
      opt_headers: 'headers（按 header 路由）',
    },
    routingKey: {
      label: 'Routing Key',
      help: 'Routing key 模板，支持占位符',
      placeholder: 'device.[productId].[actionType]',
    },
    useTls: { label: '启用 TLS', help: '启用后端口建议改 5671' },
    username: { label: '用户名', placeholder: 'guest' },
    password: { label: '密码', help: 'AMQP 密码；落盘 AES 加密' },
    presets: {
      local_topic_label: '本地 RabbitMQ topic exchange',
      local_topic_description: '本机 RabbitMQ + topic exchange + 设备维度 routingKey',
      fanout_label: 'fanout 广播',
      fanout_description: '所有绑定 queue 都会收到（routingKey 不重要）',
    },
  },

  // ============================== MySQL ==============================
  mysql: {
    name: 'MySQL',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'JDBC URL；建议附 useSSL/serverTimezone/characterEncoding 参数',
      placeholder: 'jdbc:mysql://host:3306/db?useSSL=false&serverTimezone=UTC',
    },
    username: {
      label: '用户名',
      help: '数据库用户名（与后端 MysqlConnectionDto.username 对齐，归属 connection 段）',
      placeholder: 'iot_writer',
    },
    table: { label: '目标表', help: '目标表名；支持模板 [productId]', placeholder: 'iot_data' },
    columnMapping: {
      label: '字段映射',
      help: 'JSON 格式：表字段: 占位符；占位符可用 envelope 任意字段',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: '冲突策略',
      help: '冲突处理策略',
      opt_INSERT: 'INSERT（失败抛错）',
      opt_UPDATE: 'UPDATE（ON DUPLICATE KEY UPDATE）',
      opt_IGNORE: 'IGNORE（INSERT IGNORE）',
    },
    password: { label: '密码', help: '数据库密码；落盘 AES 加密' },
    presets: {
      local_label: '本地 MySQL',
      local_description: '本机 MySQL + INSERT 模式',
      aliyun_rds_label: '阿里云 RDS for MySQL',
      aliyun_rds_description: '阿里云 RDS + UPDATE 模式（按 device_id+ts 主键）',
    },
  },

  // ============================== HTTP ==============================
  http: {
    name: 'HTTP',
    url: {
      label: 'URL',
      help: '目标接口 URL；支持模板 [tenantId] [productId]',
      placeholder: 'https://api.business.com/iot/data',
    },
    method: { label: 'Method' },
    contentType: { label: 'Content-Type', placeholder: 'application/json' },
    bearerToken: { label: 'Bearer Token', help: 'Bearer 鉴权 token（可空）；落盘 AES 加密' },
    basicUsername: { label: 'Basic 用户名', help: 'Basic Auth 用户名（可空）' },
    basicPassword: { label: 'Basic 密码', help: 'Basic Auth 密码（可空）；落盘加密' },
    presets: {
      simple_post_label: '简单 POST',
      simple_post_description: '无鉴权 POST application/json',
      bearer_auth_label: 'Bearer Token 鉴权',
      bearer_auth_description: 'OAuth2 / JWT Bearer 鉴权 + JSON body',
    },
  },

  // ============================== WebHook ==============================
  webhook: {
    name: 'WebHook',
    url: { label: 'Webhook URL', help: 'Webhook 接收方 URL', placeholder: 'https://webhook.example.com/iot' },
    contentType: { label: 'Content-Type' },
    signMethod: {
      label: '签名算法',
      help: '签名算法；HMAC_SHA256 通用推荐',
      opt_HMAC_SHA1: 'HMAC_SHA1',
      opt_HMAC_SHA256: 'HMAC_SHA256（推荐）',
      opt_HMAC_SHA512: 'HMAC_SHA512',
    },
    signHeaderName: { label: '签名 Header 名', help: '签名 header 名称' },
    timestampHeaderName: { label: '时间戳 Header 名', help: '时间戳 header 名称（防重放）' },
    secretKey: {
      label: '签名密钥',
      help: '签名密钥；落盘 AES 加密',
      placeholder: '至少 32 字节随机字符串',
    },
    presets: {
      hmac_sha256_label: 'HMAC-SHA256 + 时间戳防重放',
      hmac_sha256_description: '推荐生产配置：SHA256 签名 + X-Timestamp 防重放',
    },
  },

  // ============================== MQTT ==============================
  mqtt: {
    name: 'MQTT',
    broker: {
      label: 'Broker',
      help: 'MQTT broker 地址（tcp:// 或 ssl://）',
      placeholder: 'tcp://broker.example.com:1883 或 ssl://broker.example.com:8883',
    },
    clientId: { label: 'Client ID', help: 'MQTT clientId；留空自动生成', placeholder: 'thinglinks-bridge-[dsId]' },
    topicTemplate: {
      label: 'Topic 模板',
      help: '目标 topic 模板，支持占位符',
      placeholder: 'out/[productId]/[deviceId]',
    },
    qos: {
      label: 'QoS',
      help: '0=fire-forget / 1=at-least-once（推荐）/ 2=exactly-once',
      opt_0: '0 - fire-forget',
      opt_1: '1 - at-least-once（推荐）',
      opt_2: '2 - exactly-once',
    },
    retained: { label: '保留消息', help: 'MQTT retained flag' },
    username: { label: '用户名', help: 'MQTT 鉴权用户名（可空）' },
    password: { label: '密码', help: 'MQTT 鉴权密码；落盘 AES 加密' },
    caCert: {
      label: 'TLS CA 证书',
      help: 'PEM 格式 CA 证书（仅 ssl:// 模式需要）；落盘 AES 加密',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    presets: {
      tcp_no_auth_label: 'TCP 无鉴权',
      tcp_no_auth_description: '内网外部 broker，无 TLS / 无鉴权',
      ssl_with_auth_label: 'SSL + 用户名密码',
      ssl_with_auth_description: '生产推荐：SSL + 用户名密码鉴权',
    },
  },

  // ============================== TDengine ==============================
  tdengine: {
    name: 'TDengine',
    host: { label: 'Host', placeholder: 'tdengine.example.com' },
    port: { label: 'RESTful 端口', help: 'TDengine RESTful 端口（taosadapter 默认 6041）' },
    database: { label: '数据库', placeholder: 'iot' },
    superTable: {
      label: '超表（STable）',
      help: '超表名；桥接消息会 INSERT 到该 STable 自动管理子表',
      placeholder: 'device_data',
    },
    childTableTemplate: {
      label: '子表命名模板',
      help: '可选；按设备分子表用',
      placeholder: 'd_[deviceIdentification]',
    },
    tagsMapping: {
      label: '标签字段映射',
      help: 'TAGS 映射 JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    columnMapping: {
      label: '字段映射（含时间戳）',
      help: '字段映射 JSON，时间戳字段必填',
      placeholder: ' "ts": "[timestamp]", "value": "[payload]" ',
    },
    username: { label: '用户名' },
    password: { label: '密码', help: 'TDengine 默认 root/taosdata；落盘 AES 加密' },
    presets: {
      local_default_label: '本地 TDengine',
      local_default_description: '本机 TDengine + 默认 root/taosdata + iot 库 + device_data 超表',
    },
  },

  // ============================== ClickHouse ==============================
  clickhouse: {
    name: 'ClickHouse',
    jdbcUrl: {
      label: 'JDBC URL',
      help: 'ClickHouse JDBC URL（HTTP 端口默认 8123，TCP 端口 9000）',
      placeholder: 'jdbc:clickhouse://host:8123/db',
    },
    database: { label: '数据库', placeholder: 'iot' },
    table: { label: '目标表', placeholder: 'device_data_wide' },
    columnMapping: {
      label: '字段映射',
      help: 'JSON 格式：表字段: 占位符',
      placeholder: ' "ts": "[timestamp]", "device_id": "[deviceIdentification]", "payload": "[payload]" ',
    },
    useAsyncInsert: {
      label: '异步插入',
      help: '启用 ClickHouse 异步插入特性（async insert）',
    },
    username: { label: '用户名' },
    password: { label: '密码', help: 'ClickHouse 用户密码（无密码可空）；落盘 AES 加密' },
    presets: {
      local_label: '本地 ClickHouse',
      local_description: '本机 ClickHouse + default 用户 + 异步插入',
    },
  },

  // ============================== InfluxDB ==============================
  influxdb: {
    name: 'InfluxDB',
    apiVersion: {
      label: 'API 版本',
      help: 'V2 用 token 鉴权，V1 用用户名/密码',
      opt_V2: 'V2（推荐）',
      opt_V1: 'V1（兼容旧实例）',
    },
    url: { label: 'URL', placeholder: 'http://influx.example.com:8086' },
    org: { label: 'Org', help: 'V2 必填；V1 可空', placeholder: 'iot-org' },
    bucket: { label: 'Bucket / Database', help: 'V2 叫 bucket / V1 叫 database', placeholder: 'device_metrics' },
    measurement: {
      label: 'Measurement',
      help: '写入 measurement 名；支持模板 [productId]',
      placeholder: 'device_data',
    },
    tagsMapping: {
      label: 'Tags 映射',
      help: 'Tags 映射 JSON',
      placeholder: ' "productId": "[productId]", "deviceId": "[deviceIdentification]" ',
    },
    fieldsMapping: { label: 'Fields 映射', placeholder: ' "value": "[payload]" ' },
    token: { label: 'API Token', help: 'V2 API token；落盘 AES 加密' },
    username: { label: 'V1 用户名', help: 'V1 模式用' },
    password: { label: 'V1 密码', help: 'V1 模式用；落盘加密' },
    presets: {
      v2_local_label: '本地 InfluxDB v2',
      v2_local_description: '本机 InfluxDB v2 + token 鉴权',
    },
    validate: {
      v1_username_required: 'InfluxDB V1 模式必须填用户名',
      v2_org_required: 'InfluxDB V2 模式必须填 Org',
    },
  },

  // ============================== IoTDB ==============================
  iotdb: {
    name: 'Apache IoTDB',
    host: { label: 'Host', placeholder: 'iotdb.example.com' },
    port: { label: '端口' },
    storageGroup: { label: '存储组', help: 'IoTDB storage group，如 root.iot', placeholder: 'root.iot' },
    timeseriesTemplate: {
      label: '时序路径模板',
      help: '时序路径模板；按设备维度生成时序',
      placeholder: 'root.iot.[routingKey]',
    },
    columnMapping: { label: '字段映射', placeholder: ' "value": "[payload]" ' },
    username: { label: '用户名' },
    password: { label: '密码', help: 'IoTDB 默认 root/root；落盘 AES 加密' },
    presets: {
      local_default_label: '本地 IoTDB',
      local_default_description: '本机 IoTDB + 默认 root/root + root.iot 存储组',
    },
  },

  // ============================== PostgreSQL ==============================
  postgresql: {
    name: 'PostgreSQL',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:postgresql://host:5432/db' },
    username: { label: '用户名', placeholder: 'postgres' },
    schemaName: { label: 'Schema', help: '默认 public' },
    table: { label: '目标表', placeholder: 'iot_data' },
    columnMapping: {
      label: '字段映射',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: '冲突策略',
      opt_INSERT: 'INSERT（失败抛错）',
      opt_UPSERT: 'UPSERT（ON CONFLICT DO UPDATE）',
      opt_IGNORE: 'IGNORE（ON CONFLICT DO NOTHING）',
    },
    conflictKeys: {
      label: 'UPSERT 冲突键',
      help: 'onConflict=UPSERT 时必填；逗号分隔列名',
      placeholder: 'device_id,ts',
    },
    password: { label: '密码', help: '数据库密码；落盘 AES 加密' },
    presets: {
      local_label: '本地 PostgreSQL',
      local_description: '本机 PostgreSQL + public schema + INSERT',
      upsert_label: 'UPSERT 模式',
      upsert_description: '按 (device_id, ts) 主键 UPSERT；适合补数 / 重复消息幂等',
    },
  },

  // ============================== MongoDB ==============================
  mongodb: {
    name: 'MongoDB',
    uri: {
      label: 'Connection URI',
      help: 'MongoDB 连接 URI；可包含用户名密码（mongodb://user:pass＠host:port）',
      placeholder: 'mongodb://host:27017 或 mongodb+srv://...',
    },
    database: { label: '数据库', placeholder: 'iot' },
    collection: { label: 'Collection', help: '集合名；支持模板 [productId]', placeholder: 'device_data' },
    writeMode: {
      label: '写入策略',
      opt_INSERT: 'INSERT（每条新文档）',
      opt_UPSERT: 'UPSERT（按键更新或插入）',
    },
    upsertKey: { label: 'UPSERT 过滤键', help: 'writeMode=UPSERT 时必填', placeholder: 'deviceId' },
    writeConcern: {
      label: 'Write Concern',
      opt_UNACKNOWLEDGED: 'UNACKNOWLEDGED（最快无确认）',
      opt_ACKNOWLEDGED: 'ACKNOWLEDGED（推荐）',
      opt_MAJORITY: 'MAJORITY（多数派写入）',
    },
    username: { label: '用户名', help: 'URI 已含用户名时可空' },
    password: { label: '密码', help: '落盘 AES 加密' },
    authDatabase: { label: '认证数据库' },
    presets: {
      local_label: '本地 MongoDB',
      local_description: '本机 MongoDB + iot 库 + device_data collection',
      upsert_label: 'UPSERT 按设备幂等',
      upsert_description: '按 deviceId UPSERT，重复消息不重复入库',
    },
  },

  // ============================== Pulsar ==============================
  pulsar: {
    name: 'Apache Pulsar',
    serviceUrl: {
      label: 'Service URL',
      help: 'Pulsar broker service URL',
      placeholder: 'pulsar://host:6650 或 pulsar+ssl://host:6651',
    },
    topic: {
      label: 'Topic',
      help: 'Pulsar 完整 topic 路径；支持 persistent/non-persistent',
      placeholder: 'persistent://tenant/namespace/topic',
    },
    producerName: { label: 'Producer Name', help: '可空；空则自动生成', placeholder: 'iot-bridge' },
    sendMode: {
      label: '发送模式',
      opt_ASYNC: 'ASYNC（异步，吞吐高）',
      opt_SYNC: 'SYNC（同步，强一致）',
    },
    compressionType: {
      label: '压缩类型',
      opt_NONE: 'NONE（无压缩）',
      opt_LZ4: 'LZ4（推荐，速度快）',
      opt_ZLIB: 'ZLIB',
      opt_ZSTD: 'ZSTD（压缩率高）',
      opt_SNAPPY: 'SNAPPY',
    },
    enableBatching: { label: '启用 batching', help: '开启后吞吐高、单条延迟略增' },
    schemaType: {
      label: 'Schema 类型',
      opt_BYTES: 'BYTES（原始字节）',
      opt_STRING: 'STRING',
      opt_JSON: 'JSON',
      opt_AVRO: 'AVRO',
    },
    authToken: { label: 'JWT Auth Token', help: 'Pulsar JWT 鉴权 token；落盘 AES 加密' },
    tlsCert: {
      label: 'TLS 客户端证书',
      help: 'TLS 模式必填；PEM 格式；落盘加密',
      placeholder: '-----BEGIN CERTIFICATE-----\n...',
    },
    tlsKey: {
      label: 'TLS 客户端私钥',
      help: 'TLS 模式必填；PEM 格式；落盘加密',
      placeholder: '-----BEGIN PRIVATE KEY-----\n...',
    },
    presets: {
      local_no_auth_label: '本地 Pulsar 无鉴权',
      local_no_auth_description: '本机 Pulsar standalone + LZ4 压缩 + ASYNC 发送',
      jwt_prod_label: '生产 JWT 鉴权',
      jwt_prod_description: '生产 Pulsar 集群 + JWT token + ZSTD 压缩',
    },
  },

  // ============================== DM 达梦 ==============================
  dm: {
    name: '达梦 DM',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:dm://host:5236/DAMENG' },
    username: { label: '用户名', placeholder: 'SYSDBA' },
    schemaName: { label: 'Schema', help: '默认与用户名同名 schema；可空' },
    table: { label: '目标表', placeholder: 'iot_data 或 IOT.iot_data' },
    columnMapping: {
      label: '字段映射',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onDuplicate: {
      label: '冲突策略',
      opt_INSERT: 'INSERT（失败抛错）',
      opt_UPDATE: 'UPDATE（MERGE INTO）',
      opt_IGNORE: 'IGNORE（忽略冲突）',
    },
    password: { label: '密码', help: '数据库密码;落盘 AES 加密' },
    presets: {
      local_label: '本地达梦 DM',
      local_description: '本机 DM + SYSDBA + INSERT 模式',
    },
  },

  // ============================== KingBase 人大金仓 ==============================
  kingbase: {
    name: '人大金仓 KingBase',
    jdbcUrl: { label: 'JDBC URL', placeholder: 'jdbc:kingbase8://host:54321/dbname' },
    username: { label: '用户名', placeholder: 'system' },
    schemaName: { label: 'Schema', help: '默认 public（PG 兼容）' },
    table: { label: '目标表', placeholder: 'iot_data' },
    columnMapping: {
      label: '字段映射',
      placeholder: ' "device_id": "[deviceIdentification]", "ts": "[timestamp]", "payload": "[payload]" ',
    },
    onConflict: {
      label: '冲突策略',
      opt_INSERT: 'INSERT（失败抛错）',
      opt_UPSERT: 'UPSERT（ON CONFLICT DO UPDATE）',
      opt_IGNORE: 'IGNORE（ON CONFLICT DO NOTHING）',
    },
    conflictKeys: {
      label: 'UPSERT 冲突键',
      help: 'onConflict=UPSERT 时必填;逗号分隔列名',
      placeholder: 'device_id,ts',
    },
    password: { label: '密码', help: '数据库密码;落盘 AES 加密' },
    presets: {
      local_label: '本地金仓 KingBase',
      local_description: '本机 KingBase + public schema + INSERT 模式',
    },
  },
};
