import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { Tooltip } from 'ant-design-vue';
import CopyableText from '/@/components/CopyableText';
import type { CardField } from '/@/components/BusinessCardList';
import { thingModelCodeRules } from '/@/utils/iot/dataTypeValidator';

const { t } = useI18n();

const tableEllipsisStyle = {
  display: 'inline-block',
  maxWidth: '100%',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

const renderTextCell = (value: unknown) => {
  const text = value == null || value === '' ? '--' : String(value);
  return (
    <Tooltip placement="topLeft" title={text === '--' ? '' : text}>
      <span style={tableEllipsisStyle}>{text}</span>
    </Tooltip>
  );
};

/**
 * BusinessCardList 卡片字段配置。
 * 命令没有独立 icon 字段,列表页统一使用默认命令 SVG。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.link.productCommand.productCommand.commandCode'),
    field: 'commandCode',
    span: 24,
  },
  {
    label: t('iot.link.productCommand.productCommand.description'),
    field: 'description',
    span: 24,
  },
  {
    label: t('iot.link.productCommand.productCommand.remark'),
    field: 'remark',
    span: 24,
  },
];

// 列表页字段
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productCommand.productCommand.serviceId'),
      dataIndex: 'serviceId',
      width: 220,
      ellipsis: true,
      customRender: ({ record }) => {
        return <CopyableText text={record.serviceId || ''} style={{ maxWidth: '100%' }} />;
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.commandCode'),
      dataIndex: 'commandCode',
      width: 180,
      ellipsis: true,
      customRender: ({ record }) => {
        return <CopyableText text={record.commandCode || ''} style={{ maxWidth: '100%' }} />;
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.commandName'),
      dataIndex: 'commandName',
      width: 180,
      ellipsis: true,
      customRender: ({ record }) => {
        return renderTextCell(record.commandName);
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.description'),
      dataIndex: 'description',
      width: 260,
      ellipsis: true,
      customRender: ({ record }) => {
        return renderTextCell(record.description);
      },
    },
    {
      title: t('iot.link.productCommand.productCommand.remark'),
      dataIndex: 'remark',
      width: 200,
      ellipsis: true,
      customRender: ({ record }) => {
        return renderTextCell(record.remark);
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
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 8, xxl: 8 },
      componentProps: { allowClear: true },
    },
    {
      label: t('iot.link.productCommand.productCommand.commandName'),
      field: 'commandName',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 8, xxl: 8 },
      componentProps: { allowClear: true },
    },
    {
      label: t('iot.link.productCommand.productCommand.description'),
      field: 'description',
      component: 'Input',
      colProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 8, xxl: 8 },
      componentProps: { allowClear: true },
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
