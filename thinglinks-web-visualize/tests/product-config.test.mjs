import assert from 'node:assert/strict';
import childProcess from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';

import { checkProductConfig, runCli } from '../scripts/product-config.mjs';

const { execFileSync } = childProcess;
const PRODUCT_LOCK_REF = 'refs/thinglinks/product-config-lock/thinglinks-web-visualize';
const OPEN_SOURCE_PACKAGE_LICENSE = 'Apache-2.0';
const LICENSE_FILE_PACKAGE_LICENSE = 'SEE LICENSE IN LICENSE-COMMERCIAL';
const PRODUCT_JOURNAL_FILE = 'thinglinks-product-config-thinglinks-web-visualize.journal.json';

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
# 清单版本
THINGLINKS_PRODUCT_MANIFEST_VERSION=1
# 仓库独立维护路径
THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL
`;

function createFixture(t, manifest = MANIFEST) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'thinglinks-product-config-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  fs.writeFileSync(path.join(root, '.thinglinks-product.env'), manifest);
  fs.writeFileSync(
    path.join(root, 'package.json'),
    `${JSON.stringify(
      {
        name: 'thinglinks-web-visualize',
        version: '1.0.8',
        license: manifest.includes('THINGLINKS_LICENSE_MODEL=open-source')
          ? OPEN_SOURCE_PACKAGE_LICENSE
          : LICENSE_FILE_PACKAGE_LICENSE,
      },
      null,
      2,
    )}\n`,
  );
  fs.writeFileSync(path.join(root, 'LICENSE'), 'Apache License');
  const trackedFiles = ['.thinglinks-product.env', 'package.json', 'LICENSE'];
  if (manifest.includes('LICENSE-COMMERCIAL')) {
    fs.writeFileSync(path.join(root, 'LICENSE-COMMERCIAL'), 'Commercial license');
    trackedFiles.push('LICENSE-COMMERCIAL');
  }
  execFileSync('git', ['init', '--quiet'], { cwd: root });
  execFileSync('git', ['add', ...trackedFiles], { cwd: root });
  return root;
}

function writeProductLockOwner(root, owner) {
  return execFileSync('git', ['hash-object', '-w', '--stdin'], {
    cwd: root,
    encoding: 'utf8',
    input: `${JSON.stringify(owner)}\n`,
  }).trim();
}

function updateProductLockRef(root, oid, oldOid = '') {
  execFileSync('git', ['update-ref', PRODUCT_LOCK_REF, oid, oldOid], {
    cwd: root,
    stdio: ['ignore', 'pipe', 'pipe'],
  });
}

function getProductLockOid(root) {
  try {
    return execFileSync('git', ['rev-parse', '--verify', PRODUCT_LOCK_REF], {
      cwd: root,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
    }).trim();
  } catch {
    return null;
  }
}

function writeProductLock(root, pid, token) {
  const oid = writeProductLockOwner(root, { pid, token, createdAt: Date.now() });
  updateProductLockRef(root, oid);
  return oid;
}

function getDeadPid() {
  const candidate = Math.max(999_999, process.pid + 100_000);
  try {
    process.kill(candidate, 0);
  } catch (error) {
    if (error.code === 'ESRCH') return candidate;
    throw error;
  }
  throw new Error(`测试 PID ${candidate} 当前仍存活`);
}

test('非法版本在写入前失败且不污染清单和 package.json', (t) => {
  const root = createFixture(t);
  const manifestBefore = fs.readFileSync(path.join(root, '.thinglinks-product.env'), 'utf8');
  const packageBefore = fs.readFileSync(path.join(root, 'package.json'), 'utf8');

  assert.throws(() => runCli(['set-version', 'not-a-version'], root), /版本号格式不正确/);
  assert.equal(fs.readFileSync(path.join(root, '.thinglinks-product.env'), 'utf8'), manifestBefore);
  assert.equal(fs.readFileSync(path.join(root, 'package.json'), 'utf8'), packageBefore);
});

test('合法版本一次更新清单和 package.json', (t) => {
  const root = createFixture(t);

  runCli(['set-version', '1.2.3-beta.1'], root);

  assert.match(
    fs.readFileSync(path.join(root, '.thinglinks-product.env'), 'utf8'),
    /^THINGLINKS_COMPONENT_VERSION=1\.2\.3-beta\.1$/m,
  );
  assert.equal(
    JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8')).version,
    '1.2.3-beta.1',
  );
});

test('版本号严格遵循 SemVer，并接受构建元数据', (t) => {
  const root = createFixture(t);

  for (const version of ['1.2.3-.', '1.2.3-foo..bar', '01.2.3', '1.02.3']) {
    assert.throws(() => runCli(['set-version', version], root), /版本号格式不正确/);
  }

  assert.doesNotThrow(() => runCli(['set-version', '1.2.3-beta.1+build.7'], root));
  assert.equal(
    JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8')).version,
    '1.2.3-beta.1+build.7',
  );
});

test('render 和 set-version 在读取清单前拒绝活进程持有的仓库锁', (t) => {
  const commands = [['render'], ['set-version', '1.2.3']];

  for (const [index, command] of commands.entries()) {
    const root = createFixture(t);
    const lockOid = writeProductLock(root, process.pid, `live-${index}`);
    fs.rmSync(path.join(root, '.thinglinks-product.env'));

    assert.throws(() => runCli(command, root), new RegExp(`进程 ${process.pid}`));
    assert.equal(getProductLockOid(root), lockOid);
  }
});

test('render 和 set-version 以 CAS 回收死进程锁且成功后无锁引用残留', (t) => {
  const commands = [['render'], ['set-version', '1.2.3']];

  for (const [index, command] of commands.entries()) {
    const root = createFixture(t);
    writeProductLock(root, getDeadPid(), `stale-${index}`);

    assert.doesNotThrow(() => runCli(command, root));
    assert.equal(getProductLockOid(root), null);
  }
});

test('render 和 set-version 异常退出时按自身 OID 清理仓库锁引用', (t) => {
  const commands = [['render'], ['set-version', '1.2.3']];

  for (const [index, command] of commands.entries()) {
    const root = createFixture(t);
    writeProductLock(root, getDeadPid(), `stale-error-${index}`);
    fs.writeFileSync(path.join(root, 'package.json'), '{ invalid json');

    assert.throws(() => runCli(command, root), /package\.json 不是合法 JSON/);
    assert.equal(getProductLockOid(root), null);
  }
});

test('死锁回收的 observed OID 变化时不会删除新持有者', (t) => {
  const root = createFixture(t);
  const staleOid = writeProductLock(root, getDeadPid(), 'stale-owner');
  const liveOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'live-owner',
    createdAt: Date.now(),
  });
  const originalExecFileSync = childProcess.execFileSync;
  let ownerChanged = false;

  childProcess.execFileSync = function (file, args, options) {
    if (
      !ownerChanged &&
      file === 'git' &&
      args[0] === 'update-ref' &&
      args[1] === '-d' &&
      args[2] === PRODUCT_LOCK_REF &&
      args[3] === staleOid
    ) {
      originalExecFileSync('git', ['update-ref', PRODUCT_LOCK_REF, liveOid, staleOid], {
        cwd: root,
        stdio: ['ignore', 'pipe', 'pipe'],
      });
      ownerChanged = true;
    }
    return originalExecFileSync(file, args, options);
  };

  try {
    assert.throws(() => runCli(['render'], root), new RegExp(`进程 ${process.pid}`));
  } finally {
    childProcess.execFileSync = originalExecFileSync;
  }

  assert.equal(ownerChanged, true);
  assert.equal(getProductLockOid(root), liveOid);
});

test('无锁引用时两个 owner blob 只有一个能以空旧值完成 CAS 抢占', (t) => {
  const root = createFixture(t);
  const firstOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'first-contender',
    createdAt: Date.now(),
  });
  const secondOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'second-contender',
    createdAt: Date.now(),
  });

  updateProductLockRef(root, firstOid);
  assert.throws(() => updateProductLockRef(root, secondOid), /Command failed/);
  assert.equal(getProductLockOid(root), firstOid);
});

test('首次 create-CAS 失败且竞争者随即释放时有界重试成功并清理锁引用', (t) => {
  const root = createFixture(t);
  const contenderOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'transient-contender',
    createdAt: Date.now(),
  });
  const originalExecFileSync = childProcess.execFileSync;
  let claimAttempts = 0;
  let injectedRace = false;

  childProcess.execFileSync = function (file, args, options) {
    const isCreateClaim =
      file === 'git' && args[0] === 'update-ref' && args[1] === PRODUCT_LOCK_REF && args[3] === '';
    if (isCreateClaim) {
      claimAttempts += 1;
      if (!injectedRace) {
        originalExecFileSync('git', ['update-ref', PRODUCT_LOCK_REF, contenderOid, ''], {
          cwd: root,
          stdio: ['ignore', 'pipe', 'pipe'],
        });
        injectedRace = true;
        try {
          return originalExecFileSync(file, args, options);
        } finally {
          originalExecFileSync('git', ['update-ref', '-d', PRODUCT_LOCK_REF, contenderOid], {
            cwd: root,
            stdio: ['ignore', 'pipe', 'pipe'],
          });
        }
      }
    }
    return originalExecFileSync(file, args, options);
  };

  try {
    assert.doesNotThrow(() => runCli(['render'], root));
  } finally {
    childProcess.execFileSync = originalExecFileSync;
  }

  assert.equal(injectedRace, true);
  assert.equal(claimAttempts, 2);
  assert.equal(getProductLockOid(root), null);
});

test('create-CAS 持续失败且锁引用为空时达到上限后明确报错', (t) => {
  const root = createFixture(t);
  const originalExecFileSync = childProcess.execFileSync;
  let claimAttempts = 0;

  childProcess.execFileSync = function (file, args, options) {
    const isCreateClaim =
      file === 'git' && args[0] === 'update-ref' && args[1] === PRODUCT_LOCK_REF && args[3] === '';
    if (isCreateClaim) {
      claimAttempts += 1;
      const error = new Error('synthetic permission denied');
      error.status = 128;
      throw error;
    }
    return originalExecFileSync(file, args, options);
  };

  try {
    assert.throws(
      () => runCli(['render'], root),
      /连续 4 次 create-CAS 失败.*synthetic permission denied/,
    );
  } finally {
    childProcess.execFileSync = originalExecFileSync;
  }

  assert.equal(claimAttempts, 4);
  assert.equal(getProductLockOid(root), null);
});

test('写入前锁引用已切换时旧 writer 不修改文件且保留新 owner', (t) => {
  const root = createFixture(t);
  const packagePath = path.join(root, 'package.json');
  const packageBefore = fs.readFileSync(packagePath, 'utf8');
  const liveOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'replacement-owner',
    createdAt: Date.now(),
  });
  const originalExecFileSync = childProcess.execFileSync;
  let ownerChanged = false;

  childProcess.execFileSync = function (file, args, options) {
    if (!ownerChanged && file === 'git' && args[0] === 'ls-files') {
      const result = originalExecFileSync(file, args, options);
      const claimedOid = originalExecFileSync('git', ['rev-parse', '--verify', PRODUCT_LOCK_REF], {
        cwd: root,
        encoding: 'utf8',
        stdio: ['ignore', 'pipe', 'pipe'],
      }).trim();
      originalExecFileSync('git', ['update-ref', PRODUCT_LOCK_REF, liveOid, claimedOid], {
        cwd: root,
        stdio: ['ignore', 'pipe', 'pipe'],
      });
      ownerChanged = true;
      return result;
    }
    return originalExecFileSync(file, args, options);
  };

  try {
    assert.throws(() => runCli(['render'], root), /产品配置锁归属在执行期间发生变化/);
  } finally {
    childProcess.execFileSync = originalExecFileSync;
  }

  assert.equal(ownerChanged, true);
  assert.equal(fs.readFileSync(packagePath, 'utf8'), packageBefore);
  assert.equal(getProductLockOid(root), liveOid);
});

test('临时文件准备后锁引用切换时每次 rename 前阻止旧 writer 落盘', (t) => {
  const root = createFixture(t);
  const manifestPath = path.join(root, '.thinglinks-product.env');
  const packagePath = path.join(root, 'package.json');
  const manifestBefore = fs.readFileSync(manifestPath, 'utf8');
  const packageBefore = fs.readFileSync(packagePath, 'utf8');
  const liveOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'prepared-replacement',
    createdAt: Date.now(),
  });
  const originalWriteFileSync = fs.writeFileSync;
  let ownerChanged = false;

  fs.writeFileSync = function (file, data, options) {
    const result = originalWriteFileSync(file, data, options);
    if (
      !ownerChanged &&
      typeof file === 'string' &&
      file.startsWith(root) &&
      file.includes('.tmp-')
    ) {
      const claimedOid = getProductLockOid(root);
      updateProductLockRef(root, liveOid, claimedOid);
      ownerChanged = true;
    }
    return result;
  };

  try {
    assert.throws(() => runCli(['set-version', '1.2.3'], root), /产品配置锁归属在执行期间发生变化/);
  } finally {
    fs.writeFileSync = originalWriteFileSync;
  }

  assert.equal(ownerChanged, true);
  assert.equal(fs.readFileSync(manifestPath, 'utf8'), manifestBefore);
  assert.equal(fs.readFileSync(packagePath, 'utf8'), packageBefore);
  assert.equal(
    fs.readdirSync(root).some((entry) => entry.includes('.tmp-') || entry.includes('.rollback-')),
    false,
  );
  assert.equal(getProductLockOid(root), liveOid);
});

test('首个 rename 后锁引用切换时不再写后续目标或执行无锁回滚', (t) => {
  const root = createFixture(t);
  const manifestPath = path.join(root, '.thinglinks-product.env');
  const packagePath = path.join(root, 'package.json');
  const packageBefore = fs.readFileSync(packagePath, 'utf8');
  const liveOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'rename-replacement',
    createdAt: Date.now(),
  });
  const originalRenameSync = fs.renameSync;
  let ownerChanged = false;

  fs.renameSync = function (source, target) {
    const result = originalRenameSync(source, target);
    if (
      !ownerChanged &&
      typeof source === 'string' &&
      source.includes('.tmp-') &&
      target === manifestPath
    ) {
      const claimedOid = getProductLockOid(root);
      updateProductLockRef(root, liveOid, claimedOid);
      ownerChanged = true;
    }
    return result;
  };

  try {
    assert.throws(() => runCli(['set-version', '1.2.3'], root), /锁归属已变化.*未执行回滚/);
  } finally {
    fs.renameSync = originalRenameSync;
  }

  assert.equal(ownerChanged, true);
  assert.match(fs.readFileSync(manifestPath, 'utf8'), /^THINGLINKS_COMPONENT_VERSION=1\.2\.3$/m);
  assert.equal(fs.readFileSync(packagePath, 'utf8'), packageBefore);
  assert.equal(
    fs.readdirSync(root).some((entry) => entry.includes('.tmp-') || entry.includes('.rollback-')),
    false,
  );
  assert.equal(getProductLockOid(root), liveOid);
});

test('未完成的多文件更新由持久 journal 在下一次写入前恢复', (t) => {
  const root = createFixture(t);
  const manifestPath = path.join(root, '.thinglinks-product.env');
  const packagePath = path.join(root, 'package.json');
  const gitDir = execFileSync('git', ['rev-parse', '--git-dir'], {
    cwd: root,
    encoding: 'utf8',
  }).trim();
  const journalPath = path.resolve(root, gitDir, PRODUCT_JOURNAL_FILE);
  const originalRenameSync = fs.renameSync;
  let packageWriteFailed = false;

  fs.renameSync = function (source, target) {
    if (target === packagePath && String(source).includes('.tmp-')) {
      packageWriteFailed = true;
      throw new Error('synthetic package rename failure');
    }
    if (packageWriteFailed && target === manifestPath && String(source).includes('.rollback-')) {
      throw new Error('synthetic rollback failure');
    }
    return originalRenameSync(source, target);
  };

  try {
    assert.throws(() => runCli(['set-version', '1.2.3'], root), /回滚未完成/);
  } finally {
    fs.renameSync = originalRenameSync;
  }

  assert.equal(fs.existsSync(journalPath), true);
  assert.match(fs.readFileSync(manifestPath, 'utf8'), /^THINGLINKS_COMPONENT_VERSION=1\.2\.3$/m);

  assert.doesNotThrow(() => runCli(['render'], root));
  assert.equal(fs.existsSync(journalPath), false);
  assert.match(fs.readFileSync(manifestPath, 'utf8'), /^THINGLINKS_COMPONENT_VERSION=1\.0\.8$/m);
  assert.equal(JSON.parse(fs.readFileSync(packagePath, 'utf8')).version, '1.0.8');
});

test('release 的 delete-CAS 失败时报告归属变化并保留新 owner', (t) => {
  const root = createFixture(t);
  const liveOid = writeProductLockOwner(root, {
    pid: process.pid,
    token: 'release-replacement',
    createdAt: Date.now(),
  });
  const originalExecFileSync = childProcess.execFileSync;
  let ownerChanged = false;

  childProcess.execFileSync = function (file, args, options) {
    if (
      !ownerChanged &&
      file === 'git' &&
      args[0] === 'update-ref' &&
      args[1] === '-d' &&
      args[2] === PRODUCT_LOCK_REF
    ) {
      const owner = JSON.parse(
        originalExecFileSync('git', ['cat-file', 'blob', args[3]], {
          cwd: root,
          encoding: 'utf8',
          stdio: ['ignore', 'pipe', 'pipe'],
        }),
      );
      assert.equal(owner.pid, process.pid);
      assert.equal(typeof owner.token, 'string');
      assert.equal(Number.isSafeInteger(owner.createdAt), true);
      originalExecFileSync('git', ['update-ref', PRODUCT_LOCK_REF, liveOid, args[3]], {
        cwd: root,
        stdio: ['ignore', 'pipe', 'pipe'],
      });
      ownerChanged = true;
    }
    return originalExecFileSync(file, args, options);
  };

  try {
    assert.throws(() => runCli(['render'], root), /产品配置锁归属在执行期间发生变化/);
  } finally {
    childProcess.execFileSync = originalExecFileSync;
  }

  assert.equal(ownerChanged, true);
  assert.equal(getProductLockOid(root), liveOid);
});

test('检查覆盖未跟踪且未忽略的发行标识文件', (t) => {
  const root = createFixture(t);
  fs.writeFileSync(path.join(root, 'local-note.md'), '临时说明：ThingLinks-Web-Pro');

  assert.throws(() => checkProductConfig(root), /local-note\.md/);
});

test('检查识别 BifroMQ 插件旧发行名称', (t) => {
  const root = createFixture(t);
  fs.writeFileSync(path.join(root, 'plugin-note.md'), '旧名称：BifroMQ-Plugin-Pro');

  assert.throws(() => checkProductConfig(root), /plugin-note\.md/);
});

test('检查识别代码命名和中英文发行版本标识', (t) => {
  const markers = [
    'class WebProConfig {}',
    'class WebProFeature {}',
    'const webProConfig = {};',
    'class ThingLinksJobProFeature {}',
    'class BifromqPluginProAuth {}',
    'const thinglinksProConfig = {};',
    'const thinglinksproConfig = {};',
    'const thinglinksJobProFeature = {};',
    'const webproConfig = {};',
    'const cloudproConfig = {};',
    'const utilproConfig = {};',
    'const bifromqPluginProAuth = {};',
    'const marker = "CloudPro";',
    'ThingLinksPro,',
    'const packageName = "thinglinks-pro";',
    'thinglinks_pro,',
    'thinglinkspro;',
    'console.log(data, "data-pro");',
    'console.debug(payload, "payload_pro");',
    '旧模块：thinglinks-job-pro',
    '商业版',
    'Commercial Edition',
    '社区版',
    'Community Edition',
    '开源版',
    'Pro Edition',
    'Professional Edition',
    'Open Source Edition',
  ];

  for (const [index, marker] of markers.entries()) {
    const root = createFixture(t);
    const file = `marker-${index}.txt`;
    fs.writeFileSync(path.join(root, file), marker);
    assert.throws(() => checkProductConfig(root), new RegExp(file));
  }
});

test('检查识别日文和韩文发行版本标识', (t) => {
  const markers = ['コミュニティ版', 'エンタープライズ版', '상용 버전', '커뮤니티 에디션'];

  for (const [index, marker] of markers.entries()) {
    const root = createFixture(t);
    fs.writeFileSync(path.join(root, `localized-marker-${index}.md`), marker);
    assert.throws(() => checkProductConfig(root), new RegExp(`localized-marker-${index}\\.md`));
  }
});

test('以 Pro 开头的普通单词不会被误判为发行标识', (t) => {
  const root = createFixture(t);
  fs.writeFileSync(
    path.join(root, 'protocol-note.md'),
    [
      'thinglinks-protocol-starter ProtocolMessageAdapter protocol product production',
      'Product Professional Protocol Project Process Properties',
      'ThingLinksProduct ThingLinksProfessional ThingLinksProtocol ThingLinksProject ThingLinksProcess ThingLinksProperties',
      'thinglinksProduct thinglinksProfessional thinglinksProtocol thinglinksProject thinglinksProcess thinglinksProperties',
      'thinglinksproduct thinglinksprofessional thinglinksprotocol thinglinksproject thinglinksprocess thinglinksproperties',
      'jessibuca-pro Jessibuca-Pro ant-pro-page-container-main',
      'console.log("thinglinks-protocol-starter", "jessibuca-pro", "Professional", "Product");',
    ].join('\n'),
  );

  assert.doesNotThrow(() => checkProductConfig(root));
});

test('同步保护路径中的发行信息不会被当作散落标识', (t) => {
  const root = createFixture(t);
  fs.writeFileSync(path.join(root, 'LICENSE-COMMERCIAL'), 'Enterprise Edition license');
  execFileSync('git', ['add', 'LICENSE-COMMERCIAL'], { cwd: root });

  assert.doesNotThrow(() => checkProductConfig(root));
});

test('同步保护路径拒绝空项、重复项和非规范相对路径', (t) => {
  const current =
    'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL';
  const invalidCases = [
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,,LICENSE,LICENSE-COMMERCIAL',
      message: /同步保护路径不能包含空项/,
    },
    {
      value:
        'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE,LICENSE-COMMERCIAL',
      message: /同步保护路径重复：LICENSE/,
    },
    {
      value:
        'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,/tmp/local,LICENSE-COMMERCIAL',
      message: /同步保护路径不得使用绝对路径/,
    },
    {
      value:
        'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,../local,LICENSE-COMMERCIAL',
      message: /同步保护路径禁止越界或非规范化片段/,
    },
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=./.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL',
      message: /同步保护路径必须使用规范化相对路径/,
    },
    {
      value:
        'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,docs//local,LICENSE-COMMERCIAL',
      message: /同步保护路径禁止越界或非规范化片段/,
    },
    {
      value:
        'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,docs/./local,LICENSE-COMMERCIAL',
      message: /同步保护路径禁止越界或非规范化片段/,
    },
  ];

  for (const { value, message } of invalidCases) {
    const root = createFixture(t, MANIFEST.replace(current, value));
    assert.throws(() => checkProductConfig(root), message);
  }
});

test('同步保护路径拒绝 glob 且必须保护清单和授权文件', (t) => {
  const current =
    'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL';
  const invalidCases = [
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-*',
      message: /不得包含 glob 元字符/,
    },
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=LICENSE-COMMERCIAL',
      message: /必须包含 \.thinglinks-product\.env/,
    },
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE-COMMERCIAL',
      message: /必须包含 LICENSE/,
    },
    {
      value: 'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE',
      message: /必须包含 THINGLINKS_LICENSE_FILE/,
    },
  ];

  for (const { value, message } of invalidCases) {
    const root = createFixture(t, MANIFEST.replace(current, value));
    assert.throws(() => checkProductConfig(root), message);
  }
});

test('同步保护路径中的每个文件或目录均在仓库中存在', (t) => {
  const current =
    'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL';
  const root = createFixture(t, MANIFEST.replace(current, `${current},docs/product-boundary`));

  assert.throws(() => checkProductConfig(root), /同步保护路径不存在：docs\/product-boundary/);
});

test('同步保护路径支持仓库中已存在的文件和目录', (t) => {
  const current =
    'THINGLINKS_SYNC_PROTECTED_PATHS=.thinglinks-product.env,LICENSE,LICENSE-COMMERCIAL';
  const root = createFixture(t, MANIFEST.replace(current, `${current},docs`));
  fs.mkdirSync(path.join(root, 'docs'));

  assert.doesNotThrow(() => checkProductConfig(root));
});

test('授权文件和同步保护路径拒绝符号链接及仓库 realpath 越界', (t) => {
  const root = createFixture(t);
  const outside = fs.mkdtempSync(path.join(os.tmpdir(), 'thinglinks-product-outside-'));
  t.after(() => fs.rmSync(outside, { recursive: true, force: true }));
  fs.writeFileSync(path.join(outside, 'license.txt'), 'Commercial license');
  fs.rmSync(path.join(root, 'LICENSE-COMMERCIAL'));
  fs.symlinkSync(path.join(outside, 'license.txt'), path.join(root, 'LICENSE-COMMERCIAL'));

  assert.throws(() => checkProductConfig(root), /符号链接|仓库边界/);

  fs.rmSync(path.join(root, 'LICENSE-COMMERCIAL'));
  fs.writeFileSync(path.join(root, 'LICENSE-COMMERCIAL'), 'Commercial license');
  fs.symlinkSync(outside, path.join(root, 'protected-docs'));
  const manifest = fs
    .readFileSync(path.join(root, '.thinglinks-product.env'), 'utf8')
    .replace('LICENSE,LICENSE-COMMERCIAL', 'LICENSE,LICENSE-COMMERCIAL,protected-docs');
  fs.writeFileSync(path.join(root, '.thinglinks-product.env'), manifest);

  assert.throws(() => checkProductConfig(root), /符号链接|仓库边界/);
});

test('校验清单格式版本、授权模型和运行标识', (t) => {
  const invalidCases = [
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_PRODUCT_MANIFEST_VERSION=1',
        'THINGLINKS_PRODUCT_MANIFEST_VERSION=2',
      ),
      message: /清单格式版本仅支持 1/,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_LICENSE_MODEL=commercial',
        'THINGLINKS_LICENSE_MODEL=private',
      ),
      message: /授权模型仅允许/,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_WEB_CLIENT_ID=thinglinks_web',
        'THINGLINKS_WEB_CLIENT_ID=thinglinks_pro',
      ),
      message: /THINGLINKS_WEB_CLIENT_ID/,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_MQ_NAMESPACE=thinglinks',
        'THINGLINKS_MQ_NAMESPACE=ThingLinks!',
      ),
      message: /THINGLINKS_MQ_NAMESPACE/,
    },
  ];

  for (const { manifest, message } of invalidCases) {
    const root = createFixture(t, manifest);
    assert.throws(() => checkProductConfig(root), message);
  }
});

test('发行类型与授权模型拒绝不匹配组合', (t) => {
  const invalidManifests = [
    MANIFEST.replace('THINGLINKS_EDITION_CODE=enterprise', 'THINGLINKS_EDITION_CODE=community'),
    MANIFEST.replace('THINGLINKS_LICENSE_MODEL=commercial', 'THINGLINKS_LICENSE_MODEL=open-source'),
  ];

  for (const manifest of invalidManifests) {
    const root = createFixture(t, manifest);
    assert.throws(() => checkProductConfig(root), /发行类型与授权模型组合不匹配/);
  }
});

test('发行类型与授权模型接受支持的组合', (t) => {
  const supportedManifests = [
    MANIFEST.replace('THINGLINKS_EDITION_CODE=enterprise', 'THINGLINKS_EDITION_CODE=community')
      .replace('THINGLINKS_EDITION_NAME_ZH=旗舰版', 'THINGLINKS_EDITION_NAME_ZH=社区版')
      .replace('THINGLINKS_EDITION_NAME_EN=Enterprise', 'THINGLINKS_EDITION_NAME_EN=Community')
      .replace('THINGLINKS_EDITION_NAME_JA=エンタープライズ版', 'THINGLINKS_EDITION_NAME_JA=コミュニティ版')
      .replace('THINGLINKS_LICENSE_MODEL=commercial', 'THINGLINKS_LICENSE_MODEL=open-source')
      .replace('THINGLINKS_LICENSE_FILE=LICENSE-COMMERCIAL', 'THINGLINKS_LICENSE_FILE=LICENSE'),
    MANIFEST.replace(
      'THINGLINKS_EDITION_CODE=enterprise',
      'THINGLINKS_EDITION_CODE=commercial',
    )
      .replace('THINGLINKS_EDITION_NAME_ZH=旗舰版', 'THINGLINKS_EDITION_NAME_ZH=商业版')
      .replace('THINGLINKS_EDITION_NAME_EN=Enterprise', 'THINGLINKS_EDITION_NAME_EN=Commercial')
      .replace('THINGLINKS_EDITION_NAME_JA=エンタープライズ版', 'THINGLINKS_EDITION_NAME_JA=商用版')
      .replace('THINGLINKS_LICENSE_MODEL=commercial', 'THINGLINKS_LICENSE_MODEL=dual-license'),
    MANIFEST.replace(
      'THINGLINKS_LICENSE_MODEL=commercial',
      'THINGLINKS_LICENSE_MODEL=dual-license',
    ),
  ];

  for (const manifest of supportedManifests) {
    const root = createFixture(t, manifest);
    assert.doesNotThrow(() => checkProductConfig(root));
  }
});

test('package license 由授权模型派生，检查拒绝篡改且 render 可恢复', (t) => {
  const cases = [
    {
      manifest: MANIFEST,
      expected: LICENSE_FILE_PACKAGE_LICENSE,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_LICENSE_MODEL=commercial',
        'THINGLINKS_LICENSE_MODEL=dual-license',
      ),
      expected: LICENSE_FILE_PACKAGE_LICENSE,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_EDITION_CODE=enterprise',
        'THINGLINKS_EDITION_CODE=community',
      )
        .replace('THINGLINKS_EDITION_NAME_ZH=旗舰版', 'THINGLINKS_EDITION_NAME_ZH=社区版')
        .replace('THINGLINKS_EDITION_NAME_EN=Enterprise', 'THINGLINKS_EDITION_NAME_EN=Community')
        .replace('THINGLINKS_EDITION_NAME_JA=エンタープライズ版', 'THINGLINKS_EDITION_NAME_JA=コミュニティ版')
        .replace('THINGLINKS_LICENSE_MODEL=commercial', 'THINGLINKS_LICENSE_MODEL=open-source')
        .replace('THINGLINKS_LICENSE_FILE=LICENSE-COMMERCIAL', 'THINGLINKS_LICENSE_FILE=LICENSE'),
      expected: OPEN_SOURCE_PACKAGE_LICENSE,
    },
  ];

  for (const { manifest, expected } of cases) {
    const root = createFixture(t, manifest);
    const packagePath = path.join(root, 'package.json');
    const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
    packageJson.license =
      expected === OPEN_SOURCE_PACKAGE_LICENSE
        ? LICENSE_FILE_PACKAGE_LICENSE
        : OPEN_SOURCE_PACKAGE_LICENSE;
    fs.writeFileSync(packagePath, `${JSON.stringify(packageJson, null, 2)}\n`);

    assert.throws(() => checkProductConfig(root), /package\.json license 应为/);
    runCli(['render'], root);
    assert.equal(JSON.parse(fs.readFileSync(packagePath, 'utf8')).license, expected);
  }
});

test('开源授权文件不得引用缺失的商业附加条款', (t) => {
  const manifest = MANIFEST
    .replace('THINGLINKS_EDITION_CODE=enterprise', 'THINGLINKS_EDITION_CODE=community')
    .replace('THINGLINKS_EDITION_NAME_ZH=旗舰版', 'THINGLINKS_EDITION_NAME_ZH=社区版')
    .replace('THINGLINKS_EDITION_NAME_EN=Enterprise', 'THINGLINKS_EDITION_NAME_EN=Community')
    .replace('THINGLINKS_EDITION_NAME_JA=エンタープライズ版', 'THINGLINKS_EDITION_NAME_JA=コミュニティ版')
    .replace('THINGLINKS_LICENSE_MODEL=commercial', 'THINGLINKS_LICENSE_MODEL=open-source')
    .replace('THINGLINKS_LICENSE_FILE=LICENSE-COMMERCIAL', 'THINGLINKS_LICENSE_FILE=LICENSE')
    .replace(',LICENSE-COMMERCIAL', '');
  const root = createFixture(t, manifest);
  fs.writeFileSync(
    path.join(root, 'LICENSE'),
    'Apache License\nAdditional commercial terms apply — see LICENSE-COMMERCIAL.\n',
  );

  assert.throws(() => checkProductConfig(root), /开源授权文件.*商业附加条款/);
});

test('发行展示名称与发行编码保持固定映射', (t) => {
  const root = createFixture(
    t,
    MANIFEST.replace('THINGLINKS_EDITION_NAME_EN=Enterprise', 'THINGLINKS_EDITION_NAME_EN=Commercial'),
  );

  assert.throws(() => checkProductConfig(root), /发行展示名称与发行编码不匹配/);
});

test('授权文件必须存在且为仓库内普通文件', (t) => {
  const root = createFixture(t);
  fs.rmSync(path.join(root, 'LICENSE-COMMERCIAL'));

  assert.throws(() => checkProductConfig(root), /授权文件不存在/);
});

test('产品清单拒绝未知配置键和非注释语法', (t) => {
  const invalidManifests = [
    {
      manifest: `${MANIFEST}# 未知配置\nTHINGLINKS_UNEXPECTED=value\n`,
      message: /未知配置项 THINGLINKS_UNEXPECTED/,
    },
    {
      manifest: `${MANIFEST}source .env.local\n`,
      message: /不支持的语法/,
    },
    {
      manifest: MANIFEST.replace(
        'THINGLINKS_PRODUCT_NAME=ThingLinks',
        'THINGLINKS_PRODUCT_NAME="ThingLinks\'',
      ),
      message: /引号必须同种且成对闭合/,
    },
  ];

  for (const { manifest, message } of invalidManifests) {
    const root = createFixture(t, manifest);
    assert.throws(() => checkProductConfig(root), message);
  }
});

test('非发行身份字段拒绝独立英文版本词和中文版本标识', (t) => {
  const invalidValues = [
    ['THINGLINKS_PRODUCT_CODE=thinglinks', 'THINGLINKS_PRODUCT_CODE=thinglinks-pro'],
    ['THINGLINKS_PRODUCT_CODE=thinglinks', 'THINGLINKS_PRODUCT_CODE=thinglinkspro'],
    ['THINGLINKS_PRODUCT_NAME=ThingLinks', 'THINGLINKS_PRODUCT_NAME="ThingLinks Community"'],
    [
      'THINGLINKS_PRODUCT_NAME_ZH=ThingLinks物联网平台',
      'THINGLINKS_PRODUCT_NAME_ZH=ThingLinks商业版',
    ],
    ['THINGLINKS_COMPONENT_CODE=thinglinks-web-visualize', 'THINGLINKS_COMPONENT_CODE=thinglinks-commercial'],
    [
      'THINGLINKS_COMPONENT_NAME="ThingLinks Web Visualize"',
      'THINGLINKS_COMPONENT_NAME="ThingLinks Enterprise"',
    ],
    ['THINGLINKS_COMPONENT_NAME="ThingLinks Web Visualize"', 'THINGLINKS_COMPONENT_NAME="ThingLinksWebPro"'],
    [
      'THINGLINKS_NPM_PACKAGE_NAME=thinglinks-web-visualize',
      'THINGLINKS_NPM_PACKAGE_NAME=thinglinks-community',
    ],
    ['THINGLINKS_WEB_CLIENT_ID=thinglinks_web', 'THINGLINKS_WEB_CLIENT_ID=thinglinks_enterprise'],
    ['THINGLINKS_MQ_NAMESPACE=thinglinks', 'THINGLINKS_MQ_NAMESPACE=thinglinks-community'],
  ];

  for (const [currentValue, invalidValue] of invalidValues) {
    const root = createFixture(t, MANIFEST.replace(currentValue, invalidValue));
    assert.throws(() => checkProductConfig(root), /不得包含发行版本标识/);
  }
});

test('稳定身份字段拒绝 Professional Edition 和 Open Source Edition', (t) => {
  const invalidNames = ['ThingLinks Professional Edition', 'ThingLinks Open Source Edition'];

  for (const name of invalidNames) {
    const root = createFixture(
      t,
      MANIFEST.replace('THINGLINKS_PRODUCT_NAME=ThingLinks', `THINGLINKS_PRODUCT_NAME="${name}"`),
    );
    assert.throws(() => checkProductConfig(root), /不得包含发行版本标识/);
  }
});

test('稳定身份字段允许 Professional Platform 和常见 Pro 前缀单词', (t) => {
  const allowedNames = [
    'ThingLinks Professional Platform',
    'ThingLinks Product',
    'ThingLinks Protocol',
    'ThingLinks Project',
    'ThingLinks Process',
    'ThingLinks Properties',
  ];

  for (const name of allowedNames) {
    const manifest = MANIFEST.replace(
      'THINGLINKS_PRODUCT_NAME=ThingLinks',
      `THINGLINKS_PRODUCT_NAME="${name}"`,
    );
    const root = createFixture(t, manifest);
    assert.doesNotThrow(() => checkProductConfig(root));
  }
});
