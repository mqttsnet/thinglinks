import assert from 'node:assert/strict';
import test from 'node:test';

import { buildDruidUrl, resolveApiOrigin } from '../src/views/devOperation/developer/db/url.mjs';

test('生产环境未配置 VITE_PROXY 时回退到当前站点', () => {
  assert.equal(
    resolveApiOrigin(undefined, 'https://console.example.com'),
    'https://console.example.com',
  );
  assert.equal(
    buildDruidUrl('system', undefined, 'https://console.example.com'),
    'https://console.example.com/system/druid/index.html',
  );
});

test('代理配置不是合法 JSON 时回退到当前站点', () => {
  assert.equal(
    resolveApiOrigin('not-json', 'https://console.example.com'),
    'https://console.example.com',
  );
});

test('优先使用 /api 代理目标并规范路径斜杠', () => {
  const proxy = JSON.stringify([
    ['/basic-api', 'https://mock.example.com'],
    ['/api', 'http://127.0.0.1:18760/'],
  ]);

  assert.equal(
    buildDruidUrl('/system/', proxy, 'https://console.example.com/'),
    'http://127.0.0.1:18760/system/druid/index.html',
  );
});
