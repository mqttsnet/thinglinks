<template>
  <PageWrapper contentFullHeight dense>
    <BasicTable :switchFlag="switchFlag" @register="registerTable" @switch-change="getSwitchChange">
      <!-- 卡片视图(Flexy)── 通用 BusinessCardList,与设备 / 北向集成等页面统一风格 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="packageName"
          :nameFallback="t('iot.link.ota.otaUpgrades.table.title')"
          :fields="cardFields"
          badgeField="packageType"
          :badgeDictType="DictEnum.LINK_OTA_PACKAGES_TYPE"
          :permissions="cardPermissions"
          :extraActions="cardExtraActions"
          @add="handleAdd"
          @view="handleViewDetail"
          @edit="handleEdit"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage="{ record }">
            <component :is="getOtaPackageTypeSvg(record?.packageType)" />
          </template>
        </BusinessCardList>
      </template>
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
                tooltip: t('common.title.details'),
                icon: 'ant-design:file-text-outlined',
                onClick: handleViewDetail.bind(null, record),
                auth: 'link:otaUpgrades:detail',
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
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardPermissions, CardAction } from '/@/components/BusinessCardList';
  import { getOtaPackageTypeSvg } from '/@/components/iot/ota/svg';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '/@/api/iot/link/ota/otaUpgrades';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './otaUpgrades.data';
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
      BusinessCardList,
    },
    setup() {
      const { t } = useI18n();
      const { push } = useRouter();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerModalProduct, { openModal: openModalProduct }] = useModal();

      // 卡片视图配置(Flexy)
      const cardListRef = ref<any>(null);
      const cardFields = buildCardFields();
      const cardPermissions: CardPermissions = {
        add: 'basic:link:otaUpgrades:add',
        edit: 'basic:link:otaUpgrades:edit',
        delete: 'basic:link:otaUpgrades:delete',
        view: 'link:otaUpgrades:detail',
      };
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.copy'),
          icon: 'ant-design:copy-outlined',
          permission: 'basic:link:otaUpgrades:copy',
          event: 'copy',
        },
      ];

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

      // 卡片额外操作(复制)
      function handleCardExtraAction({ event, record }: { event: string; record: Recordable }) {
        if (event === 'copy') {
          openModal(true, { record, type: ActionEnum.COPY });
        }
      }

      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 进入资源详情页(独立页面)── 表格 / 卡片的「详情」统一跳转,不再用弹窗查看
      function handleViewDetail(record: Recordable, e: Event) {
        e?.stopPropagation();
        push(`/link/otaUpgrades/${record.id}`);
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调:同时刷新表格视图与卡片视图
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
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
        handleViewDetail,
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
        // 卡片视图
        page,
        deleteSingle,
        cardListRef,
        cardFields,
        cardPermissions,
        cardExtraActions,
        handleCardExtraAction,
        getOtaPackageTypeSvg,
        DictEnum,
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
