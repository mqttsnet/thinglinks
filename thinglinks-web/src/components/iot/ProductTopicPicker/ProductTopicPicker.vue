<template>
  <div class="product-topic-picker">
    <!-- ============ 单选 (multiple=false): input + 选择主题 按钮 ============ -->
    <template v-if="!multiple">
      <a-input-group compact class="picker-input-group">
        <a-input
          :value="(modelValue as string) || ''"
          :placeholder="placeholder || t('component.productTopicPicker.inputPlaceholder')"
          :disabled="disabled"
          :allow-clear="!disabled"
          :style="{ width: `calc(100% - ${triggerWidth}px)` }"
          @change="onInputChange"
        >
          <template #prefix>
            <FunctionOutlined
              v-if="modelValue && isWildcard(modelValue as string)"
              class="topic-mode-icon--custom"
            />
            <UnorderedListOutlined v-else-if="modelValue" :style="{ color: '#52c41a' }" />
            <FilterOutlined v-else />
          </template>
        </a-input>
        <a-button
          type="primary"
          ghost
          :disabled="!productIdentification || disabled"
          @click="openPicker"
        >
          <template #icon><AppstoreOutlined /></template>
          {{ t('component.productTopicPicker.pickButton') }}
        </a-button>
      </a-input-group>

      <!-- 模式提示 (有值时) -->
      <div v-if="modelValue && showModeHint" class="mode-hint">
        <a-tag v-if="isWildcard(modelValue as string)" color="blue">
          <template #icon><FunctionOutlined /></template>
          {{ t('component.productTopicPicker.modeCustom') }}
        </a-tag>
        <a-tag v-else color="green">
          <template #icon><UnorderedListOutlined /></template>
          {{ t('component.productTopicPicker.modeBasic') }}
        </a-tag>
      </div>
    </template>

    <!-- ============ 多选 (multiple=true): tag 列表 + 添加 主题 按钮 ============ -->
    <template v-else>
      <div class="multi-picker-shell" :class="{ 'is-empty': !multiValue.length, 'is-disabled': disabled }">
        <div class="multi-tags">
          <a-tag
            v-for="topic in multiValue"
            :key="topic"
            :closable="!disabled"
            :color="isWildcard(topic) ? 'blue' : 'green'"
            class="multi-tag"
            @close="onRemoveTag(topic)"
          >
            <FunctionOutlined v-if="isWildcard(topic)" />
            <UnorderedListOutlined v-else />
            <span class="multi-tag-text">{{ topic }}</span>
          </a-tag>
          <span v-if="!multiValue.length" class="multi-empty">
            {{ multiEmptyText || t('component.productTopicPicker.multiEmpty') }}
          </span>
        </div>
        <a-button
          type="primary"
          ghost
          size="small"
          :disabled="!productIdentification || disabled"
          @click="openPicker"
        >
          <template #icon><PlusOutlined /></template>
          {{ t('component.productTopicPicker.pickButton') }}
        </a-button>
      </div>
      <div v-if="multiValue.length && showModeHint" class="multi-summary">
        {{ t('component.productTopicPicker.multiSelected', { count: multiValue.length }) }}
      </div>
    </template>

    <!-- 未选产品提示 -->
    <div v-if="!productIdentification && !disabled" class="picker-tip">
      <InfoCircleOutlined />
      {{ t('component.productTopicPicker.tipPickProductFirst') }}
    </div>

    <PickerModal @register="registerModal" @success="onModalSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import {
    AppstoreOutlined,
    FunctionOutlined,
    FilterOutlined,
    UnorderedListOutlined,
    InfoCircleOutlined,
    PlusOutlined,
  } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import PickerModal from './PickerModal.vue';
  import type { ProductTopicPickerMode } from './types';

  defineOptions({ name: 'ProductTopicPicker' });

  /**
   * v-model 类型:
   * - multiple=false(默认): {@code string} 单一 topic
   * - multiple=true: {@code string[]} 多个 topic 模式
   */
  type PickerModelValue = string | string[];

  const props = defineProps({
    /** 当前 topic 值(v-model);单选时 string,多选时 string[] */
    modelValue: { type: [String, Array] as any, default: '' },
    /** 关联产品标识(必填,组件依赖此值拉取基础 topic 列表) */
    productIdentification: { type: String, default: '' },
    /** 输入框占位(仅 multiple=false) */
    placeholder: { type: String, default: '' },
    /** 是否禁用 */
    disabled: { type: Boolean, default: false },
    /** 默认打开的模式 */
    defaultMode: {
      type: String as () => ProductTopicPickerMode,
      default: undefined,
    },
    /** 是否显示当前 topic 的"基础/自定义"模式标签 */
    showModeHint: { type: Boolean, default: true },
    /** 是否多选模式(默认 false) */
    multiple: { type: Boolean, default: false },
    /** 多选模式下空态提示文案(覆盖默认 i18n) */
    multiEmptyText: { type: String, default: '' },
  });

  const emit = defineEmits<{
    (e: 'update:modelValue', v: PickerModelValue): void;
    (e: 'change', v: PickerModelValue, mode: ProductTopicPickerMode): void;
  }>();

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();

  /** 触发器按钮宽度(中文 ~96px,留点 padding) */
  const triggerWidth = computed(() => 110);

  /** 多选时数组化 modelValue(防御外部传入非数组值) */
  const multiValue = computed<string[]>(() => {
    const v = props.modelValue;
    if (Array.isArray(v)) return v.filter((x) => !!x);
    if (typeof v === 'string' && v) return [v];
    return [];
  });

  function openPicker() {
    if (!props.productIdentification) {
      createMessage.warning(t('component.productTopicPicker.tipPickProductFirst'));
      return;
    }
    openModal(true, {
      productIdentification: props.productIdentification,
      // 多选下,把 modal 当作"单次添加"工具,所以传空 value 让模态从空状态开始
      value: props.multiple ? '' : (props.modelValue as string),
      defaultMode: props.defaultMode,
    });
  }

  function onModalSuccess(topic: string, mode: ProductTopicPickerMode) {
    if (!props.multiple) {
      emit('update:modelValue', topic);
      emit('change', topic, mode);
      return;
    }
    // 多选:追加到数组,自动去重
    if (!topic) return;
    const cur = multiValue.value;
    if (cur.includes(topic)) {
      createMessage.info(t('component.productTopicPicker.alreadyAdded'));
      return;
    }
    const next = [...cur, topic];
    emit('update:modelValue', next);
    emit('change', next, mode);
  }

  function onInputChange(e: Event) {
    const v = (e.target as HTMLInputElement).value || '';
    emit('update:modelValue', v);
    emit('change', v, isWildcard(v) ? 'custom' : 'basic');
  }

  function onRemoveTag(topic: string) {
    const next = multiValue.value.filter((t) => t !== topic);
    emit('update:modelValue', next);
    emit('change', next, 'basic');
  }

  /**
   * 判断是否为通配符 topic(自定义模式标识)。
   * <p>含 +(单层通配)或 #(多层通配)即视为自定义。
   */
  function isWildcard(v?: string): boolean {
    return !!v && /[+#]/.test(v);
  }
</script>

<style lang="less" scoped>
  .topic-mode-icon--custom {
    color: @primary-color;
  }

  .product-topic-picker {
    width: 100%;
  }

  .picker-input-group {
    display: flex;
    width: 100%;
  }

  .mode-hint {
    margin-top: 6px;

    :deep(.ant-tag) {
      font-size: 11px;
      padding: 1px 6px;
      margin-right: 0;
    }
  }

  .picker-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #8c8c8c;
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(svg) {
      color: #faad14;
    }
  }

  // ============ 多选模式样式 ============
  .multi-picker-shell {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 6px 8px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    background: #fff;
    transition: border-color 0.18s ease;

    &:hover {
      border-color: #4096ff;
    }

    &.is-empty {
      background: #fafbfc;
    }

    &.is-disabled {
      background: #f5f5f5;
      cursor: not-allowed;
    }
  }

  .multi-tags {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    min-height: 26px;
    align-items: center;
  }

  .multi-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin: 0;
    font-family: 'JetBrains Mono', Menlo, Consolas, monospace;
    font-size: 12px;

    .multi-tag-text {
      max-width: 280px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .multi-empty {
    font-size: 12px;
    color: #bfbfbf;
  }

  .multi-summary {
    margin-top: 4px;
    font-size: 12px;
    color: #8c8c8c;
  }
</style>
