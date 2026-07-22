<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable @register="registerTable" :switchFlag="switchFlag" @switch-change="getSwitchChange">
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="ruleName"
          :nameFallback="t('iot.rule.integration.bridge.card.nameFallback')"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineValue="true"
          :statusOnlineLabel="t('iot.rule.integration.bridge.status.enabled')"
          :statusOfflineLabel="t('iot.rule.integration.bridge.status.disabled')"
          badgeField="direction"
          :badgeDictType="DictEnum.BRIDGE_DIRECTION"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="handleSwitchByCard"
          @extra-action="handleCardExtraAction"
        >
          <template #cardImage>
            <DataBridgeSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['rule:integration:bridge:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.rule.integration.bridge.switchView') }}
        </a-button>
      </template>

      <template #ruleName="{ record }">
        <div class="rule-name-cell">
          <DataBridgeSvg class="rule-svg" />
          <span>{{ record?.ruleName }}</span>
        </div>
      </template>

      <template #direction="{ record }">
        <a-tag :color="getDirectionColor(record?.direction)">
          {{ getDictLabel('BRIDGE_DIRECTION', record?.direction, '') }}
        </a-tag>
      </template>

      <template #dataSourceName="{ record }">
        <span v-if="record?.dataSourceCode || record?.dataSourceName">
          {{ record?.dataSourceCode }}
          <span v-if="record?.dataSourceName" class="ds-name-secondary">
            ({{ record?.dataSourceName }})
          </span>
        </span>
        <span v-else>{{ record?.dataSourceId || '-' }}</span>
      </template>

      <template #enable="{ record }">
        <a-tag :color="record?.enable ? 'success' : 'default'">
          {{
            record?.enable
              ? t('iot.rule.integration.bridge.status.enabled')
              : t('iot.rule.integration.bridge.status.disabled')
          }}
        </a-tag>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('iot.rule.integration.bridge.action.detail'),
                icon: 'ant-design:search-outlined',
                auth: 'rule:integration:bridge:view',
                onClick: handleView.bind(null, record),
              },
              {
                tooltip: t('iot.rule.integration.bridge.action.test'),
                icon: 'ant-design:thunderbolt-outlined',
                auth: 'rule:integration:bridge:test',
                onClick: handleTestSink.bind(null, record),
              },
              {
                tooltip: record.enable
                  ? t('iot.rule.integration.bridge.action.disable')
                  : t('iot.rule.integration.bridge.action.enable'),
                icon: record.enable
                  ? 'ant-design:pause-circle-outlined'
                  : 'ant-design:play-circle-outlined',
                color: record.enable ? 'warning' : 'success',
                auth: record.enable
                  ? 'rule:integration:bridge:disable'
                  : 'rule:integration:bridge:enable',
                onClick: handleToggleEnable.bind(null, record),
              },
              {
                tooltip: t('iot.rule.integration.bridge.action.copy'),
                icon: 'ant-design:copy-outlined',
                auth: 'rule:integration:bridge:copy',
                onClick: handleCopy.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'rule:integration:bridge:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                auth: 'rule:integration:bridge:delete',
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
    <TestSinkModal @register="registerTestModal" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { page, deleteSingle, changeStatus } from '/@/api/iot/rule/integration/dataBridge';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './bridge.data';
  import EditModal from './Edit.vue';
  import TestSinkModal from './TestSinkModal.vue';
  import { useDict } from '/@/components/Dict';
  import { DataBridgeResultVO } from '/@/api/iot/rule/integration/model/dataBridgeModel';
  import { DataBridgeSvg } from '/@/components/iot/integration/svg';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  defineOptions({ name: '桥接规则' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const { detailRouteName, goDetail } = useDetailRoute();

  const switchFlag = ref<boolean>(true);
  const cardListRef = ref<any>(null);
  const cardFields = buildCardFields();

  const cardPermissions: CardPermissions = {
    add: 'rule:integration:bridge:add',
    edit: 'rule:integration:bridge:edit',
    delete: 'rule:integration:bridge:delete',
    view: 'rule:integration:bridge:view',
  };

  /**
   * 卡片视图额外操作：测试发送 + 启停 + 复制（详情/编辑/删除已内置在 BusinessCardList）。
   */
  const cardExtraActions: CardAction[] = [
    {
      tooltip: t('iot.rule.integration.bridge.action.test'),
      icon: 'ant-design:thunderbolt-outlined',
      permission: 'rule:integration:bridge:test',
      event: 'test',
    },
    {
      tooltip: t('iot.rule.integration.bridge.action.enable'),
      icon: 'ant-design:play-circle-outlined',
      permission: 'rule:integration:bridge:enable',
      event: 'toggle',
      color: 'success',
      disabled: (r: any) => r.enable === true,
    },
    {
      tooltip: t('iot.rule.integration.bridge.action.disable'),
      icon: 'ant-design:pause-circle-outlined',
      permission: 'rule:integration:bridge:disable',
      event: 'toggle',
      color: 'warning',
      disabled: (r: any) => r.enable !== true,
    },
    {
      tooltip: t('iot.rule.integration.bridge.action.copy'),
      icon: 'ant-design:copy-outlined',
      permission: 'rule:integration:bridge:copy',
      event: 'copy',
    },
  ];

  const [registerModal, { openModal }] = useModal();
  const [registerTestModal, { openModal: openTestModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('iot.rule.integration.bridge.table.title'),
    api: page,
    immediate: false,
    rowKey: 'id',
    columns: columns(),
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema(),
      autoSubmitOnEnter: true,
      resetButtonOptions: { preIcon: 'ant-design:rest-outlined' },
      submitButtonOptions: { preIcon: 'ant-design:search-outlined' },
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
    beforeFetch: handleFetchParams,
    actionColumn: {
      width: 280,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function handleView(record: DataBridgeResultVO, e?: Event) {
    e?.stopPropagation();
    if (record?.id) goDetail(record.id);
  }

  function handleEdit(record: DataBridgeResultVO, e?: Event) {
    e?.stopPropagation();
    openModal(true, { record, isUpdate: true });
  }

  /**
   * 复制规则：以 COPY 模式打开新增弹窗，字段回显但 ID 置空，
   * 用户改唯一编码字段（ruleName / ruleCode 等）后保存为新规则。
   */
  function handleCopy(record: DataBridgeResultVO, e?: Event) {
    e?.stopPropagation();
    openModal(true, { record, type: ActionEnum.COPY });
  }

  async function handleDelete(record: DataBridgeResultVO) {
    if (!record.id) return;
    if (record.enable) {
      createMessage.warning(t('iot.rule.integration.bridge.tips.deleteEnabled'));
      return;
    }
    await deleteSingle(record.id);
    createMessage.success(t('common.tips.deleteSuccess'));
    handleSuccess();
  }

  function handleTestSink(record: DataBridgeResultVO, e?: Event) {
    e?.stopPropagation();
    openTestModal(true, { record });
  }

  async function handleToggleEnable(record: DataBridgeResultVO, e?: Event) {
    e?.stopPropagation();
    if (!record.id) return;
    const target = !record.enable;
    try {
      await changeStatus(record.id, target);
      createMessage.success(
        target
          ? t('iot.rule.integration.bridge.detail.enableSuccess')
          : t('iot.rule.integration.bridge.detail.disableSuccess'),
      );
      handleSuccess();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.bridge.tips.enableMustTestPass') + ': ' + (e?.message ?? ''),
      );
    }
  }

  function handleSuccess() {
    reload();
    cardListRef.value?.reload();
  }

  function switchView() {
    switchFlag.value = !switchFlag.value;
  }

  function getSwitchChange(e: boolean) {
    switchFlag.value = e;
  }

  function handleSwitchByCard(e: boolean) {
    switchFlag.value = e;
  }

  function handleCardExtraAction(payload: { event: string; record: DataBridgeResultVO }) {
    switch (payload.event) {
      case 'test':
        handleTestSink(payload.record);
        break;
      case 'toggle':
        handleToggleEnable(payload.record);
        break;
      case 'copy':
        handleCopy(payload.record);
        break;
    }
  }

  function getDirectionColor(direction?: string): string {
    switch (direction) {
      case '10':
        return 'green';
      case '20':
        return 'blue';
      default:
        return 'default';
    }
  }

  watch(switchFlag, (newValue) => {
    if (newValue === false) reload();
  });
</script>

<style lang="less" scoped>
  .rule-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .rule-svg {
      width: 24px;
      height: 24px;
      flex-shrink: 0;
    }
  }

  .ds-name-secondary {
    color: #8c97a5;
    font-size: 12px;
    margin-left: 4px;
  }
</style>
