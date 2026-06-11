<template>
  <section class="home-messages">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :md="12">
        <div class="home-panel">
          <div class="home-panel__header">
            <div class="home-section-title">
              <span class="home-section-title__bar" />
              <span>{{ t('workbench.messages.title') }}</span>
              <span v-if="unreadCount > 0" class="home-section-title__count">
                {{ unreadCount }}
              </span>
            </div>
          </div>
          <div class="home-panel__body">
            <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
            <a-empty
              v-else-if="!messages.length"
              :description="t('workbench.messages.empty')"
              :image-style="{ height: '40px' }"
            />
            <div v-else>
              <MessageItem v-for="m in messages" :key="m.id" :item="m" @click="handleMsgClick" />
            </div>
          </div>
        </div>
      </a-col>

      <a-col :xs="24" :md="12">
        <div class="home-panel">
          <div class="home-panel__header">
            <div class="home-section-title">
              <span class="home-section-title__bar" />
              <span>{{ t('workbench.announcement.title') }}</span>
            </div>
          </div>
          <div class="home-panel__body">
            <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
            <a-empty
              v-else-if="!announcements.length"
              :description="t('workbench.announcement.empty')"
              :image-style="{ height: '40px' }"
            />
            <div v-else>
              <MessageItem
                v-for="a in announcements"
                :key="a.id"
                :item="a"
                @click="handleAnnClick"
              />
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </section>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import MessageItem from '../components/MessageItem.vue';
  import { myNotice } from '/@/api/basic/msg/extendNotice';
  import type { MessageItemVO } from '../types';

  const { t } = useI18n();
  const router = useRouter();

  const messages = ref<MessageItemVO[]>([]);
  const announcements = ref<MessageItemVO[]>([]);
  const loading = ref(false);

  const unreadCount = computed(() => messages.value.filter((m) => m.unread).length);

  // 跟头部铃铛一致，一次 myNotice 拿全量，再按 list 拆到左右两侧
  async function loadAll() {
    loading.value = true;
    try {
      const res: any = await myNotice({ model: {}, current: 1, size: 6 });

      const todos: any[] = res?.todoList?.records || [];
      const warnings: any[] = res?.earlyWarningList?.records || [];
      const notices: any[] = res?.noticeList?.records || [];

      // 左：待办 + 预警，按时间倒序合并
      messages.value = [...todos, ...warnings]
        .sort((a, b) => {
          const ta = new Date(a.createdTime || 0).getTime();
          const tb = new Date(b.createdTime || 0).getTime();
          return tb - ta;
        })
        .slice(0, 5)
        .map((m) => ({
          id: m.id,
          title: m.title || m.content || '-',
          createdTime: m.createdTime,
          unread: !m.isRead,
          type: warnings.includes(m) ? 'warning' : 'info',
          url: m.handlerUrl,
        }));

      announcements.value = notices.slice(0, 5).map((n) => ({
        id: n.id,
        title: n.title || '-',
        createdTime: n.createdTime,
        type: 'announcement',
        url: n.handlerUrl,
      }));
    } catch (_) {
      messages.value = [];
      announcements.value = [];
    } finally {
      loading.value = false;
    }
  }

  function handleMsgClick(m: MessageItemVO) {
    if (m.url) {
      if (/^https?:\/\//i.test(m.url)) {
        window.open(m.url, '_blank');
      } else {
        router.push(m.url);
      }
    }
  }

  function handleAnnClick(a: MessageItemVO) {
    handleMsgClick(a);
  }

  onMounted(loadAll);
</script>

<style lang="less" scoped>
  .home-messages {
    margin-bottom: 20px;
  }

  .home-panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px 22px;
    min-height: 200px;
    height: 100%;
    display: flex;
    flex-direction: column;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
  }

  .home-panel__body {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.ant-empty) {
      margin: auto 0;
    }
  }

  .home-panel__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .home-section-title {
    display: inline-flex;
    align-items: baseline;
    gap: 8px;
    font-size: 15px;
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.1px;
  }

  .home-section-title__bar {
    display: none;
  }

  .home-section-title__count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 18px;
    height: 18px;
    padding: 0 6px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
    background: rgba(250, 137, 107, 0.1);
    color: #d04a2d;
  }
</style>
