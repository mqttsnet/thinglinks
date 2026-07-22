import { defHttp } from '/@/utils/http/axios';
import { ServicePrefixEnum } from '/@/enums/commonEnum';
import { OptionsGetResultModel, CodeQueryVO, SystemApiVO, OptionsItem } from './model/optionsModel';
import { RequestEnum } from '/@/enums/httpEnum';
import { isString } from '/@/utils/is';
import { TimeDelayReq, DelayResult, AsyncResult } from '/@/utils/thinglinks/timeDelayReq';
import { DefDictItemResultVO } from '../../devOperation/system/model/defDictItemModel';

export const Api = {
  SystemApiScan: (serviceProfix: string) => {
    return {
      url: `/${serviceProfix}/anyone/systemApiScan`,
      method: RequestEnum.GET,
    };
  },
  FindEnumMapByType: {
    url: `${ServicePrefixEnum.OAUTH}/anyTenant/enums/findEnumMapByType`,
    method: RequestEnum.POST,
  },
  FindDictByType: {
    url: `${ServicePrefixEnum.OAUTH}/anyUser/dict/findDictByType`,
    method: RequestEnum.POST,
  },
  FindCodeListByType: {
    url: `${ServicePrefixEnum.OAUTH}/anyUser/dict/findDictMapByType`,
    method: RequestEnum.POST,
  },
  FindDictMapByType2: {
    url: `${ServicePrefixEnum.OAUTH}/anyUser/dict/findDictMapByType2`,
    method: RequestEnum.POST,
  },
  Params: {
    url: `${ServicePrefixEnum.OAUTH}/anyUser/parameter/value`,
    method: RequestEnum.GET,
  },
};

export const findSystemApi = (serviceProfix: string) => {
  return defHttp.request<Map<String, SystemApiVO[]>>({ ...Api.SystemApiScan(serviceProfix) });
};

/**
 * @description: Get 蜜桔
 */
export const findEnumMapByType = (params: CodeQueryVO[] = []) => {
  return defHttp.request<OptionsGetResultModel>({ ...Api.FindEnumMapByType, params });
};

/**
 * @description: Get 字典
 */
export const findCodeListByType = (params: CodeQueryVO[] = []) => {
  return defHttp.request<any>({
    ...Api.FindCodeListByType,
    params,
  });
};

export const findDictMapByType = (params: CodeQueryVO[] = []) => {
  return defHttp.request<Record<string, DefDictItemResultVO[]>>({
    ...Api.FindDictMapByType2,
    params,
  });
};

export const findDictByType = (params: CodeQueryVO) => {
  return defHttp.request<OptionsItem[]>({
    ...Api.FindDictByType,
    params,
  });
};

export const findParams = (params: string[] | string = []) => {
  if (isString(params)) {
    params = [params];
  }
  return defHttp.request<string>({ ...Api.Params, params });
};

const codeTimeDelayReq = new TimeDelayReq({
  cacheKey: (param) => `${param?.type}`,
  getErrorData(_param, error, _reject) {
    return {
      code: 400,
      msg: error.message || '请求错误',
      data: [],
    };
  },
  // 实现批量请求
  async api(paramList, cacheKey) {
    const data = await findCodeListByType(paramList);
    const resultMap: Map<string, DelayResult> = new Map<string, DelayResult>();
    paramList.forEach((param) => {
      const key = cacheKey(param);
      const dictList = data[param?.type];
      if (dictList) {
        resultMap.set(key, {
          key,
          isOk: true,
          data: {
            code: 0,
            data: dictList,
          },
        });
      }
    });
    return resultMap;
  },
});

export async function asyncFindDictList(param: Recordable): Promise<AsyncResult> {
  return codeTimeDelayReq.loadByParam(param);
}

const enumTimeDelayReq = new TimeDelayReq({
  cacheKey: (param: any) => `${param?.type}`,
  getErrorData(_param: any, error, _reject) {
    return {
      code: 400,
      msg: error?.message || '请求错误',
      data: [],
    };
  },
  // 实现批量请求
  async api(paramList, cacheKey) {
    const data = await findEnumMapByType(paramList);
    const resultMap: Map<string, DelayResult> = new Map<string, DelayResult>();
    paramList.forEach((param) => {
      const key = cacheKey(param);
      const enumList = data[param?.type];
      if (enumList) {
        resultMap.set(key, {
          key,
          isOk: true,
          data: {
            code: 0,
            data: enumList,
          },
        });
      }
    });
    return resultMap;
  },
});

export async function asyncFindEnumList(code: Recordable): Promise<AsyncResult> {
  return await enumTimeDelayReq.loadByParam(code);
}

/**
 * 清空前端字典内存缓存(TimeDelayReq 单例 + Pinia useDictStore).
 *
 * <p>字典/字典项 CRUD(新增 / 编辑 / 启停 / 删除 / 导入)成功后调用,
 * 让所有走 {@code dictComponentProps(...)} 的下拉框、@Echo 回显等
 * 立即拉取最新值,无需 30 分钟 TTL 过期或 F5 刷新.
 *
 * <p>用法:
 * <pre>
 *   import { clearDictCache } from '/@/api/thinglinks/common/general';
 *   function handleSuccess() {
 *     clearDictCache();
 *     reload();
 *   }
 * </pre>
 */
export function clearDictCache(): void {
  codeTimeDelayReq.clear();
}
