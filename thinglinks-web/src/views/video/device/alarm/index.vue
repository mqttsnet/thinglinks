<template>
  <PageWrapper dense contentFullHeight>
    <!-- 统计卡片 - Flexy 风格 -->
    <div class="alarm-stats">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card size="small" hoverable class="stat-card">
            <div class="stat-card__inner">
              <div class="stat-card__icon stat-card__icon--blue">
                <AlertOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value" style="color: #1890ff">{{ statsData.totalCount || 0 }}</div>
                <div class="stat-card__label">{{ t('video.device.alarm.stats.totalCount') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card size="small" hoverable class="stat-card">
            <div class="stat-card__inner">
              <div class="stat-card__icon stat-card__icon--orange">
                <ExclamationCircleOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value" style="color: #faad14">{{ statsData.unhandledCount || 0 }}</div>
                <div class="stat-card__label">{{ t('video.device.alarm.stats.pendingCount') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card size="small" hoverable class="stat-card">
            <div class="stat-card__inner">
              <div class="stat-card__icon stat-card__icon--purple">
                <PlusCircleOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__value" style="color: #722ed1">{{ todayCount }}</div>
                <div class="stat-card__label">{{ t('video.device.alarm.stats.todayCount') }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card size="small" hoverable class="stat-card">
            <div class="stat-card__inner">
              <div class="stat-card__icon stat-card__icon--red">
                <PieChartOutlined />
              </div>
              <div class="stat-card__content">
                <div class="stat-card__label" style="margin-bottom: 4px">{{ t('video.device.alarm.stats.priorityDistribution') }}</div>
                <div class="priority-tags">
                  <a-tag
                    v-for="item in statsData.byPriority"
                    :key="item.name"
                    :color="getPriorityColor(item.name)"
                    size="small"
                  >
                    L{{ item.name }}: {{ item.count }}
                  </a-tag>
                  <span v-if="!statsData.byPriority?.length" class="no-data">{{ t('video.device.alarm.stats.noData') }}</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 表格/卡片切换 -->
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="wrappedPageApi"
          :title="title"
          :searchData="searchData"
          nameField="alarmTitle"
          :fields="cardFields"
          badgeField="handleStatusLabel"
          statusField="handleStatus"
          :statusOnlineValue="0"
          :statusOnlineLabel="t('video.device.alarm.stats.onlineLabel')"
          :statusOfflineLabel="t('video.device.alarm.stats.offlineLabel')"
          :permissions="cardPermissions"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <WarningOutlined style="font-size: 40px; color: #faad14" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          danger
          preIcon="ant-design:delete-outlined"
          v-hasAnyPermission="['video:device:alarm:clear']"
          @click="handleBatchIgnore"
          :disabled="!selectedRowKeys.length"
        >
          批量忽略 ({{ selectedRowKeys.length }})
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('common.switchView') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('video.device.alarm.tooltipView'),
                icon: 'ant-design:eye-outlined',
                auth: 'video:device:alarm:view',
                onClick: handleView.bind(null, record, undefined),
              },
              {
                tooltip: t('video.device.alarm.confirm'),
                icon: 'ant-design:check-outlined',
                color: 'warning',
                auth: 'video:device:alarm:confirm',
                ifShow: record.handleStatus === 0,
                onClick: handleConfirm.bind(null, record),
              },
              {
                tooltip: t('video.device.alarm.resolve'),
                icon: 'ant-design:check-circle-outlined',
                color: 'success',
                auth: 'video:device:alarm:resolve',
                ifShow: record.handleStatus === 1,
                onClick: handleResolve.bind(null, record),
              },
              {
                tooltip: t('video.device.alarm.ignore'),
                icon: 'ant-design:stop-outlined',
                color: 'default',
                auth: 'video:device:alarm:ignore',
                ifShow: record.handleStatus === 0 || record.handleStatus === 1,
                onClick: handleIgnore.bind(null, record),
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardField, CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { useWebSocket } from '@vueuse/core';
  import { useUserStore } from '/@/store/modules/user';
  import { notification } from 'ant-design-vue';
  import {
    AlertOutlined,
    ExclamationCircleOutlined,
    PlusCircleOutlined,
    PieChartOutlined,
    WarningOutlined,
  } from '@ant-design/icons-vue';
  import {
    page,
    statistics,
    batchIgnore,
  } from '/@/api/video/device/alarm';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { useDetailRoute } from '/@/hooks/web/usePage';
  import { columns, searchFormSchema } from './alarm.data';
  import { useAlarmActions } from './useAlarmActions';
  import type { AlarmStatisticsResultVO } from '/@/api/video/device/model/alarmModel';

  export default defineComponent({
    name: 'VideoDeviceAlarm',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      BusinessCardList,
      AlertOutlined,
      ExclamationCircleOutlined,
      PlusCircleOutlined,
      PieChartOutlined,
      WarningOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const { goDetail } = useDetailRoute();

      // 处理状态映射（按 i18n 动态生成）
      const getStatusText = (status: number): string => {
        const map: Record<number, string> = {
          0: t('video.device.alarm.status.pending'),
          1: t('video.device.alarm.status.processing'),
          2: t('video.device.alarm.status.resolved'),
          3: t('video.device.alarm.status.ignored'),
        };
        return map[status] || '';
      };
      const { handleConfirm, handleResolve, handleIgnore } = useAlarmActions(async () => {
        handleSuccess();
        loadStats();
      });

      function handleView(record: Recordable, e?: Event) {
        e?.stopPropagation();
        goDetail(record.id);
      }

      const statsData = reactive<AlarmStatisticsResultVO>({});
      const selectedRowKeys = ref<string[]>([]);
      const switchFlag = ref<boolean>(true); // 默认卡片视图
      const cardListRef = ref<any>(null);

      const todayCount = computed(() => {
        const today = new Date().toISOString().slice(0, 10);
        return statsData.byDay?.find((d) => d.name === today)?.count || 0;
      });

      const getPriorityColor = (level: string | undefined): string => {
        const colors: Record<string, string> = { '1': 'red', '2': 'orange', '3': 'blue', '4': 'green' };
        return colors[level || ''] || 'default';
      };

      // 包装 page API，为卡片视图添加显示字段
      const wrappedPageApi = async (params: any) => {
        const res = await page(params);
        if (res?.records) {
          res.records.forEach((r: any) => {
            // 拼接卡片标题
            r.alarmTitle = `${r.deviceIdentification || ''} - ${r.alarmDescription || t('video.device.alarm.alarmFallback')}`;
            // 处理状态标签（badge 显示）
            const statusText = r.echoMap?.handleStatus || getStatusText(r.handleStatus) || '';
            r.handleStatusLabel = r.handleStatus === 0 ? statusText : null;
          });
        }
        return res;
      };

      const cardFields: CardField[] = [
        { label: t('video.device.alarm.cards.deviceId'), field: 'deviceIdentification', span: 24 },
        { label: t('video.device.alarm.cards.alarmPriority'), field: 'echoMap.alarmPriority', span: 12 },
        { label: t('video.device.alarm.cards.alarmMethod'), field: 'echoMap.alarmMethod', span: 12 },
        { label: t('video.device.alarm.cards.alarmTime'), field: 'alarmTime', span: 24 },
        { label: t('video.device.alarm.cards.alarmDescription'), field: 'alarmDescription', span: 24 },
      ];

      const cardPermissions: CardPermissions = {};

      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('video.device.alarm.tooltipView'),
          icon: 'ant-design:eye-outlined',
          event: 'detail',
          permission: 'video:device:alarm:view',
        },
        {
          tooltip: t('video.device.alarm.tooltipConfirm'),
          icon: 'ant-design:check-outlined',
          event: 'confirm',
          permission: 'video:device:alarm:confirm',
          disabled: (record) => Number(record.handleStatus) !== 0,
        },
        {
          tooltip: t('video.device.alarm.tooltipResolve'),
          icon: 'ant-design:check-circle-outlined',
          event: 'resolve',
          permission: 'video:device:alarm:resolve',
          disabled: (record) => Number(record.handleStatus) !== 1,
        },
        {
          tooltip: t('video.device.alarm.tooltipIgnore'),
          icon: 'ant-design:stop-outlined',
          event: 'ignore',
          permission: 'video:device:alarm:ignore',
          disabled: (record) => {
            const s = Number(record.handleStatus);
            return s !== 0 && s !== 1;
          },
        },
      ];

      const handleCardExtraAction = ({ event, record }: { event: string; record: any }) => {
        if (event === 'detail') {
          handleView(record);
          return;
        }
        const status = Number(record.handleStatus);
        if (event === 'confirm') {
          if (status !== 0) return;
          handleConfirm(record);
        } else if (event === 'resolve') {
          if (status !== 1) return;
          handleResolve(record);
        } else if (event === 'ignore') {
          if (status !== 0 && status !== 1) return;
          handleIgnore(record);
        }
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.device.alarm.listTitle'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'AlarmSearch',
          labelWidth: 100,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          resetButtonOptions: { preIcon: 'ant-design:rest-outlined' },
          submitButtonOptions: { preIcon: 'ant-design:search-outlined' },
        },
        beforeFetch: handleFetchParams,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
          onChange: (keys: string[]) => {
            selectedRowKeys.value = keys;
          },
        },
        actionColumn: {
          width: 140,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      const getSwitchChange = (e) => {
        switchFlag.value = e;
      };

      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        }
      });

      onMounted(loadStats);

      async function loadStats() {
        try {
          const data = await statistics();
          Object.assign(statsData, data);
        } catch (e) {
          // ignore
        }
      }

      async function handleBatchIgnore() {
        const keys = selectedRowKeys.value;
        if (!keys.length) return;
        await batchIgnore(keys);
        createMessage.success(t('video.device.alarm.batchIgnored', { count: keys.length }));
        selectedRowKeys.value = [];
        handleSuccess();
        loadStats();
      }

      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      // ===== WebSocket 告警实时推送 =====
      let wsInstance: ReturnType<typeof useWebSocket> | null = null;

      function connectAlarmWs() {
        const protocol = window.location.protocol;
        const host = window.location.host;
        const userStore = useUserStore();
        const tenantId = userStore.getTenantId;
        const token = userStore.getToken;
        const wsUrl = `${
          protocol.includes('https') ? 'wss' : 'ws'
        }://${host}/api/wsVideo/anyone/videoSocket/alarm/${tenantId}?Token=${token}&TenantId=${tenantId}`;

        let retryCount = 0;
        wsInstance = useWebSocket(wsUrl, {
          autoReconnect: {
            retries: 5,
            delay: () => {
              const delay = Math.min(1000 * Math.pow(2, retryCount), 30000);
              retryCount++;
              return delay;
            },
          },
          heartbeat: { message: 'ping', interval: 30000 },
          onMessage: (_ws, event) => {
            try {
              const msg = JSON.parse(event.data);
              if (msg?.type === 'NEW_ALARM') {
                notification.warning({
                  message: t('video.device.alarm.ws.newAlarm'),
                  description: `${t('video.device.alarm.ws.device')} ${msg.deviceName || msg.deviceIdentification}: ${msg.alarmDescription || ''}`,
                  duration: 6,
                });
                handleSuccess();
                loadStats();
              }
            } catch (_) {
              // pong 或解析失败静默处理
            }
          },
        });
      }

      onMounted(() => {
        connectAlarmWs();
      });

      onBeforeUnmount(() => {
        wsInstance?.close?.();
      });

      return {
        t,
        page,
        wrappedPageApi,
        statsData,
        selectedRowKeys,
        todayCount,
        getPriorityColor,
        switchFlag,
        cardListRef,
        cardFields,
        cardPermissions,
        cardExtraActions,
        handleCardExtraAction,
        registerTable,
        getSwitchChange,
        switchView,
        handleView,
        handleConfirm,
        handleResolve,
        handleIgnore,
        handleBatchIgnore,
      };
    },
  });
</script>

<style lang="less" scoped>
  .alarm-stats {
    margin-bottom: 16px;

    .stat-card {
      border-radius: 12px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
      }

      :deep(.ant-card-body) {
        padding: 16px;
      }

      &__inner {
        display: flex;
        align-items: center;
        gap: 12px;
        height: 48px;
      }

      &__icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        flex-shrink: 0;

        &--blue {
          background: rgba(24, 144, 255, 0.1);
          color: #1890ff;
        }

        &--orange {
          background: rgba(250, 173, 20, 0.1);
          color: #faad14;
        }

        &--purple {
          background: rgba(114, 46, 209, 0.1);
          color: #722ed1;
        }

        &--red {
          background: rgba(255, 77, 79, 0.1);
          color: #ff4d4f;
        }
      }

      &__content {
        flex: 1;
        min-width: 0;
      }

      &__value {
        font-size: 28px;
        font-weight: 600;
        line-height: 1.2;
      }

      &__label {
        font-size: 13px;
        color: rgba(0, 0, 0, 0.45);
        margin-top: 2px;
      }
    }
  }

  .priority-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;

    .no-data {
      color: rgba(0, 0, 0, 0.25);
      font-size: 13px;
    }
  }
</style>
