import { Ref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { Tag, Tooltip, message } from 'ant-design-vue';
import CopyableText from '/@/components/CopyableText';
import { thingModelCodeRules } from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productCommand.productCommand.serviceId'),
      dataIndex: 'serviceId',
    },
    {
      title: t('iot.link.productCommand.productCommand.commandCode'),
      dataIndex: 'commandCode',
      width: 150,
      ellipsis: true,
      customRender: ({ record }) => {
        return <CopyableText text={record.commandCode || ''} />;
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.commandName'),
      dataIndex: 'commandName',
      width: 150,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.commandName}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.commandName}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.description'),
      dataIndex: 'description',
      width: 200,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.description}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.description}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.remark'),
      dataIndex: 'remark',
      width: 150,
      ellipsis: true,
      customRender: ({ record }) => {
        return (
          <Tooltip placement="topLeft" title={record.remark}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {record.remark}
            </span>
          </Tooltip>
        );
      },
    },
    // {
    //   title: t('iot.link.productCommand.productCommand.createdOrgId'),
    //   dataIndex: 'createdOrgId',
    // },
    {
      title: t('iot.link.productCommand.productCommand.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
    {
      title: t('thinglinks.common.updatedTime'),
      dataIndex: 'updatedTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    // {
    //   label: t('iot.link.productCommand.productCommand.serviceId'),
    //   field: 'serviceId',
    //   component: 'Input',
    //   colProps: { span: 6 },
    // },
    {
      label: t('iot.link.productCommand.productCommand.commandCode'),
      field: 'commandCode',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommand.productCommand.commandName'),
      field: 'commandName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.link.productCommand.productCommand.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 6 },
    },
    // {
    //   label: t('iot.link.productCommand.productCommand.remark'),
    //   field: 'remark',
    //   component: 'InputTextArea',
    //   colProps: { span: 6 },
    // },
    // {
    //   field: 'createTimeRange',
    //   label: t('common.createdTime'),
    //   component: 'RangePicker',
    //   colProps: { span: 6 },
    // },
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
      label: t('iot.link.productCommand.productCommand.serviceId'),
      field: 'serviceId',
      component: 'Input',
      show: false,
    },
    {
      label: t('iot.link.productCommand.productCommand.commandCode'),
      field: 'commandCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.commandName'),
      field: 'commandName',
      component: 'Input',
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 22 },
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 22 },
      componentProps: {
        rows: 3,
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.createdOrgId'),
      field: 'createdOrgId',
      component: 'Input',
      show: false,
    },
  ];
};
// 新增页字段
export const addFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      label: t('iot.link.productCommand.productCommand.serviceId'),
      field: 'serviceId',
      component: 'Input',
    },
    {
      label: t('iot.link.productCommand.productCommand.commandCode'),
      field: 'commandCode',
      component: 'Input',
      rules: thingModelCodeRules(),
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.commandName'),
      field: 'commandName',
      component: 'Input',
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.description'),
      field: 'description',
      component: 'Input',
      colProps: { span: 22 },
      componentProps: {
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 22 },
      componentProps: {
        rows: 3,
        placeholder: t('common.inputText'),
      },
    },
    {
      label: t('iot.link.productCommand.productCommand.createdOrgId'),
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
