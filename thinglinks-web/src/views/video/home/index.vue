<template>
  <PageWrapper class="video-home" dense>
    <MonitoringHeroSection :overview="overview" />
    <QuickActionsSection />
    <LiveStreamsSection :streams="streams" :loading="loading" />
  </PageWrapper>
</template>

<script lang="ts">
  export default {
    name: 'VideoHome',
  };
</script>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import MonitoringHeroSection from './sections/MonitoringHeroSection.vue';
  import QuickActionsSection from './sections/QuickActionsSection.vue';
  import LiveStreamsSection from './sections/LiveStreamsSection.vue';
  import { useVideoOverview } from './hooks/useVideoOverview';

  const { overview, streams, loading, load } = useVideoOverview();

  onMounted(load);
</script>

<style lang="less" scoped>
  .video-home {
    :deep(.ant-pro-page-container-main) {
      padding: 20px 24px;
      background: #f5f7fa;
      min-height: 100%;
    }
  }
</style>
