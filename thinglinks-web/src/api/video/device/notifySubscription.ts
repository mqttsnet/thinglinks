import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'notifySubscription';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.PUT,
  } as AxiosRequestConfig,
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Variables: {
    url: `${ServicePrefix}/${MODULAR}/variables`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const page = (params: any) =>
  defHttp.request<any>({ ...Api.Page, data: params });

export const save = (data: any) =>
  defHttp.request<any>({ ...Api.Save, data });

export const update = (data: any) =>
  defHttp.request<any>({ ...Api.Update, data });

export const remove = (ids: string[]) =>
  defHttp.request<boolean>({ ...Api.Delete, data: ids });

export const detail = (id: string) =>
  defHttp.request<any>({ ...Api.Detail, params: { id } });

export const getVariables = (eventType: string) =>
  defHttp.request<{ key: string; label: string }[]>({
    ...Api.Variables,
    params: { eventType },
  });
