<template>
  <PageWrapper contentFullHeight dense class="device-page">
    <!-- ===== 顶部指标卡(Flexy 4 列,与产品总览同款) ===== -->
    <a-row :gutter="16" class="metric-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon total"><DatabaseOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.deviceTotal') }}</div>
            <div class="metric-value">{{ deviceOverview?.totalDevicesCount || 0 }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.device.device.deviceTotalAmount') }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon online" :class="{ on: (deviceOverview?.onlineCount ?? 0) > 0 }">
            <ApiOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.online') }}</div>
            <div class="metric-value metric-value--ok">{{ deviceOverview?.onlineCount || 0 }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.device.device.offline') }}</span>
              <span class="sub-val">{{ deviceOverview?.offlineCount || 0 }}</span>
              <span class="sub-divider">·</span>
              <span class="sub-key">{{ t('iot.link.device.device.notConnected') }}</span>
              <span class="sub-val">{{ deviceOverview?.notConnectedCount || 0 }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon active"><CheckCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.deviceAllStatus.active') }}</div>
            <div class="metric-value metric-value--ok">{{
              deviceOverview?.activatedCount || 0
            }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{
                t('iot.link.device.device.deviceAllStatus.notActivat')
              }}</span>
              <span class="sub-val">{{ deviceOverview?.notActivatedCount || 0 }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="metric-card">
          <div class="metric-icon locked"><LockOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.link.device.device.deviceAllStatus.lock') }}</div>
            <div class="metric-value metric-value--warn">{{
              deviceOverview?.lockedCount || 0
            }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.link.device.device.deviceTotal') }}</span>
              <span class="sub-val">{{ deviceOverview?.totalDevicesCount || 0 }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- ===== 主体表格 + 卡片切换视图 =====
         注意:不传 :is-device,避免 BasicTable 渲染老版 CardList(蓝色"编辑"+红色"停用/删除"那种)
         设备列表统一走下面的 #cardView slot,用通用 BusinessCardList 渲染 Flexy 风格 -->
    <BasicTable @register="registerTable" @switch-change="getSwitchChange" :switchFlag="switchFlag">
      <!-- 卡片视图(Flexy) ── BusinessCardList,默认开启 -->
      <template #cardView="{ searchData, title }">
        <BusinessCardList
          ref="cardListRef"
          :pageApi="page"
          :deleteApi="deleteSingle"
          :title="title"
          :searchData="searchData"
          nameField="deviceName"
          :nameFallback="t('iot.link.device.device.table.title')"
          :fields="cardFields"
          statusField="connectStatus"
          :statusOnlineValue="DeviceConnectStatus.ONLINE"
          :statusOnlineLabel="t('iot.link.device.device.online')"
          :statusOfflineLabel="t('iot.link.device.device.offline')"
          :statusResolver="resolveDeviceStatus"
          badgeField="nodeType"
          badgeDictType="LINK_DEVICE_NODE_TYPE"
          :permissions="cardPermissions"
          :extraActions="cardExtraActions"
          @add="handleAdd"
          @view="handleView"
          @edit="handleEdit"
          @delete="handleDelete"
          @extra-action="handleCardExtraAction"
        >
          <!--
            设备图视觉优先级:节点类型 SVG > 产品 icon。
            原因:同一产品下大量设备共用一张产品 icon → 卡片看上去全长一样,
            分不出"普通/网关/子设备"。改为先按节点类型出三套 SVG(普通=立方蓝、
            网关=多面体紫+下挂子节点、子设备=GW 虚线接父),用户一眼看出设备身份;
            产品 icon 仍可在详情页 header 看到。
          -->
          <template #cardImage="{ record }">
            <component :is="getDeviceNodeTypeSvg(record?.nodeType)" />
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
        <a-button type="primary" preIcon="ant-design:download-outlined" @click="handleImport">
          {{ t('common.title.import') }}
        </a-button>
        <a-button preIcon="ant-design:upload-outlined" @click="handleExport">
          {{ t('common.title.bulkExport') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('iot.link.device.device.switchView') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <div class="table-operation-device">
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
            <a-divider type="vertical" class="qrcode_divider" />
            <renderQrCode
              :deviceIdentification="record?.deviceIdentification || ''"
              :deviceName="record?.deviceName || '未知设备'"
            >
              <template #trigger>
                <a-tooltip>
                  <template #title>{{ t('iot.link.device.device.qrcode') }}</template>
                  <QrcodeOutlined class="device-qrcode-trigger" />
                </a-tooltip>
              </template>
            </renderQrCode>
          </div>
        </template>
      </template>
    </BasicTable>
    <EditModal @register="registerModal" @success="handleSuccess" />
    <ImportModal @register="importModal" @reload="reload" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BusinessCardList } from '/@/components/BusinessCardList';
  import type { CardAction, CardPermissions } from '/@/components/BusinessCardList';
  import { getDeviceNodeTypeSvg } from '/@/components/iot/svg';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { useRouter } from 'vue-router';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { DeviceConnectStatus } from '/@/enums/link/device';
  import { page, remove, overview, bulkExport, deleteSingle } from '/@/api/iot/link/device/device';
  import { columns, searchFormSchema, cardFields as buildCardFields } from './device.data';
  import EditModal from './Edit.vue';
  import ImportModal from './ImportModal.vue';
  import renderQrCode from '../qrcode/index.vue';
  import {
    QrcodeOutlined,
    DatabaseOutlined,
    ApiOutlined,
    CheckCircleOutlined,
    LockOutlined,
  } from '@ant-design/icons-vue';
  import { Card, Row, Col } from 'ant-design-vue';
  import { downloadByData } from '/@/utils/file/download';
  export default defineComponent({
    name: '设备',
    components: {
      BasicTable,
      PageWrapper,
      TableAction,
      EditModal,
      ACard: Card,
      ARow: Row,
      ACol: Col,
      ImportModal,
      renderQrCode,
      QrcodeOutlined,
      DatabaseOutlined,
      ApiOutlined,
      CheckCircleOutlined,
      LockOutlined,
      BusinessCardList,
      ThumbUrl,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [importModal, { openModal: openImportModal }] = useModal();
      const { replace } = useRouter();
      const cardListRef = ref<any>(null);

      // 卡片状态三态解析:在线 / 离线 / 未连接(connectStatus 0=未连接 1=在线 2=离线),
      // 与详情页一致,避免"未连接"被二态判定误显示为"离线"。
      const resolveDeviceStatus = (record: any): { label: string; cls: string } => {
        const v = Number(record?.connectStatus);
        if (v === DeviceConnectStatus.ONLINE) {
          return { label: t('iot.link.device.device.online'), cls: 'online' };
        }
        if (v === DeviceConnectStatus.OFFLINE) {
          return { label: t('iot.link.device.device.offline'), cls: 'offline' };
        }
        return { label: t('iot.link.device.device.notConnected'), cls: 'unconnected' };
      };

      // 卡片视图配置 ── Flexy 风格,与产品列表同款
      const cardFields = buildCardFields();
      const cardPermissions: CardPermissions = {
        add: 'link:device:device:add',
        edit: 'link:device:device:edit',
        delete: 'link:device:device:delete',
        view: 'link:device:device:view',
      };
      const cardExtraActions: CardAction[] = [
        {
          tooltip: t('common.title.copy'),
          icon: 'ant-design:copy-outlined',
          permission: 'link:device:device:copy',
          event: 'copy',
        },
      ];

      // 表格
      const [registerTable, { reload, getSelectRowKeys, clearSelectedRowKeys, setLoading }] =
        useTable({
          title: t('iot.link.device.device.table.title'),
          api: page,
          columns: columns(),
          formConfig: {
            name: 'DeviceSearch',
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
            width: 200,
            title: t('common.column.action'),
            dataIndex: 'action',
          },
        });

      // 弹出复制页面
      function handleCopy(record: Recordable, e?: Event) {
        e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.COPY,
        });
      }
      // 弹出新增页面
      function handleAdd() {
        openModal(true, { type: ActionEnum.ADD });
      }

      // 弹出查看页面 ── 路由 :id 段语义是 deviceIdentification(业务唯一标识)
      function handleView(record: Recordable, e?: Event) {
        e?.stopPropagation();
        replace({
          name: '设备详情',
          params: { id: record.deviceIdentification },
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

      // 新增或编辑成功回调:同步表格 + 卡片
      function handleSuccess() {
        reload();
        cardListRef.value?.reload();
      }

      // 批量删除
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
      function handleDelete(record: Recordable, e?: Event) {
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

      // 切换视图 卡片(true=显示卡片) <-> 表格(false)。默认卡片(Flexy 风格)
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
      }

      // 设备概况统计信息
      const deviceOverview = reactive<Record<string, number>>({});

      async function fetchDeviceOverView() {
        const res = await overview();
        Object.assign(deviceOverview, res);
      }

      fetchDeviceOverView();

      function copyFn(text: string) {
        if (copyTextToClipboard(text)) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      }

      function getSwitchChange(e: boolean) {
        switchFlag.value = e;
      }

      /** 卡片视图额外操作派发(BusinessCardList @extraAction 回调) */
      function handleCardExtraAction(payload: { event: string; record: Recordable }) {
        const e = new Event('synthetic');
        switch (payload.event) {
          case 'copy':
            handleCopy(payload.record, e);
            break;
        }
      }

      const handleImport = () => {
        openImportModal(true);
      };
      const handleExport = async () => {
        try {
          setLoading(true);
          if (getSelectRowKeys().length === 0) {
            createMessage.warning(t('iot.link.device.device.validateExportMsg'));
            setLoading(false);
            return;
          }
          const res = await bulkExport(getSelectRowKeys());
          await downloadByData(res, 'device-export.xlsx');
          clearSelectedRowKeys();
          setLoading(false);
        } catch (error) {
          throw error;
        }
      };

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
        switchFlag,
        switchView,
        deviceOverview,
        copyFn,
        getSwitchChange,
        handleExport,
        importModal,
        handleImport,
        reload,
        // 卡片视图相关
        page,
        deleteSingle,
        EditModal,
        cardFields,
        cardPermissions,
        cardExtraActions,
        cardListRef,
        handleCardExtraAction,
        getDeviceNodeTypeSvg,
        resolveDeviceStatus,
        // 枚举
        DeviceConnectStatus,
      };
    },
  });
</script>
<style lang="less" scoped>
  /* Flexy 风格,完全对齐产品列表(/views/iot/link/product/product/index.vue) */
  .device-page {
    :deep(.ant-card) {
      border-radius: 14px;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    }
  }

  .metric-row {
    margin: 16px 16px 12px !important;
  }

  .metric-card {
    :deep(.ant-card-body) {
      padding: 16px 18px;
      display: flex;
      align-items: center;
      gap: 14px;
    }
  }

  .metric-icon {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;
    color: #fff;

    &.total {
      background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
      box-shadow: 0 6px 14px rgba(93, 135, 255, 0.35);
    }

    &.online {
      background: linear-gradient(135deg, #9b75e6 0%, #b095f0 100%);
      box-shadow: 0 6px 14px rgba(155, 117, 230, 0.35);

      &.on {
        background: linear-gradient(135deg, #13deb9 0%, #36e6c3 100%);
        box-shadow: 0 6px 14px rgba(19, 222, 185, 0.35);
      }
    }

    &.active {
      background: linear-gradient(135deg, #13deb9 0%, #36e6c3 100%);
      box-shadow: 0 6px 14px rgba(19, 222, 185, 0.35);
    }

    &.locked {
      background: linear-gradient(135deg, #fa896b 0%, #ff6a4a 100%);
      box-shadow: 0 6px 14px rgba(250, 137, 107, 0.35);
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    font-size: 12px;
    color: #97a1b0;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    margin-bottom: 2px;
  }

  .metric-value {
    font-size: 22px;
    font-weight: 700;
    color: #2a3547;
    font-variant-numeric: tabular-nums;
    line-height: 1.2;

    &--ok {
      color: #13deb9;
    }

    &--warn {
      color: #fa896b;
    }

    &--mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 14px;
      letter-spacing: 0.2px;
    }
  }

  .metric-sub {
    margin-top: 4px;
    font-size: 12px;
    color: #6b7280;

    .sub-key {
      color: #97a1b0;
      margin-right: 4px;
    }

    .sub-val {
      color: #2a3547;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }

    .sub-divider {
      margin: 0 6px;
      color: #d1d8e0;
    }
  }

  .device-qrcode-trigger {
    color: @primary-color;
    font-size: 16px;
    cursor: pointer;
    transition: color 0.2s ease;

    &:hover {
      color: lighten(@primary-color, 8%);
    }
  }

  .table-operation-device {
    display: flex;
    align-items: center;

    .qrcode_divider.ant-divider-vertical {
      margin: 0 10px 0 2px;
    }
  }
</style>
