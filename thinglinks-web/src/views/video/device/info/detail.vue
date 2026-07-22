<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部状态栏 -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="device-icon">
            <VideoCameraOutlined style="font-size: 28px; color: #fff" />
          </div>
          <div class="device-meta">
            <div class="device-name">
              <span class="name-text">{{ videoNodeDetail.deviceName || videoNodeDetail.customName }}</span>
              <Tag :color="isTruthyStatus(videoNodeDetail.onlineStatus) ? 'success' : 'error'">
                {{ isTruthyStatus(videoNodeDetail.onlineStatus) ? t('video.device.info.online') : t('video.device.info.offline') }}
              </Tag>
              <Tag color="blue">
                {{ getDictLabel(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL, videoNodeDetail?.accessProtocol) || videoNodeDetail.accessProtocol }}
              </Tag>
              <Tag v-if="videoNodeDetail.customName && videoNodeDetail.deviceName">
                {{ videoNodeDetail.customName }}
              </Tag>
            </div>
            <div class="header-meta">
              <span>
                <GlobalOutlined />
                {{ t('video.device.info.host') }}：{{ displayVal(videoNodeDetail.host) }}:{{ displayVal(videoNodeDetail.port) }}
              </span>
              <a-divider type="vertical" />
              <span>
                <KeyOutlined />
                {{ t('video.device.info.deviceIdentification') }}：{{ displayVal(videoNodeDetail.deviceIdentification) }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ displayVal(videoNodeDetail.createdTime) }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['video:device:info:edit']"
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
          <div class="metric-icon status" :class="{ online: videoNodeDetail.onlineStatus }">
            <CheckCircleOutlined v-if="videoNodeDetail.onlineStatus" />
            <CloseCircleOutlined v-else />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.info.onlineStatus') }}</div>
            <div class="metric-value" :class="videoNodeDetail.onlineStatus ? 'text-success' : 'text-danger'">
              {{ videoNodeDetail.onlineStatus ? t('video.device.info.online') : t('video.device.info.offline') }}
            </div>
            <div class="metric-sub-text">
              {{ t('video.device.info.registerTime') }}：{{ displayVal(videoNodeDetail.registerTime) }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon channel"><VideoCameraOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.info.channelCount') }}</div>
            <div class="metric-value">{{ videoNodeDetail.channelCount ?? 0 }}</div>
            <div class="metric-sub-text">{{ t('video.device.info.channelCountDesc') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon transport"><SwapOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.info.transport') }}</div>
            <div class="metric-value">
              {{ getDictLabel(DictEnum.VIDEO_DEVICE_TRANSPORT, videoNodeDetail?.transport) || '-' }}
            </div>
            <div class="metric-sub-text">{{ t('video.device.info.transportHelp') }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon stream"><PlayCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('video.device.info.streamMode') }}</div>
            <div class="metric-value">
              {{ getDictLabel(DictEnum.VIDEO_DEVICE_STREAM_MODE, videoNodeDetail?.streamMode) || '-' }}
            </div>
            <div class="metric-sub-text">{{ t('video.device.info.streamModeHelp') }}</div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- Tab 区域：详情 + 通道 -->
    <Card :bordered="false" class="tab-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="info" :tab="t('video.device.info.basicInformation')">
          <!-- 基本信息 + 网络配置 -->
          <Row :gutter="16" class="config-row">
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.basicInfo')" class="config-card">
                <template #extra><InfoCircleOutlined style="color: #5d87ff" /></template>
                <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
                  <a-descriptions-item :label="t('video.device.info.accessProtocol')">
                    {{ getDictLabel(DictEnum.VIDEO_DEVICE_ACCESS_PROTOCOL, videoNodeDetail?.accessProtocol) || displayVal(videoNodeDetail.accessProtocol) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.deviceName')">
                    {{ displayVal(videoNodeDetail.deviceName) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.customName')">
                    {{ displayVal(videoNodeDetail.customName) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.mediaIdentification')">
                    {{ displayVal(videoNodeDetail.mediaIdentification) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.manufacturer')">
                    {{ displayVal(videoNodeDetail.manufacturer) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.model')">
                    {{ displayVal(videoNodeDetail.model) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.firmware')">
                    {{ displayVal(videoNodeDetail.firmware) }}
                  </a-descriptions-item>
                </a-descriptions>
              </Card>
            </Col>
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.networkConfig')" class="config-card">
                <template #extra><GlobalOutlined style="color: #7c5cfc" /></template>
                <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
                  <a-descriptions-item :label="t('video.device.info.host')">
                    {{ displayVal(videoNodeDetail.host) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.port')">
                    {{ videoNodeDetail.port ?? '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.wanHost')">
                    {{ displayVal(videoNodeDetail.wanHost) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.lanHost')">
                    {{ displayVal(videoNodeDetail.lanHost) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.sdpHost')">
                    {{ displayVal(videoNodeDetail.sdpHost) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.localHost')">
                    {{ displayVal(videoNodeDetail.localHost) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.accessEndpoint')">
                    {{ displayVal(videoNodeDetail.accessEndpoint) }}
                  </a-descriptions-item>
                </a-descriptions>
              </Card>
            </Col>
          </Row>

          <!-- SIP 配置 + 系统信息 -->
          <Row :gutter="16" class="config-row">
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.sipConfig')" class="config-card">
                <template #extra><ApiOutlined style="color: #13deb9" /></template>
                <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
                  <a-descriptions-item :label="t('video.device.info.transport')">
                    {{ getDictLabel(DictEnum.VIDEO_DEVICE_TRANSPORT, videoNodeDetail?.transport) || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.streamMode')">
                    {{ getDictLabel(DictEnum.VIDEO_DEVICE_STREAM_MODE, videoNodeDetail?.streamMode) || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.expires')">
                    {{ videoNodeDetail.expires != null ? `${videoNodeDetail.expires} s` : '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.keepaliveInterval')">
                    {{ videoNodeDetail.keepaliveInterval != null ? `${videoNodeDetail.keepaliveInterval} s` : '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.keepaliveTimeoutCount')">
                    {{ videoNodeDetail.keepaliveTimeoutCount ?? '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.authType')">
                    {{ getDictLabel(DictEnum.VIDEO_GB28181_SIP_REGISTER_WAY, videoNodeDetail?.authType) || displayVal(videoNodeDetail.authType) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.authSecret')">
                    <span class="secret-field">
                      <span>{{ secretVisible ? displayVal(videoNodeDetail.authSecret) : '******' }}</span>
                      <EyeInvisibleOutlined v-if="secretVisible" class="secret-eye" @click="secretVisible = false" />
                      <EyeOutlined v-else class="secret-eye" @click="secretVisible = true" />
                    </span>
                  </a-descriptions-item>
                </a-descriptions>
              </Card>
            </Col>
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.systemInfo')" class="config-card">
                <template #extra><SettingOutlined style="color: #ffae1f" /></template>
                <a-descriptions :column="1" :labelStyle="labelStyle" :contentStyle="contentStyle">
                  <a-descriptions-item :label="t('video.device.info.ability')">
                    {{ displayVal(videoNodeDetail.ability) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.channelCount')">
                    {{ videoNodeDetail.channelCount ?? '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.registerTime')">
                    {{ displayVal(videoNodeDetail.registerTime) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.lastKeepaliveTime')">
                    {{ displayVal(videoNodeDetail.lastKeepaliveTime) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.remark')">
                    {{ displayVal(videoNodeDetail.remark) }}
                  </a-descriptions-item>
                  <a-descriptions-item :label="t('video.device.info.createdTime')">
                    {{ displayVal(videoNodeDetail.createdTime) }}
                  </a-descriptions-item>
                </a-descriptions>
              </Card>
            </Col>
          </Row>

          <!-- 协议配置 + 扩展参数（JSON 字段自动展开，后端加字段前端无需改） -->
          <Row :gutter="16" class="config-row">
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.protocolConfig')" class="config-card">
                <template #extra><ApiOutlined style="color: #a8dc3e" /></template>
                <JsonAutoDescriptions
                  :data="videoNodeDetail.protocolConfig"
                  i18n-prefix="video.device.info.protocolConfigFields"
                  :label-style="labelStyle"
                  :content-style="contentStyle"
                />
              </Card>
            </Col>
            <Col :xs="24" :lg="12">
              <Card :bordered="false" :title="t('video.device.info.extendParams')" class="config-card">
                <template #extra><SettingOutlined style="color: #ff7a45" /></template>
                <JsonAutoDescriptions
                  :data="videoNodeDetail.extendParams"
                  i18n-prefix="video.device.info.extendParamsFields"
                  :label-style="labelStyle"
                  :content-style="contentStyle"
                />
              </Card>
            </Col>
          </Row>
        </a-tab-pane>

        <!-- 通道列表 Tab -->
        <a-tab-pane key="channel" :tab="channelTabTitle">
          <div class="channel-toolbar">
            <a-space>
              <a-input-search
                v-model:value="channelSearch.channelName"
                :placeholder="t('video.device.channel.channelName')"
                style="width: 200px"
                allow-clear
                @search="loadChannels"
              />
              <a-select
                v-model:value="channelSearch.channelType"
                :placeholder="t('video.device.channel.channelType')"
                style="width: 160px"
                allow-clear
                @change="loadChannels"
              >
                <a-select-option v-for="opt in channelTypeOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </a-select-option>
              </a-select>
              <a-button @click="resetChannelSearch">
                <template #icon><ReloadOutlined /></template>
              </a-button>
            </a-space>
            <a-space>
              <a-button
                type="primary"
                @click="handleAddChannel"
                v-hasAnyPermission="['video:device:channel:add']"
              >
                <template #icon><PlusOutlined /></template>
                {{ t('common.title.add') }}
              </a-button>
            </a-space>
          </div>

          <!-- 通道卡片列表 -->
          <a-spin :spinning="channelLoading">
            <div v-if="channelList.length > 0" class="channel-grid">
              <div v-for="ch in channelList" :key="ch.id" class="channel-card-wrapper">
                <Card :bordered="false" class="channel-card" :class="{ offline: !ch.onlineStatus }">
                  <div class="channel-card-header">
                    <div class="channel-name-row">
                      <span class="channel-name">{{ ch.channelName || ch.channelIdentification }}</span>
                      <Tag :color="ch.onlineStatus ? 'success' : 'default'" size="small">
                        {{ ch.onlineStatus ? t('video.device.info.online') : t('video.device.info.offline') }}
                      </Tag>
                    </div>
                    <a-dropdown>
                      <EllipsisOutlined class="channel-action-icon" />
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleViewChannel(ch)">
                            <SearchOutlined /> {{ t('common.title.view') }}
                          </a-menu-item>
                          <a-menu-item
                            @click="handleEditChannel(ch)"
                            v-hasAnyPermission="['video:device:channel:edit']"
                          >
                            <EditOutlined /> {{ t('common.title.edit') }}
                          </a-menu-item>
                          <a-menu-item
                            danger
                            @click="handleDeleteChannel(ch)"
                            v-hasAnyPermission="['video:device:channel:delete']"
                          >
                            <DeleteOutlined /> {{ t('common.title.delete') }}
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                  <div class="channel-card-body">
                    <div class="channel-field">
                      <span class="field-label">{{ t('video.device.channel.channelIdentification') }}</span>
                      <span class="field-value">{{ ch.channelIdentification }}</span>
                    </div>
                    <div class="channel-field">
                      <span class="field-label">{{ t('video.device.channel.channelType') }}</span>
                      <span class="field-value">
                        {{ getDictLabel(DictEnum.VIDEO_DEVICE_CHANNEL_TYPE, ch.channelType) || ch.channelType || '-' }}
                      </span>
                    </div>
                    <div class="channel-field">
                      <span class="field-label">{{ t('video.device.channel.ptzType') }}</span>
                      <span class="field-value">
                        {{ getDictLabel(DictEnum.VIDEO_DEVICE_CHANNEL_PTZ_TYPE, ch.ptzType) || ch.ptzType || '-' }}
                      </span>
                    </div>
                    <div class="channel-field">
                      <span class="field-label">{{ t('video.device.channel.manufacturer') }}</span>
                      <span class="field-value">{{ ch.manufacturer || '-' }}</span>
                    </div>
                  </div>
                  <div class="channel-card-footer">
                    <span v-if="ch.hasAudio" class="channel-tag"><SoundOutlined /> {{ t('video.device.channel.hasAudio') }}</span>
                    <span v-if="ch.ptzCapability" class="channel-tag"><AimOutlined /> PTZ</span>
                    <span v-if="ch.talkCapability" class="channel-tag"><AudioOutlined /> {{ t('video.device.channel.talkCapability') }}</span>
                  </div>
                </Card>
              </div>
            </div>
            <a-empty v-else :description="t('common.noData')" />
          </a-spin>

          <!-- 分页 -->
          <div v-if="channelTotal > 0" class="channel-pagination">
            <a-pagination
              v-model:current="channelPage.current"
              v-model:pageSize="channelPage.size"
              :total="channelTotal"
              show-size-changer
              show-quick-jumper
              :show-total="(total) => `${total} ${t('video.device.info.channel')}`"
              @change="loadChannels"
              @showSizeChange="loadChannels"
            />
          </div>
        </a-tab-pane>
      </a-tabs>
    </Card>

    <EditModal @register="registerDeviceModal" @success="load" />
    <ChannelEditModal @register="registerChannelModal" @success="loadChannels" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { detail } from '/@/api/video/device/info';
  import { page as channelPageApi, remove as channelRemove } from '/@/api/video/device/channel';
  import { PageWrapper } from '/@/components/Page';
  import JsonAutoDescriptions from '/@/components/JsonAutoDescriptions';
  import {
    EditOutlined,
    VideoCameraOutlined,
    GlobalOutlined,
    KeyOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    SwapOutlined,
    PlayCircleOutlined,
    InfoCircleOutlined,
    ApiOutlined,
    SettingOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined,
    DeleteOutlined,
    EllipsisOutlined,
    SoundOutlined,
    AimOutlined,
    AudioOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import { DictEnum, ActionEnum } from '/@/enums/commonEnum';
  import { Card, Tag, Row, Col } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';
  import { asyncFindDictList } from '/@/api/thinglinks/common/general';
  import EditModal from './Edit.vue';
  import ChannelEditModal from '../channel/Edit.vue';

  const { t } = useI18n();
  const { currentRoute } = useRouter();
  const { getDictLabel } = useDict();
  const { createConfirm, createMessage } = useMessage();
  const [registerDeviceModal, { openModal: openDeviceModal }] = useModal();
  const [registerChannelModal, { openModal: openChannelModal }] = useModal();

  const labelStyle = { width: '160px', fontWeight: 500, color: '#666' };
  const contentStyle = { color: '#333' };

  let videoNodeDetail = reactive<Record<string, any>>({});
  const id = ref('');
  const activeTab = ref('info');
  const secretVisible = ref(false);

  // ========== 通道相关 ==========
  const channelList = ref<any[]>([]);
  const channelLoading = ref(false);
  const channelTotal = ref(0);
  const channelPage = reactive({ current: 1, size: 12 });
  const channelSearch = reactive({ channelName: '', channelType: undefined as string | undefined });
  const channelTypeOptions = ref<{ label: string; value: string }[]>([]);

  const channelTabTitle = computed(() => {
    const count = videoNodeDetail.channelCount ?? channelTotal.value;
    return `${t('video.device.info.channel')}${count > 0 ? ` (${count})` : ''}`;
  });

  onMounted(async () => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    await load();
    loadChannelTypeOptions();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(videoNodeDetail, res);
  };

  const loadChannels = async () => {
    channelLoading.value = true;
    try {
      const params: any = {
        current: channelPage.current,
        size: channelPage.size,
        model: {
          deviceIdentification: videoNodeDetail.deviceIdentification,
          ...(channelSearch.channelName ? { channelName: channelSearch.channelName } : {}),
          ...(channelSearch.channelType != null ? { channelType: channelSearch.channelType } : {}),
        },
      };
      const res = await channelPageApi(params);
      channelList.value = res?.records || [];
      channelTotal.value = Number(res?.total) || 0;
    } finally {
      channelLoading.value = false;
    }
  };

  const loadChannelTypeOptions = async () => {
    try {
      const res = await asyncFindDictList({ type: DictEnum.VIDEO_DEVICE_CHANNEL_TYPE });
      const list = res?.data || res || [];
      channelTypeOptions.value = (Array.isArray(list) ? list : []).map((d: any) => ({
        label: d.label || d.name || '',
        value: d.value ?? d.code ?? '',
      }));
    } catch (_) {}
  };

  const resetChannelSearch = () => {
    channelSearch.channelName = '';
    channelSearch.channelType = undefined;
    channelPage.current = 1;
    loadChannels();
  };

  const onTabChange = (key: string) => {
    if (key === 'channel' && channelList.value.length === 0) {
      loadChannels();
    }
  };

  function handleEdit() {
    openDeviceModal(true, { record: videoNodeDetail, type: ActionEnum.EDIT });
  }

  function handleAddChannel() {
    // channel Edit.vue 仅在非 ADD 模式赋值 record，ADD 时需通过 COPY 模式预填 deviceIdentification
    openChannelModal(true, {
      type: ActionEnum.COPY,
      record: { deviceIdentification: videoNodeDetail.deviceIdentification },
    });
  }

  function handleViewChannel(ch: any) {
    openChannelModal(true, { record: ch, type: ActionEnum.VIEW });
  }

  function handleEditChannel(ch: any) {
    openChannelModal(true, { record: ch, type: ActionEnum.EDIT });
  }

  function handleDeleteChannel(ch: any) {
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async () => {
        await channelRemove([ch.id]);
        createMessage.success(t('common.tips.deleteSuccess'));
        loadChannels();
        load(); // 刷新 channelCount
      },
    });
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

  // ========== 顶部 Header ==========
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

    .device-icon {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: linear-gradient(135deg, @primary, #3a66e8);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .device-meta {
      flex: 1;
    }

    .device-name {
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

    .header-meta {
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

  // ========== 指标卡片 ==========
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

      &.status {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: @danger;

        &.online {
          background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
          color: #0bb783;
        }
      }

      &.channel {
        background: linear-gradient(135deg, #d4e7fe 0%, #a6c8ff 100%);
        color: @primary;
      }

      &.transport {
        background: linear-gradient(135deg, #e8dbff 0%, #c5a8ff 100%);
        color: @purple;
      }

      &.stream {
        background: linear-gradient(135deg, #c3f5e5 0%, #7de8c5 100%);
        color: #0bb783;
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
      font-size: 22px;
      font-weight: 700;
      color: #2a3547;
      margin-bottom: 2px;
      letter-spacing: -0.5px;
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

  // ========== Tab 卡片 ==========
  .tab-card {
    :deep(.ant-card-body) {
      padding: 16px 24px;
    }

    :deep(.ant-tabs-nav) {
      margin-bottom: 20px;
    }
  }

  // ========== 配置卡片 ==========
  .config-row {
    margin-bottom: 16px;
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

  // ========== 通道 Tab 样式 ==========
  .channel-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;
  }

  .channel-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }

  .channel-card-wrapper {
    display: flex;
  }

  .channel-card {
    width: 100%;
    border: 1px solid #e8ecf1;
    border-radius: @radius-md !important;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: @shadow-hover;
      transform: translateY(-2px);
      border-color: @primary;
    }

    &.offline {
      opacity: 0.75;
    }

    :deep(.ant-card-body) {
      padding: 18px 20px;
    }

    .channel-card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 14px;
    }

    .channel-name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;
    }

    .channel-name {
      font-size: 15px;
      font-weight: 600;
      color: #2a3547;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .channel-action-icon {
      font-size: 18px;
      color: #8c97a5;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        background: #f0f2f5;
        color: @primary;
      }
    }

    .channel-card-body {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px 16px;
    }

    .channel-field {
      display: flex;
      flex-direction: column;

      .field-label {
        font-size: 12px;
        color: #8c97a5;
        margin-bottom: 2px;
      }

      .field-value {
        font-size: 13px;
        color: #2a3547;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .channel-card-footer {
      display: flex;
      gap: 8px;
      margin-top: 14px;
      padding-top: 12px;
      border-top: 1px solid #f0f2f5;
      flex-wrap: wrap;
    }

    .channel-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 10px;
      font-size: 12px;
      color: @primary;
      background: #eef2ff;
      border-radius: 20px;
    }
  }

  .channel-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #f0f2f5;
  }
</style>
