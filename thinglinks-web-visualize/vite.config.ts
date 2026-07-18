import type { ConfigEnv, UserConfig } from 'vite'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { OUTPUT_DIR, brotliSize, chunkSizeWarningLimit, terserOptions, rollupOptions } from './build/constant'
import viteCompression from 'vite-plugin-compression'
import { viteMockServe } from 'vite-plugin-mock'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import { createProxy } from './build/vite/proxy';
import { wrapperEnv } from './build/utils';
import { loadProductConfig, toPublicProductInfo, toViteProductEnv } from './build/productConfig';

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir)
}
export default ({ command, mode }: ConfigEnv): UserConfig => {

  const root = process.cwd();

  const productConfig = loadProductConfig(root);
  const productEnv = toViteProductEnv(productConfig);
  const env = { ...loadEnv(mode, root), ...productEnv };

  const viteEnv = wrapperEnv(env);

  const { VITE_PORT, VITE_PUBLIC_PATH, VITE_PROXY, VITE_DROP_CONSOLE } = viteEnv;
  console.log(
    'mode=%s, root=%s, product=%s@%s, edition=%s',
    mode,
    root,
    productConfig.componentCode,
    productConfig.componentVersion,
    productConfig.editionCode
  );
  return {
    base: process.env.NODE_ENV === 'production' ? './' : '/',
    // 路径重定向
    resolve: {
      alias: [
        {
          find: /\/#\//,
          replacement: pathResolve('types')
        },
        {
          find: '@',
          replacement: pathResolve('src')
        },
        {
          find: 'vue-i18n',
          replacement: 'vue-i18n/dist/vue-i18n.cjs.js', //解决i8n警告
        }
      ],
      dedupe: ['vue']
    },
    server: {
      https: false,
      // Listening on all local IPs
      host: true,
      port: VITE_PORT,
      // Load proxy configuration from .env
      proxy: createProxy(VITE_PROXY),
    },
    // 全局 css 注册
    css: {
      preprocessorOptions: {
        scss: {
          javascriptEnabled: true,
          additionalData: `@import "src/styles/common/style.scss";`
        }
      }
    },
    define: {
      __THINGLINKS_PRODUCT_INFO__: JSON.stringify(toPublicProductInfo(productConfig)),
      'import.meta.env.VITE_GLOB_CLIENT_ID': JSON.stringify(productEnv.VITE_GLOB_CLIENT_ID)
    },
    plugins: [
      vue(),
      monacoEditorPlugin({
        languageWorkers: ['editorWorkerService', 'typescript', 'json', 'html']
      }),
      viteMockServe({
        mockPath: '/src/api/mock',
        // 开发打包开关
        localEnabled: command === 'serve',
        // 生产打包开关
        prodEnabled: false,
        // 打开后，可以读取 ts 文件模块。 请注意，打开后将无法监视.js 文件
        supportTs: true,
        // 监视文件更改
        watchFiles: true
      }),
      // 压缩
      viteCompression({
        verbose: true,
        disable: false,
        threshold: 10240,
        algorithm: 'gzip',
        ext: '.gz'
      })
    ],
    build: {
      target: ['edge90','chrome90','firefox90','safari15', 'es2019'],
      outDir: OUTPUT_DIR,
      // minify: 'terser', // 如果需要用terser混淆，可打开这两行
      // terserOptions: terserOptions,
      rollupOptions: rollupOptions,
      brotliSize: brotliSize,
      chunkSizeWarningLimit: chunkSizeWarningLimit
    }
  }
}
