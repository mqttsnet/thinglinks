<template>
  <PageWrapper :title="t('iot.link.operationMaintenance.cacert.sslTester.title')" content-background>
    <template #headerContent>
      <div class="text-sm text-gray-500">{{ t('iot.link.operationMaintenance.cacert.sslTester.subtitle') }}</div>
    </template>

    <a-row :gutter="16">
      <!-- 左侧 输入表单 -->
      <a-col :xs="24" :md="10" :xl="8">
        <a-card :title="t('iot.link.operationMaintenance.cacert.sslTester.inputTitle')" size="small">
          <a-form layout="vertical" :model="form">
            <a-form-item :label="t('iot.link.operationMaintenance.cacert.sslTester.clientId')">
              <a-input
                v-model:value="form.clientIdentifier"
                :placeholder="t('iot.link.operationMaintenance.cacert.sslTester.clientIdPh')"
                allow-clear />
            </a-form-item>

            <a-form-item :label="t('iot.link.operationMaintenance.cacert.sslTester.targetCa')">
              <ApiSelect
                v-model:value="form.caSerialNumber"
                :api="loadCaOptions"
                :placeholder="t('iot.link.operationMaintenance.cacert.sslTester.targetCaPh')"
                allow-clear
                :params="{ size: 200, current: 1 }" />
              <div class="text-xs text-gray-400 mt-1">
                {{ t('iot.link.operationMaintenance.cacert.sslTester.targetCaHint') }}
              </div>
            </a-form-item>

            <a-form-item
              :label="t('iot.link.operationMaintenance.cacert.sslTester.clientCert')"
              :required="true">
              <a-textarea
                v-model:value="form.clientCertBase64"
                :rows="10"
                :placeholder="t('iot.link.operationMaintenance.cacert.sslTester.clientCertPh')" />
              <div class="text-xs text-gray-400 mt-1 flex items-center gap-2">
                <a-upload
                  :before-upload="handleFileUpload"
                  :show-upload-list="false"
                  accept=".pem,.crt,.cer,.txt">
                  <a-button size="small">{{ t('iot.link.operationMaintenance.cacert.sslTester.uploadFile') }}</a-button>
                </a-upload>
                <span>{{ t('iot.link.operationMaintenance.cacert.sslTester.uploadHint') }}</span>
              </div>
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                :loading="testing"
                :disabled="!form.clientCertBase64"
                block
                @click="handleTest">
                <template #icon><ExperimentOutlined /></template>
                {{ t('iot.link.operationMaintenance.cacert.sslTester.startTest') }}
              </a-button>
            </a-form-item>

            <a-form-item v-if="result">
              <a-button block @click="handleReset">
                {{ t('iot.link.operationMaintenance.cacert.sslTester.reset') }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右侧 测试结果 -->
      <a-col :xs="24" :md="14" :xl="16">
        <a-card :title="t('iot.link.operationMaintenance.cacert.sslTester.resultTitle')" size="small">
          <!-- 空态 -->
          <a-empty
            v-if="!result && !testing"
            :description="t('iot.link.operationMaintenance.cacert.sslTester.emptyHint')" />

          <a-spin v-else-if="testing">
            <div class="py-10 text-center text-gray-500">
              {{ t('iot.link.operationMaintenance.cacert.sslTester.testing') }}
            </div>
          </a-spin>

          <template v-else-if="result">
            <!-- summary 顶部 -->
            <a-alert
              :type="result.success ? 'success' : 'error'"
              :message="resultSummaryLabel"
              :description="result.summary"
              show-icon
              class="mb-4" />

            <div class="text-xs text-gray-400 mb-3">
              {{ t('iot.link.operationMaintenance.cacert.sslTester.totalCost', { ms: result.totalCostMs || 0 }) }}
            </div>

            <!-- 分步卡片 -->
            <StepResultCard
              v-for="(step, idx) in result.steps"
              :key="step.step"
              :index="idx + 1"
              :step="step" />
          </template>
        </a-card>
      </a-col>
    </a-row>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { ExperimentOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { ApiSelect } from '/@/components/Form';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { sslTest } from '/@/api/iot/link/device/sslTester';
  import { page as caPage } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import type {
    DeviceSslTestQuery,
    DeviceSslTestResultVO,
  } from '/@/api/iot/link/device/model/sslTesterModel';
  import StepResultCard from './components/StepResultCard.vue';

  defineOptions({ name: 'CaCertSslTester' });

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const form = ref<DeviceSslTestQuery>({
    clientCertBase64: '',
    caSerialNumber: undefined,
    clientIdentifier: undefined,
  });
  const testing = ref(false);
  const result = ref<DeviceSslTestResultVO | null>(null);

  const resultSummaryLabel = computed(() =>
    result.value?.success
      ? t('iot.link.operationMaintenance.cacert.sslTester.testPass')
      : t('iot.link.operationMaintenance.cacert.sslTester.testFail'),
  );

  /** 拉已颁发(state=1)的 CA 作为下拉数据源 */
  async function loadCaOptions(params: any) {
    const res = await caPage({ model: { state: 1 } as any, ...params });
    return (res?.records || []).map((c: any) => ({
      label: `${c.certName} · ${c.serialNumber?.substring(0, 12)}…`,
      value: c.serialNumber,
    }));
  }

  /** 上传 PEM / crt 文件 → 自动 base64 化 */
  function handleFileUpload(file: File): boolean {
    const reader = new FileReader();
    reader.onload = () => {
      const text = String(reader.result || '');
      // 如果是 PEM 文本(含 BEGIN CERTIFICATE),抽出中间 base64;否则直接当 base64 用
      const m = /-----BEGIN CERTIFICATE-----([\s\S]*?)-----END CERTIFICATE-----/.exec(text);
      form.value.clientCertBase64 = (m ? m[1] : text).replace(/\s+/g, '');
    };
    reader.readAsText(file);
    return false; // 阻止自动上传
  }

  async function handleTest() {
    if (!form.value.clientCertBase64?.trim()) {
      createMessage.warning(t('iot.link.operationMaintenance.cacert.sslTester.needClientCert'));
      return;
    }
    testing.value = true;
    result.value = null;
    try {
      result.value = await sslTest({
        clientCertBase64: form.value.clientCertBase64.trim(),
        caSerialNumber: form.value.caSerialNumber || undefined,
        clientIdentifier: form.value.clientIdentifier || undefined,
      });
    } catch (e: any) {
      createMessage.error(e?.message || t('iot.link.operationMaintenance.cacert.sslTester.testException'));
    } finally {
      testing.value = false;
    }
  }

  function handleReset() {
    result.value = null;
    form.value = { clientCertBase64: '', caSerialNumber: undefined, clientIdentifier: undefined };
  }
</script>
