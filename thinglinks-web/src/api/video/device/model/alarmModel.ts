export interface VideoDeviceAlarmResultVO {
  echoMap?: any;
  id?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
  updatedBy?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  alarmPriority?: number;
  alarmMethod?: number;
  alarmTime?: string;
  alarmDescription?: string;
  alarmType?: number;
  alarmTypeParam?: string;
  longitude?: number;
  latitude?: number;
  handleStatus?: number; // 0=待处理/1=处理中/2=已处理/3=已忽略
  handleUserId?: string;
  handleTime?: string;
  handleResult?: string;
  createdOrgId?: string;
}

export interface VideoDeviceAlarmSaveVO {
  deviceIdentification: string;
  channelIdentification?: string;
  alarmPriority?: number;
  alarmMethod?: number;
  alarmTime?: string;
  alarmDescription?: string;
  alarmType?: number;
  longitude?: number;
  latitude?: number;
}

export interface AlarmStatisticsResultVO {
  totalCount?: number;
  unhandledCount?: number;
  byPriority?: CountItem[];
  byDevice?: CountItem[];
  byDay?: CountItem[];
}

export interface CountItem {
  name?: string;
  count?: number;
}

export interface VideoDeviceAlarmPageQuery {
  deviceIdentification?: string;
  channelIdentification?: string;
  alarmPriority?: number;
  alarmMethod?: number;
  alarmType?: number;
  handleStatus?: number;
  startTime?: string;
  endTime?: string;
}
