import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productTopic.productTopic.topicType'),
      dataIndex: ['echoMap', 'topicType'],
      width: 100,
      fixed: 'left',
    },
    {
      title: t('iot.link.productTopic.productTopic.functionType'),
      dataIndex: ['echoMap', 'functionType'],
      width: 100,
    },
    {
      title: t('iot.link.productTopic.productTopic.topic'),
      dataIndex: 'topic',
      ellipsis: true,
      showSorterTooltip: true,
      enableCopy: true,
      enableTooltip: true,
      tooltipPlacement: 'topLeft',
      minWidth: 400,
    },
    {
      title: t('iot.link.productTopic.productTopic.publisher'),
      dataIndex: ['echoMap', 'publisher'],
      width: 120,
    },
    {
      title: t('iot.link.productTopic.productTopic.subscriber'),
      dataIndex: ['echoMap', 'subscriber'],
      width: 120,
    },
    {
      title: t('iot.link.productTopic.productTopic.remark'),
      dataIndex: 'remark',
      ellipsis: true,
      showSorterTooltip: true,
      enableTooltip: true,
      width: 200,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.productTopic.productTopic.topicType'),
      field: 'topicType',
      component: 'ApiSelect',
      defaultValue: '',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TOPIC_TYPE),
        allowClear: true,
        placeholder: `请选择${t('iot.link.productTopic.productTopic.topicType')}`,
      },
    },
    {
      label: t('iot.link.productTopic.productTopic.functionType'),
      field: 'functionType',
      component: 'ApiSelect',
      defaultValue: '',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TOPIC_FUNCTION_TYPE),
        allowClear: true,
        placeholder: `请选择${t('iot.link.productTopic.productTopic.functionType')}`,
      },
    },
    {
      label: t('iot.link.device.device.topic'),
      field: 'topic',
      component: 'Input',
      defaultValue: '',
      show: true,
      componentProps: {
        disabled: false,
        allowClear: true,
        placeholder: `请输入${t('iot.link.device.device.topic')}`,
      },
    },
    {
      label: t('iot.link.productTopic.productTopic.publisher'),
      field: 'publisher',
      component: 'ApiSelect',
      defaultValue: '',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TOPIC_PUBLISHER),
        allowClear: true,
        placeholder: `请选择${t('iot.link.productTopic.productTopic.publisher')}`,
      },
    },
    {
      label: t('iot.link.productTopic.productTopic.subscriber'),
      field: 'subscriber',
      component: 'ApiSelect',
      defaultValue: '',
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_PRODUCT_TOPIC_SUBSCRIBER),
        allowClear: true,
        placeholder: `请选择${t('iot.link.productTopic.productTopic.subscriber')}`,
      },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      label: 'ID',
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.device.device.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: true,
        // placeholder: `请输入${t('iot.link.device.device.deviceIdentification')}`,
      },
    },
    {
      label: t('iot.link.device.device.actionType'),
      field: 'actionType',
      component: 'Select',
      defaultValue: 0,
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.actionType')}`,
        options: [
          {
            label: '上线',
            value: 0,
            key: 0,
          },
          {
            label: '离线',
            value: 1,
            key: 1,
          },
        ],
      },
    },
    {
      label: t('iot.link.device.device.status'),
      field: 'status',
      component: 'Select',
      defaultValue: 0,
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.status')}`,
        options: [
          {
            label: '启用',
            value: 0,
            key: 0,
          },
          {
            label: '禁用',
            value: 1,
            key: 1,
          },
        ],
      },
    },
    {
      label: t('iot.link.device.device.userName'),
      field: 'userName',
      component: 'Input',
      show: false,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.userName')}`,
      },
    },
    {
      label: t('iot.link.device.device.password'),
      field: 'password',
      component: 'InputPassword',
      show: false,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.password')}`,
      },
    },
    {
      label: t('iot.link.device.device.deviceStatus'),
      field: 'deviceStatus',
      component: 'Select',
      defaultValue: 0,
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.deviceStatus')}`,
        options: [
          {
            label: '启用',
            value: 1,
            key: '启用',
          },
          {
            label: '禁用',
            value: 2,
            key: 2,
          },
          {
            label: '未激活',
            value: 0,
            key: 0,
          },
        ],
      },
    },
    {
      label: t('iot.link.device.device.encryptMethod'),
      field: 'encryptMethod',
      component: 'Select',
      defaultValue: 0,
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.encryptMethod')}`,
        options: [
          {
            label: '明文传输',
            value: 0,
            key: '明文传输',
          },
          {
            label: 'SM4',
            value: 1,
            key: 1,
          },
          {
            label: 'AES',
            value: 2,
            key: 2,
          },
        ],
      },
    },
    {
      label: t('iot.link.device.device.encryptKey'),
      field: 'encryptKey',
      component: 'Input',
      show: false,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.encryptKey')}`,
      },
    },
    {
      label: t('iot.link.device.device.encryptVector'),
      field: 'encryptVector',
      component: 'Input',
      show: false,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.encryptVector')}`,
      },
    },
    {
      label: t('iot.link.device.device.signKey'),
      field: 'signKey',
      component: 'Input',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.signKey')}`,
      },
    },
    {
      label: t('iot.link.device.device.connector'),
      field: 'connector',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.connector')}`,
      },
    },
    {
      label: t('iot.link.device.device.nodeType'),
      field: 'nodeType',
      component: 'Select',
      defaultValue: 0,
      show: true,
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.device.device.nodeType')}`,
        options: [
          {
            label: '普通设备',
            value: 0,
            key: 0,
          },
          {
            label: '网关设备',
            value: 1,
            key: 1,
          },
          {
            label: '网关子设备',
            value: 2,
            key: 2,
          },
        ],
      },
    },
    {
      label: t('iot.link.device.device.gatewayId'),
      field: 'gatewayId',
      component: 'Input',
      show: false,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.gatewayId')}`,
      },
    },
    {
      label: t('iot.link.device.device.deviceSdkVersion'),
      field: 'deviceSdkVersion',
      component: 'Input',
      defaultValue: 'v1',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.deviceSdkVersion')}`,
      },
    },
    {
      label: t('iot.link.device.device.fwVersion'),
      field: 'fwVersion',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.fwVersion')}`,
      },
    },
    {
      label: t('iot.link.device.device.swVersion'),
      field: 'swVersion',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.swVersion')}`,
      },
    },
    {
      label: t('iot.link.device.device.productId'),
      field: 'productId',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.productId')}`,
      },
    },
    {
      label: t('iot.link.device.device.deviceTags'),
      field: 'deviceTags',
      component: 'Input',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        mode: 'tags',
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.deviceTags')}`,
      },
    },
    {
      label: t('iot.link.device.device.description'),
      field: 'description',
      component: 'Input',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.description')}`,
      },
    },
    {
      label: t('iot.link.device.device.remark'),
      field: 'remark',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: false }],
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.device.device.remark')}`,
      },
    },
    // 废弃
    // {
    //   label: t('iot.link.device.device.clientId'),
    //   dataIndex: 'clientId',
    //   component: 'Input',
    //   componentProps: {
    //     placeholder: `请输入${t('iot.link.device.device.appId')}`,
    //   },
    // },
    // {
    //   label: t('iot.link.device.device.deviceIdentification'),
    //   dataIndex: 'deviceIdentification',
    //   component: 'Input',
    //   componentProps: {
    //     placeholder: `请输入${t('iot.link.device.device.appId')}`,
    //   },
    // },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
