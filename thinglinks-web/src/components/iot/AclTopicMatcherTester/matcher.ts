/**
 * ACL 主题匹配引擎(纯前端,与后端 AclMatcherUtil + AclTopicPatternPlaceholderReplacer 1:1 对齐)。
 *
 * <p>支持:
 * <ul>
 *   <li>5 个占位符替换(${app_id} / ${user_name} / ${device_identification} /
 *       ${product_identification} / ${device_sdk_version})</li>
 *   <li>MQTT 通配符:`+`(单层非空) / `#`(必须末尾,多层贪婪) / `$xxx`(系统主题精确 + 子层级)</li>
 *   <li>多规则按 priority 排序找最高优先级命中</li>
 * </ul>
 *
 * @author mqttsnet
 */
import type {
  AclLevelDiff,
  AclPlaceholderKey,
  AclPlaceholderValues,
  AclTestResult,
  TestableAclRule,
} from './types';

const PH_KEYS: AclPlaceholderKey[] = [
  'app_id',
  'user_name',
  'device_identification',
  'product_identification',
  'device_sdk_version',
];

/** 取出 pattern 中实际使用的占位符 */
export function detectUsedPlaceholders(pattern: string): AclPlaceholderKey[] {
  if (!pattern) return [];
  return PH_KEYS.filter((k) => pattern.includes('${' + k + '}'));
}

/** 占位符替换(对齐 AclTopicPatternPlaceholderReplacer:null/undefined 视为空字符串) */
export function replacePlaceholders(pattern: string, values: AclPlaceholderValues): string {
  if (!pattern) return '';
  let p = pattern;
  for (const k of PH_KEYS) {
    const v = values[k] ?? '';
    p = p.split('${' + k + '}').join(v);
  }
  return p;
}

/** Pattern 合法性校验(对齐 AclMatcherUtil.compilePattern 抛错规则) */
export function validatePattern(pattern: string): { valid: boolean; error?: string } {
  if (!pattern || pattern === '#') return { valid: true };
  const levels = pattern.split('/');
  for (let i = 0; i < levels.length; i++) {
    if (levels[i] === '#' && i !== levels.length - 1) {
      return { valid: false, error: 'hashNotAtEnd' };
    }
  }
  return { valid: true };
}

/** Topic 合法性校验(测试用 topic 必须是具体值,不能含通配符) */
export function validateTopic(topic: string): { valid: boolean; error?: string } {
  if (!topic) return { valid: false };
  const levels = topic.split('/');
  for (let i = 0; i < levels.length; i++) {
    if (levels[i] === '+' || levels[i] === '#') {
      return { valid: false, error: 'topicHasWildcard' };
    }
  }
  return { valid: true };
}

/** MQTT 主题匹配(对齐 AclMatcherUtil.safeIsTopicMatch + compilePattern) */
export function isTopicMatch(pattern: string, topic: string): boolean {
  if (!pattern || !topic) return false;
  if (pattern === '#') return true;
  if (pattern.startsWith('$')) {
    const re = new RegExp('^' + escapeRegex(pattern) + '(?:/[^/]+)*$');
    return re.test(topic);
  }
  const patternLevels = pattern.split('/');
  let regex = '^';
  for (let i = 0; i < patternLevels.length; i++) {
    const lv = patternLevels[i];
    if (lv === '#') {
      if (i !== patternLevels.length - 1) return false;
      regex += '.*';
    } else if (lv === '+') {
      regex += '[^/]+';
    } else {
      regex += escapeRegex(lv);
    }
    if (i < patternLevels.length - 1) regex += '/';
  }
  regex += '$';
  try {
    return new RegExp(regex).test(topic);
  } catch {
    return false;
  }
}

/** 层级 diff(用于 UI 可视化) */
export function diffLevels(patternLevels: string[], topicLevels: string[]): AclLevelDiff[] {
  const out: AclLevelDiff[] = [];
  const maxLen = Math.max(patternLevels.length, topicLevels.length);
  for (let i = 0; i < maxLen; i++) {
    const p = patternLevels[i];
    const tk = topicLevels[i];
    if (p === '#') {
      const rest = topicLevels.slice(i).join('/');
      out.push({
        pattern: '#',
        topic: rest || '',
        matched: i === patternLevels.length - 1,
        note: 'multi-wildcard',
      });
      break;
    } else if (p === '+') {
      out.push({
        pattern: p,
        topic: tk ?? '',
        matched: tk != null && tk.length > 0,
        note: 'single-wildcard',
      });
    } else if (p === undefined) {
      out.push({ pattern: '', topic: tk ?? '', matched: false, note: 'topic-extra' });
    } else if (tk === undefined) {
      out.push({ pattern: p, topic: '', matched: false, note: 'pattern-extra' });
    } else {
      out.push({
        pattern: p,
        topic: tk,
        matched: p === tk,
        note: p === tk ? 'exact' : 'mismatch',
      });
    }
  }
  return out;
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * 完整测试入口 ── 对齐后端 AclMatcherUtil.findMatchingRule 流程:
 * <ol>
 *   <li>过滤启用规则 + 按 priority 排序</li>
 *   <li>逐条:占位符替换 → pattern 校验 → topic 匹配</li>
 *   <li>第一个 topic 命中的规则即胜出,返回其 decision</li>
 * </ol>
 *
 * <p>注:规则的 actionType 字段不参与匹配引擎,因为前端测试器无法可靠模拟设备真实动作语义,
 * 引入 actionType 校验反而让用户困惑。actionType 在 UI 上仅作"规则配置概览"展示。
 */
export function testAclMatch(args: {
  rules: TestableAclRule[];
  testTopic: string;
  placeholderValues: AclPlaceholderValues;
}): AclTestResult {
  const { rules, testTopic, placeholderValues } = args;

  const topicValidation = validateTopic(testTopic);
  if (!topicValidation.valid) {
    return {
      matchedRule: null,
      decision: null,
      resolvedPattern: '',
      levelDiff: [],
      topicError: topicValidation.error,
    };
  }

  const sorted = (rules || [])
    .filter((r) => r && r.enabled !== false)
    .sort((a, b) => (a.priority ?? Number.MAX_SAFE_INTEGER) - (b.priority ?? Number.MAX_SAFE_INTEGER));

  let firstResolved = '';
  let firstDiff: AclLevelDiff[] = [];

  for (const rule of sorted) {
    const resolved = replacePlaceholders(rule.topicPattern || '', placeholderValues);
    const validation = validatePattern(resolved);
    const patternLevels = resolved.split('/');
    const topicLevels = testTopic.split('/');
    const diff = diffLevels(patternLevels, topicLevels);

    if (!firstResolved) {
      firstResolved = resolved;
      firstDiff = diff;
    }

    if (!validation.valid) {
      // pattern 非法 ── 单规则模式直接返回错误;多规则模式跳过看下一条
      if (sorted.length === 1) {
        return {
          matchedRule: null,
          decision: null,
          resolvedPattern: resolved,
          levelDiff: diff,
          patternError: validation.error,
        };
      }
      continue;
    }

    if (!isTopicMatch(resolved, testTopic)) continue;

    return {
      matchedRule: rule,
      decision: rule.decision ?? null,
      resolvedPattern: resolved,
      levelDiff: diff,
    };
  }

  return {
    matchedRule: null,
    decision: null,
    resolvedPattern: firstResolved,
    levelDiff: firstDiff,
  };
}
