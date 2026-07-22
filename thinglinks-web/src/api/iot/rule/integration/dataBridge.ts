import {
  DataBridgeSaveVO,
  DataBridgeUpdateVO,
  DataBridgeResultVO,
  DataBridgePageQuery,
  TestSinkRequest,
  TestSinkResult,
} from './model/dataBridgeModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'dataBridge';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: { url: `${ServicePrefix}/${MODULAR}/page`, method: RequestEnum.POST } as AxiosRequestConfig,
  Save: { url: `${ServicePrefix}/${MODULAR}/saveDataBridge`, method: RequestEnum.POST } as AxiosRequestConfig,
  Update: { url: `${ServicePrefix}/${MODULAR}/updateDataBridge`, method: RequestEnum.PUT },
  Delete: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.DELETE } as AxiosRequestConfig,
  DeleteSingle: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/deleteDataBridge/${id}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig),
  TestSink: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/testSink/${id}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig),
  ChangeStatus: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}`,
    method: RequestEnum.PUT,
  } as AxiosRequestConfig),
  Copy: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/copy/${id}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig),
};

export const page = (params: PageParams<DataBridgePageQuery>) =>
  defHttp.request<PageResult<DataBridgeResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<DataBridgeResultVO>({ url: `${ServicePrefix}/${MODULAR}/getDataBridgeDetail/${id}`, method: RequestEnum.GET });

export const save = (params: DataBridgeSaveVO) =>
  defHttp.request<DataBridgeResultVO>({ ...Api.Save, params });

export const update = (params: DataBridgeUpdateVO) =>
  defHttp.request<DataBridgeResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) =>
  defHttp.request<boolean>({ ...Api.DeleteSingle(id) });

export const remove = (params: string[]) =>
  defHttp.request<boolean>({ ...Api.Delete, params });

/** 测试发送（基于规则当前配置 + 用户提供的样例 envelope，调用 thinglinks-util Sink.send 实际发送一次） */
export const testSink = (id: string, sampleEnvelope: TestSinkRequest) =>
  defHttp.request<TestSinkResult>({ ...Api.TestSink(id), params: sampleEnvelope });

/** 启停桥接规则（启用要求关联数据源 enable=true）
 * <p>enable 拼进 URL ── 后端用 @RequestParam，defHttp 会把 PUT 的 params 转 body 导致 query 丢失。
 */
export const changeStatus = (id: string, enable: boolean) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}?enable=${enable}`,
    method: RequestEnum.PUT,
  });

/** 复制规则（同模板新建一条 disabled 规则） */
export const copy = (id: string) =>
  defHttp.request<string>({ ...Api.Copy(id) });
