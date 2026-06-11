import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const MODULAR = 'deviceShadow';
const ServicePrefix = ServicePrefixEnum.LINK;

export const Api = {
  QueryDeviceShadow: {
    url: `${ServicePrefix}/${MODULAR}/queryDeviceShadow`,
    method: RequestEnum.GET,
  } as AxiosRequestConfig,
};

/**
 * 设备影子查询参数。
 *
 * <p>与后端 {@code DeviceShadowPageQuery} 对齐:</p>
 * <ul>
 *   <li>{@code deviceIdentification}: 必填,设备业务标识</li>
 *   <li>{@code serviceCode}: 可选,指定服务编码,空 = 查所有服务</li>
 *   <li>{@code versionNo}: 可选,产品版本序号 ── 空时默认按设备绑定版本;
 *       传值则按指定历史版本快照解析物模型 + 读对应 TD 子表(支持回看上一版本影子)</li>
 *   <li>{@code startTime / endTime}: 可选,纳秒时间戳;同时提供或同时为空,时间窗 ≤ 60 分钟</li>
 * </ul>
 */
export interface DeviceShadowQueryParam {
  deviceIdentification: string;
  serviceCode?: string;
  versionNo?: string;
  startTime?: number;
  endTime?: number;
}

/**
 * 查询设备影子(单次 GET,非长连)。
 *
 * <p>实时数据用 WebSocket;一次性 / 历史版本回看用本接口。</p>
 */
export const queryDeviceShadow = (params: DeviceShadowQueryParam) =>
  defHttp.request<any>({ ...Api.QueryDeviceShadow, params });
