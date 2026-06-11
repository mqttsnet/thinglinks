<template>
  <PageWrapper dense contentFullHeight class="log-page">
    <!-- ===== 顶部统计卡（Flexy 风格） ===== -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="12" :sm="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon ok"><CheckCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.rule.integration.log.metric.success') }}</div>
            <div class="metric-value">{{ stats.success ?? '-' }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon fail"><CloseCircleOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.rule.integration.log.metric.failed') }}</div>
            <div class="metric-value">{{ stats.failed ?? '-' }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon dlq"><AlertOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.rule.integration.log.metric.deadLetter') }}</div>
            <div class="metric-value">{{ stats.deadLetter ?? '-' }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon avg"><DashboardOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t('iot.rule.integration.log.metric.avgLatency') }}</div>
            <div class="metric-value">{{ stats.avgLatency ?? '-' }}<small> ms</small></div>
          </div>
        </Card>
      </Col>
    </Row>

    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button
          v-hasAnyPermission="['rule:integration:log:export']"
          @click="handleExport"
        >
          <template #icon><DownloadOutlined /></template>
          {{ t('iot.rule.integration.log.action.export') }}
        </a-button>
        <a-button
          v-hasAnyPermission="['rule:integration:log:statistics']"
          @click="handleStatistics"
        >
          <template #icon><BarChartOutlined /></template>
          {{ t('iot.rule.integration.log.action.statistics') }}
        </a-button>
      </template>

      <template #status="{ record }">
        <a-tag :color="getStatusColor(record?.status)">
          {{ getDictLabel('BRIDGE_MESSAGE_STATUS', record?.status, '') }}
        </a-tag>
      </template>

      <template #triggerSource="{ record }">
        {{ getDictLabel('BRIDGE_TRIGGER_SOURCE', record?.triggerSource, '') }}
      </template>

      <template #ruleName="{ record }">
        {{ record?.echoMap?.bridgeRuleId || record?.bridgeRuleId || '-' }}
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                tooltip: t('iot.rule.integration.log.action.detail'),
                icon: 'ant-design:eye-outlined',
                auth: 'rule:integration:log:view',
                onClick: handleDetail.bind(null, record),
              },
              {
                tooltip: t('iot.rule.integration.log.action.replay'),
                icon: 'ant-design:redo-outlined',
                color: 'warning',
                auth: 'rule:integration:log:replay',
                ifShow: record.status === '03',
                popConfirm: {
                  title: t('iot.rule.integration.log.tips.confirmReplay'),
                  confirm: handleReplay.bind(null, record),
                },
              },
            ]"
            :stopButtonPropagation="true"
          />
        </template>
      </template>
    </BasicTable>

    <TraceDetailDrawer @register="registerDrawer" @replayed="reload" />
    <StatisticsModal @register="registerStatsModal" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col } from 'ant-design-vue';
  import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    AlertOutlined,
    DashboardOutlined,
    DownloadOutlined,
    BarChartOutlined,
  } from '@ant-design/icons-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useDrawer } from '/@/components/Drawer';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { page, replay } from '/@/api/iot/rule/integration/bridgeExecutionTrace';
  import { columns, searchFormSchema } from './log.data';
  import TraceDetailDrawer from './components/TraceDetailDrawer.vue';
  import StatisticsModal from './components/StatisticsModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useDict } from '/@/components/Dict';
  import { BridgeExecutionTraceResultVO } from '/@/api/iot/rule/integration/model/bridgeExecutionTraceModel';

  defineOptions({ name: '桥接日志' });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const { currentRoute } = useRouter();

  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerStatsModal, { openModal: openStatsModal }] = useModal();

  /**
   * 顶部 4 项统计：按当前查询结果聚合（成功 / 失败 / 死信 / 平均耗时）。
   * <p>"查看统计"按钮（StatisticsModal）走全局 24h 维度，与本快照独立。
   */
  const stats = reactive<{
    success: number | null;
    failed: number | null;
    deadLetter: number | null;
    avgLatency: number | null;
  }>({
    success: null,
    failed: null,
    deadLetter: null,
    avgLatency: null,
  });

  /**
   * 拉取分页数据后即时聚合统计（前端简化方案；后端 stats 接口可替换）。
   */
  function refreshStats(records: BridgeExecutionTraceResultVO[]) {
    if (!records?.length) {
      stats.success = stats.failed = stats.deadLetter = stats.avgLatency = 0;
      return;
    }
    const success = records.filter((r) => r.status === '00').length;
    const failed = records.filter((r) => r.status === '01').length;
    const deadLetter = records.filter((r) => r.status === '03').length;
    const total = records.reduce((sum, r) => sum + (r.totalLatencyMs ?? 0), 0);
    stats.success = success;
    stats.failed = failed;
    stats.deadLetter = deadLetter;
    stats.avgLatency = records.length ? Math.round(total / records.length) : 0;
  }

  const [registerTable, { reload, getDataSource }] = useTable({
    title: t('iot.rule.integration.log.table.title'),
    api: async (params: any) => {
      const res = await page(params);
      refreshStats((res?.records ?? []) as BridgeExecutionTraceResultVO[]);
      return res;
    },
    rowKey: 'id',
    columns: columns(),
    formConfig: {
      labelWidth: 100,
      schemas: searchFormSchema(),
      autoSubmitOnEnter: true,
      resetButtonOptions: { preIcon: 'ant-design:rest-outlined' },
      submitButtonOptions: { preIcon: 'ant-design:search-outlined' },
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
    beforeFetch: handleFetchParams,
    actionColumn: {
      width: 130,
      title: t('common.column.action'),
      dataIndex: 'action',
    },
  });

  /**
   * 路由 query 联动：从桥接规则详情等其它页面跳来时，自动按 bridgeRuleId 过滤。
   */
  onMounted(() => {
    const q = currentRoute.value.query ?? {};
    if (q.bridgeRuleId || q.deviceIdentification) {
      // BasicTable 会自动用 formConfig 默认值；手动 reload 一次
      setTimeout(() => reload(), 0);
    }
  });

  function handleDetail(record: BridgeExecutionTraceResultVO) {
    if (!record.traceId) return;
    openDrawer(true, { traceId: record.traceId, bridgeRuleId: record.bridgeRuleId });
  }

  async function handleReplay(record: BridgeExecutionTraceResultVO) {
    if (!record.traceId) return;
    try {
      const newTraceId = await replay(record.traceId);
      createMessage.success(
        t('iot.rule.integration.log.tips.replaySuccess') + (newTraceId ?? ''),
      );
      await reload();
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.log.tips.replayFailed') + ': ' + (e?.message ?? ''),
      );
    }
  }

  /**
   * 导出当前结果集（前端简化版：CSV 拼接当前 dataSource）。
   * 后端如有专门的 export 接口（按 query 服务端流式导出大数据集），可替换为该接口。
   */
  function handleExport() {
    const data = getDataSource();
    if (!data?.length) {
      createMessage.warning(t('iot.rule.integration.log.tips.exportEmpty'));
      return;
    }
    const headers = [
      'traceId',
      'status',
      'bridgeRuleId',
      'triggerSource',
      'deviceIdentification',
      'actionType',
      'stepCount',
      'totalLatencyMs',
      'startTime',
    ];
    const rows = data.map((r: any) =>
      headers
        .map((h) => {
          const v = r[h] ?? '';
          return /[",\n]/.test(String(v)) ? `"${String(v).replace(/"/g, '""')}"` : v;
        })
        .join(','),
    );
    const csv = '﻿' + headers.join(',') + '\n' + rows.join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `bridge-trace-${Date.now()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    createMessage.success(t('iot.rule.integration.log.tips.exportSuccess'));
  }

  function handleStatistics() {
    openStatsModal(true, {});
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
</script>

<style lang="less" scoped>
  .log-page {
    background: #f6f8fb;
  }

  .metric-row {
    margin: 16px;
  }

  .metric-card {
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);

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

    &.ok {
      background: rgba(19, 222, 185, 0.12);
      color: #13deb9;
    }

    &.fail {
      background: rgba(250, 75, 75, 0.12);
      color: #fa4b4b;
    }

    &.dlq {
      background: rgba(247, 75, 161, 0.12);
      color: #f74ba1;
    }

    &.avg {
      background: rgba(93, 135, 255, 0.1);
      color: #5d87ff;
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
    font-size: 22px;
    font-weight: 700;
    margin-top: 2px;

    small {
      font-size: 12px;
      color: #6b7280;
      font-weight: 400;
      margin-left: 4px;
    }
  }
</style>
