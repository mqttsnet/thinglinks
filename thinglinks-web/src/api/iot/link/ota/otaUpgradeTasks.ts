import {
  OtaUpgradeTasksSaveVO,
  OtaUpgradeTasksUpdateVO,
  OtaUpgradeTasksResultVO,
  OtaUpgradeTasksPageQuery,
} from './model/otaUpgradeTasksModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'otaUpgradeTasks';
// tips: 建议在ServicePrefixEnum中新增：LINK = '/link'，并将下方代码改为： const ServicePrefix = ServicePrefixEnum.LINK;
// tips: /link 需要与 thinglinks-gateway-server.yml中配置的Path一致，否则无法正常调用接口！！！
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Detail: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/details/${id}`,
      method: RequestEnum.GET,
    };
  } as AxiosRequestConfig,
  Copy: {
    url: `${ServicePrefix}/${MODULAR}/copy`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Save: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.PUT,
  },
  DeleteSingle: function (id: string) {
    return {
      url: `${ServicePrefix}/${MODULAR}/deleteOtaUpgradeTask/${id}`,
      method: RequestEnum.DELETE,
    };
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}/deleteOtaUpgradeTasks`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  UpgradeAgain: {
    url: `${ServicePrefix}/${MODULAR}/sendDeviceOtaUpgradeCommand`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  SaveV2: {
    url: `${ServicePrefix}/${MODULAR}/saveUpgradeTask`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  UpdateV2: {
    url: `${ServicePrefix}/${MODULAR}/updateUpgradeTask`,
    method: RequestEnum.PUT,
  },
};

export const copy = (id: string) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<OtaUpgradeTasksPageQuery>) =>
  defHttp.request<PageResult<OtaUpgradeTasksResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.Detail(id) });

export const query = (params: OtaUpgradeTasksPageQuery) =>
  defHttp.request<OtaUpgradeTasksResultVO[]>({ ...Api.Query, params });

export const save = (params: OtaUpgradeTasksSaveVO) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.Save, params });

export const update = (params: OtaUpgradeTasksUpdateVO) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.Update, params });

export const saveV2 = (params: OtaUpgradeTasksSaveVO) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.SaveV2, params });

export const updateV2 = (params: OtaUpgradeTasksUpdateVO) =>
  defHttp.request<OtaUpgradeTasksResultVO>({ ...Api.UpdateV2, params });

export const deleteSingle = (id: string) => defHttp.request<boolean>({ ...Api.DeleteSingle(id) });
export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

export const upgradeAgain = (params: OtaUpgradeRecordsUpdateVO) =>
  defHttp.request<OtaUpgradeRecordsResultVO>({ ...Api.UpgradeAgain, params });
