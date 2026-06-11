<template>
  <PageWrapper contentFullHeight>
    <!-- 页面标题区域 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="page-title">
            <div class="title-icon">
              <SettingOutlined />
            </div>
            <div class="title-content">
              <span class="title-text">{{ t('video.device.sipConfig.pageTitle') }}</span>
              <span class="title-desc">{{ t('video.device.sipConfig.pageDesc') }}</span>
            </div>
          </div>
        </div>
        <a-button type="primary" ghost :loading="loading" @click="loadData">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.redo') }}
        </a-button>
      </div>
    </Card>

    <!-- 接入参数卡片 -->
    <Card :bordered="false" class="config-card main-config-card">
      <template #title>
        <div class="card-title-row">
          <SafetyCertificateOutlined class="card-title-icon primary" />
          <span>{{ t('video.device.sipConfig.accessParams') }}</span>
        </div>
      </template>
      <template #extra>
        <span class="card-title-desc">{{ t('video.device.sipConfig.accessParamsDesc') }}</span>
      </template>

      <Row :gutter="[24, 0]">
        <Col :xs="24" :lg="12">
          <div class="param-list">
            <div class="param-item" v-for="item in leftParams" :key="item.key">
              <div class="param-label">
                <span class="label-text">{{ item.label }}</span>
                <a-tooltip :title="item.help" placement="top">
                  <QuestionCircleOutlined class="label-help" />
                </a-tooltip>
              </div>
              <div class="param-value">
                <div class="value-box" :class="{ 'value-secret': item.secret }">
                  <span v-if="item.secret && !passwordVisible">••••••••</span>
                  <span v-else>{{ item.value || '-' }}</span>
                  <span v-if="item.suffix" class="value-suffix">{{ item.suffix }}</span>
                </div>
                <div class="value-actions">
                  <a-tooltip v-if="item.secret" :title="passwordVisible ? t('video.device.sipConfig.hide') : t('video.device.sipConfig.show')" placement="top">
                    <EyeInvisibleOutlined
                      v-if="passwordVisible"
                      class="action-icon"
                      @click="passwordVisible = false"
                    />
                    <EyeOutlined v-else class="action-icon" @click="passwordVisible = true" />
                  </a-tooltip>
                  <CopyOutlined class="action-icon copy-icon" @click="handleCopy(item)" />
                </div>
              </div>
            </div>
          </div>
        </Col>
        <Col :xs="24" :lg="12">
          <div class="param-list">
            <div class="param-item" v-for="item in rightParams" :key="item.key">
              <div class="param-label">
                <span class="label-text">{{ item.label }}</span>
                <a-tooltip :title="item.help" placement="top">
                  <QuestionCircleOutlined class="label-help" />
                </a-tooltip>
              </div>
              <div class="param-value">
                <div class="value-box">
                  <span>{{ item.value || '-' }}</span>
                  <span v-if="item.suffix" class="value-suffix">{{ item.suffix }}</span>
                </div>
                <div class="value-actions">
                  <CopyOutlined class="action-icon copy-icon" @click="handleCopy(item)" />
                </div>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </Card>

    <Row :gutter="16" class="bottom-row">
      <!-- 配置指引 -->
      <Col :xs="24" :lg="14">
        <Card :bordered="false" class="config-card guide-card">
          <template #title>
            <div class="card-title-row">
              <ReadOutlined class="card-title-icon success" />
              <span>{{ t('video.device.sipConfig.configGuide') }}</span>
            </div>
          </template>
          <template #extra>
            <Tag color="blue">GB28181</Tag>
          </template>

          <div class="guide-desc">{{ t('video.device.sipConfig.configGuideDesc') }}</div>
          <div class="guide-steps">
            <div class="step-item" v-for="(step, index) in steps" :key="index">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-text">{{ step }}</div>
            </div>
          </div>
        </Card>
      </Col>

      <!-- 节点状态 -->
      <Col :xs="24" :lg="10">
        <Card :bordered="false" class="config-card node-card">
          <template #title>
            <div class="card-title-row">
              <CloudServerOutlined class="card-title-icon purple" />
              <span>{{ t('video.device.sipConfig.nodeStatus') }}</span>
            </div>
          </template>
          <template #extra>
            <Tag :color="hasOnlineNode ? 'success' : 'default'">
              {{ hasOnlineNode ? t('video.device.sipConfig.online') : t('video.device.sipConfig.offline') }}
            </Tag>
          </template>

          <div class="node-desc">{{ t('video.device.sipConfig.nodeStatusDesc') }}</div>

          <div v-if="sipConfig.serverNodes && sipConfig.serverNodes.length" class="node-list">
            <div class="node-item" v-for="node in sipConfig.serverNodes" :key="node.instanceId">
              <div class="node-status-dot" :class="isTruthyStatus(node.onlineStatus) ? 'online' : 'offline'" />
              <div class="node-info">
                <div class="node-instance">{{ node.instanceId }}</div>
                <div class="node-meta">
                  <span class="node-ips">{{ node.monitorIps?.join(', ') || '-' }}</span>
                  <a-divider type="vertical" />
                  <span class="node-time">{{ node.registerTime || '-' }}</span>
                </div>
              </div>
              <Tag :color="isTruthyStatus(node.onlineStatus) ? 'success' : 'error'" class="node-tag">
                {{ isTruthyStatus(node.onlineStatus) ? t('video.device.sipConfig.online') : t('video.device.sipConfig.offline') }}
              </Tag>
            </div>
          </div>

          <a-empty v-else :description="t('video.device.sipConfig.noNodes')" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        </Card>
      </Col>
    </Row>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getSipConfigInfo } from '/@/api/video/device/sipConfig';
  import type { SipConfigResultVO } from '/@/api/video/device/sipConfig';
  import { PageWrapper } from '/@/components/Page';
  import { handleCopyTextV2, isTruthyStatus } from '/@/utils/thinglinks/common';
  import { Card, Row, Col, Tag, Empty } from 'ant-design-vue';
  import {
    SettingOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    QuestionCircleOutlined,
    CopyOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
    ReadOutlined,
    CloudServerOutlined,
  } from '@ant-design/icons-vue';

  const { t } = useI18n();
  const loading = ref(false);
  const passwordVisible = ref(false);

  const sipConfig = reactive<SipConfigResultVO>({
    sipId: '',
    sipDomain: '',
    sipPort: 0,
    sipPassword: '',
    sipHost: '',
    registerTimeInterval: 0,
    serverNodes: [],
  });

  interface ParamItem {
    key: string;
    label: string;
    help: string;
    value: string;
    suffix?: string;
    secret?: boolean;
  }

  const leftParams = computed<ParamItem[]>(() => [
    {
      key: 'sipId',
      label: t('video.device.sipConfig.sipId'),
      help: t('video.device.sipConfig.sipIdHelp'),
      value: sipConfig.sipId,
    },
    {
      key: 'sipDomain',
      label: t('video.device.sipConfig.sipDomain'),
      help: t('video.device.sipConfig.sipDomainHelp'),
      value: sipConfig.sipDomain,
    },
    {
      key: 'sipHost',
      label: t('video.device.sipConfig.sipHost'),
      help: t('video.device.sipConfig.sipHostHelp'),
      value: sipConfig.sipHost,
    },
    {
      key: 'sipPassword',
      label: t('video.device.sipConfig.sipPassword'),
      help: t('video.device.sipConfig.sipPasswordHelp'),
      value: sipConfig.sipPassword,
      secret: true,
    },
  ]);

  const rightParams = computed<ParamItem[]>(() => [
    {
      key: 'sipPort',
      label: t('video.device.sipConfig.sipPort'),
      help: t('video.device.sipConfig.sipPortHelp'),
      value: sipConfig.sipPort ? String(sipConfig.sipPort) : '-',
    },
    {
      key: 'registerTimeInterval',
      label: t('video.device.sipConfig.registerTimeInterval'),
      help: t('video.device.sipConfig.registerTimeIntervalHelp'),
      value: sipConfig.registerTimeInterval ? String(sipConfig.registerTimeInterval) : '-',
      suffix: t('video.device.sipConfig.seconds'),
    },
    {
      key: 'transportProtocol',
      label: t('video.device.sipConfig.transportProtocol'),
      help: t('video.device.sipConfig.transportProtocolHelp'),
      value: t('video.device.sipConfig.transportProtocolValue'),
    },
  ]);

  const steps = computed(() => [
    t('video.device.sipConfig.step1'),
    t('video.device.sipConfig.step2'),
    t('video.device.sipConfig.step3'),
    t('video.device.sipConfig.step4'),
    t('video.device.sipConfig.step5'),
    t('video.device.sipConfig.step6'),
    t('video.device.sipConfig.step7'),
    t('video.device.sipConfig.step8'),
    t('video.device.sipConfig.step9'),
    t('video.device.sipConfig.step10'),
  ]);

  const hasOnlineNode = computed(() =>
    sipConfig.serverNodes?.some((n) => n.onlineStatus) ?? false,
  );

  async function loadData() {
    loading.value = true;
    try {
      const res = await getSipConfigInfo();
      if (res) {
        Object.assign(sipConfig, res);
      }
    } finally {
      loading.value = false;
    }
  }

  async function handleCopy(item: ParamItem) {
    const text = item.secret ? sipConfig.sipPassword : item.value;
    if (text) {
      await handleCopyTextV2(text);
    }
  }

  onMounted(() => {
    loadData();
  });
</script>

<style lang="less" scoped>
  // Flexy Dashboard 风格变量
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
  @danger: #fa896b;
  @purple: #7c5cfc;
  @radius-lg: 16px;
  @radius-md: 12px;
  @shadow-card: 0 2px 12px rgba(0, 0, 0, 0.04);
  @shadow-hover: 0 4px 20px rgba(0, 0, 0, 0.08);

  :deep(.ant-card) {
    border-radius: @radius-lg;
    box-shadow: @shadow-card;
    border: none;
    transition: box-shadow 0.3s ease;
  }

  // ========== 页面标题 ==========
  .header-card {
    margin-bottom: 20px;

    :deep(.ant-card-body) {
      padding: 24px 28px;
    }

    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .page-title {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .title-icon {
      width: 52px;
      height: 52px;
      border-radius: @radius-md;
      background: linear-gradient(135deg, #e8f0fe 0%, #d0e0fd 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: @primary;
      flex-shrink: 0;
    }

    .title-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .title-text {
      font-size: 20px;
      font-weight: 700;
      color: #2a3547;
      letter-spacing: -0.3px;
    }

    .title-desc {
      font-size: 13px;
      color: #8c97a5;
      line-height: 1.5;
    }
  }

  // ========== 接入参数卡片 ==========
  .main-config-card {
    margin-bottom: 20px;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 16px 24px 12px;

      .ant-card-head-title {
        font-weight: 600;
        font-size: 15px;
        color: #2a3547;
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }

    .card-title-desc {
      font-size: 12px;
      color: #8c97a5;
      font-weight: 400;
    }
  }

  .card-title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .card-title-icon {
    font-size: 18px;

    &.primary {
      color: @primary;
    }

    &.success {
      color: @success;
    }

    &.purple {
      color: @purple;
    }
  }

  .param-list {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .param-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-radius: @radius-md;
    transition: background 0.2s ease;

    &:hover {
      background: #f8f9fc;

      .copy-icon {
        opacity: 1;
      }
    }

    & + .param-item {
      border-top: 1px solid #f5f5f5;
    }
  }

  .param-label {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 130px;
    flex-shrink: 0;

    .label-text {
      font-size: 13px;
      font-weight: 500;
      color: #5a6a85;
    }

    .label-help {
      font-size: 12px;
      color: #bbb;
      cursor: help;
      transition: color 0.2s;

      &:hover {
        color: @primary;
      }
    }
  }

  .param-value {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    justify-content: flex-end;
  }

  .value-box {
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
    font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
    letter-spacing: 0.3px;
    background: #f5f7fa;
    padding: 6px 14px;
    border-radius: 8px;
    border: 1px solid #eef0f4;

    &.value-secret {
      color: #8c97a5;
      letter-spacing: 2px;
    }

    .value-suffix {
      font-size: 12px;
      font-weight: 400;
      color: #8c97a5;
      margin-left: 4px;
    }
  }

  .value-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .action-icon {
    font-size: 15px;
    color: #bbb;
    cursor: pointer;
    padding: 4px;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      color: @primary;
      background: #eef3ff;
    }
  }

  .copy-icon {
    opacity: 0;
    transition: opacity 0.2s ease, color 0.2s ease;
  }

  // ========== 底部区域 ==========
  .bottom-row {
    :deep(.ant-col) {
      display: flex;
    }
  }

  // ========== 配置指引 ==========
  .guide-card {
    width: 100%;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 16px 24px 12px;

      .ant-card-head-title {
        font-weight: 600;
        font-size: 15px;
        color: #2a3547;
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .guide-desc {
    font-size: 13px;
    color: #8c97a5;
    margin-bottom: 20px;
    line-height: 1.6;
  }

  .guide-steps {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .step-item {
    display: flex;
    align-items: flex-start;
    gap: 14px;
    padding: 10px 0;
    position: relative;

    & + .step-item {
      &::before {
        content: '';
        position: absolute;
        left: 14px;
        top: -2px;
        width: 1px;
        height: 12px;
        background: #e0e5ec;
      }
    }
  }

  .step-number {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: linear-gradient(135deg, #e8f5f0 0%, #c3f5e5 100%);
    color: #0bb783;
    font-size: 12px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .step-text {
    font-size: 13px;
    color: #2a3547;
    line-height: 28px;
    font-weight: 400;
  }

  // ========== 节点状态 ==========
  .node-card {
    width: 100%;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 16px 24px 12px;

      .ant-card-head-title {
        font-weight: 600;
        font-size: 15px;
        color: #2a3547;
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .node-desc {
    font-size: 13px;
    color: #8c97a5;
    margin-bottom: 16px;
    line-height: 1.6;
  }

  .node-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .node-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 16px;
    background: #fafbfc;
    border-radius: @radius-md;
    border: 1px solid #f0f2f5;
    transition: all 0.2s ease;

    &:hover {
      background: #f5f7fa;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }
  }

  .node-status-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;

    &.online {
      background: @success;
      box-shadow: 0 0 6px rgba(19, 222, 185, 0.4);
    }

    &.offline {
      background: #d9d9d9;
    }
  }

  .node-info {
    flex: 1;
    min-width: 0;
  }

  .node-instance {
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
    margin-bottom: 4px;
  }

  .node-meta {
    font-size: 12px;
    color: #8c97a5;

    .node-ips {
      font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
    }
  }

  .node-tag {
    flex-shrink: 0;
  }

  // ========== 响应式 ==========
  @media (max-width: 768px) {
    .header-card {
      .header-row {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
      }
    }

    .param-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;

      .param-value {
        width: 100%;
        justify-content: flex-start;
      }
    }

    .copy-icon {
      opacity: 1;
    }
  }
</style>
