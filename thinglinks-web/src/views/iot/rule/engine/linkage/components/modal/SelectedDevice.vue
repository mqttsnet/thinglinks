<template>
  <div class="type-list">
    <div class="select-title">
      <div
        ><ApartmentOutlined style="margin-right: 4px" />{{
          t('iot.link.engine.linkage.selectedDevice')
        }}</div
      >
      <div class="slot"><slot name="debug"></slot></div>
    </div>
    <template v-if="selectedDevice.deviceIdentification">
      <!-- <div class="isSelect" v-for="(record,index) in selectedDevice" :key="index"> -->
      <div class="isSelect">
        <div class="device-item isSelected">
          <div class="device-info">
            <div class="device-art">
              <component :is="getDeviceNodeTypeSvg(selectedDevice?.nodeType)" />
            </div>
            <div class="info">
              <a-tooltip placement="topLeft" :title="selectedDevice?.deviceName">
                <div class="device-name">{{ selectedDevice?.deviceName }}</div>
              </a-tooltip>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.deviceIdentification') }}</span>
                <a-tooltip placement="topLeft" :title="selectedDevice?.deviceIdentification">
                  <span class="value">{{ selectedDevice?.deviceIdentification }}</span>
                </a-tooltip>
              </div>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.nodeType') }}</span>
                <span class="value">{{
                  getDictLabel('LINK_DEVICE_NODE_TYPE', selectedDevice?.nodeType, '')
                }}</span>
              </div>
              <div class="device-form">
                <span class="label">{{ t('iot.link.device.device.encryptMethod') }}</span>
                <span class="value">{{
                  getDictLabel('LINK_DEVICE_ENCRYPT_METHOD', selectedDevice?.encryptMethod, '')
                }}</span>
              </div>
            </div>
          </div>
          <div class="device-status">
            <span
              class="status-dot"
              :class="getConnectStatusClass(selectedDevice?.connectStatus)"
            ></span>
            <span class="red" v-if="selectedDevice?.connectStatus == 1">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', selectedDevice?.connectStatus, '')
            }}</span>
            <span v-else-if="selectedDevice?.connectStatus == 2">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', selectedDevice?.connectStatus, '')
            }}</span>
            <span v-else-if="selectedDevice?.connectStatus == 0">{{
              getDictLabel('LINK_DEVICE_CONNECT_STATUS', selectedDevice?.connectStatus, '')
            }}</span>
            <a-divider type="vertical" />
            <span class="green" v-if="selectedDevice?.deviceStatus == 1">{{
              getDictLabel('LINK_DEVICE_STATUS', selectedDevice?.deviceStatus, '')
            }}</span>
            <span class="red" v-else-if="selectedDevice?.deviceStatus == 2">{{
              getDictLabel('LINK_DEVICE_STATUS', selectedDevice?.deviceStatus, '')
            }}</span>
            <span v-else-if="selectedDevice?.deviceStatus == 0">{{
              getDictLabel('LINK_DEVICE_STATUS', selectedDevice?.deviceStatus, '')
            }}</span>
          </div>
          <div class="device-btns" @click="deleteDevice()" v-if="deleteBtn">
            <!-- <div class="btn danger" @click="handleDelete(record)">删除</div> -->
            <div class="btn">
              <Icon icon="ant-design:delete-outlined" class="action-icon" />
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
  import { getDeviceNodeTypeSvg } from '/@/components/iot/svg';
  import { Icon } from '/@/components/Icon';
  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'SelectedDevice',
    components: {
      ApartmentOutlined,
      Icon,
    },
    props: {
      // 多选数组
      // selectedDevice:{
      //   type: Array,
      //   default: []
      // }
      // 先设置为单选
      selectedDevice: {
        type: Object,
        default: () => {},
      },
      deleteBtn: {
        type: Boolean,
        default: true,
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      watch(
        () => props.selectedDevice,
        () => {
          state.selectedDevice = props.selectedDevice;
        },
      );
      watch(
        () => props.deleteBtn,
        () => {
          state.deleteBtn = props.deleteBtn;
        },
      );
      const state = reactive({
        selectedDevice: props.selectedDevice,
        typeId: null,
        deleteBtn: props.deleteBtn,
      });
      const deleteDevice = () => {
        emit('deleteDevice', 1);
      };
      const getConnectStatusClass = (status?: number | string | null) => {
        const value = Number(status);
        return {
          'status-dot--online': value === 1,
          'status-dot--inactive': value === 0,
          'status-dot--offline': value !== 0 && value !== 1,
        };
      };

      return {
        t,
        deleteDevice,
        getDictLabel,
        getDeviceNodeTypeSvg,
        getConnectStatusClass,
        ...toRefs(state),
      };
    },
  });
</script>
<style lang="less" scoped>
  .type-list {
    height: 100%;
    overflow-y: auto;

    .select-title {
      color: @primary-color;
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

        .device-art {
          width: 30%;

          :deep(svg) {
            width: 100%;
            height: auto;
          }
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
      border: 2px solid @primary-color;
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      max-width: calc(100% - 158px);

      .device-art {
        width: 60%;

        :deep(svg) {
          width: 100%;
          height: auto;
        }
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

      .status-dot {
        width: 8px;
        height: 8px;
        margin-right: 6px;
        border-radius: 50%;
        background: @text-color-secondary;

        &--online {
          background: @button-success-color;
        }

        &--inactive {
          background: @text-color-secondary;
        }

        &--offline {
          background: @button-error-color;
        }
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
      border: 2px solid @primary-color;
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

        .action-icon {
          color: @button-error-color;
          cursor: pointer;
          font-size: 15px;
        }
      }
    }
  }
</style>
