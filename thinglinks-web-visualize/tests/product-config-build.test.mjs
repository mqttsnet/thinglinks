import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';

import productManifestCore from '../build/productManifestCore.cjs';

const { loadProductManifestValues } = productManifestCore;

const MANIFEST = `# 产品编码
THINGLINKS_PRODUCT_CODE=thinglinks
# 产品名称
THINGLINKS_PRODUCT_NAME=ThingLinks
# 产品中文名称
THINGLINKS_PRODUCT_NAME_ZH=ThingLinks物联网平台
# 组件编码
THINGLINKS_COMPONENT_CODE=thinglinks-web-visualize
# 组件名称
THINGLINKS_COMPONENT_NAME="ThingLinks Web Visualize"
# 组件版本
THINGLINKS_COMPONENT_VERSION=1.0.8
# 包名称
THINGLINKS_NPM_PACKAGE_NAME=thinglinks-web-visualize
# 发行编码
THINGLINKS_EDITION_CODE=enterprise
# 发行中文名称
THINGLINKS_EDITION_NAME_ZH=旗舰版
# 发行英文名称
THINGLINKS_EDITION_NAME_EN=Enterprise
# 发行日文名称
THINGLINKS_EDITION_NAME_JA=エンタープライズ版
# 授权模型
THINGLINKS_LICENSE_MODEL=commercial
# 授权文件
THINGLINKS_LICENSE_FILE=LICENSE-COMMERCIAL
# Web 客户端标识
THINGLINKS_WEB_CLIENT_ID=thinglinks_web
# 消息命名空间
THINGLINKS_MQ_NAMESPACE=thinglinks
# 产品公共站点
THINGLINKS_PUBLIC_SITE_URL=https://thinglinks.mqttsnet.com
# 清单版本
THINGLINKS_PRODUCT_MANIFEST_VERSION=1
# 仓库独立维护路径
THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL
`;

function createBuildFixture(t, manifest) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'thinglinks-build-product-config-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  fs.writeFileSync(path.join(root, '.thinglinks-product.env'), manifest);
  fs.writeFileSync(path.join(root, 'LICENSE'), 'Apache License');
  fs.writeFileSync(path.join(root, 'LICENSE-COMMERCIAL'), 'Commercial license');
  return root;
}

test('Vite 构建加载器使用共享清单核心', () => {
  const source = fs.readFileSync(new URL('../build/productConfig.ts', import.meta.url), 'utf8');
  assert.match(source, /loadProductManifestValues/);
  assert.match(source, /productManifestCore\.cjs/);
});

test('构建加载器拒绝未知 SECRET_TOKEN', (t) => {
  const manifest = `${MANIFEST}# 非法密钥\nSECRET_TOKEN=do-not-accept\n`;
  const root = createBuildFixture(t, manifest);

  assert.throws(() => loadProductManifestValues(root), /未知配置项 SECRET_TOKEN/);
});

test('构建加载器拒绝产品名称中的 Pro 发行标识', (t) => {
  const manifest = MANIFEST.replace(
    'THINGLINKS_PRODUCT_NAME=ThingLinks',
    'THINGLINKS_PRODUCT_NAME="ThingLinks Pro"',
  );
  const root = createBuildFixture(t, manifest);

  assert.throws(
    () => loadProductManifestValues(root),
    /THINGLINKS_PRODUCT_NAME 不得包含发行版本标识/,
  );
});

test('公开产品信息同时提供中英文产品名，界面按当前语言选择', () => {
  const buildSource = fs.readFileSync(new URL('../build/productConfig.ts', import.meta.url), 'utf8');
  const settingSource = fs.readFileSync(
    new URL('../src/settings/productSetting.ts', import.meta.url),
    'utf8',
  );
  const systemInfoSource = fs.readFileSync(
    new URL('../src/components/GoSystemInfo/index.vue', import.meta.url),
    'utf8',
  );

  assert.match(buildSource, /productNames:[\s\S]*'zh-CN': config\.productNameZh/);
  assert.match(settingSource, /getProductName/);
  assert.match(systemInfoSource, /\{\{ productName \}\}/);
});
