/**
 * 当前应用守卫：若 userStore 当前的 appKey 已不在 findMyApplication 返回列表中
 * （例如该应用被管理员从租户处解绑），清空本地 appKey/Id/Name 以防卡住。
 */
import { useUserStore } from '/@/store/modules/user';

export function useAppStaleGuard() {
  const userStore = useUserStore();

  function ensure(appList: any[] | undefined | null) {
    if (!appList || !appList.length) return false;
    const currentKey = userStore.getApplicationKey;
    if (!currentKey) return false;
    const exists = appList.some((a) => a?.appKey === currentKey);
    if (exists) return false;
    userStore.setApplicationId('');
    userStore.setApplicationKey('');
    userStore.setApplicationName('');
    return true;
  }

  return { ensure };
}
