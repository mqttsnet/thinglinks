export interface VideoRecordPlanPageQuery {
  id?: string;
  planName?: string;
  planType?: number;
  planStatus?: number;
  mediaIdentification?: string;
  createdOrgId?: string;
}

export interface VideoRecordPlanSaveVO {
  planName?: string;
  planType?: number;
  mediaIdentification?: string;
  recordFormat?: string;
  segmentDuration?: number;
  retentionDays?: number;
  storagePath?: string;
  planStatus?: number;
  scheduleRule?: string;
  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
}

export interface VideoRecordPlanUpdateVO extends VideoRecordPlanSaveVO {
  id: string;
}

export interface VideoRecordPlanResultVO {
  id?: string;
  planName?: string;
  planType?: number;
  mediaIdentification?: string;
  recordFormat?: string;
  segmentDuration?: number;
  retentionDays?: number;
  storagePath?: string;
  planStatus?: number;
  scheduleRule?: string;
  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  echoMap?: any;
}
