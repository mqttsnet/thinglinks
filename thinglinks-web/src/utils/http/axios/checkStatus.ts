import type { ErrorMessageMode } from '/#/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStoreWithOut } from '/@/store/modules/user';
import projectSetting from '/@/settings/projectSetting';
import { SessionTimeoutProcessingEnum } from '/@/enums/appEnum';
import { ResultEnum } from '/@/enums/httpEnum';

const { createMessage, createErrorModal } = useMessage();
const stp = projectSetting.sessionTimeoutProcessing;
// 用户、员工、企业被禁用，都提示重登
// const ALERT_LOGIN_CODE = [100_000_001, 100_000_002, 100_000_003];
// 应用无权限、token 超时等直接退出重登
// const RE_LOGIN_CODE = [100_000_004];

export function checkStatus(err: any, errorMessageMode: ErrorMessageMode = 'message'): void {
  const { response, request } = err || {};
  const msg: string = response?.data?.msg ?? '';
  const status: number = response?.status;
  // const code: number = response?.data?.code;
  // if (ALERT_LOGIN_CODE.includes(code)) {
  //   status = 401;
  // }

  const { t } = useI18n();
  const userStore = useUserStoreWithOut();
  let errMessage = '';
  console.warn('status=%s, url=%s, msg=%s', status, request?.responseURL, msg);
  switch (status) {
    case 400:
      errMessage = `${msg}`;
      break;
    // 401: Not logged in
    // Jump to the login page if not logged in, and carry the path of the current page
    // Return to the current page after successful login. This step needs to be operated on the login page.
    case ResultEnum.UNAUTHORIZED:
    case ResultEnum.NOT_TOKEN:
    case ResultEnum.INVALID_TOKEN:
    case ResultEnum.TOKEN_TIMEOUT:
    case ResultEnum.BE_REPLACED:
    case ResultEnum.KICK_OUT:
    case ResultEnum.NOT_VALUE_EXPIRE:
    case ResultEnum.CODE_11074:
      userStore.setToken('');
      errMessage = msg || t('sys.api.errMsg401');
      if (stp === SessionTimeoutProcessingEnum.PAGE_COVERAGE) {
        userStore.setSessionTimeout(true);
      } else {
        userStore.logout(true);
      }
      break;
    case ResultEnum.FORBIDDEN:
      errMessage = t('sys.api.errMsg403');
      break;
    // 404请求不存在
    case 404:
      errMessage = t('sys.api.errMsg404');
      break;
    case 405:
      errMessage = t('sys.api.errMsg405');
      break;
    case 408:
      errMessage = t('sys.api.errMsg408');
      break;
    case 500:
      errMessage = t('sys.api.errMsg500');
      break;
    case 501:
      errMessage = t('sys.api.errMsg501');
      break;
    case 502:
      errMessage = t('sys.api.errMsg502');
      break;
    case 503:
      errMessage = t('sys.api.errMsg503');
      break;
    case 504:
      errMessage = t('sys.api.errMsg504');
      break;
    case 505:
      errMessage = t('sys.api.errMsg505');
      break;
    default:
  }

  if (errMessage) {
    if (errorMessageMode === 'modal') {
      createErrorModal({ title: t('sys.api.errorTip'), content: errMessage });
    } else if (errorMessageMode === 'message') {
      createMessage.error(errMessage);
    } else if (errorMessageMode === 'notification') {
      createMessage.error(errMessage);
    }
  }
}
