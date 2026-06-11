import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'sipConfig';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export interface SipServerNodeVO {
  instanceId: string;
  monitorIps: string[];
  port: number;
  onlineStatus: boolean;
  registerTime: string;
}

export interface SipConfigResultVO {
  sipId: string;
  sipDomain: string;
  sipPort: number;
  sipPassword: string;
  sipHost: string;
  registerTimeInterval: number;
  serverNodes: SipServerNodeVO[];
}

export const Api = {
  Info: {
    url: `${ServicePrefix}/${MODULAR}/info`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const getSipConfigInfo = () =>
  defHttp.request<SipConfigResultVO>({ ...Api.Info });
