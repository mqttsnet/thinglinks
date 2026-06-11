export interface OtaUpgradeRecordsPageQuery {
  taskId?: string; // 任务ID，关联ota_upgrade_tasks表
  deviceIdentification?: string; // 设备标识
  upgradeStatus?: number; // 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
  progress?: number; // 升级进度（百分比）
  errorCode?: string; // 错误代码
  errorMessage?: string; // 错误信息
  startTime?: string; // 升级开始时间
  endTime?: string; // 升级结束时间
  successDetails?: string; // 升级成功详细信息
  failureDetails?: string; // 升级失败详细信息
  logDetails?: string; // 升级过程日志
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeRecordsSaveVO {
  taskId?: string; // 任务ID，关联ota_upgrade_tasks表
  deviceIdentification?: string; // 设备标识
  upgradeStatus?: number; // 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
  progress?: number; // 升级进度（百分比）
  errorCode?: string; // 错误代码
  errorMessage?: string; // 错误信息
  startTime?: string; // 升级开始时间
  endTime?: string; // 升级结束时间
  successDetails?: string; // 升级成功详细信息
  failureDetails?: string; // 升级失败详细信息
  logDetails?: string; // 升级过程日志
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeRecordsUpdateVO {
  id: string;
  taskId?: string; // 任务ID，关联ota_upgrade_tasks表
  deviceIdentification?: string; // 设备标识
  upgradeStatus?: number; // 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
  progress?: number; // 升级进度（百分比）
  errorCode?: string; // 错误代码
  errorMessage?: string; // 错误信息
  startTime?: string; // 升级开始时间
  endTime?: string; // 升级结束时间
  successDetails?: string; // 升级成功详细信息
  failureDetails?: string; // 升级失败详细信息
  logDetails?: string; // 升级过程日志
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeRecordsResultVO {
  echoMap?: any;
  createdTime?: string; // 记录创建时间
  id?: string; // 主键
  taskId?: string; // 任务ID，关联ota_upgrade_tasks表
  deviceIdentification?: string; // 设备标识
  upgradeStatus?: number; // 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
  progress?: number; // 升级进度（百分比）
  errorCode?: string; // 错误代码
  errorMessage?: string; // 错误信息
  startTime?: string; // 升级开始时间
  endTime?: string; // 升级结束时间
  successDetails?: string; // 升级成功详细信息
  failureDetails?: string; // 升级失败详细信息
  logDetails?: string; // 升级过程日志
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}
