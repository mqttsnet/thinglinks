<template>
  <Dropdown
    v-model:visible="visible"
    :trigger="['click']"
    placement="bottomRight"
    :overlayClassName="`${prefixCls}__overlay`"
  >
    <span :class="[prefixCls, `${prefixCls}--${theme}`]">
      <span
        :class="`${prefixCls}__logo`"
        :style="{ background: currentGradient.gradient, color: currentGradient.textColor }"
      >
        <Icon icon="ant-design:appstore-outlined" :size="14" />
      </span>
      <span :class="`${prefixCls}__name hidden md:inline`">
        {{ currentName }}
      </span>
      <Icon icon="ant-design:down-outlined" :size="10" :class="`${prefixCls}__arrow`" />
    </span>

    <template #overlay>
      <div :class="`${prefixCls}-menu`">
        <div :class="`${prefixCls}-menu__header`">
          <span :class="`${prefixCls}-menu__title`">{{ t('workbench.dropdown.switchApp') }}</span>
          <span v-if="apps.length" :class="`${prefixCls}-menu__count`">{{ apps.length }}</span>
        </div>

        <div v-if="apps.length > 6" :class="`${prefixCls}-menu__search`">
          <Input
            v-model:value="keyword"
            size="small"
            :placeholder="t('workbench.dropdown.searchPlaceholder')"
            allow-clear
          >
            <template #prefix>
              <Icon icon="ant-design:search-outlined" :size="12" />
            </template>
          </Input>
        </div>

        <div v-if="!filteredApps.length" :class="`${prefixCls}-menu__empty`">
          <Icon icon="ant-design:inbox-outlined" :size="24" />
          <span>{{ t('workbench.apps.empty') }}</span>
        </div>

        <div v-else :class="`${prefixCls}-menu__list`">
          <div
            v-for="app in filteredApps"
            :key="app.id"
            :class="[
              `${prefixCls}-menu__item`,
              { 'is-current': isCurrent(app), 'is-expired': isExpired(app) },
            ]"
            @click="handleClick(app)"
          >
            <span
              :class="`${prefixCls}-menu__item-logo`"
              :style="{
                background: getAppGradient(app.appKey).gradient,
                color: getAppGradient(app.appKey).textColor,
              }"
            >
              <img v-if="app.logoUrl" :src="app.logoUrl" :alt="app.name" />
              <span v-else>{{ initialOf(app) }}</span>
            </span>
            <span :class="`${prefixCls}-menu__item-body`">
              <span :class="`${prefixCls}-menu__item-name-row`">
                <span :class="`${prefixCls}-menu__item-name`">{{ app.name }}</span>
                <span v-if="app.version" :class="`${prefixCls}-menu__item-version`">{{
                  formatVersion(app.version)
                }}</span>
                <span
                  v-if="/^https?:\/\//i.test(app.url || '')"
                  :class="`${prefixCls}-menu__item-external`"
                >
                  <Icon icon="ant-design:export-outlined" :size="11" />
                </span>
              </span>
              <span v-if="app.introduce" :class="`${prefixCls}-menu__item-desc`">{{
                app.introduce
              }}</span>
              <span
                v-if="typeLabel(app) || isCurrent(app) || expireBadge(app)"
                :class="`${prefixCls}-menu__item-footer`"
              >
                <span v-if="typeLabel(app)" :class="`${prefixCls}-menu__item-tag`">
                  {{ typeLabel(app) }}
                </span>
                <span
                  v-if="isCurrent(app)"
                  :class="[`${prefixCls}-menu__item-badge`, 'is-current']"
                >
                  {{ t('workbench.dropdown.current') }}
                </span>
                <span
                  v-else-if="expireBadge(app)"
                  :class="[`${prefixCls}-menu__item-badge`, expireBadge(app)?.cls]"
                >
                  {{ expireBadge(app)?.text }}
                </span>
              </span>
            </span>
          </div>
        </div>
      </div>
    </template>
  </Dropdown>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import dayjs from 'dayjs';
  import { Dropdown, Input } from 'ant-design-vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { useAppLauncher } from '/@/views/thinglinks/home/hooks/useAppLauncher';
  import { useAppSwitchController } from '/@/views/thinglinks/home/hooks/useAppSwitchController';
  import { getAppGradient } from '/@/views/thinglinks/home/data';
  import { findMyApplication } from '/@/api/thinglinks/profile/userInfo';
  import type { DefApplicationResultVO } from '/@/api/devOperation/application/model/defApplicationModel';

  interface Props {
    theme?: string;
  }
  withDefaults(defineProps<Props>(), { theme: 'light' });

  const { t } = useI18n();
  const { prefixCls } = useDesign('header-app-switch');
  const userStore = useUserStore();
  const { launch } = useAppLauncher();
  const { visible } = useAppSwitchController();

  const apps = ref<DefApplicationResultVO[]>([]);
  const keyword = ref('');

  const currentKey = computed(() => userStore.getApplicationKey);
  const currentName = computed(
    () => userStore.getApplicationName || t('workbench.dropdown.switchApp'),
  );
  const currentGradient = computed(() => getAppGradient(currentKey.value));

  const filteredApps = computed(() => {
    const list = apps.value;
    const kw = keyword.value.trim().toLowerCase();
    if (!kw) return list;
    return list.filter(
      (a) =>
        (a.name || '').toLowerCase().includes(kw) ||
        (a.appKey || '').toLowerCase().includes(kw) ||
        (a.introduce || '').toLowerCase().includes(kw),
    );
  });

  function isCurrent(app: DefApplicationResultVO) {
    return !!currentKey.value && app.appKey === currentKey.value;
  }

  function isExpired(app: any): boolean {
    return !!app?.expirationTime && new Date(app.expirationTime).getTime() < Date.now();
  }

  function initialOf(app: DefApplicationResultVO): string {
    return (app.name || app.appKey || 'A').slice(0, 1).toUpperCase();
  }

  function formatVersion(v?: string | number): string {
    const s = String(v ?? '').trim();
    if (!s) return '';
    return /^v/i.test(s) ? s : `v${s}`;
  }

  function typeLabel(app: DefApplicationResultVO): string {
    const ty = String(app.type ?? '');
    if (ty === '10') return t('workbench.apps.typeSelfBuilt');
    if (ty === '20') return t('workbench.apps.typeThirdParty');
    return '';
  }

  function expireBadge(app: DefApplicationResultVO) {
    const exp = app.expirationTime;
    if (!exp) return null;
    const d = dayjs(exp);
    if (!d.isValid()) return null;
    const diffDays = Math.ceil((d.valueOf() - Date.now()) / (24 * 60 * 60 * 1000));
    if (diffDays < 0) return { cls: 'is-expired', text: t('workbench.apps.expired') };
    if (diffDays <= 7) return { cls: 'is-expire-soon', text: t('workbench.apps.expireSoon') };
    return null;
  }

  async function loadApps() {
    try {
      const res: any = await findMyApplication();
      apps.value = Array.isArray(res) ? res : res?.records || [];
    } catch (_) {
      apps.value = [];
    }
  }

  function handleClick(app: DefApplicationResultVO) {
    if (isExpired(app)) return;
    if (isCurrent(app)) {
      visible.value = false;
      return;
    }
    launch(app);
  }

  onMounted(loadApps);
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-header-app-switch';

  .@{prefix-cls} {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background-color 0.2s;
    max-width: 200px;

    &:hover {
      background: rgba(93, 135, 255, 0.08);
    }

    &__logo {
      width: 24px;
      height: 24px;
      border-radius: 7px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    &__name {
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 120px;
    }

    &__arrow {
      color: #8c97a5;
      margin-left: 2px;
    }

    &--dark &__name {
      color: #fff;
    }
  }
</style>

<style lang="less">
  @prefix-cls: ~'@{namespace}-header-app-switch';

  .@{prefix-cls}__overlay {
    .ant-dropdown-menu {
      padding: 0;
      background: transparent;
      box-shadow: none;
    }
  }

  .@{prefix-cls}-menu {
    width: 380px;
    max-width: calc(100vw - 24px);
    padding: 14px 10px 10px;
    background: #fff;
    border-radius: 14px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(93, 135, 255, 0.08);

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px 10px;
    }

    &__title {
      font-size: 12px;
      font-weight: 600;
      color: #8c97a5;
      letter-spacing: 0.3px;
      text-transform: uppercase;
    }

    &__count {
      font-size: 11px;
      font-weight: 600;
      color: #5d87ff;
      padding: 1px 8px;
      background: rgba(93, 135, 255, 0.1);
      border-radius: 9999px;
    }

    &__search {
      padding: 0 8px 10px;
    }

    &__empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 24px;
      color: #b0bac6;
      font-size: 13px;
    }

    &__list {
      max-height: 420px;
      overflow-y: auto;
      padding-right: 2px;

      &::-webkit-scrollbar {
        width: 6px;
      }
      &::-webkit-scrollbar-thumb {
        background: rgba(140, 151, 165, 0.2);
        border-radius: 9999px;
      }
    }

    &__item {
      position: relative;
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px 10px;
      border-radius: 10px;
      cursor: pointer;
      transition: background-color 0.15s;

      & + & {
        margin-top: 2px;
      }

      &:hover {
        background: rgba(93, 135, 255, 0.06);
      }

      &.is-current {
        background: linear-gradient(
          135deg,
          rgba(93, 135, 255, 0.08) 0%,
          rgba(124, 92, 252, 0.04) 100%
        );
      }

      &.is-expired {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &-logo {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 16px;
        flex-shrink: 0;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 10px;
        }
      }

      &-body {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 3px;
      }

      &-name-row {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
      }

      &-name {
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        min-width: 0;
      }

      &-version {
        font-size: 11px;
        color: #8c97a5;
        font-family: 'SFMono-Regular', Consolas, monospace;
        flex-shrink: 0;
      }

      &-external {
        color: #b0bac6;
        display: inline-flex;
        flex-shrink: 0;
      }

      &-desc {
        font-size: 12px;
        color: #8c97a5;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      &-footer {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        margin-top: 2px;
      }

      &-tag {
        font-size: 11px;
        line-height: 18px;
        padding: 0 8px;
        border-radius: 9999px;
        background: rgba(93, 135, 255, 0.1);
        color: #5d87ff;
        font-weight: 500;
      }

      &-badge {
        font-size: 11px;
        line-height: 18px;
        padding: 0 8px;
        border-radius: 9999px;
        font-weight: 600;

        &.is-current {
          background: #5d87ff;
          color: #fff;
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
    }
  }
</style>
