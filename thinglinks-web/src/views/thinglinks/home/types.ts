/**
 * 通用首页公共类型定义。
 * 仅用于当前目录内部传参与响应解构，不跨目录使用。
 */

export interface MetricSparkPoint {
  label: string;
  value: number;
}

export interface MetricCardData {
  key: string;
  label: string;
  value: string | number;
  sub?: string;
  trend?: number; // 同比/环比百分比，正负带向
  sparkline?: MetricSparkPoint[];
  icon?: string;
  gradient?: string; // 渐变 token 名（'blue' | 'purple' | ...）
  live?: boolean; // 是否显示 Live 呼吸灯
}

export interface MessageItemVO {
  id: string | number;
  title: string;
  type?: 'info' | 'warning' | 'announcement' | 'security';
  createdTime?: string;
  unread?: boolean;
  url?: string;
}

export interface LogItemVO {
  id: string | number;
  title: string;
  action?: string;
  createdTime?: string;
  ip?: string;
  userAgent?: string;
  status?: 'success' | 'warning' | 'failed';
}

export interface ShortcutVO {
  key: string;
  name: string;
  path: string;
  icon?: string;
  recent?: boolean;
  pinned?: boolean;
  /** 访问 / 收藏时所属应用 ID(用于跨应用点击时切应用 + 跳转) */
  applicationId?: string;
  /** 访问 / 收藏时所属应用 appKey */
  applicationKey?: string;
  /** 访问 / 收藏时所属应用名称(用于切应用提示) */
  applicationName?: string;
}

export interface PlatformTileVO {
  key: string;
  title: string;
  subtitle?: string;
  icon: string;
  color: string;
  url: string;
  external?: boolean;
  /** 运行时替换 subtitle 的来源标记。'version' → 根产品清单中的组件版本与发行名称 */
  dynamicSubtitle?: 'version';
}
