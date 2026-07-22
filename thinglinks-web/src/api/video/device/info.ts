import {
  VideoDeviceInfoSaveVO,
  VideoDeviceInfoUpdateVO,
  VideoDeviceInfoResultVO,
  VideoDeviceInfoPageQuery,
} from './model/infoModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'videoDevice';
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
  defHttp.request<VideoDeviceInfoResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<VideoDeviceInfoPageQuery>) =>
  defHttp.request<PageResult<VideoDeviceInfoResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoDeviceInfoResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoDeviceInfoPageQuery) =>
  defHttp.request<VideoDeviceInfoResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoDeviceInfoSaveVO) =>
  defHttp.request<VideoDeviceInfoResultVO>({ ...Api.Save, params });

export const update = (params: VideoDeviceInfoUpdateVO) =>
  defHttp.request<VideoDeviceInfoResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });
