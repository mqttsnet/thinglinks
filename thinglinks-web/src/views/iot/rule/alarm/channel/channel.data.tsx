import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { ActionEnum } from '/@/enums/commonEnum';
import { DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.engine.channel.id'),
      dataIndex: 'id',
    },
    {
      title: t('iot.link.engine.channel.channelName'),
      dataIndex: 'channelName',
    },
    {
      title: t('iot.link.engine.channel.channelType'),
      dataIndex: 'channelType',
      slots: { customRender: 'channelType' },
    },
    {
      title: t('iot.link.engine.channel.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.engine.channel.channelStatus'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('iot.link.engine.channel.createdBy'),
      dataIndex: 'createdBy',
      customRender: ({ record }) => echoMapText(record, 'createdBy'),
    },
    {
      title: t('iot.link.engine.channel.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      title: t('iot.link.engine.channel.updatedBy'),
      dataIndex: 'updatedBy',
      customRender: ({ record }) => echoMapText(record, 'updatedBy'),
    },
    {
      title: t('iot.link.engine.channel.updatedTime'),
      dataIndex: 'updatedTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.link.engine.channel.channelName'),
      field: 'channelName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'channelType',
      label: t('iot.link.engine.channel.channelType'),
      component: 'ApiSelect',
      colProps: { span: 6 },
      show: true,
      componentProps: {
        withIconMap: {
          '0': 'dingding',
          '1': 'qiyeweixin',
          '2': 'feishu',
        },
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_TYPE),
      },
    },
    {
      field: 'status',
      label: t('iot.link.engine.channel.channelStatus'),
      component: 'ApiSelect',
      colProps: { span: 6 },
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_STATUS),
      },
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
      label: t('iot.link.engine.channel.channelName'),
      field: 'channelName',
      rules: [{ required: true }],
      component: 'Input',
    },
    {
      label: t('iot.link.engine.channel.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.engine.channel.status')}`,
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_STATUS),
      },
    },
    {
      field: 'channelType',
      label: t('iot.link.engine.channel.channelType'),
      rules: [{ required: true }],
      component: 'ApiSelect',
      componentProps: {
        disabled: false,
        allowClear: false,
        // placeholder: `请选择${t('iot.link.engine.channel.channelType')}`,
        withIconMap: {
          '0': 'dingding',
          '1': 'qiyeweixin',
          '2': 'feishu',
        },
        ...dictComponentProps(DictEnum.RULE_ALARM_CHANNEL_TYPE),
      },
    },
    {
      field: 'token',
      label: t('iot.link.engine.channel.token'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType !== undefined;
      },
    },
    {
      field: 'appId',
      label: t('iot.link.engine.channel.appId'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == '2';
      },
    },
    {
      field: 'appSecret',
      label: t('iot.link.engine.channel.appSecret'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == '2';
      },
    },

    {
      field: 'serverAddress',
      label: t('iot.link.engine.channel.serverAddress'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
    },
    {
      field: 'port',
      label: t('iot.link.engine.channel.port'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
    },
    {
      field: 'sendPeople',
      label: t('iot.link.engine.channel.sendPeople'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
      componentProps: {
        disabled: false,
        allowClear: false,
        placeholder: t('iot.link.engine.channel.placeholder.userName'),
      },
    },
    {
      field: 'userName',
      label: t('iot.link.engine.channel.userName'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
      componentProps: {
        disabled: false,
        allowClear: false,
        placeholder: t('iot.link.engine.channel.placeholder.userName'),
      },
    },
    {
      field: 'password',
      label: t('iot.link.engine.channel.password'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
    },
    {
      field: 'reginId',
      label: 'ReginId',
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
    },
    {
      field: 'accessKeyld',
      label: 'AccessKeyld',
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return false;
      },
    },
    {
      field: 'secret',
      label: t('iot.link.engine.channel.secret'),
      rules: [{ required: true }],
      component: 'Input',
      show: ({ values }) => {
        return values.channelType == '0';
      },
    },
    {
      field: 'remark',
      label: t('iot.link.engine.channel.remark'),
      component: 'Input',
      show: ({ values }) => {
        return values.channelType;
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
