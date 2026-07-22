<template>
  <div class="bridge-stats">
    <!-- 顶部 4 个数字 -->
    <Row :gutter="12" class="stats-summary">
      <Col :span="6">
        <div class="sum-cell ok">
          <div class="sum-label">{{ t('iot.rule.integration.bridge.detail.stats.success') }}</div>
          <div class="sum-value">{{ summary.success }}</div>
        </div>
      </Col>
      <Col :span="6">
        <div class="sum-cell fail">
          <div class="sum-label">{{ t('iot.rule.integration.bridge.detail.stats.failed') }}</div>
          <div class="sum-value">{{ summary.failed }}</div>
        </div>
      </Col>
      <Col :span="6">
        <div class="sum-cell dlq">
          <div class="sum-label">{{ t('iot.rule.integration.bridge.detail.stats.deadLetter') }}</div>
          <div class="sum-value">{{ summary.deadLetter }}</div>
        </div>
      </Col>
      <Col :span="6">
        <div class="sum-cell avg">
          <div class="sum-label">{{ t('iot.rule.integration.bridge.detail.stats.avgLatency') }}</div>
          <div class="sum-value">
            {{ summary.avgLatency }}<small>ms</small>
          </div>
        </div>
      </Col>
    </Row>

    <!-- 折线图 -->
    <div class="chart-toolbar">
      <span class="hint">{{ t('iot.rule.integration.bridge.detail.stats.title') }}</span>
      <a-radio-group v-model:value="hoursRange" size="small" @change="reload">
        <a-radio-button :value="6">6h</a-radio-button>
        <a-radio-button :value="12">12h</a-radio-button>
        <a-radio-button :value="24">24h</a-radio-button>
      </a-radio-group>
    </div>
    <a-spin :spinning="loading">
      <Empty
        v-if="!loading && totalCount === 0"
        :description="t('iot.rule.integration.bridge.detail.statsEmpty')"
        style="padding: 60px 0"
      />
      <div v-else ref="chartRef" class="chart-canvas"></div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted, watch, type Ref } from 'vue';
  import { Row, Col, Empty } from 'ant-design-vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { dateUtil } from '/@/utils/dateUtil';
  import { page as pageTrace } from '/@/api/iot/rule/integration/bridgeExecutionTrace';

  const props = defineProps<{
    bridgeRuleId: string;
  }>();

  const { t } = useI18n();
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

  const loading = ref(false);
  const hoursRange = ref(24);
  const buckets = ref<{ hour: string; success: number; failed: number; dlq: number; avgLatency: number }[]>([]);

  const summary = reactive({
    success: 0,
    failed: 0,
    deadLetter: 0,
    avgLatency: 0,
  });

  const totalCount = computed(() => summary.success + summary.failed + summary.deadLetter);

  /**
   * 拉取该规则最近 N 小时的所有 trace（最多 500 条），按小时分桶聚合。
   * 后端如有 stats 接口可替换成它（更高效），当前用 page 接口前端聚合。
   */
  async function reload() {
    if (!props.bridgeRuleId) return;
    loading.value = true;
    try {
      const since = dateUtil()
        .subtract(hoursRange.value, 'hour')
        .format('YYYY-MM-DD HH:mm:ss');
      const res: any = await pageTrace({
        size: 500,
        current: 1,
        model: { bridgeRuleId: props.bridgeRuleId, startTimeBegin: since },
      } as any);
      const records: any[] = res?.records ?? [];
      buckets.value = aggregateByHour(records, hoursRange.value);
      // 顶部 summary
      summary.success = records.filter((r) => r.status === '00').length;
      summary.failed = records.filter((r) => r.status === '01').length;
      summary.deadLetter = records.filter((r) => r.status === '03').length;
      const sumLat = records.reduce((s, r) => s + (r.totalLatencyMs ?? 0), 0);
      summary.avgLatency = records.length ? Math.round(sumLat / records.length) : 0;
      drawChart();
    } finally {
      loading.value = false;
    }
  }

  /** 把 records 按"yyyy-MM-dd HH"分桶，统计成功/失败/死信数 + 平均耗时 */
  function aggregateByHour(records: any[], hours: number) {
    const slot: Record<string, any> = {};
    // 先填空桶
    const now = dateUtil().startOf('hour');
    for (let i = hours - 1; i >= 0; i--) {
      const h = now.subtract(i, 'hour').format('MM-DD HH:00');
      slot[h] = { hour: h, success: 0, failed: 0, dlq: 0, latSum: 0, latCount: 0 };
    }
    // 填实数据
    records.forEach((r) => {
      if (!r.startTime) return;
      const key = dateUtil(r.startTime).format('MM-DD HH:00');
      if (!slot[key]) return;
      if (r.status === '00') slot[key].success += 1;
      else if (r.status === '01') slot[key].failed += 1;
      else if (r.status === '03') slot[key].dlq += 1;
      slot[key].latSum += r.totalLatencyMs ?? 0;
      slot[key].latCount += 1;
    });
    return Object.values(slot).map((b: any) => ({
      hour: b.hour,
      success: b.success,
      failed: b.failed,
      dlq: b.dlq,
      avgLatency: b.latCount ? Math.round(b.latSum / b.latCount) : 0,
    }));
  }

  function drawChart() {
    const xAxisData = buckets.value.map((b) => b.hour);
    setOptions({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross', label: { backgroundColor: '#5d87ff' } },
      },
      legend: {
        data: [
          t('iot.rule.integration.bridge.detail.stats.success'),
          t('iot.rule.integration.bridge.detail.stats.failed'),
          t('iot.rule.integration.bridge.detail.stats.deadLetter'),
          t('iot.rule.integration.bridge.detail.stats.avgLatency'),
        ],
        top: 8,
        textStyle: { color: '#6b7280', fontSize: 12 },
      },
      grid: { left: 40, right: 40, top: 48, bottom: 28 },
      xAxis: {
        type: 'category',
        data: xAxisData,
        boundaryGap: false,
        axisLabel: { color: '#6b7280', fontSize: 11 },
        axisLine: { lineStyle: { color: '#e8eaf0' } },
      },
      yAxis: [
        {
          type: 'value',
          name: t('iot.rule.integration.bridge.detail.stats.count'),
          axisLabel: { color: '#6b7280', fontSize: 11 },
          splitLine: { lineStyle: { color: '#f0f2f5' } },
        },
        {
          type: 'value',
          name: 'ms',
          axisLabel: { color: '#6b7280', fontSize: 11 },
          splitLine: { show: false },
        },
      ],
      series: [
        {
          name: t('iot.rule.integration.bridge.detail.stats.success'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.value.map((b) => b.success),
          itemStyle: { color: '#13deb9' },
          areaStyle: { color: 'rgba(19, 222, 185, 0.15)' },
        },
        {
          name: t('iot.rule.integration.bridge.detail.stats.failed'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.value.map((b) => b.failed),
          itemStyle: { color: '#fa4b4b' },
          areaStyle: { color: 'rgba(250, 75, 75, 0.15)' },
        },
        {
          name: t('iot.rule.integration.bridge.detail.stats.deadLetter'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.value.map((b) => b.dlq),
          itemStyle: { color: '#f74ba1' },
        },
        {
          name: t('iot.rule.integration.bridge.detail.stats.avgLatency'),
          type: 'bar',
          yAxisIndex: 1,
          data: buckets.value.map((b) => b.avgLatency),
          itemStyle: { color: 'rgba(93, 135, 255, 0.4)', borderRadius: [4, 4, 0, 0] },
          barMaxWidth: 18,
        },
      ],
    });
  }

  onMounted(() => reload());

  watch(
    () => props.bridgeRuleId,
    () => reload(),
  );
</script>

<style lang="less" scoped>
  .bridge-stats {
    padding: 0 4px;
  }

  .stats-summary {
    margin-bottom: 16px;
  }

  .sum-cell {
    padding: 14px 16px;
    border-radius: 10px;
    background: #f6f8fb;
    border: 1px solid #e8eaf0;

    .sum-label {
      font-size: 12px;
      color: #6b7280;
      margin-bottom: 4px;
    }

    .sum-value {
      font-size: 22px;
      font-weight: 700;
      color: #2a3547;

      small {
        font-size: 12px;
        color: #6b7280;
        font-weight: 400;
        margin-left: 2px;
      }
    }

    &.ok {
      .sum-value {
        color: #13deb9;
      }
    }

    &.fail {
      .sum-value {
        color: #fa4b4b;
      }
    }

    &.dlq {
      .sum-value {
        color: #f74ba1;
      }
    }

    &.avg {
      .sum-value {
        color: #5d87ff;
      }
    }
  }

  .chart-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .hint {
      color: #6b7280;
      font-size: 13px;
      font-weight: 500;
    }
  }

  .chart-canvas {
    height: 320px;
    width: 100%;
  }
</style>
