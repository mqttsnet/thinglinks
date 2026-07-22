<template>
  <div :class="[`${prefixCls}__header px-2 py-5`, $attrs.class]">
    <BasicTitle :helpMessage="helpMessage" normal>
      <template v-if="props.title">
        {{ props.title }}
      </template>
      <template v-else>
        <slot name="title"></slot>
      </template>
    </BasicTitle>
    <div :class="`${prefixCls}__action`">
      <slot name="action"></slot>
      <BasicArrow v-if="props.canExpan" up :expand="props.show" @click="$emit('expand')" />
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { BasicArrow, BasicTitle } from '/@/components/Basic';

  /**
   * 改为 `<script setup>` 模式避免 defineComponent + module scope `components` 引用导致的
   * ESM 循环依赖 TDZ 错误（"Cannot access 'BasicArrow' before initialization"）。
   * <p>setup 模式下顶层 import 自动作为 template 可用组件，无需在 module scope 显式注册。
   */
  defineOptions({ inheritAttrs: false });

  const props = defineProps({
    prefixCls: { type: String, default: '' },
    helpMessage: {
      type: [Array, String] as PropType<string[] | string>,
      default: '',
    },
    title: { type: String, default: '' },
    show: { type: Boolean, default: false },
    canExpan: { type: Boolean, default: false },
  });

  defineEmits<{
    (e: 'expand'): void;
  }>();
</script>
