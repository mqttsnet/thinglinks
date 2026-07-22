import {
  ProductPublishRecordPageQuery,
  ProductPublishRecordResultVO,
} from './model/productPublishRecordModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'productPublishRecord';
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

/** 分页查询发布记录。 */
export const page = (params: PageParams<ProductPublishRecordPageQuery>) =>
  defHttp.request<PageResult<ProductPublishRecordResultVO>>({ ...Api.Page, data: params });
