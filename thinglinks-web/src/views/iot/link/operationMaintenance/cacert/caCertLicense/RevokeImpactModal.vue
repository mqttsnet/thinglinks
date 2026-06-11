<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t(`${tNs}.impact.title`)"
    :maskClosable="false"
    :destroyOnClose="true"
    :ok-text="t(`${tNs}.impact.confirm`)"
    :ok-button-props="{ danger: true, disabled: !record?.id }"
    :confirm-loading="submitting"
    @ok="handleConfirm"
    :width="720"
  >
    <a-spin :spinning="loading">
      <a-alert
        v-if="impact"
        type="warning"
        show-icon
        :message="
          t(`${tNs}.impact.warning`, {
            count: impact.boundDeviceCount ?? 0,
            online: impact.onlineDeviceCount ?? 0,
          })
        "
        class="impact-alert"
      />

      <!-- 关联设备摘要 (top 50) -->
      <div class="section-title">{{ t(`${tNs}.impact.boundDevices`) }}</div>
      <a-table
        :columns="deviceColumns"
        :data-source="impact?.topDevices || []"
        :pagination="false"
        size="small"
        :scroll="{ y: 280 }"
        row-key="deviceIdentification"
      >
        <template #emptyText>
          <a-empty :description="t('thinglinks.common.noData')" />
        </template>
      </a-table>

      <!-- 吊销原因 -->
      <a-form layout="vertical" class="reason-form">
        <a-form-item :label="t(`${tNs}.impact.revokeReason`)">
          <a-textarea
            v-model:value="reason"
            :placeholder="t(`${tNs}.impact.revokeReasonPh`)"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { revoke } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import { defHttp } from '/@/utils/http/axios';
  import { RequestEnum } from '/@/enums/httpEnum';
  import { ServicePrefixEnum } from '/@/enums/commonEnum';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emit = defineEmits(['success', 'register']);

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const record = ref<any>(null);
  const impact = ref<any>(null);
  const loading = ref(false);
  const submitting = ref(false);
  const reason = ref<string>('');

  const deviceColumns = [
    { title: t(`${tNs}.impact.deviceIdentification`), dataIndex: 'deviceIdentification' },
    { title: t(`${tNs}.impact.deviceName`), dataIndex: 'deviceName' },
    {
      title: t(`${tNs}.impact.online`),
      dataIndex: 'connectStatus',
      width: 90,
      customRender: ({ text }: { text: number }) =>
        text === 1
          ? t(`${tNs}.impact.connectStatusOnline`)
          : t(`${tNs}.impact.connectStatusOffline`),
    },
  ];

  const [registerModal, { closeModal }] = useModalInner(async (data: { record?: any }) => {
    record.value = data?.record ?? null;
    impact.value = null;
    reason.value = '';
    if (record.value?.id) {
      await loadImpact(record.value.id);
    }
  });

  /**
   * 加载影响面 ── 调 GET /caCertLicense/impact/{id}.
   * 单独写裸 fetch 是因为 caCertLicense.ts 里 Api.Impact 是 URL 模板,
   * 直接复用 import 的话 url 拼接逻辑不够清晰; 这里行内一调更明确.
   */
  async function loadImpact(id: number | string) {
    loading.value = true;
    try {
      impact.value = await defHttp.request({
        url: `${ServicePrefixEnum.LINK}/caCertLicense/impact/${id}`,
        method: RequestEnum.GET,
      });
    } catch (e: any) {
      createMessage.error(e?.message || t(`${tNs}.impact.loadFailed`));
    } finally {
      loading.value = false;
    }
  }

  async function handleConfirm() {
    if (!record.value?.id) return;
    submitting.value = true;
    try {
      await revoke(record.value.id, reason.value || undefined);
      createMessage.success(t(`${tNs}.impact.revokeSuccess`));
      emit('success');
      closeModal();
    } catch (e: any) {
      createMessage.error(e?.message || t(`${tNs}.impact.revokeFailed`));
    } finally {
      submitting.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .impact-alert {
    margin-bottom: 16px;
  }

  .section-title {
    font-weight: 600;
    margin: 8px 0;
    color: #333;
  }

  .reason-form {
    margin-top: 16px;
  }
</style>
