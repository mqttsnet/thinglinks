import {
  VideoPlatformSaveVO,
  VideoPlatformUpdateVO,
  VideoPlatformResultVO,
  VideoPlatformPageQuery,
} from './model/infoModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'platform';
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
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const copy = (id: string) =>
  defHttp.request<VideoPlatformResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<VideoPlatformPageQuery>) =>
  defHttp.request<PageResult<VideoPlatformResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoPlatformResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoPlatformPageQuery) =>
  defHttp.request<VideoPlatformResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoPlatformSaveVO) =>
  defHttp.request<VideoPlatformResultVO>({ ...Api.Save, params });

export const update = (params: VideoPlatformUpdateVO) =>
  defHttp.request<VideoPlatformResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const setEnable = (id: string, enable: boolean) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/${id}/enable?enable=${enable}`,
    method: RequestEnum.POST,
  });
