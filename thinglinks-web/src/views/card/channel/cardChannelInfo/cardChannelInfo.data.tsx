import { Ref } from 'vue';
// import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';
// import { useDict } from '/@/components/Dict';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('card.channel.cardChannelInfo.channelName'),
      dataIndex: 'channelName',
    },
    {
      title: t('card.channel.cardChannelInfo.keyParameter'),
      dataIndex: 'keyParameter',
    },
    {
      title: t('card.channel.cardChannelInfo.officialFlag'),
      dataIndex: 'officialFlag',
      slots: { customRender: 'officialFlag' },
    },
    {
      title: t('card.channel.cardChannelInfo.refreshFlag'),
      dataIndex: 'refreshFlag',
      slots: { customRender: 'refreshFlag' },
    },
    {
      title: t('card.channel.cardChannelInfo.operatorType'),
      dataIndex: 'operatorType',
      slots: { customRender: 'operatorType' },
    },
    {
      title: t('card.channel.cardChannelInfo.provinceName'),
      dataIndex: 'provinceName',
    },
    {
      title: t('card.channel.cardChannelInfo.provinceCode'),
      dataIndex: 'provinceCode',
    },
    {
      title: t('card.channel.cardChannelInfo.appKey'),
      dataIndex: 'appKey',
    },
    {
      title: t('card.channel.cardChannelInfo.secret'),
      dataIndex: 'secret',
    },
    {
      title: t('card.channel.cardChannelInfo.code'),
      dataIndex: 'code',
    },
    {
      title: t('card.channel.cardChannelInfo.appId'),
      dataIndex: 'appId',
    },
    {
      title: t('card.channel.cardChannelInfo.password'),
      dataIndex: 'password',
    },
    {
      title: t('card.channel.cardChannelInfo.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('card.channel.cardChannelInfo.channelType'),
      dataIndex: 'channelType',
      slots: { customRender: 'channelType' },
    },
    {
      title: t('card.channel.cardChannelInfo.extendParams'),
      dataIndex: 'extendParams',
    },
    {
      title: t('card.channel.cardChannelInfo.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('card.channel.cardChannelInfo.createdOrgId'),
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
      label: t('card.channel.cardChannelInfo.officialFlag'),
      field: 'officialFlag',
      colProps: { span: 6 },
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_OFFICIAL_FLAG),
      },
    },
    {
      label: t('card.channel.cardChannelInfo.refreshFlag'),
      field: 'refreshFlag',
      component: 'RadioButtonGroup',
      colProps: { span: 6 },
      labelWidth: '120px',
      componentProps: {
        options: [
          { label: t('card.channel.cardChannelInfo.selectYes'), value: 1 },
          { label: t('card.channel.cardChannelInfo.selectNo'), value: 0 },
        ],
      },
    },
    {
      label: t('card.channel.cardChannelInfo.operatorType'),
      field: 'operatorType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_OPERATOR),
      },
    },
    {
      label: t('card.channel.cardChannelInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_STATUS),
      },
    },
    {
      label: t('card.channel.cardChannelInfo.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_TYPE),
      },
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
      label: t('card.channel.cardChannelInfo.channelName'),
      field: 'channelName',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.keyParameter'),
      field: 'keyParameter',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.officialFlag'),
      field: 'officialFlag',
      colProps: { span: 12 },
      component: 'ApiSelect',
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_OFFICIAL_FLAG),
      },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.refreshFlag'),
      field: 'refreshFlag',
      component: 'RadioGroup',
      colProps: { span: 12 },
      labelWidth: '100px',
      componentProps: {
        options: [
          { label: t('card.channel.cardChannelInfo.selectYes'), value: 1 },
          { label: t('card.channel.cardChannelInfo.selectNo'), value: 0 },
        ],
      },
      defaultValue: 0,
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.operatorType'),
      field: 'operatorType',
      component: 'ApiSelect',
      colProps: { span: 12 },
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_OPERATOR),
      },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.status'),
      field: 'status',
      component: 'ApiSelect',
      colProps: { span: 12 },
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_STATUS),
      },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      colProps: { span: 12 },
      componentProps: {
        allowClear: false,
        stringToNumber: true,
        ...dictComponentProps(DictEnum.CARD_CHANNEL_TYPE),
      },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.provinceName'),
      field: 'provinceName',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.provinceCode'),
      field: 'provinceCode',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.appKey'),
      field: 'appKey',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.secret'),
      field: 'secret',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.code'),
      field: 'code',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.appId'),
      field: 'appId',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.password'),
      field: 'password',
      component: 'InputPassword',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.extendParams'),
      field: 'extendParams',
      component: 'Input',
      colProps: { span: 12 },
      required: true,
    },
    {
      label: t('card.channel.cardChannelInfo.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 12 },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
