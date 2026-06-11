<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <!-- 卡片视图 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="ruleName"
          :nameFallback="t('iot.link.operationMaintenance.accessControl.deviceAclRule.card.nameFallback')"
          :fields="cardFields"
          statusField="enabled"
          :statusOnlineValue="true"
          :statusOnlineLabel="t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.enabled')"
          :statusOfflineLabel="t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.disabled')"
          badgeField="ruleLevel"
          :badgeDictType="DictEnum.LINK_ACL_RULE_LEVEL"
          :permissions="cardPermissions"
          detailRouteName="ACL规则详情"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="handleSwitchByCard"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <DeviceAclRuleSvg />
          </template>
        </BusinessCardList>
      </template>

      <!-- 工具栏 -->
      <template #toolbar>
        <a-button
          type="primary"
          color="error"
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
          v-hasAnyPermission="['link:accessControl:deviceAclRule:delete']"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['link:accessControl:deviceAclRule:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>

      <!-- 列定制 -->
      <template #ruleLevel="{ record }">
        <a-tag :color="record?.ruleLevel === 1 ? 'cyan' : 'blue'">
          {{ getDictLabel('LINK_ACL_RULE_LEVEL', record?.ruleLevel, '') }}
        </a-tag>
      </template>
      <template #actionType="{ record }">
        <a-tag :color="getActionTypeColor(record?.actionType)">
          {{ getDictLabel('LINK_ACL_RULE_ACTION_TYPE', record?.actionType, '') }}
        </a-tag>
      </template>
      <template #decision="{ record }">
        <a-tag :color="isAllow(record?.decision) ? 'success' : 'error'">
          {{
            isAllow(record?.decision)
              ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.allow')
              : t('iot.link.operationMaintenance.accessControl.deviceAclRule.deny')
          }}
        </a-tag>
      </template>
      <template #enabled="{ record }">
        <a-tag :color="isAllow(record?.enabled) ? 'processing' : 'default'">
          {{
            isAllow(record?.enabled)
              ? t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.enabled')
              : t('iot.link.operationMaintenance.accessControl.deviceAclRule.status.disabled')
          }}
        </a-tag>
      </template>

      <!-- 行操作 -->
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
                auth: 'link:accessControl:deviceAclRule:edit',
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'link:accessControl:deviceAclRule:delete',
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

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { useDict } from '/@/components/Dict';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, remove } from '/@/api/iot/link/operationMaintenance/accessControl/deviceAclRule';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './deviceAclRule.data';
  import EditModal from './Edit.vue';
  import DeviceAclRuleSvg from './components/DeviceAclRuleSvg.vue';

  const { t } = useI18n();
  const { push } = useRouter();
  const { createMessage, createConfirm } = useMessage();
  const { getDictLabel } = useDict();
  const [registerModal, { openModal }] = useModal();

  defineOptions({ name: 'ACL规则' });

  // ============================== 视图切换 ==============================
  const switchFlag = ref<boolean>(true);
  function switchView() {
    switchFlag.value = !switchFlag.value;
  }
  function getSwitchChange(e: boolean) {
    switchFlag.value = e;
  }
  /** BusinessCardList 内部"切回表格视图"按钮触发(emit 'input', false) */
  function handleSwitchByCard(e: boolean) {
    switchFlag.value = e;
  }
  // 切回表格时刷新数据(对齐 datasource 模块标准实现)
  watch(switchFlag, (newValue) => {
    if (newValue === false) reload();
  });

  // ============================== Card 视图 ==============================
  const cardListRef = ref<any>(null);
  const cardFields = buildCardFields();
  const cardPermissions: CardPermissions = {
    add: 'link:accessControl:deviceAclRule:add',
    edit: 'link:accessControl:deviceAclRule:edit',
    delete: 'link:accessControl:deviceAclRule:delete',
    view: 'link:accessControl:deviceAclRule:view',
  };
  const cardExtraActions: CardAction[] = [
    {
      tooltip: t('iot.link.operationMaintenance.accessControl.deviceAclRule.action.copy'),
      icon: 'ant-design:copy-outlined',
      event: 'copy',
    },
  ];

  /**
   * BusinessCardList 单条删除桥接 ── 后端 remove 接口接受 ids 数组。
   */
  async function deleteSingle(id: any) {
    return remove([String(id)]);
  }

  function handleCardExtraAction(payload: { action: string; record: any }) {
    if (payload.action === 'copy') {
      // 复制规则:用现有 record 反填新增表单
      openModal(true, {
        record: { ...payload.record, id: undefined, ruleName: `${payload.record.ruleName}_copy` },
        type: ActionEnum.ADD,
      });
    }
  }

  // ============================== 列表 ==============================
  const [registerTable, { reload, getSelectRowKeys }] = useTable({
    title: t('iot.link.operationMaintenance.accessControl.deviceAclRule.table.title'),
    api: page,
    columns: columns(),
    formConfig: {
      name: 'DeviceAclRuleSearch',
      labelWidth: 120,
      schemas: searchFormSchema(),
      autoSubmitOnEnter: true,
      resetButtonOptions: { preIcon: 'ant-design:rest-outlined' },
      submitButtonOptions: { preIcon: 'ant-design:search-outlined' },
    },
    beforeFetch: handleFetchParams,
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    rowSelection: { type: 'checkbox', columnWidth: 40 },
    actionColumn: {
      width: 160,
      title: t('common.column.action'),
      dataIndex: 'action',
      fixed: 'right',
    },
    scroll: { x: 1600 },
  });

  // ============================== 表格列工具 ==============================
  function isAllow(v: any): boolean {
    return v === '1' || v === 1 || v === true;
  }

  function getActionTypeColor(actionType: number | string | undefined): string {
    // 0-全部 / 1-发布 / 2-订阅 / 3-取消订阅
    const map: Record<string, string> = {
      '0': 'purple',
      '1': 'blue',
      '2': 'cyan',
      '3': 'orange',
    };
    return map[String(actionType)] || 'default';
  }

  // ============================== 事件 ==============================
  function handleAdd() {
    openModal(true, { type: ActionEnum.ADD });
  }

  function handleView(record: Recordable, e: Event) {
    e?.stopPropagation();
    push({ name: 'ACL规则详情', params: { id: record.id } });
  }

  function handleEdit(record: Recordable, e: Event) {
    e?.stopPropagation();
    openModal(true, { record, type: ActionEnum.EDIT });
  }

  function handleSuccess() {
    reload();
    cardListRef.value?.reload?.();
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
</script>

<style lang="less" scoped>
  :deep(.mono-code) {
    font-family: Menlo, Monaco, 'Courier New', monospace;
    font-size: 12px;
    background: #f5f5f5;
    padding: 1px 6px;
    border-radius: 3px;
    color: #d4380d;
  }
</style>
