/**
 * 后端 @Echo 回填值访问 helper。
 *
 * <p>项目里所有 ResultVO 走 lamp echoService 回填中文展示值到 {@code echoMap},
 * key = 字段名,value = 字典 label / 用户昵称 / 组织名等字符串。
 * 模板里直接 {@code record.echoMap?.createdBy ?? record.createdBy ?? '-'}
 * 链条繁琐且重复,本 helper 统一三段兜底逻辑。</p>
 *
 * <p>覆盖场景:</p>
 * <ul>
 *   <li>用户 ID 字段 ── createdBy / updatedBy 等 ({@code @Echo(DEF_USER_ID_CLASS)})</li>
 *   <li>组织 ID 字段 ── createdOrgId 等 ({@code @Echo(ORG_ID_CLASS)})</li>
 *   <li>字典字段 ── status / type 等 ({@code @Echo(DICTIONARY_ITEM_FEIGN_CLASS)})</li>
 * </ul>
 */

/**
 * 取后端 @Echo 回填的中文展示值。
 *
 * <p>优先级:{@code echoMap[field]} → {@code record[field]} → {@code fallback}。
 * 后端 echoService 不会处理 null 值字段(此时 echoMap 里没有对应 key),
 * 兜底走原值显示(裸 ID 或 null),用户至少能看到一个值而不是空白。</p>
 *
 * @param record   返回 VO,需含 echoMap 字段(实现 EchoVO 的后端自动回填)
 * @param field    字段名,与后端 VO 字段名一致(如 'createdBy' / 'createdOrgId' / 'status')
 * @param fallback 都没有时的占位,默认 '-'
 * @returns        中文展示文本
 */
export function echoMapText(
  record: Record<string, any> | null | undefined,
  field: string,
  fallback: string = '-',
): string {
  if (!record) return fallback;
  const echoed = record.echoMap?.[field];
  if (echoed != null && echoed !== '') return String(echoed);
  const raw = record[field];
  if (raw != null && raw !== '') return String(raw);
  return fallback;
}
