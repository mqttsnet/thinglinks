import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'deviceControl';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export interface DeviceControlParams {
  deviceIdentification: string;
  channelIdentification: string;
  controlType: string;
  controlValue?: string;
}

const buildQuery = (params: Record<string, any>) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&');

export const Api = {
  Control: {
    url: `${ServicePrefix}/${MODULAR}/control`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  TeleBoot: {
    url: `${ServicePrefix}/${MODULAR}/teleBoot`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  RecordStart: {
    url: `${ServicePrefix}/${MODULAR}/record/start`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  RecordStop: {
    url: `${ServicePrefix}/${MODULAR}/record/stop`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  GuardSet: {
    url: `${ServicePrefix}/${MODULAR}/guard/set`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  GuardReset: {
    url: `${ServicePrefix}/${MODULAR}/guard/reset`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  ForceKeyFrame: {
    url: `${ServicePrefix}/${MODULAR}/forceKeyFrame`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const deviceControl = (params: DeviceControlParams) =>
  defHttp.request<boolean>({ ...Api.Control, params });

export const teleBoot = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.TeleBoot,
    url: `${Api.TeleBoot.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const recordStart = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.RecordStart,
    url: `${Api.RecordStart.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const recordStop = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.RecordStop,
    url: `${Api.RecordStop.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const guardSet = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.GuardSet,
    url: `${Api.GuardSet.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const guardReset = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.GuardReset,
    url: `${Api.GuardReset.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const forceKeyFrame = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.ForceKeyFrame,
    url: `${Api.ForceKeyFrame.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });
