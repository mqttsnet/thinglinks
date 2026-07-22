const fs = require('node:fs');
const path = require('node:path');

const MANIFEST_FILE = '.thinglinks-product.env';
const BASE_LICENSE_FILE = 'LICENSE';
const VERSION_PATTERN =
  /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-(?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*))*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?$/;
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
const EDITION_DISPLAY_NAMES = new Map([
  [
    'community',
    {
      THINGLINKS_EDITION_NAME_ZH: '社区版',
      THINGLINKS_EDITION_NAME_EN: 'Community',
      THINGLINKS_EDITION_NAME_JA: 'コミュニティ版',
    },
  ],
  [
    'commercial',
    {
      THINGLINKS_EDITION_NAME_ZH: '商业版',
      THINGLINKS_EDITION_NAME_EN: 'Commercial',
      THINGLINKS_EDITION_NAME_JA: '商用版',
    },
  ],
  [
    'enterprise',
    {
      THINGLINKS_EDITION_NAME_ZH: '旗舰版',
      THINGLINKS_EDITION_NAME_EN: 'Enterprise',
      THINGLINKS_EDITION_NAME_JA: 'エンタープライズ版',
    },
  ],
]);
const OPEN_SOURCE_COMMERCIAL_REFERENCE_PATTERN =
  /LICENSE-COMMERCIAL|additional\s+commercial\s+terms|商业附加条款|商用ライセンス|추가\s*상업\s*약관/i;

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
  'THINGLINKS_PUBLIC_SITE_URL',
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
  'THINGLINKS_PUBLIC_SITE_URL',
];

function productError(message) {
  return new Error(`错误：${message}`);
}

function normalizeProductRelativePath(filePath) {
  return filePath.replaceAll('\\', '/').replace(/^\.\//, '').replace(/\/$/, '');
}

function validateProductVersion(version) {
  if (!VERSION_PATTERN.test(version)) {
    throw productError('大屏版本号格式不正确，应使用 SemVer（例如 1.2.3、1.2.3-beta.1+build.7）');
  }
}

function derivePackageLicense(licenseModel, licenseFile = BASE_LICENSE_FILE) {
  if (licenseModel === 'open-source') return OPEN_SOURCE_PACKAGE_LICENSE;
  if (licenseModel === 'commercial' || licenseModel === 'dual-license') {
    return `SEE LICENSE IN ${normalizeProductRelativePath(licenseFile)}`;
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
    if (!trimmed) {
      previousContent = '';
      continue;
    }
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
  const expectedEditionNames = EDITION_DISPLAY_NAMES.get(values.THINGLINKS_EDITION_CODE);
  for (const [key, expected] of Object.entries(expectedEditionNames)) {
    if (values[key] !== expected) {
      throw productError(
        `发行展示名称与发行编码不匹配：${values.THINGLINKS_EDITION_CODE} 的 ${key} 应为 ${expected}`,
      );
    }
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

  let publicSiteUrl;
  try {
    publicSiteUrl = new URL(values.THINGLINKS_PUBLIC_SITE_URL);
  } catch {
    throw productError('THINGLINKS_PUBLIC_SITE_URL 不是有效地址');
  }
  if (
    publicSiteUrl.protocol !== 'https:' ||
    publicSiteUrl.username ||
    publicSiteUrl.password ||
    publicSiteUrl.search ||
    publicSiteUrl.hash ||
    publicSiteUrl.pathname !== '/'
  ) {
    throw productError('THINGLINKS_PUBLIC_SITE_URL 应为不含认证信息、路径和参数的 HTTPS 站点地址');
  }

  const licensePath = normalizeProductRelativePath(values.THINGLINKS_LICENSE_FILE);
  if (!licensePath || path.isAbsolute(licensePath) || licensePath.split('/').includes('..')) {
    throw productError('THINGLINKS_LICENSE_FILE 只能是仓库内相对文件路径');
  }
  if (values.THINGLINKS_LICENSE_MODEL === 'open-source' && licensePath !== BASE_LICENSE_FILE) {
    throw productError(`开源授权模型的授权文件必须为 ${BASE_LICENSE_FILE}`);
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

function assertRepositoryPath(projectRoot, relativePath, expectedType, label) {
  const normalizedPath = normalizeProductRelativePath(relativePath);
  const rootRealPath = fs.realpathSync(projectRoot);
  let currentPath = projectRoot;

  for (const segment of normalizedPath.split('/')) {
    currentPath = path.join(currentPath, segment);
    let stats;
    try {
      stats = fs.lstatSync(currentPath);
    } catch (error) {
      if (error.code === 'ENOENT') throw productError(`${label}不存在：${relativePath}`);
      throw productError(`无法读取${label} ${relativePath}：${error.message}`);
    }
    if (stats.isSymbolicLink()) {
      throw productError(`${label}不得使用符号链接：${relativePath}`);
    }
  }

  const targetRealPath = fs.realpathSync(currentPath);
  const relativeRealPath = path.relative(rootRealPath, targetRealPath);
  if (
    relativeRealPath === '..' ||
    relativeRealPath.startsWith(`..${path.sep}`) ||
    path.isAbsolute(relativeRealPath)
  ) {
    throw productError(`${label} realpath 超出仓库边界：${relativePath}`);
  }

  const stats = fs.lstatSync(currentPath);
  if (expectedType === 'file' && !stats.isFile()) {
    throw productError(`${label}不存在或不是普通文件：${relativePath}`);
  }
  if (expectedType === 'file-or-directory' && !stats.isFile() && !stats.isDirectory()) {
    throw productError(`${label}不是文件或目录：${relativePath}`);
  }
  return currentPath;
}

function assertProductLicenseFile(projectRoot, values) {
  const licensePath = assertRepositoryPath(
    projectRoot,
    values.THINGLINKS_LICENSE_FILE,
    'file',
    '授权文件',
  );
  if (
    values.THINGLINKS_LICENSE_MODEL === 'open-source' &&
    OPEN_SOURCE_COMMERCIAL_REFERENCE_PATTERN.test(fs.readFileSync(licensePath, 'utf8'))
  ) {
    throw productError('开源授权文件不得引用商业附加条款');
  }
}

function assertProductProtectedPaths(projectRoot, values) {
  const protectedPaths = parseSyncProtectedPaths(values.THINGLINKS_SYNC_PROTECTED_PATHS);
  for (const relativePath of protectedPaths) {
    assertRepositoryPath(projectRoot, relativePath, 'file', '同步保护路径');
  }
}

function loadProductManifestValues(projectRoot) {
  const manifestPath = assertRepositoryPath(projectRoot, MANIFEST_FILE, 'file', '产品配置文件');
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
