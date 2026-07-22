<template>
  <div class="pv-diff-viewer">
    <!-- 工具栏:类型过滤 + 关键字搜索 + 展开 / 收起全部 -->
    <div v-if="hasNodes && showToolbar" class="pv-toolbar">
      <a-radio-group v-model:value="typeFilter" size="small" button-style="solid">
        <a-radio-button value="ALL">
          {{ t('iot.link.product.publishRecord.diff.filterAll') }}
        </a-radio-button>
        <a-radio-button value="ADDED">
          {{ t('iot.link.product.publishRecord.diff.changeType.ADDED') }}
        </a-radio-button>
        <a-radio-button value="MODIFIED">
          {{ t('iot.link.product.publishRecord.diff.changeType.MODIFIED') }}
        </a-radio-button>
        <a-radio-button value="REMOVED">
          {{ t('iot.link.product.publishRecord.diff.changeType.REMOVED') }}
        </a-radio-button>
      </a-radio-group>
      <a-input
        v-model:value="keyword"
        size="small"
        allowClear
        class="pv-search"
        :placeholder="t('iot.link.product.publishRecord.diff.searchPlaceholder')"
      />
      <span class="pv-toolbar-flex" />
      <a-button type="link" size="small" @click="expandAll">
        {{ t('iot.link.product.publishRecord.diff.expandAll') }}
      </a-button>
      <a-button type="link" size="small" @click="collapseAll">
        {{ t('iot.link.product.publishRecord.diff.collapseAll') }}
      </a-button>
    </div>

    <a-empty
      v-if="!hasNodes"
      :description="emptyText || t('iot.link.product.publishRecord.diff.noChanges')"
    />
    <a-empty
      v-else-if="!filteredNodes.length"
      :description="t('iot.link.product.publishRecord.diff.noMatch')"
    />

    <div v-else class="pv-tree">
      <DiffNode
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
  import { computed, reactive, ref, watch } from 'vue';
  import { refDebounced } from '@vueuse/core';
  import { useI18n } from '/@/hooks/web/useI18n';
  import type { ProductVersionDiffNode } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import DiffNode from './DiffNode.vue';

  /**
   * 产品物模型版本差异层级展示器。
   *
   * <p>输入后端 diff 的层级节点树(产品 / 服务 / 属性 / 命令 / 命令参数),按物模型层级
   * 平铺缩进渲染:每个标量字段单独成行(不再堆 JSON),长值换行不截断。支持按变更类型过滤、
   * 关键字搜索、整体展开 / 收起。版本对比 / 变更记录共用。</p>
   */
  const props = withDefaults(
    defineProps<{
      /** 后端 diff 的层级节点树。 */
      nodes?: ProductVersionDiffNode[];
      /** 无变更时的占位文案,缺省走 diff.noChanges。 */
      emptyText?: string;
      /** 是否显示顶部工具栏(类型过滤 / 搜索 / 展开收起);变更记录内嵌小 diff 可关。 */
      showToolbar?: boolean;
    }>(),
    { showToolbar: true },
  );

  const { t } = useI18n();

  type TypeFilter = 'ALL' | 'ADDED' | 'MODIFIED' | 'REMOVED';
  const typeFilter = ref<TypeFilter>('ALL');
  /** 输入双向绑定;实际参与 filter / watch 的是 {@link debouncedKeyword},避免大产品树每字符全量重算。 */
  const keyword = ref('');
  const debouncedKeyword = refDebounced(keyword, 300);

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

  // 过滤 / 搜索激活时过滤后的树较小,自动展开以直接看到命中项
  watch([typeFilter, debouncedKeyword], () => {
    expandSignal.mode = typeFilter.value !== 'ALL' || debouncedKeyword.value.trim() ? 'all' : 'default';
    expandSignal.seq += 1;
  });

  /** 关键字命中:节点名 / 编码 / 字段名 / 字段中文名。 */
  function keywordHit(node: ProductVersionDiffNode, kw: string): boolean {
    if (!kw) return true;
    if ((node.name || '').toLowerCase().includes(kw)) return true;
    if ((node.code || '').toLowerCase().includes(kw)) return true;
    return (node.fields || []).some(
      (f) =>
        (f.label || '').toLowerCase().includes(kw) || (f.field || '').toLowerCase().includes(kw),
    );
  }

  /** 节点自身是否命中过滤条件(变更类型 + 关键字)。 */
  function selfMatch(node: ProductVersionDiffNode, kw: string): boolean {
    const typeOk = typeFilter.value === 'ALL' || node.changeType === typeFilter.value;
    return typeOk && keywordHit(node, kw);
  }

  /** 递归过滤:节点自身命中、或有命中的子孙,则保留(子孙也按命中收窄)。 */
  function filterNode(node: ProductVersionDiffNode, kw: string): ProductVersionDiffNode | null {
    const kids = (node.children || [])
      .map((c) => filterNode(c, kw))
      .filter((c): c is ProductVersionDiffNode => c !== null);
    if (selfMatch(node, kw) || kids.length) {
      return { ...node, children: kids };
    }
    return null;
  }

  const filteredNodes = computed<ProductVersionDiffNode[]>(() => {
    const list = props.nodes || [];
    if (typeFilter.value === 'ALL' && !debouncedKeyword.value.trim()) {
      return list;
    }
    const kw = debouncedKeyword.value.trim().toLowerCase();
    return list
      .map((n) => filterNode(n, kw))
      .filter((n): n is ProductVersionDiffNode => n !== null);
  });
</script>

<style lang="less" scoped>
  .pv-diff-viewer {
    .pv-toolbar {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 0 4px 10px;

      .pv-search {
        width: 200px;
      }

      .pv-toolbar-flex {
        flex: 1;
      }
    }

    .pv-tree {
      border: 1px solid #eef0f4;
      border-radius: 12px;
      overflow: hidden;
      background: #fff;
    }
  }
</style>
