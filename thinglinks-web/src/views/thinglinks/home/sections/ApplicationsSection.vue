<template>
  <section class="home-apps">
    <div class="home-section-header">
      <div class="home-section-title">
        <span class="home-section-title__bar" />
        <span>{{ t('workbench.apps.title') }}</span>
        <span v-if="filteredApps.length" class="home-section-title__count">{{ filteredApps.length }}</span>
      </div>
      <a-input
        v-if="apps.length > 6"
        v-model:value="keyword"
        size="small"
        :placeholder="t('workbench.apps.search')"
        allow-clear
        class="home-apps__search"
      >
        <template #prefix>
          <Icon icon="ant-design:search-outlined" :size="12" />
        </template>
      </a-input>
    </div>

    <a-skeleton v-if="loading" active :paragraph="{ rows: 4 }" />
    <a-empty v-else-if="!apps.length" :description="t('workbench.apps.empty')" />
    <a-row v-else :gutter="[16, 16]">
      <a-col v-for="app in filteredApps" :key="app.id" :xs="24" :sm="12" :md="12" :lg="8" :xl="6">
        <ApplicationCard :app="app" @launch="handleLaunch" />
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';
  import ApplicationCard from '../components/ApplicationCard.vue';
  import { useAppLauncher } from '../hooks/useAppLauncher';
  import type { DefApplicationResultVO } from '/@/api/devOperation/application/model/defApplicationModel';

  interface Props {
    apps: DefApplicationResultVO[];
    loading?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), { loading: false });

  const { t } = useI18n();
  const { launch } = useAppLauncher();
  const keyword = ref('');

  const filteredApps = computed(() => {
    const list = props.apps || [];
    const kw = keyword.value.trim().toLowerCase();
    if (!kw) return list;
    return list.filter(
      (a) =>
        (a.name || '').toLowerCase().includes(kw) ||
        (a.appKey || '').toLowerCase().includes(kw) ||
        (a.introduce || '').toLowerCase().includes(kw),
    );
  });

  function handleLaunch(app: DefApplicationResultVO) {
    launch(app);
  }
</script>

<style lang="less" scoped>
  .home-apps {
    margin-bottom: 20px;
  }

  .home-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;
    gap: 12px;
    flex-wrap: wrap;
  }

  .home-section-title {
    display: inline-flex;
    align-items: baseline;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .home-section-title__bar {
    display: none;
  }

  .home-section-title__count {
    font-size: 12px;
    font-weight: 400;
    color: #8c97a5;
  }

  .home-apps__search {
    width: 200px;
    max-width: 50%;
  }
</style>
