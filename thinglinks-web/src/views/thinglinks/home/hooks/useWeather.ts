import { onMounted, reactive, toRefs } from 'vue';
import md5 from 'crypto-js/md5';
import { useDict } from '/@/components/Dict';
import { DictEnum } from '/@/enums/commonEnum';
import { anyonePage as queryLoginLogPage } from '/@/api/basic/system/baseLoginLog';

export type WeatherCode = 'sunny' | 'cloudy' | 'overcast' | 'rain' | 'snow' | 'thunder' | 'fog';

interface WeatherState {
  loading: boolean;
  error: boolean;
  city: string;
  temp: string;
  phrase: string;
  code: WeatherCode;
}

interface AmapWebCreds {
  key: string;
  signKey: string;
}

const CN_TO_CODE: Array<[RegExp, WeatherCode]> = [
  [/雷/, 'thunder'],
  [/雪|冰雹/, 'snow'],
  [/雨/, 'rain'],
  [/雾|霾|沙|尘/, 'fog'],
  [/多云/, 'cloudy'],
  [/阴/, 'overcast'],
];

const AMAP_WEB_CRED_CACHE = 'thinglinks:home:amapWebCreds';
const DEFAULT_FALLBACK = { adcode: '310000', city: '上海市' };
const DICT_FETCH_TIMEOUT_MS = 3000;

function withTimeout<T>(p: Promise<T>, ms: number): Promise<T> {
  return Promise.race([
    p,
    new Promise<T>((_, reject) => setTimeout(() => reject(new Error('timeout')), ms)),
  ]);
}

function mapWeather(phrase: string): WeatherCode {
  for (const [re, code] of CN_TO_CODE) {
    if (re.test(phrase)) return code;
  }
  return 'sunny';
}

// 高德 Web Services 签名规则：
// 按 key 字典序排序参数 → "k1=v1&k2=v2" → 末尾拼 signKey → MD5 → 小写
function signParams(params: Record<string, string>, signKey: string): string {
  const sorted = Object.keys(params).sort();
  const joined = sorted.map((k) => `${k}=${params[k]}`).join('&') + signKey;
  return md5(joined).toString();
}

function buildUrl(path: string, params: Record<string, string>, creds: AmapWebCreds): string {
  const finalParams: Record<string, string> = { ...params, key: creds.key };
  if (creds.signKey) {
    finalParams.sig = signParams(finalParams, creds.signKey);
  }
  const qs = Object.keys(finalParams)
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(finalParams[k])}`)
    .join('&');
  return `https://restapi.amap.com${path}?${qs}`;
}

export function useWeather() {
  const state = reactive<WeatherState>({
    loading: true,
    error: false,
    city: '',
    temp: '',
    phrase: '',
    code: 'sunny',
  });
  const { initGetDictList } = useDict();

  function readCachedCreds(): AmapWebCreds | null {
    try {
      const cached = localStorage.getItem(AMAP_WEB_CRED_CACHE);
      if (cached) return JSON.parse(cached) as AmapWebCreds;
    } catch {
      /* ignore */
    }
    return null;
  }

  async function getCreds(): Promise<AmapWebCreds | null> {
    // 有缓存直接用；同时后台异步刷新字典，不阻塞首屏
    const cached = readCachedCreds();
    if (cached?.key) {
      refreshCredsInBackground();
      return cached;
    }
    // 无缓存：同步拉字典，带 3s 超时防止 Redis 挂起拖垮首屏
    try {
      const dictList = (await withTimeout(
        initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_WEB_SERVICES_API_KEY),
        DICT_FETCH_TIMEOUT_MS,
      )) as any[];
      const key = dictList.find((i: any) => i.key === 'key')?.name ?? '';
      const signKey = dictList.find((i: any) => i.key === 'signKey')?.name ?? '';
      if (key) {
        const creds = { key, signKey };
        localStorage.setItem(AMAP_WEB_CRED_CACHE, JSON.stringify(creds));
        return creds;
      }
      console.warn(
        '[Weather] 租户未配置高德 Web Services API Key，天气 chip 不显示。字典：TENANT_AMAP__AAPPLICATION_WEB_SERVICES_API_KEY',
      );
    } catch (e) {
      console.warn('[Weather] 字典接口获取高德 key 失败（Redis/网络异常），天气 chip 不显示', e);
    }
    return null;
  }

  async function refreshCredsInBackground() {
    try {
      const dictList = (await withTimeout(
        initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_WEB_SERVICES_API_KEY),
        DICT_FETCH_TIMEOUT_MS,
      )) as any[];
      const key = dictList.find((i: any) => i.key === 'key')?.name ?? '';
      const signKey = dictList.find((i: any) => i.key === 'signKey')?.name ?? '';
      if (key) localStorage.setItem(AMAP_WEB_CRED_CACHE, JSON.stringify({ key, signKey }));
    } catch {
      /* ignore - 后台刷新失败不影响当前会话 */
    }
  }

  async function resolveIpFromLoginLog(): Promise<string> {
    try {
      const res: any = await queryLoginLogPage({ model: {}, current: 1, size: 1 });
      const list: any[] = res?.records || [];
      return list[0]?.requestIp || '';
    } catch {
      return '';
    }
  }

  function safeStr(v: unknown): string {
    // 兼容 null / undefined / 数组 / 对象等非字符串，统一返回 trim 后的 string；
    // 高德定位失败时 adcode/city 字段会返回 []，String([]) === ''，正好得到空串。
    if (v == null) return '';
    if (Array.isArray(v)) return v.length ? String(v[0]).trim() : '';
    return String(v).trim();
  }

  /**
   * 是否私网 / 回环 / 本地 IP。这些 IP 高德 IP 定位接口必然返回空 adcode，
   * 提前过滤掉避免一次明知会失败的网络请求。
   */
  function isPrivateOrLocalIp(ip: string): boolean {
    if (!ip) return false;
    if (ip === '127.0.0.1' || ip === '0:0:0:0:0:0:0:1' || ip === '::1') return true;
    if (/^10\./.test(ip)) return true;
    if (/^192\.168\./.test(ip)) return true;
    if (/^172\.(1[6-9]|2\d|3[0-1])\./.test(ip)) return true;
    return false;
  }

  // 高德 IP 定位：ip 为空时用调用方 IP；返回 adcode + city。
  // 私网 IP 或定位失败属于预期内（开发环境 / 内网部署），静默返回空触发上层兜底，不打 warning。
  async function lookupCity(
    creds: AmapWebCreds,
    ip?: string,
  ): Promise<{ adcode: string; city: string }> {
    if (ip && isPrivateOrLocalIp(ip)) {
      return { adcode: '', city: '' };
    }
    try {
      const params: Record<string, string> = {};
      if (ip) params.ip = ip;
      const resp = await fetch(buildUrl('/v3/ip', params, creds));
      const data = await resp.json().catch(() => null);
      if (data?.status === '1') {
        const adcode = safeStr(data.adcode);
        const city = safeStr(data.city) || safeStr(data.province);
        // adcode 为空表示高德识别为私网/无法定位（正常情况），上层会重试或走默认城市，不需告警
        if (adcode) return { adcode, city };
      } else {
        console.warn('[Weather] 高德 IP 定位 status 非成功', data?.info || data);
      }
    } catch (e) {
      console.warn('[Weather] 高德 IP 定位请求异常', e);
    }
    return { adcode: '', city: '' };
  }

  // 高德实时天气：city 可以是 adcode 也可以是中文名。任何关键字段缺失都返回 null
  async function fetchLive(
    creds: AmapWebCreds,
    adcode: string,
  ): Promise<{ temperature: string; weather: string } | null> {
    try {
      const resp = await fetch(
        buildUrl('/v3/weather/weatherInfo', { city: adcode, extensions: 'base' }, creds),
      );
      const data = await resp.json().catch(() => null);
      if (data?.status !== '1') {
        console.warn('[Weather] 高德天气 status 非成功', data?.info || data);
        return null;
      }
      const lives = Array.isArray(data?.lives) ? data.lives : [];
      if (!lives.length) {
        console.warn('[Weather] 高德天气 lives 为空', data);
        return null;
      }
      const live = lives[0] || {};
      const temperature = safeStr(live.temperature);
      const weather = safeStr(live.weather);
      if (!temperature || !weather) {
        console.warn('[Weather] 高德天气返回字段不完整（temperature/weather 为空）', live);
        return null;
      }
      return { temperature, weather };
    } catch (e) {
      console.warn('[Weather] 高德天气请求异常', e);
    }
    return null;
  }

  async function load() {
    state.loading = true;
    state.error = false;
    try {
      const creds = await getCreds();
      if (!creds?.key) {
        state.error = true;
        return;
      }

      const ip = await resolveIpFromLoginLog();
      let located = await lookupCity(creds, ip || undefined);
      if (!located.adcode) {
        // 再试一次不带 ip（用调用方 IP）
        located = await lookupCity(creds);
      }
      if (!located.adcode) {
        located = DEFAULT_FALLBACK;
      }

      const live = await fetchLive(creds, located.adcode);
      if (!live) {
        state.error = true;
        state.city = (located.city || '').replace(/市$/, '');
        return;
      }

      state.city = (located.city || '').replace(/市$/, '');
      state.temp = live.temperature;
      state.phrase = live.weather;
      state.code = mapWeather(live.weather);
    } catch {
      state.error = true;
    } finally {
      state.loading = false;
    }
  }

  onMounted(load);

  return { ...toRefs(state), refresh: load };
}
