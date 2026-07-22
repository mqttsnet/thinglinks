import {
  VideoGatewayMappingSaveVO,
  VideoGatewayMappingUpdateVO,
  VideoGatewayMappingResultVO,
  VideoGatewayMappingPageQuery,
} from './model/mappingModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'videoGatewayMapping';
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
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<VideoGatewayMappingPageQuery>) =>
  defHttp.request<PageResult<VideoGatewayMappingResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoGatewayMappingResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoGatewayMappingPageQuery) =>
  defHttp.request<VideoGatewayMappingResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoGatewayMappingSaveVO) =>
  defHttp.request<VideoGatewayMappingResultVO>({ ...Api.Save, params });

export const update = (params: VideoGatewayMappingUpdateVO) =>
  defHttp.request<VideoGatewayMappingResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.Delete, params: [id] });
