import {
  ExecutionLogPageQuery,
  GetRuleExecutionLogDetails,
  RuleExecutionLogStatsResult,
} from './model/executionLogModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'ruleExecutionLog';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,

  GetRuleExecutionLogDetails: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/getRuleExecutionLogDetails/${id}`,
      method: RequestEnum.GET,
    } as AxiosRequestConfig;
  },

  Stats: {
    url: `${ServicePrefix}/${MODULAR}/stats`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,

  Clear: {
    url: `${ServicePrefix}/${MODULAR}/clear`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<ExecutionLogPageQuery>) =>
  defHttp.request<PageResult<GetRuleExecutionLogDetails>>({ ...Api.Page, params });

export const getRuleExecutionLogDetails = (id: string) =>
  defHttp.request<GetRuleExecutionLogDetails>({ ...Api.GetRuleExecutionLogDetails(id) });

export const stats = (params: PageParams<ExecutionLogPageQuery>) =>
  defHttp.request<RuleExecutionLogStatsResult>(
    { ...Api.Stats, params },
    { errorMessageMode: 'none' },
  );

export const clearLogs = (params: PageParams<ExecutionLogPageQuery>) =>
  defHttp.request<number>({ ...Api.Clear, params });
