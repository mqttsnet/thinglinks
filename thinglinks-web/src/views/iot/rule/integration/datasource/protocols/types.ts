import type { FormSchema } from '/@/components/Form';

/**
 * 协议模块统一契约（前端 Strategy + Registry 模式核心接口）。
 *
 * 每种数据源类型（Kafka / Redis / RocketMQ / RabbitMQ / MySQL / PostgreSQL /
 * HTTP / WebHook / MQTT / TDengine / ClickHouse / InfluxDB / IoTDB / MongoDB / Pulsar）
 * 对应一个独立 .ts 文件实现本接口，注册到 {@link ./index.ts} registry。
 *
 * <h3>设计原则（OCP / 修一个协议不影响其它）</h3>
 * <ul>
 *   <li>每协议一个文件：connection / credential schema + presets + 默认策略全在一处</li>
 *   <li>新增协议 = 新建一个文件 + 在 index.ts 注册一行；既有协议代码 0 改</li>
 *   <li>字段名 = 后端 entity DTO ({@code dto/bridge/protocol/xxx/}) field 名 = JSON key（单一真相）</li>
 *   <li>provided helpers 的实现可选（registry 提供合理默认行为）</li>
 * </ul>
 */
export interface ProtocolModule {
  /** 协议类型标识（与字典 BRIDGE_DATA_SOURCE_TYPE 子项 1:1 对齐；ConnectorType.name() 大写） */
  readonly type: string;

  /** 连接参数表单 schema（必填） */
  connectionFields(): FormSchema[];

  /** 凭证表单 schema（必填，无凭证协议返回 []） */
  credentialFields(): FormSchema[];

  /**
   * connection_json 反序列化（可选；默认 JSON.parse + null 兜底）。
   * <p>协议有特殊解析需求时（嵌套结构 / 字段名映射）可重写。
   */
  parseConnection?(json: string | null | undefined): Record<string, any>;

  /** credential_json 反序列化（可选） */
  parseCredential?(json: string | null | undefined): Record<string, any>;

  /**
   * "加载示例配置" preset 列表（可选；空数组 / 不实现都隐藏下拉）。
   */
  examplePresets?(): ProtocolPreset[];

  /**
   * 协议级智能预填默认策略（可选；ADD 模式选定 sourceType 后自动填）。
   * <p>例如 Kafka 推荐 retryMax=0（producer 内部已重试），HTTP 推荐 retryMax=3。
   */
  recommendedDefaults?(): ProtocolDefaultPolicy;

  /**
   * 表单提交前协议级校验（可选；返回错误信息字符串则阻止提交）。
   * <p>用于业务规则校验（如 Redis SENTINEL 模式必填 sentinels）；非空表单填值层面校验由 schema 的 rules 处理。
   */
  validate?(values: Record<string, any>): string | null;
}

/**
 * 一条预设配置（"加载示例"下拉项）。
 */
export interface ProtocolPreset {
  /** 唯一 key（同一协议内不重复）。一般用 'local' / 'aliyun_xxx' / 'prod_typical' 等 */
  key: string;

  /** 显示名（中文短句，如"本地开发环境"） */
  label: string;

  /** 详细描述（hover tooltip） */
  description?: string;

  /** 连接段预设值（与 connectionFields() field 名对齐） */
  connection?: Record<string, any>;

  /** 凭证段预设值 */
  credential?: Record<string, any>;
}

/**
 * 协议级智能预填默认策略（{@code DataSource.default_*} 字段集合）。
 */
export interface ProtocolDefaultPolicy {
  defaultQos?: number;
  defaultRateLimitQps?: number;
  defaultRetryMaxTimes?: number;
  defaultRetryBackoffMs?: number;
  defaultTimeoutMs?: number;
}
