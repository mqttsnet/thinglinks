export interface VideoRecordFileResultVO {
  id?: string;
  planId?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  streamIdentification?: string;
  app?: string;
  mediaIdentification?: string;
  fileName?: string;
  fileId?: string;
  fileSize?: number;
  fileFormat?: string;
  duration?: number;
  startTime?: string;
  endTime?: string;
  thumbnailFileId?: string;
  fileStatus?: number;
  extendParams?: string;
  createdOrgId?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  deleted?: number;
  echoMap?: any;
}

export interface RecordDateItem {
  date: string;
  count: number;
}
