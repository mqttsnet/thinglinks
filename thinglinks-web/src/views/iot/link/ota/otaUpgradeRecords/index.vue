<template>
  <PageWrapper contentFullHeight class="ota-task-detail">
    <!-- ===== 顶部 Header(公共信息,Flexy 风格,对齐产品详情)===== -->
    <a-card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="rule-icon"><OtaTaskSvg /></div>
          <div class="rule-meta">
            <div class="rule-title">
              <span class="name-text">{{ taskDetail?.taskName || '-' }}</span>
              <a-tag v-if="taskDetail" :color="taskStatusColor">
                {{ getDictLabel('LINK_OTA_TASK_STATUS', taskDetail?.taskStatus, '-') }}
              </a-tag>
              <a-tag v-if="taskDetail" color="geekblue">
                {{ getDictLabel('LINK_OTA_UPGRADE_METHOD', taskDetail?.upgradeMethod, '-') }}
              </a-tag>
            </div>
            <div class="meta-line">
              <span class="meta-id">
                <NumberOutlined />
                {{ t('iot.link.ota.otaUpgradeTasks.taskId') }}:
                <span class="value mono">{{ taskDetail?.id || '-' }}</span>
                <a-tooltip :title="t('common.title.copy')">
                  <CopyOutlined class="copy-btn" @click="copyTaskId" />
                </a-tooltip>
              </span>
              <a-divider v-if="packageName" type="vertical" />
              <span v-if="packageName">
                <InboxOutlined />
                {{ t('iot.link.ota.otaUpgrades.packageName') }}:
                <span class="value">{{ packageName }}</span>
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="load">
            <template #icon><ReloadOutlined /></template>
            {{ t('common.title.refresh') }}
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- ===== 4 指标卡(任务关键信息一眼可见)===== -->
    <a-row :gutter="16" class="metric-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon mode"><RocketOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.ota.otaUpgradeTasks.upgradeMode') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_OTA_UPGRADE_METHOD', taskDetail?.upgradeMethod, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.ota.otaUpgradeTasks.upgradeScope') }}</span>
              <span class="sub-val">{{ scopeLabel }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon status"><CheckCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.ota.otaUpgradeTasks.taskStatus') }}</div>
            <div class="metric-value">
              {{ getDictLabel('LINK_OTA_TASK_STATUS', taskDetail?.taskStatus, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('thinglinks.common.createdTime') }}</span>
              <a-tooltip :title="taskDetail?.createdTime || ''">
                <span class="sub-val">{{ taskDetail?.createdTime || '-' }}</span>
              </a-tooltip>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon time"><ClockCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.link.ota.otaUpgradeTasks.scheduledStartTime') }}
            </div>
            <a-tooltip :title="taskDetail?.scheduledStartTime || ''">
              <div class="metric-value metric-value--mono">
                {{ taskDetail?.scheduledStartTime || '-' }}
              </div>
            </a-tooltip>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.ota.otaUpgradeTasks.scheduledEndTime') }}</span>
              <span class="sub-val">{{ taskDetail?.scheduledEndTime || '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon strategy"><ThunderboltOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.ota.otaUpgradeTasks.upgradeRate') }}</div>
            <div class="metric-value">{{ taskDetail?.upgradeRate ?? '-' }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.ota.otaUpgradeTasks.maxRetryCount') }}</span>
              <span class="sub-val">{{ taskDetail?.maxRetryCount ?? '-' }}</span>
              <span class="sub-divider">·</span>
              <span class="sub-key">{{
                t('iot.link.ota.otaUpgradeTasks.retryIntervalMinutes')
              }}</span>
              <span class="sub-val">{{ taskDetail?.retryIntervalMinutes ?? '-' }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- ===== Tabs(基础信息 / 升级记录,按权限码展示)===== -->
    <a-card v-if="taskDetail && cardTabList.length" :bordered="false" class="panel-card">
      <a-tabs v-model:activeKey="activeKey" size="small">
        <a-tab-pane v-for="item in cardTabList" :tab="item.name" :key="item.key" />
      </a-tabs>
      <div class="tab-content">
        <OtaUpgradeTaskDetail v-if="activeKey === '1'" :task-detail="taskDetail" />
        <OtaUpgradeRecords v-else-if="activeKey === '2'" :task-detail="taskDetail" />
      </div>
    </a-card>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, onMounted, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useTabs } from '/@/hooks/web/useTabs';
  import {
    CopyOutlined,
    ReloadOutlined,
    NumberOutlined,
    InboxOutlined,
    RocketOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    ThunderboltOutlined,
  } from '@ant-design/icons-vue';
  import { detail as getUpgradeTaskApi } from '/@/api/iot/link/ota/otaUpgradeTasks';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Tag, Tabs, Space, Button, Row, Col, Divider, Tooltip } from 'ant-design-vue';
  import { useDict } from '/@/components/Dict';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { OtaTaskSvg } from '/@/components/iot/ota/svg';
  import OtaUpgradeTaskDetail from './components/OtaUpgradeTaskDetail.vue';
  import OtaUpgradeRecords from './components/OtaUpgradeRecords.vue';

  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'OTA升级任务详情',
    components: {
      PageWrapper,
      ACard: Card,
      ATag: Tag,
      ASpace: Space,
      AButton: Button,
      ARow: Row,
      ACol: Col,
      ADivider: Divider,
      ATooltip: Tooltip,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
      CopyOutlined,
      ReloadOutlined,
      NumberOutlined,
      InboxOutlined,
      RocketOutlined,
      CheckCircleOutlined,
      ClockCircleOutlined,
      ThunderboltOutlined,
      OtaTaskSvg,
      OtaUpgradeTaskDetail,
      OtaUpgradeRecords,
    },
    setup() {
      const route = useRoute();
      const { createMessage } = useMessage();
      const { t } = useI18n();
      const { isPermission } = usePermission();
      const { setTitle } = useTabs();
      const taskDetail = ref<any>(null);
      const loading = ref(false);

      const packageName = computed(() => taskDetail.value?.otaUpgradesResult?.packageName);

      // 任务状态色:1 执行中 蓝 / 2 已完成 绿 / 3 失败 红 / 其余灰
      const taskStatusColor = computed(() => {
        const s = Number(taskDetail.value?.taskStatus);
        if (s === 2) return 'success';
        if (s === 3) return 'error';
        if (s === 1) return 'processing';
        return 'default';
      });

      // 升级范围:走字典(LINK_OTA_UPGRADE_SCOPE)展示
      const scopeLabel = computed(() => {
        const scope = taskDetail.value?.upgradeScope ?? taskDetail.value?.echoMap?.upgradeScope;
        return scope != null ? getDictLabel('LINK_OTA_UPGRADE_SCOPE', scope, '-') : '-';
      });

      // 标签页:权限码绑定到 tab,无权限不展示
      const cardTabList = computed(() => {
        const list = [
          {
            name: t('iot.link.ota.otaUpgradeTasks.taskBasicInfo'),
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

      const activeKey = ref<string>('1');

      watch(
        cardTabList,
        (newList) => {
          if (newList.length > 0 && !newList.some((tab) => tab.key === activeKey.value)) {
            activeKey.value = newList[0].key;
          }
        },
        { immediate: true },
      );

      const load = async () => {
        loading.value = true;
        try {
          const res = await getUpgradeTaskApi(route.params.id as string);
          taskDetail.value = res;
          setTitle(
            `${t('iot.link.ota.otaUpgradeTasks.upgradeTaskDetail')} - ${res?.taskName || ''}`,
          );
        } catch (error) {
          console.error('获取升级任务详情失败:', error);
          createMessage.error(t('iot.link.ota.otaUpgradeTasks.fetchTaskDetailFail'));
        } finally {
          loading.value = false;
        }
      };

      const copyTaskId = () => {
        handleCopyTextV2(String(taskDetail.value?.id ?? ''));
      };

      onMounted(load);

      return {
        t,
        taskDetail,
        packageName,
        taskStatusColor,
        scopeLabel,
        cardTabList,
        activeKey,
        getDictLabel,
        load,
        copyTaskId,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* Flexy 风格,完全对齐产品 / 设备详情 */
  .ota-task-detail {
    :deep(.ant-card) {
      border-radius: 14px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    }
  }

  .header-card {
    margin: 16px 16px 0;

    :deep(.ant-card-body) {
      padding: 20px 24px;
    }
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
  }

  .rule-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #eef2ff 0%, #e8f4fd 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.1);
    flex-shrink: 0;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .rule-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
  }

  .rule-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    .name-text {
      font-size: 18px;
      font-weight: 600;
      color: #2a3547;
    }
  }

  .meta-line {
    color: #6b7280;
    font-size: 13px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .value {
      color: #2a3547;
      font-weight: 500;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      }
    }

    .copy-btn {
      cursor: pointer;
      color: #5d87ff;
      margin-left: 2px;

      &:hover {
        color: #2952cc;
      }
    }

    :deep(.anticon:not(.copy-btn)) {
      color: #97a1b0;
    }
  }

  /* ===== 4 指标卡 ===== */
  .metric-row {
    margin: 16px 16px 0 !important;
  }

  .metric-card {
    height: 100%;

    :deep(.ant-card-body) {
      padding: 16px 18px;
      display: flex;
      align-items: center;
      gap: 14px;
      height: 100%;
      min-height: 88px;
    }
  }

  .metric-icon {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    flex-shrink: 0;

    &.mode {
      background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
      box-shadow: 0 6px 14px rgba(93, 135, 255, 0.35);
    }
    &.status {
      background: linear-gradient(135deg, #13deb9 0%, #36e6c3 100%);
      box-shadow: 0 6px 14px rgba(19, 222, 185, 0.35);
    }
    &.time {
      background: linear-gradient(135deg, #13c2c2 0%, #48d6f0 100%);
      box-shadow: 0 6px 14px rgba(19, 194, 194, 0.35);
    }
    &.strategy {
      background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
      box-shadow: 0 6px 14px rgba(255, 174, 31, 0.35);
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    font-size: 12px;
    color: #97a1b0;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    margin-bottom: 2px;
  }

  .metric-value {
    font-size: 18px;
    font-weight: 700;
    color: #2a3547;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &--mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 14px;
      letter-spacing: 0.2px;
    }
  }

  .metric-sub {
    margin-top: 4px;
    font-size: 12px;
    color: #6b7280;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    min-width: 0;

    .sub-key {
      color: #97a1b0;
      flex-shrink: 0;
    }
    .sub-val {
      color: #2a3547;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      min-width: 0;
    }
    .sub-divider {
      color: #c8cfdb;
    }
  }

  /* ===== Tabs 主体 ===== */
  .panel-card {
    margin: 16px;

    :deep(.ant-card-body) {
      padding: 0;
    }

    :deep(.ant-tabs) {
      padding: 0 16px;
    }

    :deep(.ant-tabs-nav) {
      margin: 0;
      &::before {
        border-bottom: 1px solid #f0f2f5;
      }
    }
  }

  .tab-content {
    padding: 16px 18px 20px;
    overflow-x: auto;
  }
</style>
