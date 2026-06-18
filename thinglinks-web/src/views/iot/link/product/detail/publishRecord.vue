<template>
  <!-- 裸 div ── 父级 panel-card 已给固定高度 + flex,无需 PageWrapper -->
  <div class="publish-record-page">
    <!-- 工具栏:提示 + 版本号筛选 + 刷新(icon) -->
    <div class="pr-toolbar">
      <span class="pr-hint">{{ t('iot.link.product.publishRecord.hint') }}</span>
      <div class="pr-toolbar-right">
        <!--
          版本筛选 ── 支持搜索完整 versionNo:
          - label 留完整 versionNo,供 option-filter-prop 用完整值匹配粘贴的雪花 ID
          - optionLabelProp="shortLabel" 让选中态显示短版本,避免长 ID 撑大宽度
          - #option 插槽自定义下拉项:短 ID + tooltip 完整 ID
        -->
        <a-select
          v-model:value="versionFilter"
          :options="versionFilterOptions"
          :placeholder="t('iot.link.product.publishRecord.versionFilterPlaceholder')"
          allow-clear
          size="middle"
          style="width: 260px"
          show-search
          option-filter-prop="label"
          option-label-prop="shortLabel"
          :getPopupContainer="(triggerNode: any) => triggerNode.parentNode"
          @change="onVersionFilterChange"
        >
          <template #option="{ value, shortLabel }">
            <a-tooltip :title="value" placement="left">
              <span class="version-option-text">{{ shortLabel }}</span>
            </a-tooltip>
          </template>
        </a-select>
        <a-tooltip :title="t('common.title.refresh')">
          <a-button shape="circle" size="middle" @click="reload">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <!-- flexy 卡片列表 -->
    <div class="record-card-list">
      <div v-if="loading" class="loading-wrap">
        <a-spin />
      </div>

      <template v-else-if="records.length">
        <div
          v-for="record in records"
          :key="record.id"
          class="record-card"
          :class="['intent-' + record.intent, 'status-' + record.status]"
        >
          <!-- 左侧:状态色条 + 意图图标 -->
          <div class="card-rail">
            <component :is="getIntentIcon(record.intent)" class="rail-icon" />
          </div>

          <!-- 主体 -->
          <div class="card-body">
            <div class="card-head">
              <!-- 意图 chip:Flexy 风格,左侧色块 icon + 文字 -->
              <span :class="['intent-chip', 'intent-' + record.intent]">
                <component :is="getIntentIcon(record.intent)" class="chip-icon" />
                <span class="chip-label">{{ getIntentLabel(record.intent) }}</span>
              </span>

              <div class="version-change">
                <span
                  v-if="record.sourceVersion"
                  class="version-pill from"
                  :title="record.sourceVersion"
                >
                  {{ record.sourceVersion }}
                </span>
                <component
                  v-if="record.sourceVersion && record.intent !== 2"
                  :is="getArrowIcon(record.intent)"
                  class="arrow"
                />
                <span class="version-pill to" :title="record.targetVersion">
                  {{ record.targetVersion || '—' }}
                </span>
              </div>

              <a-badge
                class="status-badge"
                :status="getStatusBadge(record.status)"
                :text="getStatusLabel(record.status)"
              />
              <span class="duration">{{
                formatDuration(record.startedTime, record.finishedTime)
              }}</span>
            </div>

            <!-- 失败:截断摘要 ── 完整原因走详情弹窗(避免长堆栈撑爆卡片) -->
            <div v-if="record.status === 2 && record.failedReason" class="card-error">
              <ExclamationCircleFilled class="err-icon" />
              <span class="err-msg" :title="record.failedReason">
                {{ truncate(record.failedReason, 80) }}
              </span>
              <a-button type="link" size="small" class="err-more" @click="handleViewDetail(record)">
                {{ t('iot.link.product.publishRecord.action.viewFullReason') }}
              </a-button>
            </div>

            <!-- 成功 / 执行中:DDL 条数 + 完整列表入口 -->
            <div v-else-if="(record.ddlItems?.length ?? 0) > 0" class="card-ddls">
              <DatabaseOutlined class="ddl-icon" />
              <span class="ddl-count">
                {{
                  t('iot.link.product.publishRecord.ddlCount', {
                    n: record.ddlItems?.length ?? 0,
                  })
                }}
              </span>
              <a-button type="link" size="small" class="ddl-more" @click="handleViewDetail(record)">
                {{ t('iot.link.product.publishRecord.action.viewDdl') }}
              </a-button>
            </div>

            <!-- 策略执行结果战报(仅发布 intent=0 且有快照):全量总 / 灰度总+展开分组 / 影子总 -->
            <StrategyResultPanel
              v-if="record.intent === 0 && record.canaryResult"
              :result="record.canaryResult"
            />

            <!-- 备注(若有,以浅灰小条形式显示;长备注同样进详情弹窗) -->
            <div v-if="record.remark" class="card-remark" :title="record.remark">
              <FileTextOutlined class="remark-icon" />
              <span class="remark-text">{{ truncate(record.remark, 100) }}</span>
            </div>

            <div class="card-foot">
              <span class="time">{{ formatRelative(record.createdTime) }}</span>
              <span class="dot">·</span>
              <span class="operator">{{ echoMapText(record, 'createdBy') }}</span>

              <div class="actions">
                <a-button type="link" size="small" @click="handleViewDetail(record)">
                  <InfoCircleOutlined />
                  {{ t('iot.link.product.publishRecord.action.detail') }}
                </a-button>
                <!--
                  预览快照按钮:跟版本管理 tab 的「预览快照」是同一份组件 + 同一份文案,
                  让用户在哪个 tab 都看到一致的入口语义,降低认知 / 维护成本。
                  清理(intent=2)操作的目标版本快照已被删,按钮 disabled。
                -->
                <a-tooltip
                  :title="
                    canPreviewSnapshot(record)
                      ? ''
                      : t('iot.link.product.publishRecord.action.snapshotUnavailableTip')
                  "
                >
                  <a-button
                    type="link"
                    size="small"
                    :disabled="!canPreviewSnapshot(record)"
                    @click="handlePreviewSnapshot(record)"
                  >
                    <FileSearchOutlined />
                    {{ t('iot.link.product.versionList.preview.entry') }}
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </template>

      <a-empty
        v-else-if="!records.length && !loading"
        :description="t('iot.link.product.publishRecord.empty')"
      />

      <!-- 无限滚动 sentinel:进入视口 → 触发 loadMore -->
      <div ref="sentinelRef" class="record-sentinel">
        <a-spin v-if="loadingMore" size="small" />
        <span v-else-if="!hasMore && records.length" class="end-text">
          {{ t('iot.link.product.publishRecord.noMore', { n: records.length }) }}
        </span>
      </div>
    </div>

    <PublishRecordDetailModal @register="registerDetailModal" />
    <SnapshotPreviewModal @register="registerPreviewModal" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import {
    ArrowRightOutlined,
    RollbackOutlined,
    DeleteOutlined,
    RocketOutlined,
    DatabaseOutlined,
    FileSearchOutlined,
    ExclamationCircleFilled,
    InfoCircleOutlined,
    FileTextOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';
  import { listByProduct as listProductVersions } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionResultVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { formatSnapshotId } from '/@/utils/iot/version';
  import { page } from '/@/api/iot/link/productPublishRecord/productPublishRecord';
  import type { ProductPublishRecordResultVO } from '/@/api/iot/link/productPublishRecord/model/productPublishRecordModel';
  import { echoMapText } from '/@/utils/echo';
  import { useModal } from '/@/components/Modal';
  import PublishRecordDetailModal from './PublishRecordDetailModal.vue';
  import SnapshotPreviewModal from './SnapshotPreviewModal.vue';
  import StrategyResultPanel from './StrategyResultPanel.vue';

  const props = defineProps<{
    productIdentification: string;
  }>();

  const { t } = useI18n();

  // ──────────── 无限滚动状态 ────────────
  /** 每页固定 12 条;无限滚动模式下用户无需感知 page size。 */
  const PAGE_SIZE = 12;
  const records = ref<ProductPublishRecordResultVO[]>([]);
  /** 已加载到第几页(0 = 未加载)。 */
  const currentPage = ref(0);
  /** 是否还有下一页。 */
  const hasMore = ref(true);
  /** 首屏加载中。 */
  const loading = ref(false);
  /** 下滑追加加载中。 */
  const loadingMore = ref(false);
  const sentinelRef = ref<HTMLElement | null>(null);
  let observer: IntersectionObserver | null = null;

  // ──────────── 版本筛选 ────────────
  /** 用户选中的版本号(空 = 所有);切换时触发后端重查(走 model.targetVersion 过滤)。 */
  const versionFilter = ref<string | undefined>(undefined);
  /** 产品全量版本列表(给筛选下拉用),独立 fetch 一次,避免依赖滚动加载已 fetch 的版本 */
  const allVersions = ref<ProductVersionResultVO[]>([]);

  /**
   * 版本筛选下拉选项。
   *
   * <p>三段字段各司其职:</p>
   * <ul>
   *   <li>{@code value}:真实选中值,提交给后端 model.targetVersion</li>
   *   <li>{@code label}:完整 versionNo ── 给 antd <code>option-filter-prop="label"</code>
   *       做"用户粘贴完整雪花 ID 也能搜索"</li>
   *   <li>{@code shortLabel}:短版本号 ── 给 <code>option-label-prop="shortLabel"</code>
   *       做选中态紧凑显示,避免长 ID 撑大 select 宽度;同时被 #option 插槽用作下拉项主文本</li>
   * </ul>
   */
  const versionFilterOptions = computed(() =>
    allVersions.value
      .filter((v) => !!v.versionNo)
      .map((v) => ({
        value: v.versionNo,
        label: v.versionNo,
        shortLabel: formatSnapshotId(v.versionNo!) || v.versionNo,
      })),
  );

  /** 拉产品全部版本(给筛选下拉用)。 */
  async function loadAllVersions() {
    if (!props.productIdentification) return;
    try {
      const res = await listProductVersions(props.productIdentification);
      allVersions.value = Array.isArray(res) ? (res as ProductVersionResultVO[]) : [];
    } catch {
      allVersions.value = [];
    }
  }

  /** 切换版本筛选:重置 records + sentinel 重新观察 + 重新拉。 */
  function onVersionFilterChange() {
    fetchPage(true);
  }

  /**
   * 拉取下一页 / 重置首屏。
   *
   * <p><b>无限滚动模式</b>:reset=true 时清空已加载列表 + 回到 page=1;reset=false 追加。
   * 并发守卫避免 IntersectionObserver 在 sentinel 进入视口 / 数据未撑出 scrollbar 时双触发。</p>
   */
  async function fetchPage(reset: boolean) {
    if (!props.productIdentification) return;
    if (loading.value || loadingMore.value) return;
    if (reset) {
      currentPage.value = 0;
      hasMore.value = true;
      records.value = [];
      loading.value = true;
    } else {
      if (!hasMore.value) return;
      loadingMore.value = true;
    }
    try {
      const next = currentPage.value + 1;
      const res: any = await page({
        current: next,
        size: PAGE_SIZE,
        // model.targetVersion 走后端 SQL 过滤(后端 PageQuery 已有该字段),
        // 比前端 filter 更准确(无限滚动场景下,前端只能滤已加载的页)
        model: {
          productIdentification: props.productIdentification,
          targetVersion: versionFilter.value || undefined,
        },
      } as any);
      const newRecords: ProductPublishRecordResultVO[] = Array.isArray(res?.records)
        ? res.records
        : [];
      records.value.push(...newRecords);
      currentPage.value = next;
      hasMore.value = next < (res?.pages ?? 0);
    } finally {
      loading.value = false;
      loadingMore.value = false;
    }

    // 兜底:数据加载完毕后 nextTick 检查 sentinel 是否仍在视口
    // 首屏 12 条不够撑出 scrollbar 时,sentinel 一直可见 → 继续拉直到撑满 / hasMore=false
    await nextTick();
    if (
      hasMore.value &&
      sentinelRef.value &&
      isElementInViewport(sentinelRef.value as HTMLElement)
    ) {
      fetchPage(false);
    }
  }

  /** 判断元素是否在视口内(用于 fetchPage 兜底)。 */
  function isElementInViewport(el: HTMLElement): boolean {
    const rect = el.getBoundingClientRect();
    return (
      rect.top < (window.innerHeight || document.documentElement.clientHeight) && rect.bottom > 0
    );
  }

  /** 工具栏刷新:重置首屏(用户主动刷新一定回到顶部 + 拉最新)。 */
  function reload() {
    fetchPage(true);
  }

  /**
   * 设置 IntersectionObserver 监听 sentinel。
   *
   * <p>不用 scroll 事件 ── 因为父容器 / 自身 scrollbar 都可能 trigger,而我们不确定真正
   * 滚动的是哪一层(tab body / 整个详情页 / panel 本身)。IntersectionObserver 默认观察
   * viewport,无论哪一层滚动只要 sentinel 进入视口就会触发,这是无限滚动最鲁棒的实现。</p>
   */
  function setupObserver() {
    if (!sentinelRef.value || observer) return;
    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && hasMore.value && !loading.value && !loadingMore.value) {
          fetchPage(false);
        }
      },
      { rootMargin: '120px' },
    );
    observer.observe(sentinelRef.value as Element);
  }

  onMounted(async () => {
    // 并行:筛选下拉的版本列表 + 首屏 records 加载(两个独立查询)
    loadAllVersions();
    await fetchPage(true);
    await nextTick();
    setupObserver();
  });

  onBeforeUnmount(() => {
    observer?.disconnect();
    observer = null;
  });

  // ──────────── 文案 / 颜色 / 图标 ────────────

  function getIntentLabel(intent?: number): string {
    return t(`iot.link.product.publishRecord.intent.${intent}`);
  }

  function getIntentIcon(intent?: number) {
    if (intent === 0) return RocketOutlined;
    if (intent === 1) return RollbackOutlined;
    if (intent === 2) return DeleteOutlined;
    return DatabaseOutlined;
  }

  function getArrowIcon(intent?: number) {
    return intent === 1 ? RollbackOutlined : ArrowRightOutlined;
  }

  function getStatusLabel(status?: number): string {
    return t(`iot.link.product.publishRecord.status.${status}`);
  }

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

  function formatDuration(start?: string, end?: string): string {
    if (!start || !end) return '—';
    const ms = new Date(end).getTime() - new Date(start).getTime();
    if (ms < 0) return '—';
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  }

  function formatRelative(time?: string): string {
    if (!time) return '—';
    const diffMs = Date.now() - new Date(time).getTime();
    const min = Math.floor(diffMs / 60000);
    if (min < 1) return t('iot.link.product.publishRecord.time.justNow');
    if (min < 60) return t('iot.link.product.publishRecord.time.minutesAgo', { n: min });
    const hr = Math.floor(min / 60);
    if (hr < 24) return t('iot.link.product.publishRecord.time.hoursAgo', { n: hr });
    const day = Math.floor(hr / 24);
    if (day < 30) return t('iot.link.product.publishRecord.time.daysAgo', { n: day });
    return time;
  }

  // parsedDdls() 已删除 ── 后端 ddlItems 现在是 typed PublishDdlItemVO[],
  // 模板里直接 record.ddlItems?.length 即可,无需手动 JSON.parse。

  /** 文本截断:超过 n 字符末尾加 '...',title 属性 hover 看完整。 */
  function truncate(text?: string | null, n = 80): string {
    if (!text) return '';
    return text.length <= n ? text : text.slice(0, n) + '...';
  }

  // ──────────── 预览快照(跟版本管理 tab 复用同一个 SnapshotPreviewModal 组件) ────────────

  const [registerPreviewModal, { openModal: openPreviewModal }] = useModal();

  /**
   * 是否允许预览快照:
   * - targetVersion 必须存在
   * - intent=2(清理)的目标版本快照已被删除,无可看
   */
  function canPreviewSnapshot(record: ProductPublishRecordResultVO): boolean {
    return !!record.targetVersion && record.intent !== 2;
  }

  /**
   * 打开版本快照预览(跟 versionList.vue 的 handlePreview 是同一份语义)。
   *
   * <p>跟 versionList 那边不同的是:这里没有现成的 record.productSnapshotJson 可复用
   * (publishRecord 列表 API 不返 snapshot),只传 productIdentification + versionNo,
   * 让 SnapshotPreviewModal 内部走 fetchVersionDetail 拉。</p>
   */
  function handlePreviewSnapshot(record: ProductPublishRecordResultVO) {
    if (!canPreviewSnapshot(record)) return;
    openPreviewModal(true, {
      productIdentification: record.productIdentification,
      versionNo: record.targetVersion,
    });
  }

  // ──────────── 详情弹窗:DDL 全量 + 失败原因 + 元信息 ────────────

  const [registerDetailModal, { openModal: openDetailModal }] = useModal();

  function handleViewDetail(record: ProductPublishRecordResultVO) {
    openDetailModal(true, { record });
  }
</script>

<style lang="less" scoped>
  /* tab 内独立滚动 ── 裸 div + flex 链 */
  .publish-record-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
  }

  .pr-toolbar {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 8px 4px 12px;

    .pr-hint {
      flex: 1;
      min-width: 0;
      font-size: 12px;
      color: #97a1b0;
    }

    .pr-toolbar-right {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  // ──────────── 卡片列表 ──── tab 内独立滚动:占剩余高度,只在自己内部滑动 ──────
  .record-card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 0 4px 12px;
    flex: 1;
    min-height: 0;
    overflow-y: auto;

    .loading-wrap {
      display: flex;
      justify-content: center;
      padding: 60px 0;
    }
  }

  .record-card {
    display: flex;
    /* 关键:flex-shrink:0 防止在父级 flex column 容器(.record-card-list)空间不足时被等比压扁
     * (默认 flex-shrink:1 会让所有卡片均分父高度,卡片内容被截断 ── 用户只看到第一行 head,
     * 失败摘要 / DDL / 备注 / 操作按钮全消失。父级有 overflow-y:auto,总高超出时滚动)。 */
    flex-shrink: 0;
    background: #fff;
    border: 1px solid #eef0f4;
    border-radius: 12px;
    /* overflow:hidden 保留:卡片左侧 card-rail 渐变背景需要 + 圆角裁切 ── 不能改 visible */
    overflow: hidden;
    transition: box-shadow 0.2s ease, border-color 0.2s ease;

    &:hover {
      border-color: #e2e6ee;
      box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
    }

    // 失败行高亮
    &.status-2 {
      border-color: rgba(255, 77, 79, 0.28);
      background: rgba(255, 77, 79, 0.02);
    }

    .card-rail {
      width: 46px;
      flex-shrink: 0;
      display: flex;
      align-items: flex-start;
      justify-content: center;
      padding-top: 18px;

      .rail-icon {
        font-size: 18px;
      }
    }

    // 意图色条
    &.intent-0 .card-rail {
      background: linear-gradient(180deg, rgba(82, 196, 26, 0.12), rgba(82, 196, 26, 0.02));
      border-right: 3px solid #52c41a;
      .rail-icon {
        color: #52c41a;
      }
    }
    &.intent-1 .card-rail {
      background: linear-gradient(180deg, rgba(250, 140, 22, 0.12), rgba(250, 140, 22, 0.02));
      border-right: 3px solid #fa8c16;
      .rail-icon {
        color: #fa8c16;
      }
    }
    &.intent-2 .card-rail {
      background: linear-gradient(180deg, rgba(255, 77, 79, 0.12), rgba(255, 77, 79, 0.02));
      border-right: 3px solid #ff4d4f;
      .rail-icon {
        color: #ff4d4f;
      }
    }

    .card-body {
      flex: 1;
      padding: 16px 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      min-width: 0;
    }

    .card-head {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;

      .status-badge {
        margin-left: auto;
      }

      .duration {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 12px;
        color: #97a1b0;
        font-variant-numeric: tabular-nums;
      }
    }

    /* ─── 意图 chip:Flexy 风格(色块图标 + 文字 + 圆角胶囊) ─── */
    .intent-chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px 4px 6px;
      border-radius: 12px;
      font-size: 12.5px;
      font-weight: 600;
      line-height: 1.4;
      transition: all 0.18s ease;

      .chip-icon {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 22px;
        height: 22px;
        border-radius: 8px;
        font-size: 12px;
        color: #fff;
      }

      .chip-label {
        letter-spacing: 0.2px;
      }

      /* intent 0 - 发布:主蓝 */
      &.intent-0 {
        color: #2952cc;
        background: linear-gradient(135deg, #eef2ff 0%, #f0f7ff 100%);
        border: 1px solid rgba(93, 135, 255, 0.18);

        .chip-icon {
          background: linear-gradient(135deg, #5d87ff 0%, #49beff 100%);
          box-shadow: 0 4px 10px rgba(93, 135, 255, 0.35);
        }
      }

      /* intent 1 - 回滚:暖橙 */
      &.intent-1 {
        color: #b25e00;
        background: linear-gradient(135deg, #fff7e6 0%, #fff3d6 100%);
        border: 1px solid rgba(255, 174, 31, 0.22);

        .chip-icon {
          background: linear-gradient(135deg, #ffae1f 0%, #ffc94a 100%);
          box-shadow: 0 4px 10px rgba(255, 174, 31, 0.35);
        }
      }

      /* intent 2 - 清理:警示红 */
      &.intent-2 {
        color: #b13d3a;
        background: linear-gradient(135deg, #fff0ee 0%, #ffe5e0 100%);
        border: 1px solid rgba(250, 137, 107, 0.22);

        .chip-icon {
          background: linear-gradient(135deg, #fa896b 0%, #ff6a4a 100%);
          box-shadow: 0 4px 10px rgba(250, 137, 107, 0.35);
        }
      }
    }

    /* ─── 版本号 pill:完整 16 位 monospace 展示 ─── */
    .version-change {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      flex-wrap: nowrap;
      min-width: 0;

      .version-pill {
        padding: 3px 10px;
        border-radius: 8px;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 12px;
        line-height: 1.5;
        white-space: nowrap;
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.2px;
        user-select: text;

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

    /* 失败:截断摘要 + "查看完整原因"按钮 */
    .card-error {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: linear-gradient(135deg, #fff5f5 0%, #fff0ee 100%);
      border: 1px solid #ffd6d3;
      border-radius: 10px;
      font-size: 12.5px;
      flex-wrap: wrap;

      .err-icon {
        color: #fa896b;
        font-size: 14px;
        flex-shrink: 0;
      }

      .err-msg {
        flex: 1;
        min-width: 0;
        color: #b13d3a;
        line-height: 1.6;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .err-more {
        margin-left: auto;
        font-size: 12px;
        padding: 0 4px;
        height: 24px;
        color: #fa896b;

        &:hover {
          color: #ff6a4a;
        }
      }
    }

    /* DDL 条数 + "查看 DDL" 按钮(详细列表走详情弹窗) */
    .card-ddls {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12.5px;
      color: #5b6b82;

      .ddl-icon {
        color: #5d87ff;
      }

      .ddl-count {
        font-weight: 500;
      }

      .ddl-more {
        margin-left: auto;
        font-size: 12px;
        padding: 0 4px;
        height: 24px;
      }
    }

    /* 备注:浅灰条带 + 行截断 */
    .card-remark {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 12px;
      background: #f7f9fc;
      border-radius: 8px;
      font-size: 12.5px;
      color: #5b6b82;

      .remark-icon {
        color: #a0aec0;
        flex-shrink: 0;
      }

      .remark-text {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .card-foot {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #97a1b0;
      padding-top: 8px;
      border-top: 1px dashed #eef0f4;

      .dot {
        opacity: 0.5;
      }

      .actions {
        margin-left: auto;
      }
    }
  }

  /* 无限滚动锚点 sentinel:IntersectionObserver 观察它进入视口触发 loadMore */
  .record-sentinel {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 36px;
    padding: 14px 0 8px;

    .end-text {
      font-size: 12px;
      color: #a0aec0;
    }
  }

  /* 版本下拉 #option 项 ── 单行展示短版本号,完整 ID 走外层 tooltip */
  .version-option-text {
    display: inline-block;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 13px;
    color: #2a3547;
  }
</style>
