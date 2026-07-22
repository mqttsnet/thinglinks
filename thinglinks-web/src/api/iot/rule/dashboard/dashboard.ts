import { DetailsQuery, DashboardDetailsResultVo } from './model/dashboardModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum, ContentTypeEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'ruleDashboardStats';
const ServicePrefix = ServicePrefixEnum.RULE;

export const Api = {
  AssetSummary: {
    url: `${ServicePrefix}/${MODULAR}/assetSummary`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  BridgeSummary: {
    url: `${ServicePrefix}/${MODULAR}/bridgeSummary`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

export const ruleAssetSummary = () => defHttp.request({ ...Api.AssetSummary });

/** 北向集成 / 数据桥接统计(资产统计右栏看板用) */
export const bridgeSummary = () => defHttp.request<any>({ ...Api.BridgeSummary });
