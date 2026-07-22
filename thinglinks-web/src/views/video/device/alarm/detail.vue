<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部状态栏 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <!-- 级别色带条 + 主体信息 -->
        <div class="header-body">
          <div class="priority-strip" :style="{ background: priorityColor }" />
          <div class="header-main">
            <div class="alarm-title">
              <span class="title-text">
                {{ displayVal(alarmDetail.alarmDescription, t('video.device.alarm.defaultTitle')) }}
              </span>
              <Tag :color="statusTagColor">
                {{
                  alarmDetail?.echoMap?.handleStatus ||
                  handleStatusText(Number(alarmDetail.handleStatus ?? 0))
                }}
              </Tag>
              <Tag :color="priorityTagColor">
                {{ t('video.device.alarm.alarmPriority') }}：{{
                  alarmDetail?.echoMap?.alarmPriority || displayVal(alarmDetail.alarmPriority)
                }}
              </Tag>
              <Tag v-if="alarmDetail.alarmMethod != null" color="blue">
                {{ t('video.device.alarm.alarmMethod') }}：{{
                  alarmDetail?.echoMap?.alarmMethod || alarmDetail.alarmMethod
                }}
              </Tag>
            </div>
            <div class="header-meta">
              <span>
                {{ t('video.device.alarm.deviceIdentification') }}：{{
                  displayVal(alarmDetail.deviceIdentification)
                }}
              </span>
              <a-divider type="vertical" />
              <span>
                {{ t('video.device.alarm.channelIdentification') }}：{{
                  displayVal(alarmDetail.channelIdentification)
                }}
              </span>
              <a-divider type="vertical" />
              <span>
                {{ t('video.device.alarm.alarmTime') }}：{{ displayVal(alarmDetail.alarmTime) }}
              </span>
            </div>
          </div>
        </div>
        <!-- 操作按钮 -->
        <a-space>
          <a-button
            v-if="Number(alarmDetail.handleStatus) === 0"
            type="primary"
            v-hasAnyPermission="['video:device:alarm:confirm']"
            @click="onConfirm"
          >
            <template #icon><CheckOutlined /></template>
            {{ t('video.device.alarm.confirm') }}
          </a-button>
          <a-button
            v-if="Number(alarmDetail.handleStatus) === 1"
            type="primary"
            v-hasAnyPermission="['video:device:alarm:resolve']"
            @click="onResolve"
          >
            <template #icon><CheckCircleOutlined /></template>
            {{ t('video.device.alarm.resolve') }}
          </a-button>
          <a-button
            v-if="Number(alarmDetail.handleStatus) === 0 || Number(alarmDetail.handleStatus) === 1"
            v-hasAnyPermission="['video:device:alarm:ignore']"
            @click="onIgnore"
          >
            <template #icon><StopOutlined /></template>
            {{ t('video.device.alarm.ignore') }}
          </a-button>
          <a-button @click="handleBack">
            <template #icon><ArrowLeftOutlined /></template>
            {{ t('video.device.alarm.back') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- 关键指标卡 -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon time"><ClockCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.alarm.alarmTime') }}</div>
            <div class="metric-value ellipsis-1" :title="displayVal(alarmDetail.alarmTime)">
              {{ displayVal(alarmDetail.alarmTime) }}
            </div>
            <div class="metric-sub">
              {{ relativeTime(alarmDetail.alarmTime) }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon priority" :style="{ background: priorityGradient, color: priorityColor }">
            <ExclamationCircleOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.alarm.alarmPriority') }}</div>
            <div class="metric-value">
              {{
                alarmDetail?.echoMap?.alarmPriority || displayVal(alarmDetail.alarmPriority)
              }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon progress"><CheckCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.alarm.handleStatus') }}</div>
            <div class="metric-value">
              {{
                alarmDetail?.echoMap?.handleStatus ||
                handleStatusText(Number(alarmDetail.handleStatus ?? 0))
              }}
            </div>
            <a-progress
              :percent="handleProgressPercent"
              :showInfo="false"
              :strokeColor="priorityColor"
              size="small"
            />
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon handler"><UserOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.alarm.handleUser') }}</div>
            <div class="metric-value ellipsis-1" :title="handleUserDisplay">
              {{ handleUserDisplay }}
            </div>
            <div class="metric-sub">{{ displayVal(alarmDetail.handleTime) }}</div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 详情两列 -->
    <Row :gutter="16" class="config-row">
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.device.alarm.baseInfo')" class="config-card">
          <template #extra><InfoCircleOutlined style="color: #5d87ff" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.device.alarm.id')">
              {{ displayVal(alarmDetail.id) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.deviceIdentification')">
              {{ displayVal(alarmDetail.deviceIdentification) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.channelIdentification')">
              {{ displayVal(alarmDetail.channelIdentification) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.alarmType')">
              {{ displayVal(alarmDetail.alarmType) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.alarmDescription')">
              {{ displayVal(alarmDetail.alarmDescription) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.location')">
              <template v-if="hasLocation">
                <span>
                  {{ t('video.device.alarm.longitude') }}：{{ alarmDetail.longitude }}
                  &nbsp;&nbsp;
                  {{ t('video.device.alarm.latitude') }}：{{ alarmDetail.latitude }}
                </span>
                <a-button type="link" size="small" @click="handleViewOnMap">
                  <template #icon><EnvironmentOutlined /></template>
                  {{ t('video.device.alarm.viewOnMap') }}
                </a-button>
              </template>
              <span v-else>-</span>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.createdTime')">
              {{ displayVal(alarmDetail.createdTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.device.alarm.handleInfo')" class="config-card">
          <template #extra><CheckCircleOutlined style="color: #13deb9" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.device.alarm.handleStatus')">
              <Tag :color="statusTagColor">
                {{
                  alarmDetail?.echoMap?.handleStatus ||
                  handleStatusText(Number(alarmDetail.handleStatus ?? 0))
                }}
              </Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.handleUser')">
              {{ handleUserDisplay }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.handleTime')">
              {{ displayVal(alarmDetail.handleTime) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.device.alarm.handleResult')">
              <div class="handle-result-box" v-if="alarmDetail.handleResult">
                {{ alarmDetail.handleResult }}
              </div>
              <span v-else class="empty-hint">-- {{ t('video.device.alarm.noHandleRecord') }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <!-- 告警类型参数 JSON -->
    <Card
      v-if="alarmTypeParamPretty"
      :bordered="false"
      :title="t('video.device.alarm.alarmTypeParam')"
      class="config-card json-card"
    >
      <template #extra>
        <a-button type="link" size="small" @click="handleCopyJson">
          <template #icon><CopyOutlined /></template>
          {{ t('common.title.copy') }}
        </a-button>
      </template>
      <pre class="json-pre">{{ alarmTypeParamPretty }}</pre>
    </Card>
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent, reactive, ref, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag } from 'ant-design-vue';
  import {
    ArrowLeftOutlined,
    CheckCircleOutlined,
    CheckOutlined,
    ClockCircleOutlined,
    CopyOutlined,
    EnvironmentOutlined,
    ExclamationCircleOutlined,
    InfoCircleOutlined,
    StopOutlined,
    UserOutlined,
  } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { detail as fetchAlarmDetail } from '/@/api/video/device/alarm';
  import type { VideoDeviceAlarmResultVO } from '/@/api/video/device/model/alarmModel';
  import { useAlarmActions } from './useAlarmActions';

  const HANDLE_STATUS_PROGRESS: Record<number, number> = {
    0: 10,
    1: 50,
    2: 100,
    3: 100,
  };

  /** 告警级别色板：1-红 / 2-橙 / 3-蓝 / 4-绿 */
  const PRIORITY_COLOR: Record<number, string> = {
    1: '#fa5c7c',
    2: '#ffae1f',
    3: '#5d87ff',
    4: '#13deb9',
  };

  const PRIORITY_GRADIENT: Record<number, string> = {
    1: 'linear-gradient(135deg, #ffe4ea 0%, #fbb6c2 100%)',
    2: 'linear-gradient(135deg, #fff3d6 0%, #ffd79a 100%)',
    3: 'linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%)',
    4: 'linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%)',
  };

  export default defineComponent({
    name: 'VideoDeviceAlarmDetail',
    components: {
      PageWrapper,
      Card,
      Row,
      Col,
      Tag,
      ArrowLeftOutlined,
      CheckCircleOutlined,
      CheckOutlined,
      ClockCircleOutlined,
      CopyOutlined,
      EnvironmentOutlined,
      ExclamationCircleOutlined,
      InfoCircleOutlined,
      StopOutlined,
      UserOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const { clipboardRef, copiedRef } = useCopyToClipboard();
      const router = useRouter();
      const { currentRoute } = router;

      const handleStatusText = (status: number): string => {
        const map: Record<number, string> = {
          0: t('video.device.alarm.status.pending'),
          1: t('video.device.alarm.status.processing'),
          2: t('video.device.alarm.status.resolved'),
          3: t('video.device.alarm.status.ignored'),
        };
        return map[status] || '';
      };

      const alarmDetail = reactive<VideoDeviceAlarmResultVO>({} as VideoDeviceAlarmResultVO);
      const id = ref<string>('');

      const labelStyle = { width: '140px', fontWeight: 500, color: '#666' };
      const contentStyle = { color: '#333' };

      const { handleConfirm, handleResolve, handleIgnore } = useAlarmActions(load);

      async function load() {
        if (!id.value) return;
        const res = await fetchAlarmDetail(id.value);
        if (res) {
          Object.keys(alarmDetail).forEach((k) => delete (alarmDetail as any)[k]);
          Object.assign(alarmDetail, res);
        }
      }

      onMounted(() => {
        id.value = (currentRoute.value.params.id as string) ?? '';
        load();
      });

      function displayVal(val: any, fallback = '-'): string {
        if (val == null || val === '') return fallback;
        return String(val);
      }

      function relativeTime(val?: string | null): string {
        if (!val) return '';
        const d = new Date(val);
        if (isNaN(d.getTime())) return '';
        const diff = Date.now() - d.getTime();
        const minutes = Math.floor(diff / 60000);
        if (minutes < 1) return t('video.device.alarm.justNow');
        if (minutes < 60) return `${minutes} ${t('video.device.alarm.minutesAgo')}`;
        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `${hours} ${t('video.device.alarm.hoursAgo')}`;
        const days = Math.floor(hours / 24);
        return `${days} ${t('video.device.alarm.daysAgo')}`;
      }

      const priorityColor = computed(
        () => PRIORITY_COLOR[Number(alarmDetail.alarmPriority ?? 3)] ?? '#5d87ff',
      );
      const priorityGradient = computed(
        () => PRIORITY_GRADIENT[Number(alarmDetail.alarmPriority ?? 3)] ?? PRIORITY_GRADIENT[3],
      );
      const priorityTagColor = computed(() => {
        const p = Number(alarmDetail.alarmPriority);
        return p === 1 ? 'red' : p === 2 ? 'orange' : p === 4 ? 'green' : 'blue';
      });

      const statusTagColor = computed(() => {
        const s = Number(alarmDetail.handleStatus);
        return s === 0 ? 'orange' : s === 1 ? 'processing' : s === 2 ? 'success' : 'default';
      });

      const handleProgressPercent = computed(
        () => HANDLE_STATUS_PROGRESS[Number(alarmDetail.handleStatus ?? 0)] ?? 0,
      );

      const handleUserDisplay = computed(() => {
        const name = alarmDetail?.echoMap?.handleUserId;
        if (name) return String(name);
        if (alarmDetail.handleUserId) return String(alarmDetail.handleUserId);
        return '-';
      });

      const hasLocation = computed(
        () =>
          alarmDetail.longitude != null &&
          alarmDetail.longitude !== 0 &&
          alarmDetail.latitude != null &&
          alarmDetail.latitude !== 0,
      );

      const alarmTypeParamPretty = computed(() => {
        const raw = alarmDetail.alarmTypeParam;
        if (!raw) return '';
        try {
          return JSON.stringify(JSON.parse(String(raw)), null, 2);
        } catch {
          return String(raw);
        }
      });

      function onConfirm() {
        handleConfirm(alarmDetail);
      }

      function onResolve() {
        handleResolve(alarmDetail);
      }

      function onIgnore() {
        handleIgnore(alarmDetail);
      }

      function handleBack() {
        router.back();
      }

      function handleViewOnMap() {
        router.push({
          path: '/device/videoDevice/position',
          query: {
            lng: String(alarmDetail.longitude ?? ''),
            lat: String(alarmDetail.latitude ?? ''),
          },
        });
      }

      function handleCopyJson() {
        clipboardRef.value = alarmTypeParamPretty.value;
        if (copiedRef.value) {
          createMessage.success(t('common.tips.copySuccess'));
        }
      }

      return {
        t,
        alarmDetail,
        labelStyle,
        contentStyle,
        priorityColor,
        priorityGradient,
        priorityTagColor,
        statusTagColor,
        handleProgressPercent,
        handleUserDisplay,
        hasLocation,
        alarmTypeParamPretty,
        handleStatusText,
        displayVal,
        relativeTime,
        onConfirm,
        onResolve,
        onIgnore,
        handleBack,
        handleViewOnMap,
        handleCopyJson,
      };
    },
  });
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
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

  .header-card {
    margin-bottom: 20px;

    :deep(.ant-card-body) {
      padding: 24px 28px;
    }

    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
    }

    .header-body {
      display: flex;
      align-items: stretch;
      gap: 16px;
      flex: 1;
      min-width: 0;
    }

    .priority-strip {
      width: 4px;
      border-radius: 2px;
      flex-shrink: 0;
    }

    .header-main {
      flex: 1;
      min-width: 0;
    }

    .alarm-title {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 14px;

      .title-text {
        font-size: 20px;
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

      &.time {
        background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
        color: @primary;
      }

      &.progress {
        background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
        color: @success;
      }

      &.handler {
        background: linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%);
        color: #7c5cfc;
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
      font-size: 20px;
      font-weight: 700;
      color: #2a3547;
      margin-bottom: 4px;
      letter-spacing: -0.5px;
    }

    .metric-sub {
      font-size: 12px;
      color: #8c97a5;
    }

    .ellipsis-1 {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .config-row {
    margin-bottom: 20px;
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
  }

  .handle-result-box {
    background: #f5f7fa;
    border-radius: @radius-md;
    padding: 10px 14px;
    color: #2a3547;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .empty-hint {
    color: #b0bac6;
    font-style: italic;
  }

  .json-card {
    margin-bottom: 20px;
  }

  .json-pre {
    margin: 0;
    padding: 16px;
    background: #f5f7fa;
    border-radius: @radius-md;
    font-family: 'Fira Code', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 13px;
    line-height: 1.6;
    color: #2a3547;
    overflow-x: auto;
    max-height: 280px;
  }
</style>
