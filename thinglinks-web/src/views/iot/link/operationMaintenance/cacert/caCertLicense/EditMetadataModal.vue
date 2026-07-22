<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t(`${tNs}.edit.title`)"
    :maskClosable="false"
    :keyboard="true"
    :ok-text="t('common.title.save')"
    @ok="handleSubmit"
    :width="640"
  >
    <a-alert
      type="info"
      show-icon
      :message="t(`${tNs}.edit.tip`)"
      style="margin-bottom: 16px"
    />

    <!-- 不可改的核心字段(只读展示,提示用户) -->
    <a-descriptions
      bordered
      :column="1"
      size="small"
      style="margin-bottom: 16px"
    >
      <a-descriptions-item :label="t(`${tNs}.serialNumber`)">
        <span class="readonly">{{ record?.serialNumber || '-' }}</span>
      </a-descriptions-item>
      <a-descriptions-item :label="t(`${tNs}.commonName`)">
        <span class="readonly">{{ record?.commonName || '-' }}</span>
      </a-descriptions-item>
      <a-descriptions-item :label="t(`${tNs}.algorithm`)">
        <a-tag color="blue">
          {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, record?.algorithm, '-') }}
        </a-tag>
      </a-descriptions-item>
    </a-descriptions>

    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';
  import { update } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import { editMetadataSchema } from './caCertLicense.data';

  defineOptions({ name: '编辑CA许可证证书' });

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const emit = defineEmits<{ (e: 'success'): void; (e: 'register', ...args: any[]): void }>();

  const record = ref<any>(null);

  const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
    name: 'CaCertLicenseEditMetadata',
    labelWidth: 100,
    schemas: editMetadataSchema(),
    showActionButtonGroup: false,
    baseColProps: { span: 22 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(
    async (data: { record?: any }) => {
      setModalProps({ confirmLoading: false });
      record.value = data?.record ?? null;
      await resetSchema(editMetadataSchema());
      await resetFields();
      if (record.value) {
        // 把核心字段填回表单(algorithm/state 透传给后端 @NotNull)
        await setFieldsValue({
          id: record.value.id,
          algorithm: record.value.algorithm,
          state: record.value.state,
          certName: record.value.certName,
          remark: record.value.remark,
        });
      }
    },
  );

  async function handleSubmit() {
    try {
      const params = await validate();
      setModalProps({ confirmLoading: true });
      await update(params);
      createMessage.success(t(`${tNs}.edit.success`));
      closeModal();
      emit('success');
    } catch (e: any) {
      if (e?.errorFields) return; // 表单校验错误已由 BasicForm 显示
      createMessage.error(e?.message || t(`${tNs}.edit.failed`));
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .readonly {
    color: #888;
    font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
  }
</style>
