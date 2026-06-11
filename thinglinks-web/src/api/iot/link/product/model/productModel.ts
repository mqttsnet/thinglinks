import { Ref } from 'vue';
export interface ProductPageQuery {
  appId?: string; // 应用ID
  templateId?: string; // 产品id
  productName?: string; // 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
  productIdentification?: string; // 产品标识
  productType?: number; // 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
  manufacturerId?: string; // 厂商ID:支持英文大小写，数字，下划线和中划线
  manufacturerName?: string; // 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
  model?: string; // 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
  dataFormat?: string; // 数据格式，默认为JSON无需修改。
  deviceType?: string; // 设备类型:支持英文大小写、数字、下划线和中划线
  protocolType?: string; // 设备接入平台的协议类型，默认为MQTT无需修改。
  productStatus?: number; // 状态(字典值：0启用  1停用)
  activeVersionNo?: string; // 产品版本
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
}

export interface ProductSaveVO {
  appId?: string; // 应用ID
  templateId?: string; // 产品id
  productName?: string; // 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
  productIdentification?: string; // 产品标识
  productType?: number; // 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
  manufacturerId?: string; // 厂商ID:支持英文大小写，数字，下划线和中划线
  manufacturerName?: string; // 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
  model?: string; // 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
  dataFormat?: string; // 数据格式，默认为JSON无需修改。
  deviceType?: string; // 设备类型:支持英文大小写、数字、下划线和中划线
  protocolType?: string; // 设备接入平台的协议类型，默认为MQTT无需修改。
  productStatus?: number; // 状态(字典值：0启用  1停用)
  activeVersionNo?: string; // 产品版本
  icon?: string; // 图标
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
}

export interface ProductUpdateVO {
  id: string;
  appId?: string; // 应用ID
  templateId?: string; // 产品id
  productName?: string; // 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
  productIdentification?: string; // 产品标识
  productType?: number; // 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
  manufacturerId?: string; // 厂商ID:支持英文大小写，数字，下划线和中划线
  manufacturerName?: string; // 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
  model?: string; // 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
  dataFormat?: string; // 数据格式，默认为JSON无需修改。
  deviceType?: string; // 设备类型:支持英文大小写、数字、下划线和中划线
  protocolType?: string; // 设备接入平台的协议类型，默认为MQTT无需修改。
  productStatus?: number; // 状态(字典值：0启用  1停用)
  activeVersionNo?: string; // 产品版本
  icon?: string; // 图标
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
}

export interface ProductResultVO {
  echoMap?: any;
  id?: string; // id
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  templateId?: string; // 产品id
  productName?: string; // 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
  productIdentification?: string; // 产品标识
  productType?: string; // 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
  manufacturerId?: string; // 厂商ID:支持英文大小写，数字，下划线和中划线
  manufacturerName?: string; // 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
  model?: string; // 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
  dataFormat?: string; // 数据格式，默认为JSON无需修改。
  deviceType?: string; // 设备类型:支持英文大小写、数字、下划线和中划线
  protocolType?: string; // 设备接入平台的协议类型，默认为MQTT无需修改。
  productStatus?: number; // 状态(字典值：0启用  1停用)
  activeVersionNo?: string; // 产品版本(已正式发布的版本号,DRAFT 不写入此字段)
  previousFullVersionNo?: string; // 灰度发布时,被切换前的全量版本号(供灰度路由 + 回滚使用)
  icon?: string; // 图标
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
}

export interface ProductQuickSaveVO {
  appId?: string; // 应用ID
  templateId?: string; // 产品id
  productName?: string; // 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
  productIdentification?: string; // 产品标识
  productType?: string; // 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
  manufacturerId?: string; // 厂商ID:支持英文大小写，数字，下划线和中划线
  manufacturerName?: string; // 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
  model?: string; // 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
  dataFormat?: string; // 数据格式，默认为JSON无需修改。
  deviceType?: string; // 设备类型:支持英文大小写、数字、下划线和中划线
  protocolType?: string; // 设备接入平台的协议类型，默认为MQTT无需修改。
  productStatus?: string; // 状态(字典值：0启用  1停用)
  activeVersionNo?: string; // 产品版本
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
  services?: any[]; // 服务
}
export interface UploadFormData  {
  formData?: Ref<FormData | null>;
}