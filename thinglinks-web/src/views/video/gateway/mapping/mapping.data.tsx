import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps, renderYesNoComponent, yesNoComponentProps } from '/@/utils/thinglinks/common';
const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.gateway.mapping.srcProtocol'),
      dataIndex: ['echoMap', 'srcProtocol'],
      width: 120,
    },
    {
      title: t('video.gateway.mapping.srcDeviceIdentification'),
      dataIndex: 'srcDeviceIdentification',
      width: 200,
    },
    {
      title: t('video.gateway.mapping.gbDeviceId'),
      dataIndex: 'gbDeviceId',
      width: 200,
    },
    {
      title: t('video.gateway.mapping.gbChannelId'),
      dataIndex: 'gbChannelId',
      width: 200,
    },
    {
      title: t('video.gateway.mapping.enable'),
      dataIndex: 'enable',
      width: 80,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.gateway.mapping.autoPush'),
      dataIndex: 'autoPush',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.gateway.mapping.registerStatus'),
      dataIndex: 'registerStatus',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('video.gateway.mapping.lastRegisterTime'),
      dataIndex: 'lastRegisterTime',
      width: 180,
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
      label: t('video.gateway.mapping.srcProtocol'),
      field: 'srcProtocol',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.gateway.mapping.srcDeviceIdentification'),
      field: 'srcDeviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.gateway.mapping.gbDeviceId'),
      field: 'gbDeviceId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.gateway.mapping.enable'),
      field: 'enable',
      component: 'Select',
      colProps: { span: 6 },
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
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
      label: t('video.gateway.mapping.srcProtocol'),
      field: 'srcProtocol',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('video.gateway.mapping.helpMessage.srcProtocol'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL),
      },
    },
    {
      label: t('video.gateway.mapping.srcDeviceIdentification'),
      field: 'srcDeviceIdentification',
      component: 'Input',
      required: true,
      helpMessage: t('video.gateway.mapping.helpMessage.srcDeviceIdentification'),
    },
    {
      label: t('video.gateway.mapping.srcChannelIdentification'),
      field: 'srcChannelIdentification',
      component: 'Input',
      helpMessage: t('video.gateway.mapping.helpMessage.srcChannelIdentification'),
    },
    {
      label: t('video.gateway.mapping.gbDeviceId'),
      field: 'gbDeviceId',
      component: 'Input',
      required: true,
      helpMessage: t('video.gateway.mapping.helpMessage.gbDeviceId'),
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.gateway.mapping.gbChannelId'),
      field: 'gbChannelId',
      component: 'Input',
      required: true,
      helpMessage: t('video.gateway.mapping.helpMessage.gbChannelId'),
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.gateway.mapping.gbPlatformId'),
      field: 'gbPlatformId',
      component: 'Input',
      helpMessage: t('video.gateway.mapping.helpMessage.gbPlatformId'),
    },
    {
      label: t('video.gateway.mapping.enable'),
      field: 'enable',
      component: 'RadioButtonGroup',
      defaultValue: true,
      helpMessage: t('video.gateway.mapping.helpMessage.enable'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.gateway.mapping.autoPush'),
      field: 'autoPush',
      component: 'RadioButtonGroup',
      defaultValue: false,
      helpMessage: t('video.gateway.mapping.helpMessage.autoPush'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.gateway.mapping.remark'),
      field: 'remark',
      component: 'InputTextArea',
      helpMessage: t('video.gateway.mapping.helpMessage.remark'),
      componentProps: {
        maxlength: 500,
        rows: 3,
      },
    },
  ];
};
