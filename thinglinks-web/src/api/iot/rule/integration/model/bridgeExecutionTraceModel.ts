/**
 * 桥接执行 trace + step 模型定义
 *
 * trace 主表 + step 步骤表两层模型支持"链路回放"UI（参照场景联动详情抽屉）
 */

export interface BridgeExecutionTracePageQuery {
  traceId?: string;
  bridgeRuleId?: string;
  direction?: string;
  triggerSource?: string;        // DEVICE_DATA / SUBSCRIPTION / TEST_SINK / REPLAY
  tenantId?: string;
  productIdentification?: string;
  deviceIdentification?: string;
  actionType?: string;           // PUBLISH / CONNECT / CLOSE / ...
  status?: string;               // 00-成功 / 01-失败 / 02-部分成功 / 03-死信
  startTimeBegin?: string;       // 时间区间起
  startTimeEnd?: string;
}

export interface BridgeExecutionStepResultVO {
  echoMap?: any;
  id?: string;
  traceId?: string;
  stepNo?: number;
  stepType?: string;             // INGEST / RULE_MATCH / RATE_LIMIT / TRANSFORM / SINK_SEND / DEAD_LETTER / INBOUND_FORWARD
  stepName?: string;             // 步骤可读名称（中文）
  status?: string;               // 00-成功 / 01-失败 / 02-跳过
  latencyMs?: number;
  inputSummary?: string;         // 输入摘要 JSON
  outputSummary?: string;        // 输出摘要 JSON
  errorMsg?: string;
  startedAt?: string;            // 步骤开始时间（毫秒精度）
  extendParams?: string;         // 步骤特异协议数据 JSON
  remark?: string;
}

export interface BridgeExecutionTraceResultVO {
  echoMap?: any;
  id?: string;
  traceId?: string;
  bridgeRuleId?: string;
  direction?: string;
  triggerSource?: string;
  tenantId?: string;
  productIdentification?: string;
  deviceIdentification?: string;
  actionType?: string;
  topic?: string;
  dataSourceId?: string;
  subscriptionSourceId?: string;
  status?: string;
  stepCount?: number;
  totalLatencyMs?: number;
  startTime?: string;
  endTime?: string;
  sourcePayloadSummary?: string;
  resultSummary?: string;
  errorMsg?: string;
  extendParams?: string;
  remark?: string;
  /** 详情接口附带 steps 子集合（按 step_no 升序） */
  steps?: BridgeExecutionStepResultVO[];
}

export interface BridgeExecutionStepPageQuery {
  traceId?: string;
  stepType?: string;
  status?: string;
}
