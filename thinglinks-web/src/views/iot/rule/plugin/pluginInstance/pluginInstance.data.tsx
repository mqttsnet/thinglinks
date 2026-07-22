import { Ref, unref } from 'vue';
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
      title: t('iot.rule.plugin.pluginInstance.instanceIdentification'),
      dataIndex: 'instanceIdentification',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.instanceName'),
      dataIndex: 'instanceName',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.applicationName'),
      dataIndex: 'applicationName',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.machineIp'),
      dataIndex: 'machineIp',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.portRangeStart'),
      dataIndex: 'portRangeStart',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.portRangeEnd'),
      dataIndex: 'portRangeEnd',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.extendParams'),
      dataIndex: 'extendParams',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.remark'),
      dataIndex: 'remark',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.createdOrgId'),
      dataIndex: 'createdOrgId',
      customRender: ({ record }) => echoMapText(record, 'createdOrgId'),
    },
    {
      title: t('iot.rule.plugin.pluginInstance.weight'),
      dataIndex: 'weight',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.healthy'),
      dataIndex: 'healthy',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.enabled'),
      dataIndex: 'enabled',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.ephemeral'),
      dataIndex: 'ephemeral',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.clusterName'),
      dataIndex: 'clusterName',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.heartBeatInterval'),
      dataIndex: 'heartBeatInterval',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.heartBeatTimeOut'),
      dataIndex: 'heartBeatTimeOut',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.ipDeleteTimeOut'),
      dataIndex: 'ipDeleteTimeOut',
    },
    {
      title: t('iot.rule.plugin.pluginInstance.machinePort'),
      dataIndex: 'machinePort',
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
      label: t('iot.rule.plugin.pluginInstance.instanceName'),
      field: 'instanceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInstance.applicationName'),
      field: 'applicationName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.plugin.pluginInstance.machinePort'),
      field: 'machinePort',
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
      label: t('iot.rule.plugin.pluginInstance.instanceName'),
      field: 'instanceName',
      component: 'Input',
      rules: [{ required: true }],
    },
    {
      label: t('iot.rule.plugin.pluginInstance.applicationName'),
      field: 'instanceIdentification',
      component: 'ApipluginSelectNodeCard',
      rules: [{ required: true }],
      componentProps: ({ formModel, formActionType }) => {
        return {
          type: 'instanceIdentification',
          onSelect: (value) => {
            console.log(value);
            const { validateFields } = formActionType;
            formModel.instanceIdentification = value?.instanceId;
            validateFields(['instanceId']);
          },
          value: formModel.instanceId,
        };
      },
    },
    {
      label: t('iot.rule.plugin.pluginInstance.portRangeStart'),
      field: 'portRangeStart',
      component: 'Input',
      // defaultValue: '2',
      rules: [{ required: true }],
      // colSlot: 'portRangeStart',
    },
    {
      label: t('iot.rule.plugin.pluginInstance.portRangeEnd'),
      field: 'portRangeEnd',
      component: 'Input',
      // defaultValue: '100',
      rules: [{ required: true }],
    },
    {
      label: t('iot.rule.plugin.pluginInstance.remark'),
      field: 'remark',
      component: 'InputTextArea',
    },
  ];
};

export const detailColumns: BasicColumn[] = [
  {
    title: t('iot.rule.plugin.pluginInstance.disk.dirName'),
    dataIndex: 'dirName',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.sysTypeName'),
    dataIndex: 'sysTypeName',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.typeName'),
    dataIndex: 'typeName',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.total'),
    dataIndex: 'total',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.free'),
    dataIndex: 'free',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.used'),
    dataIndex: 'used',
  },
  {
    title: t('iot.rule.plugin.pluginInstance.disk.usage'),
    dataIndex: 'usage',
  },
];
// 前端自定义表单验证规则
export const customFormSchemaRules = (type: Ref<ActionEnum>): Partial<FormSchemaExt>[] => {
  return [
    {
      field: 'portRangeStart',
      rules: [
        {
          required: true,
          trigger: ['change', 'blur'],
          async validator(_, value) {
            console.log(value);

            if (unref(type) === ActionEnum.EDIT) {
              return Promise.resolve();
            }
            if (value < 0 || value > 65535) {
              return Promise.reject('起始端口号必须在0到65535之间。');
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      field: 'portRangeEnd',
      rules: [
        {
          required: true,
          trigger: ['change', 'blur'],
          validator: async (_, value) => {
            if (value < 0 || value > 65535) {
              return '结束端口号必须在0到65535之间。';
            }
            return Promise.resolve();
          },
        },
      ],
    },
  ];
};
