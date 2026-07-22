<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    title="查看详情"
    :maskClosable="false"
    @ok="handleOk"
    @cancel="handleCancel"
    :keyboard="true"
    :useWrapper="false"
    :height="600"
  >
    <div class="container">
      <a-progress :percent="dataSource.progress" />
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="baseInfo" tab="基本信息">
          <BasicForm @register="registerForm" />
        </a-tab-pane>
        <a-tab-pane key="upgrade" tab="升级日志">
          <a-collapse v-model:activeKey="collapseActiveKey">
            <a-collapse-panel key="upgradeStatus" :showArrow="false" :disabled="true">
              <template #header> <div class="collapse_title">升级状态</div> </template>

              <template #extra>
                <span class="upgrade_icon">{{
                  getDictLabel(DictEnum.LINK_OTA_TASK_RECORD_STATUS, dataSource.upgradeStatus)
                }}</span>
                <SvgIcon name="iot-link-ota-upgradeWait" v-if="dataSource.upgradeStatus === 0" />
                <SvgIcon name="iot-link-ota-upgrading" v-else-if="dataSource.upgradeStatus === 1" />
                <SvgIcon name="iot-link-ota-upgradeSuccess" v-else-if="dataSource.upgradeStatus === 2" />
                <SvgIcon name="iot-link-ota-upgradeFail" v-else-if="dataSource.upgradeStatus === 3" />
              </template>
            </a-collapse-panel>
            <a-collapse-panel key="successDetails" :disabled="!dataSource.successDetails">
              <template #header> <div class="collapse_title">成功信息</div> </template>
              <template #extra>
                <SvgIcon name="iot-link-ota-upgradeSuccess" />
              </template>
              <p>{{ dataSource.successDetails }}</p>
            </a-collapse-panel>
            <a-collapse-panel key="failureDetails" :disabled="!dataSource.failureDetails">
              <template #header> <div class="collapse_title">失败信息</div> </template>
              <template #extra>
                <SvgIcon name="iot-link-ota-upgradeFail" />
              </template>
              <p>{{ dataSource.failureDetails }}</p>
            </a-collapse-panel>
            <a-collapse-panel key="logDetails" :disabled="!dataSource.logDetails">
              <template #header> <div class="collapse_title">升级日志</div> </template>
              <template #extra>
                <SvgIcon name="iot-link-ota-upgradeLog" />
              </template>
              <p>{{ dataSource.logDetails }}</p>
            </a-collapse-panel>
          </a-collapse>
        </a-tab-pane>
      </a-tabs>
    </div>
  </BasicModal>
</template>

<script setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { editFormSchema } from './otaUpgradeRecords.data';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';

  const { getDictLabel } = useDict();
  const activeKey = ref('baseInfo');
  const collapseActiveKey = ref(['1']);
  const dataSource = ref({});
  const type = ref(ActionEnum.VIEW);
  const [registerForm, { resetSchema, resetFields, setFieldsValue }] = useForm({
    name: 'OtaUpgradeRecordsEdit',
    labelWidth: 100,
    schemas: editFormSchema(type),
    showActionButtonGroup: false,
    disabled: true,
    baseColProps: { span: 11 },
    actionColOptions: {
      span: 22,
    },
  });
  const [registerModel, { closeModal }] = useModalInner(async (data) => {
    await handleReset();
    await setFieldsValue(data?.record);
    dataSource.value = data?.record || {};
    collapseActiveKey.value = ['1'];
    type.value = data?.type || ActionEnum.VIEW;
  });

  const handleReset = async () => {
    await resetSchema(editFormSchema(type));
    await resetFields();
    activeKey.value = 'baseInfo';
    dataSource.value = {};
  };

  const handleCancel = () => {
    handleReset();
    closeModal();
  };

  const handleOk = async () => {
    handleReset();
    closeModal();
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0;
    height: 400px;
    .collapse_title {
      font-weight: 500;
    }
    .upgrade_icon {
      display: inline-block;
      margin-right: 8px;
    }
  }
  :deep(.ant-picker.ant-picker-disabled) {
    width: 100%;
  }
  :deep(.ant-collapse .ant-collapse-item-disabled > .ant-collapse-header) {
    color: rgba(0, 0, 0, 0.85);
  }
  :deep(.ant-tabs-content-holder) {
    height: 312px;
    overflow: auto;
  }
  :deep(.ant-progress-line) {
    width: 96%;
  }
</style>
