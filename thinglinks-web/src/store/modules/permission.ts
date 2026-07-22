import type { AppRouteRecordRaw, Menu } from '/@/router/types';

import { defineStore } from 'pinia';
import { store } from '/@/store';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStore } from './user';
import { useAppStoreWithOut } from './app';
import { computed, toRaw, unref } from 'vue';
import { flatMultiLevelRoutes, transformObjToRoute } from '/@/router/helper/routeHelper';
import { diagnoseRouteList } from '/@/router/helper/routeDiagnostics';
import { transformRouteToMenu } from '/@/router/helper/menuHelper';

import projectSetting from '/@/settings/projectSetting';
import { useGlobSetting } from '/@/hooks/setting';
import { PermissionModeEnum } from '/@/enums/appEnum';

import { AfterMyTenantRoutes, AfterRoutes, AfterVbenRoutes, asyncRoutes } from '/@/router/routes';
import { ERROR_LOG_ROUTE, PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';

import { filter } from '/@/utils/helper/treeHelper';

import { findResourceList } from '/@/api/thinglinks/common/oauth';
import { VisibleResourceVO } from '/@/api/thinglinks/common/model/menuModel';

import { useMessage } from '/@/hooks/web/useMessage';
import { PageEnum } from '/@/enums/pageEnum';

import { BeforeRoutes } from '/@/router/routes';
import { MenuModeEnum, MenuTypeEnum } from '/@/enums/menuEnum';

const globSetting = useGlobSetting();
const DEV_OPER_APP_ID = globSetting.devOperationApplicationId;

interface PermissionState {
  // Whether the route has been dynamically added
  // 路由是否动态添加
  isDynamicAddedRoute: boolean;
  // To trigger a menu update
  // 触发菜单更新
  lastBuildMenuTime: number;
  // Backstage menu list
  // 后台菜单列表
  backMenuList: Menu[];
  // 菜单列表
  frontMenuList: Menu[];
  // 权限
  visibleResource: VisibleResourceVO;
}

export const usePermissionStore = defineStore({
  id: 'app-permission',
  state: (): PermissionState => ({
    // Whether the route has been dynamically added
    // 路由是否动态添加
    isDynamicAddedRoute: false,
    // To trigger a menu update
    // 触发菜单更新
    lastBuildMenuTime: 0,
    // Backstage menu list
    // 后台菜单列表
    backMenuList: [],
    // menu List
    // 菜单列表
    frontMenuList: [],
    // 权限
    visibleResource: {} as VisibleResourceVO,
  }),
  getters: {
    getBackMenuList(): Menu[] {
      return this.backMenuList;
    },
    getFrontMenuList(): Menu[] {
      return this.frontMenuList;
    },
    getLastBuildMenuTime(): number {
      return this.lastBuildMenuTime;
    },
    getIsDynamicAddedRoute(): boolean {
      return this.isDynamicAddedRoute;
    },
    getVisibleResource(): VisibleResourceVO {
      return this.visibleResource;
    },
  },
  actions: {
    setBackMenuList(list: Menu[]) {
      this.backMenuList = list;
      list?.length > 0 && this.setLastBuildMenuTime();
    },

    setFrontMenuList(list: Menu[]) {
      this.frontMenuList = list;
    },

    setLastBuildMenuTime() {
      this.lastBuildMenuTime = new Date().getTime();
    },

    setDynamicAddedRoute(added: boolean) {
      this.isDynamicAddedRoute = added;
    },

    setVisibleResource(visibleResource: VisibleResourceVO) {
      this.visibleResource = visibleResource;
    },

    resetState(): void {
      this.isDynamicAddedRoute = false;
      this.backMenuList = [];
      this.lastBuildMenuTime = 0;
      this.visibleResource = {} as VisibleResourceVO;
    },
    // 加载资源
    async changePermissionCode(): Promise<AppRouteRecordRaw[]> {
      const userStore = useUserStore();
      const appStore = useAppStoreWithOut();
      const applicationId = userStore.getApplicationId;
      const getMenuMode = computed(() => appStore.getMenuSetting.mode);
      const getMenuType = computed(() => appStore.getMenuSetting.type);
      const getSplit = computed(() => appStore.getMenuSetting.split);

      const isMixModeAndSplit = computed(() => {
        return (
          unref(getMenuMode) === MenuModeEnum.INLINE &&
          unref(getMenuType) === MenuTypeEnum.MIX &&
          unref(getSplit)
        );
      });
      let visibleResource = {} as VisibleResourceVO;
      if (unref(isMixModeAndSplit)) {
        visibleResource = await findResourceList();
      } else {
        visibleResource = await findResourceList(applicationId);
      }

      this.setVisibleResource(visibleResource);
      // 把后端下发的角色码同步到 userStore.roleList，供首页/权限判断使用
      useUserStore().setRoleList((visibleResource.roleList as any) || []);
      return visibleResource.routerList;
    },

    // 构建路由
    async buildRoutesAction(): Promise<AppRouteRecordRaw[]> {
      const { t } = useI18n();
      const userStore = useUserStore();

      const appStore = useAppStoreWithOut();

      let routes: AppRouteRecordRaw[] = [];
      const roleList = toRaw(userStore.getRoleList) || [];
      const { permissionMode = projectSetting.permissionMode } = appStore.getProjectConfig;

      // 路由过滤器 在 函数filter 作为回调传入遍历使用
      const routeFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        // 抽出角色
        const { roles } = meta || {};
        if (!roles) return true;
        // 进行角色权限判断
        return roleList.some((role) => roles.includes(role));
      };

      const routeRemoveIgnoreFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        // ignoreRoute 为true 则路由仅用于菜单生成，不会在实际的路由表中出现
        const { ignoreRoute } = meta || {};
        // arr.filter 返回 true 表示该元素通过测试
        return !ignoreRoute;
      };

      /**
       * @description 根据设置的首页path，修正routes中的affix标记（固定首页）
       * */
      const patchHomeAffix = (routes: AppRouteRecordRaw[]) => {
        if (!routes || routes.length === 0) return;
        let homePath: string = PageEnum.BASE_HOME;

        function patcher(routes: AppRouteRecordRaw[], parentPath = '') {
          if (parentPath) parentPath = parentPath + '/';
          routes.forEach((route: AppRouteRecordRaw) => {
            const { path, children, redirect } = route;
            const currentPath = path.startsWith('/') ? path : parentPath + path;
            if (currentPath === homePath) {
              if (redirect) {
                homePath = route.redirect! as string;
              } else {
                route.meta = Object.assign({}, route.meta, { affix: true });
                throw new Error('end');
              }
            }
            children && children.length > 0 && patcher(children, currentPath);
          });
        }

        try {
          patcher(routes);
        } catch (e) {
          // 已处理完毕跳出循环
        }
        return;
      };

      switch (permissionMode) {
        // 角色权限
        case PermissionModeEnum.ROLE:
          // 对非一级路由进行过滤
          routes = filter(asyncRoutes, routeFilter);
          // 对一级路由根据角色权限过滤
          routes = routes.filter(routeFilter);
          // Convert multi-level routing to level 2 routing
          // 将多级路由转换为 2 级路由
          routes = flatMultiLevelRoutes(routes);
          break;

        // 路由映射， 默认进入该case
        case PermissionModeEnum.ROUTE_MAPPING:
          // 对非一级路由进行过滤
          routes = filter(asyncRoutes, routeFilter);
          // 对一级路由再次根据角色权限过滤
          routes = routes.filter(routeFilter);
          // 将路由转换成菜单
          const menuList = transformRouteToMenu(routes, true);
          // 移除掉 ignoreRoute: true 的路由 非一级路由
          routes = filter(routes, routeRemoveIgnoreFilter);
          // 移除掉 ignoreRoute: true 的路由 一级路由；
          routes = routes.filter(routeRemoveIgnoreFilter);
          // 对菜单进行排序
          menuList.sort((a, b) => {
            return (a.meta?.orderNo || 0) - (b.meta?.orderNo || 0);
          });

          // 设置菜单列表
          this.setFrontMenuList(menuList);

          // Convert multi-level routing to level 2 routing
          // 将多级路由转换为 2 级路由
          routes = flatMultiLevelRoutes(routes);
          break;

        //  If you are sure that you do not need to do background dynamic permissions, please comment the entire judgment below
        //  如果确定不需要做后台动态权限，请在下方注释整个判断
        case PermissionModeEnum.BACK:
          const { createMessage } = useMessage();

          createMessage.loading({
            content: t('sys.app.menuLoading'),
            duration: 1,
          });

          // 当前应用
          const applicationId = userStore.getApplicationId;
          // !Simulate to obtain permission codes from the background,
          // 模拟从后台获取权限码，
          // this function may only need to be executed once, and the actual project can be put at the right time by itself
          // 这个功能可能只需要执行一次，实际项目可以自己放在合适的时间
          let routeList: AppRouteRecordRaw[] = [];
          try {
            routeList = await this.changePermissionCode();
          } catch (error) {
            console.error(error);
          }

          // 0. 健康检查：把"缺 name / 缺 title / 缺 code / 重复"等配置异常打到控制台
          diagnoseRouteList(routeList);

          // 1. 动态引入组件
          routeList = transformObjToRoute(routeList);

          const isDevOper = applicationId === DEV_OPER_APP_ID; // 开发运营系统才显示 vben官方的静态示例
          // 后台路由(routeList) + 前端写死的路由(BeforeRoutes、AfterRoutes、AfterMyTenantRoutes)
          const afterRouteList = [
            ...AfterRoutes,
            ...AfterMyTenantRoutes,
            ...(isDevOper ? AfterVbenRoutes : []),
          ];

          // 2. 添加前端静态路由
          routeList = [...BeforeRoutes, ...routeList, ...afterRouteList];

          // 3. 后台路由转菜单结构
          const backMenuList = transformRouteToMenu(routeList);
          this.setBackMenuList(backMenuList);

          // remove meta.ignoreRoute item
          // 4. 删除 meta.ignoreRoute 项
          routeList = filter(routeList, routeRemoveIgnoreFilter);
          routeList = routeList.filter(routeRemoveIgnoreFilter);

          // 5. 将多级路由转换为 2 级路由
          routeList = flatMultiLevelRoutes(routeList);
          routes = [PAGE_NOT_FOUND_ROUTE, ...routeList];
          break;
      }

      routes.push(ERROR_LOG_ROUTE);
      patchHomeAffix(routes);
      return routes;
    },
  },
});

// Need to be used outside the setup
// 需要在设置之外使用
export function usePermissionStoreWithOut() {
  return usePermissionStore(store);
}
