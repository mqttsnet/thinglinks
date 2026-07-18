const fs = require('node:fs');
const path = require('node:path');

const MANIFEST_FILE = '.thinglinks-product.env';
const BASE_LICENSE_FILE = 'LICENSE';
const VERSION_PATTERN = /^\d+\.\d+\.\d+([-.][0-9A-Za-z.-]+)?$/;
const IDENTITY_EDITION_TOKEN_PATTERN =
  /(^|[^A-Za-z0-9])(?:pro|community|commercial|enterprise)(?=[^A-Za-z0-9]|$)/i;
const IDENTITY_EDITION_NAME_PATTERN = /旗舰版|商业版|社区版|开源版|企业版/;
const IDENTITY_EDITION_PHRASE_PATTERN =
  /(^|[^A-Za-z0-9])(?:professional|open\s+source)\s+edition(?=[^A-Za-z0-9]|$)/i;
const COMPACT_EDITION_SUFFIX_PATTERN = /(?:pro|community|commercial|enterprise)$/i;
const CAMEL_EDITION_PATTERN = /(?:Pro|Community|Commercial|Enterprise)(?=[A-Z0-9]|$)/;
const LICENSE_MODELS = new Set(['open-source', 'commercial', 'dual-license']);
const OPEN_SOURCE_PACKAGE_LICENSE = 'Apache-2.0';
const LICENSE_FILE_PACKAGE_LICENSE = 'SEE LICENSE IN LICENSE';
const EDITION_LICENSE_MODELS = new Map([
  ['community', new Set(['open-source'])],
  ['commercial', new Set(['commercial', 'dual-license'])],
  ['enterprise', new Set(['commercial', 'dual-license'])],
]);

const REQUIRED_KEYS = [
  'THINGLINKS_PRODUCT_CODE',
  'THINGLINKS_PRODUCT_NAME',
  'THINGLINKS_PRODUCT_NAME_ZH',
  'THINGLINKS_COMPONENT_CODE',
  'THINGLINKS_COMPONENT_NAME',
  'THINGLINKS_COMPONENT_VERSION',
  'THINGLINKS_NPM_PACKAGE_NAME',
  'THINGLINKS_EDITION_CODE',
  'THINGLINKS_EDITION_NAME_ZH',
  'THINGLINKS_EDITION_NAME_EN',
  'THINGLINKS_EDITION_NAME_JA',
  'THINGLINKS_LICENSE_MODEL',
  'THINGLINKS_LICENSE_FILE',
  'THINGLINKS_WEB_CLIENT_ID',
  'THINGLINKS_MQ_NAMESPACE',
  'THINGLINKS_PRODUCT_MANIFEST_VERSION',
  'THINGLINKS_SYNC_PROTECTED_PATHS',
];
const REQUIRED_KEY_SET = new Set(REQUIRED_KEYS);

const NEUTRAL_IDENTITY_KEYS = [
  'THINGLINKS_PRODUCT_CODE',
  'THINGLINKS_PRODUCT_NAME',
  'THINGLINKS_PRODUCT_NAME_ZH',
  'THINGLINKS_COMPONENT_CODE',
  'THINGLINKS_COMPONENT_NAME',
  'THINGLINKS_NPM_PACKAGE_NAME',
  'THINGLINKS_WEB_CLIENT_ID',
  'THINGLINKS_MQ_NAMESPACE',
];

function productError(message) {
  return new Error(`错误：${message}`);
}

function normalizeProductRelativePath(filePath) {
  return filePath.replaceAll('\\', '/').replace(/^\.\//, '').replace(/\/$/, '');
}

function validateProductVersion(version) {
  if (!VERSION_PATTERN.test(version)) {
    throw productError('Web 版本号格式不正确，应为 x.y.z 或带预发布后缀的语义化版本');
  }
}

function derivePackageLicense(licenseModel) {
  if (licenseModel === 'open-source') return OPEN_SOURCE_PACKAGE_LICENSE;
  if (licenseModel === 'commercial' || licenseModel === 'dual-license') {
    return LICENSE_FILE_PACKAGE_LICENSE;
  }
  throw productError(`无法为授权模型 ${licenseModel} 派生 package license`);
}

function hasIdentityEditionMarker(value) {
  return (
    IDENTITY_EDITION_TOKEN_PATTERN.test(value) ||
    IDENTITY_EDITION_NAME_PATTERN.test(value) ||
    IDENTITY_EDITION_PHRASE_PATTERN.test(value) ||
    COMPACT_EDITION_SUFFIX_PATTERN.test(value) ||
    CAMEL_EDITION_PATTERN.test(value)
  );
}

function parseManifestValue(rawValue, source, lineNumber, key) {
  const value = rawValue.trim();
  const startsWithQuote = value.startsWith('"') || value.startsWith("'");
  const endsWithQuote = value.endsWith('"') || value.endsWith("'");
  if (startsWithQuote || endsWithQuote) {
    if (!startsWithQuote || !endsWithQuote || value[0] !== value[value.length - 1]) {
      throw productError(`${source}:${lineNumber} 配置项 ${key} 的单/双引号必须同种且成对闭合`);
    }
    return value.slice(1, -1);
  }
  return value;
}

function parseSyncProtectedPaths(rawValue) {
  const protectedPaths = rawValue.split(',');
  const seen = new Set();

  for (const item of protectedPaths) {
    if (!item) throw productError('同步保护路径不能包含空项');
    if (/[*?[\]]/.test(item)) {
      throw productError(`同步保护路径不得包含 glob 元字符：${item}`);
    }
    if (path.isAbsolute(item) || /^[A-Za-z]:[\\/]/.test(item)) {
      throw productError(`同步保护路径不得使用绝对路径：${item}`);
    }
    if (item.startsWith('./')) {
      throw productError(`同步保护路径必须使用规范化相对路径：${item}`);
    }
    if (!/^[A-Za-z0-9._/-]+$/.test(item)) {
      throw productError(`同步保护路径包含不安全字符：${item}`);
    }

    const segments = item.split('/');
    if (segments.includes('..') || segments.includes('.') || segments.includes('')) {
      throw productError(`同步保护路径禁止越界或非规范化片段：${item}`);
    }
    if (seen.has(item)) throw productError(`同步保护路径重复：${item}`);
    seen.add(item);
  }

  return protectedPaths;
}

function parseProductManifestContent(content, source = MANIFEST_FILE) {
  const lines = content.split(/\r?\n/);
  const values = {};
  let previousContent = '';

  for (const [index, line] of lines.entries()) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    if (trimmed.startsWith('#')) {
      previousContent = trimmed;
      continue;
    }
    const match = trimmed.match(/^([A-Z][A-Z0-9_]*)=(.*)$/);
    if (!match) {
      throw productError(`${source}:${index + 1} 存在不支持的语法，仅允许空行、注释和 KEY=VALUE`);
    }
    const key = match[1];
    if (!REQUIRED_KEY_SET.has(key)) {
      throw productError(`${source}:${index + 1} 包含未知配置项 ${key}`);
    }
    if (Object.hasOwn(values, key)) {
      throw productError(`${source} 中配置项 ${key} 重复定义`);
    }
    if (!previousContent.startsWith('#') || !/[\u3400-\u9fff]/.test(previousContent)) {
      throw productError(`配置项 ${key} 前必须紧邻一行中文注释`);
    }
    values[key] = parseManifestValue(match[2], source, index + 1, key);
    previousContent = trimmed;
  }

  for (const key of REQUIRED_KEYS) {
    if (!values[key]) throw productError(`产品配置项 ${key} 不能为空`);
  }
  if (!/^(community|commercial|enterprise)$/.test(values.THINGLINKS_EDITION_CODE)) {
    throw productError('发行版本编码仅允许 community、commercial、enterprise');
  }
  if (!LICENSE_MODELS.has(values.THINGLINKS_LICENSE_MODEL)) {
    throw productError('授权模型仅允许 open-source、commercial、dual-license');
  }
  if (
    !EDITION_LICENSE_MODELS.get(values.THINGLINKS_EDITION_CODE).has(values.THINGLINKS_LICENSE_MODEL)
  ) {
    throw productError(
      `发行类型与授权模型组合不匹配：${values.THINGLINKS_EDITION_CODE} + ${values.THINGLINKS_LICENSE_MODEL}`,
    );
  }
  if (values.THINGLINKS_PRODUCT_MANIFEST_VERSION !== '1') {
    throw productError('产品清单格式版本仅支持 1');
  }
  validateProductVersion(values.THINGLINKS_COMPONENT_VERSION);
  for (const key of NEUTRAL_IDENTITY_KEYS) {
    if (hasIdentityEditionMarker(values[key])) {
      throw productError(`${key} 不得包含发行版本标识`);
    }
  }

  const runtimeIdentifiers = [
    ['THINGLINKS_WEB_CLIENT_ID', /^[a-z][a-z0-9_-]{1,63}$/],
    ['THINGLINKS_MQ_NAMESPACE', /^[a-z][a-z0-9-]{0,62}$/],
  ];
  for (const [key, safePattern] of runtimeIdentifiers) {
    if (!safePattern.test(values[key])) throw productError(`${key} 格式不安全`);
  }

  const licensePath = normalizeProductRelativePath(values.THINGLINKS_LICENSE_FILE);
  if (!licensePath || path.isAbsolute(licensePath) || licensePath.split('/').includes('..')) {
    throw productError('THINGLINKS_LICENSE_FILE 只能是仓库内相对文件路径');
  }

  const protectedPaths = parseSyncProtectedPaths(values.THINGLINKS_SYNC_PROTECTED_PATHS);
  if (!protectedPaths.includes(MANIFEST_FILE)) {
    throw productError(`THINGLINKS_SYNC_PROTECTED_PATHS 必须包含 ${MANIFEST_FILE}`);
  }
  if (!protectedPaths.includes(BASE_LICENSE_FILE)) {
    throw productError(`THINGLINKS_SYNC_PROTECTED_PATHS 必须包含 ${BASE_LICENSE_FILE}`);
  }
  if (!protectedPaths.includes(licensePath)) {
    throw productError('THINGLINKS_SYNC_PROTECTED_PATHS 必须包含 THINGLINKS_LICENSE_FILE');
  }

  return values;
}

function assertProductLicenseFile(projectRoot, values) {
  const licensePath = path.join(
    projectRoot,
    normalizeProductRelativePath(values.THINGLINKS_LICENSE_FILE),
  );
  if (!fs.existsSync(licensePath) || !fs.statSync(licensePath).isFile()) {
    throw productError(`授权文件不存在或不是普通文件：${values.THINGLINKS_LICENSE_FILE}`);
  }
}

function assertProductProtectedPaths(projectRoot, values) {
  const protectedPaths = parseSyncProtectedPaths(values.THINGLINKS_SYNC_PROTECTED_PATHS);
  for (const relativePath of protectedPaths) {
    const targetPath = path.join(projectRoot, normalizeProductRelativePath(relativePath));
    let targetStats;
    try {
      targetStats = fs.statSync(targetPath);
    } catch (error) {
      if (error.code === 'ENOENT') {
        throw productError(`同步保护路径不存在：${relativePath}`);
      }
      throw productError(`无法读取同步保护路径 ${relativePath}：${error.message}`);
    }
    if (!targetStats.isFile() && !targetStats.isDirectory()) {
      throw productError(`同步保护路径不是文件或目录：${relativePath}`);
    }
  }
}

function loadProductManifestValues(projectRoot) {
  const manifestPath = path.join(projectRoot, MANIFEST_FILE);
  if (!fs.existsSync(manifestPath)) throw productError(`缺少产品配置文件：${manifestPath}`);
  const values = parseProductManifestContent(fs.readFileSync(manifestPath, 'utf8'), manifestPath);
  assertProductLicenseFile(projectRoot, values);
  assertProductProtectedPaths(projectRoot, values);
  return values;
}

module.exports = {
  assertProductLicenseFile,
  assertProductProtectedPaths,
  derivePackageLicense,
  loadProductManifestValues,
  normalizeProductRelativePath,
  parseProductManifestContent,
  validateProductVersion,
};
