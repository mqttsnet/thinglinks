/**
 * 应用启动器 —— 所有应用卡/下拉切换点击都走这里。
 *
 * 按 `def_application.url` 字段分发：
 * - `http(s)://` 外链 → window.open 新窗口，不动本地状态
 * - `/` 开头内部路径 → 切 appKey + 整页重载（清 tabs、重建路由、重新拉菜单）
 * - 裸路径 → 自动补 '/' 后同上
 * - 空 → 切 appKey + 整页重载到 /home/welcome
 *
 * 为什么整页重载（而不是 router.push + changePermissionCode）：
 * - 后端动态菜单是在路由守卫里按 applicationId 拉取并 addRoute 注册的，只切
 *   `changePermissionCode` 不会重建 router 和左侧菜单 computed，导致"看起来
 *   没切"。现有的「切换企业」(switchTenantAndOrg) 就是靠 location.reload 解决同
 *   样问题，这里对齐它的做法。
 *
 * 所有判定主键为 appKey，不依赖数字 id。
 */
import { useUserStore } from '/@/store/modules/user';
import { usePermissionStore } from '/@/store/modules/permission';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { router } from '/@/router';
import type { DefApplicationResultVO } from '/@/api/thinglinks/profile/model/defApplicationModel';

const FALLBACK_HOME = '/home/welcome';

export function useAppLauncher() {
  const userStore = useUserStore();
  const permissionStore = usePermissionStore();
  const tabStore = useMultipleTabStore();
  const { createMessage } = useMessage();
  const { t } = useI18n();

  function isExternalUrl(u: string): boolean {
    return /^https?:\/\//i.test(u);
  }

  /**
   * 把后端给的 url 归一成 Vue Router 可识别的内部路径（形如 "/xxx"）。
   * 容错：去掉前导的 #（后台手滑可能会输成 "#/home/welcome"）、空白、多余 /。
   */
  function normalizeInternalPath(u: string): string {
    if (!u) return '';
    let v = u.trim();
    // 去掉所有前导 #，防止最终出现 /#/#/xxx 这样的双 hash
    while (v.startsWith('#')) v = v.slice(1);
    if (!v) return '';
    return v.startsWith('/') ? v : `/${v}`;
  }

  async function launch(app: DefApplicationResultVO | any): Promise<void> {
    if (!app?.appKey) {
      createMessage.error(t('workbench.launcher.invalidApp'));
      return;
    }

    const url = (app.url || '').trim();

    // 1. 外链：新窗口，不切本地状态
    if (isExternalUrl(url)) {
      window.open(url, '_blank', 'noopener,noreferrer');
      createMessage.info(t('workbench.launcher.openedInNewTab'));
      return;
    }

    // 2. 内链/空：持久化 appKey → 整页重载 → permissionGuard 自动按新 applicationId 拉菜单
    const target = url ? normalizeInternalPath(url) : FALLBACK_HOME;

    try {
      userStore.setApplicationId(app.id ?? '');
      userStore.setApplicationKey(app.appKey);
      userStore.setApplicationName(app.name ?? '');

      // 清旧应用的 tabs 和权限缓存，避免刷新后残留旧状态
      tabStore.resetState();
      permissionStore.resetState();
    } catch (e) {
      createMessage.error(t('workbench.launcher.switchFailed'));
      return;
    }

    // 整页重载到目标路径（触发路由守卫重新拉取菜单 + 注册动态路由）。
    //
    // 关键约束：必须**强制** location.reload()，不能只 location.href = href。
    // 因为 href 与当前 URL 相同时（两个应用恰好都映射到 /home/welcome，或
    // hash 模式下相同 hash），浏览器把 href 赋值视为 no-op 不重载，导致
    // setApplicationId 已写入但菜单仍按旧 applicationId 渲染（用户表现为
    // "来回切应用菜单不变"）。
    //
    // 模式参照已有 switchTenantAndOrg：router.replace 先把当前路由切到目标
    // 路径，再 setTimeout 让 pinia 持久化和路由切换完成 → 然后无条件 reload。
    // 200ms 是与 switchTenantAndOrg 对齐的稳态等待，足以覆盖 IndexedDB /
    // localStorage 同步窗口。
    try {
      await router.replace(target);
    } catch {
      try {
        await router.replace(FALLBACK_HOME);
      } catch {
        /* swallow: 即使路由切换失败，下面的 reload 仍会重新拉菜单 */
      }
    }
    setTimeout(() => {
      window.location.reload();
    }, 200);
  }

  return { launch, isExternalUrl };
}
