import {
  DataSourceSaveVO,
  DataSourceUpdateVO,
  DataSourceResultVO,
  DataSourcePageQuery,
} from './model/dataSourceModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'dataSource';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: { url: `${ServicePrefix}/${MODULAR}/page`, method: RequestEnum.POST } as AxiosRequestConfig,
  Detail: { url: `${ServicePrefix}/${MODULAR}/detail`, method: RequestEnum.GET } as AxiosRequestConfig,
  Save: { url: `${ServicePrefix}/${MODULAR}/saveDataSource`, method: RequestEnum.POST } as AxiosRequestConfig,
  Update: { url: `${ServicePrefix}/${MODULAR}/updateDataSource`, method: RequestEnum.PUT },
  Delete: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.DELETE } as AxiosRequestConfig,
  DeleteSingle: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/deleteDataSource/${id}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig),
  TestConnection: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/testConnection/${id}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig),
  TestConnectionByForm: {
    url: `${ServicePrefix}/${MODULAR}/testConnectionByForm`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  ChangeStatus: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}`,
    method: RequestEnum.PUT,
  } as AxiosRequestConfig),
};

export const page = (params: PageParams<DataSourcePageQuery>) =>
  defHttp.request<PageResult<DataSourceResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<DataSourceResultVO>({ url: `${ServicePrefix}/${MODULAR}/getDataSourceDetail/${id}`, method: RequestEnum.GET });

export const save = (params: DataSourceSaveVO) =>
  defHttp.request<DataSourceResultVO>({ ...Api.Save, params });

export const update = (params: DataSourceUpdateVO) =>
  defHttp.request<DataSourceResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) =>
  defHttp.request<boolean>({ ...Api.DeleteSingle(id) });

export const remove = (params: string[]) =>
  defHttp.request<boolean>({ ...Api.Delete, params });

/** 测试连接（基于 DB 已保存数据源） */
export const testConnection = (id: string) =>
  defHttp.request<boolean>({ ...Api.TestConnection(id) });

/** 测试连接（基于编辑表单未保存的值） */
export const testConnectionByForm = (params: DataSourceSaveVO) =>
  defHttp.request<boolean>({ ...Api.TestConnectionByForm, params });

/** 启停数据源（启用前后端兜底校验测试连接）
 * <p>enable 拼进 URL ── 后端用 @RequestParam，defHttp 会把 PUT 的 params 转 body 导致 query 丢失。
 */
export const changeStatus = (id: string, enable: boolean) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}?enable=${enable}`,
    method: RequestEnum.PUT,
  });
