import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { echoMapText } from '/@/utils/echo';

const { t } = useI18n();
// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.group.deviceGroupRel.groupId'),
      dataIndex: 'groupId',
    },
    {
      title: t('iot.link.group.deviceGroupRel.deviceIdentification'),
      dataIndex: 'deviceIdentification',
    },
    {
      title: t('iot.link.group.deviceGroupRel.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.link.group.deviceGroupRel.updatedTime'),
      dataIndex: 'updatedTime',
    },
    {
      title: t('iot.link.group.deviceGroupRel.updatedBy'),
      dataIndex: 'updatedBy',
      customRender: ({ record }) => echoMapText(record, 'updatedBy'),
    },
    {
      title: t('iot.link.group.deviceGroupRel.createdOrgId'),
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
      label: t('iot.link.group.deviceGroupRel.groupId'),
      field: 'groupId',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.group.deviceGroupRel.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.group.deviceGroupRel.remark'),
      field: 'remark',
      component: 'InputTextArea',
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
      label: t('iot.link.group.deviceGroupRel.groupId'),
      field: 'groupId',
      component: 'Input',
    },
    {
      label: t('iot.link.group.deviceGroupRel.deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
    },
    {
      label: t('iot.link.group.deviceGroupRel.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
    {
      label: t('iot.link.group.deviceGroupRel.updatedTime'),
      field: 'updatedTime',
      component: 'DatePicker',
      defaultValue: 'CURRENT_TIMESTAMP',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: { defaultValue: dateUtil('00:00:00', 'HH:mm:ss') },
      },
    },
    {
      label: t('iot.link.group.deviceGroupRel.updatedBy'),
      field: 'updatedBy',
      component: 'Input',
    },
    {
      label: t('iot.link.group.deviceGroupRel.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};
