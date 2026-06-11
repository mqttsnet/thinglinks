<template>
  <div class="timeline-item" :class="statusClass">
    <div class="timeline-item__axis">
      <span class="timeline-item__dot" />
      <span v-if="!isLast" class="timeline-item__line" />
    </div>
    <div class="timeline-item__body">
      <div class="timeline-item__row">
        <span class="timeline-item__time">{{ formattedTime }}</span>
        <span class="timeline-item__desc">{{ item.title }}</span>
      </div>
      <div v-if="metaText" class="timeline-item__meta">{{ metaText }}</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import dayjs from 'dayjs';
  import type { LogItemVO } from '../types';

  interface Props {
    item: LogItemVO;
    isLast?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), { isLast: false });

  const statusClass = computed(() => {
    switch (props.item.status) {
      case 'failed':
        return 'is-danger';
      case 'warning':
        return 'is-warning';
      default:
        return 'is-primary';
    }
  });

  const formattedTime = computed(() => {
    if (!props.item.createdTime) return '-';
    const d = dayjs(props.item.createdTime);
    if (!d.isValid()) return '-';
    const today = dayjs().startOf('day');
    if (d.isAfter(today)) return d.format('HH:mm');
    return d.format('MM-DD HH:mm');
  });

  const metaText = computed(() => {
    const parts = [props.item.ip, props.item.userAgent].filter(Boolean);
    return parts.join(' · ');
  });
</script>

<style lang="less" scoped>
  .timeline-item {
    position: relative;
    display: flex;
    gap: 14px;
    padding: 2px 4px 2px 4px;
    min-height: 56px;
  }

  .timeline-item__axis {
    position: relative;
    width: 12px;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    padding-top: 6px;
  }

  .timeline-item__dot {
    position: relative;
    z-index: 1;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
    box-shadow: 0 0 0 3px #fff, 0 0 0 4px currentColor;

    .is-primary & { color: #5d87ff; background: #5d87ff; }
    .is-warning & { color: #ffae1f; background: #ffae1f; }
    .is-danger & { color: #fa896b; background: #fa896b; }
  }

  .timeline-item__line {
    position: absolute;
    top: 20px;
    bottom: -10px;
    left: 50%;
    width: 1px;
    background: repeating-linear-gradient(
      to bottom,
      #e5e9f2 0 4px,
      transparent 4px 8px
    );
    transform: translateX(-50%);
  }

  .timeline-item__body {
    flex: 1;
    min-width: 0;
    padding-bottom: 14px;
  }

  .timeline-item__row {
    display: flex;
    align-items: baseline;
    gap: 12px;
    flex-wrap: wrap;
  }

  .timeline-item__time {
    font-family: 'SFMono-Regular', Consolas, monospace;
    font-size: 11px;
    color: #8c97a5;
    font-weight: 500;
    letter-spacing: 0.1px;
    flex-shrink: 0;
    min-width: 60px;
  }

  .timeline-item__desc {
    font-size: 13px;
    color: #2a3547;
    font-weight: 500;
    line-height: 1.4;
    flex: 1;
    min-width: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .timeline-item__meta {
    font-size: 11px;
    color: #b0bac6;
    margin-top: 3px;
    margin-left: 72px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
