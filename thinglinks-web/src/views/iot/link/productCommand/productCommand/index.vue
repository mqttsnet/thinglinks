<template>
  <div class="command-container model-list-container">
    <BasicTable
      class="model-list-table"
      @register="registerTable"
      ref="basicTableRef"
      @switch-change="getSwitchChange"
      :switchFlag="switchFlag"
    >
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :title="title || '命令列表'"
          :searchData="{ ...searchData, serviceId: currentServiceId }"
          nameField="commandName"
          :nameFallback="t('iot.link.productCommand.productCommand.commandName')"
          :fields="cardFields"
          variant="model"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extra-action="handleCardExtraAction"
        >
          <template #headerExtra>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
              {{ t('common.title.add') }}
            </a-button>
          </template>
          <template #cardImage>
            <ProductModelCardSvg variant="command" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
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
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
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
    <DetailModal @register="registerDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, toRef, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { ProductModelCardSvg } from '/@/components/iot/svg';
  import { page, remove } from '/@/api/iot/link/productCommand/productCommand';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './productCommand.data';
  import EditModal from './Edit.vue';
  import DetailModal from './Detail.vue';

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '产品模型设备服务命令表维护',
    components: {
      BasicTable,
      TableAction,
      BusinessCardList,
      ProductModelCardSvg,
      EditModal,
      DetailModal,
    },
    props: {
      serviceId: {
        type: String,
        default: '',
      },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const currentServiceId = toRef(props, 'serviceId');
      const basicTableRef = ref<any>(null);
      const cardListRef = ref<any>(null);
      const cardFields = buildCardFields();
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.view'),
          icon: 'ant-design:search-outlined',
          event: 'view',
        },
        {
          tooltip: t('common.title.edit'),
          icon: 'ant-design:edit-outlined',
          event: 'edit',
        },
        {
          tooltip: t('common.title.copy'),
          icon: 'ant-design:copy-outlined',
          event: 'copy',
        },
        {
          tooltip: t('common.title.delete'),
          icon: 'ant-design:delete-outlined',
          event: 'delete',
          color: 'error',
        },
      ];
      const switchFlag = ref<boolean>(true);
      watch(currentServiceId, () => {
        reload();
        cardListRef.value?.reload();
      });
      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: '命令列表',
        api: page,
        columns: columns(),
        formConfig: {
          name: 'ProductCommandSearch',
          labelWidth: 72,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          baseColProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 6, xxl: 6 },
          compact: true,
          actionColOptions: {
            xs: 24,
            sm: 24,
            md: 24,
            lg: 24,
            xl: 6,
            xxl: 6,
            style: { textAlign: 'right' },
          },
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
        searchInfo: {
          serviceId: currentServiceId,
        },
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        actionColumn: {
          width: 180,
          title: t('common.column.action'),
          dataIndex: 'action',
          fixed: 'right',
        },
        scroll: { x: 1280 },
      });

      function getTablePopupContainer() {
        return basicTableRef.value?.$el ?? document.body;
      }

      // 弹出复制页面
      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.COPY,
          getContainer: getTablePopupContainer,
        });
      }
      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          serviceId: currentServiceId.value,
          type: ActionEnum.ADD,
          getContainer: getTablePopupContainer,
        });
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.VIEW,
          getContainer: getTablePopupContainer,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
          getContainer: getTablePopupContainer,
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

      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        const event = new Event('click');
        switch (payload.event) {
          case 'view':
            handleView(payload.record, event);
            break;
          case 'edit':
            handleEdit(payload.record, event);
            break;
          case 'copy':
            handleCopy(payload.record, event);
            break;
          case 'delete':
            createConfirm({
              iconType: 'warning',
              content: t('common.tips.confirmDelete'),
              onOk: async () => batchDelete([payload.record.id]),
            });
            break;
        }
      }

      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      function getSwitchChange(e: boolean) {
        switchFlag.value = e;
      }

      watch(switchFlag, (newValue) => {
        if (newValue === false) reload();
      });

      return {
        t,
        registerTable,
        registerModal,
        handleView,
        handleAdd,
        handleCopy,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        registerDrawer,
        basicTableRef,
        cardListRef,
        cardFields,
        cardExtraActions,
        handleCardExtraAction,
        switchFlag,
        switchView,
        getSwitchChange,
        page,
        currentServiceId,
      };
    },
  });
</script>
<style lang="less" scoped>
  .command-container {
    height: 100%;
  }

  .model-list-container {
    :deep(.thinglinks-basic-table-form-container),
    :deep(.vben-basic-table-form-container) {
      padding: 0;
    }

    :deep(.thinglinks-basic-table .ant-form),
    :deep(.vben-basic-table .ant-form) {
      padding: 10px 12px 2px;
      margin-bottom: 10px;
      border: 1px solid @border-color-base;
      border-radius: 8px;
    }

    :deep(.thinglinks-basic-table .ant-form-item),
    :deep(.vben-basic-table .ant-form-item) {
      margin-bottom: 8px !important;
    }

    :deep(.thinglinks-basic-table .ant-form-item-label),
    :deep(.vben-basic-table .ant-form-item-label) {
      padding-right: 6px;
    }

    :deep(.thinglinks-basic-table .ant-form-item-control-input),
    :deep(.vben-basic-table .ant-form-item-control-input) {
      min-height: 32px;
    }

    :deep(.thinglinks-basic-table .ant-input),
    :deep(.thinglinks-basic-table .ant-select-selector),
    :deep(.vben-basic-table .ant-input),
    :deep(.vben-basic-table .ant-select-selector) {
      min-height: 32px;
    }
  }
</style>
