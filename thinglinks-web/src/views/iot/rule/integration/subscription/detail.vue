<template>
  <PageWrapper contentFullHeight class="sub-detail">
    <EditModal @register="registerEditModal" @success="load" />

    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="sub-icon">
            <ApiOutlined />
          </div>
          <div class="sub-meta">
            <div class="sub-title">
              <span class="name-text">{{ detailData.sourceName || '-' }}</span>
              <Tag :color="detailData.enable ? 'success' : 'default'">
                {{
                  detailData.enable
                    ? t('iot.rule.integration.subscription.status.enabled')
                    : t('iot.rule.integration.subscription.status.disabled')
                }}
              </Tag>
              <Tag color="blue">
                {{ getDictLabel('BRIDGE_TARGET_HANDLER', detailData.targetHandler, '-') }}
              </Tag>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t('iot.rule.integration.subscription.sourceCode') }}：
                {{ detailData.sourceCode || '-' }}
              </span>
              <a-divider type="vertical" />
              <span>
                <DatabaseOutlined />
                {{ t('iot.rule.integration.subscription.dataSourceId') }}：
                {{ detailData.dataSourceCode || detailData.dataSourceId || '-' }}
                <span v-if="detailData.dataSourceName" class="ds-name-secondary">
                  ({{ detailData.dataSourceName }})
                </span>
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ detailData.updatedTime || detailData.createdTime || '-' }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            {{ t('iot.rule.integration.subscription.detail.refresh') }}
          </a-button>
          <a-button
            :type="detailData.enable ? 'default' : 'primary'"
            @click="handleToggle"
            v-hasAnyPermission="['rule:integration:subscription:toggle']"
          >
            <template #icon>
              <PauseCircleOutlined v-if="detailData.enable" />
              <PlayCircleOutlined v-else />
            </template>
            {{
              detailData.enable
                ? t('iot.rule.integration.subscription.status.disabled')
                : t('iot.rule.integration.subscription.status.enabled')
            }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['rule:integration:subscription:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('iot.rule.integration.subscription.detail.edit') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon handler"><BranchesOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.subscription.detail.metric.handler') }}
            </div>
            <div class="metric-value">
              {{ getDictLabel('BRIDGE_TARGET_HANDLER', detailData.targetHandler, '-') }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon ds"><DatabaseOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.subscription.detail.metric.dataSource') }}
            </div>
            <div class="metric-value">
              {{ detailData.dataSourceCode || detailData.dataSourceId || '-' }}
            </div>
            <div v-if="detailData.dataSourceName" class="metric-sub">
              {{ detailData.dataSourceName }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon offset"><HistoryOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.subscription.detail.metric.offset') }}
            </div>
            <div class="metric-value">{{ detailData.lastConsumeOffset || '-' }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon enable" :class="{ on: detailData.enable }">
            <PoweroffOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.subscription.detail.metric.enable') }}
            </div>
            <div class="metric-value">
              {{
                detailData.enable
                  ? t('iot.rule.integration.subscription.status.enabled')
                  : t('iot.rule.integration.subscription.status.disabled')
              }}
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <Card :bordered="false" class="panel-card">
      <a-tabs v-model:activeKey="activeTab" size="small">
        <a-tab-pane
          key="mapping"
          :tab="t('iot.rule.integration.subscription.detail.tabs.mapping')"
        >
          <a-alert
            type="info"
            :message="t('iot.rule.integration.subscription.detail.mappingHint')"
            show-icon
            style="margin-bottom: 12px"
          />
          <JsonViewer :value="mappingJson" />
        </a-tab-pane>
        <a-tab-pane
          key="target"
          :tab="t('iot.rule.integration.subscription.detail.tabs.target')"
        >
          <a-descriptions bordered :column="{ xs: 1, sm: 2 }" size="small">
            <a-descriptions-item :label="t('iot.rule.integration.subscription.targetHandler')">
              {{ getDictLabel('BRIDGE_TARGET_HANDLER', detailData.targetHandler, '-') }}
            </a-descriptions-item>
            <a-descriptions-item
              :label="t('iot.rule.integration.subscription.targetProductIdentification')"
            >
              {{ detailData.targetProductIdentification || '-' }}
            </a-descriptions-item>
            <a-descriptions-item
              :label="t('iot.rule.integration.subscription.targetTopicTemplate')"
              :span="2"
            >
              <code>{{ detailData.targetTopicTemplate || '-' }}</code>
            </a-descriptions-item>
            <a-descriptions-item
              :label="t('iot.rule.integration.subscription.lastConsumeOffset')"
              :span="2"
            >
              {{ detailData.lastConsumeOffset || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
        <a-tab-pane
          key="meta"
          :tab="t('iot.rule.integration.subscription.detail.tabs.meta')"
        >
          <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
            <a-descriptions-item :label="t('iot.rule.integration.subscription.appId')">
              {{ getDictLabel('LINK_APPLICATION_SCENARIO', detailData.appId, '-') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.subscription.sourceCode')">
              {{ detailData.sourceCode || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.subscription.dataSourceId')">
              <a-button
                type="link"
                size="small"
                @click="goDataSource"
                v-hasAnyPermission="['rule:integration:datasource:view']"
              >
                {{ detailData.dataSourceCode || detailData.dataSourceId || '-' }}
                <span v-if="detailData.dataSourceName" class="ds-name-secondary">
                  ({{ detailData.dataSourceName }})
                </span>
                <RightOutlined v-if="detailData.dataSourceId" />
              </a-button>
            </a-descriptions-item>
            <a-descriptions-item
              :label="t('iot.rule.integration.subscription.extendParams')"
              :span="3"
            >
              {{ detailData.extendParams || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.subscription.remark')" :span="3">
              {{ detailData.remark || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.createdTime')">
              {{ detailData.createdTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.updatedTime')">
              {{ detailData.updatedTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.subscription.createdBy')">
              {{ echoMapText(detailData, 'createdBy') }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
      </a-tabs>
    </Card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag, Tabs, Descriptions, Alert } from 'ant-design-vue';
  import {
    ApiOutlined,
    NumberOutlined,
    DatabaseOutlined,
    ClockCircleOutlined,
    BranchesOutlined,
    HistoryOutlined,
    PoweroffOutlined,
    ReloadOutlined,
    PauseCircleOutlined,
    PlayCircleOutlined,
    RightOutlined,
    EditOutlined,
  } from '@ant-design/icons-vue';
  import { detail, changeStatus } from '/@/api/iot/rule/integration/subscriptionSource';
  import { PageWrapper } from '/@/components/Page';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import JsonViewer from '../datasource/components/JsonViewer.vue';
  import EditModal from './Edit.vue';
  import { echoMapText } from '/@/utils/echo';

  defineOptions({ name: 'IntegrationSubscriptionDetail' });

  const { t } = useI18n();
  const { currentRoute, push } = useRouter();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();

  const id = ref('');
  const detailData = reactive<any>({});
  const activeTab = ref<'mapping' | 'target' | 'meta'>('mapping');

  const mappingJson = computed(() => detailData.mappingJson || '[]');

  const [registerEditModal, { openModal: openEditModal }] = useModal();

  function handleEdit() {
    openEditModal(true, { record: detailData, isUpdate: true });
  }

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = (params.id as string) ?? '';
    load();
  });

  async function load() {
    if (!id.value) return;
    const res: any = await detail(id.value);
    Object.assign(detailData, res ?? {});
  }

  async function handleRefresh() {
    await load();
  }

  async function handleToggle() {
    const target = !detailData.enable;
    try {
      await changeStatus(id.value, target);
      createMessage.success(
        target
          ? t('iot.rule.integration.subscription.detail.enableSuccess')
          : t('iot.rule.integration.subscription.detail.disableSuccess'),
      );
      await load();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.subscription.tips.enableSourceRequired') +
          ': ' +
          (e?.message ?? ''),
      );
    }
  }

  function goDataSource() {
    if (!detailData.dataSourceId) return;
    push({ path: '/integration/datasource/' + detailData.dataSourceId });
  }
</script>

<style lang="less" scoped>
  .sub-detail {
    background: #f6f8fb;
  }

  .header-card,
  .panel-card,
  .metric-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  }

  .header-card {
    margin: 16px 16px 0;
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .sub-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.25);
  }

  .sub-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .sub-title {
    display: flex;
    align-items: center;
    gap: 8px;

    .name-text {
      font-size: 18px;
      font-weight: 600;
      color: #2a3547;
    }
  }

  .meta-line {
    color: #6b7280;
    font-size: 13px;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  .metric-row {
    margin: 16px;
  }

  .metric-card {
    :deep(.ant-card-body) {
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .metric-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;

    &.handler {
      background: rgba(93, 135, 255, 0.1);
      color: #5d87ff;
    }

    &.ds {
      background: rgba(73, 190, 255, 0.1);
      color: #49beff;
    }

    &.offset {
      background: rgba(255, 174, 31, 0.1);
      color: #ffae1f;
    }

    &.enable {
      background: rgba(160, 174, 192, 0.12);
      color: #a0aec0;

      &.on {
        background: rgba(19, 222, 185, 0.12);
        color: #13deb9;
      }
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    color: #6b7280;
    font-size: 13px;
  }

  .metric-value {
    color: #2a3547;
    font-size: 15px;
    font-weight: 600;
    margin-top: 2px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  .metric-sub {
    color: #8c97a5;
    font-size: 12px;
    margin-top: 2px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  .ds-name-secondary {
    color: #8c97a5;
    font-size: 12px;
    margin-left: 4px;
  }

  .panel-card {
    margin: 0 16px 16px;

    :deep(.ant-card-body) {
      padding: 16px 20px;
    }
  }
</style>
