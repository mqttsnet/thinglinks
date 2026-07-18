export interface AlarmPageQuery {
  appId?: string; // 应用ID
  alarmChannelIds?: any; // 告警渠道ID集合
  alarmIdentification?: string; // 告警编码
  alarmIdentificationList?: any; // 告警编码集合
  alarmName?: string; //	告警名称
  alarmScene?: string; // 告警场景
  level?: number; // 应用ID
  id?: number; // 主键ID
  status?: number; // 告警级别
  remark?: string; // 启用状态
  createdOrgId?: number; // 创建人组织
}

export interface AlarmSaveVO {
  appId?: string; // 应用ID
  alarmChannelIds?: any; // 告警渠道ID集合
  alarmIdentification?: string; // 告警编码
  alarmName?: string; //	告警名称
  alarmScene?: string; // 告警场景
  level?: number; // 应用ID
  id?: string;
  status?: number; // 告警级别
  remark?: string; // 启用状态
  createdOrgId?: number; // 创建人组织
}

export interface AlarmUpdateVO {
  appId?: string; // 应用ID
  alarmChannelIds?: any; // 告警渠道ID集合
  alarmIdentification?: string; // 告警编码
  alarmName?: string; //	告警名称
  alarmScene?: string; // 告警场景
  level?: number; // 应用ID
  id?: number; // 主键ID
  status?: number; // 告警级别
  remark?: string; // 启用状态
  createdOrgId?: number; // 创建人组织
}

export interface AlarmResultVO {
  echoMap?: any;
  id?: string; // id
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  createdOrgId?: number; // 创建人组织
  appId?: string; // 应用ID
  alarmName?: string; // 告警名称
  alarmIdentification?: string; // 告警编码
  level?: number; // 告警级别
  alarmScene?: string; // 告警场景
  alarmChannelIds?: any; // 告警渠道ID集合
  alarmChannelResultVOS?: any; // 告警渠道详情
  ruleAlarmChannelDetailsResultVOList?: any[]; // 告警渠道详情
  remark?: string; // 规则描述
  status?: number; // 状态
}
