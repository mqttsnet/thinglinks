import { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps, yesNoComponentProps } from '/@/utils/thinglinks/common';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import { page as listDataSources } from '/@/api/iot/rule/integration/dataSource';
import type { CardField } from '/@/components/BusinessCardList';
import { router } from '/@/router';

const { t } = useI18n();

/** 从当前路由 query 取默认值（数据源详情→引用规则跳转用） */
function queryDefault(key: string): string | undefined {
  try {
    const v = router.currentRoute.value?.query?.[key];
    if (typeof v === 'string' && v.length > 0) return v;
  } catch {
    /* ignore */
  }
  return undefined;
}

/**
 * 修复 Modal 内 Select 选完后会触发 resetSchema 重组表单的字段（如 direction /
 * dataSourceId / ac_targetHandler）的 popup 错位问题。
 *
 * <p>原理：默认 popup 容器是 trigger.parentNode，schema re-render 时 DOM 引用失效，
 * popup 计算位置错乱。固定走 .ant-modal-body，popup 跟 modal 同步定位。
 */
const popupInModalBody = (trigger: HTMLElement): HTMLElement =>
  (trigger?.closest?.('.ant-modal-body') as HTMLElement) ??
  (trigger?.parentNode as HTMLElement) ??
  document.body;

/**
 * 卡片视图字段配置（BusinessCardList）。
 * 方向徽章已通过 badgeField 渲染；卡片体只展示规则编码 / 关联数据源 / 优先级 / 修改时间。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.rule.integration.bridge.ruleCode'),
    field: 'ruleCode',
    span: 24,
  },
  {
    label: t('iot.rule.integration.bridge.dataSourceId'),
    field: 'dataSourceCode',
    span: 12,
  },
  {
    label: t('iot.rule.integration.bridge.priority'),
    field: 'priority',
    span: 12,
  },
  {
    label: t('thinglinks.common.updatedTime'),
    field: 'updatedTime',
    span: 24,
  },
];

/** 列表页字段 */
export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.rule.integration.bridge.ruleName'),
      dataIndex: 'ruleName',
      slots: { customRender: 'ruleName' },
    },
    { title: t('iot.rule.integration.bridge.ruleCode'), dataIndex: 'ruleCode' },
    {
      title: t('iot.rule.integration.bridge.direction'),
      dataIndex: 'direction',
      slots: { customRender: 'direction' },
    },
    {
      title: t('iot.rule.integration.bridge.dataSourceId'),
      dataIndex: 'dataSourceId',
      slots: { customRender: 'dataSourceName' },
    },
    {
      title: t('iot.rule.integration.bridge.priority'),
      dataIndex: 'priority',
      width: 80,
    },
    {
      title: t('iot.rule.integration.bridge.enable'),
      dataIndex: 'enable',
      slots: { customRender: 'enable' },
      width: 100,
    },
    { title: t('iot.rule.integration.bridge.remark'), dataIndex: 'remark' },
    {
      title: t('iot.rule.integration.bridge.createdTime'),
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
      label: t('iot.rule.integration.bridge.ruleName'),
      field: 'ruleName',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.integration.bridge.dataSourceId'),
      field: 'dataSourceId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      defaultValue: queryDefault('dataSourceId'),
      componentProps: {
        // 复用编辑表单同款数据源拉取(全部启用 + 禁用,不过滤,搜索时可以选到任何已建数据源)
        api: async (params) => {
          const res = await listDataSources({
            size: 100,
            current: 1,
            model: {},
            ...params,
          } as any);
          return (res?.records ?? []).map((r: any) => ({
            label: r.dataSourceName + ' (' + r.sourceType + ')',
            value: r.id,
          }));
        },
        immediate: true,
        allowClear: true,
        showSearch: true,
        // filterOption 走前端模糊搜(已加载 100 条,无需后端搜)
        filterOption: (input: string, option: any) =>
          (option?.label ?? '').toLowerCase().indexOf(input.toLowerCase()) >= 0,
      },
    },
    {
      label: t('iot.rule.integration.bridge.direction'),
      field: 'direction',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: { ...dictComponentProps(DictEnum.BRIDGE_DIRECTION) },
    },
    {
      label: t('iot.rule.integration.bridge.enable'),
      field: 'enable',
      component: 'RadioGroup',
      colProps: { span: 6 },
      componentProps: { ...yesNoComponentProps() },
    },
  ];
};

/**
 * 基础信息字段（始终显示，不随 direction / sourceType 变化）。
 *
 * <p>包括：id（隐藏）/ ruleName / ruleCode / appId / direction / dataSourceId。
 * <p>match_config / action_config 字段由 {@code Edit.vue} 监听 direction +
 * 所选 dataSource.sourceType 后通过 {@code resetSchema} 动态拼装。
 *
 * @param _type 当前操作类型
 */
export const basicEditFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    { field: 'id', label: 'ID', component: 'Input', show: false },
    // ========== 基础信息 ==========
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('iot.rule.integration.bridge.group.basic'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.bridge.ruleName'),
      field: 'ruleName',
      component: 'Input',
      required: true,
      helpMessage: t('iot.rule.integration.bridge.helpMessage.ruleName'),
      componentProps: {
        placeholder: t('iot.rule.integration.bridge.placeholder.ruleName'),
        maxlength: 100,
      },
    },
    {
      label: t('iot.rule.integration.bridge.ruleCode'),
      field: 'ruleCode',
      component: 'Input',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.ruleCode'),
      componentProps: {
        placeholder: t('iot.rule.integration.bridge.placeholder.ruleCode'),
        maxlength: 100,
      },
      dynamicDisabled: () => _type.value === ActionEnum.EDIT,
    },
    {
      label: t('iot.rule.integration.bridge.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.bridge.helpMessage.appId'),
      componentProps: { ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO) },
    },
    {
      label: t('iot.rule.integration.bridge.direction'),
      field: 'direction',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.bridge.helpMessage.direction'),
      componentProps: {
        ...dictComponentProps(DictEnum.BRIDGE_DIRECTION),
        // 选完触发 resetSchema 重组表单 → 固定 popup 到 modal body，避免 DOM 重渲染时位置错位
        getPopupContainer: popupInModalBody,
      },
    },
    {
      label: t('iot.rule.integration.bridge.dataSourceId'),
      field: 'dataSourceId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.integration.bridge.helpMessage.dataSourceId'),
      componentProps: {
        api: async (params) => {
          const res = await listDataSources({
            size: 100,
            current: 1,
            model: { enable: true },
            ...params,
          } as any);
          return (res?.records ?? []).map((r: any) => ({
            label: r.dataSourceName + ' (' + r.sourceType + ')',
            value: r.id,
          }));
        },
        immediate: true,
        // 选完触发 resetSchema 重组表单 → 固定 popup 到 modal body
        getPopupContainer: popupInModalBody,
      },
    },
  ];
};

/**
 * 流控 / 重试覆盖字段 + 优先级 + 备注。
 *
 * <p>NULL = 沿用关联数据源的 default_*。
 */
export const overrideEditFormSchema = (): FormSchema[] => {
  return [
    // ========== 流控/重试覆盖（NULL = 沿用数据源默认） ==========
    {
      field: 'divider-fallback',
      component: 'Divider',
      label: t('iot.rule.integration.bridge.group.fallback'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.bridge.qos'),
      field: 'qos',
      component: 'InputNumber',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.qos'),
      componentProps: {
        min: 0,
        max: 2,
        placeholder: t('iot.rule.integration.bridge.placeholder.fallbackNullDefault'),
        style: 'width:100%',
      },
    },
    {
      label: t('iot.rule.integration.bridge.rateLimitQps'),
      field: 'rateLimitQps',
      component: 'InputNumber',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.rateLimitQps'),
      componentProps: {
        min: 0,
        placeholder: t('iot.rule.integration.bridge.placeholder.fallbackNullDefault'),
        style: 'width:100%',
      },
    },
    {
      label: t('iot.rule.integration.bridge.retryMaxTimes'),
      field: 'retryMaxTimes',
      component: 'InputNumber',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.retryMaxTimes'),
      componentProps: {
        min: 0,
        placeholder: t('iot.rule.integration.bridge.placeholder.fallbackNullDefault'),
        style: 'width:100%',
      },
    },
    {
      label: t('iot.rule.integration.bridge.retryBackoffMs'),
      field: 'retryBackoffMs',
      component: 'InputNumber',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.retryBackoffMs'),
      componentProps: {
        min: 0,
        placeholder: t('iot.rule.integration.bridge.placeholder.fallbackNullDefault'),
        style: 'width:100%',
      },
    },
    {
      label: t('iot.rule.integration.bridge.timeoutMs'),
      field: 'timeoutMs',
      component: 'InputNumber',
      helpMessage: t('iot.rule.integration.bridge.helpMessage.timeoutMs'),
      componentProps: {
        min: 0,
        placeholder: t('iot.rule.integration.bridge.placeholder.fallbackNullDefault'),
        style: 'width:100%',
      },
    },
    {
      label: t('iot.rule.integration.bridge.priority'),
      field: 'priority',
      component: 'InputNumber',
      defaultValue: 100,
      helpMessage: t('iot.rule.integration.bridge.helpMessage.priority'),
      componentProps: { min: 0, style: 'width:100%' },
    },
    // ========== 高级配置 ==========
    {
      field: 'divider-extras',
      component: 'Divider',
      label: t('iot.rule.integration.bridge.group.extras'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.integration.bridge.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.integration.bridge.helpMessage.remark'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.integration.bridge.placeholder.remark'),
        maxlength: 500,
        showCount: true,
      },
    },
  ];
};

/**
 * 编辑表单 schema —— 用于 useForm 初始化。
 *
 * <p>仅包含基础信息 + 流控覆盖；match_config / action_config 字段在 Edit.vue 中
 * 监听 direction + dataSourceId 变化后动态拼装（{@code getMatchConfigSchema} /
 * {@code getActionConfigSchema}）。
 *
 * @param _type 当前操作类型
 */
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [...basicEditFormSchema(_type), ...overrideEditFormSchema()];
};

export const customFormSchemaRules = (
  _type: Ref<ActionEnum>,
  _getFieldsValue: () => any,
): Partial<FormSchemaExt>[] => {
  return [];
};
