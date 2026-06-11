export interface OtaUpgradesPageQuery {
  appId?: string; // 应用ID
  packageName?: string; // 包名称
  packageType?: number; // 升级包类型(0:软件包、1:固件包)
  productIdentification?: string; // 产品标识
  version?: string; // 升级包版本号
  fileLocation?: string; // 升级包的位置
  status?: number; // 状态
  description?: string; // 升级包功能描述
  customInfo?: string; // 自定义信息
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradesSaveVO {
  appId?: string; // 应用ID
  packageName?: string; // 包名称
  packageType?: number; // 升级包类型(0:软件包、1:固件包)
  productIdentification?: string; // 产品标识
  version?: string; // 升级包版本号
  fileLocation?: string; // 升级包的位置
  status?: number; // 状态
  description?: string; // 升级包功能描述
  customInfo?: string; // 自定义信息
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradesUpdateVO {
  id: string;
  appId?: string; // 应用ID
  packageName?: string; // 包名称
  packageType?: number; // 升级包类型(0:软件包、1:固件包)
  productIdentification?: string; // 产品标识
  version?: string; // 升级包版本号
  fileLocation?: string; // 升级包的位置
  status?: number; // 状态
  description?: string; // 升级包功能描述
  customInfo?: string; // 自定义信息
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradesResultVO {
  echoMap?: any;
  id?: string; // 主键
  createdBy?: string; // 创建人
  createdTime?: string; // 创建时间
  updatedBy?: string; // 更新人
  updatedTime?: string; // 更新时间
  appId?: string; // 应用ID
  packageName?: string; // 包名称
  packageType?: number; // 升级包类型(0:软件包、1:固件包)
  productIdentification?: string; // 产品标识
  version?: string; // 升级包版本号
  fileLocation?: string; // 升级包的位置
  status?: number; // 状态
  description?: string; // 升级包功能描述
  customInfo?: string; // 自定义信息
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}
