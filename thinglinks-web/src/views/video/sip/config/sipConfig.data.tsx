import { h, Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';

const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.sip.config.configName'),
      dataIndex: 'configName',
      width: 140,
    },
    {
      title: t('video.sip.config.sipId'),
      dataIndex: 'sipId',
      width: 200,
    },
    {
      title: t('video.sip.config.sipDomain'),
      dataIndex: 'sipDomain',
      width: 120,
    },
    {
      title: t('video.sip.config.sipServerAddress'),
      dataIndex: 'sipServerAddress',
      width: 180,
    },
    {
      title: t('video.sip.config.bindIp'),
      dataIndex: 'bindIp',
      width: 160,
    },
    {
      title: t('video.sip.config.isDefault'),
      dataIndex: 'isDefault',
      width: 80,
      customRender: ({ text }) =>
        text === 1
          ? h(Tag, { color: 'gold' }, () => t('video.sip.config.defaultTag'))
          : h(Tag, null, () => '-'),
    },
    {
      title: t('video.sip.config.status'),
      dataIndex: 'status',
      width: 80,
      customRender: ({ text }) =>
        text === 1
          ? h(Tag, { color: 'success' }, () => t('video.sip.config.enabled'))
          : h(Tag, { color: 'default' }, () => t('video.sip.config.disabled')),
    },
    {
      title: t('video.sip.config.createdTime'),
      dataIndex: 'createdTime',
      width: 170,
      sorter: true,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('video.sip.config.configName'),
      field: 'configName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.sip.config.sipId'),
      field: 'sipId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.sip.config.status'),
      field: 'status',
      component: 'Select',
      colProps: { span: 4 },
      componentProps: {
        options: [
          { label: t('video.sip.config.enabled'), value: 1 },
          { label: t('video.sip.config.disabled'), value: 0 },
        ],
        allowClear: true,
      },
    },
  ];
};

export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      label: t('video.sip.config.configName'),
      field: 'configName',
      component: 'Input',
      rules: [{ required: true, message: t('video.sip.config.rule.configName') }],
      colProps: { span: 24 },
    },
    {
      label: t('video.sip.config.sipId'),
      field: 'sipId',
      component: 'Input',
      rules: [
        { required: true, message: t('video.sip.config.rule.sipId') },
        { len: 20, message: t('video.sip.config.rule.sipIdLen') },
        { pattern: /^\d{20}$/, message: t('video.sip.config.rule.sipIdPattern') },
      ],
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.sipIdHelp'),
    },
    {
      label: t('video.sip.config.sipDomain'),
      field: 'sipDomain',
      component: 'Input',
      rules: [{ required: true, message: t('video.sip.config.rule.sipDomain') }],
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.sipDomainHelp'),
    },
    {
      label: t('video.sip.config.sipPassword'),
      field: 'sipPassword',
      component: 'InputPassword',
      rules: [{ required: true, message: t('video.sip.config.rule.sipPassword') }],
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.sipPasswordHelp'),
    },
    {
      label: t('video.sip.config.sipServerAddress'),
      field: 'sipServerAddress',
      component: 'Input',
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.sipServerAddressHelp'),
    },
    {
      label: t('video.sip.config.bindIp'),
      field: 'bindIp',
      component: 'Input',
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.bindIpHelp'),
    },
    {
      label: t('video.sip.config.registerInterval'),
      field: 'registerInterval',
      component: 'InputNumber',
      colProps: { span: 12 },
      helpMessage: t('video.sip.config.registerIntervalHelp'),
    },
    {
      label: t('video.sip.config.isDefaultLabel'),
      field: 'isDefault',
      component: 'RadioButtonGroup',
      defaultValue: 0,
      colProps: { span: 12 },
      componentProps: {
        options: [
          { label: t('video.sip.config.no'), value: 0 },
          { label: t('video.sip.config.yes'), value: 1 },
        ],
      },
      helpMessage: t('video.sip.config.isDefaultHelp'),
    },
    {
      label: t('video.sip.config.status'),
      field: 'status',
      component: 'RadioButtonGroup',
      defaultValue: 1,
      colProps: { span: 12 },
      componentProps: {
        options: [
          { label: t('video.sip.config.enabled'), value: 1 },
          { label: t('video.sip.config.disabled'), value: 0 },
        ],
      },
    },
    {
      label: t('video.sip.config.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      componentProps: { rows: 2 },
    },
  ];
};

export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
