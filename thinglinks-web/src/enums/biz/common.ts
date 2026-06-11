/**
 * 通用业务常量(对齐后端 com.mqttsnet.thinglinks.common.constant.BizConstant)。
 *
 * <p>原则:**前后端共用的字面值必须用常量管理**,避免散落硬编码。
 * 当后端 BizConstant 改了字面值,前端这里同步改一处即可。
 *
 * <p>使用规约:
 * <ul>
 *   <li>{@code BizConstant.ALL} 表示"全部"通配 ── Video / Bridge / 场景联动 / 设备命令 等共用</li>
 *   <li>{@code BizConstant.STAR} 是 MQTT 行业惯例的兼容写法,后端可同时识别</li>
 * </ul>
 *
 * @author mqttsnet
 */
export const BizConstant = {
  /** 通配"全部",对齐后端 BizConstant.ALL = "all" */
  ALL: 'all',
  /** MQTT 行业惯例通配符,后端 isWildcard() 同时识别 ALL 和 STAR */
  STAR: '*',
} as const;

/**
 * 类型守卫:判断是否为通配字面量。
 * 与后端 BridgeMatchConfig.isWildcard 行为对齐。
 */
export function isWildcard(v: string | null | undefined): boolean {
  return v === BizConstant.ALL || v === BizConstant.STAR;
}
