/**
 * 个人收藏菜单 —— 基于 localStorage 持久化，零后端成本。
 * 存储结构: { key, name, path, icon }[]
 */
import { computed, ref, watch } from 'vue';
import type { ShortcutVO } from '../types';

const STORAGE_KEY = 'thinglinks_pinned_shortcuts';

function read(): ShortcutVO[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((v) => v && v.path) : [];
  } catch {
    return [];
  }
}

function write(list: ShortcutVO[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  } catch {
    // ignore quota
  }
}

export function usePinnedShortcuts() {
  const list = ref<ShortcutVO[]>(read());

  watch(list, (v) => write(v), { deep: true });

  const pinned = computed<ShortcutVO[]>(() =>
    list.value.map((item) => ({ ...item, pinned: true })),
  );

  function has(path: string): boolean {
    return list.value.some((s) => s.path === path);
  }

  function pin(shortcut: ShortcutVO) {
    if (!shortcut?.path || has(shortcut.path)) return;
    list.value = [
      ...list.value,
      {
        key: shortcut.path,
        name: shortcut.name,
        path: shortcut.path,
        icon: shortcut.icon || 'ant-design:folder-outlined',
        // 保留应用上下文(否则收藏后切应用,点击仍 404)
        applicationId: shortcut.applicationId,
        applicationKey: shortcut.applicationKey,
        applicationName: shortcut.applicationName,
      },
    ];
  }

  function unpin(path: string) {
    list.value = list.value.filter((s) => s.path !== path);
  }

  function toggle(shortcut: ShortcutVO) {
    has(shortcut.path) ? unpin(shortcut.path) : pin(shortcut);
  }

  function clear() {
    list.value = [];
  }

  return { pinned, has, pin, unpin, toggle, clear };
}
