import assert from 'node:assert/strict';
import fs from 'node:fs';
import test from 'node:test';

const packageInfo = JSON.parse(
  fs.readFileSync(new URL('../package.json', import.meta.url), 'utf8'),
);

test('所有直接 Vite 构建命令在启动 Vite 前仅执行一次完整产品门禁', () => {
  const buildScripts = ['build'];

  for (const scriptName of buildScripts) {
    const command = packageInfo.scripts[scriptName];
    assert.equal(
      command.match(/pnpm product:check/g)?.length,
      1,
      `${scriptName} 必须且只能执行一次产品门禁`,
    );
    assert.ok(
      command.indexOf('pnpm product:check') < command.indexOf('vite build'),
      `${scriptName} 必须在 Vite 启动前执行产品门禁`,
    );
  }
});

test('工程提供独立且不可跳过的类型检查命令', () => {
  assert.equal(packageInfo.scripts['type:check'], 'vue-tsc --noEmit');
});

test('生产构建默认关闭 mock，Iframe 示例地址与统一站点一致', () => {
  const viteSource = fs.readFileSync(new URL('../vite.config.ts', import.meta.url), 'utf8');
  const iframeSource = fs.readFileSync(
    new URL('../src/packages/components/Informations/Mores/Iframe/config.ts', import.meta.url),
    'utf8',
  );

  assert.match(viteSource, /prodEnabled:\s*false/);
  assert.match(iframeSource, /https:\/\/thinglinks\.mqttsnet\.com/);
});

test('客户端标识只由产品清单注入，环境文件不重复维护', () => {
  const envSource = fs.readFileSync(new URL('../.env', import.meta.url), 'utf8');
  assert.doesNotMatch(envSource, /^VITE_GLOB_CLIENT_ID=/m);
});

test('社区授权文件和多语言说明只声明 Apache 2.0', () => {
  const licenseSource = fs.readFileSync(new URL('../LICENSE', import.meta.url), 'utf8');
  assert.match(licenseSource, /^\s*Apache License/m);
  assert.doesNotMatch(licenseSource, /LICENSE-COMMERCIAL|Additional commercial terms/i);

  for (const file of ['README.md', 'README.zh-CN.md', 'README.ja.md', 'README.ko.md']) {
    const source = fs.readFileSync(new URL(`../${file}`, import.meta.url), 'utf8');
    assert.doesNotMatch(source, /additional terms|附加条款|追加条項|추가 약관/i, file);
  }
});
