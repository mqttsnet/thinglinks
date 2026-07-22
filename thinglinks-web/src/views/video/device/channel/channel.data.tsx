import { h, Ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { yesNoComponentProps, dictComponentProps, dictComponentProps2 } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.device.channel.deviceIdentification'),
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('video.device.channel.channelIdentification'),
      dataIndex: 'channelIdentification',
    },
    {
      title: t('video.device.channel.channelNo'),
      dataIndex: 'channelNo',
    },
    {
      title: t('video.device.channel.channelType'),
      dataIndex: ['echoMap', 'channelType'],
    },
    {
      title: t('video.device.channel.channelName'),
      dataIndex: 'channelName',
    },
    {
      title: t('video.device.channel.streamIdentification'),
      dataIndex: 'streamIdentification',
    },
    {
      title: t('video.device.channel.streamType'),
      dataIndex: ['echoMap', 'streamType'],
    },
    {
      title: t('video.device.channel.manufacturer'),
      dataIndex: 'manufacturer',
    },
    {
      title: t('video.device.channel.model'),
      dataIndex: 'model',
    },
    {
      title: t('video.device.channel.onlineStatus'),
      dataIndex: 'onlineStatus',
      width: 80,
      customRender: ({ text }) =>
        text === true || text === 1
          ? h(Tag, { color: 'success' }, () => t('video.device.info.online'))
          : h(Tag, { color: 'default' }, () => t('video.device.info.offline')),
    },
    {
      title: t('video.device.channel.host'),
      dataIndex: 'host',
    },
    {
      title: t('video.device.channel.port'),
      dataIndex: 'port',
    },
    {
      title: t('video.device.channel.hasAudio'),
      dataIndex: 'hasAudio',
    },
    {
      title: t('video.device.channel.ptzType'),
      dataIndex: ['echoMap', 'ptzType'],
    },
    {
      title: t('video.device.channel.ptzCapability'),
      dataIndex: 'ptzCapability',
    },
    {
      title: t('video.device.channel.talkCapability'),
      dataIndex: 'talkCapability',
    },
    {
      title: t('video.device.channel.fullAddress'),
      dataIndex: 'fullAddress',
    },
    {
      title: t('video.device.channel.remark'),
      dataIndex: 'remark',
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
      label: t('video.device.channel.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.channel.channelIdentification'),
      field: 'channelIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.channel.channelName'),
      field: 'channelName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.channel.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps2({ type: DictEnum.VIDEO_DEVICE_CHANNEL_TYPE, extendFirst: true, stringToNumber: true }),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.device.channel.manufacturer'),
      field: 'manufacturer',
      component: 'Input',
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
      label: t('video.device.channel.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      required: true,
    },
    {
      label: t('video.device.channel.channelIdentification'),
      field: 'channelIdentification',
      component: 'Input',
      required: true,
    },
    {
      label: t('video.device.channel.channelNo'),
      field: 'channelNo',
      component: 'InputNumber',
    },
    {
      label: t('video.device.channel.channelType'),
      field: 'channelType',
      component: 'ApiSelect',
      required: true,
      componentProps: {
        ...dictComponentProps2({ type: DictEnum.VIDEO_DEVICE_CHANNEL_TYPE, extendFirst: true, stringToNumber: true }),
      },
    },
    {
      label: t('video.device.channel.channelName'),
      field: 'channelName',
      component: 'Input',
    },
    {
      label: t('video.device.channel.streamIdentification'),
      field: 'streamIdentification',
      component: 'Input',
    },
    {
      label: t('video.device.channel.streamType'),
      field: 'streamType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_STREAM_TYPE),
      },
    },
    {
      label: t('video.device.channel.manufacturer'),
      field: 'manufacturer',
      component: 'Input',
    },
    {
      label: t('video.device.channel.model'),
      field: 'model',
      component: 'Input',
    },
    {
      label: t('video.device.channel.host'),
      field: 'host',
      component: 'Input',
    },
    {
      label: t('video.device.channel.port'),
      field: 'port',
      component: 'InputNumber',
    },
    {
      label: t('video.device.channel.password'),
      field: 'password',
      component: 'InputPassword',
    },
    {
      label: t('video.device.channel.longitude'),
      field: 'longitude',
      component: 'InputNumber',
    },
    {
      label: t('video.device.channel.latitude'),
      field: 'latitude',
      component: 'InputNumber',
    },
    {
      label: t('video.device.channel.fullAddress'),
      field: 'fullAddress',
      component: 'Input',
    },
    {
      label: t('video.device.channel.provinceCode'),
      field: 'provinceCode',
      component: 'Input',
    },
    {
      label: t('video.device.channel.cityCode'),
      field: 'cityCode',
      component: 'Input',
    },
    {
      label: t('video.device.channel.regionCode'),
      field: 'regionCode',
      component: 'Input',
    },
    {
      label: t('video.device.channel.hasAudio'),
      field: 'hasAudio',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
    },
    {
      label: t('video.device.channel.ptzType'),
      field: 'ptzType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps2({ type: DictEnum.VIDEO_DEVICE_CHANNEL_PTZ_TYPE, extendFirst: true, stringToNumber: true }),
      },
    },
    {
      label: t('video.device.channel.ptzCapability'),
      field: 'ptzCapability',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
    },
    {
      label: t('video.device.channel.talkCapability'),
      field: 'talkCapability',
      component: 'RadioGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
    },
    {
      label: t('video.device.channel.secrecy'),
      field: 'secrecy',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps2({ type: DictEnum.VIDEO_DEVICE_SECRECY, extendFirst: true, stringToNumber: true }),
      },
    },
    {
      label: t('video.device.channel.extendParams'),
      field: 'extendParams',
      component: 'Input',
    },
    {
      label: t('video.device.channel.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
