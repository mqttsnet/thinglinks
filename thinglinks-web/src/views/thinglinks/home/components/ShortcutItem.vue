<template>
  <div class="home-shortcut" @click="handleClick">
    <div class="home-shortcut__icon">
      <Icon :icon="item.icon || 'ant-design:folder-outlined'" :size="18" />
    </div>
    <div class="home-shortcut__name">{{ displayName }}</div>
    <span
      v-if="pinnable"
      class="home-shortcut__pin"
      :class="{ 'is-pinned': item.pinned }"
      @click.stop="handleTogglePin"
    >
      <Icon
        :icon="item.pinned ? 'ant-design:star-filled' : 'ant-design:star-outlined'"
        :size="14"
      />
    </span>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import type { ShortcutVO } from '../types';

  interface Props {
    item: ShortcutVO;
    pinnable?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), { pinnable: true });
  const emit = defineEmits<{
    (e: 'click', item: ShortcutVO): void;
    (e: 'togglePin', item: ShortcutVO): void;
  }>();

  const { t, te } = useI18n();

  const displayName = computed(() => {
    const n = props.item.name;
    if (!n) return props.item.path;
    return te(n) ? t(n) : n;
  });

  function handleClick() {
    emit('click', props.item);
  }
  function handleTogglePin() {
    emit('togglePin', props.item);
  }
</script>

<style lang="less" scoped>
  .home-shortcut {
    position: relative;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background-color 0.2s;
    min-height: 44px;

    &:hover {
      background: #f6f9ff;
    }
  }

  .home-shortcut__icon {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: #ecf2ff;
    color: #5d87ff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .home-shortcut__name {
    flex: 1;
    font-size: 13px;
    color: #49536a;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    min-width: 0;
  }

  .home-shortcut__pin {
    color: #b0bac6;
    opacity: 0;
    transition: opacity 0.2s, color 0.2s;
    padding: 4px;

    .home-shortcut:hover & {
      opacity: 1;
    }

    &.is-pinned {
      color: #ffae1f;
      opacity: 1;
    }

    &:hover {
      color: #ffae1f;
    }
  }
</style>
