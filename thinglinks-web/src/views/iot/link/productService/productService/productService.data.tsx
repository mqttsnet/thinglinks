import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
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
      title: t('thinglinks.common.createdTime'),
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
      component: 'Input',
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
      label: t('thinglinks.common.createdTime'),
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
    },
    {
      label: t('iot.link.productService.productService.serviceCode'),
      field: 'serviceCode',
      component: 'Input',
      rules: thingModelCodeRules(),
    },
    {
      label: t('iot.link.productService.productService.serviceName'),
      field: 'serviceName',
      component: 'Input',
    },
    {
      label: t('iot.link.productService.productService.serviceType'),
      field: 'serviceType',
      component: 'Input',
    },
    {
      label: t('iot.link.productService.productService.serviceStatus'),
      field: 'serviceStatus',
      component: 'Input',
      defaultValue: '0',
    },
    {
      label: t('iot.link.productService.productService.description'),
      field: 'description',
      component: 'Input',
    },
    {
      label: t('iot.link.productService.productService.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
    {
      label: t('iot.link.productService.productService.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
