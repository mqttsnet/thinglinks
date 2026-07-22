<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部状态栏 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="server-name">
            <span class="name-text">{{ videoNodeDetail.name }}</span>
            <Tag :color="isTruthyStatus(videoNodeDetail?.onlineStatus) ? 'success' : 'error'">
              {{ isTruthyStatus(videoNodeDetail?.onlineStatus) ? t('video.media.server.online') : t('video.media.server.offline') }}
            </Tag>
            <Tag color="blue">{{
              videoNodeDetail?.echoMap?.type || getDictLabel(DictEnum.VIDEO_MEDIA_SERVER_TYPE, videoNodeDetail?.type)
            }}</Tag>
            <Tag v-if="videoNodeDetail.version">v{{ videoNodeDetail.version }}</Tag>
            <Tag v-if="videoNodeDetail.defaultServer" color="orange">{{ t('video.media.server.defaultNode') }}</Tag>
          </div>
          <div class="header-meta">
            <span>{{ t('video.media.server.appId') }}：<Tag v-if="videoNodeDetail.appId" color="blue">{{ videoNodeDetail.appId }}</Tag>{{ getDictLabel(DictEnum.VIDEO_APPLICATION_SCENARIO, videoNodeDetail.appId) || '' }}</span>
            <a-divider type="vertical" />
            <span>{{ t('video.media.server.mediaIdentification') }}：{{ displayVal(videoNodeDetail.mediaIdentification) }}</span>
            <a-divider type="vertical" />
            <span>{{ t('video.media.server.host') }}：{{ displayVal(videoNodeDetail.host) }}</span>
            <a-divider type="vertical" />
            <span>{{ t('video.media.server.lastAliveTime') }}：{{ displayVal(videoNodeDetail.lastAliveTime) }}</span>
          </div>
        </div>
        <a-space>
          <a-button
            :loading="testLoading"
            @click="handleTestConnection"
            v-hasAnyPermission="['video:media:server:view']"
          >
            <template #icon><ApiOutlined /></template>
            {{ t('video.media.server.testConnection') }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['video:media:server:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('video.media.server.updateNodeInfo') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- 性能指标卡片 -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon cpu"><DashboardOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.media.server.cpuUsageLabel') }}</div>
            <div class="metric-value">{{ formatPercent(toNum(videoNodeDetail.cpuUsage)) }}</div>
            <a-progress
              :percent="toNum(videoNodeDetail.cpuUsage)"
              :showInfo="false"
              :strokeColor="getProgressColor(toNum(videoNodeDetail.cpuUsage))"
              size="small"
            />
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon memory"><CloudServerOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.media.server.memoryUsageLabel') }}</div>
            <div class="metric-value">{{ formatPercent(toNum(videoNodeDetail.memoryUsage)) }}</div>
            <a-progress
              :percent="toNum(videoNodeDetail.memoryUsage)"
              :showInfo="false"
              :strokeColor="getProgressColor(toNum(videoNodeDetail.memoryUsage))"
              size="small"
            />
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon stream"><PlayCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.media.server.streamCount') }}</div>
            <div class="metric-value">
              {{ toNum(videoNodeDetail.currentStreams) }}
              <span class="metric-sub">/ {{ videoNodeDetail.maxStreams != null ? videoNodeDetail.maxStreams : '∞' }}</span>
            </div>
            <a-progress
              :percent="videoNodeDetail.maxStreams ? Math.round((toNum(videoNodeDetail.currentStreams) / toNum(videoNodeDetail.maxStreams)) * 100) : 0"
              :showInfo="false"
              strokeColor="#1890ff"
              size="small"
            />
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon network"><SwapOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.media.server.networkThroughput') }}</div>
            <div class="metric-value net-value">
              <span class="net-up">↑ {{ formatBytes(toNum(videoNodeDetail.networkInSpeed)) }}/s</span>
              <span class="net-down">↓ {{ formatBytes(toNum(videoNodeDetail.networkOutSpeed)) }}/s</span>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 详细配置 -->
    <Row :gutter="16" class="config-row">
      <!-- 地址配置 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.media.server.addressConfig')" class="config-card">
          <template #extra><GlobalOutlined style="color: #5d87ff" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.media.server.host')">
              {{ displayVal(videoNodeDetail.host) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.hookHost')">
              {{ displayVal(videoNodeDetail.hookHost) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.sdpHost')">
              {{ displayVal(videoNodeDetail.sdpHost) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.streamHost')">
              {{ displayVal(videoNodeDetail.streamHost) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.secret')">
              <span class="secret-field">
                <span>{{ secretVisible ? displayVal(videoNodeDetail.secret) : '******' }}</span>
                <EyeInvisibleOutlined
                  v-if="secretVisible"
                  class="secret-eye"
                  @click="secretVisible = false"
                />
                <EyeOutlined v-else class="secret-eye" @click="secretVisible = true" />
              </span>
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>

      <!-- 端口配置 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.media.server.portConfig')" class="config-card">
          <template #extra><ApiOutlined style="color: #7c5cfc" /></template>
          <a-descriptions :column="2" :labelStyle="labelStyleSm" :contentStyle="contentStyleSm">
            <a-descriptions-item :label="t('video.media.server.httpPort')">
              {{ displayPort(videoNodeDetail.httpPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.httpSslPort')">
              {{ displayPort(videoNodeDetail.httpSslPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtmpPort')">
              {{ displayPort(videoNodeDetail.rtmpPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtmpSslPort')">
              {{ displayPort(videoNodeDetail.rtmpSslPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtspPort')">
              {{ displayPort(videoNodeDetail.rtspPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtspSslPort')">
              {{ displayPort(videoNodeDetail.rtspSslPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.flvPort')">
              {{ displayPort(videoNodeDetail.flvPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.flvSslPort')">
              {{ displayPort(videoNodeDetail.flvSslPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.wsFlvPort')">
              {{ displayPort(videoNodeDetail.wsFlvPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.wsFlvSslPort')">
              {{ displayPort(videoNodeDetail.wsFlvSslPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtpProxyPort')">
              {{ displayPort(videoNodeDetail.rtpProxyPort) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.recordAssistPort')">
              {{ displayPort(videoNodeDetail.recordAssistPort) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <Row :gutter="16" class="config-row">
      <!-- 流与录像配置 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.media.server.streamRecordConfig')" class="config-card">
          <template #extra><VideoCameraOutlined style="color: #13deb9" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.media.server.rtpEnable')">
              <RenderBoolTag :value="videoNodeDetail.rtpEnable" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.rtpPortRange')">
              {{ displayVal(videoNodeDetail.rtpPortRange) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.sendRtpPortRange')">
              {{ displayVal(videoNodeDetail.sendRtpPortRange) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.recordPath')">
              {{ displayVal(videoNodeDetail.recordPath) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.recordDay')">
              {{ videoNodeDetail.recordDay != null ? videoNodeDetail.recordDay : '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.transcodeSuffix')">
              {{ displayVal(videoNodeDetail.transcodeSuffix) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.hookAliveInterval')">
              {{ videoNodeDetail.hookAliveInterval != null ? `${videoNodeDetail.hookAliveInterval} s` : '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>

      <!-- 系统信息 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.media.server.systemInfo')" class="config-card">
          <template #extra><SettingOutlined style="color: #ffae1f" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.media.server.autoConfig')">
              <RenderBoolTag :value="videoNodeDetail.autoConfig" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.defaultServer')">
              <RenderBoolTag :value="videoNodeDetail.defaultServer" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.capabilities')">
              {{ displayVal(videoNodeDetail.capabilities) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.extendParams')">
              {{ displayVal(videoNodeDetail.extendParams) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.remark')">
              {{ displayVal(videoNodeDetail.remark) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.createdBy')">
              {{ echoMapText(videoNodeDetail, 'createdBy') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.createdTime')">
              {{ displayVal(videoNodeDetail.createdTime) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.updatedBy')">
              {{ echoMapText(videoNodeDetail, 'updatedBy') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.media.server.updatedTime')">
              {{ displayVal(videoNodeDetail.updatedTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted, onUnmounted, h } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useUserStore } from '/@/store/modules/user';
  import { useWebSocket } from '@vueuse/core';
  import { detail, realTimeMetrics, testConnection } from '/@/api/video/media/server';
  import type { VideoMediaServerResultVO } from '/@/api/video/media/model/serverModel';
  import { PageWrapper } from '/@/components/Page';
  import {
    EditOutlined,
    DashboardOutlined,
    CloudServerOutlined,
    PlayCircleOutlined,
    SwapOutlined,
    GlobalOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
    ApiOutlined,
    VideoCameraOutlined,
    SettingOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import EditModal from './Edit.vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';
  import { Tag, Card, Row, Col } from 'ant-design-vue';
  import { echoMapText } from '/@/utils/echo';

  const { getDictLabel } = useDict();
  const { t } = useI18n();

  const RenderBoolTag = (props: { value?: boolean | null }) => {
    if (props.value === true) return h(Tag, { color: 'success' }, () => t('thinglinks.common.yes'));
    if (props.value === false) return h(Tag, { color: 'error' }, () => t('thinglinks.common.no'));
    return h('span', '-');
  };
  RenderBoolTag.props = ['value'];
  const { currentRoute } = useRouter();
  const { createMessage } = useMessage();
  const testLoading = ref(false);

  const labelStyle = { width: '160px', fontWeight: 500, color: '#666' };
  const contentStyle = { color: '#333' };
  const labelStyleSm = { width: '130px', fontWeight: 500, color: '#666', fontSize: '13px' };
  const contentStyleSm = { color: '#333', fontSize: '13px' };

  let videoNodeDetail = reactive<VideoMediaServerResultVO>({});
  const secretVisible = ref(false);
  const [registerModal, { openModal }] = useModal();
  const id = ref('');
  let wsInstance: ReturnType<typeof useWebSocket> | null = null;

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    load();
  });

  onUnmounted(() => {
    wsInstance?.close();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(videoNodeDetail, res);
    // 先通过 HTTP 获取一次实时指标
    loadMetrics();
    // 建立 WS 连接持续接收指标推送
    connectMetricsWs();
  };

  const loadMetrics = async () => {
    try {
      const metrics = await realTimeMetrics(id.value);
      if (metrics) {
        Object.assign(videoNodeDetail, metrics);
      }
    } catch (_) {
      // 指标接口不可达时静默处理
    }
  };

  const connectMetricsWs = () => {
    const protocol = window.location.protocol;
    const host = window.location.host;
    const userStore = useUserStore();
    const tenantId = userStore.getTenantId;
    const token = userStore.getToken;
    const wsUrl = `${
      protocol.includes('https') ? 'wss' : 'ws'
    }://${host}/api/wsVideo/anyone/videoSocket/mediaServerMetrics/${tenantId}/${id.value}?Token=${token}&TenantId=${tenantId}`;

    wsInstance = useWebSocket(wsUrl, {
      autoReconnect: {
        retries: 3,
        delay: 10000,
        onFailed() {
          console.debug(t('video.media.server.wsReconnectFailed'));
        },
      },
      heartbeat: {
        message: 'ping',
        interval: 30000,
      },
      onMessage: (_ws, event) => {
        try {
          const metrics = JSON.parse(event.data);
          if (metrics) {
            Object.assign(videoNodeDetail, metrics);
          }
        } catch (_) {
          // 解析失败静默处理
        }
      },
    });
  };

  async function handleTestConnection() {
    const { host, httpPort, secret } = videoNodeDetail;
    if (!host || !httpPort || !secret) {
      createMessage.warning(t('video.media.server.testConnectionTip'));
      return;
    }
    testLoading.value = true;
    try {
      const result = await testConnection(host, httpPort, secret);
      if (result) {
        createMessage.success(t('video.media.server.testConnectionSuccess'));
      } else {
        createMessage.error(t('video.media.server.testConnectionFail'));
      }
    } catch (e) {
      errorMsg(t('video.media.server.testConnectionFail'));
    } finally {
      testLoading.value = false;
    }
  }

  function handleEdit(e: Event) {
    e?.stopPropagation();
    openModal(true, {
      record: videoNodeDetail,
      type: ActionEnum.EDIT,
    });
  }

  function handleSuccess() {
    load();
  }

  /** 安全转数值（后端可能返回字符串类型的数字） */
  function toNum(val?: any): number {
    if (val == null) return 0;
    const n = Number(val);
    return isNaN(n) ? 0 : n;
  }

  /** 端口显示：null/0 显示为 "-" */
  function displayPort(val?: number | null): string {
    if (val == null || val === 0) return '-';
    return String(val);
  }

  /** 通用空值显示 */
  function displayVal(val?: string | null): string {
    if (val == null || val === '') return '-';
    return val;
  }

  function formatPercent(val?: number) {
    if (val == null) return '-';
    return `${val.toFixed(1)}%`;
  }

  function formatBytes(bytes?: number) {
    if (bytes == null || bytes === 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    let i = 0;
    let val = bytes;
    while (val >= 1024 && i < units.length - 1) {
      val /= 1024;
      i++;
    }
    return `${val.toFixed(1)} ${units[i]}`;
  }

  function getProgressColor(val?: number) {
    if (val == null) return '#1890ff';
    if (val >= 90) return '#ff4d4f';
    if (val >= 70) return '#faad14';
    return '#52c41a';
  }
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

  // 通用卡片基础样式
  :deep(.ant-card) {
    border-radius: @radius-lg;
    box-shadow: @shadow-card;
    border: none;
    transition: box-shadow 0.3s ease;
  }

  .header-card {
    margin-bottom: 20px;

    :deep(.ant-card-body) {
      padding: 24px 28px;
    }

    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }

    .header-left {
      flex: 1;
    }

    .server-name {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 14px;

      .name-text {
        font-size: 22px;
        font-weight: 700;
        color: #2a3547;
        letter-spacing: -0.3px;
      }
    }

    .header-meta {
      font-size: 13px;
      color: #8c97a5;
      line-height: 1.8;
    }
  }

  .metric-row {
    margin-bottom: 20px;

    :deep(.ant-col) {
      display: flex;
    }
  }

  .metric-card {
    width: 100%;
    transition: box-shadow 0.3s ease, transform 0.2s ease;

    &:hover {
      box-shadow: @shadow-hover;
      transform: translateY(-2px);
    }

    :deep(.ant-card-body) {
      display: flex;
      align-items: center;
      gap: 18px;
      padding: 22px 20px;
      height: 100%;
    }

    .metric-icon {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;

      &.cpu {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: #e85d3a;
      }

      &.memory {
        background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
        color: @primary;
      }

      &.stream {
        background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
        color: #0bb783;
      }

      &.network {
        background: linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%);
        color: @purple;
      }
    }

    .metric-body {
      flex: 1;
      min-width: 0;
      min-height: 76px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .metric-label {
      font-size: 13px;
      color: #8c97a5;
      margin-bottom: 4px;
      font-weight: 500;
    }

    .metric-value {
      font-size: 24px;
      font-weight: 700;
      color: #2a3547;
      margin-bottom: 8px;
      letter-spacing: -0.5px;

      .metric-sub {
        font-size: 14px;
        font-weight: 400;
        color: #8c97a5;
      }
    }

    .net-value {
      font-size: 15px;
      font-weight: 600;
      display: flex;
      flex-direction: column;
      gap: 2px;
      margin-bottom: 0;

      .net-up {
        color: @success;
      }

      .net-down {
        color: @primary;
      }
    }

    :deep(.ant-progress-bg) {
      border-radius: 4px;
    }

    :deep(.ant-progress-inner) {
      border-radius: 4px;
    }
  }

  .config-row {
    margin-bottom: 20px;
  }

  .secret-field {
    display: inline-flex;
    align-items: center;
    gap: 8px;

    .secret-eye {
      cursor: pointer;
      font-size: 14px;
      color: #1890ff;
      transition: color 0.2s;

      &:hover {
        color: @primary;
      }
    }
  }

  .config-card {
    height: 100%;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 16px 24px 12px;

      .ant-card-head-title {
        font-weight: 600;
        font-size: 15px;
        color: #2a3547;
      }

      .ant-card-extra {
        font-size: 18px;
      }
    }

    :deep(.ant-card-body) {
      padding: 16px 24px 20px;
    }

    :deep(.ant-descriptions-item-label) {
      background: transparent;
      color: #5a6a85;
      font-weight: 500;
    }

    :deep(.ant-descriptions-item-content) {
      color: #2a3547;
    }
  }
</style>