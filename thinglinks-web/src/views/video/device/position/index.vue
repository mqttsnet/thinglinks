<template>
  <PageWrapper dense contentFullHeight>
    <div class="position-toolbar">
      <a-input
        v-model:value="deviceId"
        :placeholder="t('video.device.position.inputDeviceId')"
        style="width: 300px"
        @pressEnter="handleSearch"
      />
      <a-button type="primary" preIcon="ant-design:search-outlined" @click="handleSearch">
        {{ t('video.device.position.search') }}
      </a-button>
      <a-button
        type="primary"
        preIcon="ant-design:aim-outlined"
        @click="handleSubscribe"
        v-hasAnyPermission="['video:device:position:subscribe']"
        :disabled="!deviceId"
      >
        {{ t('video.device.position.subscribe') }}
      </a-button>
      <a-button
        preIcon="ant-design:disconnect-outlined"
        @click="handleUnsubscribe"
        v-hasAnyPermission="['video:device:position:unsubscribe']"
        :disabled="!deviceId"
      >
        {{ t('video.device.position.unsubscribe') }}
      </a-button>
    </div>

    <Card v-if="positionData" :bordered="false">
      <a-descriptions
        :title="t('video.device.position.latestPosition')"
        bordered
      >
        <a-descriptions-item :label="t('video.device.position.deviceIdentification')">
          {{ positionData.deviceIdentification }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.longitude')">
          {{ positionData.longitude }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.latitude')">
          {{ positionData.latitude }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.altitude')">
          {{ positionData.altitude }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.speed')">
          {{ positionData.speed }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.direction')">
          {{ positionData.direction }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.geoCoordSys')">
          {{ positionData.geoCoordSys }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.reportTime')">
          {{ positionData.reportTime }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('video.device.position.createdTime')">
          {{ positionData.createdTime }}
        </a-descriptions-item>
      </a-descriptions>
    </Card>
    <a-empty v-else :description="t('video.device.position.noPosition')" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { Card } from 'ant-design-vue';
  import {
    getLatestPosition,
    subscribe,
    unsubscribe,
  } from '/@/api/video/device/position';
  import type { VideoDeviceMobilePositionResultVO } from '/@/api/video/device/model/positionModel';

  export default defineComponent({
    name: 'VideoDevicePosition',
    components: { PageWrapper, Card },
    setup() {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const deviceId = ref<string>('');
      const positionData = ref<VideoDeviceMobilePositionResultVO | null>(null);

      async function handleSearch() {
        if (!deviceId.value) {
          createMessage.warning(t('video.device.position.inputDeviceId'));
          return;
        }
        const res = await getLatestPosition(deviceId.value);
        positionData.value = res || null;
      }

      async function handleSubscribe() {
        if (!deviceId.value) return;
        await subscribe(deviceId.value, 5);
        createMessage.success(t('video.device.position.subscribeSuccess'));
      }

      async function handleUnsubscribe() {
        if (!deviceId.value) return;
        await unsubscribe(deviceId.value);
        createMessage.success(t('video.device.position.unsubscribeSuccess'));
      }

      return {
        t,
        deviceId,
        positionData,
        handleSearch,
        handleSubscribe,
        handleUnsubscribe,
      };
    },
  });
</script>
<style lang="less" scoped>
  .position-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }
</style>
