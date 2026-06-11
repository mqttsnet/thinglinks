<template>
  <PageWrapper dense contentFullHeight contentClass="playback-page">
    <div class="playback-layout">
      <!-- Left Panel -->
      <div class="playback-left">
        <Card :bordered="false" class="left-card">
          <!-- Date Picker -->
          <div class="left-section">
            <div class="section-label">
              <CalendarOutlined />
              {{ t('video.record.playback.selectDate') }}
            </div>
            <a-date-picker
              v-model:value="selectedDate"
              :allow-clear="false"
              style="width: 100%"
              size="small"
              @change="handleDateChange"
            />
          </div>

          <!-- Playback Type -->
          <div class="left-section">
            <div class="section-label">
              <SwitcherOutlined />
              {{ t('video.record.playback.sourceType') }}
            </div>
            <a-radio-group v-model:value="playbackType" size="small" button-style="solid" class="type-radio">
              <a-radio-button value="device">{{ t('video.record.playback.deviceRecord') }}</a-radio-button>
              <a-radio-button value="cloud">{{ t('video.record.playback.cloudRecord') }}</a-radio-button>
            </a-radio-group>
          </div>

          <!-- Channel Selector -->
          <div class="left-section channel-section">
            <div class="section-label">
              <VideoCameraOutlined />
              {{ t('video.record.playback.channelList') }}
            </div>
            <ChannelSelector ref="channelSelectorRef" @select="handleChannelSelect" />
          </div>

          <!-- Record File List (Cloud mode) -->
          <div class="left-section file-section" v-if="playbackType === 'cloud'">
            <RecordFileList
              :files="recordFiles"
              :active-file-id="activeFileId"
              :loading="filesLoading"
              @select="handleFileSelect"
              @download="handleFileDownload"
            />
          </div>
        </Card>
      </div>

      <!-- Right Panel -->
      <div class="playback-right">
        <!-- Player Card -->
        <Card :bordered="false" class="player-card">
          <template #title>
            <div class="player-header">
              <div class="player-title">
                <PlaySquareOutlined />
                <span>{{ t('video.record.playback.videoPlayback') }}</span>
                <Tag v-if="playbackStatus === 'playing'" color="success">
                  {{ t('video.record.playback.playing') }}
                </Tag>
                <Tag v-else-if="playbackStatus === 'paused'" color="warning">
                  {{ t('video.record.playback.paused') }}
                </Tag>
                <Tag v-else-if="playbackStatus === 'loading'" color="processing">
                  {{ t('video.record.playback.loading') }}
                </Tag>
              </div>
              <div class="player-type-selector">
                <a-select
                  v-model:value="currentPlayerType"
                  size="small"
                  style="width: 130px"
                  @change="handlePlayerTypeChange"
                >
                  <a-select-option :value="VideoPlayerType.JESSIBUCA">Jessibuca</a-select-option>
                  <a-select-option :value="VideoPlayerType.FLV">FLV.js</a-select-option>
                  <a-select-option :value="VideoPlayerType.HLS">HLS.js</a-select-option>
                </a-select>
              </div>
            </div>
          </template>

          <!-- Player Area -->
          <div class="player-area">
            <div v-if="!videoUrl" class="player-placeholder">
              <div class="placeholder-icon">
                <VideoCameraOutlined />
              </div>
              <div class="placeholder-text">
                {{ selectedChannel
                  ? t('video.record.playback.clickToPlay')
                  : t('video.record.playback.selectChannelFirst')
                }}
              </div>
              <div class="placeholder-hint">
                {{ t('video.record.playback.shortcutHint') }}
              </div>
            </div>
            <VideoPlayer
              v-else
              ref="playerRef"
              :url="videoUrl"
              :playerType="currentPlayerType"
              :height="playerHeight"
              auto-aspect
              @play="onPlayerPlay"
              @pause="onPlayerPause"
              @error="onPlayerError"
            />
          </div>
        </Card>

        <!-- Timeline Card -->
        <Card :bordered="false" class="timeline-card" v-if="selectedChannel">
          <RecordTimeline
            ref="timelineRef"
            :segments="timelineSegments"
            :currentTime="currentPlayTime"
            @seek="handleTimelineSeek"
          />
        </Card>

        <!-- Controls Card -->
        <Card :bordered="false" class="controls-card" v-if="selectedChannel">
          <PlaybackControls
            :playing="playbackStatus === 'playing'"
            :currentSpeed="currentSpeed"
            :currentTime="currentPlayTime"
            :canControl="!!videoUrl"
            :canDownload="playbackType === 'cloud' && !!activeFileId"
            @play="handlePlay"
            @pause="handlePause"
            @stop="handleStop"
            @seekForward="handleSeekForward"
            @seekBackward="handleSeekBackward"
            @speedChange="handleSpeedChange"
            @screenshot="handleScreenshot"
            @download="handleDownloadCurrent"
            @fullscreen="handleFullscreen"
          />
        </Card>
      </div>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
  import dayjs, { Dayjs } from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Tag } from 'ant-design-vue';
  import {
    CalendarOutlined,
    SwitcherOutlined,
    VideoCameraOutlined,
    PlaySquareOutlined,
  } from '@ant-design/icons-vue';
  import { VideoPlayer } from '/@/components/video/player';
  import { VideoPlayerType } from '/@/enums/video/player';
  import type { PlayerType } from '/@/components/video/player';
  import type { VideoDeviceChannelResultVO } from '/@/api/video/device/model/channelModel';
  import type { VideoRecordFileResultVO } from '/@/api/video/record/model/fileModel';
  import type { PlaybackResultVO } from '/@/api/video/record/model/playbackModel';

  import { listByDate } from '/@/api/video/record/file';
  import { getPlayUrl as getFilePlayUrl, getDownloadUrl } from '/@/api/video/record/file';
  import {
    playbackStart,
    playbackStop,
    playbackPause,
    playbackResume,
    playbackSpeed,
    playbackSeek,
  } from '/@/api/video/stream/playback';

  import ChannelSelector from './components/ChannelSelector.vue';
  import RecordFileList from './components/RecordFileList.vue';
  import RecordTimeline from './components/RecordTimeline.vue';
  import PlaybackControls from './components/PlaybackControls.vue';

  const { t } = useI18n();
  const { createMessage } = useMessage();

  // ─── State ──────────────────────────────
  const selectedDate = ref<Dayjs>(dayjs());
  const playbackType = ref<'device' | 'cloud'>('cloud');
  const selectedChannel = ref<VideoDeviceChannelResultVO | null>(null);
  const recordFiles = ref<VideoRecordFileResultVO[]>([]);
  const filesLoading = ref(false);
  const activeFileId = ref<string>('');
  const videoUrl = ref('');
  const playbackStatus = ref<'idle' | 'loading' | 'playing' | 'paused' | 'stopped'>('idle');
  const currentSpeed = ref(1);
  const currentPlayTime = ref(0); // seconds from 00:00
  const playerHeight = ref(420);

  // Player refs
  const playerRef = ref<any>(null);
  const timelineRef = ref<any>(null);
  const channelSelectorRef = ref<any>(null);

  // Player type
  const PLAYER_TYPE_KEY = 'video_playback_player_type';
  const savedType = (localStorage.getItem(PLAYER_TYPE_KEY) as PlayerType) || VideoPlayerType.JESSIBUCA;
  const currentPlayerType = ref<PlayerType>(savedType);

  // Device playback state
  const devicePlaybackResult = ref<PlaybackResultVO | null>(null);
  let playTimeTimer: ReturnType<typeof setInterval> | null = null;

  // ─── Timeline segments (computed from recordFiles or device record query) ──
  const timelineSegments = computed(() => {
    return recordFiles.value
      .filter((f) => f.startTime && f.endTime)
      .map((f) => ({
        startTime: timeToSeconds(f.startTime!),
        endTime: timeToSeconds(f.endTime!),
      }));
  });

  // ─── Helpers ──────────────────────────────
  function timeToSeconds(timeStr: string): number {
    try {
      const d = dayjs(timeStr);
      return d.hour() * 3600 + d.minute() * 60 + d.second();
    } catch {
      return 0;
    }
  }

  function getVideoUrlFromResult(result: PlaybackResultVO): string {
    const isHttps = window.location.protocol.includes('https');
    switch (currentPlayerType.value) {
      case VideoPlayerType.HLS:
        return isHttps ? result.httpsHls?.url : result.hls?.url;
      case VideoPlayerType.FLV:
        return isHttps ? result.httpsFlv?.url : result.flv?.url;
      case VideoPlayerType.JESSIBUCA:
      default:
        // 回放流使用 wsFlv；HTTPS 环境下将 ws:// 替换为 wss://
        if (isHttps && result.wsFlv?.url) {
          return result.wsFlv.url.replace('ws://', 'wss://');
        }
        return result.wsFlv?.url;
    }
  }

  // ─── Date & Channel Selection ──────────────
  function handleDateChange() {
    loadRecordData();
  }

  function handleChannelSelect(channel: VideoDeviceChannelResultVO) {
    // Stop any current playback
    stopPlayback();
    selectedChannel.value = channel;
    loadRecordData();
  }

  // ─── Load record data based on type ──────────
  async function loadRecordData() {
    if (!selectedChannel.value) return;

    const channel = selectedChannel.value;
    const dateStr = selectedDate.value.format('YYYY-MM-DD');

    if (playbackType.value === 'cloud') {
      await loadCloudRecords(channel, dateStr);
    } else {
      await loadDeviceRecords(channel, dateStr);
    }
  }

  async function loadCloudRecords(
    channel: VideoDeviceChannelResultVO,
    date: string,
  ) {
    filesLoading.value = true;
    try {
      const res = await listByDate(
        channel.deviceIdentification!,
        channel.channelIdentification!,
        date,
      );
      recordFiles.value = res || [];
    } catch (e) {
      recordFiles.value = [];
      createMessage.error(t('video.record.playback.noRecordData'));
    } finally {
      filesLoading.value = false;
    }
  }

  async function loadDeviceRecords(
    channel: VideoDeviceChannelResultVO,
    date: string,
  ) {
    filesLoading.value = true;
    try {
      // For device recording, we use the playback API to query device-side records
      // The results come asynchronously via SIP MESSAGE callback
      createMessage.info(t('video.record.playback.queryDeviceRecord'));

      // Build time range for the selected date
      const startTime = `${date} 00:00:00`;
      const endTime = `${date} 23:59:59`;

      // Start the playback directly with the time range
      // The device will stream the recording for the given period
      const result = await playbackStart({
        deviceIdentification: channel.deviceIdentification!,
        channelIdentification: channel.channelIdentification!,
        startTime,
        endTime,
      });

      devicePlaybackResult.value = result;
      const url = getVideoUrlFromResult(result);
      if (url) {
        videoUrl.value = url;
        playbackStatus.value = 'loading';
        // Set timeline to full day
        recordFiles.value = [
          {
            startTime,
            endTime,
            deviceIdentification: channel.deviceIdentification,
            channelIdentification: channel.channelIdentification,
          } as VideoRecordFileResultVO,
        ];
        startPlayTimeTracking(startTime);
      }
    } catch (e) {
      createMessage.error(t('video.record.playback.queryDeviceRecordFailed'));
      recordFiles.value = [];
    } finally {
      filesLoading.value = false;
    }
  }

  // ─── Cloud file playback ──────────────────
  async function handleFileSelect(file: VideoRecordFileResultVO) {
    if (!file.id) return;
    activeFileId.value = file.id;
    playbackStatus.value = 'loading';

    try {
      const url = await getFilePlayUrl(file.id);
      if (url) {
        videoUrl.value = url;
        // Set initial play time from file start
        currentPlayTime.value = timeToSeconds(file.startTime || '');
      } else {
        createMessage.error(t('video.record.playback.playbackFailed'));
        playbackStatus.value = 'idle';
      }
    } catch (e) {
      createMessage.error(t('video.record.playback.playbackFailed'));
      playbackStatus.value = 'idle';
    }
  }

  async function handleFileDownload(file: VideoRecordFileResultVO) {
    if (!file.id) return;
    try {
      const url = await getDownloadUrl(file.id);
      if (url) {
        window.open(url, '_blank');
        createMessage.success(t('video.record.playback.downloadStarted'));
      }
    } catch (e) {
      createMessage.error(t('video.record.playback.playbackFailed'));
    }
  }

  // ─── Playback Controls ──────────────────
  function handlePlay() {
    if (playbackType.value === 'device' && devicePlaybackResult.value) {
      playbackResume(
        selectedChannel.value!.deviceIdentification!,
        selectedChannel.value!.channelIdentification!,
      );
    }
    playerRef.value?.play?.();
    playbackStatus.value = 'playing';
  }

  function handlePause() {
    if (playbackType.value === 'device' && devicePlaybackResult.value) {
      playbackPause(
        selectedChannel.value!.deviceIdentification!,
        selectedChannel.value!.channelIdentification!,
      );
    }
    playerRef.value?.pause?.();
    playbackStatus.value = 'paused';
  }

  async function handleStop() {
    await stopPlayback();
    createMessage.info(t('video.record.playback.playbackStopped'));
  }

  async function stopPlayback() {
    // Stop device playback via SIP
    if (playbackType.value === 'device' && devicePlaybackResult.value && selectedChannel.value) {
      try {
        await playbackStop(
          selectedChannel.value.deviceIdentification!,
          selectedChannel.value.channelIdentification!,
        );
      } catch (e) {
        // ignore
      }
      devicePlaybackResult.value = null;
    }
    // Destroy player
    playerRef.value?.destroy?.();
    videoUrl.value = '';
    playbackStatus.value = 'idle';
    activeFileId.value = '';
    currentPlayTime.value = 0;
    currentSpeed.value = 1;
    stopPlayTimeTracking();
  }

  async function handleSeekForward() {
    const seekSeconds = 30;
    if (playbackType.value === 'device' && selectedChannel.value) {
      const newTime = currentPlayTime.value + seekSeconds;
      try {
        await playbackSeek(
          selectedChannel.value.deviceIdentification!,
          selectedChannel.value.channelIdentification!,
          Math.floor(dayjs(selectedDate.value.format('YYYY-MM-DD')).unix() + newTime),
        );
        currentPlayTime.value = newTime;
      } catch (e) {
        createMessage.error(t('video.record.playback.seekFailed'));
      }
    } else {
      // Cloud: advance current play time (player handles natively)
      currentPlayTime.value += seekSeconds;
    }
  }

  async function handleSeekBackward() {
    const seekSeconds = 30;
    if (playbackType.value === 'device' && selectedChannel.value) {
      const newTime = Math.max(0, currentPlayTime.value - seekSeconds);
      try {
        await playbackSeek(
          selectedChannel.value.deviceIdentification!,
          selectedChannel.value.channelIdentification!,
          Math.floor(dayjs(selectedDate.value.format('YYYY-MM-DD')).unix() + newTime),
        );
        currentPlayTime.value = newTime;
      } catch (e) {
        createMessage.error(t('video.record.playback.seekFailed'));
      }
    } else {
      currentPlayTime.value = Math.max(0, currentPlayTime.value - seekSeconds);
    }
  }

  async function handleSpeedChange(speed: number) {
    currentSpeed.value = speed;
    if (playbackType.value === 'device' && selectedChannel.value) {
      try {
        await playbackSpeed(
          selectedChannel.value.deviceIdentification!,
          selectedChannel.value.channelIdentification!,
          speed,
        );
      } catch (e) {
        createMessage.error(t('video.record.playback.seekFailed'));
      }
    }
    // Cloud: handled natively by player (playbackRate)
  }

  async function handleTimelineSeek(seconds: number) {
    if (playbackType.value === 'device' && selectedChannel.value) {
      try {
        await playbackSeek(
          selectedChannel.value.deviceIdentification!,
          selectedChannel.value.channelIdentification!,
          Math.floor(dayjs(selectedDate.value.format('YYYY-MM-DD')).unix() + seconds),
        );
        currentPlayTime.value = seconds;
      } catch (e) {
        createMessage.error(t('video.record.playback.seekFailed'));
      }
    } else {
      // Cloud mode: find the file that contains this time, play it
      currentPlayTime.value = seconds;
      const file = recordFiles.value.find((f) => {
        const start = timeToSeconds(f.startTime || '');
        const end = timeToSeconds(f.endTime || '');
        return seconds >= start && seconds <= end;
      });
      if (file && file.id) {
        handleFileSelect(file);
      }
    }
  }

  function handleScreenshot() {
    playerRef.value?.screenshot?.(`playback_${dayjs().format('YYYYMMDDHHmmss')}`);
    createMessage.success(t('video.record.playback.screenshotSaved'));
  }

  async function handleDownloadCurrent() {
    if (activeFileId.value) {
      try {
        const url = await getDownloadUrl(activeFileId.value);
        if (url) {
          window.open(url, '_blank');
          createMessage.success(t('video.record.playback.downloadStarted'));
        }
      } catch (e) {
        createMessage.error(t('video.record.playback.playbackFailed'));
      }
    }
  }

  function handleFullscreen() {
    playerRef.value?.fullscreen?.();
  }

  function handlePlayerTypeChange(type: PlayerType) {
    currentPlayerType.value = type;
    localStorage.setItem(PLAYER_TYPE_KEY, type);
  }

  // ─── Player events ──────────────────
  function onPlayerPlay() {
    playbackStatus.value = 'playing';
  }

  function onPlayerPause() {
    playbackStatus.value = 'paused';
  }

  function onPlayerError(e: any) {
    console.error('Player error:', e);
    playbackStatus.value = 'stopped';
  }

  // ─── Play time tracking for device playback ──
  function startPlayTimeTracking(startTime: string) {
    stopPlayTimeTracking();
    const startSec = timeToSeconds(startTime);
    currentPlayTime.value = startSec;

    playTimeTimer = setInterval(() => {
      if (playbackStatus.value === 'playing') {
        currentPlayTime.value += currentSpeed.value;
      }
    }, 1000);
  }

  function stopPlayTimeTracking() {
    if (playTimeTimer) {
      clearInterval(playTimeTimer);
      playTimeTimer = null;
    }
  }

  // ─── Keyboard shortcuts ──────────────────
  function handleKeydown(e: KeyboardEvent) {
    // Ignore if user is typing in an input
    if (['INPUT', 'TEXTAREA', 'SELECT'].includes((e.target as HTMLElement)?.tagName)) return;

    switch (e.code) {
      case 'Space':
        e.preventDefault();
        if (playbackStatus.value === 'playing') {
          handlePause();
        } else if (videoUrl.value) {
          handlePlay();
        }
        break;
      case 'ArrowLeft':
        e.preventDefault();
        handleSeekBackward();
        break;
      case 'ArrowRight':
        e.preventDefault();
        handleSeekForward();
        break;
      case 'ArrowUp':
        e.preventDefault();
        {
          const speeds = [0.25, 0.5, 1, 2, 4];
          const idx = speeds.indexOf(currentSpeed.value);
          if (idx < speeds.length - 1) {
            handleSpeedChange(speeds[idx + 1]);
          }
        }
        break;
      case 'ArrowDown':
        e.preventDefault();
        {
          const speeds = [0.25, 0.5, 1, 2, 4];
          const idx = speeds.indexOf(currentSpeed.value);
          if (idx > 0) {
            handleSpeedChange(speeds[idx - 1]);
          }
        }
        break;
    }
  }

  // ─── Watch playback type switch ──────────
  watch(playbackType, () => {
    stopPlayback();
    recordFiles.value = [];
    loadRecordData();
  });

  // ─── Lifecycle ──────────────────
  onMounted(() => {
    window.addEventListener('keydown', handleKeydown);
  });

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown);
    stopPlayback();
    stopPlayTimeTracking();
  });
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
  @danger: #fa896b;
  @radius-lg: 16px;
  @shadow-card: 0 2px 12px rgba(0, 0, 0, 0.04);

  .playback-page {
    padding: 0 !important;
  }

  .playback-layout {
    display: flex;
    height: 100%;
    gap: 16px;
    padding: 16px;
  }

  // ─── Left Panel ──────────────────
  .playback-left {
    width: 300px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
  }

  .left-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    border-radius: @radius-lg;
    box-shadow: @shadow-card;
    border: none;

    :deep(.ant-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 16px;
      overflow: hidden;
    }
  }

  .left-section {
    margin-bottom: 16px;
    flex-shrink: 0;

    &.channel-section {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    &.file-section {
      flex: 0 0 auto;
      max-height: 200px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      border-top: 1px solid #f0f2f5;
      padding-top: 12px;
    }
  }

  .section-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 8px;

    .anticon {
      color: @primary;
    }
  }

  .type-radio {
    width: 100%;

    :deep(.ant-radio-button-wrapper) {
      width: 50%;
      text-align: center;
      font-size: 12px;
      border-radius: 8px !important;
      border: 1px solid #e8ecf1 !important;

      &:first-child {
        border-right: none !important;
        border-radius: 8px 0 0 8px !important;
      }

      &:last-child {
        border-radius: 0 8px 8px 0 !important;
      }

      &.ant-radio-button-wrapper-checked {
        background: @primary;
        border-color: @primary !important;
        color: #fff;
      }
    }
  }

  // ─── Right Panel ──────────────────
  .playback-right {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  :deep(.ant-card) {
    border-radius: @radius-lg;
    box-shadow: @shadow-card;
    border: none;
  }

  .player-card {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;

    :deep(.ant-card-head) {
      flex-shrink: 0;
      border-bottom: 1px solid #f0f2f5;
      padding: 12px 20px;
      min-height: auto;

      .ant-card-head-title {
        padding: 0;
      }
    }

    :deep(.ant-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0;
      min-height: 0;
    }
  }

  .player-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .player-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;

    .anticon {
      color: @primary;
    }
  }

  .player-area {
    flex: 1;
    min-height: 0;
    position: relative;
  }

  .player-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 300px;
    background: linear-gradient(135deg, #f6f8fb 0%, #eef2f8 100%);
    border-radius: 0 0 @radius-lg @radius-lg;

    .placeholder-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, #e0e9ff 0%, #c5d6ff 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;

      .anticon {
        font-size: 36px;
        color: @primary;
      }
    }

    .placeholder-text {
      font-size: 15px;
      font-weight: 500;
      color: #5a6a85;
      margin-bottom: 8px;
    }

    .placeholder-hint {
      font-size: 12px;
      color: #a8b4c0;
    }
  }

  .timeline-card {
    flex-shrink: 0;

    :deep(.ant-card-body) {
      padding: 12px 16px;
    }
  }

  .controls-card {
    flex-shrink: 0;

    :deep(.ant-card-body) {
      padding: 8px 12px;
    }
  }
</style>
