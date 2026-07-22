import axios, { AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { RequestHttpHeaderEnum, ResultEnum, ModuleTypeEnum } from '@/enums/httpEnum'
import { PageEnum, ErrorPageNameMap } from '@/enums/pageEnum'
import { StorageEnum } from '@/enums/storageEnum'
import { axiosPre } from '@/settings/httpSetting'
import { useGlobSetting } from '@/hooks/setting';
import { getApplicationId, getTenantId, getToken } from '@/utils/auth';
import { SystemStoreEnum, SystemStoreUserInfoEnum } from '@/store/modules/systemStore/systemStore.d'
import { redirectErrorPage, getLocalStorage, routerTurnByName, isPreview } from '@/utils'
import { fetchAllowList } from './axios.config'
import includes from 'lodash/includes'
import { Base64 } from 'js-base64';
import i18n from '@/i18n';
import router from '@/router';
import type { Result } from '/#/axios'

export interface MyResponseType<T> extends Result<T> {}

export type BinaryResponseType = 'arraybuffer' | 'blob'
export type BinaryResponseData<T extends BinaryResponseType> = T extends 'arraybuffer'
  ? ArrayBuffer
  : Blob

const {
  uploadUrl,
  apiUrl,
  multiTenantType,
  clientId,
  clientSecret,
  tokenKey,
  tenantIdKey,
  applicationIdKey,
  authorizationKey,
} = useGlobSetting();

const axiosInstance = axios.create({
  // baseURL: `${import.meta.env.PROD ? import.meta.env.VITE_PRODUCTION_PATH : ''}${axiosPre}`,
  baseURL: import.meta.env.DEV ? 'api' : import.meta.env.VITE_PRODUCTION_PATH,
  timeout: ResultEnum.TIMEOUT
})

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 白名单校验
    // if (includes(fetchAllowList, config.url)) return config
    // // 获取 token
    // const info = getLocalStorage(StorageEnum.GO_SYSTEM_STORE)
    // // 重新登录
    // if (!info) {
    //   routerTurnByName(PageEnum.BASE_LOGIN_NAME)
    //   return config
    // }
    // const userInfo = info[SystemStoreEnum.USER_INFO]
    // config.headers[userInfo[SystemStoreUserInfoEnum.TOKEN_NAME] || 'token'] =  userInfo[SystemStoreUserInfoEnum.USER_TOKEN] || ''
    
    // 增加租户编码
    if (getTenantId()) {
      (config as Recordable).headers[tenantIdKey] = getTenantId();
    }
    // 标识
    if (getToken()) {
      (config as Recordable).headers[tokenKey] = getToken();
    }
    if (getApplicationId()) {
      (config as Recordable).headers[applicationIdKey] = getApplicationId();
    }
    (config as Recordable).headers[authorizationKey] = `${Base64.encode(`${clientId}:${clientSecret}`)}`;

    // 当前请求地址#号后的路径，需要用户后台判断该页面的数据权限
    (config as Recordable).headers['Path'] = router?.currentRoute?.value?.fullPath;
    // // 多语言标识
    (config as Recordable).headers['Locale'] = i18n.global.locale.value;

    return config
  },
  (err: AxiosError) => {
    return Promise.reject(err)
  }
)

// 响应拦截器
axiosInstance.interceptors.response.use(
  (res: AxiosResponse) => {
    // 预览页面错误不进行处理
    if (isPreview()) {
      return Promise.resolve(res.data)
    }
    const responseData = res.data as Partial<Result<unknown>>
    const { code } = responseData

    if (code === undefined || code === null) return Promise.resolve(res.data)

    // 成功
    if (code === ResultEnum.DATA_SUCCESS) {
      return Promise.resolve(res.data)
    }

    // 登录过期
    if (code === ResultEnum.TOKEN_OVERDUE) {
      window['$message'].error(window['$t']('http.token_overdue_message'))
      routerTurnByName(PageEnum.BASE_LOGIN_NAME)
      return Promise.resolve(res.data)
    }

    // 固定错误码重定向
    if (ErrorPageNameMap.get(code)) {
      redirectErrorPage(code)
      return Promise.resolve(res.data)
    }

    // 提示错误
    window['$message'].error(window['$t'](responseData.msg || ''))
    return Promise.resolve(res.data)
  },
  (err: AxiosError) => {
    const status = err.response?.status
    switch (status) {
      case 401:
        routerTurnByName(PageEnum.BASE_LOGIN_NAME)
        return Promise.reject(err)

      default:
        return Promise.reject(err)
    }
  }
)

export default axiosInstance
