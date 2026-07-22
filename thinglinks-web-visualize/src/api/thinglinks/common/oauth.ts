import { RequestHttpEnum, ContentTypeEnum } from '@/enums/httpEnum'
import { ServicePrefixEnum } from '@/enums/commonEnum';
import {
  LoginParamVO,
  LoginResultVO,
  LogoutParams,
} from './model/userModel';
import { get, getRaw, post, put } from '@/api/http';
import type { DefUserInfoResultVO } from '/#/store';
import { ErrorMessageMode } from '/#/axios';
// 单个X数据
export const chartDataUrl = '/anyTenant/captcha'

const Api = {
  // 登录
  Login: {
    url: `${ServicePrefixEnum.OAUTH}/anyTenant/login`,
    method: RequestHttpEnum.POST,
  },
  // 加载验证码
  LoadCaptcha: {
    // 正则
    url: `${ServicePrefixEnum.OAUTH}${chartDataUrl}`,
    method: RequestHttpEnum.GET,
    responseType: 'arraybuffer',
  },
  // 登出
  Logout:{
    url: `${ServicePrefixEnum.OAUTH}/anyUser/logout`,
    method: RequestHttpEnum.POST,
  },
  // UserInfo
  UserInfo:{
    url: `${ServicePrefixEnum.OAUTH}/anyone/getUserInfoById`,
    method: RequestHttpEnum.GET,
  },
  // 查询单位和部门
  FindCompanyDept: {
    url: `${ServicePrefixEnum.OAUTH}/anyone/findCompanyDept`,
    method: 'GET',
  },
  // 切换当前企业
  SwitchTenantAndOrg: {
    url: `${ServicePrefixEnum.OAUTH}/anyone/switchTenantAndOrg`,
  },
  // 设置默认企业
  UpdateDefaultTenant: {
    url: `${ServicePrefixEnum.BASE}/anyone/updateDefaultTenant`,
  },
}
/**
 * @description: 加载验证码
 */
export function loadCaptcha(key: string) {
  return getRaw(Api.LoadCaptcha.url, { key }, 'arraybuffer')
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParamVO, _mode: ErrorMessageMode = 'modal') {
  return post<LoginResultVO>(Api.Login.url, params);
}
/**
 * @description: user logout api
 */
export function logout(params: LogoutParams) {
  return post(Api.Logout.url, params);
}
/**
 * @description: userinfo
 */
export function getUserInfoById() {
  return get<DefUserInfoResultVO>(Api.UserInfo.url);
}

export function findCompanyDept(tenantId?: string) {
  return get(Api.FindCompanyDept.url, { tenantId });
}

export function switchTenantAndOrg(tenantId?: string, orgId?: string | null) {
  return put(Api.SwitchTenantAndOrg.url, { tenantId, orgId }, ContentTypeEnum.FORM_URLENCODED);
}

export function updateDefaultTenant(tenantId?: string) {
  return put(Api.UpdateDefaultTenant.url, { tenantId }, ContentTypeEnum.FORM_URLENCODED);
}
