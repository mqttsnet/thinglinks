<template>
  <div
    v-if="app"
    class="home-app-card"
    :class="{ 'is-current': isCurrent, 'is-expired': isExpired }"
    @click="handleClick"
  >
    <div class="home-app-card__logo" :style="{ background: gradient.gradient, color: gradient.textColor }">
      <img v-if="app.logoUrl" :src="app.logoUrl" :alt="app.name" @error="logoLoaded = false" v-show="logoLoaded" />
      <span v-else-if="!logoLoaded || !app.logoUrl" class="home-app-card__logo-fallback">
        {{ initial }}
      </span>
    </div>
    <div class="home-app-card__body">
      <div class="home-app-card__header">
        <span class="home-app-card__name">{{ app.name }}</span>
        <span v-if="isExternal" class="home-app-card__external" :title="t('workbench.apps.newTabTip')">
          <Icon icon="ant-design:export-outlined" :size="12" />
        </span>
      </div>
      <div class="home-app-card__meta">
        <span v-if="versionLabel" class="home-app-card__version">{{ versionLabel }}</span>
        <a-tag v-if="typeLabel" color="blue" class="home-app-card__type">{{ typeLabel }}</a-tag>
      </div>
      <div v-if="app.introduce" class="home-app-card__desc">{{ app.introduce }}</div>
      <div v-if="showLicense && licenseInfo" class="home-app-card__license">
        <div class="home-app-card__license-row">
          <Icon icon="ant-design:safety-certificate-outlined" :size="12" />
          <span class="home-app-card__license-label">{{ t('thinglinks.home.application.licenseGrantedAt') }}</span>
          <span class="home-app-card__license-value">{{ licenseInfo.grantedAt }}</span>
        </div>
        <div class="home-app-card__license-row">
          <Icon icon="ant-design:clock-circle-outlined" :size="12" />
          <span class="home-app-card__license-label">{{ t('thinglinks.home.application.licenseExpireAt') }}</span>
          <span
            class="home-app-card__license-value"
            :class="{
              'is-expired': licenseInfo.isExpired,
              'is-soon': licenseInfo.isExpiringSoon,
              'is-permanent': licenseInfo.isPermanent,
            }"
          >
            {{ licenseInfo.expireAt }}
          </span>
        </div>
      </div>
      <div class="home-app-card__footer">
        <span v-if="isCurrent" class="home-app-card__badge is-current">
          {{ t('workbench.apps.current') }}
        </span>
        <span v-if="expireBadge" class="home-app-card__badge" :class="expireBadge.cls">
          {{ expireBadge.text }}
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import dayjs from 'dayjs';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useUserStore } from '/@/store/modules/user';
  import { getAppGradient } from '../data';
  import type { DefApplicationResultVO } from '/@/api/devOperation/application/model/defApplicationModel';

  interface Props {
    app: DefApplicationResultVO;
    /** 是否展示授权信息（开通时间 / 到期时间），通常在"我的应用"区域显示 */
    showLicense?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), {
    showLicense: false,
  });
  const emit = defineEmits<{ (e: 'launch', app: DefApplicationResultVO): void }>();

  const { t } = useI18n();
  const userStore = useUserStore();

  const logoLoaded = ref(true);

  // props.app 在 v-for 中可能短暂为 undefined（数据加载中 / keep-alive 重建组件瞬态），
  // 所有 computed 都要做空值守卫，避免 reading 'appKey' on undefined 抛错并引发 Vue 渲染连锁失败。
  const gradient = computed(() => getAppGradient(props.app?.appKey || ''));

  const isCurrent = computed(() => !!props.app && userStore.getApplicationKey === props.app.appKey);

  const isExternal = computed(() => /^https?:\/\//i.test(props.app?.url || ''));

  const initial = computed(() =>
    (props.app?.name || props.app?.appKey || 'A').slice(0, 1).toUpperCase(),
  );

  // 版本号统一展示规则：
  //  - 后端可能存为 "1.0.0" / "v1.0.0" / "V1.0" / 空字符串等多种形态；
  //  - 卡片统一以小写 "v" 前缀展示，已包含 "v"/"V" 时去重避免变成 "vv1.0.0"。
  const versionLabel = computed(() => {
    const raw = (props.app?.version || '').trim();
    if (!raw) return '';
    return /^v/i.test(raw) ? `v${raw.slice(1)}` : `v${raw}`;
  });

  const typeLabel = computed(() => {
    const t10 = props.app.type;
    if (!t10) return '';
    if (String(t10) === '10') return t('workbench.apps.typeSelfBuilt');
    if (String(t10) === '20') return t('workbench.apps.typeThirdParty');
    return '';
  });

  const expireInfo = computed(() => {
    const exp = props.app.expirationTime;
    if (!exp) return null;
    const d = dayjs(exp);
    if (!d.isValid()) return null;
    const diffMs = d.valueOf() - Date.now();
    const diffDays = Math.ceil(diffMs / (24 * 60 * 60 * 1000));
    return { diffDays, date: d };
  });

  const isExpired = computed(() => !!expireInfo.value && expireInfo.value.diffDays < 0);

  const expireBadge = computed(() => {
    const info = expireInfo.value;
    if (!info) return null;
    if (info.diffDays < 0) {
      return { cls: 'is-expired', text: t('workbench.apps.expired') };
    }
    if (info.diffDays <= 7) {
      return { cls: 'is-expire-soon', text: t('workbench.apps.expireSoon') };
    }
    return null;
  });

  // 授权信息：开通时间 + 到期时间。后端 expirationTime 为空表示长期有效。
  const licenseInfo = computed(() => {
    if (!props.app) return null;
    const created = props.app.createdTime;
    const expiration = props.app.expirationTime;
    const grantedAt = created && dayjs(created).isValid()
      ? dayjs(created).format('YYYY-MM-DD')
      : '-';

    let expireAt = t('thinglinks.home.application.licensePermanent');
    let isPermanent = true;
    let isExpired = false;
    let isExpiringSoon = false;

    if (expiration) {
      const d = dayjs(expiration);
      if (d.isValid()) {
        expireAt = d.format('YYYY-MM-DD');
        isPermanent = false;
        const diffDays = Math.ceil((d.valueOf() - Date.now()) / (24 * 60 * 60 * 1000));
        isExpired = diffDays < 0;
        isExpiringSoon = diffDays >= 0 && diffDays <= 7;
      }
    }

    return { grantedAt, expireAt, isPermanent, isExpired, isExpiringSoon };
  });

  function handleClick() {
    if (isExpired.value) return;
    emit('launch', props.app);
  }
</script>

<style lang="less" scoped>
  .home-app-card {
    position: relative;
    display: flex;
    gap: 14px;
    padding: 16px 18px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
    cursor: pointer;
    transition: transform 0.25s ease, box-shadow 0.25s ease;
    height: 100%;
    min-height: 112px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 6px rgba(20, 37, 66, 0.04), 0 12px 36px rgba(93, 135, 255, 0.1);
    }

    &.is-current {
      box-shadow: 0 0 0 2px #5d87ff, 0 6px 24px rgba(93, 135, 255, 0.14);

      &:hover {
        box-shadow: 0 0 0 2px #5d87ff, 0 12px 36px rgba(93, 135, 255, 0.2);
      }
    }

    &.is-expired {
      opacity: 0.55;
      cursor: not-allowed;

      &:hover {
        transform: none;
        box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
      }
    }
  }

  .home-app-card__logo {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 18px;
    letter-spacing: -0.2px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
    }
  }

  .home-app-card__logo-fallback {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  .home-app-card__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .home-app-card__header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
  }

  .home-app-card__name {
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    line-height: 1.3;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
    min-width: 0;
  }

  .home-app-card__external {
    color: #8c97a5;
    display: flex;
    flex-shrink: 0;
  }

  .home-app-card__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  .home-app-card__version {
    font-size: 11px;
    color: #8c97a5;
    font-family: 'SFMono-Regular', Consolas, monospace;
  }

  .home-app-card__type {
    margin: 0;
    font-size: 11px;
    line-height: 18px;
    padding: 0 6px;
  }

  .home-app-card__desc {
    font-size: 12px;
    color: #8c97a5;
    line-height: 1.45;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .home-app-card__license {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 8px;
    padding: 8px 10px;
    background: rgba(93, 135, 255, 0.05);
    border-radius: 6px;
    border-left: 2px solid rgba(93, 135, 255, 0.4);
  }

  .home-app-card__license-row {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 11px;
    color: #6b7a8f;
    line-height: 1.4;
  }

  .home-app-card__license-label {
    color: #8c97a5;
    flex-shrink: 0;
  }

  .home-app-card__license-value {
    color: #2a3547;
    font-family: 'SFMono-Regular', Consolas, monospace;
    font-weight: 500;

    &.is-permanent {
      color: #11926f;
    }
    &.is-soon {
      color: #c98a10;
    }
    &.is-expired {
      color: #d03b5b;
    }
  }

  .home-app-card__footer {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: auto;
  }

  .home-app-card__badge {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    font-size: 11px;
    font-weight: 600;
    border-radius: 9999px;

    &.is-current {
      background: rgba(93, 135, 255, 0.12);
      color: #5d87ff;
    }
    &.is-expire-soon {
      background: rgba(255, 174, 31, 0.14);
      color: #c98a10;
    }
    &.is-expired {
      background: rgba(250, 92, 124, 0.12);
      color: #d03b5b;
    }
  }
</style>
