<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="planName"
          :nameFallback="t('common.undefinedText')"
          :fields="cardFields"
          statusField="planStatus"
          :statusOnlineValue="'1'"
          :statusOnlineLabel="t('video.record.plan.enabled')"
          :statusOfflineLabel="t('video.record.plan.disabled')"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <VideoRecordPlanSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:record:plan:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:record:plan:add']"
        >
          {{ t('common.title.add') }}
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
                tooltip: t('common.title.view'),
                icon: 'ant-design:search-outlined',
                onClick: handleView.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
                auth: 'video:record:plan:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'video:record:plan:add',
              },
              {
                tooltip: record.planStatus === '1' ? t('video.record.plan.deactivate') : t('video.record.plan.activate'),
                icon: record.planStatus === '1' ? 'ant-design:pause-circle-outlined' : 'ant-design:play-circle-outlined',
                color: record.planStatus === '1' ? 'warning' : 'success',
                auth: 'video:record:plan:activate',
                popConfirm: record.planStatus === '1' ? {
                  title: t('video.record.plan.confirmDeactivate'),
                  confirm: handleToggleStatus.bind(null, record),
                } : undefined,
                onClick: record.planStatus !== '1' ? handleToggleStatus.bind(null, record) : undefined,
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:record:plan:delete',
                popConfirm: {
                  title: t('common.tips.confirmDelete'),
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import { VideoRecordPlanSvg } from '/@/components/video';
  import type { CardField, CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle, activate, deactivate } from '/@/api/video/record/plan';
  import { columns, searchFormSchema } from './plan.data';
  import EditModal from './Edit.vue';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'VideoRecordPlan',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      VideoRecordPlanSvg,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { detailRouteName, goDetail } = useDetailRoute();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      const cardFields: CardField[] = [
        {
          label: t('video.record.plan.planType'),
          field: 'planType',
          dictType: DictEnum.VIDEO_RECORD_PLAN_TYPE,
          span: 12,
        },
        {
          label: t('video.record.plan.recordFormat'),
          field: 'recordFormat',
          span: 12,
        },
        {
          label: t('video.record.plan.retentionDays'),
          field: 'retentionDays',
        },
        {
          label: t('thinglinks.common.createdTime'),
          field: 'createdTime',
        },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:record:plan:add',
        edit: 'video:record:plan:edit',
        delete: 'video:record:plan:delete',
        view: 'video:record:plan:view',
      };

      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('video.record.plan.activate'),
          icon: 'ant-design:play-circle-outlined',
          event: 'toggleStatus',
          permission: 'video:record:plan:activate',
        },
      ];

      const handleCardExtraAction = ({ event, record }: { event: string; record: any }) => {
        if (event === 'toggleStatus') {
          handleToggleStatus(record);
        }
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.record.plan.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'VideoRecordPlanSearch',
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
          width: 240,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      const getSwitchChange = (e) => {
        switchFlag.value = e;
      };

      function handleAdd() {
        openModal(true, { type: ActionEnum.ADD });
      }

      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        goDetail(record.id);
      }

      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, { record, type: ActionEnum.EDIT });
      }

      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, { record, type: ActionEnum.COPY });
      }

      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      async function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          await deleteSingle(record.id);
          createMessage.success(t('common.tips.deleteSuccess'));
          handleSuccess();
        }
      }

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
              await remove(ids);
              createMessage.success(t('common.tips.deleteSuccess'));
              handleSuccess();
            } catch (e) {}
          },
        });
      }

      async function handleToggleStatus(record: Recordable, e?: Event) {
        e?.stopPropagation();
        try {
          if (record.planStatus === '1') {
            await deactivate(record.id);
            createMessage.success(t('video.record.plan.deactivateSuccess'));
          } else {
            await activate(record.id);
            createMessage.success(t('video.record.plan.activateSuccess'));
          }
          handleSuccess();
        } catch (err) {
          createMessage.error(t('video.record.plan.activateFailed'));
        }
      }

      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        }
      });

      return {
        t,
        page,
        deleteSingle,
        EditModal,
        cardListRef,
        detailRouteName,
        goDetail,
        registerTable,
        registerModal,
        cardFields,
        cardPermissions,
        cardExtraActions,
        handleCardExtraAction,
        handleAdd,
        handleView,
        handleEdit,
        handleCopy,
        handleDelete,
        handleBatchDelete,
        handleToggleStatus,
        handleSuccess,
        switchFlag,
        getSwitchChange,
        switchView,
      };
    },
  });
</script>
