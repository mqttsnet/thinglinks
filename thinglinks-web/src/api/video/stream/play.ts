import { PlayStartParams, PlayResultVO } from './model/playModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'play';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Start: {
    url: `${ServicePrefix}/${MODULAR}/start`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Stop: {
    url: `${ServicePrefix}/${MODULAR}/stop`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  StreamInfo: {
    url: `${ServicePrefix}/${MODULAR}/streamInfo`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const playStart = (params: PlayStartParams) =>
  defHttp.request<PlayResultVO>({ ...Api.Start, params });

export const playStop = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.Stop,
    url: `${Api.Stop.url}?deviceIdentification=${encodeURIComponent(deviceIdentification)}&channelIdentification=${encodeURIComponent(channelIdentification)}`,
  });

export const streamInfo = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<PlayResultVO>({
    ...Api.StreamInfo,
    params: { deviceIdentification, channelIdentification },
  });
