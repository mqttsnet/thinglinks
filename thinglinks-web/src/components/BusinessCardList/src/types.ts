import type { Component } from 'vue';

/**
 * 卡片字段定义
 */
export interface CardField {
  /** 显示标签 */
  label: string;
  /** 数据字段名 */
  field: string;
  /** 字典类型（自动通过 getDictLabel 翻译） */
  dictType?: string;
  /** 列宽（基于 24 栅格，默认 24=整行）。可配合其他字段组成一行（如两列各 12） */
  span?: number;
}

/**
 * 卡片操作按钮定义
 */
export interface CardAction {
  /** 按钮提示文本 */
  tooltip: string;
  /** 按钮图标（ant-design 图标名） */
  icon: string;
  /** 权限编码 */
  permission?: string;
  /** 点击事件名（触发 emit） */
  event: string;
  /** 图标尺寸（默认 16px） */
  iconSize?: number;
  /** 是否禁用该按钮（保持可见但不可点击；未定义默认可点击）。向后兼容的可选扩展字段 */
  disabled?: (record: Record<string, any>) => boolean;
  /** 操作色调。交互按钮使用系统按钮语义色,不直接写色值 */
  color?: 'primary' | 'success' | 'warning' | 'error';
}

/**
 * 权限配置
 */
export interface CardPermissions {
  add?: string;
  edit?: string;
  delete?: string;
  view?: string;
}

/**
 * BusinessCardList 组件 Props 类型
 */
export interface BusinessCardListProps {
  /** 分页查询 API（必须返回 { records, total }） */
  pageApi: (...args: any[]) => Promise<any>;
  /** 单条删除 API（传入 id） */
  deleteApi?: (id: any) => Promise<any>;
  /** 卡片标题 */
  title?: string;
  /** 搜索表单数据（由 BasicTable 传入） */
  searchData?: Record<string, any>;
  /** 卡片名称取自记录的哪个字段（默认 'name'） */
  nameField?: string;
  /** 卡片名称缺省文本 */
  nameFallback?: string;
  /** 卡片展示字段列表 */
  fields?: CardField[];
  /** 在线状态字段名（显示右下角圆点，不传则不显示） */
  statusField?: string;
  /** 代表"在线"的值（默认 true） */
  statusOnlineValue?: any;
  /** 在线状态标签文本 */
  statusOnlineLabel?: string;
  /** 离线状态标签文本 */
  statusOfflineLabel?: string;
  /** 多态状态解析器（可选）：返回 { label, cls } 渲染在线/离线/未连接等多态;不传则回退 statusOnlineValue 二态判定 */
  statusResolver?: (record: any) => { label: string; cls: string };
  /** 徽章/标签字段名（显示右上角 badge，不传则不显示） */
  badgeField?: string;
  /** 徽章的字典类型（用于翻译显示文本） */
  badgeDictType?: string;
  /** 权限编码集合 */
  permissions?: CardPermissions;
  /** 点击卡片图片跳转的路由名称 */
  detailRouteName?: string;
  /** 编辑弹窗组件（通过 useModal 内部注册） */
  editModal?: Component;
  /** 额外操作按钮定义 */
  extraActions?: CardAction[];
  /** 卡片布局变体。default 用于产品/设备/北向集成大卡片,model 用于物模型属性/命令紧凑卡片 */
  variant?: 'default' | 'model';
}
