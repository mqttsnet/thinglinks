/**
 * 数据桥接-数据源 模型定义
 *
 * 与后端 DataSource entity / VO 1:1 对齐：
 *   - rule_data_source.connection_json → connectionJson（敏感，列表接口屏蔽）
 *   - rule_data_source.credential_json → credentialJson（敏感，列表接口屏蔽）
 */

export interface DataSourcePageQuery {
  id?: string;
  appId?: string;
  dataSourceName?: string;       // 数据源名称（模糊查询）
  dataSourceCode?: string;       // 业务唯一编码
  direction?: string;            // 方向 10/20/30
  sourceType?: string;           // 协议类型 KAFKA/REDIS/...
  enable?: boolean;
  healthStatus?: string;         // HEALTHY/DEGRADED/DOWN/UNKNOWN
}

export interface DataSourceSaveVO {
  appId?: string;
  dataSourceName: string;        // 必填
  dataSourceCode?: string;       // 不传后端自动生成 snowflake
  direction: string;             // 必填
  sourceType: string;            // 必填
  connectionJson: string;        // 必填，连接参数 JSON
  credentialJson?: string;       // 凭证 JSON
  serialization?: string;        // 默认 JSON
  defaultQos?: number;
  defaultRateLimitQps?: number;
  defaultRetryMaxTimes?: number;
  defaultRetryBackoffMs?: number;
  defaultTimeoutMs?: number;
  defaultDeadLetterDataSourceId?: string;
  extendParams?: string;
  remark?: string;
}

export interface DataSourceUpdateVO extends DataSourceSaveVO {
  id: string;
}

export interface DataSourceResultVO {
  echoMap?: any;
  id?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;

  appId?: string;
  dataSourceName?: string;
  dataSourceCode?: string;
  direction?: string;
  sourceType?: string;
  /** 列表接口此字段为 null（后端屏蔽），仅详情接口含明文 */
  connectionJson?: string;
  /** 列表接口此字段为 null（后端屏蔽），仅详情接口含明文 */
  credentialJson?: string;
  serialization?: string;

  defaultQos?: number;
  defaultRateLimitQps?: number;
  defaultRetryMaxTimes?: number;
  defaultRetryBackoffMs?: number;
  defaultTimeoutMs?: number;
  defaultDeadLetterDataSourceId?: string;

  enable?: boolean;
  healthStatus?: string;
  lastHealthCheckTime?: string;

  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
}
