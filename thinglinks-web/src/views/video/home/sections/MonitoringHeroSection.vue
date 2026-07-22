<template>
  <section class="video-hero">
    <div class="video-hero__top">
      <div class="video-hero__brand">
        <Icon icon="ant-design:video-camera-outlined" :size="20" class="video-hero__brand-icon" />
        <div class="video-hero__brand-body">
          <div class="video-hero__brand-title">
            {{ t('video.home.title') }}
            <span class="video-hero__brand-version">v{{ version }}</span>
          </div>
          <div class="video-hero__brand-slogan">{{ t('video.home.slogan') }}</div>
        </div>
      </div>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('video.dashboard.stats.onlineDevice')"
          :value="overview.onlineDevices"
          :sub="overview.totalDevices ? `/ ${overview.totalDevices}` : ''"
          icon="ant-design:video-camera-outlined"
          icon-color="#13deb9"
          icon-bg="#e6f9f4"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('video.dashboard.stats.pendingAlarms')"
          :value="overview.pendingAlarms"
          icon="ant-design:alert-outlined"
          icon-color="#fa896b"
          icon-bg="#ffeae3"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('video.dashboard.stats.activeStreams')"
          :value="overview.activeStreams"
          :sub="overview.maxStreams ? `/ ${overview.maxStreams}` : ''"
          icon="ant-design:play-circle-outlined"
          icon-color="#5d87ff"
          icon-bg="#ecf2ff"
        />
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <MetricCard
          :label="t('video.home.mediaNodes')"
          :value="overview.mediaNodesOnline"
          :sub="overview.mediaNodes ? `/ ${overview.mediaNodes}` : ''"
          icon="ant-design:cloud-server-outlined"
          icon-color="#7c5cfc"
          icon-bg="#f3eeff"
        />
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import MetricCard from '/@/views/thinglinks/home/components/MetricCard.vue';
  import type { VideoOverview } from '../hooks/useVideoOverview';

  interface Props {
    overview: VideoOverview;
    version?: string;
  }
  withDefaults(defineProps<Props>(), { version: '1.0' });

  const { t } = useI18n();
</script>

<style lang="less" scoped>
  .video-hero {
    margin-bottom: 16px;
  }

  .video-hero__top {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }

  .video-hero__brand {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .video-hero__brand-icon {
    color: #5d87ff;
  }

  .video-hero__brand-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
  }

  .video-hero__brand-version {
    font-size: 11px;
    font-weight: 500;
    padding: 1px 6px;
    border-radius: 4px;
    background: #f0f4fa;
    color: #8c97a5;
  }

  .video-hero__brand-slogan {
    font-size: 12px;
    color: #8c97a5;
    margin-top: 2px;
  }
</style>
