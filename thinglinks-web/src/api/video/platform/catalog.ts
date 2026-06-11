import { VideoPlatformCatalogSaveVO, VideoPlatformCatalogUpdateVO, VideoPlatformCatalogResultVO, VideoPlatformCatalogPageQuery } from './model/catalogModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'platformCatalog';
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
  ListByPlatform: {
    url: `${ServicePrefix}/${MODULAR}/listByPlatform`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<VideoPlatformCatalogPageQuery>) =>
  defHttp.request<PageResult<VideoPlatformCatalogResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoPlatformCatalogResultVO>({ ...Api.Detail, params: { id } });

export const listByPlatform = (platformId: string) =>
  defHttp.request<VideoPlatformCatalogResultVO[]>({ ...Api.ListByPlatform, params: { platformId } });

export const save = (params: VideoPlatformCatalogSaveVO) =>
  defHttp.request<VideoPlatformCatalogResultVO>({ ...Api.Save, params });

export const update = (params: VideoPlatformCatalogUpdateVO) =>
  defHttp.request<VideoPlatformCatalogResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });
