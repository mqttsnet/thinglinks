import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const scannedDirectories = ['src', 'build', 'public', 'scripts'];
const excludedDirectories = new Set([
  path.join('public', 'jessibuca'),
  path.join('public', 'resource', 'jessibuca-pro'),
  path.join('public', 'resource', 'tinymce'),
]);
const rootConfigurationFiles = new Set([
  '.editorconfig',
  '.eslintignore',
  '.eslintrc.js',
  '.gitattributes',
  '.gitignore',
  '.gitpod.yml',
  '.npmrc',
  '.prettierignore',
  '.stylelintignore',
  '.thinglinks-product.env',
  '.yarnrc',
  '.yarnrc.yml',
  'Jenkinsfile',
  'index.html',
  'package.json',
  'pnpm-lock.yaml',
  'tsconfig.json',
]);
const textExtensions = new Set([
  '.css',
  '.html',
  '.js',
  '.jsx',
  '.json',
  '.less',
  '.md',
  '.mjs',
  '.scss',
  '.ts',
  '.tsx',
  '.txt',
  '.vue',
  '.yaml',
  '.yml',
]);
const secretPatterns = [
  { name: 'Google API Key', pattern: /\bAIza[0-9A-Za-z_-]{35}\b/g },
  { name: 'AWS Access Key', pattern: /\b(?:AKIA|ASIA)[0-9A-Z]{16}\b/g },
  { name: 'GitHub Token', pattern: /\bgh[pousr]_[0-9A-Za-z]{30,}\b/g },
  {
    name: 'Private Key',
    pattern:
      /-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----[\s\S]*?-----END (?:RSA |EC |OPENSSH )?PRIVATE KEY-----/g,
  },
];

function isWithin(relativePath, relativeDirectory) {
  return (
    relativePath === relativeDirectory || relativePath.startsWith(`${relativeDirectory}${path.sep}`)
  );
}

function isRootConfiguration(relativePath) {
  if (relativePath.includes(path.sep)) return false;
  return (
    relativePath === '.env' ||
    relativePath.startsWith('.env.') ||
    rootConfigurationFiles.has(relativePath) ||
    /\.config\.(?:cjs|js|mjs|ts)$/.test(relativePath) ||
    /^(?:jsconfig|tsconfig)(?:\.[^.]+)?\.json$/.test(relativePath)
  );
}

function listScannedFiles() {
  const repositoryFiles = execFileSync('git', ['ls-files', '-co', '--exclude-standard', '-z'], {
    cwd: projectRoot,
  })
    .toString('utf8')
    .split('\0')
    .filter(Boolean)
    .map((relativePath) => path.normalize(relativePath));

  return repositoryFiles.filter((relativePath) => {
    if ([...excludedDirectories].some((directory) => isWithin(relativePath, directory))) {
      return false;
    }
    const inScannedDirectory = scannedDirectories.some((directory) =>
      isWithin(relativePath, directory),
    );
    if (!inScannedDirectory && !isRootConfiguration(relativePath)) return false;
    const absolutePath = path.join(projectRoot, relativePath);
    return (
      fs.existsSync(absolutePath) &&
      fs.lstatSync(absolutePath).isFile() &&
      (isRootConfiguration(relativePath) ||
        textExtensions.has(path.extname(relativePath).toLowerCase()))
    );
  });
}

test('自有前端源码、脚本和工程配置不包含高可信明文密钥', () => {
  const findings = [];
  for (const relativePath of listScannedFiles()) {
    const content = fs.readFileSync(path.join(projectRoot, relativePath), 'utf8');
    for (const { name, pattern } of secretPatterns) {
      pattern.lastIndex = 0;
      if (pattern.test(content)) findings.push(`${relativePath}: ${name}`);
    }
  }
  assert.deepEqual(findings, []);
});

test('Google 地图使用环境变量并为缺失配置和加载失败提供三语言提示', () => {
  const component = fs.readFileSync(
    path.join(projectRoot, 'src/views/demo/charts/map/Google.vue'),
    'utf8',
  );
  const env = fs.readFileSync(path.join(projectRoot, '.env'), 'utf8');
  const globalTypes = fs.readFileSync(path.join(projectRoot, 'types/global.d.ts'), 'utf8');

  assert.match(component, /import\.meta\.env\.VITE_GOOGLE_MAPS_API_KEY\?\.trim\(\)/);
  assert.doesNotMatch(component, /key=AIza/);
  assert.match(component, /lat:\s*39\.915,\s*lng:\s*116\.404/);
  assert.match(component, /demo\.charts\.googleMap\.missingApiKey/);
  assert.match(component, /demo\.charts\.googleMap\.loadFailed/);
  assert.match(env, /^VITE_GOOGLE_MAPS_API_KEY=\s*$/m);
  assert.match(globalTypes, /VITE_GOOGLE_MAPS_API_KEY\?:\s*string/);

  for (const locale of ['zh-CN', 'en-US', 'ja']) {
    const messages = fs.readFileSync(
      path.join(projectRoot, `src/locales/lang/${locale}/demo/charts/googleMap.ts`),
      'utf8',
    );
    assert.match(messages, /missingApiKey:/);
    assert.match(messages, /loadFailed:/);
  }
});
