/**
 * 产品物模型版本变更日志 TS 模型。
 *
 * 与后端 thinglinks-link-entity 中 com.mqttsnet.thinglinks.productversionchangelog.* 包对齐。
 * 变更日志为 append-only 资产变更审计流水,只读对外。
 */

/** 单条字段级变更明细(changeDetailJson parse 后数组项)。 */
export interface FieldChange {
  /** 字段名(英文标识)。 */
  field?: string;
  /** 字段中文标签。 */
  label?: string;
  /** 变更前值(新增时为 null)。 */
  before?: any;
  /** 变更后值(删除时为 null)。 */
  after?: any;
}

/** 变更日志(对应 product_version_change_log 表)。 */
export interface ProductVersionChangeLogResultVO {
  id?: string;
  /** 产品标识。 */
  productIdentification?: string;
  /** 本次变更累积进的版本序号(对应后端 versionNo,DB 列 version_no)。 */
  versionNo?: string;
  /** 变更类型(0-新增,1-编辑,2-删除)。 */
  changeType?: number;
  /** 变更维度(0-产品信息,1-服务,2-属性,3-命令)。 */
  targetType?: number;
  /** 变更摘要(人类可读)。 */
  changeSummary?: string;
  /** 字段级变更明细 JSON 字符串,parse 后为 FieldChange[]。 */
  changeDetailJson?: string;
  createdTime?: string;
  createdBy?: number;
  createdOrgId?: number;
  /**
   * 后端 @Echo 回填的中文展示值集合,key = 字段名。
   *
   * 当前已 echo:changeType(字典),createdBy(用户昵称),createdOrgId(组织名)。
   */
  echoMap?: Record<string, string>;
}

/** 分页查询参数。 */
export interface ProductVersionChangeLogPageQuery {
  productIdentification?: string;
  /** 版本序号过滤(对应后端 versionNo)。 */
  versionNo?: string;
  changeType?: number;
  targetType?: number;
}
