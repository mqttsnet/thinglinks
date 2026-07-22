import { h, Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';

const { t } = useI18n();

const EVENT_TYPE_KEYS: Record<string, string> = {
  ALARM: 'video.device.subscription.event.ALARM',
  DEVICE_ONLINE: 'video.device.subscription.event.DEVICE_ONLINE',
  DEVICE_OFFLINE: 'video.device.subscription.event.DEVICE_OFFLINE',
  STREAM_CLOSE: 'video.device.subscription.event.STREAM_CLOSE',
};

const RECIPIENT_SCOPE_KEYS: Record<string, string> = {
  SELF: 'video.device.subscription.scope.SELF',
  ORG: 'video.device.subscription.scope.ORG',
  CUSTOM: 'video.device.subscription.scope.CUSTOM',
};

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.device.subscription.subscriptionName'),
      dataIndex: 'subscriptionName',
      width: 180,
    },
    {
      title: t('video.device.subscription.channelType'),
      dataIndex: ['echoMap', 'channelType'],
      width: 100,
    },
    {
      title: t('video.device.subscription.messageTemplate'),
      dataIndex: 'templateCode',
      width: 140,
    },
    {
      title: t('video.device.subscription.eventType'),
      dataIndex: 'eventTypes',
      width: 220,
      customRender: ({ text }) => {
        if (!text) return '-';
        return text
          .split(',')
          .map((s: string) => {
            const key = EVENT_TYPE_KEYS[s.trim()];
            return key ? t(key) : s.trim();
          })
          .join(', ');
      },
    },
    {
      title: t('video.device.subscription.priorityFilter'),
      dataIndex: 'priorityFilter',
      width: 120,
      customRender: ({ text }) => text || t('video.device.subscription.priorityFilterAll'),
    },
    {
      title: t('video.device.subscription.receiverScope'),
      dataIndex: 'recipientScope',
      width: 110,
      customRender: ({ text }) => {
        const key = RECIPIENT_SCOPE_KEYS[text];
        return key ? t(key) : text || '-';
      },
    },
    {
      title: t('video.device.subscription.atAll'),
      dataIndex: 'atAll',
      width: 80,
      customRender: ({ text }) =>
        text === 1
          ? h(Tag, { color: 'blue' }, () => t('video.device.subscription.yes'))
          : h(Tag, { color: 'default' }, () => t('video.device.subscription.no')),
    },
    {
      title: t('video.device.subscription.status'),
      dataIndex: 'status',
      width: 80,
      customRender: ({ text }) =>
        text === 1
          ? h(Tag, { color: 'success' }, () => t('video.device.subscription.enabled'))
          : h(Tag, { color: 'default' }, () => t('video.device.subscription.disabled')),
    },
    {
      title: t('video.device.subscription.createdTime'),
      dataIndex: 'createdTime',
      width: 170,
      sorter: true,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('video.device.subscription.subscriptionName'),
      field: 'subscriptionName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.subscription.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      colProps: { span: 5 },
      componentProps: {
        ...dictComponentProps(DictEnum.NOTIFY_CHANNEL_TYPE),
        allowClear: true,
      },
    },
    {
      label: t('video.device.subscription.status'),
      field: 'status',
      component: 'Select',
      colProps: { span: 4 },
      componentProps: {
        options: [
          { label: t('video.device.subscription.enabled'), value: 1 },
          { label: t('video.device.subscription.disabled'), value: 0 },
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
      label: t('video.device.subscription.subscriptionName'),
      field: 'subscriptionName',
      component: 'Input',
      rules: [{ required: true, message: t('video.device.subscription.rule.subscriptionName') }],
      colProps: { span: 24 },
    },
    {
      label: t('video.device.subscription.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      rules: [{ required: true, message: t('video.device.subscription.rule.channelType') }],
      colProps: { span: 12 },
      componentProps: {
        ...dictComponentProps(DictEnum.NOTIFY_CHANNEL_TYPE),
        allowClear: false,
      },
    },
    {
      label: t('video.device.subscription.messageTemplateCode'),
      field: 'templateCode',
      component: 'Input',
      rules: [{ required: true, message: t('video.device.subscription.rule.templateCode') }],
      colProps: { span: 12 },
      helpMessage: t('video.device.subscription.messageTemplateCodeHelp'),
    },
    {
      label: t('video.device.subscription.status'),
      field: 'status',
      component: 'RadioButtonGroup',
      defaultValue: 1,
      colProps: { span: 12 },
      componentProps: {
        options: [
          { label: t('video.device.subscription.enabled'), value: 1 },
          { label: t('video.device.subscription.disabled'), value: 0 },
        ],
      },
    },
    {
      label: t('video.device.subscription.eventType'),
      field: 'eventTypes',
      component: 'CheckboxGroup',
      rules: [{ required: true, message: t('video.device.subscription.rule.eventTypes') }],
      colProps: { span: 24 },
      componentProps: {
        options: [
          { label: t('video.device.subscription.event.ALARM'), value: 'ALARM' },
          { label: t('video.device.subscription.event.DEVICE_ONLINE'), value: 'DEVICE_ONLINE' },
          { label: t('video.device.subscription.event.DEVICE_OFFLINE'), value: 'DEVICE_OFFLINE' },
          { label: t('video.device.subscription.event.STREAM_CLOSE'), value: 'STREAM_CLOSE' },
        ],
      },
    },
    {
      label: t('video.device.subscription.priorityFilter'),
      field: 'priorityFilter',
      component: 'Input',
      colProps: { span: 12 },
      helpMessage: t('video.device.subscription.priorityFilterHelp'),
      show: ({ values }) => values.eventTypes?.includes?.('ALARM'),
    },
    // ===== 渠道凭证 (按渠道类型动态显示) =====
    {
      label: 'Token',
      field: 'configToken',
      component: 'Input',
      colProps: { span: 12 },
      rules: [{ required: true, message: t('video.device.subscription.rule.token') }],
      show: ({ values }) => !!values.channelType && values.channelType !== 'NOTICE',
    },
    {
      label: 'Secret',
      field: 'configSecret',
      component: 'Input',
      colProps: { span: 12 },
      rules: [{ required: true, message: t('video.device.subscription.rule.secret') }],
      show: ({ values }) => values.channelType === 'DINGTALK',
    },
    {
      label: 'AppId',
      field: 'configAppId',
      component: 'Input',
      colProps: { span: 12 },
      rules: [{ required: true, message: t('video.device.subscription.rule.appId') }],
      show: ({ values }) => values.channelType === 'FEISHU',
    },
    {
      label: 'AppSecret',
      field: 'configAppSecret',
      component: 'Input',
      colProps: { span: 12 },
      rules: [{ required: true, message: t('video.device.subscription.rule.appSecret') }],
      show: ({ values }) => values.channelType === 'FEISHU',
    },
    // ===== 接收人 =====
    {
      label: t('video.device.subscription.receiverScope'),
      field: 'recipientScope',
      component: 'RadioButtonGroup',
      defaultValue: 'SELF',
      colProps: { span: 24 },
      componentProps: {
        options: [
          { label: t('video.device.subscription.scope.SELF'), value: 'SELF' },
          { label: t('video.device.subscription.scope.ORG'), value: 'ORG' },
          { label: t('video.device.subscription.scope.CUSTOM'), value: 'CUSTOM' },
        ],
      },
      show: ({ values }) => values.channelType === 'NOTICE',
    },
    {
      label: t('video.device.subscription.recipientIds'),
      field: 'recipientIds',
      component: 'Input',
      colProps: { span: 24 },
      helpMessage: t('video.device.subscription.recipientIdsHelp'),
      show: ({ values }) => values.recipientScope === 'CUSTOM',
    },
    {
      label: t('video.device.subscription.atAllLabel'),
      field: 'atAll',
      component: 'Switch',
      defaultValue: 0,
      colProps: { span: 12 },
      componentProps: {
        checkedValue: 1,
        unCheckedValue: 0,
      },
      show: ({ values }) =>
        values.channelType === 'DINGTALK' ||
        values.channelType === 'FEISHU' ||
        values.channelType === 'ENTERPRISE_WECHAT',
    },
    // ===== 跳转链接 & 消息模板 =====
    {
      label: t('video.device.subscription.jumpUrlTemplate'),
      field: 'jumpUrlTemplate',
      component: 'Input',
      colProps: { span: 24 },
      helpMessage: t('video.device.subscription.jumpUrlTemplateHelp'),
    },
    {
      label: t('video.device.subscription.msgTemplate'),
      field: 'msgTemplate',
      component: 'InputTextArea',
      colProps: { span: 24 },
      componentProps: {
        rows: 6,
        placeholder: t('video.device.subscription.msgTemplatePlaceholder'),
      },
      helpMessage: t('video.device.subscription.msgTemplateHelp'),
    },
    {
      label: t('video.device.subscription.remark'),
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
