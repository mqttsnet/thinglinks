<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="
      triggerType >= 2
        ? t('iot.link.engine.linkage.executeAction')
        : t('iot.link.engine.linkage.triggerRule')
    "
    :maskClosable="false"
    @ok="handleSubmit"
    :destroyOnClose="true"
    :keyboard="true"
    :showOkBtn="false"
    :showCancelBtn="false"
    width="1200px"
    min-height="400px"
  >
    <div :class="triggerType == 1 ? 'two' : ''">
      <!-- <a-steps :current="activeStep" @change="next"> -->
      <a-steps :current="activeStep">
        <a-step :title="t('iot.link.engine.linkage.selectProduct')" v-if="triggerType < 3" />
        <a-step :title="t('iot.link.engine.linkage.selectDevice')" />
        <!-- <a-step title="触发类型" v-if="triggerType == 1" /> -->
        <a-step :title="t('iot.link.engine.linkage.executeAction')" v-if="triggerType >= 2" />
      </a-steps>
      <div class="form_box" v-show="activeStep == 0">
        <a-row :gutter="[24, 24]">
          <a-col :span="6" class="heightwrapper">
            <SelectedProduct :selectedProduct="selectedProduct" @delete-product="deleteProduct" />
          </a-col>
          <a-col :span="18" class="heightwrapper">
            <ProductCardList
              :isSelect="true"
              @select-product-card="selectProductCard"
              :productIdentification="selectedProduct.productIdentification"
            />
          </a-col>
        </a-row>
      </div>
      <div class="form_box" v-if="activeStep == 1">
        <a-row
          :gutter="[20]"
          v-if="isAllDevice(selectedDevice.deviceIdentification) || selectType == 1"
        >
          <a-col :span="12">
            <div
              class="select-all-device"
              :class="{ active: isAllDevice(selectedDevice.deviceIdentification) }"
              @click="
                selectType = 1;
                selectedDevice.deviceIdentification = 'all';
                selectedDeviceIds = [];
              "
            >
              <span>{{ t('iot.link.engine.linkage.allDevices') }}</span>
              <GatewayDeviceSvg class="select-device-svg" />
            </div>
          </a-col>
          <a-col :span="12">
            <div
              class="select-all-device"
              @click="
                selectType = 2;
                selectedDevice.deviceIdentification = '';
                selectedDeviceIds = [];
              "
            >
              <span>{{ t('iot.link.engine.linkage.custom') }}</span>
              <CommonDeviceSvg class="select-device-svg" />
            </div>
          </a-col>
        </a-row>
        <div class="select-device" v-else>
          <div class="return">
            <a-button
              type="primary"
              @click="
                selectType = 1;
                selectedDevice.deviceIdentification = 'all';
                selectedDeviceIds = [];
              "
              >{{ t('common.back') }}</a-button
            >
          </div>
          <BasicDeviceSelector
            v-model="selectedDeviceIds"
            :productIdentification="selectedProduct.productIdentification"
            :showBackButton="false"
            :showSelectedDevice="true"
            :allowSelectAll="false"
            :multiple="false"
          />
        </div>
      </div>

      <div class="form_box" v-if="activeStep == 2 && triggerType >= 2">
        <actionsList
          ref="actionsListState"
          @select-type-card="selectActionCard"
          :productIdentification="selectedProduct.productIdentification"
          :actionItem="actionItem"
        />
      </div>
    </div>
    <template #appendFooter>
      <!-- <a-button @click="prev" v-if="(triggerType < 3 && activeStep > 0 )|| (triggerType == 3 && activeStep>1)">上一步</a-button> -->
      <a-button @click="prev" v-if="activeStep > 0">{{
        t('iot.link.engine.linkage.prev')
      }}</a-button>
      <a-button type="primary" @click="next">{{ t('iot.link.engine.linkage.next') }}</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs, reactive, getCurrentInstance, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Steps, Row, Col } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import ProductCardList from '/@/components/iot/link/product/ProductCardList.vue';
  import BasicDeviceSelector from '/@/components/iot/BasicSelect/BasicDevice/BasicDeviceSelector.vue';
  import SelectedProduct from './SelectedProduct.vue';
  import actionsList from '../action/actionsList.vue';
  import { CommonDeviceSvg, GatewayDeviceSvg } from '/@/components/iot/svg';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import { detailBydeviceIdentification } from '/@/api/iot/link/device/device';
  import { canConvertType } from '/@/utils/index';

  const ALL_DEVICE_VALUE = 'all';

  export default defineComponent({
    name: '规则联动详情',
    components: {
      BasicModal,
      ProductCardList,
      BasicDeviceSelector,
      SelectedProduct,
      actionsList,
      CommonDeviceSvg,
      GatewayDeviceSvg,
      [Steps.name]: Steps,
      [Steps.Step.name]: Steps.Step,
      ARow: Row,
      ACol: Col,
    },
    props: {
      triggerType: {
        type: Number,
        default: 1,
      },
    },
    emits: ['success', 'register', 'saveTriggerAction', 'saveTriggerRule'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const { proxy } = getCurrentInstance();

      const state = reactive({
        activeStep: 0,
        selectedProduct: {},
        selectedDeviceIds: [] as string[],
        selectedDevice: {},
        selectedType: {},
        triggerType: props.triggerType,
        index: 0,
        childrenIndex: 0,
        actionIndex: 0,
        actionChildrenIndex: null,
        actionsAddType: 0,
        actionItem: {},
        selectType: 1, // 1选择全部和自定义  2 选择自定义
      });
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        type.value = data?.type || ActionEnum.ADD;
        // 触发条件
        state.index = data?.index || 0;
        state.childrenIndex = data?.childrenIndex || 0;

        // 执行动作
        state.actionIndex = data?.actionIndex || 0;
        state.actionChildrenIndex = data?.actionChildrenIndex;
        state.actionsAddType = data?.actionsAddType || 0;

        if (props.triggerType == 3) {
          state.activeStep = 1;
          state.selectedProduct = {
            productIdentification: data.productIdentification,
          };
        } else {
          state.activeStep = 0;
          state.selectedProduct = {};
        }
        state.selectedDevice = {};
        state.selectedType = {};
        if (unref(type) !== ActionEnum.ADD) {
          // 赋值
          if (props.triggerType == 2 || props.triggerType == 3) {
            // 动作的回显
            if (data.actionItem) {
              state.activeStep = null;
              await getSelectDetailByProductIdentification(data.actionItem.productIdentification);
              await getSelectDetailByDeviceIdentification(data.actionItem.deviceIdentification);
              state.activeStep = 2;
              state.actionItem = data.actionItem;

              // state.selectedDevice.deviceIdentification = data.actionItem.deviceIdentification;
            }
          } else {
            if (data.conditionItem.leftParam) {
              await getSelectDetailByProductIdentification(
                data.conditionItem.leftParam.productIdentification,
              );
              await getSelectDetailByDeviceIdentification(
                data.conditionItem.leftParam.deviceIdentification,
              );
              // state.selectedDevice.deviceIdentification = data.actionItem.deviceIdentification;
            }
          }
        } else {
          // 重置
        }
      });
      const getSelectDetailByProductIdentification = async (productIdentification) => {
        const res = await getFullProductInfo(productIdentification);
        state.selectedProduct = res;
      };
      const getSelectDetailByDeviceIdentification = async (deviceIdentification) => {
        if (isAllDevice(deviceIdentification)) {
          state.selectedDeviceIds = [];
          state.selectedDevice.deviceIdentification = ALL_DEVICE_VALUE;
        } else if (deviceIdentification) {
          const res = await detailBydeviceIdentification(deviceIdentification);
          state.selectedDevice = res;
          state.selectedDeviceIds = [deviceIdentification];
          state.selectType = 2;
        } else {
          state.selectedDeviceIds = [];
          state.selectedDevice = {};
        }
      };
      const selectProductCard = (column) => {
        if (state.selectedProduct.productIdentification == column.productIdentification) {
          createMessage.warn(t('iot.link.productCommand.productCommand.description4'));
          return false;
        }
        state.selectedProduct = column;
        state.selectedDeviceIds = [];
        state.selectedDevice = {};
        state.actionItem = {};
      };
      const selectTypeCard = (column) => {
        state.selectedType = column;
      };
      const { createMessage } = useMessage();

      const next = () => {
        if (state.activeStep == 0) {
          if (!state.selectedProduct.productIdentification) {
            createMessage.warn(t('iot.link.productCommand.productCommand.description5'));
            return false;
          }
          state.activeStep = 1;
        } else if (state.activeStep == 1) {
          // 检查是否选择了设备
          if (state.selectedDeviceIds.length === 0 && !state.selectedDevice.deviceIdentification) {
            createMessage.warn(t('iot.link.productCommand.productCommand.description1'));
            return false;
          }
          // 同步 selectedDevice 数据
          if (state.selectedDeviceIds.length > 0) {
            state.selectedDevice.deviceIdentification = state.selectedDeviceIds[0];
          }
          if (state.triggerType == 1) {
            createMessage.success(t('common.tips.saveSuccess'));
            emit('saveTriggerRule', { ...state });
            handleSubmit();
          } else {
            state.activeStep = 2;
          }
        } else {
          const command = proxy.$refs.actionsListState.command;
          if (
            !command.service.serviceCode ||
            !command.commands.commandCode ||
            !paramsRule(command.params)
          ) {
            createMessage.warn(t('iot.link.productCommand.productCommand.description6'));
            return false;
          }
          // TODO 类型校验失败数据提示信息处理
          // if (paramsTypeRule(command.params)?.length) {
          //   const errKeyText = paramsTypeRule(command.params).map(item => item.key)?.join('、');
          //   notification.warn({
          //     message: t('common.tips.tips'),
          //     description: `以下执行动作参数类型错误：${errKeyText}`,
          //   });
          //   return false;
          // }
          if (!paramsTypeRule(command.params)) {
            createMessage.warn(t('iot.link.productCommand.productCommand.description7'));
            return false;
          }
          createMessage.success(t('common.tips.saveSuccess'));
          emit('saveTriggerAction', { ...state, ...command });
          state.actionChildrenIndex = null;
          handleSubmit();
        }
      };
      // 判空校验
      const paramsRule = (arr) => {
        let ruleFlag = true;
        arr.forEach((item) => {
          if (!item.key || !item.value) {
            ruleFlag = false;
          }
        });
        return ruleFlag;
      };
      // 参数类型校验
      const paramsTypeRule = (arr) => {
        let errInfo = [];
        arr.forEach((item) => {
          if (!item?.datatype) return;
          if (!canConvertType(item?.datatype, item?.value)) {
            errInfo.push(item);
          }
        });
        return errInfo;
      };
      const prev = () => {
        state.activeStep--;
      };
      const handleSubmit = () => {
        closeModal();
      };
      const deleteProduct = () => {
        state.selectedProduct = {};
      };
      const isAllDevice = (value) => value === ALL_DEVICE_VALUE;

      return {
        type,
        t,
        registerModal,
        selectProductCard,
        selectTypeCard,
        handleSubmit,
        deleteProduct,
        isAllDevice,
        next,
        prev,
        ...toRefs(state),
      };
    },
  });
</script>
<style lang="less" scoped>
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
    border-left: 3px solid @primary-color;
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
      border-color: @primary-color;
    }

    .select-device-svg {
      width: 120px;
      height: auto;
    }
  }

  .return {
    text-align: right;
  }
</style>
../../../../../../../api/iot/link/product/product../../../../../../../api/iot/link/device/device
