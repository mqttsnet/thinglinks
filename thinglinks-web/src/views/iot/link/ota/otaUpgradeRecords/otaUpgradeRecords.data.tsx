import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.ota.otaUpgradeRecords.taskId'),
      dataIndex: 'taskId',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.deviceIdentification'),
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.upgradeStatus'),
      dataIndex: 'upgradeStatus',
      slots: { customRender: 'upgradeStatusColumn' },
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.progress'),
      dataIndex: 'progress',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.errorCode'),
      dataIndex: 'errorCode',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.errorMessage'),
      dataIndex: 'errorMessage',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.startTime'),
      dataIndex: 'startTime',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.endTime'),
      dataIndex: 'endTime',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.successDetails'),
      dataIndex: 'successDetails',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.failureDetails'),
      dataIndex: 'failureDetails',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.logDetails'),
      dataIndex: 'logDetails',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.ota.otaUpgradeRecords.createdOrgId'),
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
      label: t('iot.link.ota.otaUpgradeRecords.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.upgradeStatus'),
      field: 'upgradeStatus',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_OTA_TASK_RECORD_STATUS),
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.progress'),
      field: 'progress',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorCode'),
      field: 'errorCode',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorMessage'),
      field: 'errorMessage',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.startTime'),
      field: 'startTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.endTime'),
      field: 'endTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.successDetails'),
      field: 'successDetails',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.failureDetails'),
      field: 'failureDetails',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.logDetails'),
      field: 'logDetails',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 6 },
    },
    {
      field: 'createTimeRange',
      label: t('common.createdTime'),
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
      label: t('iot.link.ota.otaUpgradeRecords.taskId'),
      field: 'taskId',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.startTime'),
      field: 'startTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.endTime'),
      field: 'endTime',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorCode'),
      field: 'errorCode',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.errorMessage'),
      field: 'errorMessage',
      component: 'Input',
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 22 },
    },
    {
      label: t('iot.link.ota.otaUpgradeRecords.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
