<template>
  <PageWrapper class="home-welcome" dense>
    <template v-if="isUser">
      <HeroSection :apps-total="apps.length" :apps-available="availableApps" />
      <ShortcutsSection />
      <MessagesSection />
      <ActivityLogSection />
      <PlatformInfoSection />
    </template>

    <div v-else class="home-welcome__empty">
      <Empty
        :image="illustration"
        :image-style="{
          'justify-content': 'center',
          'align-items': 'center',
          display: 'flex',
          height: '250px',
        }"
      >
        <template #description>
          <div class="home-welcome__empty-desc">{{ t('workbench.emptyTenant.title') }}</div>
        </template>
        <a-button type="primary" @click="handleTenant">
          {{ t('workbench.emptyTenant.registerTenant') }}
        </a-button>
        <a-button type="primary" style="margin-left: 20px" @click="handleEmployee">
          {{ t('workbench.emptyTenant.becomeEmployee') }}
        </a-button>
      </Empty>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
  export default {
    name: 'Welcome',
  };
</script>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Empty } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { findMyApplication } from '/@/api/thinglinks/profile/userInfo';
  import illustration from '/@/assets/svg/illustration.svg';

  import HeroSection from './sections/HeroSection.vue';
  import ShortcutsSection from './sections/ShortcutsSection.vue';
  import MessagesSection from './sections/MessagesSection.vue';
  import ActivityLogSection from './sections/ActivityLogSection.vue';
  import PlatformInfoSection from './sections/PlatformInfoSection.vue';
  import { useAppStaleGuard } from './hooks/useAppStaleGuard';

  import type { DefApplicationResultVO } from '/@/api/devOperation/application/model/defApplicationModel';

  const { t } = useI18n();
  const { createSuccessModal } = useMessage();
  const { replace } = useRouter();
  const userStore = useUserStore();
  const { ensure } = useAppStaleGuard();

  const apps = ref<DefApplicationResultVO[]>([]);
  const loadingApps = ref(false);

  const isUser = computed(
    () => !!userStore.getUserInfo?.employeeId && userStore.getUserInfo?.employeeId !== '0',
  );

  const availableApps = computed(
    () => apps.value.filter((a: any) => !isAppExpired(a)).length,
  );

  function isAppExpired(a: any): boolean {
    if (!a?.expirationTime) return false;
    return new Date(a.expirationTime).getTime() < Date.now();
  }

  async function loadApps() {
    loadingApps.value = true;
    try {
      const res: any = await findMyApplication();
      apps.value = Array.isArray(res) ? res : res?.records || [];
      ensure(apps.value);
    } catch (_) {
      apps.value = [];
    } finally {
      loadingApps.value = false;
    }
  }

  function handleEmployee() {
    createSuccessModal({ content: t('workbench.emptyTenant.contactAdmin') });
  }

  function handleTenant() {
    replace({ name: 'myTenantInfo' });
  }

  onMounted(() => {
    if (isUser.value) loadApps();
  });
</script>

<style lang="less" scoped>
  .home-welcome.thinglinks-page-wrapper {
    padding: 20px 24px;
    background: #f5f7fa;
    min-height: 100%;
  }

  // 兼容 PageWrapper 内部可能的容器
  .home-welcome :deep(.thinglinks-page-wrapper-content) {
    padding: 0;
  }

  .home-welcome__empty {
    padding: 32px;
  }

  .home-welcome__empty-desc {
    margin: 40px auto;
    font-size: 1.75rem;
  }

  :deep(.ant-empty-image img) {
    margin: auto;
    height: 100%;
  }
</style>
