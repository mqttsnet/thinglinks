import { VideoPlatformChannelBindParams, VideoPlatformChannelResultVO, VideoPlatformChannelPageQuery } from './model/channelModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'platformChannel';
const ServicePrefix = ServicePrefixEnum.VIDEO;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  List: {
    url: `${ServicePrefix}/${MODULAR}/list`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
  Bind: {
    url: `${ServicePrefix}/${MODULAR}/bind`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  Unbind: {
    url: `${ServicePrefix}/${MODULAR}/unbind`,
    method: RequestEnum.DELETE,
  } as AxiosRequestConfig,
};

export const page = (params: PageParams<VideoPlatformChannelPageQuery>) =>
  defHttp.request<PageResult<VideoPlatformChannelResultVO>>({ ...Api.Page, params });

export const listByPlatform = (platformId: string) =>
  defHttp.request<VideoPlatformChannelResultVO[]>({ ...Api.List, params: { platformId } });

export const bind = (params: VideoPlatformChannelBindParams) =>
  defHttp.request<boolean>({ ...Api.Bind, params });

// 注意: 后端仅支持单通道解绑 (@RequestParam deviceChannelId)，前端暂按串行调用
export const unbind = async (platformId: string, channelIds: string[]) => {
  for (const id of channelIds) {
    await defHttp.request<boolean>({
      ...Api.Unbind,
      url: `${Api.Unbind.url}?platformId=${encodeURIComponent(platformId)}&deviceChannelId=${encodeURIComponent(id)}`,
    });
  }
  return true;
};
