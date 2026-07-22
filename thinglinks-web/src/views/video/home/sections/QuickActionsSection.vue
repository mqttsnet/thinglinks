<template>
  <section class="video-quick">
    <div class="video-section-header">
      <div class="video-section-title">
        <span class="video-section-title__bar" />
        <span>{{ t('video.home.quickActions') }}</span>
      </div>
    </div>
    <a-row :gutter="[16, 16]">
      <a-col v-for="tile in tiles" :key="tile.key" :xs="12" :sm="12" :md="8" :lg="6" :xl="6">
        <div class="video-quick-tile" @click="goto(tile.path)">
          <div class="video-quick-tile__icon" :style="{ background: tile.iconBg, color: tile.iconColor }">
            <Icon :icon="tile.icon" :size="22" />
          </div>
          <div class="video-quick-tile__body">
            <div class="video-quick-tile__title">{{ t(tile.titleKey) }}</div>
            <div class="video-quick-tile__desc">{{ t(tile.descKey) }}</div>
          </div>
          <Icon icon="ant-design:arrow-right-outlined" :size="13" class="video-quick-tile__arrow" />
        </div>
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';

  const router = useRouter();
  const { t } = useI18n();

  const tiles = [
    {
      key: 'device',
      titleKey: 'video.home.quick.device.title',
      descKey: 'video.home.quick.device.desc',
      icon: 'ant-design:video-camera-outlined',
      iconBg: 'rgba(19,222,185,0.12)',
      iconColor: '#0bb783',
      path: '/device/videoDevice',
    },
    {
      key: 'live',
      titleKey: 'video.home.quick.live.title',
      descKey: 'video.home.quick.live.desc',
      icon: 'ant-design:play-circle-outlined',
      iconBg: 'rgba(93,135,255,0.12)',
      iconColor: '#5d87ff',
      path: '/device/videoLive',
    },
    {
      key: 'playback',
      titleKey: 'video.home.quick.playback.title',
      descKey: 'video.home.quick.playback.desc',
      icon: 'ant-design:history-outlined',
      iconBg: 'rgba(124,92,252,0.12)',
      iconColor: '#7c5cfc',
      path: '/record/playback',
    },
    {
      key: 'alarm',
      titleKey: 'video.home.quick.alarm.title',
      descKey: 'video.home.quick.alarm.desc',
      icon: 'ant-design:alert-outlined',
      iconBg: 'rgba(250,137,107,0.12)',
      iconColor: '#d04a2d',
      path: '/device/videoDeviceAlarm',
    },
    {
      key: 'map',
      titleKey: 'video.home.quick.map.title',
      descKey: 'video.home.quick.map.desc',
      icon: 'ant-design:environment-outlined',
      iconBg: 'rgba(73,190,255,0.12)',
      iconColor: '#1a93e5',
      path: '/dashboard/map',
    },
    {
      key: 'stats',
      titleKey: 'video.home.quick.stats.title',
      descKey: 'video.home.quick.stats.desc',
      icon: 'ant-design:bar-chart-outlined',
      iconBg: 'rgba(255,174,31,0.12)',
      iconColor: '#c98a10',
      path: '/dashboard/stats',
    },
    {
      key: 'mediaServer',
      titleKey: 'video.home.quick.mediaServer.title',
      descKey: 'video.home.quick.mediaServer.desc',
      icon: 'ant-design:cloud-server-outlined',
      iconBg: 'rgba(93,135,255,0.12)',
      iconColor: '#5d87ff',
      path: '/media/mediaServer',
    },
    {
      key: 'sip',
      titleKey: 'video.home.quick.sip.title',
      descKey: 'video.home.quick.sip.desc',
      icon: 'ant-design:api-outlined',
      iconBg: 'rgba(124,92,252,0.12)',
      iconColor: '#7c5cfc',
      path: '/sip/sipConfig',
    },
  ];

  function goto(path: string) {
    router.push(path);
  }
</script>

<style lang="less" scoped>
  .video-quick {
    margin-bottom: 20px;
  }

  .video-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 14px;
  }

  .video-section-title {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
  }

  .video-section-title__bar {
    display: none;
  }

  .video-quick-tile {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 18px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
    cursor: pointer;
    transition: transform 0.25s ease, box-shadow 0.25s ease;
    min-height: 72px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 6px rgba(20, 37, 66, 0.04), 0 12px 36px rgba(93, 135, 255, 0.1);

      .video-quick-tile__arrow {
        color: #5d87ff;
      }
    }
  }

  .video-quick-tile__icon {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .video-quick-tile__body {
    flex: 1;
    min-width: 0;
  }

  .video-quick-tile__title {
    font-size: 13px;
    font-weight: 600;
    color: #2a3547;
    line-height: 1.3;
  }

  .video-quick-tile__desc {
    font-size: 11px;
    color: #8c97a5;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .video-quick-tile__arrow {
    color: #b0bac6;
    transition: color 0.2s;
    flex-shrink: 0;
  }
</style>
