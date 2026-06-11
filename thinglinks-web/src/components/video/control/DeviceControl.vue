<template>
  <div class="device-control">
    <div class="control-title">{{ t('video.device.live.control.title') }}</div>
    <div class="control-actions">
      <a-tooltip :title="t('video.device.live.control.teleBoot')">
        <a-button @click="handleTeleBoot" danger size="small">
          <template #icon><ReloadOutlined /></template>
          {{ t('video.device.live.control.teleBoot') }}
        </a-button>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.control.recordStart')">
        <a-button @click="handleRecordStart" type="primary" size="small">
          <template #icon><VideoCameraOutlined /></template>
          {{ t('video.device.live.control.recordStart') }}
        </a-button>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.control.recordStop')">
        <a-button @click="handleRecordStop" size="small">
          <template #icon><VideoCameraAddOutlined /></template>
          {{ t('video.device.live.control.recordStop') }}
        </a-button>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.control.guardSet')">
        <a-button @click="handleGuardSet" size="small">
          <template #icon><SafetyCertificateOutlined /></template>
          {{ t('video.device.live.control.guardSet') }}
        </a-button>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.control.guardReset')">
        <a-button @click="handleGuardReset" size="small">
          <template #icon><UnlockOutlined /></template>
          {{ t('video.device.live.control.guardReset') }}
        </a-button>
      </a-tooltip>
      <a-tooltip :title="t('video.device.live.control.forceKeyFrame')">
        <a-button @click="handleForceKeyFrame" size="small">
          <template #icon><PictureOutlined /></template>
          {{ t('video.device.live.control.forceKeyFrame') }}
        </a-button>
      </a-tooltip>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    ReloadOutlined,
    VideoCameraOutlined,
    VideoCameraAddOutlined,
    SafetyCertificateOutlined,
    UnlockOutlined,
    PictureOutlined,
  } from '@ant-design/icons-vue';
  import {
    teleBoot,
    recordStart,
    recordStop,
    guardSet,
    guardReset,
    forceKeyFrame,
  } from '/@/api/video/device/deviceControl';

  export default defineComponent({
    name: 'DeviceControl',
    components: {
      ReloadOutlined,
      VideoCameraOutlined,
      VideoCameraAddOutlined,
      SafetyCertificateOutlined,
      UnlockOutlined,
      PictureOutlined,
    },
    props: {
      deviceIdentification: { type: String, required: true },
      channelIdentification: { type: String, required: true },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();

      function handleTeleBoot() {
        createConfirm({
          iconType: 'warning',
          title: t('video.device.live.control.teleBoot'),
          content: t('video.device.live.confirmReboot'),
          onOk: async () => {
            try {
              await teleBoot(props.deviceIdentification, props.channelIdentification);
              createMessage.success(t('video.device.live.control.teleBootSuccess'));
            } catch (e: any) {
              createMessage.error(e?.message || t('video.device.live.control.teleBootFailed'));
            }
          },
        });
      }

      async function handleRecordStart() {
        try {
          await recordStart(props.deviceIdentification, props.channelIdentification);
          createMessage.success(t('video.device.live.control.recordStartSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.control.recordFailed'));
        }
      }

      async function handleRecordStop() {
        try {
          await recordStop(props.deviceIdentification, props.channelIdentification);
          createMessage.success(t('video.device.live.control.recordStopSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.control.recordFailed'));
        }
      }

      async function handleGuardSet() {
        try {
          await guardSet(props.deviceIdentification, props.channelIdentification);
          createMessage.success(t('video.device.live.control.guardSetSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.control.guardFailed'));
        }
      }

      async function handleGuardReset() {
        try {
          await guardReset(props.deviceIdentification, props.channelIdentification);
          createMessage.success(t('video.device.live.control.guardResetSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.control.guardFailed'));
        }
      }

      async function handleForceKeyFrame() {
        try {
          await forceKeyFrame(props.deviceIdentification, props.channelIdentification);
          createMessage.success(t('video.device.live.control.forceKeyFrameSuccess'));
        } catch (e: any) {
          createMessage.error(e?.message || t('video.device.live.control.forceKeyFrameFailed'));
        }
      }

      return {
        t,
        handleTeleBoot,
        handleRecordStart,
        handleRecordStop,
        handleGuardSet,
        handleGuardReset,
        handleForceKeyFrame,
      };
    },
  });
</script>
<style lang="less" scoped>
  .device-control {
    .control-title {
      font-weight: 600;
      font-size: 14px;
      margin-bottom: 12px;
    }

    .control-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 6px;

      .ant-btn {
        font-size: 12px;
        padding: 0 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
</style>
