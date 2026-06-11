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
  import { page as pageTrace } from '/@/api/iot/rule/integration/bridgeExecutionTrace';

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

  const [registerModal] = useModalInner(async () => {
    // 弹窗打开时拉数据
    await reload();
  });

  async function reload() {
    loading.value = true;
    try {
      const since = dateUtil()
        .subtract(hoursRange.value, 'hour')
        .format('YYYY-MM-DD HH:mm:ss');
      const res: any = await pageTrace({
        size: 1000,
        current: 1,
        model: { startTimeBegin: since },
      } as any);
      const records: any[] = res?.records ?? [];
      computePie(records);
      computeTopRules(records);
      await nextTick();
      drawPie();
      drawLine(records);
    } finally {
      loading.value = false;
    }
  }

  function computePie(records: any[]) {
    const success = records.filter((r) => r.status === '00').length;
    const failed = records.filter((r) => r.status === '01').length;
    const dlq = records.filter((r) => r.status === '03').length;
    pieData.value = [
      {
        name: t('iot.rule.integration.log.metric.success'),
        value: success,
        itemStyle: { color: '#13deb9' },
      },
      {
        name: t('iot.rule.integration.log.metric.failed'),
        value: failed,
        itemStyle: { color: '#fa4b4b' },
      },
      {
        name: t('iot.rule.integration.log.metric.deadLetter'),
        value: dlq,
        itemStyle: { color: '#f74ba1' },
      },
    ];
  }

  function computeTopRules(records: any[]) {
    const map: Record<string, number> = {};
    records.forEach((r) => {
      const key = r.echoMap?.bridgeRuleId ?? r.bridgeRuleId ?? '-';
      if (!key || key === '-') return;
      map[key] = (map[key] ?? 0) + 1;
    });
    topRules.value = Object.entries(map)
      .map(([ruleId, count]) => ({ ruleId, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 8);
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
            formatter: '{b}\n{d}%',
            color: '#2a3547',
            fontSize: 12,
          },
          data: pieData.value,
        },
      ],
    });
  }

  function drawLine(records: any[]) {
    const slot: Record<string, any> = {};
    const now = dateUtil().startOf('hour');
    for (let i = hoursRange.value - 1; i >= 0; i--) {
      const h = now.subtract(i, 'hour').format('MM-DD HH:00');
      slot[h] = { hour: h, ok: 0, fail: 0, dlq: 0 };
    }
    records.forEach((r) => {
      if (!r.startTime) return;
      const key = dateUtil(r.startTime).format('MM-DD HH:00');
      if (!slot[key]) return;
      if (r.status === '00') slot[key].ok += 1;
      else if (r.status === '01') slot[key].fail += 1;
      else if (r.status === '03') slot[key].dlq += 1;
    });
    const buckets = Object.values(slot);
    const xAxis = buckets.map((b: any) => b.hour);
    setLineOptions({
      tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
      legend: {
        data: [
          t('iot.rule.integration.log.metric.success'),
          t('iot.rule.integration.log.metric.failed'),
          t('iot.rule.integration.log.metric.deadLetter'),
        ],
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
        axisLabel: { color: '#6b7280', fontSize: 11 },
        splitLine: { lineStyle: { color: '#f0f2f5' } },
      },
      series: [
        {
          name: t('iot.rule.integration.log.metric.success'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.map((b: any) => b.ok),
          itemStyle: { color: '#13deb9' },
          areaStyle: { color: 'rgba(19, 222, 185, 0.18)' },
        },
        {
          name: t('iot.rule.integration.log.metric.failed'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.map((b: any) => b.fail),
          itemStyle: { color: '#fa4b4b' },
          areaStyle: { color: 'rgba(250, 75, 75, 0.18)' },
        },
        {
          name: t('iot.rule.integration.log.metric.deadLetter'),
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: buckets.map((b: any) => b.dlq),
          itemStyle: { color: '#f74ba1' },
        },
      ],
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
