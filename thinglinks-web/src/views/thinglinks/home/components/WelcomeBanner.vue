<template>
  <div class="welcome-banner">
    <!-- 右侧装饰层：几何圆 + 抽象应用网格 -->
    <svg class="welcome-banner__deco" viewBox="0 0 200 160" aria-hidden="true">
      <circle cx="170" cy="40" r="56" fill="rgba(255,255,255,0.18)" />
      <circle cx="145" cy="115" r="34" fill="rgba(255,255,255,0.12)" />
      <circle cx="190" cy="130" r="18" fill="rgba(255,255,255,0.22)" />
      <rect x="110" y="20" width="14" height="14" rx="3" fill="rgba(255,255,255,0.35)" />
      <rect x="128" y="20" width="14" height="14" rx="3" fill="rgba(255,255,255,0.28)" />
      <rect x="110" y="38" width="14" height="14" rx="3" fill="rgba(255,255,255,0.22)" />
      <rect x="128" y="38" width="14" height="14" rx="3" fill="rgba(255,255,255,0.32)" />
    </svg>

    <div class="welcome-banner__body">
      <div class="welcome-banner__title">{{ t('workbench.banner.title') }}</div>
      <div class="welcome-banner__subtitle">
        {{ t('workbench.banner.subtitle', { count: appsCount }) }}
      </div>
      <button class="welcome-banner__cta" type="button" @click="handleClick">
        {{ t('workbench.banner.cta') }}
        <Icon icon="ant-design:arrow-right-outlined" :size="12" />
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';

  interface Props {
    appsCount?: number;
  }
  withDefaults(defineProps<Props>(), { appsCount: 0 });
  const emit = defineEmits<{ (e: 'click'): void }>();

  const { t } = useI18n();

  function handleClick() {
    emit('click');
  }
</script>

<style lang="less" scoped>
  .welcome-banner {
    position: relative;
    padding: 28px 32px;
    background: linear-gradient(135deg, #ff8a65 0%, #fa896b 55%, #ff7043 100%);
    border-radius: 12px;
    color: #fff;
    overflow: hidden;
    height: 100%;
    min-height: 180px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    box-shadow: 0 1px 3px rgba(250, 137, 107, 0.12), 0 10px 32px rgba(250, 137, 107, 0.22);
  }

  .welcome-banner__deco {
    position: absolute;
    top: 10px;
    right: 0;
    width: 180px;
    height: 150px;
    pointer-events: none;
  }

  .welcome-banner__body {
    position: relative;
    z-index: 1;
    max-width: calc(100% - 140px);
  }

  .welcome-banner__title {
    font-size: 22px;
    font-weight: 700;
    letter-spacing: -0.3px;
    line-height: 1.25;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .welcome-banner__subtitle {
    font-size: 13px;
    font-weight: 500;
    opacity: 0.92;
    margin-top: 6px;
    line-height: 1.4;
  }

  .welcome-banner__cta {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin-top: 18px;
    padding: 9px 18px;
    background: #fff;
    color: #e6543a;
    border: none;
    border-radius: 9999px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    &:hover {
      transform: translateX(2px);
      box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
    }
  }

  @media (max-width: 992px) {
    .welcome-banner {
      padding: 22px 24px;
      min-height: 140px;
    }
    .welcome-banner__title {
      font-size: 18px;
    }
  }
</style>
