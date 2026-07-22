import {
  VideoStreamPushSaveVO,
  VideoStreamPushUpdateVO,
  VideoStreamPushResultVO,
  VideoStreamPushPageQuery,
} from './model/pushModel';
import type { PlayUrlResultVO } from './model/proxyModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'videoStreamPush';
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
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteStreamPush/${id}`,
      method: RequestEnum.DELETE,
    };
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
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
  defHttp.request<VideoStreamPushResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<VideoStreamPushPageQuery>) =>
  defHttp.request<PageResult<VideoStreamPushResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoStreamPushResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoStreamPushPageQuery) =>
  defHttp.request<VideoStreamPushResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoStreamPushSaveVO) =>
  defHttp.request<VideoStreamPushResultVO>({ ...Api.Save, params });

export const update = (params: VideoStreamPushUpdateVO) =>
  defHttp.request<VideoStreamPushResultVO>({ ...Api.Update, params });
export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const getPlayUrl = (id: string) =>
  defHttp.request<PlayUrlResultVO>({ ...Api.GetPlayUrl(id) });
