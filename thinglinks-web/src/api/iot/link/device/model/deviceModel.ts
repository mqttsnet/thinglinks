import { any } from 'vue-types';
import { Ref } from 'vue';

export interface DevicePageQuery {
  clientId?: string; // 客户端标识
  userName?: string; // 用户名
  password?: string; // 密码
  appId?: string; // 应用ID
  authMode?: number; // 认证方式0-无认证，1-用户名密码，2-ssl证书
  encryptKey?: string; // 加密密钥
  encryptVector?: string; // 加密向量
  signKey?: string; // 签名密钥
  encryptMethod?: number; // 传输协议的加密方式：0-明文传输、1-SM4、2-AES
  deviceIdentification?: string; // 设备标识
  deviceName?: string; // 设备名称
  connector?: string; // 连接实例
  description?: string; // 设备描述
  deviceStatus?: number; // 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
  connectStatus?: number; // 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
  deviceTags?: string; // 设备标签
  productId?: string; // 产品id
  productIdentification?: string; // 产品标识
  swVersion?: string; // 软件版本
  fwVersion?: string; // 固件版本
  deviceSdkVersion?: string; // sdk版本
  gatewayId?: string; // 网关设备id
  nodeType?: number; // 设备类型0普通设备 1网关设备- 2子设备
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface DeviceSaveVO {
  appId: string; // 应用ID
  deviceName: string; // 设备名称
  authMode: number; // 认证方式0-无认证，1-用户名密码，2-ssl证书
  userName?: string; // 用户名
  password?: string; // 密码
  deviceStatus: number; // 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
  encryptMethod: number; // 传输协议的加密方式：0-明文传输、1-SM4、2-AES
  encryptKey: string; // 加密密钥
  encryptVector: string; // 加密向量
  signKey?: string; // 签名密钥
  connector: string; // 连接实例
  nodeType: number; // 设备类型普通设备 1网关设备- 2子设备
  gatewayId?: number; // 网关设备id
  fwVersion: string; // 固件版本
  swVersion: string; // 软件版本
  productId: string; // 产品id
  productIdentification?: string; // 产品标识
  deviceSdkVersion: string; // sdk版本
  deviceTags?: string; // 设备标签
  description?: string; // 设备描述
  remark?: string; // 备注
  // connectStatus: number; // 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
  // deviceIdentification?: string; // 设备标识
  // clientId?: string; // 客户端标识
  // createdOrgId?: string; // 创建人组织
}

export interface DeviceUpdateVO {
  id: string;
  clientId?: string; // 客户端标识
  userName?: string; // 用户名
  password?: string; // 密码
  appId?: string; // 应用ID
  authMode?: number; // 认证方式0-无认证，1-用户名密码，2-ssl证书
  encryptKey?: string; // 加密密钥
  encryptVector?: string; // 加密向量
  signKey?: string; // 签名密钥
  encryptMethod?: number; // 传输协议的加密方式：0-明文传输、1-SM4、2-AES
  deviceIdentification?: string; // 设备标识
  deviceName?: string; // 设备名称
  connector?: string; // 连接实例
  description?: string; // 设备描述
  deviceStatus?: number; // 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
  connectStatus?: number; // 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
  deviceTags?: string; // 设备标签
  productId?: string; // 产品id
  productIdentification?: string; // 产品标识
  swVersion?: string; // 软件版本
  fwVersion?: string; // 固件版本
  deviceSdkVersion?: string; // sdk版本
  gatewayId?: string; // 网关设备id
  nodeType?: number; // 设备类型 0普通设备 1网关设备- 2子设备
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface DeviceResultVO {
  echoMap?: any;
  id?: string; // id
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  clientId?: string; // 客户端标识
  userName?: string; // 用户名
  password?: string; // 密码
  appId?: string; // 应用ID
  authMode?: string; // 认证方式0-无认证，1-用户名密码，2-ssl证书
  encryptKey?: string; // 加密密钥
  encryptVector?: string; // 加密向量
  signKey?: string; // 签名密钥
  encryptMethod?: string; // 传输协议的加密方式：0-明文传输、1-SM4、2-AES
  deviceIdentification?: string; // 设备标识
  deviceName?: string; // 设备名称
  connector?: string; // 连接实例
  description?: string; // 设备描述
  deviceStatus?: string; // 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
  connectStatus?: string; // 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
  deviceTags?: string; // 设备标签
  productId?: string; // 产品id
  productIdentification?: string; // 产品标识
  boundProductVersionNo?: string; // 绑定的产品版本序号(系统在注册/发布灰度时写入,数据上报按此快照解析物模型)
  swVersion?: string; // 软件版本
  fwVersion?: string; // 固件版本
  deviceSdkVersion?: string; // sdk版本
  gatewayId?: string; // 网关设备id
  nodeType?: string; // 设备类型 0普通设备 1网关设备- 2子设备
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
  deviceLocationResultVO: any; //位置信息
  area: any; //省市县
  address: string; // 地址
  map: any; //经纬度
  protocolType: string; // 协议类型
  productResultVO: any; //相关产品信息
  productName: string; //产品名称
  gatewayName: string; //网关设备名称
  subDeviceResultVOList: any; // 设备集合
}

export interface DeviceStatusVO {
  status: string;
}

export interface UploadFormData {
  formData?: Ref<FormData | null>;
}
