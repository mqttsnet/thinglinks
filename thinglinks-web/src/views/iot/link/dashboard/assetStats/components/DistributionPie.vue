<template>
  <!--
    通用分布环形卡(Flexy 风格)。
    用于「设备类型分布」「设备状态分布」等占比类可视化:
      - 标题栏:色条 + 标题 + 右侧总数
      - 中部:甜甜圈环形(中心显示总数),hover 高亮分项
      - 底部:图例列表(色点 + 名称 + 数值 + 占比)
    数据全为空时显示空态。
  -->
  <div class="dist-card">
    <div class="dist-title">
      <span class="title-bar" :style="{ background: barColor }" />
      <span class="title-text">{{ title }}</span>
      <span class="title-total">{{ total }}</span>
    </div>

    <template v-if="total > 0">
      <dataLineChart :options="pieOption" :height="chartHeight" />
      <div class="dist-legend">
        <div v-for="(item, idx) in items" :key="item.name" class="legend-row">
          <span class="dot" :style="{ background: palette[idx % palette.length] }" />
          <span class="name">{{ item.name }}</span>
          <span class="val">{{ item.value }}</span>
          <span class="pct">{{ percent(item.value) }}</span>
        </div>
      </div>
    </template>

    <div v-else class="dist-empty">
      <a-empty :image="emptyImage" :description="t('iot.link.dashboard.assetStats.noData')" />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Empty } from 'ant-design-vue';
  import dataLineChart from './dataLineChart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';

  defineOptions({ name: 'DistributionPie' });

  const props = withDefaults(
    defineProps<{
      title: string;
      /** [{ name, value }] */
      items: Array<{ name: string; value: number }>;
      /** 标题色条 + 环形主色板 */
      palette?: string[];
      barColor?: string;
      chartHeight?: string;
    }>(),
    {
      items: () => [],
      palette: () => ['#5d87ff', '#13deb9', '#ffae1f', '#fa896b', '#539bff', '#b6a2de'],
      barColor: '#5d87ff',
      chartHeight: '180px',
    },
  );

  const { t } = useI18n();
  const emptyImage = Empty.PRESENTED_IMAGE_SIMPLE;

  const total = computed(() =>
    (props.items || []).reduce((sum, i) => sum + (Number(i.value) || 0), 0),
  );

  const percent = (v: number) => {
    if (!total.value) return '0%';
    return ((Number(v) || 0) / total.value * 100).toFixed(1) + '%';
  };

  const pieOption = computed(() => ({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    color: props.palette,
    series: [
      {
        type: 'pie',
        radius: ['58%', '82%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: {
          show: true,
          position: 'center',
          formatter: () => `{num|${total.value}}\n{label|${t('iot.link.dashboard.assetStats.total')}}`,
          rich: {
            num: { fontSize: 22, fontWeight: 700, color: '#2a3547' },
            label: { fontSize: 12, color: '#97a1b0', padding: [4, 0, 0, 0] },
          },
        },
        emphasis: {
          label: { show: true },
          scaleSize: 4,
        },
        labelLine: { show: false },
        data: (props.items || []).map((i) => ({ name: i.name, value: Number(i.value) || 0 })),
      },
    ],
  }));
</script>

<style lang="less" scoped>
  .dist-card {
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 16px 18px;
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .dist-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 8px;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
    }
    .title-total {
      margin-left: auto;
      font-size: 18px;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
      color: #2a3547;
    }
  }

  .dist-legend {
    margin-top: 8px;

    .legend-row {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 2px;
      font-size: 13px;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
      }
      .name {
        color: #4a5568;
      }
      .val {
        margin-left: auto;
        font-weight: 600;
        color: #2a3547;
        font-variant-numeric: tabular-nums;
      }
      .pct {
        width: 52px;
        text-align: right;
        color: #97a1b0;
        font-variant-numeric: tabular-nums;
      }
    }
  }

  .dist-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
