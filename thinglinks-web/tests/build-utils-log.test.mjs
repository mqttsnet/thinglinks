import assert from 'node:assert/strict';
import test from 'node:test';

import safeConfigLog from '../build/safeConfigLog.cjs';

const { formatConfigOverrideSummary } = safeConfigLog;

test('命令行覆盖日志只包含安全键名和数量，不包含配置值', () => {
  const summary = formatConfigOverrideSummary({
    NODE_ENV: 'production',
    VITE_GLOB_CLIENT_SECRET: 'must-not-appear',
  });

  assert.match(summary, /2 项/);
  assert.match(summary, /NODE_ENV/);
  assert.match(summary, /VITE_GLOB_CLIENT_SECRET/);
  assert.doesNotMatch(summary, /production/);
  assert.doesNotMatch(summary, /must-not-appear/);
});

test('没有命令行覆盖配置时输出零项摘要', () => {
  assert.equal(formatConfigOverrideSummary({}), '命令行参数未覆盖配置文件（0 项）');
});
