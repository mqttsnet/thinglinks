export interface VideoStreamProxyPageQuery {
  appId?: string; // 应用ID
  proxyType?: string; // 代理类型
  proxyName?: string; // 代理名称
  streamIdentification?: string; // 流唯一标识
  url?: string; // 拉流地址
  srcUrl?: string; // 源地址
  dstUrl?: string; // 目标地址
  timeoutMs?: number; // 超时时间（毫秒）
  ffmpegCmdKey?: string; // FFmpeg模板KEY
  rtpType?: string; // RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
  gbIdentification?: string; // 国标唯一标识
  mediaIdentification?: string; // 媒体唯一标识
  enableAudio?: boolean; // 是否启用音频
  enableMp4?: boolean; // 是否启用MP4
  status?: boolean; // 状态
  enableRemoveNoneReader?: boolean; // 无人观看时是否删除
  streamKey?: string; // 拉流代理时ZLM返回的KEY，用于停止拉流代理
  enableDisableNoneReader?: boolean; // 无人观看时是否自动停用
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  createdTime?: string; // 创建时间
}

export interface VideoStreamProxySaveVO {
  appId?: string; // 应用ID
  proxyType?: string; // 代理类型
  proxyName?: string; // 代理名称
  streamIdentification?: string; // 流唯一标识
  url?: string; // 拉流地址
  srcUrl?: string; // 源地址
  dstUrl?: string; // 目标地址
  timeoutMs?: number; // 超时时间（毫秒）
  ffmpegCmdKey?: string; // FFmpeg模板KEY
  rtpType?: string; // RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
  gbIdentification?: string; // 国标唯一标识
  mediaIdentification?: string; // 媒体唯一标识
  enableAudio?: boolean; // 是否启用音频
  enableMp4?: boolean; // 是否启用MP4
  status?: boolean; // 状态
  enableRemoveNoneReader?: boolean; // 无人观看时是否删除
  streamKey?: string; // 拉流代理时ZLM返回的KEY，用于停止拉流代理
  enableDisableNoneReader?: boolean; // 无人观看时是否自动停用
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  maxRetryCount?: number; // 最大重试次数
}

export interface VideoStreamProxyUpdateVO {
  id: string;
  appId?: string; // 应用ID
  proxyType?: string; // 代理类型
  proxyName?: string; // 代理名称
  streamIdentification?: string; // 流唯一标识
  url?: string; // 拉流地址
  srcUrl?: string; // 源地址
  dstUrl?: string; // 目标地址
  timeoutMs?: number; // 超时时间（毫秒）
  ffmpegCmdKey?: string; // FFmpeg模板KEY
  rtpType?: string; // RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
  gbIdentification?: string; // 国标唯一标识
  mediaIdentification?: string; // 媒体唯一标识
  enableAudio?: boolean; // 是否启用音频
  enableMp4?: boolean; // 是否启用MP4
  status?: boolean; // 状态
  enableRemoveNoneReader?: boolean; // 无人观看时是否删除
  streamKey?: string; // 拉流代理时ZLM返回的KEY，用于停止拉流代理
  enableDisableNoneReader?: boolean; // 无人观看时是否自动停用
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  maxRetryCount?: number; // 最大重试次数
}

export interface VideoStreamProxyResultVO {
  echoMap?: any;
  id?: string; // 唯一标识符
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  proxyType?: string; // 代理类型
  proxyName?: string; // 代理名称
  streamIdentification?: string; // 流唯一标识
  url?: string; // 拉流地址
  srcUrl?: string; // 源地址
  dstUrl?: string; // 目标地址
  timeoutMs?: number; // 超时时间（毫秒）
  ffmpegCmdKey?: string; // FFmpeg模板KEY
  rtpType?: string; // RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
  gbIdentification?: string; // 国标唯一标识
  mediaIdentification?: string; // 媒体唯一标识
  enableAudio?: boolean; // 是否启用音频
  enableMp4?: boolean; // 是否启用MP4
  status?: boolean; // 状态
  enableRemoveNoneReader?: boolean; // 无人观看时是否删除
  streamKey?: string; // 拉流代理时ZLM返回的KEY，用于停止拉流代理
  enableDisableNoneReader?: boolean; // 无人观看时是否自动停用
  extendParams?: string; // 扩展参数
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  pullRetryCount?: number; // 拉流重试次数
  maxRetryCount?: number; // 最大重试次数
  lastPullTime?: string; // 最近拉流时间
  lastError?: string; // 最近错误信息
  zlmMediaServerStreamInfoList?: ZlmStreamInfoVO[]; // ZLM流媒体信息集合
}

export interface ZlmStreamInfoVO {
  hls?: string; // HLS播放地址
  httpsHls?: string; // HTTPS HLS播放地址
  flv?: string; // HTTP-FLV播放地址
  httpsFlv?: string; // HTTPS HTTP-FLV播放地址
  wsFlv?: string; // WebSocket FLV播放地址
  wssFlv?: string; // WebSocket Secure FLV播放地址
  rtsp?: string; // RTSP播放地址
  rtmp?: string; // RTMP播放地址
  mediaServer?: any; // 媒体服务器信息
}

export interface PlayUrlResultVO {
  zlmMediaServerStreamInfoList?: ZlmStreamInfoVO[];
}
