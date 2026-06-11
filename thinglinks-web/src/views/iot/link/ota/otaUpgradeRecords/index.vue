<template>
  <PageWrapper contentBackground>
    <template #footer>
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane v-for="tab in tabList" :key="tab.key" :tab="tab.name" />
      </a-tabs>
    </template>

    <div v-show="activeKey === '1'" class="m-4 desc-wrap">
      <OtaUpgradeTaskDetail v-if="taskDetail" :task-detail="taskDetail" />
    </div>
    <div v-show="activeKey === '2'" class="m-4 desc-wrap">
      <OtaUpgradeRecords v-if="taskDetail" :task-detail="taskDetail" />
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, onMounted, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { usePermission } from '/@/hooks/web/usePermission';
  // api
  import { detail as getUpgradeTaskApi } from '/@/api/iot/link/ota/otaUpgradeTasks';
  // components
  import { PageWrapper } from '/@/components/Page';
  import { Divider, Card, Descriptions, Steps, Tabs } from 'ant-design-vue';
  import OtaUpgradeTaskDetail from './components/OtaUpgradeTaskDetail.vue';
  import OtaUpgradeRecords from './components/OtaUpgradeRecords.vue';

  export default defineComponent({
    components: {
      PageWrapper,
      [Divider.name]: Divider,
      [Card.name]: Card,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
      [Steps.name]: Steps,
      [Steps.Step.name]: Steps.Step,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
      OtaUpgradeTaskDetail,
      OtaUpgradeRecords,
    },
    setup() {
      const route = useRoute();
      const { createMessage } = useMessage();
      const { t } = useI18n();
      const { isPermission } = usePermission();
      const taskDetail = ref<any>(null);
      const loading = ref(false);

      // 标签页列表，通过computed返回
      const tabList = computed(() => {
        const list = [
          {
            name: t('iot.link.ota.otaUpgradeTasks.taskDetail'),
            key: '1',
            isShowAuth: isPermission(['link:otaUpgradeTasks:detail:view']),
          },
          {
            name: t('iot.link.ota.otaUpgradeRecords.upgradeRecords'),
            key: '2',
            isShowAuth: isPermission(['link:otaUpgradeTasks:detail:records']),
          },
        ];
        return list.filter((i) => i.isShowAuth);
      });

      // 默认值取tabList的第一个
      const activeKey = ref<string>(tabList.value[0]?.key);

      // 监听tabList变化，如果当前activeKey不在列表中，则设置为第一个
      watch(
        tabList,
        (newList) => {
          if (
            newList.length > 0 &&
            (!activeKey.value || !newList.some((tab) => tab.key === activeKey.value))
          ) {
            activeKey.value = newList[0].key;
          }
        },
        { immediate: true },
      );

      // 统一获取任务详情，避免子组件重复请求
      const fetchTaskDetail = async () => {
        if (taskDetail.value) return; // 已加载则不再请求

        loading.value = true;
        try {
          const res = await getUpgradeTaskApi(route.params.id as string);
          taskDetail.value = res;
        } catch (error) {
          console.error('获取升级任务详情失败:', error);
          createMessage.error(t('iot.link.ota.otaUpgradeTasks.fetchTaskDetailFail'));
        } finally {
          loading.value = false;
        }
      };

      onMounted(() => {
        fetchTaskDetail();
      });

      return {
        activeKey,
        taskDetail,
        loading,
        t,
        tabList,
      };
    },
  });
</script>
