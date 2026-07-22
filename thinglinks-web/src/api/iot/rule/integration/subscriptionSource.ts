import {
  SubscriptionSourceSaveVO,
  SubscriptionSourceUpdateVO,
  SubscriptionSourceResultVO,
  SubscriptionSourcePageQuery,
} from './model/subscriptionSourceModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'subscriptionSource';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  Page: { url: `${ServicePrefix}/${MODULAR}/page`, method: RequestEnum.POST } as AxiosRequestConfig,
  Save: { url: `${ServicePrefix}/${MODULAR}/saveSubscriptionSource`, method: RequestEnum.POST } as AxiosRequestConfig,
  Update: { url: `${ServicePrefix}/${MODULAR}/updateSubscriptionSource`, method: RequestEnum.PUT },
  Delete: { url: `${ServicePrefix}/${MODULAR}`, method: RequestEnum.DELETE } as AxiosRequestConfig,
  DeleteSingle: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/deleteSubscriptionSource/${id}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig),
  ChangeStatus: (id: string) => ({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}`,
    method: RequestEnum.PUT,
  } as AxiosRequestConfig),
};

export const page = (params: PageParams<SubscriptionSourcePageQuery>) =>
  defHttp.request<PageResult<SubscriptionSourceResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<SubscriptionSourceResultVO>({ url: `${ServicePrefix}/${MODULAR}/getSubscriptionSourceDetail/${id}`, method: RequestEnum.GET });

export const save = (params: SubscriptionSourceSaveVO) =>
  defHttp.request<SubscriptionSourceResultVO>({ ...Api.Save, params });

export const update = (params: SubscriptionSourceUpdateVO) =>
  defHttp.request<SubscriptionSourceResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) =>
  defHttp.request<boolean>({ ...Api.DeleteSingle(id) });

export const remove = (params: string[]) =>
  defHttp.request<boolean>({ ...Api.Delete, params });

/** 启停订阅源（启用时启动后台 KafkaConsumer/MqttClient subscribe；禁用时 stop）
 * <p>enable 拼进 URL ── 后端用 @RequestParam，defHttp 会把 PUT 的 params 转 body 导致 query 丢失。
 */
export const changeStatus = (id: string, enable: boolean) =>
  defHttp.request<boolean>({
    url: `${ServicePrefix}/${MODULAR}/changeStatus/${id}?enable=${enable}`,
    method: RequestEnum.PUT,
  });
