<template>
  <div class="ptz-control">
    <div class="ptz-section">
      <div class="ptz-title">{{ t('video.device.live.ptz.title') }}</div>

      <!-- Direction Pad -->
      <div class="ptz-direction-pad">
        <div class="ptz-row">
          <a-button
            size="small"
            class="ptz-btn corner"
            @mousedown="startMove('LEFT_UP')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('LEFT_UP')"
            @touchend.prevent="stopMove"
          >
            <ArrowUpOutlined class="rotate-icon rotate-315" />
          </a-button>
          <a-button
            size="small"
            class="ptz-btn"
            @mousedown="startMove('UP')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('UP')"
            @touchend.prevent="stopMove"
          >
            <ArrowUpOutlined />
          </a-button>
          <a-button
            size="small"
            class="ptz-btn corner"
            @mousedown="startMove('RIGHT_UP')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('RIGHT_UP')"
            @touchend.prevent="stopMove"
          >
            <ArrowUpOutlined class="rotate-icon rotate-45" />
          </a-button>
        </div>
        <div class="ptz-row">
          <a-button
            size="small"
            class="ptz-btn"
            @mousedown="startMove('LEFT')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('LEFT')"
            @touchend.prevent="stopMove"
          >
            <ArrowLeftOutlined />
          </a-button>
          <a-button size="small" class="ptz-btn center" type="primary" danger @click="handleStop">
            <BorderOutlined />
          </a-button>
          <a-button
            size="small"
            class="ptz-btn"
            @mousedown="startMove('RIGHT')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('RIGHT')"
            @touchend.prevent="stopMove"
          >
            <ArrowRightOutlined />
          </a-button>
        </div>
        <div class="ptz-row">
          <a-button
            size="small"
            class="ptz-btn corner"
            @mousedown="startMove('LEFT_DOWN')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('LEFT_DOWN')"
            @touchend.prevent="stopMove"
          >
            <ArrowDownOutlined class="rotate-icon rotate-45" />
          </a-button>
          <a-button
            size="small"
            class="ptz-btn"
            @mousedown="startMove('DOWN')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('DOWN')"
            @touchend.prevent="stopMove"
          >
            <ArrowDownOutlined />
          </a-button>
          <a-button
            size="small"
            class="ptz-btn corner"
            @mousedown="startMove('RIGHT_DOWN')"
            @mouseup="stopMove"
            @mouseleave="stopMove"
            @touchstart.prevent="startMove('RIGHT_DOWN')"
            @touchend.prevent="stopMove"
          >
            <ArrowDownOutlined class="rotate-icon rotate-315" />
          </a-button>
        </div>
      </div>

      <!-- Zoom -->
      <div class="ptz-zoom">
        <a-button
          size="small"
          @mousedown="startZoom(2)"
          @mouseup="stopMove"
          @mouseleave="stopMove"
          @touchstart.prevent="startZoom(2)"
          @touchend.prevent="stopMove"
        >
          <ZoomInOutlined /> {{ t('video.device.live.ptz.zoomIn') }}
        </a-button>
        <a-button
          size="small"
          @mousedown="startZoom(1)"
          @mouseup="stopMove"
          @mouseleave="stopMove"
          @touchstart.prevent="startZoom(1)"
          @touchend.prevent="stopMove"
        >
          <ZoomOutOutlined /> {{ t('video.device.live.ptz.zoomOut') }}
        </a-button>
      </div>

      <!-- Speed Slider -->
      <div class="ptz-speed">
        <span>{{ t('video.device.live.ptz.speed') }}:</span>
        <a-slider v-model:value="moveSpeed" :min="1" :max="255" :step="1" style="flex: 1; margin: 0 8px;" />
        <span>{{ moveSpeed }}</span>
      </div>

      <!-- Presets -->
      <a-divider style="margin: 8px 0;" />
      <div class="ptz-presets">
        <div class="ptz-preset-row">
          <span>{{ t('video.device.live.ptz.presetId') }}:</span>
          <a-input-number v-model:value="presetId" :min="1" :max="255" size="small" style="width: 80px; margin: 0 4px;" />
        </div>
        <div class="ptz-preset-actions">
          <a-button size="small" type="primary" @click="handlePresetSet">
            {{ t('video.device.live.ptz.presetSet') }}
          </a-button>
          <a-button size="small" @click="handlePresetCall">
            {{ t('video.device.live.ptz.presetCall') }}
          </a-button>
          <a-button size="small" danger @click="handlePresetDelete">
            {{ t('video.device.live.ptz.presetDelete') }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    ArrowUpOutlined,
    ArrowDownOutlined,
    ArrowLeftOutlined,
    ArrowRightOutlined,
    BorderOutlined,
    ZoomInOutlined,
    ZoomOutOutlined,
  } from '@ant-design/icons-vue';
  import { ptzDirection, ptzStop, presetSet, presetCall, presetDelete } from '/@/api/video/device/ptz';

  export default defineComponent({
    name: 'PtzControl',
    components: {
      ArrowUpOutlined,
      ArrowDownOutlined,
      ArrowLeftOutlined,
      ArrowRightOutlined,
      BorderOutlined,
      ZoomInOutlined,
      ZoomOutOutlined,
    },
    props: {
      deviceIdentification: { type: String, required: true },
      channelIdentification: { type: String, required: true },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const moveSpeed = ref(128);
      const presetId = ref(1);
      // 防抖：避免 mouseup 和 mouseleave 同时触发两次 stop
      let stopTimer: ReturnType<typeof setTimeout> | null = null;

      async function startMove(direction: string) {
        // 取消可能存在的 stop 定时器
        if (stopTimer) {
          clearTimeout(stopTimer);
          stopTimer = null;
        }
        try {
          await ptzDirection({
            deviceIdentification: props.deviceIdentification,
            channelIdentification: props.channelIdentification,
            command: 'DIRECTION',
            direction,
            moveSpeed: moveSpeed.value,
          });
        } catch (e: any) {
          createMessage.error(e?.message || 'PTZ control failed');
        }
      }

      async function startZoom(zoomDirection: number) {
        if (stopTimer) {
          clearTimeout(stopTimer);
          stopTimer = null;
        }
        try {
          await ptzDirection({
            deviceIdentification: props.deviceIdentification,
            channelIdentification: props.channelIdentification,
            command: 'ZOOM',
            zoomDirection,
            zoomSpeed: moveSpeed.value,
          });
        } catch (e: any) {
          createMessage.error(e?.message || 'Zoom failed');
        }
      }

      function stopMove() {
        // 用防抖避免 mouseup + mouseleave 短时间内双重触发
        if (stopTimer) return;
        stopTimer = setTimeout(async () => {
          stopTimer = null;
          try {
            await ptzStop(props.deviceIdentification, props.channelIdentification);
          } catch (_e) {
            // Silent fail for stop
          }
        }, 50);
      }

      async function handleStop() {
        if (stopTimer) {
          clearTimeout(stopTimer);
          stopTimer = null;
        }
        try {
          await ptzStop(props.deviceIdentification, props.channelIdentification);
        } catch (_e) {
          // Silent
        }
      }

      async function handlePresetSet() {
        try {
          await presetSet({
            deviceIdentification: props.deviceIdentification,
            channelIdentification: props.channelIdentification,
            command: 'PRESET_SET',
            presetId: presetId.value,
          });
          createMessage.success(t('video.device.live.ptz.presetSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.ptz.presetFailed'));
        }
      }

      async function handlePresetCall() {
        try {
          await presetCall({
            deviceIdentification: props.deviceIdentification,
            channelIdentification: props.channelIdentification,
            command: 'PRESET_CALL',
            presetId: presetId.value,
          });
          createMessage.success(t('video.device.live.ptz.presetSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.ptz.presetFailed'));
        }
      }

      async function handlePresetDelete() {
        try {
          await presetDelete({
            deviceIdentification: props.deviceIdentification,
            channelIdentification: props.channelIdentification,
            command: 'PRESET_DELETE',
            presetId: presetId.value,
          });
          createMessage.success(t('video.device.live.ptz.presetSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.ptz.presetFailed'));
        }
      }

      return {
        t,
        moveSpeed,
        presetId,
        startMove,
        startZoom,
        stopMove,
        handleStop,
        handlePresetSet,
        handlePresetCall,
        handlePresetDelete,
      };
    },
  });
</script>
<style lang="less" scoped>
  .ptz-control {
    .ptz-title {
      font-weight: 600;
      font-size: 14px;
      margin-bottom: 12px;
    }

    .ptz-direction-pad {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;

      .ptz-row {
        display: flex;
        gap: 2px;
      }

      .ptz-btn {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        padding: 0;

        &.center {
          border-radius: 50%;
        }
      }
    }

    // 通过 CSS 旋转实现对角箭头图标
    .rotate-icon {
      display: inline-block;

      &.rotate-45 {
        transform: rotate(45deg);
      }

      &.rotate-315 {
        transform: rotate(-45deg);
      }
    }

    .ptz-zoom {
      display: flex;
      gap: 8px;
      justify-content: center;
      margin-top: 12px;
    }

    .ptz-speed {
      display: flex;
      align-items: center;
      margin-top: 12px;
      font-size: 13px;
    }

    .ptz-presets {
      .ptz-preset-row {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        font-size: 13px;
      }

      .ptz-preset-actions {
        display: flex;
        gap: 4px;
      }
    }
  }
</style>
