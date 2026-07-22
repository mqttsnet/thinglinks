<template>
  <div class="channel-selector">
    <!-- Search -->
    <div class="channel-search">
      <a-input
        v-model:value="searchText"
        :placeholder="t('video.record.playback.searchChannel')"
        allow-clear
        size="small"
      >
        <template #prefix>
          <SearchOutlined style="color: #bfbfbf" />
        </template>
      </a-input>
    </div>

    <!-- Channel list -->
    <div class="channel-list">
      <a-spin :spinning="loading" size="small">
        <div v-if="!loading && filteredChannels.length === 0" class="channel-empty">
          <InboxOutlined style="font-size: 28px; color: #d9d9d9" />
          <span>{{ t('video.record.playback.noChannel') }}</span>
        </div>
        <div
          v-for="channel in filteredChannels"
          :key="channel.channelIdentification"
          class="channel-item"
          :class="{ active: selectedChannel?.channelIdentification === channel.channelIdentification }"
          @click="handleSelect(channel)"
        >
          <div class="channel-info">
            <div class="channel-name">
              <VideoCameraOutlined class="channel-icon" />
              <span>{{ channel.channelName || channel.channelIdentification }}</span>
            </div>
            <div class="channel-id">{{ channel.channelIdentification }}</div>
          </div>
          <div class="channel-status">
            <Badge :status="isTruthyStatus(channel.onlineStatus) ? 'success' : 'default'" />
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { SearchOutlined, VideoCameraOutlined, InboxOutlined } from '@ant-design/icons-vue';
  import { Badge } from 'ant-design-vue';
  import { query as queryChannels } from '/@/api/video/device/channel';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';
  import type { VideoDeviceChannelResultVO } from '/@/api/video/device/model/channelModel';

  const props = defineProps<{
    deviceIdentification?: string;
  }>();

  const emit = defineEmits<{
    (e: 'select', channel: VideoDeviceChannelResultVO): void;
  }>();

  const { t } = useI18n();
  const searchText = ref('');
  const loading = ref(false);
  const channels = ref<VideoDeviceChannelResultVO[]>([]);
  const selectedChannel = ref<VideoDeviceChannelResultVO | null>(null);

  const filteredChannels = computed(() => {
    const kw = searchText.value.toLowerCase().trim();
    if (!kw) return channels.value;
    return channels.value.filter(
      (c) =>
        c.channelName?.toLowerCase().includes(kw) ||
        c.channelIdentification?.toLowerCase().includes(kw),
    );
  });

  function handleSelect(channel: VideoDeviceChannelResultVO) {
    selectedChannel.value = channel;
    emit('select', channel);
  }

  async function loadChannels() {
    loading.value = true;
    try {
      const params: any = {};
      if (props.deviceIdentification) {
        params.deviceIdentification = props.deviceIdentification;
      }
      const res = await queryChannels(params);
      channels.value = res || [];
    } catch (e) {
      channels.value = [];
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    loadChannels();
  });

  defineExpose({ loadChannels, selectedChannel });
</script>

<style lang="less" scoped>
  .channel-selector {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .channel-search {
    padding: 0 0 10px;
    flex-shrink: 0;

    :deep(.ant-input-affix-wrapper) {
      border-radius: 8px;
      border-color: #e8ecf1;
      background: #f8fafc;

      &:hover,
      &:focus-within {
        border-color: #5d87ff;
        background: #fff;
      }
    }
  }

  .channel-list {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 2px;
    }
  }

  .channel-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    gap: 8px;
    color: #bfbfbf;
    font-size: 13px;
  }

  .channel-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
    margin-bottom: 4px;

    &:hover {
      background: #f0f4ff;
    }

    &.active {
      background: linear-gradient(135deg, #5d87ff 0%, #7c9fff 100%);
      box-shadow: 0 2px 8px rgba(93, 135, 255, 0.3);

      .channel-name,
      .channel-id {
        color: #fff;
      }

      .channel-icon {
        color: rgba(255, 255, 255, 0.8);
      }

      :deep(.ant-badge-status-dot) {
        box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.8);
      }
    }
  }

  .channel-info {
    flex: 1;
    min-width: 0;
  }

  .channel-name {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 500;
    color: #2a3547;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    .channel-icon {
      flex-shrink: 0;
      color: #8c97a5;
    }
  }

  .channel-id {
    font-size: 11px;
    color: #8c97a5;
    margin-top: 2px;
    padding-left: 20px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
