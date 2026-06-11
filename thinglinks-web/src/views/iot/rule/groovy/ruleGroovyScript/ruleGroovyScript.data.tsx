import { Ref } from 'vue';
import { yesNoComponentProps, renderYesNoComponent } from '/@/utils/thinglinks/common';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
import { dictComponentProps } from '/@/utils/thinglinks/common';
import type { CardField } from '/@/components/BusinessCardList';
import { FormSchemaExt, RuleType } from '/@/api/thinglinks/common/formValidateService';
import { check } from '/@/api/devOperation/ops/defInterface';
import { Rule } from '/@/components/Form';

const { t } = useI18n();

/**
 * 设备上行转换脚本类型 —— 规则脚本现仅此一种类型,固定写入 scriptType:
 *   productIdentification = 产品标识(IotProductPicker 选中值)
 *   objectVersion         = 产品发布版本号(版本级联选中值)
 *   topicPattern          = topic 模式(ProductTopicPicker 选中值)
 * 保存时由 Edit.vue 统一置为此常量,表单不再暴露 scriptType 字典字段。
 */
export const TOPIC_INBOUND_TRANSFORM = 'topicInboundTransform';

export const columns = (): BasicColumn[] => {
  return [
    {
      title: t('iot.rule.groovy.ruleGroovyScript.name'),
      dataIndex: 'name',
      width: 180,
      fixed: 'left',
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.appId'),
      dataIndex: ['echoMap', 'appId'],
      width: 120,
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.channelCode'),
      dataIndex: ['echoMap', 'channelCode'],
      width: 120,
    },
    {
      // productIdentification 产品标识(裸值),直接展示原值。
      title: t('iot.rule.groovy.ruleGroovyScript.topicInbound.product'),
      dataIndex: 'productIdentification',
      width: 120,
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern'),
      dataIndex: 'topicPattern',
      width: 160,
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.enable'),
      dataIndex: 'enable',
      width: 100,
      customRender: ({ text }) => renderYesNoComponent(text, true),
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.objectVersion'),
      dataIndex: 'objectVersion',
      width: 100,
    },
    {
      title: t('iot.rule.groovy.ruleGroovyScript.remark'),
      dataIndex: 'remark',
      ellipsis: true,
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
      label: t('iot.rule.groovy.ruleGroovyScript.name'),
      field: 'name',
      component: 'Input',
      colProps: { span: 6 },
      componentProps: {
        placeholder: t('iot.rule.groovy.ruleGroovyScript.search.placeholder'),
        allowClear: true,
      },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.appId'),
      field: 'appId',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.channelCode'),
      field: 'channelCode',
      component: 'ApiSelect',
      colProps: { span: 6 },
      componentProps: {
        ...dictComponentProps(DictEnum.RULE_GROOVY_SCRIPT_CHANNEL_CODE),
      },
    },
    {
      // productIdentification 产品标识(裸值),按产品标识文本检索。
      label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.product'),
      field: 'productIdentification',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern'),
      field: 'topicPattern',
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

/**
 * 卡片视图字段配置（BusinessCardList）。
 * <p>展示核心元数据，覆盖 channelCode / productIdentification / topicPattern +
 * 最后修改时间，方便用户在卡片视图直接判断脚本归属。
 */
export const cardFields = (): CardField[] => [
  {
    label: t('iot.rule.groovy.ruleGroovyScript.channelCode'),
    field: 'channelCode',
    dictType: DictEnum.RULE_GROOVY_SCRIPT_CHANNEL_CODE,
    span: 12,
  },
  {
    // productIdentification 产品标识(裸值),直接展示原值,不再走字典翻译。
    label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.product'),
    field: 'productIdentification',
    span: 12,
  },
  {
    label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern'),
    field: 'topicPattern',
    span: 12,
  },
  {
    label: t('thinglinks.common.updatedTime'),
    field: 'updatedTime',
  },
];

export const editFormSchema = (_type: Ref<ActionEnum>): FormSchema[] => {
  return [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    // ========== 基本信息 ==========
    {
      field: 'divider-basic',
      component: 'Divider',
      label: t('iot.rule.groovy.ruleGroovyScript.group.basic'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.name'),
      field: 'name',
      component: 'Input',
      required: true,
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.name'),
      componentProps: {
        placeholder: t('iot.rule.groovy.ruleGroovyScript.placeholder.name'),
        maxlength: 100,
      },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.appId'),
      field: 'appId',
      component: 'ApiSelect',
      required: true,
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.appId'),
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.LINK_APPLICATION_SCENARIO),
      },
    },
    // ========== 脚本归属 ==========
    {
      field: 'divider-classification',
      component: 'Divider',
      label: t('iot.rule.groovy.ruleGroovyScript.group.classification'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.channelCode'),
      field: 'channelCode',
      component: 'ApiSelect',
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.channelCode'),
      componentProps: {
        allowClear: false,
        ...dictComponentProps(DictEnum.RULE_GROOVY_SCRIPT_CHANNEL_CODE),
      },
    },
    // —— 设备上行转换脚本固定绑定「产品 → 产品版本 → 主题模式」,三字段统一用 slot 渲染:
    //    productIdentification → 产品选择器(IotProductPicker),选中产品标识存入 productIdentification;
    //    objectVersion         → 产品版本级联(a-select,选项 listByProduct 过滤 versionStatus∈[1,2,3]);
    //    topicPattern          → 主题模式选择器(ProductTopicPicker),:productIdentification 绑定当前产品标识。
    {
      label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.product'),
      field: 'productIdentification',
      component: 'Input',
      slot: 'productIdentification',
      colProps: { span: 22 },
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.businessCode'),
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.productVersion'),
      field: 'objectVersion',
      component: 'Input',
      slot: 'objectVersion',
      colProps: { span: 22 },
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.objectVersion'),
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.topicInbound.topicPattern'),
      field: 'topicPattern',
      component: 'Input',
      slot: 'topicPattern',
      colProps: { span: 22 },
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.topicInbound.helpMessage.topicPattern'),
    },
    // ========== 脚本内容 ==========
    {
      field: 'divider-content',
      component: 'Divider',
      label: t('iot.rule.groovy.ruleGroovyScript.group.content'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.scriptContent'),
      field: 'scriptContent',
      component: 'Input',
      slot: 'scriptContent',
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.scriptContent'),
      itemProps: {
        extra: 'groovy',
      },
      colProps: { span: 24 },
      dynamicRules: () => {
        const rules: Rule[] = [];
        rules.push({
          required: true,
          message: t('common.rules.require'),
          ruleType: RuleType.append,
        });
        return rules;
      },
    },
    // ========== 其他设置 ==========
    {
      field: 'divider-extras',
      component: 'Divider',
      label: t('iot.rule.groovy.ruleGroovyScript.group.extras'),
      colProps: { span: 24 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.enable'),
      field: 'enable',
      component: 'RadioGroup',
      defaultValue: '0',
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.enable'),
      componentProps: {
        ...yesNoComponentProps(),
      },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.extendParams'),
      field: 'extendParams',
      component: 'Input',
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.extendParams'),
      componentProps: { maxlength: 2000 },
    },
    {
      label: t('iot.rule.groovy.ruleGroovyScript.remark'),
      field: 'remark',
      component: 'InputTextArea',
      colProps: { span: 24 },
      helpMessage: t('iot.rule.groovy.ruleGroovyScript.helpMessage.remark'),
      componentProps: {
        rows: 3,
        placeholder: t('iot.rule.groovy.ruleGroovyScript.placeholder.remark'),
        maxlength: 500,
        showCount: true,
      },
    },
  ];
};

// 前端自定义表单验证规则
export const customFormSchemaRules = (
  _type: Ref<ActionEnum>,
  getFieldsValue: () => Recordable,
): Partial<FormSchemaExt>[] => {
  return [
    {
      field: 'code',
      type: RuleType.append,
      rules: [
        {
          trigger: ['change', 'blur'],
          async validator(_, value) {
            const model = await getFieldsValue();
            if (value && (await check(value, model?.id))) {
              return Promise.reject(t('devOperation.ops.defInterface.code') + '已经存在');
            }
            return Promise.resolve();
          },
        },
      ],
    },
  ];
};
