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
          nameField="gbDeviceId"
          :nameFallback="t('common.undefinedText')"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineLabel="t('video.gateway.mapping.enabled')"
          :statusOfflineLabel="t('video.gateway.mapping.disabled')"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          @input="getSwitchChange"
        >
          <template #cardImage>
            <VideoGatewayMappingSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:gateway:mapping:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:gateway:mapping:add']"
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
                auth: 'video:gateway:mapping:view',
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
                auth: 'video:gateway:mapping:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'video:gateway:mapping:add',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:gateway:mapping:delete',
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
  import { VideoGatewayMappingSvg } from '/@/components/video';
  import type { CardField, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '/@/api/video/gateway/mapping';
  import { columns, searchFormSchema } from './mapping.data';
  import EditModal from './Edit.vue';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'VideoGatewayMapping',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      VideoGatewayMappingSvg,
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
          label: t('video.gateway.mapping.srcProtocol'),
          field: 'srcProtocol',
          dictType: DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL,
          span: 12,
        },
        {
          label: t('video.gateway.mapping.autoPush'),
          field: 'autoPush',
          span: 12,
        },
        {
          label: t('video.gateway.mapping.srcDeviceIdentification'),
          field: 'srcDeviceIdentification',
        },
        {
          label: t('video.gateway.mapping.gbChannelId'),
          field: 'gbChannelId',
        },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:gateway:mapping:add',
        edit: 'video:gateway:mapping:edit',
        delete: 'video:gateway:mapping:delete',
        view: 'video:gateway:mapping:view',
      };

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.gateway.mapping.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'VideoGatewayMappingSearch',
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
          width: 200,
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
        handleAdd,
        handleView,
        handleEdit,
        handleCopy,
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
