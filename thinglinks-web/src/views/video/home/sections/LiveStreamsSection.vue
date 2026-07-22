<template>
  <section class="video-streams">
    <div class="video-section-header">
      <div class="video-section-title">
        <span class="video-section-title__bar" />
        <span>{{ t('video.dashboard.stats.activeStreamList') }}</span>
        <span v-if="streams.length" class="video-section-title__count">{{ streams.length }}</span>
      </div>
      <a-button type="link" size="small" @click="gotoStats">
        {{ t('common.title.details') }}
        <Icon icon="ant-design:right-outlined" :size="10" />
      </a-button>
    </div>

    <div class="video-streams__panel">
      <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
      <a-empty
        v-else-if="!streams.length"
        :description="t('workbench.messages.empty')"
        :image-style="{ height: '50px' }"
      />
      <a-table
        v-else
        :dataSource="streamsTop"
        :columns="columns"
        :pagination="false"
        :show-header="true"
        size="small"
        row-key="callId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'app'">
            <span class="video-streams__app">{{ record.app || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'streamId'">
            <span class="video-streams__mono">{{ record.streamId || record.stream || '-' }}</span>
          </template>
        </template>
      </a-table>
    </div>
  </section>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';

  interface Props {
    streams: any[];
    loading?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), { loading: false });

  const router = useRouter();
  const { t } = useI18n();

  const streamsTop = computed(() => (props.streams || []).slice(0, 8));

  const columns = computed(() => [
    { key: 'app', title: t('video.dashboard.stats.app'), dataIndex: 'app', width: 120 },
    { key: 'streamId', title: t('video.dashboard.stats.streamId'), dataIndex: 'stream', ellipsis: true },
    { key: 'server', title: t('video.dashboard.stats.server'), dataIndex: 'serverName', width: 140 },
  ]);

  function gotoStats() {
    router.push('/dashboard/stats');
  }
</script>

<style lang="less" scoped>
  .video-streams {
    margin-bottom: 20px;
  }

  .video-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;
  }

  .video-section-title {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
  }

  .video-section-title__bar {
    display: none;
  }

  .video-section-title__count {
    font-size: 12px;
    font-weight: 400;
    color: #8c97a5;
  }

  .video-streams__panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px 22px;
    min-height: 180px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .video-streams__app {
    font-weight: 500;
    color: #49536a;
  }

  .video-streams__mono {
    font-family: 'SFMono-Regular', Consolas, monospace;
    font-size: 12px;
    color: #8c97a5;
  }
</style>
