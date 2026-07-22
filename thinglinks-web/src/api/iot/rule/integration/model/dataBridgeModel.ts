/**
 * 数据桥接-规则 模型定义
 *
 * 与后端 DataBridge entity / VO 1:1 对齐：
 *   - rule_data_bridge.action_config_json → actionConfigJson（敏感，列表接口屏蔽）
 *   - rule_data_bridge.match_config_json → matchConfigJson（不加密，matcher 热路径用）
 */

export interface DataBridgePageQuery {
  id?: string;
  appId?: string;
  ruleName?: string;             // 规则名称（模糊查询）
  ruleCode?: string;
  direction?: string;            // 10/20
  dataSourceId?: string;
  enable?: boolean;
}

export interface DataBridgeSaveVO {
  appId?: string;
  ruleName: string;              // 必填
  ruleCode?: string;             // 不传后端自动生成
  direction: string;             // 必填
  dataSourceId: string;          // 必填
  matchConfigJson: string;       // 必填，匹配条件 JSON
  actionConfigJson: string;      // 必填，动作配置 JSON
  qos?: number;                  // 规则级覆盖
  rateLimitQps?: number;
  retryMaxTimes?: number;
  retryBackoffMs?: number;
  timeoutMs?: number;
  deadLetterDataSourceId?: string;
  priority?: number;             // 默认 100
  extendParams?: string;
  remark?: string;
}

export interface DataBridgeUpdateVO extends DataBridgeSaveVO {
  id: string;
}

export interface DataBridgeResultVO {
  echoMap?: any;
  id?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;

  appId?: string;
  ruleName?: string;
  ruleCode?: string;
  direction?: string;
  dataSourceId?: string;
  /** 关联数据源业务编码(后端 service join 反填,列表/卡片/详情友好展示) */
  dataSourceCode?: string;
  /** 关联数据源名称(后端 service join 反填,列表/卡片/详情友好展示) */
  dataSourceName?: string;
  matchConfigJson?: string;
  /** 列表接口此字段为 null（后端屏蔽），仅详情接口含明文 */
  actionConfigJson?: string;

  qos?: number;
  rateLimitQps?: number;
  retryMaxTimes?: number;
  retryBackoffMs?: number;
  timeoutMs?: number;
  deadLetterDataSourceId?: string;

  enable?: boolean;
  priority?: number;
  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
}

/** testSink 入参（任意 Map，会被 JSON 序列化成 byte[] 发送） */
export interface TestSinkRequest {
  [key: string]: any;
}

/** testSink 返回结果 */
export interface TestSinkResult {
  success: boolean;
  messageId?: string;
  latencyMs: number;
  errorCode?: string;
  errorMessage?: string;
  attributes?: Record<string, any>;
}
