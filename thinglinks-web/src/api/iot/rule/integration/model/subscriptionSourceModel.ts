/**
 * 数据桥接-订阅源 模型定义
 */

export interface SubscriptionSourcePageQuery {
  id?: string;
  appId?: string;
  sourceName?: string;
  sourceCode?: string;
  dataSourceId?: string;
  targetHandler?: string;        // MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER
  enable?: boolean;
}

export interface SubscriptionSourceSaveVO {
  appId?: string;
  sourceName: string;            // 必填
  sourceCode?: string;
  dataSourceId: string;          // 必填，关联数据源（direction=20 入站 或 30 双向）
  targetHandler: string;         // 必填
  mappingJson: string;           // 必填，字段映射 JSON
  targetProductIdentification?: string; // MQTT_FORWARD 时必填
  targetTopicTemplate?: string;
  extendParams?: string;
  remark?: string;
}

export interface SubscriptionSourceUpdateVO extends SubscriptionSourceSaveVO {
  id: string;
}

export interface SubscriptionSourceResultVO {
  echoMap?: any;
  id?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;

  appId?: string;
  sourceName?: string;
  sourceCode?: string;
  dataSourceId?: string;
  /** 关联数据源业务编码(后端 service join 反填,列表/卡片/详情友好展示) */
  dataSourceCode?: string;
  /** 关联数据源名称(后端 service join 反填,列表/卡片/详情友好展示) */
  dataSourceName?: string;
  targetHandler?: string;
  mappingJson?: string;
  targetProductIdentification?: string;
  targetTopicTemplate?: string;

  enable?: boolean;
  lastConsumeOffset?: string;

  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
}
