import assert from 'node:assert/strict';
import fs from 'node:fs';
import test from 'node:test';

test('版本字段焦点始终对应可维护的文本帮助页', () => {
  const schema = fs.readFileSync(
    new URL('../src/views/devOperation/developer/genProject/genProject.data.tsx', import.meta.url),
    'utf8',
  );
  const view = fs.readFileSync(
    new URL('../src/views/devOperation/developer/genProject/index.vue', import.meta.url),
    'utf8',
  );

  assert.match(schema, /field: 'version'[\s\S]*?changeTab\(e\.target\.id\)/);
  assert.match(view, /key: 'project_version'/);
  assert.match(view, /description: t\('devOperation\.developer\.genProject\.versionGuide'\)/);
  assert.match(view, /v-if="item\.description"/);
});
