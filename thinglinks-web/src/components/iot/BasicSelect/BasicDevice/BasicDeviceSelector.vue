<template>
  <div class="device-selector">
    <a-row :gutter="[20]" v-if="allowSelectAll && (selectedIds.length === 0 || selectType === 1)">
      <a-col :span="12">
        <div
          class="select-all-device"
          :class="{ active: selectedIds.length === 0 }"
          @click="selectTypeFn(1)"
        >
          <span>{{ t('iot.link.engine.linkage.allDevices') }}</span>
          <img :src="childrenDevice" alt="" />
        </div>
      </a-col>
      <a-col :span="12">
        <div class="select-all-device" @click="selectTypeFn(2)">
          <span>{{ t('iot.link.engine.linkage.custom') }}</span>
          <img :src="commonDevice" alt="" />
        </div>
      </a-col>
    </a-row>

    <div class="select-device" v-else>
      <div class="return" v-if="showBackButton">
        <a-button type="primary" @click="goBack">{{ t('common.back') }}</a-button>
      </div>
      <a-row :gutter="[24, 24]">
        <a-col :span="showSelectedDevice ? 6 : 0">
          <div class="type-list">
            <div class="select-title">
              <div class="select-title__label">
                <ApartmentOutlined style="margin-right: 4px" />
                {{ t('iot.link.engine.linkage.selectedDevice') }}
              </div>
            </div>
            <template v-if="selectedList.length">
              <div
                class="isSelect"
                v-for="record in selectedList"
                :key="record.deviceIdentification"
              >
                <div class="device-item isSelected">
                  <div class="device-info">
                    <img v-if="record?.nodeType === 2" :src="childrenDevice" />
                    <img v-else-if="record?.nodeType === 0" :src="gatewayDevice" />
                    <img v-else-if="record?.nodeType === 1" :src="commonDevice" />
                    <img v-else :src="deviceDefault" />
                    <div class="info">
                      <a-tooltip placement="topLeft" :title="record?.deviceName">
                        <div class="device-name">{{
                          record?.deviceName || record?.deviceIdentification
                        }}</div>
                      </a-tooltip>
                      <div class="device-form">
                        <span class="label">{{
                          t('iot.link.device.device.deviceIdentification')
                        }}</span>
                        <a-tooltip
                          placement="topLeft"
                          :title="record?.deviceIdentification || record?.deviceIdentification"
                        >
                          <span class="value">{{
                            record?.deviceIdentification || record?.deviceIdentification
                          }}</span>
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
                  <div class="device-btns" @click.stop="removeId(record.deviceIdentification)">
                    <div class="btn">
                      <img :src="DeleteY" alt="" />
                    </div>
                  </div>
                </div>
              </div>
              <div class="type-list__actions">
                <a-button type="link" size="small" @click="clearAll">{{
                  t('common.resetText')
                }}</a-button>
              </div>
            </template>
            <div class="empty" v-else>
              <a-empty :description="t('iot.link.engine.linkage.descriptionDevice')" />
            </div>
          </div>
        </a-col>
        <a-col :span="showSelectedDevice ? 18 : 24">
          <!-- 右侧设备列表（复刻 CardList 选择态样式） -->
          <div class="card-list" style="background-color: #fff; padding: 6px">
            <div class="loading" v-if="cardLoading"><a-spin /></div>
            <div style="padding: 0 23px">
              <div class="search-wrapper">
                <a-row :gutter="[16, 16]">
                  <a-col :span="12">
                    <a-input
                      v-model:value="searchDeviceName"
                      :placeholder="
                        t('iot.basic.basicComp.BasicSelectDeviceModal.searchDeviceName')
                      "
                      allow-clear
                      @press-enter="handleSearch"
                    >
                      <template #prefix>
                        <SearchOutlined />
                      </template>
                    </a-input>
                  </a-col>
                  <a-col :span="12">
                    <a-input
                      v-model:value="searchDeviceIdentification"
                      :placeholder="
                        t('iot.basic.basicComp.BasicSelectDeviceModal.searchDeviceIdentification')
                      "
                      allow-clear
                      @press-enter="handleSearch"
                    >
                      <template #prefix>
                        <SearchOutlined />
                      </template>
                    </a-input>
                  </a-col>
                  <a-col :span="24" style="text-align: right">
                    <a-space>
                      <a-button @click="handleReset">
                        {{ t('common.resetText') }}
                      </a-button>
                      <a-button type="primary" @click="handleSearch">
                        {{ t('common.searchText') }}
                      </a-button>
                    </a-space>
                  </a-col>
                </a-row>
              </div>
              <a-row :gutter="[24, 12]" v-if="deviceList.length">
                <a-col
                  v-for="record in deviceList"
                  :key="record.deviceIdentification"
                  :xs="12"
                  :sm="12"
                  :md="12"
                  :lg="12"
                  :xl="12"
                  :xxl="12"
                >
                  <div class="isSelect">
                    <div
                      class="device-item"
                      :class="[
                        'card-device-item',
                        selectedIds.includes(record.deviceIdentification) ? 'isSelected' : '',
                      ]"
                      @click="onSelectRecord(record)"
                    >
                      <renderQrcode
                        :deviceIdentification="record?.deviceIdentification || ''"
                        :deviceName="record?.deviceName || t('common.unknown')"
                        class="qrcode_wrap"
                      >
                        <template #trigger>
                          <QrcodeOutlined :style="{ color: '#999', fontSize: '20px' }" />
                        </template>
                      </renderQrcode>
                      <div class="device-info">
                        <img v-if="record?.nodeType === 2" :src="childrenDevice" />
                        <img v-else-if="record?.nodeType === 0" :src="gatewayDevice" />
                        <img v-else-if="record?.nodeType === 1" :src="commonDevice" />
                        <img v-else :src="deviceDefault" />
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
                            <span class="label">{{
                              t('iot.link.device.device.encryptMethod')
                            }}</span>
                            <span class="value">{{
                              getDictLabel('LINK_DEVICE_ENCRYPT_METHOD', record?.encryptMethod, '')
                            }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="device-btns">
                        <div class="device-status">
                          <img
                            :src="record?.connectStatus == 1 ? Icon4 : Icon5"
                            alt=""
                            class="img"
                          />
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
                      </div>
                    </div>
                  </div>
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
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script lang="ts">
  // TODO 后期升级cardList为公共组件此处需调整
  import { defineComponent, reactive, toRefs, watch, PropType, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Row, Col, Divider, Button, Space } from 'ant-design-vue';
  import {
    Pagination as APagination,
    Spin as ASpin,
    Tooltip as ATooltip,
    Empty as AEmpty,
  } from 'ant-design-vue';
  import { ApartmentOutlined, QrcodeOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { Input } from 'ant-design-vue';
  import renderQrcode from '/@/views/iot/link/device/qrcode/index.vue';
  import { page } from '/@/api/iot/link/device/device';
  import { useDict } from '/@/components/Dict';
  import childrenDevice from '/@/assets/images/iot/link/deviceAndProduct/childrenDevice.png';
  import commonDevice from '/@/assets/images/iot/link/deviceAndProduct/commonDevice.png';
  import gatewayDevice from '/@/assets/images/iot/link/deviceAndProduct/gatwayDevice.png';
  import deviceDefault from '/@/assets/images/iot/link/device/deviceManagement.gif';
  import Icon4 from '/@/assets/images/iot/link/device/Icon4.png';
  import Icon5 from '/@/assets/images/iot/link/device/Icon5.png';
  import DeleteY from '/@/assets/images/iot/link/device/delete-y.png';

  export default defineComponent({
    name: 'BasicDeviceSelector',
    components: {
      ARow: Row,
      ACol: Col,
      ADivider: Divider,
      AButton: Button,
      ASpace: Space,
      APagination,
      ASpin,
      ATooltip,
      AEmpty,
      AInput: Input,
      renderQrcode,
      ApartmentOutlined,
      QrcodeOutlined,
      SearchOutlined,
    },
    props: {
      productIdentification: { type: String, required: true },
      modelValue: { type: Array as PropType<string[]>, default: () => [] },
      showSelectedDevice: { type: Boolean, default: true },
      showBackButton: { type: Boolean, default: true },
      allowSelectAll: { type: Boolean, default: true },
      multiple: { type: Boolean, default: true },
    },
    emits: ['update:modelValue', 'change'],
    setup(props, { emit, expose }) {
      const { t } = useI18n();
      const { getDictLabel } = useDict();

      const state = reactive({
        selectedIds: [...props.modelValue] as string[],
        selectedDeviceMap: {} as Record<string, any>,
        selectType: 1,
        deviceList: [] as any[],
        cardLoading: false,
        current: 1,
        size: 20,
        total: 0,
        pageSizeOptions: ['10', '20', '30', '40', '50'] as string[],
        searchDeviceName: '' as string,
        searchDeviceIdentification: '' as string,
      });

      const resetComponentState = (
        options: { clearSelection?: boolean; clearCache?: boolean; resetPager?: boolean } = {},
      ) => {
        const { clearSelection = false, clearCache = false, resetPager = false } = options;
        if (clearSelection) {
          state.selectedIds = [];
          state.selectType = 1;
          state.selectedDeviceMap = {};
          return;
        }
        if (clearCache) {
          state.selectedDeviceMap = {};
        }
        if (resetPager) {
          state.current = 1;
          state.total = 0;
          state.deviceList = [];
        }
      };

      const fetchList = async () => {
        state.cardLoading = true;
        const currentProduct = props.productIdentification;
        const model: Record<string, any> = {};
        if (currentProduct) {
          model.productIdentification = currentProduct;
        }
        // 添加搜索条件
        if (state.searchDeviceName) {
          model.deviceName = state.searchDeviceName;
        }
        if (state.searchDeviceIdentification) {
          model.deviceIdentification = state.searchDeviceIdentification;
        }
        try {
          const res: any = await page({
            current: state.current,
            size: state.size,
            model,
            extra: {},
          } as any);
          if (currentProduct !== props.productIdentification) {
            return;
          }
          state.total = res.total;
          state.deviceList = res.records || [];
          state.deviceList.forEach((item: any) => {
            if (item?.deviceIdentification) {
              state.selectedDeviceMap[item.deviceIdentification] = item;
            }
          });
        } finally {
          state.cardLoading = false;
        }
      };

      // 防止死循环的标志
      let isInternalUpdate = false;

      const syncSelectedFromProps = (ids: string[]) => {
        const normalized = Array.isArray(ids) ? [...ids] : [];

        // 检查是否真的需要更新，避免不必要的状态变更
        const currentIdsStr = JSON.stringify([...state.selectedIds].sort());
        const newIdsStr = JSON.stringify([...normalized].sort());

        if (currentIdsStr !== newIdsStr) {
          state.selectedIds = normalized;
          state.selectType = normalized.length === 0 ? 1 : 2;
          if (normalized.length === 0) {
            state.selectedDeviceMap = {};
          }
        }
      };

      watch(
        () => props.productIdentification,
        async () => {
          resetComponentState({ clearSelection: true, clearCache: true, resetPager: true });
          await fetchList();
        },
        { immediate: true },
      );

      watch(
        () => props.modelValue,
        (val: string[]) => {
          if (!isInternalUpdate) {
            syncSelectedFromProps(val);
          }
        },
        { immediate: true, deep: true },
      );

      watch(
        () => state.selectedIds,
        (newIds, oldIds) => {
          // 只有当选择真正发生变化时才触发事件
          const newIdsStr = JSON.stringify([...newIds].sort());
          const oldIdsStr = JSON.stringify([...(oldIds || [])].sort());

          if (newIdsStr !== oldIdsStr) {
            isInternalUpdate = true;
            emit('update:modelValue', [...state.selectedIds]);
            emit('change', [...state.selectedIds]);
            // 使用 nextTick 确保在下一个事件循环中重置标志
            setTimeout(() => {
              isInternalUpdate = false;
            }, 0);
          }
        },
        { deep: true },
      );

      const selectTypeFn = (type: number) => {
        state.selectType = type;
        if (type === 1) {
          // 批量更新，避免触发多次 watch
          const newIds = [];
          if (JSON.stringify(state.selectedIds) !== JSON.stringify(newIds)) {
            state.selectedIds = newIds;
          }
        }
      };

      const ensureSelectionsHaveData = () => {
        state.selectedIds.forEach((deviceIdentification) => {
          if (!state.selectedDeviceMap[deviceIdentification]) {
            const found = state.deviceList.find(
              (item) => item?.deviceIdentification === deviceIdentification,
            );
            if (found) state.selectedDeviceMap[deviceIdentification] = found;
          }
        });
      };

      const onSelectRecord = (record: any) => {
        const deviceIdentification = record?.deviceIdentification;
        if (!deviceIdentification) return;

        // 先更新设备映射
        state.selectedDeviceMap[deviceIdentification] = record;

        // 计算新的选中列表
        let newSelectedIds;
        if (props.multiple) {
          const currentIds = [...state.selectedIds];
          const idx = currentIds.indexOf(deviceIdentification);
          if (idx >= 0) {
            currentIds.splice(idx, 1);
          } else {
            currentIds.push(deviceIdentification);
          }
          newSelectedIds = currentIds;
        } else {
          newSelectedIds = [deviceIdentification];
        }

        // 批量更新状态
        state.selectedIds = newSelectedIds;
        state.selectType = newSelectedIds.length === 0 ? 1 : 2;
        ensureSelectionsHaveData();
      };

      const clearAll = () => {
        // 批量更新状态
        const newIds = [];
        if (JSON.stringify(state.selectedIds) !== JSON.stringify(newIds)) {
          state.selectedIds = newIds;
          state.selectType = 1;
        }
      };

      const removeId = (deviceIdentification: string) => {
        const currentIds = [...state.selectedIds];
        const idx = currentIds.indexOf(deviceIdentification);
        if (idx >= 0) {
          currentIds.splice(idx, 1);
          // 批量更新状态
          state.selectedIds = currentIds;
          if (currentIds.length === 0) {
            state.selectType = 1;
          }
        }
      };

      const change = () => fetchList();

      const handleSearch = () => {
        // 搜索时，重置到第一页并重新获取数据
        state.current = 1;
        fetchList();
      };

      const handleReset = () => {
        // 重置搜索条件
        state.searchDeviceName = '';
        state.searchDeviceIdentification = '';
        state.current = 1;
        fetchList();
      };

      const selectedList = computed(() => {
        ensureSelectionsHaveData();
        return state.selectedIds
          .map((deviceIdentification) => state.selectedDeviceMap[deviceIdentification])
          .filter((item) => !!item);
      });

      const goBack = () => selectTypeFn(1);

      const validate = () => {
        if (state.selectType === 2 && state.selectedIds.length === 0) return false;
        return true;
      };

      watch(
        () => [state.current, state.size],
        () => {
          fetchList();
        },
        { deep: true },
      );

      fetchList();

      expose({
        validate,
        reset: () => {
          resetComponentState({ clearSelection: true, clearCache: true, resetPager: true });
          fetchList();
        },
      });

      return {
        t,
        getDictLabel,
        ...toRefs(state),
        selectedList,
        selectTypeFn,
        onSelectRecord,
        clearAll,
        removeId,
        change,
        handleSearch,
        handleReset,
        goBack,
        childrenDevice,
        commonDevice,
        gatewayDevice,
        deviceDefault,
        Icon4,
        Icon5,
        DeleteY,
      };
    },
  });
</script>

<style lang="less" scoped>
  .device-selector {
    height: 100%;
  }

  .tr {
    text-align: right;
  }

  .select-device {
    height: 100%;
  }

  .return {
    text-align: right;
    margin-bottom: 16px;
  }

  .type-list {
    height: 100%;
    overflow-y: auto;

    .select-title {
      color: #1966ff;
      font-size: 16px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      &__label {
        display: flex;
        align-items: center;
      }
    }

    .isSelect {
      .device-item {
        margin-bottom: 16px;

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
            position: absolute;
            top: 34px;
            left: 0;
            padding: 0 16px;
          }
        }
      }
    }
  }

  .type-list__actions {
    text-align: right;
    margin-top: 8px;
  }

  .device-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    padding: 16px 0;
    cursor: pointer;
    position: relative;
    background-color: #fff;
    transition: all 0.5s;
    border: 2px solid transparent;

    .info {
      max-width: 100%;
    }

    &.isSelected {
      box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      border: 2px solid #1a66ff;
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      max-width: calc(100% - 158px);

      img {
        width: 60%;
        height: 60%;
      }

      .device-name {
        font-size: 16px;
        font-weight: 700;
        margin-bottom: 16px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
        // margin-bottom: 16px !important;
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
          // width: 55%;
          flex: 1;
          font-size: 14px;
          font-weight: 500;
          color: #2a2a2a;
          padding: 0 8px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .device-status {
      display: flex;
      align-items: center;
      position: absolute;
      top: 6px;
      left: 10px;
      border: 0 none;

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

    .device-btns {
      display: flex;
      width: 98px;
      height: 28px;
      border-radius: 45px;
      border: 2px solid #1a66ff;
      justify-content: center;
      align-items: center;
      position: absolute;
      top: 6px;
      right: 10px;

      .btn {
        width: 28px;
        text-align: center;
        position: relative;

        & + .btn::before {
          content: '';
          display: block;
          position: absolute;
          width: 1px;
          height: 7px;
          background-color: #e2e2e2;
          left: 0;
          top: 5px;
        }

        img {
          width: 15px;
          height: 15px;
          margin: 0 auto;
          cursor: pointer;
        }
      }
    }
  }

  .card-list {
    background-color: #fff;
    padding: 6px;

    .search-wrapper {
      margin-bottom: 16px;
    }

    .loading {
      width: 100%;
      height: 600px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .isSelect {
      .card-device-item {
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
  }

  .card-device-item {
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
