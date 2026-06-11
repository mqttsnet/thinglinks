/**
 * 产品版本序号格式化工具。
 *
 * <p>系统所有"版本"字段(product.activeVersionNo / product.previousFullVersionNo /
 * product_version.version 等)在 DB 层用 16 位短雪花字符串作为不可变快照标识,
 * 前端展示时统一通过本工具兜底空值并保持完整长度展示(用户视角"版本序号" =
 * 系统按时间有序的全局序号,语义自洽,不再期待 1.0.0 这种语义化版本)。</p>
 */

/**
 * 格式化版本序号,空值返回 fallback。
 *
 * @param v        原始版本序号(雪花字符串)
 * @param fallback 空值占位,默认 '-'
 * @returns 原值或 fallback
 */
export function formatSnapshotId(v?: string | null, fallback = '-'): string {
  return v && String(v).trim() ? String(v) : fallback;
}

/**
 * 判断版本序号是否为有效值(用于决定是否渲染复制按钮 / Tag)。
 */
export function hasSnapshotId(v?: string | null): boolean {
  return !!(v && String(v).trim());
}
