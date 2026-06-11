<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t('iot.rule.integration.bridge.action.test')"
    :maskClosable="false"
    :width="800"
    @ok="handleTest"
  >
    <a-form layout="vertical">
      <a-form-item :label="t('iot.rule.integration.bridge.testSink.sampleLabel')">
        <a-textarea
          v-model:value="sampleJson"
          :rows="10"
          :placeholder="t('iot.rule.integration.bridge.testSink.samplePlaceholder')"
        />
      </a-form-item>
    </a-form>

    <a-divider v-if="result">{{ t('iot.rule.integration.bridge.testSink.result') }}</a-divider>
    <a-result
      v-if="result?.success"
      status="success"
      :title="t('iot.rule.integration.bridge.tips.testSinkSuccess')"
      :sub-title="
        `messageId: ${result.messageId ?? '-'} · ${t('iot.rule.integration.bridge.testSink.latency')} ${result.latencyMs} ms`
      "
    >
      <template #extra>
        <a-descriptions size="small" :column="1" bordered>
          <a-descriptions-item label="messageId">{{ result.messageId ?? '-' }}</a-descriptions-item>
          <a-descriptions-item :label="t('iot.rule.integration.bridge.testSink.latency')">
            {{ result.latencyMs }} ms
          </a-descriptions-item>
          <a-descriptions-item label="attributes">
            <pre style="margin: 0">{{ JSON.stringify(result.attributes ?? {}, null, 2) }}</pre>
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-result>
    <a-result
      v-else-if="result"
      status="error"
      :title="t('iot.rule.integration.bridge.tips.testSinkFailed')"
      :sub-title="result.errorMessage ?? t('iot.rule.integration.bridge.testSink.unknownError')"
    >
      <template #extra>
        <div v-if="result.errorCode">errorCode: {{ result.errorCode }}</div>
        <div>{{ t('iot.rule.integration.bridge.testSink.latency') }}: {{ result.latencyMs }} ms</div>
      </template>
    </a-result>

    <template #footer>
      <a-button @click="handleCancel">{{ t('common.closeText') }}</a-button>
      <a-button type="primary" :loading="loading" @click="handleTest">
        <template #icon><Icon icon="ant-design:thunderbolt-outlined" /></template>
        {{ t('iot.rule.integration.bridge.action.test') }}
      </a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';
  import { testSink } from '/@/api/iot/rule/integration/dataBridge';
  import {
    DataBridgeResultVO,
    TestSinkResult,
  } from '/@/api/iot/rule/integration/model/dataBridgeModel';

  defineOptions({ name: '测试发送' });

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const ruleId = ref<string>('');
  const sampleJson = ref<string>(
    `{\n  "tenantId": "test",\n  "productIdentification": "productA",\n  "deviceIdentification": "device-001",\n  "actionType": "PUBLISH",\n  "payload": "{\\"temperature\\":25.5}",\n  "ts": ${Date.now()}\n}`,
  );
  const loading = ref(false);
  const result = ref<TestSinkResult | null>(null);

  const [registerModel, { closeModal }] = useModalInner((data: { record?: DataBridgeResultVO }) => {
    ruleId.value = data?.record?.id ?? '';
    result.value = null;
  });

  async function handleTest() {
    if (!ruleId.value) {
      createMessage.error('规则 ID 缺失');
      return;
    }
    let envelope: any;
    try {
      envelope = JSON.parse(sampleJson.value || '{}');
    } catch (e: any) {
      createMessage.error('样例 envelope JSON 格式错误：' + e.message);
      return;
    }
    try {
      loading.value = true;
      const r = await testSink(ruleId.value, envelope);
      result.value = r;
      // 显式弹 toast,避免用户没注意 a-result 误判"没报错 = 成功"
      if (r?.success) {
        createMessage.success(t('iot.rule.integration.bridge.tips.testSinkSuccess'));
      } else {
        createMessage.error(
          (t('iot.rule.integration.bridge.tips.testSinkFailed') as string) +
            (r?.errorMessage ? '：' + r.errorMessage : ''),
        );
      }
    } catch (e: any) {
      result.value = {
        success: false,
        latencyMs: 0,
        errorMessage: e?.message ?? '未知错误',
      };
      createMessage.error(
        (t('iot.rule.integration.bridge.tips.testSinkFailed') as string) +
          '：' + (e?.message ?? '未知错误'),
      );
    } finally {
      loading.value = false;
    }
  }

  function handleCancel() {
    closeModal();
  }
</script>
