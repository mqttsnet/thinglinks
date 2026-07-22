<template>
  <!--
    SecretField ── 敏感值通用展示组件。

    场景:密码 / 签名密钥 / 加密密钥 / 加密向量 / Token 等。
    交互:
      - 默认渲染脱敏(前 N 后 N + 中间 ***);长度 ≤4 全 ***
      - 👁 小眼睛:切换明文 / 脱敏(组件内部 state,不向外抛)
      - 📋 复制:复制**明文**到剪贴板
      - 空值显示 "-",不渲染按钮
    无障碍:button 都有 aria-label;visible state 不持久化(刷新页面回到脱敏)。
  -->
  <span class="secret-field" :class="{ 'is-empty': isEmpty }">
    <Tooltip v-if="!isEmpty && visible" :title="modelValue">
      <span class="value mono">{{ modelValue }}</span>
    </Tooltip>
    <span v-else-if="!isEmpty" class="value mono">{{ desensitized }}</span>
    <span v-else class="value-empty">-</span>

    <span v-if="!isEmpty" class="actions">
      <Tooltip :title="visible ? t('common.title.hide') : t('common.title.show')">
        <span
          class="icon-btn"
          role="button"
          tabindex="0"
          :aria-label="visible ? t('common.title.hide') : t('common.title.show')"
          @click="toggle"
          @keydown.enter="toggle"
          @keydown.space.prevent="toggle"
        >
          <EyeInvisibleOutlined v-if="visible" />
          <EyeOutlined v-else />
        </span>
      </Tooltip>
      <Tooltip :title="t('common.title.copy')">
        <span
          class="icon-btn"
          role="button"
          tabindex="0"
          :aria-label="t('common.title.copy')"
          @click="copy"
          @keydown.enter="copy"
          @keydown.space.prevent="copy"
        >
          <CopyOutlined />
        </span>
      </Tooltip>
    </span>
  </span>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Tooltip } from 'ant-design-vue';
  import { EyeOutlined, EyeInvisibleOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';

  defineOptions({ name: 'SecretField' });

  const props = withDefaults(
    defineProps<{
      /** 真实值;空字符串 / null / undefined 视为 empty */
      modelValue?: string | number | null;
      /** 前后保留位数,默认 2(展示 xx***xx) */
      visibleEdge?: number;
      /** 自定义脱敏字符,默认 `*` */
      maskChar?: string;
    }>(),
    {
      modelValue: '',
      visibleEdge: 2,
      maskChar: '*',
    },
  );

  const { t } = useI18n();

  const visible = ref(false);

  const isEmpty = computed(() => {
    const v = props.modelValue;
    return v == null || String(v).length === 0;
  });

  /** 脱敏算法:长度 ≤ 2*visibleEdge → 全 ***;否则前 N 后 N + 3 个 maskChar */
  const desensitized = computed(() => {
    if (isEmpty.value) return '';
    const s = String(props.modelValue);
    const edge = Math.max(0, props.visibleEdge);
    if (s.length <= edge * 2) {
      return props.maskChar.repeat(3);
    }
    return `${s.slice(0, edge)}${props.maskChar.repeat(3)}${s.slice(-edge)}`;
  });

  function toggle() {
    visible.value = !visible.value;
  }

  async function copy() {
    if (isEmpty.value) return;
    await handleCopyTextV2(String(props.modelValue));
  }
</script>

<style lang="less" scoped>
  .secret-field {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
    max-width: 100%;

    &.is-empty .value-empty {
      color: #97a1b0;
    }

    .value {
      color: #2a3547;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      min-width: 0;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 13px;
      }
    }

    .actions {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      flex-shrink: 0;
    }

    .icon-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      border-radius: 6px;
      color: #97a1b0;
      cursor: pointer;
      transition: background 0.18s ease, color 0.18s ease;

      &:hover,
      &:focus-visible {
        background: #f4f6fb;
        color: @primary-color;
        outline: none;
      }
    }
  }
</style>
