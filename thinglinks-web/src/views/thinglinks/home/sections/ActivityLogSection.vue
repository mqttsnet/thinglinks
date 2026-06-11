<template>
  <section class="home-activity">
    <div class="home-panel">
      <div class="home-panel__header">
        <div class="home-section-title">
          {{ t('workbench.activity.title') }}
        </div>
      </div>
      <div class="home-panel__body">
        <a-skeleton v-if="loading" active :paragraph="{ rows: 4 }" />
        <a-empty
          v-else-if="!feed.length"
          :description="t('workbench.activity.empty')"
          :image-style="{ height: '48px' }"
        />
        <div v-else class="home-activity__list">
          <TimelineItem
            v-for="(it, idx) in feed"
            :key="it.id"
            :item="it"
            :is-last="idx === feed.length - 1"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import dayjs from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import TimelineItem from '../components/TimelineItem.vue';
  import { anyonePage } from '/@/api/basic/system/baseLoginLog';
  import { page as pageOp } from '/@/api/basic/system/baseOperationLog';
  import { useUserStore } from '/@/store/modules/user';
  import type { LogItemVO } from '../types';

  const { t } = useI18n();
  const userStore = useUserStore();

  const loading = ref(false);
  const logins = ref<LogItemVO[]>([]);
  const operations = ref<LogItemVO[]>([]);

  const feed = computed<LogItemVO[]>(() => {
    const merged = [...logins.value, ...operations.value];
    merged.sort((a, b) => {
      const ta = dayjs(a.createdTime || '').valueOf() || 0;
      const tb = dayjs(b.createdTime || '').valueOf() || 0;
      return tb - ta;
    });
    return merged.slice(0, 10);
  });

  async function loadAll() {
    loading.value = true;
    const username = userStore.getUserInfo?.username;
    const [loginRes, opRes] = await Promise.allSettled([
      anyonePage({ model: {}, current: 1, size: 6 }),
      pageOp({ model: username ? { userName: username } : {}, current: 1, size: 6 }),
    ]);

    if (loginRes.status === 'fulfilled') {
      const list: any[] = (loginRes.value as any)?.records || [];
      logins.value = list.map((l) => ({
        id: `login-${l.id ?? `${l.requestIp}-${l.createdTime}`}`,
        title: l.description || `${t('workbench.activity.loginAction')} ${l.account || l.userName || ''}`.trim(),
        createdTime: l.createdTime || l.loginDate,
        ip: l.requestIp,
        userAgent: [l.browser, l.operatingSystem].filter(Boolean).join(' · '),
        status: 'success',
      }));
    } else {
      logins.value = [];
    }

    if (opRes.status === 'fulfilled') {
      const list: any[] = (opRes.value as any)?.records || [];
      operations.value = list.map((o) => ({
        id: `op-${o.id ?? `${o.requestIp}-${o.startTime}`}`,
        title: o.description || `${t('workbench.activity.operationAction')} ${o.actionMethod || o.requestUri || ''}`.trim(),
        createdTime: o.startTime || o.createdTime,
        ip: o.requestIp,
        userAgent: o.httpMethod,
        status: o.consumingTime && Number(o.consumingTime) > 1000 ? 'warning' : 'success',
      }));
    } else {
      operations.value = [];
    }

    loading.value = false;
  }

  onMounted(loadAll);
</script>

<style lang="less" scoped>
  .home-activity {
    margin-bottom: 20px;
  }

  .home-panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px 22px;
    min-height: 260px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .home-panel__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .home-section-title {
    font-size: 15px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .home-activity__list {
    display: flex;
    flex-direction: column;
    padding: 4px 0;
  }
</style>
