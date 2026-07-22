<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <!-- 使用通用卡片视图插槽 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="name"
          :fields="cardFields"
          statusField="onlineStatus"
          :statusOnlineLabel="t('video.media.server.online')"
          :statusOfflineLabel="t('video.media.server.offline')"
          badgeField="type"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          @input="getSwitchChange"
        >
          <template #cardImage>
            <VideoServerSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:media:server:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:media:server:add']"
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
                auth: 'video:media:server:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'video:media:server:copy',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:media:server:delete',
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
  import { VideoServerSvg } from '/@/components/video';
  import type { CardField, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '/@/api/video/media/server';
  import { columns, searchFormSchema } from './server.data';
  import EditModal from './Edit.vue';
  import { useRouter } from 'vue-router';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'VideoMediaServer',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      VideoServerSvg,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm } = useMessage();
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const { detailRouteName, goDetail } = useDetailRoute();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      // 卡片视图字段配置
      const cardFields: CardField[] = [
        { label: t('video.media.server.appId'), field: 'appId', dictType: DictEnum.VIDEO_APPLICATION_SCENARIO, span: 12 },
        { label: t('video.media.server.host'), field: 'host', span: 12 },
        { label: t('video.media.server.createdTime'), field: 'createdTime' },
        { label: t('video.media.server.mediaIdentification'), field: 'mediaIdentification' },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:media:server:add',
        edit: 'video:media:server:edit',
        delete: 'video:media:server:delete',
        view: 'video:media:server:view',
      };

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.media.server.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'VideoMediaServerSearch',
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

      async function handleDeleteSingle(id: string) {
        await deleteSingle(id);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          handleDeleteSingle(record.id);
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
              await batchDelete(ids);
            } catch (e) {}
          },
        });
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
        handleView,
        handleAdd,
        handleCopy,
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
