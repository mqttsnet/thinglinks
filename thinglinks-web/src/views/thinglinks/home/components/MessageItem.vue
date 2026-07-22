<template>
  <div
    class="home-message-item"
    :class="{ 'is-unread': item.unread }"
    @click="handleClick"
  >
    <span class="home-message-item__dot" :class="typeClass" />
    <div class="home-message-item__body">
      <div class="home-message-item__title">{{ item.title }}</div>
      <div class="home-message-item__time">{{ formattedTime }}</div>
    </div>
    <a-badge v-if="item.unread" color="#5d87ff" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import dayjs from 'dayjs';
  import relativeTime from 'dayjs/plugin/relativeTime';
  import type { MessageItemVO } from '../types';

  dayjs.extend(relativeTime);

  interface Props {
    item: MessageItemVO;
  }
  const props = defineProps<Props>();
  const emit = defineEmits<{ (e: 'click', item: MessageItemVO): void }>();

  const typeClass = computed(() => {
    switch (props.item.type) {
      case 'warning':
        return 'is-warning';
      case 'security':
        return 'is-danger';
      case 'announcement':
        return 'is-info';
      default:
        return 'is-primary';
    }
  });

  const formattedTime = computed(() => {
    if (!props.item.createdTime) return '';
    const d = dayjs(props.item.createdTime);
    if (!d.isValid()) return '';
    const diff = Date.now() - d.valueOf();
    if (diff < 7 * 24 * 60 * 60 * 1000) return d.fromNow();
    return d.format('YYYY-MM-DD HH:mm');
  });

  function handleClick() {
    emit('click', props.item);
  }
</script>

<style lang="less" scoped>
  .home-message-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f6f9ff;
    }

    &.is-unread .home-message-item__title {
      font-weight: 600;
      color: #2a3547;
    }
  }

  .home-message-item__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;

    &.is-primary { background: #5d87ff; }
    &.is-info { background: #49beff; }
    &.is-warning { background: #ffae1f; }
    &.is-danger { background: #fa896b; }
  }

  .home-message-item__body {
    flex: 1;
    min-width: 0;
  }

  .home-message-item__title {
    font-size: 13px;
    color: #49536a;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .home-message-item__time {
    font-size: 11px;
    color: #b0bac6;
    margin-top: 2px;
  }
</style>
