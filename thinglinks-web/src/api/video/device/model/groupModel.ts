export interface VideoDeviceGroupPageQuery {
  id?: string;
  groupName?: string;
  parentId?: string;
  groupType?: number;
  groupPath?: string;
  groupLevel?: number;
  enable?: boolean;
  createdOrgId?: string;
}

export interface VideoDeviceGroupSaveVO {
  groupName?: string;
  parentId?: string;
  groupType?: number;
  sortOrder?: number;
  groupPath?: string;
  groupLevel?: number;
  icon?: string;
  enable?: boolean;
  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
}

export interface VideoDeviceGroupUpdateVO extends VideoDeviceGroupSaveVO {
  id: string;
}

export interface VideoDeviceGroupResultVO {
  id?: string;
  groupName?: string;
  parentId?: string;
  groupType?: number;
  sortOrder?: number;
  groupPath?: string;
  groupLevel?: number;
  icon?: string;
  enable?: boolean;
  extendParams?: string;
  remark?: string;
  createdOrgId?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  echoMap?: any;
}

export interface VideoDeviceGroupRelationSaveVO {
  groupId?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  sortOrder?: number;
  extendParams?: string;
  createdOrgId?: string;
}

export interface VideoDeviceGroupRelationResultVO {
  id?: string;
  groupId?: string;
  deviceIdentification?: string;
  channelIdentification?: string;
  sortOrder?: number;
  extendParams?: string;
  createdOrgId?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  echoMap?: any;
}
