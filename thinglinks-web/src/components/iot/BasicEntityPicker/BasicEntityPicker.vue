<template>
  <div class="bep-trigger" :class="{ disabled, focused: modalOpen }">
    <!-- ============ 触发器显示区 ============ -->
    <div class="bep-trigger-display" @click="openPicker">
      <!-- 多选 tags 形态 -->
      <template v-if="multiple && hasValue">
        <a-tag
          v-for="v in displayedTags"
          :key="String(v)"
          color="processing"
          closable
          class="bep-trigger-tag"
          @close.stop="removeOne(v)"
        >
          {{ tagLabel(v) }}
        </a-tag>
        <span v-if="customCount > previewLimit" class="bep-more-hint">
          +{{ customCount - previewLimit }}
        </span>
      </template>

      <!-- 单选:input 形态 -->
      <template v-else-if="!multiple && hasValue">
        <a-tag color="processing" class="bep-trigger-tag">
          {{ singleLabel }}
        </a-tag>
      </template>

      <!-- 空 -->
      <span v-else class="bep-trigger-empty">
        {{ triggerLabels?.empty ?? t('component.basicEntityPicker.triggerEmpty') }}
      </span>
    </div>

    <!-- ============ 操作按钮区 ============ -->
    <div class="bep-trigger-actions">
      <a-button
        v-if="showClear && hasValue && !disabled"
        type="text"
        size="small"
        class="bep-trigger-clear"
        @click.stop="handleClear"
      >
        <template #icon><CloseCircleOutlined /></template>
      </a-button>
      <a-button type="primary" size="small" :disabled="disabled" @click.stop="openPicker">
        <template #icon><AppstoreOutlined /></template>
        {{ triggerLabels?.button ?? t('component.basicEntityPicker.triggerButton') }}
      </a-button>
    </div>

    <PickerModal @register="registerModal" @success="onModalSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, PropType } from 'vue';
  import { CloseCircleOutlined, AppstoreOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import PickerModal from './PickerModal.vue';
  import type {
    EntityRawValue,
    EntityValue,
    EntityDescField,
    EntityFilter,
    EntityPageRequest,
    EntityPageResponse,
    EntityDetailApi,
    EntityTriggerLabels,
    EntityPickerExpose,
  } from './types';

  defineOptions({ name: 'BasicEntityPicker' });

  const props = defineProps({
    /** v-model:单选 string,多选 string[] */
    modelValue: {
      type: [String, Number, Array, null] as PropType<EntityValue>,
      default: () => null,
    },
    /** true=多选,false=单选(默认) */
    multiple: { type: Boolean, default: false },
    /** Modal 标题 */
    title: { type: String, default: '' },
    /** Modal 宽度 */
    width: { type: [String, Number], default: 900 },
    /** 每页大小 */
    pageSize: { type: Number, default: 12 },
    /** 分页 API ── 必填 */
    pageApi: {
      type: Function as PropType<(req: EntityPageRequest) => Promise<EntityPageResponse | any>>,
      required: true,
    },
    /** 反查 API ── 用于 modelValue 已有值时反查 record 显示 label */
    detailApi: { type: Function as PropType<EntityDetailApi>, default: undefined },
    /** API 固定参数(透传到 pageApi.extra) */
    pageParams: { type: Object, default: () => ({}) },
    /** 值字段(默认 id) */
    valueField: { type: String, default: 'id' },
    /** 标题字段(默认 name) */
    labelField: { type: String, default: 'name' },
    /** 搜索字段(默认 = labelField) */
    searchField: { type: String, default: '' },
    /** 卡片副信息字段 */
    descFields: { type: Array as PropType<EntityDescField[]>, default: () => [] },
    /** 顶部过滤器 */
    filters: { type: Array as PropType<EntityFilter[]>, default: () => [] },
    /** 搜索框 placeholder */
    searchPlaceholder: { type: String, default: '' },
    /** 空数据提示 */
    emptyText: { type: String, default: '' },
    /** 触发器自定义文案 */
    triggerLabels: { type: Object as PropType<EntityTriggerLabels>, default: () => ({}) },
    /** 多选触发器最多预览 tag 数 */
    previewLimit: { type: Number, default: 5 },
    /** 是否显示清空按钮 */
    showClear: { type: Boolean, default: true },
    /** 禁用 */
    disabled: { type: Boolean, default: false },
    /** 单条是否禁用判断函数(传给 modal) */
    isItemDisabled: {
      type: Function as PropType<(record: any) => boolean>,
      default: () => false,
    },
    /** 卡片右上角徽标字段(如产品 protocolType / 设备 nodeType) */
    headerBadgeField: { type: String, default: '' },
    /** 徽标字段对应的字典 key(可选) */
    headerBadgeDictType: { type: String, default: '' },
    /** 卡片底部右下状态点字段(如设备 connectStatus / 产品 productStatus) */
    statusField: { type: String, default: '' },
    /** 状态字段被视为"在线/启用"的值集合 */
    statusOnlineValues: {
      type: Array as PropType<Array<string | number>>,
      default: () => [],
    },
    /** 卡片底部左下时间字段(如 createdTime) */
    timeField: { type: String, default: '' },
  });

  const emit = defineEmits<{
    (e: 'update:modelValue', v: EntityValue): void;
    (e: 'change', v: EntityValue): void;
    (e: 'clear'): void;
  }>();

  const { t } = useI18n();
  const [registerModal, { openModal }] = useModal();

  /** 弹窗是否打开(触发器高亮用) */
  const modalOpen = ref(false);

  /** 触发器侧 record 缓存 ── 在不打开 modal 时也能用 detailApi 反查 record 显示 label */
  const recordMap = reactive(new Map<EntityRawValue, any>());

  // ============================== Derived ==============================
  const hasValue = computed(() => {
    const v = props.modelValue;
    if (v == null) return false;
    if (Array.isArray(v)) return v.length > 0;
    return true;
  });

  const customCount = computed(() => {
    if (!props.multiple) return 0;
    return Array.isArray(props.modelValue) ? props.modelValue.length : 0;
  });

  const displayedTags = computed<EntityRawValue[]>(() => {
    if (!props.multiple) return [];
    const arr = Array.isArray(props.modelValue) ? (props.modelValue as EntityRawValue[]) : [];
    return arr.slice(0, props.previewLimit);
  });

  const singleLabel = computed(() => {
    if (props.multiple) return '';
    const v = props.modelValue;
    if (v == null) return '';
    return tagLabel(v as EntityRawValue);
  });

  function tagLabel(v: EntityRawValue): string {
    const r = recordMap.get(v);
    if (!r) return String(v);
    return String(r[props.labelField] ?? r[props.valueField] ?? v);
  }

  // ============================== 触发器交互 ==============================
  /**
   * 打开弹窗。
   * <p>主动 blur 当前 focused 元素以规避 ant-design-vue Modal 的 a11y 警告
   * "Blocked aria-hidden on an element because its descendant retained focus" ──
   * 触发按钮失焦后,Modal mask 给祖先加 aria-hidden=true 不会再跟"button focused"冲突。
   */
  function openPicker() {
    if (props.disabled) return;
    // 失焦当前活动元素(通常是触发按钮)
    if (typeof document !== 'undefined' && document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
    modalOpen.value = true;
    openModal(true, {
      modelValue: props.modelValue,
      multiple: props.multiple,
      title: props.title,
      width: props.width,
      pageSize: props.pageSize,
      searchPlaceholder: props.searchPlaceholder,
      emptyText: props.emptyText,
      valueField: props.valueField,
      labelField: props.labelField,
      searchField: props.searchField || props.labelField,
      descFields: props.descFields,
      filters: props.filters,
      pageParams: props.pageParams,
      isItemDisabled: props.isItemDisabled,
      pageApi: props.pageApi,
      detailApi: props.detailApi,
      // 卡片增强字段透传
      headerBadgeField: props.headerBadgeField,
      headerBadgeDictType: props.headerBadgeDictType,
      statusField: props.statusField,
      statusOnlineValues: props.statusOnlineValues,
      timeField: props.timeField,
    });
  }

  function onModalSuccess(v: EntityValue) {
    modalOpen.value = false;
    emit('update:modelValue', v);
    emit('change', v);
  }

  function handleClear() {
    const empty: EntityValue = props.multiple ? [] : null;
    emit('update:modelValue', empty);
    emit('change', empty);
    emit('clear');
  }

  function removeOne(v: EntityRawValue) {
    if (!props.multiple) return;
    const arr = Array.isArray(props.modelValue) ? (props.modelValue as EntityRawValue[]) : [];
    const next = arr.filter((x) => x !== v);
    emit('update:modelValue', next);
    emit('change', next);
  }

  // ============================== 触发器 record 缓存 ==============================
  // modelValue 变化时,把不在 recordMap 的 id 反查 detailApi 拉回来
  watch(
    () => props.modelValue,
    async (v) => {
      if (!props.detailApi) return;
      const ids: EntityRawValue[] = (
        Array.isArray(v) ? (v as EntityRawValue[]) : v != null ? [v as EntityRawValue] : []
      ).filter((id) => !recordMap.has(id));
      if (ids.length === 0) return;
      try {
        const list = await props.detailApi(ids);
        list.forEach((r) => {
          const k = r?.[props.valueField];
          if (k != null) recordMap.set(k, r);
        });
      } catch {
        /* ignore */
      }
    },
    { immediate: true },
  );

  // ============================== Expose imperative API ==============================
  defineExpose<EntityPickerExpose>({
    open: openPicker,
    close: () => openModal(false),
    clear: handleClear,
  });
</script>

<style lang="less" scoped>
  .bep-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    min-height: 32px;
    padding: 4px 8px;
    background: #fff;
    border: 1px solid #d0d4d9;
    border-radius: 4px;
    transition: all 0.18s ease;

    &:hover {
      border-color: #1677ff;
    }

    &.focused {
      border-color: #1677ff;
      box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
    }

    &.disabled {
      background: #f5f5f5;
      cursor: not-allowed;

      .bep-trigger-display {
        cursor: not-allowed;
      }
    }
  }

  .bep-trigger-display {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    min-height: 24px;

    .bep-trigger-tag {
      margin: 1px 0;
      font-size: 12px;

      :deep(.anticon-close) {
        margin-inline-start: 4px;
      }
    }

    .bep-trigger-empty {
      color: #b9bec5;
      font-size: 13px;
      padding-left: 4px;
    }

    .bep-more-hint {
      color: #1677ff;
      font-size: 12px;
      margin-left: 2px;
    }
  }

  .bep-trigger-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;

    .bep-trigger-clear {
      color: #b9bec5;

      &:hover {
        color: #f5222d;
      }
    }
  }
</style>
