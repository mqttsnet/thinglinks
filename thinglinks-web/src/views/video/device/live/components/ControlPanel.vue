<template>
  <div class="control-panel">
    <!-- Active Cell Info -->
    <div v-if="activeSource" class="control-panel__header">
      <div class="control-panel__header-icon" :class="`control-panel__header-icon--${activeSource.sourceType}`">
        <Icon :icon="sourceIcon" :size="18" />
      </div>
      <div class="control-panel__header-info">
        <span class="control-panel__header-name">{{ sourceName }}</span>
        <span class="control-panel__header-type">{{ sourceTypeLabel }}</span>
      </div>
    </div>
    <div v-else class="control-panel__empty">
      <Icon icon="ant-design:select-outlined" :size="32" color="#d4dbe4" />
      <span>{{ t('video.device.live.control.selectCell') }}</span>
    </div>

    <!-- Player Type Switch -->
    <div v-if="activeSource" class="control-panel__section">
      <div class="control-panel__section-title">
        <Icon icon="ant-design:play-circle-outlined" :size="14" />
        {{ t('video.device.live.playerType') }}
      </div>
      <a-radio-group
        :value="playerType"
        size="small"
        button-style="solid"
        @change="(e: any) => $emit('update:playerType', e.target.value)"
        class="control-panel__player-switch"
      >
        <a-radio-button value="jessibuca">Jessibuca</a-radio-button>
        <a-radio-button value="flv">FLV</a-radio-button>
        <a-radio-button value="hls">HLS</a-radio-button>
      </a-radio-group>
    </div>

    <!-- PTZ Control (only for device channels with PTZ capability) -->
    <div v-if="showPtz" class="control-panel__section">
      <div class="control-panel__section-title">
        <Icon icon="ant-design:control-outlined" :size="14" />
        {{ t('video.device.live.ptz.title') }}
      </div>
      <div class="control-panel__ptz-grid">
        <a-button size="small" @click="handlePtz('up_left')"><Icon icon="ant-design:arrow-up-outlined" :size="12" style="transform: rotate(-45deg)" /></a-button>
        <a-button size="small" @click="handlePtz('up')"><Icon icon="ant-design:arrow-up-outlined" :size="12" /></a-button>
        <a-button size="small" @click="handlePtz('up_right')"><Icon icon="ant-design:arrow-up-outlined" :size="12" style="transform: rotate(45deg)" /></a-button>
        <a-button size="small" @click="handlePtz('left')"><Icon icon="ant-design:arrow-left-outlined" :size="12" /></a-button>
        <a-button size="small" type="primary" shape="circle" @click="handlePtz('stop')"><Icon icon="ant-design:stop-outlined" :size="12" /></a-button>
        <a-button size="small" @click="handlePtz('right')"><Icon icon="ant-design:arrow-right-outlined" :size="12" /></a-button>
        <a-button size="small" @click="handlePtz('down_left')"><Icon icon="ant-design:arrow-down-outlined" :size="12" style="transform: rotate(45deg)" /></a-button>
        <a-button size="small" @click="handlePtz('down')"><Icon icon="ant-design:arrow-down-outlined" :size="12" /></a-button>
        <a-button size="small" @click="handlePtz('down_right')"><Icon icon="ant-design:arrow-down-outlined" :size="12" style="transform: rotate(-45deg)" /></a-button>
      </div>
      <div class="control-panel__ptz-zoom">
        <a-button size="small" block @click="handlePtz('zoom_in')">
          <Icon icon="ant-design:zoom-in-outlined" :size="12" /> {{ t('video.device.live.ptz.zoomIn') }}
        </a-button>
        <a-button size="small" block @click="handlePtz('zoom_out')">
          <Icon icon="ant-design:zoom-out-outlined" :size="12" /> {{ t('video.device.live.ptz.zoomOut') }}
        </a-button>
      </div>
      <div class="control-panel__ptz-speed">
        <span>{{ t('video.device.live.ptz.speed') }}</span>
        <a-slider v-model:value="ptzSpeed" :min="1" :max="255" :step="1" size="small" />
      </div>
    </div>

    <!-- Device Control (only for device channels) -->
    <div v-if="showDeviceControl" class="control-panel__section">
      <div class="control-panel__section-title">
        <Icon icon="ant-design:setting-outlined" :size="14" />
        {{ t('video.device.live.control.title') }}
      </div>
      <div class="control-panel__device-actions">
        <a-button size="small" block @click="handleDeviceAction('record')">
          <template #icon><Icon icon="ant-design:video-camera-add-outlined" :size="12" /></template>
          {{ t('video.device.live.control.recordStart') }}
        </a-button>
        <a-button size="small" block @click="handleDeviceAction('guard')">
          <template #icon><Icon icon="ant-design:safety-certificate-outlined" :size="12" /></template>
          {{ t('video.device.live.control.guardSet') }}
        </a-button>
        <a-button size="small" block @click="handleDeviceAction('keyframe')">
          <template #icon><Icon icon="ant-design:pic-center-outlined" :size="12" /></template>
          {{ t('video.device.live.control.forceKeyFrame') }}
        </a-button>
      </div>
    </div>

    <!-- Stream URLs -->
    <div v-if="activeSource && streamInfo" class="control-panel__section">
      <div class="control-panel__section-title">
        <Icon icon="ant-design:link-outlined" :size="14" />
        {{ t('video.device.live.streamUrls') }}
      </div>
      <div class="control-panel__urls">
        <template v-for="(urlObj, protocol) in streamUrlMap" :key="protocol">
          <div v-if="urlObj" class="control-panel__url-item">
            <span class="control-panel__url-label">{{ protocol }}</span>
            <a-typography-paragraph
              :copyable="{ text: urlObj }"
              :ellipsis="{ rows: 1 }"
              :content="urlObj"
              class="control-panel__url-value"
            />
          </div>
        </template>
      </div>
    </div>

    <!-- Quick Actions -->
    <div v-if="activeSource" class="control-panel__section">
      <div class="control-panel__section-title">
        <Icon icon="ant-design:thunderbolt-outlined" :size="14" />
        {{ t('video.device.live.control.quickActions') }}
      </div>
      <div class="control-panel__actions">
        <a-button size="small" @click="$emit('snapshot')">
          <template #icon><Icon icon="ant-design:camera-outlined" :size="14" /></template>
          {{ t('video.device.live.snapshot') }}
        </a-button>
        <a-button size="small" @click="$emit('fullscreen')">
          <template #icon><Icon icon="ant-design:fullscreen-outlined" :size="14" /></template>
          {{ t('video.device.live.fullscreen') }}
        </a-button>
        <a-button size="small" danger @click="$emit('close-cell')">
          <template #icon><Icon icon="ant-design:close-outlined" :size="14" /></template>
          {{ t('video.device.live.stop') }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, type PropType } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';
  import { ptzControl } from '/@/api/video/device/ptz';
  import type { StreamSource } from '../types';

  export default defineComponent({
    name: 'ControlPanel',
    components: { Icon },
    props: {
      activeSource: { type: Object as PropType<StreamSource | null>, default: null },
      playerType: { type: String, default: 'jessibuca' },
      streamInfo: { type: Object, default: null },
    },
    emits: ['update:playerType', 'snapshot', 'fullscreen', 'close-cell'],
    setup(props) {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const ptzSpeed = ref(128);

      const sourceIcon = computed(() => {
        switch (props.activeSource?.sourceType) {
          case 'channel':
            return 'ant-design:video-camera-outlined';
          case 'proxy':
            return 'ant-design:cloud-download-outlined';
          case 'push':
            return 'ant-design:cloud-upload-outlined';
          default:
            return 'ant-design:video-camera-outlined';
        }
      });

      const sourceName = computed(() => {
        if (!props.activeSource) return '';
        switch (props.activeSource.sourceType) {
          case 'channel':
            return props.activeSource.channelName || props.activeSource.channelIdentification || '';
          case 'proxy':
            return props.activeSource.proxyName || props.activeSource.streamIdentification || '';
          case 'push':
            return props.activeSource.streamIdentification || props.activeSource.appId || '';
          default:
            return '';
        }
      });

      const sourceTypeLabel = computed(() => {
        switch (props.activeSource?.sourceType) {
          case 'channel':
            return `GB28181 · ${props.activeSource.deviceIdentification || ''}`;
          case 'proxy':
            return t('video.device.live.source.proxy');
          case 'push':
            return t('video.device.live.source.push');
          default:
            return '';
        }
      });

      const showPtz = computed(() => {
        return (
          props.activeSource?.sourceType === 'channel' &&
          props.activeSource?.ptzCapability &&
          props.activeSource?.deviceIdentification &&
          props.activeSource?.channelIdentification
        );
      });

      const showDeviceControl = computed(() => {
        return (
          props.activeSource?.sourceType === 'channel' &&
          props.activeSource?.deviceIdentification &&
          props.activeSource?.channelIdentification
        );
      });

      const streamUrlMap = computed(() => {
        const info = props.streamInfo;
        if (!info) return {};
        const map: Record<string, string> = {};
        if (info.flv?.url) map['HTTP-FLV'] = info.flv.url;
        if (info.wsFlv?.url) map['WS-FLV'] = info.wsFlv.url;
        if (info.hls?.url) map['HLS'] = info.hls.url;
        if (info.rtmp?.url) map['RTMP'] = info.rtmp.url;
        if (info.rtsp?.url) map['RTSP'] = info.rtsp.url;
        if (info.rtc?.url) map['WebRTC'] = info.rtc.url;
        if (info.fmp4?.url) map['FMP4'] = info.fmp4.url;
        // For proxy/push zlmMediaServerStreamInfoList
        if (info.zlmMediaServerStreamInfoList?.length) {
          const first = info.zlmMediaServerStreamInfoList[0];
          if (first.flv) map['HTTP-FLV'] = first.flv;
          if (first.wsFlv) map['WS-FLV'] = first.wsFlv;
          if (first.hls) map['HLS'] = first.hls;
          if (first.rtmp) map['RTMP'] = first.rtmp;
          if (first.rtsp) map['RTSP'] = first.rtsp;
        }
        return map;
      });

      /** PTZ direction map */
      const ptzCommandMap: Record<string, string> = {
        up: 'up', down: 'down', left: 'left', right: 'right',
        up_left: 'upleft', up_right: 'upright',
        down_left: 'downleft', down_right: 'downright',
        zoom_in: 'zoomin', zoom_out: 'zoomout', stop: 'stop',
      };

      async function handlePtz(direction: string) {
        if (!props.activeSource?.deviceIdentification || !props.activeSource?.channelIdentification) return;
        try {
          const command = ptzCommandMap[direction] || direction;
          await ptzControl({
            deviceIdentification: props.activeSource.deviceIdentification,
            channelIdentification: props.activeSource.channelIdentification,
            command,
            speed: ptzSpeed.value,
          });
        } catch (e: any) {
          console.warn('PTZ control error:', e);
        }
      }

      function handleDeviceAction(action: string) {
        createMessage.info(`${action} - ${t('video.device.live.control.title')}`);
      }

      return {
        t,
        ptzSpeed,
        sourceIcon,
        sourceName,
        sourceTypeLabel,
        showPtz,
        showDeviceControl,
        streamUrlMap,
        handlePtz,
        handleDeviceAction,
      };
    },
  });
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
  @purple: #7c5cfc;

  .control-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d4dbe4;
      border-radius: 4px;
    }

    &__empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 40px 20px;
      color: #8c97a5;
      font-size: 13px;
    }

    &__header {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 14px 16px;
      border-bottom: 1px solid #f0f2f5;

      &-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        flex-shrink: 0;

        &--channel {
          background: linear-gradient(135deg, @primary, fade(@primary, 70%));
        }

        &--proxy {
          background: linear-gradient(135deg, @purple, fade(@purple, 70%));
        }

        &--push {
          background: linear-gradient(135deg, @warning, fade(@warning, 70%));
        }
      }

      &-info {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      &-name {
        font-size: 14px;
        font-weight: 600;
        color: #2a3547;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      &-type {
        font-size: 11px;
        color: #8c97a5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    &__section {
      padding: 12px 16px;
      border-bottom: 1px solid #f6f8fb;

      &:last-child {
        border-bottom: none;
      }

      &-title {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        margin-bottom: 10px;
      }
    }

    &__player-switch {
      width: 100%;

      :deep(.ant-radio-button-wrapper) {
        flex: 1;
        text-align: center;
        font-size: 12px;
      }
    }

    &__urls {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    &__url-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &__url-label {
      font-size: 11px;
      font-weight: 600;
      color: @primary;
      background: fade(@primary, 10%);
      padding: 1px 6px;
      border-radius: 4px;
      flex-shrink: 0;
      min-width: 60px;
      text-align: center;
    }

    &__url-value {
      flex: 1;
      min-width: 0;
      margin-bottom: 0 !important;
      font-size: 11px;
    }

    &__ptz-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 4px;
      margin-bottom: 10px;

      .ant-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 32px;
      }
    }

    &__ptz-zoom {
      display: flex;
      gap: 6px;
      margin-bottom: 8px;

      .ant-btn {
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
      }
    }

    &__ptz-speed {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: #5a6a85;

      .ant-slider {
        flex: 1;
      }
    }

    &__device-actions {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .ant-btn {
        font-size: 12px;
        text-align: left;
      }
    }

    &__actions {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .ant-btn {
        font-size: 12px;
      }
    }
  }
</style>
