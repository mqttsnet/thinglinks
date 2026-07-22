<template>
  <div v-if="result" class="srp">
    <!-- 全量 -->
    <template v-if="strategy === 0">
      <div class="srp-line">
        <span class="srp-num">{{ affected }}</span>
        <span class="srp-sub">{{ t('iot.link.product.publish.result.fullSwitched') }}</span>
      </div>
      <div class="srp-bar"
        ><span class="srp-fill" :style="{ width: pct + '%', background: '#378ADD' }"></span
      ></div>
      <div class="srp-cap">
        <span>{{ t('iot.link.product.publish.result.ofTotalLabel') }}</span>
        <span>{{ affected }} / {{ total }} · {{ pct }}%</span>
      </div>
    </template>

    <!-- 灰度 -->
    <template v-else-if="strategy === 1">
      <div class="srp-src"><BranchesOutlined />{{ canarySourceText }}</div>
      <div class="srp-line">
        <span class="srp-num">{{ affected }}</span>
        <span class="srp-sub">{{
          t('iot.link.product.publish.result.canaryHit', {
            target: result.canary?.targetCount ?? affected,
          })
        }}</span>
      </div>
      <div class="srp-bar"
        ><span class="srp-fill" :style="{ width: pct + '%', background: '#1D9E75' }"></span
      ></div>
      <div class="srp-cap">
        <span>{{ t('iot.link.product.publish.result.ofTotalLabel') }}</span>
        <span>{{ affected }} / {{ total }} · {{ pct }}%</span>
      </div>
      <details v-if="(result.canary?.groups?.length ?? 0) > 0" class="srp-groups">
        <summary>{{
          t('iot.link.product.publish.result.expandGroups', {
            n: result.canary?.groups?.length ?? 0,
          })
        }}</summary>
        <div v-for="g in result.canary?.groups" :key="g.groupId" class="srp-grow">
          <span class="srp-gname" :title="g.groupName">{{ g.groupName }}</span>
          <span class="srp-gbar"
            ><span class="srp-gfill" :style="{ width: groupPct(g.deviceCount) + '%' }"></span
          ></span>
          <span class="srp-gnum">{{ g.deviceCount ?? 0 }} · {{ groupPct(g.deviceCount) }}%</span>
        </div>
      </details>
    </template>

    <!-- 影子 -->
    <template v-else-if="strategy === 2">
      <div class="srp-line">
        <span class="srp-num">{{ result.shadow?.preBuiltStableCount ?? 0 }}</span>
        <span class="srp-sub">{{ t('iot.link.product.publish.result.shadowPreBuilt') }}</span>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { BranchesOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import type { StrategyResultDTO } from '/@/api/iot/link/productPublishRecord/model/productPublishRecordModel';

  const props = defineProps<{ result?: StrategyResultDTO }>();
  const { t } = useI18n();

  const strategy = computed(() => props.result?.strategy);
  const affected = computed(() => props.result?.affectedDeviceCount ?? 0);
  const total = computed(() => props.result?.productTotalAtPublish ?? 0);
  const pct = computed(() =>
    total.value > 0 ? Math.round((affected.value / total.value) * 1000) / 10 : 0,
  );

  function groupPct(count?: number): number {
    return total.value > 0 ? Math.round(((count ?? 0) / total.value) * 1000) / 10 : 0;
  }

  const canarySourceText = computed(() => {
    const c = props.result?.canary;
    if (!c) return '';
    if (c.source === 'group') {
      return t('iot.link.product.publish.result.sourceGroup', { n: c.groups?.length ?? 0 });
    }
    if (c.source === 'manual') return t('iot.link.product.publish.result.sourceManual');
    if (c.source === 'percent') {
      return t('iot.link.product.publish.result.sourcePercent', { percent: c.percent ?? 0 });
    }
    return '';
  });
</script>

<style lang="less" scoped>
  .srp {
    margin-top: 8px;
  }
  .srp-src {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: var(--color-text-secondary);
    margin-bottom: 8px;
  }
  .srp-line {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 6px;
  }
  .srp-num {
    font-size: 18px;
    font-weight: 500;
    line-height: 1;
    color: var(--color-text-primary);
  }
  .srp-sub {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
  .srp-bar {
    height: 7px;
    border-radius: 999px;
    background: var(--color-border-tertiary);
    overflow: hidden;
    margin: 4px 0;
  }
  .srp-fill {
    display: block;
    height: 100%;
    border-radius: 999px;
  }
  .srp-cap {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--color-text-secondary);
  }
  .srp-groups {
    margin-top: 10px;

    summary {
      font-size: 12px;
      color: var(--color-text-info);
      cursor: pointer;
    }

    .srp-grow {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 8px;
    }
    .srp-gname {
      font-size: 13px;
      min-width: 88px;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .srp-gbar {
      flex: 1;
      height: 6px;
      border-radius: 999px;
      background: var(--color-border-tertiary);
      overflow: hidden;
    }
    .srp-gfill {
      display: block;
      height: 100%;
      border-radius: 999px;
      background: #5dcaa5;
    }
    .srp-gnum {
      font-size: 12px;
      color: var(--color-text-secondary);
      min-width: 92px;
      text-align: right;
      font-variant-numeric: tabular-nums;
    }
  }
</style>
