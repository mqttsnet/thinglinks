<template>
  <BasicTable @register="registerTable">
    <template #toolbar>
      <a-button
        v-hasAnyPermission="[RoleEnum.TENANT_DEVELOPER_TOOLS_GENERATOR_EDIT_DELETE]"
        color="error"
        preIcon="ant-design:delete-outlined"
        type="primary"
        @click="handleBatchDelete"
      >
        {{ t('common.title.delete') }}
      </a-button>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'action'">
        <TableAction :actions="createActions(record, column)" :stopButtonPropagation="true" />
      </template>
    </template>
  </BasicTable>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { cloneDeep } from 'lodash-es';
  import {
    ActionItem,
    BasicTable,
    EditRecordRow,
    TableAction,
    useTable,
  } from '/@/components/Table';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    page,
    remove,
    syncField,
    update as updateColumn,
  } from '/@/api/devOperation/developer/defGenTableColumn';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { columnColumns, searchColumnFormSchema } from './defGenTableColumn.data';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { DefGenTableColumnUpdateVO } from '/@/api/devOperation/developer/model/defGenTableColumnModel';

  export default defineComponent({
    name: '修改代码配置',
    components: {
      BasicTable,
      TableAction,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();

      const currentEditKeyRef = ref('');
      const tableId = ref<string>('');

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: '字段',
        api: page,
        columns: columnColumns(),
        formConfig: {
          name: 'column_search',
          labelWidth: 120,
          schemas: searchColumnFormSchema(),
          autoSubmitOnEnter: true,
          resetButtonOptions: {
            preIcon: 'ant-design:rest-outlined',
          },
          submitButtonOptions: {
            preIcon: 'ant-design:search-outlined',
          },
        },
        defSort: {
          sort: 'sortValue',
          order: 'ascend',
        },
        searchInfo: {
          tableId: tableId,
        },
        immediate: false,
        beforeFetch: handleFetchParams,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        clickToRowSelect: false,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        scroll: { y: 500 },
        actionColumn: {
          width: 200,
          title: t('common.column.action'),
          dataIndex: 'action',
        },
      });

      function createActions(record: EditRecordRow): ActionItem[] {
        if (!record.editable) {
          return [
            {
              tooltip: t('common.title.edit'),
              icon: 'ant-design:edit-outlined',
              auth: RoleEnum.TENANT_DEVELOPER_TOOLS_GENERATOR_EDIT_EDIT,
              disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.key : false,
              onClick: handleEdit.bind(null, record),
            },
            {
              tooltip: t('common.title.delete'),
              icon: 'ant-design:delete-outlined',
              auth: RoleEnum.TENANT_DEVELOPER_TOOLS_GENERATOR_EDIT_DELETE,
              color: 'error',
              popConfirm: {
                title: t('common.tips.confirmDelete'),
                confirm: handleDelete.bind(null, record),
              },
            },
            {
              tooltip: '同步',
              auth: RoleEnum.TENANT_DEVELOPER_TOOLS_GENERATOR_EDIT_SYNC,
              icon: 'ant-design:cloud-sync-outlined',
              popConfirm: {
                title: '同步字段会重新读取数据库中字段信息，覆盖已修改的配置，确定同步该字段吗？',
                confirm: handleSync.bind(null, record),
              },
            },
          ];
        }
        return [
          {
            label: '保存',
            onClick: handleSave.bind(null, record),
          },
          {
            label: '取消',
            popConfirm: {
              title: '是否取消编辑',
              confirm: handleCancel.bind(null, record),
            },
          },
        ];
      }

      async function handleSync(record: EditRecordRow, e: Event) {
        e?.stopPropagation();
        await syncField(record.tableId, record.id);
        createMessage.success(t('devOperation.developer.defGenTableColumn.syncSuccess'));
        reload();
      }

      async function handleDelete(record: EditRecordRow, e: Event) {
        e?.stopPropagation();
        await remove(record.id);
        createMessage.success(t('common.tips.deleteSuccess'));
        reload();
      }

      async function handleSave(record: EditRecordRow, e: Event) {
        e?.stopPropagation();
        // 校验
        createMessage.loading({ content: '正在保存...', duration: 0, key: 'saving' });

        const valid = await record.onValid?.();
        if (valid) {
          const data = cloneDeep(record.editValueRefs) as unknown as DefGenTableColumnUpdateVO;
          const params = { ...unref(record), ...data };

          await updateColumn(params);

          // 保存之后提交编辑状态
          const pass = await record.onEdit?.(false, true);
          if (pass) {
            currentEditKeyRef.value = '';
          }
          createMessage.success('数据已保存');
        } else {
          createMessage.error('请填写正确的数据');
        }
      }

      async function handleCancel(record: EditRecordRow) {
        currentEditKeyRef.value = '';
        record.onEdit?.(false, false);
      }

      async function handleEdit(record: EditRecordRow) {
        currentEditKeyRef.value = record.key;
        record.onEdit?.(true);
      }

      async function load(tId: string) {
        if (tId) {
          tableId.value = tId;
          reload();
        } else {
          createMessage.warn(t('devOperation.developer.defGenTableColumn.notExist'));
        }
      }

      async function handleBatchDelete() {
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
              reload();
            } catch (e) {}
          },
        });
      }

      return {
        t,
        RoleEnum,
        registerTable,
        createActions,
        handleBatchDelete,
        load,
      };
    },
  });
</script>
