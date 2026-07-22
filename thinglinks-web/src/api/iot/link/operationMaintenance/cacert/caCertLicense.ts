import {
  CaCertLicenseSaveVO,
  CaCertLicenseUpdateVO,
  CaCertLicenseResultVO,
  CaCertLicensePageQuery,
  CaCertLicenseImpactResultVO,
} from './model/caCertLicenseModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'caCertLicense';
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
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Update: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.PUT,
  },
  Delete: {
    url: `${ServicePrefix}/${MODULAR}`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
  Query: {
    url: `${ServicePrefix}/${MODULAR}/query`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Import: {
    url: `${ServicePrefix}/${MODULAR}/importPemCertificate`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Impact: {
    url: `${ServicePrefix}/${MODULAR}/impact`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Revoke: {
    url: `${ServicePrefix}/${MODULAR}/revoke`,
    method: RequestEnum.PUT,
  } as AxiosRequestConfig,
  DownloadPack: {
    url: `${ServicePrefix}/${MODULAR}/issueClientCert`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Audit: {
    url: `${ServicePrefix}/${MODULAR}/audit`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const copy = (id: string) =>
  defHttp.request<CaCertLicenseResultVO>({ ...Api.Copy, params: { id } });

export const page = (params: PageParams<CaCertLicensePageQuery>) =>
  defHttp.request<PageResult<CaCertLicenseResultVO>>({ ...Api.Page, params });

export const detail = (id: string) =>
  defHttp.request<CaCertLicenseResultVO>({ ...Api.Detail, params: { id } });

export const query = (params: CaCertLicensePageQuery) =>
  defHttp.request<CaCertLicenseResultVO[]>({ ...Api.Query, params });

export const save = (params: CaCertLicenseSaveVO) =>
  defHttp.request<CaCertLicenseResultVO>({ ...Api.Save, params });

export const update = (params: CaCertLicenseUpdateVO) =>
  defHttp.request<CaCertLicenseResultVO>({ ...Api.Update, params });

export const importPemCertificate = (params: CaCertLicenseSaveVO) =>
  defHttp.request<CaCertLicenseResultVO>({ ...Api.Import, params });

export const remove = (params: string[]) => defHttp.request<boolean>({ ...Api.Delete, params });

/** 单条删除 ── 给 BusinessCardList 的 :deleteApi 用,与 bridge/datasource 风格对齐 */
export const deleteSingle = (id: string | number) => remove([String(id)]);

/** CA 证书影响面 ── 返回绑定此 CA 的设备数 / 在线数 / 前 50 条设备简要 */
export const getImpact = (id: string | number) =>
  defHttp.request<CaCertLicenseImpactResultVO>({
    ...Api.Impact,
    url: `${Api.Impact.url}/${id}`,
  });

/** 吊销 CA 证书 ── 会立即失效所有绑定此 CA 设备的 cache */
export const revoke = (id: string | number, revocationReason?: string) =>
  defHttp.request<boolean>({
    ...Api.Revoke,
    url: `${Api.Revoke.url}/${id}`,
    params: { revocationReason },
  });

/** 签发并下载客户端证书 ZIP 包 (浏览器自动下载) */
export const downloadClientCertPack = (id: string | number, notAfter: string) =>
  defHttp.request<Blob>(
    {
      ...Api.DownloadPack,
      params: { id, notAfter },
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );

/** CA 证书审计日志 ── 按时间倒序最多 200 条 */
export interface CaCertAuditLogVO {
  id?: string | number;
  caId?: number;
  caSerialNumber?: string;
  type?: string;
  detail?: string;
  createdBy?: number | string;
  createdTime?: string;
}
export const listAudit = (id: string | number) =>
  defHttp.request<CaCertAuditLogVO[]>({
    ...Api.Audit,
    url: `${Api.Audit.url}/${id}`,
  });
