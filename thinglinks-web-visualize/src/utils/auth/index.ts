import { Persistent, BasicKeys } from '@/utils/cache/persistent';
import type { BasicStore } from '@/utils/cache/persistent';
import projectSetting from '@/settings/projectSetting';
import { CacheTypeEnum, TOKEN_KEY, TENANT_ID_KEY, APPLICATION_ID_KEY } from '@/enums/cacheEnum';

const { permissionCacheType } = projectSetting;
const isLocal = permissionCacheType === CacheTypeEnum.LOCAL;

export function getToken() {
  return String(getAuthCache(TOKEN_KEY) || '');
}

export function getTenantId() {
  return String(getAuthCache(TENANT_ID_KEY) || '');
}

export function getApplicationId() {
  return String(getAuthCache(APPLICATION_ID_KEY) || '');
}

export function getAuthCache<K extends BasicKeys>(key: K): Nullable<BasicStore[K]> {
  return isLocal
    ? Persistent.getLocal<BasicStore[K]>(key)
    : Persistent.getSession<BasicStore[K]>(key);
}

export function setAuthCache<K extends BasicKeys>(key: K, value: BasicStore[K]) {
  return isLocal
    ? Persistent.setLocal(key, value, true)
    : Persistent.setSession(key, value, true);
}

export function clearAuthCache(immediate = true) {
  const fn = isLocal ? Persistent.clearLocal : Persistent.clearSession;
  return fn(immediate);
}
