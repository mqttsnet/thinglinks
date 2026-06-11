<template>
  <div class="step-card" :class="statusClass">
    <div class="step-head" @click="expanded = !expanded">
      <div class="step-no">{{ index }}</div>
      <div class="step-title">
        <div class="step-name">{{ step.name }}</div>
        <div v-if="step.reason" class="step-reason">{{ step.reason }}</div>
      </div>
      <a-tag :color="tagColor">{{ statusLabel }}</a-tag>
      <span class="step-cost" v-if="step.costMs != null">{{ step.costMs }} ms</span>
      <DownOutlined :rotate="expanded ? 180 : 0" class="step-toggle" />
    </div>

    <div v-show="expanded && detailEntries.length" class="step-detail">
      <div v-for="[k, v] in detailEntries" :key="k" class="detail-row">
        <span class="detail-k">{{ k }}</span>
        <span class="detail-v">{{ formatVal(v) }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import type { DeviceSslTestStepVO } from '/@/api/iot/link/device/model/sslTesterModel';

  const props = defineProps<{
    index: number;
    step: DeviceSslTestStepVO;
  }>();

  const { t } = useI18n();
  const expanded = ref(props.step.status !== 'PASS');

  const statusClass = computed(() => `is-${props.step.status.toLowerCase()}`);
  const tagColor = computed(() =>
    props.step.status === 'PASS' ? 'success' : props.step.status === 'FAIL' ? 'error' : 'default',
  );
  const statusLabel = computed(() => {
    if (props.step.status === 'PASS') return t('iot.link.operationMaintenance.cacert.sslTester.step.pass');
    if (props.step.status === 'FAIL') return t('iot.link.operationMaintenance.cacert.sslTester.step.fail');
    return t('iot.link.operationMaintenance.cacert.sslTester.step.skip');
  });

  const detailEntries = computed(() =>
    props.step.detail ? Object.entries(props.step.detail) : [],
  );

  function formatVal(v: any): string {
    if (v == null) return '-';
    if (typeof v === 'object') return JSON.stringify(v);
    return String(v);
  }
</script>

<style lang="less" scoped>
  .step-card {
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    margin-bottom: 10px;
    background: #fff;
    transition: border-color 0.2s;

    &.is-pass { border-left: 4px solid #52c41a; }
    &.is-fail { border-left: 4px solid #ff4d4f; }
    &.is-skip { border-left: 4px solid #d9d9d9; opacity: 0.7; }
  }

  .step-head {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px;
    cursor: pointer;
    user-select: none;
  }

  .step-no {
    flex: 0 0 28px;
    height: 28px;
    border-radius: 50%;
    background: #f3f4f6;
    color: #6b7280;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
  }

  .step-title {
    flex: 1;
    min-width: 0;
  }

  .step-name {
    font-weight: 500;
    color: #1f2937;
  }

  .step-reason {
    margin-top: 2px;
    font-size: 12px;
    color: #ef4444;
  }

  .step-cost {
    font-size: 12px;
    color: #9ca3af;
  }

  .step-toggle {
    color: #9ca3af;
    transition: transform 0.2s;
  }

  .step-detail {
    padding: 0 14px 12px 50px;
    border-top: 1px dashed #f3f4f6;
    font-size: 12px;
  }

  .detail-row {
    display: flex;
    padding: 4px 0;
    gap: 12px;
  }

  .detail-k {
    flex: 0 0 140px;
    color: #6b7280;
  }

  .detail-v {
    flex: 1;
    color: #1f2937;
    word-break: break-all;
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  }
</style>
