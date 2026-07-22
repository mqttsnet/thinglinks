import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';
import { loadProductConfig, toViteProductEnv } from './productConfig';
import safeConfigLog from './safeConfigLog.cjs';

const { formatConfigOverrideSummary } = safeConfigLog;

export function isDevFn(mode: string): boolean {
  return mode === 'development';
}

export function isProdFn(mode: string): boolean {
  return mode === 'production';
}

/**
 * Whether to generate package preview
 */
export function isReportMode(): boolean {
  return process.env.REPORT === 'true';
}

// Read all environment variable configuration files to process.env
export function wrapperEnv(envConf: Recordable): ViteEnv {
  const ret: any = {};

  for (const envName of Object.keys(envConf)) {
    let realName = envConf[envName].replace(/\\n/g, '\n');
    realName = realName === 'true' ? true : realName === 'false' ? false : realName;

    if (envName === 'VITE_PORT') {
      realName = Number(realName);
    }
    if (envName === 'VITE_PROXY' && realName) {
      try {
        realName = JSON.parse(realName.replace(/'/g, '"'));
      } catch (error) {
        realName = '';
      }
    }
    ret[envName] = realName;
    if (typeof realName === 'string') {
      process.env[envName] = realName;
    } else if (typeof realName === 'object') {
      process.env[envName] = JSON.stringify(realName);
    }
  }
  return ret;
}

/**
 * 获取当前环境下生效的配置文件名
 */
function getConfFiles() {
  const script = process.env.npm_lifecycle_script;
  const reg = new RegExp('--mode ([a-z_\\d]+)');
  const result = reg.exec(script as string) as any;
  if (result) {
    const mode = result[1] as string;
    return ['.env', `.env.${mode}`];
  }
  return ['.env', '.env.production'];
}

/**
 * 获取命令行中传入的参数
 */
function getConfByScript() {
  const script = process.env.npm_lifecycle_script;
  const regex = new RegExp('([A-Za-z_\\d]+)=([A-Za-z_\\d]+)', 'g');
  let matcher;
  const scriptEnv = {};
  while ((matcher = regex.exec(script as string)) !== null) {
    if (matcher.index === regex.lastIndex) {
      regex.lastIndex++;
    }

    if (matcher) {
      const key = matcher[1] as string;
      const value = matcher[2] as string;
      scriptEnv[key] = value;
    }
  }

  return scriptEnv;
}

/**
 * Get the environment variables starting with the specified prefix
 * @param match prefix
 * @param confFiles ext
 */
export function getEnvConfig(match = 'VITE_GLOB_', confFiles = getConfFiles()) {
  let envConfig = {};

  // 配置文件中的配置
  // .env.{mode} > .env 中的配置
  confFiles.forEach((item) => {
    try {
      const env = dotenv.parse(fs.readFileSync(path.resolve(process.cwd(), item)));
      envConfig = { ...envConfig, ...env };
    } catch (e) {
      console.error(`Error in parsing ${item}`, e);
    }
  });

  // 命令行中的配置
  const scriptConfig = getConfByScript();
  envConfig = { ...envConfig, ...scriptConfig };
  console.log(formatConfigOverrideSummary(scriptConfig));

  // 产品清单提供构建所需的产品身份与运行标识。
  envConfig = { ...envConfig, ...toViteProductEnv(loadProductConfig()) };

  const reg = new RegExp(`^(${match})`);
  Object.keys(envConfig).forEach((key) => {
    if (!reg.test(key)) {
      Reflect.deleteProperty(envConfig, key);
    }
  });
  return envConfig;
}

/**
 * Get user root directory
 * @param dir file path
 */
export function getRootPath(...dir: string[]) {
  return path.resolve(process.cwd(), ...dir);
}
