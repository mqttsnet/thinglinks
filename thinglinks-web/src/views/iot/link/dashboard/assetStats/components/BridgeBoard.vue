<template>
  <!--
    北向集成 · 数据桥接 看板(Flexy 风格)。
    上:资源概览 4 小格(数据源 / 桥接规则 / 订阅源 / 今日执行,均含启用或细分)
    下:今日执行健康 ── 成功率环形(中心 %)+ 成功/失败/部分/死信 图例
    数据源:rule 的 /ruleDashboardStats/bridgeSummary(纯 rule 模块统计,无跨服务调用)
  -->
  <div class="bridge-board">
    <div class="bb-title">
      <span class="title-bar" />
      {{ t('iot.link.dashboard.assetStats.bridgeBoard') }}
    </div>

    <!-- 资源概览 4 小格 -->
    <div class="bb-grid">
      <div class="bb-cell">
        <div class="bb-cell-label">{{ t('iot.link.dashboard.assetStats.dataSource') }}</div>
        <div class="bb-cell-value">{{ summary.dataSourceTotal || 0 }}</div>
        <div class="bb-cell-sub">
          {{ t('iot.link.dashboard.assetStats.enabled') }} <b>{{ summary.dataSourceEnabled || 0 }}</b>
        </div>
      </div>
      <div class="bb-cell">
        <div class="bb-cell-label">{{ t('iot.link.dashboard.assetStats.bridgeRule') }}</div>
        <div class="bb-cell-value">{{ summary.bridgeRuleTotal || 0 }}</div>
        <div class="bb-cell-sub">
          {{ t('iot.link.dashboard.assetStats.outbound') }} <b>{{ summary.bridgeRuleOutbound || 0 }}</b>
          <span class="sep">·</span>
          {{ t('iot.link.dashboard.assetStats.inbound') }} <b>{{ summary.bridgeRuleInbound || 0 }}</b>
        </div>
      </div>
      <div class="bb-cell">
        <div class="bb-cell-label">{{ t('iot.link.dashboard.assetStats.subscriptionSource') }}</div>
        <div class="bb-cell-value">{{ summary.subscriptionTotal || 0 }}</div>
        <div class="bb-cell-sub">
          {{ t('iot.link.dashboard.assetStats.enabled') }} <b>{{ summary.subscriptionEnabled || 0 }}</b>
        </div>
      </div>
      <div class="bb-cell">
        <div class="bb-cell-label">{{ t('iot.link.dashboard.assetStats.todayExec') }}</div>
        <div class="bb-cell-value">{{ summary.todayExecTotal || 0 }}</div>
        <div class="bb-cell-sub">
          <span :class="['hc', healthClass]">●</span>
          {{ t('iot.link.dashboard.assetStats.dataSourceHealthy') }} <b>{{ summary.dataSourceHealthy || 0 }}</b>
        </div>
      </div>
    </div>

    <!-- 今日执行健康:成功率环形 + 图例 -->
    <div class="bb-health">
      <div class="bb-ring">
        <a-progress
          type="circle"
          :percent="successRate"
          :width="96"
          :stroke-color="rateColor(successRate)"
          :stroke-width="8"
          :format="successRateFormat"
        />
        <div class="ring-cap">{{ t('iot.link.dashboard.assetStats.todaySuccessRate') }}</div>
      </div>
      <div class="bb-legend">
        <div class="lg-row">
          <span class="dot" style="background: #13deb9" />
          <span class="name">{{ t('iot.link.dashboard.assetStats.execSuccess') }}</span>
          <span class="val">{{ summary.todaySuccess || 0 }}</span>
        </div>
        <div class="lg-row">
          <span class="dot" style="background: #ffae1f" />
          <span class="name">{{ t('iot.link.dashboard.assetStats.execPartial') }}</span>
          <span class="val">{{ summary.todayPartial || 0 }}</span>
        </div>
        <div class="lg-row">
          <span class="dot" style="background: #fa896b" />
          <span class="name">{{ t('iot.link.dashboard.assetStats.execFailed') }}</span>
          <span class="val">{{ summary.todayFailed || 0 }}</span>
        </div>
        <div class="lg-row">
          <span class="dot" style="background: #97a1b0" />
          <span class="name">{{ t('iot.link.dashboard.assetStats.execDeadLetter') }}</span>
          <span class="val">{{ summary.todayDeadLetter || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, toRefs } from 'vue';
  import { Progress } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { bridgeSummary } from '/@/api/iot/rule/dashboard/dashboard';

  export default defineComponent({
    name: 'BridgeBoard',
    components: {
      [Progress.name]: Progress,
    },
    setup() {
      const { t } = useI18n();
      const state = reactive<{ summary: Record<string, number> }>({ summary: {} });

      /** 今日成功率 = 成功 / 执行总数;无执行时 0 */
      const successRate = computed(() => {
        const total = Number(state.summary.todayExecTotal) || 0;
        if (!total) return 0;
        return Math.round(((Number(state.summary.todaySuccess) || 0) / total) * 100);
      });

      const rateColor = (v: number) => (v >= 90 ? '#13deb9' : v >= 70 ? '#ffae1f' : '#fa896b');

      /** 环形中心文案:antd-vue 3.x Progress format 是 prop(函数),返回百分比文本 */
      const successRateFormat = (p?: number) => `${p ?? 0}%`;

      /** 数据源健康标识色:有异常→红,全健康→绿,无数据源→灰 */
      const healthClass = computed(() => {
        if ((Number(state.summary.dataSourceAbnormal) || 0) > 0) return 'bad';
        if ((Number(state.summary.dataSourceTotal) || 0) > 0) return 'good';
        return 'idle';
      });

      const load = async () => {
        try {
          const res: any = await bridgeSummary();
          state.summary = res || {};
        } catch (e) {
          console.warn('[assetStats] bridgeSummary 加载失败', e);
        }
      };

      onMounted(load);

      return { t, ...toRefs(state), successRate, successRateFormat, rateColor, healthClass };
    },
  });
</script>

<style lang="less" scoped>
  .bridge-board {
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 16px 18px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .bb-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 14px;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
      background: #5d87ff;
    }
  }

  /* 资源概览 2×2 */
  .bb-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 16px;

    .bb-cell {
      padding: 12px 14px;
      background: #f7f9ff;
      border: 1px solid #eef1f7;
      border-radius: 10px;

      .bb-cell-label {
        font-size: 12px;
        color: #97a1b0;
      }
      .bb-cell-value {
        margin-top: 4px;
        font-size: 22px;
        font-weight: 700;
        color: #2a3547;
        font-variant-numeric: tabular-nums;
        line-height: 1.2;
      }
      .bb-cell-sub {
        margin-top: 4px;
        font-size: 12px;
        color: #8c97a5;

        b {
          color: #2a3547;
          font-weight: 600;
          font-variant-numeric: tabular-nums;
        }
        .sep {
          color: #c8cfdb;
          margin: 0 3px;
        }
        .hc {
          font-size: 10px;
          margin-right: 2px;
          &.good { color: #13deb9; }
          &.bad { color: #fa896b; }
          &.idle { color: #c8cfdb; }
        }
      }
    }
  }

  /* 今日执行健康 */
  .bb-health {
    display: flex;
    align-items: center;
    gap: 18px;
    padding-top: 14px;
    border-top: 1px dashed #eef1f7;
    flex: 1;

    .bb-ring {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;

      .ring-cap {
        font-size: 11px;
        color: #97a1b0;
      }
    }

    .bb-legend {
      flex: 1;

      .lg-row {
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
      }
    }
  }
</style>
