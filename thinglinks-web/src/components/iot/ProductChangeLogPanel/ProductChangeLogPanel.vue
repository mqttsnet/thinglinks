<template>
  <div ref="scrollRef" class="pcl-panel">
    <!-- 首屏加载 -->
    <div v-if="loading" class="pcl-loading">
      <a-spin />
    </div>

    <!-- 空态 -->
    <a-empty
      v-else-if="!logs.length"
      class="pcl-empty"
      :description="t('iot.link.product.changeLog.empty')"
    />

    <!-- 时间线 -->
    <a-timeline v-else class="pcl-timeline">
      <a-timeline-item v-for="log in logs" :key="log.id" :color="dotColor(log.changeType)">
        <div :class="['pcl-card', 'ct-' + (log.changeType ?? 1)]">
          <!-- 头部:变更类型 + 维度 + 摘要 -->
          <div class="pcl-head">
            <span :class="['pcl-type', 'ct-' + (log.changeType ?? 1)]">
              <component :is="typeIcon(log.changeType)" class="type-icon" />
              {{ typeLabel(log.changeType) }}
            </span>
            <span class="pcl-dim">{{ dimLabel(log.targetType) }}</span>
            <span class="pcl-summary">{{ log.changeSummary || '—' }}</span>
            <a-button
              v-if="fields(log).length"
              type="link"
              size="small"
              class="pcl-toggle"
              @click="toggle(log.id)"
            >
              {{
                expandedIds.has(log.id ?? '')
                  ? t('iot.link.product.changeLog.collapse')
                  : t('iot.link.product.changeLog.detail')
              }}
              <CaretRightOutlined :class="['toggle-caret', { open: expandedIds.has(log.id ?? '') }]" />
            </a-button>
          </div>

          <!-- 元信息:时间 + 操作人 + 变更处数 -->
          <div class="pcl-meta">
            <span class="meta-item">
              <ClockCircleOutlined class="meta-icon" />
              {{ formatTime(log.createdTime) }}
            </span>
            <span class="meta-sep">·</span>
            <span class="meta-item">
              <UserOutlined class="meta-icon" />
              {{ echoMapText(log, 'createdBy') }}
            </span>
            <template v-if="fields(log).length">
              <span class="meta-sep">·</span>
              <span class="meta-count">
                {{ fields(log).length }} {{ t('iot.link.product.changeLog.changeCount') }}
              </span>
            </template>
          </div>

          <!-- 展开:字段变更小表 -->
          <div v-if="expandedIds.has(log.id ?? '') && fields(log).length" class="pcl-fields">
            <div class="fields-head">
              <span class="col-field">{{ t('iot.link.product.changeLog.field.name') }}</span>
              <span class="col-val">{{ t('iot.link.product.changeLog.field.before') }}</span>
              <span class="col-val">{{ t('iot.link.product.changeLog.field.after') }}</span>
            </div>
            <div v-for="(fc, i) in fields(log)" :key="i" class="fields-row">
              <span class="col-field" :title="fc.field">{{ fc.label || fc.field || '—' }}</span>
              <span class="col-val v-before">{{ fmt(fc.before) }}</span>
              <span class="col-val v-after">{{ fmt(fc.after) }}</span>
            </div>
          </div>
        </div>
      </a-timeline-item>
    </a-timeline>

    <!-- 无限滚动 sentinel:IntersectionObserver 观察它进入视口 → 触发 loadMore -->
    <div ref="sentinelRef" class="pcl-sentinel">
      <a-spin v-if="loadingMore" size="small" />
      <span v-else-if="!hasMore && logs.length" class="end-text">
        {{ t('iot.link.product.changeLog.noMore', { n: logs.length }) }}
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import {
    ClockCircleOutlined,
    UserOutlined,
    CaretRightOutlined,
    PlusCircleOutlined,
    EditOutlined,
    MinusCircleOutlined,
  } from '@ant-design/icons-vue';
  import { page } from '/@/api/iot/link/productVersionChangeLog/productVersionChangeLog';
  import { echoMapText } from '/@/utils/echo';
  import type {
    ProductVersionChangeLogResultVO,
    FieldChange,
  } from '/@/api/iot/link/productVersionChangeLog/model/productVersionChangeLogModel';

  /**
   * 产品物模型变更记录面板。
   *
   * <p>调 productVersionChangeLog/page 拉取按版本切片的变更流水,渲染 flexy 时间线:
   * 每条 = 变更类型 + 维度 + 摘要 + 时间 + 操作人,可展开看字段级前后对比小表。
   * 下滑追加分页。产品「变更记录」页签与发布管理抽屉共用。</p>
   */
  const props = defineProps<{
    /** 产品标识。 */
    productIdentification: string;
    /** 指定版本(变更记录按版本切片);不传则不带 versionNo 过滤。 */
    versionNo?: string;
  }>();

  const { t } = useI18n();

  /** 每页条数。 */
  const PAGE_SIZE = 15;

  const logs = ref<ProductVersionChangeLogResultVO[]>([]);
  /** 首屏加载。 */
  const loading = ref(false);
  /** 下滑追加加载。 */
  const loadingMore = ref(false);
  /** 是否还有下一页。 */
  const hasMore = ref(true);
  /** 已加载到第几页。 */
  const current = ref(0);
  const scrollRef = ref<HTMLElement>();
  /** 底部哨兵元素:IntersectionObserver 观察它进入视口触发加载下一页。 */
  const sentinelRef = ref<HTMLElement | null>(null);
  let observer: IntersectionObserver | null = null;
  const expandedIds = ref<Set<string>>(new Set());
  /** changeDetailJson 解析缓存,避免每次渲染重复 JSON.parse。 */
  const fieldsCache = new Map<string, FieldChange[]>();

  /**
   * 拉取下一页变更日志。
   *
   * <p>后端 page 按 id 倒序(最新在前):首页即最新一批,下滑追加更早记录。</p>
   *
   * <p><b>并发守卫</b>:onMounted 与 watch 在挂载瞬间可能双触发(getter 返回新数组、props 引用
   * 抖动等场景),两条调用都跑 reset → 同 cursor 发请求 → append,会让同一条记录在 logs
   * 里出现两次。守卫放在分支选择之前,正在跑的调用未结束前,后到的 reset / load-more 直接丢弃。</p>
   *
   * @param reset true=首屏 / 刷新(清空重来),false=下滑追加
   */
  async function fetchPage(reset: boolean) {
    if (!props.productIdentification) return;
    // 并发守卫 ── 任一加载中的请求未结束,新的调用直接丢弃,避免重复 append
    if (loading.value || loadingMore.value) return;
    if (reset) {
      current.value = 0;
      hasMore.value = true;
      logs.value = [];
      expandedIds.value = new Set();
      fieldsCache.clear();
      loading.value = true;
    } else {
      if (!hasMore.value) return;
      loadingMore.value = true;
    }
    try {
      const next = current.value + 1;
      const res: any = await page({
        model: {
          productIdentification: props.productIdentification,
          versionNo: props.versionNo,
        },
        current: next,
        size: PAGE_SIZE,
      });
      const records: ProductVersionChangeLogResultVO[] = Array.isArray(res?.records)
        ? res.records
        : [];
      logs.value.push(...records);
      current.value = next;
      hasMore.value = next < (res?.pages ?? 0);
    } finally {
      loading.value = false;
      loadingMore.value = false;
    }

    // 兜底:首屏数据不够撑出 scrollbar → sentinel 仍在视口 → 自动追加下一页
    // 解决"只显示第 1 页 15 条,即使点刷新也不会自动 load 第 2 页"的 bug
    await nextTick();
    if (hasMore.value && sentinelRef.value && isElementInViewport(sentinelRef.value as HTMLElement)) {
      fetchPage(false);
    }
  }

  function isElementInViewport(el: HTMLElement): boolean {
    const rect = el.getBoundingClientRect();
    return rect.top < (window.innerHeight || document.documentElement.clientHeight) && rect.bottom > 0;
  }

  /**
   * 设置 IntersectionObserver ── 比 scroll 事件鲁棒得多。
   *
   * <p>scroll 事件只对 panel 自身 scrollbar 生效;但本组件可能嵌入到:</p>
   * <ul>
   *   <li>changeLog tab(panel 自己有 scrollbar)</li>
   *   <li>ChangeLogDrawer 抽屉(drawer body 有 scrollbar,panel 没有)</li>
   *   <li>详情页内嵌(若高度链坏掉,谁都没 scrollbar,父级 page 滚动)</li>
   * </ul>
   * <p>IntersectionObserver 默认观察 viewport,无论哪一层在滚都能正确触发。</p>
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

  // 产品 / 版本变化时重新拉取(抽屉复用同一组件实例时尤为重要)
  watch(
    () => [props.productIdentification, props.versionNo],
    () => fetchPage(true),
  );

  onMounted(async () => {
    await fetchPage(true);
    await nextTick();
    setupObserver();
  });

  onBeforeUnmount(() => {
    observer?.disconnect();
    observer = null;
  });

  /** 对外暴露 reload,供宿主工具栏刷新。 */
  function reload() {
    fetchPage(true);
  }
  defineExpose({ reload });

  function toggle(id?: string) {
    if (!id) return;
    const next = new Set(expandedIds.value);
    next.has(id) ? next.delete(id) : next.add(id);
    expandedIds.value = next;
  }

  /** 解析 changeDetailJson → FieldChange[](带缓存)。 */
  function fields(log: ProductVersionChangeLogResultVO): FieldChange[] {
    const id = log.id ?? '';
    if (fieldsCache.has(id)) return fieldsCache.get(id)!;
    let list: FieldChange[] = [];
    if (log.changeDetailJson) {
      try {
        const parsed = JSON.parse(log.changeDetailJson);
        list = Array.isArray(parsed) ? parsed : [];
      } catch {
        list = [];
      }
    }
    fieldsCache.set(id, list);
    return list;
  }

  /** 变更类型(0-新增 1-编辑 2-删除)→ 文案。 */
  function typeLabel(type?: number): string {
    return t(`iot.link.product.changeLog.changeType.${type ?? 1}`);
  }

  /** 变更维度(0-产品信息 1-服务 2-属性 3-命令)→ 文案。 */
  function dimLabel(target?: number): string {
    return t(`iot.link.product.changeLog.targetType.${target ?? 0}`);
  }

  /** 时间线圆点颜色。 */
  function dotColor(type?: number): string {
    if (type === 0) return 'green';
    if (type === 2) return 'red';
    return 'blue';
  }

  function typeIcon(type?: number) {
    if (type === 0) return PlusCircleOutlined;
    if (type === 2) return MinusCircleOutlined;
    return EditOutlined;
  }

  function formatTime(time?: string): string {
    if (!time) return '—';
    return time.replace('T', ' ').slice(0, 19);
  }

  /** 字段值格式化:空 → 占位符,对象 → JSON。 */
  function fmt(v: any): string {
    if (v === null || v === undefined || v === '') return '—';
    if (typeof v === 'object') {
      try {
        return JSON.stringify(v);
      } catch {
        return String(v);
      }
    }
    return String(v);
  }
</script>

<style lang="less" scoped>
  .pcl-panel {
    height: 100%;
    overflow-y: auto;
    padding: 4px 6px 4px 2px;
  }

  .pcl-loading {
    display: flex;
    justify-content: center;
    padding: 64px 0;
  }

  .pcl-empty {
    padding: 56px 0;
  }

  .pcl-timeline {
    padding: 14px 8px 4px 10px;
  }

  /* 无限滚动 sentinel ── IntersectionObserver 观察的锚点元素 */
  .pcl-sentinel {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 36px;
    padding: 8px 0 16px;

    .end-text {
      font-size: 12px;
      color: #a0aec0;
    }
  }

  // ──────────── 单条卡片 ────────────
  .pcl-card {
    background: #fff;
    border: 1px solid #eef0f4;
    border-radius: 12px;
    padding: 14px 18px;
    transition: box-shadow 0.2s ease, border-color 0.2s ease;

    &:hover {
      border-color: #e2e6ee;
      box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
    }
  }

  .pcl-head {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  // 变更类型胶囊
  .pcl-type {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
    padding: 3px 10px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;

    .type-icon {
      font-size: 12px;
    }

    &.ct-0 {
      color: #389e0d;
      background: #f0f9ea;
    }
    &.ct-1 {
      color: #1677ff;
      background: #eef4ff;
    }
    &.ct-2 {
      color: #d9363e;
      background: #fff0f0;
    }
  }

  // 维度标签(柔和蓝灰底)
  .pcl-dim {
    flex-shrink: 0;
    padding: 3px 10px;
    border-radius: 8px;
    font-size: 12px;
    color: #5b6b82;
    background: #f1f4f9;
  }

  .pcl-summary {
    flex: 1;
    min-width: 120px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .pcl-toggle {
    flex-shrink: 0;
    padding: 0 2px;

    .toggle-caret {
      font-size: 10px;
      transition: transform 0.18s ease;

      &.open {
        transform: rotate(90deg);
      }
    }
  }

  // 元信息行
  .pcl-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 7px;
    margin-top: 10px;
    font-size: 12px;
    color: #97a1b0;

    .meta-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .meta-icon {
      font-size: 12px;
    }

    .meta-sep {
      color: #d6dae1;
    }

    .meta-count {
      padding: 1px 9px;
      border-radius: 8px;
      background: #eef4ff;
      color: #1677ff;
      font-weight: 500;
    }
  }

  // ──────────── 字段变更小表 ────────────
  .pcl-fields {
    margin-top: 12px;
    border: 1px solid #eef0f4;
    border-radius: 10px;
    overflow: hidden;
  }

  .fields-head,
  .fields-row {
    display: grid;
    grid-template-columns: minmax(110px, 1fr) minmax(0, 1.4fr) minmax(0, 1.4fr);
    gap: 14px;
    padding: 8px 14px;
  }

  .fields-head {
    background: #f7f9fc;
    font-size: 11px;
    font-weight: 600;
    color: #8c97a5;
    letter-spacing: 0.2px;
  }

  .fields-row {
    font-size: 12px;
    line-height: 1.6;
    border-top: 1px solid #f2f4f8;

    .col-field {
      color: #5b6b82;
      font-weight: 500;
      word-break: break-all;
    }

    .col-val {
      min-width: 0;
      word-break: break-all;
      white-space: pre-wrap;
    }

    .v-before {
      color: #98a0ad;
    }

    .v-after {
      color: #2a3547;
    }
  }
</style>
