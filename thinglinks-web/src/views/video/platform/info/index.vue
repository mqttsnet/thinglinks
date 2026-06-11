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
          :deleteApi="remove"
          :title="title"
          :searchData="searchData"
          nameField="name"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineLabel="t('video.platform.info.enablePlatform')"
          :statusOfflineLabel="t('video.platform.info.disablePlatform')"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <ShareAltOutlined style="font-size: 40px; color: #1890ff" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:platform:info:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:platform:info:add']"
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
                auth: 'video:platform:info:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'video:platform:info:copy',
              },
              {
                tooltip: record.enable
                  ? t('video.platform.info.disablePlatform')
                  : t('video.platform.info.enablePlatform'),
                icon: record.enable
                  ? 'ant-design:stop-outlined'
                  : 'ant-design:check-circle-outlined',
                auth: 'video:platform:info:enable',
                popConfirm: {
                  title: record.enable
                    ? t('video.platform.info.disablePlatform') + '?'
                    : t('video.platform.info.enablePlatform') + '?',
                  confirm: handleToggleEnable.bind(null, record),
                },
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:platform:info:delete',
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
  import { ShareAltOutlined } from '@ant-design/icons-vue';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, setEnable } from '/@/api/video/platform/info';
  import { columns, searchFormSchema } from './info.data';
  import EditModal from './Edit.vue';
  import { useRouter } from 'vue-router';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'VideoPlatformInfo',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      ShareAltOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const { detailRouteName, goDetail } = useDetailRoute();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      const cardFields: CardField[] = [
        { label: t('video.platform.info.serverGbId'), field: 'serverGbId', span: 24 },
        { label: t('video.platform.info.serverIp'), field: 'serverIp', span: 12 },
        { label: t('video.platform.info.serverPort'), field: 'serverPort', span: 12 },
        { label: t('video.platform.info.transport'), field: 'transport', dictType: DictEnum.VIDEO_DEVICE_TRANSPORT, span: 12 },
        { label: t('video.platform.info.cascadeType'), field: 'cascadeType', dictType: DictEnum.VIDEO_PLATFORM_CASCADE_TYPE, span: 12 },
        { label: t('video.platform.info.createdTime'), field: 'createdTime' },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:platform:info:add',
        edit: 'video:platform:info:edit',
        delete: 'video:platform:info:delete',
        view: 'video:platform:info:view',
      };

      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('video.platform.info.enablePlatform') + '/' + t('video.platform.info.disablePlatform'),
          icon: 'ant-design:poweroff-outlined',
          event: 'toggleEnable',
          permission: 'video:platform:info:enable',
        },
      ];

      const handleCardExtraAction = ({ event, record }: { event: string; record: any }) => {
        if (event === 'toggleEnable') {
          handleToggleEnable(record);
        }
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.platform.info.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'VideoPlatformSearch',
          labelWidth: 140,
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

      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, { record, type: ActionEnum.COPY });
      }

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

      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      async function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          await remove([record.id]);
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

      async function handleToggleEnable(record: Recordable, e?: Event) {
        e?.stopPropagation();
        await setEnable(record.id, !record.enable);
        createMessage.success(t('common.tips.editSuccess'));
        handleSuccess();
        // 刷新卡片列表
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
        remove,
        detailRouteName,
        goDetail,
        EditModal,
        cardListRef,
        registerTable,
        registerModal,
        cardFields,
        cardPermissions,
        cardExtraActions,
        handleCardExtraAction,
        handleView,
        handleAdd,
        handleCopy,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        handleToggleEnable,
        switchFlag,
        getSwitchChange,
        switchView,
      };
    },
  });
</script>
