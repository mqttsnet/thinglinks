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
          nameField="deviceName"
          :nameFallback="t('common.undefinedText')"
          :fields="cardFields"
          statusField="onlineStatus"
          :statusOnlineLabel="t('video.device.info.online')"
          :statusOfflineLabel="t('video.device.info.offline')"
          badgeField="transport"
          badgeDictType="VIDEO_DEVICE_TRANSPORT"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          @input="getSwitchChange"
          @delete="handleCardDelete"
        >
          <template #cardImage>
            <VideoDeviceSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['video:device:info:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['video:device:info:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button
          preIcon="ant-design:radar-chart-outlined"
          @click="handleOnvifDiscover"
          v-hasAnyPermission="['video:device:info:add']"
        >
          {{ t('video.onvif.scan') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('common.switchView') }}
        </a-button>
      </template>
      <template #transport="{ record }">
        {{ getDictLabel('VIDEO_DEVICE_TRANSPORT', record?.transport, '') }}
      </template>
      <template #streamMode="{ record }">
        {{ getDictLabel('VIDEO_DEVICE_STREAM_MODE', record?.streamMode, '') }}
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
                auth: 'video:device:info:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'video:device:info:copy',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'video:device:info:delete',
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
    <OnvifDiscoverModal @register="registerOnvifModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { useDetailRoute } from '/@/hooks/web/usePage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import { VideoDeviceSvg } from '/@/components/video';
  import type { CardField, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/video/device/info';
  import { columns, searchFormSchema } from './info.data';
  import EditModal from './Edit.vue';
  import OnvifDiscoverModal from './OnvifDiscoverModal.vue';
  import { useDict } from '/@/components/Dict';
  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'VideoDeviceInfo',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      OnvifDiscoverModal,
      BusinessCardList,
      VideoDeviceSvg,
    },
    setup() {
      const { t } = useI18n();
      const { push } = useRouter();
      const { createConfirm, createMessage } = useMessage();
      const { detailRouteName, goDetail } = useDetailRoute();

      // 包装 API，避免后端未实现时白屏
      async function safePage(params: any) {
        try {
          return await page(params);
        } catch (e: any) {
          console.warn('videoDeviceInfo page error:', e);
          createMessage.error(e?.message || t('video.device.info.loadFailed'));
          return { records: [], total: 0 };
        }
      }
      const [registerModal, { openModal }] = useModal();
      const [registerOnvifModal, { openModal: openOnvifModal }] = useModal();
      function handleOnvifDiscover() {
        openOnvifModal(true, {});
      }
      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      const cardFields: CardField[] = [
        { label: t('video.device.info.manufacturer'), field: 'manufacturer', span: 12 },
        { label: t('video.device.info.host'), field: 'host', span: 12 },
        { label: t('video.device.info.createdTime'), field: 'createdTime' },
        { label: t('video.device.info.deviceIdentification'), field: 'deviceIdentification' },
      ];

      const cardPermissions: CardPermissions = {
        add: 'video:device:info:add',
        edit: 'video:device:info:edit',
        delete: 'video:device:info:delete',
        view: 'video:device:info:view',
      };
      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('video.device.info.table.title'),
        api: safePage,
        columns: columns(),
        formConfig: {
          name: 'VideoDeviceInfoSearch',
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
      // 弹出复制页面
      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.COPY,
        });
      }
      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出查看页面
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
            } catch (e) {}
          },
        });
      }
      // 卡片模式删除（因为 device API 没有 deleteSingle，需要用 remove([id])）
      function handleCardDelete(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            await remove([record.id]);
            createMessage.success(t('common.tips.deleteSuccess'));
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
        detailRouteName,
        goDetail,
        cardListRef,
        registerTable,
        registerModal,
        registerOnvifModal,
        handleOnvifDiscover,
        cardFields,
        cardPermissions,
        handleView,
        handleAdd,
        handleCopy,
        handleEdit,
        handleDelete,
        handleCardDelete,
        handleBatchDelete,
        handleSuccess,
        switchFlag,
        getSwitchChange,
        switchView,
        getDictLabel,
      };
    },
  });
</script>
