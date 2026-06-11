import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps, dictComponentProps2 } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.ota.otaUpgrades.appId'),
      dataIndex: 'appId',
    },
    {
      title: t('iot.link.ota.otaUpgrades.packageName'),
      dataIndex: 'packageName',
    },
    {
      title: t('iot.link.ota.otaUpgrades.packageType'),
      dataIndex: 'packageType',
      slots: { customRender: 'packageTypeColumn' },
    },
    {
      title: t('iot.link.ota.otaUpgrades.productIdentification'),
      dataIndex: 'productIdentification',
    },
    {
      title: t('iot.link.ota.otaUpgrades.version'),
      dataIndex: 'version',
    },
    {
      title: t('iot.link.ota.otaUpgrades.fileLocation'),
      dataIndex: 'fileLocation',
    },
    {
      title: t('iot.link.ota.otaUpgrades.signMethod'),
      dataIndex: ['echoMap', 'signMethod'],
    },
    {
      title: t('iot.link.ota.otaUpgrades.status'),
      dataIndex: 'status',
      slots: { customRender: 'statusColumn' },
    },
    {
      title: t('iot.link.ota.otaUpgrades.description'),
      dataIndex: 'description',
    },
    {
      title: t('iot.link.ota.otaUpgrades.customInfo'),
      dataIndex: 'customInfo',
    },
    {
      title: t('iot.link.ota.otaUpgrades.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.ota.otaUpgrades.createdOrgId'),
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

export const searchFormSchema = (params: Object): FormSchema[] => {
  const { productIdentification } = params;
  return [
    {
      label: t('iot.link.ota.otaUpgrades.appId'),
      field: 'appId',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageName'),
      field: 'packageName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageType'),
      field: 'packageType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_OTA_PACKAGES_TYPE,
          extendFirst: true,
          stringToNumber: true,
        }),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: {
        allowClear: true,
        readonly: true,
        // placeholder: '请选择',
        onClick: (e) => productIdentification(e.target.value),
        class: 'pointer_input',
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.version'),
      field: 'version',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgrades.status'),
      field: 'status',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_OTA_PACKAGES_STATUS),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.signMethod'),
      field: 'signMethod',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_OTA_PACKAGES_SIGN_METHOD,
          extendFirst: true,
          stringToNumber: true,
        }),
      },
    },
    {
      field: 'createTimeRange',
      label: t('iot.link.ota.otaUpgrades.createdTime'),
      component: 'RangePicker',
      colProps: { span: 6 },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>, callback: any): FormSchema[] => {
  const { productIdentification } = callback;
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.ota.otaUpgrades.appId'),
      field: 'appId',
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageName'),
      field: 'packageName',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgrades.packageType'),
      field: 'packageType',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        disabled: _type.value !== ActionEnum.ADD,
        ...dictComponentProps2({
          type: DictEnum.LINK_OTA_PACKAGES_TYPE,
          extendFirst: true,
          stringToNumber: true,
        }),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      componentProps: {
        allowClear: false,
        readonly: true,
        disabled: _type.value !== ActionEnum.ADD,
        // placeholder: '请选择',
        onClick: (e) => productIdentification(e.target.value),
        class: 'pointer_input',
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.version'),
      field: 'version',
      component: 'Input',
      helpMessage: t('iot.link.ota.otaUpgrades.helpMessage.version'),
    },
    {
      label: t('iot.link.ota.otaUpgrades.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: 0,
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_OTA_PACKAGES_STATUS,
          extendFirst: true,
          stringToNumber: true,
        }),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.fileLocation'),
      field: 'fileLocation',
      component: 'Upload',
      colProps: { span: 22 },
      componentProps: ({ schema, tableAction, formActionType, formModel }) => {
        return {
          isDef: false,
          maxSize: 2048,
          multiple: true,
          accept: ['zip', '.bin', '.dav', '.tar', '.gzip', '.apk', '.gz', '.xz', '.pack', '.deb'],
          uploadParams: { bizType: FileBizTypeEnum.BASE_LINK_OTA_PACK },
          resultField: 'id',
          onChange: async (file) => {
            console.log(111, file);

            await formActionType.setFieldsValue({ fileLocation: [file] });
            await formActionType.validateFields(['fileLocation']);
          },
        };
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.signMethod'),
      field: 'signMethod',
      component: 'ApiSelect',
      defaultValue: 1,
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_OTA_PACKAGES_SIGN_METHOD,
          extendFirst: true,
          stringToNumber: true,
        }),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.description'),
      field: 'description',
      colProps: { span: 22 },
      component: 'InputTextArea',
    },
    {
      label: t('iot.link.ota.otaUpgrades.customInfo'),
      field: 'customInfo',
      defaultValue: '',
      component: 'Input',
      colProps: { span: 22 },
      colSlot: 'customInfo',
      show: true,
    },
    {
      label: t('iot.link.ota.otaUpgrades.remark'),
      field: 'remark',
      colProps: { span: 22 },
      component: 'InputTextArea',
      componentProps: {
        placeHolder: t('iot.link.ota.otaUpgrades.placeholder.descriptionPlaceholder'),
      },
    },
    {
      label: t('iot.link.ota.otaUpgrades.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [
    {
      field: 'fileLocation',
      rules: [
        {
          required: true,
          trigger: ['change', 'blur'],
          validator: async (_, value) => {
            if (!value?.length) {
              return Promise.reject(
                t('common.uploadText') + `${t('iot.link.ota.otaUpgrades.fileLocation')}`,
              );
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'version',
      rules: [
        {
          required: true,
          trigger: ['change', 'blur'],
          message: t('common.filloutText') + `${t('iot.link.ota.otaUpgrades.version')}`,
        },
        {
          pattern: /^\d+\.\d+\.\d+(-[0-9A-Za-z-]+)?(\+[0-9A-Za-z-]+)?$/,
          trigger: ['change', 'blur'],
          message: t('iot.link.ota.otaUpgrades.versionRule'),
        },
      ],
    },
  ];
};
