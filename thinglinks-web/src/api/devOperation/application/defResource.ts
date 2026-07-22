import {
  DefResourcePageQuery,
  DefResourceResultVO,
  DefResourceSaveVO,
  DefResourceUpdateVO,
} from './model/defResourceModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { ContentTypeEnum, RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'defResource';

export const Api = {
  Page: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Tree: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/tree`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Move: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/moveResource`,
    method: RequestEnum.PUT,
    headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
  } as AxiosRequestConfig,
  Get: (id: string) => {
    return {
      url: `${ServicePrefixEnum.TENANT}/${MODULAR}/${id}`,
      method: RequestEnum.GET,
    } as AxiosRequestConfig;
  },
  Save: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}`,
    method: RequestEnum.PUT,
  },
  Delete: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Check: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/check`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  CheckPath: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/checkPath`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  CheckName: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/checkName`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  ClearCache: {
    url: `${ServicePrefixEnum.TENANT}/${MODULAR}/clearCache`,
    method: RequestEnum.POST,
    headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
  } as AxiosRequestConfig,
};

export const tree = (params?: DefResourcePageQuery) =>
  defHttp.request<DefResourceResultVO>({ ...Api.Tree, params });

export const move = (id: string, parentId: string) =>
  defHttp.request<boolean>({ ...Api.Move, params: { id, parentId } });

export const page = (params: PageParams<DefResourcePageQuery>) =>
  defHttp.request<PageResult<DefResourceResultVO>>({ ...Api.Page, params });

export const query = (params: DefResourcePageQuery) =>
  defHttp.request<DefResourceResultVO[]>({ ...Api.Query, params });

export const get = (id: string) => defHttp.request<DefResourceResultVO[]>({ ...Api.Get(id) });

export const save = (params: DefResourceSaveVO) =>
  defHttp.request<DefResourceResultVO>({ ...Api.Save, params });

export const update = (params: DefResourceUpdateVO) =>
  defHttp.request<DefResourceResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const check = (code: string, id?: string) =>
  defHttp.request<boolean>({ ...Api.Check, params: { code, id } });

export const checkPath = (path: string, applicationId: string, id?: string) =>
  defHttp.request<boolean>({ ...Api.CheckPath, params: { id, applicationId, path } });
export const checkName = (name: string, applicationId: string, id?: string) =>
  defHttp.request<boolean>({ ...Api.CheckName, params: { id, applicationId, name } });

export const clearCache = (applicationId: string) =>
  defHttp.request<boolean>({ ...Api.ClearCache, params: { applicationId } });
