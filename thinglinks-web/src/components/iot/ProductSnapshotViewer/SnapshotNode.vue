<template>
  <div class="ps-snap-node">
    <!-- 节点行 -->
    <div
      class="node-row"
      :class="['lv-' + node.level.toLowerCase(), { expandable }]"
      :style="{ paddingLeft: indent + 'px' }"
      @click="toggle"
    >
      <span class="caret">
        <CaretRightOutlined v-if="expandable" :class="{ open: expanded }" />
      </span>
      <a-tag :color="levelColor" class="lv-tag">{{ levelLabel }}</a-tag>
      <span class="node-name" :title="displayName">{{ displayName }}</span>
      <span v-if="showCode" class="node-code" :title="node.code">{{ node.code }}</span>
      <span class="node-flex" />
      <span v-if="childCount" class="node-count" :title="childCountTitle">{{ childCount }}</span>
    </div>

    <!-- 展开:字段值 + 子节点 -->
    <div v-if="expandable && expanded" class="node-body">
      <div v-if="node.fields && node.fields.length" class="field-list" :style="bodyIndent">
        <div v-for="f in node.fields" :key="f.field" class="field-row">
          <span class="f-label" :title="f.field">{{ f.label || f.field }}</span>
          <span class="f-val">
            <span class="v">{{ fmt(f.value) }}</span>
          </span>
        </div>
      </div>

      <!-- 命令:请求 / 响应参数分组 -->
      <template v-if="node.level === 'COMMAND'">
        <div v-for="grp in paramGroups" :key="grp.kind" class="param-group">
          <div class="group-title" :style="bodyIndent">
            <span>{{ t(`iot.link.product.versionList.preview.paramKind.${grp.kind}`) }}</span>
            <span class="group-count">{{ grp.items.length }}</span>
          </div>
          <SnapshotNode
            v-for="(c, i) in grp.items"
            :key="i"
            :node="c"
            :depth="depth + 1"
            :expand-signal="expandSignal"
          />
        </div>
      </template>
      <template v-else>
        <SnapshotNode
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
  import type { ProductSnapshotNode } from './snapshotToNodes';

  /** 展开信号:由顶层 viewer 广播「展开 / 收起全部」「过滤自动展开」。 */
  interface ExpandSignal {
    mode: 'default' | 'all' | 'none';
    seq: number;
  }

  /**
   * 快照层级节点递归渲染 —— 镜像 {@code DiffNode} 的视觉范式,
   * 但只显示当前值(无 changeType / before / after)。
   */
  const props = defineProps<{
    node: ProductSnapshotNode;
    depth: number;
    expandSignal: ExpandSignal;
  }>();

  const { t } = useI18n();

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
    t(`iot.link.product.versionList.preview.level.${props.node.level}`),
  );

  /** 产品节点显示层级名,其余显示节点名。 */
  const displayName = computed(() =>
    props.node.level === 'PRODUCT'
      ? props.node.name || levelLabel.value
      : props.node.name || props.node.code || levelLabel.value,
  );

  const showCode = computed(
    () =>
      props.node.level !== 'PRODUCT' && !!props.node.code && props.node.code !== props.node.name,
  );

  /** level → tag 颜色(与左侧色条同色系)。 */
  const levelColor = computed(() => {
    switch (props.node.level) {
      case 'PRODUCT':
        return 'processing';
      case 'SERVICE':
        return 'purple';
      case 'PROPERTY':
        return 'cyan';
      case 'COMMAND':
        return 'orange';
      case 'COMMAND_PARAM':
        return 'default';
      default:
        return 'default';
    }
  });

  /** 直接子节点数(命令算其 children 总数,服务算属性 + 命令)。 */
  const childCount = computed(() => props.node.children?.length ?? 0);
  const childCountTitle = computed(() =>
    childCount.value ? `${childCount.value} ${levelLabel.value}` : '',
  );

  /** 命令的请求 / 响应参数分组(空组不渲染)。 */
  const paramGroups = computed(() => {
    const children = props.node.children || [];
    return (['REQUEST', 'RESPONSE'] as const)
      .map((kind) => ({ kind, items: children.filter((c) => c.paramKind === kind) }))
      .filter((g) => g.items.length);
  });

  /** 值格式化:空 → 占位符,对象 → JSON。 */
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
  .node-row {
    display: flex;
    align-items: center;
    gap: 8px;
    min-height: 42px;
    padding-right: 14px;
    border-bottom: 1px solid #f2f4f8;
    border-left: 3px solid transparent;
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

    .lv-tag {
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
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 260px;
    }

    .node-code {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 11.5px;
      color: #8c97a5;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 200px;
    }

    .node-flex {
      flex: 1;
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

    // 各层级左侧色条 ── 一眼看清是哪个级别
    &.lv-product {
      border-left-color: #1677ff;
    }
    &.lv-service {
      border-left-color: #722ed1;
    }
    &.lv-property {
      border-left-color: #13c2c2;
    }
    &.lv-command {
      border-left-color: #fa8c16;
    }
    &.lv-command_param {
      border-left-color: #d9d9d9;
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
      color: #2a3547;
      background: rgba(22, 119, 255, 0.06);
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
