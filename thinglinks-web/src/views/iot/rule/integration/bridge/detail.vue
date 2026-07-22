<template>
  <PageWrapper contentFullHeight class="bridge-detail">
    <EditModal @register="registerEditModal" @success="load" />
    <TestSinkModal @register="registerTestModal" />
    <TraceDetailDrawer @register="registerTraceDrawer" @replayed="loadRecentLogs" />

    <!-- ===== 顶部 Header ===== -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="rule-icon">
            <DataBridgeSvg />
          </div>
          <div class="rule-meta">
            <div class="rule-title">
              <span class="name-text">{{ detailData.ruleName || '-' }}</span>
              <Tag :color="detailData.enable ? 'success' : 'default'">
                {{
                  detailData.enable
                    ? t('iot.rule.integration.bridge.status.enabled')
                    : t('iot.rule.integration.bridge.status.disabled')
                }}
              </Tag>
              <Tag :color="getDirectionColor(detailData.direction)">
                {{ getDictLabel('BRIDGE_DIRECTION', detailData.direction, '-') }}
              </Tag>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t('iot.rule.integration.bridge.ruleCode') }}：{{ detailData.ruleCode || '-' }}
              </span>
              <a-divider type="vertical" />
              <span>
                <DatabaseOutlined />
                {{ t('iot.rule.integration.bridge.dataSourceId') }}：
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
            {{ t('iot.rule.integration.bridge.detail.refresh') }}
          </a-button>
          <a-button
            @click="handleTest"
            v-hasAnyPermission="['rule:integration:bridge:test']"
          >
            <template #icon><ThunderboltOutlined /></template>
            {{ t('iot.rule.integration.bridge.detail.runTest') }}
          </a-button>
          <a-button
            :type="detailData.enable ? 'default' : 'primary'"
            @click="handleToggle"
            v-hasAnyPermission="
              detailData.enable
                ? ['rule:integration:bridge:disable']
                : ['rule:integration:bridge:enable']
            "
          >
            <template #icon>
              <PauseCircleOutlined v-if="detailData.enable" />
              <PlayCircleOutlined v-else />
            </template>
            {{
              detailData.enable
                ? t('iot.rule.integration.bridge.action.disable')
                : t('iot.rule.integration.bridge.action.enable')
            }}
          </a-button>
          <a-button
            type="primary"
            @click="handleEdit"
            v-hasAnyPermission="['rule:integration:bridge:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('iot.rule.integration.bridge.detail.edit') }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- ===== 4 指标 ===== -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon dir"><SwapOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.bridge.detail.metric.direction') }}
            </div>
            <div class="metric-value">
              {{ getDictLabel('BRIDGE_DIRECTION', detailData.direction, '-') }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.rule.integration.bridge.ruleName') }}</span>
              <span class="sub-val">{{ detailData.ruleName || '-' }}</span>
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon ds"><DatabaseOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.bridge.detail.metric.dataSource') }}
            </div>
            <div class="metric-value">
              {{ detailData.dataSourceCode || detailData.dataSourceId || '-' }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.rule.integration.bridge.detail.metric.dataSourceName') }}</span>
              <span class="sub-val">{{ detailData.dataSourceName || '-' }}</span>
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon prio"><RocketOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">
              {{ t('iot.rule.integration.bridge.detail.metric.priority') }}
            </div>
            <div class="metric-value">{{ detailData.priority ?? '-' }}</div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.rule.integration.bridge.ruleCode') }}</span>
              <span class="sub-val">{{ detailData.ruleCode || '-' }}</span>
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
              {{ t('iot.rule.integration.bridge.detail.metric.enable') }}
            </div>
            <div class="metric-value">
              {{
                detailData.enable
                  ? t('iot.rule.integration.bridge.status.enabled')
                  : t('iot.rule.integration.bridge.status.disabled')
              }}
            </div>
            <div class="metric-sub">
              <span class="sub-key">{{ t('iot.rule.integration.bridge.updatedTime') }}</span>
              <span class="sub-val">{{ detailData.updatedTime || detailData.createdTime || '-' }}</span>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- ===== Tabs 主体 ===== -->
    <Card :bordered="false" class="panel-card">
      <a-tabs v-model:activeKey="activeTab" size="small">
        <!-- 匹配条件 -->
        <a-tab-pane
          key="match"
          :tab="t('iot.rule.integration.bridge.detail.tabs.match')"
        >
          <JsonViewer :value="matchConfigJson" />
        </a-tab-pane>
        <!-- 动作配置 -->
        <a-tab-pane
          key="action"
          :tab="t('iot.rule.integration.bridge.detail.tabs.action')"
        >
          <a-alert
            type="warning"
            :message="t('iot.rule.integration.bridge.detail.encryptedHint')"
            show-icon
            style="margin-bottom: 12px"
          />
          <JsonViewer :value="actionConfigJson" :masked="true" />
        </a-tab-pane>
        <!-- 流控覆盖 -->
        <a-tab-pane
          key="override"
          :tab="t('iot.rule.integration.bridge.detail.tabs.override')"
        >
          <a-alert
            type="info"
            :message="t('iot.rule.integration.bridge.detail.overrideHint')"
            show-icon
            style="margin-bottom: 12px"
          />
          <table class="override-table">
            <thead>
              <tr>
                <th>{{ t('iot.rule.integration.bridge.detail.field') }}</th>
                <th>{{ t('iot.rule.integration.bridge.detail.ruleValue') }}</th>
                <th>{{ t('iot.rule.integration.bridge.detail.dsDefault') }}</th>
                <th>{{ t('iot.rule.integration.bridge.detail.actualEffective') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.qos') }}</td>
                <td><span :class="{ inherit: detailData.qos == null }">{{ valOrDash(detailData.qos) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultQos) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.qos, 'defaultQos')) }}</td>
              </tr>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.rateLimitQps') }}</td>
                <td><span :class="{ inherit: detailData.rateLimitQps == null }">{{ valOrDash(detailData.rateLimitQps) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultRateLimitQps) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.rateLimitQps, 'defaultRateLimitQps')) }}</td>
              </tr>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.retryMaxTimes') }}</td>
                <td><span :class="{ inherit: detailData.retryMaxTimes == null }">{{ valOrDash(detailData.retryMaxTimes) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultRetryMaxTimes) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.retryMaxTimes, 'defaultRetryMaxTimes')) }}</td>
              </tr>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.retryBackoffMs') }}</td>
                <td><span :class="{ inherit: detailData.retryBackoffMs == null }">{{ valOrDash(detailData.retryBackoffMs) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultRetryBackoffMs) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.retryBackoffMs, 'defaultRetryBackoffMs')) }}</td>
              </tr>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.timeoutMs') }}</td>
                <td><span :class="{ inherit: detailData.timeoutMs == null }">{{ valOrDash(detailData.timeoutMs) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultTimeoutMs) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.timeoutMs, 'defaultTimeoutMs')) }}</td>
              </tr>
              <tr>
                <td>{{ t('iot.rule.integration.bridge.deadLetterDataSourceId') }}</td>
                <td><span :class="{ inherit: detailData.deadLetterDataSourceId == null }">{{ valOrDash(detailData.deadLetterDataSourceId) }}</span></td>
                <td class="ds">{{ valOrDash(dsFallback.defaultDeadLetterDataSourceId) }}</td>
                <td class="effective">{{ valOrDash(effective(detailData.deadLetterDataSourceId, 'defaultDeadLetterDataSourceId')) }}</td>
              </tr>
            </tbody>
          </table>
        </a-tab-pane>
        <!-- 24h 统计：懒加载（切到 Tab 才挂载，避免进 detail 就预拉一段 trace）-->
        <a-tab-pane key="stats" :tab="t('iot.rule.integration.bridge.detail.tabs.stats')">
          <BridgeStatsLineChart v-if="id && activeTab === 'stats'" :bridgeRuleId="id" />
        </a-tab-pane>
        <!-- 最近日志 -->
        <a-tab-pane key="log" :tab="t('iot.rule.integration.bridge.detail.tabs.log')">
          <div class="log-toolbar">
            <a-button size="small" @click="loadRecentLogs" :loading="recentLogsLoading">
              <template #icon><ReloadOutlined /></template>
              {{ t('iot.rule.integration.bridge.detail.refresh') }}
            </a-button>
            <a-button
              type="link"
              @click="goLogList"
              v-hasAnyPermission="['rule:integration:log:view']"
            >
              {{ t('iot.rule.integration.bridge.detail.viewAllLogs') }}
              <RightOutlined />
            </a-button>
          </div>
          <a-empty
            v-if="!recentLogsLoading && recentLogs.length === 0"
            :description="t('iot.rule.integration.bridge.detail.logEmpty')"
            style="padding: 60px 0"
          />
          <a-list
            v-else
            :dataSource="recentLogs"
            size="small"
            :loading="recentLogsLoading"
          >
            <template #renderItem="{ item }">
              <a-list-item
                class="log-list-item"
                @click="openTrace(item.traceId)"
                v-hasAnyPermission="['rule:integration:log:view']"
              >
                <div class="log-row">
                  <a-tag :color="getStatusColor(item.status)" class="log-status">
                    {{ getDictLabel('BRIDGE_MESSAGE_STATUS', item.status, '-') }}
                  </a-tag>
                  <div class="log-main">
                    <div class="log-title">
                      {{ item.deviceIdentification || '-' }} · {{ item.actionType || '-' }}
                    </div>
                    <div class="log-sub">
                      {{ item.startTime }} · {{ item.totalLatencyMs ?? 0 }} ms
                    </div>
                  </div>
                  <RightOutlined class="log-arrow" />
                </div>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
        <!-- 元数据 -->
        <a-tab-pane key="meta" :tab="t('iot.rule.integration.bridge.detail.tabs.meta')">
          <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
            <a-descriptions-item :label="t('iot.rule.integration.bridge.appId')">
              {{ getDictLabel('LINK_APPLICATION_SCENARIO', detailData.appId, '-') }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.bridge.ruleCode')">
              {{ detailData.ruleCode || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.bridge.priority')">
              {{ detailData.priority ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.bridge.remark')" :span="3">
              {{ detailData.remark || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.createdTime')">
              {{ detailData.createdTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('thinglinks.common.updatedTime')">
              {{ detailData.updatedTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('iot.rule.integration.bridge.createdBy')">
              {{ echoMapText(detailData, 'createdBy') }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
      </a-tabs>
    </Card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag, Tabs, Descriptions, Alert, Empty } from 'ant-design-vue';
  import {
    SwapOutlined,
    DatabaseOutlined,
    NumberOutlined,
    ClockCircleOutlined,
    RocketOutlined,
    PoweroffOutlined,
    ReloadOutlined,
    ThunderboltOutlined,
    PauseCircleOutlined,
    PlayCircleOutlined,
    EditOutlined,
    RightOutlined,
  } from '@ant-design/icons-vue';
  import { detail, changeStatus } from '/@/api/iot/rule/integration/dataBridge';
  import { detail as dataSourceDetail } from '/@/api/iot/rule/integration/dataSource';
  import { page as pageTrace } from '/@/api/iot/rule/integration/bridgeExecutionTrace';
  import { PageWrapper } from '/@/components/Page';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { DataBridgeSvg } from '/@/components/iot/integration/svg';
  import JsonViewer from '../datasource/components/JsonViewer.vue';
  import EditModal from './Edit.vue';
  import TestSinkModal from './TestSinkModal.vue';
  import TraceDetailDrawer from '../log/components/TraceDetailDrawer.vue';
  import BridgeStatsLineChart from './components/BridgeStatsLineChart.vue';
  import { echoMapText } from '/@/utils/echo';

  defineOptions({ name: 'IntegrationBridgeDetail' });

  const { t } = useI18n();
  const { currentRoute, push } = useRouter();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();

  const id = ref('');
  const detailData = reactive<any>({});
  const activeTab = ref<'match' | 'action' | 'override' | 'stats' | 'log' | 'meta'>('match');
  const recentLogs = ref<any[]>([]);
  const recentLogsLoading = ref(false);

  /** 关联数据源 default_* 兜底值（流控覆盖 Tab 显示"实际生效"用） */
  const dsFallback = reactive<{
    defaultQos: any;
    defaultRateLimitQps: any;
    defaultRetryMaxTimes: any;
    defaultRetryBackoffMs: any;
    defaultTimeoutMs: any;
    defaultDeadLetterDataSourceId: any;
  }>({
    defaultQos: null,
    defaultRateLimitQps: null,
    defaultRetryMaxTimes: null,
    defaultRetryBackoffMs: null,
    defaultTimeoutMs: null,
    defaultDeadLetterDataSourceId: null,
  });

  /**
   * 计算实际生效值：rule 字段 NOT NULL → 用 rule；NULL → 用 dataSource default_*。
   */
  function effective(ruleField: any, fallbackKey: keyof typeof dsFallback): any {
    if (ruleField !== null && ruleField !== undefined) return ruleField;
    return dsFallback[fallbackKey];
  }

  function valOrDash(v: any): string {
    if (v === null || v === undefined || v === '') return '-';
    return String(v);
  }

  const matchConfigJson = computed(() => detailData.matchConfigJson || '{}');
  const actionConfigJson = computed(() => detailData.actionConfigJson || '{}');

  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerTestModal, { openModal: openTestModal }] = useModal();
  const [registerTraceDrawer, { openDrawer: openTraceDrawer }] = useDrawer();

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = (params.id as string) ?? '';
    load();
  });

  /** 切到"最近日志" Tab 时自动拉数据（首次） */
  watch(activeTab, (val) => {
    if (val === 'log' && recentLogs.value.length === 0) {
      loadRecentLogs();
    }
  });

  async function load() {
    if (!id.value) return;
    const res: any = await detail(id.value);
    Object.assign(detailData, res ?? {});
    // 拉关联数据源 default_* 用于流控覆盖 Tab 显示"实际生效值"
    if (res?.dataSourceId) {
      try {
        const ds: any = await dataSourceDetail(String(res.dataSourceId));
        dsFallback.defaultQos = ds?.defaultQos ?? null;
        dsFallback.defaultRateLimitQps = ds?.defaultRateLimitQps ?? null;
        dsFallback.defaultRetryMaxTimes = ds?.defaultRetryMaxTimes ?? null;
        dsFallback.defaultRetryBackoffMs = ds?.defaultRetryBackoffMs ?? null;
        dsFallback.defaultTimeoutMs = ds?.defaultTimeoutMs ?? null;
        dsFallback.defaultDeadLetterDataSourceId = ds?.defaultDeadLetterDataSourceId ?? null;
      } catch (e) {
        // 数据源拉取失败仅影响"沿用默认"显示，不阻断详情页
      }
    }
  }

  /** 拉取该规则最近 5 条 trace（嵌入小列表，详情走侧拉抽屉）*/
  async function loadRecentLogs() {
    if (!id.value) return;
    recentLogsLoading.value = true;
    try {
      const res: any = await pageTrace({
        size: 5,
        current: 1,
        model: { bridgeRuleId: id.value },
      } as any);
      recentLogs.value = res?.records ?? [];
    } finally {
      recentLogsLoading.value = false;
    }
  }

  async function handleRefresh() {
    await load();
    if (activeTab.value === 'log') await loadRecentLogs();
  }

  function handleTest() {
    openTestModal(true, { record: detailData });
  }

  function handleEdit() {
    openEditModal(true, { record: detailData, isUpdate: true });
  }

  function openTrace(traceId: string) {
    openTraceDrawer(true, { traceId });
  }

  function getStatusColor(status?: string): string {
    switch (status) {
      case '00':
        return 'success';
      case '01':
        return 'error';
      case '02':
        return 'warning';
      case '03':
        return 'magenta';
      default:
        return 'default';
    }
  }

  async function handleToggle() {
    const target = !detailData.enable;
    try {
      await changeStatus(id.value, target);
      createMessage.success(
        target
          ? t('iot.rule.integration.bridge.detail.enableSuccess')
          : t('iot.rule.integration.bridge.detail.disableSuccess'),
      );
      await load();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.bridge.tips.enableMustTestPass') + ': ' + (e?.message ?? ''),
      );
    }
  }

  /**
   * 跳到桥接日志页 + 默认按 ruleId 过滤（依赖日志页支持 query 参数）。
   */
  function goLogList() {
    push({ path: '/integration/log', query: { bridgeRuleId: id.value } });
  }

  function getDirectionColor(direction?: string): string {
    switch (direction) {
      case '10':
        return 'green';
      case '20':
        return 'blue';
      default:
        return 'default';
    }
  }
</script>

<style lang="less" scoped>
  .bridge-detail {
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

  .rule-icon {
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

  .rule-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .rule-title {
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

  /* a-col 在 Row(flex) 下默认等高;卡片 height 100% 撑满 col,4 张卡强制同高 */
  .metric-card {
    height: 100%;
    border-radius: 14px;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    cursor: default;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
    }

    :deep(.ant-card-body) {
      height: 100%;
      padding: 18px 20px;
      display: flex;
      align-items: center;
      gap: 14px;
    }
  }

  .metric-icon {
    width: 50px;
    height: 50px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
    flex-shrink: 0;

    /* 渐变背景 + 同色柔影,白色图标 ── 高端统计卡风格 */
    &.dir {
      background: linear-gradient(135deg, #49beff 0%, #2e90e8 100%);
      box-shadow: 0 6px 14px rgba(73, 190, 255, 0.4);
    }

    &.ds {
      background: linear-gradient(135deg, #5d87ff 0%, #3d5fe0 100%);
      box-shadow: 0 6px 14px rgba(93, 135, 255, 0.4);
    }

    &.prio {
      background: linear-gradient(135deg, #ffae1f 0%, #e8930a 100%);
      box-shadow: 0 6px 14px rgba(255, 174, 31, 0.4);
    }

    &.enable {
      background: linear-gradient(135deg, #b0bac9 0%, #8c97a5 100%);
      box-shadow: 0 6px 14px rgba(160, 174, 192, 0.35);

      &.on {
        background: linear-gradient(135deg, #13deb9 0%, #07b894 100%);
        box-shadow: 0 6px 14px rgba(19, 222, 185, 0.4);
      }
    }
  }

  .metric-body {
    flex: 1;
    min-width: 0;
  }

  .metric-label {
    color: #8c97a5;
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 0.3px;
  }

  .metric-value {
    color: #2a3547;
    font-size: 19px;
    font-weight: 700;
    margin-top: 3px;
    line-height: 1.25;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  .metric-sub {
    font-size: 12px;
    margin-top: 4px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;

    .sub-key {
      display: inline-block;
      padding: 1px 6px;
      margin-right: 6px;
      border-radius: 4px;
      background: #f0f2f5;
      color: #8c97a5;
      font-size: 11px;
    }

    .sub-val {
      color: #6b7280;
    }
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

  .log-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    gap: 8px;
  }

  .log-list-item {
    cursor: pointer;
    transition: background 0.15s ease;
    padding: 12px 8px !important;

    &:hover {
      background: #f6f8fb;
    }
  }

  .log-row {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;

    .log-status {
      margin: 0;
      flex-shrink: 0;
    }

    .log-main {
      flex: 1;
      min-width: 0;

      .log-title {
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
      }

      .log-sub {
        font-size: 12px;
        color: #6b7280;
        margin-top: 2px;
      }
    }

    .log-arrow {
      color: #a0aec0;
      flex-shrink: 0;
    }
  }

  .inherit {
    color: #a0aec0;
    font-style: italic;
  }

  /* ===== 流控覆盖表格 ===== */
  .override-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;

    th,
    td {
      padding: 10px 14px;
      border: 1px solid #f0f2f5;
      text-align: left;
    }

    thead th {
      background: #f6f8fb;
      color: #6b7280;
      font-weight: 600;
      font-size: 12px;
    }

    tbody {
      td:first-child {
        color: #4b5563;
        font-weight: 500;
        background: #fafbff;
      }

      td.ds {
        color: #6b7280;
      }

      td.effective {
        color: #5d87ff;
        font-weight: 700;
      }
    }
  }
</style>
