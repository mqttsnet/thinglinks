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
          :pageApi="wrappedPageApi"
          :deleteApi="remove"
          :title="title"
          :searchData="searchData"
          nameField="subscriptionName"
          :fields="cardFields"
          badgeField="channelTypeLabel"
          statusField="status"
          :statusOnlineValue="1"
          :statusOnlineLabel="t('video.device.subscription.enabled')"
          :statusOfflineLabel="t('video.device.subscription.disabled')"
          :permissions="cardPermissions"
          :editModal="EditModal"
          @input="getSwitchChange"
        >
          <template #cardImage>
            <BellOutlined style="font-size: 40px; color: #722ed1" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          danger
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:device:alarm:subscription:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:device:alarm:subscription:add']"
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
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'video:device:alarm:subscription:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:device:alarm:subscription:delete',
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
  import type { CardField, CardPermissions } from '/@/components/BusinessCardList';
  import { BellOutlined } from '@ant-design/icons-vue';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/video/device/notifySubscription';
  import { columns, searchFormSchema } from './subscription.data';
  import EditModal from './Edit.vue';

  // 事件类型 → i18n key
  const EVENT_TYPE_KEYS: Record<string, string> = {
    ALARM: 'video.device.subscription.event.ALARM',
    DEVICE_ONLINE: 'video.device.subscription.event.DEVICE_ONLINE',
    DEVICE_OFFLINE: 'video.device.subscription.event.DEVICE_OFFLINE',
    STREAM_CLOSE: 'video.device.subscription.event.STREAM_CLOSE',
  };

  // 接收范围 → i18n key
  const RECIPIENT_SCOPE_KEYS: Record<string, string> = {
    SELF: 'video.device.subscription.scope.SELF',
    ORG: 'video.device.subscription.scope.ORG',
    CUSTOM: 'video.device.subscription.scope.CUSTOM',
  };

  export default defineComponent({
    name: 'VideoNotifySubscription',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      BellOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      // 包装 page API，翻译 eventTypes / recipientScope / channelType
      const wrappedPageApi = async (params: any) => {
        const res = await page(params);
        if (res?.records) {
          res.records.forEach((r: any) => {
            // 翻译事件类型: "ALARM,DEVICE_ONLINE" → "告警, 设备上线"
            if (r.eventTypes) {
              r.eventTypesLabel = r.eventTypes
                .split(',')
                .map((s: string) => {
                  const k = EVENT_TYPE_KEYS[s.trim()];
                  return k ? t(k) : s.trim();
                })
                .join(', ');
            }
            // 翻译接收范围
            const scopeKey = RECIPIENT_SCOPE_KEYS[r.recipientScope];
            r.recipientScopeLabel = scopeKey ? t(scopeKey) : r.recipientScope || '-';
            // 渠道类型（用 echoMap 回显）
            r.channelTypeLabel = r.echoMap?.channelType || r.channelType || null;
          });
        }
        return res;
      };

      const cardFields: CardField[] = [
        { label: t('video.device.subscription.eventType'), field: 'eventTypesLabel', span: 24 },
        { label: t('video.device.subscription.receiverScope'), field: 'recipientScopeLabel', span: 12 },
        { label: t('video.device.subscription.messageTemplate'), field: 'templateCode', span: 12 },
        { label: t('video.device.subscription.createdTime'), field: 'createdTime', span: 24 },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:device:alarm:subscription:add',
        edit: 'video:device:alarm:subscription:edit',
        delete: 'video:device:alarm:subscription:delete',
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.device.subscription.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'NotifySubscriptionSearch',
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
        rowSelection: { type: 'checkbox', columnWidth: 40 },
        actionColumn: {
          width: 120,
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

      function handleEdit(record: Recordable, e?: Event) {
        e?.stopPropagation();
        openModal(true, { type: ActionEnum.EDIT, record });
      }

      async function handleDelete(record: Recordable, e?: Event) {
        e?.stopPropagation();
        if (record?.id) {
          await remove([record.id]);
          createMessage.success(t('common.tips.deleteSuccess'));
          handleSuccess();
        }
      }

      function handleBatchDelete() {
        const keys = getSelectRowKeys();
        if (!keys?.length) {
          createMessage.warning(t('common.tips.pleaseSelectTheData'));
          return;
        }
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            await remove(keys as string[]);
            createMessage.success(t('common.tips.deleteSuccess'));
            handleSuccess();
          },
        });
      }

      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
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
        wrappedPageApi,
        remove,
        EditModal,
        cardListRef,
        registerTable,
        registerModal,
        cardFields,
        cardPermissions,
        handleAdd,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        switchFlag,
        getSwitchChange,
        switchView,
      };
    },
  });
</script>
