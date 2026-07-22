export interface VideoSipConfigResultVO {
  echoMap?: any;
  id?: string;
  configName?: string;
  sipId?: string;
  sipDomain?: string;
  sipPassword?: string;
  sipServerAddress?: string;
  bindIp?: string;
  isDefault?: number;
  registerInterval?: number;
  status?: number;
  remark?: string;
  createdOrgId?: string;
  createdTime?: string;
  createdBy?: string;
  updatedTime?: string;
}

export interface VideoSipConfigSaveVO {
  configName: string;
  sipId: string;
  sipDomain: string;
  sipPassword?: string;
  sipServerAddress?: string;
  bindIp?: string;
  isDefault?: number;
  registerInterval?: number;
  status?: number;
  remark?: string;
}

export interface VideoSipConfigPageQuery {
  configName?: string;
  sipId?: string;
  sipDomain?: string;
  status?: number;
}

export interface SipServerInfo {
  port?: number;
  monitorIps?: string[];
  timeout?: number;
}
