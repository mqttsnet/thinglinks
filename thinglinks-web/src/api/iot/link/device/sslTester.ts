import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';
import type { DeviceSslTestQuery, DeviceSslTestResultVO } from './model/sslTesterModel';

const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  SslTest: {
    url: `${ServicePrefix}/device/sslTest`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

/** SSL 证书认证测试器 ── 端到端模拟 PKI 链路,返回 6 步分步详情 */
export const sslTest = (params: DeviceSslTestQuery) =>
  defHttp.request<DeviceSslTestResultVO>({ ...Api.SslTest, params });
