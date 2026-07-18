<template>
  <div class="action-trace">
    <div class="action-toolbar">
      <ARadioGroup
        v-model:value="groupFilter"
        :options="groupOptions"
        option-type="button"
        button-style="solid"
        size="small"
        class="action-group-seg"
        @change="handleSearch"
      />

      <div class="action-filters">
        <ASelect
          v-model:value="statusFilter"
          allow-clear
          size="small"
          class="action-status-select"
          :options="statusOptions"
          :placeholder="Ta('statusFilter')"
          @change="handleSearch"
        />
        <ARangePicker
          v-model:value="timeRange"
          show-time
          value-format="YYYY-MM-DD HH:mm:ss"
          size="small"
          class="action-time-range"
          @change="handleSearch"
        />
        <AInput
          v-model:value="keyword"
          allow-clear
          size="small"
          class="action-keyword"
          :placeholder="Ta('keywordPh')"
          @press-enter="handleSearch"
          @change="onKeywordChange"
        >
          <template #prefix>
            <span class="action-filter-prefix">{{ Ta('keywordPrefix') }}</span>
          </template>
        </AInput>
        <AButton size="small" type="primary" class="action-search-btn" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          {{ Ta('search') }}
        </AButton>
        <AButton size="small" :loading="loading" @click="loadActions">
          <template #icon><ReloadOutlined /></template>
          {{ Ta('refresh') }}
        </AButton>
      </div>
    </div>

    <div class="action-summary">
      <div v-for="item in summaryCards" :key="item.key" class="summary-item" :class="item.key">
        <div class="summary-dot"></div>
        <div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <ASpin :spinning="loading">
      <div v-if="displayGroups.length === 0" class="action-empty">
        <AEmpty :description="hasActiveFilter ? Ta('emptyFiltered') : Ta('empty')" />
      </div>

      <div v-else class="action-timeline">
        <div v-for="group in displayGroups" :key="group.date" class="timeline-day">
          <div class="day-label">{{ group.date }}</div>
          <div class="day-list">
            <div
              v-for="record in group.items"
              :key="record.id || record.createdTime"
              class="action-record"
              :class="recordClass(record)"
            >
              <div class="record-line">
                <div class="record-dot">
                  <ApiOutlined v-if="actionGroup(record) === 'data'" />
                  <BranchesOutlined v-else-if="actionGroup(record) === 'subscription'" />
                  <WarningOutlined v-else-if="actionGroup(record) === 'exception'" />
                  <ImportOutlined v-else-if="actionGroup(record) === 'inbound'" />
                  <LinkOutlined v-else />
                </div>
              </div>

              <div class="record-main">
                <div class="record-head">
                  <div class="record-title">
                    <span class="record-action">{{ actionText(record) }}</span>
                    <ATag :color="groupColor(record)" class="record-tag">
                      {{ groupText(record) }}
                    </ATag>
                    <ATag :color="statusColor(record)" class="record-tag">
                      {{ statusText(record) }}
                    </ATag>
                  </div>
                  <span class="record-time">{{ record.createdTime || '-' }}</span>
                </div>

                <div class="record-meta">
                  <span>
                    {{ t('iot.link.device.device.deviceIdentification') }}:
                    <b>{{ record.deviceIdentification || '-' }}</b>
                  </span>
                  <span v-if="parsed(record).clientId"
                    >clientId: {{ parsed(record).clientId }}</span
                  >
                  <span v-if="parsed(record).protocol"
                    >protocol: {{ parsed(record).protocol }}</span
                  >
                  <span v-if="parsed(record).traceId">traceId: {{ parsed(record).traceId }}</span>
                </div>

                <div v-if="parsed(record).topic" class="record-topic" :title="parsed(record).topic">
                  {{ parsed(record).topic }}
                </div>
                <div class="record-summary">{{ recordSummary(record) }}</div>
              </div>

              <div class="record-actions">
                <AButton type="link" size="small" @click="openDetail(record)">
                  <template #icon><EyeOutlined /></template>
                  {{ Ta('viewPayload') }}
                </AButton>
                <AButton type="link" size="small" @click="copyRecord(record)">
                  <template #icon><CopyOutlined /></template>
                  {{ Ta('copy') }}
                </AButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ASpin>

    <div v-if="pagination.total > 0" class="action-pagination">
      <APagination
        v-model:current="pagination.current"
        v-model:pageSize="pagination.size"
        :total="pagination.total"
        show-size-changer
        show-quick-jumper
        size="small"
        :show-total="(total) => Ta('totalWithCount', { total })"
        @change="loadActions"
        @show-size-change="loadActions"
      />
    </div>

    <ADrawer
      v-model:visible="detailVisible"
      :title="Ta('detailTitle')"
      :width="720"
      class="action-detail-drawer"
      destroy-on-close
    >
      <template v-if="activeRecord">
        <div class="detail-section">
          <div class="detail-title">{{ Ta('basicInfo') }}</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.actionType') }}</span>
              <b>{{ actionText(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.status') }}</span>
              <b>{{ statusText(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ t('iot.link.device.device.createdTime') }}</span>
              <b>{{ activeRecord.createdTime || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Ta('group') }}</span>
              <b>{{ groupText(activeRecord) }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ t('iot.link.device.device.deviceIdentification') }}</span>
              <b>{{ activeRecord.deviceIdentification || '-' }}</b>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-title">{{ Ta('parsedInfo') }}</div>
          <div v-if="actionDetailItems.length" class="detail-grid">
            <div
              v-for="item in actionDetailItems"
              :key="item.key"
              class="detail-item"
              :class="{ wide: item.wide }"
            >
              <span>{{ item.label }}</span>
              <b>{{ item.value }}</b>
            </div>
          </div>
          <div v-else class="detail-empty">{{ Ta('noParsedFields') }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-title">{{ Ta('rawPayload') }}</div>
          <ATabs size="small">
            <ATabPane key="message" :tab="t('iot.link.device.device.message')">
              <pre class="raw-block">{{ prettyText(activeRecord.message) }}</pre>
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
    ApiOutlined,
    BranchesOutlined,
    CopyOutlined,
    EyeOutlined,
    ImportOutlined,
    LinkOutlined,
    ReloadOutlined,
    SearchOutlined,
    WarningOutlined,
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
  import { page } from '/@/api/iot/link/deviceAction/deviceAction';
  import type { DeviceActionResultVO } from '/@/api/iot/link/deviceAction/model/deviceActionModel';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { useDict } from '/@/components/Dict';

  defineOptions({ name: 'DeviceAction' });

  const ARadioGroup = Radio.Group;
  const ARangePicker = DatePicker.RangePicker;
  const ATabPane = ATabs.TabPane;

  const props = defineProps({
    deviceIdentification: {
      type: String,
      default: '',
    },
  });

  type ActionGroup = 'all' | 'lifecycle' | 'data' | 'subscription' | 'exception' | 'inbound';

  interface ParsedAction {
    topic?: string;
    clientId?: string;
    userId?: string;
    tenantId?: string | number;
    clientType?: string;
    protocol?: string;
    reason?: string;
    traceId?: string;
    source?: string;
    qos?: string | number;
    packetId?: string | number;
    summary: string;
    rawText: string;
  }

  interface DetailField {
    key: string;
    label: string;
    value: string;
    wide?: boolean;
  }

  const actionGroupMap: Record<Exclude<ActionGroup, 'all'>, string[]> = {
    lifecycle: ['CONNECT', 'DISCONNECT', 'CLOSE', 'KICKED', 'HEART_TIMEOUT'],
    data: ['PUBLISH', 'PING'],
    subscription: ['SUBSCRIBE', 'UNSUBSCRIBE'],
    exception: ['ERROR', 'DISPATCH_ERROR', 'UNKNOWN'],
    inbound: ['INBOUND', 'BRIDGE_INBOUND'],
  };

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const Ta = (key: string, params?: Recordable) =>
    t(`iot.link.device.device.deviceActionRecord.${key}`, params);

  const loading = ref(false);
  const records = ref<DeviceActionResultVO[]>([]);
  const groupFilter = ref<ActionGroup>('all');
  const statusFilter = ref<string | undefined>();
  const keyword = ref('');
  const timeRange = ref<string[] | null>(null);
  const detailVisible = ref(false);
  const activeRecord = ref<DeviceActionResultVO | null>(null);
  const pagination = reactive({
    current: 1,
    size: 20,
    total: 0,
  });

  const groupOptions = computed(() => [
    { label: Ta('all'), value: 'all' },
    { label: Ta('lifecycle'), value: 'lifecycle' },
    { label: Ta('data'), value: 'data' },
    { label: Ta('subscription'), value: 'subscription' },
    { label: Ta('exception'), value: 'exception' },
    { label: Ta('inbound'), value: 'inbound' },
  ]);

  const statusOptions = computed(() => [
    { label: Ta('actionSuccess'), value: '0' },
    { label: Ta('actionFailed'), value: '1' },
  ]);

  const hasActiveFilter = computed(
    () =>
      groupFilter.value !== 'all' ||
      !!statusFilter.value ||
      !!keyword.value.trim() ||
      (Array.isArray(timeRange.value) && timeRange.value.length > 0),
  );

  const displayRows = computed(() => {
    const key = keyword.value.trim().toLowerCase();
    return records.value.filter((record) => {
      if (!matchGroup(record)) return false;
      if (!key) return true;
      return parsed(record).rawText.toLowerCase().includes(key);
    });
  });

  const actionDetailItems = computed(() =>
    activeRecord.value ? actionDetailFields(activeRecord.value) : [],
  );

  const displayGroups = computed(() => {
    const groups: { date: string; items: DeviceActionResultVO[] }[] = [];
    displayRows.value.forEach((record) => {
      const date = formatDateGroup(record.createdTime);
      let group = groups.find((item) => item.date === date);
      if (!group) {
        group = { date, items: [] };
        groups.push(group);
      }
      group.items.push(record);
    });
    return groups;
  });

  const summaryCards = computed(() => {
    const rows = displayRows.value;
    const success = rows.filter((item) => Number(item.status) === 0).length;
    const failed = rows.filter((item) => Number(item.status) === 1).length;
    const latestRecord = rows[0];
    return [
      { key: 'total', label: Ta('total'), value: pagination.total },
      { key: 'visible', label: Ta('visible'), value: rows.length },
      { key: 'success', label: Ta('pageSuccess'), value: success },
      { key: 'failed', label: Ta('pageFailed'), value: failed },
      { key: 'latest', label: Ta('latest'), value: latestRecord ? actionText(latestRecord) : '-' },
    ];
  });

  watch(
    () => props.deviceIdentification,
    () => {
      pagination.current = 1;
      loadActions();
    },
  );

  onMounted(() => loadActions());

  async function loadActions(): Promise<void> {
    if (!props.deviceIdentification) return;
    loading.value = true;
    try {
      const model: Recordable = {
        deviceIdentification: props.deviceIdentification,
      };
      if (statusFilter.value !== undefined) model.status = Number(statusFilter.value);
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
    loadActions();
  }

  function onKeywordChange(): void {
    if (!keyword.value.trim()) handleSearch();
  }

  function actionType(record: DeviceActionResultVO): string {
    return String(record?.actionType || '').toUpperCase();
  }

  function actionText(record: DeviceActionResultVO): string {
    return (
      record?.echoMap?.actionType ||
      getDictLabel('LINK_DEVICE_ACTION_TYPE', record?.actionType, record?.actionType || '') ||
      Ta('unknownAction')
    );
  }

  function statusText(record: DeviceActionResultVO): string {
    const label =
      record?.echoMap?.status || getDictLabel('LINK_DEVICE_ACTION_STATUS', record?.status, '');
    if (label) return label;
    if (Number(record?.status) === 0) return Ta('actionSuccess');
    if (Number(record?.status) === 1) return Ta('actionFailed');
    return Ta('unknownStatus');
  }

  function statusColor(record: DeviceActionResultVO): string {
    if (Number(record?.status) === 0) return 'success';
    if (Number(record?.status) === 1) return 'error';
    return 'default';
  }

  function actionGroup(record: DeviceActionResultVO): Exclude<ActionGroup, 'all'> {
    const type = actionType(record);
    const hit = Object.entries(actionGroupMap).find(([, values]) => values.includes(type));
    return (hit?.[0] as Exclude<ActionGroup, 'all'>) || 'exception';
  }

  function groupText(record: DeviceActionResultVO): string {
    return Ta(actionGroup(record));
  }

  function groupColor(record: DeviceActionResultVO): string {
    const group = actionGroup(record);
    if (group === 'lifecycle') return 'success';
    if (group === 'data') return 'processing';
    if (group === 'subscription') return 'purple';
    if (group === 'exception') return 'error';
    return 'cyan';
  }

  function recordClass(record: DeviceActionResultVO): string {
    return `is-${actionGroup(record)}`;
  }

  function matchGroup(record: DeviceActionResultVO): boolean {
    if (groupFilter.value === 'all') return true;
    return actionGroup(record) === groupFilter.value;
  }

  function recordSummary(record: DeviceActionResultVO): string {
    const p = parsed(record);
    if (p.reason) return p.reason;
    if (p.topic) return p.topic;
    return p.summary || '-';
  }

  function actionDetailFields(record: DeviceActionResultVO): DetailField[] {
    const p = parsed(record);
    const fields: DetailField[] = [];
    addDetailField(fields, 'topic', 'topic', p.topic, true);
    addDetailField(fields, 'clientId', 'clientId', p.clientId);
    addDetailField(fields, 'userId', 'userId', p.userId);
    addDetailField(fields, 'tenantId', 'tenantId', p.tenantId);
    addDetailField(fields, 'clientType', 'clientType', p.clientType);
    addDetailField(fields, 'protocol', 'protocol', p.protocol);
    addDetailField(fields, 'traceId', 'traceId', p.traceId);
    addDetailField(fields, 'qos', 'qos', p.qos);
    addDetailField(fields, 'packetId', 'packetId', p.packetId);
    addDetailField(fields, 'source', 'source', p.source);
    addDetailField(fields, 'reason', 'reason', p.reason, true);
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

  function openDetail(record: DeviceActionResultVO): void {
    activeRecord.value = record;
    detailVisible.value = true;
  }

  function copyRecord(record: DeviceActionResultVO): void {
    const text = [record?.message, record?.remark].filter(Boolean).join('\n\n');
    handleCopyTextV2(text || JSON.stringify(record));
  }

  const parsedCache = new WeakMap<object, ParsedAction>();
  function parsed(record: DeviceActionResultVO): ParsedAction {
    if (record && typeof record === 'object' && parsedCache.has(record)) {
      return parsedCache.get(record)!;
    }
    const message = parsePayload(record?.message);
    const remark = parsePayload(record?.remark);
    const mergedText = [record?.message, record?.remark].filter(Boolean).join('\n');
    const parsedText = safeStringify([message, remark]);
    const source = [message, remark];
    const result: ParsedAction = {
      topic: firstFound(source, ['topic']),
      clientId: firstFound(source, ['clientId', 'clientID', 'client_id']),
      userId: firstFound(source, ['userId', 'userID', 'user_id']),
      tenantId: firstFound(source, ['tenantId', 'tenantID', 'tenant_id']),
      clientType: firstFound(source, ['clientType', 'client_type']),
      protocol: firstFound(source, ['protocol', 'protocolType']),
      reason: firstFound(source, [
        'reason',
        'reasonCode',
        'errorMsg',
        'eventDesc',
        'description',
        'desc',
        'message',
        'msg',
      ]),
      traceId: firstFound(source, ['traceId', 'traceID', 'requestId', 'spanId']),
      source: firstFound(source, ['source', 'from']),
      qos: firstFound(source, ['qos']),
      packetId: firstFound(source, ['packetId', 'packetID', 'packet_id']),
      summary: summarize(message, remark, mergedText),
      rawText: `${mergedText}\n${parsedText}\n${safeStringify(record)}`,
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

  function summarize(message: any, remark: any, fallback: string): string {
    const text = safeStringify(message) || safeStringify(remark) || fallback || '';
    const compact = text.replace(/\s+/g, ' ').trim();
    if (!compact) return '';
    return compact.length > 160 ? `${compact.slice(0, 160)}...` : compact;
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

  function formatDateGroup(value?: string): string {
    if (!value) return Ta('unknownTime');
    return value.split(' ')[0] || value;
  }
</script>

<style lang="less" scoped>
  .action-trace {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .action-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #eef2f7;
    border-radius: 8px;
  }

  .action-group-seg {
    flex: none;

    :deep(.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)) {
      background: #009688;
      border-color: #009688;
    }
  }

  .action-filters {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-wrap: wrap;
    min-width: 0;
  }

  .action-status-select {
    width: 116px;
  }

  .action-time-range {
    width: 320px;
  }

  .action-keyword {
    width: 360px;
    height: 28px;
    background: #fff;
    border-color: #e5ebf3;
    border-radius: 6px;
    box-shadow: none;
  }

  .action-filter-prefix {
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

  .action-search-btn {
    background: #009688;
    border-color: #009688;
  }

  :deep(.action-keyword.ant-input-affix-wrapper) {
    padding-top: 0;
    padding-bottom: 0;
  }

  :deep(.action-keyword.ant-input-affix-wrapper-focused),
  :deep(.action-keyword.ant-input-affix-wrapper:hover) {
    border-color: #009688;
    box-shadow: 0 0 0 2px rgb(0 150 136 / 10%);
  }

  .action-summary {
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

  .summary-item.visible .summary-dot {
    background: #8165ff;
  }

  .summary-item.success .summary-dot {
    background: #009688;
  }

  .summary-item.failed .summary-dot {
    background: #ff4d4f;
  }

  .summary-item.latest .summary-dot {
    background: #faad14;
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

  .action-empty {
    padding: 36px 0;
    background: #fff;
    border: 1px dashed #eef2f7;
    border-radius: 8px;
  }

  .action-timeline {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .timeline-day {
    display: grid;
    grid-template-columns: 92px minmax(0, 1fr);
    gap: 12px;
  }

  .day-label {
    padding-top: 12px;
    color: #6b7280;
    font-size: 12px;
    font-weight: 600;
    text-align: right;
  }

  .day-list {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 8px;

    &::before {
      position: absolute;
      top: 18px;
      bottom: 18px;
      left: 16px;
      width: 1px;
      background: #e8eef5;
      content: '';
    }
  }

  .action-record {
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

    &.is-lifecycle {
      border-left-color: #009688;
    }

    &.is-subscription {
      border-left-color: #8165ff;
    }

    &.is-exception {
      border-left-color: #ff4d4f;
    }

    &.is-inbound {
      border-left-color: #13c2c2;
    }
  }

  .record-line {
    position: relative;
    display: flex;
    justify-content: center;
  }

  .record-dot {
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    color: #0960bd;
    background: #eef6ff;
    border-radius: 50%;
  }

  .is-lifecycle .record-dot {
    color: #009688;
    background: #e9f8f5;
  }

  .is-subscription .record-dot {
    color: #8165ff;
    background: #f0edff;
  }

  .is-exception .record-dot {
    color: #ff4d4f;
    background: #fff1f0;
  }

  .is-inbound .record-dot {
    color: #13c2c2;
    background: #e6fffb;
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

  .record-action {
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

  .record-topic,
  .record-summary {
    margin-top: 6px;
    color: #2a3547;
    font-size: 12px;
    line-height: 18px;
    overflow-wrap: anywhere;
    word-break: break-all;
  }

  .record-topic {
    color: #8c8c8c;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }

  .record-actions {
    display: flex;
    align-items: flex-start;
    gap: 2px;
    padding-top: 2px;
    white-space: nowrap;
  }

  .action-pagination {
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
    .action-toolbar {
      flex-direction: column;
    }

    .action-filters,
    .action-group-seg {
      width: 100%;
    }

    .action-keyword,
    .action-time-range {
      flex: 1;
      width: auto;
      min-width: 220px;
    }

    .action-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .timeline-day {
      grid-template-columns: 1fr;
      gap: 6px;
    }

    .day-label {
      padding-top: 0;
      text-align: left;
    }

    .action-record {
      grid-template-columns: 28px minmax(0, 1fr);
    }

    .record-actions {
      grid-column: 1 / -1;
      padding-left: 38px;
    }

    .action-keyword,
    .action-time-range,
    .action-status-select {
      width: 100%;
      min-width: 100%;
    }

    .action-summary {
      grid-template-columns: 1fr;
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
