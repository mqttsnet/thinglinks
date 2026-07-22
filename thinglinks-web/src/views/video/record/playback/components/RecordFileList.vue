<template>
  <div class="record-file-list">
    <div class="file-list-header">
      <span class="file-list-title">{{ t('video.record.playback.recordSegments') }}</span>
      <span class="file-list-count" v-if="files.length">{{ files.length }}</span>
    </div>
    <a-spin :spinning="loading" size="small">
      <div v-if="!loading && files.length === 0" class="file-list-empty">
        <FolderOpenOutlined style="font-size: 24px; color: #d9d9d9" />
        <span>{{ t('video.record.playback.noRecord') }}</span>
      </div>
      <div class="file-list-scroll" v-else>
        <div
          v-for="(file, idx) in files"
          :key="file.id || idx"
          class="file-item"
          :class="{ active: activeFileId === file.id }"
          @click="handleClick(file)"
        >
          <div class="file-item-left">
            <div class="file-item-icon">
              <PlayCircleOutlined v-if="activeFileId === file.id" />
              <VideoCameraOutlined v-else />
            </div>
            <div class="file-item-info">
              <div class="file-item-time">
                {{ formatFileTime(file.startTime) }} - {{ formatFileTime(file.endTime) }}
              </div>
              <div class="file-item-meta">
                <span v-if="file.duration">{{ formatDuration(file.duration) }}</span>
                <span v-if="file.fileSize" class="file-item-sep">{{ formatFileSize(file.fileSize) }}</span>
                <span v-if="file.fileFormat" class="file-item-format">{{ file.echoMap?.fileFormat || file.fileFormat }}</span>
              </div>
            </div>
          </div>
          <div class="file-item-actions" v-if="file.id">
            <a-tooltip :title="t('video.record.playback.download')">
              <a-button
                type="text"
                size="small"
                class="action-btn"
                @click.stop="$emit('download', file)"
              >
                <DownloadOutlined />
              </a-button>
            </a-tooltip>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '/@/hooks/web/useI18n';
  import {
    PlayCircleOutlined,
    VideoCameraOutlined,
    FolderOpenOutlined,
    DownloadOutlined,
  } from '@ant-design/icons-vue';
  import type { VideoRecordFileResultVO } from '/@/api/video/record/model/fileModel';

  defineProps<{
    files: VideoRecordFileResultVO[];
    activeFileId?: string;
    loading?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'select', file: VideoRecordFileResultVO): void;
    (e: 'download', file: VideoRecordFileResultVO): void;
  }>();

  const { t } = useI18n();

  function handleClick(file: VideoRecordFileResultVO) {
    emit('select', file);
  }

  function formatFileTime(time?: string): string {
    if (!time) return '--:--';
    // Extract HH:mm:ss from datetime string
    const match = time.match(/(\d{2}:\d{2}:\d{2})/);
    if (match) return match[1];
    // Try parsing as date
    try {
      const d = new Date(time);
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
    } catch {
      return time;
    }
  }

  function formatDuration(seconds: number): string {
    if (seconds < 60) return `${seconds}s`;
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    if (m < 60) return s > 0 ? `${m}m${s}s` : `${m}m`;
    const h = Math.floor(m / 60);
    const rm = m % 60;
    return rm > 0 ? `${h}h${rm}m` : `${h}h`;
  }

  function formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes}B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)}GB`;
  }
</script>

<style lang="less" scoped>
  .record-file-list {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .file-list-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 10px;
    flex-shrink: 0;

    .file-list-title {
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;
    }

    .file-list-count {
      font-size: 11px;
      background: #5d87ff;
      color: #fff;
      padding: 0 6px;
      border-radius: 10px;
      line-height: 18px;
      font-weight: 600;
    }
  }

  .file-list-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 30px 0;
    gap: 8px;
    color: #bfbfbf;
    font-size: 12px;
  }

  .file-list-scroll {
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

  .file-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    margin-bottom: 3px;
    border: 1px solid transparent;

    &:hover {
      background: #f6f8fb;
      border-color: #e8ecf1;
    }

    &.active {
      background: #f0f4ff;
      border-color: #d4dfff;

      .file-item-icon {
        color: #5d87ff;
      }

      .file-item-time {
        color: #5d87ff;
        font-weight: 600;
      }
    }
  }

  .file-item-left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    min-width: 0;
  }

  .file-item-icon {
    font-size: 16px;
    color: #8c97a5;
    flex-shrink: 0;
  }

  .file-item-info {
    min-width: 0;
  }

  .file-item-time {
    font-size: 13px;
    font-weight: 500;
    color: #2a3547;
    font-family: 'SF Mono', 'Fira Code', monospace;
  }

  .file-item-meta {
    font-size: 11px;
    color: #a8b4c0;
    margin-top: 2px;
    display: flex;
    align-items: center;
    gap: 4px;

    .file-item-sep::before {
      content: '·';
      margin-right: 4px;
    }

    .file-item-format {
      background: #f0f2f5;
      padding: 0 4px;
      border-radius: 3px;
      text-transform: uppercase;
      font-size: 10px;
      font-weight: 600;
    }
  }

  .action-btn {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    color: #8c97a5;
    font-size: 13px;

    &:hover {
      color: #5d87ff;
      background: #e0e9ff;
    }
  }
</style>
