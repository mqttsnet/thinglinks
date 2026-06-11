<template>
  <div class="home-log-item">
    <div class="home-log-item__icon" :class="statusClass">
      <Icon :icon="iconName" :size="14" />
    </div>
    <div class="home-log-item__body">
      <div class="home-log-item__title">
        <span class="home-log-item__action">{{ item.title }}</span>
        <span v-if="item.action" class="home-log-item__action-sub">· {{ item.action }}</span>
      </div>
      <div class="home-log-item__meta">
        <span v-if="item.ip" class="home-log-item__meta-item">
          <Icon icon="ant-design:global-outlined" :size="11" />
          {{ item.ip }}
        </span>
        <span v-if="item.userAgent" class="home-log-item__meta-item">
          <Icon icon="ant-design:chrome-outlined" :size="11" />
          {{ item.userAgent }}
        </span>
      </div>
    </div>
    <div class="home-log-item__time">{{ formattedTime }}</div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import dayjs from 'dayjs';
  import relativeTime from 'dayjs/plugin/relativeTime';
  import { Icon } from '/@/components/Icon';
  import type { LogItemVO } from '../types';

  dayjs.extend(relativeTime);

  interface Props {
    item: LogItemVO;
  }
  const props = defineProps<Props>();

  const statusClass = computed(() => {
    switch (props.item.status) {
      case 'failed':
        return 'is-danger';
      case 'warning':
        return 'is-warning';
      default:
        return 'is-success';
    }
  });

  const iconName = computed(() => {
    switch (props.item.status) {
      case 'failed':
        return 'ant-design:close-circle-outlined';
      case 'warning':
        return 'ant-design:warning-outlined';
      default:
        return 'ant-design:check-circle-outlined';
    }
  });

  const formattedTime = computed(() => {
    if (!props.item.createdTime) return '';
    const d = dayjs(props.item.createdTime);
    if (!d.isValid()) return '';
    const diff = Date.now() - d.valueOf();
    if (diff < 7 * 24 * 60 * 60 * 1000) return d.fromNow();
    return d.format('MM-DD HH:mm');
  });
</script>

<style lang="less" scoped>
  .home-log-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    border-radius: 10px;
    transition: background 0.2s;

    &:hover {
      background: #f6f9ff;
    }
  }

  .home-log-item__icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.is-success {
      background: #e6f9f4;
      color: #0bb783;
    }
    &.is-warning {
      background: #fff5e0;
      color: #c98a10;
    }
    &.is-danger {
      background: #ffeae3;
      color: #d04a2d;
    }
  }

  .home-log-item__body {
    flex: 1;
    min-width: 0;
  }

  .home-log-item__title {
    font-size: 13px;
    color: #49536a;
    line-height: 1.35;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .home-log-item__action {
    font-weight: 500;
  }

  .home-log-item__action-sub {
    color: #8c97a5;
    font-weight: 400;
    margin-left: 4px;
  }

  .home-log-item__meta {
    display: flex;
    gap: 10px;
    font-size: 11px;
    color: #b0bac6;
    margin-top: 2px;
  }

  .home-log-item__meta-item {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }

  .home-log-item__time {
    font-size: 11px;
    color: #b0bac6;
    white-space: nowrap;
    flex-shrink: 0;
    padding-top: 8px;
  }
</style>
