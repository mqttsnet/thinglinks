<template>
  <section class="home-shortcuts">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :md="12">
        <div class="home-panel">
          <div class="home-panel__header">
            <div class="home-section-title">
              <span class="home-section-title__bar" />
              <span>{{ t('workbench.shortcuts.recent') }}</span>
            </div>
          </div>
          <div class="home-panel__body">
            <a-empty
              v-if="!recent.length"
              :description="t('workbench.shortcuts.empty')"
              :image-style="{ height: '40px' }"
            />
            <div v-else class="home-shortcuts__list">
              <ShortcutItem
                v-for="sc in recent"
                :key="sc.key"
                :item="{ ...sc, pinned: isPinned(sc.path) }"
                @click="goto"
                @toggle-pin="togglePinned"
              />
            </div>
          </div>
        </div>
      </a-col>

      <a-col :xs="24" :md="12">
        <div class="home-panel">
          <div class="home-panel__header">
            <div class="home-section-title">
              <span class="home-section-title__bar" />
              <span>{{ t('workbench.shortcuts.pinned') }}</span>
            </div>
          </div>
          <div class="home-panel__body">
            <a-empty
              v-if="!pinned.length"
              :description="t('workbench.shortcuts.empty')"
              :image-style="{ height: '40px' }"
            />
            <div v-else class="home-shortcuts__list">
              <ShortcutItem
                v-for="sc in pinned"
                :key="sc.key"
                :item="sc"
                @click="goto"
                @toggle-pin="togglePinned"
              />
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import ShortcutItem from '../components/ShortcutItem.vue';
  import { useRecentAccess } from '../hooks/useRecentAccess';
  import { usePinnedShortcuts } from '../hooks/usePinnedShortcuts';
  import { useAppLauncher } from '../hooks/useAppLauncher';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { findMyApplication } from '/@/api/thinglinks/profile/userInfo';
  import type { ShortcutVO } from '../types';

  const { t } = useI18n();
  const router = useRouter();
  const userStore = useUserStore();
  const { recent } = useRecentAccess(8);
  const { pinned, has, toggle } = usePinnedShortcuts();
  const { launch } = useAppLauncher();
  const { createMessage } = useMessage();

  // 与 recentAccessGuard.ts 中的 ERROR_NAMES 对齐 ── 路由命中这些 name 视为 NotFound
  const ERROR_ROUTE_NAMES = new Set([
    'ErrorPage',
    'PageNotFound',
    'RedirectTo',
    'ErrorLogPage',
  ]);

  function isPinned(path: string) {
    return has(path);
  }

  /**
   * 判定路径在当前应用路由表里是否能解析到一个有效菜单.
   * <p>用于"菜单权限被管理员回收"场景:重新登录或切应用后,旧菜单已不在路由表里,
   * 此时 router.resolve 返回 matched=[] 或 name=NotFound,我们拦下给友好提示.
   * <p>同会话内被回收的菜单(还没刷新)无法检出 ── 那是后端 API 层的事,前端最多
   * 在请求被 403 后退化展示空数据,与此快捷入口判定无关.
   */
  function isRouteResolvable(path: string): boolean {
    try {
      const resolved = router.resolve(path);
      if (!resolved.matched?.length) return false;
      const name = typeof resolved.name === 'string' ? resolved.name : '';
      if (name && ERROR_ROUTE_NAMES.has(name)) return false;
      return true;
    } catch {
      return false;
    }
  }

  /**
   * 快捷入口点击跳转 ── 三分支策略,所有提示从用户视角写,告诉他下一步怎么做.
   *
   * <p>分支决策:
   * <ol>
   *   <li>明确同应用(applicationId === current)
   *       <ul>
   *         <li>router.resolve 命中 → push
   *         <li>命不中 → "菜单当前不可访问,可能权限有变更" (软提示,不武断地说"被回收")
   *       </ul>
   *   <li>老数据(无 applicationId)── 旧的收藏 / 最近访问没存应用上下文
   *       <ul>
   *         <li>当前应用能 resolve → push (兼容旧行为)
   *         <li>resolve 不到 → "该菜单不在当前应用「{app}」中,请通过右上角切换器切到对应应用"
   *             (重点引导用户切应用 ── 这是最高频原因,而非"权限被回收")
   *       </ul>
   *   <li>明确跨应用 → findMyApplication() 校验目标应用仍可访问 + 未过期,再委托 launch
   * </ol>
   */
  async function goto(item: ShortcutVO) {
    if (!item?.path) return;
    const targetAppId = item.applicationId;
    const currentAppId = userStore.getApplicationId;
    const currentAppName = userStore.getApplicationName || '';

    // —— 分支 1:明确同应用 ——
    if (targetAppId && targetAppId === currentAppId) {
      if (!isRouteResolvable(item.path)) {
        // 真同应用还 NotFound = 大概率是菜单下线 / 权限调整
        createMessage.info(
          t('workbench.shortcuts.permissionRevoked', { name: item.name }),
        );
        return;
      }
      router.push(item.path);
      return;
    }

    // —— 分支 2:老数据(没存 applicationId)── 这是用户最容易踩的坑 ——
    if (!targetAppId) {
      if (isRouteResolvable(item.path)) {
        // 当前应用碰巧能解析(可能本就同应用)→ 直接 push,无需多此一举
        router.push(item.path);
        return;
      }
      // 当前应用解析不到 → 99% 是属于其他应用的菜单,引导用户切应用
      createMessage.info(
        t('workbench.shortcuts.menuNotInCurrentApp', {
          name: item.name,
          app: currentAppName,
        }),
      );
      return;
    }

    // —— 分支 3:明确跨应用(targetAppId 存在且 !== currentAppId)——
    if (!item.applicationKey) {
      // 有 applicationId 但缺 appKey ── 不正常的老数据,降级到引导切应用
      createMessage.info(
        t('workbench.shortcuts.menuNotInCurrentApp', {
          name: item.name,
          app: currentAppName,
        }),
      );
      return;
    }

    // 拉最新应用列表校验目标应用仍可访问 + 未过期
    let apps;
    try {
      apps = (await findMyApplication()) || [];
    } catch {
      createMessage.error(t('workbench.shortcuts.appCheckFailed'));
      return;
    }

    const target = apps.find(
      (a: any) =>
        a.appKey === item.applicationKey || String(a.id ?? '') === String(targetAppId),
    );
    if (!target) {
      createMessage.info(
        t('workbench.shortcuts.appNotAccessible', { app: item.applicationName || '' }),
      );
      return;
    }
    if (target.expirationTime && new Date(target.expirationTime).getTime() < Date.now()) {
      createMessage.warning(
        t('workbench.shortcuts.appExpired', { app: target.name || item.applicationName || '' }),
      );
      return;
    }

    createMessage.info(t('workbench.shortcuts.switchingApp', { app: target.name || '' }));
    // 用最新 app 数据 launch ── 避免 stale 的 name/id 与后端不一致
    launch({ ...target, url: item.path });
  }

  function togglePinned(item: ShortcutVO) {
    toggle(item);
  }
</script>

<style lang="less" scoped>
  .home-shortcuts {
    margin-bottom: 20px;
  }

  .home-panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px 22px;
    min-height: 200px;
    height: 100%;
    display: flex;
    flex-direction: column;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .home-panel__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    /* 占位高度：让最近访问 / 我的收藏的卡片高度对齐，过多内容时本区域出现内部滚动条 */
    max-height: 360px;
    overflow-y: auto;
    /* 内部滚动条精简 */
    scrollbar-width: thin;
    scrollbar-color: rgba(20, 37, 66, 0.18) transparent;

    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(20, 37, 66, 0.18);
      border-radius: 3px;
    }
    &::-webkit-scrollbar-thumb:hover {
      background: rgba(20, 37, 66, 0.32);
    }

    :deep(.ant-empty) {
      margin: 0;
    }
  }

  .home-panel__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .home-section-title {
    display: inline-flex;
    align-items: baseline;
    gap: 8px;
    font-size: 15px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .home-section-title__bar {
    display: none;
  }

  /* 每行 2 个，自适应窄屏退化为单列 */
  .home-shortcuts__list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    align-content: start;
  }

  @media (max-width: 600px) {
    .home-shortcuts__list {
      grid-template-columns: 1fr;
    }
  }
</style>
