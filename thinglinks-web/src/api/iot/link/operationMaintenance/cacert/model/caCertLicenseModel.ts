export interface CaCertLicensePageQuery {
  certName?: string; // 证书名称
  serialNumber?: string; // 证书序列号
  commonName?: string; // 通用名称
  organization?: string; // 组织名称
  organizationalUnit?: string; // 组织单位名称
  countryName?: string; // 国家
  provinceName?: string; // 省份/州
  localityName?: string; // 城市
  email?: string; // 邮箱
  licenseBase64?: string; // License文件内容(Base64编码)
  businessLicenseFileid?: string; // 营业执照文件ID
  authorizationCertFileid?: string; // 授权证书文件ID
  algorithm?: number; // 签名算法(0-RSA、1-EC)
  param1?: string; // RSA公钥n或ECC Point x
  param2?: string; // RSA公钥e或ECC Point y
  extendParams?: string; // 扩展信息
  notBefore?: string; // 证书颁发时间
  notAfter?: string; // 证书过期时间
  revokeTime?: string; // 证书撤销时间
  state?: number; // 证书状态(0-待完善、1-已颁发、2-已撤销)
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface CaCertLicenseSaveVO {
  certName?: string; // 证书名称
  serialNumber?: string; // 证书序列号
  commonName?: string; // 通用名称
  organization?: string; // 组织名称
  organizationalUnit?: string; // 组织单位名称
  countryName?: string; // 国家
  provinceName?: string; // 省份/州
  localityName?: string; // 城市
  email?: string; // 邮箱
  licenseBase64?: string; // License文件内容(Base64编码)
  businessLicenseFileid?: string; // 营业执照文件ID
  authorizationCertFileid?: string; // 授权证书文件ID
  algorithm?: number; // 签名算法(0-RSA、1-EC)
  param1?: string; // RSA公钥n或ECC Point x
  param2?: string; // RSA公钥e或ECC Point y
  extendParams?: string; // 扩展信息
  notBefore?: string; // 证书颁发时间
  notAfter?: string; // 证书过期时间
  revokeTime?: string; // 证书撤销时间
  state?: number; // 证书状态(0-待完善、1-已颁发、2-已撤销)
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface CaCertLicenseUpdateVO {
  id: string;
  certName?: string; // 证书名称
  serialNumber?: string; // 证书序列号
  commonName?: string; // 通用名称
  organization?: string; // 组织名称
  organizationalUnit?: string; // 组织单位名称
  countryName?: string; // 国家
  provinceName?: string; // 省份/州
  localityName?: string; // 城市
  email?: string; // 邮箱
  licenseBase64?: string; // License文件内容(Base64编码)
  businessLicenseFileid?: string; // 营业执照文件ID
  authorizationCertFileid?: string; // 授权证书文件ID
  algorithm?: number; // 签名算法(0-RSA、1-EC)
  param1?: string; // RSA公钥n或ECC Point x
  param2?: string; // RSA公钥e或ECC Point y
  extendParams?: string; // 扩展信息
  notBefore?: string; // 证书颁发时间
  notAfter?: string; // 证书过期时间
  revokeTime?: string; // 证书撤销时间
  state?: number; // 证书状态(0-待完善、1-已颁发、2-已撤销)
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface CaCertLicenseResultVO {
  echoMap?: any;
  id?: string; // id
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  certName?: string; // 证书名称
  serialNumber?: string; // 证书序列号
  commonName?: string; // 通用名称
  organization?: string; // 组织名称
  organizationalUnit?: string; // 组织单位名称
  countryName?: string; // 国家
  provinceName?: string; // 省份/州
  localityName?: string; // 城市
  email?: string; // 邮箱
  licenseBase64?: string; // License文件内容(Base64编码)
  businessLicenseFileid?: string; // 营业执照文件ID
  authorizationCertFileid?: string; // 授权证书文件ID
  algorithm?: number; // 签名算法(0-RSA、1-EC)
  param1?: string; // RSA公钥n或ECC Point x
  param2?: string; // RSA公钥e或ECC Point y
  extendParams?: string; // 扩展信息
  notBefore?: string; // 证书颁发时间
  notAfter?: string; // 证书过期时间
  revokeTime?: string; // 证书撤销时间
  state?: number; // 证书状态(0-待完善、1-已颁发、2-已撤销)
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

/** CA 证书影响面 ── 用于吊销前评估 */
export interface CaCertLicenseImpactResultVO {
  caId: number | string;
  caSerialNumber?: string;
  caName?: string;
  /** 绑定此 CA 的设备总数 */
  boundDeviceCount: number;
  /** 其中在线设备数 */
  onlineDeviceCount: number;
  /** 前 N 条设备简要 */
  topDevices?: Array<{
    id: number | string;
    deviceIdentification: string;
    deviceName?: string;
    productIdentification?: string;
    connectStatus?: number;
    lastHeartbeatTime?: string;
  }>;
}
