<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部状态栏 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="plan-icon">
            <VideoCameraOutlined style="font-size: 28px; color: #fff" />
          </div>
          <div class="plan-meta">
            <div class="plan-name">
              <span class="name-text">{{ planDetail.planName || '-' }}</span>
              <Tag :color="planDetail.planStatus === '1' ? 'success' : 'default'">
                {{ planDetail.planStatus === '1' ? t('video.record.plan.enabled') : t('video.record.plan.disabled') }}
              </Tag>
              <Tag color="blue">
                {{ planDetail.echoMap?.planType || planDetail.planType || '-' }}
              </Tag>
            </div>
            <div class="header-meta-line">
              <span>
                <DatabaseOutlined />
                {{ t('video.record.plan.mediaIdentification') }}：{{ displayVal(planDetail.mediaIdentification) }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ displayVal(planDetail.createdTime) }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button
            type="primary"
            @click="goPlayback"
            v-hasAnyPermission="['video:record:playback:view']"
          >
            <template #icon><PlaySquareOutlined /></template>
            {{ t('video.record.plan.play') }}
          </a-button>
          <a-button
            v-if="planDetail.planStatus !== '1'"
            type="primary"
            @click="handleActivate"
            v-hasAnyPermission="['video:record:plan:activate']"
          >
            <template #icon><PlayCircleOutlined /></template>
            {{ t('video.record.plan.activate') }}
          </a-button>
          <a-button
            v-else
            @click="handleDeactivate"
            v-hasAnyPermission="['video:record:plan:activate']"
          >
            <template #icon><PauseCircleOutlined /></template>
            {{ t('video.record.plan.deactivate') }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['video:record:plan:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('common.title.edit') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- 指标卡片行 -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon type"><VideoCameraOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.record.plan.recordFormat') }}</div>
            <div class="metric-value">{{ planDetail.recordFormat || 'mp4' }}</div>
            <div class="metric-sub-text">{{ t('video.record.plan.helpMessage.recordFormat') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon duration"><FieldTimeOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.record.plan.segmentDuration') }}</div>
            <div class="metric-value">
              {{ formatDuration(planDetail.segmentDuration) }}
            </div>
            <div class="metric-sub-text">{{ t('video.record.plan.helpMessage.segmentDuration') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon retention"><CalendarOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.record.plan.retentionDays') }}</div>
            <div class="metric-value">
              {{ planDetail.retentionDays ?? 7 }}
              <span class="metric-unit">{{ t('video.record.plan.retentionDaysUnit') }}</span>
            </div>
            <div class="metric-sub-text">{{ t('video.record.plan.helpMessage.retentionDays') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon status" :class="{ active: planDetail.planStatus === '1' }">
            <CheckCircleOutlined v-if="planDetail.planStatus === '1'" />
            <StopOutlined v-else />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.record.plan.planStatus') }}</div>
            <div class="metric-value" :class="planDetail.planStatus === '1' ? 'text-success' : 'text-danger'">
              {{ planDetail.planStatus === '1' ? t('video.record.plan.enabled') : t('video.record.plan.disabled') }}
            </div>
            <div class="metric-sub-text">{{ t('video.record.plan.helpMessage.planStatus') }}</div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 详细配置 -->
    <Row :gutter="16" class="config-row">
      <!-- 基本信息 + 录像配置 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.record.plan.basicInfo')" class="config-card">
          <template #extra><InfoCircleOutlined style="color: #5d87ff" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.record.plan.planName')">
              {{ displayVal(planDetail.planName) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.planType')">
              <Tag color="blue">{{ planDetail.echoMap?.planType || displayVal(planDetail.planType) }}</Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.mediaIdentification')">
              {{ displayVal(planDetail.mediaIdentification) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.recordFormat')">
              <Tag color="purple">{{ planDetail.recordFormat || 'mp4' }}</Tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.segmentDuration')">
              {{ formatDuration(planDetail.segmentDuration) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.retentionDays')">
              <span class="highlight-number">{{ planDetail.retentionDays ?? 7 }}</span>
              {{ t('video.record.plan.retentionDaysUnit') }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>

      <!-- 调度与存储 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.record.plan.scheduleConfig')" class="config-card">
          <template #extra><ClockCircleOutlined style="color: #7c5cfc" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.record.plan.scheduleRule')">
              <div v-if="planDetail.scheduleRule" class="schedule-rule-box">
                <pre class="schedule-rule-code">{{ formatJson(planDetail.scheduleRule) }}</pre>
              </div>
              <span v-else class="text-muted">{{ t('video.record.plan.noScheduleRule') }}</span>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.storagePath')">
              <code v-if="planDetail.storagePath" class="storage-path">{{ planDetail.storagePath }}</code>
              <span v-else class="text-muted">{{ t('video.record.plan.noStoragePath') }}</span>
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.remark')">
              {{ planDetail.remark || t('video.record.plan.noRemark') }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <!-- 系统信息 -->
    <Row :gutter="16" class="config-row">
      <Col :xs="24">
        <Card :bordered="false" :title="t('video.record.plan.systemInfo')" class="config-card">
          <template #extra><SettingOutlined style="color: #13deb9" /></template>
          <a-descriptions :column="{ xs: 1, sm: 2, lg: 4 }" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.record.plan.extendParams')">
              {{ displayVal(planDetail.extendParams) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.createdBy')">
              {{ echoMapText(planDetail, 'createdBy') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.createdTime')">
              {{ displayVal(planDetail.createdTime) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.record.plan.updatedTime')">
              {{ displayVal(planDetail.updatedTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <EditModal @register="registerModal" @success="load" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { detail, activate, deactivate } from '/@/api/video/record/plan';
  import type { VideoRecordPlanResultVO } from '/@/api/video/record/model/planModel';
  import { PageWrapper } from '/@/components/Page';
  import {
    VideoCameraOutlined,
    EditOutlined,
    PlayCircleOutlined,
    PauseCircleOutlined,
    PlaySquareOutlined,
    DatabaseOutlined,
    ClockCircleOutlined,
    FieldTimeOutlined,
    CalendarOutlined,
    CheckCircleOutlined,
    StopOutlined,
    InfoCircleOutlined,
    SettingOutlined,
  } from '@ant-design/icons-vue';
  import { Card, Tag, Row, Col } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import EditModal from './Edit.vue';
  import { echoMapText } from '/@/utils/echo';

  const { t } = useI18n();
  const router = useRouter();
  const { currentRoute } = router;
  const { createConfirm, createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();

  const labelStyle = { width: '140px', fontWeight: 500, color: '#666' };
  const contentStyle = { color: '#333' };

  let planDetail = reactive<VideoRecordPlanResultVO>({});
  const id = ref('');

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    load();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(planDetail, res);
  };

  function goPlayback() {
    router.push({ path: '/video/record/playback' });
  }

  function handleEdit() {
    openModal(true, { record: planDetail, type: ActionEnum.EDIT });
  }

  async function handleActivate() {
    try {
      await activate(id.value);
      createMessage.success(t('video.record.plan.activateSuccess'));
      await load();
    } catch (e) {
      createMessage.error(t('video.record.plan.activateFailed'));
    }
  }

  function handleDeactivate() {
    createConfirm({
      iconType: 'warning',
      content: t('video.record.plan.confirmDeactivate'),
      onOk: async () => {
        try {
          await deactivate(id.value);
          createMessage.success(t('video.record.plan.deactivateSuccess'));
          await load();
        } catch (e) {
          createMessage.error(t('video.record.plan.activateFailed'));
        }
      },
    });
  }

  function displayVal(val?: string | number | null): string {
    if (val == null || val === '') return '-';
    return String(val);
  }

  function formatDuration(seconds?: number): string {
    if (!seconds) return '3600s (1h)';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    let result = `${seconds}s`;
    if (h > 0) {
      result += ` (${h}h`;
      if (m > 0) result += `${m}m`;
      result += ')';
    } else if (m > 0) {
      result += ` (${m}m`;
      if (s > 0) result += `${s}s`;
      result += ')';
    }
    return result;
  }

  function formatJson(str: string): string {
    try {
      return JSON.stringify(JSON.parse(str), null, 2);
    } catch {
      return str;
    }
  }
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
  @danger: #fa896b;
  @purple: #7c5cfc;
  @radius-lg: 16px;
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
    }

    .header-left {
      display: flex;
      align-items: flex-start;
      gap: 20px;
      flex: 1;
    }

    .plan-icon {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: linear-gradient(135deg, @danger, #e85d4a);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .plan-meta {
      flex: 1;
    }

    .plan-name {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;
      flex-wrap: wrap;

      .name-text {
        font-size: 22px;
        font-weight: 700;
        color: #2a3547;
        letter-spacing: -0.3px;
      }
    }

    .header-meta-line {
      font-size: 13px;
      color: #8c97a5;
      line-height: 1.8;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;

      .anticon {
        margin-right: 4px;
      }
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

      &.type {
        background: linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%);
        color: @purple;
      }

      &.duration {
        background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
        color: @primary;
      }

      &.retention {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: @warning;
      }

      &.status {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: @danger;

        &.active {
          background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
          color: #0bb783;
        }
      }
    }

    .metric-body {
      flex: 1;
      min-width: 0;
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
      margin-bottom: 2px;
    }

    .metric-unit {
      font-size: 13px;
      font-weight: 400;
      color: #8c97a5;
    }

    .text-success {
      color: @success;
    }

    .text-danger {
      color: @danger;
    }

    .metric-sub-text {
      font-size: 12px;
      color: #a8b4c0;
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
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.03);
    border: 1px solid #f0f2f5;

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

  .highlight-number {
    font-size: 16px;
    font-weight: 700;
    color: @primary;
  }

  .text-muted {
    color: #a8b4c0;
    font-style: italic;
  }

  .schedule-rule-box {
    background: #f6f8fb;
    border: 1px solid #e5eaef;
    border-radius: 8px;
    padding: 10px 14px;
    max-height: 200px;
    overflow-y: auto;
  }

  .schedule-rule-code {
    margin: 0;
    font-size: 12px;
    font-family: 'SF Mono', 'Fira Code', monospace;
    color: #2a3547;
    white-space: pre-wrap;
    word-break: break-all;
  }

  .storage-path {
    background: #f6f8fb;
    color: @purple;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 13px;
  }
</style>
