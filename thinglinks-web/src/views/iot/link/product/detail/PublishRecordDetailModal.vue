<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.product.publishRecord.detail.title')"
    width="820px"
    :showOkBtn="false"
    :maskClosable="false"
    :cancelText="t('common.title.close')"
    wrapClassName="publish-record-detail-wrap"
  >
    <template v-if="record">
      <!-- 头部胶囊条:意图 + 状态 + 源 → 目标 -->
      <div class="prd-header">
        <span :class="['intent-chip', 'intent-' + record.intent]">
          <component :is="getIntentIcon(record.intent)" class="chip-icon" />
          {{ t(`iot.link.product.publishRecord.intent.${record.intent}`) }}
        </span>
        <a-badge
          class="status-badge"
          :status="getStatusBadge(record.status)"
          :text="t(`iot.link.product.publishRecord.status.${record.status}`)"
        />
        <div class="version-flow">
          <span v-if="record.sourceVersion" class="ver-pill from">{{ record.sourceVersion }}</span>
          <ArrowRightOutlined v-if="record.sourceVersion" class="arrow" />
          <span class="ver-pill to">{{ record.targetVersion || '—' }}</span>
        </div>
      </div>

      <!-- 元信息表格(2 列) -->
      <div class="prd-section">
        <div class="prd-section-title">
          <InfoCircleOutlined />
          {{ t('iot.link.product.publishRecord.detail.sectionMeta') }}
        </div>
        <div class="meta-grid">
          <div class="meta-item">
            <div class="meta-label">
              {{ t('iot.link.product.publishRecord.detail.fieldProductIdentification') }}
            </div>
            <div class="meta-value mono">{{ record.productIdentification || '—' }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">
              {{ t('iot.link.product.publishRecord.detail.fieldOperator') }}
            </div>
            <div class="meta-value">{{ echoMapText(record, 'createdBy') }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">
              {{ t('iot.link.product.publishRecord.detail.fieldRetry') }}
            </div>
            <div class="meta-value"
              >{{ record.retryCount ?? 0 }} / {{ record.maxRetryCount ?? 3 }}</div
            >
          </div>
        </div>
      </div>

      <!-- 发布策略(仅意图=发布的记录展示) -->
      <div v-if="showStrategy" class="prd-section">
        <div class="prd-section-title">
          <ThunderboltOutlined />
          {{ t('iot.link.product.publishRecord.detail.sectionStrategy') }}
        </div>
        <div class="strategy-grid">
          <!-- 策略徽章:全量蓝 / 灰度橙 / 影子紫 -->
          <div class="strategy-cell">
            <div class="strategy-label">
              {{ t('iot.link.product.publishRecord.detail.fieldStrategy') }}
            </div>
            <span :class="['strategy-chip', `strategy-${record!.publishStrategy}`]">
              <component :is="getStrategyIcon(record!.publishStrategy)" class="chip-icon" />
              {{ getStrategyText(record!) }}
            </span>
          </div>
          <!-- CANARY 时显示:模式 + 模式细节 -->
          <template v-if="canaryDetail">
            <div class="strategy-cell">
              <div class="strategy-label">
                {{ t('iot.link.product.publishRecord.detail.canaryMode') }}
              </div>
              <span :class="['canary-mode', `canary-mode-${canaryDetail.mode}`]">
                {{ canaryModeText }}
              </span>
            </div>
            <div class="strategy-cell wide">
              <div class="strategy-label">
                {{
                  canaryDetail.mode === 'percent'
                    ? t('iot.link.product.publishRecord.detail.canaryPercentValue', { n: 0 }).split(
                        ' ',
                      )[0]
                    : t('iot.link.product.publishRecord.detail.canaryWhitelistDevices')
                }}
              </div>
              <div v-if="canaryDetail.mode === 'percent'" class="canary-percent-bar">
                <div class="canary-percent-track">
                  <div
                    class="canary-percent-fill"
                    :style="{ width: (canaryDetail.canaryPercent ?? 0) + '%' }"
                  ></div>
                </div>
                <span class="canary-percent-num">{{ canaryDetail.canaryPercent ?? 0 }}%</span>
              </div>
              <div v-else-if="canaryDetail.mode === 'whitelist'" class="canary-whitelist">
                <span class="canary-count">
                  {{
                    t('iot.link.product.publishRecord.detail.canaryWhitelistCount', {
                      n: canaryDetail.deviceIdentifications?.length ?? 0,
                    })
                  }}
                </span>
                <div
                  v-if="(canaryDetail.deviceIdentifications?.length ?? 0) > 0"
                  class="canary-whitelist-chips"
                >
                  <code
                    v-for="(d, i) in canaryDetail.deviceIdentifications"
                    :key="i"
                    class="canary-dev-chip"
                    >{{ d }}</code
                  >
                </div>
                <div v-else class="empty-hint inline">
                  {{ t('iot.link.product.publishRecord.detail.canaryWhitelistEmpty') }}
                </div>
              </div>
            </div>
          </template>
          <!-- canaryConfigJson 解析失败时显示原文 + 错误提示 -->
          <div v-if="canaryRawInvalid" class="strategy-cell wide">
            <div class="strategy-label danger">
              {{ t('iot.link.product.publishRecord.detail.canaryConfigInvalid') }}
            </div>
            <pre class="canary-raw">{{ record!.canaryConfigJson }}</pre>
          </div>
        </div>
      </div>

      <!-- 时间轴(开始 → 结束 → 持续) -->
      <div class="prd-section">
        <div class="prd-section-title">
          <ClockCircleOutlined />
          {{ t('iot.link.product.publishRecord.detail.sectionTimeline') }}
        </div>
        <div class="timeline-grid">
          <div class="time-cell">
            <div class="time-label">
              {{ t('iot.link.product.publishRecord.detail.fieldStartedTime') }}
            </div>
            <div class="time-value">{{ formatTime(record.startedTime) }}</div>
          </div>
          <div class="time-cell">
            <div class="time-label">
              {{ t('iot.link.product.publishRecord.detail.fieldFinishedTime') }}
            </div>
            <div class="time-value">{{ formatTime(record.finishedTime) }}</div>
          </div>
          <div class="time-cell highlight">
            <div class="time-label">
              {{ t('iot.link.product.publishRecord.detail.fieldDuration') }}
            </div>
            <div class="time-value duration">
              {{ formatDuration(record.startedTime, record.finishedTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 失败原因(仅 status=2) -->
      <div v-if="record.status === 2" class="prd-section">
        <div class="prd-section-title danger">
          <ExclamationCircleFilled />
          {{ t('iot.link.product.publishRecord.detail.sectionFailure') }}
          <a-button
            v-if="record.failedReason"
            type="link"
            size="small"
            class="section-action"
            @click="onCopyReason"
          >
            <CopyOutlined />
            {{ t('iot.link.product.publishRecord.detail.copyReason') }}
          </a-button>
        </div>
        <pre v-if="record.failedReason" class="failure-box">{{ record.failedReason }}</pre>
        <div v-else class="empty-hint">
          {{ t('iot.link.product.publishRecord.detail.noFailure') }}
        </div>
      </div>

      <!-- DDL 执行清单(结构化:每条带成功/失败 / 耗时 / 错误信息) -->
      <div class="prd-section">
        <div class="prd-section-title">
          <DatabaseOutlined />
          {{ t('iot.link.product.publishRecord.detail.sectionDdl') }}
          <span v-if="ddlItems.length" class="section-count">
            {{ t('iot.link.product.publishRecord.ddlCount', { n: ddlItems.length }) }}
          </span>
          <span v-if="ddlFailedCount > 0" class="section-count danger">
            {{ t('iot.link.product.publishRecord.detail.ddlFailedCount', { n: ddlFailedCount }) }}
          </span>
        </div>

        <div v-if="ddlItems.length" class="ddl-item-list">
          <div
            v-for="(item, i) in ddlItems"
            :key="i"
            :class="['ddl-item', item.success ? 'ok' : 'fail']"
          >
            <!-- 头部:操作 / 服务 / 状态徽章 / 重试次数 / 耗时 -->
            <div class="ddl-item-head">
              <span class="ddl-op-tag" :class="`op-${item.operation}`">
                {{ formatOperation(item.operation) }}
              </span>
              <span class="ddl-service">
                <span v-if="item.serviceName" class="svc-name">{{ item.serviceName }}</span>
                <code class="svc-code">{{ item.serviceCode || '—' }}</code>
              </span>
              <span class="ddl-status">
                <CheckCircleFilled v-if="item.success" class="ok-icon" />
                <ExclamationCircleFilled v-else class="fail-icon" />
                {{
                  item.success
                    ? t('iot.link.product.publishRecord.detail.ddlSuccess')
                    : t('iot.link.product.publishRecord.detail.ddlFailed')
                }}
              </span>
              <!-- 重试次数 badge:>1 时才显示(首次执行无需突出),提示用户这条经过 Job 补偿 -->
              <a-tooltip
                v-if="(item.attemptCount ?? 0) > 1"
                :title="
                  t('iot.link.product.publishRecord.detail.attemptTooltip', {
                    n: item.attemptCount,
                  })
                "
              >
                <span :class="['ddl-attempt', item.success ? 'recovered' : 'retrying']">
                  <RedoOutlined class="attempt-icon" />
                  ×{{ item.attemptCount }}
                </span>
              </a-tooltip>
              <span v-if="item.durationMs != null" class="ddl-duration">
                {{ formatMs(item.durationMs) }}
              </span>
            </div>
            <!-- 表名(始终显示) + 字段数 + 最近执行时间 + 展开/折叠 + 复制按钮(失败时才显示交互按钮) -->
            <div class="ddl-item-meta">
              <DatabaseOutlined class="meta-mini-icon" />
              <code class="meta-stable" :title="item.stableName">{{ item.stableName }}</code>
              <span v-if="item.columnCount != null" class="meta-cols">
                {{ t('iot.link.product.publishRecord.detail.ddlColumns', { n: item.columnCount }) }}
              </span>
              <!-- 最近执行时间(Job 重试场景下 = 最后一次执行,首次执行 = 首次时间) -->
              <span v-if="item.executedAt" class="meta-time" :title="item.executedAt">
                <ClockCircleOutlined class="meta-mini-icon" />
                {{ item.executedAt }}
              </span>
              <!-- 展开/折叠:成功 + 有 schema → 看表结构;失败 + 有 errorMsg → 看错误明细 -->
              <span v-if="canExpandItem(item)" class="meta-actions">
                <a-button type="link" size="small" class="meta-btn" @click="toggleDdlExpand(i)">
                  <component :is="isDdlExpanded(i) ? UpOutlined : DownOutlined" />
                  {{
                    isDdlExpanded(i)
                      ? t('iot.link.product.publishRecord.detail.collapseDetail')
                      : t('iot.link.product.publishRecord.detail.expandDetail')
                  }}
                </a-button>
                <a-tooltip
                  v-if="!item.success && item.errorMsg"
                  :title="t('iot.link.product.publishRecord.detail.copyError')"
                >
                  <a-button
                    type="link"
                    size="small"
                    class="meta-btn"
                    @click="onCopyDdlError(item.errorMsg)"
                  >
                    <CopyOutlined />
                  </a-button>
                </a-tooltip>
              </span>
            </div>
            <!-- 失败错误明细 ── 默认展开(用户最关注),可折叠 -->
            <div v-if="!item.success && item.errorMsg && isDdlExpanded(i)" class="ddl-item-error">
              {{ item.errorMsg }}
            </div>
            <!-- 成功 + 有表结构 → 展开 TDengine describe 反查的真实 schema -->
            <div v-if="item.success && hasSchema(item) && isDdlExpanded(i)" class="ddl-item-schema">
              <!-- 行级字节 chip:展示该表行级总和 / 65531 上限 / 占比 -->
              <div v-if="item.rowBytes != null" class="schema-summary">
                <span :class="['bytes-chip', rowBytesLevel(item.rowBytes)]">
                  <DatabaseOutlined class="chip-icon" />
                  {{
                    t('iot.link.product.publishRecord.detail.rowBytes', {
                      used: item.rowBytes,
                      total: TD_ROW_MAX_BYTES,
                    })
                  }}
                  <span class="bytes-pct">{{ rowBytesPct(item.rowBytes) }}%</span>
                </span>
              </div>
              <!-- 字段表:schema 在前,tag 在后(note=TAG 标签) -->
              <table class="schema-table">
                <thead>
                  <tr>
                    <th>{{ t('iot.link.product.publishRecord.detail.schemaCol.field') }}</th>
                    <th>{{ t('iot.link.product.publishRecord.detail.schemaCol.type') }}</th>
                    <th>{{ t('iot.link.product.publishRecord.detail.schemaCol.length') }}</th>
                    <th>{{ t('iot.link.product.publishRecord.detail.schemaCol.bytes') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(f, j) in item.schemaFields ?? []" :key="'s' + j">
                    <td
                      ><code>{{ f.field }}</code></td
                    >
                    <td
                      ><span class="td-type">{{ f.type }}</span></td
                    >
                    <td>{{ f.length ?? '—' }}</td>
                    <td class="td-bytes">{{ f.bytes ?? 0 }}</td>
                  </tr>
                  <tr v-for="(f, j) in item.tagsFields ?? []" :key="'t' + j" class="tag-row">
                    <td>
                      <code>{{ f.field }}</code>
                      <a-tag color="purple" class="tag-badge">TAG</a-tag>
                    </td>
                    <td
                      ><span class="td-type">{{ f.type }}</span></td
                    >
                    <td>{{ f.length ?? '—' }}</td>
                    <td class="td-bytes">{{ f.bytes ?? 0 }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div v-else class="empty-hint">
          {{ t('iot.link.product.publishRecord.detail.emptyDdl') }}
        </div>
      </div>

      <!-- 备注 -->
      <div v-if="record.remark" class="prd-section">
        <div class="prd-section-title">
          <FileTextOutlined />
          {{ t('iot.link.product.publishRecord.detail.sectionRemark') }}
        </div>
        <div class="remark-box">{{ record.remark }}</div>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import {
    InfoCircleOutlined,
    ClockCircleOutlined,
    DatabaseOutlined,
    FileTextOutlined,
    ExclamationCircleFilled,
    CheckCircleFilled,
    ArrowRightOutlined,
    RocketOutlined,
    RollbackOutlined,
    DeleteOutlined,
    CopyOutlined,
    UpOutlined,
    DownOutlined,
    RedoOutlined,
    ThunderboltOutlined,
    GlobalOutlined,
    UserOutlined,
    EyeOutlined,
  } from '@ant-design/icons-vue';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { echoMapText } from '/@/utils/echo';
  import type { ProductPublishRecordResultVO } from '/@/api/iot/link/productPublishRecord/model/productPublishRecordModel';

  /**
   * 单条 DDL 执行明细 ── 对应后端 {@code PublishDdlItemVO}。
   * <p>后端 {@code ProductVersionPublishOrchestrator} 把每条 service 的 CREATE_STABLE / DROP_STABLE
   * 执行结果序列化到 {@code ddl_summary} JSON 列(由 JacksonTypeHandler 自动 ↔ Java List)。
   * 前端 {@code ProductPublishRecordResultVO.ddlItems} 拿到的就是 typed 数组,无需 JSON.parse。</p>
   */
  /** TDengine 字段定义快照(对应后端 {@code DdlFieldVO},由 DESCRIBE 反查得到)。 */
  interface DdlField {
    field?: string;
    type?: string;
    length?: number;
    bytes?: number;
  }

  interface PublishDdlItem {
    operation?: string;
    stableName?: string;
    serviceCode?: string;
    serviceName?: string;
    columnCount?: number;
    success?: boolean;
    errorMsg?: string;
    durationMs?: number;
    /** 本次执行时间(yyyy-MM-dd HH:mm:ss);Job 重试时为最新时间。 */
    executedAt?: string;
    /** 执行次数:首次=1,Job 重试累加。 */
    attemptCount?: number;
    /** 表的普通字段(CREATE STABLE 成功后 DESCRIBE 反查得到)。 */
    schemaFields?: DdlField[];
    /** 表的 tag 字段(同 schemaFields)。 */
    tagsFields?: DdlField[];
    /** 单行字段字节数合计(不含 tag)。 */
    rowBytes?: number;
  }

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const record = ref<ProductPublishRecordResultVO | null>(null);
  /** 展开了详情的 DDL 项索引集合(失败项默认展开,成功项无需展开)。 */
  const expandedDdlIds = ref<Set<number>>(new Set());

  const [registerModal] = useModalInner((data: any) => {
    record.value = data?.record ?? null;
    // 重置展开集合 + 失败项默认展开错误,用户最关注
    expandedDdlIds.value = new Set();
    const items = ddlItems.value;
    items.forEach((it, i) => {
      if (it.success === false) expandedDdlIds.value.add(i);
    });
  });

  function isDdlExpanded(index: number): boolean {
    return expandedDdlIds.value.has(index);
  }

  function toggleDdlExpand(index: number) {
    const next = new Set(expandedDdlIds.value);
    if (next.has(index)) next.delete(index);
    else next.add(index);
    expandedDdlIds.value = next;
  }

  function onCopyDdlError(errorMsg?: string) {
    if (!errorMsg) return;
    if (copyTextToClipboard(errorMsg)) {
      createMessage.success(t('iot.link.product.publishRecord.detail.errorCopied'));
    }
  }

  // ───────── DDL item 展开规则:成功 + 有 schema → 看表结构;失败 + 有 errorMsg → 看错误 ─────────

  /** TDengine 单行字段字节合计上限(展示用,跟后端 TdSchemaInspector.TD_ROW_MAX_BYTES 对齐)。 */
  const TD_ROW_MAX_BYTES = 65531;

  /** 该 item 是否有 TD describe 反查的字段定义可展示。 */
  function hasSchema(item: PublishDdlItem): boolean {
    return (item.schemaFields?.length ?? 0) > 0 || (item.tagsFields?.length ?? 0) > 0;
  }

  /** 该 item 是否可以展开(成功看表结构 / 失败看错误)── 决定展开按钮是否显示。 */
  function canExpandItem(item: PublishDdlItem): boolean {
    return (
      (item.success === true && hasSchema(item)) || (item.success === false && !!item.errorMsg)
    );
  }

  /** 行级字节占比 0~100 ── 给 chip 文案 + 配色用。 */
  function rowBytesPct(rowBytes?: number): number {
    if (!rowBytes || rowBytes <= 0) return 0;
    return Math.min(100, Math.round((rowBytes / TD_ROW_MAX_BYTES) * 100));
  }

  /** 占比 chip 配色级别:ok < 60% / warn 60-85% / danger > 85% ── CSS class 用。 */
  function rowBytesLevel(rowBytes?: number): 'ok' | 'warn' | 'danger' {
    const pct = rowBytesPct(rowBytes);
    if (pct > 85) return 'danger';
    if (pct > 60) return 'warn';
    return 'ok';
  }

  /**
   * 直接从 record.ddlItems 取 typed 列表 ── 后端 JacksonTypeHandler 已自动反序列化,
   * 前端不再需要 JSON.parse(record.ddlSummary)。
   *
   * <p>极少数历史数据(改造前发布的)可能 ddlItems 为 null,统一返空数组保护渲染。</p>
   */
  const ddlItems = computed<PublishDdlItem[]>(() => {
    return (record.value?.ddlItems as PublishDdlItem[] | undefined) ?? [];
  });

  /** 失败 DDL 数(头部 chip 提示用)。 */
  const ddlFailedCount = computed(() => ddlItems.value.filter((it) => it.success === false).length);

  function formatOperation(op?: string): string {
    if (!op) return '—';
    // CREATE_STABLE / DROP_STABLE → 字典文案,缺失时直接展示
    const key = `iot.link.product.publishRecord.detail.op.${op}`;
    const translated = t(key);
    return translated === key ? op : translated;
  }

  function formatMs(ms?: number): string {
    if (ms == null) return '';
    if (ms < 1000) return `${ms} ms`;
    return `${(ms / 1000).toFixed(2)} s`;
  }

  function getIntentIcon(intent?: number) {
    if (intent === 0) return RocketOutlined;
    if (intent === 1) return RollbackOutlined;
    if (intent === 2) return DeleteOutlined;
    return DatabaseOutlined;
  }

  // ───────── 发布策略展示(intent=0 发布记录 + publishStrategy 非空 才显示)─────────

  /** 发布策略 JSON 配置 ── 对应后端 CanaryConfigDTO 的结构。 */
  interface CanaryConfig {
    mode?: string;
    deviceIdentifications?: string[];
    canaryPercent?: number;
  }

  /** 策略徽章图标:全量=GlobalOutlined,灰度=UserOutlined,影子=EyeOutlined。 */
  function getStrategyIcon(strategy?: number) {
    if (strategy === 0) return GlobalOutlined;
    if (strategy === 1) return UserOutlined;
    if (strategy === 2) return EyeOutlined;
    return ThunderboltOutlined;
  }

  /**
   * 取策略中文名:优先 echoMap(后端字典回显),fallback 到本地映射。
   *
   * @echoMap 由 @Echo 注解填充,key = 字段名;publishStrategy 已在 ResultVO 上加注解。
   */
  function getStrategyText(rec: ProductPublishRecordResultVO): string {
    return echoMapText(rec, 'publishStrategy') || '—';
  }

  /** 仅意图=发布(0)且 publishStrategy 非空时展示策略区块 ── 回滚/清理记录无策略概念。 */
  const showStrategy = computed(() => {
    const r = record.value;
    return !!r && r.intent === 0 && r.publishStrategy != null;
  });

  /**
   * 解析灰度配置 JSON 为 typed CanaryConfig。
   * 仅 CANARY(strategy=1)且 canaryConfigJson 非空时尝试解析;解析失败返 undefined,
   * 模板侧通过 {@link canaryRawInvalid} 单独提示原文。
   */
  const canaryDetail = computed<CanaryConfig | undefined>(() => {
    const r = record.value;
    if (!r || r.publishStrategy !== 1 || !r.canaryConfigJson) return undefined;
    try {
      return JSON.parse(r.canaryConfigJson) as CanaryConfig;
    } catch {
      return undefined;
    }
  });

  /** canaryConfigJson 存在但解析失败的兜底标记 ── 让用户看到脏数据原文,便于排查。 */
  const canaryRawInvalid = computed(() => {
    const r = record.value;
    if (!r || r.publishStrategy !== 1) return false;
    if (!r.canaryConfigJson) return false;
    return canaryDetail.value === undefined;
  });

  /** 灰度模式中文 ── whitelist/percent/未知。 */
  const canaryModeText = computed(() => {
    const mode = canaryDetail.value?.mode;
    if (mode === 'whitelist') return t('iot.link.product.publishRecord.detail.canaryModeWhitelist');
    if (mode === 'percent') return t('iot.link.product.publishRecord.detail.canaryModePercent');
    return t('iot.link.product.publishRecord.detail.canaryModeUnknown');
  });

  function getStatusBadge(status?: number): 'processing' | 'success' | 'error' | 'default' {
    switch (status) {
      case 0:
        return 'processing';
      case 1:
        return 'success';
      case 2:
        return 'error';
      default:
        return 'default';
    }
  }

  function formatTime(time?: string | null): string {
    if (!time) return '—';
    return String(time).replace('T', ' ').slice(0, 19);
  }

  function formatDuration(start?: string | null, end?: string | null): string {
    if (!start || !end) return '—';
    const ms = new Date(end).getTime() - new Date(start).getTime();
    if (ms < 0) return '—';
    if (ms < 1000) return `${ms} ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)} s`;
    return `${Math.floor(ms / 60000)} min ${Math.floor((ms % 60000) / 1000)} s`;
  }

  function onCopyReason() {
    const reason = record.value?.failedReason;
    if (!reason) return;
    if (copyTextToClipboard(reason)) {
      createMessage.success(t('iot.link.product.publishRecord.detail.reasonCopied'));
    }
  }
</script>

<style lang="less">
  /* ─── Flexy 详情弹窗 ── 非 scoped + wrap class 命名空间(跨 modal teleport 边界) ─── */
  .publish-record-detail-wrap {
    .ant-modal-content {
      border-radius: 16px;
      overflow: hidden;
    }

    .ant-modal-body {
      background: #f5f7fa;
      padding: 20px;
      max-height: calc(100vh - 220px);
      overflow-y: auto;
    }

    .ant-modal-header .ant-modal-title {
      font-size: 16px;
      font-weight: 700;
      color: #2a3547;
    }

    /* ─ 头部胶囊条 ─ */
    .prd-header {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 16px;
      margin-bottom: 14px;
      background: #fff;
      border-radius: 14px;
      box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
      flex-wrap: wrap;

      .intent-chip {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 4px 12px 4px 6px;
        border-radius: 12px;
        font-size: 13px;
        font-weight: 700;

        .chip-icon {
          width: 22px;
          height: 22px;
          border-radius: 8px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          font-size: 12px;
        }

        &.intent-0 {
          color: #2952cc;
          background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
          .chip-icon {
            background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
            box-shadow: 0 4px 10px rgba(93, 135, 255, 0.35);
          }
        }
        &.intent-1 {
          color: #b25e00;
          background: linear-gradient(135deg, #fff7e6 0%, #fff3d6 100%);
          .chip-icon {
            background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
            box-shadow: 0 4px 10px rgba(255, 174, 31, 0.35);
          }
        }
        &.intent-2 {
          color: #b13d3a;
          background: linear-gradient(135deg, #fff0ee 0%, #ffe5e0 100%);
          .chip-icon {
            background: linear-gradient(135deg, #fa896b 0%, #ff6a4a 100%);
            box-shadow: 0 4px 10px rgba(250, 137, 107, 0.35);
          }
        }
      }

      .status-badge {
        font-size: 13px;
        font-weight: 500;
      }

      .version-flow {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        margin-left: auto;

        .ver-pill {
          padding: 3px 10px;
          border-radius: 8px;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-size: 12px;
          font-variant-numeric: tabular-nums;
          letter-spacing: 0.2px;

          &.from {
            color: #5b6b82;
            background: #f1f4f9;
            border: 1px solid #e8ecf2;
          }
          &.to {
            color: #2952cc;
            background: #eef4ff;
            border: 1px solid #cfe2ff;
            font-weight: 600;
          }
        }

        .arrow {
          color: #b8c0cc;
          font-size: 13px;
        }
      }
    }

    /* ─ 区块通用 ─ */
    .prd-section {
      background: #fff;
      border-radius: 14px;
      padding: 16px 18px;
      margin-bottom: 12px;
      box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
    }

    .prd-section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      font-weight: 700;
      color: #2a3547;
      margin-bottom: 12px;

      .anticon {
        color: #5d87ff;
        font-size: 15px;
      }

      &.danger .anticon {
        color: #fa896b;
      }

      .section-count {
        font-weight: 500;
        font-size: 12px;
        color: #97a1b0;
        padding: 1px 8px;
        border-radius: 8px;
        background: #f1f4f9;
        margin-left: 4px;
      }

      .section-action {
        margin-left: auto;
        font-size: 12px;
      }
    }

    /* ─ 元信息 2 列 ─ */
    .meta-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px 18px;
    }

    .meta-item {
      .meta-label {
        font-size: 11px;
        color: #97a1b0;
        text-transform: uppercase;
        letter-spacing: 0.3px;
        margin-bottom: 4px;
      }

      .meta-value {
        font-size: 13px;
        color: #2a3547;
        font-weight: 500;
        word-break: break-all;

        &.mono {
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-size: 12.5px;
        }
      }
    }

    /* ─ 发布策略区 ─ 自适应 grid:策略徽章 + 灰度模式 + 详情(百分比/白名单) ─ */
    .strategy-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px 18px;
    }

    .strategy-cell {
      &.wide {
        grid-column: 1 / -1;
      }

      .strategy-label {
        font-size: 11px;
        color: #97a1b0;
        text-transform: uppercase;
        letter-spacing: 0.3px;
        margin-bottom: 6px;

        &.danger {
          color: #b13d3a;
          text-transform: none;
        }
      }
    }

    .strategy-chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px 4px 6px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 700;

      .chip-icon {
        width: 22px;
        height: 22px;
        border-radius: 8px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 12px;
        padding: 4px;
      }

      /* 0=全量发布:蓝 */
      &.strategy-0 {
        color: #2952cc;
        background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
        .chip-icon {
          background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
          box-shadow: 0 4px 10px rgba(93, 135, 255, 0.35);
        }
      }
      /* 1=灰度:橙 */
      &.strategy-1 {
        color: #b25e00;
        background: linear-gradient(135deg, #fff7e6 0%, #fff3d6 100%);
        .chip-icon {
          background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
          box-shadow: 0 4px 10px rgba(255, 174, 31, 0.35);
        }
      }
      /* 2=影子:紫 */
      &.strategy-2 {
        color: #6750a4;
        background: linear-gradient(135deg, #f3eafe 0%, #ece2fb 100%);
        .chip-icon {
          background: linear-gradient(135deg, #9b75e6 0%, #b095f0 100%);
          box-shadow: 0 4px 10px rgba(155, 117, 230, 0.35);
        }
      }
    }

    .canary-mode {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 8px;
      font-size: 12px;
      font-weight: 600;

      &.canary-mode-whitelist {
        color: #2952cc;
        background: #eef4ff;
        border: 1px solid #cfe2ff;
      }
      &.canary-mode-percent {
        color: #b25e00;
        background: #fff7e6;
        border: 1px solid #ffd591;
      }
    }

    .canary-percent-bar {
      display: flex;
      align-items: center;
      gap: 10px;

      .canary-percent-track {
        flex: 1;
        height: 8px;
        border-radius: 4px;
        background: #f1f4f9;
        overflow: hidden;
      }

      .canary-percent-fill {
        height: 100%;
        background: linear-gradient(90deg, #ffae1f 0%, #ffc94a 100%);
        border-radius: 4px;
        transition: width 0.3s ease;
      }

      .canary-percent-num {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 13px;
        font-weight: 700;
        color: #b25e00;
        font-variant-numeric: tabular-nums;
        min-width: 44px;
        text-align: right;
      }
    }

    .canary-whitelist {
      .canary-count {
        display: inline-block;
        font-size: 12px;
        color: #5b6b82;
        padding: 2px 8px;
        border-radius: 6px;
        background: #f1f4f9;
        margin-bottom: 8px;
      }

      .canary-whitelist-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        max-height: 120px;
        overflow-y: auto;
        padding: 4px 0;
      }

      .canary-dev-chip {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11.5px;
        color: #2a3547;
        padding: 2px 8px;
        border-radius: 6px;
        background: rgba(93, 135, 255, 0.08);
        border: 1px solid rgba(93, 135, 255, 0.18);
      }

      .empty-hint.inline {
        padding: 8px 12px;
        text-align: left;
        font-size: 11.5px;
      }
    }

    .canary-raw {
      margin: 0;
      padding: 10px 12px;
      border-radius: 8px;
      background: #fff5f5;
      border: 1px solid #ffd6d3;
      color: #b13d3a;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 11.5px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }

    /* ─ 时间轴 3 列 ─ */
    .timeline-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
    }

    .time-cell {
      padding: 12px 14px;
      border-radius: 10px;
      background: #f8fafc;

      &.highlight {
        background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
        .time-value.duration {
          color: #5d87ff;
          font-weight: 700;
        }
      }

      .time-label {
        font-size: 11px;
        color: #97a1b0;
        margin-bottom: 4px;
      }

      .time-value {
        font-size: 13px;
        color: #2a3547;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-variant-numeric: tabular-nums;
      }
    }

    /* ─ 失败原因 / 备注 / DDL ─ */
    .failure-box {
      margin: 0;
      padding: 12px 14px;
      border-radius: 10px;
      background: linear-gradient(135deg, #fff5f5 0%, #fff0ee 100%);
      border: 1px solid #ffd6d3;
      color: #b13d3a;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 12px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
      max-height: 260px;
      overflow-y: auto;
    }

    /* ─── 结构化 DDL 明细列表(每条带状态/服务/耗时/错误) ─── */
    .ddl-item-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-height: 420px;
      overflow-y: auto;
    }

    .ddl-item {
      /* flex-shrink:0 防止父级 flex column 容器 max-height 触发滚动时子项被压扁 */
      flex-shrink: 0;
      padding: 10px 12px;
      border-radius: 10px;
      background: #f8fafc;
      border: 1px solid #eef0f4;
      transition: border-color 0.18s ease;

      &.ok {
        border-color: #b7ecd9;
        background: linear-gradient(135deg, #f4fbf7 0%, #ebfaf2 100%);
      }
      &.fail {
        border-color: #ffd6d3;
        background: linear-gradient(135deg, #fff5f5 0%, #fff0ee 100%);
      }
    }

    .ddl-item-head {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
      margin-bottom: 6px;

      .ddl-op-tag {
        padding: 1px 8px;
        border-radius: 6px;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 0.3px;
        color: #fff;

        &.op-CREATE_STABLE {
          background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
        }
        &.op-DROP_STABLE {
          background: linear-gradient(135deg, #fa896b 0%, #ff6a4a 100%);
        }
        &.op-ALTER_STABLE {
          background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
        }
      }

      .ddl-service {
        display: inline-flex;
        align-items: center;
        gap: 6px;

        .svc-name {
          font-size: 12.5px;
          font-weight: 600;
          color: #2a3547;
        }
        .svc-code {
          font-size: 11px;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          color: #5b6b82;
          padding: 1px 6px;
          border-radius: 4px;
          background: rgba(0, 0, 0, 0.03);
        }
      }

      .ddl-status {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        margin-left: auto;
        font-size: 12px;
        font-weight: 600;

        .ok-icon {
          color: #13deb9;
        }
        .fail-icon {
          color: #fa896b;
        }
      }

      .ddl-duration {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11.5px;
        color: #97a1b0;
        font-variant-numeric: tabular-nums;
        padding: 1px 6px;
        border-radius: 4px;
        background: rgba(0, 0, 0, 0.03);
      }

      /* 重试次数 badge ── 1 次以下不显示;recovered=经过重试最终成功(蓝),retrying=仍在重试中(橙) */
      .ddl-attempt {
        display: inline-flex;
        align-items: center;
        gap: 3px;
        padding: 1px 8px;
        border-radius: 8px;
        font-size: 11px;
        font-weight: 700;
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.2px;

        .attempt-icon {
          font-size: 10px;
        }

        &.recovered {
          color: #2952cc;
          background: rgba(93, 135, 255, 0.12);
        }
        &.retrying {
          color: #b25e00;
          background: rgba(255, 174, 31, 0.16);
        }
      }
    }

    .ddl-item-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      padding-left: 4px;

      .meta-mini-icon {
        color: #a0aec0;
        font-size: 11px;
      }

      .meta-stable {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11.5px;
        color: #2a3547;
        word-break: break-all;
      }

      .meta-cols {
        font-size: 11px;
        color: #97a1b0;
      }

      /* 最近执行时间(Job 重试场景下 = 最后一次时间) */
      .meta-time {
        display: inline-flex;
        align-items: center;
        gap: 3px;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11px;
        color: #97a1b0;
        font-variant-numeric: tabular-nums;
      }

      /* 失败项右侧操作按钮(展开/折叠 + 复制错误) */
      .meta-actions {
        margin-left: auto;
        display: inline-flex;
        align-items: center;
        gap: 2px;
      }

      .meta-btn {
        padding: 0 6px;
        height: 22px;
        font-size: 11.5px;
        font-weight: 500;
        color: #5d87ff;

        &:hover {
          color: #2952cc;
          background: rgba(93, 135, 255, 0.06);
        }

        .anticon {
          font-size: 11px;
        }
      }
    }

    .ddl-item-error {
      margin-top: 8px;
      padding: 8px 10px;
      border-radius: 6px;
      background: rgba(250, 137, 107, 0.08);
      color: #b13d3a;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 11.5px;
      line-height: 1.5;
      word-break: break-word;
      white-space: pre-wrap;
    }

    /* TDengine 反查的表结构表 ── 字段名 / 类型 / 长度 / 字节数 */
    .ddl-item-schema {
      margin-top: 10px;
      padding: 10px 12px;
      border-radius: 8px;
      background: #f8fafc;
      border: 1px solid #eef0f4;
    }

    .schema-summary {
      margin-bottom: 8px;

      .bytes-chip {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 11.5px;
        font-weight: 500;
        font-variant-numeric: tabular-nums;

        .chip-icon {
          font-size: 12px;
        }
        .bytes-pct {
          font-weight: 600;
          margin-left: 2px;
        }

        /* < 60%:绿,安全 */
        &.ok {
          color: #0fb094;
          background: #ebfaf2;
          border: 1px solid #b7ecd9;
        }
        /* 60-85%:橙,警示 */
        &.warn {
          color: #b06900;
          background: #fff7e6;
          border: 1px solid #ffd591;
        }
        /* > 85%:红,接近 65531 上限 */
        &.danger {
          color: #b13d3a;
          background: #fff5f5;
          border: 1px solid #ffd6d3;
        }
      }
    }

    .schema-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;

      th,
      td {
        padding: 6px 10px;
        text-align: left;
        border-bottom: 1px dashed #e8ecf2;
      }
      th {
        color: #5b6b82;
        font-weight: 600;
        background: rgba(255, 255, 255, 0.6);
      }
      td {
        color: #2a3547;
        code {
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-size: 12px;
          color: #2a3547;
          background: transparent;
        }
      }
      .td-type {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11.5px;
        color: #5b6b82;
      }
      .td-bytes {
        font-variant-numeric: tabular-nums;
        color: #5b6b82;
      }
      .tag-row td {
        background: rgba(155, 117, 230, 0.04);
      }
      .tag-badge {
        margin-left: 6px;
        font-size: 10px;
        padding: 0 6px;
        line-height: 16px;
        height: 16px;
      }
    }

    /* 头部失败计数高亮 */
    .prd-section-title .section-count.danger {
      background: rgba(250, 137, 107, 0.12);
      color: #b13d3a;
    }

    .remark-box {
      padding: 12px 14px;
      border-radius: 10px;
      background: #f8fafc;
      color: #4a5568;
      font-size: 13px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .empty-hint {
      padding: 16px;
      text-align: center;
      color: #a0aec0;
      font-size: 12px;
      background: #f8fafc;
      border-radius: 8px;
    }
  }
</style>
