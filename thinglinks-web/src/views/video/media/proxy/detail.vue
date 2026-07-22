<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部信息卡片 -->
    <div class="detail-info">
      <Card :bordered="false">
        <div class="header-row">
          <div class="header-left">
            <span class="title">{{ proxyDetail.proxyName }}</span>
            <Tag :color="proxyDetail.status ? 'green' : 'red'">
              {{ proxyDetail.status ? t('video.media.proxy.statusEnabled') : t('video.media.proxy.statusDisabled') }}
            </Tag>
            <Tag color="blue">
              {{ getDictLabel(DictEnum.VIDEO_MEDIA_STREAM_PROXY_TYPE, proxyDetail.proxyType) }}
            </Tag>
          </div>
          <div class="header-right">
            <a-button
              type="primary"
              danger
              @click="handleEdit"
              v-hasAnyPermission="['video:media:proxy:edit']"
            >
              <template #icon><EditOutlined /></template>
              {{ t('video.media.proxy.updateProxyInfo') }}
            </a-button>
          </div>
        </div>
        <div class="meta-row">
          <div class="meta-item">
            <span class="meta-label">{{ t('video.media.proxy.appId') }}</span>
            <span><Tag v-if="proxyDetail.appId" color="blue">{{ proxyDetail.appId }}</Tag>{{ getDictLabel(DictEnum.VIDEO_APPLICATION_SCENARIO, proxyDetail.appId) || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('video.media.proxy.streamIdentification') }}</span>
            <span>{{ proxyDetail.streamIdentification || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('video.media.proxy.mediaIdentification') }}</span>
            <span>{{ proxyDetail.mediaIdentification || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('video.media.proxy.createdTime') }}</span>
            <span>{{ proxyDetail.createdTime || '-' }}</span>
          </div>
        </div>
      </Card>
    </div>

    <!-- 播放器 + 基本信息 -->
    <Row :gutter="16" class="detail-info">
      <Col :span="14">
        <Card :bordered="false">
          <template #title>
            <div class="player-header">
              <span>{{ t('video.media.proxy.videoPlaying') }}</span>
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
          </template>
          <VideoPlayer
            ref="playerRef"
            :url="videoUrl"
            :playerType="currentPlayerType"
            :height="380"
            auto-aspect
          />
          <RealTimeInfo :videoUrl="videoUrl" :urlMap="urlMap" />
        </Card>
      </Col>
      <Col :span="10">
        <Card :title="t('video.media.proxy.baseInfo')" :bordered="false">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item :label="t('video.media.proxy.proxyName')">
              {{ proxyDetail.proxyName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.streamIdentification')">
              {{ proxyDetail.streamIdentification || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.url')">
              {{ proxyDetail.url || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.srcUrl')">
              {{ proxyDetail.srcUrl || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.dstUrl')">
              {{ proxyDetail.dstUrl || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.rtpType')">
              {{ getDictLabel(DictEnum.VIDEO_MEDIA_STREAM_PROXY_RTP_TYPE, proxyDetail.rtpType) || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.timeoutMs')">
              {{ proxyDetail.timeoutMs ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.ffmpegCmdKey')">
              {{ proxyDetail.ffmpegCmdKey || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.enableAudio')">
              <Tag :color="proxyDetail.enableAudio ? 'green' : 'default'">
                {{ proxyDetail.enableAudio ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
              </Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.enableMp4')">
              <Tag :color="proxyDetail.enableMp4 ? 'green' : 'default'">
                {{ proxyDetail.enableMp4 ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
              </Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.enableRemoveNoneReader')">
              <Tag :color="proxyDetail.enableRemoveNoneReader ? 'green' : 'default'">
                {{ proxyDetail.enableRemoveNoneReader ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
              </Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.proxy.remark')">
              {{ proxyDetail.remark || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { detail, getPlayUrl } from '/@/api/video/media/proxy';
  import type { VideoStreamProxyResultVO } from '/@/api/video/media/model/proxyModel';
  import { PageWrapper } from '/@/components/Page';
  import { EditOutlined } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import EditModal from './Edit.vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { Tag, Card, Row, Col } from 'ant-design-vue';
  import { VideoPlayer } from '/@/components/video/player';
  import { RealTimeInfo } from '/@/components/VideoPlayer';
  import { VideoPlayerType } from '/@/enums/video/player';
  import type { PlayerType } from '/@/components/video/player';

  const { getDictLabel } = useDict();
  const { t } = useI18n();
  const { currentRoute } = useRouter();

  const proxyDetail = reactive<Partial<VideoStreamProxyResultVO>>({});
  const [registerModal, { openModal }] = useModal();
  const PLAYER_TYPE_STORAGE_KEY = 'video_player_type';
  const id = ref('');
  const urlMap = ref<Record<string, any>>({});
  const savedType = (localStorage.getItem(PLAYER_TYPE_STORAGE_KEY) as PlayerType) || VideoPlayerType.JESSIBUCA;
  const currentPlayerType = ref<PlayerType>(savedType);
  const playerRef = ref<any>(null);

  const videoUrl = computed(() => {
    if (!urlMap.value || Object.keys(urlMap.value).length === 0) return '';
    const isHttps = window.location.protocol.includes('https');
    switch (currentPlayerType.value) {
      case VideoPlayerType.HLS:
        return isHttps ? urlMap.value?.httpsHls : urlMap.value?.hls;
      case VideoPlayerType.FLV:
        // flv.js 使用 HTTP-FLV 更稳定
        return isHttps ? urlMap.value?.httpsFlv : urlMap.value?.flv;
      case VideoPlayerType.JESSIBUCA:
      default:
        // Jessibuca 使用 WebSocket FLV
        return isHttps ? urlMap.value?.wssFlv : urlMap.value?.wsFlv;
    }
  });

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    load();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(proxyDetail, res);
    loadPlayUrl();
  };

  const loadPlayUrl = async () => {
    try {
      const res = await getPlayUrl(id.value);
      if (res?.zlmMediaServerStreamInfoList?.length > 0) {
        const streamInfo = { ...res.zlmMediaServerStreamInfoList[0] };
        delete streamInfo.mediaServer;
        urlMap.value = streamInfo;
      } else {
        urlMap.value = {};
      }
    } catch (e) {
      urlMap.value = {};
    }
  };

  const handlePlayerChange = (type: PlayerType) => {
    currentPlayerType.value = type;
    localStorage.setItem(PLAYER_TYPE_STORAGE_KEY, type);
  };

  function handleEdit() {
    openModal(true, {
      record: proxyDetail,
      type: ActionEnum.EDIT,
    });
  }

  function handleSuccess() {
    load();
  }
</script>

<style lang="less" scoped>
  .detail-info {
    & + .detail-info {
      margin-top: 16px;
    }
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .meta-row {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #a6a6a6;

    .meta-item {
      padding-right: 12px;

      & + .meta-item {
        padding-left: 12px;
        border-left: 1px solid #e0e0e0;
      }

      .meta-label {
        margin-right: 4px;
      }
    }
  }

  .player-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
</style>
