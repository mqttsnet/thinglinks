import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';

const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.device.group.groupName'),
      dataIndex: 'groupName',
      width: 200,
    },
    {
      title: t('video.device.group.groupType'),
      dataIndex: 'echoMap.groupType',
      width: 120,
    },
    {
      title: t('video.device.group.sortOrder'),
      dataIndex: 'sortOrder',
      width: 80,
    },
    {
      title: t('video.device.group.enable'),
      dataIndex: 'enable',
      width: 80,
      customRender: ({ text }) => {
        return text
          ? t('thinglinks.common.yes')
          : t('thinglinks.common.no');
      },
    },
    {
      title: t('video.device.group.remark'),
      dataIndex: 'remark',
      width: 200,
    },
    {
      title: t('thinglinks.common.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
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
      field: 'parentId',
      label: 'parentId',
      component: 'Input',
      show: false,
    },
    {
      label: t('video.device.group.groupName'),
      field: 'groupName',
      component: 'Input',
      required: true,
      helpMessage: t('video.device.group.helpMessage.groupName'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.device.group.groupType'),
      field: 'groupType',
      component: 'ApiSelect',
      defaultValue: '0',
      helpMessage: t('video.device.group.helpMessage.groupType'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_GROUP_TYPE),
      },
    },
    {
      label: t('video.device.group.sortOrder'),
      field: 'sortOrder',
      component: 'InputNumber',
      defaultValue: 0,
      helpMessage: t('video.device.group.helpMessage.sortOrder'),
      componentProps: {
        min: 0,
        max: 9999,
      },
    },
    {
      label: t('video.device.group.enable'),
      field: 'enable',
      component: 'RadioButtonGroup',
      defaultValue: true,
      helpMessage: t('video.device.group.helpMessage.enable'),
      componentProps: {
        options: [
          { label: t('thinglinks.common.yes'), value: true },
          { label: t('thinglinks.common.no'), value: false },
        ],
      },
    },
    {
      label: t('video.device.group.remark'),
      field: 'remark',
      component: 'InputTextArea',
      helpMessage: t('video.device.group.helpMessage.remark'),
      componentProps: {
        maxlength: 500,
        rows: 3,
      },
    },
  ];
};

export const bindDeviceFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('video.device.group.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      required: true,
      helpMessage: t('video.device.group.helpMessage.deviceIdentification'),
      rules: [
        { required: true, message: t('video.device.group.deviceIdentification') },
        { pattern: /^\d{20}$/, message: t('video.device.group.helpMessage.deviceIdentification') },
      ],
      componentProps: {
        maxlength: 20,
      },
    },
    {
      label: t('video.device.group.channelIdentification'),
      field: 'channelIdentification',
      component: 'Input',
      helpMessage: t('video.device.group.helpMessage.channelIdentification'),
      componentProps: {
        maxlength: 20,
      },
    },
  ];
};
