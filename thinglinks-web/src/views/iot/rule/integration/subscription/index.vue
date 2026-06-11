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
          nameField="sourceName"
          :nameFallback="t('iot.rule.integration.subscription.card.nameFallback')"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineValue="true"
          :statusOnlineLabel="t('iot.rule.integration.subscription.status.enabled')"
          :statusOfflineLabel="t('iot.rule.integration.subscription.status.disabled')"
          badgeField="targetHandler"
          :badgeDictType="DictEnum.BRIDGE_TARGET_HANDLER"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="handleSwitchByCard"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <DataBridgeSvg />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['rule:integration:subscription:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.rule.integration.subscription.switchView') }}
        </a-button>
      </template>

      <template #targetHandler="{ record }">
        <a-tag color="blue">
          {{ getDictLabel('BRIDGE_TARGET_HANDLER', record?.targetHandler, '') }}
        </a-tag>
      </template>

      <template #enable="{ record }">
        <a-tag :color="record?.enable ? 'success' : 'default'">
          {{
            record?.enable
              ? t('iot.rule.integration.subscription.status.enabled')
              : t('iot.rule.integration.subscription.status.disabled')
          }}
        </a-tag>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('iot.rule.integration.subscription.action.detail'),
                icon: 'ant-design:search-outlined',
                auth: 'rule:integration:subscription:view',
                onClick: handleView.bind(null, record),
              },
              {
                tooltip: record.enable
                  ? t('iot.rule.integration.subscription.status.disabled')
                  : t('iot.rule.integration.subscription.status.enabled'),
                icon: record.enable
                  ? 'ant-design:pause-circle-outlined'
                  : 'ant-design:play-circle-outlined',
                color: record.enable ? 'warning' : 'success',
                auth: 'rule:integration:subscription:toggle',
                onClick: handleToggleEnable.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'rule:integration:subscription:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                auth: 'rule:integration:subscription:delete',
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
  import { DictEnum } from '/@/enums/commonEnum';
  import {
    page,
    deleteSingle,
    changeStatus,
  } from '/@/api/iot/rule/integration/subscriptionSource';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './subscription.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  import { SubscriptionSourceResultVO } from '/@/api/iot/rule/integration/model/subscriptionSourceModel';
  import { DataBridgeSvg } from '/@/components/iot/integration/svg';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  defineOptions({ name: '订阅源' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const { detailRouteName, goDetail } = useDetailRoute();

  const switchFlag = ref<boolean>(true);
  const cardListRef = ref<any>(null);
  const cardFields = buildCardFields();

  const cardPermissions: CardPermissions = {
    add: 'rule:integration:subscription:add',
    edit: 'rule:integration:subscription:edit',
    delete: 'rule:integration:subscription:delete',
    view: 'rule:integration:subscription:view',
  };

  const cardExtraActions: CardAction[] = [
    {
      tooltip: t('iot.rule.integration.subscription.status.enabled'),
      icon: 'ant-design:play-circle-outlined',
      permission: 'rule:integration:subscription:toggle',
      event: 'toggle',
      disabled: (r: any) => r.enable === true,
    },
    {
      tooltip: t('iot.rule.integration.subscription.status.disabled'),
      icon: 'ant-design:pause-circle-outlined',
      permission: 'rule:integration:subscription:toggle',
      event: 'toggle',
      disabled: (r: any) => r.enable !== true,
    },
  ];

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('iot.rule.integration.subscription.table.title'),
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
      width: 220,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function handleView(record: SubscriptionSourceResultVO, e?: Event) {
    e?.stopPropagation();
    if (record?.id) goDetail(record.id);
  }

  function handleEdit(record: SubscriptionSourceResultVO, e?: Event) {
    e?.stopPropagation();
    openModal(true, { record, isUpdate: true });
  }

  async function handleDelete(record: SubscriptionSourceResultVO) {
    if (!record.id) return;
    if (record.enable) {
      createMessage.warning(t('iot.rule.integration.subscription.tips.deleteEnabled'));
      return;
    }
    await deleteSingle(record.id);
    createMessage.success(t('common.tips.deleteSuccess'));
    handleSuccess();
  }

  async function handleToggleEnable(record: SubscriptionSourceResultVO, e?: Event) {
    e?.stopPropagation();
    if (!record.id) return;
    const target = !record.enable;
    try {
      await changeStatus(record.id, target);
      createMessage.success(
        target
          ? t('iot.rule.integration.subscription.detail.enableSuccess')
          : t('iot.rule.integration.subscription.detail.disableSuccess'),
      );
      handleSuccess();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.subscription.tips.enableSourceRequired') +
          ': ' +
          (e?.message ?? ''),
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

  function handleCardExtraAction(payload: { event: string; record: SubscriptionSourceResultVO }) {
    if (payload.event === 'toggle') {
      handleToggleEnable(payload.record);
    }
  }

  watch(switchFlag, (newValue) => {
    if (newValue === false) reload();
  });
</script>
