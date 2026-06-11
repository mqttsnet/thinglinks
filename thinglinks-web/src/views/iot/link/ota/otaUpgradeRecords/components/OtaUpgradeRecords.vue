<template>
  <SummaryCard :data="count" />
  <BasicTable
    @register="registerTable"
    @switch-change="getSwitchChange"
    :switchFlag="switchFlag"
    :isOtaUpgradeRecords="true"
    class="ota-upgrade-records-table"
  >
    <template #toolbar>
      <!-- <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['basic:link:otaUpgradeRecords:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button> -->
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
  <EditModal @register="registerModal" @success="handleSuccess" />
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
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { PermModeEnum } from '/@/enums/roleEnum';
  import { page, remove, GetOtaRecordsSummary } from '/@/api/iot/link/ota/otaUpgradeRecords';
  import { upgradeAgain } from '/@/api/iot/link/ota/otaUpgradeTasks';
  import { columns, searchFormSchema } from '../otaUpgradeRecords.data';
  import EditModal from '../Edit.vue';
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
      EditModal,
      SummaryCard,
      ToTopOutlined,
      AddOtaUpgradeDevice,
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
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [openFullLoading, closeFullLoading] = useLoading({
        tip: t('common.loadingText'),
      });

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

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.VIEW,
        });
      }

      // 点击记录进入详情页
      function handleViewDetail(record: Recordable, e: Event) {
        e?.stopPropagation();
        push({
          name: 'OTA升级任务记录详情',
          params: { id: record.id },
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
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

      // 点击批量删除
      function handleBatchDelete() {
        const ids = getSelectRowKeys();
        if (!ids || ids.length <= 0) {
          createMessage.warning(t('common.tips.pleaseSelectTheData'));
          return;
        }
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await batchDelete(ids);
            } catch (e) {}
          },
        });
      }

      //
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

      const singleUpgrade = (record: Recordable, e: Event) => {
        batchUpgrade(e, record);
      };

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
            tooltip: t('common.title.view'),
            icon: 'ant-design:search-outlined',
            onClick: handleView.bind(null, record),
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
        registerModal,
        handleView,
        handleViewDetail,
        handleDelete,
        handleBatchDelete,
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
      };
    },
  });
</script>
<style lang="less" scoped>
  .ota-upgrade-records-table {
    padding: 0 !important;
  }
</style>
../../../../../api/iot/link/ota/otaUpgradeRecords
