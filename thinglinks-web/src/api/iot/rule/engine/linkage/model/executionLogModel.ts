export interface ExecutionLogPageQuery {
  id?: string;
  appId?: string; // 应用ID
  ruleIdentification?: string; // 唯一标识
  ruleName?: string; // 规则名称
  status?: string; // 规则执行状态：0-未执行，1-执行中，2-已完成
  startTime?: string; // 规则执行开始时间
  startTimeBegin?: string; // 规则执行开始时间-起
  startTimeEnd?: string; // 规则执行开始时间-止
  endTime?: string; // 规则执行结束时间
  remark?: string; // 产品
  extendParams?: string; // 扩展参数（文本格式）
  startTimeRange?: string[];
}

export interface RuleExecutionLogStatsResult {
  total?: number;
  completed?: number;
  executing?: number;
  notExecuted?: number;
  avgLatencyMs?: number;
  timeline?: RuleExecutionLogTimelinePoint[];
}

export interface RuleExecutionLogTimelinePoint {
  timeLabel?: string;
  completed?: number;
  executing?: number;
  notExecuted?: number;
  avgLatencyMs?: number;
}

export interface RuleExecutionLogStepResult {
  id?: string;
  stepNo?: number;
  stepType?: string;
  stepName?: string;
  status?: string;
  latencyMs?: number;
  inputSummary?: string;
  outputSummary?: string;
  errorMsg?: string;
  startedAt?: string;
  extendParams?: string;
  remark?: string;
}

export interface GetRuleExecutionLogDetails {
  id?: string;
  ruleIdentification?: string;
  ruleName?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
  remark?: string;
  extendParams?: string;
  stepCount?: number;
  totalLatencyMs?: number;
  triggerSource?: string;
  resultSummary?: string;
  steps?: RuleExecutionLogStepResult[];
}
