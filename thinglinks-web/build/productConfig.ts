import fs from 'fs';
import path from 'path';
import productManifestCore from './productManifestCore.cjs';

const { loadProductManifestValues } = productManifestCore;

export interface ThingLinksProductConfig {
  productCode: string;
  productName: string;
  productNameZh: string;
  componentCode: string;
  componentName: string;
  componentVersion: string;
  npmPackageName: string;
  editionCode: 'community' | 'commercial' | 'enterprise';
  editionNameZh: string;
  editionNameEn: string;
  editionNameJa: string;
  licenseModel: string;
  licenseFile: string;
  webClientId: string;
  mqNamespace: string;
  manifestVersion: string;
  syncProtectedPaths: string;
}

const KEY_MAP = {
  productCode: 'THINGLINKS_PRODUCT_CODE',
  productName: 'THINGLINKS_PRODUCT_NAME',
  productNameZh: 'THINGLINKS_PRODUCT_NAME_ZH',
  componentCode: 'THINGLINKS_COMPONENT_CODE',
  componentName: 'THINGLINKS_COMPONENT_NAME',
  componentVersion: 'THINGLINKS_COMPONENT_VERSION',
  npmPackageName: 'THINGLINKS_NPM_PACKAGE_NAME',
  editionCode: 'THINGLINKS_EDITION_CODE',
  editionNameZh: 'THINGLINKS_EDITION_NAME_ZH',
  editionNameEn: 'THINGLINKS_EDITION_NAME_EN',
  editionNameJa: 'THINGLINKS_EDITION_NAME_JA',
  licenseModel: 'THINGLINKS_LICENSE_MODEL',
  licenseFile: 'THINGLINKS_LICENSE_FILE',
  webClientId: 'THINGLINKS_WEB_CLIENT_ID',
  mqNamespace: 'THINGLINKS_MQ_NAMESPACE',
  manifestVersion: 'THINGLINKS_PRODUCT_MANIFEST_VERSION',
  syncProtectedPaths: 'THINGLINKS_SYNC_PROTECTED_PATHS',
} as const;

export function loadProductConfig(root = process.cwd()): ThingLinksProductConfig {
  // CLI 与 Vite 的产品清单解析入口。
  const raw = loadProductManifestValues(root);
  const config = {} as Record<string, string>;
  Object.entries(KEY_MAP).forEach(([field, key]) => {
    const value = raw[key]?.trim();
    if (!value) {
      throw new Error(`产品配置项 ${key} 不能为空`);
    }
    config[field] = value;
  });

  const packagePath = path.resolve(root, 'package.json');
  if (!fs.existsSync(packagePath)) {
    throw new Error(`缺少 package.json：${packagePath}`);
  }
  const packageInfo = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
  if (packageInfo.name !== config.npmPackageName) {
    throw new Error(`package.json name 应为 ${config.npmPackageName}`);
  }
  if (packageInfo.version !== config.componentVersion) {
    throw new Error(`package.json version 应为 ${config.componentVersion}`);
  }

  return config as unknown as ThingLinksProductConfig;
}

export function toViteProductEnv(config: ThingLinksProductConfig) {
  return {
    VITE_GLOB_CLIENT_ID: config.webClientId,
  };
}

export function toPublicProductInfo(config: ThingLinksProductConfig) {
  return {
    productCode: config.productCode,
    productName: config.productName,
    componentCode: config.componentCode,
    componentName: config.componentName,
    componentVersion: config.componentVersion,
    editionCode: config.editionCode,
    mqNamespace: config.mqNamespace,
    editionNames: {
      'zh-CN': config.editionNameZh,
      'en-US': config.editionNameEn,
      ja: config.editionNameJa,
    },
  };
}
