<template>
  <div class="device-debug" style="width: 100%">
    <actionSelect
      ref="triggerAction"
      :type="3"
      style="width: 100%"
      :productIdentification="productIdentification"
    />
    <a-button style="margin: 10px 0; width: 100%" type="primary" @click="issueParams"
      ><PlusOutlined />{{ t('iot.link.productCommand.productCommand.commandIssued') }}</a-button
    >
  </div>
</template>
<script lang="ts">
  import { defineComponent, toRefs, reactive, onMounted, getCurrentInstance } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Row, Col } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import SelectedDevice from '/@/views/iot/rule/engine/linkage/components/modal/SelectedDevice.vue';
  import actionsList from '/@/views/iot/rule/engine/linkage/components/action/actionsList.vue';
  import actionSelect from '/@/views/iot/rule/engine/linkage/components/action/actionSelect.vue';
  import CardList from '/@/components/iot/link/device/CardList.vue';
  import { issueCommands } from '/@/api/iot/link/deviceCommand/deviceCommand';
  import { convertToType } from '/@/utils/index';
  import {
    ApartmentOutlined,
    ControlOutlined,
    ApiOutlined,
    PlusOutlined,
    CloseCircleOutlined,
    CloseSquareOutlined,
    DeleteOutlined,
    PartitionOutlined,
    AlertOutlined,
    PullRequestOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictList } = useDict();

  export default defineComponent({
    name: '设备调试',
    components: {
      ApartmentOutlined,
      ControlOutlined,
      ApiOutlined,
      PlusOutlined,
      CloseCircleOutlined,
      CloseSquareOutlined,
      DeleteOutlined,
      PartitionOutlined,
      AlertOutlined,
      PullRequestOutlined,
      actionSelect,
      SelectedDevice,
      actionsList,
      CardList,
      Row,
      Col,
    },
    props: {
      productIdentification: {
        type: String,
        default: '',
      },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { proxy } = getCurrentInstance();
      const { createMessage } = useMessage();
      const { t } = useI18n();
      const state = reactive({
        productIdentification: props.productIdentification,
        selectedDevice: {},
        debugStep: 1,
        actionItem: {},
      });
      onMounted(() => {
        load();
      });
      const load = async () => {};

      const handleSuccess = () => {
        load();
      };
      const selectDeviceCard = (column) => {
        if (state.selectedDevice.deviceIdentification == column.deviceIdentification) {
          createMessage.warn(t('iot.link.productCommand.productCommand.description1'));
          return false;
        }
        state.selectedDevice = column;
      };
      const deleteDevice = () => {
        // state.selectedDevice.splice(index,1)
        state.selectedDevice = {};
      };
      const startDebug = (step) => {
        // state.selectedDevice.splice(index,1)
        if (step == 1) {
          state.debugStep = step;
        } else {
          if (state.selectedDevice?.deviceIdentification) {
            state.debugStep = step;
          } else {
            createMessage.warn(t('iot.link.productCommand.productCommand.description2'));
            return false;
          }
        }
      };
      const getParamsObject = (arr) => {
        let jsonData = {};
        arr.map((item) => {
          if (item.datatype) {
            jsonData[item.key] = convertToType(item.datatype, item.value);
          } else {
            jsonData[item.key] = item.value;
          }
        });
        return jsonData;
      };
      const issueParams = async () => {
        const triggerAction = proxy.$refs.triggerAction.actions;
        console.log(triggerAction);
        const actionContent = triggerAction[0].actionContent;
        const commandWrapper = {
          serial: actionContent.serial.map((actionItem) => {
            return {
              msgType: actionItem.msgType,
              cmd: actionItem.cmd,
              params: getParamsObject(actionItem.params),
              serviceCode: actionItem.serviceCode,
              deviceIdentification: actionItem.deviceIdentification,
              productIdentification: actionItem.productIdentification,
            };
          }),
          parallel: actionContent.parallel.map((actionItem) => {
            return {
              msgType: actionItem.msgType,
              cmd: actionItem.cmd,
              params: getParamsObject(actionItem.params),
              serviceCode: actionItem.serviceCode,
              deviceIdentification: actionItem.deviceIdentification,
              productIdentification: actionItem.productIdentification,
            };
          }),
        };
        if (!commandWrapper.serial.length && !commandWrapper.parallel.length) {
          createMessage.error(t('iot.link.productCommand.productCommand.description3'));
          return;
        }
        const res = await issueCommands(commandWrapper);
        if (res) {
          createMessage.success(t('iot.link.productCommand.productCommand.commmandIssuedSuccess'));
        } else {
          createMessage.error(t('iot.link.productCommand.productCommand.commmandIssuedError'));
        }
      };

      return {
        t,
        ...toRefs(state),
        deleteDevice,
        selectDeviceCard,
        startDebug,
        issueParams,
      };
    },
  });
</script>
../../../../api/iot/link/product/product../../../../api/iot/link/device/device../../../../api/iot/link/deviceCommand/deviceCommand
