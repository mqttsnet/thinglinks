<template>
  <PageWrapper dense contentFullHeight>
    <div class="dashboard-stats">
      <!-- 统计卡片 - Flexy 风格 -->
      <a-row :gutter="16" class="stats-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card hoverable class="stat-card stat-card--green">
            <div class="stat-card__inner">
              <div class="stat-card__icon">
                <VideoCameraOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value">{{ overview.onlineDeviceCount || 0 }}</div>
                <div class="stat-card__label">{{ t('video.dashboard.stats.onlineDevice') }}</div>
              </div>
              <div class="stat-card__extra">
                / {{ totalDeviceCount }}
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card hoverable class="stat-card stat-card--blue">
            <div class="stat-card__inner">
              <div class="stat-card__icon">
                <PlayCircleOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value">{{ overview.totalActiveStreams || 0 }}</div>
                <div class="stat-card__label">{{ t('video.dashboard.stats.activeStreams') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card hoverable class="stat-card stat-card--orange">
            <div class="stat-card__inner">
              <div class="stat-card__icon">
                <AlertOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value">{{ alarmStats.totalCount || 0 }}</div>
                <div class="stat-card__label">{{ t('video.dashboard.stats.totalAlarms') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card hoverable class="stat-card stat-card--red">
            <div class="stat-card__inner">
              <div class="stat-card__icon">
                <ExclamationCircleOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value">{{ alarmStats.unhandledCount || 0 }}</div>
                <div class="stat-card__label">{{ t('video.dashboard.stats.pendingAlarms') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 告警趋势图 + 级别分布 -->
      <a-row :gutter="16" class="chart-row">
        <a-col :xs="24" :lg="16">
          <a-card class="chart-card" :bordered="false">
            <template #title>
              <div class="chart-card__title">
                <span>{{ t('video.dashboard.stats.alarmTrend7d') }}</span>
              </div>
            </template>
            <div ref="alarmChartRef" class="chart-container" />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-card class="chart-card" :bordered="false">
            <template #title>
              <div class="chart-card__title">
                <span>{{ t('video.dashboard.stats.alarmPriorityDistribution') }}</span>
              </div>
            </template>
            <div ref="priorityChartRef" class="chart-container" />
          </a-card>
        </a-col>
      </a-row>

      <!-- 活跃流列表 -->
      <a-card class="stream-card" :bordered="false">
        <template #title>
          <div class="chart-card__title">
            <span>{{ t('video.dashboard.stats.activeStreamList') }}</span>
            <a-tag color="blue">{{ t('video.dashboard.stats.streamCount', { count: streamList.length }) }}</a-tag>
          </div>
        </template>
        <template #extra>
          <a-button size="small" @click="loadStreams">
            <template #icon><ReloadOutlined /></template>
            {{ t('common.redo') }}
          </a-button>
        </template>
        <a-table
          :dataSource="streamList"
          :columns="streamColumns"
          rowKey="callId"
          size="small"
          :pagination="false"
        />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted, nextTick } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import {
    VideoCameraOutlined,
    PlayCircleOutlined,
    AlertOutlined,
    ExclamationCircleOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';
  import {
    getStreamList,
    getStreamOverview,
    getAlarmStatistics,
  } from '/@/api/video/dashboard/dashboard';
  import { useI18n } from '/@/hooks/web/useI18n';
  import * as echarts from 'echarts';

  const { t } = useI18n();

  const overview = reactive<any>({});
  const alarmStats = reactive<any>({});
  const streamList = ref<any[]>([]);

  const alarmChartRef = ref<HTMLElement>();
  const priorityChartRef = ref<HTMLElement>();

  const totalDeviceCount = computed(
    () => (overview.onlineDeviceCount || 0) + (overview.offlineDeviceCount || 0),
  );

  const streamColumns = [
    { title: t('video.dashboard.stats.deviceId'), dataIndex: 'deviceIdentification', width: 180 },
    { title: t('video.dashboard.stats.app'), dataIndex: 'app', width: 100 },
    { title: t('video.dashboard.stats.streamId'), dataIndex: 'stream', width: 160 },
    { title: t('video.dashboard.stats.server'), dataIndex: 'mediaServerHost', width: 140 },
    { title: t('video.dashboard.stats.callId'), dataIndex: 'callId', ellipsis: true },
  ];

  onMounted(async () => {
    await Promise.all([loadOverview(), loadAlarmStats(), loadStreams()]);
    await nextTick();
    renderAlarmChart();
    renderPriorityChart();
  });

  async function loadOverview() {
    try {
      const data = await getStreamOverview();
      Object.assign(overview, data);
    } catch (_) {}
  }

  async function loadAlarmStats() {
    try {
      const data = await getAlarmStatistics();
      Object.assign(alarmStats, data);
    } catch (_) {}
  }

  async function loadStreams() {
    try {
      streamList.value = (await getStreamList()) || [];
    } catch (_) {}
  }

  function renderAlarmChart() {
    if (!alarmChartRef.value) return;
    const chart = echarts.init(alarmChartRef.value);
    const days = (alarmStats.byDay || []).map((d: any) => d.name);
    const counts = (alarmStats.byDay || []).map((d: any) => d.count);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#e0e0e0' } }, axisLabel: { color: '#666' } },
      yAxis: { type: 'value', minInterval: 1, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
      series: [
        {
          data: counts,
          type: 'line',
          smooth: true,
          lineStyle: { width: 3, color: '#5b8ff9' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(91, 143, 249, 0.25)' },
              { offset: 1, color: 'rgba(91, 143, 249, 0.02)' },
            ]),
          },
          itemStyle: { color: '#5b8ff9' },
        },
      ],
      grid: { top: 20, right: 20, bottom: 30, left: 50 },
    });
  }

  function renderPriorityChart() {
    if (!priorityChartRef.value) return;
    const chart = echarts.init(priorityChartRef.value);
    const colorMap: Record<string, string> = { '1': '#ff4d4f', '2': '#faad14', '3': '#1890ff', '4': '#52c41a' };
    const data = (alarmStats.byPriority || []).map((d: any) => ({
      name: `L${d.name}`,
      value: d.count,
      itemStyle: { color: colorMap[d.name] || '#999' },
    }));
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['45%', '72%'],
          data,
          label: { show: true, formatter: '{b}: {c}', fontSize: 12 },
          emphasis: {
            itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.15)' },
          },
        },
      ],
    });
  }
</script>

<style lang="less" scoped>
  .dashboard-stats {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .stat-card {
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    }

    &__inner {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    &__icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;
    }

    &__content {
      flex: 1;
    }

    &__value {
      font-size: 28px;
      font-weight: 600;
      line-height: 1.2;
    }

    &__label {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.45);
      margin-top: 4px;
    }

    &__extra {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.35);
      flex-shrink: 0;
    }

    &--green {
      .stat-card__icon {
        background: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }
      .stat-card__value { color: #52c41a; }
    }

    &--blue {
      .stat-card__icon {
        background: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }
      .stat-card__value { color: #1890ff; }
    }

    &--orange {
      .stat-card__icon {
        background: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }
      .stat-card__value { color: #faad14; }
    }

    &--red {
      .stat-card__icon {
        background: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }
      .stat-card__value { color: #ff4d4f; }
    }
  }

  .chart-card {
    border-radius: 12px;
    height: 100%;

    &__title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }

    .chart-container {
      height: 320px;
    }
  }

  .chart-row {
    .ant-col {
      margin-bottom: 0;
    }
  }

  .stream-card {
    border-radius: 12px;

    .chart-card__title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
  }
</style>
