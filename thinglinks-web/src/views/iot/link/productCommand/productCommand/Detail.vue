<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerModel"
    :title="t('iot.link.productCommand.productCommand.detailTitle')"
    :width="800"
    :maskClosable="false"
    :keyboard="true"
  >
    <a-tabs default-active-key="1" v-model:activeKey="currentKey">
      <a-tab-pane key="1" :tab="t('iot.link.productCommand.productCommand.issueParameters')" />
      <a-tab-pane key="2" :tab="t('iot.link.productCommand.productCommand.responseParameters')" />
    </a-tabs>
    <div v-if="serviceId && commandId">
      <productCommandRequest
        :serviceId="serviceId"
        :commandId="commandId"
        v-if="currentKey == '1'"
      />
      <productCommandResponse
        :serviceId="serviceId"
        :commandId="commandId"
        v-else-if="currentKey == '2'"
      />
    </div>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { Tabs } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import productCommandRequest from '../../productCommandRequest/productCommandRequest/index.vue';
  import productCommandResponse from '../../productCommandResponse/productCommandResponse/index.vue';

  export default defineComponent({
    name: '编辑产品模型设备服务命令表维护',
    components: {
      BasicDrawer,
      productCommandRequest,
      productCommandResponse,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
    },
    setup(_) {
      const { t } = useI18n();
      const currentKey = ref('0');
      const serviceId = ref('');
      const commandId = ref('');
      const [registerModel, { setDrawerProps: setProps }] = useDrawerInner(async (data) => {
        serviceId.value = '';
        setProps({ confirmLoading: false, getContainer: data?.getContainer });
        setTimeout(() => {
          serviceId.value = data?.record?.serviceId;
          commandId.value = data?.record?.id;
          currentKey.value = '1';
          setProps({ width: '80%' });
        }, 10);
      });

      return { t, registerModel, currentKey, serviceId, commandId };
    },
  });
</script>
