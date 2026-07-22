<template>
  <div class="home-platform-tile" @click="handleClick">
    <div class="home-platform-tile__icon" :style="{ background: iconBg, color: item.color }">
      <Icon :icon="item.icon" :size="22" />
    </div>
    <div class="home-platform-tile__body">
      <div class="home-platform-tile__title">{{ t(item.title) }}</div>
      <div v-if="subtitleText" class="home-platform-tile__subtitle">{{ subtitleText }}</div>
    </div>
    <span v-if="item.external" class="home-platform-tile__external">
      <Icon icon="ant-design:export-outlined" :size="12" />
    </span>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useLocale } from '/@/locales/useLocale';
  import { getProductEditionName, productInfo } from '/@/settings/productSetting';
  import type { PlatformTileVO } from '../types';

  interface Props {
    item: PlatformTileVO;
  }
  const props = defineProps<Props>();
  const router = useRouter();
  const { t } = useI18n();
  const { getLocale } = useLocale();

  const iconBg = computed(() => {
    const c = props.item.color;
    return `${c}1a`;
  });

  const subtitleText = computed(() => {
    if (props.item.dynamicSubtitle === 'version') {
      const edition = getProductEditionName(getLocale.value);
      return `${productInfo.componentName} v${productInfo.componentVersion} · ${edition}`;
    }
    return props.item.subtitle ? t(props.item.subtitle) : '';
  });

  function handleClick() {
    const url = props.item.url;
    if (!url) return;
    if (/^https?:\/\//i.test(url)) {
      window.open(url, '_blank', 'noopener,noreferrer');
    } else {
      router.push(url);
    }
  }
</script>

<style lang="less" scoped>
  .home-platform-tile {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 18px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
    cursor: pointer;
    transition: transform 0.25s ease, box-shadow 0.25s ease;
    height: 100%;
    min-height: 76px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 6px rgba(20, 37, 66, 0.04), 0 12px 36px rgba(93, 135, 255, 0.1);
    }
  }

  .home-platform-tile__icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .home-platform-tile__body {
    flex: 1;
    min-width: 0;
  }

  .home-platform-tile__title {
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    line-height: 1.3;
  }

  .home-platform-tile__subtitle {
    font-size: 12px;
    color: #8c97a5;
    margin-top: 3px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .home-platform-tile__external {
    color: #b0bac6;
    display: flex;
    transition: color 0.2s;

    .home-platform-tile:hover & {
      color: #5d87ff;
    }
  }

</style>
