import {
  OtaUpgradeRecordsSaveVO,
  OtaUpgradeRecordsUpdateVO,
  OtaUpgradeRecordsResultVO,
  OtaUpgradeRecordsPageQuery,
} from './model/otaUpgradeRecordsModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'otaUpgradeRecords';
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}/detail`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Copy: {
    url: `${ServicePrefix}/${MODULAR}/copy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.PUT,
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  GetOtaRecordsSummary: function (taskId: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/getOtaUpgradeRecordsSummary/${taskId}`,
      method: RequestEnum.GET,
    } as AxiosRequestConfig;
  },
  SingleQuery: {
    url: `${ServicePrefix}/${MODULAR}/detail`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const copy = (id: string) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<OtaUpgradeRecordsPageQuery>) =>
  defHttp.request<PageResult<OtaUpgradeRecordsResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: OtaUpgradeRecordsPageQuery) =>
  defHttp.request<OtaUpgradeRecordsResultVO[]>({ ...Api.Query, params });

export const save = (params: OtaUpgradeRecordsSaveVO) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.Save, params });

export const update = (params: OtaUpgradeRecordsUpdateVO) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const GetOtaRecordsSummary = (taskId: string) =>
  defHttp.request<any>({ ...Api.GetOtaRecordsSummary(taskId) });

export const getSingleQuery = (id: string) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.SingleQuery, params: { id } });
