<template>
  <a-drawer :title="t('video.device.live.tour.title')" :visible="visible" :width="360" @close="$emit('close')">
    <div class="tour-config">
      <div class="tour-config__hint">
        {{ t('video.device.live.tour.description') }}
      </div>

      <!-- 已选通道列表 -->
      <div class="tour-config__list">
        <div v-for="(item, index) in tourChannels" :key="index" class="tour-config__item">
          <span class="tour-config__item-name">{{ item.name }}</span>
          <a-input-number
            v-model:value="item.staySeconds"
            :min="3"
            :max="300"
            size="small"
            :addon-after="t('video.device.live.tour.seconds')"
            style="width: 100px"
          />
          <a-button size="small" danger @click="removeChannel(index)">
            <Icon icon="ant-design:delete-outlined" :size="12" />
          </a-button>
        </div>
        <a-empty v-if="!tourChannels.length" :description="t('video.device.live.tour.emptyHint')" :image-style="{ height: '40px' }" />
      </div>

      <!-- 添加通道 -->
      <a-select
        v-model:value="selectedChannelId"
        :placeholder="t('video.device.live.tour.selectPlaceholder')"
        style="width: 100%; margin-top: 8px"
        show-search
        :filter-option="filterOption"
        @change="addChannel"
      >
        <a-select-option v-for="ch in availableChannels" :key="ch.id" :value="ch.id">
          {{ ch.channelName || ch.channelIdentification }}
        </a-select-option>
      </a-select>

      <!-- 操作按钮 -->
      <div class="tour-config__actions">
        <a-button type="primary" block :disabled="tourChannels.length < 2" @click="handleStart">
          <Icon icon="ant-design:play-circle-outlined" :size="14" />
          {{ t('video.device.live.tour.startButton') }}
        </a-button>
      </div>
    </div>
  </a-drawer>
</template>

<script lang="ts" setup>
  import { ref, type PropType } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { t } = useI18n();

  export interface TourChannel {
    id: string;
    name: string;
    deviceIdentification: string;
    channelIdentification: string;
    staySeconds: number;
  }

  const props = defineProps({
    visible: { type: Boolean, default: false },
    availableChannels: { type: Array as PropType<any[]>, default: () => [] },
  });

  const emit = defineEmits(['close', 'start']);

  const tourChannels = ref<TourChannel[]>([]);
  const selectedChannelId = ref<string>();

  function addChannel(id: string) {
    const ch = props.availableChannels.find((c: any) => c.id === id);
    if (!ch) return;
    if (tourChannels.value.some((t) => t.id === id)) return;
    tourChannels.value.push({
      id: ch.id,
      name: ch.channelName || ch.channelIdentification,
      deviceIdentification: ch.deviceIdentification,
      channelIdentification: ch.channelIdentification,
      staySeconds: 15,
    });
    selectedChannelId.value = undefined;
  }

  function removeChannel(index: number) {
    tourChannels.value.splice(index, 1);
  }

  function handleStart() {
    emit('start', [...tourChannels.value]);
  }

  function filterOption(input: string, option: any) {
    return (option?.children?.[0]?.children || '').toLowerCase().includes(input.toLowerCase());
  }
</script>

<style lang="less" scoped>
  .tour-config {
    &__hint {
      color: #8c97a5;
      font-size: 13px;
      margin-bottom: 12px;
    }

    &__list {
      max-height: 300px;
      overflow-y: auto;
    }

    &__item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 0;
      border-bottom: 1px solid #f0f0f0;

      &-name {
        flex: 1;
        font-size: 13px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    &__actions {
      margin-top: 16px;
    }
  }
</style>
