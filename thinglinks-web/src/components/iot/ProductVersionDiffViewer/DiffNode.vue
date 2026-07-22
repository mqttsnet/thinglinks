<template>
  <div class="pv-diff-node">
    <!-- 节点行 -->
    <div
      class="node-row"
      :class="['ct-' + (node.changeType || 'MODIFIED').toLowerCase(), { expandable }]"
      :style="{ paddingLeft: indent + 'px' }"
      @click="toggle"
    >
      <span class="caret">
        <CaretRightOutlined v-if="expandable" :class="{ open: expanded }" />
      </span>
      <a-tag :color="typeColor" class="ct-tag">{{ typeLabel }}</a-tag>
      <span class="node-name">{{ displayName }}</span>
      <span v-if="showCode" class="node-code">{{ node.code }}</span>
      <span class="node-flex" />
      <span class="node-level">{{ levelLabel }}</span>
      <span v-if="changeCount" class="node-count">{{ changeCount }}</span>
    </div>

    <!-- 展开:字段变更 + 子节点 -->
    <div v-if="expandable && expanded" class="node-body">
      <div v-if="node.fields && node.fields.length" class="field-list" :style="bodyIndent">
        <div
          v-for="f in node.fields"
          :key="f.field"
          class="field-row"
          :class="'ct-' + (f.changeType || 'MODIFIED').toLowerCase()"
        >
          <span class="f-label" :title="f.field">{{ f.label || f.field }}</span>
          <span class="f-val">
            <span v-if="f.changeType !== 'ADDED'" class="v v-before">{{ fmt(f.before, f.dictType) }}</span>
            <span v-if="f.changeType === 'MODIFIED'" class="v-arrow">→</span>
            <span v-if="f.changeType !== 'REMOVED'" class="v v-after">{{ fmt(f.after, f.dictType) }}</span>
          </span>
        </div>
      </div>

      <!-- 命令:请求 / 响应参数分组;其余层级直接递归 -->
      <template v-if="node.level === 'COMMAND'">
        <div v-for="grp in paramGroups" :key="grp.kind" class="param-group">
          <div class="group-title" :style="bodyIndent">
            <span>{{ t(`iot.link.product.publishRecord.diff.paramKind.${grp.kind}`) }}</span>
            <span class="group-count">{{ grp.items.length }}</span>
          </div>
          <DiffNode
            v-for="(c, i) in grp.items"
            :key="i"
            :node="c"
            :depth="depth + 1"
            :expand-signal="expandSignal"
          />
        </div>
      </template>
      <template v-else>
        <DiffNode
          v-for="(c, i) in node.children || []"
          :key="i"
          :node="c"
          :depth="depth + 1"
          :expand-signal="expandSignal"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { CaretRightOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import type { ProductVersionDiffNode } from '/@/api/iot/link/productVersion/model/productVersionModel';

  /** 展开信号:由顶层 viewer 广播「展开 / 收起全部」「过滤自动展开」。 */
  interface ExpandSignal {
    mode: 'default' | 'all' | 'none';
    seq: number;
  }

  const props = defineProps<{
    node: ProductVersionDiffNode;
    depth: number;
    expandSignal: ExpandSignal;
  }>();

  const { t } = useI18n();
  const { getDictLabel } = useDict();

  /** 默认展开层级:仅顶层(产品 / 服务)展开,更深默认收起,避免大树刷屏。 */
  const DEFAULT_EXPAND_DEPTH = 1;

  const indent = computed(() => props.depth * 18 + 12);
  const bodyIndent = computed(() => ({ paddingLeft: indent.value + 26 + 'px' }));

  const expandable = computed(
    () => !!(props.node.fields?.length || props.node.children?.length),
  );

  const expanded = ref(props.depth < DEFAULT_EXPAND_DEPTH);

  // 响应工具栏的展开 / 收起广播
  watch(
    () => props.expandSignal.seq,
    () => {
      const mode = props.expandSignal.mode;
      expanded.value =
        mode === 'all' ? true : mode === 'none' ? false : props.depth < DEFAULT_EXPAND_DEPTH;
    },
  );

  function toggle() {
    if (expandable.value) {
      expanded.value = !expanded.value;
    }
  }

  const levelLabel = computed(() =>
    t(`iot.link.product.publishRecord.diff.level.${props.node.level}`),
  );

  /** 产品节点显示层级名,其余显示节点名。 */
  const displayName = computed(() =>
    props.node.level === 'PRODUCT'
      ? levelLabel.value
      : props.node.name || props.node.code || levelLabel.value,
  );

  const showCode = computed(
    () =>
      props.node.level !== 'PRODUCT' && !!props.node.code && props.node.code !== props.node.name,
  );

  const typeLabel = computed(() =>
    t(`iot.link.product.publishRecord.diff.changeType.${props.node.changeType}`),
  );

  const typeColor = computed(() => {
    if (props.node.changeType === 'ADDED') return 'success';
    if (props.node.changeType === 'REMOVED') return 'error';
    return 'processing';
  });

  /** 本节点直接变更数:字段数 + 子节点数。 */
  const changeCount = computed(
    () => (props.node.fields?.length ?? 0) + (props.node.children?.length ?? 0),
  );

  /** 命令的请求 / 响应参数分组(仅保留有变更的组)。 */
  const paramGroups = computed(() => {
    const children = props.node.children || [];
    return (['REQUEST', 'RESPONSE'] as const)
      .map((kind) => ({ kind, items: children.filter((c) => c.paramKind === kind) }))
      .filter((g) => g.items.length);
  });

  /**
   * 值格式化:空 → 占位符,对象 → JSON,带 dictType 的字段 → 字典中文标签。
   *
   * <p>字典翻译走 {@link useDict.getDictLabel};未命中字典(字典不存在 / 值不在字典中)
   * 时回退原始值,而不是空白,避免误导。</p>
   */
  function fmt(v: any, dictType?: string): string {
    if (v === null || v === undefined || v === '') return '—';
    if (dictType) {
      // getDictLabel 未命中 → 回退到原值字符串(保留可观察性)
      return getDictLabel(dictType, v, String(v));
    }
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
  .node-row {
    display: flex;
    align-items: center;
    gap: 8px;
    min-height: 42px;
    padding-right: 14px;
    border-bottom: 1px solid #f2f4f8;
    transition: background 0.15s;

    &.expandable {
      cursor: pointer;
    }

    &:hover {
      background: #f7f9fc;
    }

    .caret {
      flex-shrink: 0;
      width: 14px;
      color: #8c97a5;
      font-size: 11px;

      .open {
        transform: rotate(90deg);
      }
    }

    .ct-tag {
      flex-shrink: 0;
      margin: 0;
      font-weight: 500;
      border-radius: 5px;
    }

    .node-name {
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;
      white-space: nowrap;
    }

    .node-code {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 11.5px;
      color: #8c97a5;
      white-space: nowrap;
    }

    .node-flex {
      flex: 1;
    }

    .node-level {
      flex-shrink: 0;
      font-size: 11px;
      color: #a8b1bd;
    }

    .node-count {
      flex-shrink: 0;
      min-width: 18px;
      height: 18px;
      padding: 0 6px;
      font-size: 11px;
      line-height: 18px;
      text-align: center;
      color: #6b7280;
      background: #f0f2f5;
      border-radius: 9px;
    }
  }

  // 变更类型左侧色条
  .node-row {
    border-left: 3px solid transparent;

    &.ct-added {
      border-left-color: #52c41a;
    }
    &.ct-removed {
      border-left-color: #ff4d4f;
    }
    &.ct-modified {
      border-left-color: #1677ff;
    }
  }

  .field-list {
    padding-top: 6px;
    padding-bottom: 8px;
    padding-right: 14px;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }

  .field-row {
    display: flex;
    gap: 12px;
    font-size: 12px;
    line-height: 1.7;

    .f-label {
      flex-shrink: 0;
      min-width: 96px;
      max-width: 160px;
      color: #8c97a5;
      word-break: break-all;
    }

    .f-val {
      flex: 1;
      min-width: 0;
      display: flex;
      align-items: baseline;
      flex-wrap: wrap;
      gap: 6px;
    }

    .v {
      max-height: 180px;
      overflow: auto;
      padding: 1px 7px;
      border-radius: 4px;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      white-space: pre-wrap;
      word-break: break-all;
    }

    .v-before {
      background: rgba(255, 77, 79, 0.08);
      color: #8c8c8c;
      text-decoration: line-through;
    }

    .v-after {
      background: rgba(82, 196, 26, 0.1);
      color: #2a3547;
    }

    .v-arrow {
      color: #d0d5dd;
    }

    &.ct-added .f-label {
      color: #52c41a;
    }
    &.ct-removed .f-label {
      color: #ff4d4f;
    }
  }

  .param-group {
    .group-title {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-top: 6px;
      padding-bottom: 4px;
      font-size: 12px;
      font-weight: 600;
      color: #6b7280;

      .group-count {
        min-width: 16px;
        height: 16px;
        padding: 0 5px;
        font-size: 11px;
        font-weight: 400;
        line-height: 16px;
        text-align: center;
        color: #8c97a5;
        background: #f0f2f5;
        border-radius: 8px;
      }
    }
  }
</style>
