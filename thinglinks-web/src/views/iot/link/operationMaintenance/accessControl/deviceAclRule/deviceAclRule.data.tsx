import { Ref, h } from 'vue';
import { Tag } from 'ant-design-vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { FormSchemaExt } from '/@/api/thinglinks/common/formValidateService';
import {
  dictComponentProps,
  dictComponentProps2,
  yesNoComponentProps,
} from '/@/utils/thinglinks/common';
import type { CardField } from '/@/components/BusinessCardList';

const { t } = useI18n();
const tk = (k: string) => t(`iot.link.operationMaintenance.accessControl.deviceAclRule.${k}`);

/**
 * 卡片视图字段配置(BusinessCardList)。
 *
 * <p>右上 badge = ruleLevel(产品级/设备级);右下圆点 = enabled。
 * 卡片体展示产品 + 动作 + 优先级 + 时间;decision/deviceIdentification 在详情页看
 * (decision 是 boolean,无字典翻译;deviceIdentification 产品级时为空,放卡片体丑)。
 */
export const cardFields = (): CardField[] => [
  {
    label: tk('productIdentification'),
    field: 'productIdentification',
    span: 24,
  },
  {
    label: tk('actionType'),
    field: 'actionType',
    dictType: DictEnum.LINK_ACL_RULE_ACTION_TYPE,
    span: 12,
  },
  {
    label: tk('priority'),
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
      title: tk('ruleName'),
      dataIndex: 'ruleName',
      width: 200,
      ellipsis: true,
    },
    {
      title: tk('ruleLevel'),
      dataIndex: 'ruleLevel',
      slots: { customRender: 'ruleLevel' },
      width: 110,
    },
    {
      title: tk('productIdentification'),
      dataIndex: 'productIdentification',
      width: 160,
      ellipsis: true,
    },
    {
      title: tk('deviceIdentification'),
      dataIndex: 'deviceIdentification',
      width: 160,
      ellipsis: true,
      customRender: ({ value }) => value || '-',
    },
    {
      title: tk('actionType'),
      dataIndex: 'actionType',
      slots: { customRender: 'actionType' },
      width: 110,
    },
    {
      title: tk('decision'),
      dataIndex: 'decision',
      slots: { customRender: 'decision' },
      width: 90,
    },
    {
      title: tk('priority'),
      dataIndex: 'priority',
      sorter: true,
      width: 100,
    },
    {
      title: tk('topicPattern'),
      dataIndex: 'topicPattern',
      ellipsis: true,
      customRender: ({ value }) =>
        value ? h('code', { class: 'mono-code' }, value) : '-',
    },
    {
      title: tk('ipWhitelist'),
      dataIndex: 'ipWhitelist',
      ellipsis: true,
      customRender: ({ value }) => value || '-',
    },
    {
      title: tk('enabled'),
      dataIndex: 'enabled',
      slots: { customRender: 'enabled' },
      width: 90,
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
      label: tk('ruleName'),
      field: 'ruleName',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { placeholder: t('common.inputText') },
    },
    {
      label: tk('ruleLevel'),
      field: 'ruleLevel',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_ACL_RULE_LEVEL),
      },
    },
    {
      label: tk('productIdentification'),
      field: 'productIdentification',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { placeholder: t('common.inputText') },
    },
    {
      label: tk('deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: { placeholder: t('common.inputText') },
    },
    {
      label: tk('actionType'),
      field: 'actionType',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_ACL_RULE_ACTION_TYPE),
      },
    },
    {
      label: tk('decision'),
      field: 'decision',
      component: 'RadioButtonGroup',
      componentProps: {
        ...yesNoComponentProps(),
      },
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

/**
 * 编辑页字段(参照桥接规则 Edit.vue 风格 ── Divider 作为分组标题)。
 *
 * <p>三段:① 基础信息 ── ② 范围匹配 ── ③ 权限决策。
 * Divider 字段的 label 即为该组标题(BasicForm 内置组件,自动渲染 a-divider orientation="left")。
 */
export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },

    // ============================== ① 基础信息 ==============================
    {
      field: 'divider-basic',
      component: 'Divider',
      label: tk('section.base'),
      colProps: { span: 24 },
    },
    {
      label: tk('ruleName'),
      field: 'ruleName',
      component: 'Input',
      required: true,
      componentProps: {
        placeholder: tk('placeholder.ruleName'),
        maxlength: 100,
        showCount: true,
      },
      helpMessage: [
        tk('helpMessage.ruleName[0]'),
        tk('helpMessage.ruleName[1]'),
        tk('helpMessage.ruleName[2]'),
      ],
    },
    {
      label: tk('priority'),
      field: 'priority',
      component: 'InputNumber',
      required: true,
      defaultValue: 500,
      componentProps: {
        min: 0,
        max: 1000,
        style: { width: '100%' },
        placeholder: tk('placeholder.priority'),
      },
      helpMessage: [
        tk('helpMessage.priority[0]'),
        tk('helpMessage.priority[1]'),
        tk('helpMessage.priority[2]'),
        tk('helpMessage.priority[3]'),
        tk('helpMessage.priority[4]'),
        tk('helpMessage.priority[5]'),
      ],
    },
    {
      label: tk('enabled'),
      field: 'enabled',
      component: 'RadioButtonGroup',
      required: true,
      defaultValue: '1',
      componentProps: {
        ...yesNoComponentProps(),
      },
    },

    // ============================== ② 范围匹配 ==============================
    {
      field: 'divider-scope',
      component: 'Divider',
      label: tk('section.scope'),
      colProps: { span: 24 },
    },
    {
      label: tk('ruleLevel'),
      field: 'ruleLevel',
      // ApiRadioGroup 才能正确接 dictComponentProps2 的 api + params 加载字典选项;
      // RadioButtonGroup 期望静态 options,会导致选项不显示。
      component: 'ApiRadioGroup',
      required: true,
      defaultValue: 0,
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_ACL_RULE_LEVEL,
          extendFirst: true,
          stringToNumber: true,
        }),
        isBtn: true,   // 渲染成 RadioButton 风格
      },
      helpMessage: [
        tk('helpMessage.ruleLevel[0]'),
        tk('helpMessage.ruleLevel[1]'),
        tk('helpMessage.ruleLevel[2]'),
        tk('helpMessage.ruleLevel[3]'),
      ],
    },
    {
      label: tk('productIdentification'),
      field: 'productIdentification',
      required: true,
      component: 'Input',
      // AllOrCustomPicker 触发器较宽,单独占整行(span 22),否则 trigger 文案易截断
      colProps: { span: 22 },
      slot: 'productIdentification',
      helpMessage: [
        tk('helpMessage.productIdentification[0]'),
        tk('helpMessage.productIdentification[1]'),
        tk('helpMessage.productIdentification[2]'),
      ],
    },
    {
      label: tk('deviceIdentification'),
      field: 'deviceIdentification',
      component: 'Input',
      ifShow: ({ values }) => Number(values.ruleLevel) === 1,
      required: ({ values }) => Number(values.ruleLevel) === 1,
      colProps: { span: 22 },
      slot: 'deviceIdentification',
      helpMessage: [
        tk('helpMessage.deviceIdentification[0]'),
        tk('helpMessage.deviceIdentification[1]'),
        tk('helpMessage.deviceIdentification[2]'),
      ],
    },
    {
      label: tk('actionType'),
      field: 'actionType',
      component: 'ApiRadioGroup',
      defaultValue: 0,
      required: true,
      componentProps: {
        ...dictComponentProps2({
          type: DictEnum.LINK_ACL_RULE_ACTION_TYPE,
          extendFirst: true,
          stringToNumber: true,
        }),
        isBtn: true,
      },
      helpMessage: [
        tk('helpMessage.actionType[0]'),
        tk('helpMessage.actionType[1]'),
        tk('helpMessage.actionType[2]'),
        tk('helpMessage.actionType[3]'),
        tk('helpMessage.actionType[4]'),
        tk('helpMessage.actionType[5]'),
      ],
    },
    {
      label: tk('topicPattern'),
      field: 'topicPattern',
      component: 'Input',
      required: true,
      colProps: { span: 22 },
      slot: 'topicPattern',
      helpMessage: [
        tk('helpMessage.topicPattern[0]'),
        tk('helpMessage.topicPattern[1]'),
        tk('helpMessage.topicPattern[2]'),
        tk('helpMessage.topicPattern[3]'),
        tk('helpMessage.topicPattern[4]'),
      ],
    },

    // ============================== ③ 权限决策 ==============================
    {
      field: 'divider-decision',
      component: 'Divider',
      label: tk('section.decision'),
      colProps: { span: 24 },
    },
    {
      label: tk('decision'),
      field: 'decision',
      component: 'RadioButtonGroup',
      required: true,
      defaultValue: '1',
      componentProps: {
        ...yesNoComponentProps(),
      },
      helpMessage: [
        tk('helpMessage.decision[0]'),
        tk('helpMessage.decision[1]'),
        tk('helpMessage.decision[2]'),
        tk('helpMessage.decision[3]'),
        tk('helpMessage.decision[4]'),
      ],
    },
    {
      label: tk('ipWhitelist'),
      field: 'ipWhitelist',
      component: 'Input',
      required: true,
      colProps: { span: 22 },
      slot: 'ipWhitelist',
      helpMessage: [
        tk('helpMessage.ipWhitelist[0]'),
        tk('helpMessage.ipWhitelist[1]'),
        tk('helpMessage.ipWhitelist[2]'),
        tk('helpMessage.ipWhitelist[3]'),
      ],
    },
    {
      label: tk('remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 22 },
      componentProps: {
        rows: 3,
        maxlength: 500,
        showCount: true,
        placeholder: t('common.inputText'),
      },
    },
  ];
};

/** 列表页 + 详情页 ProductTopic 选择弹窗的列定义 */
export const topicColumns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.link.productTopic.productTopic.functionType'),
      dataIndex: ['echoMap', 'functionType'],
      width: 120,
    },
    {
      title: t('iot.link.productTopic.productTopic.topic'),
      dataIndex: 'topic',
      ellipsis: true,
    },
    {
      title: t('iot.link.productTopic.productTopic.publisher'),
      dataIndex: ['echoMap', 'publisher'],
      width: 120,
    },
    {
      title: t('iot.link.productTopic.productTopic.subscriber'),
      dataIndex: ['echoMap', 'subscriber'],
      width: 120,
    },
    {
      title: t('iot.link.productTopic.productTopic.remark'),
      dataIndex: 'remark',
      ellipsis: true,
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (_): Partial<FormSchemaExt>[] => {
  return [];
};

// ============================== 渲染辅助 ==============================

/**
 * 决策值 Tag 渲染:1 = 允许(绿) / 0 = 拒绝(红)。
 */
export function renderDecisionTag(value: any) {
  const isAllow = value === '1' || value === 1 || value === true;
  return h(
    Tag,
    { color: isAllow ? 'success' : 'error', class: 'acl-decision-tag' },
    () => (isAllow ? tk('allow') : tk('deny')),
  );
}

/**
 * 启用状态 Tag 渲染。
 */
export function renderEnabledTag(value: any) {
  const isEnabled = value === '1' || value === 1 || value === true;
  return h(
    Tag,
    { color: isEnabled ? 'processing' : 'default' },
    () => (isEnabled ? tk('status.enabled') : tk('status.disabled')),
  );
}
