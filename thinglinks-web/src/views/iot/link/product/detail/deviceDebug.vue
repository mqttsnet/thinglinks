<template>
  <div class="device-debug">
    <a-alert
      type="info"
      show-icon
      :message="t('iot.link.productCommand.productCommand.draftHint')"
      style="margin-bottom: 12px"
    />
    <actionSelect
      ref="triggerAction"
      :type="3"
      style="width: 100%"
      :productIdentification="productIdentification"
    />
    <a-button style="margin: 10px 0; width: 100%" type="primary" @click="issueParams">
      <PlusOutlined />
      {{ t('iot.link.productCommand.productCommand.commandIssued') }}
    </a-button>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, getCurrentInstance } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { Button, Alert } from 'ant-design-vue';
import actionSelect from '/@/views/iot/rule/engine/linkage/components/action/actionSelect.vue';
import { issueCommands } from '/@/api/iot/link/deviceCommand/deviceCommand';
import { convertToType } from '/@/utils/index';
import { PlusOutlined } from '@ant-design/icons-vue';

export default defineComponent({
  name: 'DeviceDebug',
  components: {
    AButton: Button,
    AAlert: Alert,
    PlusOutlined,
    actionSelect,
  },
  props: {
    productIdentification: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance();
    const { createMessage } = useMessage();
    const { t } = useI18n();

    const state = reactive({
      productIdentification: props.productIdentification,
    });

    const getParamsObject = (arr: any[]) => {
      const jsonData: Record<string, any> = {};
      arr.forEach((item) => {
        if (item.datatype) {
          jsonData[item.key] = convertToType(item.datatype, item.value);
        } else {
          jsonData[item.key] = item.value;
        }
      });
      return jsonData;
    };

    const issueParams = async () => {
      const triggerAction = (proxy as any).$refs.triggerAction.actions;
      console.log(triggerAction);
      const actionContent = triggerAction[0].actionContent;
      const commandWrapper = {
        serial: actionContent.serial.map((actionItem: any) => {
          return {
            msgType: actionItem.msgType,
            cmd: actionItem.cmd,
            params: getParamsObject(actionItem.params),
            serviceCode: actionItem.serviceCode,
            deviceIdentification: actionItem.deviceIdentification,
            productIdentification: actionItem.productIdentification,
          };
        }),
        parallel: actionContent.parallel.map((actionItem: any) => {
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
      issueParams,
    };
  },
});
</script>

<style lang="less" scoped>
/* 父级 panel-card 已固定高度,这里 100% 撑满 + 自身滚动(命令调试可能很长) */
.device-debug {
  width: 100%;
  height: 100%;
  overflow-y: auto;
}
</style>