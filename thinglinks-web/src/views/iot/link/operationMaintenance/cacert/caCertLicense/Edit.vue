<template>
  <PageWrapper contentFullHeight class="ca-edit-page">
    <Card :bordered="false" class="edit-card" :loading="loading">
      <div class="edit-header">
        <div class="title-block">
          <div class="edit-title">{{ t(`${tNs}.edit.title`) }}</div>
          <div class="edit-subtitle">{{ t(`${tNs}.edit.tip`) }}</div>
        </div>
        <Space>
          <Button @click="goBack">{{ t(`${tNs}.edit.backList`) }}</Button>
          <Button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ t('common.title.save') }}
          </Button>
        </Space>
      </div>

      <Alert type="info" show-icon :message="t(`${tNs}.edit.tip`)" class="edit-tip" />

      <Descriptions
        v-if="record?.id"
        bordered
        :column="{ xs: 1, md: 3 }"
        size="small"
        class="readonly-info"
      >
        <Descriptions.Item :label="t(`${tNs}.serialNumber`)">
          <span class="mono-text">{{ record.serialNumber || '-' }}</span>
        </Descriptions.Item>
        <Descriptions.Item :label="t(`${tNs}.commonName`)">
          {{ record.commonName || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="t(`${tNs}.algorithm`)">
          <Tag color="blue">
            {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, record.algorithm, '-') }}
          </Tag>
        </Descriptions.Item>
      </Descriptions>

      <BasicForm @register="registerForm" />
    </Card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Alert, Button, Card, Descriptions, Space, Tag } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useDict } from '/@/components/Dict';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { DictEnum } from '/@/enums/commonEnum';
  import { detail, update } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import type { CaCertLicenseResultVO } from '/@/api/iot/link/operationMaintenance/cacert/model/caCertLicenseModel';
  import { editMetadataSchema } from './caCertLicense.data';

  defineOptions({ name: '编辑CA许可证证书' });

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const router = useRouter();
  const { currentRoute } = router;

  const loading = ref(false);
  const submitLoading = ref(false);
  const record = ref<CaCertLicenseResultVO | null>(null);

  const routeId = computed(() => {
    const { params, query } = currentRoute.value;
    return String(params?.id || query?.id || query?.recordId || '');
  });

  const listPath = computed(() => {
    const path = currentRoute.value.path;
    return path.replace(/\/edit(?:\/.*)?$/i, '') || '/';
  });

  const [registerForm, { setFieldsValue, resetFields, validate, resetSchema }] = useForm({
    name: 'CaCertLicenseEditPage',
    labelWidth: 100,
    schemas: editMetadataSchema(),
    showActionButtonGroup: false,
    baseColProps: { span: 16 },
  });

  onMounted(load);

  async function load() {
    await resetSchema(editMetadataSchema());
    await resetFields();

    if (!routeId.value) {
      createMessage.warning(t(`${tNs}.edit.noId`));
      goBack();
      return;
    }

    loading.value = true;
    try {
      const res = await detail(routeId.value);
      record.value = res || null;
      await setFieldsValue({
        id: res?.id || routeId.value,
        algorithm: res?.algorithm,
        state: res?.state,
        certName: res?.certName,
        remark: res?.remark,
      });
    } catch (e: any) {
      createMessage.error(e?.message || t(`${tNs}.detail.loadFailed`));
    } finally {
      loading.value = false;
    }
  }

  async function handleSubmit() {
    if (!routeId.value) {
      createMessage.warning(t(`${tNs}.edit.noId`));
      return;
    }

    try {
      const params = await validate();
      submitLoading.value = true;
      await update({
        ...params,
        id: params.id || routeId.value,
      });
      createMessage.success(t(`${tNs}.edit.success`));
      goBack();
    } catch (e: any) {
      if (e?.errorFields) return;
      createMessage.error(e?.message || t(`${tNs}.edit.failed`));
    } finally {
      submitLoading.value = false;
    }
  }

  function goBack() {
    router.replace({ path: listPath.value });
  }
</script>

<style lang="less" scoped>
  .ca-edit-page {
    @media (max-width: 768px) {
      .edit-header {
        flex-direction: column;
      }
    }

    .edit-card {
      border-radius: 8px;
      box-shadow: 0 8px 24px rgb(149 157 165 / 12%);
    }

    .edit-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 16px;
    }

    .title-block {
      min-width: 0;
    }

    .edit-title {
      color: #2a3547;
      font-size: 18px;
      font-weight: 700;
      line-height: 1.35;
    }

    .edit-subtitle {
      margin-top: 6px;
      color: #5a6a85;
      font-size: 13px;
      line-height: 1.5;
    }

    .edit-tip,
    .readonly-info {
      margin-bottom: 16px;
    }

    .mono-text {
      color: #2a3547;
      font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
      word-break: break-all;
    }
  }
</style>
