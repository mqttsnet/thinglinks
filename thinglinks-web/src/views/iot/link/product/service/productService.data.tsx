import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
import { thingModelCodeRules } from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productService.productService.productId'),
      dataIndex: 'productId',
    },
    {
      title: t('iot.link.productService.productService.serviceCode'),
      dataIndex: 'serviceCode',
    },
    {
      title: t('iot.link.productService.productService.serviceName'),
      dataIndex: 'serviceName',
    },
    {
      title: t('iot.link.productService.productService.serviceType'),
      dataIndex: 'serviceType',
    },
    {
      title: t('iot.link.productService.productService.serviceStatus'),
      dataIndex: 'serviceStatus',
    },
    {
      title: t('iot.link.productService.productService.description'),
      dataIndex: 'description',
    },
    {
      title: t('iot.link.productService.productService.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.productService.productService.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('iot.link.productService.productService.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productService.productService.productId'),
      field: 'productId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.serviceCode'),
      field: 'serviceCode',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.serviceName'),
      field: 'serviceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.serviceType'),
      field: 'serviceType',
      component: 'ApiSelect',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.serviceStatus'),
      field: 'serviceStatus',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productService.productService.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 6 },
    },
    {
      field: 'createTimeRange',
      label: t('iot.link.productService.productService.createdTime'),
      component: 'RangePicker',
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
      label: t('iot.link.productService.productService.productId'),
      field: 'productId',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.productService.productService.serviceCode'),
      field: 'serviceCode',
      component: 'Input',
      helpMessage: t('iot.link.productService.productService.helpMessage.serviceCode'),
      rules: thingModelCodeRules(),
    },
    {
      label: t('iot.link.productService.productService.serviceName'),
      field: 'serviceName',
      component: 'Input',
      helpMessage: t('iot.link.productService.productService.serviceName'),
      required: true,
    },
    {
      label: t('iot.link.productService.productService.serviceType'),
      field: 'serviceType',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_TYPE),
      },
      helpMessage: t('iot.link.productService.productService.serviceType'),
      required: true,
    },
    {
      label: t('iot.link.productService.productService.serviceStatus'),
      field: 'serviceStatus',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS),
      },
      helpMessage: t('iot.link.productService.productService.serviceStatus'),
      required: true,
    },
    {
      label: t('iot.link.productService.productService.description'),
      field: 'description',
      component: 'Input',
      helpMessage: t('iot.link.productService.productService.helpMessage.description'),
      colProps: { span: 22 },
      required: true,
    },
    {
      label: t('iot.link.productService.productService.remark'),
      field: 'remark',
      component: 'InputTextArea',
      helpMessage: t('iot.link.productService.productService.remark'),
      colProps: { span: 22 },
    },
    {
      label: t('iot.link.productService.productService.createdOrgId'),
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
