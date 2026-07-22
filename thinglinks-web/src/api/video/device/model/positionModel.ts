export interface VideoDeviceMobilePositionResultVO {
  echoMap?: any;
  id?: string;
  deviceIdentification?: string; // 设备国标编号
  channelIdentification?: string; // 通道国标编号
  longitude?: number; // 经度
  latitude?: number; // 纬度
  altitude?: number; // 海拔(米)
  speed?: number; // 速度(km/h)
  direction?: number; // 方向(度)
  reportTime?: string; // 上报时间
  geoCoordSys?: string; // 地理坐标系
  createdTime?: string; // 创建时间
}
