import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { DictEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { thingModelCodeRules } from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();

// 产品基本信息表单 Schema
export const getProductFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.product.product.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.product.product.productName'),
      field: 'productName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.productName'),
      },
    },
    {
      label: t('iot.link.product.product.productType'),
      field: 'productType',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TYPE),
      },
    },
    {
      label: t('iot.link.product.product.manufacturerId'),
      field: 'manufacturerId',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.manufacturerId'),
      },
    },
    {
      label: t('iot.link.product.product.manufacturerName'),
      field: 'manufacturerName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.manufacturerName'),
      },
    },
    {
      label: t('iot.link.product.product.model'),
      field: 'model',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.model'),
      },
    },
    {
      label: t('iot.link.product.product.dataFormat'),
      field: 'dataFormat',
      component: 'ApiSelect',
      required: true,
      defaultValue: 'JSON',
      colProps: { span: 8 },
      componentProps: {
        disabled: true,
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_DATA_FORMAT),
      },
    },
    {
      label: t('iot.link.product.product.deviceType'),
      field: 'deviceType',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.deviceType'),
      },
    },
    {
      label: t('iot.link.product.product.protocolType'),
      field: 'protocolType',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_PROTOCOL_TYPE),
      },
    },
    {
      label: t('iot.link.product.product.productStatus'),
      field: 'productStatus',
      component: 'ApiSelect',
      defaultValue: '0',
      colProps: { span: 8 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS),
      },
    },
    {
      label: t('iot.link.product.product.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.product.product.remark'),
        autoSize: { minRows: 2, maxRows: 5 },
      },
    },
  ];
};

// 服务表单 Schema
export const getServiceFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productService.productService.serviceCode'),
      field: 'serviceCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productService.productService.serviceCode'),
      },
    },
    {
      label: t('iot.link.productService.productService.serviceName'),
      field: 'serviceName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productService.productService.serviceName'),
      },
    },
    {
      label: t('iot.link.productService.productService.serviceType'),
      field: 'serviceType',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps('LINK_PRODUCT_SERVICE_TYPE'),
      },
    },
    {
      label: t('iot.link.productService.productService.serviceStatus'),
      field: 'serviceStatus',
      component: 'ApiSelect',
      defaultValue: '0',
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_STATUS),
      },
    },
    {
      label: t('iot.link.productService.productService.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productService.productService.description'),
      },
    },
    {
      label: t('iot.link.productService.productService.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productService.productService.remark'),
        autoSize: { minRows: 2, maxRows: 5 },
      },
    },
  ];
};

// 属性表单 Schema
export const getPropertyFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productProperty.productProperty.propertyCode'),
      field: 'propertyCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productProperty.productProperty.propertyCode'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.propertyName'),
      field: 'propertyName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productProperty.productProperty.propertyName'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.datatype'),
      field: 'datatype',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.icon'),
      field: 'icon',
      component: 'Upload',
      colProps: { span: 8 },
      componentProps: ({ formActionType }) => {
        return {
          isDef: false,
          multiple: false,
          maxNumber: 1,
          accept: ['.png'],
          uploadParams: { bizType: FileBizTypeEnum.BASE_LINK_PRODUCT_ICON },
          resultField: 'id',
          onChange: async (file) => {
            await formActionType.setFieldsValue({ icon: [file[0]] });
            await formActionType.validateFields(['icon']);
          },
        };
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productProperty.productProperty.description'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.enumlist'),
      field: 'enumlist',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['string'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return values.datatype === 'string' ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.enumlist'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.min'),
      field: 'min',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.min'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.max'),
      field: 'max',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.max'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.maxlength'),
      field: 'maxlength',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['string', 'DateTime'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['string', 'DateTime'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productProperty.productProperty.maxlength'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.method'),
      field: 'method',
      component: 'ApiRadioGroup',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.ACCESS_MODE),
        isBtn: true,
        buttonStyle: 'solid',
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.required'),
      field: 'required',
      component: 'ApiRadioGroup',
      required: true,
      defaultValue: '1',
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_REQUIRED),
        isBtn: true,
        buttonStyle: 'solid',
        isBtn: true,
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.step'),
      field: 'step',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.step'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.unit'),
      field: 'unit',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.unit'),
      },
    },
    {
      label: t('iot.link.productProperty.productProperty.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productProperty.productProperty.remark'),
      },
    },
  ];
};

// 命令表单 Schema
export const getCommandFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productCommand.productCommand.commandCode'),
      field: 'commandCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommand.productCommand.commandCode'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.commandName'),
      field: 'commandName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommand.productCommand.commandName'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommand.productCommand.description'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder: t('common.inputText') + t('iot.link.productCommand.productCommand.remark'),
      },
    },
  ];
};

// 请求参数表单 Schema
export const getRequestFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterCode'),
      field: 'parameterCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandRequest.productCommandRequest.parameterCode'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterName'),
      field: 'parameterName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandRequest.productCommandRequest.parameterName'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.parameterDescription'),
      field: 'parameterDescription',
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandRequest.productCommandRequest.parameterDescription'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.datatype'),
      field: 'datatype',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.required'),
      field: 'required',
      component: 'ApiRadioGroup',
      required: true,
      defaultValue: '1',
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_REQUIRED),
        isBtn: true,
        buttonStyle: 'solid',
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.step'),
      field: 'step',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandRequest.productCommandRequest.step'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.enumlist'),
      field: 'enumlist',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandRequest.productCommandRequest.enumlist'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.min'),
      field: 'min',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandRequest.productCommandRequest.min'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.max'),
      field: 'max',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandRequest.productCommandRequest.max'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.maxlength'),
      field: 'maxlength',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['string'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['string'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandRequest.productCommandRequest.maxlength'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.unit'),
      field: 'unit',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandRequest.productCommandRequest.unit'),
      },
    },
    {
      label: t('iot.link.productCommandRequest.productCommandRequest.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandRequest.productCommandRequest.remark'),
      },
    },
  ];
};

// 响应参数表单 Schema
export const getResponseFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.parameterCode'),
      field: 'parameterCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.parameterCode'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.parameterName'),
      field: 'parameterName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.parameterName'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.parameterDescription'),
      field: 'parameterDescription',
      component: 'Input',
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.parameterDescription'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.datatype'),
      field: 'datatype',
      component: 'ApiSelect',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.required'),
      field: 'required',
      component: 'ApiRadioGroup',
      required: true,
      defaultValue: '1',
      colProps: { span: 8 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_SERVICE_REQUIRED),
        isBtn: true,
        buttonStyle: 'solid',
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.step'),
      field: 'step',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandResponse.productCommandResponse.step'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.enumlist'),
      field: 'enumlist',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.enumlist'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.min'),
      field: 'min',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandResponse.productCommandResponse.min'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.max'),
      field: 'max',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['int', 'decimal'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['int', 'decimal'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandResponse.productCommandResponse.max'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.maxlength'),
      field: 'maxlength',
      component: 'Input',
      colProps: { span: 8 },
      ifShow: ({ values }) => ['string'].includes(values.datatype),
      dynamicRules: ({ values }) => {
        return ['string'].includes(values.datatype) ? [{ required: true }] : [];
      },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.maxlength'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.unit'),
      field: 'unit',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: {
        placeholder:
          t('common.inputText') + t('iot.link.productCommandResponse.productCommandResponse.unit'),
      },
    },
    {
      label: t('iot.link.productCommandResponse.productCommandResponse.remark'),
      field: 'remark',
      component: 'InputTextArea',
      labelCol: { span: 3 },
      colProps: { span: 24 },
      componentProps: {
        placeholder:
          t('common.inputText') +
          t('iot.link.productCommandResponse.productCommandResponse.remark'),
      },
    },
  ];
};

// 创建默认的服务数据
export const createDefaultService = (createdOrgId?: string) => ({
  serviceCode: '',
  serviceName: '',
  serviceType: '',
  serviceStatus: '0',
  description: '',
  remark: '',
  properties: [createDefaultProperty(createdOrgId)],
  commands: [createDefaultCommand(createdOrgId)],
});

// 创建默认的属性数据
export const createDefaultProperty = (createdOrgId?: string) => ({
  createdOrgId: createdOrgId,
  datatype: '',
  description: '',
  enumlist: '',
  icon: '',
  max: '',
  maxlength: '',
  method: '',
  min: '',
  propertyCode: '',
  propertyName: '',
  remark: '',
  required: '1',
  serviceId: null,
  step: '',
  unit: '',
});

// 创建默认的命令数据
export const createDefaultCommand = (createdOrgId?: string) => ({
  commandCode: '',
  commandName: '',
  createdOrgId: createdOrgId,
  description: '',
  remark: '',
  requests: [createDefaultRequest(createdOrgId)],
  responses: [createDefaultResponse(createdOrgId)],
  serviceId: null,
});

// 创建默认的请求参数数据
export const createDefaultRequest = (createdOrgId?: string) => ({
  commandId: null,
  createdOrgId: createdOrgId,
  datatype: '',
  enumlist: '',
  max: '',
  maxlength: '',
  min: '',
  parameterCode: '',
  parameterDescription: '',
  parameterName: '',
  remark: '',
  required: '1',
  serviceId: null,
  step: '',
  unit: '',
});

// 创建默认的响应参数数据
export const createDefaultResponse = (createdOrgId?: string) => ({
  commandId: null,
  createdOrgId: createdOrgId,
  datatype: '',
  enumlist: '',
  max: '',
  maxlength: '',
  min: '',
  parameterCode: '',
  parameterDescription: '',
  parameterName: '',
  remark: '',
  required: '1',
  serviceId: null,
  step: '',
  unit: '',
});

// 创建默认的表单状态
export const createDefaultFormState = (createdOrgId?: string) => ({
  appId: '',
  deviceType: '',
  manufacturerId: '',
  manufacturerName: '',
  model: '',
  productName: '',
  protocolType: '',
  remark: '',
  productStatus: '0',
  productType: '',
  dataFormat: 'JSON',
  services: [createDefaultService(createdOrgId)],
});
