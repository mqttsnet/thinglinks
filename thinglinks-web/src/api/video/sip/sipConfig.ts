import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';
import type { SipServerInfo } from './model/sipConfigModel';

const MODULAR = 'sipConfig';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Page: { url: `${ServicePrefix}/${MODULAR}/page`, method: RequestEnum.POST } as AxiosRequestConfig,
  Save: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.POST } as AxiosRequestConfig,
  Update: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.PUT } as AxiosRequestConfig,
  Delete: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.DELETE } as AxiosRequestConfig,
  Detail: { url: `${ServicePrefix}/${MODULAR}/detail`, method: RequestEnum.GET } as AxiosRequestConfig,
  SetDefault: (id: string) =>
    ({ url: `${ServicePrefix}/${MODULAR}/${id}/setDefault`, method: RequestEnum.POST } as AxiosRequestConfig),
  RefreshCache: { url: `${ServicePrefix}/${MODULAR}/refreshCache`, method: RequestEnum.POST } as AxiosRequestConfig,
  ServerInfo: { url: `${ServicePrefix}/${MODULAR}/serverInfo`, method: RequestEnum.GET } as AxiosRequestConfig,
};

export const page = (params: any) => defHttp.request<any>({ ...Api.Page, data: params });
export const save = (data: any) => defHttp.request<any>({ ...Api.Save, data });
export const update = (data: any) => defHttp.request<any>({ ...Api.Update, data });
export const remove = (ids: string[]) => defHttp.request<boolean>({ ...Api.Delete, data: ids });
export const detail = (id: string) => defHttp.request<any>({ ...Api.Detail, params: { id } });
export const setDefault = (id: string) => defHttp.request<boolean>(Api.SetDefault(id));
export const refreshCache = () => defHttp.request<boolean>(Api.RefreshCache);
export const getServerInfo = () => defHttp.request<SipServerInfo>(Api.ServerInfo);
