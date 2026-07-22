import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';
import { LAYOUT, DOC_LAYOUT } from '/@/router/constant';

// import.meta.glob('*', { eager: true }) 直接引入所有的模块 Vite 独有的功能
const modules = import.meta.glob('./modules/**/*.ts', { eager: true });
const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

// 根路由
export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const DocMenu1: AppRouteModule[] = [
  {
    name: '首页',
    path: '/welcome/index',
    component: () => import('/@/views/open/welcome/index.vue'),
    meta: {
      title: '首页',
      ignoreAuth: true,
    },
  },
];

export const DocMenu2: AppRouteModule[] = [
  {
    name: '开放文档',
    path: '/doc/api',
    component: () => import('/@/views/open/doc/index.vue'),
    meta: {
      title: '开放文档',
      ignoreAuth: true,
    },
  },
  {
    name: '帮助文档',
    path: '/doc/help',
    component: () => import('/@/views/open/help/index.vue'),
    meta: {
      title: '帮助文档',
      ignoreAuth: true,
    },
  },
  {
    name: '签名算法',
    path: '/doc/sign',
    component: () => import('/@/views/open/sign/index.vue'),
    meta: {
      title: '签名算法',
      ignoreAuth: true,
    },
  },

  {
    name: '全局响应码',
    path: '/doc/code',
    component: () => import('/@/views/open/code/index.vue'),
    meta: {
      title: '全局响应码',
      ignoreAuth: true,
    },
  },
];

export const LoginRoute: AppRouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('/@/views/sys/login/Login.vue'),
    meta: {
      title: t('routes.basic.login'),
    },
  },
  {
    meta: {
      title: '首页',
      icon: 'ep:document',
    },
    name: '根',
    path: '/welcome',
    redirect: '/welcome/index',
    component: DOC_LAYOUT,
    children: DocMenu1,
  },
  {
    meta: {
      title: 'API文档',
      icon: 'ep:document',
    },
    name: 'doc',
    path: '/doc',
    redirect: '/doc/api',
    component: DOC_LAYOUT,
    children: DocMenu2,
  },
];

/**
 * 拼接在后端路由之前的前端路由
 */
export const BeforeRoutes: AppRouteRecordRaw[] = [
  {
    path: '/home',
    name: 'Home',
    component: LAYOUT,
    redirect: '/home/welcome',
    meta: {
      icon: 'bx:bx-home',
      hideMenu: true,
      title: t('routes.dashboard.welcome'),
    },
    children: [
      {
        path: 'welcome',
        name: 'Welcome',
        component: () => import('/@/views/thinglinks/home/index.vue'),
        meta: {
          title: t('routes.dashboard.welcome'),
          affix: true,
          icon: 'bx:bx-home',
        },
      },
    ],
  },
  {
    path: '/profile',
    name: 'profile',
    component: LAYOUT,
    redirect: '/profile/index',
    meta: {
      title: t('layout.header.dropdownProfile'),
      hideMenu: true,
    },
    children: [
      {
        path: 'index',
        name: 'profileIndex',
        component: () => import('/@/views/thinglinks/profile/index.vue'),
        meta: {
          title: t('layout.header.dropdownProfile'),
          hideMenu: true,
        },
      },
    ],
  },
  {
    path: '/video',
    name: 'VideoAppHome',
    component: LAYOUT,
    redirect: '/video/home',
    meta: {
      title: t('video.home.title'),
      hideMenu: true,
    },
    children: [
      {
        path: 'home',
        name: 'VideoHome',
        component: () => import('/@/views/video/home/index.vue'),
        meta: {
          title: t('video.home.title'),
          hideMenu: true,
          icon: 'ant-design:video-camera-outlined',
        },
      },
    ],
  },
];

/**
 * 拼接在后端路由之后的前端路由
 */
export const AfterMyTenantRoutes: AppRouteModule[] = [
  {
    path: '/myTenant',
    name: 'myTenant',
    component: LAYOUT,
    redirect: '/myTenant/info',
    meta: {
      title: '我的企业',
      hideChildrenInMenu: true,
      hideMenu: true,
      icon: 'ant-design:group-outlined',
    },
    children: [
      {
        path: 'info',
        name: 'myTenantInfo',
        component: () => import('/@/views/basic/myTenant/index.vue'),
        meta: {
          title: '我的企业',
          hideMenu: true,
          icon: 'ant-design:group-outlined',
        },
      },
    ],
  },
];
export const AfterVbenRoutes: AppRouteModule[] = [
  {
    path: '/vben',
    name: '静态示例',
    component: LAYOUT,
    meta: {
      icon: 'ant-design:table-outlined',
      title: '静态示例',
    },
    redirect: '/about/index',
    children: routeModuleList,
  },
];
// 后台动态路由之后的路由
export const AfterRoutes: AppRouteModule[] = [];

// Basic routing without permission
// 未经许可的基本路由
export const basicRoutes = [
  ...LoginRoute,
  RootRoute,
  ...mainOutRoutes,
  REDIRECT_ROUTE,
  PAGE_NOT_FOUND_ROUTE,
];
