import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import type { CardField } from '/@/components/BusinessCardList';

const { t } = useI18n();

// 卡片视图字段(flexy 风格;生效类型走右上角徽章、状态走右下角角标,不在此重复)
export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.engine.linkage.ruleIdentification'),
    field: 'ruleIdentification',
    span: 24,
  },
  {
    label: t('iot.link.engine.linkage.remark'),
    field: 'remark',
    span: 24,
  },
  {
    label: t('thinglinks.common.createdTime'),
    field: 'createTime',
    span: 24,
  },
];

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.device.device.id'),
      dataIndex: 'id',
    },
    {
      title: t('iot.link.engine.linkage.ruleName'),
      dataIndex: 'ruleName',
    },
    {
      title: t('iot.link.engine.linkage.ruleIdentification'),
      dataIndex: 'ruleIdentification',
    },
    {
      title: t('iot.link.engine.linkage.effectiveType'),
      dataIndex: 'effectiveType',
      slots: { customRender: 'effectiveType' },
    },
    {
      title: t('iot.link.engine.linkage.remark'),
      dataIndex: 'remark',
      ellipsis: true,
      showSorterTooltip: true,
      customRender: ({ record }) => {
        return record.remark;
      },
    },
    {
      title: t('iot.link.engine.linkage.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.engine.linkage.ruleName'),
      field: 'ruleName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.engine.linkage.ruleIdentification'),
      field: 'ruleIdentification',
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

// 编辑弹窗 ── 基础信息卡(与 Edit.vue 的 flexy 分卡布局一一对应)
export const editBasicFormSchema = (): FormSchema[] => {
  return [
    {
      label: 'ID',
      field: 'id',
      component: 'Input',
      show: false,
    },
    // 注意 appointContent 无实际意义，主要用于编辑提交时保留原始扩展字段
    {
      label: 'appointContent',
      field: 'appointContent',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.engine.linkage.ruleName'),
      field: 'ruleName',
      component: 'Input',
      required: true,
    },
    {
      label: t('iot.link.product.product.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.engine.linkage.status'),
      field: 'status',
      component: 'Switch',
      rules: [
        {
          required: true,
          async validator(_, value) {
            value === 0 || value ? Promise.resolve() : Promise.reject();
          },
        },
      ],
      componentProps: {
        checkedValue: 1,
        unCheckedValue: 0,
      },
    },
    {
      label: t('iot.link.engine.linkage.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      required: true,
      componentProps: {
        maxlength: 500,
        showCount: true,
        autoSize: { minRows: 2, maxRows: 5 },
      },
    },
  ];
};

// 编辑弹窗 ── 生效时间卡(生效类型 + 触发频率 + 周/时段窗口)
export const editEffectiveFormSchema = (weekOptions: any[] = []): FormSchema[] => {
  return [
    {
      label: t('iot.link.engine.linkage.effectiveType'),
      field: 'effectiveType',
      component: 'ApiRadioGroup',
      required: true,
      defaultValue: 0,
      colProps: { span: 24 },
      componentProps: {
        stringToNumber: true,
        ...dictComponentProps(DictEnum.RULE_EFFECTIVE_TYPE),
      },
    },
    {
      label: t('iot.link.engine.linkage.frequency'),
      field: 'frequency',
      component: 'InputNumber',
      rules: [
        {
          required: true,
          async validator(_, value) {
            if (value == null || value <= 0) {
              return Promise.reject(t('iot.link.engine.linkage.mustGreaterThanZero'));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      label: t('iot.link.engine.linkage.week'),
      field: 'week',
      component: 'CheckboxGroup',
      colProps: { span: 24 },
      rules: [
        {
          required: true,
          message: t('common.chooseText') + t('iot.link.engine.linkage.week'),
        },
      ],
      componentProps: {
        options: weekOptions,
      },
      ifShow: ({ values }) => values.effectiveType == 1,
    },
    {
      label: t('iot.link.engine.linkage.startTime'),
      field: 'timeframeStart',
      component: 'TimePicker',
      componentProps: {
        format: 'HH:mm',
        valueFormat: 'HH:mm',
      },
      required: true,
      ifShow: ({ values }) => values.effectiveType == 1,
    },
    {
      label: t('iot.link.engine.linkage.endTime'),
      field: 'timeframeEnd',
      component: 'TimePicker',
      componentProps: {
        format: 'HH:mm',
        valueFormat: 'HH:mm',
      },
      required: true,
      ifShow: ({ values }) => values.effectiveType == 1,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
