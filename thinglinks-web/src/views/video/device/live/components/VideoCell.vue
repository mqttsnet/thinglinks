<template>
  <div
    class="video-cell"
    :class="{
      'video-cell--active': active,
      'video-cell--drag-over': isDragOver,
      'video-cell--playing': status === 'playing',
      'video-cell--error': status === 'error',
    }"
    :draggable="!!source"
    @click="$emit('select')"
    @dragstart="onDragStartCell"
    @dragenter.prevent="onDragEnter"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop.prevent="onDrop"
  >
    <!-- Empty State -->
    <div v-if="!source" class="video-cell__empty">
      <div class="video-cell__empty-icon">
        <Icon icon="ant-design:video-camera-outlined" :size="cellIconSize" />
      </div>
      <span class="video-cell__empty-text">{{ t('video.device.live.grid.dragHere') }}</span>
      <span class="video-cell__empty-index">{{ cellIndex + 1 }}</span>
    </div>

    <!-- Loading State -->
    <div v-else-if="status === 'loading'" class="video-cell__loading">
      <a-spin :tip="t('video.device.live.connecting')" />
    </div>

    <!-- Error State -->
    <div v-else-if="status === 'error'" class="video-cell__error">
      <Icon icon="ant-design:warning-outlined" :size="28" color="#fa896b" />
      <span class="video-cell__error-text">{{ errorMsg || t('video.device.live.playFailed') }}</span>
      <a-button size="small" type="link" @click.stop="$emit('retry')">
        {{ t('video.device.live.retry') }}
      </a-button>
    </div>

    <!-- Player -->
    <div v-else class="video-cell__player">
      <VideoPlayer
        ref="playerRef"
        :url="playUrl"
        :playerType="playerType"
        :hasAudio="source?.hasAudio ?? true"
        width="100%"
        height="100%"
        fill-mode="cover"
        @error="onPlayerError"
      />
    </div>

    <!-- Overlay: 只覆盖顶部，不遮挡 Jessibuca 底部控制栏 -->
    <div v-if="source" class="video-cell__overlay">
      <span class="video-cell__source-name">{{ sourceName }}</span>
      <span class="video-cell__source-tag">{{ sourceTag }}</span>
      <span style="flex:1"></span>
      <a-tooltip :title="t('video.device.live.streamUrls')">
        <span class="video-cell__action-btn" @click.stop="$emit('show-urls')">
          <Icon icon="ant-design:link-outlined" :size="14" />
        </span>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.stop')">
        <span class="video-cell__action-btn video-cell__action-btn--danger" @click.stop="$emit('close')">
          <Icon icon="ant-design:close-outlined" :size="14" />
        </span>
      </a-tooltip>
    </div>

    <!-- Drag Over Indicator -->
    <div v-if="isDragOver" class="video-cell__drop-indicator">
      <Icon icon="ant-design:plus-outlined" :size="32" />
      <span>{{ t('video.device.live.grid.dragHere') }}</span>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, type PropType } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { VideoPlayer } from '/@/components/video/player';
  import { Icon } from '/@/components/Icon';
  import type { PlayerType } from '/@/components/video/player/types';
  import type { StreamSource, CellStatus } from '../types';

  export default defineComponent({
    name: 'VideoCell',
    components: { VideoPlayer, Icon },
    props: {
      cellIndex: { type: Number, required: true },
      source: { type: Object as PropType<StreamSource | null>, default: null },
      active: { type: Boolean, default: false },
      playerType: { type: String as () => PlayerType, default: 'jessibuca' },
      playUrl: { type: String, default: '' },
      status: { type: String as () => CellStatus, default: 'empty' },
      errorMsg: { type: String, default: '' },
      layout: { type: Number, default: 4 },
    },
    emits: ['select', 'close', 'drop', 'swap', 'retry', 'show-urls', 'error'],
    setup(props, { emit, expose }) {
      const { t } = useI18n();
      const playerRef = ref<any>(null);
      // Use counter to fix dragleave firing on child elements
      const dragCounter = ref(0);
      const isDragOver = computed(() => dragCounter.value > 0);

      const cellIconSize = computed(() => {
        if (props.layout <= 1) return 48;
        if (props.layout <= 4) return 36;
        return 24;
      });

      const sourceName = computed(() => {
        if (!props.source) return '';
        switch (props.source.sourceType) {
          case 'channel':
            return props.source.channelName || props.source.channelIdentification || '';
          case 'proxy':
            return props.source.proxyName || props.source.streamIdentification || '';
          case 'push':
            return props.source.streamIdentification || props.source.appId || '';
          default:
            return '';
        }
      });

      const sourceTag = computed(() => {
        if (!props.source) return '';
        switch (props.source.sourceType) {
          case 'channel':
            return 'GB';
          case 'proxy':
            return t('video.device.live.source.proxy');
          case 'push':
            return t('video.device.live.source.push');
          default:
            return '';
        }
      });

      // 宫格自身可拖拽（用于交换位置）
      function onDragStartCell(e: DragEvent) {
        if (!props.source || !e.dataTransfer) return;
        e.dataTransfer.effectAllowed = 'copyMove';
        // 用 text/plain 存宫格索引（浏览器对自定义 MIME 限制多）
        // 加前缀 "cell:" 区分设备树拖入的数据
        e.dataTransfer.setData('text/plain', `cell:${props.cellIndex}`);
      }

      function onDragEnter(e: DragEvent) {
        dragCounter.value++;
      }

      function onDragOver(e: DragEvent) {
        e.preventDefault();
      }

      function onDragLeave() {
        dragCounter.value--;
      }

      function onDrop(e: DragEvent) {
        dragCounter.value = 0;
        try {
          // 优先检查 application/json（设备树拖入的流源）
          const jsonData = e.dataTransfer?.getData('application/json');
          if (jsonData) {
            const source: StreamSource = JSON.parse(jsonData);
            emit('drop', source);
            return;
          }
          // 检查 text/plain 是否是宫格间拖拽（格式 "cell:索引"）
          const textData = e.dataTransfer?.getData('text/plain') || '';
          if (textData.startsWith('cell:')) {
            const fromIndex = Number(textData.substring(5));
            if (!isNaN(fromIndex) && fromIndex !== props.cellIndex) {
              emit('swap', fromIndex);
            }
          }
        } catch (err) {
          console.warn('Drop parse error:', err);
        }
      }

      function handleSnapshot() {
        playerRef.value?.screenshot?.(`snapshot_${Date.now()}`);
      }

      function handleFullscreen() {
        playerRef.value?.fullscreen?.();
      }

      function onPlayerError(err: any) {
        console.warn('VideoCell player error:', err);
        emit('error', err);
      }

      // 暴露给父组件调用
      expose({ snapshot: handleSnapshot });

      return {
        t,
        playerRef,
        isDragOver,
        cellIconSize,
        sourceName,
        onDragStartCell,
        sourceTag,
        onDragEnter,
        onDragOver,
        onDragLeave,
        onDrop,
        handleSnapshot,
        handleFullscreen,
        onPlayerError,
      };
    },
  });
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @danger: #fa896b;
  @border-color: #e5eaef;

  .video-cell {
    position: relative;
    background: #0a0e17;
    border: 2px solid transparent;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.25s ease;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: fade(@primary, 40%);

      .video-cell__overlay {
        opacity: 1;
        pointer-events: auto;
      }
    }

    &--active {
      border-color: @primary;
      box-shadow: 0 0 0 2px fade(@primary, 20%);
    }

    &--drag-over {
      border-color: @success !important;
      background: fade(@success, 6%);
      box-shadow: 0 0 0 3px fade(@success, 20%);
    }

    &--error {
      border-color: fade(@danger, 40%);
    }

    &__empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      width: 100%;
      height: 100%;
      position: relative;
      pointer-events: none;

      &-icon {
        color: #3a4255;
        opacity: 0.6;
      }

      &-text {
        color: #5a6a85;
        font-size: 12px;
        opacity: 0.8;
      }

      &-index {
        position: absolute;
        top: 8px;
        left: 10px;
        color: #3a4255;
        font-size: 12px;
        font-weight: 600;
      }
    }

    &__loading {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: #fff;
      pointer-events: none;

      :deep(.ant-spin) {
        color: #fff;

        .ant-spin-text {
          color: rgba(255, 255, 255, 0.65);
        }
      }
    }

    &__error {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      width: 100%;
      height: 100%;

      &-text {
        color: rgba(255, 255, 255, 0.65);
        font-size: 12px;
      }
    }

    &__player {
      width: 100%;
      height: 100%;

      :deep(.video-player-wrapper) {
        height: 100%;

        > div {
          height: 100% !important;

          // 仅约束 <video> 标签（FLV/HLS 播放器），
          // 不覆盖 canvas — Jessibuca 用 transform+position 自行管理 canvas 缩放布局
          video {
            width: 100% !important;
            height: 100% !important;
            object-fit: contain;
          }
        }
      }
    }

    &__overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 8px;
      background: linear-gradient(180deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: none;
      z-index: 5;
    }

    &__source-name {
      color: #fff;
      font-size: 12px;
      font-weight: 500;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__source-tag {
      color: @primary;
      background: rgba(93, 135, 255, 0.2);
      font-size: 10px;
      padding: 1px 6px;
      border-radius: 10px;
      flex-shrink: 0;
    }

    &__action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
      }

      &--danger:hover {
        background: fade(@danger, 60%);
      }
    }

    &__drop-indicator {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: fade(@success, 10%);
      color: @success;
      font-size: 13px;
      z-index: 10;
      border-radius: 8px;
      pointer-events: none;
    }
  }
</style>
