<template>
  <SummaryCard :data="count" />
  <BasicTable
    @register="registerTable"
    @switch-change="getSwitchChange"
    :switchFlag="switchFlag"
    class="ota-upgrade-records-table"
  >
    <!-- 卡片视图(Flexy)── 通用 BusinessCardList,与北向集成等页面统一风格 -->
    <template #cardView="{ searchData, title }">
      <BusinessCardList
        ref="cardListRef"
        :pageApi="cardPageApi"
        :deleteApi="cardDeleteApi"
        :title="title"
        :searchData="searchData"
        nameField="deviceIdentification"
        :nameFallback="t('iot.link.ota.otaUpgradeRecords.deviceIdentification')"
        :fields="cardFields"
        statusField="upgradeStatus"
        :statusResolver="recordStatusResolver"
        :permissions="cardPermissions"
        detailRouteName="OTA升级任务记录详情"
        :extraActions="cardExtraActions"
        @input="getSwitchChange"
        @extraAction="handleCardExtraAction"
      >
        <template #headerExtra>
          <a-button @click="addDeviceUpgrade">
            <ToTopOutlined />
            {{ t('iot.link.ota.otaUpgradeRecords.addDeviceUpgrade') }}
          </a-button>
        </template>
        <template #cardImage="{ record }">
          <OtaRecordStatusBadge :status="record?.upgradeStatus" :size="74" />
        </template>
      </BusinessCardList>
    </template>
    <template #toolbar>
      <a-button @click="batchUpgrade">
        <ToTopOutlined />
        {{ t('iot.link.ota.otaUpgradeRecords.batchUpgrade') }}
      </a-button>
      <a-button @click="addDeviceUpgrade">
        <ToTopOutlined />
        {{ t('iot.link.ota.otaUpgradeRecords.addDeviceUpgrade') }}
      </a-button>
      <a-button preIcon="ant-design:swap-outlined" @click="switchView"
        >{{ t('iot.link.device.device.switchView') }}
      </a-button>
    </template>
    <template #upgradeStatusColumn="{ record }">
      {{ getDictLabel('LINK_OTA_TASK_RECORD_STATUS', record?.upgradeStatus, '') }}
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'action'">
        <TableAction :actions="getTableActions(record)" :stopButtonPropagation="true" />
      </template>
    </template>
  </BasicTable>
  <AddOtaUpgradeDevice @register="registerModalAddDevice" />
</template>
<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useLoading } from '/@/components/Loading';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardPermissions, CardAction } from '/@/components/BusinessCardList';
  import { OtaRecordStatusBadge } from '/@/components/iot/ota/svg';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { PermModeEnum } from '/@/enums/roleEnum';
  import { page, remove, GetOtaRecordsSummary } from '/@/api/iot/link/ota/otaUpgradeRecords';
  import { upgradeAgain } from '/@/api/iot/link/ota/otaUpgradeTasks';
  import {
    columns,
    searchFormSchema,
    cardFields as buildCardFields,
  } from '../otaUpgradeRecords.data';
  import SummaryCard from './summaryCard.vue';
  import { useDict } from '/@/components/Dict';
  import { ToTopOutlined } from '@ant-design/icons-vue';
  import AddOtaUpgradeDevice from './AddOtaUpgradeDevice.vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  const { getDictLabel } = useDict();
  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: 'OTA升级记录',
    components: {
      BasicTable,
      TableAction,
      SummaryCard,
      ToTopOutlined,
      AddOtaUpgradeDevice,
      BusinessCardList,
      OtaRecordStatusBadge,
    },
    props: {
      taskDetail: {
        type: Object,
        default: null,
      },
    },
    setup(props) {
      const { t } = useI18n();
      const route = useRoute();
      const { push } = useRouter();
      const { hasAnyPermission } = usePermission();
      const taskId = route.params.id;
      const count = ref({
        totalCount: 0,
        pendingUpgradeCount: 0,
        upgradingCount: 0,
        upgradeSuccessCount: 0,
        upgradeFailureCount: 0,
      });
      const productIdentification = ref('');
      const { createMessage } = useMessage();
      const [openFullLoading, closeFullLoading] = useLoading({
        tip: t('common.loadingText'),
      });

      // 卡片视图(Flexy)配置
      const cardListRef = ref<any>(null);
      const cardFields = buildCardFields();
      const cardPermissions: CardPermissions = {
        delete: 'basic:link:otaUpgradeRecords:delete',
        view: 'link:otaUpgradeTasks:detail:records',
      };
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('iot.link.ota.otaUpgradeRecords.upgrade'),
          icon: 'ant-design:to-top-outlined',
          event: 'upgrade',
        },
      ];
      // 卡片分页:注入当前任务 taskId(记录列表按任务过滤)
      const cardPageApi = (params: Recordable) => page({ ...params, taskId });
      const cardDeleteApi = (id: string) => remove([id]);
      // 升级记录状态 → 卡片右下角状态标签(待升级 琥珀 / 升级中 蓝 / 成功 绿 / 失败 红)
      const recordStatusResolver = (record: Recordable) => {
        const s = Number(record?.upgradeStatus);
        if (s === 2)
          return { label: t('iot.link.ota.otaUpgradeRecords.statusSuccess'), cls: 'online' };
        if (s === 1)
          return { label: t('iot.link.ota.otaUpgradeRecords.statusUpgrading'), cls: 'info' };
        if (s === 3)
          return { label: t('iot.link.ota.otaUpgradeRecords.statusFailed'), cls: 'danger' };
        return { label: t('iot.link.ota.otaUpgradeRecords.statusPending'), cls: 'offline' };
      };

      // 表格
      const [registerTable, { reload, getSelectRows }] = useTable({
        title: t('iot.link.ota.otaUpgradeRecords.table.title'),
        api: page,
        searchInfo: { taskId },
        columns: columns(),
        formConfig: {
          name: 'OtaUpgradeRecordsSearch',
          labelWidth: 120,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          resetButtonOptions: {
            preIcon: 'ant-design:rest-outlined',
          },
          submitButtonOptions: {
            preIcon: 'ant-design:search-outlined',
          },
        },
        beforeFetch: handleFetchParams,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        actionColumn: {
          width: 200,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      onMounted(async () => {
        // 从 props 获取 productIdentification，避免重复请求
        if (props.taskDetail?.otaUpgradesResult?.productIdentification) {
          productIdentification.value = props.taskDetail.otaUpgradesResult.productIdentification;
        }

        // 获取统计信息
        try {
          const {
            totalCount,
            pendingUpgradeCount,
            upgradingCount,
            upgradeSuccessCount,
            upgradeFailureCount,
          } = await GetOtaRecordsSummary(taskId as string);
          count.value = {
            totalCount,
            pendingUpgradeCount,
            upgradingCount,
            upgradeSuccessCount,
            upgradeFailureCount,
          };
        } catch (error) {
          console.error('获取统计信息失败:', error);
        }
      });

      // 点击记录进入详情页
      function handleViewDetail(record: Recordable, e: Event) {
        e?.stopPropagation();
        push({
          name: 'OTA升级任务记录详情',
          params: { id: record.id },
        });
      }

      // 删除成功 / 重新升级后回调:同步刷新表格与卡片
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      // 点击单行删除
      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          batchDelete([record.id]);
        }
      }

      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      const switchFlag = ref<boolean>(true);

      function getSwitchChange(e) {
        switchFlag.value = e;
      }

      const batchUpgrade = async (e: Event, record: Recordable) => {
        let selectRows = record ? [record] : getSelectRows();
        if (!selectRows.length) {
          return createMessage.warning(t('iot.link.ota.otaUpgradeRecords.tips.selectDevice'));
        }

        openFullLoading();
        try {
          const upgradeTaskId = taskId;
          const deviceIdentificationList = selectRows.map((i) => i.deviceIdentification);

          const data = await upgradeAgain({
            deviceIdentificationList,
            upgradeTaskId,
          });

          if (data) {
            createMessage.success(data);
          }
        } catch (err) {
          console.log(err);
        }
        closeFullLoading();
      };

      const singleUpgrade = (record: Recordable, e?: Event) => {
        batchUpgrade(e as Event, record);
      };

      // 卡片额外操作:重新升级该设备
      function handleCardExtraAction({ event, record }: { event: string; record: Recordable }) {
        if (event === 'upgrade') {
          singleUpgrade(record);
        }
      }

      const [registerModalAddDevice, { openModal: openModalAddDevice }] = useModal();
      const addDeviceUpgrade = () => {
        openModalAddDevice(true, {
          productIdentification: productIdentification.value,
          upgradeTaskId: taskId,
        });
      };

      // 获取表格操作按钮
      function getTableActions(record: Recordable) {
        return [
          {
            tooltip: t('common.title.details'),
            icon: 'ant-design:file-text-outlined',
            onClick: handleViewDetail.bind(null, record),
            authMode: PermModeEnum.HasAny,
          },
          {
            tooltip: t('iot.link.ota.otaUpgradeRecords.upgrade'),
            icon: 'ant-design:to-top-outlined',
            onClick: singleUpgrade.bind(null, record),
          },
          {
            tooltip: t('common.title.delete'),
            icon: 'ant-design:delete-outlined',
            color: 'error',
            auth: 'basic:link:otaUpgradeRecords:delete',
            popConfirm: {
              title: t('common.tips.confirmDelete'),
              confirm: handleDelete.bind(null, record),
            },
          },
        ];
      }

      return {
        t,
        registerTable,
        handleViewDetail,
        handleDelete,
        handleSuccess,
        switchView,
        getSwitchChange,
        switchFlag,
        getDictLabel,
        count,
        batchUpgrade,
        singleUpgrade,
        registerModalAddDevice,
        addDeviceUpgrade,
        getTableActions,
        hasAnyPermission,
        // 卡片视图
        cardListRef,
        cardFields,
        cardPermissions,
        cardExtraActions,
        cardPageApi,
        cardDeleteApi,
        recordStatusResolver,
        handleCardExtraAction,
      };
    },
  });
</script>
<style lang="less" scoped>
  .ota-upgrade-records-table {
    padding: 0 !important;
  }
</style>
