import { defineStore } from 'pinia';
import { isAxiosError } from 'axios';
import router from '@/router';
/* enum */ 
import {
  APPLICATION_ID_KEY,
  APPLICATION_NAME_KEY,
  EXPIRE_TIME_KEY,
  REFRESH_TOKEN_KEY,
  ROLES_KEY,
  TENANT_ID_KEY,
  TOKEN_KEY,
  USER_INFO_KEY,
} from '@/enums/cacheEnum';
import { PageEnum } from '@/enums/pageEnum'
import { StorageEnum } from '@/enums/storageEnum'
import type { LoginParamVO, LogoutParams } from '@/api/thinglinks/common/model/userModel';
import type { ErrorMessageMode } from '/#/axios';
import type { DefUserInfoResultVO } from '/#/store';

/* api */
import { getUserInfoById, loginApi, logout, switchTenantAndOrg } from '@/api/thinglinks/common/oauth'
/* utils */
import { getAuthCache, setAuthCache } from '@/utils/auth';
import { clearLocalStorage, routerTurnByName } from '@/utils'
/* hooks */
import { useGlobSetting } from '@/hooks/setting'

const globSetting = useGlobSetting();
const DEF_APP_ID = globSetting.defApplicationId;
const { GO_LOGIN_INFO_STORE } = StorageEnum;

// 清理旧版本保存在 localStorage 中的登录表单（其中可能包含密码）。
try {
  clearLocalStorage(GO_LOGIN_INFO_STORE)
} catch (error) {
  console.warn('清理旧版本登录信息失败', error)
}

interface UserState {
  userInfo: Nullable<DefUserInfoResultVO>;
  token?: string;
  roleList: string[];
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  refreshToken?: string;
  expireTime?: string;
  tenantId?: string;
  applicationId: string;
  applicationName: string;
}

interface ApiErrorPayload {
  msg?: string;
}

export const useUserStore = defineStore({
  id: 'app-user',
  state: (): UserState => ({
    // user info
    userInfo: null,
    // token
    token: undefined,
    // roleList
    roleList: [],
    // Whether the login expired
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    refreshToken: '',
    expireTime: '',
    // 租户ID
    tenantId: '',
    // 应用id
    applicationId: '',
    applicationName: '',
  }),
  getters: {
    // 当前用户信息
    getUserInfo(): DefUserInfoResultVO | null {
      return this.userInfo || getAuthCache(USER_INFO_KEY) || null;
    },
    // 当前用户的Token
    getToken(): string {
      return this.token || String(getAuthCache(TOKEN_KEY) || '');
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
    // 当前租户ID
    getTenantId(): string {
      return this.tenantId || String(getAuthCache(TENANT_ID_KEY) || '');
    },
    // 当前应用ID
    getApplicationId(): string {
      return this.applicationId || String(getAuthCache(APPLICATION_ID_KEY) || '');
    },
    // 当前应用名称
    getApplicationName(): string {
      return this.applicationName || getAuthCache(APPLICATION_NAME_KEY) || '';
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRefreshToken(info: string) {
      this.refreshToken = info;
      setAuthCache(REFRESH_TOKEN_KEY, info);
    },
    setExpireTime(info: string) {
      this.expireTime = info;
      setAuthCache(EXPIRE_TIME_KEY, info);
    },
    // 租户id
    setTenantId(info: string) {
      this.tenantId = info;
      setAuthCache(TENANT_ID_KEY, info);
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },

    async switchTenantAndOrg(companyId: string, orgId?: string | null) {
      try {
        const { isSuccess = false, code = null, data = {}, errorMsg } = await switchTenantAndOrg(companyId, orgId);
        if (isSuccess && code === 0 && data) {
          const { token, tenantId, refreshToken, expiration } = data;
          // save token
          this.setToken(token);
          this.setRefreshToken(refreshToken);
          this.setExpireTime(expiration);
          this.setTenantId(tenantId);

          this.setSessionTimeout(false);
          window['$message'].success('切换成功，即将刷新页面');
          setTimeout(() => {
            location.reload();
          }, 200);
        } else {
          window['$message'].error(errorMsg || '切换租户失败，请重试');
        }
      } catch (error) {
        return Promise.reject(error);
      }
    },

    /**
    * @description: login
    */
    async login(
      params: LoginParamVO & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      },
    ): Promise<DefUserInfoResultVO | null> {
      try {
        const { goHome: _goHome = true, mode, ...loginParams } = params;
        const response = await loginApi(loginParams, mode);
        if (response.isSuccess && response.data) {
          const { token, tenantId, refreshToken, expiration } = response.data;
          window['$message'].success(window['$t']('login.login_success'));
          // save token
          this.setRefreshToken(refreshToken);
          this.setExpireTime(expiration);
          this.setTenantId(tenantId);
          this.setToken(token);
          routerTurnByName(PageEnum.BASE_HOME_NAME, true)
          
          return this.getUserInfoAction(mode, true);
        }else{
          window['$message'].error(response.msg)
          return Promise.reject(response);
        }
      } catch (error) {
        if (isAxiosError<ApiErrorPayload>(error) && error.response?.data) {
          const errorData = error.response.data
          window['$message'].error(errorData.msg || '登录失败')
          return Promise.reject(errorData);
        }
        return Promise.reject(error);
      }
    },
    // 刷新时加载用户信息
    async getUserInfoAction(
      _mode: ErrorMessageMode = 'none',
      isSetAppId = false,
    ): Promise<DefUserInfoResultVO> {
      const response = await getUserInfoById()
      if (!response.isSuccess || !response.data) {
        throw new Error(response.errorMsg || response.msg || '获取用户信息失败')
      }
      const userInfo = response.data
      if (isSetAppId) {
        this.setApplicationId(userInfo?.defApplication?.id ?? DEF_APP_ID)
        this.setApplicationName(userInfo?.defApplication?.name ?? '')
      }
      this.setUserInfo(userInfo)
      return userInfo
    },
    setUserInfo(info: DefUserInfoResultVO | null) {
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setApplicationId(info: string) {
      this.applicationId = info;
      setAuthCache(APPLICATION_ID_KEY, info);
    },
    setApplicationName(info: string) {
      this.applicationName = info;
      setAuthCache(APPLICATION_NAME_KEY, info);
    },
    /**
     * @description: logout
     */
    async logout() {
      if (this.getToken) {
        const param: LogoutParams = {
          token: this.getToken,
        };
        await logout(param).finally(() => {
          clearLocalStorage(GO_LOGIN_INFO_STORE)
          this.setToken('');
          routerTurnByName(PageEnum.BASE_LOGIN_NAME);
        });
      } else {
        clearLocalStorage(GO_LOGIN_INFO_STORE)
        this.setToken('');
        routerTurnByName(PageEnum.BASE_LOGIN_NAME);
      }
    },
  }
})
