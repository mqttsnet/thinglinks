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
          nameField="configName"
          :fields="cardFields"
          badgeField="isDefaultLabel"
          statusField="status"
          :statusOnlineValue="1"
          :statusOnlineLabel="t('video.sip.config.enabled')"
          :statusOfflineLabel="t('video.sip.config.disabled')"
          :permissions="cardPermissions"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <ApiOutlined style="font-size: 40px; color: #1890ff" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          danger
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:sip:config:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:sip:config:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button
          preIcon="ant-design:sync-outlined"
          @click="handleRefreshCache"
          v-hasAnyPermission="['video:sip:config:refreshCache']"
        >
          {{ t('video.sip.config.refreshCache') }}
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
                auth: 'video:sip:config:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('video.sip.config.setDefault'),
                icon: 'ant-design:star-outlined',
                auth: 'video:sip:config:setDefault',
                ifShow: record.isDefault !== 1,
                popConfirm: {
                  title: t('video.sip.config.setDefaultConfirm'),
                  confirm: handleSetDefault.bind(null, record),
                },
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:sip:config:delete',
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
  import type { CardField, CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { ApiOutlined } from '@ant-design/icons-vue';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove, setDefault, refreshCache } from '/@/api/video/sip/sipConfig';
  import { columns, searchFormSchema } from './sipConfig.data';
  import EditModal from './Edit.vue';

  export default defineComponent({
    name: 'VideoSipConfig',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      ApiOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      // 包装 page API，为卡片视图添加 isDefaultLabel 字段（badge 显示用）
      const wrappedPageApi = async (params: any) => {
        const res = await page(params);
        if (res?.records) {
          res.records.forEach((r: any) => {
            r.isDefaultLabel = r.isDefault === 1 ? t('video.sip.config.defaultTag') : null;
          });
        }
        return res;
      };

      const cardFields: CardField[] = [
        { label: t('video.sip.config.sipId'), field: 'sipId', span: 24 },
        { label: t('video.sip.config.sipDomain'), field: 'sipDomain', span: 12 },
        { label: t('video.sip.config.sipServerAddress'), field: 'sipServerAddress', span: 12 },
        { label: t('video.sip.config.bindIp'), field: 'bindIp', span: 24 },
        { label: t('video.sip.config.createdTime'), field: 'createdTime', span: 24 },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:sip:config:add',
        edit: 'video:sip:config:edit',
        delete: 'video:sip:config:delete',
      };

      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('video.sip.config.setDefault'),
          icon: 'ant-design:star-outlined',
          event: 'setDefault',
          permission: 'video:sip:config:setDefault',
        },
        {
          tooltip: t('video.sip.config.refreshCache'),
          icon: 'ant-design:sync-outlined',
          event: 'refreshCache',
          permission: 'video:sip:config:refreshCache',
        },
      ];

      const handleCardExtraAction = ({ event, record }: { event: string; record: any }) => {
        if (event === 'setDefault') {
          handleSetDefault(record);
        } else if (event === 'refreshCache') {
          handleRefreshCache();
        }
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.sip.config.pageTitle'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'SipConfigSearch',
          labelWidth: 100,
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
        rowSelection: { type: 'checkbox', columnWidth: 40 },
        actionColumn: {
          width: 150,
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

      async function handleSetDefault(record: Recordable, e?: Event) {
        e?.stopPropagation();
        await setDefault(record.id);
        createMessage.success(t('video.sip.config.setDefaultSuccess'));
        handleSuccess();
      }

      async function handleRefreshCache() {
        await refreshCache();
        createMessage.success(t('video.sip.config.cacheRefreshed'));
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
        cardExtraActions,
        handleCardExtraAction,
        handleAdd,
        handleEdit,
        handleDelete,
        handleSetDefault,
        handleRefreshCache,
        handleBatchDelete,
        handleSuccess,
        switchFlag,
        getSwitchChange,
        switchView,
      };
    },
  });
</script>
