/**
 * 产品发布记录 TS 模型。
 *
 * 与后端 thinglinks-link-entity 中 com.mqttsnet.thinglinks.productpublishrecord.* 包对齐。
 */

/**
 * TDengine 超级表字段定义快照 ── 对应后端 {@code DdlFieldVO}。
 *
 * 由 Orchestrator 在 CREATE STABLE 成功后调 TDengine DESCRIBE 反查得到,
 * 是 TD 实际存储的真相(不是提交时的 DTO),前端 DDL 详情弹窗按此渲染表结构。
 */
export interface DdlFieldVO {
  /** 字段名(如 ts / event_time / temperature)。 */
  field?: string;
  /** 字段类型(TDengine 实际类型:TIMESTAMP / INT / NCHAR / BINARY / ...)。 */
  type?: string;
  /** 字段长度(变长类型 NCHAR/BINARY 的字符数;定长类型为 null)。 */
  length?: number;
  /** 字段实际字节数(NCHAR=length×4,BINARY=length,INT=4 ...)。 */
  bytes?: number;
}

/**
 * 单条 DDL 执行明细 ── 对应后端 {@code PublishDdlItemVO}。
 *
 * 后端用 mybatis-plus JacksonTypeHandler 把 List<PublishDdlItemVO> 直接序列化到
 * product_publish_record.ddl_summary JSON 列;前端拿到的是 typed 数组,无需 JSON.parse。
 */
export interface PublishDdlItemVO {
  /** 操作类型:CREATE_STABLE / DROP_STABLE / ALTER_STABLE 等。 */
  operation?: string;
  /** 超级表名(由 ProductTdsNamer 拼接)。 */
  stableName?: string;
  /** 服务编码(便于反查物模型 service)。 */
  serviceCode?: string;
  /** 服务名称(人类友好展示)。 */
  serviceName?: string;
  /** 字段数(schema columns 数量,含 ts/event_time 系统字段)。 */
  columnCount?: number;
  /** 是否执行成功。 */
  success?: boolean;
  /** 失败原因(success=false 时来自驱动 errMsg)。 */
  errorMsg?: string;
  /** 执行耗时(毫秒)。 */
  durationMs?: number;
  /** 本次执行时间(yyyy-MM-dd HH:mm:ss)── Job 重试时更新为最新时间。 */
  executedAt?: string;
  /** 执行次数(首次=1,Job 重试累加)。 */
  attemptCount?: number;
  /**
   * 表的普通字段列表(CREATE STABLE 成功后由 TDengine DESCRIBE 反查)。
   * DROP_STABLE / describe 失败时为 null。
   */
  schemaFields?: DdlFieldVO[];
  /** 表的 tag 字段列表(同 schemaFields,由 describe 反查)。 */
  tagsFields?: DdlFieldVO[];
  /** 单行字段字节数合计(不含 tag,TD 行级上限 65531)。 */
  rowBytes?: number;
}

/** 灰度命中的分组快照(对应后端 CanaryGroup)。 */
export interface CanaryGroup {
  groupId?: string;
  groupName?: string;
  deviceCount?: number;
}

/** 灰度执行结果快照(对应后端 StrategyResultDTO.CanaryResult)。 */
export interface CanaryResult {
  /** 来源:group | manual | percent。 */
  source?: string;
  /** 所选分组明细(source=group)。 */
  groups?: CanaryGroup[];
  /** 设备名单(source=manual)。 */
  deviceIdentifications?: string[];
  /** 灰度比例(source=percent)。 */
  percent?: number;
  /** 规则目标设备数。 */
  targetCount?: number;
}

/** 影子执行结果快照(对应后端 StrategyResultDTO.ShadowResult)。 */
export interface ShadowResult {
  /** 本次预建成功的超表数。 */
  preBuiltStableCount?: number;
}

/**
 * 策略执行结果快照(对应后端 StrategyResultDTO / canary_result_json,发布那一刻冻结)。
 * 全量只用通用字段;灰度填 canary;影子填 shadow。
 */
export interface StrategyResultDTO {
  /** 发布策略(0=全量 / 1=灰度 / 2=影子)── 快照自包含。 */
  strategy?: number;
  /** 本次实际改绑设备数(去重;含网关连带子设备)。 */
  affectedDeviceCount?: number;
  /** 发布那一刻该产品设备总数(占比基数)。 */
  productTotalAtPublish?: number;
  canary?: CanaryResult;
  shadow?: ShadowResult;
}

/** 发布记录(对应 product_publish_record 表)。 */
export interface ProductPublishRecordResultVO {
  id?: string;
  productIdentification?: string;
  sourceVersion?: string;
  targetVersion?: string;
  intent?: number;
  status?: number;
  /**
   * DDL 执行明细列表(typed,与后端 ddl_summary JSON 列对齐)。
   *
   * 后端 typeHandler 自动反序列化,前端无需 JSON.parse(record.ddlSummary)。
   */
  ddlItems?: PublishDdlItemVO[];
  /**
   * 发布策略(字典 PRODUCT_PUBLISH_STRATEGY,0=全量 / 1=灰度 / 2=影子)。
   *
   * 来源:发布记录表本身无此字段,后端 Controller 在 handlerResult 阶段按
   * (productIdentification, targetVersion) 反查 product_version 表富化。
   * 仅意图=发布(intent=0)的记录非空。
   */
  publishStrategy?: number;
  /**
   * 灰度配置 JSON(仅 publishStrategy=CANARY=1 时非空)。
   * 结构:
   *   {"mode":"whitelist","deviceIdentifications":["dev001","dev002"]}
   *   {"mode":"percent","canaryPercent":30}
   */
  canaryConfigJson?: string;
  /** 策略执行结果快照(发布那一刻冻结;全量/灰度/影子按策略填不同字段)。 */
  canaryResult?: StrategyResultDTO;
  failedReason?: string;
  /** 已重试次数(达 maxRetryCount 不再重跑)。 */
  retryCount?: number;
  /** 最大重试次数(用户可配,上限 10)。 */
  maxRetryCount?: number;
  startedTime?: string;
  finishedTime?: string;
  remark?: string;
  deleted?: number;
  createdTime?: string;
  createdBy?: number;
  updatedTime?: string;
  updatedBy?: number;
  createdOrgId?: number;
  /**
   * 后端 @Echo 回填的中文展示值集合,key = 字段名。
   *
   * 当前已 echo:intent / status(字典),createdBy / updatedBy(用户昵称),createdOrgId(组织名)。
   */
  echoMap?: Record<string, string>;
}

/** 分页查询参数。 */
export interface ProductPublishRecordPageQuery {
  productIdentification?: string;
  targetVersion?: string;
  intent?: number;
  status?: number;
}
