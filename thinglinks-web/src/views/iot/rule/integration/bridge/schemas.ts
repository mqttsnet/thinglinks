/**
 * 桥接规则 match_config / action_config 的动态 FormSchema 工厂。
 *
 * <p>{@code Edit.vue} 监听 direction、所选 DataSource 的 sourceType、targetHandler 等变化，
 * 调对应的 schema 工厂重组表单字段。
 *
 * <p>JSON 序列化 / 反序列化使用 Optional 风格容错（参考 datasource/schemas.ts）。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */

import type { FormSchema } from '/@/components/Table';
import { DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { useI18n } from '/@/hooks/web/useI18n';
import { page as pageSubscriptionSource } from '/@/api/iot/rule/integration/subscriptionSource';
import type {
  OutboundMatchConfigDto,
  InboundMatchConfigDto,
  OutboundActionConfigDto,
  InboundActionConfigDto,
  PayloadFilterDto,
} from './dto';

// ============================== i18n helpers ==============================

const { t } = useI18n();
/** match outbound 字段 i18n */
const tmo = (key: string) => t(`iot.rule.integration.bridge.matchConfig.outbound.${key}`);
/** match inbound 字段 i18n */
const tmi = (key: string) => t(`iot.rule.integration.bridge.matchConfig.inbound.${key}`);
/** match payload filter 选项 */
const tfilter = (key: string) =>
  t(`iot.rule.integration.bridge.matchConfig.payloadFilterOpts.${key}`);
/** action 通用 head */
const tac = (key: string) => t(`iot.rule.integration.bridge.actionForm.common.${key}`);
/** action inbound 段 */
const taib = (key: string) => t(`iot.rule.integration.bridge.actionForm.inbound.${key}`);

// ============================== Optional 风格 helper ==============================

/**
 * 容错的 JSON 反序列化（参考 datasource/schemas.ts），返回 null 表示空 / 解析失败。
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
 * 解析 match_config_json（按 direction 决定具体类型）。
 * 失败 / 空时返回 {} 兜底，下游字段走 default。
 */
export function parseMatchConfigJson<T extends OutboundMatchConfigDto | InboundMatchConfigDto>(
  json?: string,
): T {
  return safeJsonParse<T>(json) ?? ({} as T);
}

/**
 * 解析 action_config_json。
 */
export function parseActionConfigJson<T extends OutboundActionConfigDto | InboundActionConfigDto>(
  json?: string,
): T {
  return safeJsonParse<T>(json) ?? ({} as T);
}

/**
 * 把对象组装成 JSON 字符串（过滤 null / undefined / 空字符串 / 空数组 / 空对象）。
 */
export function assembleJson(values: object): string {
  return JSON.stringify(prune(values));
}

/**
 * 递归清除空值。
 */
function prune(obj: any): any {
  if (Array.isArray(obj)) {
    const arr = obj.map(prune).filter((v) => v !== undefined);
    return arr;
  }
  if (obj && typeof obj === 'object') {
    const out: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(obj)) {
      const pv = prune(v);
      if (pv === undefined || pv === null) continue;
      if (typeof pv === 'string' && pv.trim() === '') continue;
      if (Array.isArray(pv) && pv.length === 0) continue;
      if (typeof pv === 'object' && !Array.isArray(pv) && Object.keys(pv).length === 0) continue;
      out[k] = pv;
    }
    return out;
  }
  return obj;
}

// ============================== 出站 match config schema ==============================

/**
 * 出站规则的 match config 字段（产品 / 动作 / topic 过滤 / 设备过滤 / 内容过滤 / 时间窗口）。
 *
 * <p>本期实现核心 3 项（产品 / 动作 / topicPatterns）；高级过滤项暂以 JSON 文本形式占位，
 * 后续 Phase 5 可拆为可视化子表单。
 */
export const outboundMatchSchema = (): FormSchema[] => [
  {
    label: tmo('productIdentifications.label'),
    field: 'mc_productIdentifications',
    component: 'Input',
    slot: 'mc_productIdentifications',
    // v-model 双态:'all' 字符串(全部模式) 或 (string|number)[] 数组(自定义);
    // Form 默认 required 只识别字符串非空,无法判定数组 → 自定义 validator 覆盖两态.
    rules: [
      {
        required: true,
        validator: (_rule: any, value: any) => {
          if (value === 'all') return Promise.resolve();
          if (Array.isArray(value) && value.length > 0) return Promise.resolve();
          return Promise.reject(new Error(t('common.chooseText')));
        },
        trigger: 'change',
      },
    ],
    helpMessage: tmo('productIdentifications.help'),
    colProps: { span: 24 },
  },
  {
    label: tmo('actionTypes.label'),
    field: 'mc_actionTypes',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      mode: 'multiple',
      placeholder: tmo('actionTypes.placeholder'),
      // 字典 LINK_DEVICE_ACTION_TYPE 已与 DeviceActionTypeEnum 17 个枚举严格对齐,
      // 前端不做白名单过滤,完整展示所有事件类型 ── 单一来源(SSOT)、数据层透明
      ...dictComponentProps(DictEnum.LINK_DEVICE_ACTION_TYPE),
    },
    helpMessage: tmo('actionTypes.help'),
  },
  {
    // 单一字段 ── 多选 MQTT 主题模式(对齐 ACL,直接存 topic 模板字符串)
    label: tmo('topicPatterns.label'),
    field: 'mc_topicPatterns',
    component: 'Input',
    slot: 'mc_topicPatterns',
    helpMessage: tmo('topicPatterns.help'),
    colProps: { span: 24 },
  },
  {
    // 数据形态过滤 ── 多选字典 BRIDGE_PAYLOAD_KIND(RAW / THING_MODEL);
    // 不选 / 空 = 后端默认只消费 RAW;勾选「物模型数据」才桥接 THING_MODEL.
    label: tmo('payloadKinds.label'),
    field: 'mc_payloadKinds',
    component: 'ApiSelect',
    componentProps: {
      mode: 'multiple',
      placeholder: tmo('payloadKinds.placeholder'),
      ...dictComponentProps(DictEnum.BRIDGE_PAYLOAD_KIND),
    },
    helpMessage: tmo('payloadKinds.help'),
  },
  // 高级（折叠展示给用户视觉）
  {
    label: tmo('deviceIdentifications.label'),
    field: 'mc_deviceIdentifications',
    component: 'Input',
    slot: 'mc_deviceIdentifications',
    helpMessage: tmo('deviceIdentifications.help'),
    colProps: { span: 24 },
  },
  {
    label: tmo('deviceTagsAny.label'),
    field: 'mc_deviceTagsAny',
    component: 'Select',
    componentProps: {
      mode: 'tags',
      placeholder: tmo('deviceTagsAny.placeholder'),
      tokenSeparators: [',', ' '],
    },
    helpMessage: tmo('deviceTagsAny.help'),
  },
  {
    label: tmo('payloadFilterType.label'),
    field: 'mc_payloadFilterType',
    component: 'Select',
    defaultValue: 'NONE',
    componentProps: {
      options: [
        { label: tfilter('NONE'), value: 'NONE' },
        { label: tfilter('JSON_PATH'), value: 'JSON_PATH' },
        { label: tfilter('SPEL'), value: 'SPEL' },
        { label: tfilter('GROOVY'), value: 'GROOVY' },
      ],
    },
    helpMessage: tmo('payloadFilterType.help'),
  },
  {
    label: tmo('payloadFilterExpression.label'),
    field: 'mc_payloadFilterExpression',
    component: 'InputTextArea',
    componentProps: { rows: 2, placeholder: tmo('payloadFilterExpression.placeholder') },
    helpMessage: tmo('payloadFilterExpression.help'),
    ifShow: ({ values }) => values.mc_payloadFilterType && values.mc_payloadFilterType !== 'NONE',
  },
  {
    label: tmo('timeWindowCronExpr.label'),
    field: 'mc_timeWindowCronExpr',
    component: 'Input',
    componentProps: { placeholder: tmo('timeWindowCronExpr.placeholder') },
    helpMessage: tmo('timeWindowCronExpr.help'),
  },
];

// ============================== 入站 match config schema ==============================

export const inboundMatchSchema = (): FormSchema[] => [
  {
    label: tmi('subscriptionSourceIds.label'),
    field: 'mc_subscriptionSourceIds',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      mode: 'multiple',
      placeholder: tmi('subscriptionSourceIds.placeholder'),
      api: async (params: any) => {
        const res = await pageSubscriptionSource({
          size: 100,
          current: 1,
          model: { enable: true },
          ...params,
        } as any);
        return (res?.records ?? []).map((r: any) => ({
          label: `${r.sourceName} (${r.sourceCode})`,
          value: Number(r.id),
        }));
      },
      immediate: true,
    },
    helpMessage: tmi('subscriptionSourceIds.help'),
  },
  {
    label: tmi('messageFilterType.label'),
    field: 'mc_messageFilterType',
    component: 'Select',
    defaultValue: 'NONE',
    componentProps: {
      options: [
        { label: tfilter('NONE'), value: 'NONE' },
        { label: tfilter('JSON_PATH'), value: 'JSON_PATH' },
        { label: tfilter('SPEL'), value: 'SPEL' },
        { label: tfilter('GROOVY'), value: 'GROOVY' },
      ],
    },
  },
  {
    label: tmi('messageFilterExpression.label'),
    field: 'mc_messageFilterExpression',
    component: 'InputTextArea',
    componentProps: { rows: 2, placeholder: tmi('messageFilterExpression.placeholder') },
    ifShow: ({ values }) => values.mc_messageFilterType && values.mc_messageFilterType !== 'NONE',
  },
];

// ============================== 出站 action config schema (按 sourceType 异构) ==============================

const outboundActionCommonHead = (): FormSchema[] => [
  {
    label: tac('payloadFormat.label'),
    field: 'ac_payloadFormat',
    component: 'Select',
    defaultValue: 'JSON',
    componentProps: {
      options: [
        { label: tac('payloadFormat.opt_JSON'), value: 'JSON' },
        { label: tac('payloadFormat.opt_STRING'), value: 'STRING' },
        { label: tac('payloadFormat.opt_HEX'), value: 'HEX' },
        { label: tac('payloadFormat.opt_RAW'), value: 'RAW' },
      ],
    },
  },
  {
    label: tac('payloadTemplate.label'),
    field: 'ac_payloadTemplate',
    component: 'InputTextArea',
    componentProps: { rows: 3, placeholder: tac('payloadTemplate.placeholder') },
    helpMessage: tac('payloadTemplate.help'),
  },
];

// ============================== 出站 action 全协议委托 protocols/ registry ==============================
// 15 个 sourceType (Kafka/Redis/RocketMQ/RabbitMQ/MySQL/HTTP/WebHook/MQTT/TDengine/
// ClickHouse/InfluxDB/IoTDB/PostgreSQL/MongoDB/Pulsar) 各自的 actionFields / assembleAction /
// flattenAction / presetToFlat 全部独立到 protocols/XxxBridgeAction.ts，本文件不再 switch。
//
// 修改一个协议字段 → 改一个 protocols/XxxBridgeAction.ts 文件，0 改其它协议。
// 新增协议 → 加 1 个 protocols/XxxBridgeAction.ts + protocols/index.ts 加 1 行 import。
import {
  getActionFields,
  assembleActionByType,
  flattenActionByType,
  allActionFieldNamesAcrossProtocols,
} from './protocols';

/** 按 sourceType 取出站 action schema 子段（不含 transform 共用段）。 */
function outboundActionForSourceType(sourceType?: string): FormSchema[] {
  return getActionFields(sourceType);
}

// ============================== 入站 action config schema (按 targetHandler 异构) ==============================

const inboundActionTargetHandlerSchema = (): FormSchema[] => [
  {
    label: taib('targetHandler.label'),
    field: 'ac_targetHandler',
    component: 'Select',
    required: true,
    defaultValue: 'MQTT_FORWARD',
    componentProps: {
      options: [
        { label: taib('targetHandler.opt_MQTT_FORWARD'), value: 'MQTT_FORWARD' },
        { label: taib('targetHandler.opt_RAW_INSERT'), value: 'RAW_INSERT' },
        { label: taib('targetHandler.opt_RULE_TRIGGER'), value: 'RULE_TRIGGER' },
      ],
      // 选完触发 resetSchema 重组入站 action 段表单 → 固定 popup 到 modal body
      getPopupContainer: (trigger: HTMLElement) =>
        (trigger?.closest?.('.ant-modal-body') as HTMLElement) ??
        (trigger?.parentNode as HTMLElement) ??
        document.body,
    },
    helpMessage: taib('targetHandler.help'),
  },
];

const inboundActionMqttForward = (): FormSchema[] => [
  {
    label: taib('mqttForward.targetProductIdentification.label'),
    field: 'ac_targetProductIdentification',
    component: 'Input',
    required: true,
    componentProps: { placeholder: taib('mqttForward.targetProductIdentification.placeholder') },
    helpMessage: taib('mqttForward.targetProductIdentification.help'),
  },
  {
    label: taib('mqttForward.targetTopicTemplate.label'),
    field: 'ac_targetTopicTemplate',
    component: 'Input',
    required: true,
    componentProps: { placeholder: taib('mqttForward.targetTopicTemplate.placeholder') },
    helpMessage: taib('mqttForward.targetTopicTemplate.help'),
  },
  {
    label: taib('mqttForward.fieldMapping.label'),
    field: 'ac_fieldMapping',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: taib('mqttForward.fieldMapping.placeholder'),
    },
    helpMessage: taib('mqttForward.fieldMapping.help'),
  },
];

const inboundActionRawInsert = (): FormSchema[] => [
  {
    label: taib('rawInsert.fieldMapping.label'),
    field: 'ac_fieldMapping',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 4,
      placeholder: taib('rawInsert.fieldMapping.placeholder'),
    },
    helpMessage: taib('rawInsert.fieldMapping.help'),
  },
];

const inboundActionRuleTrigger = (): FormSchema[] => [
  {
    label: taib('ruleTrigger.triggerRuleId.label'),
    field: 'ac_triggerRuleId',
    component: 'InputNumber',
    required: true,
    componentProps: {
      style: 'width:100%',
      min: 1,
      placeholder: taib('ruleTrigger.triggerRuleId.placeholder'),
    },
    helpMessage: taib('ruleTrigger.triggerRuleId.help'),
  },
];

/**
 * 按 targetHandler 取入站 action schema 子段。
 */
function inboundActionForHandler(handler?: string): FormSchema[] {
  switch (handler) {
    case 'MQTT_FORWARD':
      return inboundActionMqttForward();
    case 'RAW_INSERT':
      return inboundActionRawInsert();
    case 'RULE_TRIGGER':
      return inboundActionRuleTrigger();
    default:
      return [];
  }
}

// ============================== 公开工厂 ==============================

/**
 * 取 match config schema（按 direction）。
 */
export function getMatchConfigSchema(direction?: string): FormSchema[] {
  if (direction === '20') return inboundMatchSchema();
  return outboundMatchSchema();
}

/**
 * 取 action config schema。
 *
 * @param direction 桥接方向（10/20）
 * @param sourceType 出站时所选数据源的 sourceType
 * @param targetHandler 入站时的处理方式
 */
export function getActionConfigSchema(
  direction?: string,
  sourceType?: string,
  targetHandler?: string,
): FormSchema[] {
  if (direction === '20') {
    return [...inboundActionTargetHandlerSchema(), ...inboundActionForHandler(targetHandler)];
  }
  // 出站
  return [...outboundActionCommonHead(), ...outboundActionForSourceType(sourceType)];
}

/**
 * 列举所有可能的 match / action 字段名（用于 Edit.vue 提交时切片）。
 */
export const allMatchFieldNames = (): Set<string> => {
  const names = new Set<string>();
  outboundMatchSchema().forEach((s) => names.add(s.field));
  inboundMatchSchema().forEach((s) => names.add(s.field));
  return names;
};

export const allActionFieldNames = (): Set<string> => {
  const names = new Set<string>();
  outboundActionCommonHead().forEach((s) => names.add(s.field));
  // 全协议出站字段名委托 protocols/ registry（新增协议在 registry 加 1 行即可，本处 0 改）
  allActionFieldNamesAcrossProtocols().forEach((f) => names.add(f));
  inboundActionTargetHandlerSchema().forEach((s) => names.add(s.field));
  ['MQTT_FORWARD', 'RAW_INSERT', 'RULE_TRIGGER'].forEach((h) => {
    inboundActionForHandler(h).forEach((s) => names.add(s.field));
  });
  return names;
};

// ============================== assemble / parse helpers (双向变换) ==============================

/**
 * 把表单字段值（mc_* 前缀）组装回 match_config_json。
 *
 * @param values 表单 getFieldsValue() 全量
 * @param direction 桥接方向（决定走 outbound 还是 inbound 结构）
 */
export function assembleMatchConfigJson(values: Record<string, any>, direction?: string): string {
  if (direction === '20') {
    const dto: InboundMatchConfigDto = {
      subscriptionSourceIds: parseNumberArray(values.mc_subscriptionSourceIds),
      messageFilter: composePayloadFilter(
        values.mc_messageFilterType,
        values.mc_messageFilterExpression,
      ),
    };
    return assembleJson(dto);
  }
  const dto: OutboundMatchConfigDto = {
    productIdentifications: parseStringArray(values.mc_productIdentifications),
    actionTypes: parseStringArray(values.mc_actionTypes),
    topicPatterns: parseStringArray(values.mc_topicPatterns),
    payloadKinds: parseStringArray(values.mc_payloadKinds),
    deviceFilter: composeDeviceFilter(values),
    payloadFilter: composePayloadFilter(
      values.mc_payloadFilterType,
      values.mc_payloadFilterExpression,
    ),
    timeWindow: values.mc_timeWindowCronExpr
      ? { cronExpr: values.mc_timeWindowCronExpr }
      : undefined,
  };
  return assembleJson(dto);
}

/**
 * 把表单字段值（ac_* 前缀）组装回 action_config_json。
 */
export function assembleActionConfigJson(
  values: Record<string, any>,
  direction?: string,
  sourceType?: string,
): string {
  if (direction === '20') {
    const dto: InboundActionConfigDto = {
      targetHandler: values.ac_targetHandler,
      targetProductIdentification: values.ac_targetProductIdentification,
      targetTopicTemplate: values.ac_targetTopicTemplate,
      fieldMapping: values.ac_fieldMapping,
      triggerRuleId: values.ac_triggerRuleId,
    };
    return assembleJson(dto);
  }
  // 出站：通用 head + 按 sourceType 委托 protocols/ registry（下面单 if 即可）
  const dto: OutboundActionConfigDto = {
    payloadFormat: values.ac_payloadFormat,
    payloadTemplate: values.ac_payloadTemplate,
  };
  if (sourceType) {
    const sub = assembleActionByType(sourceType, values);
    if (sub) {
      const key = sourceType.toLowerCase() as keyof OutboundActionConfigDto;
      (dto as any)[key] = sub;
    }
  }
  return assembleJson(dto);
}

/**
 * 把 match_config_json 反序列化为扁平表单字段值（mc_* 前缀）。
 */
export function flattenMatchConfig(
  json: string | undefined,
  direction?: string,
): Record<string, any> {
  const out: Record<string, any> = {};
  if (direction === '20') {
    const dto = parseMatchConfigJson<InboundMatchConfigDto>(json);
    out.mc_subscriptionSourceIds = dto.subscriptionSourceIds ?? [];
    out.mc_messageFilterType = dto.messageFilter?.type ?? 'NONE';
    out.mc_messageFilterExpression = dto.messageFilter?.expression;
    return out;
  }
  const dto = parseMatchConfigJson<OutboundMatchConfigDto>(json);
  out.mc_productIdentifications = dto.productIdentifications ?? [];
  out.mc_actionTypes = dto.actionTypes ?? [];
  out.mc_topicPatterns = dto.topicPatterns ?? [];
  out.mc_payloadKinds = dto.payloadKinds ?? [];
  out.mc_deviceIdentifications = dto.deviceFilter?.deviceIdentifications ?? [];
  out.mc_deviceTagsAny = dto.deviceFilter?.tagsAny ?? [];
  out.mc_payloadFilterType = dto.payloadFilter?.type ?? 'NONE';
  out.mc_payloadFilterExpression = dto.payloadFilter?.expression;
  out.mc_timeWindowCronExpr = dto.timeWindow?.cronExpr;
  return out;
}

/**
 * 把 action_config_json 反序列化为扁平表单字段值（ac_* 前缀）。
 */
export function flattenActionConfig(
  json: string | undefined,
  direction?: string,
  sourceType?: string,
): Record<string, any> {
  const out: Record<string, any> = {};
  if (direction === '20') {
    const dto = parseActionConfigJson<InboundActionConfigDto>(json);
    out.ac_targetHandler = dto.targetHandler ?? 'MQTT_FORWARD';
    out.ac_targetProductIdentification = dto.targetProductIdentification;
    out.ac_targetTopicTemplate = dto.targetTopicTemplate;
    out.ac_fieldMapping = dto.fieldMapping;
    out.ac_triggerRuleId = dto.triggerRuleId;
    return out;
  }
  const dto = parseActionConfigJson<OutboundActionConfigDto>(json);
  out.ac_payloadFormat = dto.payloadFormat ?? 'JSON';
  out.ac_payloadTemplate = dto.payloadTemplate;
  if (sourceType) {
    const sub = (dto as any)[sourceType.toLowerCase()];
    Object.assign(out, flattenActionByType(sourceType, sub));
  }
  return out;
}

// ============================== 内部小工具 ==============================

function composeDeviceFilter(values: Record<string, any>) {
  const ids = parseStringArray(values.mc_deviceIdentifications);
  const tags = parseStringArray(values.mc_deviceTagsAny);
  if (!ids?.length && !tags?.length) return undefined;
  return {
    deviceIdentifications: ids,
    tagsAny: tags,
  };
}

function composePayloadFilter(type?: string, expression?: string): PayloadFilterDto | undefined {
  if (!type || type === 'NONE') return undefined;
  return { type: type as PayloadFilterDto['type'], expression };
}

function parseStringArray(v: any): string[] | undefined {
  if (v === null || v === undefined || v === '') return undefined;
  if (Array.isArray(v)) return v.map(String).filter((s) => s.trim() !== '');
  return [String(v)];
}

function parseNumberArray(v: any): number[] | undefined {
  if (v === null || v === undefined || v === '') return undefined;
  const arr = Array.isArray(v) ? v : [v];
  const nums = arr.map((s) => Number(s)).filter((n) => Number.isFinite(n));
  return nums.length ? nums : undefined;
}
