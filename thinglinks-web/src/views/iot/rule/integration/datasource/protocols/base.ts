/**
 * 协议模块共用工具：JSON 反序列化 + 提交前组装。
 *
 * <p>这些函数是 {@link ProtocolModule.parseConnection / parseCredential} 的默认实现，
 * 协议模块可直接用，也可在 .ts 文件里重写满足特殊需要。
 */

/**
 * 安全解析 JSON 字符串。空 / 非法 / null 都返回 null（不抛异常）。
 */
export function safeJsonParse<T = unknown>(json?: string | null): T | null {
  if (!json || !json.trim()) return null;
  try {
    return JSON.parse(json) as T;
  } catch {
    return null;
  }
}

/**
 * 默认 connection_json 反序列化：空 / 非法返回 {}。
 */
export function defaultParseConnection(json?: string | null): Record<string, any> {
  return safeJsonParse<Record<string, any>>(json) ?? {};
}

/**
 * 默认 credential_json 反序列化：空 / 非法返回 {}。
 */
export function defaultParseCredential(json?: string | null): Record<string, any> {
  return safeJsonParse<Record<string, any>>(json) ?? {};
}

/**
 * 表单 flat values → JSON 字符串（提交时用）。
 * <p>过滤 null / undefined / 空字符串，避免污染 DB。
 */
export function assembleJson(values: Record<string, unknown>): string {
  const cleaned: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(values)) {
    if (v === null || v === undefined) continue;
    if (typeof v === 'string' && v.trim() === '') continue;
    cleaned[k] = v;
  }
  return JSON.stringify(cleaned);
}
