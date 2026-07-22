import type { ZlmStreamInfoVO } from './proxyModel';

export interface VideoStreamPushPageQuery {
  appId?: string; // 应用ID
  streamIdentification?: string; // 流唯一标识
  totalReaderCount?: number; // 观看总人数
  originType?: number; // 产生源类型
  originUrl?: string; // 产生源的url
  vhost?: string; // 音视频轨道
  bytesSpeed?: number; // 数据产生速度，单位byte/s
  aliveSecond?: number; // 存活时间，单位秒
  mediaIdentification?: string; // 媒体唯一标识
  serverId?: string; // 使用的服务ID
  pushTime?: string; // 推流时间
  status?: boolean; // 状态
  pushIng?: boolean; // 是否正在推流
  self?: boolean; // 是否自己平台的推流
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface VideoStreamPushSaveVO {
  appId?: string; // 应用ID
  streamIdentification?: string; // 流唯一标识
  totalReaderCount?: number; // 观看总人数
  originType?: number; // 产生源类型
  originUrl?: string; // 产生源的url
  vhost?: string; // 音视频轨道
  bytesSpeed?: number; // 数据产生速度，单位byte/s
  aliveSecond?: number; // 存活时间，单位秒
  mediaIdentification?: string; // 媒体唯一标识
  serverId?: string; // 使用的服务ID
  pushTime?: string; // 推流时间
  status?: boolean; // 状态
  pushIng?: boolean; // 是否正在推流
  self?: boolean; // 是否自己平台的推流
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface VideoStreamPushUpdateVO {
  id: string;
  appId?: string; // 应用ID
  streamIdentification?: string; // 流唯一标识
  totalReaderCount?: number; // 观看总人数
  originType?: number; // 产生源类型
  originUrl?: string; // 产生源的url
  vhost?: string; // 音视频轨道
  bytesSpeed?: number; // 数据产生速度，单位byte/s
  aliveSecond?: number; // 存活时间，单位秒
  mediaIdentification?: string; // 媒体唯一标识
  serverId?: string; // 使用的服务ID
  pushTime?: string; // 推流时间
  status?: boolean; // 状态
  pushIng?: boolean; // 是否正在推流
  self?: boolean; // 是否自己平台的推流
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface VideoStreamPushResultVO {
  echoMap?: any;
  id?: string; // id
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  streamIdentification?: string; // 流唯一标识
  totalReaderCount?: number; // 观看总人数
  originType?: number; // 产生源类型
  originUrl?: string; // 产生源的url
  vhost?: string; // 音视频轨道
  bytesSpeed?: number; // 数据产生速度，单位byte/s
  aliveSecond?: number; // 存活时间，单位秒
  mediaIdentification?: string; // 媒体唯一标识
  serverId?: string; // 使用的服务ID
  pushTime?: string; // 推流时间
  status?: boolean; // 状态
  pushIng?: boolean; // 是否正在推流
  self?: boolean; // 是否自己平台的推流
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  zlmMediaServerStreamInfoList?: ZlmStreamInfoVO[]; // ZLM流媒体信息集合
  pushUrl?: string; // 推流入口 RTMP
  pushUrlRtsp?: string; // 推流入口 RTSP
}
