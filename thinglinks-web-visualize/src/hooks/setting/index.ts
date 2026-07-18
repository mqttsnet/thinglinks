import type { GlobConfig } from '/#/config';

import { warn } from '@/utils/log';
import { getAppEnvConfig } from '@/utils/env';

export const useGlobSetting = (): Readonly<GlobConfig> => {
  const {
    VITE_GLOB_APP_TITLE,
    VITE_GLOB_API_URL,
    VITE_GLOB_APP_SHORT_NAME,
    VITE_GLOB_API_URL_PREFIX,
    VITE_GLOB_UPLOAD_URL,
    VITE_GLOB_CLIENT_ID,
    VITE_GLOB_CLIENT_SECRET,
    VITE_GLOB_MULTI_TENANT_TYPE,
    VITE_GLOB_SHOW_CAPTCHA,
    VITE_GLOB_TIPS,
    VITE_GLOB_DEF_APPLICATION_ID,
    VITE_GLOB_BASE_APPLICATION_ID,
    VITE_GLOB_DEV_OPERATION_APPLICATION_ID,
    VITE_GLOB_PREVIEW_URL_PREFIX,
    VITE_GLOB_TOKEN_KEY,
    VITE_GLOB_TENANT_ID_KEY,
    VITE_GLOB_APPLICATION_ID_KEY,
    VITE_GLOB_AUTHORIZATION_KEY,
    VITE_GLOB_AXIOS_TIMEOUT,
  } = getAppEnvConfig();

  if (!/^[a-zA-Z_]+$/.test(VITE_GLOB_APP_SHORT_NAME)) {
    warn(
      `VITE_GLOB_APP_SHORT_NAME Variables can only be characters/underscores, please modify in the environment variables and re-running.`,
    );
  }

  // Take global configuration
  const glob: Readonly<GlobConfig> = {
    title: VITE_GLOB_APP_TITLE,
    apiUrl: VITE_GLOB_API_URL,
    shortName: VITE_GLOB_APP_SHORT_NAME,
    urlPrefix: VITE_GLOB_API_URL_PREFIX,
    uploadUrl: VITE_GLOB_UPLOAD_URL,
    clientId: VITE_GLOB_CLIENT_ID,
    clientSecret: VITE_GLOB_CLIENT_SECRET,
    multiTenantType: VITE_GLOB_MULTI_TENANT_TYPE,
    showCaptcha: VITE_GLOB_SHOW_CAPTCHA,
    tips: VITE_GLOB_TIPS,
    defApplicationId: VITE_GLOB_DEF_APPLICATION_ID,
    baseApplicationId: VITE_GLOB_BASE_APPLICATION_ID,
    devOperationApplicationId: VITE_GLOB_DEV_OPERATION_APPLICATION_ID,
    previewUrlPrefix: VITE_GLOB_PREVIEW_URL_PREFIX,
    tokenKey: VITE_GLOB_TOKEN_KEY,
    tenantIdKey: VITE_GLOB_TENANT_ID_KEY,
    applicationIdKey: VITE_GLOB_APPLICATION_ID_KEY,
    authorizationKey: VITE_GLOB_AUTHORIZATION_KEY,
    axiosTimeout: VITE_GLOB_AXIOS_TIMEOUT,
  };
  return glob as Readonly<GlobConfig>;
};
