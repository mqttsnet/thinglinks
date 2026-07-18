export interface AlarmRecordPageQuery {
  appId?: string; // 应用ID
  channelId?: string; // 渠道ID
  contentData?: string; // 告警具体内容信息
  alarmIdentification?: string; // 告警编码
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
  handledStatus?: string; // 处理状态
  handledTime?: string; // 	处理时间
  handlingNotes?: string; // 处理记录
  id?: string; // 主键
  occurredTime?: string; // 发生时间
  resolutionNotes?: string; // 	解决记录
  resolvedTime?: string; // 解决时间
}

export interface AlarmRecordSaveVO {
  id?: string;
  appId?: string; // 应用ID
  alarmName?: string; // 告警名称
  alarmIdentification?: string; // 告警编码
  alarmLevel?: string; // 告警级别
  alarmScene?: string; // 告警场景
  alarmChannel?: string; // 告警渠道
  remark?: string; // 规则描述
  status?: number; // 状态
}

export interface AlarmRecordUpdateVO {
  id: string;
  appId?: string; // 应用ID
  alarmName?: string; // 告警名称
  alarmIdentification?: string; // 告警编码
  alarmLevel?: string; // 告警级别
  alarmScene?: string; // 告警场景
  alarmChannel?: string; // 告警渠道
  remark?: string; // 规则描述
  status?: number; // 状态
}

export interface AlarmRecordResultVO {
  echoMap?: any;
  createdTime?: string; // 创建时间
  createdBy?: string; // 创建人
  updatedTime?: string; // 最后修改时间
  updatedBy?: string; // 最后修改人
  appId?: string; // 应用ID
  channelId?: string; // 渠道ID
  contentData?: string; // 告警具体内容信息
  alarmIdentification?: string; // 告警编码
  remark?: string; // 产品描述
  createdOrgId?: string; // 创建人组织
  handledStatus?: number; // 处理状态
  handledTime?: string; // 	处理时间
  handlingNotes?: string; // 处理记录
  id?: string; // 主键
  occurredTime?: string; // 发生时间
  resolutionNotes?: string; // 	解决记录
  resolvedTime?: string; // 解决时间
  alarmName?: string; // 告警名称
  ruleAlarmDetailsResultVO?: RuleAlarmDetailsResultVO; // 告警规则详情
}

export interface RuleAlarmDetailsResultVO {
  echoMap?: any;
  id?: string | number;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;
  appId?: string;
  alarmName?: string;
  alarmIdentification?: string;
  alarmScene?: string;
  alarmChannelIds?: string;
  level?: number;
  status?: number;
  remark?: string;
  ruleAlarmChannelDetailsResultVOList?: RuleAlarmChannelDetailsResultVO[];
}

export interface RuleAlarmChannelDetailsResultVO {
  echoMap?: any;
  id?: string | number;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;
  channelName?: string;
  channelType?: number;
  channelConfig?: string;
  status?: number;
  remark?: string;
}
