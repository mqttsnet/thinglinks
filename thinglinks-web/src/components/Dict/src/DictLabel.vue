<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Think Gem
-->
<template>
  <template v-for="item in dictList" :key="item.id">
    <span class="jeesite-dict-label">
      <template v-if="item.cssClass?.startsWith('tag ')">
        <Tag :color="item.cssClass?.substring(4)?.split(' ')[0]" :title="item.name">
          <Icon v-if="props.icon && item.icon && item.icon != ''" :icon="item.icon" class="pr-1" />
          {{ item.name }}
        </Tag>
      </template>
      <template v-else-if="item.cssClass?.startsWith('badge ')">
        <Icon v-if="props.icon && item.icon && item.icon != ''" :icon="item.icon" class="pr-1" />
        <Badge
          :status="
            item.cssClass.indexOf(' error') >= 0
              ? 'error'
              : item.cssClass.indexOf(' success') >= 0
              ? 'success'
              : item.cssClass.indexOf(' warning') >= 0
              ? 'warning'
              : item.cssClass.indexOf(' processing') >= 0
              ? 'processing'
              : 'default'
          "
          :text="item.name"
          :title="item.name"
        />
      </template>
      <template v-else>
        <span :class="item.cssClass" :style="item.cssStyle" :title="item.name">
          <Icon v-if="props.icon && item.icon && item.icon != ''" :icon="item.icon" class="pr-1" />
          {{ item.name }}
        </span>
      </template>
    </span>
  </template>
  <template v-if="dictList.length == 0">
    <span class="jeesite-dict-label">
      {{ fallbackText }}
    </span>
  </template>
</template>
<script lang="ts" setup>
  import { ref, watch, computed } from 'vue';
  import { Tag, Badge } from 'ant-design-vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from './useDict';

  const { t } = useI18n();

  /**
   * 字典标签组件
   *
   * 改为 `<script setup>` 模式避免 defineComponent + script 顶层
   * import 导致的 ESM 循环依赖 TDZ 错误
   * （"Cannot access 'Icon' before initialization"）。
   *
   * 注意：defineProps 默认值不能引用 setup 内变量（如 t），编译器会 hoist 到 setup 外。
   * defaultValue 显示用 fallbackText 计算属性兜底（运行时才调 t）。
   */
  const props = defineProps({
    dictType: { type: String, default: '' },
    dictValue: { type: [String, Number, Array, Boolean], default: undefined },
    defaultValue: { type: String, default: undefined },
    icon: { type: Boolean, default: true },
  });

  /**
   * 默认显示文案：调用方传了 defaultValue 用之，否则用 i18n 翻译"未知"。
   */
  const fallbackText = computed(() => props.defaultValue ?? t('未知'));

  const { getDictList } = useDict();
  const dictList = ref<any[]>([]);

  watch(
    () => props.dictValue,
    () => {
      dictList.value = getDictList(props.dictType).filter((item) =>
        (',' + props.dictValue + ',').includes(',' + item.value + ','),
      );
    },
    { immediate: true },
  );
</script>
<style lang="less">
  .jeesite-dict-label {
    padding: 0 2px;
  }
</style>
