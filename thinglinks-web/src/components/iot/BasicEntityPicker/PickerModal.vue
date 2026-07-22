<template>
  <BasicModal
    @register="registerModal"
    :title="title || t('component.basicEntityPicker.defaultTitle')"
    :maskClosable="false"
    :width="width"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="bep-shell">
      <!-- 顶部:搜索 + 过滤器 -->
      <div class="bep-toolbar">
        <a-input
          v-model:value="keyword"
          :placeholder="searchPlaceholder || t('component.basicEntityPicker.searchPlaceholder')"
          allow-clear
          class="bep-search"
          @change="onSearch"
        >
          <template #prefix><SearchOutlined /></template>
        </a-input>

        <template v-for="f in filters" :key="f.field">
          <a-select
            v-if="f.type === 'select'"
            v-model:value="filterValues[f.field]"
            :options="filterOptionsOf(f)"
            :placeholder="f.placeholder || f.label"
            allow-clear
            :style="{ width: (f.width || 160) + 'px' }"
            @change="onSearch"
          />
          <a-input
            v-else
            v-model:value="filterValues[f.field]"
            :placeholder="f.placeholder || f.label"
            allow-clear
            :style="{ width: (f.width || 160) + 'px' }"
            @change="onSearch"
          />
        </template>
      </div>

      <!-- 已选预览(多选才显示) -->
      <div v-if="multiple && Array.isArray(internalValue) && internalValue.length > 0" class="bep-selected">
        <span class="bep-selected-label">
          {{ t('component.basicEntityPicker.selectedCount', { count: internalValue.length }) }}:
        </span>
        <a-tag
          v-for="v in (internalValue as EntityRawValue[])"
          :key="String(v)"
          color="processing"
          closable
          @close.stop="removeOne(v)"
        >
          {{ tagLabel(v) }}
        </a-tag>
      </div>

      <!-- 卡片网格 -->
      <a-spin :spinning="loading">
        <div v-if="records.length === 0" class="bep-empty">
          <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" :description="emptyText || t('component.basicEntityPicker.empty')" />
        </div>

        <a-row v-else :gutter="[16, 16]" class="bep-grid">
          <a-col v-for="record in records" :key="getRecordKey(record)" :lg="12" :md="12" :sm="24" :xl="8" :xs="24" :xxl="8">
            <div
              class="bep-card"
              :class="{
                active: isSelected(record),
                disabled: isItemDisabled(record),
              }"
              @click="onCardClick(record)"
            >
              <!-- header:名称(左)+ badge(右) -->
              <div class="bep-card-header">
                <span class="bep-card-name" :title="getLabel(record)">{{ getLabel(record) }}</span>
                <span v-if="headerBadgeText(record)" class="bep-card-badge">{{ headerBadgeText(record) }}</span>
              </div>

              <!-- body:多行 info_row(label : value) -->
              <div v-if="descFields.length" class="bep-card-body">
                <div v-for="d in descFields" :key="d.field" class="bep-info-row" :title="getDescValue(record, d)">
                  <span class="bep-info-label">{{ d.label }}</span>
                  <span class="bep-info-value">{{ getDescValue(record, d) }}</span>
                </div>
              </div>

              <!-- footer:时间(左)+ 状态点(右) -->
              <div v-if="hasFooter(record)" class="bep-card-footer">
                <span class="bep-info-time">{{ footerTimeText(record) }}</span>
                <span v-if="statusField" class="bep-status-dot" :class="{ online: isOnline(record), offline: !isOnline(record) }"></span>
              </div>

              <!-- 选中标记 -->
              <div v-if="isSelected(record)" class="bep-check-mark">✓</div>
            </div>
          </a-col>
        </a-row>
      </a-spin>

      <!-- 分页 -->
      <div class="bep-pagination">
        <a-pagination
          v-model:current="current"
          v-model:pageSize="size"
          :total="total"
          size="small"
          :show-total="(t) => totalLabel(t)"
          show-size-changer
          show-quick-jumper
          :page-size-options="['12', '24', '48']"
          @change="loadData"
        />
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import { Empty } from 'ant-design-vue';
  import { SearchOutlined } from '@ant-design/icons-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDebounceFn } from '@vueuse/core';
  import { useDict } from '/@/components/Dict';
  import type {
    EntityRawValue,
    EntityValue,
    EntityDescField,
    EntityFilter,
    EntityPageRequest,
  } from './types';

  defineOptions({ name: 'BasicEntityPickerModal' });

  const emit = defineEmits<{
    (e: 'success', value: EntityValue): void;
    (e: 'register', ...args: any[]): void;
  }>();

  const { t } = useI18n();
  const { getDictList, initGetDictList, getDictLabel } = useDict();

  // ============================== Modal 入参缓存 ==============================
  const modelValueProp = ref<EntityValue>([]);
  const multiple = ref(true);
  const title = ref('');
  const width = ref<number | string>(900);
  const pageSize = ref(12);
  const searchPlaceholder = ref('');
  const emptyText = ref('');
  const valueField = ref('id');
  const labelField = ref('name');
  const searchField = ref('');
  const descFields = ref<EntityDescField[]>([]);
  const filters = ref<EntityFilter[]>([]);
  const pageParams = ref<Record<string, any>>({});
  const isItemDisabledFn = ref<(record: any) => boolean>(() => false);

  // ===== 卡片增强字段 =====
  /** 卡片右上角徽标字段(如产品 protocolType / 设备 nodeType) */
  const headerBadgeField = ref('');
  /** 徽标字段对应的字典 key(可选;有则走字典翻译) */
  const headerBadgeDictType = ref('');
  /** 卡片底部右下状态点字段(如设备 connectStatus / 产品 productStatus) */
  const statusField = ref('');
  /** 状态字段被视为"在线/启用"的值集合(命中即绿点,否则灰点) */
  const statusOnlineValues = ref<Array<string | number>>([]);
  /** 卡片底部左下时间字段(如 createdTime / updatedTime) */
  const timeField = ref('');

  // pageApi 由父组件传入
  let pageApi: ((req: EntityPageRequest) => Promise<any>) | null = null;
  let detailApi: ((values: EntityRawValue[]) => Promise<any[]>) | null = null;

  // ============================== 内部状态 ==============================
  const internalValue = ref<EntityValue>([]);
  const records = ref<any[]>([]);
  const recordMap = reactive(new Map<EntityRawValue, any>());
  const total = ref(0);
  const current = ref(1);
  const size = ref(12);
  const keyword = ref('');
  const filterValues = reactive<Record<string, any>>({});
  const loading = ref(false);

  // ============================== 工具方法 ==============================
  function getRecordKey(record: any): EntityRawValue {
    return record?.[valueField.value];
  }
  function getLabel(record: any): string {
    if (!record) return '';
    return String(record[labelField.value] ?? record[valueField.value] ?? '');
  }
  function getDescValue(record: any, d: EntityDescField): string {
    if (!record) return '';
    if (d.formatter) return d.formatter(record);
    const v = getNestedValue(record, d.field);
    if (v == null) return '-';
    if (d.dictType) return getDictLabel(d.dictType, String(v)) || String(v);
    return String(v);
  }
  function getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((a, k) => (a == null ? a : a[k]), obj);
  }

  function filterOptionsOf(f: EntityFilter): { label: string; value: any }[] {
    if (f.options && f.options.length) return f.options;
    if (f.dictType) {
      // 走 useDict 的字典数组(同步)
      const list = getDictList(f.dictType) || [];
      return list.map((d: any) => ({ label: d.label, value: d.value }));
    }
    return [];
  }

  function isSelected(record: any): boolean {
    const k = getRecordKey(record);
    if (multiple.value) {
      return Array.isArray(internalValue.value) && (internalValue.value as EntityRawValue[]).includes(k);
    }
    return internalValue.value === k;
  }

  // ===== 卡片增强 helper =====
  /** 卡片右上角徽标文本(走字典 / 否则原值);空字段或空值返回 '' 不渲染 */
  function headerBadgeText(record: any): string {
    if (!headerBadgeField.value || !record) return '';
    const v = getNestedValue(record, headerBadgeField.value);
    if (v == null || v === '') return '';
    if (headerBadgeDictType.value) {
      return getDictLabel(headerBadgeDictType.value, String(v)) || String(v);
    }
    return String(v);
  }

  /** 卡片底部时间文本 */
  function footerTimeText(record: any): string {
    if (!timeField.value || !record) return '';
    return String(getNestedValue(record, timeField.value) ?? '');
  }

  /** 是否在线(状态字段命中 statusOnlineValues 为绿点) */
  function isOnline(record: any): boolean {
    if (!statusField.value || !record) return false;
    const v = getNestedValue(record, statusField.value);
    if (v == null) return false;
    if (statusOnlineValues.value.length === 0) return false;
    return statusOnlineValues.value.some((onv) => String(onv) === String(v));
  }

  /** footer 是否要渲染(timeField 或 statusField 任一有值就显示) */
  function hasFooter(record: any): boolean {
    if (timeField.value && getNestedValue(record, timeField.value)) return true;
    if (statusField.value) return true;
    return false;
  }

  function isItemDisabled(record: any): boolean {
    return !!isItemDisabledFn.value?.(record);
  }

  function tagLabel(v: EntityRawValue): string {
    const r = recordMap.get(v);
    return r ? getLabel(r) : String(v);
  }

  function totalLabel(n: number) {
    return t('component.table.total', { total: n });
  }

  // ============================== 选择行为 ==============================
  function onCardClick(record: any) {
    if (isItemDisabled(record)) return;
    const k = getRecordKey(record);
    recordMap.set(k, record);

    if (multiple.value) {
      const arr = Array.isArray(internalValue.value)
        ? [...(internalValue.value as EntityRawValue[])]
        : [];
      const idx = arr.indexOf(k);
      if (idx >= 0) arr.splice(idx, 1);
      else arr.push(k);
      internalValue.value = arr;
    } else {
      // 单选:点击选中后立即关弹窗
      internalValue.value = k;
      emit('success', internalValue.value);
      closeModal();
    }
  }

  function removeOne(v: EntityRawValue) {
    if (!multiple.value) return;
    const arr = Array.isArray(internalValue.value)
      ? (internalValue.value as EntityRawValue[]).filter((x) => x !== v)
      : [];
    internalValue.value = arr;
  }

  // ============================== 数据加载 ==============================
  async function loadData() {
    if (!pageApi) return;
    loading.value = true;
    try {
      const req: EntityPageRequest = {
        pageNum: current.value,
        pageSize: size.value,
        keyword: keyword.value || undefined,
        filters: { ...filterValues },
        extra: { ...pageParams.value },
      };
      const res: any = await pageApi(req);
      const list: any[] = res?.records ?? res?.list ?? [];
      records.value = list;
      total.value = res?.total ?? list.length;
      // 把当前页 record 缓存进 recordMap,便于触发器/tag 回显
      list.forEach((r) => {
        const k = getRecordKey(r);
        if (k != null) recordMap.set(k, r);
      });
    } catch (e) {
      records.value = [];
      total.value = 0;
    } finally {
      loading.value = false;
    }
  }

  const onSearch = useDebounceFn(() => {
    current.value = 1;
    loadData();
  }, 350);

  /** 反查回显 ── 保留 modelValue 中已选但当前页没拉到的 record(给 tag 显示 label) */
  async function ensureSelectedRecords() {
    if (!detailApi) return;
    const ids = (
      multiple.value
        ? (Array.isArray(internalValue.value) ? (internalValue.value as EntityRawValue[]) : [])
        : (internalValue.value != null ? [internalValue.value as EntityRawValue] : [])
    ).filter((v) => v != null && !recordMap.has(v));
    if (ids.length === 0) return;
    try {
      const list = await detailApi(ids);
      list.forEach((r) => {
        const k = getRecordKey(r);
        if (k != null) recordMap.set(k, r);
      });
    } catch {
      /* ignore */
    }
  }

  // ============================== Modal 注册 ==============================
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });

    // 接收父组件参数
    modelValueProp.value = data?.modelValue ?? (data?.multiple ? [] : null);
    multiple.value = !!data?.multiple;
    title.value = data?.title || '';
    width.value = data?.width || 900;
    pageSize.value = data?.pageSize ?? 12;
    searchPlaceholder.value = data?.searchPlaceholder || '';
    emptyText.value = data?.emptyText || '';
    valueField.value = data?.valueField || 'id';
    labelField.value = data?.labelField || 'name';
    searchField.value = data?.searchField || labelField.value;
    descFields.value = Array.isArray(data?.descFields) ? data.descFields : [];
    filters.value = Array.isArray(data?.filters) ? data.filters : [];
    pageParams.value = data?.pageParams || {};
    isItemDisabledFn.value = typeof data?.isItemDisabled === 'function' ? data.isItemDisabled : () => false;

    // 卡片增强(向后兼容,缺省即不渲染对应槽位)
    headerBadgeField.value = data?.headerBadgeField || '';
    headerBadgeDictType.value = data?.headerBadgeDictType || '';
    statusField.value = data?.statusField || '';
    statusOnlineValues.value = Array.isArray(data?.statusOnlineValues) ? data.statusOnlineValues : [];
    timeField.value = data?.timeField || '';

    pageApi = typeof data?.pageApi === 'function' ? data.pageApi : null;
    detailApi = typeof data?.detailApi === 'function' ? data.detailApi : null;

    // 初始化内部状态
    internalValue.value = multiple.value
      ? (Array.isArray(modelValueProp.value)
          ? [...(modelValueProp.value as EntityRawValue[])]
          : [])
      : (modelValueProp.value as EntityRawValue) ?? null;

    keyword.value = '';
    Object.keys(filterValues).forEach((k) => delete filterValues[k]);
    current.value = 1;
    size.value = pageSize.value;

    // 字典预加载(过滤器 + descFields + headerBadge 字典翻译用)
    filters.value
      .filter((f) => f.dictType)
      .forEach((f) => initGetDictList(f.dictType!));
    descFields.value
      .filter((d) => d.dictType)
      .forEach((d) => initGetDictList(d.dictType!));
    if (headerBadgeDictType.value) {
      initGetDictList(headerBadgeDictType.value);
    }

    await ensureSelectedRecords();
    await loadData();
  });

  // ============================== 确认 ==============================
  function handleConfirm() {
    emit('success', internalValue.value);
    closeModal();
  }
  function handleCancel() {
    closeModal();
  }

  // 监听 modelValue 变化(父组件清空时同步)
  watch(modelValueProp, (v) => {
    internalValue.value = multiple.value
      ? (Array.isArray(v) ? [...(v as EntityRawValue[])] : [])
      : (v as EntityRawValue) ?? null;
  });
</script>

<style lang="less" scoped>
  .bep-shell {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 480px;
  }

  .bep-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;

    .bep-search {
      width: 280px;
    }
  }

  .bep-selected {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: #f6f8fb;
    border-radius: 6px;
    border: 1px dashed #e1e6ed;

    .bep-selected-label {
      font-size: 12px;
      color: #595959;
      margin-right: 4px;
    }
  }

  .bep-empty {
    padding: 60px 0;
    text-align: center;
  }

  .bep-grid {
    min-height: 380px;
  }

  // ============================== 卡片 ==============================
  .bep-card {
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
    // 防御性 ── 父级 a-col 已经控制了百分比宽度,但内部 flex item 长内容仍可能溢出。
    // min-width: 0 + 内部 flex value 同样设 0,确保 ellipsis 正常 + 不撑破列宽。
    min-width: 0;

    &:hover {
      border-color: @primary-color;
      box-shadow: 0 4px 16px rgb(15 23 42 / 6%);
      transform: translateY(-2px);
    }

    &.active {
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

  // header:名称(左)+ badge(右)
  .bep-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    gap: 8px;

    .bep-card-name {
      flex: 1;
      font-size: 15px;
      font-weight: 600;
      color: #1a1a2e;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      padding-right: 22px; // 给右上角 check-mark 让位
    }

    .bep-card-badge {
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

  // body:多行 info_row
  .bep-card-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .bep-info-row {
      display: flex;
      align-items: baseline;
      font-size: 13px;
      line-height: 1.6;
      overflow: hidden;
      min-width: 0;

      .bep-info-label {
        flex-shrink: 0;
        color: #8c8c8c;
        min-width: 72px;

        &::after {
          content: '：';
        }
      }

      .bep-info-value {
        flex: 1;
        // 关键:flex item 在 flex 容器里要 min-width: 0 才能正常 ellipsis
        min-width: 0;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  // footer:时间(左)+ 状态点(右)
  .bep-card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
    padding-top: 8px;
    border-top: 1px solid #f5f5f5;

    .bep-info-time {
      font-size: 12px;
      color: #bfbfbf;
    }

    .bep-status-dot {
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

  // 选中标记
  .bep-check-mark {
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

  .bep-pagination {
    margin-top: 12px;
    text-align: right;
  }
</style>
