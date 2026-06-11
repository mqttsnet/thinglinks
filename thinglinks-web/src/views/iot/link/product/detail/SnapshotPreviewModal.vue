<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.link.product.versionList.preview.title')"
    width="1000px"
    :minHeight="600"
    :showOkBtn="false"
    :cancelText="t('common.title.close')"
    class="snapshot-preview-modal"
  >
    <template v-if="loading">
      <div class="sp-loading">
        <a-spin :tip="t('common.title.loading')" />
      </div>
    </template>

    <template v-else-if="snapshot">
      <!-- ─────── 头部摘要卡(紧凑布局:产品名 + 版本 + 状态 + 元数据 一行收尾) ─────── -->
      <div class="sp-header">
        <div class="sp-line">
          <span class="sp-name">{{ snapshot.productName || '—' }}</span>
          <SnapshotIdTag v-if="snapshot.versionNo" :value="snapshot.versionNo" color="blue" />
          <a-tag :color="statusTagColor" class="sp-status-tag">{{ statusLabel }}</a-tag>

          <span class="sp-divider" />

          <span v-if="snapshot.productIdentification" class="meta-kv">
            <KeyOutlined class="meta-icon" />
            <code>{{ snapshot.productIdentification }}</code>
          </span>
          <span v-if="snapshot.publishTime" class="meta-kv">
            <ClockCircleOutlined class="meta-icon" />
            <span>{{ formatPublishTime(snapshot.publishTime) }}</span>
          </span>
        </div>
      </div>

      <!-- ─────── 维度过滤 + 视图切换 / JSON 复制(单条工具栏) ─────── -->
      <div class="sp-toolbar">
        <div class="sp-summary-chips">
          <SummaryChip
            color="processing"
            :label="t('iot.link.product.versionList.preview.filterInfo')"
            :count="productFieldCount"
            :active="levelFilter === 'PRODUCT'"
            :disabled="productFieldCount === 0"
            @click="toggleFilter('PRODUCT')"
          />
          <SummaryChip
            color="purple"
            :label="t('iot.link.product.versionList.preview.filterService')"
            :count="serviceCount"
            :active="levelFilter === 'SERVICE'"
            :disabled="serviceCount === 0"
            @click="toggleFilter('SERVICE')"
          />
          <SummaryChip
            color="cyan"
            :label="t('iot.link.product.versionList.preview.filterProperty')"
            :count="propertyCount"
            :active="levelFilter === 'PROPERTY'"
            :disabled="propertyCount === 0"
            @click="toggleFilter('PROPERTY')"
          />
          <SummaryChip
            color="orange"
            :label="t('iot.link.product.versionList.preview.filterCommand')"
            :count="commandCount"
            :active="levelFilter === 'COMMAND'"
            :disabled="commandCount === 0"
            @click="toggleFilter('COMMAND')"
          />
          <a-button
            v-if="levelFilter !== 'ALL'"
            type="link"
            size="small"
            class="clear-filter-btn"
            @click="resetFilter"
          >
            {{ t('iot.link.product.versionList.preview.filterAll') }}
          </a-button>
        </div>

        <span class="sp-toolbar-flex" />

        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="visual">
            <AppstoreOutlined />
            {{ t('iot.link.product.versionList.preview.tabVisual') }}
          </a-radio-button>
          <a-radio-button value="raw">
            <CodeOutlined />
            {{ t('iot.link.product.versionList.preview.tabRaw') }}
          </a-radio-button>
        </a-radio-group>
        <a-button v-if="viewMode === 'raw'" type="text" size="small" @click="copyJson">
          <CopyOutlined />
          {{ t('iot.link.product.versionList.preview.copyJson') }}
        </a-button>
      </div>

      <!-- ─────── 主体 ─────── -->
      <ProductSnapshotViewer
        v-if="viewMode === 'visual'"
        ref="viewerRef"
        :nodes="nodes"
        :level-filter="levelFilter"
        @filtered="onFiltered"
      />

      <div v-else class="sp-raw">
        <div class="raw-bar">
          <FileTextOutlined />
          <span>{{ jsonSizeLabel }}</span>
        </div>
        <pre class="raw-pre"><code>{{ formattedJson }}</code></pre>
      </div>
    </template>

    <template v-else>
      <a-empty :description="t('iot.link.product.versionList.preview.noServices')" />
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import {
    KeyOutlined,
    ClockCircleOutlined,
    AppstoreOutlined,
    CodeOutlined,
    CopyOutlined,
    FileTextOutlined,
  } from '@ant-design/icons-vue';
  import { detail as fetchVersionDetail } from '/@/api/iot/link/productVersion/productVersion';
  import type {
    ProductVersionResultVO,
    ProductSnapshotVO,
  } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import {
    ProductSnapshotViewer,
    snapshotToNodes,
    type ProductSnapshotNode,
  } from '/@/components/iot/ProductSnapshotViewer';
  import { SnapshotIdTag } from '/@/components/iot';
  import SummaryChip from './SummaryChip.vue';

  /** 维度过滤值,与 ProductSnapshotViewer 内部 LevelFilter 对齐。 */
  type LevelFilter = 'ALL' | 'PRODUCT' | 'SERVICE' | 'PROPERTY' | 'COMMAND';

  /**
   * 版本快照预览弹窗。
   *
   * <p>头部 = 产品名 + 版本号(可复制) + 状态徽标 + 标识 / 发布时间 + 维度统计 chip。
   * 主体 = 「可视化」走 {@link ProductSnapshotViewer} 层级树(与版本对比同款范式),
   * 「原始 JSON」走暗色代码块 + 复制。</p>
   *
   * <p>调用方:版本列表「预览快照」按钮 → 传 record(优先用列表里已有的
   * productSnapshotJson)或 productIdentification + version(再去后端 detail 拉)。</p>
   */
  const { t } = useI18n();
  const { createMessage } = useMessage();

  const loading = ref(false);
  const versionRow = ref<ProductVersionResultVO | undefined>(undefined);
  const snapshot = ref<ProductSnapshotVO | undefined>(undefined);
  const viewMode = ref<'visual' | 'raw'>('visual');

  /**
   * 维度过滤(头部 SummaryChip 单选 / 取消)。
   *
   * <p>切换规则:</p>
   * <ul>
   *   <li>点击未激活 chip → 切到该维度,同时滚动定位到首个匹配节点</li>
   *   <li>再次点击已激活 chip → 重置回 ALL(等同 toggle 语义)</li>
   *   <li>显式"全部"链接按钮 → 直接重置回 ALL</li>
   *   <li>空分类(count=0)→ chip 灰显置 disabled,不响应点击</li>
   * </ul>
   */
  const levelFilter = ref<LevelFilter>('ALL');
  const viewerRef = ref<InstanceType<typeof ProductSnapshotViewer> | null>(null);

  function toggleFilter(target: LevelFilter) {
    levelFilter.value = levelFilter.value === target ? 'ALL' : target;
  }

  function resetFilter() {
    levelFilter.value = 'ALL';
  }

  /**
   * viewer 触发的"过滤完成"事件 ── 定位到树容器顶部(自然滚动到首个匹配节点),
   * 避免用户切到大量子节点维度后还要手动往上翻。
   */
  async function onFiltered() {
    await nextTick();
    const tree = (viewerRef.value as any)?.treeRef as HTMLElement | undefined;
    if (tree?.scrollIntoView) {
      tree.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  /** 层级节点(由 viewer 直接消费)。 */
  const nodes = computed<ProductSnapshotNode[]>(() => snapshotToNodes(snapshot.value, t));

  const statusLabel = computed(() => {
    const s = versionRow.value?.versionStatus;
    return s != null ? t(`iot.link.product.versionList.status.${s}`) : '—';
  });

  /** versionStatus → ant-tag color。 */
  const statusTagColor = computed(() => {
    const s = versionRow.value?.versionStatus;
    if (s === 1) return 'success';   // PUBLISHED
    if (s === 2) return 'warning';   // CANARY
    if (s === 3) return 'purple';    // SHADOW
    if (s === 4) return 'default';   // ROLLED_BACK
    if (s === 5) return 'default';   // ARCHIVED
    return 'default';                 // DRAFT / unknown
  });

  /** 维度统计:产品字段数。 */
  const productFieldCount = computed(() => nodes.value[0]?.fields?.length ?? 0);

  /** 服务数。 */
  const serviceCount = computed(() => snapshot.value?.services?.length ?? 0);

  /** 全服务属性合计。 */
  const propertyCount = computed(
    () =>
      snapshot.value?.services?.reduce(
        (sum, s) => sum + (s.properties?.length ?? 0),
        0,
      ) ?? 0,
  );

  /** 全服务命令合计。 */
  const commandCount = computed(
    () =>
      snapshot.value?.services?.reduce(
        (sum, s) => sum + (s.commands?.length ?? 0),
        0,
      ) ?? 0,
  );

  /** 美化后的 JSON 字符串。 */
  const formattedJson = computed(() => {
    if (!snapshot.value) return '';
    try {
      return JSON.stringify(snapshot.value, null, 2);
    } catch {
      return '';
    }
  });

  const jsonSizeLabel = computed(() => {
    const len = formattedJson.value?.length ?? 0;
    if (len < 1024) return `${len} B`;
    if (len < 1024 * 1024) return `${(len / 1024).toFixed(1)} KB`;
    return `${(len / 1024 / 1024).toFixed(2)} MB`;
  });

  const [registerModal] = useModalInner(async (data) => {
    versionRow.value = undefined;
    snapshot.value = undefined;
    viewMode.value = 'visual';
    levelFilter.value = 'ALL';

    loading.value = true;
    try {
      let row: ProductVersionResultVO | undefined = data?.record;
      if (!row?.productSnapshotJson && data?.productIdentification && data?.versionNo) {
        const res: any = await fetchVersionDetail(data.productIdentification, data.versionNo);
        row = res;
      }
      versionRow.value = row;
      snapshot.value = parseSnapshot(row?.productSnapshotJson);
    } finally {
      loading.value = false;
    }
  });

  function parseSnapshot(json?: string): ProductSnapshotVO | undefined {
    if (!json) return undefined;
    try {
      return JSON.parse(json) as ProductSnapshotVO;
    } catch {
      return undefined;
    }
  }

  function formatPublishTime(time?: number | string): string {
    if (!time) return '—';
    const ms = typeof time === 'number' ? time : Date.parse(time);
    if (!ms || Number.isNaN(ms)) return String(time);
    return new Date(ms).toLocaleString();
  }

  function copyJson() {
    if (!formattedJson.value) return;
    const ok = copyTextToClipboard(formattedJson.value);
    if (ok) {
      createMessage.success(t('iot.link.product.versionList.preview.copySuccess'));
    } else {
      createMessage.warning(t('iot.link.product.versionList.preview.copyFail'));
    }
  }
</script>

<style lang="less" scoped>
  .snapshot-preview-modal {
    .sp-loading {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 280px;
    }

    // ─────── 头部摘要卡(紧凑单行) ───────
    .sp-header {
      padding: 10px 14px;
      margin-bottom: 10px;
      border-radius: 10px;
      background: #f7f9fc;
      border: 1px solid #eef0f4;
    }

    .sp-line {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;
      font-size: 12px;
      color: #6b7280;

      .sp-name {
        font-size: 15px;
        font-weight: 600;
        color: #2a3547;
      }

      .sp-status-tag {
        margin: 0;
        border-radius: 6px;
      }

      .sp-divider {
        width: 1px;
        height: 14px;
        background: #e1e5eb;
        margin: 0 2px;
      }

      .meta-kv {
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }

      .meta-icon {
        color: #97a1b0;
        font-size: 11px;
      }

      code {
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        color: #2a3547;
        background: transparent;
      }
    }

    // ─────── 单条工具栏:维度过滤 chip + 视图切换 + JSON 复制 ───────
    .sp-toolbar {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 12px;

      .sp-summary-chips {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .clear-filter-btn {
        padding: 0 6px;
        font-size: 12px;
        height: auto;
      }

      .sp-toolbar-flex {
        flex: 1;
      }
    }

    // ─────── 原始 JSON ───────
    .sp-raw {
      border-radius: 12px;
      background: #1d2433;
      overflow: hidden;
      border: 1px solid #1a2030;

      .raw-bar {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 14px;
        font-size: 12px;
        color: #94a3b8;
        background: rgba(255, 255, 255, 0.03);
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
      }

      .raw-pre {
        margin: 0;
        padding: 14px 16px;
        max-height: 540px;
        overflow: auto;
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        font-size: 12.5px;
        line-height: 1.7;
        color: #d6deeb;
        white-space: pre;

        &::-webkit-scrollbar {
          width: 8px;
          height: 8px;
        }
        &::-webkit-scrollbar-thumb {
          background: rgba(255, 255, 255, 0.12);
          border-radius: 4px;
        }
      }
    }
  }
</style>
