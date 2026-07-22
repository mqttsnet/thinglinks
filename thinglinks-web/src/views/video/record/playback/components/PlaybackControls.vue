<template>
  <div class="playback-controls">
    <div class="controls-left">
      <!-- Seek backward -->
      <a-tooltip :title="t('video.record.playback.seekBackward')">
        <a-button
          type="text"
          size="small"
          class="ctrl-btn"
          @click="$emit('seekBackward')"
          :disabled="!canControl"
        >
          <BackwardOutlined />
        </a-button>
      </a-tooltip>

      <!-- Play / Pause -->
      <a-tooltip :title="playing ? t('video.record.playback.pause') : t('video.record.playback.play')">
        <a-button
          type="text"
          size="small"
          class="ctrl-btn play-btn"
          @click="$emit(playing ? 'pause' : 'play')"
          :disabled="!canControl"
        >
          <PauseCircleFilled v-if="playing" />
          <PlayCircleFilled v-else />
        </a-button>
      </a-tooltip>

      <!-- Seek forward -->
      <a-tooltip :title="t('video.record.playback.seekForward')">
        <a-button
          type="text"
          size="small"
          class="ctrl-btn"
          @click="$emit('seekForward')"
          :disabled="!canControl"
        >
          <ForwardOutlined />
        </a-button>
      </a-tooltip>

      <!-- Stop -->
      <a-tooltip :title="t('video.record.playback.stop')">
        <a-button
          type="text"
          size="small"
          class="ctrl-btn"
          @click="$emit('stop')"
          :disabled="!canControl"
        >
          <StopOutlined style="color: #fa896b" />
        </a-button>
      </a-tooltip>

      <!-- Current Time -->
      <div class="time-display" v-if="canControl">
        <ClockCircleOutlined />
        <span>{{ formatTime(currentTime) }}</span>
      </div>
    </div>

    <div class="controls-right">
      <!-- Speed -->
      <div class="speed-control">
        <span class="speed-label">{{ t('video.record.playback.speed') }}</span>
        <a-dropdown :trigger="['click']">
          <a-button size="small" class="speed-btn">
            {{ currentSpeed }}x
            <DownOutlined style="font-size: 10px; margin-left: 2px" />
          </a-button>
          <template #overlay>
            <a-menu @click="handleSpeedChange">
              <a-menu-item v-for="s in speedOptions" :key="s" :class="{ active: currentSpeed === s }">
                {{ s }}x
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>

      <!-- Screenshot -->
      <a-tooltip :title="t('video.record.playback.screenshot')">
        <a-button type="text" size="small" class="ctrl-btn" @click="$emit('screenshot')" :disabled="!canControl">
          <CameraOutlined />
        </a-button>
      </a-tooltip>

      <!-- Download -->
      <a-tooltip :title="t('video.record.playback.download')">
        <a-button type="text" size="small" class="ctrl-btn" @click="$emit('download')" :disabled="!canDownload">
          <DownloadOutlined />
        </a-button>
      </a-tooltip>

      <!-- Fullscreen -->
      <a-tooltip :title="t('video.record.playback.fullscreen')">
        <a-button type="text" size="small" class="ctrl-btn" @click="$emit('fullscreen')" :disabled="!canControl">
          <ExpandOutlined />
        </a-button>
      </a-tooltip>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '/@/hooks/web/useI18n';
  import {
    PlayCircleFilled,
    PauseCircleFilled,
    BackwardOutlined,
    ForwardOutlined,
    StopOutlined,
    ClockCircleOutlined,
    CameraOutlined,
    DownloadOutlined,
    ExpandOutlined,
    DownOutlined,
  } from '@ant-design/icons-vue';

  const props = withDefaults(
    defineProps<{
      playing?: boolean;
      currentSpeed?: number;
      currentTime?: number;
      canControl?: boolean;
      canDownload?: boolean;
    }>(),
    {
      playing: false,
      currentSpeed: 1,
      currentTime: 0,
      canControl: false,
      canDownload: false,
    },
  );

  const emit = defineEmits<{
    (e: 'play'): void;
    (e: 'pause'): void;
    (e: 'stop'): void;
    (e: 'seekForward'): void;
    (e: 'seekBackward'): void;
    (e: 'speedChange', speed: number): void;
    (e: 'screenshot'): void;
    (e: 'download'): void;
    (e: 'fullscreen'): void;
  }>();

  const { t } = useI18n();
  const speedOptions = [0.25, 0.5, 1, 2, 4];

  function handleSpeedChange({ key }: { key: string }) {
    emit('speedChange', Number(key));
  }

  function formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
</script>

<style lang="less" scoped>
  .playback-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #f8fafc;
    border-radius: 10px;
    border: 1px solid #e8ecf1;
  }

  .controls-left,
  .controls-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .ctrl-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    font-size: 16px;
    color: #5a6a85;
    transition: all 0.2s;

    &:hover:not(:disabled) {
      background: #e8ecf1;
      color: #5d87ff;
    }

    &.play-btn {
      font-size: 22px;
      width: 40px;
      height: 40px;
      color: #5d87ff;

      &:hover:not(:disabled) {
        color: #4570e6;
      }
    }
  }

  .time-display {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: 8px;
    padding: 4px 10px;
    background: #fff;
    border: 1px solid #e8ecf1;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #2a3547;
    font-family: 'SF Mono', 'Fira Code', monospace;

    .anticon {
      font-size: 12px;
      color: #8c97a5;
    }
  }

  .speed-control {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-right: 4px;

    .speed-label {
      font-size: 12px;
      color: #8c97a5;
    }

    .speed-btn {
      min-width: 50px;
      font-size: 12px;
      font-weight: 600;
      color: #5d87ff;
      border: 1px solid #d4dfff;
      border-radius: 6px;
      background: #f0f4ff;

      &:hover {
        background: #e0e9ff;
      }
    }
  }

  :deep(.ant-menu-item.active) {
    color: #5d87ff;
    font-weight: 600;
    background: #f0f4ff;
  }
</style>
