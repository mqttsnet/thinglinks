import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { Tag } from 'ant-design-vue';
import { query } from '../../../../../api/iot/rule/alarm/channel';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { DictEnum } from '/@/enums/commonEnum';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.engine.alarm.id'),
      dataIndex: 'id',
    },
    {
      title: t('iot.link.engine.alarm.alarmName'),
      dataIndex: 'alarmName',
    },
    {
      title: t('iot.link.engine.alarm.alarmIdentification'),
      dataIndex: 'alarmIdentification',
    },
    {
      title: t('iot.link.engine.alarm.level'),
      dataIndex: 'level',
      slots: { customRender: 'level' },
    },
    {
      title: t('iot.link.engine.alarm.appId'),
      dataIndex: 'appId',
      ellipsis: true,
      showSorterTooltip: true,
      customRender: ({ record }) => {
        return record.appId;
      },
    },
    {
      title: t('iot.link.engine.alarm.alarmScene'),
      dataIndex: 'alarmScene',
      slots: { customRender: 'alarmScene' },
    },
    {
      title: t('iot.link.engine.alarm.status'),
      dataIndex: 'status',
      slots: { customRender: 'status' },
    },
    {
      title: t('iot.link.engine.alarm.createBy'),
      dataIndex: 'createdBy',
      customRender: ({ record }) => echoMapText(record, 'createdBy'),
    },
    {
      title: t('iot.link.engine.alarm.createTime'),
      dataIndex: 'createdTime',
    },
    {
      title: t('iot.link.engine.alarm.updateBy'),
      dataIndex: 'updatedBy',
      customRender: ({ record }) => echoMapText(record, 'updatedBy'),
    },
    {
      title: t('iot.link.engine.alarm.updateTime'),
      dataIndex: 'updatedTime',
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      field: 'alarmName',
      label: t('iot.link.engine.alarm.alarmName'),
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'alarmScene',
      label: t('iot.link.engine.alarm.alarmScene'),
      component: 'ApiSelect',
      colProps: { span: 6 },
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_SCENE),
      },
    },
    {
      field: 'alarmIdentification',
      label: t('iot.link.engine.alarm.alarmIdentification'),
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'level',
      label: t('iot.link.engine.alarm.level'),
      component: 'ApiSelect',
      colProps: { span: 6 },
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
    },
    {
      field: 'status',
      label: t('iot.link.engine.alarm.status'),
      component: 'ApiSelect',
      colProps: { span: 6 },
      show: true,
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_ALARM_STATUS),
      },
    },
  ];
};

// 编辑页字段
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      label: 'ID',
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.engine.alarm.alarmName'),
      field: 'alarmName',
      component: 'Input',
      show: true,
      rules: [{ required: true }],
    },
    {
      label: t('iot.link.engine.alarm.appId'),
      field: 'appId',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.link.engine.alarm.alarmScene'),
      field: 'alarmScene',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_ALARM_SCENE),
      },
    },
    {
      label: t('iot.link.engine.alarm.status'),
      field: 'status',
      component: 'ApiSelect',
      defaultValue: '0',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_ALARM_STATUS),
      },
    },
    {
      label: t('iot.link.engine.alarm.level'),
      field: 'level',
      component: 'ApiRadioGroup',
      show: true,
      rules: [{ required: true }],
      componentProps: {
        disabled: false,
        allowClear: true,
        ...dictComponentProps(DictEnum.RULE_ALARM_LEVEL),
      },
    },
    {
      label: t('iot.link.engine.alarm.alarmChannel'),
      field: 'alarmChannelIds',
      component: 'ApiSelect',
      show: true,
      rules: [{ required: true, type: 'array' }],
      colProps: { span: 22 },
      componentProps: {
        disabled: false,
        allowClear: true,
        mode: 'multiple',
        api: query,
        labelField: 'channelName',
        valueField: 'id',
        params: {
          status: 1,
        },
      },
    },
    {
      label: t('iot.link.engine.alarm.remark'),
      field: 'remark',
      component: 'InputTextArea',
      show: true,
      rules: [{ required: false }],
      colProps: { span: 22 },
      componentProps: {
        disabled: false,
        // placeholder: `请输入${t('iot.link.engine.alarm.remark')}`,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
