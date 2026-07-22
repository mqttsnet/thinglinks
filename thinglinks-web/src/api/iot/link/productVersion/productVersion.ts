import {
  ProductVersionDiffVO,
  ProductVersionPublishVO,
  ProductVersionPurgeVO,
  ProductVersionResultVO,
  ProductVersionRollbackVO,
  ProductVersionStatisticsResultVO,
} from './model/productVersionModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'productVersion';
const ServicePrefix = ServicePrefixEnum.LINK;

/** REST 路由集中表。 */
export const Api = {
  ListByProduct: {
    url: `${ServicePrefix}/${MODULAR}/listByProduct`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Detail: {
    url: `${ServicePrefix}/${MODULAR}/detailByVersionNo`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Publish: {
    url: `${ServicePrefix}/${MODULAR}/publish`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Rollback: {
    url: `${ServicePrefix}/${MODULAR}/rollback`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  PurgeHistory: {
    url: `${ServicePrefix}/${MODULAR}/purgeHistory`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Diff: {
    url: `${ServicePrefix}/${MODULAR}/diff`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Statistics: {
    url: `${ServicePrefix}/${MODULAR}/statistics`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  CurrentDraft: {
    url: `${ServicePrefix}/${MODULAR}/currentDraft`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

/** 按产品标识查询所有版本(按创建时间倒序)。 */
export const listByProduct = (productIdentification: string) =>
  defHttp.request<ProductVersionResultVO[]>({
    ...Api.ListByProduct,
    params: { productIdentification },
  });

/** 单版本详情(含完整 productSnapshotJson)。 */
export const detail = (productIdentification: string, versionNo: string) =>
  defHttp.request<ProductVersionResultVO>({
    ...Api.Detail,
    params: { productIdentification, versionNo },
  });

/** 发布新版本。 */
export const publishVersion = (data: ProductVersionPublishVO) =>
  defHttp.request<ProductVersionResultVO>({ ...Api.Publish, data });

/** 回滚到历史版本。 */
export const rollbackVersion = (data: ProductVersionRollbackVO) =>
  defHttp.request<ProductVersionResultVO>({ ...Api.Rollback, data });

/** 历史清理。 */
export const purgeHistoryVersion = (data: ProductVersionPurgeVO) =>
  defHttp.request<ProductVersionResultVO>({ ...Api.PurgeHistory, data });

/** 计算两版本字段级 diff。 */
export const diffVersion = (
  productIdentification: string,
  targetVersion: string,
  sourceVersion?: string,
) =>
  defHttp.request<ProductVersionDiffVO>({
    ...Api.Diff,
    params: { productIdentification, targetVersion, sourceVersion },
  });

/** 总览页 5 个统计指标。 */
export const versionStatistics = () =>
  defHttp.request<ProductVersionStatisticsResultVO>({ ...Api.Statistics });

/** 取产品当前草稿版本(无草稿返回 null)。 */
export const currentDraft = (productIdentification: string) =>
  defHttp.request<ProductVersionResultVO | null>({
    ...Api.CurrentDraft,
    params: { productIdentification },
  });
