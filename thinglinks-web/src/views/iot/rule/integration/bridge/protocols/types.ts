import type { FormSchema } from '/@/components/Form';

/**
 * 桥接规则 - 出站动作协议模块统一契约（Strategy + Registry）。
 *
 * <p>每种数据源类型在桥接 action_config_json 里的子段（kafka/redis/.../pulsar）
 * 对应一个 BridgeOutboundActionModule。把"按 sourceType 分支"集中到 registry 一处，
 * 各协议代码隔离到独立 .ts 文件，<b>修改一个协议不影响其它 14 个</b>。
 *
 * <h3>每个文件需实现 4 个职责</h3>
 * <ol>
 *   <li>{@link #actionFields} ── 出站 ac_xxx_* 表单字段（FormSchema 数组）</li>
 *   <li>{@link #assembleAction} ── 提交时 flat values → nested DTO（assembleActionConfigJson）</li>
 *   <li>{@link #flattenAction} ── 编辑回显时 nested DTO → flat values（flattenActionConfig）</li>
 *   <li>{@link #presetToFlat} ── "加载示例"时 preset 子对象 → flat values</li>
 * </ol>
 *
 * @author mqttsnet
 */
export interface BridgeOutboundActionModule<TActionDto = any> {
  /** sourceType 标识（与 ConnectorType / 字典 BRIDGE_DATA_SOURCE_TYPE 对齐） */
  readonly type: string;

  /** 出站 action 段表单字段（不含 payloadFormat / payloadTemplate 等共用 head） */
  actionFields(): FormSchema[];

  /**
   * 提交时把 flat values 组装成 OutboundActionConfigDto 子对象。
   *
   * @param values 表单全量 values（含 ac_xxx_yyy 格式的扁平字段）
   * @return 协议子对象（如 Kafka 返回 { partitionKey, headers }）；空字段返回 undefined 由 prune 过滤
   */
  assembleAction(values: Record<string, any>): TActionDto;

  /**
   * 编辑时反序列化协议子对象为 flat values。
   *
   * @param dto OutboundActionConfigDto.kafka 等子对象（可空）
   * @return flat 字段（key 用 ac_xxx_yyy 前缀）
   */
  flattenAction(dto: TActionDto | undefined): Record<string, any>;

  /**
   * "加载示例"时把 preset 子对象转 flat values（与 flattenAction 类似但宽容字段缺失）。
   */
  presetToFlat(preset: TActionDto | undefined): Record<string, any>;
}
