/**
 * AclTopicMatcherTester ── ACL 规则匹配测试器(IoT 通用)
 *
 * <p>作用:基于已填写好的规则数据(单条或多条),通过选择真实产品 / 设备替换占位符 +
 * 输入测试 topic + 选 actionType,实时模拟后端 ACL 命中过程,输出"允许 / 拒绝 / 未命中"决策。
 *
 * <p>对齐后端能力:
 * <ul>
 *   <li>占位符替换(5 个):{@code AclTopicPatternPlaceholderReplacer}</li>
 *   <li>MQTT 通配符匹配(`+` 单层 / `#` 末尾多层 / `$xxx` 系统主题 / 其它精确):{@code AclMatcherUtil}</li>
 * </ul>
 *
 * @author mqttsnet
 */

/** 占位符 key(对齐后端 5 个占位符) */
export type AclPlaceholderKey =
  | 'app_id'
  | 'user_name'
  | 'device_identification'
  | 'product_identification'
  | 'device_sdk_version';

/** 占位符值集合(均为 string,空字符串表示未填) */
export type AclPlaceholderValues = Record<AclPlaceholderKey, string>;

/** 可测试的规则结构 ── 包含决策、模式、动作类型、优先级等 */
export interface TestableAclRule {
  /** 规则名(用于显示;无则用 ruleId / topicPattern 兜底) */
  ruleName?: string;
  /** 主题模式(可含占位符 + MQTT 通配符) */
  topicPattern: string;
  /** 决策:true=允许 / false=拒绝;未传则视为"仅测试是否命中,不输出决策" */
  decision?: boolean;
  /** 限定动作类型(PUBLISH / SUBSCRIBE / UNSUBSCRIBE);空表示对所有动作生效 */
  actionType?: string;
  /** 优先级(数字越小越优先);多条规则时按此排序选最高 */
  priority?: number;
  /** 是否启用 */
  enabled?: boolean;
}

/** 单层匹配过程(用于 UI 高亮) */
export interface AclLevelDiff {
  pattern: string;
  topic: string;
  matched: boolean;
  note: 'exact' | 'single-wildcard' | 'multi-wildcard' | 'mismatch' | 'topic-extra' | 'pattern-extra';
}

/** 完整测试结果 */
export interface AclTestResult {
  /** 命中的最高优先级规则(无则 null) */
  matchedRule: TestableAclRule | null;
  /** 整体决策:true=允许 / false=拒绝 / null=未命中(默认 deny 由业务侧解释) */
  decision: boolean | null;
  /** 替换占位符后的最终 pattern(命中规则的;若都未命中则取 rules[0] 的 resolvedPattern 用于诊断) */
  resolvedPattern: string;
  /** 层级 diff(用于 UI 可视化命中规则;未命中时取 rules[0] 的 diff) */
  levelDiff: AclLevelDiff[];
  /** Pattern 校验错误(命中规则的 pattern 不合法时) */
  patternError?: string;
  /** Topic 校验错误(用户输入的 topic 含通配符等) */
  topicError?: string;
}
