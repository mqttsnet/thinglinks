export interface VideoGatewayMappingPageQuery {
  id?: string;
  srcProtocol?: string;
  srcDeviceIdentification?: string;
  srcChannelIdentification?: string;
  gbDeviceId?: string;
  gbChannelId?: string;
  gbPlatformId?: number;
  enable?: boolean;
  autoPush?: boolean;
  registerStatus?: boolean;
}

export interface VideoGatewayMappingSaveVO {
  srcProtocol?: string;
  srcDeviceIdentification?: string;
  srcChannelIdentification?: string;
  gbDeviceId?: string;
  gbChannelId?: string;
  gbPlatformId?: number;
  enable?: boolean;
  autoPush?: boolean;
  mappingConfig?: string;
  remark?: string;
}

export interface VideoGatewayMappingUpdateVO extends VideoGatewayMappingSaveVO {
  id: string;
}

export interface VideoGatewayMappingResultVO {
  id?: string;
  srcProtocol?: string;
  srcDeviceIdentification?: string;
  srcChannelIdentification?: string;
  gbDeviceId?: string;
  gbChannelId?: string;
  gbPlatformId?: number;
  enable?: boolean;
  autoPush?: boolean;
  mappingConfig?: string;
  registerStatus?: boolean;
  lastRegisterTime?: string;
  remark?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  echoMap?: any;
}
