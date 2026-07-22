/**
 * 协议总线统计 API ── 租户隔离(后端从 ContextUtil 自动注入 tenantId)
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const ServicePrefix = ServicePrefixEnum.MQS;
const MODULAR = 'bus/stats';

/** 当日 dispatcher 总事件分布(label → count) */
export interface DispatchStatsMap {
  [label: string]: number;
}

/** 当日 stage 执行分布(label → count) */
export interface StageStatsMap {
  [label: string]: number;
}

/** 当日 relay 投递分布(label → count) */
export interface RelayStatsMap {
  [label: string]: number;
}

/** 总览健康度 */
export interface BusHealth {
  total: number;
  success: number;
  failed: number;
  dropped: number;
  noRoute: number;
}

/** 全维度汇总(前端 dashboard 一次拉齐) */
export interface BusTodaySummary {
  dispatch: DispatchStatsMap;
  stage: StageStatsMap;
  relay: RelayStatsMap;
  noRoute: { [label: string]: number };
  canonicalizeFail: { [label: string]: number };
}

const Api = {
  TodayDispatch: { url: `${ServicePrefix}/${MODULAR}/today/dispatch`, method: RequestEnum.GET } as AxiosRequestConfig,
  TodayStage: { url: `${ServicePrefix}/${MODULAR}/today/stage`, method: RequestEnum.GET } as AxiosRequestConfig,
  TodayRelay: { url: `${ServicePrefix}/${MODULAR}/today/relay`, method: RequestEnum.GET } as AxiosRequestConfig,
  TodaySummary: { url: `${ServicePrefix}/${MODULAR}/today/summary`, method: RequestEnum.GET } as AxiosRequestConfig,
  TodayHealth: { url: `${ServicePrefix}/${MODULAR}/today/health`, method: RequestEnum.GET } as AxiosRequestConfig,
  Dimension: { url: `${ServicePrefix}/${MODULAR}/dimension`, method: RequestEnum.GET } as AxiosRequestConfig,
};

export const queryTodayDispatch = () => defHttp.request<DispatchStatsMap>({ ...Api.TodayDispatch });

export const queryTodayStage = () => defHttp.request<StageStatsMap>({ ...Api.TodayStage });

export const queryTodayRelay = () => defHttp.request<RelayStatsMap>({ ...Api.TodayRelay });

export const queryTodaySummary = () => defHttp.request<BusTodaySummary>({ ...Api.TodaySummary });

export const queryTodayHealth = () => defHttp.request<BusHealth>({ ...Api.TodayHealth });

export const queryDimension = (date: string | undefined, dimension: string) =>
  defHttp.request<{ [label: string]: number }>({
    ...Api.Dimension,
    params: { date, dimension },
  });
