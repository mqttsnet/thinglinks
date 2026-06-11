/**
 * 产品物模型版本相关 TS 模型。
 *
 * 与后端 thinglinks-link-entity 中 com.mqttsnet.thinglinks.productversion.* 包对齐。
 */

/** 版本快照 ── 命令参数(request / response 共用)。 */
export interface ProductSnapshotCommandParameterVO {
  parameterCode?: string;
  parameterName?: string;
  datatype?: string;
  enumlist?: string;
  min?: string;
  max?: string;
  step?: string;
  maxlength?: string;
  unit?: string;
  required?: number;
  description?: string;
}

/** 版本快照 ── 命令节点。 */
export interface ProductSnapshotCommandVO {
  commandCode?: string;
  commandName?: string;
  description?: string;
  requests?: ProductSnapshotCommandParameterVO[];
  responses?: ProductSnapshotCommandParameterVO[];
}

/** 版本快照 ── 属性节点。 */
export interface ProductSnapshotPropertyVO {
  propertyCode?: string;
  propertyName?: string;
  datatype?: string;
  enumlist?: string;
  min?: string;
  max?: string;
  step?: string;
  maxlength?: string;
  unit?: string;
  method?: string;
  required?: number;
  description?: string;
}

/** 版本快照 ── 服务节点。 */
export interface ProductSnapshotServiceVO {
  serviceCode?: string;
  serviceName?: string;
  serviceType?: string;
  serviceStatus?: number;
  description?: string;
  properties?: ProductSnapshotPropertyVO[];
  commands?: ProductSnapshotCommandVO[];
}

/** 版本快照 ── 顶层产品树。 */
export interface ProductSnapshotVO {
  /** 版本序号(本快照的雪花标识,对应后端 versionNo,DB 列 version_no)。 */
  versionNo?: string;
  publishTime?: number;
  publishStrategy?: number;
  appId?: string;
  productIdentification?: string;
  templateId?: number;
  productName?: string;
  productType?: number;
  manufacturerId?: string;
  manufacturerName?: string;
  model?: string;
  dataFormat?: string;
  deviceType?: string;
  protocolType?: string;
  activeVersionNo?: string;
  remark?: string;
  services?: ProductSnapshotServiceVO[];
}

/** 版本实体(对应 product_version 表)。 */
export interface ProductVersionResultVO {
  id?: string;
  productIdentification?: string;
  versionNo?: string;
  versionStatus?: number;
  productSnapshotJson?: string;
  publishStrategy?: number;
  canaryConfigJson?: string;
  publishTime?: string;
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
   * 当前已 echo:versionStatus / publishStrategy(字典),
   * createdBy / updatedBy(用户昵称),createdOrgId(组织名)。
   */
  echoMap?: Record<string, string>;
}

/** 分页查询参数。 */
export interface ProductVersionPageQuery {
  productIdentification?: string;
  versionNo?: string;
  versionStatus?: number;
  publishStrategy?: number;
}

/** 发布请求 VO。 */
export interface ProductVersionPublishVO {
  productIdentification: string;
  publishStrategy: number;
  canaryConfigJson?: string;
  publishRemark?: string;
}

/** 回滚请求 VO。 */
export interface ProductVersionRollbackVO {
  productIdentification: string;
  targetVersion: string;
  rollbackRemark?: string;
}

/** 历史清理请求 VO。 */
export interface ProductVersionPurgeVO {
  productIdentification: string;
  versionNo: string;
  purgeRemark?: string;
}

/** 版本差异 ── 字段级变更。 */
export interface ProductVersionFieldDiffVO {
  field: string;
  label?: string;
  changeType: 'ADDED' | 'REMOVED' | 'MODIFIED';
  before?: any;
  after?: any;
  /** 字典类型(供前端按值翻译为中文,无字典属性为 null);来自源字段 @Echo.dictType。 */
  dictType?: string;
}

/** 版本差异 ── 层级节点(递归,与物模型层级一致)。 */
export interface ProductVersionDiffNode {
  level: 'PRODUCT' | 'SERVICE' | 'PROPERTY' | 'COMMAND' | 'COMMAND_PARAM';
  paramKind?: 'REQUEST' | 'RESPONSE';
  code?: string;
  name?: string;
  changeType: 'ADDED' | 'REMOVED' | 'MODIFIED';
  fields?: ProductVersionFieldDiffVO[];
  children?: ProductVersionDiffNode[];
}

/** 版本差异摘要。 */
export interface ProductVersionDiffSummaryVO {
  productInfoChanged?: number;
  serviceAdded?: number;
  serviceRemoved?: number;
  serviceModified?: number;
  propertyAdded?: number;
  propertyRemoved?: number;
  propertyModified?: number;
  commandAdded?: number;
  commandRemoved?: number;
  commandModified?: number;
}

/** 版本完整差异。 */
export interface ProductVersionDiffVO {
  sourceVersion?: string;
  targetVersion: string;
  summary?: ProductVersionDiffSummaryVO;
  nodes?: ProductVersionDiffNode[];
  changeSummaryText?: string;
  changeType?: number;
}

/** 总览页统计指标(5 基础 + 2 建模治理)。 */
export interface ProductVersionStatisticsResultVO {
  productTotal?: number;
  publishedProductCount?: number;
  canaryProductCount?: number;
  unpublishedProductCount?: number;
  recentPublishCount7d?: number;
  /** 物模型服务数(建模深度) */
  thingModelServiceCount?: number;
  /** 发布版本总量 */
  publishedVersionTotal?: number;
}
