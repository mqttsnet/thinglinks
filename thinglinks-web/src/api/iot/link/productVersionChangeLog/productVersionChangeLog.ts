import {
  ProductVersionChangeLogPageQuery,
  ProductVersionChangeLogResultVO,
} from './model/productVersionChangeLogModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'productVersionChangeLog';
const ServicePrefix = ServicePrefixEnum.LINK;

/** REST 路由集中表。 */
export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  ListByProduct: {
    url: `${ServicePrefix}/${MODULAR}/listByProduct`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

/** 分页查询变更日志。 */
export const page = (params: PageParams<ProductVersionChangeLogPageQuery>) =>
  defHttp.request<PageResult<ProductVersionChangeLogResultVO>>({ ...Api.Page, data: params });

/** 按产品标识查询全部变更日志(created_time 倒序)。 */
export const listByProduct = (productIdentification: string) =>
  defHttp.request<ProductVersionChangeLogResultVO[]>({
    ...Api.ListByProduct,
    params: { productIdentification },
  });
