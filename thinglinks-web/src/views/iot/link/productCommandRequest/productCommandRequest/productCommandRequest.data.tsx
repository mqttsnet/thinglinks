import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
// 与属性 Edit 共用同一份 datatype 校验工具,设备调试参数填写也复用
import {
  buildHelpMessage,
  TD_NCHAR_MAX,
  thingModelCodeRules,
} from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.serviceId'),
      dataIndex: 'serviceId',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.commandId'),
      dataIndex: 'commandId',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.parameterCode'),
      dataIndex: 'parameterCode',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.parameterName'),
      dataIndex: 'parameterName',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.parameterDescription'),
      dataIndex: 'parameterDescription',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.datatype'),
      dataIndex: 'datatype',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.enumlist'),
      dataIndex: 'enumlist',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.max'),
      dataIndex: 'max',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.maxlength'),
      dataIndex: 'maxlength',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.min'),
      dataIndex: 'min',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.required'),
      dataIndex: 'required',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.step'),
      dataIndex: 'step',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.unit'),
      dataIndex: 'unit',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.productCommandRequest.productCommandRequest.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('common.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    // {
    //   label: t('iot.link.productCommandRequest.productCommandRequest.serviceId'),
    //   field: 'serviceId',
    //   component: 'Input',
    //   colProps: { span: 6 },
    // },
    // {
    //   label: t('iot.link.productCommandRequest.productCommandRequest.commandId'),
    //   field: 'commandId',
    //   component: 'Input',
    //   colProps: { span: 6 },
    // },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterCode'),
      field: 'parameterCode',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 5, xxl: 5 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterName'),
      field: 'parameterName',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 5, xxl: 5 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.datatype'),
      field: 'datatype',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 5, xxl: 5 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_COMMAND_DATA_TYPE),
      },
    },
    {
      field: 'createTimeRange',
      label: t('common.createdTime'),
      component: 'RangePicker',
      colProps: { xs: 24, sm: 24, md: 16, lg: 12, xl: 5, xxl: 5 },
      componentProps: {
        style: { width: '100%' },
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterDescription'),
      field: 'parameterDescription',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 5, xxl: 5 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.enumlist'),
      field: 'enumlist',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.max'),
      field: 'max',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.maxlength'),
      field: 'maxlength',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.min'),
      field: 'min',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.required'),
      field: 'required',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.step'),
      field: 'step',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.unit'),
      field: 'unit',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 6 },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.serviceId'),
      field: 'serviceId',
      component: 'Input',
      defaultValue: '0',
      show: false,
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.commandId'),
      field: 'commandId',
      component: 'Input',
      defaultValue: '0',
      show: false,
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterCode'),
      field: 'parameterCode',
      component: 'Input',
      rules: thingModelCodeRules(),
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterName'),
      field: 'parameterName',
      component: 'Input',
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterDescription'),
      field: 'parameterDescription',
      component: 'Input',
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.datatype'),
      field: 'datatype',
      component: 'ApiSelect',
      // 动态 tooltip:根据当前选中的 datatype 给出范围/格式提示
      helpMessage: ({ values }) => buildHelpMessage(values?.datatype),
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_COMMAND_DATA_TYPE),
      },
      rules: [{ required: true }],
    },
    // 枚举值(仅 string)
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.enumlist'),
      field: 'enumlist',
      component: 'Input',
      ifShow: ({ values }) => values.datatype === 'string',
      helpMessage: t('iot.link.productProperty.productProperty.enumlistHelp'),
      componentProps: { placeholder: 'RED,GREEN,BLUE' },
    },
    // 数值约束(仅 int / decimal)── InputNumber + int 精度联动
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.min'),
      field: 'min',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      helpMessage: ({ values }) => buildHelpMessage(values?.datatype),
      componentProps: ({ formModel }) => ({
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
      }),
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.max'),
      field: 'max',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      helpMessage: t('iot.link.productProperty.productProperty.maxGreaterThanMin'),
      componentProps: ({ formModel }) => ({
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
      }),
    },
    // 字符串长度(仅 string / DateTime / jsonObject)── 上限收紧到 TD_NCHAR_MAX,变长类型必填
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.maxlength'),
      field: 'maxlength',
      component: 'InputNumber',
      ifShow: ({ values }) => ['string', 'DateTime', 'jsonObject'].includes(values.datatype),
      helpMessage: t('iot.link.productProperty.productProperty.help.string', { max: TD_NCHAR_MAX }),
      dynamicRules: ({ values }) => {
        if (!['string', 'DateTime', 'jsonObject'].includes(values?.datatype)) return [];
        return [{ required: true, message: t('common.inputText') as string }];
      },
      componentProps: { style: { width: '100%' }, min: 1, max: TD_NCHAR_MAX, precision: 0 },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.required'),
      field: 'required',
      defaultValue: '1',
      component: 'ApiRadioGroup',
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_REQUIRED),
        isBtn: true,
        isSolid: true,
      },
    },
    // 步长 + 单位(仅 int / decimal)
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.step'),
      field: 'step',
      component: 'InputNumber',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      helpMessage: t('iot.link.productProperty.productProperty.help.step'),
      componentProps: ({ formModel }) => ({
        style: { width: '100%' },
        precision: formModel.datatype === 'int' ? 0 : undefined,
        min: 0,
      }),
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.unit'),
      field: 'unit',
      component: 'Input',
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
