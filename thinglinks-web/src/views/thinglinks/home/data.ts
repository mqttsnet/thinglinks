/**
 * 通用首页静态字典与常量配置。
 * 新版聚焦 SaaS 平台门面（品牌/渐变/快捷默认/平台瓷砖/问候），
 * 老 workbench 遗留数据（navItems / dynamicInfoItems / groupItems）已删。
 */
import type { PlatformTileVO, ShortcutVO } from './types';

/** 按 appKey 映射的 Flexy 渐变色板（Logo 底座 / Card Accent） */
export const APP_GRADIENT_MAP: Record<string, { gradient: string; color: string; textColor: string }> = {
  basicPlatform: {
    gradient: 'linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%)',
    color: '#5d87ff',
    textColor: '#2a6df4',
  },
  iotSystem: {
    gradient: 'linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%)',
    color: '#7c5cfc',
    textColor: '#5b3adb',
  },
  videoSystem: {
    gradient: 'linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%)',
    color: '#13deb9',
    textColor: '#0bb783',
  },
  iotCardSystem: {
    gradient: 'linear-gradient(135deg, #fff3d6 0%, #ffd79a 100%)',
    color: '#ffae1f',
    textColor: '#c98a10',
  },
  visualizationSystem: {
    gradient: 'linear-gradient(135deg, #ffe4ea 0%, #fbb6c2 100%)',
    color: '#fa5c7c',
    textColor: '#d03b5b',
  },
  devOperation: {
    gradient: 'linear-gradient(135deg, #d6e4ff 0%, #91b9ff 100%)',
    color: '#3a66e8',
    textColor: '#224ed3',
  },
  iotMobile: {
    gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    color: '#e85d3a',
    textColor: '#c04220',
  },
  _default: {
    gradient: 'linear-gradient(135deg, #ebeef5 0%, #c0c4cc 100%)',
    color: '#8c97a5',
    textColor: '#5a6673',
  },
};

/** 按时间返回 appKey 对应的渐变配置（找不到走 _default） */
export function getAppGradient(appKey?: string | null) {
  if (!appKey) return APP_GRADIENT_MAP._default;
  return APP_GRADIENT_MAP[appKey] ?? APP_GRADIENT_MAP._default;
}

/** 时间段问候字典 — 值为 i18n key，组件里 t(key) 翻译 */
export const GREETINGS = {
  earlyMorning: 'workbench.greeting.earlyMorning',
  morning: 'workbench.greeting.morning',
  forenoon: 'workbench.greeting.forenoon',
  noon: 'workbench.greeting.noon',
  afternoon: 'workbench.greeting.afternoon',
  evening: 'workbench.greeting.evening',
  night: 'workbench.greeting.night',
};

/** 平台信息瓷砖 — title/subtitle 为 i18n key；subtitle 可由 dynamicSubtitle 运行时覆盖 */
export const PLATFORM_TILES: PlatformTileVO[] = [
  {
    key: 'version',
    title: 'workbench.platform.version.title',
    subtitle: 'workbench.platform.version.subtitle',
    icon: 'ant-design:appstore-outlined',
    color: '#5d87ff',
    url: 'https://mqttsnet.com',
    external: true,
    dynamicSubtitle: 'version',
  },
  {
    key: 'docs',
    title: 'workbench.platform.docs.title',
    subtitle: 'workbench.platform.docs.subtitle',
    icon: 'ant-design:book-outlined',
    color: '#13deb9',
    url: 'https://mqttsnet.yuque.com/trgbro/thinglinks',
    external: true,
  },
  {
    key: 'github',
    title: 'workbench.platform.github.title',
    subtitle: 'workbench.platform.github.subtitle',
    icon: 'ant-design:github-filled',
    color: '#24292e',
    url: 'https://github.com/mqttsnet/thinglinks',
    external: true,
  },
  {
    key: 'support',
    title: 'workbench.platform.support.title',
    subtitle: 'workbench.platform.support.subtitle',
    icon: 'ant-design:customer-service-outlined',
    color: '#ffae1f',
    url: 'https://github.com/mqttsnet/thinglinks/issues',
    external: true,
  },
];

/** 默认快捷入口 — name 为 i18n key */
export const DEFAULT_SHORTCUTS: ShortcutVO[] = [
  {
    key: 'profile',
    name: 'workbench.shortcut.profile',
    path: '/profile',
    icon: 'ant-design:user-outlined',
  },
  {
    key: 'tenant',
    name: 'workbench.shortcut.tenant',
    path: '/tenant/tenant',
    icon: 'ant-design:group-outlined',
  },
  {
    key: 'application',
    name: 'workbench.shortcut.application',
    path: '/application/application',
    icon: 'ant-design:appstore-add-outlined',
  },
  {
    key: 'videoDevice',
    name: 'workbench.shortcut.videoDevice',
    path: '/device/videoDevice',
    icon: 'ant-design:video-camera-outlined',
  },
];

/** 品牌 slogan i18n key */
export const BRAND_SLOGAN_KEY = 'workbench.brand.slogan';
export const BRAND_TAGLINE_KEY = 'workbench.brand.tagline';

/** 系统运行状态（text 为 i18n key） */
export const SYSTEM_STATUS = {
  operational: { text: 'workbench.system.operational', color: '#13deb9' },
  degraded: { text: 'workbench.system.degraded', color: '#ffae1f' },
  outage: { text: 'workbench.system.outage', color: '#fa5c7c' },
};
