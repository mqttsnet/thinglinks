import { DeviceCommandPageQuery, DeviceCommandWrapper } from './model/deviceCommandModel';
import { PageParams, PageResult } from '/@/api/model/baseModel';
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum, ContentTypeEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'deviceCommand';
// tips: 建议在ServicePrefixEnum中新增：LINK = '/link'，并将下方代码改为： const ServicePrefix = ServicePrefixEnum.LINK;
// tips: /link 需要与 thinglinks-gateway-server.yml中配置的Path一致，否则无法正常调用接口！！！
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  Page: {
    url: `${ServicePrefix}/${MODULAR}/page`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  IssueCommands: {
    url: `${ServicePrefix}/${MODULAR}/issueCommands`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
  DebugHistory: {
    url: `${ServicePrefix}/${MODULAR}/debugHistory`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};
export const page = (params: PageParams<DeviceCommandPageQuery>) =>
  defHttp.request<PageResult<DeviceCommandPageQuery>>({ ...Api.Page, params });
export const issueCommands = (params: DeviceCommandWrapper) =>
  defHttp.request<any>({ ...Api.IssueCommands, params });
/** 调试台下发记录:命令下发(0)/响应(1),设备可空=当前租户全部,倒序近 N 条 */
export const debugHistory = (params: { deviceIdentification?: string; limit?: number }) =>
  defHttp.request<any[]>({ ...Api.DebugHistory, params });
