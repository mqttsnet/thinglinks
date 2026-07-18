<template>
  <BasicDrawer
    v-bind="$attrs"
    width="80%"
    @register="register"
    :title="t('iot.link.engine.executionLog.executionLog')"
    :maskClosable="false"
    class="rule-log-drawer"
  >
    <div class="log-metric-row">
      <div class="metric-card">
        <div class="metric-mark total"></div>
        <div>
          <div class="metric-label">{{ t('iot.link.engine.executionLog.metricTotal') }}</div>
          <div class="metric-value">{{ pageStats.total }}</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-mark done"></div>
        <div>
          <div class="metric-label">{{ t('iot.link.engine.executionLog.metricCompleted') }}</div>
          <div class="metric-value">{{ pageStats.completed }}</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-mark processing"></div>
        <div>
          <div class="metric-label">{{ t('iot.link.engine.executionLog.metricExecuting') }}</div>
          <div class="metric-value">{{ pageStats.executing }}</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-mark warning"></div>
        <div>
          <div class="metric-label">{{ t('iot.link.engine.executionLog.metricNotExecuted') }}</div>
          <div class="metric-value">{{ pageStats.notExecuted }}</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-mark avg"></div>
        <div>
          <div class="metric-label">{{ t('iot.link.engine.executionLog.metricAvgDuration') }}</div>
          <div class="metric-value">{{ pageStats.avgDurationText }}</div>
        </div>
      </div>
    </div>

    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button @click="handleOpenStats">
          <template #icon><BarChartOutlined /></template>
          {{ t('iot.link.engine.executionLog.statsTitle') }}
        </a-button>
        <a-button danger @click="handleClearLogs">
          <template #icon><DeleteOutlined /></template>
          {{ t('iot.link.engine.executionLog.clearLogs') }}
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">
          {{ getDictLabel('RULE_EXECUTION_LOG_STATUS', record.status, '') }}
        </a-tag>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.view'),
                icon: 'ant-design:eye-outlined',
                onClick: handleView.bind(null, record),
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>

    <BasicModal
      @register="registerModel"
      :title="t('iot.link.engine.executionLog.detailTitle')"
      :maskClosable="false"
      :keyboard="true"
      width="1080px"
      :showOkBtn="false"
      :showCancelBtn="false"
      class="rule-log-detail-modal"
    >
      <a-spin :spinning="loading">
        <div class="log-detail" v-if="activeRecord.id || detail.id">
          <div class="summary-card">
            <div class="summary-main">
              <a-tag :color="getStatusColor(activeRecord.status)" class="status-tag">
                {{ getDictLabel('RULE_EXECUTION_LOG_STATUS', activeRecord.status, '-') }}
              </a-tag>
              <div class="summary-title">
                <div class="rule-name">{{ detail.ruleName || activeRecord.ruleName || '-' }}</div>
                <div class="rule-id">
                  {{ t('iot.link.engine.executionLog.id') }}:
                  <span>{{ detail.id || activeRecord.id || '-' }}</span>
                  <a-tooltip :title="t('common.title.copy')">
                    <CopyOutlined
                      class="copy-icon"
                      @click="copyText(detail.id || activeRecord.id)"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <div class="summary-stats">
              <div class="stat-item">
                <span>{{ t('iot.link.engine.executionLog.triggerSource') }}</span>
                <b>{{ detail.triggerSource || '-' }}</b>
              </div>
              <div class="stat-item">
                <span>{{ t('iot.link.engine.executionLog.stepCount') }}</span>
                <b>{{ detail.stepCount || stepLogs.length }}</b>
              </div>
              <div class="stat-item">
                <span>{{ t('iot.link.engine.executionLog.duration') }}</span>
                <b>{{ durationText }}</b>
              </div>
              <div class="stat-item wide">
                <span>{{ t('iot.link.engine.executionLog.startTime') }}</span>
                <b>{{ detail.startTime || activeRecord.startTime || '-' }}</b>
              </div>
            </div>
          </div>

          <div class="path-card">
            <div class="section-head">{{ t('iot.link.engine.executionLog.executionPath') }}</div>
            <div class="path-chain" v-if="stepLogs.length">
              <template v-for="(step, index) in stepLogs" :key="stepKey(step, index)">
                <button
                  type="button"
                  class="path-chip"
                  :class="statusClass(step.status)"
                  @click="scrollToStep(index)"
                >
                  <span>{{ step.stepName || step.stepType || '-' }}</span>
                  <em>{{ formatLatency(step.latencyMs) }}</em>
                </button>
                <span v-if="index < stepLogs.length - 1" class="path-arrow">→</span>
              </template>
            </div>
            <a-empty v-else :description="t('iot.link.engine.executionLog.noSteps')" />
          </div>

          <div class="step-list" v-if="stepLogs.length">
            <div
              class="step-card"
              :class="statusClass(item.status)"
              v-for="(item, index) in stepLogs"
              :key="stepKey(item, index)"
              :ref="(el) => setStepRef(el, index)"
            >
              <div class="step-index">{{ item.stepNo || index + 1 }}</div>
              <div class="step-content">
                <div class="step-top">
                  <div>
                    <div class="step-title">{{ item.stepName || '-' }}</div>
                    <div class="step-meta">
                      <span>{{ item.startedAt || '-' }}</span>
                      <span>{{ item.stepType || '-' }}</span>
                      <span>{{ formatLatency(item.latencyMs) }}</span>
                    </div>
                  </div>
                  <a-tag :color="stepStatusColor(item.status)">
                    {{ stepStatusText(item.status) }}
                  </a-tag>
                </div>
                <div class="step-text" v-if="item.remark || item.errorMsg">
                  {{ item.errorMsg || item.remark }}
                </div>
                <div class="step-io">
                  <div class="io-block" v-if="item.inputSummary">
                    <div class="io-title">{{ t('iot.link.engine.executionLog.input') }}</div>
                    <pre class="params">{{ formatPayload(item.inputSummary) }}</pre>
                  </div>
                  <div class="io-block" v-if="item.outputSummary">
                    <div class="io-title">{{ t('iot.link.engine.executionLog.output') }}</div>
                    <pre class="params">{{ formatPayload(item.outputSummary) }}</pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else :description="t('iot.link.engine.executionLog.noSteps')" />

          <a-collapse ghost class="raw-collapse">
            <a-collapse-panel key="raw" :header="t('iot.link.engine.executionLog.rawInfo')">
              <pre class="raw-json" v-if="rawInfo">{{ rawInfo }}</pre>
              <a-empty v-else :description="t('iot.link.engine.executionLog.noRawInfo')" />
            </a-collapse-panel>
          </a-collapse>
        </div>
      </a-spin>
    </BasicModal>

    <BasicModal
      @register="registerStatsModel"
      :title="t('iot.link.engine.executionLog.statsTitle')"
      :maskClosable="false"
      :showOkBtn="false"
      :showCancelBtn="false"
      width="980px"
      class="rule-log-stats-modal"
    >
      <a-spin :spinning="statsLoading">
        <div class="stats-grid">
          <div class="stats-card distribution">
            <div class="stats-card-title">
              {{ t('iot.link.engine.executionLog.statsDistribution') }}
            </div>
            <div ref="pieRef" class="pie-canvas"></div>
            <div class="stats-legend">
              <div v-for="item in pieLegend" :key="item.name" class="legend-row">
                <span class="dot" :style="{ background: item.color }"></span>
                <span class="legend-name">{{ item.name }}</span>
                <span class="legend-value">{{ item.value }}</span>
              </div>
            </div>
          </div>
          <div class="stats-card timeline">
            <div class="stats-card-title">
              {{ t('iot.link.engine.executionLog.statsTimeline') }}
            </div>
            <div ref="lineRef" class="line-canvas"></div>
          </div>
        </div>
      </a-spin>
    </BasicModal>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, nextTick, reactive, ref, type ComponentPublicInstance, type Ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BarChartOutlined, CopyOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import {
    clearLogs,
    page,
    stats,
    getRuleExecutionLogDetails,
  } from '/@/api/iot/rule/engine/linkage/executionLog';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { columns, searchFormSchema } from './executionLogDetail';
  import { useDict } from '/@/components/Dict';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { useECharts } from '/@/hooks/web/useECharts';

  defineOptions({ name: 'RuleExecutionLogDetail' });

  const { getDictLabel } = useDict();
  const { t } = useI18n();
  const { createMessage, createConfirm } = useMessage();

  const loading = ref(false);
  const statsLoading = ref(false);
  const propertyMetadata = ref<Recordable>({});
  const activeRecord = ref<Recordable>({});
  const detail = ref<Recordable>({});
  const lastFetchParams = ref<Recordable>({});
  const pieRef = ref<HTMLDivElement | null>(null);
  const lineRef = ref<HTMLDivElement | null>(null);
  const { setOptions: setPieOptions } = useECharts(pieRef as Ref<HTMLDivElement>);
  const { setOptions: setLineOptions } = useECharts(lineRef as Ref<HTMLDivElement>);
  const pageStats = reactive<{
    total: number;
    completed: number | string;
    executing: number | string;
    notExecuted: number | string;
    avgDuration: number | string;
    avgDurationText: string;
  }>({
    total: 0,
    completed: '-',
    executing: '-',
    notExecuted: '-',
    avgDuration: '-',
    avgDurationText: '-',
  });
  const statsData = reactive<Recordable>({
    total: 0,
    completed: 0,
    executing: 0,
    notExecuted: 0,
    avgLatencyMs: 0,
    timeline: [],
  });
  const stepRefs = ref<HTMLElement[]>([]);

  const [register] = useDrawerInner((data) => {
    propertyMetadata.value = data || {};
    reload();
  });

  const [registerTable, { reload }] = useTable({
    title: t('iot.link.engine.executionLog.table.title'),
    api: async (params) => {
      const res = await page(params);
      refreshPageStats(undefined, res);
      stats(params)
        .then((statsRes) => refreshPageStats(statsRes, res))
        .catch(() => refreshPageStats(undefined, res));
      return res;
    },
    immediate: false,
    columns: columns(),
    formConfig: {
      name: 'ProductSearch',
      labelWidth: 110,
      schemas: searchFormSchema(),
      fieldMapToTime: [
        ['startTimeRange', ['startTimeBegin', 'startTimeEnd'], 'YYYY-MM-DD HH:mm:ss'],
      ],
      autoSubmitOnEnter: true,
      resetButtonOptions: {
        preIcon: 'ant-design:rest-outlined',
      },
      submitButtonOptions: {
        preIcon: 'ant-design:search-outlined',
      },
    },
    beforeFetch: (params) => {
      const nextParams = handleFetchParams(params);
      const range = nextParams.model?.startTimeRange;
      if (Array.isArray(range)) {
        nextParams.model.startTimeBegin = range[0];
        nextParams.model.startTimeEnd = range[1];
        delete nextParams.model.startTimeRange;
      }
      nextParams.model = {
        ...(nextParams.model || {}),
        ruleIdentification: propertyMetadata.value.ruleIdentification,
      };
      lastFetchParams.value = nextParams;
      return nextParams;
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    showIndexColumn: false,
    canResize: false,
    actionColumn: {
      width: 100,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  const [registerModel, { openModal }] = useModal();
  const [registerStatsModel, { openModal: openStatsModal }] = useModal();

  const pieLegend = computed(() => [
    {
      name: t('iot.link.engine.executionLog.metricCompleted'),
      value: Number(statsData.completed || 0),
      color: '#13deb9',
    },
    {
      name: t('iot.link.engine.executionLog.metricExecuting'),
      value: Number(statsData.executing || 0),
      color: '#ffae1f',
    },
    {
      name: t('iot.link.engine.executionLog.metricNotExecuted'),
      value: Number(statsData.notExecuted || 0),
      color: '#5d87ff',
    },
  ]);

  const stepLogs = computed<Recordable[]>(() => {
    const steps = detail.value.steps;
    return Array.isArray(steps) ? steps : [];
  });

  const durationText = computed(() => {
    if (detail.value.totalLatencyMs != null) {
      return formatLatency(detail.value.totalLatencyMs);
    }
    const extendLatency = parseExtendLatency(
      detail.value.extendParams || activeRecord.value.extendParams,
    );
    if (extendLatency != null) {
      return formatLatency(extendLatency);
    }
    const start = detail.value.startTime || activeRecord.value.startTime;
    const end = detail.value.endTime || activeRecord.value.endTime;
    const diff = Date.parse(end || '') - Date.parse(start || '');
    return Number.isFinite(diff) && diff >= 0 ? formatLatency(diff) : '-';
  });

  const rawInfo = computed(() => {
    const payload = {
      rule: {
        id: detail.value.id || activeRecord.value.id,
        ruleIdentification:
          detail.value.ruleIdentification || activeRecord.value.ruleIdentification,
        ruleName: detail.value.ruleName || activeRecord.value.ruleName,
        status: detail.value.status || activeRecord.value.status,
        startTime: detail.value.startTime || activeRecord.value.startTime,
        endTime: detail.value.endTime || activeRecord.value.endTime,
        totalLatencyMs: detail.value.totalLatencyMs || activeRecord.value.totalLatencyMs,
        stepCount: detail.value.stepCount,
        remark: detail.value.remark || activeRecord.value.remark,
        extendParams: detail.value.extendParams || activeRecord.value.extendParams,
        triggerSource: detail.value.triggerSource,
        resultSummary: detail.value.resultSummary,
      },
      steps: stepLogs.value,
    };
    return JSON.stringify(payload, null, 2);
  });

  function refreshPageStats(result?: Recordable, pageResult?: Recordable) {
    pageStats.total = Number(result?.total ?? pageResult?.total ?? 0);
    pageStats.completed = result?.completed ?? '-';
    pageStats.executing = result?.executing ?? '-';
    pageStats.notExecuted = result?.notExecuted ?? '-';
    pageStats.avgDuration = result?.avgLatencyMs ?? '-';
    pageStats.avgDurationText =
      pageStats.avgDuration === '-' || pageStats.avgDuration == null
        ? '-'
        : formatLatency(pageStats.avgDuration);
  }

  function buildScopedParams() {
    const source = lastFetchParams.value || {};
    const model = {
      ...(source.model || {}),
      ruleIdentification: propertyMetadata.value.ruleIdentification,
    };
    delete model.startTimeRange;
    return {
      ...source,
      current: source.current || 1,
      size: source.size || 20,
      model,
    };
  }

  async function handleOpenStats() {
    openStatsModal(true);
    await nextTick();
    await loadStatsData();
  }

  async function loadStatsData() {
    statsLoading.value = true;
    try {
      let result: Recordable | undefined;
      try {
        result = await stats(buildScopedParams());
      } catch {
        result = buildStatsFallbackFromCurrentPage();
      }
      Object.assign(statsData, normalizeStatsResult(result));
      await waitForStatsChartReady();
      drawStatsCharts();
    } finally {
      statsLoading.value = false;
    }
  }

  function buildStatsFallbackFromCurrentPage() {
    return {
      total: 0,
      completed: 0,
      executing: 0,
      notExecuted: 0,
      avgLatencyMs: 0,
      timeline: [],
    };
  }

  async function waitForStatsChartReady() {
    for (let index = 0; index < 10; index++) {
      await nextTick();
      if (pieRef.value?.offsetHeight && lineRef.value?.offsetHeight) {
        return;
      }
      await new Promise((resolve) => window.setTimeout(resolve, 30));
    }
  }

  function normalizeStatsResult(result?: Recordable) {
    const completed = Number(result?.completed || 0);
    const executing = Number(result?.executing || 0);
    const notExecuted = Number(result?.notExecuted || 0);
    const total = Number(result?.total ?? completed + executing + notExecuted);
    const avgLatencyMs = Number(result?.avgLatencyMs || 0);
    let timeline = Array.isArray(result?.timeline) ? result.timeline : [];
    if (!timeline.length && total > 0) {
      timeline = [
        {
          timeLabel: t('iot.link.engine.executionLog.statsCurrent'),
          completed,
          executing,
          notExecuted,
          avgLatencyMs,
        },
      ];
    }
    return {
      total,
      completed,
      executing,
      notExecuted,
      avgLatencyMs,
      timeline,
    };
  }

  function handleClearLogs() {
    createConfirm({
      iconType: 'warning',
      title: t('iot.link.engine.executionLog.clearLogs'),
      content: t('iot.link.engine.executionLog.clearLogsConfirm'),
      onOk: async () => {
        const count = await clearLogs(buildScopedParams());
        createMessage.success(
          t('iot.link.engine.executionLog.clearLogsSuccess', { count: count || 0 }),
        );
        await reload();
      },
    });
  }

  function drawStatsCharts() {
    drawPieChart();
    drawLineChart();
  }

  function drawPieChart() {
    const hasData = pieLegend.value.some((item) => item.value > 0);
    setPieOptions({
      tooltip: { trigger: 'item' },
      legend: { show: false },
      graphic: hasData
        ? []
        : [
            {
              type: 'text',
              left: 'center',
              top: 'middle',
              style: {
                text: t('iot.link.engine.executionLog.statsNoData'),
                fill: '#8c94a3',
                fontSize: 13,
              },
            },
          ],
      series: [
        {
          name: t('iot.link.engine.executionLog.statsDistribution'),
          type: 'pie',
          radius: ['58%', '78%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: hasData,
            formatter: '{b}\n{d}%',
            color: '#2a3547',
            fontSize: 12,
          },
          data: hasData
            ? pieLegend.value.map((item) => ({
                name: item.name,
                value: item.value,
                itemStyle: { color: item.color },
              }))
            : [
                {
                  name: t('iot.link.engine.executionLog.statsNoData'),
                  value: 1,
                  itemStyle: { color: '#edf2f7' },
                },
              ],
        },
      ],
    });
  }

  function drawLineChart() {
    const timeline = Array.isArray(statsData.timeline) ? statsData.timeline : [];
    const labels = timeline.length
      ? timeline.map((item) => item.timeLabel || '-')
      : [t('iot.link.engine.executionLog.statsNoData')];
    const hasData = timeline.some(
      (item) =>
        Number(item.completed || 0) > 0 ||
        Number(item.executing || 0) > 0 ||
        Number(item.notExecuted || 0) > 0,
    );
    setLineOptions({
      tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
      legend: {
        top: 0,
        data: pieLegend.value.map((item) => item.name),
        textStyle: { color: '#667085', fontSize: 12 },
      },
      grid: { left: 38, right: 24, top: 42, bottom: 30 },
      graphic: hasData
        ? []
        : [
            {
              type: 'text',
              left: 'center',
              top: 'middle',
              style: {
                text: t('iot.link.engine.executionLog.statsNoData'),
                fill: '#8c94a3',
                fontSize: 13,
              },
            },
          ],
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: labels,
        axisLabel: { color: '#667085', fontSize: 11, hideOverlap: true },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#667085', fontSize: 11 },
        splitLine: { lineStyle: { color: '#edf2f7' } },
      },
      series: [
        {
          name: t('iot.link.engine.executionLog.metricCompleted'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: timeline.length ? timeline.map((item) => Number(item.completed || 0)) : [0],
          itemStyle: { color: '#13deb9' },
          areaStyle: { color: 'rgba(19, 222, 185, 0.14)' },
        },
        {
          name: t('iot.link.engine.executionLog.metricExecuting'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: timeline.length ? timeline.map((item) => Number(item.executing || 0)) : [0],
          itemStyle: { color: '#ffae1f' },
          areaStyle: { color: 'rgba(255, 174, 31, 0.14)' },
        },
        {
          name: t('iot.link.engine.executionLog.metricNotExecuted'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: timeline.length ? timeline.map((item) => Number(item.notExecuted || 0)) : [0],
          itemStyle: { color: '#5d87ff' },
          areaStyle: { color: 'rgba(93, 135, 255, 0.14)' },
        },
      ],
    });
  }

  async function handleView(record: Recordable, e: Event) {
    e?.stopPropagation();
    activeRecord.value = record || {};
    detail.value = {};
    stepRefs.value = [];
    openModal(true);
    loading.value = true;
    try {
      detail.value = await getRuleExecutionLogDetails(record.id);
    } finally {
      loading.value = false;
    }
  }

  function stepKey(step: Recordable, index: number) {
    return `${step.stepType || 'step'}-${step.id || step.stepNo || index}`;
  }

  function setStepRef(el: Element | ComponentPublicInstance | null, index: number) {
    if (!el) return;
    stepRefs.value[index] = el as HTMLElement;
  }

  function scrollToStep(index: number) {
    const target = stepRefs.value[index];
    if (target?.scrollIntoView) {
      target.scrollIntoView({ block: 'center', behavior: 'smooth' });
    }
  }

  function formatLatency(value?: number | string) {
    const next = Number(value);
    if (!Number.isFinite(next) || next < 0) return '-';
    return next === 0 ? '< 1 ms' : `${next} ms`;
  }

  function parseExtendLatency(extendParams?: string) {
    if (!extendParams) return undefined;
    try {
      const parsed = JSON.parse(extendParams);
      const latency = Number(parsed?.latencyMs);
      return Number.isFinite(latency) && latency >= 0 ? latency : undefined;
    } catch {
      return undefined;
    }
  }

  function formatPayload(value?: unknown) {
    if (!value) return '-';
    if (typeof value !== 'string') {
      return JSON.stringify(value, null, 2);
    }
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }

  function statusClass(status?: string) {
    if (status === '01') return 'failed';
    if (status === '02') return 'skipped';
    return 'success';
  }

  function stepStatusColor(status?: string) {
    if (status === '01') return 'error';
    if (status === '02') return 'warning';
    return 'success';
  }

  function stepStatusText(status?: string) {
    if (status === '01') return t('iot.link.engine.executionLog.failed');
    if (status === '02') return t('iot.link.engine.executionLog.skipped');
    return t('iot.link.engine.executionLog.success');
  }

  function getStatusColor(status?: number | string) {
    const value = Number(status);
    if (value === 2) return 'success';
    if (value === 1) return 'warning';
    return 'error';
  }

  function copyText(text?: number | string) {
    if (!text) return;
    const result = copyTextToClipboard(String(text));
    if (result) {
      createMessage.success(t('common.tips.copySuccess'));
    } else {
      createMessage.warning(t('common.tips.copyFail'));
    }
  }
</script>

<style lang="less" scoped>
  .rule-log-drawer {
    :deep(.vben-basic-table-form-container) {
      padding-top: 4px;
    }
  }

  .log-metric-row {
    display: grid;
    grid-template-columns: repeat(5, minmax(130px, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  .metric-card {
    display: flex;
    min-height: 76px;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    background: @component-background;
    border: 1px solid @border-color-base;
    border-radius: 8px;
  }

  .metric-mark {
    width: 6px;
    height: 34px;
    flex: 0 0 6px;
    background: @primary-color;
    border-radius: 6px;

    &.done {
      background: @success-color;
    }

    &.processing {
      background: @warning-color;
    }

    &.warning {
      background: @warning-color;
    }

    &.avg {
      background: @info-color;
    }
  }

  .metric-label {
    color: @text-color-secondary;
    font-size: 13px;
    line-height: 18px;
  }

  .metric-value {
    margin-top: 4px;
    color: @text-color;
    font-size: 22px;
    font-weight: 600;
    line-height: 28px;

    small {
      margin-left: 3px;
      color: @text-color-secondary;
      font-size: 12px;
      font-weight: 400;
    }
  }

  .log-detail {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .summary-card,
  .path-card {
    padding: 18px 20px;
    background: @component-background;
    border: 1px solid @border-color-base;
    border-radius: 8px;
  }

  .summary-card {
    display: flex;
    justify-content: space-between;
    gap: 24px;
  }

  .summary-main {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 14px;
  }

  .status-tag {
    padding: 4px 12px;
    border-radius: 6px;
  }

  .summary-title {
    min-width: 0;
  }

  .rule-name {
    color: @text-color;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
  }

  .rule-id {
    margin-top: 5px;
    color: @text-color-secondary;
    font-size: 13px;

    span {
      margin-left: 4px;
      color: @text-color;
      font-family: Menlo, Consolas, monospace;
    }
  }

  .copy-icon {
    margin-left: 8px;
    color: @primary-color;
    cursor: pointer;
  }

  .summary-stats {
    display: grid;
    grid-template-columns: repeat(4, minmax(90px, auto));
    gap: 18px;
    align-items: center;
  }

  .stat-item {
    min-width: 90px;

    span {
      display: block;
      color: @text-color-secondary;
      font-size: 12px;
      line-height: 18px;
    }

    b {
      display: block;
      margin-top: 4px;
      color: @text-color;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
    }

    &.wide {
      min-width: 160px;

      b {
        font-size: 14px;
      }
    }
  }

  .section-head {
    margin-bottom: 12px;
    color: @text-color;
    font-weight: 600;
  }

  .path-chain {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .path-chip {
    display: inline-flex;
    max-width: 240px;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    overflow: hidden;
    color: @primary-color;
    font-size: 13px;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
    background: fade(@primary-color, 8%);
    border: 1px solid fade(@primary-color, 22%);
    border-radius: 18px;
    cursor: pointer;

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    em {
      flex: 0 0 auto;
      color: @text-color-secondary;
      font-size: 12px;
      font-style: normal;
    }

    &.skipped {
      color: @warning-color;
      background: fade(@warning-color, 8%);
      border-color: fade(@warning-color, 26%);
    }

    &.failed {
      color: @error-color;
      background: fade(@error-color, 8%);
      border-color: fade(@error-color, 22%);
    }
  }

  .path-arrow {
    color: @text-color-secondary;
  }

  .step-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .step-card {
    display: flex;
    gap: 14px;
    padding: 16px;
    background: @component-background;
    border: 1px solid @border-color-base;
    border-left: 4px solid @primary-color;
    border-radius: 8px;

    &.success {
      border-left-color: @success-color;
    }

    &.skipped {
      border-left-color: @warning-color;
    }

    &.failed {
      border-left-color: @error-color;
    }
  }

  .step-index {
    width: 28px;
    height: 28px;
    flex: 0 0 28px;
    color: @primary-color;
    font-weight: 600;
    line-height: 28px;
    text-align: center;
    background: fade(@primary-color, 10%);
    border-radius: 50%;
  }

  .step-content {
    min-width: 0;
    flex: 1;
  }

  .step-top {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .step-title {
    color: @text-color;
    font-size: 15px;
    font-weight: 600;
  }

  .step-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 6px;
    color: @text-color-secondary;
    font-size: 12px;
  }

  .step-text {
    margin-top: 10px;
    color: @text-color;
    line-height: 1.6;
  }

  .step-io {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
    margin-top: 12px;
  }

  .io-title {
    margin-bottom: 6px;
    color: @text-color-secondary;
    font-size: 12px;
  }

  .params,
  .raw-json {
    margin: 12px 0 0;
    padding: 12px;
    overflow: auto;
    color: @text-color;
    font-size: 12px;
    line-height: 1.6;
    background: @background-color-light;
    border: 1px solid @border-color-base;
    border-radius: 6px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .io-block .params {
    max-height: 280px;
    margin-top: 0;
    color: #dce6ff;
    background: #101827;
    border-color: #101827;
  }

  .raw-json {
    max-height: 520px;
    margin-top: 0;
  }

  .raw-collapse {
    background: @component-background;
    border: 1px solid @border-color-base;
    border-radius: 8px;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: 320px minmax(0, 1fr);
    gap: 16px;
  }

  .stats-card {
    min-height: 340px;
    padding: 16px;
    background: @component-background;
    border: 1px solid @border-color-base;
    border-radius: 8px;
  }

  .stats-card-title {
    color: @text-color;
    font-size: 15px;
    font-weight: 600;
    line-height: 22px;
  }

  .pie-canvas {
    width: 100%;
    height: 220px;
  }

  .line-canvas {
    width: 100%;
    height: 292px;
  }

  .stats-legend {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-top: 8px;
    border-top: 1px dashed @border-color-base;
  }

  .legend-row {
    display: flex;
    align-items: center;
    gap: 8px;
    color: @text-color-secondary;
    font-size: 13px;
  }

  .dot {
    width: 8px;
    height: 8px;
    flex: 0 0 8px;
    border-radius: 50%;
  }

  .legend-name {
    flex: 1;
  }

  .legend-value {
    color: @text-color;
    font-weight: 600;
  }

  @media (max-width: 960px) {
    .log-metric-row {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .summary-card {
      flex-direction: column;
    }

    .summary-stats {
      grid-template-columns: repeat(2, minmax(120px, 1fr));
    }

    .step-io {
      grid-template-columns: 1fr;
    }

    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
