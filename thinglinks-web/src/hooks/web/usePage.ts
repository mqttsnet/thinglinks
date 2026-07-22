import type { RouteLocationRaw, Router } from 'vue-router';

import { PageEnum } from '/@/enums/pageEnum';
import { unref, computed } from 'vue';

import { useRouter } from 'vue-router';
import { REDIRECT_NAME } from '/@/router/constant';

export type PathAsPageEnum<T> = T extends { path: string } ? T & { path: PageEnum } : T;
export type RouteLocationRawEx = PathAsPageEnum<RouteLocationRaw>;

function handleError(e: Error) {
  console.error(e);
}

/**
 * page switch
 */
export function useGo(_router?: Router) {
  const { push, replace } = _router || useRouter();
  function go(opt: RouteLocationRawEx = PageEnum.BASE_HOME, isReplace = false) {
    if (!opt) {
      return;
    }
    isReplace ? replace(opt).catch(handleError) : push(opt).catch(handleError);
  }
  return go;
}

/**
 * 动态查找当前页面对应的详情路由（:id 子路由）
 * 通过 router.getRoutes() 匹配 "当前路径/:id" 的路由，
 * 资源表改名后自动生效，无需硬编码路由名称。
 *
 * @example
 * const { detailRouteName, goDetail } = useDetailRoute();
 * // 在 handleView 中直接使用
 * goDetail(record.id);
 * // 或在 BusinessCardList 中传递
 * :detailRouteName="detailRouteName"
 */
export function useDetailRoute(_router?: Router) {
  const router = _router || useRouter();
  const { currentRoute, push } = router;

  const detailRouteName = computed(() => {
    const currentPath = unref(currentRoute)?.path;
    if (!currentPath) return undefined;
    const detailPath = currentPath.replace(/\/$/, '') + '/:id';
    const allRoutes = router.getRoutes();
    const detailRoute = allRoutes.find(
      (r) => r.path === detailPath || r.path === detailPath.replace(/\/+/g, '/'),
    );
    return (detailRoute?.name as string) || undefined;
  });

  function goDetail(id: string | number) {
    const name = unref(detailRouteName);
    if (name) {
      push({ name, params: { id } });
    }
  }

  return { detailRouteName, goDetail };
}

/**
 * @description: redo current page
 */
export const useRedo = (_router?: Router) => {
  const { replace, currentRoute } = _router || useRouter();
  const { query, params = {}, name, fullPath } = unref(currentRoute.value);
  function redo(): Promise<boolean> {
    return new Promise((resolve) => {
      if (name === REDIRECT_NAME) {
        resolve(false);
        return;
      }
      const redirectType = name && Object.keys(params).length > 0 ? 'name' : 'path';
      params['path'] = redirectType === 'name' ? String(name) : fullPath;
      // _redirect_type 仅经 history.state 传递(redirect/index.vue 从 history.state.params 读取),
      // 不放进路由 params:redirect 路由未声明该参数,vue-router 4.1+ 会丢弃未声明参数并告警。
      replace({
        name: REDIRECT_NAME,
        params,
        state: { params: { ...params, _redirect_type: redirectType } },
        query,
      }).then(() => resolve(true));
    });
  }
  return redo;
};
