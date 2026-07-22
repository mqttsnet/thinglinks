<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    :maskClosable="false"
    :keyboard="true"
    :width="width"
    :bodyStyle="{ padding: 0, background: '#f6f7fb' }"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="picker-shell">
      <!-- ================== 模式切换:全部 / 自定义 ================== -->
      <div class="mode-row" v-if="allowAll">
        <div class="mode-card" :class="{ active: mode === 'all' }" @click="switchMode('all')">
          <div class="mode-icon icon-all"><CheckSquareOutlined /></div>
          <div class="mode-text">
            <div class="mode-title">{{ t('component.allOrCustomPicker.modeAll') }}</div>
            <div class="mode-desc">{{ t('component.allOrCustomPicker.modeAllDesc') }}</div>
          </div>
          <div class="mode-check" v-if="mode === 'all'"><CheckOutlined /></div>
        </div>
        <div class="mode-card" :class="{ active: mode === 'custom' }" @click="switchMode('custom')">
          <div class="mode-icon icon-custom"><FilterOutlined /></div>
          <div class="mode-text">
            <div class="mode-title">{{ t('component.allOrCustomPicker.modeCustom') }}</div>
            <div class="mode-desc">{{ t('component.allOrCustomPicker.modeCustomDesc') }}</div>
          </div>
          <div class="mode-check" v-if="mode === 'custom'"><CheckOutlined /></div>
        </div>
      </div>

      <!-- ================== 自定义面板 ================== -->
      <!--
        PickerModal 经常作为 BasicForm 的 #slot 子组件被使用(如 bridge Edit.vue),
        虽然 Modal 本身走 teleport,但 Vue inject 链不会断,外层 FormItem 会把
        弹窗内任意 a-input / a-select 误收集为受控字段并报 "FormItem can only
        collect one field item" 警告. 用 ant 官方 a-form-item-rest 显式隔离.
      -->
      <a-form-item-rest>
      <div class="custom-pane" v-if="mode === 'custom'">
        <!-- ─── 顶部:搜索 + 过滤项 + 操作 ─── -->
        <div class="toolbar">
          <!-- 多字段搜索 ── 配置了 searchFields 时,每字段一个独立输入框(类似列表搜索) -->
          <template v-if="searchFields && searchFields.length">
            <a-input
              v-for="sf in searchFields"
              :key="sf.field"
              :value="searchValues[sf.field]"
              :placeholder="sf.placeholder ?? t('component.allOrCustomPicker.searchPlaceholder')"
              allow-clear
              :style="{ maxWidth: (sf.width ?? 200) + 'px' }"
              @update:value="(v: any) => onSearchFieldInput(sf.field, v)"
              @press-enter="onSearch"
            />
          </template>
          <!-- 单字段搜索 ── 未配置 searchFields 时退化为单 keyword 输入框 -->
          <a-input-search
            v-else
            v-model:value="searchKeyword"
            :placeholder="searchPlaceholder ?? t('component.allOrCustomPicker.searchPlaceholder')"
            allow-clear
            @search="onSearch"
            @press-enter="onSearch"
            @change="onSearchInput"
            style="max-width: 280px"
          />
          <!-- 顶部过滤项(可选) -->
          <template v-if="filters && filters.length">
            <div class="filter-group" v-for="f in filters" :key="f.field">
              <span class="filter-label">{{ f.label }}:</span>
              <a-select
                v-if="(f.type ?? 'select') === 'select'"
                :value="filterValues[f.field]"
                :options="f.options"
                :allow-clear="f.allowClear ?? true"
                :style="{ width: (f.width ?? 140) + 'px' }"
                size="small"
                :placeholder="t('component.allOrCustomPicker.allFilter')"
                @update:value="(v: any) => onFilterChange(f.field, v)"
              />
              <a-radio-group
                v-else
                :value="filterValues[f.field]"
                :options="f.options"
                size="small"
                @change="(e: any) => onFilterChange(f.field, e?.target?.value)"
              />
            </div>
          </template>
          <span class="toolbar-spacer"></span>
          <a-tooltip :title="t('common.title.refresh')">
            <a-button size="small" type="text" @click="reload"><ReloadOutlined /></a-button>
          </a-tooltip>
        </div>

        <!-- ─── 卡片网格 ─── -->
        <a-spin :spinning="loading" wrapperClassName="grid-spin">
          <div class="card-grid" v-if="records.length">
            <div
              class="picker-card"
              :class="{ selected: isSelected(item), disabled: isItemDisabled(item) }"
              v-for="item in records"
              :key="String(getValue(item))"
              @click="!isItemDisabled(item) && toggleItem(item)"
            >
              <!-- 自定义卡片(slot) ── 默认渲染 -->
              <slot name="card" :record="item" :selected="isSelected(item)">
                <!-- header:名称(左) + badge(右) -->
                <div class="card-header">
                  <a-tooltip :title="getLabel(item)" placement="topLeft">
                    <span class="card-title">{{ getLabel(item) }}</span>
                  </a-tooltip>
                  <span v-if="headerBadgeText(item)" class="card-badge">
                    {{ headerBadgeText(item) }}
                  </span>
                </div>
                <!-- body:多行 info_row -->
                <div v-if="descFields.length" class="card-fields">
                  <div class="card-field" v-for="f in descFields" :key="f.field">
                    <span class="card-label">{{ f.label }}</span>
                    <a-tag
                      v-if="f.style === 'tag'"
                      :color="f.tagColor ? f.tagColor(readPath(item, f.field), item) : 'default'"
                    >
                      {{ formatField(item, f) }}
                    </a-tag>
                    <a-tooltip v-else :title="formatField(item, f)" placement="topLeft">
                      <span class="card-value">{{ formatField(item, f) }}</span>
                    </a-tooltip>
                  </div>
                </div>
                <!-- footer:时间(左) + 状态点(右) -->
                <div v-if="hasFooter(item)" class="card-footer">
                  <span class="card-time">{{ footerTimeText(item) }}</span>
                  <span
                    v-if="statusField"
                    class="card-status-dot"
                    :class="{ online: isOnline(item), offline: !isOnline(item) }"
                  ></span>
                </div>
              </slot>
              <!-- 选中圆形 ✓ -->
              <div class="card-check-mark" v-if="isSelected(item)">✓</div>
            </div>
          </div>
          <a-empty
            v-else
            :description="emptyText ?? t('component.allOrCustomPicker.empty')"
            class="grid-empty"
          />
        </a-spin>

        <!-- ─── 分页 ─── -->
        <div class="pagination-row" v-if="total > 0">
          <a-pagination
            :current="pageNum"
            :pageSize="pageSize"
            :total="total"
            :show-size-changer="false"
            :show-total="(t: number) => paginationText(t)"
            size="small"
            @change="onPageChange"
          />
        </div>

        <!-- ─── 已选面板:跨页保留 + 单删 + 清空 + 反查回显 ─── -->
        <div class="selected-pane" v-if="multiple">
          <div class="selected-head">
            <span class="selected-title">
              <BookOutlined />
              {{
                t('component.allOrCustomPicker.selectedCount', { count: selectedSet.size })
              }}
            </span>
            <a-button
              v-if="selectedSet.size > 0"
              type="link"
              size="small"
              danger
              @click="clearSelected"
            >
              <DeleteOutlined />
              {{ t('common.cleanText') }}
            </a-button>
          </div>
          <div class="selected-tags" v-if="selectedSet.size > 0">
            <a-tag
              v-for="v in displayedSelected"
              :key="String(v)"
              color="blue"
              closable
              @close="removeOne(v)"
            >
              {{ tagLabel(v) }}
            </a-tag>
            <a-button
              v-if="selectedSet.size > displayLimit && !expandSelected"
              type="link"
              size="small"
              @click="expandSelected = true"
            >
              {{
                t('component.allOrCustomPicker.expandMore', {
                  count: selectedSet.size - displayLimit,
                })
              }}
            </a-button>
            <a-button
              v-else-if="selectedSet.size > displayLimit && expandSelected"
              type="link"
              size="small"
              @click="expandSelected = false"
            >
              {{ t('component.allOrCustomPicker.collapse') }}
            </a-button>
          </div>
          <div v-else class="selected-empty">
            {{ t('component.allOrCustomPicker.noSelection') }}
          </div>
        </div>
      </div>
      </a-form-item-rest>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, PropType } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDebounceFn } from '@vueuse/core';
  import { useDict } from '/@/components/Dict';
  import { BizConstant } from '/@/enums/biz/common';
  import {
    BookOutlined,
    CheckOutlined,
    CheckSquareOutlined,
    DeleteOutlined,
    FilterOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';
  import type {
    PickerDescField,
    PickerDetailApi,
    PickerFilter,
    PickerPageRequest,
    PickerPageResponse,
    PickerRawValue,
    PickerSearchField,
    PickerValue,
  } from './types';

  defineOptions({ name: 'AllOrCustomPickerModal' });

  // ============================== Props ==============================
  const props = defineProps({
    title: { type: String, default: '' },
    /** 当前值;'all' 或数组 */
    initialValue: { type: [String, Array] as PropType<PickerValue>, default: () => [] },
    /** 通配字面值,默认对齐全局 {@link BizConstant.ALL}({@code 'all'}) */
    allValue: { type: String, default: BizConstant.ALL },
    /** 是否允许"全部"模式 */
    allowAll: { type: Boolean, default: true },
    /** 单选 / 多选;false=单选(选中即关闭) */
    multiple: { type: Boolean, default: true },
    /** 多选最大数;0=不限 */
    maxCount: { type: Number, default: 0 },
    /** 分页 API ── 内部传 PickerPageRequest,调用方适配 */
    pageApi: {
      type: Function as PropType<(p: PickerPageRequest) => Promise<PickerPageResponse | any>>,
      required: true,
    },
    /** 反查 API ── 用于回显 modelValue 已有值但当前页拉不到的 record 详情 */
    detailApi: { type: Function as PropType<PickerDetailApi>, default: undefined },
    /** API 额外固定参数(透传到 PickerPageRequest.extra) */
    pageParams: { type: Object, default: () => ({}) },
    /** 字段:取作为 value */
    valueField: { type: String, default: 'id' },
    /** 字段:卡片标题 */
    labelField: { type: String, default: 'name' },
    /** 字段:搜索字段(默认 = labelField) */
    searchField: { type: String, default: '' },
    /**
     * 多字段搜索配置 ── 配置后渲染 N 个独立输入框(类似列表搜索栏);
     * 业务侧 pageApi 通过 {@link PickerPageRequest#searchValues} 拿到 {字段名 → 输入值} map.
     * 不配置则使用单 keyword 输入框.
     */
    searchFields: { type: Array as PropType<PickerSearchField[]>, default: () => [] },
    /** 卡片副信息字段 */
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
    /** 是否禁用某项的判断函数 */
    isDisabled: {
      type: Function as PropType<(record: any) => boolean>,
      default: () => false,
    },
  });

  const emit = defineEmits<{
    (e: 'confirm', value: PickerValue): void;
    (e: 'cancel'): void;
  }>();

  const { t } = useI18n();
  const { getDictLabel, initGetDictList } = useDict();
  const [registerModal, { closeModal }] = useModalInner(() => {
    // 弹窗打开时预加载字典(headerBadge / descFields 中的 dictType)
    if (props.headerBadgeDictType) {
      initGetDictList(props.headerBadgeDictType);
    }
  });

  // ============================== 内部状态 ==============================

  /** 'all' / 'custom' 模式 */
  const mode = ref<'all' | 'custom'>('all');

  /** 已选 valueField 集合(原始类型,跨页保留) */
  const selectedSet = ref<Set<PickerRawValue>>(new Set());

  /** 已知 value→record 映射(给 tag 显示标签 + 已选面板用) */
  const recordMap = reactive(new Map<PickerRawValue, Record<string, any>>());

  /** 列表 */
  const records = ref<Record<string, any>[]>([]);
  const total = ref(0);
  const pageNum = ref(1);
  const loading = ref(false);

  /** 单字段搜索(未配置 searchFields 时使用) */
  const searchKeyword = ref('');

  /** 多字段搜索(配置 searchFields 时使用) */
  const searchValues = reactive<Record<string, any>>({});

  /** 顶部过滤项当前值 */
  const filterValues = reactive<Record<string, any>>({});

  /** 已选面板展开/折叠 */
  const expandSelected = ref(false);

  // ============================== 派生 ==============================

  const displayedSelected = computed<PickerRawValue[]>(() => {
    const arr = Array.from(selectedSet.value);
    if (expandSelected.value) return arr;
    return arr.slice(0, props.displayLimit);
  });

  // ============================== 初始化 / 反查回显 ==============================

  watch(
    () => props.initialValue,
    async (v) => {
      // 重置过滤项默认值(只在打开时一次性)
      if (props.filters?.length) {
        for (const f of props.filters) {
          if (filterValues[f.field] === undefined && f.defaultValue !== undefined) {
            filterValues[f.field] = f.defaultValue;
          }
        }
      }

      if (v === props.allValue || (Array.isArray(v) && v.length === 1 && v[0] === props.allValue)) {
        mode.value = 'all';
        selectedSet.value = new Set();
        return;
      }

      if (Array.isArray(v) && v.length > 0) {
        mode.value = 'custom';
        const next = new Set<PickerRawValue>(v as PickerRawValue[]);
        selectedSet.value = next;
        // 反查未缓存的 records,用于已选面板正确显示标签
        const unknown: PickerRawValue[] = [];
        for (const x of next) {
          if (!recordMap.has(x)) unknown.push(x);
        }
        if (unknown.length > 0 && props.detailApi) {
          try {
            const detailRecords = await props.detailApi(unknown);
            for (const r of detailRecords ?? []) {
              const k = readPath(r, props.valueField) as PickerRawValue;
              if (k != null) recordMap.set(k, r);
            }
          } catch {
            // 反查失败时 ── tag 退化为原始 value 字符串显示,不影响功能
          }
        }
        loadPage(1);
        return;
      }

      // 空 ── 默认进入"全部"(allowAll=true 时)或"自定义"
      mode.value = props.allowAll ? 'all' : 'custom';
      selectedSet.value = new Set();
      if (mode.value === 'custom') loadPage(1);
    },
    { immediate: true },
  );

  // ============================== 工具 ==============================

  function readPath(record: any, path: string): any {
    if (!record || !path) return undefined;
    return path.split('.').reduce((acc, k) => (acc == null ? acc : acc[k]), record);
  }
  function getValue(record: any): PickerRawValue {
    return readPath(record, props.valueField) as PickerRawValue;
  }
  function getLabel(record: any): string {
    const v = readPath(record, props.labelField);
    return v == null ? '' : String(v);
  }
  function formatField(record: any, f: PickerDescField): string {
    const raw = readPath(record, f.field);
    return f.formatter ? f.formatter(raw, record) : raw == null ? '-' : String(raw);
  }
  function tagLabel(v: PickerRawValue): string {
    const r = recordMap.get(v);
    return r ? getLabel(r) : String(v);
  }
  function isSelected(record: any): boolean {
    return selectedSet.value.has(getValue(record));
  }

  // ===== 卡片增强 helper =====
  /** 卡片右上角徽标文本 */
  function headerBadgeText(record: any): string {
    if (!props.headerBadgeField || !record) return '';
    const v = readPath(record, props.headerBadgeField);
    if (v == null || v === '') return '';
    if (props.headerBadgeDictType) {
      return getDictLabel(props.headerBadgeDictType, String(v)) || String(v);
    }
    return String(v);
  }

  /** 卡片底部时间文本 */
  function footerTimeText(record: any): string {
    if (!props.timeField || !record) return '';
    return String(readPath(record, props.timeField) ?? '');
  }

  /** 状态字段命中 statusOnlineValues 即视为在线 */
  function isOnline(record: any): boolean {
    if (!props.statusField || !record) return false;
    const v = readPath(record, props.statusField);
    if (v == null) return false;
    if (props.statusOnlineValues.length === 0) return false;
    return props.statusOnlineValues.some((onv) => String(onv) === String(v));
  }

  /** footer 是否要渲染(timeField 或 statusField 任一有值就显示) */
  function hasFooter(record: any): boolean {
    if (props.timeField && readPath(record, props.timeField)) return true;
    if (props.statusField) return true;
    return false;
  }
  function isItemDisabled(record: any): boolean {
    if (props.isDisabled?.(record)) return true;
    if (
      props.maxCount > 0 &&
      !isSelected(record) &&
      selectedSet.value.size >= props.maxCount
    ) {
      return true;
    }
    return false;
  }
  function paginationText(tn: number): string {
    return t('component.allOrCustomPicker.pageTotal', { total: tn });
  }

  // ============================== 模式 ==============================

  function switchMode(target: 'all' | 'custom') {
    if (mode.value === target) return;
    mode.value = target;
    if (target === 'custom' && records.value.length === 0) {
      loadPage(1);
    }
  }

  // ============================== 列表 / 搜索 / 过滤 ==============================

  async function loadPage(np: number) {
    pageNum.value = np;
    loading.value = true;
    try {
      // 多字段搜索:取非空值组成 searchValues;否则走单 keyword.
      const multiFieldEntries = Object.entries(searchValues).filter(
        ([, v]) => v !== undefined && v !== null && String(v).length > 0,
      );
      const req: PickerPageRequest = {
        pageNum: np,
        pageSize: props.pageSize,
        keyword: searchKeyword.value || undefined,
        searchValues: multiFieldEntries.length > 0
          ? Object.fromEntries(multiFieldEntries)
          : undefined,
        filters: { ...filterValues },
        extra: { ...props.pageParams },
      };
      const res: any = await props.pageApi(req);
      // 兼容多种返回 shape
      const data = res?.records || res?.list ? res : res?.data ?? res;
      records.value = Array.isArray(data?.records)
        ? data.records
        : Array.isArray(data?.list)
          ? data.list
          : Array.isArray(data)
            ? data
            : [];
      total.value = Number(data?.total ?? records.value.length);

      // 缓存当前页 records 用于已选 tag 标签
      records.value.forEach((r) => {
        const v = getValue(r);
        if (v != null && !recordMap.has(v)) recordMap.set(v, r);
      });
    } catch {
      records.value = [];
      total.value = 0;
    } finally {
      loading.value = false;
    }
  }

  /** 防抖搜索:输入即触发(300ms) */
  const debouncedReload = useDebounceFn(() => loadPage(1), 300);
  function onSearchInput(_e: any) {
    debouncedReload();
  }
  /** 多字段搜索 ── 单个字段值变更后防抖 reload */
  function onSearchFieldInput(field: string, value: any) {
    searchValues[field] = value;
    debouncedReload();
  }
  function onSearch() {
    loadPage(1);
  }
  function onPageChange(np: number) {
    loadPage(np);
  }
  function reload() {
    loadPage(pageNum.value);
  }
  function onFilterChange(field: string, value: any) {
    filterValues[field] = value;
    loadPage(1);
  }

  // ============================== 选择 ==============================

  function toggleItem(record: any) {
    const v = getValue(record);
    if (v == null) return;
    const next = new Set(selectedSet.value);
    if (next.has(v)) next.delete(v);
    else next.add(v);
    selectedSet.value = next;
    recordMap.set(v, record);

    // 单选模式:选中即提交关闭
    if (!props.multiple && next.has(v)) {
      handleConfirm();
    }
  }
  function removeOne(v: PickerRawValue) {
    const next = new Set(selectedSet.value);
    next.delete(v);
    selectedSet.value = next;
  }
  function clearSelected() {
    selectedSet.value = new Set();
  }

  // ============================== 提交 ==============================

  function handleConfirm() {
    if (mode.value === 'all') {
      emit('confirm', props.allValue);
    } else {
      emit('confirm', Array.from(selectedSet.value) as PickerValue);
    }
    closeModal();
  }
  function handleCancel() {
    emit('cancel');
    closeModal();
  }
</script>

<style lang="less" scoped>
  .picker-shell {
    padding: 16px 20px 8px;
  }

  // ============================== 模式切换卡片 ==============================
  .mode-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 16px;
  }
  .mode-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 18px;
    background: #ffffff;
    border: 2px solid transparent;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
    position: relative;

    &:hover {
      border-color: @primary-color;
      transform: translateY(-1px);
      box-shadow: 0 4px 10px rgb(15 23 42 / 6%);
    }
    &.active {
      border-color: @primary-color;
      background: #fff;
      box-shadow: 0 4px 14px rgb(15 23 42 / 8%);
    }
  }
  .mode-icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;

    &.icon-all {
      background: rgba(19, 222, 185, 0.12);
      color: #13deb9;
    }
    &.icon-custom {
      background: #f5f7fa;
      color: @primary-color;
    }
  }
  .mode-text {
    flex: 1;
    min-width: 0;
  }
  .mode-title {
    font-size: 15px;
    font-weight: 600;
    color: #050708;
    margin-bottom: 2px;
  }
  .mode-desc {
    font-size: 12px;
    color: #8c8f99;
    line-height: 1.5;
  }
  .mode-check {
    position: absolute;
    top: 10px;
    right: 12px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: @primary-color;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }

  // ============================== 自定义面板 ==============================
  .custom-pane {
    background: #ffffff;
    border-radius: 10px;
    padding: 14px 16px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  }

  // ── 顶部工具条 ──
  .toolbar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 12px;
  }
  .filter-group {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  .filter-label {
    font-size: 12px;
    color: #5a5d66;
  }
  .toolbar-spacer {
    flex: 1;
  }

  // ── 卡片网格 ──
  .grid-spin {
    min-height: 280px;
  }
  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
    max-height: 480px;
    overflow-y: auto;
    padding: 2px 4px 4px 2px;
  }
  .picker-card {
    position: relative;
    padding: 16px;
    border: 2px solid #f0f0f0;
    border-radius: 12px;
    background: #fff;
    cursor: pointer;
    transition: all 0.25s ease;
    overflow: hidden;
    min-height: 148px;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    // ⚠️ 关键 ── Grid 子项默认 min-width: auto = 内容固有宽度;长 clientId / 长 tag 会撑大本轨道,
    // 挤压同行其他 1fr 轨道变窄(就是"选中后某张卡变窄"现象的根因)。强制 0 让所有卡严格平分 1fr,
    // 再显式 width: 100% 锁定该 grid track 内的 stretch,选中态 border 加粗也不影响轨道宽度。
    min-width: 0;
    width: 100%;

    &:hover {
      border-color: @primary-color;
      box-shadow: 0 4px 16px rgb(15 23 42 / 6%);
      transform: translateY(-2px);
    }
    &.selected {
      border-color: @primary-color;
      background: #fff;
      box-shadow: 0 4px 16px rgb(15 23 42 / 8%);
    }
    &.disabled {
      cursor: not-allowed;
      opacity: 0.5;
      &:hover {
        transform: none;
        box-shadow: none;
      }
    }
  }

  // header:名称(左)+ 右上角徽标
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    gap: 8px;

    .card-title {
      flex: 1;
      font-size: 15px;
      font-weight: 600;
      color: #1a1a2e;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      padding-right: 22px; // 给右上角 ✓ 让位
    }
    .card-badge {
      flex-shrink: 0;
      font-size: 12px;
      color: @primary-color;
      background: #f5f7fa;
      padding: 2px 8px;
      border-radius: 4px;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // body:多行字段
  .card-fields {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    // flex item 在 flex 容器内默认 min-width:auto,长内容会反向撑大父级;
    // 显式 0 + 100% 双重锁定,确保 .card-fields 严格跟随 .picker-card 宽度。
    min-width: 0;
    width: 100%;

    .card-field {
      display: flex;
      align-items: baseline;
      font-size: 13px;
      line-height: 1.6;
      overflow: hidden;
      // 防止 flex item 默认 min-width:auto 把外层 grid 撑宽
      min-width: 0;
      width: 100%;
    }
    .card-label {
      flex-shrink: 0;
      color: #8c8c8c;
      min-width: 72px;

      &::after {
        content: '：';
      }
    }
    .card-value {
      flex: 1;
      // 关键:flex item 在 flex 容器里要 min-width: 0 才能正常 ellipsis;
      // 不设 → 长 value 会撑爆父级 grid track。
      min-width: 0;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // footer:时间 + 状态点
  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
    padding-top: 8px;
    border-top: 1px solid #f5f5f5;

    .card-time {
      font-size: 12px;
      color: #bfbfbf;
    }
    .card-status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;

      &.online {
        background: #52c41a;
        box-shadow: 0 0 4px rgba(82, 196, 26, 0.5);
      }
      &.offline {
        background: #d9d9d9;
      }
    }
  }

  // 选中圆形 ✓
  .card-check-mark {
    position: absolute;
    top: 8px;
    right: 12px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: @primary-color;
    color: #fff;
    font-size: 14px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .grid-empty {
    padding: 50px 0;
  }

  // ── 分页 ──
  .pagination-row {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }

  // ── 已选面板 ──
  .selected-pane {
    margin-top: 12px;
    padding: 10px 12px;
    background: #f6f7fb;
    border-radius: 8px;
    border: 1px dashed #e2e6f0;
  }
  .selected-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  .selected-title {
    font-size: 13px;
    font-weight: 500;
    color: #5a5d66;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  .selected-empty {
    font-size: 12px;
    color: #bfbfbf;
    padding: 4px 0;
  }
</style>
