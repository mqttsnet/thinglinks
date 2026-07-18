import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const commandStylePattern =
  /(?:不得|必须|禁止|不要|建议|一定要|修改后请|后续迁移|设置.+即可|同步社区|整改)/;

test('全部已跟踪环境文件的每个配置项均有相邻的中性中文用途注释', () => {
  const envFiles = execFileSync('git', ['ls-files', '-z', '--', '.env*'], {
    cwd: projectRoot,
  })
    .toString('utf8')
    .split('\0')
    .filter(Boolean);

  assert.ok(envFiles.length > 0);
  for (const relativePath of envFiles) {
    const lines = fs.readFileSync(path.join(projectRoot, relativePath), 'utf8').split(/\r?\n/);
    for (const [index, line] of lines.entries()) {
      const assignment = line.match(/^\s*([A-Z][A-Z0-9_]*)\s*=/);
      if (!assignment) continue;
      const comment = lines[index - 1]?.trim() || '';
      assert.match(
        comment,
        /^#.*[\u3400-\u9fff]/,
        `${relativePath}:${index + 1} 配置项 ${assignment[1]} 缺少相邻中文注释`,
      );
      assert.doesNotMatch(
        comment,
        commandStylePattern,
        `${relativePath}:${index} 配置项 ${assignment[1]} 的注释不是中性用途描述`,
      );
    }
  }
});
