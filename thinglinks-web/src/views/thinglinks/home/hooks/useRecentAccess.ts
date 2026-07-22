/**
 * 最近访问菜单 —— 持久化存储，跨会话保留；
 * 优先读取 router.afterEach 落盘的 RecentAccessStore，
 * 列表为空时回退到当前打开的 multipleTab 以保证首次访问也有内容。
 */
import { computed } from 'vue';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';
import { useRecentAccessStore } from './useRecentAccessStore';
import type { ShortcutVO } from '../types';

const EXCLUDED_PATHS = new Set(['/', '/home/welcome', '/redirect', '/login']);

export function useRecentAccess(limit = 8) {
  const tabStore = useMultipleTabStore();
  const { items } = useRecentAccessStore();

  const recent = computed<ShortcutVO[]>(() => {
    const persisted = items.value;
    if (persisted.length) {
      return persisted.slice(0, limit).map((r) => ({
        key: r.path,
        name: r.name,
        path: r.path,
        icon: r.icon || 'ant-design:folder-outlined',
        recent: true,
        applicationId: r.applicationId,
        applicationKey: r.applicationKey,
        applicationName: r.applicationName,
      }));
    }

    const tabs = tabStore.getTabList || [];
    const list: ShortcutVO[] = [];
    const seen = new Set<string>();
    for (let i = tabs.length - 1; i >= 0 && list.length < limit; i--) {
      const tab: any = tabs[i];
      const path = tab?.path || tab?.fullPath;
      if (!path || EXCLUDED_PATHS.has(path) || seen.has(path)) continue;
      if (path.startsWith('/redirect/')) continue;
      seen.add(path);
      list.push({
        key: path,
        name: (tab?.meta?.title as string) || tab?.name || path,
        path,
        icon: (tab?.meta?.icon as string) || 'ant-design:folder-outlined',
        recent: true,
      });
    }
    return list;
  });

  return { recent };
}
