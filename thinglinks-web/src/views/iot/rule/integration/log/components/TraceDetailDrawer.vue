<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="t('iot.rule.integration.log.detail.title')"
    width="72%"
    :showFooter="false"
    class="trace-drawer"
  >
    <a-spin :spinning="loading" class="trace-spin">
      <!-- ========== 顶部 Banner ========== -->
      <Card v-if="trace" :bordered="false" class="banner-card">
        <div class="banner-row">
          <div class="banner-left">
            <Tag :color="getStatusTagColor(trace.status)" class="status-tag">
              {{ getStatusLabel(trace.status) }}
            </Tag>
            <div class="banner-meta">
              <div class="banner-title">
                {{ trace.echoMap?.bridgeRuleId ?? trace.bridgeRuleId ?? '-' }}
              </div>
              <div class="banner-sub">
                {{ t('iot.rule.integration.log.traceId') }}:
                <span class="trace-id-text">{{ trace.traceId }}</span>
                <a-tooltip :title="t('common.title.copy')">
                  <CopyOutlined class="copy-icon" @click="handleCopyTraceId" />
                </a-tooltip>
                <a-popconfirm
                  v-if="trace.status === '03'"
                  :title="t('iot.rule.integration.log.tips.confirmReplay')"
                  @confirm="handleReplay"
                >
                  <a-button
                    type="primary"
                    size="small"
                    danger
                    class="replay-btn"
                    v-hasAnyPermission="['rule:integration:log:replay']"
                  >
                    <template #icon><RedoOutlined /></template>
                    {{ t('iot.rule.integration.log.action.replay') }}
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
          </div>
          <div class="banner-stats">
            <div class="stat-item">
              <div class="stat-label">{{ t('iot.rule.integration.log.triggerSource') }}</div>
              <div class="stat-value">{{
                getDictLabel('BRIDGE_TRIGGER_SOURCE', trace.triggerSource, '-')
              }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">{{ t('iot.rule.integration.log.stepCount') }}</div>
              <div class="stat-value">{{ trace.stepCount ?? 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">{{ t('iot.rule.integration.log.totalLatencyMs') }}</div>
              <div class="stat-value highlight">
                {{ trace.totalLatencyMs ?? 0 }} <small>ms</small>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">{{ t('iot.rule.integration.log.startTime') }}</div>
              <div class="stat-value muted">{{ trace.startTime || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 执行路径 chip 链 -->
        <div v-if="trace.steps?.length" class="path-section">
          <div class="path-label">{{ t('iot.rule.integration.log.detail.executionPath') }}</div>
          <div class="path-chain">
            <template v-for="(s, i) in trace.steps" :key="s.id">
              <span
                class="path-chip"
                :class="getStepClass(s.status)"
                @click="scrollToStep(s.stepNo)"
              >
                {{ s.stepName || s.stepType }}
              </span>
              <span v-if="i < (trace.steps?.length ?? 0) - 1" class="path-arrow">→</span>
            </template>
          </div>
        </div>
      </Card>

      <!-- ========== trace.steps 为空时的占位 ========== -->
      <Card v-if="trace && !trace?.steps?.length" :bordered="false" class="empty-card">
        <a-empty :description="t('iot.rule.integration.log.detail.noSteps')" />
      </Card>

      <!-- ========== 设计链路回放（节点流式图谱）========== -->
      <Card v-if="trace?.steps?.length" :bordered="false" class="canvas-card">
        <template #title>
          <span class="section-title">
            {{ t('iot.rule.integration.log.detail.linkageReplay') }}
          </span>
        </template>
        <template #extra>
          <a-space>
            <a-select
              v-model:value="focusedStep"
              size="small"
              style="min-width: 240px"
              :placeholder="t('iot.rule.integration.log.detail.locate')"
              @change="onFocusChanged"
            >
              <a-select-option v-for="s in trace.steps" :key="s.stepNo" :value="s.stepNo">
                {{ s.stepName }} · {{ s.stepType }} · ID {{ s.stepNo }}
              </a-select-option>
            </a-select>
            <a-button size="small" @click="canvasResetActual" :disabled="canvasScale === 1">
              <template #icon><ZoomInOutlined /></template>
              {{ t('iot.rule.integration.log.detail.actualSize') }}
            </a-button>
            <a-button size="small" @click="canvasFitAll">
              <template #icon><ExpandOutlined /></template>
              {{ t('iot.rule.integration.log.detail.fitAll') }}
            </a-button>
          </a-space>
        </template>

        <div
          class="canvas-wrap"
          :class="{ dragging: isDragging }"
          ref="canvasWrapRef"
          @mousedown="onCanvasMouseDown"
          @touchstart="onCanvasTouchStart"
        >
          <div
            class="canvas-flow"
            ref="canvasFlowRef"
            :style="{ transform: `scale(${canvasScale})`, transformOrigin: 'left center' }"
          >
            <template v-for="(s, i) in trace.steps" :key="s.id">
              <div class="node-stack">
                <div
                  class="node-card"
                  :class="[getStepClass(s.status), { focused: focusedStep === s.stepNo }]"
                  :ref="(el) => setNodeRef(s.stepNo, el)"
                  @click="onNodeClick(s.stepNo)"
                >
                  <div class="node-icon">
                    <component :is="getStepIconComp(s.stepType)" />
                  </div>
                  <div class="node-name">{{ s.stepName || s.stepType }}</div>
                  <div class="node-type">{{ s.stepType }}</div>
                  <!-- RULE_MATCH 节点显示命中条件数徽章 -->
                  <div
                    v-if="s.stepType === 'RULE_MATCH' && getMatchedConditions(s).length"
                    class="node-badge"
                  >
                    {{ getMatchedConditions(s).length }}
                  </div>
                  <div class="node-status-dot"></div>
                </div>
                <!-- RULE_MATCH 节点下方挂条件芯片栏(画布分支兼容) -->
                <div
                  v-if="s.stepType === 'RULE_MATCH' && getMatchedConditions(s).length"
                  class="node-conditions"
                >
                  <div class="branch-line"></div>
                  <div class="branch-chips">
                    <a-tooltip
                      v-for="(c, ci) in getMatchedConditions(s)"
                      :key="ci"
                      :title="getConditionTooltip(c)"
                    >
                      <div
                        class="branch-chip"
                        :class="{ hit: c.hit, miss: !c.hit }"
                        @click.stop="onConditionChipClick(s.stepNo, ci)"
                      >
                        <component :is="getMatchKindIcon(c.kind)" />
                        <span>{{ getMatchKindLabel(c) }}</span>
                      </div>
                    </a-tooltip>
                  </div>
                </div>
              </div>
              <!-- 纯 CSS 箭头(::before 横线 + ::after 三角),保证垂直居中 + 跨浏览器一致 -->
              <div
                v-if="i < (trace.steps?.length ?? 0) - 1"
                class="node-arrow"
                :class="getArrowClass(s.status, trace.steps?.[i + 1]?.status)"
                aria-hidden="true"
              ></div>
            </template>
          </div>
        </div>
        <!-- 拖拽提示(节点 ≥ 4 个或缩放后才显示)-->
        <div
          v-if="trace.steps && (trace.steps.length >= 4 || canvasScale < 1)"
          class="canvas-hint"
        >
          <DragOutlined />
          {{ t('iot.rule.integration.log.detail.dragHint') }}
        </div>
      </Card>

      <!-- ========== 节点执行步骤 ========== -->
      <Card v-if="trace?.steps?.length" :bordered="false" class="steps-card">
        <template #title>
          <span class="section-title">
            {{ t('iot.rule.integration.log.detail.nodeSteps') }}
          </span>
        </template>
        <template #extra>
          <span class="hint">{{ t('iot.rule.integration.log.detail.nodeStepsHelp') }}</span>
        </template>

        <div class="step-list">
          <div
            v-for="s in trace.steps"
            :key="s.id"
            class="step-card"
            :class="[getStepClass(s.status), { focused: focusedStep === s.stepNo }]"
            :ref="(el) => setStepCardRef(s.stepNo, el)"
            @click="onStepCardClick(s.stepNo)"
          >
            <div class="step-head">
              <div class="step-no">{{ s.stepNo }}</div>
              <Tag :color="getStatusTagColor(s.status)" class="step-status-tag">
                {{ getStatusLabel(s.status) }}
              </Tag>
              <div class="step-name">{{ s.stepName }}</div>
              <Tag v-if="getBranch(s)" color="blue" class="step-branch-tag">
                {{ getBranch(s) }}
              </Tag>
              <div class="step-latency">{{ s.latencyMs ?? 0 }} ms</div>
            </div>

            <div class="step-time">
              {{ s.startedAt }} · <span class="step-type">{{ s.stepType }}</span>
            </div>

            <div v-if="getDescription(s)" class="step-desc" :class="getStepClass(s.status)">
              {{ getDescription(s) }}
            </div>

            <div v-if="hasMetricChips(s)" class="step-metric-row">
              <div
                v-for="m in getMetricChips(s)"
                :key="m.label"
                class="metric-chip"
                :class="m.tone"
              >
                <div class="m-label">{{ m.label }}</div>
                <div class="m-value">{{ m.value }}</div>
              </div>
            </div>

            <!-- RULE_MATCH 命中条件明细 ── 每条带 kind 图标 + matcher chip + reason 说明 -->
            <div
              v-if="s.stepType === 'RULE_MATCH' && getMatchedConditions(s).length"
              class="step-block conditions-block"
            >
              <div class="block-title">
                {{ t('iot.rule.integration.log.step.matchedConditions') }}
                <Tag color="processing" class="conditions-count-tag">
                  {{ getMatchedConditions(s).length }}
                </Tag>
              </div>
              <div class="condition-list">
                <div
                  v-for="(c, idx) in getMatchedConditions(s)"
                  :key="idx"
                  class="condition-row"
                  :class="{ hit: c.hit, miss: !c.hit }"
                  :ref="(el) => setConditionRowRef(s.stepNo, idx, el)"
                >
                  <!-- 左:命中状态 + kind 图标 + 维度名 -->
                  <div class="condition-head">
                    <Tag :color="c.hit ? 'success' : 'default'" class="condition-hit-tag">
                      {{
                        c.hit
                          ? t('iot.rule.integration.log.step.conditionHit')
                          : t('iot.rule.integration.log.step.conditionMiss')
                      }}
                    </Tag>
                    <component
                      :is="getMatchKindIcon(c.kind)"
                      class="condition-kind-icon"
                    />
                    <span class="condition-label">{{ getMatchKindLabel(c) }}</span>
                    <Tag
                      v-if="c.matcher"
                      class="matcher-tag"
                      :color="getMatcherTagColor(c.matcher)"
                    >
                      {{ getMatcherLabel(c.matcher) }}
                    </Tag>
                  </div>
                  <!-- 中:expected / actual 对比 -->
                  <div class="condition-body">
                    <div class="cond-field">
                      <span class="cond-field-label">
                        {{ t('iot.rule.integration.log.step.conditionField') }}
                      </span>
                      <code class="cond-field-value">{{ c.field || '-' }}</code>
                    </div>
                    <div v-if="c.expected != null" class="cond-field">
                      <span class="cond-field-label">
                        {{ t('iot.rule.integration.log.step.conditionExpected') }}
                      </span>
                      <code class="cond-field-value">{{ formatConditionValue(c.expected) }}</code>
                    </div>
                    <div v-if="c.actual != null" class="cond-field">
                      <span class="cond-field-label">
                        {{ t('iot.rule.integration.log.step.conditionActualLabel') }}
                      </span>
                      <code class="cond-field-value">{{ formatConditionValue(c.actual) }}</code>
                    </div>
                  </div>
                  <!-- 下:reason 说明 -->
                  <div v-if="c.reason" class="condition-reason">
                    <InfoCircleOutlined />
                    <span>{{ c.reason }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- RULE_MATCH 原始规则配置 ── 折叠展示 matchConfigJson 全文,用户对比"配的"vs"命中的"-->
            <div
              v-if="s.stepType === 'RULE_MATCH' && getRawMatchConfig(s)"
              class="step-block raw-config-block"
            >
              <div
                class="block-title raw-config-title"
                @click="toggleRawConfig(s.stepNo)"
              >
                <RightOutlined
                  class="raw-config-toggle"
                  :class="{ open: rawConfigOpen[s.stepNo ?? -1] }"
                />
                {{ t('iot.rule.integration.log.step.rawConfig') }}
                <span class="raw-config-hint">
                  {{ t('iot.rule.integration.log.step.rawConfigHint') }}
                </span>
              </div>
              <pre
                v-if="rawConfigOpen[s.stepNo ?? -1]"
                class="code-block muted raw-config-code"
              >{{ formatJson(getRawMatchConfig(s)) }}</pre>
            </div>

            <div v-if="s.inputSummary" class="step-block">
              <div class="block-title">{{ t('iot.rule.integration.log.step.inputSummary') }}</div>
              <pre class="code-block">{{ formatJson(s.inputSummary) }}</pre>
            </div>

            <div v-if="s.outputSummary" class="step-block">
              <div class="block-title">{{ t('iot.rule.integration.log.step.outputSummary') }}</div>
              <pre class="code-block">{{ formatJson(s.outputSummary) }}</pre>
            </div>

            <div v-if="s.errorMsg" class="step-block error">
              <div class="block-title">
                <CloseCircleOutlined /> {{ t('iot.rule.integration.log.step.errorMsg') }}
              </div>
              <pre class="code-block error">{{ s.errorMsg }}</pre>
            </div>

            <div v-if="s.extensionJson" class="step-block">
              <div class="block-title">{{ t('iot.rule.integration.log.step.extendParams') }}</div>
              <pre class="code-block muted">{{ formatJson(s.extensionJson) }}</pre>
            </div>
          </div>
        </div>
      </Card>
    </a-spin>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, nextTick, onBeforeUnmount } from 'vue';
  import { Card, Tag, Empty } from 'ant-design-vue';
  import {
    ExpandOutlined,
    CloseCircleOutlined,
    LoginOutlined,
    FilterOutlined,
    ThunderboltOutlined,
    SwapOutlined,
    SendOutlined,
    AlertOutlined,
    DownloadOutlined,
    CopyOutlined,
    RedoOutlined,
    ApiOutlined,
    DragOutlined,
    ZoomInOutlined,
    InfoCircleOutlined,
    RightOutlined,
    AppstoreOutlined,
    BranchesOutlined,
    GoldOutlined,
    MobileOutlined,
    TagsOutlined,
    TeamOutlined,
    CodeOutlined,
    ClockCircleOutlined,
  } from '@ant-design/icons-vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import {
    detail as traceDetail,
    replay,
  } from '/@/api/iot/rule/integration/bridgeExecutionTrace';
  import type {
    BridgeExecutionTraceResultVO,
    BridgeExecutionStepResultVO,
  } from '/@/api/iot/rule/integration/model/bridgeExecutionTraceModel';

  defineOptions({ name: 'BridgeTraceDetailDrawer' });

  const emit = defineEmits<{
    (e: 'replayed', newTraceId: string): void;
  }>();

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createMessage } = useMessage();

  const trace = ref<BridgeExecutionTraceResultVO | null>(null);
  const loading = ref(false);
  const focusedStep = ref<number | undefined>(undefined);
  const canvasScale = ref(1);
  const canvasWrapRef = ref<HTMLElement | null>(null);
  const canvasFlowRef = ref<HTMLElement | null>(null);
  const nodeRefs = ref<Record<number, HTMLElement | null>>({});
  const stepCardRefs = ref<Record<number, HTMLElement | null>>({});
  /** 条件行 ref ── key=`${stepNo}:${condIdx}`,画布芯片点击时 scrollIntoView 用 */
  const conditionRowRefs = ref<Record<string, HTMLElement | null>>({});
  /** 各 RULE_MATCH step 的"原始配置"折叠展开状态(默认折叠) */
  const rawConfigOpen = ref<Record<number, boolean>>({});

  // ===== 画布拖拽状态 =====
  const isDragging = ref(false);
  const dragMoved = ref(false);
  let dragStartX = 0;
  let dragStartScroll = 0;
  /** 距离阈值:小于此值视为点击,大于此值视为拖拽。避免点击 node-card 时被吞掉。 */
  const DRAG_THRESHOLD_PX = 5;

  const [registerDrawer] = useDrawerInner(async (data) => {
    trace.value = null;
    focusedStep.value = undefined;
    nodeRefs.value = {};
    stepCardRefs.value = {};
    conditionRowRefs.value = {};
    rawConfigOpen.value = {};
    canvasScale.value = 1;
    if (!data?.traceId) return;
    loading.value = true;
    try {
      const res = await traceDetail(data.traceId, data.bridgeRuleId);
      trace.value = res;
      const steps = res?.steps ?? [];
      const lastFail = [...steps].reverse().find((s) => s.status === '01');
      focusedStep.value = (lastFail ?? steps[steps.length - 1])?.stepNo;
      // 内容渲染完成后自动全图适配:
      // - 节点多 → 自动缩到合适比例显示全部
      // - 节点少 → 保持 scale=1,scrollLeft 归零
      // 用 setTimeout(0) 等 v-for 节点全部 mount 完毕,canvasFlowRef.scrollWidth 才准
      setTimeout(() => canvasFitAll(), 50);
    } finally {
      loading.value = false;
    }
  });

  function setNodeRef(stepNo: number | undefined, el: any) {
    if (stepNo == null) return;
    nodeRefs.value[stepNo] = el as HTMLElement | null;
  }

  function setStepCardRef(stepNo: number | undefined, el: any) {
    if (stepNo == null) return;
    stepCardRefs.value[stepNo] = el as HTMLElement | null;
  }

  function scrollToStep(stepNo?: number) {
    if (stepNo == null) return;
    focusedStep.value = stepNo;
    nextTick(() => {
      stepCardRefs.value[stepNo]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }

  function onFocusChanged(stepNo: number) {
    nextTick(() => {
      nodeRefs.value[stepNo]?.scrollIntoView({
        behavior: 'smooth',
        inline: 'center',
        block: 'nearest',
      });
      stepCardRefs.value[stepNo]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }

  /**
   * 步骤卡片点击 ── 与下拉框 onFocusChanged 行为对齐:
   * 同时高亮上方设计链路节点 + 滚动到该节点中心。
   * 注意:下拉框 v-model 直接绑 focusedStep,这里要手动 set focusedStep 再触发滚动。
   */
  function onStepCardClick(stepNo?: number) {
    if (stepNo == null) return;
    focusedStep.value = stepNo;
    onFocusChanged(stepNo);
  }

  // ============================== 画布拖拽 ==============================
  /**
   * 鼠标按下 ── 监听器挂 document 而非 wrap,避免鼠标移出后释放不掉。
   */
  function onCanvasMouseDown(e: MouseEvent) {
    if (e.button !== 0) return; // 只响应左键
    const wrap = canvasWrapRef.value;
    if (!wrap) return;
    isDragging.value = true;
    dragMoved.value = false;
    dragStartX = e.clientX;
    dragStartScroll = wrap.scrollLeft;
    document.addEventListener('mousemove', onCanvasMouseMove);
    document.addEventListener('mouseup', onCanvasMouseUp);
    e.preventDefault(); // 阻止文字选中
  }

  function onCanvasMouseMove(e: MouseEvent) {
    if (!isDragging.value) return;
    const wrap = canvasWrapRef.value;
    if (!wrap) return;
    const dx = e.clientX - dragStartX;
    if (Math.abs(dx) > DRAG_THRESHOLD_PX) dragMoved.value = true;
    wrap.scrollLeft = dragStartScroll - dx;
  }

  function onCanvasMouseUp() {
    isDragging.value = false;
    document.removeEventListener('mousemove', onCanvasMouseMove);
    document.removeEventListener('mouseup', onCanvasMouseUp);
  }

  /**
   * 触屏拖拽 ── 单指起手才启动,避免与 pinch-zoom 冲突。
   * touchmove 用 passive:false 才能 preventDefault 阻止页面整屏滚动。
   */
  function onCanvasTouchStart(e: TouchEvent) {
    if (e.touches.length !== 1) return;
    const wrap = canvasWrapRef.value;
    if (!wrap) return;
    isDragging.value = true;
    dragMoved.value = false;
    dragStartX = e.touches[0].clientX;
    dragStartScroll = wrap.scrollLeft;
    document.addEventListener('touchmove', onCanvasTouchMove, { passive: false });
    document.addEventListener('touchend', onCanvasTouchEnd);
    document.addEventListener('touchcancel', onCanvasTouchEnd);
  }

  function onCanvasTouchMove(e: TouchEvent) {
    if (!isDragging.value || e.touches.length !== 1) return;
    const wrap = canvasWrapRef.value;
    if (!wrap) return;
    const dx = e.touches[0].clientX - dragStartX;
    if (Math.abs(dx) > DRAG_THRESHOLD_PX) {
      dragMoved.value = true;
      e.preventDefault(); // 阻止页面跟随滚动
    }
    wrap.scrollLeft = dragStartScroll - dx;
  }

  function onCanvasTouchEnd() {
    isDragging.value = false;
    document.removeEventListener('touchmove', onCanvasTouchMove);
    document.removeEventListener('touchend', onCanvasTouchEnd);
    document.removeEventListener('touchcancel', onCanvasTouchEnd);
  }

  /** node-card 点击 ── 拖拽距离超过阈值时吞掉点击,只在真实点击时聚焦。 */
  function onNodeClick(stepNo?: number) {
    if (dragMoved.value) {
      dragMoved.value = false;
      return;
    }
    if (stepNo != null) focusedStep.value = stepNo;
  }

  /** 卸载兜底 ── 用户在拖拽中关闭 Drawer 时残留 listener 清掉 */
  onBeforeUnmount(() => {
    document.removeEventListener('mousemove', onCanvasMouseMove);
    document.removeEventListener('mouseup', onCanvasMouseUp);
    document.removeEventListener('touchmove', onCanvasTouchMove);
    document.removeEventListener('touchend', onCanvasTouchEnd);
    document.removeEventListener('touchcancel', onCanvasTouchEnd);
  });

  /**
   * 全图适配 ── 实测容器/内容宽度算 scale,缩放后 scrollLeft 归零。
   *
   * <p>关键实现细节:
   * <ul>
   *   <li>用 {@code requestAnimationFrame} 而非 {@code nextTick} ── rAF 保证浏览器
   *       已完成 layout/paint,measurement 才准确;nextTick 只等 Vue 响应式 flush,
   *       layout 未必更新</li>
   *   <li>双 rAF(rAF inside rAF) ── 确保即使 scale 重置触发了重排,measurement 也在
   *       重排完成之后</li>
   *   <li>当 scale 已经是 1 时也强制归零 scrollLeft ── 避免"按钮无响应"的视觉错觉,
   *       同时把可能滚动到中段的视图拉回起点</li>
   *   <li>当 wrap.clientWidth 为 0(组件刚挂载但未 layout)时降级为 scale=1,不抛错</li>
   * </ul>
   */
  function canvasFitAll() {
    // ⚠ 不要在这里直接设 canvasScale=1。如果当前已经是 1,Vue 不会触发更新,
    // 后续 measurement 仍然在旧 scale 下进行 ── 但 scrollWidth 与 transform 无关,
    // 所以测量本身可靠。问题在于:用户期望"点了按钮要有反应"。
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        const wrap = canvasWrapRef.value;
        const flow = canvasFlowRef.value;
        if (!wrap || !flow) return;

        const wrapW = Math.max(0, wrap.clientWidth - 32);
        const flowW = flow.scrollWidth;
        let nextScale: number;
        if (wrapW <= 0 || flowW <= 0) {
          // dims 异常(还没 layout),保守用 1 不抛错
          nextScale = 1;
        } else if (flowW <= wrapW) {
          nextScale = 1;
        } else {
          nextScale = Math.max(0.5, +(wrapW / flowW).toFixed(3));
        }

        canvasScale.value = nextScale;
        // 总是归零 ── 即便 scale 没变,这一步也能给用户视觉反馈
        // (滚到中段时把视图拉回起点)
        wrap.scrollLeft = 0;
      });
    });
  }

  /** 实际尺寸 ── scale=1,scrollLeft 保持(用户可以拖看具体节点)。 */
  function canvasResetActual() {
    canvasScale.value = 1;
  }

  /** 复制 traceId 到剪贴板（排查时常用） */
  function handleCopyTraceId() {
    if (!trace.value?.traceId) return;
    if (copyTextToClipboard(trace.value.traceId)) {
      createMessage.success(t('common.tips.copySuccess'));
    }
  }

  /** 死信重放（仅 status=03 时按钮显示） */
  async function handleReplay() {
    if (!trace.value?.traceId) return;
    try {
      const newTraceId = await replay(trace.value.traceId);
      createMessage.success(
        t('iot.rule.integration.log.tips.replaySuccess') + (newTraceId ?? ''),
      );
      // 通知父级（list 页 / bridge detail 嵌入）刷新数据 + 状态卡
      emit('replayed', String(newTraceId ?? ''));
    } catch (e: any) {
      createMessage.error(
        t('iot.rule.integration.log.tips.replayFailed') + ': ' + (e?.message ?? ''),
      );
    }
  }

  // ============================== 工具 ==============================
  function getStatusTagColor(status?: string): string {
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

  function getStatusLabel(status?: string): string {
    switch (status) {
      case '00':
        return t('iot.rule.integration.log.detail.success');
      case '01':
        return t('iot.rule.integration.log.detail.failed');
      case '02':
        return t('iot.rule.integration.log.detail.skipped');
      case '03':
        return t('iot.rule.integration.log.action.replay');
      default:
        return '-';
    }
  }

  function getStepClass(status?: string): string {
    if (status === '00') return 'ok';
    if (status === '01') return 'fail';
    if (status === '02') return 'skip';
    return 'unknown';
  }

  function getArrowClass(prev?: string, next?: string): string {
    if (prev === '01' || next === '01') return 'fail';
    if (prev === '00' && next === '00') return 'ok';
    return 'default';
  }

  function getStepIconComp(stepType?: string) {
    switch (stepType) {
      case 'INGEST':
        return LoginOutlined;
      case 'RULE_MATCH':
        return FilterOutlined;
      case 'RATE_LIMIT':
        return ThunderboltOutlined;
      case 'TRANSFORM':
        return SwapOutlined;
      case 'SINK_SEND':
        return SendOutlined;
      case 'DEAD_LETTER':
        return AlertOutlined;
      case 'INBOUND_FORWARD':
        return DownloadOutlined;
      default:
        return ApiOutlined;
    }
  }

  function getBranch(s: BridgeExecutionStepResultVO): string | undefined {
    if (!s.extensionJson) return undefined;
    try {
      const ext = JSON.parse(s.extensionJson);
      if (s.stepType === 'RULE_MATCH') return ext?.branch ?? undefined;
      if (s.stepType === 'SINK_SEND') return ext?.sinkType ?? undefined;
      if (s.stepType === 'TRANSFORM') return ext?.scriptName ?? undefined;
    } catch {
      /* ignore */
    }
    return undefined;
  }

  function getDescription(s: BridgeExecutionStepResultVO): string | undefined {
    try {
      if (s.stepType === 'INGEST' && s.inputSummary) {
        const obj = JSON.parse(s.inputSummary);
        const fields = obj && typeof obj === 'object' ? Object.keys(obj).length : 0;
        return t('iot.rule.integration.log.detail.descIngest', { count: fields });
      }
      if (s.stepType === 'RULE_MATCH' && s.extensionJson) {
        const ext = JSON.parse(s.extensionJson);
        const matched = ext?.matchedConditions ?? [];
        return t('iot.rule.integration.log.detail.descRuleMatch', {
          hit: matched.length,
          total: ext?.totalConditions ?? matched.length,
        });
      }
      if (s.stepType === 'SINK_SEND' && s.extensionJson) {
        const ext = JSON.parse(s.extensionJson);
        return t('iot.rule.integration.log.detail.descSinkSend', {
          total: ext?.commandsTotal ?? 1,
          success: ext?.success ?? (s.status === '00' ? 1 : 0),
          failed: ext?.failed ?? (s.status === '01' ? 1 : 0),
        });
      }
    } catch {
      /* ignore */
    }
    return undefined;
  }

  function hasMetricChips(s: BridgeExecutionStepResultVO): boolean {
    return ['SINK_SEND', 'RULE_MATCH'].includes(s.stepType ?? '');
  }

  function getMetricChips(s: BridgeExecutionStepResultVO) {
    if (!s.extensionJson) return [];
    try {
      const ext = JSON.parse(s.extensionJson);
      if (s.stepType === 'SINK_SEND') {
        return [
          {
            label: t('iot.rule.integration.log.detail.metric.commands'),
            value: ext?.commandsTotal ?? 1,
            tone: 'info',
          },
          {
            label: t('iot.rule.integration.log.detail.metric.success'),
            value: ext?.success ?? '-',
            tone: 'ok',
          },
          {
            label: t('iot.rule.integration.log.detail.metric.failed'),
            value: ext?.failed ?? '-',
            tone: 'fail',
          },
        ];
      }
      if (s.stepType === 'RULE_MATCH') {
        return [
          {
            label: t('iot.rule.integration.log.detail.metric.matchType'),
            value: ext?.matchType ?? 'VISUAL',
            tone: 'info',
          },
          {
            label: t('iot.rule.integration.log.detail.metric.matchHit'),
            value: `${ext?.matchedConditions?.length ?? 0}/${ext?.totalConditions ?? 0}`,
            tone: 'ok',
          },
          {
            label: t('iot.rule.integration.log.detail.metric.branch'),
            value: ext?.branch ?? '-',
            tone: 'info',
          },
        ];
      }
    } catch {
      /* ignore */
    }
    return [];
  }

  function formatJson(raw?: string): string {
    if (!raw) return '-';
    try {
      return JSON.stringify(JSON.parse(raw), null, 2);
    } catch {
      return raw;
    }
  }

  /**
   * 解析 RULE_MATCH step 的 extensionJson.matchedConditions ── 后端在 SinkDispatcher
   * 用 LinkedHashMap 写入,字段保持 { hit, label, field, expected, actual? } 顺序。
   * 解析失败或字段缺失返空数组,UI 自然降级到只显示 metric chip。
   */
  function getMatchedConditions(s: BridgeExecutionStepResultVO): Array<Record<string, any>> {
    if (s.stepType !== 'RULE_MATCH' || !s.extensionJson) return [];
    try {
      const ext = JSON.parse(s.extensionJson);
      const arr = ext?.matchedConditions;
      return Array.isArray(arr) ? arr : [];
    } catch {
      return [];
    }
  }

  /**
   * 命中条件值的展示 ── 数组转 "a, b" / 对象 JSON / 其他保持原文,简短可读。
   */
  function formatConditionValue(v: any): string {
    if (v == null) return '-';
    if (Array.isArray(v)) return v.join(', ');
    if (typeof v === 'object') {
      try {
        return JSON.stringify(v);
      } catch {
        return String(v);
      }
    }
    return String(v);
  }

  // ============================== RULE_MATCH 增强:kind / matcher / 原始配置 ==============================

  /**
   * 后端 {@code RuleMatchConditions.Kind} 枚举 → 前端图标(基于 ant-design-vue icons)。
   * 未知 kind / 老数据(没 kind 字段) → 默认 BranchesOutlined。
   */
  function getMatchKindIcon(kind?: string) {
    switch (kind) {
      case 'PRODUCT':
        return AppstoreOutlined;
      case 'TOPIC':
        return BranchesOutlined;
      case 'ACTION_TYPE':
        return GoldOutlined;
      case 'DEVICE_ID':
        return MobileOutlined;
      case 'DEVICE_TAG':
        return TagsOutlined;
      case 'DEVICE_GROUP':
        return TeamOutlined;
      case 'PAYLOAD_FILTER':
        return CodeOutlined;
      case 'TIME_WINDOW':
        return ClockCircleOutlined;
      default:
        return BranchesOutlined;
    }
  }

  /**
   * 维度名取舍优先级:i18n(按 kind) → c.label(后端兜底名) → c.field(原始字段路径)。
   * 老数据(没 kind 字段)走 label/field 降级。
   */
  function getMatchKindLabel(c: Record<string, any>): string {
    if (c?.kind) {
      const k = `iot.rule.integration.log.step.matchKind.${c.kind}`;
      const v = t(k);
      // 未配 i18n 时 useI18n 返回 key 本身,做防御回退
      if (v && v !== k) return v;
    }
    return c?.label || c?.field || '-';
  }

  /**
   * matcher 类型(EXACT / WILDCARD / MQTT_TOPIC / JSON_PATH / SPEL / GROOVY / CRON / ANY_IN_LIST)
   * → i18n 标签;无 matcher 时返空(模板上不显示 chip)。
   */
  function getMatcherLabel(matcher?: string): string {
    if (!matcher) return '';
    const k = `iot.rule.integration.log.step.matcherType.${matcher}`;
    const v = t(k);
    return v && v !== k ? v : matcher;
  }

  /**
   * matcher chip 颜色 ── 表达式类(SpEL/JSONPath/Groovy)着重提醒"高级条件",
   * 通配符浅色,精确匹配中性。
   */
  function getMatcherTagColor(matcher?: string): string {
    switch (matcher) {
      case 'WILDCARD':
        return 'cyan';
      case 'EXACT':
        return 'blue';
      case 'ANY_IN_LIST':
        return 'geekblue';
      case 'MQTT_TOPIC':
        return 'purple';
      case 'JSON_PATH':
      case 'SPEL':
      case 'GROOVY':
        return 'orange';
      case 'CRON':
        return 'magenta';
      default:
        return 'default';
    }
  }

  /** 画布条件芯片的 tooltip ── 多行显示 field / expected / reason,信息密度高 */
  function getConditionTooltip(c: Record<string, any>): string {
    const parts: string[] = [];
    const matcher = getMatcherLabel(c.matcher);
    parts.push(matcher ? `${getMatchKindLabel(c)} (${matcher})` : getMatchKindLabel(c));
    if (c.field) parts.push(`field: ${c.field}`);
    if (c.expected != null) parts.push(`expected: ${formatConditionValue(c.expected)}`);
    if (c.actual != null) parts.push(`actual: ${formatConditionValue(c.actual)}`);
    if (c.reason) parts.push(c.reason);
    return parts.join('\n');
  }

  function setConditionRowRef(stepNo: number | undefined, idx: number, el: any) {
    if (stepNo == null) return;
    conditionRowRefs.value[`${stepNo}:${idx}`] = el as HTMLElement | null;
  }

  /**
   * 画布条件芯片点击 ── 联动:聚焦该 step + 滚动下方 step-card 到视口 + 高亮目标条件行。
   */
  function onConditionChipClick(stepNo: number | undefined, idx: number) {
    if (stepNo == null) return;
    focusedStep.value = stepNo;
    nextTick(() => {
      const target = conditionRowRefs.value[`${stepNo}:${idx}`];
      if (target) {
        target.scrollIntoView({ behavior: 'smooth', block: 'center' });
        // 闪烁高亮 1.2s 提示用户当前命中行
        target.classList.add('condition-flash');
        setTimeout(() => target.classList.remove('condition-flash'), 1200);
      } else {
        // 条件行未渲染时 fallback 到 step 卡片
        stepCardRefs.value[stepNo]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  /**
   * 取 RULE_MATCH step 的原始 matchConfigJson ── 后端写在 extensionJson.rawConfig,
   * 默认折叠不渲染,用户点击展开后才走 formatJson(避免大配置每次刷抽屉都解析)。
   */
  function getRawMatchConfig(s: BridgeExecutionStepResultVO): string | null {
    if (s.stepType !== 'RULE_MATCH' || !s.extensionJson) return null;
    try {
      const ext = JSON.parse(s.extensionJson);
      return ext?.rawConfig || null;
    } catch {
      return null;
    }
  }

  function toggleRawConfig(stepNo?: number) {
    if (stepNo == null) return;
    rawConfigOpen.value[stepNo] = !rawConfigOpen.value[stepNo];
  }
</script>

<style lang="less" scoped>
  .trace-drawer :deep(.ant-drawer-body) {
    background: #f6f8fb;
    padding: 0;
  }

  .trace-spin {
    display: block;
  }

  /* ===== 通用 ===== */
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
  }

  .hint {
    color: #6b7280;
    font-size: 12px;
  }

  /* ===== 顶部 Banner ===== */
  .banner-card {
    margin: 16px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  }

  .banner-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .banner-left {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .status-tag {
    font-size: 14px;
    padding: 6px 14px;
    margin: 0;
    font-weight: 600;
    border-radius: 6px;
  }

  .banner-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .banner-title {
    font-size: 18px;
    font-weight: 600;
    color: #2a3547;
    line-height: 1.3;
  }

  .banner-sub {
    font-size: 12px;
    color: #a0aec0;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .trace-id-text {
      color: #6b7280;
      font-family: 'JetBrains Mono', Menlo, monospace;
    }

    .copy-icon {
      color: #5d87ff;
      cursor: pointer;
      transition: color 0.15s;

      &:hover {
        color: #2563eb;
      }
    }

    .replay-btn {
      margin-left: 8px;
      border-radius: 6px;
    }
  }

  .banner-stats {
    display: flex;
    gap: 24px;
    align-items: center;
    flex-wrap: wrap;
  }

  .stat-item {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 96px;
  }

  .stat-label {
    color: #6b7280;
    font-size: 12px;
  }

  .stat-value {
    color: #2a3547;
    font-size: 16px;
    font-weight: 700;

    &.highlight {
      color: #5d87ff;
      font-size: 18px;
    }

    &.muted {
      font-weight: 500;
      color: #4b5563;
      font-size: 13px;
    }

    small {
      font-size: 12px;
      color: #6b7280;
      font-weight: 400;
      margin-left: 2px;
    }
  }

  /* ===== 执行路径 chip 链 ===== */
  .path-section {
    margin-top: 16px;
    border-top: 1px dashed #e8eaf0;
    padding-top: 16px;
  }

  .path-label {
    color: #6b7280;
    font-size: 13px;
    margin-bottom: 8px;
  }

  .path-chain {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
  }

  .path-chip {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.15s ease;
    border: 1px solid #e8eaf0;
    background: #fff;
    color: #4b5563;

    &.ok {
      background: #ecfdf5;
      border-color: #a7f3d0;
      color: #047857;
    }

    &.fail {
      background: #fef2f2;
      border-color: #fecaca;
      color: #b91c1c;
    }

    &.skip {
      background: #f3f4f6;
      border-color: #d1d5db;
      color: #6b7280;
    }

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
    }
  }

  .path-arrow {
    color: #a0aec0;
    font-size: 14px;
  }

  /* ===== Empty 占位 ===== */
  .empty-card {
    margin: 0 16px 16px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
    padding: 40px 0;
  }

  /* ===== 设计链路回放（节点流式图谱）===== */
  .canvas-card {
    margin: 0 16px 16px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
    }
  }

  .canvas-wrap {
    background: radial-gradient(circle, #e2e8f0 1px, transparent 1px) 0 0 / 16px 16px,
      #fafbff;
    border-radius: 8px;
    /* 底部 padding 加大,给 RULE_MATCH 节点下挂的 absolute 条件芯片栏留显示空间 */
    padding: 32px 16px 88px;
    overflow-x: auto;
    overflow-y: hidden;
    cursor: grab;
    user-select: none;
    /* 平滑滚动:点 fitAll / 自动聚焦节点滚到目标位置时不卡顿 */
    scroll-behavior: smooth;
    /* 触屏惯性滚动 */
    -webkit-overflow-scrolling: touch;
    /* 阻止浏览器默认横向 swipe 手势(双指/触控板) */
    overscroll-behavior-x: contain;

    &.dragging {
      cursor: grabbing;
      /* 拖拽中关闭平滑滚动,scrollLeft 实时跟手 */
      scroll-behavior: auto;
    }

    /* 拖拽中阻止子元素响应 hover 动画(避免拖动时 node-card 抖动) */
    &.dragging .node-card,
    &.dragging .node-card:hover {
      transform: none;
      transition: none;
    }
  }

  /* 拖拽提示横幅 */
  .canvas-hint {
    margin: -8px 16px 8px;
    padding: 6px 12px;
    color: #6b7280;
    font-size: 12px;
    background: rgba(93, 135, 255, 0.06);
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .canvas-flow {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: max-content;
    gap: 0;
    transition: transform 0.3s ease;
  }

  .node-card {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16px 24px;
    min-width: 140px;
    background: #fff;
    border: 2px solid #e8eaf0;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
    cursor: pointer;
    transition: all 0.2s ease;

    &.ok {
      border-color: #13deb9;

      .node-icon {
        background: rgba(19, 222, 185, 0.12);
        color: #13deb9;
      }

      .node-status-dot {
        background: #13deb9;
      }
    }

    &.fail {
      border-color: #fa4b4b;

      .node-icon {
        background: rgba(250, 75, 75, 0.12);
        color: #fa4b4b;
      }

      .node-status-dot {
        background: #fa4b4b;
        animation: pulse 1.6s infinite;
      }
    }

    &.skip {
      border-color: #d1d5db;
      opacity: 0.7;

      .node-icon {
        background: #f3f4f6;
        color: #9ca3af;
      }
    }

    &.focused {
      border-width: 3px;
      box-shadow: 0 4px 16px rgba(93, 135, 255, 0.25);
      transform: translateY(-2px);
    }

    &:hover {
      transform: translateY(-2px);
    }
  }

  .node-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    background: rgba(93, 135, 255, 0.1);
    color: #5d87ff;
    margin-bottom: 6px;
  }

  .node-name {
    font-size: 13px;
    font-weight: 600;
    color: #2a3547;
    text-align: center;
    line-height: 1.3;
  }

  .node-type {
    font-size: 10px;
    color: #a0aec0;
    margin-top: 2px;
    letter-spacing: 0.4px;
  }

  .node-status-dot {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #d1d5db;
  }

  /* RULE_MATCH 节点徽章 ── 显示命中条件数 */
  .node-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: #5d87ff;
    color: #fff;
    font-size: 11px;
    font-weight: 700;
    line-height: 20px;
    text-align: center;
    box-shadow: 0 2px 4px rgba(93, 135, 255, 0.4);
  }

  /* 节点栈:主节点 + 下方条件芯片栏(画布分支兼容)
     conditions 用 absolute 脱流,避免撑高 stack 让旁边箭头与 node-card 中线错位。 */
  .node-stack {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .node-conditions {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
  }

  .branch-line {
    width: 2px;
    height: 14px;
    background: #cbd5e1;
  }

  .branch-chips {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 6px;
    max-width: 260px;
  }

  .branch-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 11px;
    font-weight: 500;
    cursor: pointer;
    user-select: none;
    transition: all 0.15s ease;
    border: 1px solid #e8eaf0;
    background: #fff;
    color: #4b5563;

    &.hit {
      background: #ecfdf5;
      border-color: #a7f3d0;
      color: #047857;
    }

    &.miss {
      background: #fef2f2;
      border-color: #fecaca;
      color: #b91c1c;
    }

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    }
  }

  /* 纯 CSS 横线 + 三角箭头(::before 横线,::after 三角形)
     用 flex align-items center 让箭头与 node-card 真正垂直居中,
     跨浏览器/缩放后不会偏移。 */
  .node-arrow {
    flex: 0 0 auto;
    position: relative;
    width: 48px;
    /* 占位高度 = node-card 大致高度,保证 flex 垂直居中正确 */
    align-self: stretch;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 4px;
    --arrow-color: #cbd5e1;
  }

  /* 横线 ── 留出右端 8px 给三角 */
  .node-arrow::before {
    content: '';
    display: block;
    width: calc(100% - 8px);
    height: 2px;
    background: var(--arrow-color);
    margin-right: 8px;
  }

  /* 三角箭头 */
  .node-arrow::after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-top: 5px solid transparent;
    border-bottom: 5px solid transparent;
    border-left: 8px solid var(--arrow-color);
  }

  .node-arrow.ok {
    --arrow-color: #13deb9;
  }

  .node-arrow.fail {
    --arrow-color: #fa4b4b;
  }

  /* 失败时横线用虚线效果 */
  .node-arrow.fail::before {
    background: repeating-linear-gradient(
      to right,
      var(--arrow-color) 0,
      var(--arrow-color) 4px,
      transparent 4px,
      transparent 8px
    );
  }

  @keyframes pulse {
    0%,
    100% {
      transform: scale(1);
      opacity: 1;
    }
    50% {
      transform: scale(1.4);
      opacity: 0.7;
    }
  }

  /* ===== 节点执行步骤 ===== */
  .steps-card {
    margin: 0 16px 16px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f2f5;
    }
  }

  .step-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .step-card {
    background: #fff;
    border: 1px solid #e8eaf0;
    border-left: 4px solid #cbd5e1;
    border-radius: 10px;
    padding: 16px 18px;
    transition: all 0.2s ease;
    cursor: pointer;

    &.ok {
      border-left-color: #13deb9;
    }

    &.fail {
      border-left-color: #fa4b4b;
    }

    &.skip {
      border-left-color: #d1d5db;
    }

    &.focused {
      box-shadow: 0 4px 16px rgba(93, 135, 255, 0.15);
      border-color: #5d87ff;
    }

    &:hover {
      transform: translateX(2px);
    }
  }

  .step-head {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .step-no {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #5d87ff;
    color: #fff;
    font-weight: 700;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .ok .step-no {
    background: #13deb9;
  }

  .fail .step-no {
    background: #fa4b4b;
  }

  .skip .step-no {
    background: #9ca3af;
  }

  .step-status-tag {
    margin: 0;
  }

  .step-name {
    font-size: 15px;
    font-weight: 600;
    color: #2a3547;
  }

  .step-branch-tag {
    margin: 0;
    font-size: 11px;
    border-radius: 4px;
  }

  .step-latency {
    margin-left: auto;
    color: #6b7280;
    font-size: 13px;
    font-weight: 600;
  }

  .step-time {
    color: #a0aec0;
    font-size: 12px;
    margin: 6px 0 10px;

    .step-type {
      color: #6b7280;
      letter-spacing: 0.3px;
    }
  }

  .step-desc {
    padding: 8px 12px;
    border-radius: 6px;
    font-size: 13px;
    margin-bottom: 12px;

    &.ok {
      background: #ecfdf5;
      color: #047857;
    }

    &.fail {
      background: #fef2f2;
      color: #b91c1c;
    }

    &.skip {
      background: #f3f4f6;
      color: #6b7280;
    }
  }

  .step-metric-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .metric-chip {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 6px 12px;
    border-radius: 6px;
    background: #f6f8fb;
    border: 1px solid #e8eaf0;
    min-width: 80px;

    .m-label {
      font-size: 11px;
      color: #6b7280;
    }

    .m-value {
      font-size: 14px;
      font-weight: 700;
      color: #2a3547;
    }

    &.ok .m-value {
      color: #13deb9;
    }

    &.fail .m-value {
      color: #fa4b4b;
    }

    &.info .m-value {
      color: #5d87ff;
    }
  }

  .step-block {
    margin-bottom: 10px;

    .block-title {
      font-size: 13px;
      font-weight: 600;
      color: #4b5563;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    &.error .block-title {
      color: #b91c1c;
    }
  }

  .code-block {
    margin: 0;
    padding: 12px 14px;
    background: #0f172a;
    color: #e2e8f0;
    border-radius: 8px;
    font-family: 'JetBrains Mono', Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    max-height: 280px;
    overflow: auto;
    white-space: pre-wrap;
    word-break: break-word;

    &.error {
      background: #450a0a;
      color: #fee2e2;
    }

    &.muted {
      background: #1e293b;
    }
  }

  /* ===== RULE_MATCH 命中条件明细 ===== */
  .conditions-block {
    .conditions-count-tag {
      margin-left: 8px;
      font-size: 11px;
      border-radius: 10px;
      font-weight: 600;
    }

    .condition-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .condition-row {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 12px 14px;
      border-radius: 8px;
      border: 1px solid #e8eaf0;
      background: #fff;
      font-size: 13px;
      transition: all 0.2s ease;

      &.hit {
        background: #f0fdf4;
        border-color: #a7f3d0;
      }

      &.miss {
        background: #fef2f2;
        border-color: #fecaca;
      }

      &.condition-flash {
        animation: condition-flash-anim 1.2s ease-out;
      }
    }

    .condition-head {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
    }

    .condition-hit-tag {
      margin: 0;
      font-size: 11px;
      border-radius: 4px;
    }

    .condition-kind-icon {
      color: #5d87ff;
      font-size: 14px;
    }

    .condition-label {
      font-weight: 600;
      color: #2a3547;
      font-size: 14px;
    }

    .matcher-tag {
      margin: 0;
      font-size: 11px;
      border-radius: 4px;
      font-weight: 500;
    }

    .condition-body {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 8px 10px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 6px;
      border-left: 3px solid #5d87ff;
    }

    .cond-field {
      display: flex;
      align-items: baseline;
      gap: 8px;
      flex-wrap: wrap;

      .cond-field-label {
        color: #6b7280;
        font-size: 12px;
        min-width: 56px;
      }

      .cond-field-value {
        background: #f3f4f6;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'JetBrains Mono', Menlo, Monaco, Consolas, monospace;
        font-size: 12px;
        color: #2a3547;
        word-break: break-all;
      }
    }

    .condition-reason {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      padding: 6px 10px;
      background: rgba(93, 135, 255, 0.08);
      border-radius: 6px;
      color: #4b5563;
      font-size: 12px;
      line-height: 1.5;

      :deep(svg) {
        color: #5d87ff;
        flex-shrink: 0;
        margin-top: 2px;
      }
    }
  }

  @keyframes condition-flash-anim {
    0% {
      box-shadow: 0 0 0 0 rgba(93, 135, 255, 0.6);
    }
    50% {
      box-shadow: 0 0 0 8px rgba(93, 135, 255, 0.15);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(93, 135, 255, 0);
    }
  }

  /* ===== 原始配置折叠 ===== */
  .raw-config-block {
    .raw-config-title {
      cursor: pointer;
      user-select: none;
      display: flex;
      align-items: center;
      gap: 6px;

      &:hover {
        color: #5d87ff;
      }
    }

    .raw-config-toggle {
      transition: transform 0.2s ease;
      font-size: 11px;
      color: #6b7280;

      &.open {
        transform: rotate(90deg);
      }
    }

    .raw-config-hint {
      margin-left: 8px;
      color: #a0aec0;
      font-size: 12px;
      font-weight: 400;
    }

    .raw-config-code {
      margin-top: 6px;
    }
  }
</style>
