<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.rule.integration.log.action.statistics')"
    :showFooter="false"
    width="960px"
  >
    <a-spin :spinning="loading">
      <a-alert
        type="info"
        show-icon
        :message="t('iot.rule.integration.log.statistics.scopeHint')"
        style="margin-bottom: 16px"
      />
      <Row :gutter="16" class="stats-row">
        <Col :span="8">
          <Card :bordered="false" class="chart-card">
            <template #title>
              <span class="title">{{ t('iot.rule.integration.log.statistics.distribution') }}</span>
            </template>
            <div ref="pieRef" class="pie-canvas"></div>
            <div class="pie-legend">
              <div v-for="d in pieData" :key="d.name" class="legend-row">
                <span class="dot" :style="{ background: d.itemStyle.color }"></span>
                <span class="legend-name">{{ d.name }}</span>
                <span class="legend-val">{{ d.value }}</span>
              </div>
            </div>
          </Card>
        </Col>
        <Col :span="16">
          <Card :bordered="false" class="chart-card">
            <template #title>
              <span class="title">{{ t('iot.rule.integration.log.statistics.timeline') }}</span>
            </template>
            <template #extra>
              <a-radio-group v-model:value="hoursRange" size="small" @change="reload">
                <a-radio-button :value="6">6h</a-radio-button>
                <a-radio-button :value="12">12h</a-radio-button>
                <a-radio-button :value="24">24h</a-radio-button>
              </a-radio-group>
            </template>
            <div ref="lineRef" class="line-canvas"></div>
          </Card>
        </Col>
      </Row>

      <!-- 顶部规则排行 -->
      <Card :bordered="false" class="chart-card top-rules">
        <template #title>
          <span class="title">{{ t('iot.rule.integration.log.statistics.topRules') }}</span>
        </template>
        <Empty
          v-if="!loading && topRules.length === 0"
          :description="t('iot.rule.integration.log.statistics.empty')"
          style="padding: 30px 0"
        />
        <a-list v-else size="small" :dataSource="topRules">
          <template #renderItem="{ item, index }">
            <a-list-item>
              <div class="rank-row">
                <div class="rank-no" :class="{ podium: index < 3 }">{{ index + 1 }}</div>
                <div class="rank-name">{{ item.ruleId }}</div>
                <div class="rank-bar-wrap">
                  <div
                    class="rank-bar"
                    :style="{ width: (item.count / (topRules[0]?.count || 1)) * 100 + '%' }"
                  ></div>
                </div>
                <div class="rank-count">{{ item.count }}</div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </Card>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, type Ref, nextTick } from 'vue';
  import { Row, Col, Card, Empty } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { dateUtil } from '/@/utils/dateUtil';
  import { stats as statsTrace } from '/@/api/iot/rule/integration/bridgeExecutionTrace';
  import type {
    BridgeExecutionTraceStatsResultVO,
    BridgeExecutionTraceStatsTimelinePoint,
  } from '/@/api/iot/rule/integration/model/bridgeExecutionTraceModel';

  defineOptions({ name: 'BridgeLogStatisticsModal' });

  const { t } = useI18n();
  const pieRef = ref<HTMLDivElement | null>(null);
  const lineRef = ref<HTMLDivElement | null>(null);
  const { setOptions: setPieOptions } = useECharts(pieRef as Ref<HTMLDivElement>);
  const { setOptions: setLineOptions } = useECharts(lineRef as Ref<HTMLDivElement>);

  const loading = ref(false);
  const hoursRange = ref(24);
  const pieData = ref<{ name: string; value: number; itemStyle: { color: string } }[]>([]);
  const topRules = ref<{ ruleId: string; count: number }[]>([]);

  type MetricKey = 'success' | 'failed' | 'partial' | 'deadLetter';

  const metricConfig: Array<{ key: MetricKey; fallback: string; color: string }> = [
    { key: 'success', fallback: '成功', color: '#13deb9' },
    { key: 'failed', fallback: '失败', color: '#fa4b4b' },
    { key: 'partial', fallback: '部分成功', color: '#ffae1f' },
    { key: 'deadLetter', fallback: '死信', color: '#f74ba1' },
  ];

  const [registerModal] = useModalInner(async () => {
    // 弹窗打开时拉数据
    await reload();
  });

  async function reload() {
    loading.value = true;
    try {
      const until = dateUtil().format('YYYY-MM-DD HH:mm:ss');
      const since = dateUtil().subtract(hoursRange.value, 'hour').format('YYYY-MM-DD HH:mm:ss');
      const res = await statsTrace({
        size: 1,
        current: 1,
        model: { startTimeBegin: since, startTimeEnd: until },
      } as any);
      applyStats(res || {});
      await nextTick();
      drawPie();
      drawLine(res?.timeline || []);
    } finally {
      loading.value = false;
    }
  }

  function metricName(key: MetricKey, fallback: string) {
    const i18nKey = `iot.rule.integration.log.metric.${key}`;
    const label = t(i18nKey);
    return label === i18nKey ? fallback : label;
  }

  function metricAreaColor(key: MetricKey) {
    if (key === 'success') return 'rgba(19, 222, 185, 0.18)';
    if (key === 'failed') return 'rgba(250, 75, 75, 0.18)';
    return undefined;
  }

  function applyStats(result: BridgeExecutionTraceStatsResultVO) {
    pieData.value = metricConfig.map((item) => ({
      name: metricName(item.key, item.fallback),
      value: Number(result[item.key] || 0),
      itemStyle: { color: item.color },
    }));
    topRules.value = (result.topRules || [])
      .filter((item) => item.bridgeRuleId !== undefined && item.bridgeRuleId !== null)
      .map((item) => ({
        ruleId: String(item.bridgeRuleId),
        count: Number(item.count || 0),
      }));
  }

  function drawPie() {
    setPieOptions({
      tooltip: { trigger: 'item' },
      legend: { show: false },
      series: [
        {
          name: 'Status',
          type: 'pie',
          radius: ['55%', '80%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: true,
            formatter: (params: any) =>
              Number(params.value) > 0 ? `${params.name}\n${params.percent}%` : '',
            color: '#2a3547',
            fontSize: 12,
          },
          data: pieData.value,
        },
      ],
    });
  }

  function drawLine(timeline: BridgeExecutionTraceStatsTimelinePoint[]) {
    const buckets = timeline.length
      ? timeline
      : [{ timeLabel: '-', success: 0, failed: 0, partial: 0, deadLetter: 0 }];
    const xAxis = buckets.map((b) => b.timeLabel || '-');
    const metrics = metricConfig.map((item) => ({
      ...item,
      name: metricName(item.key, item.fallback),
    }));
    setLineOptions({
      tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
      legend: {
        data: metrics.map((item) => item.name),
        top: 0,
        textStyle: { color: '#6b7280', fontSize: 12 },
      },
      grid: { left: 36, right: 24, top: 36, bottom: 28 },
      xAxis: {
        type: 'category',
        data: xAxis,
        boundaryGap: false,
        axisLabel: { color: '#6b7280', fontSize: 11 },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { color: '#6b7280', fontSize: 11 },
        splitLine: { lineStyle: { color: '#f0f2f5' } },
      },
      series: metrics.map((item) => ({
        name: item.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: buckets.map((b) => Number(b[item.key] || 0)),
        itemStyle: { color: item.color },
        areaStyle: metricAreaColor(item.key) ? { color: metricAreaColor(item.key) } : undefined,
      })),
    });
  }
</script>

<style lang="less" scoped>
  .stats-row {
    margin-bottom: 16px;
  }

  .chart-card {
    border-radius: 10px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      min-height: 44px;

      .ant-card-head-title {
        padding: 12px 0;
      }
    }

    .title {
      font-size: 14px;
      font-weight: 600;
      color: #2a3547;
    }
  }

  .pie-canvas {
    width: 100%;
    height: 220px;
  }

  .pie-legend {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .legend-row {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .legend-name {
        flex: 1;
        color: #6b7280;
      }

      .legend-val {
        color: #2a3547;
        font-weight: 600;
      }
    }
  }

  .line-canvas {
    width: 100%;
    height: 240px;
  }

  .top-rules {
    .rank-row {
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;

      .rank-no {
        width: 26px;
        height: 26px;
        border-radius: 50%;
        background: #f6f8fb;
        color: #6b7280;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 12px;
        flex-shrink: 0;

        &.podium {
          background: linear-gradient(135deg, #ffae1f, #ffd56b);
          color: #fff;
        }
      }

      .rank-name {
        font-size: 13px;
        color: #2a3547;
        font-weight: 500;
        max-width: 240px;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
        flex-shrink: 0;
      }

      .rank-bar-wrap {
        flex: 1;
        height: 8px;
        background: #f6f8fb;
        border-radius: 4px;
        overflow: hidden;

        .rank-bar {
          height: 100%;
          background: linear-gradient(90deg, #5d87ff, #49beff);
          border-radius: 4px;
          transition: width 0.4s ease;
        }
      }

      .rank-count {
        color: #2a3547;
        font-weight: 700;
        min-width: 36px;
        text-align: right;
      }
    }
  }
</style>
