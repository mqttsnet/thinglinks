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
          nameField="dataSourceName"
          :nameFallback="t('iot.rule.integration.datasource.card.nameFallback')"
          :fields="cardFields"
          statusField="enable"
          :statusOnlineValue="true"
          :statusOnlineLabel="t('iot.rule.integration.datasource.status.enabled')"
          :statusOfflineLabel="t('iot.rule.integration.datasource.status.disabled')"
          badgeField="sourceType"
          :badgeDictType="DictEnum.BRIDGE_DATA_SOURCE_TYPE"
          :permissions="cardPermissions"
          :detailRouteName="detailRouteName"
          :editModal="EditModal"
          :extraActions="cardExtraActions"
          @input="handleSwitchByCard"
          @extra-action="handleCardExtraAction"
        >
          <template #cardImage="{ record }">
            <component :is="getSourceTypeSvg(record?.sourceType)" />
          </template>
        </BusinessCardList>
      </template>
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['rule:integration:datasource:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.rule.integration.datasource.switchView') }}
        </a-button>
      </template>

      <template #sourceType="{ record }">
        <div class="source-type-cell">
          <component :is="getSourceTypeSvg(record?.sourceType)" class="source-svg" />
          <span>{{ getDictLabel('BRIDGE_DATA_SOURCE_TYPE', record?.sourceType, '') }}</span>
        </div>
      </template>

      <template #direction="{ record }">
        <a-tag :color="getDirectionColor(record?.direction)">
          {{ getDictLabel('BRIDGE_DIRECTION', record?.direction, '') }}
        </a-tag>
      </template>

      <template #healthStatus="{ record }">
        <a-badge
          :status="getHealthBadgeStatus(record?.healthStatus)"
          :text="getDictLabel('BRIDGE_HEALTH_STATUS', record?.healthStatus, '-')"
        />
      </template>

      <template #enable="{ record }">
        <a-tag :color="record?.enable ? 'success' : 'default'">
          {{
            record?.enable
              ? t('iot.rule.integration.datasource.status.enabled')
              : t('iot.rule.integration.datasource.status.disabled')
          }}
        </a-tag>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('iot.rule.integration.datasource.action.detail'),
                icon: 'ant-design:search-outlined',
                onClick: handleView.bind(null, record),
              },
              {
                tooltip: t('iot.rule.integration.datasource.action.test'),
                icon: 'ant-design:thunderbolt-outlined',
                auth: 'rule:integration:datasource:test',
                onClick: handleTestConnection.bind(null, record),
              },
              {
                tooltip: record.enable
                  ? t('iot.rule.integration.datasource.action.disable')
                  : t('iot.rule.integration.datasource.action.enable'),
                icon: record.enable
                  ? 'ant-design:pause-circle-outlined'
                  : 'ant-design:play-circle-outlined',
                color: record.enable ? 'warning' : 'success',
                auth: 'rule:integration:datasource:toggle',
                onClick: handleToggleEnable.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'rule:integration:datasource:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                auth: 'rule:integration:datasource:delete',
                color: 'error',
                popConfirm: {
                  title: t('iot.rule.integration.datasource.tips.deleteConfirm'),
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
    testConnection,
    changeStatus,
  } from '/@/api/iot/rule/integration/dataSource';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './datasource.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  import { DataSourceResultVO } from '/@/api/iot/rule/integration/model/dataSourceModel';
  import { getSourceTypeSvg } from '/@/components/iot/integration/svg';
  import { useDetailRoute } from '/@/hooks/web/usePage';

  defineOptions({ name: '数据源管理' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const { detailRouteName, goDetail } = useDetailRoute();

  const switchFlag = ref<boolean>(true);
  const cardListRef = ref<any>(null);
  const cardFields = buildCardFields();

  const cardPermissions: CardPermissions = {
    add: 'rule:integration:datasource:add',
    edit: 'rule:integration:datasource:edit',
    delete: 'rule:integration:datasource:delete',
    view: 'rule:integration:datasource:view',
  };

  /**
   * 卡片视图额外操作：测试连接 + 启停（编辑/删除/详情都内置在 BusinessCardList 操作区）。
   */
  const cardExtraActions: CardAction[] = [
    {
      tooltip: t('iot.rule.integration.datasource.action.test'),
      icon: 'ant-design:thunderbolt-outlined',
      permission: 'rule:integration:datasource:test',
      event: 'test',
    },
    {
      tooltip: t('iot.rule.integration.datasource.action.enable'),
      icon: 'ant-design:play-circle-outlined',
      permission: 'rule:integration:datasource:toggle',
      event: 'toggle',
      color: 'success',
      disabled: (r: any) => r.enable === true,
    },
    {
      tooltip: t('iot.rule.integration.datasource.action.disable'),
      icon: 'ant-design:pause-circle-outlined',
      permission: 'rule:integration:datasource:toggle',
      event: 'toggle',
      color: 'warning',
      disabled: (r: any) => r.enable !== true,
    },
  ];

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('iot.rule.integration.datasource.table.title'),
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
      width: 240,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function handleView(record: DataSourceResultVO, e?: Event) {
    e?.stopPropagation();
    if (record?.id) goDetail(record.id);
  }

  function handleEdit(record: DataSourceResultVO, e?: Event) {
    e?.stopPropagation();
    openModal(true, { record, isUpdate: true });
  }

  async function handleDelete(record: DataSourceResultVO) {
    if (!record.id) return;
    if (record.enable) {
      createMessage.warning(t('iot.rule.integration.datasource.tips.deleteEnabled'));
      return;
    }
    await deleteSingle(record.id);
    createMessage.success(t('common.tips.deleteSuccess'));
    handleSuccess();
  }

  async function handleTestConnection(record: DataSourceResultVO) {
    if (!record.id) return;
    // ⚠ 本项目 createMessage.success/error 只接受 string（Flexy 通知样式），
    // 不能像原生 ant-design Message 那样传 {content, key}。
    // loading 是直通 ant-design Message，调用返回 hide 函数；用它显式销毁加载提示。
    const hideLoading = (createMessage.loading as any)(
      t('iot.rule.integration.datasource.tips.testing'),
      0,
    );
    try {
      const ok = await testConnection(String(record.id));
      hideLoading?.();
      if (ok) {
        createMessage.success(t('iot.rule.integration.datasource.tips.testSuccess'));
      } else {
        createMessage.error(t('iot.rule.integration.datasource.tips.testFailed'));
      }
    } catch (e: any) {
      hideLoading?.();
      const msg = e?.response?.data?.msg ?? e?.message ?? '';
      createMessage.error(
        t('iot.rule.integration.datasource.tips.testFailed') + (msg ? ': ' + msg : ''),
      );
    }
  }

  async function handleToggleEnable(record: DataSourceResultVO) {
    if (!record.id) return;
    const target = !record.enable;
    try {
      await changeStatus(record.id, target);
      createMessage.success(
        target
          ? t('iot.rule.integration.datasource.detail.enableSuccess')
          : t('iot.rule.integration.datasource.detail.disableSuccess'),
      );
      handleSuccess();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.datasource.tips.enableMustTestPass') + ': ' + (e?.message ?? ''),
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

  function handleCardExtraAction(payload: { event: string; record: DataSourceResultVO }) {
    if (payload.event === 'test') {
      handleTestConnection(payload.record);
    } else if (payload.event === 'toggle') {
      handleToggleEnable(payload.record);
    }
  }

  function getDirectionColor(direction?: string): string {
    switch (direction) {
      case '10':
        return 'green';
      case '20':
        return 'blue';
      case '30':
        return 'orange';
      default:
        return 'default';
    }
  }

  function getHealthBadgeStatus(
    status?: string,
  ): 'success' | 'processing' | 'warning' | 'error' | 'default' {
    switch (status) {
      case 'HEALTHY':
        return 'success';
      case 'DEGRADED':
        return 'warning';
      case 'DOWN':
        return 'error';
      default:
        return 'default';
    }
  }

  watch(switchFlag, (newValue) => {
    if (newValue === false) reload();
  });
</script>

<style lang="less" scoped>
  .source-type-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .source-svg {
      width: 28px;
      height: 28px;
      flex-shrink: 0;
    }
  }
</style>
