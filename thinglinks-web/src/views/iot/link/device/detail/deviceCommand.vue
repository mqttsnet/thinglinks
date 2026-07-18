<template>
  <div class="command-trace">
    <div class="command-toolbar">
      <ARadioGroup
        v-model:value="typeFilter"
        :options="typeOptions"
        option-type="button"
        button-style="solid"
        size="small"
        class="command-type-seg"
        @change="handleSearch"
      />

      <div class="command-filters">
        <ASelect
          v-model:value="statusFilter"
          allow-clear
          size="small"
          class="command-status-select"
          :options="statusOptions"
          :placeholder="Tc('statusFilter')"
          @change="handleSearch"
        />
        <ARangePicker
          v-model:value="timeRange"
          show-time
          value-format="YYYY-MM-DD HH:mm:ss"
          size="small"
          class="command-time-range"
          @change="handleSearch"
        />
        <AInput
          v-model:value="keyword"
          allow-clear
          size="small"
          class="command-keyword"
          :placeholder="Tc('keywordPh')"
          @press-enter="handleSearch"
          @change="onKeywordChange"
        >
          <template #prefix>
            <span class="command-filter-prefix">{{ Tc('keywordPrefix') }}</span>
          </template>
        </AInput>
        <AButton size="small" type="primary" class="command-search-btn" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          {{ Tc('search') }}
        </AButton>
        <AButton size="small" :loading="loading" @click="loadCommands">
          <template #icon><ReloadOutlined /></template>
          {{ Tc('refresh') }}
        </AButton>
      </div>
    </div>

    <div class="command-summary">
      <div v-for="item in summaryCards" :key="item.key" class="summary-item" :class="item.key">
        <div class="summary-dot"></div>
        <div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <ASpin :spinning="loading">
      <div v-if="displayRows.length === 0" class="command-empty">
        <AEmpty :description="hasActiveFilter ? Tc('emptyFiltered') : Tc('empty')" />
      </div>

      <div v-else class="command-list">
        <div
          v-for="record in displayRows"
          :key="record.id || record.commandIdentification || record.createdTime"
          class="command-record"
          :class="recordClass(record)"
        >
          <div class="record-direction">
            <ArrowDownOutlined v-if="isResponse(record)" />
            <SyncOutlined v-else-if="isOta(record)" />
            <ArrowUpOutlined v-else />
          </div>

          <div class="record-main">
            <div class="record-head">
              <div class="record-title">
                <span class="record-command">{{ commandTitle(record) }}</span>
                <ATag :color="typeColor(record)" class="record-tag">
                  {{ typeText(record) }}
                </ATag>
                <ATag :color="statusColor(record)" class="record-tag">
                  {{ statusText(record) }}
                </ATag>
                <ATag
                  v-if="businessKnown(record)"
                  :color="businessColor(record)"
                  class="record-tag"
                >
                  {{ businessText(record) }}
                </ATag>
              </div>
              <span class="record-time">{{ record.createdTime || '-' }}</span>
            </div>

            <div class="record-meta">
              <span>
                {{ t('iot.link.device.device.commandIdentification') }}:
                <b>{{ record.commandIdentification || '-' }}</b>
              </span>
              <span v-if="parsed(record).msgType">msgType: {{ parsed(record).msgType }}</span>
              <span v-if="parsed(record).mid">mid: {{ parsed(record).mid }}</span>
            </div>

            <div v-if="parsed(record).topic" class="record-topic" :title="parsed(record).topic">
              {{ parsed(record).topic }}
            </div>
            <div class="record-summary">{{ recordSummary(record) }}</div>
          </div>

          <div class="record-actions">
            <AButton type="link" size="small" @click="openDetail(record)">
              <template #icon><EyeOutlined /></template>
              {{ Tc('viewPayload') }}
            </AButton>
            <AButton type="link" size="small" @click="copyRecord(record)">
              <template #icon><CopyOutlined /></template>
              {{ Tc('copy') }}
            </AButton>
          </div>
        </div>
      </div>
    </ASpin>

    <div v-if="pagination.total > 0" class="command-pagination">
      <APagination
        v-model:current="pagination.current"
        v-model:pageSize="pagination.size"
        :total="pagination.total"
        show-size-changer
        show-quick-jumper
        size="small"
        :show-total="(total) => Tc('totalWithCount', { total })"
        @change="loadCommands"
        @show-size-change="loadCommands"
      />
    </div>

    <ADrawer
      v-model:visible="detailVisible"
      :title="Tc('detailTitle')"
      :width="720"
      class="command-detail-drawer"
      destroy-on-close
    >
      <template v-if="activeRecord">
        <div class="detail-section">
          <div class="detail-title">{{ Tc('basicInfo') }}</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.commandIdentification') }}</span>
              <b>{{ activeRecord.commandIdentification || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.commandType') }}</span>
              <b>{{ typeText(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.status') }}</span>
              <b>{{ statusText(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.createdTime') }}</span>
              <b>{{ activeRecord.createdTime || '-' }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ t('iot.link.device.device.deviceIdentification') }}</span>
              <b>{{ activeRecord.deviceIdentification || '-' }}</b>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-title">{{ Tc('parsedInfo') }}</div>
          <div v-if="commandDetailItems.length" class="detail-grid">
            <div
              v-for="item in commandDetailItems"
              :key="item.key"
              class="detail-item"
              :class="{ wide: item.wide }"
            >
              <span>{{ item.label }}</span>
              <b>{{ item.value }}</b>
            </div>
          </div>
          <div v-else class="detail-empty">{{ Tc('noParsedFields') }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-title">{{ Tc('rawPayload') }}</div>
          <ATabs size="small">
            <ATabPane key="content" :tab="t('iot.link.device.device.content')">
              <pre class="raw-block">{{ prettyText(activeRecord.content) }}</pre>
            </ATabPane>
            <ATabPane key="remark" :tab="t('iot.link.device.device.remark')">
              <pre class="raw-block">{{ prettyText(activeRecord.remark) }}</pre>
            </ATabPane>
          </ATabs>
        </div>
      </template>
    </ADrawer>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import {
    ArrowDownOutlined,
    ArrowUpOutlined,
    CopyOutlined,
    EyeOutlined,
    ReloadOutlined,
    SearchOutlined,
    SyncOutlined,
  } from '@ant-design/icons-vue';
  import {
    Button as AButton,
    DatePicker,
    Drawer as ADrawer,
    Empty as AEmpty,
    Input as AInput,
    Pagination as APagination,
    Radio,
    Select as ASelect,
    Spin as ASpin,
    Tag as ATag,
    Tabs as ATabs,
  } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { page } from '/@/api/iot/link/deviceCommand/deviceCommand';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { useDict } from '/@/components/Dict';

  defineOptions({ name: 'DeviceCommand' });

  const ARadioGroup = Radio.Group;
  const ARangePicker = DatePicker.RangePicker;
  const ATabPane = ATabs.TabPane;

  const props = defineProps({
    deviceIdentification: {
      type: String,
      default: '',
    },
  });

  interface ParsedCommand {
    topic?: string;
    serviceCode?: string;
    cmd?: string;
    msgType?: string;
    mid?: string | number;
    errCode?: string | number;
    errMsg?: string;
    code?: string | number;
    resultCode?: string | number;
    message?: string;
    success?: boolean | string | number;
    result?: boolean | string | number;
    summary: string;
    rawText: string;
  }

  interface DetailField {
    key: string;
    label: string;
    value: string;
    wide?: boolean;
  }

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const Tc = (key: string, params?: Recordable) =>
    t(`iot.link.device.device.commandRecord.${key}`, params);

  const loading = ref(false);
  const records = ref<any[]>([]);
  const typeFilter = ref('all');
  const statusFilter = ref<string | undefined>();
  const keyword = ref('');
  const timeRange = ref<string[] | null>(null);
  const detailVisible = ref(false);
  const activeRecord = ref<any>(null);
  const pagination = reactive({
    current: 1,
    size: 20,
    total: 0,
  });

  const typeOptions = computed(() => [
    { label: Tc('all'), value: 'all' },
    { label: Tc('issueCommand'), value: '0' },
    { label: Tc('commandResponse'), value: '1' },
    { label: Tc('otaCommand'), value: '2' },
  ]);

  const statusOptions = computed(() => [
    { label: Tc('pending'), value: '0' },
    { label: Tc('success'), value: '1' },
    { label: Tc('failed'), value: '2' },
  ]);

  const hasActiveFilter = computed(
    () =>
      typeFilter.value !== 'all' ||
      !!statusFilter.value ||
      !!keyword.value.trim() ||
      (Array.isArray(timeRange.value) && timeRange.value.length > 0),
  );

  const displayRows = computed(() => {
    const key = keyword.value.trim().toLowerCase();
    if (!key) return records.value;
    return records.value.filter((record) => parsed(record).rawText.toLowerCase().includes(key));
  });

  const commandDetailItems = computed(() =>
    activeRecord.value ? commandDetailFields(activeRecord.value) : [],
  );

  const summaryCards = computed(() => {
    const rows = displayRows.value;
    const success = rows.filter((item) => Number(item.status) === 1).length;
    const failed = rows.filter((item) => Number(item.status) === 2).length;
    const pending = rows.filter((item) => Number(item.status) === 0).length;
    const latest = rows[0]?.createdTime || '-';
    return [
      { key: 'total', label: Tc('total'), value: pagination.total },
      { key: 'success', label: Tc('pageSuccess'), value: success },
      { key: 'failed', label: Tc('pageFailed'), value: failed },
      { key: 'pending', label: Tc('pagePending'), value: pending },
      { key: 'latest', label: Tc('latest'), value: latest },
    ];
  });

  watch(
    () => props.deviceIdentification,
    () => {
      pagination.current = 1;
      loadCommands();
    },
  );

  onMounted(() => loadCommands());

  async function loadCommands(): Promise<void> {
    if (!props.deviceIdentification) return;
    loading.value = true;
    try {
      const model: Recordable = {
        deviceIdentification: props.deviceIdentification,
      };
      if (typeFilter.value !== 'all') model.commandType = Number(typeFilter.value);
      if (statusFilter.value) model.status = Number(statusFilter.value);
      const searchKey = keyword.value.trim();
      if (searchKey) {
        if (/^\d{6,}$/.test(searchKey)) model.commandIdentification = searchKey;
        else model.content = searchKey;
      }
      const params: Recordable = {
        current: pagination.current,
        size: pagination.size,
        sort: 'createdTime',
        order: 'descend',
        model,
        extra: {},
      };
      if (Array.isArray(timeRange.value) && timeRange.value.length === 2) {
        params.extra = {
          createdTime_st: timeRange.value[0],
          createdTime_ed: timeRange.value[1],
        };
      }
      const result: any = await page(params as any);
      records.value = Array.isArray(result?.records) ? result.records : [];
      pagination.total = Number(result?.total || 0);
    } catch {
      records.value = [];
      pagination.total = 0;
    } finally {
      loading.value = false;
    }
  }

  function handleSearch(): void {
    pagination.current = 1;
    loadCommands();
  }

  function onKeywordChange(): void {
    if (!keyword.value.trim()) handleSearch();
  }

  function isIssue(record: any): boolean {
    return Number(record?.commandType) === 0;
  }

  function isResponse(record: any): boolean {
    return Number(record?.commandType) === 1;
  }

  function isOta(record: any): boolean {
    return Number(record?.commandType) === 2;
  }

  function typeText(record: any): string {
    if (isIssue(record)) return Tc('issueCommand');
    if (isResponse(record)) return Tc('commandResponse');
    if (isOta(record)) return Tc('otaCommand');
    return getDictLabel('LINK_DEVICE_COMMAND_TYPE', record?.commandType, '') || Tc('unknownType');
  }

  function typeColor(record: any): string {
    if (isIssue(record)) return 'processing';
    if (isResponse(record)) return 'success';
    if (isOta(record)) return 'purple';
    return 'default';
  }

  function statusText(record: any): string {
    const status = Number(record?.status);
    if (isIssue(record)) {
      if (status === 1) return Tc('sendSuccess');
      if (status === 2) return Tc('sendFailed');
      return Tc('sendPending');
    }
    if (isResponse(record)) {
      if (status === 1) return Tc('receiveSuccess');
      if (status === 2) return Tc('receiveFailed');
      return Tc('receivePending');
    }
    return getDictLabel('LINK_DEVICE_COMMAND_STATUS', record?.status, '') || Tc('unknownStatus');
  }

  function statusColor(record: any): string {
    const status = Number(record?.status);
    if (status === 1) return 'success';
    if (status === 2) return 'error';
    return 'warning';
  }

  function recordClass(record: any): string {
    if (isResponse(record)) return 'is-response';
    if (isOta(record)) return 'is-ota';
    return 'is-issue';
  }

  function commandTitle(record: any): string {
    const p = parsed(record);
    return p.cmd || p.serviceCode || record?.commandIdentification || Tc('rawCommand');
  }

  function businessKnown(record: any): boolean {
    return isResponse(record) && businessResult(record) !== undefined;
  }

  function businessResult(record: any): boolean | undefined {
    if (!isResponse(record)) return undefined;
    const p = parsed(record);
    const explicit = normalizeBusinessValue(p.success ?? p.result);
    if (explicit !== undefined) return explicit;

    const errCodeResult = normalizeCodeResult(p.errCode, false);
    if (errCodeResult !== undefined) return errCodeResult;

    const codeResult = normalizeCodeResult(p.code, true);
    if (codeResult !== undefined) return codeResult;

    return normalizeCodeResult(p.resultCode, true);
  }

  function businessText(record: any): string {
    const result = businessResult(record);
    if (result === true) return Tc('businessSuccess');
    if (result === false) return Tc('businessFailed');
    return Tc('businessUnknown');
  }

  function businessColor(record: any): string {
    const result = businessResult(record);
    if (result === true) return 'success';
    if (result === false) return 'error';
    return 'default';
  }

  function recordSummary(record: any): string {
    const p = parsed(record);
    if (p.serviceCode || p.cmd) {
      return [p.serviceCode ? `serviceCode=${p.serviceCode}` : '', p.cmd ? `cmd=${p.cmd}` : '']
        .filter(Boolean)
        .join(' · ');
    }
    return p.summary || Tc('rawPayload');
  }

  function commandDetailFields(record: any): DetailField[] {
    const p = parsed(record);
    const fields: DetailField[] = [];
    addDetailField(fields, 'topic', 'topic', p.topic, true);
    addDetailField(fields, 'serviceCode', 'serviceCode', p.serviceCode);
    addDetailField(fields, 'cmd', 'cmd', p.cmd);
    addDetailField(fields, 'msgType', 'msgType', p.msgType);
    addDetailField(fields, 'mid', 'mid', p.mid);
    if (isResponse(record)) {
      addDetailField(fields, 'businessStatus', Tc('businessStatus'), businessText(record));
    }
    addDetailField(
      fields,
      'resultCode',
      'errCode / code / resultCode',
      p.errCode ?? p.code ?? p.resultCode,
    );
    addDetailField(fields, 'message', 'errMsg / message', p.errMsg ?? p.message, true);
    return fields;
  }

  function addDetailField(
    fields: DetailField[],
    key: string,
    label: string,
    value: any,
    wide = false,
  ): void {
    if (!hasDisplayValue(value)) return;
    fields.push({ key, label, value: formatDetailValue(value), wide });
  }

  function hasDisplayValue(value: any): boolean {
    return value !== undefined && value !== null && value !== '';
  }

  function formatDetailValue(value: any): string {
    if (typeof value === 'boolean') return value ? 'true' : 'false';
    if (typeof value === 'object') return safeStringify(value) || String(value);
    return String(value);
  }

  function openDetail(record: any): void {
    activeRecord.value = record;
    detailVisible.value = true;
  }

  function copyRecord(record: any): void {
    const text = [record?.content, record?.remark].filter(Boolean).join('\n\n');
    handleCopyTextV2(text || JSON.stringify(record));
  }

  const parsedCache = new WeakMap<object, ParsedCommand>();
  function parsed(record: any): ParsedCommand {
    if (record && typeof record === 'object' && parsedCache.has(record)) {
      return parsedCache.get(record)!;
    }
    const content = parsePayload(record?.content);
    const remark = parsePayload(record?.remark);
    const mergedText = [record?.content, record?.remark].filter(Boolean).join('\n');
    const parsedText = safeStringify([content, remark]);
    const source = [content, remark];
    const result: ParsedCommand = {
      topic: firstFound(source, ['topic']),
      serviceCode: firstFound(source, ['serviceCode']),
      cmd: firstFound(source, ['cmd', 'command', 'commandCode']),
      msgType: firstFound(source, ['msgType', 'type']),
      mid: firstFound(source, ['mid', 'messageId']),
      errCode: firstFound(source, ['errCode', 'errorCode']),
      errMsg: firstFound(source, ['errMsg', 'errorMsg']),
      code: firstFound(source, ['code']),
      resultCode: firstFound(source, ['resultCode', 'retCode']),
      message: firstFound(source, ['message', 'msg']),
      success: firstFound(source, ['success']) as boolean | string | number | undefined,
      result: firstFound(source, ['result']) as boolean | string | number | undefined,
      summary: summarize(content, remark, mergedText),
      rawText: `${mergedText}\n${parsedText}`,
    };
    if (record && typeof record === 'object') parsedCache.set(record, result);
    return result;
  }

  function parsePayload(value: any): any {
    if (value === undefined || value === null || value === '') return undefined;
    if (typeof value !== 'string') return deepUnwrap(value);
    const text = value.trim();
    if (!text) return undefined;
    if (
      (text.startsWith('{') && text.endsWith('}')) ||
      (text.startsWith('[') && text.endsWith(']'))
    ) {
      try {
        return deepUnwrap(JSON.parse(text));
      } catch {
        return text;
      }
    }
    return text;
  }

  function deepUnwrap(value: any): any {
    if (typeof value === 'string') return parsePayload(value);
    if (Array.isArray(value)) return value.map(deepUnwrap);
    if (value && typeof value === 'object') {
      const output: Recordable = {};
      Object.keys(value).forEach((key) => {
        output[key] = deepUnwrap(value[key]);
      });
      return output;
    }
    return value;
  }

  function firstFound(sources: any[], keys: string[]): any {
    for (const source of sources) {
      const found = findByKey(source, keys);
      if (found !== undefined && found !== null && found !== '') return found;
    }
    return undefined;
  }

  function findByKey(value: any, keys: string[]): any {
    if (!value || typeof value !== 'object') return undefined;
    for (const key of keys) {
      if (Object.prototype.hasOwnProperty.call(value, key)) return value[key];
    }
    if (Array.isArray(value)) {
      for (const item of value) {
        const found = findByKey(item, keys);
        if (found !== undefined) return found;
      }
      return undefined;
    }
    for (const key of Object.keys(value)) {
      const found = findByKey(value[key], keys);
      if (found !== undefined) return found;
    }
    return undefined;
  }

  function summarize(content: any, remark: any, fallback: string): string {
    const text = safeStringify(content) || safeStringify(remark) || fallback || '';
    const compact = text.replace(/\s+/g, ' ').trim();
    if (!compact) return '';
    return compact.length > 140 ? `${compact.slice(0, 140)}...` : compact;
  }

  function prettyText(value: any): string {
    if (value === undefined || value === null || value === '') return '-';
    const parsedValue = parsePayload(value);
    return safeStringify(parsedValue) || String(value);
  }

  function safeStringify(value: any): string {
    if (value === undefined || value === null || value === '') return '';
    if (typeof value === 'string') return value;
    try {
      return JSON.stringify(value, null, 2);
    } catch {
      return String(value);
    }
  }

  function normalizeBusinessValue(value: any): boolean | undefined {
    if (value === undefined || value === null || value === '') return undefined;
    if (typeof value === 'boolean') return value;
    return normalizeCodeResult(value, true);
  }

  function normalizeCodeResult(value: any, acceptHttpSuccess: boolean): boolean | undefined {
    if (value === undefined || value === null || value === '') return undefined;
    const normalized = String(value).trim().toLowerCase();
    if (!normalized) return undefined;
    if (['true', 'success', 'succeed', 'succeeded', 'ok'].includes(normalized)) return true;
    if (['false', 'fail', 'failed', 'error'].includes(normalized)) return false;

    const num = Number(normalized);
    if (Number.isNaN(num)) return undefined;
    return num === 0 || (acceptHttpSuccess && num === 200);
  }
</script>

<style lang="less" scoped>
  .command-trace {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .command-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #eef2f7;
    border-radius: 8px;
  }

  .command-type-seg {
    flex: none;

    :deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
      background: #009688;
      border-color: #009688;
    }
  }

  .command-filters {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-wrap: wrap;
    min-width: 0;
  }

  .command-status-select {
    width: 128px;
  }

  .command-time-range {
    width: 320px;
  }

  .command-keyword {
    width: 340px;
    height: 28px;
    background: #fff;
    border-color: #e5ebf3;
    border-radius: 6px;
    box-shadow: none;
  }

  .command-filter-prefix {
    display: inline-flex;
    align-items: center;
    height: 18px;
    padding-right: 6px;
    margin-right: 2px;
    color: #009688;
    font-size: 12px;
    line-height: 18px;
    border-right: 1px solid #edf1f5;
  }

  .command-search-btn {
    background: #009688;
    border-color: #009688;
  }

  :deep(.command-keyword.ant-input-affix-wrapper) {
    padding-top: 0;
    padding-bottom: 0;
  }

  :deep(.command-keyword.ant-input-affix-wrapper-focused),
  :deep(.command-keyword.ant-input-affix-wrapper:hover) {
    border-color: #009688;
    box-shadow: 0 0 0 2px rgb(0 150 136 / 10%);
  }

  .command-summary {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 10px;
  }

  .summary-item {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 58px;
    padding: 10px 12px;
    background: #fff;
    border: 1px solid #eef2f7;
    border-radius: 8px;
  }

  .summary-dot {
    width: 8px;
    height: 28px;
    border-radius: 999px;
    background: #0960bd;
  }

  .summary-item.success .summary-dot {
    background: #009688;
  }

  .summary-item.failed .summary-dot {
    background: #ff4d4f;
  }

  .summary-item.pending .summary-dot {
    background: #faad14;
  }

  .summary-item.latest .summary-dot {
    background: #8165ff;
  }

  .summary-value {
    max-width: 100%;
    overflow: hidden;
    color: #2a3547;
    font-size: 16px;
    font-weight: 600;
    line-height: 22px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .summary-label {
    color: #8c8c8c;
    font-size: 12px;
  }

  .command-empty {
    padding: 36px 0;
    background: #fff;
    border: 1px dashed #eef2f7;
    border-radius: 8px;
  }

  .command-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .command-record {
    display: grid;
    grid-template-columns: 34px minmax(0, 1fr) auto;
    gap: 10px;
    padding: 12px;
    background: #fff;
    border: 1px solid #eef2f7;
    border-left: 3px solid #0960bd;
    border-radius: 8px;
    transition: border-color 0.2s, background 0.2s;

    &:hover {
      border-color: #d9eee9;
      background: #fbfefd;
    }

    &.is-response {
      border-left-color: #009688;
    }

    &.is-ota {
      border-left-color: #8165ff;
    }
  }

  .record-direction {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    color: #0960bd;
    background: #eef6ff;
    border-radius: 50%;
  }

  .is-response .record-direction {
    color: #009688;
    background: #e9f8f5;
  }

  .is-ota .record-direction {
    color: #8165ff;
    background: #f0edff;
  }

  .record-main {
    min-width: 0;
  }

  .record-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .record-title {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
    min-width: 0;
  }

  .record-command {
    color: #2a3547;
    font-size: 15px;
    font-weight: 600;
  }

  .record-tag {
    margin-right: 0;
  }

  .record-time {
    flex: none;
    color: #bfbfbf;
    font-size: 12px;
    line-height: 22px;
    white-space: nowrap;
  }

  .record-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 14px;
    margin-top: 8px;
    color: #6b7280;
    font-size: 12px;
  }

  .record-topic {
    margin-top: 6px;
    color: #8c8c8c;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 18px;
    overflow-wrap: anywhere;
    word-break: break-all;
  }

  .record-summary {
    margin-top: 6px;
    color: #2a3547;
    font-size: 12px;
    line-height: 18px;
    overflow-wrap: anywhere;
    word-break: break-all;
  }

  .record-actions {
    display: flex;
    align-items: flex-start;
    gap: 2px;
    padding-top: 2px;
    white-space: nowrap;
  }

  .command-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 4px;
  }

  .detail-section + .detail-section {
    margin-top: 18px;
  }

  .detail-title {
    margin-bottom: 10px;
    color: #2a3547;
    font-size: 14px;
    font-weight: 600;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .detail-item {
    padding: 10px 12px;
    background: #f8fafc;
    border: 1px solid #eef2f7;
    border-radius: 8px;

    &.wide {
      grid-column: 1 / -1;
    }

    span {
      display: block;
      margin-bottom: 4px;
      color: #8c8c8c;
      font-size: 12px;
    }

    b {
      color: #2a3547;
      font-weight: 500;
      overflow-wrap: anywhere;
      word-break: break-all;
    }
  }

  .detail-empty {
    padding: 12px;
    color: #8c8c8c;
    background: #f8fafc;
    border: 1px dashed #e5ebf3;
    border-radius: 8px;
    font-size: 12px;
  }

  .raw-block {
    min-height: 180px;
    max-height: 420px;
    margin: 0;
    padding: 12px;
    overflow: auto;
    color: #2a3547;
    background: #f8fafc;
    border: 1px solid #eef2f7;
    border-radius: 8px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 18px;
    white-space: pre-wrap;
    word-break: break-all;
  }

  @media (max-width: 1200px) {
    .command-toolbar {
      flex-direction: column;
    }

    .command-filters,
    .command-type-seg {
      width: 100%;
    }

    .command-keyword,
    .command-time-range {
      flex: 1;
      width: auto;
      min-width: 220px;
    }

    .command-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .command-record {
      grid-template-columns: 28px minmax(0, 1fr);
    }

    .record-actions {
      grid-column: 1 / -1;
      padding-left: 38px;
    }

    .command-keyword,
    .command-time-range,
    .command-status-select {
      width: 100%;
      min-width: 100%;
    }

    .command-summary {
      grid-template-columns: 1fr;
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
