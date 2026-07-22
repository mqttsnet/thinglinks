/**
 * 设备上行前置转换脚本 ── 在线调试 API
 *
 * <p>POST 到 mqs:{@code /mqs/transform/debug}。携带脚本内容 + 调试设备标识 + 源 topic/原始报文,
 * 后端用该设备/产品的真实缓存快照注入脚本并执行,返回执行结果与绑定变量快照,供前端"变量检视器"展示。
 *
 * @author mqttsnet
 */
import { defHttp } from '/@/utils/http/axios';
import { RequestEnum } from '/@/enums/httpEnum';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import type { AxiosRequestConfig } from 'axios';

const ServicePrefix = ServicePrefixEnum.MQS;
const MODULAR = 'transform';

/**
 * 脚本执行结果。
 */
export interface TransformDebugResult {
  executionStatus: 'SUCCESS' | 'FAILED';
  context: any;
  exception?: string;
  errorMessage?: string;
  /** 脚本执行日志(脚本里 log.info 等的输出);可选 */
  logs?: string[];
}

/**
 * 实际注入脚本的绑定变量快照 ── 检视器渲染的数据源。
 *
 * <p>device / product 为后端缓存命中的基础信息(无 password);未命中时为 null。
 * 字段集合可能随后端扩展,前端检视器以"遍历对象"方式渲染,新增字段无需改 UI。
 */
export interface TransformDebugBinding {
  originTopic: string;
  originBody: string;
  clientId: string;
  deviceIdentification: string;
  productIdentification: string;
  device: Record<string, any> | null;
  product: Record<string, any> | null;
  /** 脚本 extend_params 解析出的自定义变量(config 绑定);空 / 非法为空对象 */
  config: Record<string, any> | null;
  /** 按版本解析的物模型(productModel 绑定):services[].properties[];版本未解析为 null */
  productModel: Record<string, any> | null;
}

/**
 * 调试响应 data。
 */
export interface TransformDebugResponse {
  result: TransformDebugResult;
  binding: TransformDebugBinding;
  /** 设备是否命中缓存(false 时 device 为 null,需提示用户) */
  deviceResolved: boolean;
  /** 产品是否命中缓存(false 时 product 为 null) */
  productResolved: boolean;
}

/**
 * 调试请求体。
 */
export interface TransformDebugParams {
  scriptContent: string;
  deviceIdentification: string;
  originTopic: string;
  originBody: string;
  /** 扩展参数 JSON(注入脚本 config);调试时可填该脚本的 extend_params 一并试跑 */
  extendParams?: string;
  /** 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)── 调试也计入执行统计,可选 */
  scriptUniqueKey?: string;
  /** 调试使用的产品版本号 ── 据此解析物模型注入脚本(可调当前 / 下个版本);可选 */
  objectVersion?: string;
}

const Api = {
  Debug: {
    url: `${ServicePrefix}/${MODULAR}/debug`,
    method: RequestEnum.POST,
  } as AxiosRequestConfig,
};

/**
 * 在线试跑设备上行前置转换脚本。
 *
 * @param params 脚本内容 + 调试设备标识 + 源 topic / 原始报文
 */
export const debugTransform = (params: TransformDebugParams) =>
  defHttp.request<TransformDebugResponse>({ ...Api.Debug, data: params });
