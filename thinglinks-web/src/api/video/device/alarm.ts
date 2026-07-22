import { VideoDeviceAlarmResultVO, AlarmStatisticsResultVO } from './model/alarmModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'deviceAlarm';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  List: {
    url: `${ServicePrefix}/${MODULAR}/list`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Statistics: {
    url: `${ServicePrefix}/${MODULAR}/statistics`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  UnhandledCount: {
    url: `${ServicePrefix}/${MODULAR}/unhandledCount`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Confirm: {
    url: `${ServicePrefix}/${MODULAR}/confirm`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Resolve: {
    url: `${ServicePrefix}/${MODULAR}/resolve`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Ignore: {
    url: `${ServicePrefix}/${MODULAR}/ignore`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  BatchIgnore: {
    url: `${ServicePrefix}/${MODULAR}/batchIgnore`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Handle: {
    url: `${ServicePrefix}/${MODULAR}/handle`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Clear: {
    url: `${ServicePrefix}/${MODULAR}/clear`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
};

export const page = (params: any) =>
  defHttp.request<any>({ ...Api.Page, data: params });

export const list = (deviceIdentification: string) =>
  defHttp.request<VideoDeviceAlarmResultVO[]>({
    ...Api.List,
    params: { deviceIdentification },
  });

export const statistics = (startTime?: string, endTime?: string) =>
  defHttp.request<AlarmStatisticsResultVO>({
    ...Api.Statistics,
    params: { startTime, endTime },
  });

export const unhandledCount = () =>
  defHttp.request<number>({ ...Api.UnhandledCount });

export const detail = (id: string | number) =>
  defHttp.request<VideoDeviceAlarmResultVO>({
    ...Api.Detail,
    url: `${Api.Detail.url}/${id}`,
  });

export const confirmAlarm = (alarmId: string, handleResult?: string) =>
  defHttp.request<boolean>({
    ...Api.Confirm,
    url: `${Api.Confirm.url}?alarmId=${alarmId}${handleResult ? `&handleResult=${encodeURIComponent(handleResult)}` : ''}`,
  });

export const resolveAlarm = (alarmId: string, handleResult?: string) =>
  defHttp.request<boolean>({
    ...Api.Resolve,
    url: `${Api.Resolve.url}?alarmId=${alarmId}${handleResult ? `&handleResult=${encodeURIComponent(handleResult)}` : ''}`,
  });

export const ignoreAlarm = (alarmId: string, handleResult?: string) =>
  defHttp.request<boolean>({
    ...Api.Ignore,
    url: `${Api.Ignore.url}?alarmId=${alarmId}${handleResult ? `&handleResult=${encodeURIComponent(handleResult)}` : ''}`,
  });

export const batchIgnore = (alarmIds: string[]) =>
  defHttp.request<boolean>({ ...Api.BatchIgnore, data: alarmIds });

export const handleAlarm = (alarmId: string, handleStatus: number) =>
  defHttp.request<boolean>({
    ...Api.Handle,
    url: `${Api.Handle.url}?alarmId=${alarmId}&handleStatus=${handleStatus}`,
  });

export const clearAlarm = (deviceIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.Clear,
    url: `${Api.Clear.url}?deviceIdentification=${encodeURIComponent(deviceIdentification)}`,
  });
