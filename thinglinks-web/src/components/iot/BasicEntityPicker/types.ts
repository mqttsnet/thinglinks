/**
 * BasicEntityPicker - 通用业务实体选择器(单选/多选,卡片+分页+搜索)。
 *
 * <p>跟 {@link import('../AllOrCustomPicker/types').PickerValue AllOrCustomPicker}
 * 的区别:本组件**不带"全部/自定义"切换语义**,纯单选/多选场景使用,业务字段类型直接对应:
 * <ul>
 *   <li>单选(multiple=false): {@code v-model: string} ── 业务字段就是 string id,无需适配</li>
 *   <li>多选(multiple=true):  {@code v-model: string[]} ── 业务字段就是 id 数组</li>
 * </ul>
 * 用于 ACL 规则、设备命令、规则联动等"必须选具体实体"的场景(无"全部"语义)。
 *
 * @author mqttsnet
 */

/** v-model 单条原值 */
export type EntityRawValue = string | number;

/** v-model 整体值(单选 string,多选 string[]) */
export type EntityValue = EntityRawValue | EntityRawValue[] | null | undefined;

/** 卡片副信息字段(显示在每张卡片上) */
export interface EntityDescField {
  /** 字段路径(支持 a.b.c 嵌套读取) */
  field: string;
  /** 显示标签 */
  label: string;
  /** 字典翻译类型(可选) */
  dictType?: string;
  /** 自定义渲染(优先级最高) */
  formatter?: (record: any) => string;
}

/** 顶部过滤器(modal 内,在搜索框右边显示) */
export interface EntityFilter {
  /** 后端字段名(传给 pageApi.filters) */
  field: string;
  /** 显示标签 */
  label: string;
  /** 控件类型 */
  type: 'select' | 'input';
  /** select 的选项(可选) */
  options?: { label: string; value: any }[];
  /** 字典翻译(优先级低于 options) */
  dictType?: string;
  /** placeholder */
  placeholder?: string;
  /** 占位宽度(px,默认 160) */
  width?: number;
}

/** pageApi 入参 */
export interface EntityPageRequest {
  /** 当前页码(从 1 开始) */
  pageNum: number;
  /** 每页大小 */
  pageSize: number;
  /** 搜索关键字(对应 searchField) */
  keyword?: string;
  /** 顶部过滤器值 */
  filters?: Record<string, any>;
  /** 调用方传入的固定参数(从 pageParams prop 透传) */
  extra?: Record<string, any>;
}

/** pageApi 返回值(适配项目原生 PageResult 即可) */
export interface EntityPageResponse {
  records: any[];
  total: number;
}

/** detailApi 反查接口(用于 modelValue 已有值时的回显) */
export type EntityDetailApi = (values: EntityRawValue[]) => Promise<any[]>;

/** 触发器形态 */
export type EntityTriggerStyle = 'input' | 'tags';

/** 触发器自定义文案 */
export interface EntityTriggerLabels {
  /** 空选时占位 */
  empty?: string;
  /** 触发按钮文案 */
  button?: string;
}

/** 暴露的 imperative API */
export interface EntityPickerExpose {
  open: () => void;
  close: () => void;
  clear: () => void;
}
