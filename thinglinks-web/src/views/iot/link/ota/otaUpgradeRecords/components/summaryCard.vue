<template>
  <div class="summary-list">
    <div v-for="item in items" :key="item.key" class="summary-card" :class="`is-${item.status}`">
      <div class="summary-orb">
        <OtaRecordStatusBadge :status="item.status" :size="48" />
      </div>
      <div class="summary-info">
        <div class="summary-num" :style="{ color: item.color }">{{ data[item.key] ?? 0 }}</div>
        <div class="summary-label">{{ item.label }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { OtaRecordStatusBadge } from '/@/components/iot/ota/svg';

  defineOptions({ name: 'OtaRecordsSummaryCard' });

  const props = defineProps<{ data?: Record<string, number> }>();
  const data = computed(() => props.data || {});

  const { t } = useI18n();

  // 汇总指标:键对应后端统计字段,status 驱动水晶球配色与字形,与卡片列表 / 详情页语义一致
  const items = computed(() => [
    {
      key: 'totalCount',
      status: 'total',
      label: t('iot.link.ota.otaUpgradeRecords.total'),
      color: '#5d87ff',
    },
    {
      key: 'pendingUpgradeCount',
      status: 'pending',
      label: t('iot.link.ota.otaUpgradeRecords.statusPending'),
      color: '#d48806',
    },
    {
      key: 'upgradingCount',
      status: 'upgrading',
      label: t('iot.link.ota.otaUpgradeRecords.statusUpgrading'),
      color: '#0a8f8f',
    },
    {
      key: 'upgradeSuccessCount',
      status: 'success',
      label: t('iot.link.ota.otaUpgradeRecords.statusSuccess'),
      color: '#0c8a6f',
    },
    {
      key: 'upgradeFailureCount',
      status: 'failed',
      label: t('iot.link.ota.otaUpgradeRecords.statusFailed'),
      color: '#e03131',
    },
  ]);
</script>

<style scoped lang="less">
  .summary-list {
    margin: 16px 0 0;
    display: flex;
    align-items: stretch;
    gap: 16px;
    flex-wrap: wrap;
  }

  .summary-card {
    flex: 1;
    min-width: 190px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 18px;
    background: linear-gradient(180deg, #ffffff 0%, #fafbff 100%);
    border: 1px solid #eef1f7;
    border-radius: 14px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
    transition: transform 0.18s ease, box-shadow 0.18s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
    }

    /* 不同状态左侧细色条,扫一眼即可区分 */
    &.is-total {
      border-left: 3px solid #5d87ff;
    }
    &.is-pending {
      border-left: 3px solid #ffae1f;
    }
    &.is-upgrading {
      border-left: 3px solid #13c2c2;
    }
    &.is-success {
      border-left: 3px solid #13deb9;
    }
    &.is-failed {
      border-left: 3px solid #fa5252;
    }
  }

  .summary-orb {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .summary-info {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .summary-num {
    font-size: 28px;
    font-weight: 700;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
  }

  .summary-label {
    margin-top: 2px;
    font-size: 13px;
    font-weight: 500;
    color: #6b7280;
  }

  @media (max-width: 768px) {
    .summary-card {
      min-width: calc(50% - 8px);
    }
  }
</style>
