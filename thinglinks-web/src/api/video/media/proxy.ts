import {
  VideoStreamProxySaveVO,
  VideoStreamProxyUpdateVO,
  VideoStreamProxyResultVO,
  VideoStreamProxyPageQuery,
  PlayUrlResultVO,
} from './model/proxyModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'videoStreamProxy';
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
  Copy: {
    url: `${ServicePrefix}/${MODULAR}/copy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}/saveStreamProxy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}/updateStreamProxy`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteStreamProxy/${id}`,
      method: RequestEnum.DELETE,
    };
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}/deleteStreamProxies`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  GetPlayUrl: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/getPlayUrl/${id}`,
      method: RequestEnum.GET,
    };
  },
};

export const copy = (id: string) =>
  defHttp.request<VideoStreamProxyResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<VideoStreamProxyPageQuery>) =>
  defHttp.request<PageResult<VideoStreamProxyResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoStreamProxyResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoStreamProxyPageQuery) =>
  defHttp.request<VideoStreamProxyResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoStreamProxySaveVO) =>
  defHttp.request<VideoStreamProxyResultVO>({ ...Api.Save, params });

export const update = (params: VideoStreamProxyUpdateVO) =>
  defHttp.request<VideoStreamProxyResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const getPlayUrl = (id: string) =>
  defHttp.request<PlayUrlResultVO>({ ...Api.GetPlayUrl(id) });
