export interface RuleGroovyScriptPageQuery {
  appId?: string; // 应用ID
  scriptType?: string; // 脚本类型
  channelCode?: string; // 渠道编码
  productIdentification?: string; // 产品标识
  topicPattern?: string; // 主题模式
  enable?: boolean; // 是否启用
  scriptContent?: string; // 脚本内容
  extendParams?: string; // 扩展信息
  objectVersion?: string; // 版本号
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface RuleGroovyScriptSaveVO {
  appId?: string; // 应用ID
  scriptType?: string; // 脚本类型
  channelCode?: string; // 渠道编码
  productIdentification?: string; // 产品标识
  topicPattern?: string; // 主题模式
  enable?: boolean; // 是否启用
  scriptContent?: string; // 脚本内容
  extendParams?: string; // 扩展信息
  objectVersion?: string; // 版本号
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface RuleGroovyScriptUpdateVO {
  id: string;
  appId?: string; // 应用ID
  scriptType?: string; // 脚本类型
  channelCode?: string; // 渠道编码
  productIdentification?: string; // 产品标识
  topicPattern?: string; // 主题模式
  enable?: boolean; // 是否启用
  scriptContent?: string; // 脚本内容
  extendParams?: string; // 扩展信息
  objectVersion?: string; // 版本号
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}

export interface RuleGroovyScriptResultVO {
  echoMap?: any;
  id?: string; // 主键
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  scriptType?: string; // 脚本类型
  channelCode?: string; // 渠道编码
  productIdentification?: string; // 产品标识
  topicPattern?: string; // 主题模式
  enable?: boolean; // 是否启用
  scriptContent?: string; // 脚本内容
  extendParams?: string; // 扩展信息
  objectVersion?: string; // 版本号
  remark?: string; // 备注
  createdOrgId?: string; // 创建人组织
}
