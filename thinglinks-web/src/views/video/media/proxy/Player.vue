<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t('video.media.proxy.videoPlaying')"
    :maskClosable="false"
    :keyboard="true"
    :showOkBtn="false"
    :destroyOnClose="true"
    :cancelText="t('common.closeText')"
    @cancel="handleCancel"
  >
    <div class="player-header">
      <span></span>
      <a-select
        v-model:value="currentPlayerType"
        size="small"
        style="width: 140px"
        @change="handlePlayerChange"
      >
        <a-select-option :value="VideoPlayerType.JESSIBUCA">Jessibuca</a-select-option>
        <a-select-option :value="VideoPlayerType.FLV">FLV.js</a-select-option>
        <a-select-option :value="VideoPlayerType.HLS">HLS.js</a-select-option>
      </a-select>
    </div>
    <VideoPlayer
      ref="playerRef"
      :url="videoUrl"
      :playerType="currentPlayerType"
      :height="350"
      auto-aspect
    />
    <RealTimeInfo :videoUrl="videoUrl" :urlMap="urlMap" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { VideoPlayer } from '/@/components/video/player';
  import { RealTimeInfo } from '/@/components/VideoPlayer';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getPlayUrl } from '/@/api/video/media/proxy';
  import { VideoPlayerType } from '/@/enums/video/player';
  import type { PlayerType } from '/@/components/video/player';

  const PLAYER_TYPE_STORAGE_KEY = 'video_player_type';
  const { t } = useI18n();

  const urlMap = ref<Record<string, any>>({});
  const savedType = (localStorage.getItem(PLAYER_TYPE_STORAGE_KEY) as PlayerType) || VideoPlayerType.JESSIBUCA;
  const currentPlayerType = ref<PlayerType>(savedType);
  const playerRef = ref<any>(null);

  const [registerModel, { closeModal }] = useModalInner(async (data) => {
    if (data?.id) {
      const { zlmMediaServerStreamInfoList } = await getPlayUrl(data.id);
      if (zlmMediaServerStreamInfoList && zlmMediaServerStreamInfoList.length > 0) {
        const streamInfo = { ...zlmMediaServerStreamInfoList[0] };
        delete streamInfo.mediaServer;
        urlMap.value = streamInfo;
      } else {
        urlMap.value = {};
      }
    }
  });

  const videoUrl = computed(() => {
    if (!urlMap.value || Object.keys(urlMap.value).length === 0) return '';
    const isHttps = window.location.protocol.includes('https');
    switch (currentPlayerType.value) {
      case VideoPlayerType.HLS:
        return isHttps ? urlMap.value?.httpsHls : urlMap.value?.hls;
      case VideoPlayerType.FLV:
        return isHttps ? urlMap.value?.httpsFlv : urlMap.value?.flv;
      case VideoPlayerType.JESSIBUCA:
      default:
        return isHttps ? urlMap.value?.wssFlv : urlMap.value?.wsFlv;
    }
  });

  const handlePlayerChange = (_type: PlayerType) => {
    currentPlayerType.value = _type;
    localStorage.setItem(PLAYER_TYPE_STORAGE_KEY, _type);
  };

  const handleCancel = () => {
    closeModal();
  };
</script>

<style lang="less" scoped>
  .player-header {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-bottom: 8px;
  }
</style>
