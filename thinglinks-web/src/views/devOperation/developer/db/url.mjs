/** 从 Vite 代理配置解析 API 服务地址；缺失或无效配置时返回当前站点。 */
export function resolveApiOrigin(proxyValue, currentOrigin) {
  let proxyConfig = proxyValue;
  if (typeof proxyConfig === 'string') {
    if (!proxyConfig.trim()) return trimTrailingSlash(currentOrigin);
    try {
      proxyConfig = JSON.parse(proxyConfig.replaceAll("'", '"'));
    } catch {
      return trimTrailingSlash(currentOrigin);
    }
  }
  if (!Array.isArray(proxyConfig)) return trimTrailingSlash(currentOrigin);

  const apiProxy = proxyConfig.find(
    (entry) => Array.isArray(entry) && entry[0] === '/api' && typeof entry[1] === 'string',
  );
  return trimTrailingSlash(apiProxy?.[1] || currentOrigin);
}

export function buildDruidUrl(servicePath, proxyValue, currentOrigin) {
  const apiOrigin = resolveApiOrigin(proxyValue, currentOrigin);
  const normalizedService = String(servicePath || '').replace(/^\/+|\/+$/g, '');
  return `${apiOrigin}/${normalizedService}/druid/index.html`;
}

function trimTrailingSlash(value) {
  return String(value || '').replace(/\/+$/, '');
}
