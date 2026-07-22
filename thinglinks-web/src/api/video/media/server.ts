import {
  VideoMediaServerSaveVO,
  VideoMediaServerUpdateVO,
  VideoMediaServerResultVO,
  VideoMediaServerPageQuery,
  VideoMediaServerMetricsResultVO,
} from './model/serverModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'videoMediaServer';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}/detail`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  RealTimeMetrics: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/realTimeMetrics/${id}`,
      method: RequestEnum.GET,
    } as AxiosRequestConfig;
  },
  Copy: {
    url: `${ServicePrefix}/${MODULAR}/copy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}/saveMediaServer`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}/updateMediaServer`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteMediaServer/${id}`,
      method: RequestEnum.DELETE,
    } as AxiosRequestConfig;
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}/deleteMediaServers`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const copy = (id: string) =>
  defHttp.request<VideoMediaServerResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<VideoMediaServerPageQuery>) =>
  defHttp.request<PageResult<VideoMediaServerResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoMediaServerResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoMediaServerPageQuery) =>
  defHttp.request<VideoMediaServerResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoMediaServerSaveVO) =>
  defHttp.request<VideoMediaServerResultVO>({ ...Api.Save, params });

export const update = (params: VideoMediaServerUpdateVO) =>
  defHttp.request<VideoMediaServerResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const realTimeMetrics = (id: string) =>
  defHttp.request<VideoMediaServerMetricsResultVO>({ ...Api.RealTimeMetrics(id) });

export const testConnection = (host: string, httpPort: number, secret: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/testConnection`,
    method: RequestEnum.GET,
    params: { host, httpPort, secret },
  });
