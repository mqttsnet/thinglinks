import {
  OtaUpgradesSaveVO,
  OtaUpgradesUpdateVO,
  OtaUpgradesResultVO,
  OtaUpgradesPageQuery,
} from './model/otaUpgradesModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'otaUpgrades';
// tips: 建议在ServicePrefixEnum中新增：LINK = '/link'，并将下方代码改为： const ServicePrefix = ServicePrefixEnum.LINK;
// tips: /link 需要与 thinglinks-gateway-server.yml中配置的Path一致，否则无法正常调用接口！！！
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}/detail`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Copy: {
    url: `${ServicePrefix}/${MODULAR}/copy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}/saveUpgradePackage`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}/updateUpgradePackage`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteOtaUpgrade/${id}`,
      method: RequestEnum.DELETE,
    } as AxiosRequestConfig;
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}/deleteOtaUpgrades`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

export const copy = (id: string) =>
  defHttp.request<OtaUpgradesResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<OtaUpgradesPageQuery>) =>
  defHttp.request<PageResult<OtaUpgradesResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<OtaUpgradesResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: OtaUpgradesPageQuery) =>
  defHttp.request<OtaUpgradesResultVO[]>({ ...Api.Query, params });

export const save = (params: OtaUpgradesSaveVO) =>
  defHttp.request<OtaUpgradesResultVO>({ ...Api.Save, params });

export const update = (params: OtaUpgradesUpdateVO) =>
  defHttp.request<OtaUpgradesResultVO>({ ...Api.Update, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });
