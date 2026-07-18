<template>
  <div class="property-container model-list-container">
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
          :title="title || '属性列表'"
          :searchData="{ ...searchData, serviceId: currentServiceId }"
          nameField="propertyName"
          :nameFallback="t('iot.link.productProperty.productProperty.propertyName')"
          :fields="cardFields"
          variant="model"
          badgeField="datatype"
          :badgeDictType="DictEnum.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @extra-action="handleCardExtraAction"
        >
          <template #headerExtra>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
              {{ t('common.title.add') }}
            </a-button>
          </template>
          <template #cardImage="{ record }">
            <ThumbUrl
              v-if="record?.icon"
              :fileId="record.icon"
              :imageStyle="{ 'max-width': '44px', 'max-height': '44px' }"
            />
            <ProductModelCardSvg v-else variant="property" />
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
        <template v-if="column.dataIndex === 'icon'">
          <ThumbUrl
            v-if="record.icon"
            :fileId="record.icon"
            :imageStyle="{ 'max-width': '44px', 'max-height': '44px' }"
          />
          <ProductModelCardSvg v-else class="property-container__table-icon" variant="property" />
        </template>
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
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, toRef, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { ProductModelCardSvg } from '/@/components/iot/svg';
  import { page, remove } from '/@/api/iot/link/productProperty/productProperty';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './productProperty.data';
  import EditModal from './Edit.vue';
  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '产品服务属性',
    components: {
      BasicTable,
      TableAction,
      ThumbUrl,
      BusinessCardList,
      ProductModelCardSvg,
      EditModal,
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
        title: '属性列表',
        api: page,
        columns: columns(),
        formConfig: {
          name: 'ProductPropertySearch',
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
        scroll: { x: 2100 },
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
        openModal(true, {
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
        DictEnum,
      };
    },
  });
</script>
<style lang="less" scoped>
  .property-container {
    height: 100%;

    &__table-icon {
      width: 44px;
      height: 44px;
    }
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
