import {
  BridgeExecutionTraceResultVO,
  BridgeExecutionTracePageQuery,
} from './model/bridgeExecutionTraceModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'bridgeExecutionTrace';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: { url: `${ServicePrefix}/${MODULAR}/page`, method: RequestEnum.POST } as AxiosRequestConfig,
  Detail: (traceId: string, ruleId?: number | string | null) => ({
    url: `${ServicePrefix}/${MODULAR}/detail/${traceId}`,
    method: RequestEnum.GET,
    params: ruleId != null ? { ruleId } : undefined,
  } as AxiosRequestConfig),
  Replay: (traceId: string) => ({
    url: `${ServicePrefix}/${MODULAR}/replay/${traceId}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig),
};

/** trace 列表（按规则/状态/时间区间过滤） */
export const page = (params: PageParams<BridgeExecutionTracePageQuery>) =>
  defHttp.request<PageResult<BridgeExecutionTraceResultVO>>({ ...Api.Page, params });

/** trace 详情（含 steps）── 多规则场景务必传 ruleId 精确定位,否则后端返回 traceId 下最新一条 */
export const detail = (traceId: string, ruleId?: number | string | null) =>
  defHttp.request<BridgeExecutionTraceResultVO>({ ...Api.Detail(traceId, ruleId) });

/** 死信重放（仅 status=03 死信状态可调，触发新 trace） */
export const replay = (traceId: string) =>
  defHttp.request<string>({ ...Api.Replay(traceId) });
