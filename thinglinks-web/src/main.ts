//import 'virtual:unocss-devtools';
//import '@/design/index.less';
import '/@/components/VxeTable/src/css/index.scss';
//import 'virtual:windi-utilities.css';

import 'virtual:windi-base.css';
import 'virtual:windi-components.css';
import '/@/design/index.less';
import 'virtual:windi-utilities.css';
import './assets/iconfont/iconfont.css';
// Register icon sprite
import 'virtual:svg-icons-register';
import App from './App.vue';
import { createApp } from 'vue';
import { initAppConfigStore } from '/@/logics/initAppConfig';
import { setupErrorHandle } from '/@/logics/error-handle';
import { router, setupRouter } from '/@/router';
import { setupRouterGuard } from '/@/router/guard';
import { setupStore } from '/@/store';
import { setupGlobDirectives } from '/@/directives';
import { setupI18n } from '/@/locales/setupI18n';
import { registerThirdComp } from '/@/settings/registerThirdComp';
import vuetify from './plugins/vuetify';

import Antd, { Card, Typography } from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';

// if (import.meta.env.DEV) {
//   import('ant-design-vue/dist/antd.less');
// }

import { isDevMode } from './utils/env';

import { useDictStoreWithOut } from './store/modules/dict';
import { DictEnum } from './enums/commonEnum';

if (isDevMode()) {
  import('ant-design-vue/es/style');
}

async function bootstrap() {
  const app = createApp(App);
  // 全局引入antd
  app.use(Antd);
  // 显式注册 Card / Typography:这些组件只在模板里用 <a-card> / <a-typography-*>,且未在入口按名 import,
  // 生产构建会把它们 tree-shaking 掉(app.use(Antd) 的全量 install 也保不住),运行时 resolveComponent 失败 →
  // 退化成原生元素(a-card 头部 title/#extra 丢失等)。显式 use 防止被摇树。dev 用 esbuild 预打包不受影响。
  app.use(Card);
  app.use(Typography);
  // Configure store
  // 配置 store
  setupStore(app);

  // Initialize internal system configuration
  // 初始化内部系统配置
  initAppConfigStore();

  // Multilingual configuration
  // 多语言配置
  // Asynchronous case: language files may be obtained from the server side
  // 异步案例：语言文件可能从服务器端获取
  await setupI18n(app);

  // Configure routing
  // 配置路由
  setupRouter(app);

  // router-guard
  // 路由守卫
  setupRouterGuard(router);

  // Register global directive
  // 注册全局指令
  setupGlobDirectives(app);

  // Configure global error handling
  // 配置全局错误处理
  setupErrorHandle(app);

  // 注册第三方组件
  registerThirdComp(app);

  // https://next.router.vuejs.org/api/#isready
  // await router.isReady();
  app.use(vuetify);

  const dictStore = useDictStoreWithOut();
  // dictStore.registerDictTypes([DictEnum.ACCESS_MODE, DictEnum.EDUCATION]);
  dictStore.registerDictTypes([...Object.values(DictEnum)]);
  dictStore.loadAllRegisteredDicts();

  app.mount('#app');
}

bootstrap();
