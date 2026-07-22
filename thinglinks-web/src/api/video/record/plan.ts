import {
  VideoRecordPlanSaveVO,
  VideoRecordPlanUpdateVO,
  VideoRecordPlanResultVO,
  VideoRecordPlanPageQuery,
} from './model/planModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'recordPlan';
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
  Active: {
    url: `${ServicePrefix}/${MODULAR}/active`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<VideoRecordPlanPageQuery>) =>
  defHttp.request<PageResult<VideoRecordPlanResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<VideoRecordPlanResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: VideoRecordPlanPageQuery) =>
  defHttp.request<VideoRecordPlanResultVO[]>({ ...Api.Query, params });

export const save = (params: VideoRecordPlanSaveVO) =>
  defHttp.request<VideoRecordPlanResultVO>({ ...Api.Save, params });

export const update = (params: VideoRecordPlanUpdateVO) =>
  defHttp.request<VideoRecordPlanResultVO>({ ...Api.Update, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.Delete, params: [id] });

export const active = () =>
  defHttp.request<VideoRecordPlanResultVO[]>({ ...Api.Active });

export const activate = (id: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/${id}/activate`,
    method: RequestEnum.POST,
  });

export const deactivate = (id: string) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/${id}/deactivate`,
    method: RequestEnum.POST,
  });
