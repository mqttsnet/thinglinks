import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'ptz';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export interface PtzControlParams {
  deviceIdentification: string;
  channelIdentification: string;
  command: string;
  direction?: string;
  moveSpeed?: number;
  zoomDirection?: number;
  zoomSpeed?: number;
  presetId?: number;
  cruiseId?: number;
  cruiseSpeed?: number;
  cruiseStayTime?: number;
  scanId?: number;
  scanSpeed?: number;
  switchId?: number;
  switchOn?: boolean;
}

export const Api = {
  Control: {
    url: `${ServicePrefix}/${MODULAR}/control`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Direction: {
    url: `${ServicePrefix}/${MODULAR}/direction`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Stop: {
    url: `${ServicePrefix}/${MODULAR}/stop`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  PresetSet: {
    url: `${ServicePrefix}/${MODULAR}/preset/set`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  PresetCall: {
    url: `${ServicePrefix}/${MODULAR}/preset/call`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  PresetDelete: {
    url: `${ServicePrefix}/${MODULAR}/preset/delete`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

const buildQuery = (params: Record<string, any>) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&');

export const ptzControl = (params: PtzControlParams) =>
  defHttp.request<boolean>({ ...Api.Control, url: `${Api.Control.url}?${buildQuery(params)}` });

export const ptzDirection = (params: PtzControlParams) =>
  defHttp.request<boolean>({ ...Api.Direction, url: `${Api.Direction.url}?${buildQuery(params)}` });

export const ptzStop = (deviceIdentification: string, channelIdentification: string) =>
  defHttp.request<boolean>({
    ...Api.Stop,
    url: `${Api.Stop.url}?${buildQuery({ deviceIdentification, channelIdentification })}`,
  });

export const presetSet = (params: PtzControlParams) =>
  defHttp.request<boolean>({ ...Api.PresetSet, url: `${Api.PresetSet.url}?${buildQuery(params)}` });

export const presetCall = (params: PtzControlParams) =>
  defHttp.request<boolean>({ ...Api.PresetCall, url: `${Api.PresetCall.url}?${buildQuery(params)}` });

export const presetDelete = (params: PtzControlParams) =>
  defHttp.request<boolean>({ ...Api.PresetDelete, url: `${Api.PresetDelete.url}?${buildQuery(params)}` });
