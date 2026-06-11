<template>
  <PageWrapper contentFullHeight dense>
    <BasicTable
      :isOtaUpgrades="true"
      :switchFlag="switchFlag"
      @register="registerTable"
      @switch-change="getSwitchChange"
    >
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['basic:link:otaUpgrades:delete']"
          color="error"
          preIcon="ant-design:delete-outlined"
          type="primary"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          v-hasAnyPermission="['basic:link:otaUpgrades:add']"
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #packageTypeColumn="{ record }">
        {{ getDictLabel('LINK_OTA_PACKAGES_TYPE', record?.packageType, '') }}
      </template>
      <template #statusColumn="{ record }">
        {{ getDictLabel('LINK_OTA_PACKAGES_STATUS', record?.status, '') }}
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
                auth: 'basic:link:otaUpgrades:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'basic:link:otaUpgrades:copy',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'basic:link:otaUpgrades:delete',
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
    <triggerRule @register="registerModalProduct" @success="handleSuccessProduct" />
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '/@/api/iot/link/ota/otaUpgrades';
  import { columns, searchFormSchema } from './otaUpgrades.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  import triggerRule from './modal/triggerRule.vue';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: 'OTA资源',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      triggerRule,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerModalProduct, { openModal: openModalProduct }] = useModal();

      // 表格
      const [registerTable, { reload, getSelectRowKeys, getForm }] = useTable({
        title: t('iot.link.ota.otaUpgrades.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'otaUpgrades',
          labelWidth: 120,
          schemas: searchFormSchema({
            productIdentification: (val: string) =>
              openModalProduct(true, { productIdentification: val, type: ActionEnum.EDIT }),
          }),
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

      //
      function switchView() {
        console.log('switchView');
        switchFlag.value = !switchFlag.value;
      }

      const switchFlag = ref<boolean>(true);

      function getSwitchChange(e) {
        switchFlag.value = e;
      }

      function handleSuccessProduct(val) {
        getForm()?.setFieldsValue({ productIdentification: val?.productIdentification });
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
        switchView,
        getSwitchChange,
        switchFlag,
        getDictLabel,
        handleSuccessProduct,
        registerModalProduct,
      };
    },
  });
</script>
<style lang="less">
  .pointer_input {
    .ant-input {
      cursor: pointer;
    }
  }
</style>
../../../../../api/iot/link/ota/otaUpgrades
