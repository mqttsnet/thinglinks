import { AlarmSaveVO, AlarmUpdateVO, AlarmResultVO, AlarmPageQuery } from './model/alarmModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'ruleAlarm';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Detail: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/getRuleAlarmDetails/${id}`,
      method: RequestEnum.GET,
    } as AxiosRequestConfig;
  },
  Save: {
    url: `${ServicePrefix}/${MODULAR}/saveRuleAlarm`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}/updateRuleAlarm`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteRuleAlarm/${id}`,
      method: RequestEnum.DELETE,
    } as AxiosRequestConfig;
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<AlarmPageQuery>) =>
  defHttp.request<PageResult<AlarmResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<AlarmResultVO>({
    ...{
      url: `${ServicePrefix}/${MODULAR}/${id}`,
      method: RequestEnum.GET,
    },
    params: {},
  });

export const getRuleAlarmDetails = (id: string) =>
  defHttp.request<AlarmResultVO>({
    ...Api.Detail(id),
    params: {},
  });

export const save = (params: AlarmSaveVO) =>
  defHttp.request<AlarmResultVO>({ ...Api.Save, params });

export const update = (params: AlarmUpdateVO) =>
  defHttp.request<AlarmResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });
