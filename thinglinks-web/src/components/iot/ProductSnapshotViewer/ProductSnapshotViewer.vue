<template>
  <div class="ps-snap-viewer">
    <!-- 工具栏:关键字搜索 + 展开 / 收起全部
         维度过滤已上移到 SnapshotPreviewModal 头部的 SummaryChip(避免与统计 chip 重复),
         本组件接受外部 levelFilter prop 控制层级过滤,内部不再渲染 radio。 -->
    <div v-if="hasNodes && showToolbar" class="ps-toolbar">
      <a-input
        v-model:value="keyword"
        size="small"
        allowClear
        class="ps-search"
        :placeholder="t('iot.link.product.versionList.preview.searchPlaceholder')"
      />
      <span class="ps-toolbar-flex" />
      <a-button type="link" size="small" @click="expandAll">
        {{ t('iot.link.product.versionList.preview.expandAll') }}
      </a-button>
      <a-button type="link" size="small" @click="collapseAll">
        {{ t('iot.link.product.versionList.preview.collapseAll') }}
      </a-button>
    </div>

    <a-empty
      v-if="!hasNodes"
      :description="emptyText || t('iot.link.product.versionList.preview.noServices')"
    />
    <a-empty
      v-else-if="!filteredNodes.length"
      :description="t('iot.link.product.versionList.preview.noMatch')"
    />

    <div v-else class="ps-tree" ref="treeRef">
      <SnapshotNode
        v-for="(node, i) in filteredNodes"
        :key="i"
        :node="node"
        :depth="0"
        :expand-signal="expandSignal"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch, nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import SnapshotNode from './SnapshotNode.vue';
  import type { ProductSnapshotNode } from './snapshotToNodes';

  /**
   * 产品版本快照层级展示器 —— {@code ProductVersionDiffViewer} 的快照孪生组件。
   *
   * <p>输入由 {@code snapshotToNodes()} 展平后的层级节点(产品 → 服务 → 属性 / 命令 → 参数),
   * 按物模型层级缩进渲染:每个标量字段单独成行(不再堆 JSON),长值换行不截断。
   * 支持关键字搜索、整体展开 / 收起、外部 levelFilter 控制层级过滤。</p>
   *
   * <p><b>levelFilter 由外部传入</b>:维度过滤的入口由父组件(如 SnapshotPreviewModal)
   * 通过头部 SummaryChip 单选控制,避免本组件内再重复一套 radio,减少视觉冗余。</p>
   */
  type LevelFilter = 'ALL' | 'PRODUCT' | 'SERVICE' | 'PROPERTY' | 'COMMAND';

  const props = withDefaults(
    defineProps<{
      /** 由 {@code snapshotToNodes()} 转换得到的层级节点。 */
      nodes?: ProductSnapshotNode[];
      /** 空态文案,缺省走 preview.noServices。 */
      emptyText?: string;
      /** 是否显示顶部工具栏(搜索 / 展开收起)。 */
      showToolbar?: boolean;
      /** 层级过滤(由父组件控制),默认 ALL 显示全部。 */
      levelFilter?: LevelFilter;
    }>(),
    { showToolbar: true, levelFilter: 'ALL' },
  );

  const emit = defineEmits<{
    /** 过滤后的首个匹配节点已渲染,父组件可据此触发滚动定位。 */
    (e: 'filtered'): void;
  }>();

  const { t } = useI18n();

  const keyword = ref('');
  const treeRef = ref<HTMLElement | null>(null);

  /** 展开信号:工具栏「展开 / 收起全部」「过滤自动展开」经此广播给各节点。 */
  const expandSignal = reactive<{ mode: 'default' | 'all' | 'none'; seq: number }>({
    mode: 'default',
    seq: 0,
  });

  const hasNodes = computed(() => !!props.nodes?.length);

  function expandAll() {
    expandSignal.mode = 'all';
    expandSignal.seq += 1;
  }

  function collapseAll() {
    expandSignal.mode = 'none';
    expandSignal.seq += 1;
  }

  // 过滤 / 搜索激活时自动展开,直接看到命中项;并发送 filtered 事件供父组件定位首节点
  watch(
    () => [props.levelFilter, keyword.value] as const,
    async () => {
      const active = props.levelFilter !== 'ALL' || keyword.value.trim();
      expandSignal.mode = active ? 'all' : 'default';
      expandSignal.seq += 1;
      if (active) {
        await nextTick();
        emit('filtered');
      }
    },
  );

  /** 关键字命中:节点名 / 编码 / 字段名 / 字段中文名 / 字段值。 */
  function keywordHit(node: ProductSnapshotNode, kw: string): boolean {
    if (!kw) return true;
    if ((node.name || '').toLowerCase().includes(kw)) return true;
    if ((node.code || '').toLowerCase().includes(kw)) return true;
    return (node.fields || []).some(
      (f) =>
        (f.label || '').toLowerCase().includes(kw) ||
        (f.field || '').toLowerCase().includes(kw) ||
        String(f.value ?? '')
          .toLowerCase()
          .includes(kw),
    );
  }

  /**
   * 节点自身是否命中层级过滤。
   *
   * <p>选「服务」 = 只显示服务节点,但保留产品父节点用于呈现层级路径;
   * 选「属性 / 命令」同理 —— 在父节点(产品 / 服务)递归遍历到目标层级才命中。</p>
   */
  function levelMatch(level: string): boolean {
    if (props.levelFilter === 'ALL') return true;
    return level === props.levelFilter;
  }

  /** 递归过滤:节点自身命中、或有命中的子孙,则保留(子孙也按命中收窄)。 */
  function filterNode(node: ProductSnapshotNode, kw: string): ProductSnapshotNode | null {
    const kids = (node.children || [])
      .map((c) => filterNode(c, kw))
      .filter((c): c is ProductSnapshotNode => c !== null);
    const selfHit = levelMatch(node.level) && keywordHit(node, kw);
    if (selfHit || kids.length) {
      return { ...node, children: kids };
    }
    return null;
  }

  const filteredNodes = computed<ProductSnapshotNode[]>(() => {
    const list = props.nodes || [];
    if (props.levelFilter === 'ALL' && !keyword.value.trim()) {
      return list;
    }
    const kw = keyword.value.trim().toLowerCase();
    return list
      .map((n) => filterNode(n, kw))
      .filter((n): n is ProductSnapshotNode => n !== null);
  });

  /**
   * 暴露 treeRef,父组件可据此 scrollIntoView 定位过滤后的首节点。
   */
  defineExpose({
    treeRef,
  });
</script>

<style lang="less" scoped>
  .ps-snap-viewer {
    .ps-toolbar {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;
      padding: 0 4px 10px;

      .ps-search {
        width: 220px;
      }

      .ps-toolbar-flex {
        flex: 1;
      }
    }

    .ps-tree {
      border: 1px solid #eef0f4;
      border-radius: 12px;
      overflow: hidden;
      background: #fff;
    }
  }
</style>
