<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('component.aclTopicMatcherTester.title')"
    :width="900"
    :keyboard="true"
    :maskClosable="false"
    :showOkBtn="false"
  >
    <div class="amt-shell">
      <!-- ======================== 规则总览 ======================== -->
      <div class="amt-card amt-rule-card">
        <div class="amt-card-head">
          <SafetyOutlined />
          <span>{{ t('component.aclTopicMatcherTester.section.rule') }}</span>
        </div>
        <div class="amt-card-body">
          <div class="amt-rule-meta">
            <a-tag v-if="rule?.ruleName" color="blue" class="amt-rule-name">{{ rule.ruleName }}</a-tag>
            <a-tag v-if="rule?.priority != null" color="purple">
              {{ t('component.aclTopicMatcherTester.field.priority') }}: {{ rule.priority }}
            </a-tag>
            <a-tag v-if="rule?.decision != null" :color="rule.decision ? 'green' : 'red'">
              <component :is="rule.decision ? CheckCircleFilled : CloseCircleFilled" />
              {{ rule.decision
                ? t('component.aclTopicMatcherTester.field.decisionAllow')
                : t('component.aclTopicMatcherTester.field.decisionDeny') }}
            </a-tag>
          </div>
          <div class="amt-pattern-line">
            <span class="amt-line-label">{{ t('component.aclTopicMatcherTester.field.topicPattern') }}:</span>
            <code v-if="rule?.topicPattern" class="amt-pattern-code">{{ rule.topicPattern }}</code>
            <span v-else class="amt-empty-hint">{{ t('component.aclTopicMatcherTester.emptyPattern') }}</span>
          </div>
        </div>
      </div>

      <!-- ======================== 真实场景:产品 + 设备 ======================== -->
      <div class="amt-card">
        <div class="amt-card-head">
          <ApiOutlined />
          <span>{{ t('component.aclTopicMatcherTester.section.realScenario') }}</span>
          <span class="amt-card-hint">{{ t('component.aclTopicMatcherTester.realScenarioHint') }}</span>
        </div>
        <div class="amt-card-body">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="amt-pick-row">
                <span class="amt-pick-label">{{ t('component.aclTopicMatcherTester.field.product') }}</span>
                <IotProductPicker
                  v-model="selectedProductIdent"
                  :title="t('component.aclTopicMatcherTester.dialog.pickProduct')"
                  @change="onProductChange"
                />
              </div>
            </a-col>
            <a-col :span="12">
              <div class="amt-pick-row">
                <span class="amt-pick-label">{{ t('component.aclTopicMatcherTester.field.device') }}</span>
                <IotDevicePicker
                  v-model="selectedDeviceIdent"
                  :productIdentification="selectedProductIdent"
                  :title="t('component.aclTopicMatcherTester.dialog.pickDevice')"
                  @change="onDeviceChange"
                />
              </div>
            </a-col>
          </a-row>

          <!-- 占位符值预览(仅当规则的 pattern 含占位符) -->
          <div v-if="usedPlaceholders.length" class="amt-ph-preview">
            <div class="amt-ph-title">
              <BlockOutlined />
              {{ t('component.aclTopicMatcherTester.placeholderPreview') }}
            </div>
            <div class="amt-ph-grid">
              <div v-for="ph in usedPlaceholders" :key="ph" class="amt-ph-item">
                <code class="amt-ph-key">{{ '${' + ph + '}' }}</code>
                <span class="amt-ph-arrow">→</span>
                <a-input
                  v-model:value="placeholderValues[ph]"
                  size="small"
                  :placeholder="t('component.aclTopicMatcherTester.placeholderInput')"
                  allow-clear
                />
              </div>
            </div>
            <div class="amt-resolved-row">
              <span class="amt-resolved-label">
                {{ t('component.aclTopicMatcherTester.field.resolvedPattern') }}
              </span>
              <code class="amt-resolved-value">{{ resolvedPattern || '-' }}</code>
            </div>
          </div>
        </div>
      </div>

      <!-- ======================== 测试输入 ======================== -->
      <div class="amt-card">
        <div class="amt-card-head">
          <ExperimentOutlined />
          <span>{{ t('component.aclTopicMatcherTester.section.input') }}</span>
        </div>
        <div class="amt-card-body">
          <a-input
            v-model:value="testTopicInput"
            :placeholder="t('component.aclTopicMatcherTester.field.testTopicPlaceholder')"
            allow-clear
            @keydown.enter.prevent
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </div>
      </div>

      <!-- ======================== 决策结果(核心输出) ======================== -->
      <div class="amt-result" :class="resultThemeClass" v-if="testTopicInput">
        <div class="amt-result-icon">
          <component :is="resultIcon" />
        </div>
        <div class="amt-result-content">
          <div class="amt-result-title">{{ resultTitle }}</div>
          <div class="amt-result-detail">{{ resultDetail }}</div>
        </div>
        <div class="amt-result-decision">
          <a-tag v-if="result.decision === true" color="green" class="amt-decision-tag">
            <CheckCircleFilled />
            {{ t('component.aclTopicMatcherTester.field.decisionAllow') }}
          </a-tag>
          <a-tag v-else-if="result.decision === false" color="red" class="amt-decision-tag">
            <CloseCircleFilled />
            {{ t('component.aclTopicMatcherTester.field.decisionDeny') }}
          </a-tag>
        </div>
      </div>

      <!-- ======================== 层级 diff 可视化 ======================== -->
      <div v-if="testTopicInput && !result.topicError && resolvedPattern" class="amt-card">
        <div class="amt-card-head">
          <NodeIndexOutlined />
          <span>{{ t('component.aclTopicMatcherTester.section.diff') }}</span>
        </div>
        <div class="amt-card-body">
          <div class="amt-diff-row">
            <span class="amt-diff-label">{{ t('component.aclTopicMatcherTester.diff.pattern') }}</span>
            <div class="amt-diff-chips">
              <template v-for="(lv, idx) in result.levelDiff" :key="`p-${idx}`">
                <span class="amt-slash" v-if="idx > 0">/</span>
                <span
                  class="amt-chip"
                  :class="lvChipClass(lv, 'pattern')"
                  :title="lvNote(lv)"
                >{{ lv.pattern || '∅' }}</span>
              </template>
            </div>
          </div>
          <div class="amt-diff-row">
            <span class="amt-diff-label">{{ t('component.aclTopicMatcherTester.diff.topic') }}</span>
            <div class="amt-diff-chips">
              <template v-for="(lv, idx) in result.levelDiff" :key="`t-${idx}`">
                <span class="amt-slash" v-if="idx > 0">/</span>
                <span
                  class="amt-chip"
                  :class="lvChipClass(lv, 'topic')"
                  :title="lvNote(lv)"
                >{{ lv.topic || '∅' }}</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import {
    SafetyOutlined,
    ApiOutlined,
    BlockOutlined,
    ExperimentOutlined,
    NodeIndexOutlined,
    SearchOutlined,
    CheckCircleFilled,
    CloseCircleFilled,
    InfoCircleFilled,
    WarningFilled,
  } from '@ant-design/icons-vue';
  import {
    IotProductPicker,
    IotDevicePicker,
  } from '/@/components/iot/IotProductDevicePicker';
  import { query as queryProducts } from '/@/api/iot/link/product/product';
  import { page as devicePage } from '/@/api/iot/link/device/device';
  import {
    detectUsedPlaceholders,
    replacePlaceholders,
    testAclMatch,
  } from './matcher';
  import type {
    AclPlaceholderKey,
    AclPlaceholderValues,
    AclTestResult,
    AclLevelDiff,
    TestableAclRule,
  } from './types';

  defineOptions({ name: 'AclTopicMatcherTesterModal' });

  const { t } = useI18n();

  // ============================== Modal 入参 ==============================
  const rule = ref<TestableAclRule | null>(null);

  // ============================== 真实场景:产品 / 设备 ==============================
  const selectedProductIdent = ref<string>('');
  const selectedDeviceIdent = ref<string>('');

  /** 占位符值(初始空,选完真实产品/设备后自动填充) */
  const placeholderValues = reactive<AclPlaceholderValues>({
    app_id: '',
    user_name: '',
    device_identification: '',
    product_identification: '',
    device_sdk_version: '',
  });

  // ============================== 测试输入 ==============================
  const testTopicInput = ref<string>('');

  // ============================== 选择联动(占位符自动反查) ==============================
  // 产品/设备分页 + 卡片样式 全部由 IotProductPicker / IotDevicePicker 包装组件接管;
  // 这里只关心"选完之后用对应实体回填 5 个占位符"的业务联动。

  /** 选完产品 ── 反查产品拿 appId 填占位符;清空设备相关字段(避免跨产品脏数据) */
  async function onProductChange(productIdent: string | string[] | null) {
    const ident = Array.isArray(productIdent) ? productIdent[0] : productIdent;
    placeholderValues.product_identification = String(ident ?? '');
    selectedDeviceIdent.value = '';
    placeholderValues.device_identification = '';
    placeholderValues.user_name = '';
    placeholderValues.device_sdk_version = '';

    if (!ident) {
      placeholderValues.app_id = '';
      return;
    }
    try {
      const product: any = await queryProducts({ productIdentification: String(ident) } as any)
        .then((res: any) => (Array.isArray(res) ? res[0] : res))
        .catch(() => null);
      if (product) {
        placeholderValues.app_id = String(product.appId ?? '');
      }
    } catch {
      // 反查失败保持当前值
    }
  }

  /** 选完设备 ── 反查设备拿 userName / deviceSdkVersion / productIdentification 填占位符 */
  async function onDeviceChange(deviceIdent: string | string[] | null) {
    const ident = Array.isArray(deviceIdent) ? deviceIdent[0] : deviceIdent;
    placeholderValues.device_identification = String(ident ?? '');

    if (!ident) {
      placeholderValues.user_name = '';
      placeholderValues.device_sdk_version = '';
      return;
    }
    try {
      const res: any = await devicePage({
        model: { deviceIdentification: String(ident) } as any,
        size: 1,
        current: 1,
      } as any);
      const device = res?.records?.[0];
      if (device) {
        placeholderValues.user_name = String(device.userName ?? '');
        placeholderValues.device_sdk_version = String(device.deviceSdkVersion ?? '');
        // product_identification 以 device 的为准(用户可能在跨产品场景下手动选)
        if (device.productIdentification) {
          placeholderValues.product_identification = String(device.productIdentification);
        }
      }
    } catch {
      // 反查失败保持当前值
    }
  }

  // ============================== Modal 注册 ==============================
  const [registerModal] = useModalInner((data) => {
    rule.value = data?.rule ?? null;

    // 重置选择状态
    selectedProductIdent.value = '';
    selectedDeviceIdent.value = '';
    testTopicInput.value = '';
    Object.keys(placeholderValues).forEach((k) => {
      placeholderValues[k as AclPlaceholderKey] = '';
    });

    // 父组件可预填:已选产品/设备(基于规则的 productIdentification / deviceIdentification)
    if (data?.presetProductIdentification) {
      selectedProductIdent.value = String(data.presetProductIdentification);
      onProductChange(data.presetProductIdentification);
    }
    if (data?.presetDeviceIdentification) {
      selectedDeviceIdent.value = String(data.presetDeviceIdentification);
      onDeviceChange(data.presetDeviceIdentification);
    }
  });

  // ============================== 派生状态 ==============================
  const usedPlaceholders = computed<AclPlaceholderKey[]>(() => {
    return detectUsedPlaceholders(rule.value?.topicPattern || '');
  });

  const resolvedPattern = computed<string>(() => {
    return replacePlaceholders(rule.value?.topicPattern || '', placeholderValues);
  });

  /** 完整测试结果 */
  const result = computed<AclTestResult>(() => {
    if (!rule.value || !rule.value.topicPattern || !testTopicInput.value) {
      return {
        matchedRule: null,
        decision: null,
        resolvedPattern: resolvedPattern.value,
        levelDiff: [],
      };
    }
    return testAclMatch({
      rules: [rule.value],
      testTopic: testTopicInput.value,
      placeholderValues,
    });
  });

  // ============================== 决策结果 UI ──── 4 态:命中允许 / 命中拒绝 / 未命中 / 校验出错 ==============================
  const resultThemeClass = computed(() => {
    if (result.value.topicError || result.value.patternError) return 'amt-result-warn';
    if (result.value.matchedRule) {
      return result.value.decision === true ? 'amt-result-allow' : 'amt-result-deny';
    }
    return 'amt-result-info';
  });

  const resultIcon = computed(() => {
    if (result.value.topicError || result.value.patternError) return WarningFilled;
    if (result.value.matchedRule) {
      return result.value.decision === true ? CheckCircleFilled : CloseCircleFilled;
    }
    return InfoCircleFilled;
  });

  const resultTitle = computed(() => {
    if (result.value.topicError) {
      return t('component.aclTopicMatcherTester.result.topicError');
    }
    if (result.value.patternError) {
      return t('component.aclTopicMatcherTester.result.patternError');
    }
    if (result.value.matchedRule) {
      return result.value.decision === true
        ? t('component.aclTopicMatcherTester.result.matchedAllow')
        : t('component.aclTopicMatcherTester.result.matchedDeny');
    }
    return t('component.aclTopicMatcherTester.result.notMatched');
  });

  const resultDetail = computed(() => {
    if (result.value.topicError) {
      return t(`component.aclTopicMatcherTester.error.${result.value.topicError}`);
    }
    if (result.value.patternError) {
      return t(`component.aclTopicMatcherTester.error.${result.value.patternError}`);
    }
    if (result.value.matchedRule) {
      return t('component.aclTopicMatcherTester.result.matchedDetail', {
        ruleName: result.value.matchedRule.ruleName ?? '-',
        pattern: result.value.resolvedPattern,
      });
    }
    return t('component.aclTopicMatcherTester.result.notMatchedDetail');
  });

  // ============================== UI 助手 ==============================
  function lvChipClass(lv: AclLevelDiff, side: 'pattern' | 'topic') {
    return {
      'amt-chip-match': lv.matched,
      'amt-chip-miss': !lv.matched,
      'amt-chip-wildcard':
        side === 'pattern' && (lv.note === 'single-wildcard' || lv.note === 'multi-wildcard'),
    };
  }

  function lvNote(lv: AclLevelDiff): string {
    return t(`component.aclTopicMatcherTester.note.${camelCase(lv.note)}`);
  }

  function camelCase(s: string): string {
    return s.replace(/-([a-z])/g, (_m, c) => c.toUpperCase());
  }

  // ============================== Watch ==============================
  // 切 rule 时清空旧测试(防止误判)
  watch(
    () => rule.value?.topicPattern,
    () => {
      testTopicInput.value = '';
    },
  );
</script>

<style lang="less" scoped>
  .amt-shell {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  // ============================== 卡片 ==============================
  .amt-card {
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    background: #fff;
    overflow: hidden;
  }

  .amt-card-head {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    background: #fafbfc;
    border-bottom: 1px solid #f0f0f0;
    font-size: 13px;
    font-weight: 600;
    color: #1f2937;

    :deep(.anticon) {
      color: @primary-color;
      font-size: 14px;
    }

    .amt-card-hint {
      margin-left: auto;
      font-weight: 400;
      font-size: 12px;
      color: #8c8c8c;
    }
  }

  .amt-card-body {
    padding: 12px 14px;
  }

  // ============================== 规则总览 ==============================
  .amt-rule-card {
    border-color: #d6e4ff;
    background: linear-gradient(180deg, #f6faff 0%, #ffffff 60%);
  }

  .amt-rule-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;

    .amt-rule-name {
      font-weight: 600;
    }

    :deep(.ant-tag) {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      margin: 0;
    }
  }

  .amt-pattern-line {
    display: flex;
    align-items: baseline;
    gap: 8px;
    font-size: 13px;
    flex-wrap: wrap;

    .amt-line-label {
      flex-shrink: 0;
      white-space: nowrap;
      color: #8c8c8c;
    }
  }

  .amt-pattern-code {
    display: inline-block;
    padding: 4px 10px;
    background: #f0f7ff;
    color: @primary-color;
    border-radius: 4px;
    font-family: 'JetBrains Mono', Menlo, Consolas, 'Courier New', monospace;
    font-size: 13px;
    word-break: break-all;
  }

  .amt-empty-hint {
    color: #bfbfbf;
    font-style: italic;
  }

  // ============================== 真实场景 picker ==============================
  .amt-pick-row {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .amt-pick-label {
    font-size: 12px;
    color: #595959;
    white-space: nowrap;
  }

  // ============================== 占位符预览 ==============================
  .amt-ph-preview {
    margin-top: 12px;
    padding: 10px 12px;
    background: #fafbfc;
    border-radius: 6px;
    border: 1px dashed #e1e6ed;
  }

  .amt-ph-title {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    font-size: 12px;
    color: #595959;
    font-weight: 600;

    :deep(.anticon) {
      color: @primary-color;
    }
  }

  .amt-ph-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 6px 12px;
  }

  .amt-ph-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .amt-ph-key {
      flex-shrink: 0;
      width: 168px;
      padding: 2px 6px;
      background: #fff;
      color: #6b7280;
      border: 1px solid #e6e8eb;
      border-radius: 3px;
      font-family: monospace;
      font-size: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .amt-ph-arrow {
      color: #8c8c8c;
      font-size: 11px;
    }
  }

  .amt-resolved-row {
    margin-top: 10px;
    padding-top: 8px;
    border-top: 1px dashed #e6e8eb;
    display: flex;
    align-items: baseline;
    gap: 6px;
    font-size: 12px;
  }

  .amt-resolved-label {
    color: #8c8c8c;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .amt-resolved-value {
    flex: 1;
    color: @primary-color;
    background: #f0f7ff;
    padding: 2px 8px;
    border-radius: 3px;
    font-family: monospace;
    word-break: break-all;
  }

  // ============================== 决策结果 banner(核心)==============================
  .amt-result {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 18px;
    border-radius: 10px;
    border: 1px solid;
    background: #fff;
    transition: all 0.18s ease;

    .amt-result-icon {
      flex-shrink: 0;
      font-size: 32px;
      display: flex;
      align-items: center;
    }

    .amt-result-content {
      flex: 1;
      min-width: 0;
    }

    .amt-result-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 4px;
    }

    .amt-result-detail {
      font-size: 13px;
      color: #595959;
      word-break: break-all;
    }

    .amt-result-decision {
      flex-shrink: 0;
    }

    .amt-decision-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      padding: 4px 12px;
      margin: 0;
    }

    &.amt-result-allow {
      background: #f6ffed;
      border-color: #b7eb8f;
      .amt-result-icon { color: #52c41a; }
      .amt-result-title { color: #389e0d; }
    }

    &.amt-result-deny {
      background: #fff1f0;
      border-color: #ffa39e;
      .amt-result-icon { color: #ff4d4f; }
      .amt-result-title { color: #cf1322; }
    }

    &.amt-result-info {
      background: #f6f8fb;
      border-color: #e1e6ed;
      .amt-result-icon { color: #8c8c8c; }
      .amt-result-title { color: #595959; }
    }

    &.amt-result-warn {
      background: #fff7e6;
      border-color: #ffd591;
      .amt-result-icon { color: #fa8c16; }
      .amt-result-title { color: #d46b08; }
    }
  }

  // ============================== 层级 diff ==============================
  .amt-diff-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .amt-diff-label {
    flex-shrink: 0;
    min-width: 48px;
    white-space: nowrap;
    font-size: 12px;
    color: #8c8c8c;
  }

  .amt-diff-chips {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
  }

  .amt-slash {
    color: #b9bec5;
    font-weight: 700;
  }

  .amt-chip {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
    border: 1px solid transparent;
    background: #fafafa;
    color: #595959;
    cursor: default;
    transition: all 0.18s ease;

    &.amt-chip-match {
      background: #f6ffed;
      color: #389e0d;
      border-color: #b7eb8f;
    }

    &.amt-chip-miss {
      background: #fff1f0;
      color: #cf1322;
      border-color: #ffa39e;
    }

    &.amt-chip-wildcard {
      font-weight: 600;
      background: #e6f4ff;
      color: #0958d9;
      border-color: #91caff;
    }
  }
</style>
