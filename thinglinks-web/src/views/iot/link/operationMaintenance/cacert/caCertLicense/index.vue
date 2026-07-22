<template>
  <PageWrapper dense contentFullHeight>
    <BasicTable
      @register="registerTable"
      :switchFlag="switchFlag"
      @switch-change="getSwitchChange"
    >
      <!-- 卡片视图 (与 iot/rule/integration/bridge/index.vue 同款配置) -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="certName"
          :nameFallback="t(`${tNs}.card.nameFallback`)"
          :fields="cardFieldsCfg"
          statusField="state"
          :statusOnlineValue="1"
          :statusOnlineLabel="t(`${tNs}.status.issued`)"
          :statusOfflineLabel="t(`${tNs}.status.other`)"
          badgeField="algorithm"
          :badgeDictType="DictEnum.LINK_CA_CERT_ALGORITHM"
          :permissions="cardPermissions"
          :extraActions="cardExtraActions"
          @input="handleSwitchByCard"
          @add="handleImport"
          @edit="handleEdit"
          @extraAction="handleCardExtraAction"
        >
          <template #cardImage>
            <CaCertLicenseSvg />
          </template>
        </BusinessCardList>
      </template>

      <!-- 工具栏 -->
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['link:cacert:caCertLicense:add']"
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleImport"
        >
          {{ t(`${tNs}.importTitle`) }}
        </a-button>
        <a-button
          v-hasAnyPermission="['link:cacert:caCertLicense:delete']"
          danger
          preIcon="ant-design:delete-outlined"
          @click="handleBatchDelete"
        >
          {{ t('common.title.delete') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t(`${tNs}.switchView`) }}
        </a-button>
      </template>

      <!-- 列自定义渲染 -->
      <template #certName="{ record }">
        <div class="cert-name-cell">
          <CaCertLicenseSvg class="cert-svg" />
          <span>{{ record?.certName }}</span>
        </div>
      </template>

      <template #algorithm="{ record }">
        <a-tag color="blue">
          {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, record?.algorithm, '-') }}
        </a-tag>
      </template>

      <template #state="{ record }">
        <a-tag :color="getStateColor(record?.state)">
          {{ getDictLabel(DictEnum.LINK_CA_CERT_STATUS, record?.state, '-') }}
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
                tooltip: t(`${tNs}.action.downloadPack`),
                icon: 'ant-design:download-outlined',
                auth: 'link:cacert:caCertLicense:downloadPack',
                disabled: record?.state !== 1,
                onClick: handleDownloadPack.bind(null, record),
              },
              {
                tooltip: t(`${tNs}.action.testSsl`),
                icon: 'ant-design:thunderbolt-outlined',
                auth: 'link:cacert:caCertLicense:sslTest',
                onClick: handleSslTest.bind(null, record),
              },
              {
                tooltip: t(`${tNs}.action.viewImpact`),
                icon: 'ant-design:cluster-outlined',
                auth: 'link:cacert:caCertLicense:impact',
                onClick: handleImpact.bind(null, record),
              },
              {
                tooltip: t(`${tNs}.action.revoke`),
                icon: 'ant-design:stop-outlined',
                auth: 'link:cacert:caCertLicense:revoke',
                color: 'warning',
                disabled: record?.state !== 1,
                onClick: handleImpact.bind(null, record),
              },
              {
                tooltip: t('common.title.edit'),
                icon: 'ant-design:edit-outlined',
                auth: 'link:cacert:caCertLicense:edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                tooltip: t('common.title.delete'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                auth: 'link:cacert:caCertLicense:delete',
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

    <ImportModal @register="registerImportModal" @success="handleSuccess" />
    <EditMetadataModal @register="registerEditModal" @success="handleSuccess" />
    <RevokeImpactModal @register="registerImpactModal" @success="handleSuccess" />
    <DownloadPackModal @register="registerDownloadModal" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
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
    remove,
    deleteSingle,
  } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import { columns, searchFormSchema, cardFields } from './caCertLicense.data';
  import ImportModal from './ImportModal.vue';
  import EditMetadataModal from './EditMetadataModal.vue';
  import RevokeImpactModal from './RevokeImpactModal.vue';
  import DownloadPackModal from './DownloadPackModal.vue';
  import { useDict } from '/@/components/Dict';
  import { CaCertLicenseSvg } from '/@/components/iot/integration/svg';

  defineOptions({ name: 'CA许可证证书' });

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const { t } = useI18n();
  const { createMessage, createConfirm } = useMessage();
  const { getDictLabel } = useDict();
  const { push } = useRouter();

  /** 跳转详情页 (path-based,与 ApiSelectNodeCard / DeviceCertInfo 统一约定) */
  const goToDetail = (id: string | number) => {
    if (!id) return;
    push({ path: `/cacert/caCertLicense/detail/${id}` });
  };

  const switchFlag = ref<boolean>(true);
  const cardListRef = ref<any>(null);
  const cardFieldsCfg = cardFields();

  /**
   * 卡片权限 ── add 走 ImportModal (PEM 是唯一新增入口);
   * edit 走 EditMetadataModal (仅证书名 / 备注可改, PEM 解析字段不可变);
   * view / delete 走 BusinessCardList 内置流程.
   */
  const cardPermissions: CardPermissions = {
    add: 'link:cacert:caCertLicense:add',
    edit: 'link:cacert:caCertLicense:edit',
    delete: 'link:cacert:caCertLicense:delete',
    view: 'link:cacert:caCertLicense:view',
  };

  /**
   * 卡片视图额外操作 ── 第一项"查看详情"用自定义事件实现 path 跳转
   * (因为 CA 详情路由 path=/cacert/caCertLicense/detail/:id 不符合 useDetailRoute()
   * 默认的 <list>/:id 自动发现规则,只能手动跳).
   * 其后 4 项业务操作:下载客户端包 / SSL 测试 / 影响面 / 吊销.
   */
  const cardExtraActions: CardAction[] = [
    {
      tooltip: t('common.title.view'),
      icon: 'ant-design:search-outlined',
      permission: 'link:cacert:caCertLicense:view',
      event: 'view',
    },
    {
      tooltip: t(`${tNs}.action.downloadPack`),
      icon: 'ant-design:download-outlined',
      permission: 'link:cacert:caCertLicense:downloadPack',
      event: 'downloadPack',
      disabled: (r: any) => r?.state !== 1,
    },
    {
      tooltip: t(`${tNs}.action.testSsl`),
      icon: 'ant-design:thunderbolt-outlined',
      permission: 'link:cacert:caCertLicense:sslTest',
      event: 'sslTest',
    },
    {
      tooltip: t(`${tNs}.action.viewImpact`),
      icon: 'ant-design:cluster-outlined',
      permission: 'link:cacert:caCertLicense:impact',
      event: 'impact',
    },
    {
      tooltip: t(`${tNs}.action.revoke`),
      icon: 'ant-design:stop-outlined',
      permission: 'link:cacert:caCertLicense:revoke',
      event: 'impact',
      disabled: (r: any) => r?.state !== 1,
    },
  ];

  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerImpactModal, { openModal: openImpactModal }] = useModal();
  const [registerDownloadModal, { openModal: openDownloadModal }] = useModal();

  const [registerTable, { reload, getSelectRowKeys }] = useTable({
    title: t(`${tNs}.table.title`),
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
    rowSelection: { type: 'checkbox', columnWidth: 40 },
    actionColumn: {
      width: 260,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  function handleImport() {
    openImportModal(true);
  }

  function handleView(record: any, e?: Event) {
    e?.stopPropagation();
    if (record?.id) goToDetail(record.id);
  }

  /** 编辑元信息 (仅 certName + remark, 其余 PEM 解析字段不可改) */
  function handleEdit(record: any, e?: Event) {
    e?.stopPropagation();
    openEditModal(true, { record });
  }

  function handleImpact(record: any, e?: Event) {
    e?.stopPropagation();
    openImpactModal(true, { record });
  }

  function handleDownloadPack(record: any, e?: Event) {
    e?.stopPropagation();
    openDownloadModal(true, { record });
  }

  /** 跳转 SSL 测试器并预填本 CA 序列号 */
  function handleSslTest(record: any, e?: Event) {
    e?.stopPropagation();
    push({
      path: '/link/cacert/sslTester',
      query: { caSerialNumber: record?.serialNumber },
    });
  }

  async function handleDelete(record: any) {
    if (!record?.id) return;
    await batchDelete([record.id]);
  }

  async function batchDelete(ids: string[]) {
    await remove(ids);
    createMessage.success(t('common.tips.deleteSuccess'));
    handleSuccess();
  }

  function handleBatchDelete() {
    const ids = getSelectRowKeys();
    if (!ids?.length) {
      createMessage.warning(t('common.tips.pleaseSelectTheData'));
      return;
    }
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: () => batchDelete(ids as string[]),
    });
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

  function handleCardExtraAction(payload: { event: string; record: any }) {
    switch (payload.event) {
      case 'view':
        goToDetail(payload.record?.id);
        break;
      case 'impact':
        handleImpact(payload.record);
        break;
      case 'downloadPack':
        handleDownloadPack(payload.record);
        break;
      case 'sslTest':
        handleSslTest(payload.record);
        break;
    }
  }

  function getStateColor(state?: number): string {
    switch (state) {
      case 0:
        return 'warning'; // 待颁发
      case 1:
        return 'success'; // 已颁发
      case 2:
        return 'error'; // 已吊销
      default:
        return 'default';
    }
  }

  watch(switchFlag, (newValue) => {
    if (newValue === false) reload();
  });
</script>

<style lang="less" scoped>
  .cert-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .cert-svg {
      width: 24px;
      height: 24px;
      flex-shrink: 0;
    }
  }
</style>
