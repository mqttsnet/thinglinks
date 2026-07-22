import { useMessage } from '/@/hooks/web/useMessage';
import componentSetting, { SORT_FIELD } from '/@/settings/componentSetting';
import { isArray, isFunction, isString } from '/@/utils/is';
import { DictEnum } from '/@/enums/commonEnum';
import { asyncFindDictList, asyncFindEnumList } from '/@/api/thinglinks/common/general';
import { useI18n } from '/@/hooks/web/useI18n';
import { Tag } from 'ant-design-vue';

const { createMessage } = useMessage();
const { t } = useI18n();

const PAGE_PARAMS = Object.values(componentSetting.table.fetchSetting);

export const handleSearchInfoByCreateTime = (data: Recordable) => {
  const temp = { ...data };
  for (const k in data) {
    delete data[k];
  }
  data.model = temp;
  if (temp.createTimeRange) {
    data.extra = {
      createTime_st: temp.createTimeRange[0],
      createTime_ed: temp.createTimeRange[1],
    };
  }
  delete data.model['createTimeRange'];
  return data;
};

export const handleFetchParams = (data: Recordable) => {
  const tempData = { model: {}, extra: {} };
  const model = {};
  for (const key in data) {
    if (PAGE_PARAMS.includes(key)) {
      tempData[key] = data[key];
      if (key === SORT_FIELD && isArray(data[SORT_FIELD])) {
        if (data[SORT_FIELD][0] === 'echoMap') {
          tempData[key] = data[SORT_FIELD][1];
        } else {
          tempData[key] = data[SORT_FIELD][0];
        }
      }
    } else if (key.endsWith(',desc')) {
      model[key.split(',desc')[0]] = data[key];
    } else if (key.startsWith('echoMap,')) {
      const tempK = key.split('echoMap,')[1];
      model[tempK.split(',')[0]] = data[key];
    } else if (key === 'createTimeRange') {
    } else {
      model[key] = data[key];
    }
  }
  if (data?.createTimeRange) {
    tempData.extra = {
      createdTime_st: data.createTimeRange[0],
      createdTime_ed: data.createTimeRange[1],
    };
  }
  tempData.model = model;

  delete tempData.model.timeStamp;

  return tempData;
};

export const blobToObj = (data) => {
  return new Promise((resolve) => {
    if (data?.type == 'application/json') {
      const reader = new FileReader();
      reader.readAsText(data, 'utf-8');
      reader.onload = function (e) {
        try {
          const parseObj = JSON.parse(e.target?.result as string);
          resolve(parseObj);
        } catch (error) {
          resolve({
            code: -1,
            msg: '获取失败',
          });
        }
      };
    }
  });
};

/**
 * 根据 条件回调 查找节点
 * @param list 树列表
 * @param func 条件回调
 * @param resultFunc 返回值回调
 */
export function findNodeByFunction(list: any[], func: Fn, resultFunc: Fn) {
  if (!isFunction(func)) {
    console.error('func 参数不是一个函数');
    return [];
  }
  if (!list || !isArray(list)) {
    return [];
  }
  let nodeList: any[] = [];
  for (let i = 0; i < list.length; i++) {
    const item = list[i];
    if (func(item)) {
      nodeList.push(resultFunc(item));
    }
    if (item.children && item.children.length > 0) {
      const childrenNodes = findNodeByFunction(item.children, func, resultFunc);
      nodeList = [...nodeList, ...childrenNodes];
    }
  }
  return nodeList;
}

/**
 * 校验手机号格式
 * @param mobile
 */
export function validMobile(mobile) {
  const reg = /^0?1[0-9]{10}$/;
  return reg.test(mobile);
}

// 格式化文件大小 单位：B、KB、MB、GB
export const formatFileSize = (value) => {
  if (null == value || value == '') {
    return '0 M';
  }
  const unitArr = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const srcSize = parseFloat(value);
  const index = Math.floor(Math.log(srcSize) / Math.log(1024));
  const size = srcSize / Math.pow(1024, index);
  if (unitArr[index]) {
    return size.toFixed(2) + unitArr[index];
  }
  return '文件太大';
};

export const enumAllComponentProps = (
  type: DictEnum | string,
  extendFirst = true,
  excludes?: string | string[],
) => {
  return enumComponentProps(
    type,
    extendFirst,
    { label: t('thinglinks.common.all'), text: t('thinglinks.common.all'), value: null },
    excludes,
  );
};

export const enumComponentProps = (
  type: DictEnum | string,
  extendFirst = true,
  extend?: any,
  excludes?: string | string[],
) => {
  if (excludes && isString(excludes)) {
    excludes = [excludes];
  }
  return {
    api: asyncFindEnumList,
    params: { type, extendFirst, extend, excludes },
    resultField: 'data',
    showSearch: true,
    type: 'dict',
    filterOption: (input: string, option: any) => {
      return option.label.toUpperCase().indexOf(input.toUpperCase()) >= 0;
    },
  };
};

export const dictAllComponentProps = (
  type: DictEnum | string,
  extendFirst = true,
  excludes?: string | string[],
) => {
  return dictComponentProps(
    type,
    extendFirst,
    { label: t('thinglinks.common.all'), text: t('thinglinks.common.all'), value: null },
    excludes,
  );
};

export const dictComponentProps = (
  type: DictEnum | string,
  extendFirst = true,
  extend?: any,
  excludes?: string | string[],
) => {
  if (excludes && isString(excludes)) {
    excludes = [excludes];
  }
  return {
    api: asyncFindDictList,
    params: { type, extend, extendFirst, excludes },
    type: 'dict',
    resultField: 'data',
    showSearch: true,
    filterOption: (input: string, option: any) => {
      return option?.label?.toUpperCase().indexOf(input.toUpperCase()) >= 0;
    },
  };
};

export const dictComponentProps2 = (param: {
  type: DictEnum | string;
  extendFirst: boolean;
  stringToNumber?: boolean;
  extend?: any;
  excludes?: string | string[];
}) => {
  const { type, extendFirst, extend, stringToNumber } = param;
  let excludes = param.excludes;
  if (excludes && isString(excludes)) {
    excludes = [excludes];
  }
  return {
    api: asyncFindDictList,
    params: { type, extend, extendFirst, excludes },
    resultField: 'data',
    showSearch: true,
    type: 'dict',
    stringToNumber,
    filterOption: (input: string, option: any) => {
      return option?.label?.toUpperCase().indexOf(input.toUpperCase()) >= 0;
    },
  };
};

export const stateComponentProps = (all?: boolean) => {
  if (all) {
    return {
      options: [
        { label: t('thinglinks.common.all'), value: null },
        { label: t('thinglinks.common.enable'), value: true },
        { label: t('thinglinks.common.disable'), value: false },
      ],
    };
  } else {
    return {
      options: [
        { label: t('thinglinks.common.enable'), value: true },
        { label: t('thinglinks.common.disable'), value: false },
      ],
    };
  }
};

export const stateNumberComponentProps = (all?: boolean) => {
  if (all) {
    return {
      options: [
        { label: t('thinglinks.common.all'), value: null },
        { label: t('thinglinks.common.enable'), value: 1 },
        { label: t('thinglinks.common.disable'), value: 0 },
      ],
    };
  } else {
    return {
      options: [
        { label: t('thinglinks.common.enable'), value: 1 },
        { label: t('thinglinks.common.disable'), value: 0 },
      ],
    };
  }
};

export const yesNoComponentProps = (all = false) => {
  if (all) {
    return {
      options: [
        { label: t('thinglinks.common.all'), value: null },
        { label: t('thinglinks.common.yes'), value: true },
        { label: t('thinglinks.common.no'), value: false },
      ],
    };
  } else {
    return {
      options: [
        { label: t('thinglinks.common.yes'), value: true },
        { label: t('thinglinks.common.no'), value: false },
      ],
    };
  }
};

export const yesNoNumberComponentProps = (all = false) => {
  if (all) {
    return {
      options: [
        { label: t('thinglinks.common.all'), value: null },
        { label: t('thinglinks.common.yes'), value: 1 },
        { label: t('thinglinks.common.no'), value: 0 },
      ],
    };
  } else {
    return {
      options: [
        { label: t('thinglinks.common.yes'), value: 1 },
        { label: t('thinglinks.common.no'), value: 0 },
      ],
    };
  }
};

export const stateFilters = () => {
  return [
    { text: t('thinglinks.common.enable'), value: 'true' },
    { text: t('thinglinks.common.disable'), value: 'false' },
  ];
};

export const yesNoFilters = () => {
  return [
    { text: t('thinglinks.common.yes'), value: 'true' },
    { text: t('thinglinks.common.no'), value: 'false' },
  ];
};

export const getLabelFilter = (value, valueKey, labelKey, dictList) => {
  if (dictList && dictList.length) {
    const list = dictList.filter((item) => {
      return item[valueKey] == value;
    });
    if (list.length) {
      return list[0][labelKey];
    } else {
      return '';
    }
  } else {
    return '';
  }
};

/**
 * 把任意类型的状态值标准化成 boolean / null。
 * 后端可能返回 boolean / number(1|0) / string("true"|"false"|"1"|"0"|"yes"|"no")，
 * 用严格相等比较容易在类型不一致时落到默认分支显示空值，先归一再分发。
 */
export const normalizeYesNo = (v: any): boolean | null => {
  if (v === null || v === undefined) return null;
  if (typeof v === 'boolean') return v;
  if (typeof v === 'number') return v !== 0;
  if (typeof v === 'string') {
    const s = v.toLowerCase().trim();
    if (s === '' || s === 'null' || s === 'undefined') return null;
    if (s === 'true' || s === '1' || s === 'yes' || s === 'y' || s === 'online') return true;
    if (s === 'false' || s === '0' || s === 'no' || s === 'n' || s === 'offline') return false;
  }
  return Boolean(v);
};

/**
 * 在线 / 是否启用 等"是/否"语义字段的统一真值判断。
 * 详情页 / 卡片 / 标签 直接用这个，不要写 `xxx.onlineStatus ? ...` 这种朴素 truthy，
 * 后端偶发返回字符串 "false"（非空字符串 → JS 视为 truthy）时会反向渲染。
 */
export const isTruthyStatus = (v: any): boolean => {
  return normalizeYesNo(v) === true;
};

export const renderYesNoComponent = (text: boolean | null | number | string, withTag = false) => {
  const norm = normalizeYesNo(text);
  let _text = '';
  let color = '';
  if (norm === null) {
    _text = t('thinglinks.common.all');
    color = 'processing';
  } else if (norm === true) {
    _text = t('thinglinks.common.yes');
    color = 'success';
  } else {
    _text = t('thinglinks.common.no');
    color = 'error';
  }
  return withTag ? <Tag color={color}>{_text}</Tag> : _text;
};

/**
 * 复制文本
 * @param text - 文本内容
 */
export const handleCopyText = async (text: string) => {
  try {
    await window.navigator.clipboard.writeText(text);
    createMessage.success(t('common.tips.copySuccess'));
  } catch (err) {
    console.log(err);
    createMessage.error(t('common.tips.copyFail'));
  }
};

/**
 * 内容复制
 * */
export const handleCopyTextV2 = async (text: string) => {
  try {
    if (!text) {
      createMessage.error(t('common.tips.copyFail'));
      return;
    }

    if (navigator.clipboard?.writeText && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
    } else {
      const input = document.createElement('input');
      input.style.position = 'absolute';
      input.style.opacity = '0';
      input.value = text;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
    }

    createMessage.success(t('common.tips.copySuccess'));
  } catch (err) {
    console.error(`handleCopyTextV2 error:`, err);
    createMessage.error(t('common.tips.copyFail'));
  }
};

/**
 * 判断两字符串是否相等，不区分大小写
 * */
export const isEqualIgnoreCase = (str, targetStr) => {
  return (
    typeof str === 'string' &&
    typeof targetStr === 'string' &&
    str.toLowerCase() === targetStr.toLowerCase()
  );
};

/**
 * 获取具体内容
 * */
export const getLabelAlertInfoFilter = (value, valueKey, dictList) => {
  if (dictList && dictList.length) {
    const list = dictList.filter((item) => {
      return item[valueKey] == value;
    });
    if (list.length) {
      const item = list[0];
      let desc = '';
      if (item.datatype) {
        desc += `指示数据类型：${item.datatype}`;
      }
      if (item.maxlength) {
        desc += `、指示最大长度：${item.maxlength}`;
      }
      if (item.max) {
        desc += `、最大值：${item.max}`;
      }
      if (item.min || item.min === 0) {
        desc += `、最小值：${item.min}`;
      }
      return desc;
    } else {
      return '';
    }
  } else {
    return '';
  }
};
