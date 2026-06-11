<template>
  <div class="card-list" style="background-color: #fff; padding: 6px">
    <div class="card-header" v-if="!isSelect">
      <span>{{ title }}</span>
      <div>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
          {{ t('common.title.add') }}
        </a-button>
        <a-button type="primary" preIcon="ant-design:download-outlined" @click="handleImport">{{
          t('common.title.import')
        }}</a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div class="loading" v-if="cardLoading">
      <a-spin />
    </div>
    <div style="padding: 0 23px">
      <a-row :gutter="[24, 12]" v-if="deviceList.length">
        <a-col
          v-for="record in deviceList"
          :key="record.id"
          :xs="isSelect ? 12 : 24"
          :sm="isSelect ? 12 : 24"
          :md="12"
          :lg="12"
          :xl="isSelect ? 12 : 8"
          :xxl="isSelect ? 12 : 6"
        >
          <div :class="isSelect ? 'isSelect' : ''">
            <div
              class="device-item"
              :class="
                record?.id == deviceId && isSelect
                  ? 'isSelected'
                  : record?.id == deviceId
                  ? 'active'
                  : ''
              "
              @click="selectItem(record, $event)"
            >
              <renderQrcode
                :deviceIdentification="record?.deviceIdentification || ''"
                :deviceName="record?.deviceName || '未知设备'"
                :class="'qrcode_wrap'"
              >
                <template #trigger>
                  <QrcodeOutlined :style="{ color: '#999', fontSize: '20px' }" />
                </template>
              </renderQrcode>
              <div class="device-info">
                <img
                  v-if="record?.nodeType === 2"
                  @click="handleView(record, $event)"
                  src="../../../../../../../../assets/images/iot/link/deviceAndProduct/childrenDevice.png"
                />
                <img
                  v-else-if="record?.nodeType === 0"
                  @click="handleView(record, $event)"
                  src="../../../../../../../../assets/images/iot/link/deviceAndProduct/gatwayDevice.png"
                />
                <img
                  v-else-if="record?.nodeType === 1"
                  @click="handleView(record, $event)"
                  src="../../../../../../../../assets/images/iot/link/deviceAndProduct/commonDevice.png"
                />
                <img
                  v-else
                  @click="handleView(record, $event)"
                  src="../../../../../../../../assets/images/iot/link/device/deviceManagement.gif"
                />
                <div class="info">
                  <a-tooltip placement="topLeft" :title="record?.deviceName">
                    <div class="device-name">{{ record?.deviceName }}</div>
                  </a-tooltip>
                  <div class="device-form">
                    <span class="label">{{
                      t('iot.link.device.device.deviceIdentification')
                    }}</span>
                    <a-tooltip placement="topLeft" :title="record?.deviceIdentification">
                      <span class="value">{{ record?.deviceIdentification }}</span>
                    </a-tooltip>
                  </div>
                  <div class="device-form">
                    <span class="label">{{ t('iot.link.device.device.nodeType') }}</span>
                    <span class="value">{{
                      getDictLabel('LINK_DEVICE_NODE_TYPE', record?.nodeType, '')
                    }}</span>
                  </div>
                  <div class="device-form">
                    <span class="label">{{ t('iot.link.device.device.encryptMethod') }}</span>
                    <span class="value">{{
                      getDictLabel('LINK_DEVICE_ENCRYPT_METHOD', record?.encryptMethod, '')
                    }}</span>
                  </div>
                </div>
              </div>
              <div class="device-btns">
                <div class="device-status">
                  <img :src="record?.connectStatus == 1 ? Icon4 : Icon5" alt="" class="img" />
                  <span class="red" v-if="record?.connectStatus == 1">{{
                    getDictLabel('LINK_DEVICE_CONNECT_STATUS', record?.connectStatus, '')
                  }}</span>
                  <span v-else-if="record?.connectStatus == 2">{{
                    getDictLabel('LINK_DEVICE_CONNECT_STATUS', record?.connectStatus, '')
                  }}</span>
                  <span v-else-if="record?.connectStatus == 0">{{
                    getDictLabel('LINK_DEVICE_CONNECT_STATUS', record?.connectStatus, '')
                  }}</span>
                  <a-divider type="vertical" />
                  <span class="green" v-if="record?.deviceStatus == 1">{{
                    getDictLabel('LINK_DEVICE_STATUS', record?.deviceStatus, '')
                  }}</span>
                  <span class="red" v-else-if="record?.deviceStatus == 2">{{
                    getDictLabel('LINK_DEVICE_STATUS', record?.deviceStatus, '')
                  }}</span>
                  <span v-else-if="record?.deviceStatus == 0">{{
                    getDictLabel('LINK_DEVICE_STATUS', record?.deviceStatus, '')
                  }}</span>
                </div>
                <div v-if="!isSelect">
                  <div class="btn primary" @click="handleEdit(record)">{{
                    t('common.title.edit')
                  }}</div>
                  <div
                    class="btn plain"
                    v-if="record?.deviceStatus != 1"
                    @click="handleStatus(record, 1)"
                    >{{ t('iot.link.device.device.enable') }}</div
                  >
                  <div class="btn danger" v-else @click="handleStatus(record, 2)">{{
                    t('iot.link.device.device.deactivate')
                  }}</div>
                  <div class="btn danger" @click="handleDelete(record)">{{
                    t('common.title.delete')
                  }}</div>
                </div>
              </div>
            </div>
          </div>
          <!-- <a-card hoverable :bordered="false">
            <div class="device-item">
              <img src="../../../../../assets/images/device/deviceManagement.gif" />
              <div class="device-info">
                <span class="device-name">{{ record?.deviceName }}</span>
                <div>
                  <div>
                    <span>设备类型</span>
                    <span>{{ record?.deviceStatusLabel }}</span>
                  </div>
                  <div>
                    <span>产品ID </span>
                    <span>{{ record?.productId }}</span>
                  </div>
                </div>
              </div>
            </div>
            <a-space>
              <a-button type="dashed" size="small" @click="handleEdit(record, $event)">
                <EditOutlined />编辑
              </a-button>
              <a-button type="dashed" size="small"> <StopOutlined />禁用 </a-button>
              <a-button type="dashed" size="small"> <DeleteOutlined />删除 </a-button>
            </a-space>
          </a-card> -->
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="tr">
        <a-pagination
          @change="change"
          size="small"
          v-model:current="current"
          v-model:pageSize="size"
          :total="total"
          :show-total="(total) => t('component.table.total', { total })"
          show-size-changer
          show-quick-jumper
          :page-size-options="pageSizeOptions"
        />
      </div>
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
  <ImportModal @register="importModal" @reload="getDeviceList" />
</template>
<script lang="ts">
  import { defineComponent, ref, watch, reactive } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import { page, statusChange, deleteSingle } from '/@/api/iot/link/device/device';
  // components
  import { Card, Row, Col, Button, Spin, Divider, Tooltip, Pagination } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import EditModal from '/@/views/iot/link/device/device/Edit.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import ImportModal from '/@/views/iot/link/device/device/ImportModal.vue';
  import renderQrcode from '/@/views/iot/link/device/qrcode/index.vue';
  import { QrcodeOutlined } from '@ant-design/icons-vue';
  const { getDictLabel } = useDict();
  interface deviceItem {
    id: string;
    deviceName: string;
    deviceStatusLabel: string;
    productId: string;
    connectStatusLabel: string;
    connectStatus: number;
    deviceStatus: number;
    deviceIdentification: string;
    echoMap: any;
  }
  import Icon4 from '/@/assets/images/iot/link/device/Icon4.png';
  import Icon5 from '/@/assets/images/iot/link/device/Icon5.png';
  export default defineComponent({
    name: 'CardList',
    components: {
      ACard: Card,
      ARow: Row,
      ACol: Col,
      AButton: Button,
      ASpin: Spin,
      EditModal,
      ADivider: Divider,
      ATooltip: Tooltip,
      APagination: Pagination,
      ImportModal,
      renderQrcode,
      QrcodeOutlined,
    },
    props: {
      title: {
        type: String,
        default: '列表',
      },
      isSelect: {
        type: Boolean,
        default: false,
      },
      productIdentification: {
        type: String,
        default: '',
      },
      searchData: {
        type: Object,
        default: {},
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();
      const [importModal, { openModal: openImportModal }] = useModal();
      const { push } = useRouter();
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      const { createConfirm } = useMessage();
      // 设备列表
      let deviceList = ref<Array<deviceItem>>([]);
      let model = reactive({});

      // 设备列表加载状态
      const cardLoading = ref<boolean>(false);
      const getDeviceList = () => {
        cardLoading.value = true;
        if (props.productIdentification) {
          model.productIdentification = props.productIdentification;
        }
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams(model),
        }).then((res: any) => {
          total.value = res.total;
          deviceList.value = res.records;
          cardLoading.value = false;
        });
      };
      watch(
        () => props.searchData,
        (newValue) => {
          model = newValue;
          getDeviceList();
        },
        { immediate: true, deep: true },
      );
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
        emit('input', switchFlag.value);
      }

      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable) {
        // e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }

      const selectDeviceCard = (column) => {
        emit('selectDeviceCard', column);
      };

      // 点击设备
      const deviceId = ref<string>('');
      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        deviceId.value = record.id;
        // 如果选择，选中改项
        if (props.isSelect) {
          selectDeviceCard(record);
        }
      }

      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        deviceId.value = record.id;
        // 如果选择，选中改项
        if (props.isSelect) {
          selectDeviceCard(record);
        } else {
          push({
            name: '设备详情',
            params: { id: record.id },
          });
        }
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        getDeviceList();
      }

      function change() {
        getDeviceList();
      }
      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await deleteSingle(record.id);
              getDeviceList();
            } catch (e) {}
          },
        });
      }

      // 修改设备状态
      function handleStatus(record, status: number) {
        createConfirm({
          iconType: 'warning',
          content:
            status == 1 ? t('common.tips.confirmEnable') : t('common.tips.confirmDeactivate'),
          onOk: async () => {
            try {
              await statusChange(record.id, { status: status });
              getDeviceList();
            } catch (e) {}
          },
        });
      }
      const handleImport = async () => {
        openImportModal(true);
      };

      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);
      return {
        t,
        deviceList,
        cardLoading,
        switchFlag,
        switchView,
        registerModal,
        handleAdd,
        handleEdit,
        handleSuccess,
        handleView,
        selectItem,
        deviceId,
        Icon4,
        Icon5,
        current,
        total,
        size,
        getDeviceList,
        change,
        pageSizeOptions,
        handleDelete,
        handleStatus,
        getDictLabel,
        importModal,
        openImportModal,
        handleImport,
      };
    },
  });
</script>
<style lang="less" scoped>
  .tr {
    text-align: right;
  }

  .loading {
    width: 100%;
    height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 33px;

    span {
      margin-left: 7px;
    }

    div {
      .ant-btn {
        margin: 0 8px 8px 0;
      }
    }
  }

  .isSelect {
    .device-item {
      position: relative;

      .qrcode_wrap {
        position: absolute;
        top: 2px;
        right: 4px;
      }

      .device-info {
        max-width: 100%;
        display: flex;
        flex-direction: row;
        padding-top: 40px;

        img {
          width: 30%;
          height: 30%;
        }

        .info {
          width: 68%;
          margin-left: 2%;
        }

        .device-name {
          // position: absolute;
          // top: 30px;
          // left: 0;
          // padding: 0 16px;
        }
      }

      .device-btns {
        position: absolute;
        right: 0;
        top: 4px;
        border: 0 none;

        .device-status {
          border: 0 none;
        }
      }
    }
  }

  .device-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    padding-bottom: 16px;
    cursor: pointer;
    position: relative;
    background-color: #fff;
    transition: all 0.5s;
    border: 2px solid transparent;

    .qrcode_wrap {
      position: absolute;
      top: 2px;
      right: 4px;
    }

    .info {
      max-width: 100%;
    }

    &.active {
      box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      border: 2px solid #1a66ff;
      margin-top: -19px;
    }

    &.isSelected {
      // box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      // border: 2px solid #1a66ff;
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      max-width: calc(100% - 140px);

      img {
        width: 60%;
        height: 60%;
      }

      // }

      .device-name {
        font-size: 18px;
        font-weight: 700;
        margin-bottom: 16px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
      }

      .device-form {
        display: flex;
        align-items: center;
        width: 100%;
        line-height: 20px;

        & + .device-form {
          margin-top: 5px;
        }

        .label {
          width: 50px;
          text-align: right;
          font-size: 12px;
          font-weight: 500;
          color: #b6b6b6;
        }

        .value {
          width: calc(100% - 46px);
          font-size: 14px;
          font-weight: 500;
          color: #2a2a2a;
          padding-left: 8px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .device-btns {
      width: 148px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-left: 1px dashed #d4d4d4;

      .device-status {
        padding-top: 6px;
        padding-bottom: 14px;
        border-bottom: 1px dashed #d4d4d4;
        display: flex;
        align-items: center;

        .img {
          width: 18px;
          height: 18px;
          margin-right: 2px;
        }

        span {
          color: #808080;

          &.red {
            color: #fa3758;
          }

          &.green {
            color: #43cf7c;
          }
        }
      }

      .btn {
        width: 92px;
        height: 28px;
        background: #1a66ff;
        opacity: 1;
        text-align: center;
        font-size: 14px;
        line-height: 24px;
        color: #ffffff;
        border: 2px solid #1a66ff;
        border-radius: 6px;
        margin-top: 18px;

        &.plain {
          background-color: #fff;
          color: #1a66ff;
        }

        &.danger {
          background-color: #fff;
          color: #d43030;
          border: 2px solid #d43030;
        }
      }
    }
  }
</style>
