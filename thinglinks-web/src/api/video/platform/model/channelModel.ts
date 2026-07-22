export interface VideoPlatformChannelPageQuery {
  platformId?: string;
  catalogId?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  customName?: string;
}

export interface VideoPlatformChannelSaveVO {
  platformId: string;
  deviceChannelId: string;
  catalogId?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  customName?: string;
  customGbId?: string;
}

export interface VideoPlatformChannelBindParams {
  platformId: string;
  channelIds: string[];
  catalogId?: string;
}

export interface VideoPlatformChannelResultVO {
  id: string;
  platformId: string;
  deviceChannelId: string;
  catalogId: string;
  deviceIdentification: string;
  channelIdentification: string;
  customName: string;
  customGbId: string;
  createdBy: string;
  createdTime: string;
  updatedBy: string;
  updatedTime: string;
  echoMap?: Recordable;
}
