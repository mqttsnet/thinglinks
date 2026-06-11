<template>
  <PageWrapper dense contentClass="my-application-page">
    <div class="my-app">
      <!-- 顶部概览：已开通 / 可申请 / 即将到期 -->
      <section class="my-app__overview">
        <div class="my-app__overview-card my-app__overview-card--primary">
          <div class="my-app__overview-icon">
            <Icon icon="ant-design:appstore-outlined" :size="22" />
          </div>
          <div class="my-app__overview-body">
            <div class="my-app__overview-value">{{ stats.owned }}</div>
            <div class="my-app__overview-label">{{ t('thinglinks.home.application.statsOwned') }}</div>
          </div>
        </div>
        <div class="my-app__overview-card my-app__overview-card--info">
          <div class="my-app__overview-icon">
            <Icon icon="ant-design:plus-square-outlined" :size="22" />
          </div>
          <div class="my-app__overview-body">
            <div class="my-app__overview-value">{{ stats.available }}</div>
            <div class="my-app__overview-label">{{ t('thinglinks.home.application.statsAvailable') }}</div>
          </div>
        </div>
        <div class="my-app__overview-card my-app__overview-card--warn">
          <div class="my-app__overview-icon">
            <Icon icon="ant-design:clock-circle-outlined" :size="22" />
          </div>
          <div class="my-app__overview-body">
            <div class="my-app__overview-value">{{ stats.expiringSoon }}</div>
            <div class="my-app__overview-label">{{ t('thinglinks.home.application.statsExpiringSoon') }}</div>
          </div>
        </div>
      </section>

      <!-- 我的应用 -->
      <section class="my-app__section">
        <header class="my-app__header">
          <div class="my-app__title">
            <span class="my-app__title-bar" />
            <span>{{ t('thinglinks.home.application.myApplication') }}</span>
            <span v-if="filteredMyApps.length" class="my-app__count">{{ filteredMyApps.length }}</span>
          </div>
          <a-input
            v-if="myApps.length > 6"
            v-model:value="myKeyword"
            size="small"
            allow-clear
            class="my-app__search"
            :placeholder="t('workbench.apps.search')"
          >
            <template #prefix>
              <Icon icon="ant-design:search-outlined" :size="12" />
            </template>
          </a-input>
        </header>

        <a-skeleton v-if="myLoading" active :paragraph="{ rows: 4 }" />
        <a-empty
          v-else-if="!myApps.length"
          :description="t('thinglinks.home.application.description')"
        />
        <a-empty
          v-else-if="!filteredMyApps.length"
          :description="t('workbench.apps.empty')"
        />
        <a-row v-else :gutter="[16, 16]">
          <a-col
            v-for="app in filteredMyApps"
            :key="app.id || app.appKey"
            :xs="24"
            :sm="12"
            :md="12"
            :lg="8"
            :xl="6"
          >
            <ApplicationCard :app="app" :show-license="true" @launch="handleLaunch" />
          </a-col>
        </a-row>
      </section>

      <!-- 推荐应用：仅展示当前账号尚未开通的 -->
      <section class="my-app__section">
        <header class="my-app__header">
          <div class="my-app__title">
            <span class="my-app__title-bar" />
            <span>{{ t('thinglinks.home.application.recommendApplication') }}</span>
            <span v-if="filteredRecommendApps.length" class="my-app__count">
              {{ filteredRecommendApps.length }}
            </span>
          </div>
          <a-input
            v-if="availableRecommendApps.length > 6"
            v-model:value="recommendKeyword"
            size="small"
            allow-clear
            class="my-app__search"
            :placeholder="t('workbench.apps.search')"
          >
            <template #prefix>
              <Icon icon="ant-design:search-outlined" :size="12" />
            </template>
          </a-input>
        </header>

        <a-skeleton v-if="recommendLoading" active :paragraph="{ rows: 3 }" />
        <a-empty
          v-else-if="!availableRecommendApps.length"
          :description="t('thinglinks.home.application.allActivat')"
        />
        <a-empty
          v-else-if="!filteredRecommendApps.length"
          :description="t('workbench.apps.empty')"
        />
        <a-row v-else :gutter="[16, 16]">
          <a-col
            v-for="app in filteredRecommendApps"
            :key="app.id || app.appKey"
            :xs="24"
            :sm="12"
            :md="12"
            :lg="8"
            :xl="6"
          >
            <ApplicationCard :app="app" @launch="handleRecommendClick" />
          </a-col>
        </a-row>
      </section>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import dayjs from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { Icon } from '/@/components/Icon';
  import { PageWrapper } from '/@/components/Page';
  import ApplicationCard from '/@/views/thinglinks/home/components/ApplicationCard.vue';
  import { useAppLauncher } from '/@/views/thinglinks/home/hooks/useAppLauncher';
  import { findMyApplication, findRecommendApplication } from '/@/api/thinglinks/profile/userInfo';
  import type { DefApplicationResultVO } from '/@/api/devOperation/application/model/defApplicationModel';

  defineOptions({ name: '我的应用' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { launch } = useAppLauncher();
  const userStore = useUserStore();

  const myApps = ref<DefApplicationResultVO[]>([]);
  const myLoading = ref(false);
  const myKeyword = ref('');

  const recommendApps = ref<DefApplicationResultVO[]>([]);
  const recommendLoading = ref(false);
  const recommendKeyword = ref('');

  /**
   * 推荐应用排除已开通的：以 id / appKey 双键去重，避免后端两个接口偶发返回
   * 同一应用导致重复展示。
   */
  const availableRecommendApps = computed(() => {
    const ownedIds = new Set(
      myApps.value.flatMap((a) => [a.id, a.appKey].filter(Boolean) as string[]),
    );
    return recommendApps.value.filter(
      (a) => !ownedIds.has(a.id) && !(a.appKey && ownedIds.has(a.appKey)),
    );
  });

  /**
   * 我的应用智能排序：
   *  1. 当前正在使用的应用置顶
   *  2. 即将到期（≤7 天）次之
   *  3. 已过期排在最后
   *  4. 其余按 createdTime 倒序
   */
  const sortedMyApps = computed(() => {
    const currentKey = userStore.getApplicationKey;
    return [...myApps.value].sort((a, b) => {
      const wA = weight(a, currentKey);
      const wB = weight(b, currentKey);
      if (wA !== wB) return wA - wB;
      return dayjs(b.createdTime || 0).valueOf() - dayjs(a.createdTime || 0).valueOf();
    });
  });

  function weight(app: DefApplicationResultVO, currentKey: string): number {
    if (currentKey && app.appKey === currentKey) return 0;
    const days = expireDays(app);
    if (days !== null && days < 0) return 3; // 已过期
    if (days !== null && days <= 7) return 1; // 即将到期
    return 2;
  }

  function expireDays(app: DefApplicationResultVO): number | null {
    if (!app.expirationTime) return null;
    const d = dayjs(app.expirationTime);
    if (!d.isValid()) return null;
    return Math.ceil((d.valueOf() - Date.now()) / (24 * 60 * 60 * 1000));
  }

  const stats = computed(() => {
    const owned = myApps.value.length;
    const available = availableRecommendApps.value.length;
    const expiringSoon = myApps.value.filter((a) => {
      const days = expireDays(a);
      return days !== null && days >= 0 && days <= 7;
    }).length;
    return { owned, available, expiringSoon };
  });

  const filteredMyApps = computed(() => filterByKeyword(sortedMyApps.value, myKeyword.value));
  const filteredRecommendApps = computed(() =>
    filterByKeyword(availableRecommendApps.value, recommendKeyword.value),
  );

  function filterByKeyword(list: DefApplicationResultVO[], kw: string) {
    const q = kw.trim().toLowerCase();
    if (!q) return list;
    return list.filter(
      (a) =>
        (a.name || '').toLowerCase().includes(q) ||
        (a.appKey || '').toLowerCase().includes(q) ||
        (a.introduce || '').toLowerCase().includes(q),
    );
  }

  function handleLaunch(app: DefApplicationResultVO) {
    launch(app);
  }

  function handleRecommendClick() {
    // 推荐应用点击：当前账号无权访问，提示联系管理员开通
    createMessage.warn(t('thinglinks.home.application.contactOpen'));
  }

  async function loadMyApplications() {
    myLoading.value = true;
    try {
      const res = await findMyApplication();
      myApps.value = Array.isArray(res) ? res : [];
    } finally {
      myLoading.value = false;
    }
  }

  async function loadRecommendApplications() {
    recommendLoading.value = true;
    try {
      const res = await findRecommendApplication();
      recommendApps.value = Array.isArray(res) ? res : [];
    } finally {
      recommendLoading.value = false;
    }
  }

  onMounted(() => {
    loadMyApplications();
    loadRecommendApplications();
  });
</script>

<style lang="less" scoped>
  :deep(.my-application-page) {
    padding: 16px 20px 24px;
    background: #f5f7fa;
    min-height: calc(100vh - 56px);
  }

  .my-app {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  /* —— 顶部概览 —— */
  .my-app__overview {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
  }

  @media (max-width: 768px) {
    .my-app__overview {
      grid-template-columns: 1fr;
    }
  }

  .my-app__overview-card {
    display: flex;
    align-items: center;
    gap: 14px;
    background: #fff;
    border-radius: 12px;
    padding: 18px 20px;
    box-shadow:
      0 1px 3px rgba(20, 37, 66, 0.03),
      0 6px 24px rgba(20, 37, 66, 0.04);
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow:
        0 2px 6px rgba(20, 37, 66, 0.04),
        0 12px 32px rgba(20, 37, 66, 0.08);
    }
  }

  .my-app__overview-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .my-app__overview-card--primary .my-app__overview-icon {
    background: rgba(93, 135, 255, 0.12);
    color: #5d87ff;
  }
  .my-app__overview-card--info .my-app__overview-icon {
    background: rgba(73, 190, 255, 0.14);
    color: #1a9fd9;
  }
  .my-app__overview-card--warn .my-app__overview-icon {
    background: rgba(255, 174, 31, 0.14);
    color: #c98a10;
  }

  .my-app__overview-body {
    flex: 1;
    min-width: 0;
  }

  .my-app__overview-value {
    font-size: 24px;
    font-weight: 700;
    color: #2a3547;
    line-height: 1.1;
    letter-spacing: -0.4px;
  }

  .my-app__overview-label {
    margin-top: 4px;
    font-size: 12px;
    color: #8c97a5;
  }

  /* —— 列表区域 —— */
  .my-app__section {
    background: #fff;
    border-radius: 12px;
    padding: 20px 22px;
    box-shadow:
      0 1px 3px rgba(20, 37, 66, 0.03),
      0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .my-app__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;
    gap: 12px;
    flex-wrap: wrap;
  }

  .my-app__title {
    display: inline-flex;
    align-items: baseline;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .my-app__title-bar {
    display: inline-block;
    width: 4px;
    height: 16px;
    border-radius: 2px;
    background: linear-gradient(180deg, #5d87ff, #49beff);
    transform: translateY(2px);
  }

  .my-app__count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 18px;
    padding: 0 8px;
    border-radius: 9px;
    background: rgba(93, 135, 255, 0.12);
    color: #5d87ff;
    font-size: 12px;
    font-weight: 600;
  }

  .my-app__search {
    width: 200px;
    min-width: 160px;
  }
</style>
