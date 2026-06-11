<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="name"
          :nameFallback="t('iot.rule.groovy.ruleGroovyScript.card.nameFallback')"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineValue="true"
          :statusOnlineLabel="t('iot.rule.groovy.ruleGroovyScript.enabled')"
          :statusOfflineLabel="t('iot.rule.groovy.ruleGroovyScript.disabled')"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="handleSuccess"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <GroovyScriptSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['rule:groovy:ruleGroovyScript:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['rule:groovy:ruleGroovyScript:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.rule.groovy.ruleGroovyScript.switchView') }}
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
                auth: 'rule:groovy:ruleGroovyScript:view',
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
                auth: 'rule:groovy:ruleGroovyScript:edit',
              },
              {
                tooltip: t('common.title.copy'),
                icon: 'ant-design:copy-outlined',
                onClick: handleCopy.bind(null, record),
                auth: 'rule:groovy:ruleGroovyScript:copy',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'rule:groovy:ruleGroovyScript:delete',
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
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import { GroovyScriptSvg } from '/@/components/iot/groovy';
  import type { CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { page, remove, deleteSingle } from '/@/api/iot/rule/groovy/ruleGroovyScript';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './ruleGroovyScript.data';
  import EditModal from './Edit.vue';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  export default defineComponent({
    // 与菜单名一致以启用页面缓存
    name: '规则脚本',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      BusinessCardList,
      GroovyScriptSvg,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const { detailRouteName, goDetail } = useDetailRoute();

      const switchFlag = ref<boolean>(true);
      const cardListRef = ref<any>(null);

      const cardFields = buildCardFields();

      const cardPermissions: CardPermissions = {
        add: 'rule:groovy:ruleGroovyScript:add',
        edit: 'rule:groovy:ruleGroovyScript:edit',
        delete: 'rule:groovy:ruleGroovyScript:delete',
        view: 'rule:groovy:ruleGroovyScript:view',
      };

      /**
       * 卡片视图额外操作：复制脚本（编辑/删除/详情已内置在 BusinessCardList 操作区）。
       */
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.copy'),
          icon: 'ant-design:copy-outlined',
          permission: 'rule:groovy:ruleGroovyScript:copy',
          event: 'copy',
        },
      ];

      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        if (payload.event === 'copy') {
          handleCopy(payload.record);
        }
      }

      const [registerTable, { reload, getSelectRowKeys }] = useTable({
        title: t('iot.rule.groovy.ruleGroovyScript.table.title'),
        api: page,
        immediate: false,
        columns: columns(),
        formConfig: {
          name: 'RuleGroovyScriptSearch',
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

      function handleAdd() {
        openModal(true, { type: ActionEnum.ADD });
      }

      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        goDetail(record.id);
      }

      function handleEdit(record: Recordable, e: Event) {
        e?.stopPropagation();
        openModal(true, { record, type: ActionEnum.EDIT });
      }

      /**
       * 复制脚本：以 COPY 模式打开新增弹窗，字段回显但 ID 置空，
       * 用户调整归属字段（scriptType / channelCode 等）后保存为新脚本。
       */
      function handleCopy(record: Recordable, e?: Event) {
        e?.stopPropagation();
        openModal(true, { record, type: ActionEnum.COPY });
      }

      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      async function batchDelete(ids: string[]) {
        await remove(ids);
        createMessage.success(t('common.tips.deleteSuccess'));
        handleSuccess();
      }

      function handleDelete(record: Recordable, e: Event) {
        e?.stopPropagation();
        if (record?.id) {
          batchDelete([record.id]);
        }
      }

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

      function getSwitchChange(e: boolean) {
        switchFlag.value = e;
      }

      watch(switchFlag, (newValue) => {
        if (newValue === false) {
          reload();
        }
      });

      return {
        t,
        page,
        deleteSingle,
        EditModal,
        cardListRef,
        detailRouteName,
        registerTable,
        registerModal,
        cardFields,
        cardPermissions,
        cardExtraActions,
        switchFlag,
        switchView,
        getSwitchChange,
        handleAdd,
        handleView,
        handleEdit,
        handleCopy,
        handleDelete,
        handleBatchDelete,
        handleSuccess,
        handleCardExtraAction,
      };
    },
  });
</script>
