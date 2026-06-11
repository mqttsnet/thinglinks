<template>
  <div class="aoc-trigger" :class="[`style-${triggerStyle}`, { disabled, focused: modalOpen }]">
    <!-- ============ 显示区(可点击打开弹窗) ============ -->
    <div class="trigger-display" @click="openPicker">
      <!-- 全部 -->
      <template v-if="isAllMode">
        <a-tag color="success" class="all-tag">
          <CheckSquareOutlined />
          {{ triggerLabels?.all ?? t('component.allOrCustomPicker.triggerAll') }}
        </a-tag>
      </template>

      <!-- 自定义模式:tags 形态 ── 显示已选 record 的标签列表 -->
      <template v-else-if="customCount > 0 && triggerStyle === 'tags'">
        <a-tag
          v-for="v in displayedSelectedTags"
          :key="String(v)"
          color="processing"
          closable
          @close.stop="removeOne(v)"
          class="trigger-item-tag"
        >
          {{ tagLabel(v) }}
        </a-tag>
        <span class="more-hint" v-if="customCount > previewLimit">
          +{{ customCount - previewLimit }}
        </span>
      </template>

      <!-- 自定义模式:count 形态 ── 仅显示统计 -->
      <template v-else-if="customCount > 0 && triggerStyle === 'count'">
        <span class="count-text">
          {{
            triggerLabels?.custom?.(customCount) ??
            t('component.allOrCustomPicker.triggerCustom', { count: customCount })
          }}
        </span>
      </template>

      <!-- 自定义模式:compact 形态 ── 一行 + 折叠 -->
      <template v-else-if="customCount > 0 && triggerStyle === 'compact'">
        <span class="compact-text">
          <strong>{{ customCount }}</strong>
          {{ t('component.allOrCustomPicker.triggerCompactSuffix') }}
          <span class="compact-preview" v-if="customCount > 0">
            ({{ compactPreview }}{{ customCount > 3 ? '...' : '' }})
          </span>
        </span>
      </template>

      <!-- 空 -->
      <span v-else class="empty-text">
        {{ triggerLabels?.empty ?? t('component.allOrCustomPicker.triggerEmpty') }}
      </span>
    </div>

    <!-- ============ 操作按钮 ============ -->
    <div class="trigger-actions">
      <!-- 清空按钮(仅在有值时显示;hover 加亮)-->
      <a-tooltip
        :title="t('common.cleanText')"
        v-if="showClear && hasValue && !disabled"
      >
        <a-button
          type="text"
          size="small"
          class="clear-btn"
          @click.stop="handleClear"
        >
          <CloseCircleFilled />
        </a-button>
      </a-tooltip>
      <!-- 选择按钮 -->
      <a-button
        :disabled="disabled"
        type="primary"
        ghost
        size="small"
        @click="openPicker"
        class="trigger-btn"
      >
        <template #icon><EditOutlined /></template>
        {{ triggerLabels?.button ?? t('component.allOrCustomPicker.triggerButton') }}
      </a-button>
    </div>

    <!-- 内置弹窗 -->
    <PickerModal
      @register="registerModal"
      :title="title ?? t('component.allOrCustomPicker.defaultTitle')"
      :initialValue="modelValue"
      :allValue="allValue"
      :allowAll="allowAll"
      :multiple="multiple"
      :maxCount="maxCount"
      :pageApi="pageApi"
      :detailApi="detailApi"
      :pageParams="pageParams"
      :valueField="valueField"
      :labelField="labelField"
      :searchField="searchField"
      :searchFields="searchFields"
      :descFields="descFields"
      :filters="filters"
      :width="width"
      :pageSize="pageSize"
      :searchPlaceholder="searchPlaceholder"
      :emptyText="emptyText"
      :displayLimit="displayLimit"
      :headerBadgeField="headerBadgeField"
      :headerBadgeDictType="headerBadgeDictType"
      :statusField="statusField"
      :statusOnlineValues="statusOnlineValues"
      :timeField="timeField"
      :isDisabled="isDisabled"
      @confirm="onConfirm"
      @cancel="onCancel"
    >
      <template #card="slotProps"><slot name="card" v-bind="slotProps" /></template>
    </PickerModal>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive, watch, PropType } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BizConstant } from '/@/enums/biz/common';
  import {
    CheckSquareOutlined,
    CloseCircleFilled,
    EditOutlined,
  } from '@ant-design/icons-vue';
  import PickerModal from './PickerModal.vue';
  import type {
    PickerDescField,
    PickerDetailApi,
    PickerExpose,
    PickerFilter,
    PickerPageRequest,
    PickerPageResponse,
    PickerRawValue,
    PickerSearchField,
    PickerTriggerLabels,
    PickerTriggerStyle,
    PickerValue,
  } from './types';

  defineOptions({ name: 'AllOrCustomPicker' });

  // ============================== Props ==============================
  const props = defineProps({
    /** v-model 值;'all' 或数组 */
    modelValue: { type: [String, Array] as PropType<PickerValue>, default: () => [] },
    /** 弹窗标题 */
    title: { type: String, default: '' },
    /** 通配字面值,默认对齐全局 {@link BizConstant.ALL}({@code 'all'}) */
    allValue: { type: String, default: BizConstant.ALL },
    /** 是否允许"全部"模式 */
    allowAll: { type: Boolean, default: true },
    /** 多选 / 单选 */
    multiple: { type: Boolean, default: true },
    /** 多选最大数 */
    maxCount: { type: Number, default: 0 },
    /** 分页 API */
    pageApi: {
      type: Function as PropType<(p: PickerPageRequest) => Promise<PickerPageResponse | any>>,
      required: true,
    },
    /** 反查 API ── 用于 modelValue 已有值的回显标签 */
    detailApi: { type: Function as PropType<PickerDetailApi>, default: undefined },
    /** API 额外固定参数 */
    pageParams: { type: Object, default: () => ({}) },
    /** 值字段 */
    valueField: { type: String, default: 'id' },
    /** 标题字段 */
    labelField: { type: String, default: 'name' },
    /** 搜索字段(默认 = labelField) */
    searchField: { type: String, default: '' },
    /**
     * 多字段搜索配置 ── 配置后 PickerModal 渲染 N 个独立输入框(类似列表搜索栏),
     * 业务 pageApi 通过 {@code req.searchValues} 拿到 {字段 → 输入值} map.
     */
    searchFields: { type: Array as PropType<PickerSearchField[]>, default: () => [] },
    /** 卡片副信息 */
    descFields: { type: Array as PropType<PickerDescField[]>, default: () => [] },
    /** 顶部过滤项 */
    filters: { type: Array as PropType<PickerFilter[]>, default: () => [] },
    /** 弹窗宽度 */
    width: { type: [String, Number], default: 1000 },
    /** 每页大小 */
    pageSize: { type: Number, default: 12 },
    /** 搜索 placeholder */
    searchPlaceholder: { type: String, default: '' },
    /** 空数据提示 */
    emptyText: { type: String, default: '' },
    /** 已选面板默认显示数 */
    displayLimit: { type: Number, default: 12 },
    /** 卡片右上角徽标字段(如产品 protocolType) */
    headerBadgeField: { type: String, default: '' },
    /** 徽标字段对应的字典 key(可选) */
    headerBadgeDictType: { type: String, default: '' },
    /** 卡片底部右下状态点字段(如设备 connectStatus) */
    statusField: { type: String, default: '' },
    /** 状态字段被视为"在线/启用"的值集合 */
    statusOnlineValues: {
      type: Array as PropType<Array<string | number>>,
      default: () => [],
    },
    /** 卡片底部左下时间字段(如 createdTime) */
    timeField: { type: String, default: '' },
    /** 触发器形态 */
    triggerStyle: {
      type: String as PropType<PickerTriggerStyle>,
      default: 'tags',
    },
    /** 触发器自定义文案 */
    triggerLabels: { type: Object as PropType<PickerTriggerLabels>, default: () => ({}) },
    /** 触发器 tags 形态最多预览几个 */
    previewLimit: { type: Number, default: 5 },
    /** 是否显示清空按钮 */
    showClear: { type: Boolean, default: true },
    /** 禁用 */
    disabled: { type: Boolean, default: false },
    /** 禁用某项的判断函数(传给 modal) */
    isDisabled: {
      type: Function as PropType<(record: any) => boolean>,
      default: () => false,
    },
  });

  const emit = defineEmits<{
    (e: 'update:modelValue', v: PickerValue): void;
    (e: 'change', v: PickerValue): void;
    (e: 'clear'): void;
  }>();

  const { t } = useI18n();
  const [registerModal, { openModal }] = useModal();

  /** 弹窗是否打开(用于 trigger 高亮) */
  const modalOpen = ref(false);

  /**
   * 触发器侧的 record 缓存 ── 用于在不打开弹窗的情况下,把 modelValue 数组反查出
   * record 详情显示标签;弹窗内有自己的 recordMap,这里独立维护一份给触发器用。
   */
  const triggerRecordMap = reactive(new Map<PickerRawValue, Record<string, any>>());

  // ============================== Derived ==============================

  const isAllMode = computed(() => {
    const v = props.modelValue;
    if (v === props.allValue) return true;
    if (Array.isArray(v) && v.length === 1 && v[0] === props.allValue) return true;
    return false;
  });

  const customCount = computed(() => {
    const v = props.modelValue;
    if (Array.isArray(v) && !isAllMode.value) return v.length;
    return 0;
  });

  const hasValue = computed(() => isAllMode.value || customCount.value > 0);

  const displayedSelectedTags = computed<PickerRawValue[]>(() => {
    const v = props.modelValue;
    if (!Array.isArray(v) || isAllMode.value) return [];
    return v.slice(0, props.previewLimit) as PickerRawValue[];
  });

  /** compact 形态:挑前 3 个的 label 拼接 */
  const compactPreview = computed(() => {
    const v = props.modelValue;
    if (!Array.isArray(v) || isAllMode.value) return '';
    return v
      .slice(0, 3)
      .map((x) => tagLabel(x as PickerRawValue))
      .join(', ');
  });

  // ============================== 标签反查 ==============================

  /** 在外部 modelValue 变化时,异步反查未缓存的 records 用于 tag 标签显示 */
  watch(
    () => props.modelValue,
    async (v) => {
      if (!Array.isArray(v) || isAllMode.value || !props.detailApi) return;
      const unknown: PickerRawValue[] = [];
      for (const x of v) {
        if (!triggerRecordMap.has(x as PickerRawValue)) {
          unknown.push(x as PickerRawValue);
        }
      }
      if (unknown.length === 0) return;
      try {
        const arr = await props.detailApi(unknown);
        for (const r of arr ?? []) {
          const k = readPath(r, props.valueField) as PickerRawValue;
          if (k != null) triggerRecordMap.set(k, r);
        }
      } catch {
        // 反查失败 ── tag 退化为显示原始 value,不影响交互
      }
    },
    { immediate: true, deep: true },
  );

  function readPath(record: any, path: string): any {
    if (!record || !path) return undefined;
    return path.split('.').reduce((acc, k) => (acc == null ? acc : acc[k]), record);
  }
  function tagLabel(v: PickerRawValue): string {
    const r = triggerRecordMap.get(v);
    if (r) {
      const lab = readPath(r, props.labelField);
      return lab == null ? String(v) : String(lab);
    }
    return String(v);
  }

  // ============================== Handlers ==============================

  /**
   * 打开弹窗。
   * <p>主动 blur 当前 focused 元素以规避 ant-design-vue Modal 的 a11y 警告
   * "Blocked aria-hidden on an element because its descendant retained focus"。
   */
  function openPicker() {
    if (props.disabled) return;
    if (typeof document !== 'undefined' && document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
    modalOpen.value = true;
    openModal(true);
  }
  function onConfirm(v: PickerValue) {
    emit('update:modelValue', v);
    emit('change', v);
    modalOpen.value = false;
  }
  function onCancel() {
    modalOpen.value = false;
  }
  /** 触发器上的 ✕ ── 完全清空(等价 selectAll=false + 数组清空) */
  function handleClear() {
    emit('update:modelValue', [] as PickerValue);
    emit('change', [] as PickerValue);
    emit('clear');
  }
  /** 触发器 tag 上的 ✕ ── 移除单项 */
  function removeOne(v: PickerRawValue) {
    if (!Array.isArray(props.modelValue) || isAllMode.value) return;
    const next = (props.modelValue as PickerRawValue[]).filter((x) => x !== v);
    emit('update:modelValue', next);
    emit('change', next);
  }

  // ============================== Expose ==============================

  defineExpose<PickerExpose>({
    open: () => openPicker(),
    close: () => {
      openModal(false);
      modalOpen.value = false;
    },
    clear: () => handleClear(),
    selectAll: () => {
      emit('update:modelValue', props.allValue);
      emit('change', props.allValue);
    },
    reload: () => {
      // 触发关闭再开 ── 简化版重载
      openModal(false);
      setTimeout(() => openModal(true), 0);
    },
  });
</script>

<style lang="less" scoped>
  .aoc-trigger {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 6px 4px 10px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    background: #ffffff;
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease;
    min-height: 36px;

    &:hover:not(.disabled) {
      border-color: #1966ff;
    }
    &.focused {
      border-color: #1966ff;
      box-shadow: 0 0 0 2px rgba(25, 102, 255, 0.12);
    }
    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;
      background: #f5f5f5;
    }

    // tags 形态:可换行
    &.style-tags .trigger-display {
      flex-wrap: wrap;
    }
    // count / compact 形态:单行
    &.style-count .trigger-display,
    &.style-compact .trigger-display {
      flex-wrap: nowrap;
      overflow: hidden;
      white-space: nowrap;
    }
  }

  .trigger-display {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    min-height: 28px;
    padding: 2px 0;
    overflow: hidden;
    min-width: 0;
  }

  .trigger-actions {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .all-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-weight: 500;
    margin: 0;
  }

  .trigger-item-tag {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin: 0;
  }

  .count-text {
    font-size: 13px;
    color: #1966ff;
    font-weight: 500;
  }

  .compact-text {
    font-size: 13px;
    color: #2c2f33;

    strong {
      color: #1966ff;
      font-size: 14px;
      margin-right: 2px;
    }
  }

  .compact-preview {
    color: #8c8f99;
    margin-left: 4px;
  }

  .more-hint {
    font-size: 12px;
    color: #8c8f99;
  }

  .empty-text {
    font-size: 13px;
    color: #bfbfbf;
  }

  .clear-btn {
    color: #bfbfbf;
    transition: color 0.2s ease;
    padding: 0 6px;
    height: 26px;

    &:hover {
      color: #ff4d4f;
    }
  }

  .trigger-btn {
    height: 28px;
    padding: 0 12px;
  }
</style>
