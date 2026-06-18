import { Ref, h } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import type { CardField } from '/@/components/BusinessCardList';
import { useI18n } from '/@/hooks/web/useI18n';
import { useDict } from '/@/components/Dict';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt, RuleType } from '/@/api/thinglinks/common/formValidateService';
import { query } from '../../../../../api/iot/link/ota/otaUpgrades';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
import { Dayjs } from 'dayjs';
import { cloneDeep } from 'lodash-es';

// 设备分组tree
import { tree as deviceGroupTree } from '/@/api/iot/link/group/deviceGroup';
// 定向升级comp
import BasicSelectDeviceModal from '/@/components/iot/BasicSelectDeviceModal/BasicSelectDeviceModal.vue';
// 区域升级comp
import BasicAreaSelector from '/@/components/iot/BasicAreaSelection/BasicAreaSelector.vue';
import type { AreaSelectorItem } from '/@/components/iot/BasicAreaSelection/typing';

const { t } = useI18n();
const { getDictLabel } = useDict();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.ota.otaUpgradeTasks.upgradeId'),
      dataIndex: 'upgradeId',
      width: 200,
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.taskName'),
      dataIndex: 'taskName',
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.taskStatus'),
      dataIndex: 'taskStatus',
      customRender: ({ record }) => {
        return getDictLabel(DictEnum.LINK_OTA_TASK_STATUS, record?.taskStatus, '');
      },
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.upgradeMode'),
      dataIndex: ['echoMap', 'upgradeMethod'],
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.upgradeScope'),
      dataIndex: ['echoMap', 'upgradeScope'],
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.scheduledTime'),
      dataIndex: 'scheduledTime',
      format: (value: string, record: Recordable) => {
        return `${record.scheduledStartTime ?? ''} - ${record.scheduledEndTime ?? ''}`;
      },
      width: 360,
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.description'),
      dataIndex: 'description',
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.ota.otaUpgradeTasks.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('thinglinks.common.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

// 卡片视图字段(Flexy)。名称取 taskName,右上角徽标取 taskStatus(任务状态字典)
export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.ota.otaUpgradeTasks.upgradeMode'),
    field: 'echoMap.upgradeMethod',
    span: 12,
  },
  {
    label: t('iot.link.ota.otaUpgradeTasks.upgradeScope'),
    field: 'echoMap.upgradeScope',
    span: 12,
  },
  {
    label: t('iot.link.ota.otaUpgradeTasks.scheduledTime'),
    field: 'scheduledStartTime',
    span: 24,
  },
  {
    label: t('thinglinks.common.createdTime'),
    field: 'createdTime',
    span: 24,
  },
];

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.ota.otaUpgradeRecords.taskId'),
      field: 'id',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeId'),
      field: 'upgradeId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        api: query,
        labelField: 'packageName',
        valueField: 'id',
        params: {},
        search: true,
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeMode'),
      field: 'upgradeMethod',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_OTA_UPGRADE_METHOD),
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeScope'),
      field: 'upgradeScope',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_OTA_UPGRADE_SCOPE),
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.taskName'),
      field: 'taskName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'createTimeRange',
      label: t('thinglinks.common.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (
  _type: Ref<ActionEnum>,
  productIdentification: Ref<string>,
  packageType: Ref<number | undefined>,
  setFieldsValue?: (values: Recordable) => Promise<void>,
): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.taskName'),
      field: 'taskName',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeId'),
      field: 'upgradeId',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        api: query,
        labelField: 'packageName',
        valueField: 'id',
        params: {},
        search: true,
        disabled: _type.value === ActionEnum.EDIT,
        onChange: (value, option) => {
          productIdentification.value = option?.productIdentification;
          packageType.value = option?.packageType;
          // 当 upgradeId 变化时，清空相关字段
          if (setFieldsValue && value) {
            setFieldsValue({
              upgradeMethod: undefined,
              upgradeScope: undefined,
              sourceVersions: [],
              targetIds: [],
              groupIds: [],
              areaIds: [],
            });
          }
        },
      },
    },
    // 升级包对应产品标识 --- 主要用于定向升级设备信息获取，不显示，不提交
    {
      label: t('iot.link.ota.otaUpgradeTasks.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.taskStatus'),
      field: 'taskStatus',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        disabled: true,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_OTA_TASK_STATUS),
      },
      show: false,
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeMode'),
      field: 'upgradeMethod',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        disabled: _type.value === ActionEnum.EDIT,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_OTA_UPGRADE_METHOD),
      },
      helpMessage: [
        t('iot.link.ota.otaUpgradeTasks.upgradeModeTips.staticUpgrade'),
        t('iot.link.ota.otaUpgradeTasks.upgradeModeTips.dynamicUpgrade'),
      ],
      ifShow: ({ values }) => {
        return values.upgradeId || values.upgradeId === 0;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeScope'),
      field: 'upgradeScope',
      component: 'ApiSelect',
      componentProps: {
        disabled: _type.value === ActionEnum.EDIT,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_OTA_UPGRADE_SCOPE),
      },
      ifShow: ({ values }) => {
        return (
          values.upgradeMethod ||
          (values.upgradeMethod === 0 && (values.upgradeId || values.upgradeId === 0))
        );
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.directUpgrade'),
      field: 'targetIds',
      component: 'Input',
      required: true,
      componentProps: {},
      ifShow: ({ values }) => {
        return values.upgradeScope === 1;
      },
      render: ({ model }) => {
        return h(BasicSelectDeviceModal, {
          productIdentification: model.productIdentification,
          disabled: _type.value === ActionEnum.EDIT,
          selectedDevice: model.targetIds ?? [],
          onSelect: (ids: string[]) => {
            model.targetIds = ids;
          },
        });
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.groupUpgrade'),
      field: 'groupIds',
      component: 'ApiTreeSelect',
      required: true,
      componentProps: {
        disabled: _type.value === ActionEnum.EDIT,
        api: deviceGroupTree,
        labelField: 'groupName',
        valueField: 'id',
        allowClear: true,
        multiple: true,
        showCheckedStrategy: 'SHOW_ALL',
      },
      ifShow: ({ values }) => {
        return values.upgradeScope === 2;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.areaUpgrade'),
      field: 'areaIds',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.upgradeScope === 3;
      },
      render: ({ model }) => {
        return h(BasicAreaSelector, {
          modelValue: model.areaIds ?? [],
          disabled: _type.value === ActionEnum.EDIT,
          'onUpdate:modelValue': (value: unknown) => {
            model.areaIds = value as AreaSelectorItem[];
          },
        });
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.scheduledStartTime'),
      field: 'scheduledStartTime',
      component: 'DatePicker',
      componentProps: () => {
        const now = dateUtil();

        return {
          format: 'YYYY-MM-DD HH:mm:ss',
          valueFormat: 'YYYY-MM-DD HH:mm:ss',
          showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
          disabled: _type.value === ActionEnum.EDIT, // 编辑模式下禁用
          disabledDate: (current: Dayjs) => {
            // 禁用今天之前的日期
            return current && current.isBefore(now, 'day');
          },
        };
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.scheduledEndTime'),
      field: 'scheduledEndTime',
      component: 'DatePicker',
      componentProps: ({ formModel }) => {
        const now = dateUtil();

        return {
          format: 'YYYY-MM-DD HH:mm:ss',
          valueFormat: 'YYYY-MM-DD HH:mm:ss',
          showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
          disabled: _type.value === ActionEnum.EDIT, // 编辑模式下禁用
          disabledDate: (current: Dayjs) => {
            // 如果已选择开始时间，则结束时间必须在开始时间之后
            if (formModel?.scheduledStartTime) {
              const startTime = dateUtil(formModel.scheduledStartTime);
              // 禁用开始时间之前的日期
              return current && current.isBefore(startTime, 'day');
            }
            // 如果没有开始时间，禁用今天之前的日期
            return current && current.isBefore(now, 'day');
          },
        };
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.sourceVersions'),
      field: 'sourceVersions',
      component: 'ApiSelect',
      required: true,
      ifShow: ({ values }) => {
        return values.upgradeId || values.upgradeId === 0;
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.appConfirmationRequired'),
      field: 'appConfirmationRequired',
      component: 'Switch',
      required: true,
      defaultValue: false,
      componentProps: {
        checkedValue: true,
        unCheckedValue: false,
        checkedChildren: t('thinglinks.common.yes'),
        unCheckedChildren: t('thinglinks.common.no'),
        disabled: _type.value === ActionEnum.EDIT, // 编辑模式下禁用
      },
      helpMessage: t('iot.link.ota.otaUpgradeTasks.appConfirmationRequiredTips'),
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.upgradeRate'),
      field: 'upgradeRate',
      component: 'InputNumber',
      componentProps: {
        precision: 0,
        disabled: _type.value === ActionEnum.EDIT, // 编辑模式下禁用
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeout'),
      field: 'deviceUpgradeTimeout',
      component: 'InputNumber',
      required: false, // 非必填
      componentProps: {
        precision: 0,
        placeholder: t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeoutPlaceholder'),
        disabled: _type.value === ActionEnum.EDIT, // 编辑模式下禁用
      },
      helpMessage: t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeoutTips'),
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.maxRetryCount'),
      field: 'maxRetryCount',
      component: 'InputNumber',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.retryIntervalMinutes'),
      field: 'retryIntervalMinutes',
      component: 'InputNumber',
      defaultValue: 10,
      componentProps: {
        precision: 0,
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.description'),
      field: 'description',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
    {
      label: t('iot.link.ota.otaUpgradeTasks.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (
  type: Ref<ActionEnum>,
  getFieldsValue: () => Promise<Recordable>,
): Partial<FormSchemaExt>[] => {
  return [
    {
      field: 'areaIds',
      type: RuleType.cover,
      // 判断areaIds是否存在重复项
      rules: [
        {
          async validator(_rule: any, value: any) {
            if (value?.length > 0) {
              const areaIdsSet = new Set(cloneDeep(value).map((item) => item.cityCode || item.uid));
              if (areaIdsSet.size !== value.length) {
                return Promise.reject(
                  t('iot.link.ota.otaUpgradeTasks.areaUpgradeTips.areaIdsRepeat'),
                );
              }
              if (!value.length) {
                return Promise.reject(
                  t('iot.link.ota.otaUpgradeTasks.areaUpgradeTips.selectAreaRequired'),
                );
              }
              return Promise.resolve();
            } else {
              return Promise.reject();
            }
          },
        },
      ],
    },
    {
      field: 'scheduledStartTime',
      type: RuleType.cover,
      // 计划开始时间必须大于当前时间+10分钟，精确到分钟
      rules: [
        {
          required: true,
          async validator(_rule: any, value: any) {
            // 编辑模式下该字段禁用、沿用已保存的计划时间,不再做「未来时间」校验。
            // 否则编辑历史任务时开始时间已过,会永远校验失败、抽屉无法保存关闭。
            if (type.value === ActionEnum.EDIT) {
              return Promise.resolve();
            }
            if (!value) {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.scheduledStartTimeRequired'));
            }
            const now = dateUtil();
            const tenMinutesLater = now.add(10, 'minute').startOf('minute');
            const selectedTime = dateUtil(value).startOf('minute');

            // 计算时间差（分钟）
            const diffInMinutes = selectedTime.diff(tenMinutesLater, 'minute');

            // 必须大于10分钟，即时间差必须大于0分钟
            if (diffInMinutes <= 0) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.scheduledStartTimeAfterTenMinutes'),
              );
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'scheduledEndTime',
      type: RuleType.cover,
      // 计划结束时间必须大于开始时间，精确到分钟
      rules: [
        {
          required: true,
          async validator(_rule: any, value: any) {
            // 编辑模式下该字段禁用、沿用已保存值,不再校验(与开始时间同理)
            if (type.value === ActionEnum.EDIT) {
              return Promise.resolve();
            }
            if (!value) {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.scheduledEndTimeRequired'));
            }
            // 通过 getFieldsValue 获取表单的所有值
            const model = await getFieldsValue();
            if (!model.scheduledStartTime) {
              // 如果还没有选择开始时间，不验证结束时间（因为开始时间是必填的）
              return Promise.resolve();
            }

            const startTime = dateUtil(model.scheduledStartTime).startOf('minute');
            const endTime = dateUtil(value).startOf('minute');

            // 必须严格大于开始时间，精确到分钟
            // 使用 isAfter 确保严格大于
            if (!endTime.isAfter(startTime)) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.scheduledEndTimeAfterStartTime'),
              );
            }

            // 额外检查：计算时间差（分钟），确保至少大于0分钟
            const diffInMinutes = endTime.diff(startTime, 'minute');
            if (diffInMinutes <= 0) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.scheduledEndTimeAfterStartTime'),
              );
            }

            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'deviceUpgradeTimeout',
      type: RuleType.cover,
      // 设备升级超时时间范围验证（非必填，但有值时必须在5-1440之间，不能为负数）
      rules: [
        {
          async validator(_rule: any, value: any) {
            if (value !== null && value !== undefined && value !== '') {
              if (typeof value !== 'number') {
                return Promise.reject(
                  t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeoutRangeValidation'),
                );
              }
              // 不能为负数
              if (value < 0) {
                return Promise.reject(
                  t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeoutRangeValidation'),
                );
              }
              // 范围验证
              if (value < 5 || value > 1440) {
                return Promise.reject(
                  t('iot.link.ota.otaUpgradeTasks.deviceUpgradeTimeoutRangeValidation'),
                );
              }
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'upgradeRate',
      type: RuleType.cover,
      // 升级速率验证（必填，范围10-1000，不能为负数）
      rules: [
        {
          required: true,
          async validator(_rule: any, value: any) {
            if (value === null || value === undefined || value === '') {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.upgradeRateRequired'));
            }
            if (typeof value !== 'number') {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.upgradeRateRangeValidation'));
            }
            // 不能为负数
            if (value < 0) {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.upgradeRateRangeValidation'));
            }
            // 范围验证
            if (value < 10 || value > 1000) {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.upgradeRateRangeValidation'));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'maxRetryCount',
      type: RuleType.cover,
      // 最大重试次数验证（范围1-5，不能为负数）
      rules: [
        {
          required: true,
          async validator(_rule: any, value: any) {
            if (value === null || value === undefined || value === '') {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.maxRetryCountTips.rangeValidation'),
              );
            }
            if (typeof value !== 'number') {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.maxRetryCountTips.rangeValidation'),
              );
            }
            // 不能为负数
            if (value < 0) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.maxRetryCountTips.rangeValidation'),
              );
            }
            // 范围验证
            if (value < 1 || value > 5) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.maxRetryCountTips.rangeValidation'),
              );
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'retryIntervalMinutes',
      type: RuleType.cover,
      // 重试间隔分钟数验证（必填，最小值1）
      rules: [
        {
          required: true,
          async validator(_rule: any, value: any) {
            if (value === null || value === undefined || value === '') {
              return Promise.reject(t('iot.link.ota.otaUpgradeTasks.retryIntervalMinutesRequired'));
            }
            if (typeof value !== 'number' || value < 1) {
              return Promise.reject(
                t('iot.link.ota.otaUpgradeTasks.retryIntervalMinutesRangeValidation'),
              );
            }
            return Promise.resolve();
          },
        },
      ],
    },
  ];
};
