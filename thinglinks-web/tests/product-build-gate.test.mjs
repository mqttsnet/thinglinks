import assert from 'node:assert/strict';
import fs from 'node:fs';
import test from 'node:test';

const packageInfo = JSON.parse(
  fs.readFileSync(new URL('../package.json', import.meta.url), 'utf8'),
);

test('所有直接 Vite 构建命令在启动 Vite 前仅执行一次完整产品门禁', () => {
  const buildScripts = [
    'build',
    'build:test',
    'build:boot',
    'build:prod',
    'build:prod:none',
    'build:prod:column',
    'build:prod:datasource',
  ];

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
