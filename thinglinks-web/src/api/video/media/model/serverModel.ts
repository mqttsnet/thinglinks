export interface VideoMediaServerPageQuery {
  appId?: string; // 应用ID
  host?: string; // 服务器地址(IP/域名)
  hookHost?: string; // Hook回调地址(IP/域名)
  sdpHost?: string; // SDP地址(IP/域名)
  streamHost?: string; // 流播放地址(IP/域名)
  httpPort?: number; // HTTP端口
  httpSslPort?: number; // HTTPS端口
  rtmpPort?: number; // RTMP端口
  rtmpSslPort?: number; // RTMP SSL端口
  rtpProxyPort?: number; // RTP代理端口（单端口模式）
  rtspPort?: number; // RTSP端口
  rtspSslPort?: number; // RTSP SSL端口
  flvPort?: number; // FLV端口
  flvSslPort?: number; // FLV SSL端口
  wsFlvPort?: number; // WebSocket FLV端口
  wsFlvSslPort?: number; // WebSocket FLV SSL端口
  autoConfig?: boolean; // 是否开启自动配置ZLM
  secret?: string; // ZLM鉴权参数
  type?: string; // 类型（zlm/abl）
  rtpEnable?: boolean; // 是否启用多端口模式
  rtpPortRange?: string; // 多端口RTP收流端口范围
  sendRtpPortRange?: string; // RTP发流端口范围
  recordAssistPort?: number; // 录制辅助服务端口
  defaultServer?: boolean; // 是否是默认ZLM服务器
  hookAliveInterval?: number; // keepalive hook触发间隔，单位秒
  recordPath?: string; // 录像存储路径
  recordDay?: number; // 录像存储时长（天）
  transcodeSuffix?: string; // 转码的前缀
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  extendParams?: string; // 扩展参数
  name?: string; // 名称
  mediaIdentification?: string; // 媒体识别码
  version?: string; // 服务器版本号
  maxStreams?: number; // 最大承载流数量
  createdTime?: string; // 创建时间
  id?: string; // 数据ID
}

export interface VideoMediaServerSaveVO {
  appId?: string; // 应用ID
  mediaIdentification?: string; // 媒体唯一标识
  host?: string; // 服务器地址(IP/域名)
  hookHost?: string; // Hook回调地址(IP/域名)
  sdpHost?: string; // SDP地址(IP/域名)
  streamHost?: string; // 流播放地址(IP/域名)
  httpPort?: number; // HTTP端口
  httpSslPort?: number; // HTTPS端口
  rtmpPort?: number; // RTMP端口
  rtmpSslPort?: number; // RTMP SSL端口
  rtpProxyPort?: number; // RTP代理端口（单端口模式）
  rtspPort?: number; // RTSP端口
  rtspSslPort?: number; // RTSP SSL端口
  flvPort?: number; // FLV端口
  flvSslPort?: number; // FLV SSL端口
  wsFlvPort?: number; // WebSocket FLV端口
  wsFlvSslPort?: number; // WebSocket FLV SSL端口
  autoConfig?: boolean; // 是否开启自动配置ZLM
  secret?: string; // ZLM鉴权参数
  type?: string; // 类型（zlm/abl）
  rtpEnable?: boolean; // 是否启用多端口模式
  rtpPortRange?: string; // 多端口RTP收流端口范围
  sendRtpPortRange?: string; // RTP发流端口范围
  recordAssistPort?: number; // 录制辅助服务端口
  defaultServer?: boolean; // 是否是默认ZLM服务器
  hookAliveInterval?: number; // keepalive hook触发间隔，单位秒
  recordPath?: string; // 录像存储路径
  recordDay?: number; // 录像存储时长（天）
  transcodeSuffix?: string; // 转码的前缀
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  onlineStatus?: boolean; // 在线状态
  extendParams?: string; // 扩展参数
  name?: string; // 名称
  version?: string; // 服务器版本号
  capabilities?: string; // 服务器能力集(JSON)
  maxStreams?: number; // 最大承载流数量
}

export interface VideoMediaServerUpdateVO {
  id: string;
  appId?: string; // 应用ID
  host?: string; // 服务器地址(IP/域名)
  hookHost?: string; // Hook回调地址(IP/域名)
  sdpHost?: string; // SDP地址(IP/域名)
  streamHost?: string; // 流播放地址(IP/域名)
  httpPort?: number; // HTTP端口
  httpSslPort?: number; // HTTPS端口
  rtmpPort?: number; // RTMP端口
  rtmpSslPort?: number; // RTMP SSL端口
  rtpProxyPort?: number; // RTP代理端口（单端口模式）
  rtspPort?: number; // RTSP端口
  rtspSslPort?: number; // RTSP SSL端口
  flvPort?: number; // FLV端口
  flvSslPort?: number; // FLV SSL端口
  wsFlvPort?: number; // WebSocket FLV端口
  wsFlvSslPort?: number; // WebSocket FLV SSL端口
  autoConfig?: boolean; // 是否开启自动配置ZLM
  secret?: string; // ZLM鉴权参数
  type?: string; // 类型（zlm/abl）
  rtpEnable?: boolean; // 是否启用多端口模式
  rtpPortRange?: string; // 多端口RTP收流端口范围
  sendRtpPortRange?: string; // RTP发流端口范围
  recordAssistPort?: number; // 录制辅助服务端口
  defaultServer?: boolean; // 是否是默认ZLM服务器
  hookAliveInterval?: number; // keepalive hook触发间隔，单位秒
  recordPath?: string; // 录像存储路径
  recordDay?: number; // 录像存储时长（天）
  transcodeSuffix?: string; // 转码的前缀
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  onlineStatus?: boolean; // 在线状态
  extendParams?: string; // 扩展参数
  name?: string; // 名称
  mediaIdentification?: string; // 媒体标识
  version?: string; // 服务器版本号
  capabilities?: string; // 服务器能力集(JSON)
  maxStreams?: number; // 最大承载流数量
}

export interface VideoMediaServerResultVO {
  echoMap?: any;
  id?: string; // 唯一标识符
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  host?: string; // 服务器地址(IP/域名)
  hookHost?: string; // Hook回调地址(IP/域名)
  sdpHost?: string; // SDP地址(IP/域名)
  streamHost?: string; // 流播放地址(IP/域名)
  httpPort?: number; // HTTP端口
  httpSslPort?: number; // HTTPS端口
  rtmpPort?: number; // RTMP端口
  rtmpSslPort?: number; // RTMP SSL端口
  rtpProxyPort?: number; // RTP代理端口（单端口模式）
  rtspPort?: number; // RTSP端口
  rtspSslPort?: number; // RTSP SSL端口
  flvPort?: number; // FLV端口
  flvSslPort?: number; // FLV SSL端口
  wsFlvPort?: number; // WebSocket FLV端口
  wsFlvSslPort?: number; // WebSocket FLV SSL端口
  autoConfig?: boolean; // 是否开启自动配置ZLM
  secret?: string; // ZLM鉴权参数
  type?: string; // 类型（zlm/abl）
  rtpEnable?: boolean; // 是否启用多端口模式
  rtpPortRange?: string; // 多端口RTP收流端口范围
  sendRtpPortRange?: string; // RTP发流端口范围
  recordAssistPort?: number; // 录制辅助服务端口
  defaultServer?: boolean; // 是否是默认ZLM服务器
  hookAliveInterval?: number; // keepalive hook触发间隔，单位秒
  recordPath?: string; // 录像存储路径
  recordDay?: number; // 录像存储时长（天）
  transcodeSuffix?: string; // 转码的前缀
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  extendParams?: string; // 扩展参数
  name?: string; // 名称
  version?: string; // 服务器版本号
  capabilities?: string; // 服务器能力集(JSON)
  maxStreams?: number; // 最大承载流数量
  currentStreams?: number; // 当前流数量
  cpuUsage?: number; // CPU使用率
  memoryUsage?: number; // 内存使用率
  networkInSpeed?: number; // 入网速率bytes/s
  networkOutSpeed?: number; // 出网速率bytes/s
  mediaIdentification?: string; // 媒体唯一标识
  onlineStatus?: boolean; // 在线状态
  lastAliveTime?: string; // 上次心跳时间
}

export interface VideoMediaServerMetricsResultVO {
  cpuUsage?: number; // CPU使用率百分比
  memoryUsage?: number; // 内存使用率百分比
  currentStreams?: number; // 当前活跃流数量
  networkInSpeed?: number; // 入网速率bytes/s
  networkOutSpeed?: number; // 出网速率bytes/s
}
