<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable @register="registerTable" @switch-change="getSwitchChange" :switchFlag="switchFlag">
      <!-- 卡片视图(flexy 风格) ── 通过 BasicTable 的 cardView slot 注入,默认开启 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="productName"
          :nameFallback="t('iot.link.product.product.table.title')"
          :fields="cardFields"
          statusField="productStatus"
          :statusOnlineValue="0"
          :statusOnlineLabel="t('iot.link.product.product.statusEnabled')"
          :statusOfflineLabel="t('iot.link.product.product.statusDisabled')"
          badgeField="productType"
          :badgeDictType="DictEnum.LINK_PRODUCT_TYPE"
          :permissions="cardPermissions"
          :extraActions="cardExtraActions"
          @input="getSwitchChange"
          @add="handleAdd"
          @view="handleView"
          @edit="handleEdit"
          @delete="handleDelete"
          @extraAction="handleCardExtraAction"
        >
          <!-- 卡片视图工具栏补充产品特有顶层动作:快捷生成 / 导入(批量删除依赖行选择,卡片视图用每卡删除) -->
          <template #headerExtra>
            <a-button
              v-hasPermission="['link:product:product:quick']"
              type="primary"
              preIcon="ant-design:plus-outlined"
              @click="onQuick"
            >
              {{ t('iot.link.device.device.quick') }}
            </a-button>
            <a-button
              v-hasPermission="['link:product:product:import']"
              type="primary"
              preIcon="ant-design:download-outlined"
              @click="handleImport"
            >
              {{ t('common.title.import') }}
            </a-button>
          </template>
          <template #cardImage="{ record }">
            <!-- 优先用产品自定义 icon;没有就按 productType 回退到内联 SVG(flexy 风格,与桥接规则同款) -->
            <ThumbUrl
              v-if="record?.icon"
              :fileId="record.icon"
              :imageStyle="{ 'max-width': '90px', 'max-height': '90px' }"
            />
            <component v-else :is="getProductTypeSvg(record?.productType)" />
          </template>
        </BusinessCardList>
      </template>

      <template #toolbar>
        <a-button
          v-hasPermission="['link:product:product:delete']"
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          v-hasPermission="['link:product:product:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button
          v-hasPermission="['link:product:product:quick']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="onQuick"
        >
          {{ t('iot.link.device.device.quick') }}
        </a-button>
        <a-button
          v-hasPermission="['link:product:product:import']"
          type="primary"
          preIcon="ant-design:download-outlined"
          @click="handleImport"
          >{{ t('common.title.import') }}</a-button
        >
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #productTypeColumn="{ record }">
        {{ getDictLabel('LINK_PRODUCT_TYPE', record?.productType, '') }}
      </template>
      <template #productStatus="{ record }">
        {{ getDictLabel('LINK_PRODUCT_STATUS', record?.productStatus, '') }}
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'icon'">
          <ThumbUrl :fileId="record.icon" :imageStyle="{ 'max-height': '104px' }" />
        </template>
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('common.title.view'),
                icon: 'ant-design:search-outlined',
                auth: 'link:product:product:view',
                onClick: handleView.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'link:product:product:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('iot.link.product.product.action.publish'),
                icon: 'ant-design:rocket-outlined',
                color: 'success',
                auth: 'link:product:product:publish',
                onClick: handlePublish.bind(null, record),
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                auth: 'link:product:product:copy',
                onClick: handleCopy.bind(null, record),
              },
              {
                tooltip: t('common.title.export'),
                icon: 'ant-design:export-outlined',
                auth: 'link:product:product:export',
                onClick: handleExport.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'link:product:product:delete',
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
    <ImportModal @register="importModal" @reload="reload" />
    <PublishModal @register="registerPublishModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { getProductTypeSvg } from '/@/components/iot/svg';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { downloadFile } from '/@/utils/file/download.ts';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove, exportJson, deleteSingle } from '/@/api/iot/link/product/product';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './product.data';
  import EditModal from './Edit.vue';
  import ImportModal from './ImportModal.vue';
  import PublishModal from '../detail/PublishModal.vue';
  import { useRouter } from 'vue-router';
  import { Button } from 'ant-design-vue';
  import { useDict } from '/@/components/Dict';
  import { usePermission } from '/@/hooks/web/usePermission';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '产品模型',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      BusinessCardList,
      EditModal,
      ImportModal,
      PublishModal,
      AButton: Button,
      ThumbUrl,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const { hasPermission } = usePermission();
      const [registerModal, { openModal }] = useModal();
      const [importModal, { openModal: openImportModal }] = useModal();
      const [registerPublishModal, { openModal: openPublishModal }] = useModal();
      const { replace } = useRouter();
      const cardListRef = ref<any>(null);

      // 卡片视图配置 ── 与桥接规则同款 flexy 风格
      const cardFields = buildCardFields();
      const cardPermissions: CardPermissions = {
        add: 'link:product:product:add',
        edit: 'link:product:product:edit',
        delete: 'link:product:product:delete',
        view: 'link:product:product:view',
      };
      /** 卡片视图额外操作:发布版本 / 复制 / 导出(详情/编辑/删除已内置在 BusinessCardList) */
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('iot.link.product.product.action.publish'),
          icon: 'ant-design:rocket-outlined',
          permission: 'link:product:product:publish',
          event: 'publish',
        },
        {
          tooltip: t('common.title.copy'),
          icon: 'ant-design:copy-outlined',
          permission: 'link:product:product:copy',
          event: 'copy',
        },
        {
          tooltip: t('common.title.export'),
          icon: 'ant-design:export-outlined',
          permission: 'link:product:product:export',
          event: 'export',
        },
      ];

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.product.product.table.title'),
        api: page,
        columns: columns(),
        formConfig: {
          name: 'ProductSearch',
          labelWidth: 90,
          // 第一行 4 个 span 6 字段先展示;超出由"展开"按钮控制(flexy 风格)
          autoAdvancedLine: 1,
          showAdvancedButton: true,
          schemas: searchFormSchema(),
          autoSubmitOnEnter: true,
          baseColProps: { span: 6 },
          compact: true,
          resetButtonOptions: { preIcon: 'ant-design:rest-outlined' },
          submitButtonOptions: { preIcon: 'ant-design:search-outlined' },
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
      // 导出JSON
      async function handleExport(record: Recordable, e: Event) {
        e?.stopPropagation();
        try {
          const response = await exportJson(record.productIdentification);
          downloadFile(response);
          createMessage.success(t('common.tips.exportSuccess'));
        } catch (error) {
          console.error('导出失败:', error);
          createMessage.error(t('common.tips.exportFail'));
        }
      }
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
        if (!hasPermission('link:product:product:view')) {
          createMessage.warning(t('sys.api.operationFailed'));
          return;
        }
        replace({
          name: '产品详情',
          params: { id: record.id },
        });
      }

      function onQuick(e: Event) {
        e?.stopPropagation();
        replace({
          name: '快捷生成',
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

      /** 打开发布弹窗,把当前产品基础信息塞进去。 */
      function handlePublish(record: Recordable, e: Event) {
        e?.stopPropagation();
        openPublishModal(true, {
          productIdentification: record.productIdentification,
          productName: record.productName,
          currentVersion: record.activeVersionNo,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      /** 卡片视图额外操作派发(BusinessCardList @extraAction 回调) */
      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        const e = new Event('synthetic');
        switch (payload.event) {
          case 'publish':
            handlePublish(payload.record, e);
            break;
          case 'copy':
            handleCopy(payload.record, e);
            break;
          case 'export':
            handleExport(payload.record, e);
            break;
        }
      }

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      // 删除单个
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

      // 切换视图 卡片(true=显示卡片) <-> 表格(false)。默认卡片(flexy 风格)
      const switchFlag = ref<boolean>(true);
      function switchView() {
        console.log('switchView');
        switchFlag.value = !switchFlag.value;
      }

      function getSwitchChange(e) {
        switchFlag.value = e;
      }

      const handleImport = () => {
        openImportModal(true);
      };
      return {
        t,
        registerTable,
        registerModal,
        registerPublishModal,
        handleView,
        handleAdd,
        handleCopy,
        handleEdit,
        handlePublish,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        switchFlag,
        switchView,
        getSwitchChange,
        getDictLabel,
        onQuick,
        handleExport,
        importModal,
        handleImport,
        reload,
        // 卡片视图相关
        page,
        deleteSingle,
        DictEnum,
        EditModal,
        cardFields,
        cardPermissions,
        cardExtraActions,
        cardListRef,
        handleCardExtraAction,
        getProductTypeSvg,
      };
    },
  });
</script>
../../../../../api/iot/link/product/product
