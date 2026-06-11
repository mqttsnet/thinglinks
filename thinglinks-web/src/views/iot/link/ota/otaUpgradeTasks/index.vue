<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      @switch-change="getSwitchChange"
      :switchFlag="switchFlag"
      :isOtaUpgradeTasks="true"
    >
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['basic:link:otaUpgradeTasks:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['basic:link:otaUpgradeTasks:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction :actions="getTableActions(record)" :stopButtonPropagation="true" />
        </template>
      </template>
    </BasicTable>
    <EditModal @register="registerDrawer" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useDrawer } from '/@/components/Drawer';
  import { useRouter } from 'vue-router';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { PermModeEnum } from '/@/enums/roleEnum';
  import { page, remove, deleteSingle } from '/@/api/iot/link/ota/otaUpgradeTasks';
  import { columns, searchFormSchema } from './otaUpgradeTasks.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  import { usePermission } from '/@/hooks/web/usePermission';

  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: 'OTA升级任务',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const { push } = useRouter();
      const { hasAnyPermission } = usePermission();
      const [registerDrawer, { openDrawer }] = useDrawer();

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.ota.otaUpgradeTasks.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'OtaUpgradeTasksSearch',
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

      // 弹出复制页面
      function handleCopy(record: Recordable, e: Event) {
        e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.COPY,
        });
      }
      // 弹出新增页面
      function handleAdd() {
        openDrawer(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.VIEW,
        });
      }

      // 点击升级包列进入详情页
      function handleViewDetail(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (
          !hasAnyPermission([
            'link:otaUpgradeTasks:detail:view',
            'link:otaUpgradeTasks:detail:records',
          ])
        )
          return;
        push(`/link/otaUpgradeTasks/${record.id}`);
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openDrawer(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
      }

      // 删除单条数据
      const handleDeleteSingle = async (id: string) => {
        await deleteSingle(id);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      };

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      // 点击单行删除
      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          handleDeleteSingle(record.id);
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
      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      const switchFlag = ref<boolean>(true);

      function getSwitchChange(e) {
        switchFlag.value = e;
      }

      // 获取表格操作按钮
      function getTableActions(record: Recordable) {
        return [
          {
            tooltip: t('common.title.details'),
            icon: 'ant-design:file-text-outlined',
            onClick: handleViewDetail.bind(null, record),
            auth: ['link:otaUpgradeTasks:detail:view', 'link:otaUpgradeTasks:detail:records'],
            authMode: PermModeEnum.HasAny,
          },
          {
            tooltip: t('common.title.view'),
            icon: 'ant-design:search-outlined',
            onClick: handleView.bind(null, record),
          },
          {
            tooltip: t('common.title.edit'),
            icon: 'ant-design:edit-outlined',
            onClick: handleEdit.bind(null, record),
            auth: 'basic:link:otaUpgradeTasks:edit',
          },
          {
            tooltip: t('common.title.copy'),
            icon: 'ant-design:copy-outlined',
            onClick: handleCopy.bind(null, record),
            auth: 'basic:link:otaUpgradeTasks:copy',
          },
          {
            tooltip: t('common.title.delete'),
            icon: 'ant-design:delete-outlined',
            color: 'error',
            auth: 'basic:link:otaUpgradeTasks:delete',
            popConfirm: {
              title: t('common.tips.confirmDelete'),
              confirm: handleDelete.bind(null, record),
            },
          },
        ];
      }

      return {
        t,
        registerTable,
        registerDrawer,
        handleView,
        handleAdd,
        handleCopy,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        switchView,
        getSwitchChange,
        switchFlag,
        getDictLabel,
        handleViewDetail,
        hasAnyPermission,
        getTableActions,
      };
    },
  });
</script>
../../../../../api/iot/link/ota/otaUpgradeTasks
