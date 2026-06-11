<template>
  <div>
    <a-button v-if="!props.disabled" type="primary" :icon="h(PlusOutlined)" @click="selectDevice">
      {{ t('iot.link.engine.linkage.selectDevice') }}
    </a-button>
    <div class="summary-card" :class="{ 'summary-card-disabled': !props.disabled }">
      <div class="summary-header">
        <!-- 已选择{{ props.selectedDevice?.length || 0 }}个设备 -->
        {{
          t('iot.basic.basicComp.BasicSelectDeviceModal.selectedDeviceCount', {
            value: props.selectedDevice?.length || 0,
          })
        }}
      </div>
      <div class="summary-list" v-if="props.selectedDevice && props.selectedDevice.length">
        <div
          class="summary-item"
          v-for="(dev, idx) in props.selectedDevice"
          :key="(typeof dev === 'string' ? dev : dev?.deviceIdentification) ?? idx"
        >
          <span class="label">{{ t('iot.link.device.device.deviceIdentification') }}: </span>
          <span class="value">
            {{ typeof dev === 'string' ? dev : dev?.deviceIdentification || '-' }}
          </span>
        </div>
      </div>
      <a-empty v-else :description="t('iot.basic.basicComp.BasicSelectDeviceModal.noDevice')" />
    </div>
    <BasicModal
      v-bind="$attrs"
      @register="registerModal"
      :title="t('iot.link.engine.linkage.selectDevice')"
      :maskClosable="false"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :keyboard="true"
      width="1200px"
      :bodyStyle="{ height: '800px' }"
    >
      <BasicDeviceSelector
        :product-identification="props.productIdentification"
        v-model="tempSelectedDevices"
        :showSelectedDevice="true"
        :allowSelectAll="false"
        :showBackButton="false"
        :multiple="true"
        @change="handleSelect"
      />
    </BasicModal>
  </div>
</template>
<script lang="ts" setup>
  import { h, ref, defineProps } from 'vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import BasicDeviceSelector from '/@/components/Thinglinks/Iot/BasicSelect/BasicDevice/BasicDeviceSelector.vue';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { t } = useI18n();
  const [registerModal, { openModal, closeModal }] = useModal();

  const tempSelectedDevices = ref<any[]>([]); // 临时选择的设备

  const props = defineProps({
    productIdentification: String,
    selectedDevice: Array,
    disabled: Boolean,
  });

  const emit = defineEmits<{
    (e: 'select', value: any[]): void;
  }>();

  const selectDevice = () => {
    // 打开弹窗时，将当前已选设备复制到临时状态
    const currentSelected = props.selectedDevice || [];
    tempSelectedDevices.value = Array.isArray(currentSelected) ? [...currentSelected] : [];
    openModal(true);
  };

  const handleSelect = (selectedDevices: Array<any>) => {
    // 更新临时选择状态，确保是数组
    tempSelectedDevices.value = Array.isArray(selectedDevices) ? selectedDevices : [];
  };

  const handleSubmit = () => {
    // 直接提交选择结果
    emit('select', tempSelectedDevices.value);
    closeModal();
  };

  const handleCancel = () => {
    // 取消时重置临时选择状态为当前已确认的选择
    const currentSelected = props.selectedDevice || [];
    tempSelectedDevices.value = Array.isArray(currentSelected) ? [...currentSelected] : [];
    closeModal();
  };
</script>
<style scoped>
  .summary-card {
    background-color: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    padding: 12px 16px;
  }

  .summary-card-disabled {
    margin-top: 12px;
  }

  .summary-header {
    font-size: 14px;
    font-weight: 600;
    color: #1f1f1f;
    margin-bottom: 8px;
  }

  .summary-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    max-height: 320px;
    overflow: auto;
  }

  .summary-item {
    display: flex;
    align-items: center;
    padding: 6px 8px;
    border: 1px dashed #e5e5e5;
    border-radius: 6px;
    background: #fafafa;
  }

  .summary-item .label {
    color: #8c8c8c;
    font-size: 12px;
    margin-right: 4px;
  }

  .summary-item .value {
    color: #262626;
    font-size: 13px;
    font-weight: 500;
  }

  :deep() .ant-picker {
    width: 100%;
  }
</style>
