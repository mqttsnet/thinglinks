#!/usr/bin/env node

import childProcess from 'node:child_process';
import { randomUUID } from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath, pathToFileURL } from 'node:url';
import productManifestCore from '../build/productManifestCore.cjs';

const {
  assertProductLicenseFile,
  assertProductProtectedPaths,
  derivePackageLicense,
  normalizeProductRelativePath: normalizeRelativePath,
  parseProductManifestContent,
  validateProductVersion,
} = productManifestCore;

const defaultProjectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const manifestFile = '.thinglinks-product.env';
const packageFile = 'package.json';
const productConfigComponentCode = 'thinglinks-web-visualize';
const productConfigLockRef = `refs/thinglinks/product-config-lock/${productConfigComponentCode}`;
const productConfigJournalFile = `thinglinks-product-config-${productConfigComponentCode}.journal.json`;
const productConfigLockCreateMaxAttempts = 4;

const forbiddenMarkerPatterns = [
  /thinglinks-[a-z0-9][a-z0-9._-]*-pro(?=[^a-z0-9]|$)/i,
  /bifromq-plugin-pro(?=[^a-z]|$)/i,
  /thinglinks(?:[_-]?pro)(?=[^a-z0-9]|$)/i,
  /(?:^|[^a-z0-9])(?:web|cloud|util)[\s_-]?pro(?=[^a-z0-9]|$)/i,
  /(?:Web|Cloud|Util|web|cloud|util)Pro(?=[A-Z0-9_]|[^A-Za-z0-9_]|$)/,
  /(?:ThingLinks|BifroMQ|Bifromq)[A-Za-z0-9]*Pro(?=[A-Z0-9_]|[^A-Za-z0-9_]|$)/,
  /(?:web|cloud|util)pro(?=[A-Z0-9_]|[^A-Za-z0-9_]|$)/,
  /(?:thinglinks|bifromq)[A-Za-z0-9]*[Pp]ro(?=[A-Z0-9_]|[^A-Za-z0-9_]|$)/,
  /(?:LayoutHeader|Visualize)Pro(?=[A-Z0-9_]|[^A-Za-z0-9_]|$)/,
  /\bcommercial[_ -]+license\b|商业授权|商用ライセンス/i,
  /コミュニティ版|エンタープライズ版|商用版|プロ版|オープンソース版/,
  /커뮤니티\s*(?:버전|에디션)|엔터프라이즈\s*(?:버전|에디션)|상용\s*(?:버전|에디션)|프로\s*(?:버전|에디션)|오픈\s*소스\s*(?:버전|에디션)/i,
  // Console 调试标签检查项；Jessibuca-Pro 为第三方产品名称。
  /\bconsole\.(?:debug|info|log|warn|error)\s*\([^)\r\n]*(?<!jessibuca)[-_]pro(?=['"`])/i,
  /旗舰版|商业版|社区版|开源版/,
  /\b(?:pro(?:fessional)?|enterprise|commercial|community|open\s+source)\s+edition\b/i,
  /(?:^|[,{;\s])["']?(?:[a-z][a-z0-9]*[_-]?)?edition(?:[_-]?code)?["']?\s*(?::|=)\s*["']?(?:community|commercial|enterprise)["']?(?=\s*(?:[,;}\]#]|\/\/|$))/i,
];

const scanExclusions = [
  // 产品配置检查器及测试文件包含扫描规则和样例文本。
  'scripts/product-config.mjs',
  'build/productManifestCore.cjs',
  'tests/product-build-gate.test.mjs',
  'tests/product-config.test.mjs',
  'tests/product-config-build.test.mjs',
];

function productError(message) {
  return new Error(`错误：${message}`);
}

function isPathInside(filePath, basePath) {
  const normalizedFile = normalizeRelativePath(filePath);
  const normalizedBase = normalizeRelativePath(basePath);
  return normalizedFile === normalizedBase || normalizedFile.startsWith(`${normalizedBase}/`);
}

function hasForbiddenMarker(value) {
  return forbiddenMarkerPatterns.some((pattern) => pattern.test(value));
}

function readProjectFile(projectRoot, relativePath) {
  const target = path.join(projectRoot, relativePath);
  if (!fs.existsSync(target)) throw productError(`缺少文件：${target}`);
  return fs.readFileSync(target, 'utf8');
}

function parseManifest(projectRoot, content = readProjectFile(projectRoot, manifestFile)) {
  const values = parseProductManifestContent(content, path.join(projectRoot, manifestFile));
  assertProductLicenseFile(projectRoot, values);
  assertProductProtectedPaths(projectRoot, values);
  return values;
}

function runGit(projectRoot, args, options = {}) {
  return childProcess.execFileSync('git', args, {
    cwd: projectRoot,
    encoding: 'utf8',
    stdio: ['pipe', 'pipe', 'pipe'],
    ...options,
  });
}

function parseProductConfigLock(content, oid) {
  let owner;
  try {
    owner = JSON.parse(content);
  } catch {
    throw productError(`产品配置锁 owner 无效：${productConfigLockRef}@${oid}`);
  }
  if (
    !Number.isSafeInteger(owner.pid) ||
    owner.pid <= 0 ||
    typeof owner.token !== 'string' ||
    !owner.token ||
    !Number.isSafeInteger(owner.createdAt) ||
    owner.createdAt <= 0
  ) {
    throw productError(`产品配置锁 owner 无效：${productConfigLockRef}@${oid}`);
  }
  return owner;
}

function isProcessAlive(pid) {
  try {
    process.kill(pid, 0);
    return true;
  } catch (error) {
    if (error.code === 'ESRCH') return false;
    if (error.code === 'EPERM') return true;
    throw productError(`无法检查产品配置锁进程 ${pid}：${error.message}`);
  }
}

function writeProductConfigLockOwner(projectRoot) {
  const token = randomUUID();
  const owner = {
    pid: process.pid,
    token,
    createdAt: Date.now(),
  };
  try {
    const oid = runGit(projectRoot, ['hash-object', '-w', '--stdin'], {
      input: `${JSON.stringify(owner)}\n`,
    }).trim();
    if (!/^[0-9a-f]{40,64}$/.test(oid)) throw new Error(`Git 返回了无效对象 ID：${oid}`);
    return { ...owner, oid, projectRoot };
  } catch (error) {
    throw productError(`无法写入产品配置锁 owner：${error.message}`);
  }
}

function readProductConfigLockOid(projectRoot) {
  try {
    return runGit(projectRoot, ['rev-parse', '--verify', '--quiet', productConfigLockRef]).trim();
  } catch (error) {
    if (error.status === 1) return null;
    throw productError(`无法读取产品配置锁引用：${error.message}`);
  }
}

function readProductConfigLock(projectRoot) {
  const oid = readProductConfigLockOid(projectRoot);
  if (!oid) return null;
  let content;
  try {
    content = runGit(projectRoot, ['cat-file', 'blob', oid]);
  } catch (error) {
    throw productError(`无法读取产品配置锁 owner：${error.message}`);
  }
  return { ...parseProductConfigLock(content, oid), oid, projectRoot };
}

function tryClaimProductConfigLock(lock) {
  try {
    runGit(lock.projectRoot, ['update-ref', productConfigLockRef, lock.oid, '']);
    return { claimed: true };
  } catch (error) {
    return { claimed: false, error };
  }
}

function tryDeleteProductConfigLock(projectRoot, observedOid) {
  try {
    runGit(projectRoot, ['update-ref', '-d', productConfigLockRef, observedOid]);
    return true;
  } catch (error) {
    if (readProductConfigLockOid(projectRoot) !== observedOid) return false;
    throw productError(`无法删除产品配置锁引用：${error.message}`);
  }
}

function assertProductConfigLockOwnership(lock) {
  if (readProductConfigLockOid(lock.projectRoot) !== lock.oid) {
    throw productError('产品配置锁归属在执行期间发生变化');
  }
}

function getProductConfigJournalPath(projectRoot) {
  const gitPath = runGit(projectRoot, ['rev-parse', '--git-path', productConfigJournalFile]).trim();
  if (!gitPath) throw productError('无法确定产品配置事务 journal 路径');
  return path.resolve(projectRoot, gitPath);
}

function isUnsupportedDirectoryFsync(error) {
  if (['EINVAL', 'ENOTSUP', 'ENOSYS'].includes(error.code)) return true;
  return process.platform === 'win32' && ['EBADF', 'EISDIR', 'EPERM'].includes(error.code);
}

function fsyncFile(file) {
  const descriptor = fs.openSync(file, 'r+');
  try {
    fs.fsyncSync(descriptor);
  } finally {
    fs.closeSync(descriptor);
  }
}

function fsyncDirectory(directory) {
  let descriptor;
  try {
    descriptor = fs.openSync(directory, 'r');
    fs.fsyncSync(descriptor);
  } catch (error) {
    if (!isUnsupportedDirectoryFsync(error)) throw error;
  } finally {
    if (descriptor !== undefined) fs.closeSync(descriptor);
  }
}

function writeFileDurably(target, content, options) {
  fs.writeFileSync(target, content, { ...options, flag: 'wx' });
  fsyncFile(target);
}

function renameDurably(source, target) {
  fs.renameSync(source, target);
  fsyncDirectory(path.dirname(target));
}

function removeFileDurably(target) {
  try {
    fs.unlinkSync(target);
  } catch (error) {
    if (error.code === 'ENOENT') return false;
    throw error;
  }
  fsyncDirectory(path.dirname(target));
  return true;
}

function writeJsonAtomically(target, value) {
  const temporary = `${target}.tmp-${process.pid}-${randomUUID()}`;
  try {
    writeFileDurably(temporary, `${JSON.stringify(value)}\n`, { mode: 0o600 });
    renameDurably(temporary, target);
  } finally {
    removeFileDurably(temporary);
  }
}

function validateProductConfigJournal(projectRoot, journal) {
  if (
    !journal ||
    journal.version !== 1 ||
    journal.component !== productConfigComponentCode ||
    !Array.isArray(journal.files) ||
    journal.files.length < 1
  ) {
    throw productError('产品配置事务 journal 格式无效');
  }
  const allowedPaths = new Set([manifestFile, packageFile]);
  const seen = new Set();
  for (const file of journal.files) {
    if (
      !file ||
      typeof file.relativePath !== 'string' ||
      !allowedPaths.has(file.relativePath) ||
      seen.has(file.relativePath) ||
      typeof file.original !== 'string' ||
      typeof file.next !== 'string' ||
      !Number.isSafeInteger(file.mode)
    ) {
      throw productError('产品配置事务 journal 文件记录无效');
    }
    const target = path.resolve(projectRoot, file.relativePath);
    const relativeTarget = path.relative(path.resolve(projectRoot), target);
    if (relativeTarget.startsWith(`..${path.sep}`) || path.isAbsolute(relativeTarget)) {
      throw productError('产品配置事务 journal 文件路径超出仓库边界');
    }
    seen.add(file.relativePath);
  }
  return journal;
}

function readProductConfigJournal(projectRoot) {
  const journalPath = getProductConfigJournalPath(projectRoot);
  if (!fs.existsSync(journalPath)) return null;
  let journal;
  try {
    journal = JSON.parse(fs.readFileSync(journalPath, 'utf8'));
  } catch (error) {
    throw productError(`无法读取产品配置事务 journal：${error.message}`);
  }
  return { path: journalPath, ...validateProductConfigJournal(projectRoot, journal) };
}

function removeProductConfigJournal(journalPath) {
  let removed = false;
  try {
    fs.unlinkSync(journalPath);
    removed = true;
  } catch (error) {
    if (error.code === 'ENOENT') return;
    throw error;
  }

  try {
    fsyncDirectory(path.dirname(journalPath));
  } catch (error) {
    if (removed) error.productConfigJournalRemoved = true;
    throw error;
  }
}

function replaceJournalFiles(projectRoot, files, lock, contentField) {
  const prepared = [];
  try {
    for (const file of files) {
      const target = path.join(projectRoot, file.relativePath);
      const temporary = `${target}.tmp-${process.pid}-${randomUUID()}`;
      prepared.push({ target, temporary });
      writeFileDurably(temporary, file[contentField], { mode: file.mode });
    }
    for (const item of prepared) {
      assertProductConfigLockOwnership(lock);
      renameDurably(item.temporary, item.target);
    }
  } finally {
    for (const item of prepared) {
      removeFileDurably(item.temporary);
    }
  }
}

function recoverProductConfigJournal(projectRoot, lock) {
  const journal = readProductConfigJournal(projectRoot);
  if (!journal) return;
  assertProductConfigLockOwnership(lock);

  let allNext = true;
  for (const file of journal.files) {
    const current = fs.readFileSync(path.join(projectRoot, file.relativePath), 'utf8');
    if (current !== file.original && current !== file.next) {
      throw productError(
        `产品配置事务 journal 无法自动恢复，${file.relativePath} 已被事务外修改`,
      );
    }
    if (current !== file.next) allNext = false;
  }

  if (!allNext) {
    replaceJournalFiles(projectRoot, journal.files, lock, 'original');
  }
  assertProductConfigLockOwnership(lock);
  removeProductConfigJournal(journal.path);
}

function acquireProductConfigLock(projectRoot) {
  const candidate = writeProductConfigLockOwner(projectRoot);
  let emptyRefClaimFailures = 0;

  for (;;) {
    const claim = tryClaimProductConfigLock(candidate);
    if (claim.claimed) {
      assertProductConfigLockOwnership(candidate);
      return candidate;
    }

    const observed = readProductConfigLock(projectRoot);
    if (!observed) {
      emptyRefClaimFailures += 1;
      if (emptyRefClaimFailures >= productConfigLockCreateMaxAttempts) {
        throw productError(
          `产品配置锁引用为空但连续 ${productConfigLockCreateMaxAttempts} 次 create-CAS 失败：${claim.error.message}`,
        );
      }
      continue;
    }
    emptyRefClaimFailures = 0;
    if (isProcessAlive(observed.pid)) {
      throw productError(`产品配置更新正在由进程 ${observed.pid} 执行`);
    }
    tryDeleteProductConfigLock(projectRoot, observed.oid);
  }
}

function releaseProductConfigLock(lock) {
  if (!tryDeleteProductConfigLock(lock.projectRoot, lock.oid)) {
    throw productError('产品配置锁归属在执行期间发生变化');
  }
}

function withProductConfigLock(projectRoot, operation) {
  const lock = acquireProductConfigLock(projectRoot);
  let result;
  let operationError;
  try {
    recoverProductConfigJournal(projectRoot, lock);
    result = operation(lock);
  } catch (error) {
    operationError = error;
  }

  let releaseError;
  try {
    releaseProductConfigLock(lock);
  } catch (error) {
    releaseError = error;
  }

  if (operationError && releaseError) {
    throw productError(`${operationError.message}；${releaseError.message}`);
  }
  if (operationError) throw operationError;
  if (releaseError) throw releaseError;
  return result;
}

function buildPackageContent(
  projectRoot,
  values,
  source = readProjectFile(projectRoot, packageFile),
) {
  let pkg;
  try {
    pkg = JSON.parse(source);
  } catch (error) {
    throw productError(`${packageFile} 不是合法 JSON：${error.message}`);
  }
  pkg.name = values.THINGLINKS_NPM_PACKAGE_NAME;
  pkg.version = values.THINGLINKS_COMPONENT_VERSION;
  pkg.license = derivePackageLicense(
    values.THINGLINKS_LICENSE_MODEL,
    values.THINGLINKS_LICENSE_FILE,
  );
  return `${JSON.stringify(pkg, null, 2)}\n`;
}

function assertPackageMetadata(
  projectRoot,
  values,
  content = readProjectFile(projectRoot, packageFile),
) {
  let pkg;
  try {
    pkg = JSON.parse(content);
  } catch (error) {
    throw productError(`${packageFile} 不是合法 JSON：${error.message}`);
  }
  if (pkg.name !== values.THINGLINKS_NPM_PACKAGE_NAME) {
    throw productError(`${packageFile} name 应为 ${values.THINGLINKS_NPM_PACKAGE_NAME}`);
  }
  if (pkg.version !== values.THINGLINKS_COMPONENT_VERSION) {
    throw productError(`${packageFile} version 应为 ${values.THINGLINKS_COMPONENT_VERSION}`);
  }
  const expectedLicense = derivePackageLicense(
    values.THINGLINKS_LICENSE_MODEL,
    values.THINGLINKS_LICENSE_FILE,
  );
  if (pkg.license !== expectedLicense) {
    throw productError(`${packageFile} license 应为 ${expectedLicense}`);
  }
}

function listTrackedAndUntrackedFiles(projectRoot) {
  let output;
  try {
    output = childProcess.execFileSync(
      'git',
      ['ls-files', '-z', '--cached', '--others', '--exclude-standard'],
      {
        cwd: projectRoot,
      },
    );
  } catch (error) {
    throw productError(`无法读取 Git 文件清单：${error.message}`);
  }
  return output.toString('utf8').split('\0').filter(Boolean).map(normalizeRelativePath);
}

function inspectScannableFile(projectRoot, relativePath) {
  const root = path.resolve(projectRoot);
  const segments = normalizeRelativePath(relativePath).split('/');
  if (segments.some((segment) => !segment || segment === '.' || segment === '..')) {
    throw productError(`Git 文件路径不安全：${relativePath}`);
  }

  let current = root;
  for (const segment of segments) {
    current = path.join(current, segment);
    let stats;
    try {
      stats = fs.lstatSync(current);
    } catch (error) {
      if (error.code === 'ENOENT') return { exists: false };
      throw productError(`无法检查待扫描文件 ${relativePath}：${error.message}`);
    }
    if (stats.isSymbolicLink()) return { exists: true, symbolicLink: true };
  }

  const stats = fs.lstatSync(current);
  return { exists: true, symbolicLink: false, regularFile: stats.isFile(), absolutePath: current };
}

function findForbiddenMarkers(projectRoot, values, overlays = new Map()) {
  const protectedPaths = values.THINGLINKS_SYNC_PROTECTED_PATHS.split(',').map((item) =>
    normalizeRelativePath(item.trim()),
  );
  const isExcluded = (file) =>
    [...protectedPaths, ...scanExclusions].some((excluded) => isPathInside(file, excluded));
  const findings = [];

  for (const file of listTrackedAndUntrackedFiles(projectRoot)) {
    if (isExcluded(file)) continue;

    const inspected = inspectScannableFile(projectRoot, file);
    if (!inspected.exists) continue;
    if (inspected.symbolicLink) {
      findings.push(`${file}: 待扫描文件不得使用符号链接`);
      continue;
    }
    if (!inspected.regularFile) continue;

    let buffer;
    if (overlays.has(file)) {
      buffer = Buffer.from(overlays.get(file), 'utf8');
    } else {
      try {
        buffer = fs.readFileSync(inspected.absolutePath);
      } catch (error) {
        throw productError(`无法读取待扫描文件 ${file}：${error.message}`);
      }
    }
    if (hasForbiddenMarker(file)) {
      findings.push(`${file}: 文件路径包含发行版本标识`);
      continue;
    }
    if (buffer.includes(0)) continue;

    buffer
      .toString('utf8')
      .split(/\r?\n/)
      .forEach((line, index) => {
        if (hasForbiddenMarker(line)) findings.push(`${file}:${index + 1}:${line.trim()}`);
      });
  }
  return findings;
}

function validateProjectState(projectRoot, manifestContent, packageContent) {
  const values = parseManifest(projectRoot, manifestContent);
  assertPackageMetadata(projectRoot, values, packageContent);
  const findings = findForbiddenMarkers(
    projectRoot,
    values,
    new Map([
      [manifestFile, manifestContent],
      [packageFile, packageContent],
    ]),
  );
  if (findings.length) {
    throw productError(`发现不应散落维护的发行版本标识：\n${findings.join('\n')}`);
  }
  return values;
}

function replaceManifestValue(content, key, value) {
  const expression = new RegExp(`^${key}=.*$`, 'gm');
  const matches = content.match(expression) || [];
  if (matches.length !== 1) throw productError(`产品配置项 ${key} 必须且只能定义一次`);
  return content.replace(expression, `${key}=${value}`);
}

function replaceFilesAtomically(projectRoot, changes, lock) {
  assertProductConfigLockOwnership(lock);
  const prepared = [];
  const journalPath = getProductConfigJournalPath(projectRoot);
  try {
    for (const { relativePath, content } of changes) {
      const target = path.join(projectRoot, relativePath);
      const original = fs.readFileSync(target, 'utf8');
      const mode = fs.statSync(target).mode;
      const temporary = `${target}.tmp-${process.pid}-${Math.random().toString(16).slice(2)}`;
      const preparedFile = { relativePath, target, temporary, original, next: content, mode };
      prepared.push(preparedFile);
      writeFileDurably(temporary, content, { mode });
    }
    assertProductConfigLockOwnership(lock);
    if (fs.existsSync(journalPath)) {
      throw productError('产品配置事务 journal 尚未恢复，拒绝开始新事务');
    }
    writeJsonAtomically(journalPath, {
      version: 1,
      component: productConfigComponentCode,
      createdAt: Date.now(),
      files: prepared.map(({ relativePath, original, next, mode }) => ({
        relativePath,
        original,
        next,
        mode,
      })),
    });
  } catch (error) {
    for (const item of prepared) {
      removeFileDurably(item.temporary);
    }
    throw productError(`无法准备原子更新：${error.message}`);
  }
  const replaced = [];

  try {
    for (const item of prepared) {
      assertProductConfigLockOwnership(lock);
      fs.renameSync(item.temporary, item.target);
      replaced.push(item);
      fsyncDirectory(path.dirname(item.target));
    }
    assertProductConfigLockOwnership(lock);
    removeProductConfigJournal(journalPath);
  } catch (error) {
    if (error.productConfigJournalRemoved) {
      throw productError(`原子更新已提交，但无法确认事务 journal 删除已持久化：${error.message}`);
    }
    const rollbackFiles = [];
    let rollbackError;
    try {
      for (const item of [...replaced].reverse()) {
        try {
          assertProductConfigLockOwnership(lock);
        } catch {
          throw productError(`原子更新失败且锁归属已变化，未执行回滚：${error.message}`);
        }
        const rollback = `${item.target}.rollback-${process.pid}-${Math.random()
          .toString(16)
          .slice(2)}`;
        writeFileDurably(rollback, item.original, { mode: item.mode });
        rollbackFiles.push(rollback);
        try {
          assertProductConfigLockOwnership(lock);
        } catch {
          throw productError(`原子更新失败且锁归属已变化，未执行回滚：${error.message}`);
        }
        renameDurably(rollback, item.target);
      }
      assertProductConfigLockOwnership(lock);
      removeProductConfigJournal(journalPath);
    } catch (currentRollbackError) {
      rollbackError = currentRollbackError;
    } finally {
      for (const rollback of rollbackFiles) {
        removeFileDurably(rollback);
      }
    }
    if (rollbackError) {
      if (rollbackError.message.includes('锁归属已变化，未执行回滚')) throw rollbackError;
      throw productError(`原子更新失败且回滚未完成：${error.message}；${rollbackError.message}`);
    }
    throw productError(`原子更新失败，已回滚：${error.message}`);
  } finally {
    for (const item of prepared) {
      removeFileDurably(item.temporary);
    }
  }
}

export function checkProductConfig(projectRoot = defaultProjectRoot) {
  const manifestContent = readProjectFile(projectRoot, manifestFile);
  const packageContent = readProjectFile(projectRoot, packageFile);
  const values = validateProjectState(projectRoot, manifestContent, packageContent);
  console.log('产品配置、版本引用、未跟踪文件和发行边界检查通过。');
  return values;
}

export function renderProductConfig(projectRoot = defaultProjectRoot) {
  return withProductConfigLock(projectRoot, (lock) => {
    const manifestContent = readProjectFile(projectRoot, manifestFile);
    const values = parseManifest(projectRoot, manifestContent);
    const packageContent = buildPackageContent(projectRoot, values);
    validateProjectState(projectRoot, manifestContent, packageContent);
    replaceFilesAtomically(
      projectRoot,
      [{ relativePath: packageFile, content: packageContent }],
      lock,
    );
    console.log(`已根据 ${manifestFile} 原子刷新 ${packageFile}。`);
    return values;
  });
}

export function setVersion(projectRoot = defaultProjectRoot, version) {
  validateProductVersion(version || '');
  return withProductConfigLock(projectRoot, (lock) => {
    const currentManifest = readProjectFile(projectRoot, manifestFile);
    const nextManifest = replaceManifestValue(
      currentManifest,
      'THINGLINKS_COMPONENT_VERSION',
      version,
    );
    const values = parseManifest(projectRoot, nextManifest);
    const nextPackage = buildPackageContent(projectRoot, values);
    validateProjectState(projectRoot, nextManifest, nextPackage);
    replaceFilesAtomically(
      projectRoot,
      [
        { relativePath: manifestFile, content: nextManifest },
        { relativePath: packageFile, content: nextPackage },
      ],
      lock,
    );
    console.log(`已将大屏版本原子更新为 ${version}。`);
    return values;
  });
}

export function runCli(args = process.argv.slice(2), projectRoot = defaultProjectRoot) {
  const command = args[0] || 'check';
  switch (command) {
    case 'check':
      return checkProductConfig(projectRoot);
    case 'render':
      return renderProductConfig(projectRoot);
    case 'set-version':
      if (!args[1]) throw productError('用法：pnpm product:set-version <大屏新版本>');
      return setVersion(projectRoot, args[1]);
    default:
      throw productError(`未知命令：${command}；可用命令：check、render、set-version`);
  }
}

const invokedPath = process.argv[1] ? pathToFileURL(path.resolve(process.argv[1])).href : '';
if (import.meta.url === invokedPath) {
  try {
    runCli();
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}
