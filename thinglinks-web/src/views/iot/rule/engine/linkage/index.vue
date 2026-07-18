<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      @switch-change="getSwitchChange"
      :switchFlag="switchFlag"
      :isLinkAge="true"
    >
      <!-- 卡片视图(flexy 风格) ── 与产品列表同款 BusinessCardList,默认开启 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :title="title"
          :searchData="searchData"
          nameField="ruleName"
          :nameFallback="t('iot.link.engine.linkage.table.title')"
          :fields="cardFields"
          statusField="status"
          :statusResolver="resolveCardStatus"
          badgeField="effectiveType"
          :badgeDictType="DictEnum.RULE_EFFECTIVE_TYPE"
          :extraActions="cardExtraActions"
          detailRouteName="场景详情"
          @input="getSwitchChange"
          @view="handleView"
          @extraAction="handleCardExtraAction"
        >
          <!-- 联动规则无行选择依赖,卡片视图顶部只保留新增(批量删除留在表格视图) -->
          <template #headerExtra>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
              {{ t('common.title.add') }}
            </a-button>
          </template>
          <!-- 场景联动专属 flexy 插画(触发→规则→动作) -->
          <template #cardImage>
            <SceneLinkageSvg />
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
        <a-button preIcon="ant-design:swap-outlined" @click="switchView"
          >{{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #effectiveType="{ record }">
        {{ getDictLabel('RULE_EFFECTIVE_TYPE', record?.effectiveType, '') }}
      </template>
      <template #status="{ record }">
        <a-switch
          v-model:checked="record.status"
          @change="(checked) => toggleStatus(record, checked)"
          :checkedValue="1"
          :unCheckedValue="0"
          :loading="isStatusChanging(record)"
          :disabled="isStatusChanging(record)"
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
                tooltip: t('common.title.view') + t('iot.link.engine.linkage.log'),
                icon: 'ant-design:switcher-outlined',
                auth: 'rule:engine:linkage:executionLogDetail',
                onClick: openDetailDrawer.bind(null, record),
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
    <executionLogDetail @register="executionLogDetailRegister" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction } from '/@/components/BusinessCardList';
  import { SceneLinkageSvg } from '/@/components/iot/svg';
  import executionLogDetail from './executionLog/detail.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import {
    page,
    deleteSingle,
    remove,
    changeStatus,
  } from '../../../../../api/iot/rule/engine/linkage/linkage';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './linkage.data';
  import EditModal from './Edit.vue';
  import { useRouter } from 'vue-router';
  import { Button } from 'ant-design-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictLabel } = useDict();

  export default defineComponent({
    // 若需要开启页面缓存，请将此参数跟菜单名保持一致
    name: '场景联动',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      BusinessCardList,
      SceneLinkageSvg,
      EditModal,
      AButton: Button,
      executionLogDetail,
    },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const [executionLogDetailRegister, { openDrawer: openDetail }] = useDrawer();
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const cardListRef = ref<any>(null);
      const statusChangingIds = ref<Set<string>>(new Set());

      const getStatusChangingKey = (id: unknown) => String(id ?? '');
      const isStatusChanging = (record: Recordable) =>
        statusChangingIds.value.has(getStatusChangingKey(record?.id));
      const setStatusChanging = (id: unknown, changing: boolean) => {
        const key = getStatusChangingKey(id);
        if (!key) {
          return;
        }
        const next = new Set(statusChangingIds.value);
        changing ? next.add(key) : next.delete(key);
        statusChangingIds.value = next;
      };

      // 卡片视图配置 ── 与产品列表同款 flexy 风格
      const cardFields = buildCardFields();
      /** 卡片状态角标:启用(绿) / 停用(橙) */
      const resolveCardStatus = (record: Recordable) =>
        record?.status === 1
          ? { label: t('iot.link.engine.linkage.statusEnabled'), cls: 'online' }
          : { label: t('iot.link.engine.linkage.statusDisabled'), cls: 'offline' };
      /**
       * 卡片操作全部走 extraActions 显式声明(联动页历史上无按钮级权限点,
       * 走 BusinessCardList 内置 permissions 会因权限码不存在而隐藏按钮)。
       * 查看按钮由组件按详情路由(场景详情 /:id)自动显示,@view 里补 query 跳转。
       */
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.view') + t('iot.link.engine.linkage.log'),
          icon: 'ant-design:switcher-outlined',
          permission: 'rule:engine:linkage:executionLogDetail',
          event: 'log',
        },
        {
          tooltip: t('iot.link.engine.linkage.toggleStatus'),
          icon: 'ant-design:poweroff-outlined',
          color: 'warning',
          event: 'toggleStatus',
          handler: toggleCardStatus,
          disabled: isStatusChanging,
        },
        {
          tooltip: t('common.title.edit'),
          icon: 'ant-design:edit-outlined',
          event: 'edit',
        },
        {
          tooltip: t('common.title.delete'),
          icon: 'ant-design:delete-outlined',
          color: 'error',
          event: 'delete',
        },
      ];

      // 表格
      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.link.engine.linkage.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'ProductSearch',
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

      // 弹出查看页面(场景详情按 query.id 取数,这里统一补 query)
      function handleView(record: Recordable, e?: Event) {
        e?.stopPropagation();
        push({
          name: '场景详情',
          params: { id: record.id },
          query: { id: record.id, type: 'handleView' },
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable, e?: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      // 新增/编辑/启停/删除成功回调:只刷新当前视图,另一视图在切换时补刷(见 switchFlag watch)
      function handleSuccess() {
        if (switchFlag.value) {
          cardListRef.value?.reload();
        } else {
          reload();
        }
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

      // 点击单行删除(表格 popConfirm 已确认过;卡片路径走 confirmDelete)
      function handleDelete(record: Recordable, e?: Event) {
        e?.stopPropagation();
        if (record?.id) {
          handleDeleteSingle(record.id);
        }
      }

      /** 卡片删除:extraAction 无内置确认,这里补一致的二次确认 */
      function confirmDelete(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await handleDeleteSingle(record.id);
            } catch (e) {}
          },
        });
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

      const normalizeRuleStatus = (checked: boolean | number | undefined, fallback: unknown) => {
        if (checked === undefined) {
          return Number(fallback) === 1 ? 1 : 0;
        }
        return Number(checked) === 1 ? 1 : 0;
      };

      // 切换状态(表格 switch:v-model 已翻转 record.status,按变更值提交)
      async function toggleStatus(record: Recordable, checked?: boolean | number) {
        if (isStatusChanging(record)) {
          return;
        }
        const { id } = record;
        const status = normalizeRuleStatus(checked, record.status);
        setStatusChanging(id, true);
        try {
          await changeStatus(id, status);
          record.status = status;
          createMessage.success(t('common.tips.editSuccess'));
          handleSuccess();
        } catch (e) {
          handleSuccess();
        } finally {
          setStatusChanging(id, false);
        }
      }

      /** 卡片启停:record.status 未翻转,按目标状态提交 */
      async function toggleCardStatus(record: Recordable) {
        if (isStatusChanging(record)) {
          return;
        }
        const next = Number(record.status) === 1 ? 0 : 1;
        setStatusChanging(record.id, true);
        try {
          await changeStatus(record.id, next);
          record.status = next;
          createMessage.success(t('common.tips.editSuccess'));
          handleSuccess();
        } catch (e) {
          handleSuccess();
        } finally {
          setStatusChanging(record.id, false);
        }
      }

      /** 卡片视图额外操作派发(BusinessCardList @extraAction 回调) */
      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        const { event, record } = payload;
        switch (event) {
          case 'log':
            openDetailDrawer(record);
            break;
          case 'toggleStatus':
            toggleCardStatus(record);
            break;
          case 'edit':
            handleEdit(record);
            break;
          case 'delete':
            confirmDelete(record);
            break;
        }
      }

      // 切换视图 卡片(true=显示卡片) <-> 表格(false)。默认卡片(flexy 风格)
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      function getSwitchChange(e) {
        switchFlag.value = e;
      }
      // 视图切换时刷新新激活的视图,避免另一侧操作后的数据陈旧
      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        } else {
          cardListRef.value?.reload();
        }
      });
      // 查看执行日志
      const openDetailDrawer = (record: Recordable, e?: Event) => {
        e?.stopPropagation();
        openDetail(true, {
          ruleIdentification: record.ruleIdentification,
        });
      };
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
        isStatusChanging,
        executionLogDetailRegister,
        openDetailDrawer,
        // 卡片视图相关
        page,
        DictEnum,
        cardFields,
        cardExtraActions,
        cardListRef,
        resolveCardStatus,
        handleCardExtraAction,
      };
    },
  });
</script>
