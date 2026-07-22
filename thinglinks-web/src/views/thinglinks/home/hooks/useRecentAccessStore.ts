import { computed, ref } from 'vue';
import type { RouteLocationNormalized } from 'vue-router';
import { useUserStoreWithOut } from '/@/store/modules/user';

export interface RecentAccessItem {
  path: string;
  name: string;
  icon?: string;
  visitedAt: number;
  /** 访问时所属应用 ID(用于跨应用点击时切应用 + 跳转,不存则视为当前应用) */
  applicationId?: string;
  /** 访问时所属应用 appKey */
  applicationKey?: string;
  /** 访问时所属应用名称 */
  applicationName?: string;
}

const MAX = 20;
const EXCLUDED_PATHS = new Set([
  '/',
  '/home/welcome',
  '/login',
  '/error',
  '/404',
  '/403',
  '/500',
]);
const ERROR_NAMES = new Set([
  'ErrorPage',
  'PageNotFound',
  'RedirectTo',
  'ErrorLogPage',
]);

function storageKey(userId?: string | number | null): string {
  return `THINGLINKS_RECENT_ACCESS__${userId ?? 'anon'}`;
}

function safeLoad(userId?: string | number | null): RecentAccessItem[] {
  try {
    const raw = localStorage.getItem(storageKey(userId));
    if (!raw) return [];
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr.filter((x) => x && x.path && x.name) : [];
  } catch {
    return [];
  }
}

function safeSave(userId: string | number | null | undefined, list: RecentAccessItem[]) {
  try {
    localStorage.setItem(storageKey(userId), JSON.stringify(list.slice(0, MAX)));
  } catch {
    /* ignore quota errors */
  }
}

const list = ref<RecentAccessItem[]>([]);
let hydratedFor: string | number | null | undefined = undefined;

function ensureHydrated(userId?: string | number | null) {
  if (hydratedFor === userId) return;
  hydratedFor = userId;
  list.value = safeLoad(userId);
}

function shouldSkip(to: RouteLocationNormalized): boolean {
  const path = to.path;
  if (!path) return true;
  if (EXCLUDED_PATHS.has(path)) return true;
  if (path.startsWith('/redirect')) return true;
  if (path.startsWith('/error')) return true;
  const meta: any = to.meta || {};
  if (meta.hideInMenu) return true;
  if (meta.ignoreAuth) return true;
  if (!meta.title) return true;
  const name = typeof to.name === 'string' ? to.name : '';
  if (name && ERROR_NAMES.has(name)) return true;
  return false;
}

function toItem(
  to: RouteLocationNormalized,
  appCtx: { id?: string; key?: string; name?: string } = {},
): RecentAccessItem | null {
  if (shouldSkip(to)) return null;
  const meta: any = to.meta || {};
  return {
    path: to.path,
    name: String(meta.title || to.name || to.path),
    icon: meta.icon ? String(meta.icon) : undefined,
    visitedAt: Date.now(),
    applicationId: appCtx.id || undefined,
    applicationKey: appCtx.key || undefined,
    applicationName: appCtx.name || undefined,
  };
}

export function useRecentAccessStore() {
  const userStore = useUserStoreWithOut();
  const userId = computed(() => userStore.getUserInfo?.id ?? null);

  ensureHydrated(userId.value);

  function record(to: RouteLocationNormalized) {
    ensureHydrated(userId.value);
    // 捕获访问时所属应用信息(支持跨应用点击时切应用)
    const item = toItem(to, {
      id: userStore.getApplicationId,
      key: userStore.getApplicationKey,
      name: userStore.getApplicationName,
    });
    if (!item) return;
    const next = [item, ...list.value.filter((x) => x.path !== item.path)].slice(0, MAX);
    list.value = next;
    safeSave(userId.value, next);
  }

  function clear() {
    list.value = [];
    safeSave(userId.value, []);
  }

  const items = computed(() => list.value);

  return { items, record, clear };
}
