/**
 * AllOrCustomPicker ── 通用"全部 vs 自定义"卡片选择器类型契约。
 *
 * <p>设计目标:
 * <ul>
 *   <li>v-model 两态:'all'(对齐 BizConstant.ALL)或 (string|number)[] 自定义多选</li>
 *   <li>配置驱动 ── 任何带分页 API + 字段映射的领域都能用</li>
 *   <li>跨页保留 ── 选中态用 Set 维护 valueField,翻页/搜索不丢</li>
 *   <li>反查回显 ── modelValue 已有值但未在当前页 records 时,detailApi 自动反查 record 详情显示标签</li>
 *   <li>丰富 UI ── 卡片网格 / 已选面板 / 多形态触发器 / 内置过滤项 / 卡片插槽</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-05-08
 */

// ============================================================
// 值 / 模式
// ============================================================

/** Picker 的对外值 ── 'all' 字符串(全部模式)或数组(自定义模式) */
export type PickerValue = string | (string | number)[];

/** 单条 record 的原始 value 类型 */
export type PickerRawValue = string | number;

// ============================================================
// 字段配置
// ============================================================

/** 卡片副信息字段配置 ── 用于在卡片上显式展示关键信息 */
export interface PickerDescField {
  /** 已 i18n 后的标签文案 */
  label: string;
  /** 字段名;支持点号路径(如 'foo.bar') */
  field: string;
  /** 可选格式化器(常用于字典回显 / 时间格式化等) */
  formatter?: (raw: any, record: Record<string, any>) => string;
  /**
   * 副信息样式:
   * <ul>
   *   <li>'default' ── 普通文本</li>
   *   <li>'tag' ── 渲染为 Tag(适合状态等)</li>
   * </ul>
   */
  style?: 'default' | 'tag';
  /** style='tag' 时的颜色映射 */
  tagColor?: (raw: any, record: Record<string, any>) => string;
}

// ============================================================
// 顶部过滤项
// ============================================================

/** 弹窗顶部过滤项类型 */
export type PickerFilterType = 'select' | 'radio';

/** 顶部过滤项配置(可选 ── 让用户在搜索之外按维度过滤) */
export interface PickerFilter {
  /** 字段名(传给 pageApi 作为查询参数 key) */
  field: string;
  /** 显示标签 */
  label: string;
  /** 控件类型 */
  type?: PickerFilterType;
  /** 选项列表 */
  options: Array<{ label: string; value: any }>;
  /** 默认值 */
  defaultValue?: any;
  /** 是否允许清空 */
  allowClear?: boolean;
  /** 选项宽度(仅 select) */
  width?: number;
}

// ============================================================
// API 契约
// ============================================================

/** 调用 pageApi 时的标准参数(由 picker 内部组装,调用方在适配器里映射到自家 API) */
export interface PickerPageRequest {
  pageNum: number;
  pageSize: number;
  /**
   * 单字段搜索关键字 ── 当未配置 {@link PickerSearchField}[] 时使用,
   * 业务侧适配器把它映射到自家 API 默认搜索字段(如 productName).
   */
  keyword?: string;
  /**
   * 多字段搜索值 ── 当配置了 {@link PickerSearchField}[] 时使用,
   * key 是 PickerSearchField.field,value 是用户输入;业务侧适配器把它整体 merge 到 model.
   */
  searchValues?: Record<string, any>;
  /** 顶部过滤项的当前值(以 field 为 key) */
  filters?: Record<string, any>;
  /** 调用方传入的额外固定参数(pageParams) */
  extra?: Record<string, any>;
}

/**
 * 搜索栏字段配置 ── 配置后 PickerModal 渲染 N 个独立输入框,
 * 业务侧通过 {@link PickerPageRequest#searchValues} 拿到 {字段名 → 输入值} map 整体传给后端.
 * 不配置则退化为单 keyword 输入框,后端语义不变.
 */
export interface PickerSearchField {
  /** 后端查询字段名(同时作为 searchValues map 的 key) */
  field: string;
  /** 输入框 placeholder(已 i18n) */
  placeholder?: string;
  /** 输入框宽度,默认 200px */
  width?: number;
}

/** pageApi 的返回 ── 兼容多种 shape: {records,total} / {list,total} / 数组 */
export interface PickerPageResponse<T = any> {
  records?: T[];
  list?: T[];
  total?: number;
  data?: any;
}

/** detailApi:根据 value 列表反查 records;用于已选项跨页/反序列化时的回显 */
export type PickerDetailApi = (
  values: PickerRawValue[],
) => Promise<Array<Record<string, any>>>;

// ============================================================
// 触发器
// ============================================================

/**
 * 触发器展示形态:
 * <ul>
 *   <li>'tags' ── 显示已选 record 的 Tag 列表(默认,信息最丰富)</li>
 *   <li>'count' ── 仅显示统计文案(简洁)</li>
 *   <li>'compact' ── 紧凑模式,只一行 + 折叠</li>
 * </ul>
 */
export type PickerTriggerStyle = 'tags' | 'count' | 'compact';

/** 触发器自定义文案(可选;默认走 i18n 通用文案) */
export interface PickerTriggerLabels {
  /** 全部模式时显示 */
  all?: string;
  /** 自定义模式时显示前缀(默认: "已选 N 项") */
  custom?: (count: number) => string;
  /** 空时显示 */
  empty?: string;
  /** 触发按钮文字 */
  button?: string;
}

// ============================================================
// 暴露方法
// ============================================================

/** 父组件可通过 ref 调用的方法 */
export interface PickerExpose {
  /** 打开弹窗 */
  open: () => void;
  /** 关闭弹窗 */
  close: () => void;
  /** 清空选择 ── 重置为空数组 */
  clear: () => void;
  /** 设置为"全部"模式 */
  selectAll: () => void;
  /** 强制刷新列表 */
  reload: () => void;
}
