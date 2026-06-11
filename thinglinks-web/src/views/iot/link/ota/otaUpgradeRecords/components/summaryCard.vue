<template>
  <div class="card-list">
    <div class="card" v-for="(value, key) in data" :key="key">
      <img :src="getImageSrc(key)" alt="图片加载失败..." />
      <div class="info">
        <div class="num" :style="{ color: dataMap[key].color }">{{ value }}</div>
        <div class="label">{{ dataMap[key].label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const dataMap = {
    totalCount: { label: t('iot.link.ota.otaUpgradeRecords.total'), color: 'rgb(255, 187, 84)' },
    pendingUpgradeCount: {
      label: t('iot.link.ota.otaUpgradeRecords.statusPending'),
      color: 'rgba(0, 52, 255, 0.3)',
    },
    upgradingCount: {
      label: t('iot.link.ota.otaUpgradeRecords.statusUpgrading'),
      color: 'rgb(0, 97, 255)',
    },
    upgradeSuccessCount: {
      label: t('iot.link.ota.otaUpgradeRecords.statusSuccess'),
      color: 'rgb(52, 168, 83)',
    },
    upgradeFailureCount: { label: t('iot.link.ota.otaUpgradeRecords.statusFailed'), color: 'red' },
  };

  defineProps({ data: { type: Object, default: () => ({}) } });
  const getImageSrc = (key) => {
    return new URL(`../../../../../../assets/images/iot/link/${key}.png`, import.meta.url).href;
  };
</script>
<style scoped lang="less">
  .card-list {
    margin: 16px 0 0;
    display: flex;
    align-items: center;
    justify-content: space-around;
    gap: 24px;

    .card {
      flex: 1;
      background-color: #fff;
      min-width: 200px;
      min-height: 130px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: space-around;
      border: 1px solid #e8e8e8;

      img {
        width: 100px;
        transition: all linear 0.3s;

        &:hover {
          transform: scale(1.1);
          transition: all ease-in-out 0.3s;
        }
      }

      .info {
        display: flex;
        flex-direction: column;
        align-items: center;

        .num {
          font-size: 26px;
          font-weight: 600;
        }

        .label {
          font-weight: 600;
        }
      }
    }
  }
</style>
