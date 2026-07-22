<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="'新增设备升级'"
    :maskClosable="false"
    @ok="handleSubmit"
    :destroyOnClose="true"
    :keyboard="true"
    :showOkBtn="false"
    :showCancelBtn="false"
    width="1200px"
  >
    <div class="two">
      <div class="form_box">
        <div class="loading" v-if="loading">
          <a-spin />
        </div>
        <a-row :gutter="[20]" v-if="selectType == 1">
          <a-col :span="12">
            <div
              class="select-all-device"
              :class="{ active: selectType == 1 }"
              @click="selectTypeFn(1)"
            >
              <span>{{ t('iot.link.engine.linkage.allDevices') }}</span>
              <img :src="childrenDevice" alt="" />
            </div>
          </a-col>
          <a-col :span="12">
            <div class="select-all-device" @click="selectTypeFn(2)">
              <span>自定义</span>
              <img :src="commonDevice" alt="" />
            </div>
          </a-col>
        </a-row>
        <div class="select-device" v-else>
          <div class="return">
            <a-button type="primary" @click="selectType = 1">返回</a-button>
          </div>
          <a-row :gutter="[24, 24]">
            <a-col :span="6">
              <SelectedDeviceList
                :selectedDeviceList="selectedDevice"
                @deleteDevice="deleteDevice"
              />
            </a-col>
            <a-col :span="18">
              <CardList
                :isSelect="true"
                @selectDeviceCard="selectDeviceCard"
                :productIdentification="productIdentification"
              />
            </a-col>
          </a-row>
        </div>
      </div>
    </div>
    <template #appendFooter>
      <a-button type="primary" @click="confirm">确定</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs, reactive, getCurrentInstance, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Row, Col } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import CardList from '/@/components/iot/link/device/CardList.vue';
  import SelectedDeviceList from './SelectedDeviceList.vue';
  import childrenDevice from '/@/assets/images/iot/link/deviceAndProduct/childrenDevice.png';
  import commonDevice from '/@/assets/images/iot/link/deviceAndProduct/commonDevice.png';
  import { upgradeAgain } from '/@/api/iot/link/ota/otaUpgradeTasks';

  export default defineComponent({
    name: '新增设备升级',
    components: {
      BasicModal,
      CardList,
      SelectedDeviceList,
      ARow: Row,
      ACol: Col,
    },
    emits: ['success', 'register', 'saveTriggerAction', 'saveTriggerRule'],
    setup() {
      const { t } = useI18n();

      const { createMessage } = useMessage();

      const state = reactive({
        selectType: 1,
        upgradeTaskId: '',
        productIdentification: '',
        selectedDevice: [],
        loading: false,
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        state.selectType = 1;
        state.productIdentification = '';
        state.selectedDevice = [];
        state.loading = false;

        state.upgradeTaskId = data.upgradeTaskId;
        state.productIdentification = data.productIdentification;
      });

      const selectDeviceCard = (device) => {
        if (
          state.selectedDevice.find(
            (item) => device.deviceIdentification === item.deviceIdentification,
          )
        ) {
          createMessage.warning('已选择该设备，无需重新选择');
        } else {
          state.selectedDevice.push(device);
        }
      };

      const selectTypeFn = (type) => {
        state.selectType = type;
      };

      const handleSubmit = () => {
        closeModal();
      };

      const confirm = async () => {
        if (!state.selectedDevice.length && state.selectType === 2) {
          return createMessage.warning('请选择需要升级的数据');
        }

        let deviceIdentificationList = [];
        if (state.selectType === 2) {
          deviceIdentificationList = state.selectedDevice.map((i) => i.deviceIdentification);
        }
        state.loading = true;
        try {
          const data = await upgradeAgain({
            deviceIdentificationList,
            upgradeTaskId: state.upgradeTaskId,
          });

          if (data) {
            createMessage.success(data);
            closeModal();
          }
        } catch (err) {
          console.log(err);
        }
        state.loading = false;
      };

      const deleteDevice = (deviceIdentification) => {
        state.selectedDevice = state.selectedDevice.filter(
          (item) => item.deviceIdentification !== deviceIdentification,
        );
      };

      return {
        t,
        registerModal,
        selectDeviceCard,
        selectTypeFn,
        handleSubmit,
        confirm,
        deleteDevice,
        ...toRefs(state),
      };
    },
    data() {
      return {
        childrenDevice,
        commonDevice,
      };
    },
  });
</script>
<style lang="less" scope>
  .heightwrapper {
    height: 100%;
  }

  .form_box {
    padding: 20px;
  }

  .form_title {
    font-size: 16px;
    font-family: PingFang SC-Semibold, PingFang SC;
    font-weight: 600;
    color: #2e3033;
    line-height: 20px;
    padding-left: 9px;
    border-left: 3px solid #1a66ff;
  }

  .two {
    .ant-steps {
      width: 60%;
      margin: 0 auto;
    }
  }

  .select-all-device {
    display: flex;
    margin: 20px 100px;
    padding: 10px 20px;
    justify-content: space-between;
    align-items: center;
    border: 2px solid #e6ebf5;
    cursor: pointer;
    font-size: 18px;
    border-radius: 4px;

    &.active {
      border-color: #1a66ff;
    }

    img {
      width: 120px;
    }
  }

  .return {
    text-align: right;
  }

  .loading {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: absolute;
    background: rgba(240, 242, 245, 0.4);
    z-index: 1;
  }
</style>
../../../../../../../api/iot/link/product/product../../../../../../../api/iot/link/device/device
