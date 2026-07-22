<!--
  ScriptTemplatePicker ── 规则脚本「按类型一键填充模板」入口(IoT 应用内通用组件)。

  按 channelCode 从模板注册表(./templates)取该渠道的模板列表:
    · 0 个 → 按钮禁用
    · 1 个 → 直接按钮,点击填充
    · 多个 → 下拉选择
  已有脚本内容时填充前二次确认覆盖,确认后 emit('fill', 模板内容) 由调用方写入脚本字段。
  新增渠道/模板只需往 ./templates 增条,无需改本组件。
-->
<template>
  <!-- 多模板:下拉选择 -->
  <a-dropdown v-if="templates.length > 1" :disabled="disabled" :trigger="['click']">
    <a-button size="small" type="dashed" :disabled="disabled">
      <template #icon><SnippetsOutlined /></template>
      {{ t('iot.rule.groovy.ruleGroovyScript.template.fill') }}
      <DownOutlined />
    </a-button>
    <template #overlay>
      <a-menu @click="onMenuClick">
        <a-menu-item v-for="tpl in templates" :key="tpl.key">{{ tpl.label }}</a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>

  <!-- 单模板 / 无模板:直接按钮 -->
  <a-tooltip v-else :title="t('iot.rule.groovy.ruleGroovyScript.template.fillTip')">
    <a-button
      size="small"
      type="dashed"
      :disabled="disabled || templates.length === 0"
      @click="pick(templates[0])"
    >
      <template #icon><SnippetsOutlined /></template>
      {{ t('iot.rule.groovy.ruleGroovyScript.template.fill') }}
    </a-button>
  </a-tooltip>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Modal } from 'ant-design-vue';
  import { SnippetsOutlined, DownOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getScriptTemplates, type ScriptTemplateItem } from './templates';

  defineOptions({ name: 'ScriptTemplatePicker' });

  const props = withDefaults(
    defineProps<{
      /** 渠道编码 ── 据此选模板(与渠道字典对齐,大小写不敏感)。 */
      channelCode?: string;
      /** 当前脚本内容 ── 非空时填充前二次确认覆盖。 */
      currentValue?: string;
      /** 是否禁用(如编辑锁定态)。 */
      disabled?: boolean;
    }>(),
    { channelCode: '', currentValue: '', disabled: false },
  );

  const emit = defineEmits<{ (e: 'fill', content: string): void }>();

  const { t } = useI18n();
  const { createMessage } = useMessage();

  /** 当前渠道可用模板列表(数据驱动,新增模板无需改 UI) */
  const templates = computed<ScriptTemplateItem[]>(() => getScriptTemplates(props.channelCode));

  /** 下拉点选 */
  function onMenuClick({ key }: { key: string }) {
    pick(templates.value.find((it) => it.key === key));
  }

  /** 填充某模板;有内容先确认覆盖,再 emit fill 交调用方写入 */
  function pick(tpl?: ScriptTemplateItem) {
    if (!tpl) {
      createMessage.warning(t('iot.rule.groovy.ruleGroovyScript.template.noTemplate'));
      return;
    }
    const apply = () => {
      emit('fill', tpl.content);
      createMessage.success(t('iot.rule.groovy.ruleGroovyScript.template.filled'));
    };
    if (props.currentValue && props.currentValue.trim()) {
      Modal.confirm({
        title: t('iot.rule.groovy.ruleGroovyScript.template.confirmTitle'),
        content: t('iot.rule.groovy.ruleGroovyScript.template.overwriteConfirm'),
        onOk: apply,
      });
    } else {
      apply();
    }
  }
</script>
