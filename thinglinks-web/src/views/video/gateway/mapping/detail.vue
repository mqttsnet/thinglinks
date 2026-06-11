<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部状态栏 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="mapping-icon">
            <ApiOutlined style="font-size: 28px; color: #fff" />
          </div>
          <div class="mapping-meta">
            <div class="mapping-name">
              <span class="name-text">{{ mappingDetail.gbDeviceId }}</span>
              <Tag :color="mappingDetail.enable ? 'success' : 'default'">
                {{ mappingDetail.enable ? t('video.gateway.mapping.enabled') : t('video.gateway.mapping.disabled') }}
              </Tag>
              <Tag :color="mappingDetail.registerStatus ? 'processing' : 'warning'">
                {{ mappingDetail.registerStatus ? t('video.gateway.mapping.registered') : t('video.gateway.mapping.unregistered') }}
              </Tag>
              <Tag v-if="mappingDetail.autoPush" color="blue">
                {{ t('video.gateway.mapping.autoPush') }}
              </Tag>
            </div>
            <div class="header-meta-line">
              <span>
                <SwapOutlined />
                {{ getDictLabel(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL, mappingDetail?.srcProtocol) || mappingDetail.srcProtocol || '-' }}
              </span>
              <a-divider type="vertical" />
              <span>
                <LinkOutlined />
                {{ t('video.gateway.mapping.srcDeviceIdentification') }}：{{ displayVal(mappingDetail.srcDeviceIdentification) }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ displayVal(mappingDetail.createdTime) }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['video:gateway:mapping:edit']"
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
          <div class="metric-icon protocol"><SwapOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.gateway.mapping.srcProtocol') }}</div>
            <div class="metric-value">
              {{ getDictLabel(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL, mappingDetail?.srcProtocol) || mappingDetail.srcProtocol || '-' }}
            </div>
            <div class="metric-sub-text">{{ t('video.gateway.mapping.helpMessage.srcProtocol') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon enable" :class="{ active: mappingDetail.enable }">
            <CheckCircleOutlined v-if="mappingDetail.enable" />
            <StopOutlined v-else />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.gateway.mapping.enable') }}</div>
            <div class="metric-value" :class="mappingDetail.enable ? 'text-success' : 'text-danger'">
              {{ mappingDetail.enable ? t('video.gateway.mapping.enabled') : t('video.gateway.mapping.disabled') }}
            </div>
            <div class="metric-sub-text">{{ t('video.gateway.mapping.helpMessage.enable') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon register" :class="{ active: mappingDetail.registerStatus }">
            <SafetyCertificateOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.gateway.mapping.registerStatus') }}</div>
            <div class="metric-value" :class="mappingDetail.registerStatus ? 'text-success' : 'text-warning'">
              {{ mappingDetail.registerStatus ? t('video.gateway.mapping.registered') : t('video.gateway.mapping.unregistered') }}
            </div>
            <div class="metric-sub-text">{{ displayVal(mappingDetail.lastRegisterTime) }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon push" :class="{ active: mappingDetail.autoPush }">
            <SendOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.gateway.mapping.autoPush') }}</div>
            <div class="metric-value" :class="mappingDetail.autoPush ? 'text-primary' : ''">
              {{ mappingDetail.autoPush ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
            </div>
            <div class="metric-sub-text">{{ t('video.gateway.mapping.helpMessage.autoPush') }}</div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 详细配置 -->
    <Row :gutter="16" class="config-row">
      <!-- 源设备信息 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.gateway.mapping.sourceInfo')" class="config-card">
          <template #extra><DesktopOutlined style="color: #5d87ff" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.gateway.mapping.srcProtocol')">
              {{ getDictLabel(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL, mappingDetail?.srcProtocol) || displayVal(mappingDetail.srcProtocol) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.srcDeviceIdentification')">
              {{ displayVal(mappingDetail.srcDeviceIdentification) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.srcChannelIdentification')">
              {{ displayVal(mappingDetail.srcChannelIdentification) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>

      <!-- 国标映射信息 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.gateway.mapping.gbMappingInfo')" class="config-card">
          <template #extra><ApartmentOutlined style="color: #7c5cfc" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.gateway.mapping.gbDeviceId')">
              {{ displayVal(mappingDetail.gbDeviceId) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.gbChannelId')">
              {{ displayVal(mappingDetail.gbChannelId) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.gbPlatformId')">
              {{ displayVal(mappingDetail.gbPlatformId) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <Row :gutter="16" class="config-row">
      <!-- 状态与配置 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.gateway.mapping.statusConfig')" class="config-card">
          <template #extra><SettingOutlined style="color: #13deb9" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.gateway.mapping.enable')">
              <RenderBoolTag :value="mappingDetail.enable" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.autoPush')">
              <RenderBoolTag :value="mappingDetail.autoPush" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.registerStatus')">
              <RenderBoolTag :value="mappingDetail.registerStatus" />
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.lastRegisterTime')">
              {{ displayVal(mappingDetail.lastRegisterTime) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.mappingConfig')">
              {{ displayVal(mappingDetail.mappingConfig) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>

      <!-- 系统信息 -->
      <Col :xs="24" :lg="12">
        <Card :bordered="false" :title="t('video.gateway.mapping.systemInfo')" class="config-card">
          <template #extra><InfoCircleOutlined style="color: #ffae1f" /></template>
          <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
            <a-descriptions-item :label="t('video.gateway.mapping.remark')">
              {{ displayVal(mappingDetail.remark) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.createdBy')">
              {{ echoMapText(mappingDetail, 'createdBy') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.createdTime')">
              {{ displayVal(mappingDetail.createdTime) }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.updatedBy')">
              {{ echoMapText(mappingDetail, 'updatedBy') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('video.gateway.mapping.updatedTime')">
              {{ displayVal(mappingDetail.updatedTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </Card>
      </Col>
    </Row>

    <EditModal @register="registerModal" @success="load" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted, h } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { detail } from '/@/api/video/gateway/mapping';
  import type { VideoGatewayMappingResultVO } from '/@/api/video/gateway/model/mappingModel';
  import { PageWrapper } from '/@/components/Page';
  import {
    ApiOutlined,
    EditOutlined,
    SwapOutlined,
    LinkOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    StopOutlined,
    SafetyCertificateOutlined,
    SendOutlined,
    DesktopOutlined,
    ApartmentOutlined,
    SettingOutlined,
    InfoCircleOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import { DictEnum, ActionEnum } from '/@/enums/commonEnum';
  import { Card, Tag, Row, Col } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import EditModal from './Edit.vue';
  import { echoMapText } from '/@/utils/echo';

  const { t } = useI18n();
  const { currentRoute } = useRouter();
  const { getDictLabel } = useDict();
  const [registerModal, { openModal }] = useModal();

  const labelStyle = { width: '160px', fontWeight: 500, color: '#666' };
  const contentStyle = { color: '#333' };

  const RenderBoolTag = (props: { value?: boolean | null }) => {
    if (props.value === true) return h(Tag, { color: 'success' }, () => t('thinglinks.common.yes'));
    if (props.value === false) return h(Tag, { color: 'error' }, () => t('thinglinks.common.no'));
    return h('span', '-');
  };
  RenderBoolTag.props = ['value'];

  let mappingDetail = reactive<VideoGatewayMappingResultVO>({});
  const id = ref('');

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    load();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(mappingDetail, res);
  };

  function handleEdit() {
    openModal(true, { record: mappingDetail, type: ActionEnum.EDIT });
  }

  function displayVal(val?: string | number | null): string {
    if (val == null || val === '') return '-';
    return String(val);
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
  @shadow-card: 0 2px 12px rgba(0, 0, 0, 0.04);
  @shadow-hover: 0 4px 20px rgba(0, 0, 0, 0.08);

  :deep(.ant-card) {
    border-radius: @radius-lg;
    box-shadow: @shadow-card;
    border: none;
    transition: box-shadow 0.3s ease;
  }

  // ========== Header ==========
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

    .mapping-icon {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: linear-gradient(135deg, @purple, #5a3fd6);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .mapping-meta {
      flex: 1;
    }

    .mapping-name {
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

  // ========== Metric Cards ==========
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

      &.protocol {
        background: linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%);
        color: @purple;
      }

      &.enable {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: @danger;

        &.active {
          background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
          color: #0bb783;
        }
      }

      &.register {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: @warning;

        &.active {
          background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
          color: @primary;
        }
      }

      &.push {
        background: linear-gradient(135deg, #f0f2f5 0%, #d9dce0 100%);
        color: #8c97a5;

        &.active {
          background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
          color: @primary;
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

    .text-success {
      color: @success;
    }

    .text-danger {
      color: @danger;
    }

    .text-warning {
      color: @warning;
    }

    .text-primary {
      color: @primary;
    }

    .metric-sub-text {
      font-size: 12px;
      color: #a8b4c0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // ========== Config Cards ==========
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
</style>
