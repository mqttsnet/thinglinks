import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { ActionEnum, DictEnum, FileBizTypeEnum } from '/@/enums/commonEnum';
import { useDict } from '/@/components/Dict';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.rule.plugin.pluginInfo.appId'),
      dataIndex: 'appId',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.applicationName'),
      dataIndex: 'applicationName',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.pluginIdentification'),
      dataIndex: 'pluginIdentification',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.pluginCode'),
      dataIndex: 'pluginCode',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.pluginName'),
      dataIndex: 'pluginName',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.version'),
      dataIndex: 'version',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.description'),
      dataIndex: 'description',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.fileId'),
      dataIndex: 'fileId',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.fileSize'),
      dataIndex: 'fileSize',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('iot.rule.plugin.pluginInfo.level'),
      dataIndex: 'level',
      slots: { customRender: 'level' },
    },
    {
      title: t('iot.rule.plugin.pluginInfo.type'),
      dataIndex: 'type',
      slots: { customRender: 'type' },
    },
    {
      title: t('iot.rule.plugin.pluginInfo.runMode'),
      dataIndex: 'runMode',
      slots: { customRender: 'runMode' },
    },
    {
      title: t('iot.rule.plugin.pluginInfo.licenseType'),
      dataIndex: 'licenseType',
      slots: { customRender: 'licenseType' },
    },
    {
      title: t('iot.rule.plugin.pluginInfo.licenseKey'),
      dataIndex: 'licenseKey',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.validUntil'),
      dataIndex: 'validUntil',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.fileHash'),
      dataIndex: 'fileHash',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.scanStatus'),
      dataIndex: 'scanStatus',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.scanReportFileId'),
      dataIndex: 'scanReportFileId',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.scanDate'),
      dataIndex: 'scanDate',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.scanSummary'),
      dataIndex: 'scanSummary',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.extendParams'),
      dataIndex: 'extendParams',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.rule.plugin.pluginInfo.createdOrgId'),
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
      label: t('iot.rule.plugin.pluginInfo.pluginIdentification'),
      field: 'pluginIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInfo.pluginName'),
      field: 'pluginName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_STATUS),
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInfo.level'),
      field: 'level',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_LEVEL),
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInfo.type'),
      field: 'type',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_TYPE),
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInfo.runMode'),
      field: 'runMode',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_RUN_MODE),
      },
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
      label: t('iot.rule.plugin.pluginInfo.pluginName'),
      field: 'pluginName',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
      helpMessage: t('iot.rule.plugin.pluginInfo.pluginName'),
    },
    {
      label: t('iot.link.product.product.appId'),
      field: 'appId',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
      helpMessage: t('iot.link.product.product.appId'),
    },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.applicationName'),
    //   field: 'applicationName',
    //   component: 'Input',
    //   rules: [{ required: true }],
    // },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.pluginIdentification'),
    //   field: 'pluginIdentification',
    //   component: 'Input',
    //   rules: [{ required: true }],
    // },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.pluginCode'),
    //   field: 'pluginCode',
    //   component: 'Input',
    // },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.pluginName'),
    //   field: 'pluginName',
    //   component: 'Input',
    // },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.version'),
    //   field: 'version',
    //   component: 'Input',
    // },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.description'),
    //   field: 'description',
    //   component: 'Input',
    // },
    {
      label: t('iot.rule.plugin.pluginInfo.fileId'),
      field: 'fileId',
      component: 'Upload',
      // colProps: { span: 22 },
      rules: [{ required: true }],
      componentProps: ({ schema, tableAction, formActionType, formModel }) => {
        return {
          isDef: false,
          multiple: false,
          maxSize: 100,
          maxNumber: 1,
          accept: ['.zip,.jar'],
          uploadParams: { bizType: FileBizTypeEnum.BASE_LINK_PLUGIN_INFO_FILEID },
          resultField: 'id',
          onChange: async (file) => {
            console.log(file);

            await formActionType.setFieldsValue({ fileId: [file[0]] });
            await formActionType.validateFields(['fileId']);
          },
        };
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.helpMessage.fileId'),
    },
    // {
    //   label: t('iot.rule.plugin.pluginInfo.fileSize'),
    //   field: 'fileSize',
    //   component: 'Input',
    //   defaultValue: '0.00',
    //   rules: [{ required: true }],
    //   helpMessage: t('iot.rule.plugin.pluginInfo.fileSize'),
    // },
    {
      label: t('iot.rule.plugin.pluginInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      show: false,
      rules: [{ required: true }],
      defaultValue: '0',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_STATUS),
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.status'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.level'),
      field: 'level',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_LEVEL),
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.helpMessage.level'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.type'),
      field: 'type',
      component: 'ApiSelect',
      // defaultValue: '0',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_TYPE),
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.helpMessage.type'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.runMode'),
      field: 'runMode',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_RUN_MODE),
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.helpMessage.runMode'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.licenseType'),
      field: 'licenseType',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_PLUGIN_INFO_LICENSE_TYPE),
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.helpMessage.licenseType'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.licenseKey'),
      field: 'licenseKey',
      component: 'InputTextArea',
      rules: [{ required: true }],
      helpMessage: t('iot.rule.plugin.pluginInfo.licenseKey'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.validUntil'),
      field: 'validUntil',
      component: 'DatePicker',
      rules: [{ required: true }],
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
        // showTime: { defaultValue: dateUtil('00:00:00') },
      },
      helpMessage: t('iot.rule.plugin.pluginInfo.validUntil'),
    },
    {
      label: t('iot.rule.plugin.pluginInfo.remark'),
      field: 'remark',
      component: 'InputTextArea',
      helpMessage: t('iot.rule.plugin.pluginInfo.remark'),
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
