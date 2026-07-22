<template>
  <PageWrapper dense contentFullHeight class="command-param-page">
    <BasicTable @register="registerTable">
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
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/iot/link/productCommandResponse/productCommandResponse';
  import { columns, searchFormSchema } from './productCommandResponse.data';
  import EditModal from './Edit.vue';

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '产品设备响应命令属性',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
    },
    props: {
      serviceId: {
        type: String,
        default: '',
      },
      commandId: {
        type: String,
        default: '',
      },
    },
    setup(props) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.productCommandResponse.productCommandResponse.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'ProductCommandResponseSearch',
          labelWidth: 88,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          baseColProps: { xs: 24, sm: 12, md: 8, lg: 8, xl: 5, xxl: 5 },
          compact: true,
          showAdvancedButton: false,
          actionColOptions: {
            xs: 24,
            sm: 24,
            md: 24,
            lg: 24,
            xl: 4,
            xxl: 4,
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
        rowKey: 'id',
        searchInfo: {
          serviceId: props.serviceId,
          commandId: props.commandId,
        },
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
          commandId: props.commandId,
          serviceId: props.serviceId,
          type: ActionEnum.ADD,
        });
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.VIEW,
        });
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
      };
    },
  });
</script>
../../../../../api/iot/link/productCommandResponse/productCommandResponse
<style lang="less" scoped>
  .command-param-page {
    :deep(.thinglinks-basic-table-form-container),
    :deep(.vben-basic-table-form-container) {
      padding: 0;
    }

    :deep(.thinglinks-basic-table .ant-form),
    :deep(.vben-basic-table .ant-form) {
      padding: 10px 12px 2px;
      margin-bottom: 12px;
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

    :deep(.thinglinks-basic-table .ant-form-item-label > label),
    :deep(.vben-basic-table .ant-form-item-label > label) {
      white-space: nowrap;
    }

    :deep(.thinglinks-basic-table .ant-form-item-control-input),
    :deep(.vben-basic-table .ant-form-item-control-input) {
      min-height: 32px;
    }

    :deep(.thinglinks-basic-table .ant-input),
    :deep(.thinglinks-basic-table .ant-picker),
    :deep(.thinglinks-basic-table .ant-select-selector),
    :deep(.vben-basic-table .ant-input),
    :deep(.vben-basic-table .ant-picker),
    :deep(.vben-basic-table .ant-select-selector) {
      width: 100%;
      min-height: 32px;
    }
  }
</style>
