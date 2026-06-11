<!--
  JSON 对象 / JSON 字符串 自动铺开的只读 Descriptions。

  设计目标：后端 POJO 增减字段不需要改前端模板 —— 组件拿到 object 后，
  自动遍历 key，label 优先 i18n 映射（找不到则对 camelCase 做 humanize），
  value 智能格式化（null/空值隐藏、布尔→是/否、嵌套 object 递归、数组 join）。

  典型用法（:data 可以是 object 也可以是 JSON 字符串，组件自动 parse）：
    <JsonAutoDescriptions :data="device.protocolConfig"
                          i18n-prefix="video.device.info.protocolConfigFields" />

    <JsonAutoDescriptions :data="device.extendParams"
                          i18n-prefix="video.device.info.extendParamsFields" />
-->
<template>
  <div v-if="entries.length === 0" class="json-auto-empty">{{ emptyText }}</div>
  <a-descriptions
    v-else
    :column="column"
    :size="size"
    :bordered="bordered"
    :labelStyle="labelStyle"
    :contentStyle="contentStyle"
  >
    <a-descriptions-item v-for="item in entries" :key="item.key" :label="item.label">
      <JsonAutoDescriptions
        v-if="item.isObject"
        :data="item.value"
        :column="column"
        :size="size"
        :bordered="bordered"
        :i18n-prefix="childPrefix(item.key)"
        :hide-empty="hideEmpty"
        :empty-text="emptyText"
      />
      <span v-else-if="item.isArray" class="json-auto-array">
        {{ (item.value as any[]).join(', ') }}
      </span>
      <span v-else>{{ item.display }}</span>
    </a-descriptions-item>
  </a-descriptions>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';

  // 显式声明组件名以支持递归（嵌套 object 自调用）
  defineOptions({ name: 'JsonAutoDescriptions' });

  interface Entry {
    key: string;
    label: string;
    value: unknown;
    display: string;
    isObject: boolean;
    isArray: boolean;
  }

  const props = withDefaults(
    defineProps<{
      /** object / JSON string / null / undefined 都接受 */
      data?: Record<string, any> | string | null;
      /** Descriptions 列数，默认 1（适合单列详情布局） */
      column?: number;
      size?: 'small' | 'default' | 'middle';
      bordered?: boolean;
      /** i18n key 前缀，如 "video.device.info.protocolConfig"；没找到对应 key 时回退到 humanize(key) */
      i18nPrefix?: string;
      /** 是否隐藏 null / undefined / 空串字段 */
      hideEmpty?: boolean;
      /** 数据为空时显示的文字 */
      emptyText?: string;
      labelStyle?: Record<string, string>;
      contentStyle?: Record<string, string>;
    }>(),
    {
      column: 1,
      size: 'default',
      bordered: false,
      i18nPrefix: '',
      hideEmpty: true,
      emptyText: '—',
      labelStyle: () => ({}),
      contentStyle: () => ({}),
    },
  );

  const { t, te } = useI18n();

  const parsed = computed<Record<string, any>>(() => {
    const raw = props.data;
    if (!raw) return {};
    if (typeof raw === 'string') {
      try {
        const obj = JSON.parse(raw);
        return obj && typeof obj === 'object' && !Array.isArray(obj) ? obj : {};
      } catch {
        return {};
      }
    }
    if (typeof raw !== 'object' || Array.isArray(raw)) return {};
    return raw as Record<string, any>;
  });

  const entries = computed<Entry[]>(() => {
    return Object.entries(parsed.value)
      .filter(([, v]) => !props.hideEmpty || !isEmpty(v))
      .map(([key, value]) => {
        const isObject = value !== null && typeof value === 'object' && !Array.isArray(value);
        const isArray = Array.isArray(value);
        return {
          key,
          label: resolveLabel(key),
          value,
          display: isObject || isArray ? '' : formatScalar(value),
          isObject,
          isArray,
        };
      });
  });

  function isEmpty(v: unknown): boolean {
    if (v === null || v === undefined) return true;
    if (typeof v === 'string') return v.trim() === '';
    if (Array.isArray(v)) return v.length === 0;
    if (typeof v === 'object') return Object.keys(v as object).length === 0;
    return false;
  }

  function resolveLabel(key: string): string {
    if (props.i18nPrefix) {
      const i18nKey = `${props.i18nPrefix}.${key}`;
      if (te(i18nKey)) return t(i18nKey) as string;
    }
    return humanize(key);
  }

  /** camelCase → "Camel Case"；全大写 token（如 SIP/PTZ/URL）保持大写 */
  function humanize(key: string): string {
    const normalized = key
      // camelCase 拆分：小写后紧跟大写
      .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
      // 连续大写后跟小写：SIPUserAgent → SIP UserAgent → SIP User Agent
      .replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2');
    return normalized.charAt(0).toUpperCase() + normalized.slice(1);
  }

  function formatScalar(v: unknown): string {
    if (v === null || v === undefined || v === '') return '—';
    if (typeof v === 'boolean') return v ? '是' : '否';
    return String(v);
  }

  function childPrefix(key: string): string {
    return props.i18nPrefix ? `${props.i18nPrefix}.${key}` : '';
  }
</script>

<style lang="less" scoped>
  .json-auto-empty {
    color: #999;
    font-size: 13px;
    padding: 4px 0;
  }

  .json-auto-array {
    word-break: break-all;
  }
</style>
