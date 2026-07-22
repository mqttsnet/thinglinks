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
          :title="title"
          :searchData="searchData"
          nameField="streamIdentification"
          :nameFallback="t('common.undefinedText')"
          :fields="cardFields"
          statusField="status"
          :statusOnlineLabel="t('video.media.push.statusOnline')"
          :statusOfflineLabel="t('video.media.push.statusOffline')"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          @input="getSwitchChange"
        >
          <template #cardImage>
            <VideoStreamPushSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:media:push:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:media:push:add']"
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
                auth: 'video:media:push:edit',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:media:push:delete',
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
  import { useRouter } from 'vue-router';
  import { useDetailRoute } from '/@/hooks/web/usePage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import { VideoStreamPushSvg } from '/@/components/video';
  import type { CardField, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/video/media/push';
  import { columns, searchFormSchema } from './push.data';
  import EditModal from './Edit.vue';

  export default defineComponent({
    name: 'VideoMediaPush',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      VideoStreamPushSvg,
    },
    setup() {
      const { t } = useI18n();
      const { push } = useRouter();
      const { detailRouteName, goDetail } = useDetailRoute();
      const { createConfirm } = useMessage();
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      const cardFields: CardField[] = [
        { label: t('video.media.push.appId'), field: 'appId', dictType: DictEnum.VIDEO_APPLICATION_SCENARIO, span: 12 },
        { label: t('video.media.push.originType'), field: 'originType', dictType: DictEnum.VIDEO_MEDIA_ORIGIN_TYPE, span: 12 },
        { label: t('video.media.push.createdTime'), field: 'createdTime' },
        { label: t('video.media.push.mediaIdentification'), field: 'mediaIdentification' },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:media:push:add',
        edit: 'video:media:push:edit',
        delete: 'video:media:push:delete',
        view: 'video:media:push:view',
      };
      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.media.push.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'VideoStreamPushSearch',
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
      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 跳转详情页
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        goDetail(record.id);
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调
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
            } catch (e) {
              createMessage.error(t('common.tips.deleteFail'));
            }
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
