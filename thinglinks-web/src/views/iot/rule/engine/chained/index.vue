<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      @switch-change="getSwitchChange"
      :switchFlag="switchFlag"
      :isChained="true"
    >
      <template #toolbar>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button>
        <a-button
          type="primary"
          color="error"
          disabled
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #type="{ record }">
        {{ getDictLabel('RULE_INSTANCE_TYPE', record?.type, '') }}
      </template>
      <template #status="{ record }">
        <a-switch
          v-model:checked="record.status"
          @change="toggleStatus(record)"
          :checkedValue="1"
          :unCheckedValue="0"
        />
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
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import {
    page,
    deleteSingle,
    changeStatus,
  } from '../../../../../api/iot/rule/engine/chained/chained';
  import { columns, searchFormSchema } from './chained.data';
  import EditModal from './Edit.vue';
  import { useRouter } from 'vue-router';
  import { Button } from 'ant-design-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '链式规则',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      AButton: Button,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { replace } = useRouter();

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.engine.chained.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'ChainedSearch',
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
      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record.status == 0) {
          return;
        }
        replace({
          name: '规则流节点',
          params: {
            id: record.id,
            flowId: record.flowId,
            instanceAddress: record.instanceAddress,
          },
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
        // await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      const handleDeleteSingle = async (id: string) => {
        await deleteSingle(id);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      };

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
      // 切换状态
      async function toggleStatus(record) {
        let { id, status } = record;
        status = record.status ? 1 : 0;
        await changeStatus(id, status);
        createMessage.success(t('common.tips.editSuccess'));
        handleSuccess();
      }
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      function getSwitchChange(e) {
        switchFlag.value = e;
      }
      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        }
      });
      return {
        t,
        registerTable,
        registerModal,
        handleView,
        handleAdd,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        switchFlag,
        switchView,
        getSwitchChange,
        getDictLabel,
        toggleStatus,
      };
    },
  });
</script>
../../../../../api/iot/link/chained/chained
