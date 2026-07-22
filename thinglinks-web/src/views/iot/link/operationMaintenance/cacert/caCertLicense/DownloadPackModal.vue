<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t(`${tNs}.action.downloadPack`)"
    :maskClosable="false"
    :destroyOnClose="true"
    :ok-text="t(`${tNs}.downloadPack.confirm`)"
    :ok-button-props="{ disabled: !record?.id }"
    :confirm-loading="submitting"
    @ok="handleConfirm"
    :width="520"
  >
    <a-alert
      type="info"
      show-icon
      :message="t(`${tNs}.downloadPack.tip`)"
      style="margin-bottom: 16px"
    />

    <a-form layout="vertical">
      <a-form-item :label="t(`${tNs}.downloadPack.caInfo`)">
        <div class="ca-info">
          <div class="ca-name">{{ record?.certName || '—' }}</div>
          <div class="ca-meta">{{ record?.serialNumber || '—' }}</div>
        </div>
      </a-form-item>

      <a-form-item :label="t(`${tNs}.downloadPack.notAfter`)" required>
        <a-date-picker
          v-model:value="notAfter"
          show-time
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          :placeholder="t(`${tNs}.downloadPack.notAfterPh`)"
          style="width: 100%"
          :disabled-date="disabledPast"
        />
      </a-form-item>
    </a-form>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import dayjs, { Dayjs } from 'dayjs';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadClientCertPack } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emit = defineEmits(['success', 'register']);

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const record = ref<any>(null);
  const notAfter = ref<string>(dayjs().add(1, 'year').format('YYYY-MM-DD HH:mm:ss'));
  const submitting = ref(false);

  const disabledPast = (current: Dayjs) => current && current.isBefore(dayjs(), 'day');

  const [registerModal, { closeModal }] = useModalInner((data: { record?: any }) => {
    record.value = data?.record ?? null;
    notAfter.value = dayjs().add(1, 'year').format('YYYY-MM-DD HH:mm:ss');
  });

  /**
   * 调后端流式下载接口拿到 Blob → 浏览器 a.click 触发本地保存.
   */
  async function handleConfirm() {
    if (!record.value?.id || !notAfter.value) return;
    submitting.value = true;
    try {
      const res = await downloadClientCertPack(record.value.id, notAfter.value);
      const blob = res?.data || res; // isReturnNativeResponse 时 res 含 .data
      const url = window.URL.createObjectURL(blob as Blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `client_cert_${record.value.id}.zip`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      createMessage.success(t(`${tNs}.downloadPack.success`));
      emit('success');
      closeModal();
    } catch (e: any) {
      createMessage.error(e?.message || t(`${tNs}.downloadPack.failed`));
    } finally {
      submitting.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .ca-info {
    background: #fafbfc;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    padding: 8px 12px;

    .ca-name {
      font-weight: 600;
      color: #1f2933;
    }

    .ca-meta {
      font-size: 12px;
      color: #888;
      font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
      margin-top: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
</style>
