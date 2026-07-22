<!--
  协议总线 ── 租户级实时指标 + 手动测试

  @author mqttsnet
  @since 2026-05-10
-->
<template>
  <PageWrapper :title="t('iot.mqs.bus.stats.title')" :content="t('iot.mqs.bus.stats.subtitle')">
    <a-tabs v-model:activeKey="activeTab">
      <!-- 统计 Tab -->
      <a-tab-pane key="stats" :tab="t('iot.mqs.bus.stats.route.stats')">
        <div class="bus-stats-toolbar">
          <a-button type="primary" @click="refresh" :loading="loading">
            <template #icon><ReloadOutlined /></template>
            {{ t('iot.mqs.bus.stats.table.refresh') }}
          </a-button>
          <a-checkbox v-model:checked="autoRefresh" @change="onAutoRefreshChange">
            {{ t('iot.mqs.bus.stats.table.autoRefresh') }}
          </a-checkbox>
          <a-input-number
            v-if="autoRefresh"
            v-model:value="autoRefreshInterval"
            :min="5"
            :max="60"
            :addon-after="t('iot.mqs.bus.stats.table.autoRefreshInterval')"
            @change="onIntervalChange"
            style="width: 200px"
          />
        </div>

        <!-- 健康度卡片 -->
        <a-row :gutter="16" class="bus-health-cards">
          <a-col :span="24 / 5" v-for="card in healthCards" :key="card.key">
            <a-card :title="card.title" :bordered="false" size="small">
              <div class="metric-value" :style="{ color: card.color }">
                {{ formatNumber(card.value) }}
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 维度详情 -->
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="12">
            <a-card :title="'Dispatch ── ' + t('iot.mqs.bus.stats.table.label')" size="small">
              <a-table
                :columns="dispatchColumns"
                :dataSource="dispatchRows"
                :pagination="{ pageSize: 10 }"
                size="small"
                :scroll="{ y: 320 }"
                :locale="{ emptyText: t('iot.mqs.bus.stats.empty') }"
              />
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card title="Stage Executions" size="small">
              <a-table
                :columns="stageColumns"
                :dataSource="stageRows"
                :pagination="{ pageSize: 10 }"
                size="small"
                :scroll="{ y: 320 }"
                :locale="{ emptyText: t('iot.mqs.bus.stats.empty') }"
              />
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="24">
            <a-card title="Relay Sends" size="small">
              <a-table
                :columns="relayColumns"
                :dataSource="relayRows"
                :pagination="{ pageSize: 10 }"
                size="small"
                :scroll="{ y: 320 }"
                :locale="{ emptyText: t('iot.mqs.bus.stats.empty') }"
              />
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <!-- 测试 Tab -->
      <a-tab-pane key="test" :tab="t('iot.mqs.bus.stats.route.test')">
        <a-form layout="vertical">
          <a-form-item :label="t('iot.mqs.bus.stats.test.sourceTopic')" required>
            <a-input v-model:value="testSourceTopic" :placeholder="defaultTestSourceTopic" />
          </a-form-item>
          <a-form-item :label="t('iot.mqs.bus.stats.test.rawJson')" required>
            <a-textarea
              v-model:value="testRawJson"
              :rows="8"
              :placeholder="t('iot.mqs.bus.stats.test.placeholder')"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="onTestSubmit" :loading="testLoading">
              {{ t('iot.mqs.bus.stats.test.submit') }}
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 测试结果 -->
        <a-card v-if="testOutcome" :title="t('iot.mqs.bus.stats.test.outcome')" size="small">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item :label="t('iot.mqs.bus.stats.test.outcomeStatus')">
              <a-tag :color="testStatusColor">{{ testOutcome.status }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.mqs.bus.stats.test.outcomeLatency')">
              {{ testOutcome.totalLatencyMs }} ms
            </a-descriptions-item>
            <a-descriptions-item
              v-if="testOutcome.failureReason"
              :label="t('iot.mqs.bus.stats.test.outcomeFailureReason')"
              :span="2"
            >
              <span style="color: red">{{ testOutcome.failureReason }}</span>
            </a-descriptions-item>
          </a-descriptions>

          <h4 style="margin-top: 16px">{{ t('iot.mqs.bus.stats.test.outcomeStages') }}</h4>
          <a-table
            :columns="outcomeStageColumns"
            :dataSource="testOutcome.stages || []"
            :pagination="false"
            size="small"
            :locale="{ emptyText: t('iot.mqs.bus.stats.empty') }"
          />
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </PageWrapper>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { PageWrapper } from '/@/components/Page';
import { useMessage } from '/@/hooks/web/useMessage';
import { ReloadOutlined } from '@ant-design/icons-vue';
import {
  queryTodaySummary,
  queryTodayHealth,
  type BusHealth,
  type BusTodaySummary,
} from '/@/api/iot/mqs/bus/stats';
import { dispatchManual, type DispatchOutcome } from '/@/api/iot/mqs/bus/test';
import { productInfo } from '/@/settings/productSetting';

const { t } = useI18n();
const { createMessage } = useMessage();

const activeTab = ref<string>('stats');
const loading = ref(false);

// 自动刷新
const autoRefresh = ref(false);
const autoRefreshInterval = ref(15);
let timer: ReturnType<typeof setInterval> | null = null;

// 数据
const summary = ref<BusTodaySummary | null>(null);
const health = ref<BusHealth | null>(null);

// 测试
const defaultTestSourceTopic = `${productInfo.mqNamespace}-mqs-mqttMsg`;
const testSourceTopic = ref<string>(defaultTestSourceTopic);
const testRawJson = ref<string>('');
const testLoading = ref(false);
const testOutcome = ref<DispatchOutcome | null>(null);

const testStatusColor = computed(() => {
  if (!testOutcome.value) return 'default';
  switch (testOutcome.value.status) {
    case '00':
      return 'green';
    case '01':
      return 'red';
    case '02':
      return 'orange';
    case '04':
    case '05':
      return 'gold';
    default:
      return 'default';
  }
});

const healthCards = computed(() => {
  const h = health.value || { total: 0, success: 0, failed: 0, dropped: 0, noRoute: 0 };
  return [
    { key: 'total', title: t('iot.mqs.bus.stats.health.total'), value: h.total, color: '#1890ff' },
    { key: 'success', title: t('iot.mqs.bus.stats.health.success'), value: h.success, color: '#52c41a' },
    { key: 'failed', title: t('iot.mqs.bus.stats.health.failed'), value: h.failed, color: '#ff4d4f' },
    { key: 'dropped', title: t('iot.mqs.bus.stats.health.dropped'), value: h.dropped, color: '#faad14' },
    { key: 'noRoute', title: t('iot.mqs.bus.stats.health.noRoute'), value: h.noRoute, color: '#9254de' },
  ];
});

// 表数据(把 label:count map 转成行)
const dispatchRows = computed(() => mapToRows(summary.value?.dispatch));
const stageRows = computed(() => mapToRows(summary.value?.stage));
const relayRows = computed(() => mapToRows(summary.value?.relay));

const dispatchColumns = [
  { title: t('iot.mqs.bus.stats.table.label'), dataIndex: 'label', ellipsis: true },
  { title: t('iot.mqs.bus.stats.table.count'), dataIndex: 'count', width: 100 },
];
const stageColumns = [
  { title: t('iot.mqs.bus.stats.table.label'), dataIndex: 'label', ellipsis: true },
  { title: t('iot.mqs.bus.stats.table.count'), dataIndex: 'count', width: 100 },
];
const relayColumns = [
  { title: t('iot.mqs.bus.stats.table.label'), dataIndex: 'label', ellipsis: true },
  { title: t('iot.mqs.bus.stats.table.count'), dataIndex: 'count', width: 100 },
];

const outcomeStageColumns = [
  { title: '#', dataIndex: 'sequence', width: 60 },
  { title: 'Stage', dataIndex: 'stageName' },
  { title: 'Phase', dataIndex: 'phase', width: 80 },
  { title: 'Status', dataIndex: 'status', width: 80 },
  { title: 'Latency(ms)', dataIndex: 'latencyMs', width: 100 },
  { title: 'Error/Skip', dataIndex: 'errorMsg', ellipsis: true,
    customRender: ({ record }) => record.errorMsg || record.skipReason || '-' },
];

function mapToRows(map: Record<string, number> | undefined): Array<{ key: string; label: string; count: number }> {
  if (!map) return [];
  return Object.entries(map)
    .map(([label, count]) => ({ key: label, label, count }))
    .sort((a, b) => b.count - a.count);
}

function formatNumber(n: number): string {
  if (n == null) return '0';
  return n.toLocaleString('en-US');
}

async function refresh() {
  loading.value = true;
  try {
    const [s, h] = await Promise.all([queryTodaySummary(), queryTodayHealth()]);
    summary.value = s;
    health.value = h;
  } finally {
    loading.value = false;
  }
}

function onAutoRefreshChange() {
  if (autoRefresh.value) {
    startTimer();
  } else {
    stopTimer();
  }
}

function onIntervalChange() {
  if (autoRefresh.value) {
    stopTimer();
    startTimer();
  }
}

function startTimer() {
  stopTimer();
  timer = setInterval(refresh, autoRefreshInterval.value * 1000);
}

function stopTimer() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

async function onTestSubmit() {
  if (!testSourceTopic.value || !testRawJson.value) {
    createMessage.warning(t('iot.mqs.bus.stats.test.fail'));
    return;
  }
  testLoading.value = true;
  try {
    testOutcome.value = await dispatchManual(testSourceTopic.value, testRawJson.value);
    createMessage.success(t('iot.mqs.bus.stats.test.success'));
  } catch (e: any) {
    createMessage.error(t('iot.mqs.bus.stats.test.fail') + ': ' + (e?.message ?? ''));
  } finally {
    testLoading.value = false;
  }
}

onMounted(refresh);
onUnmounted(stopTimer);
</script>

<style scoped>
.bus-stats-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.bus-health-cards {
  margin-bottom: 8px;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
}
</style>
