import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';

const { t } = useI18n();

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('video.record.plan.planName'),
      dataIndex: 'planName',
      width: 200,
    },
    {
      title: t('video.record.plan.planType'),
      dataIndex: 'echoMap.planType',
      width: 120,
    },
    {
      title: t('video.record.plan.recordFormat'),
      dataIndex: 'echoMap.recordFormat',
      width: 120,
    },
    {
      title: t('video.record.plan.segmentDuration'),
      dataIndex: 'segmentDuration',
      width: 130,
    },
    {
      title: t('video.record.plan.retentionDays'),
      dataIndex: 'retentionDays',
      width: 120,
    },
    {
      title: t('video.record.plan.planStatus'),
      dataIndex: 'echoMap.planStatus',
      width: 120,
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
      label: t('video.record.plan.planName'),
      field: 'planName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('video.record.plan.planType'),
      field: 'planType',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_RECORD_PLAN_TYPE),
      },
      colProps: { span: 6 },
    },
    {
      label: t('video.record.plan.planStatus'),
      field: 'planStatus',
      component: 'ApiSelect',
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_RECORD_PLAN_STATUS),
      },
      colProps: { span: 6 },
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
      label: t('video.record.plan.planName'),
      field: 'planName',
      component: 'Input',
      required: true,
      helpMessage: t('video.record.plan.helpMessage.planName'),
      componentProps: {
        maxlength: 64,
      },
    },
    {
      label: t('video.record.plan.planType'),
      field: 'planType',
      component: 'ApiSelect',
      defaultValue: '0',
      helpMessage: t('video.record.plan.helpMessage.planType'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_RECORD_PLAN_TYPE),
      },
    },
    {
      label: t('video.record.plan.mediaIdentification'),
      field: 'mediaIdentification',
      component: 'ApiVideoSelectNodeCard',
      helpMessage: t('video.record.plan.helpMessage.mediaIdentification'),
      colProps: { span: 22 },
    },
    {
      label: t('video.record.plan.recordFormat'),
      field: 'recordFormat',
      component: 'ApiSelect',
      helpMessage: t('video.record.plan.helpMessage.recordFormat'),
      componentProps: {
        ...dictComponentProps(DictEnum.VIDEO_RECORD_FILE_FORMAT),
      },
    },
    {
      label: t('video.record.plan.segmentDuration'),
      field: 'segmentDuration',
      component: 'InputNumber',
      defaultValue: 3600,
      helpMessage: t('video.record.plan.helpMessage.segmentDuration'),
      componentProps: {
        min: 60,
        style: { width: '100%' },
      },
    },
    {
      label: t('video.record.plan.retentionDays'),
      field: 'retentionDays',
      component: 'InputNumber',
      defaultValue: 7,
      helpMessage: t('video.record.plan.helpMessage.retentionDays'),
      componentProps: {
        min: 1,
        style: { width: '100%' },
      },
    },
    {
      label: t('video.record.plan.storagePath'),
      field: 'storagePath',
      component: 'Input',
      helpMessage: t('video.record.plan.helpMessage.storagePath'),
      componentProps: {
        maxlength: 255,
      },
    },
    {
      label: t('video.record.plan.planStatus'),
      field: 'planStatus',
      component: 'ApiRadioGroup',
      defaultValue: '1',
      helpMessage: t('video.record.plan.helpMessage.planStatus'),
      componentProps: {
        isBtn: true,
        ...dictComponentProps(DictEnum.VIDEO_RECORD_PLAN_STATUS),
      },
    },
    {
      label: t('video.record.plan.scheduleRule'),
      field: 'scheduleRule',
      component: 'InputTextArea',
      helpMessage: t('video.record.plan.helpMessage.scheduleRule'),
      componentProps: {
        maxlength: 2000,
        rows: 4,
      },
    },
    {
      label: t('video.record.plan.remark'),
      field: 'remark',
      component: 'InputTextArea',
      helpMessage: t('video.record.plan.helpMessage.remark'),
      componentProps: {
        maxlength: 500,
        rows: 3,
      },
    },
  ];
};
