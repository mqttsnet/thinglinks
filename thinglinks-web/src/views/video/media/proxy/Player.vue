<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t('video.media.videoStreamProxy.videoPlaying')"
    :maskClosable="false"
    :keyboard="true"
    :showOkBtn="false"
    :destroyOnClose="true"
    :cancelText="t('common.closeText')"
    @cancel="handleCancel"
  >
    <VPlayer :videoUrl="videoUrl" />
    <RealTimeInfo :videoUrl="videoUrl" :urlMap="urlMap" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import VPlayer, { RealTimeInfo } from '/@/components/VideoPlayer';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getPlayUrl } from '/@/api/video/media/videoStreamProxy';
  const { t } = useI18n();

  const urlMap = ref<Record<string, any>>({});

  const [registerModel, { closeModal }] = useModalInner(async (data) => {
    if (data?.id) {
      const { zlmMediaServerStreamInfoList } = await getPlayUrl(data.id);
      if (zlmMediaServerStreamInfoList && zlmMediaServerStreamInfoList.length > 0) {
        urlMap.value = zlmMediaServerStreamInfoList[0];
        delete urlMap.value?.videoMediaServerResultVO;
      } else {
        urlMap.value = {};
      }
    }
  });

  const videoUrl = computed(() => {
    return window.location.protocol.includes('https') ? urlMap.value?.wssFlv : urlMap.value?.wsFlv;
  });

  const handleCancel = () => {
    closeModal();
  };
</script>
<style lang="less" scoped></style>
