import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps, yesNoComponentProps } from '/@/utils/thinglinks/common';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import type { CardField } from '/@/components/BusinessCardList';

const { t } = useI18n();

/**
 * 卡片视图字段配置（BusinessCardList）。
 * 协议 / 方向徽章已通过 statusField + badgeField 渲染，卡片体只展示业务编码 + 健康 + 时间。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.rule.integration.datasource.sourceType'),
    field: 'sourceType',
    dictType: DictEnum.BRIDGE_DATA_SOURCE_TYPE,
    span: 12,
  },
  {
    label: t('iot.rule.integration.datasource.direction'),
    field: 'direction',
    dictType: DictEnum.BRIDGE_DIRECTION,
    span: 12,
  },
  {
    label: t('iot.rule.integration.datasource.dataSourceCode'),
    field: 'dataSourceCode',
    span: 24,
  },
  {
    label: t('iot.rule.integration.datasource.healthStatus'),
    field: 'healthStatus',
    dictType: DictEnum.BRIDGE_HEALTH_STATUS,
    span: 12,
  },
  {
    label: t('thinglinks.common.updatedTime'),
    field: 'updatedTime',
    span: 12,
  },
];

/** 列表页字段 */
export const columns = (): BasicColumn[] => {
  return [
    { title: t('iot.rule.integration.datasource.dataSourceName'), dataIndex: 'dataSourceName' },
    { title: t('iot.rule.integration.datasource.dataSourceCode'), dataIndex: 'dataSourceCode' },
    {
      title: t('iot.rule.integration.datasource.sourceType'),
      dataIndex: 'sourceType',
      slots: { customRender: 'sourceType' },
    },
    {
      title: t('iot.rule.integration.datasource.direction'),
      dataIndex: 'direction',
      slots: { customRender: 'direction' },
    },
    {
      title: t('iot.rule.integration.datasource.healthStatus'),
      dataIndex: 'healthStatus',
      slots: { customRender: 'healthStatus' },
    },
    {
      title: t('iot.rule.integration.datasource.enable'),
      dataIndex: 'enable',
      slots: { customRender: 'enable' },
      width: 100,
    },
    { title: t('iot.rule.integration.datasource.remark'), dataIndex: 'remark' },
    {
      title: t('iot.rule.integration.datasource.createdTime'),
      dataIndex: 'createdTime',
      sorter: true,
      width: 180,
    },
  ];
};

/** 搜索表单 */
export const searchFormSchema = (): FormSchema[] => {
  return [
    {
      label: t('iot.rule.integration.datasource.dataSourceName'),
      field: 'dataSourceName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.integration.datasource.sourceType'),
      field: 'sourceType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_DATA_SOURCE_TYPE),
      },
    },
    {
      label: t('iot.rule.integration.datasource.direction'),
      field: 'direction',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_DIRECTION),
      },
    },
    {
      label: t('iot.rule.integration.datasource.enable'),
      field: 'enable',
      component: 'RadioGroup',
      colProps: { span: 6 },
      componentProps: {
        ...yesNoComponentProps(),
      },
    },
  ];
};

/**
 * 基础信息字段（始终显示，不随 sourceType 变化）。
 *
 * <p>包括：id（隐藏）/ 名称 / 业务编码 / 应用 / 方向 / 协议类型。
 *
 * @param _type 当前操作类型
 */
export const basicEditFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    // ========== 基础信息 ==========
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('iot.rule.integration.datasource.group.basic'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.datasource.dataSourceName'),
      field: 'dataSourceName',
      component: 'Input',
      required: true,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.dataSourceName'),
      componentProps: {
        placeholder: t('iot.rule.integration.datasource.placeholder.dataSourceName'),
        maxlength: 100,
      },
    },
    {
      label: t('iot.rule.integration.datasource.dataSourceCode'),
      field: 'dataSourceCode',
      component: 'Input',
      helpMessage: t('iot.rule.integration.datasource.helpMessage.dataSourceCode'),
      componentProps: {
        placeholder: t('iot.rule.integration.datasource.placeholder.dataSourceCode'),
        maxlength: 100,
      },
      // 业务编码不可改（已创建后是外部系统引用的稳定标识）
      dynamicDisabled: () => _type.value === ActionEnum.EDIT,
    },
    {
      label: t('iot.rule.integration.datasource.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.appId'),
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.rule.integration.datasource.direction'),
      field: 'direction',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.direction'),
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_DIRECTION),
      },
    },
    {
      label: t('iot.rule.integration.datasource.sourceType'),
      field: 'sourceType',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.sourceType'),
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_DATA_SOURCE_TYPE),
        // 协议类型选完会触发 resetSchema 整个重组表单 schema,默认 popup 容器
        // (trigger.parentNode)在 schema re-render 期间 DOM 引用失效,popup 位置
        // 计算错乱。固定走 .ant-modal-body,popup 跟 modal 同步,不受 schema 重组影响。
        getPopupContainer: (trigger: HTMLElement) =>
          (trigger?.closest?.('.ant-modal-body') as HTMLElement) ??
          (trigger?.parentNode as HTMLElement) ??
          document.body,
      },
    },
  ];
};

/**
 * 默认策略 + 高级配置字段（每种 sourceType 都用）。
 *
 * <p>包括：序列化 / qos / 限流 / 重试 / 超时 / 扩展参数 / 备注。
 * sourceType 变化后由 Edit.vue 通过 {@code defaultPolicyMap} 自动填合理默认值。
 */
export const policyEditFormSchema = (): FormSchema[] => {
  return [
    // ========== 默认策略与重试 ==========
    {
      field: 'divider-policy',
      component: 'Divider',
      label: t('iot.rule.integration.datasource.group.policy'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.datasource.serialization'),
      field: 'serialization',
      component: 'ApiSelect',
      defaultValue: 'JSON',
      helpMessage: t('iot.rule.integration.datasource.helpMessage.serialization'),
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_SERIALIZATION),
      },
    },
    {
      label: t('iot.rule.integration.datasource.defaultQos'),
      field: 'defaultQos',
      component: 'InputNumber',
      defaultValue: 1,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.defaultQos'),
      componentProps: { min: 0, max: 2, style: 'width:100%' },
    },
    {
      label: t('iot.rule.integration.datasource.defaultRateLimitQps'),
      field: 'defaultRateLimitQps',
      component: 'InputNumber',
      defaultValue: 0,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.defaultRateLimitQps'),
      componentProps: { min: 0, style: 'width:100%' },
    },
    {
      label: t('iot.rule.integration.datasource.defaultRetryMaxTimes'),
      field: 'defaultRetryMaxTimes',
      component: 'InputNumber',
      defaultValue: 3,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.defaultRetryMaxTimes'),
      componentProps: { min: 0, style: 'width:100%' },
    },
    {
      label: t('iot.rule.integration.datasource.defaultRetryBackoffMs'),
      field: 'defaultRetryBackoffMs',
      component: 'InputNumber',
      defaultValue: 1000,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.defaultRetryBackoffMs'),
      componentProps: { min: 0, style: 'width:100%' },
    },
    {
      label: t('iot.rule.integration.datasource.defaultTimeoutMs'),
      field: 'defaultTimeoutMs',
      component: 'InputNumber',
      defaultValue: 5000,
      helpMessage: t('iot.rule.integration.datasource.helpMessage.defaultTimeoutMs'),
      componentProps: { min: 0, style: 'width:100%' },
    },
    // ========== 高级配置 ==========
    {
      field: 'divider-extras',
      component: 'Divider',
      label: t('iot.rule.integration.datasource.group.extras'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.datasource.extendParams'),
      field: 'extendParams',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.datasource.helpMessage.extendParams'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.integration.datasource.placeholder.extendParams'),
        maxlength: 2000,
        showCount: true,
      },
    },
    {
      label: t('iot.rule.integration.datasource.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.datasource.helpMessage.remark'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.integration.datasource.placeholder.remark'),
        maxlength: 500,
        showCount: true,
      },
    },
  ];
};

/**
 * 编辑表单 schema —— 用于初始化 useForm 的 schemas 字段。
 *
 * <p>仅包含基础信息 + 默认策略；连接参数 / 凭证密钥的 schema 由 {@code Edit.vue}
 * 监听 sourceType 变化后通过 {@code resetSchema} 动态拼装（按 sourceType 取
 * {@code getConnectionSchema} / {@code getCredentialSchema}）。
 *
 * @param _type 当前操作类型
 */
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [...basicEditFormSchema(_type), ...policyEditFormSchema()];
};

/** 自定义校验规则 */
export const customFormSchemaRules = (
  _type: Ref<ActionEnum>,
  _getFieldsValue: () => any,
): Partial<FormSchemaExt>[] => {
  return [];
};
