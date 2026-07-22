import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  // 流监控
  StreamList: {
    url: `${ServicePrefix}/streamMonitor/list`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  StreamOverview: {
    url: `${ServicePrefix}/streamMonitor/overview`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  // 告警统计
  AlarmStatistics: {
    url: `${ServicePrefix}/deviceAlarm/statistics`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  AlarmUnhandledCount: {
    url: `${ServicePrefix}/deviceAlarm/unhandledCount`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  // 通道位置 (供地图使用)
  ChannelLocationList: {
    url: `${ServicePrefix}/videoChannel/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const getStreamList = () => defHttp.request<any[]>({ ...Api.StreamList });

export const getStreamOverview = () => defHttp.request<any>({ ...Api.StreamOverview });

export const getAlarmStatistics = (startTime?: string, endTime?: string) =>
  defHttp.request<any>({ ...Api.AlarmStatistics, params: { startTime, endTime } });

export const getAlarmUnhandledCount = () => defHttp.request<number>({ ...Api.AlarmUnhandledCount });

export const getChannelLocationList = (params?: any) =>
  defHttp.request<any>({ ...Api.ChannelLocationList, data: params });
