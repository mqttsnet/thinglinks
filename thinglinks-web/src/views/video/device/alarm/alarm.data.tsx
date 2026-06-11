import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';

const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.device.alarm.deviceIdentification'),
      dataIndex: 'deviceIdentification',
      width: 180,
    },
    {
      title: t('video.device.alarm.channelIdentification'),
      dataIndex: 'channelIdentification',
      width: 160,
    },
    {
      title: t('video.device.alarm.alarmPriority'),
      dataIndex: ['echoMap', 'alarmPriority'],
      width: 100,
    },
    {
      title: t('video.device.alarm.alarmMethod'),
      dataIndex: ['echoMap', 'alarmMethod'],
      width: 100,
    },
    {
      title: t('video.device.alarm.alarmTime'),
      dataIndex: 'alarmTime',
      width: 170,
      sorter: true,
    },
    {
      title: t('video.device.alarm.alarmDescription'),
      dataIndex: 'alarmDescription',
      width: 200,
      ellipsis: true,
    },
    {
      title: t('video.device.alarm.handleStatus'),
      dataIndex: ['echoMap', 'handleStatus'],
      width: 100,
    },
    {
      title: t('video.device.alarm.handleUserId'),
      dataIndex: ['echoMap', 'handleUserId'],
      width: 100,
    },
    {
      title: t('video.device.alarm.handleTime'),
      dataIndex: 'handleTime',
      width: 170,
    },
    {
      title: t('video.device.alarm.createdTime'),
      dataIndex: 'createdTime',
      width: 170,
    },
  ];
};

/** 搜索表单配置 */
export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('video.device.alarm.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.device.alarm.alarmPriority'),
      field: 'alarmPriority',
      component: 'ApiSelect',
      colProps: { span: 4 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ALARM_PRIORITY),
        placeholder: t('video.device.alarm.placeholderAllPriority'),
        allowClear: true,
      },
    },
    {
      label: t('video.device.alarm.handleStatus'),
      field: 'handleStatus',
      component: 'ApiSelect',
      colProps: { span: 4 },
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_DEVICE_ALARM_HANDLE_STATUS),
        placeholder: t('video.device.alarm.placeholderAllStatus'),
        allowClear: true,
      },
    },
    {
      label: t('video.device.alarm.alarmTime'),
      field: '[startTime, endTime]',
      component: 'RangePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
      colProps: { span: 8 },
    },
  ];
};
