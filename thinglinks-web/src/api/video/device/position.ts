import { VideoDeviceMobilePositionResultVO } from './model/positionModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'mobilePosition';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Latest: {
    url: `${ServicePrefix}/${MODULAR}/latest`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Subscribe: {
    url: `${ServicePrefix}/${MODULAR}/subscribe`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Unsubscribe: {
    url: `${ServicePrefix}/${MODULAR}/unsubscribe`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const getLatestPosition = (deviceIdentification: string) =>
  defHttp.request<VideoDeviceMobilePositionResultVO>({
    ...Api.Latest,
    params: { deviceIdentification },
  });

export const subscribe = (deviceIdentification: string, interval: number = 5) =>
  defHttp.request<boolean>({
    ...Api.Subscribe,
    url: `${Api.Subscribe.url}?deviceIdentification=${encodeURIComponent(deviceIdentification)}&interval=${interval}`,
  });

export const unsubscribe = (deviceIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.Unsubscribe,
    url: `${Api.Unsubscribe.url}?deviceIdentification=${encodeURIComponent(deviceIdentification)}`,
  });
