export interface VideoPlatformPageQuery {
  name?: string; // 名称
  enable?: boolean; // 启用状态
  serverGbId?: string; // 上级平台国标编号
  deviceGbId?: string; // 本平台国标编号
  transport?: string; // 传输协议
  cascadeType?: number; // 级联类型
  gbVersion?: string; // 国标版本
  onlineStatus?: boolean; // 在线状态
}

export interface VideoPlatformSaveVO {
  name?: string; // 名称
  enable?: boolean; // 启用状态
  serverGbId?: string; // 上级平台国标编号
  serverGbDomain?: string; // 上级平台国标域
  serverIp?: string; // 上级平台地址
  serverPort?: number; // 上级平台端口
  deviceGbId?: string; // 本平台国标编号
  deviceIp?: string; // 本平台地址
  devicePort?: number; // 本平台端口
  username?: string; // 用户名
  password?: string; // 密码
  expires?: number; // 注册有效期(秒)
  keepTimeout?: number; // 心跳周期(秒)
  transport?: string; // 传输协议
  characterSet?: string; // 字符集
  ptz?: boolean; // 是否共享PTZ
  rtcp?: boolean; // 是否共享RTCP
  status?: boolean; // 状态
  catalogSubscribe?: boolean; // 目录订阅
  alarmSubscribe?: boolean; // 告警订阅
  mobilePositionSubscribe?: boolean; // 位置订阅
  catalogGroup?: number; // 目录分组
  asMessageChannel?: boolean; // 作为消息通道
  sendStreamIp?: string; // 推流IP
  autoPushChannel?: boolean; // 自动推送通道
  catalogWithPlatform?: number; // 关联平台目录
  catalogWithGroup?: number; // 关联分组目录
  catalogWithRegion?: number; // 关联区域目录
  civilCode?: string; // 行政区划
  manufacturer?: string; // 厂商
  model?: string; // 型号
  address?: string; // 地址
  registerWay?: number; // 注册方式
  secrecy?: number; // 保密属性
  serverId?: string; // 服务器ID
  cascadeType?: number; // 级联类型
  gbVersion?: string; // 国标版本
  registerExpires?: number; // 注册过期时间
  keepaliveInterval?: number; // 心跳间隔
  keepaliveTimeoutCount?: number; // 心跳超时次数
  startOfflinePush?: boolean; // 离线推送
  sipIp?: string; // SIP地址
  sipPort?: number; // SIP端口
  hookUrlPrefix?: string; // Hook地址前缀
  serviceInstanceId?: string; // 服务实例ID
  cascadeSdpIp?: string; // 级联SDP地址
  createdOrgId?: string; // 创建组织
}

export interface VideoPlatformUpdateVO {
  id: string;
  name?: string;
  enable?: boolean;
  serverGbId?: string;
  serverGbDomain?: string;
  serverIp?: string;
  serverPort?: number;
  deviceGbId?: string;
  deviceIp?: string;
  devicePort?: number;
  username?: string;
  password?: string;
  expires?: number;
  keepTimeout?: number;
  transport?: string;
  characterSet?: string;
  ptz?: boolean;
  rtcp?: boolean;
  status?: boolean;
  catalogSubscribe?: boolean;
  alarmSubscribe?: boolean;
  mobilePositionSubscribe?: boolean;
  catalogGroup?: number;
  asMessageChannel?: boolean;
  sendStreamIp?: string;
  autoPushChannel?: boolean;
  catalogWithPlatform?: number;
  catalogWithGroup?: number;
  catalogWithRegion?: number;
  civilCode?: string;
  manufacturer?: string;
  model?: string;
  address?: string;
  registerWay?: number;
  secrecy?: number;
  serverId?: string;
  cascadeType?: number;
  gbVersion?: string;
  registerExpires?: number;
  keepaliveInterval?: number;
  keepaliveTimeoutCount?: number;
  startOfflinePush?: boolean;
  sipIp?: string;
  sipPort?: number;
  hookUrlPrefix?: string;
  serviceInstanceId?: string;
  cascadeSdpIp?: string;
  createdOrgId?: string;
}

export interface VideoPlatformResultVO {
  echoMap?: any;
  id?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;
  name?: string; // 名称
  enable?: boolean; // 启用状态
  serverGbId?: string; // 上级平台国标编号
  serverGbDomain?: string; // 上级平台国标域
  serverIp?: string; // 上级平台地址
  serverPort?: number; // 上级平台端口
  deviceGbId?: string; // 本平台国标编号
  deviceIp?: string; // 本平台地址
  devicePort?: number; // 本平台端口
  username?: string; // 用户名
  password?: string; // 密码
  expires?: number; // 注册有效期(秒)
  keepTimeout?: number; // 心跳周期(秒)
  transport?: string; // 传输协议
  characterSet?: string; // 字符集
  ptz?: boolean; // 是否共享PTZ
  rtcp?: boolean; // 是否共享RTCP
  status?: boolean; // 状态
  catalogSubscribe?: boolean; // 目录订阅
  alarmSubscribe?: boolean; // 告警订阅
  mobilePositionSubscribe?: boolean; // 位置订阅
  catalogGroup?: number; // 目录分组
  asMessageChannel?: boolean; // 作为消息通道
  sendStreamIp?: string; // 推流IP
  autoPushChannel?: boolean; // 自动推送通道
  catalogWithPlatform?: number; // 关联平台目录
  catalogWithGroup?: number; // 关联分组目录
  catalogWithRegion?: number; // 关联区域目录
  civilCode?: string; // 行政区划
  manufacturer?: string; // 厂商
  model?: string; // 型号
  address?: string; // 地址
  registerWay?: number; // 注册方式
  secrecy?: number; // 保密属性
  serverId?: string; // 服务器ID
  cascadeType?: number; // 级联类型
  gbVersion?: string; // 国标版本
  onlineStatus?: boolean; // 在线状态
  registerExpires?: number; // 注册过期时间
  keepaliveInterval?: number; // 心跳间隔
  keepaliveTimeoutCount?: number; // 心跳超时次数
  lastRegisterTime?: string; // 最近注册时间
  lastKeepaliveTime?: string; // 最近心跳时间
  startOfflinePush?: boolean; // 离线推送
  sipIp?: string; // SIP地址
  sipPort?: number; // SIP端口
  hookUrlPrefix?: string; // Hook地址前缀
  serviceInstanceId?: string; // 服务实例ID
  cascadeSdpIp?: string; // 级联SDP地址
  createdOrgId?: string; // 创建组织
}
