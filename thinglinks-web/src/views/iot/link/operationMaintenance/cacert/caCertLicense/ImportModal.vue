<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t(`${tNs}.importTitle`)"
    :maskClosable="false"
    :keyboard="true"
    :ok-text="t(`${tNs}.import.confirm`)"
    @ok="handleSubmit"
    :width="720"
  >
    <a-alert
      type="info"
      show-icon
      :message="t(`${tNs}.import.tip`)"
      style="margin-bottom: 16px"
    />
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { importPemCertificate } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import { importSchema } from './caCertLicense.data';

  defineOptions({ name: '导入证书' });

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emit = defineEmits<{ (e: 'success'): void; (e: 'register', ...args: any[]): void }>();

  const [registerForm, { resetFields, validate, resetSchema }] = useForm({
    name: 'CaCertLicenseImport',
    labelWidth: 120,
    schemas: importSchema(),
    showActionButtonGroup: false,
    baseColProps: { span: 22 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async () => {
    setModalProps({ confirmLoading: false });
    await resetSchema(importSchema());
    await resetFields();
  });

  async function handleSubmit() {
    try {
      const params = await validate();
      setModalProps({ confirmLoading: true });
      await importPemCertificate(params);
      createMessage.success(t(`${tNs}.import.success`));
      closeModal();
      emit('success');
    } catch (e: any) {
      // 校验异常 (e?.errorFields) 直接交给 BasicForm 处理,业务异常给消息提示
      if (e?.errorFields) return;
      createMessage.error(e?.message || t(`${tNs}.import.failed`));
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
