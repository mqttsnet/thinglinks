<template>
  <section class="home-platform">
    <div class="home-section-header">
      <div class="home-section-title">
        <span class="home-section-title__bar" />
        <span>{{ t('workbench.platform.title') }}</span>
      </div>
    </div>
    <a-row :gutter="[16, 16]">
      <a-col v-for="tile in PLATFORM_TILES" :key="tile.key" :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <PlatformTile :item="tile" />
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { useI18n } from '/@/hooks/web/useI18n';
  import PlatformTile from '../components/PlatformTile.vue';
  import { PLATFORM_TILES } from '../data';

  const { t } = useI18n();
</script>

<style lang="less" scoped>
  .home-platform {
    margin-bottom: 20px;
  }

  .home-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 14px;
  }

  .home-section-title {
    display: inline-flex;
    align-items: baseline;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .home-section-title__bar {
    display: none;
  }
</style>
