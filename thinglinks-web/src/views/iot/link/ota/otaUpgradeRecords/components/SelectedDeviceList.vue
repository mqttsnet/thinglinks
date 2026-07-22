<template>
  <div class="type-list">
    <div class="select-title">
      <div>
        <ApartmentOutlined style="margin-right: 4px" />{{
          t('iot.link.engine.linkage.selectedDevice')
        }}
      </div>
      <div class="slot">
        <slot name="debug"></slot>
      </div>
    </div>
    <template v-if="selectedDeviceList.length">
      <div class="isSelect" v-for="device in selectedDeviceList">
        <div class="device-item isSelected">
          <div class="device-info">
            <img v-if="device?.nodeType === 2" :src="childrenDevice" />
            <img v-else-if="device?.nodeType === 0" :src="gatwayDevice" />
            <img v-else-if="device?.nodeType === 1" :src="commonDevicesrc" />
            <img v-else :src="deviceManagement" />
            <div class="info">
              <a-tooltip placement="topLeft" :title="device?.deviceName">
                <div class="device-name">{{ device?.deviceName }}</div>
              </a-tooltip>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.deviceIdentification') }}</span>
                <a-tooltip placement="topLeft" :title="device?.deviceIdentification">
                  <span class="value">{{ device?.deviceIdentification }}</span>
                </a-tooltip>
              </div>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.nodeType') }}</span>
                <span class="value">{{
                  getDictLabel('LINK_DEVICE_NODE_TYPE', device?.nodeType, '')
                }}</span>
              </div>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.encryptMethod') }}</span>
                <span class="value">{{
                  getDictLabel('LINK_DEVICE_ENCRYPT_METHOD', device?.encryptMethod, '')
                }}</span>
              </div>
            </div>
          </div>
          <div class="device-status">
            <img :src="device?.connectStatus == 1 ? Icon4 : Icon5" alt="" class="img" />
            <span class="red" v-if="device?.connectStatus == 1">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', device?.connectStatus, '')
            }}</span>
            <span v-else-if="device?.connectStatus == 2">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', device?.connectStatus, '')
            }}</span>
            <span v-else-if="device?.connectStatus == 0">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', device?.connectStatus, '')
            }}</span>
            <a-divider type="vertical" />
            <span class="green" v-if="device?.deviceStatus == 1">{{
              getDictLabel('LINK_DEVICE_STATUS', device?.deviceStatus, '')
            }}</span>
            <span class="red" v-else-if="device?.deviceStatus == 2">{{
              getDictLabel('LINK_DEVICE_STATUS', device?.deviceStatus, '')
            }}</span>
            <span v-else-if="device?.deviceStatus == 0">{{
              getDictLabel('LINK_DEVICE_STATUS', device?.deviceStatus, '')
            }}</span>
          </div>
          <div class="device-btns" @click="deleteDevice(device)" v-if="deleteBtn">
            <!-- <div class="btn danger" @click="handleDelete(record)">删除</div> -->
            <div class="btn">
              <img :src="deleteY" alt="" />
            </div>
          </div>
        </div>
      </div>
    </template>
    <div class="empty" v-else>
      <a-empty :description="t('iot.link.engine.linkage.descriptionDevice')" />
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs, watch } from 'vue';
  import { ApartmentOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import Icon4 from '/@/assets/images/iot/link/device/Icon4.png';
  import Icon5 from '/@/assets/images/iot/link/device/Icon5.png';
  import childrenDevice from '/@/assets/images/iot/link/deviceAndProduct/childrenDevice.png';
  import commonDevice from '/@/assets/images/iot/link/deviceAndProduct/commonDevice.png';
  import gatwayDevice from '/@/assets/images/iot/link/deviceAndProduct/gatwayDevice.png';
  import deviceManagement from '/@/assets/images/iot/link/device/deviceManagement.gif';
  import deleteY from '/@/assets/images/iot/link/device/delete-y.png';

  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'SelectedDeviceList',
    components: {
      ApartmentOutlined,
    },
    props: {
      selectedDeviceList: {
        type: Object,
        default: {},
      },
      deleteBtn: {
        type: Boolean,
        default: true,
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      watch(
        () => props.selectedDeviceList,
        () => {
          state.selectedDeviceList = props.selectedDeviceList;
        },
      );
      watch(
        () => props.deleteBtn,
        () => {
          state.deleteBtn = props.deleteBtn;
        },
      );
      const state = reactive({
        selectedDeviceList: props.selectedDeviceList,
        typeId: null,
        deleteBtn: props.deleteBtn,
      });
      const deleteDevice = (device) => {
        emit('deleteDevice', device.deviceIdentification);
      };

      return {
        t,
        deleteDevice,
        getDictLabel,
        Icon4,
        Icon5,
        ...toRefs(state),
      };
    },
    data() {
      return {
        childrenDevice,
        commonDevice,
        gatwayDevice,
        deviceManagement,
        deleteY,
      };
    },
  });
</script>
<style lang="less" scoped>
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
    }
  }

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
          width: 55%;
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
      padding-top: 6px;
      // padding-bottom: 14px;
      // border-bottom: 1px dashed #d4d4d4;
      border: 0 none;
      display: flex;
      align-items: center;
      position: absolute;
      top: 6px;
      left: 10px;

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
      border-radius: 45px 45px 45px 45px;
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
</style>
