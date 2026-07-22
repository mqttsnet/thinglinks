<template>
  <PageWrapper contentFullHeight class="ds-detail">
    <!-- ===== 顶部 Header ===== -->
    <EditModal @register="registerEditModal" @success="load" />

    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="ds-icon">
            <component :is="getSourceTypeSvg(detailData.sourceType)" />
          </div>
          <div class="ds-meta">
            <div class="ds-title">
              <span class="name-text">{{ detailData.dataSourceName || '-' }}</span>
              <Tag :color="detailData.enable ? 'success' : 'default'">
                {{
                  detailData.enable
                    ? t('iot.rule.integration.datasource.status.enabled')
                    : t('iot.rule.integration.datasource.status.disabled')
                }}
              </Tag>
              <Tag :color="getDirectionColor(detailData.direction)">
                {{ getDictLabel('BRIDGE_DIRECTION', detailData.direction, '-') }}
              </Tag>
              <Tag color="blue">
                {{ getDictLabel('BRIDGE_DATA_SOURCE_TYPE', detailData.sourceType, '-') }}
              </Tag>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t('iot.rule.integration.datasource.dataSourceCode') }}：
                {{ detailData.dataSourceCode || '-' }}
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ detailData.updatedTime || detailData.createdTime || '-' }}
              </span>
              <a-divider type="vertical" />
              <span v-if="detailData.lastHealthCheckTime">
                <SafetyCertificateOutlined />
                {{ t('iot.rule.integration.datasource.detail.healthLastCheck') }}：
                {{ detailData.lastHealthCheckTime }}
              </span>
              <span v-else class="hint">
                <SafetyCertificateOutlined />
                {{ t('iot.rule.integration.datasource.detail.healthNeverChecked') }}
              </span>
            </div>
          </div>
        </div>
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            {{ t('iot.rule.integration.datasource.detail.refresh') }}
          </a-button>
          <a-button
            :loading="testing"
            @click="handleTest"
            v-hasAnyPermission="['rule:integration:datasource:test']"
          >
            <template #icon><ThunderboltOutlined /></template>
            {{ t('iot.rule.integration.datasource.detail.runTest') }}
          </a-button>
          <a-button
            :type="detailData.enable ? 'default' : 'primary'"
            @click="handleToggle"
            v-hasAnyPermission="['rule:integration:datasource:toggle']"
          >
            <template #icon>
              <PauseCircleOutlined v-if="detailData.enable" />
              <PlayCircleOutlined v-else />
            </template>
            {{
              detailData.enable
                ? t('iot.rule.integration.datasource.action.disable')
                : t('iot.rule.integration.datasource.action.enable')
            }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['rule:integration:datasource:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('iot.rule.integration.datasource.detail.edit') }}
          </a-button>
          <a-button
            @click="goReferences"
            v-hasAnyPermission="['rule:integration:bridge:view']"
          >
            <template #icon><LinkOutlined /></template>
            {{ t('iot.rule.integration.datasource.detail.viewReferences') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- ===== 4 指标 ===== -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon proto"><ApiOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.datasource.detail.metric.sourceType') }}
            </div>
            <div class="metric-value">
              {{ getDictLabel('BRIDGE_DATA_SOURCE_TYPE', detailData.sourceType, '-') }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon dir"><SwapOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.datasource.detail.metric.direction') }}
            </div>
            <div class="metric-value">
              {{ getDictLabel('BRIDGE_DIRECTION', detailData.direction, '-') }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon health" :class="healthColorKey">
            <CheckCircleOutlined v-if="healthColorKey === 'ok'" />
            <ExclamationCircleOutlined v-else-if="healthColorKey === 'warn'" />
            <CloseCircleOutlined v-else-if="healthColorKey === 'down'" />
            <QuestionCircleOutlined v-else />
          </div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.datasource.detail.metric.health') }}
            </div>
            <div class="metric-value">
              {{ getDictLabel('BRIDGE_HEALTH_STATUS', detailData.healthStatus, '-') }}
            </div>
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
              {{ t('iot.rule.integration.datasource.detail.metric.enable') }}
            </div>
            <div class="metric-value">
              {{
                detailData.enable
                  ? t('iot.rule.integration.datasource.status.enabled')
                  : t('iot.rule.integration.datasource.status.disabled')
              }}
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- ===== 主体 Tabs ===== -->
    <Card :bordered="false" class="panel-card">
      <template #title>
        <span class="panel-title">
          {{ t('iot.rule.integration.datasource.detail.editScript') }}
        </span>
      </template>
      <a-tabs v-model:activeKey="activeTab" size="small">
        <a-tab-pane
          key="connection"
          :tab="t('iot.rule.integration.datasource.detail.tabs.connection')"
        >
          <a-alert
            type="info"
            :message="t('iot.rule.integration.datasource.detail.encryptedHint')"
            show-icon
            style="margin-bottom: 12px"
          />
          <JsonViewer :value="connectionJson" />
        </a-tab-pane>
        <a-tab-pane
          key="credential"
          :tab="t('iot.rule.integration.datasource.detail.tabs.credential')"
        >
          <a-alert
            type="warning"
            :message="t('iot.rule.integration.datasource.detail.encryptedHint')"
            show-icon
            style="margin-bottom: 12px"
          />
          <JsonViewer :value="credentialJson" :masked="true" />
        </a-tab-pane>
        <a-tab-pane key="policy" :tab="t('iot.rule.integration.datasource.detail.tabs.policy')">
          <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
            <a-descriptions-item :label="t('iot.rule.integration.datasource.serialization')">
              {{ detailData.serialization || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.defaultQos')">
              {{ detailData.defaultQos ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.defaultRateLimitQps')">
              {{ detailData.defaultRateLimitQps ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.defaultRetryMaxTimes')">
              {{ detailData.defaultRetryMaxTimes ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.defaultRetryBackoffMs')">
              {{ detailData.defaultRetryBackoffMs ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.defaultTimeoutMs')">
              {{ detailData.defaultTimeoutMs ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item
              :label="t('iot.rule.integration.datasource.defaultDeadLetterDataSourceId')"
              :span="3"
            >
              {{ detailData.defaultDeadLetterDataSourceId ?? '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
        <a-tab-pane key="extras" :tab="t('iot.rule.integration.datasource.detail.tabs.extras')">
          <JsonViewer :value="extendParamsJson" />
        </a-tab-pane>
        <a-tab-pane key="meta" :tab="t('iot.rule.integration.datasource.detail.tabs.meta')">
          <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
            <a-descriptions-item :label="t('iot.rule.integration.datasource.appId')">
              {{ getDictLabel('LINK_APPLICATION_SCENARIO', detailData.appId, '-') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.dataSourceCode')">
              {{ detailData.dataSourceCode || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.healthStatus')">
              {{ getDictLabel('BRIDGE_HEALTH_STATUS', detailData.healthStatus, '-') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.lastHealthCheckTime')">
              {{ detailData.lastHealthCheckTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.remark')" :span="3">
              {{ detailData.remark || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.createdTime')">
              {{ detailData.createdTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.updatedTime')">
              {{ detailData.updatedTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.datasource.createdBy')">
              {{ echoMapText(detailData, 'createdBy') }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
      </a-tabs>
    </Card>
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag, Tabs, Descriptions, Alert } from 'ant-design-vue';
  import {
    ApiOutlined,
    SwapOutlined,
    ClockCircleOutlined,
    NumberOutlined,
    SafetyCertificateOutlined,
    ReloadOutlined,
    ThunderboltOutlined,
    PauseCircleOutlined,
    PlayCircleOutlined,
    PoweroffOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    ExclamationCircleOutlined,
    QuestionCircleOutlined,
    EditOutlined,
    LinkOutlined,
  } from '@ant-design/icons-vue';
  import { detail, testConnection, changeStatus } from '/@/api/iot/rule/integration/dataSource';
  import { PageWrapper } from '/@/components/Page';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { getSourceTypeSvg } from '/@/components/iot/integration/svg';
  import JsonViewer from './components/JsonViewer.vue';
  import EditModal from './Edit.vue';
  import { echoMapText } from '/@/utils/echo';

  export default defineComponent({
    name: 'IntegrationDataSourceDetail',
    components: {
      Card,
      Row,
      Col,
      Tag,
      [Tabs.name as string]: Tabs,
      [Tabs.TabPane.name as string]: Tabs.TabPane,
      [Descriptions.name as string]: Descriptions,
      [Descriptions.Item.name as string]: Descriptions.Item,
      [Alert.name as string]: Alert,
      PageWrapper,
      JsonViewer,
      EditModal,
      ApiOutlined,
      SwapOutlined,
      ClockCircleOutlined,
      NumberOutlined,
      SafetyCertificateOutlined,
      ReloadOutlined,
      ThunderboltOutlined,
      PauseCircleOutlined,
      PlayCircleOutlined,
      PoweroffOutlined,
      CheckCircleOutlined,
      CloseCircleOutlined,
      ExclamationCircleOutlined,
      QuestionCircleOutlined,
      EditOutlined,
      LinkOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { currentRoute, push } = useRouter();
      const { createMessage } = useMessage();
      const { getDictLabel } = useDict();
      const [registerEditModal, { openModal: openEditModal }] = useModal();

      const id = ref('');
      const detailData = reactive<any>({});
      const testing = ref(false);
      const activeTab = ref<'connection' | 'credential' | 'policy' | 'extras' | 'meta'>('connection');

      const connectionJson = computed(() => detailData.connectionJson || '{}');
      const credentialJson = computed(() => detailData.credentialJson || '{}');
      const extendParamsJson = computed(() => detailData.extendParams || '{}');

      const healthColorKey = computed(() => {
        switch (detailData.healthStatus) {
          case 'HEALTHY':
            return 'ok';
          case 'DEGRADED':
            return 'warn';
          case 'DOWN':
            return 'down';
          default:
            return 'unknown';
        }
      });

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

      async function handleTest() {
        if (!id.value) return;
        testing.value = true;
        try {
          const ok = await testConnection(id.value);
          if (ok) {
            createMessage.success(t('iot.rule.integration.datasource.detail.testSuccess'));
          } else {
            createMessage.error(t('iot.rule.integration.datasource.detail.testFailed'));
          }
          await load();
        } catch (e: any) {
          createMessage.error(
            t('iot.rule.integration.datasource.detail.testFailed') + ': ' + (e?.message ?? ''),
          );
        } finally {
          testing.value = false;
        }
      }

      /**
       * 启停切换：启用前先调 testConnection 兜底（防止后端报错才发现连接不通）。
       * 禁用直接切。
       */
      async function handleToggle() {
        const target = !detailData.enable;
        // 即将启用 → 前置探活
        if (target) {
          testing.value = true;
          try {
            const ok = await testConnection(id.value);
            if (!ok) {
              createMessage.error(t('iot.rule.integration.datasource.detail.enableMustTestPass'));
              return;
            }
          } catch (e: any) {
            createMessage.error(
              t('iot.rule.integration.datasource.tips.testFailed') + ': ' + (e?.message ?? ''),
            );
            return;
          } finally {
            testing.value = false;
          }
        }
        try {
          await changeStatus(id.value, target);
          createMessage.success(
            target
              ? t('iot.rule.integration.datasource.detail.enableSuccess')
              : t('iot.rule.integration.datasource.detail.disableSuccess'),
          );
          await load();
        } catch (e: any) {
          createMessage.error(
            t('iot.rule.integration.datasource.detail.enableMustTestPass') +
              ': ' +
              (e?.message ?? ''),
          );
        }
      }

      function getDirectionColor(direction?: string): string {
        switch (direction) {
          case '10':
            return 'green';
          case '20':
            return 'blue';
          case '30':
            return 'orange';
          default:
            return 'default';
        }
      }

      /** 打开编辑弹窗（带回显） */
      function handleEdit() {
        openEditModal(true, { record: detailData, isUpdate: true, type: ActionEnum.EDIT });
      }

      /** 跳转到桥接规则列表，按 dataSourceId 过滤显示引用此数据源的规则 */
      function goReferences() {
        push({ path: '/integration/bridge', query: { dataSourceId: id.value } });
      }

      return {
        t,
        getDictLabel,
        echoMapText,
        getSourceTypeSvg,
        getDirectionColor,
        detailData,
        testing,
        activeTab,
        connectionJson,
        credentialJson,
        extendParamsJson,
        healthColorKey,
        registerEditModal,
        handleRefresh,
        handleTest,
        handleToggle,
        handleEdit,
        goReferences,
        load,
      };
    },
  });
</script>

<style lang="less" scoped>
  .ds-detail {
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

  .ds-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #eef2ff 0%, #e8f4fd 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.1);

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .ds-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .ds-title {
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

    .hint {
      color: #a0aec0;
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

    &.proto {
      background: rgba(93, 135, 255, 0.1);
      color: #5d87ff;
    }

    &.dir {
      background: rgba(73, 190, 255, 0.1);
      color: #49beff;
    }

    &.health {
      background: rgba(160, 174, 192, 0.12);
      color: #a0aec0;

      &.ok {
        background: rgba(19, 222, 185, 0.12);
        color: #13deb9;
      }

      &.warn {
        background: rgba(255, 174, 31, 0.12);
        color: #ffae1f;
      }

      &.down {
        background: rgba(250, 75, 75, 0.12);
        color: #fa4b4b;
      }
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

  .panel-card {
    margin: 0 16px 16px;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
      padding: 0 20px;
      min-height: 52px;
    }

    :deep(.ant-card-body) {
      padding: 16px 20px;
    }
  }

  .panel-title {
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
  }
</style>
