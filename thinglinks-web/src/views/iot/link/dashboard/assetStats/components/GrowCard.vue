<template>
  <!--
    资产统计顶部指标区(Flexy 风格)。
    第一段:4 张 KPI 卡 ── 产品总量 / 设备总量 / 设备在线率(环形) / 近3日消息量(迷你趋势)
    第二段:建模与版本治理 ── 已发布 / 未发布 / 灰度中 / 近7天发布 / 物模型服务数 / 发布版本总量
    数据来源:assetSummary(deviceOverview + productOverview) + topologySummary(上下行) + versionStatistics(建模治理)
  -->
  <div class="grow-wrap">
    <!-- ===== 第一段:KPI 卡 ===== -->
    <a-row :gutter="[16, 16]">
      <!-- 1. 产品总量 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="kpi-card">
          <div class="kpi-icon prod"><CommonProductSvg /></div>
          <div class="kpi-body">
            <div class="kpi-label">{{ t('iot.link.dashboard.assetStats.productCount') }}</div>
            <div class="kpi-value">
              {{ product.productsTotalCount || 0 }}
              <span class="kpi-unit">{{ t('iot.link.dashboard.assetStats.unit') }}</span>
            </div>
            <div class="kpi-growth">
              <RiseOutlined />
              {{ t('iot.link.dashboard.assetStats.todayNew') }} <b>+{{ product.todayNewCount || 0 }}</b>
              <span class="sep">·</span>
              {{ t('iot.link.dashboard.assetStats.weekNew') }} <b>+{{ product.weekNewCount || 0 }}</b>
            </div>
            <div class="kpi-sub">
              <span>{{ t('iot.link.dashboard.assetStats.gatewayProducts') }}
                <b>{{ product.gatewayProductsCount || 0 }}</b></span>
              <span>{{ t('iot.link.dashboard.assetStats.ordinaryProduct') }}
                <b>{{ product.ordinaryProductsCount || 0 }}</b></span>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 2. 设备总量 -->
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="kpi-card">
          <div class="kpi-icon dev"><ClusterOutlined /></div>
          <div class="kpi-body">
            <div class="kpi-label">{{ t('iot.link.dashboard.assetStats.deviceCount') }}</div>
            <div class="kpi-value">
              {{ device.totalDevicesCount || 0 }}
              <span class="kpi-unit">{{ t('iot.link.dashboard.assetStats.taiwan') }}</span>
            </div>
            <div class="kpi-growth">
              <RiseOutlined />
              {{ t('iot.link.dashboard.assetStats.todayNew') }} <b>+{{ device.todayNewCount || 0 }}</b>
              <span class="sep">·</span>
              {{ t('iot.link.dashboard.assetStats.weekNew') }} <b>+{{ device.weekNewCount || 0 }}</b>
            </div>
            <div class="kpi-sub">
              <span>{{ t('iot.link.dashboard.assetStats.ordinaryDevices') }}
                <b>{{ device.ordinaryCount || 0 }}</b></span>
              <span>{{ t('iot.link.dashboard.assetStats.gatewayDevices') }}
                <b>{{ device.gatewayDeviceCount || 0 }}</b></span>
              <span>{{ t('iot.link.dashboard.assetStats.gatewaySubDevices') }}
                <b>{{ device.subDeviceCount || 0 }}</b></span>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 3. 设备在线率(环形进度) -->
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="kpi-card">
          <a-progress
            type="circle"
            :percent="onlineRate"
            :width="72"
            :stroke-color="rateColor(onlineRate)"
            :stroke-width="9"
            :format="onlineRateFormat"
          />
          <div class="kpi-body">
            <div class="kpi-label">{{ t('iot.link.dashboard.assetStats.onlineRate') }}</div>
            <div class="kpi-sub vert">
              <span><i class="d on" />{{ t('iot.link.dashboard.assetStats.online') }}
                <b>{{ device.onlineCount || 0 }}</b></span>
              <span><i class="d off" />{{ t('iot.link.dashboard.assetStats.offline') }}
                <b>{{ device.offlineCount || 0 }}</b></span>
              <span><i class="d nc" />{{ t('iot.link.dashboard.assetStats.notConnected') }}
                <b>{{ device.notConnectedCount || 0 }}</b></span>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 4. 近3日消息量(迷你趋势) -->
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="kpi-card">
          <div class="kpi-icon msg"><SwapOutlined /></div>
          <div class="kpi-body">
            <div class="kpi-label">{{ t('iot.link.dashboard.assetStats.recent3DaysMessage') }}</div>
            <div class="kpi-value">
              {{ totalMessage }}
              <span class="kpi-unit">{{ t('iot.link.dashboard.assetStats.article') }}</span>
            </div>
            <div class="kpi-sub">
              <span>{{ t('iot.link.dashboard.assetStats.upLink') }}
                <b>{{ topology.totalUplinkData || 0 }}</b></span>
              <span>{{ t('iot.link.dashboard.assetStats.downLink') }}
                <b>{{ topology.totalDownlinkData || 0 }}</b></span>
            </div>
          </div>
          <div v-if="miniTrend" class="kpi-mini"><dataLineChart :options="miniTrend" height="46px" /></div>
        </div>
      </a-col>
    </a-row>

    <!-- ===== 第二段:建模与版本治理 ===== -->
    <div class="sub-title">{{ t('iot.link.dashboard.assetStats.modelGovernance') }}</div>
    <a-row :gutter="[16, 16]" class="sub-row">
      <a-col v-for="m in subMetrics" :key="m.key" :xs="12" :sm="8" :lg="4">
        <div class="sub-card">
          <div class="sub-top">
            <span class="sub-dot" :style="{ background: m.color }" />
            <span class="sub-label">{{ m.label }}</span>
          </div>
          <div class="sub-value">{{ m.value }}</div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, toRefs } from 'vue';
  import { Row, Col, Progress } from 'ant-design-vue';
  import { ClusterOutlined, SwapOutlined, RiseOutlined } from '@ant-design/icons-vue';
  import dataLineChart from './dataLineChart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { CommonProductSvg } from '/@/components/iot/svg';
  import { topologySummary } from '/@/api/iot/link/dashboard/dashboard';
  import { versionStatistics } from '/@/api/iot/link/productVersion/productVersion';

  export default defineComponent({
    name: 'GrowCard',
    components: {
      [Row.name]: Row,
      [Col.name]: Col,
      [Progress.name]: Progress,
      ClusterOutlined,
      SwapOutlined,
      RiseOutlined,
      CommonProductSvg,
      dataLineChart,
    },
    props: {
      /** 设备概况(由父 index 统一加载 assetSummary 后传入,避免重复请求) */
      device: { type: Object, default: () => ({}) },
      /** 产品概况 */
      product: { type: Object, default: () => ({}) },
    },
    setup(props) {
      const { t } = useI18n();

      const state = reactive<any>({
        topology: {},
        miniTrend: null,
        // 建模与版本治理统计(来源 productVersion/statistics,后端 safeCount 兜底,缺数据返 0 不报错)
        versionStat: {},
      });

      /** device / product 来自 props(响应式);topology 本组件自管 */
      const device = computed<any>(() => props.device || {});
      const product = computed<any>(() => props.product || {});

      /** 比率:0 分母安全 */
      const rate = (num?: number, deno?: number) => {
        const d = Number(deno) || 0;
        if (!d) return 0;
        return Math.round(((Number(num) || 0) / d) * 100);
      };
      const onlineRate = computed(() =>
        rate(device.value.onlineCount, device.value.totalDevicesCount),
      );
      const totalMessage = computed(
        () => (Number(state.topology.totalUplinkData) || 0) + (Number(state.topology.totalDownlinkData) || 0),
      );

      /** 健康度配色:>=80 绿 / >=50 橙 / <50 红 */
      const rateColor = (v: number) => (v >= 80 ? '#13deb9' : v >= 50 ? '#ffae1f' : '#fa896b');

      /** 环形中心文案:antd-vue 3.x Progress format 是 prop(函数),返回百分比文本 */
      const onlineRateFormat = (p?: number) => `${p ?? 0}%`;

      /** 建模与版本治理(6 指标,字段与后端 ProductVersionStatisticsResultVO 一一对齐) */
      const subMetrics = computed(() => {
        const v: any = state.versionStat || {};
        return [
          { key: 'publishedProduct', label: t('iot.link.dashboard.assetStats.publishedProduct'), value: v.publishedProductCount || 0, color: '#13deb9' },
          { key: 'unpublishedProduct', label: t('iot.link.dashboard.assetStats.unpublishedProduct'), value: v.unpublishedProductCount || 0, color: '#8c97a5' },
          { key: 'canaryProduct', label: t('iot.link.dashboard.assetStats.canaryProduct'), value: v.canaryProductCount || 0, color: '#ffae1f' },
          { key: 'recentPublish7d', label: t('iot.link.dashboard.assetStats.recentPublish7d'), value: v.recentPublishCount7d || 0, color: '#5d87ff' },
          { key: 'thingModelService', label: t('iot.link.dashboard.assetStats.thingModelService'), value: v.thingModelServiceCount || 0, color: '#722ed1' },
          { key: 'publishedVersionTotal', label: t('iot.link.dashboard.assetStats.publishedVersionTotal'), value: v.publishedVersionTotal || 0, color: '#13c2c2' },
        ];
      });

      const buildMiniTrend = (uplink: any[], downlink: any[]) => {
        const area = (color: string) => ({
          color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color }, { offset: 1, color: '#ffffff' }] },
        });
        state.miniTrend = {
          tooltip: { trigger: 'axis', axisPointer: { type: 'line' } },
          grid: { top: 2, bottom: 2, left: 0, right: 0 },
          xAxis: { type: 'category', show: false, boundaryGap: false, data: uplink.map((i) => i.timeString) },
          yAxis: { type: 'value', show: false },
          series: [
            { name: t('iot.link.dashboard.assetStats.upLink'), type: 'line', smooth: true, symbol: 'none', color: '#ffae1f', areaStyle: area('#ffd591'), data: uplink.map((i) => i.value) },
            { name: t('iot.link.dashboard.assetStats.downLink'), type: 'line', smooth: true, symbol: 'none', color: '#539bff', areaStyle: area('#adc6ff'), data: downlink.map((i) => i.value) },
          ],
        };
      };

      const loadTopology = async () => {
        try {
          const res: any = await topologySummary();
          state.topology = res || {};
          const up = res?.dashboardDetailsResultVo?.uplinkDetails || [];
          const down = res?.dashboardDetailsResultVo?.downlinkDetails || [];
          if (up.length) buildMiniTrend(up, down);
        } catch (e) {
          console.warn('[assetStats] topology 加载失败', e);
        }
      };

      // 建模与版本治理统计(后端已 safeCount 兜底;前端再兜一层,异常时保持空对象 → 各项显示 0)
      const loadVersionStat = async () => {
        try {
          const res: any = await versionStatistics();
          state.versionStat = res || {};
        } catch (e) {
          console.warn('[assetStats] versionStatistics 加载失败', e);
        }
      };

      onMounted(() => {
        loadTopology();
        loadVersionStat();
      });

      return {
        t,
        ...toRefs(state),
        device,
        product,
        onlineRate,
        onlineRateFormat,
        totalMessage,
        rateColor,
        subMetrics,
      };
    },
  });
</script>

<style lang="less" scoped>
  .grow-wrap {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  /* ===== KPI 卡 ===== */
  .kpi-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 14px;
    height: 100%;
    min-height: 116px;
    padding: 18px;
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    overflow: hidden;
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 6px 20px rgba(45, 53, 71, 0.08);
    }

    .kpi-icon {
      width: 52px;
      height: 52px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24px;
      flex-shrink: 0;
      padding: 8px;

      :deep(svg) {
        width: 100%;
        height: 100%;
      }

      &.prod { background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%); box-shadow: 0 6px 16px rgba(93, 135, 255, 0.32); }
      &.dev { background: linear-gradient(135deg, #13deb9 0%, #20c997 100%); box-shadow: 0 6px 16px rgba(19, 222, 185, 0.32); }
      &.msg { background: linear-gradient(135deg, #ffae1f 0%, #f29b55 100%); box-shadow: 0 6px 16px rgba(255, 174, 31, 0.32); }
    }

    .kpi-body {
      flex: 1;
      min-width: 0;
    }

    .kpi-label {
      font-size: 13px;
      color: #97a1b0;
    }

    .kpi-value {
      margin-top: 2px;
      font-size: 28px;
      font-weight: 700;
      color: #2a3547;
      line-height: 1.2;
      font-variant-numeric: tabular-nums;
      word-break: break-all;

      .kpi-unit {
        font-size: 13px;
        font-weight: 400;
        color: #97a1b0;
        margin-left: 2px;
      }
    }

    /* 增长 chip:绿色凸显「今日/本周新增」,资产增长一眼看 */
    .kpi-growth {
      margin-top: 6px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #0c8a6f;

      .anticon {
        font-size: 12px;
      }
      b {
        font-weight: 700;
        font-variant-numeric: tabular-nums;
      }
      .sep {
        color: #c8cfdb;
        margin: 0 2px;
      }
    }

    .kpi-sub {
      margin-top: 8px;
      display: flex;
      flex-wrap: wrap;
      gap: 4px 14px;
      font-size: 12px;
      color: #8c97a5;

      &.vert {
        flex-direction: column;
        gap: 3px;
      }

      b {
        color: #2a3547;
        font-weight: 600;
        margin-left: 2px;
        font-variant-numeric: tabular-nums;
      }

      .d {
        display: inline-block;
        width: 7px;
        height: 7px;
        border-radius: 50%;
        margin-right: 5px;
        vertical-align: middle;
        &.on { background: #13deb9; }
        &.off { background: #fa896b; }
        &.nc { background: #c8cfdb; }
      }
    }

    .ring-num {
      font-size: 15px;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
    }

    /* 消息卡迷你趋势:沉底铺满 */
    .kpi-mini {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      height: 46px;
      opacity: 0.85;
      pointer-events: none;
    }
  }

  /* ===== 6 细分指标 ===== */
  .sub-title {
    margin: 4px 0 -4px;
    font-size: 14px;
    font-weight: 600;
    color: #1f2329;
  }

  .sub-row {
    margin-top: 0;
  }

  .sub-card {
    height: 100%;
    padding: 14px 16px;
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 4px 14px rgba(45, 53, 71, 0.06);
    }

    .sub-top {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #8c97a5;

      .sub-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
      }
    }

    .sub-value {
      margin-top: 6px;
      font-size: 22px;
      font-weight: 700;
      color: #2a3547;
      font-variant-numeric: tabular-nums;
    }
  }
</style>
