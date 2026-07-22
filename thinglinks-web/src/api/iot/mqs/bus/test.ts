/**
 * 协议总线测试 API ── 手动触发 dispatcher,用于联调 / 排错
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const ServicePrefix = ServicePrefixEnum.MQS;
const MODULAR = 'bus/test';

export interface DispatchOutcome {
  status: string;
  totalLatencyMs: number;
  failureReason?: string | null;
  stages?: Array<{
    stageName: string;
    phase: string;
    sequence: number;
    status: string;
    latencyMs: number;
    errorMsg?: string;
    skipReason?: string;
  }>;
}

const Api = {
  Dispatch: { url: `${ServicePrefix}/${MODULAR}/dispatch`, method: RequestEnum.POST } as AxiosRequestConfig,
  DispatchMqttPublish: {
    url: `${ServicePrefix}/${MODULAR}/dispatch/mqtt-publish`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

/**
 * 手动触发任意 topic dispatcher。
 *
 * @param sourceTopic 来源 topic
 * @param rawJson 原始报文 JSON
 */
export const dispatchManual = (sourceTopic: string, rawJson: string) =>
  defHttp.request<DispatchOutcome>({
    ...Api.Dispatch,
    params: { sourceTopic },
    data: rawJson,
    headers: { 'Content-Type': 'application/json' },
  });

/**
 * MQTT PUBLISH 测试 ── 自动用主数据 topic。
 *
 * @param rawJson 原始报文 JSON
 */
export const dispatchMqttPublish = (rawJson: string) =>
  defHttp.request<DispatchOutcome>({
    ...Api.DispatchMqttPublish,
    data: rawJson,
    headers: { 'Content-Type': 'application/json' },
  });
