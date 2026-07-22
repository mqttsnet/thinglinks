export interface OtaUpgradeTasksPageQuery {
  upgradeId?: string; // 升级包ID，关联ota_upgrades表
  taskName?: string; // 任务名称
  taskStatus?: number; // 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
  scheduledTime?: string; // 计划执行时间
  description?: string; // 任务描述
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeTasksSaveVO {
  upgradeId?: string; // 升级包ID，关联ota_upgrades表
  taskName?: string; // 任务名称
  taskStatus?: number; // 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
  scheduledTime?: string; // 计划执行时间
  description?: string; // 任务描述
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeTasksUpdateVO {
  id: string;
  upgradeId?: string; // 升级包ID，关联ota_upgrades表
  taskName?: string; // 任务名称
  taskStatus?: number; // 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
  scheduledTime?: string; // 计划执行时间
  description?: string; // 任务描述
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}

export interface OtaUpgradeTasksResultVO {
  echoMap?: any;
  createdBy?: string; // 创建人
  createdTime?: string; // 创建时间
  updatedBy?: string; // 更新人
  updatedTime?: string; // 更新时间
  id?: string; // 主键
  upgradeId?: string; // 升级包ID，关联ota_upgrades表
  taskName?: string; // 任务名称
  taskStatus?: number; // 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
  scheduledTime?: string; // 计划执行时间
  description?: string; // 任务描述
  remark?: string; // 描述
  createdOrgId?: string; // 创建人组织
}
