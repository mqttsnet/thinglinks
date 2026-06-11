import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps, yesNoComponentProps } from '/@/utils/thinglinks/common';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { page as listDataSources } from '/@/api/iot/rule/integration/dataSource';
import type { CardField } from '/@/components/BusinessCardList';

const { t } = useI18n();

/**
 * 卡片视图字段配置（BusinessCardList）。
 * targetHandler 已通过 badgeField 渲染；卡片体只展示编码 / 数据源 / 目标产品 / 时间。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.rule.integration.subscription.sourceCode'),
    field: 'sourceCode',
    span: 24,
  },
  {
    label: t('iot.rule.integration.subscription.dataSourceId'),
    field: 'dataSourceCode',
    span: 12,
  },
  {
    label: t('iot.rule.integration.subscription.targetProductIdentification'),
    field: 'targetProductIdentification',
    span: 12,
  },
  {
    label: t('thinglinks.common.updatedTime'),
    field: 'updatedTime',
    span: 24,
  },
];

export const columns = (): BasicColumn[] => {
  return [
    { title: t('iot.rule.integration.subscription.sourceName'), dataIndex: 'sourceName' },
    { title: t('iot.rule.integration.subscription.sourceCode'), dataIndex: 'sourceCode' },
    {
      title: t('iot.rule.integration.subscription.targetHandler'),
      dataIndex: 'targetHandler',
      slots: { customRender: 'targetHandler' },
    },
    { title: t('iot.rule.integration.subscription.targetProductIdentification'), dataIndex: 'targetProductIdentification' },
    {
      title: t('iot.rule.integration.subscription.enable'),
      dataIndex: 'enable',
      slots: { customRender: 'enable' },
      width: 100,
    },
    { title: t('iot.rule.integration.subscription.lastConsumeOffset'), dataIndex: 'lastConsumeOffset' },
    { title: t('iot.rule.integration.subscription.remark'), dataIndex: 'remark' },
    {
      title: t('iot.rule.integration.subscription.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.rule.integration.subscription.sourceName'),
      field: 'sourceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.integration.subscription.targetHandler'),
      field: 'targetHandler',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: { ...dictComponentProps(DictEnum.BRIDGE_TARGET_HANDLER) },
    },
    {
      label: t('iot.rule.integration.subscription.enable'),
      field: 'enable',
      component: 'RadioGroup',
      colProps: { span: 6 },
      componentProps: { ...yesNoComponentProps() },
    },
  ];
};

export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    { field: 'id', label: 'ID', component: 'Input', show: false },
    // ========== 基础信息 ==========
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('iot.rule.integration.subscription.group.basic'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.subscription.sourceName'),
      field: 'sourceName',
      component: 'Input',
      required: true,
      helpMessage: t('iot.rule.integration.subscription.helpMessage.sourceName'),
      componentProps: {
        placeholder: t('iot.rule.integration.subscription.placeholder.sourceName'),
        maxlength: 100,
      },
    },
    {
      label: t('iot.rule.integration.subscription.sourceCode'),
      field: 'sourceCode',
      component: 'Input',
      helpMessage: t('iot.rule.integration.subscription.helpMessage.sourceCode'),
      componentProps: {
        placeholder: t('iot.rule.integration.subscription.placeholder.sourceCode'),
        maxlength: 100,
      },
      dynamicDisabled: () => _type.value === ActionEnum.EDIT,
    },
    {
      label: t('iot.rule.integration.subscription.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.subscription.helpMessage.appId'),
      componentProps: { ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO) },
    },
    {
      label: t('iot.rule.integration.subscription.dataSourceId'),
      field: 'dataSourceId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.subscription.helpMessage.dataSourceId'),
      componentProps: {
        api: async (params) => {
          // 仅返回 direction 为 20-入站 / 30-双向 的数据源
          const res = await listDataSources({ size: 100, current: 1, model: { enable: true }, ...params } as any);
          return (res?.records ?? [])
            .filter((r: any) => r.direction === '20' || r.direction === '30')
            .map((r: any) => ({
              label: r.dataSourceName + ' (' + r.sourceType + ')',
              value: r.id,
            }));
        },
        immediate: true,
      },
    },
    // ========== 目标配置 ==========
    {
      field: 'divider-target',
      component: 'Divider',
      label: t('iot.rule.integration.subscription.group.target'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.subscription.targetHandler'),
      field: 'targetHandler',
      component: 'ApiSelect',
      required: true,
      defaultValue: 'MQTT_FORWARD',
      helpMessage: t('iot.rule.integration.subscription.helpMessage.targetHandler'),
      componentProps: { ...dictComponentProps(DictEnum.BRIDGE_TARGET_HANDLER) },
    },
    {
      label: t('iot.rule.integration.subscription.targetProductIdentification'),
      field: 'targetProductIdentification',
      component: 'Input',
      // 仅 MQTT_FORWARD 模式必填；其它 handler 留空不校验（不用 ifShow 避免 FormItem 反复挂载/卸载）
      dynamicRules: ({ values }) =>
        values?.targetHandler === 'MQTT_FORWARD'
          ? [{ required: true, message: t('common.rules.require') }]
          : [],
      helpMessage: t('iot.rule.integration.subscription.helpMessage.targetProductIdentification'),
      componentProps: {
        placeholder: t('iot.rule.integration.subscription.placeholder.targetProductIdentification'),
        maxlength: 100,
      },
    },
    {
      label: t('iot.rule.integration.subscription.targetTopicTemplate'),
      field: 'targetTopicTemplate',
      component: 'Input',
      helpMessage: t('iot.rule.integration.subscription.helpMessage.targetTopicTemplate'),
      componentProps: {
        placeholder: t('iot.rule.integration.subscription.placeholder.targetTopicTemplate'),
        maxlength: 500,
      },
    },
    // ========== 字段映射 ==========
    {
      field: 'divider-mapping',
      component: 'Divider',
      label: t('iot.rule.integration.subscription.group.mapping'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.subscription.mappingJson'),
      field: 'mappingJson',
      component: 'InputTextArea',
      required: true,
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.subscription.helpMessage.mappingJson'),
      componentProps: {
        rows: 6,
        placeholder: t('iot.rule.integration.subscription.placeholder.mappingJson'),
        maxlength: 4000,
        showCount: true,
      },
    },
    // ========== 高级配置 ==========
    {
      field: 'divider-extras',
      component: 'Divider',
      label: t('iot.rule.integration.subscription.group.extras'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.subscription.extendParams'),
      field: 'extendParams',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.subscription.helpMessage.extendParams'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.integration.subscription.placeholder.extendParams'),
        maxlength: 2000,
        showCount: true,
      },
    },
    {
      label: t('iot.rule.integration.subscription.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.subscription.helpMessage.remark'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.integration.subscription.placeholder.remark'),
        maxlength: 500,
        showCount: true,
      },
    },
  ];
};

export const customFormSchemaRules = (
  _type: Ref<ActionEnum>,
  _getFieldsValue: () => any,
): Partial<FormSchemaExt>[] => {
  return [];
};
