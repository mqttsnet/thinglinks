import type { UserConfig, ConfigEnv } from 'vite';
import pkg from './package.json';
import dayjs from 'dayjs';
import { loadEnv } from 'vite';
import { resolve } from 'path';
import { generateModifyVars } from './build/generate/generateModifyVars';
import { createProxy } from './build/vite/proxy';
import { wrapperEnv } from './build/utils';
import { createVitePlugins } from './build/vite/plugin';
import { OUTPUT_DIR } from './build/constant';
import { include, exclude } from './build/vite/optimize';
import { loadProductConfig, toPublicProductInfo, toViteProductEnv } from './build/productConfig';

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir);
}

const { dependencies, devDependencies, name, version } = pkg;
const __APP_INFO__ = {
  pkg: { dependencies, devDependencies, name, version },
  lastBuildTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
};

export default ({ command, mode }: ConfigEnv): UserConfig => {
  const root = process.cwd();

  const productConfig = loadProductConfig(root);
  const productEnv = toViteProductEnv(productConfig);
  const env = { ...loadEnv(mode, root), ...productEnv };

  // The boolean type read by loadEnv is a string. This function can be converted to boolean type
  const viteEnv = wrapperEnv(env);

  const { VITE_PORT, VITE_PUBLIC_PATH, VITE_PROXY, VITE_DROP_CONSOLE } = viteEnv;

  // 构建日志字段：模式、根目录、组件版本和发行编码。
  console.log(
    'mode=%s, root=%s, product=%s@%s, edition=%s',
    mode,
    root,
    productConfig.componentCode,
    productConfig.componentVersion,
    productConfig.editionCode,
  );
  const isBuild = command === 'build';

  return {
    // 在生产中服务时的基本公共路径
    base: VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias: [
        {
          find: 'vue-i18n',
          replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
        },
        // /@/xxxx => src/xxxx
        {
          find: /\/@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /\/#\//,
          replacement: pathResolve('types') + '/',
        },
      ],
    },
    server: {
      https: false,
      // Listening on all local IPs
      host: true,
      port: VITE_PORT,
      // Load proxy configuration from .env
      proxy: createProxy(VITE_PROXY),
    },
    esbuild: {
      pure: VITE_DROP_CONSOLE ? ['console.log', 'debugger'] : [],
    },
    build: {
      target: 'es2015',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      // minify: 'terser',
      // terserOptions: {
      //   compress: {
      //     keep_infinity: true,
      //     // Used to delete console in production environment
      //     drop_console: VITE_DROP_CONSOLE,
      //   },
      // },
      // Turning off brotliSize display can slightly reduce packaging time
      reportCompressedSize: false,
      chunkSizeWarningLimit: 2000,
    },
    define: {
      // setting vue-i18-next
      // Suppress warning
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__),
      __THINGLINKS_PRODUCT_INFO__: JSON.stringify(toPublicProductInfo(productConfig)),
      'import.meta.env.VITE_GLOB_CLIENT_ID': JSON.stringify(productEnv.VITE_GLOB_CLIENT_ID),
      //新增以下变量
      __COLOR_PLUGIN_OUTPUT_FILE_NAME__: undefined,
      __PROD__: true,
      __COLOR_PLUGIN_OPTIONS__: {},
    },

    css: {
      preprocessorOptions: {
        less: {
          modifyVars: generateModifyVars(),
          javascriptEnabled: true,
        },
        // vxe-table 4.3.5 的 scss 用了新版 Dart Sass 已弃用的 lighten()/darken()/@import,
        // 静音第三方依赖(node_modules)的弃用告警,避免每次编译刷屏 225+ 条;
        // 仅作用于依赖,本项目自有 scss 的告警照常提示。
        scss: {
          quietDeps: true,
          silenceDeprecations: ['import', 'global-builtin', 'color-functions', 'legacy-js-api'],
        },
      },
    },

    // The vite plugin used by the project. The quantity is large, so it is separately extracted and managed
    plugins: createVitePlugins(viteEnv, isBuild),

    // 引入第三方的配置
    optimizeDeps: {
      esbuildOptions: {
        target: 'es2020',
      },
      include,
      exclude,
    },
  };
};
