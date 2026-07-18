import { DefUserResultVO } from '/@/api/devOperation/tenant/model/defUserModel';

export interface DefTenantBindUserVO {
  tenantId?: string;
  userIdList: string[];
  isBind: boolean;
}
export interface BaseEmployeeRoleRelSaveVO {
  employeeId: string;
  roleIdList: string[];
  flag: boolean;
}
export interface BaseEmployeePageQuery {
  isDefault?: boolean;
  userId?: string;
  positionId?: string;
  orgIdList?: string[];
  realName?: string;
  positionStatus?: string;
  state?: boolean;
}

export interface BaseEmployeeSaveVO {
  isDefault: boolean;
  userId: string;
  positionId: string;
  realName: string;
  positionStatus: string;
  state: boolean;
}

export interface BaseEmployeeUpdateVO {
  id: string;
  isDefault: boolean;
  userId: string;
  positionId: string;
  realName: string;
  positionStatus: string;
  state: boolean;
}

export interface BaseEmployeeResultVO {
  isDefault?: boolean;
  userId?: string;
  positionId?: string;
  realName?: string;
  positionStatus?: string;
  state?: boolean;
  id?: string;
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
  echoMap?: any;
  activeStatus?: string;
  orgIdList?: string[];
  lastDeptId?: string;
  lastCompanyId?: string;
  defUser: DefUserResultVO;
  createdOrgId: string;
}
