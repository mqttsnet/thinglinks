<template>
  <button
    type="button"
    class="summary-chip"
    :class="[`color-${color}`, { 'is-active': active, 'is-disabled': disabled }]"
    :disabled="disabled"
    @click="onClick"
  >
    <span class="chip-label">{{ label }}</span>
    <span class="chip-count">{{ count }}</span>
  </button>
</template>

<script lang="ts" setup>
  /**
   * 维度统计 Chip ── 可点击的过滤器入口。
   *
   * <p>设计:</p>
   * <ul>
   *   <li>非 disabled 状态下整 chip 是按钮,点击触发 {@code click} 事件,
   *       由父组件控制 active 高亮与对应数据过滤。</li>
   *   <li>{@code count <= 0} 时由父组件传 {@code disabled=true} 灰显置灰,不可点击,
   *       避免用户点了空分类后看到 a-empty 困惑。</li>
   *   <li>{@code active=true} 时高亮边框 + 深色背景,视觉强调"当前激活的过滤维度"。</li>
   * </ul>
   */
  const props = defineProps<{
    color: 'processing' | 'purple' | 'cyan' | 'orange';
    label: string;
    count: number;
    active?: boolean;
    disabled?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'click'): void;
  }>();

  function onClick() {
    if (props.disabled) return;
    emit('click');
  }
</script>

<style lang="less" scoped>
  .summary-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    background: transparent;
    border: 1px solid transparent;
    cursor: pointer;
    transition: background-color 0.15s, border-color 0.15s, box-shadow 0.15s, transform 0.05s;

    /* 按钮原生样式重置 */
    font-family: inherit;
    line-height: 1.4;

    &:focus {
      outline: none;
    }

    &:hover:not(.is-disabled):not(.is-active) {
      transform: translateY(-1px);
    }

    &:active:not(.is-disabled) {
      transform: translateY(0);
    }

    .chip-label {
      font-weight: 500;
    }

    .chip-count {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      padding: 0 6px;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.7);
      font-weight: 600;
    }

    /* ─────── 色调 ─────── */
    &.color-processing {
      background: rgba(22, 119, 255, 0.1);
      color: var(--primary-color, #1677ff);
      &.is-active {
        background: rgba(22, 119, 255, 0.18);
        border-color: rgba(22, 119, 255, 0.45);
        box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
      }
    }

    &.color-purple {
      background: rgba(114, 46, 209, 0.1);
      color: #722ed1;
      &.is-active {
        background: rgba(114, 46, 209, 0.18);
        border-color: rgba(114, 46, 209, 0.45);
        box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.1);
      }
    }

    &.color-cyan {
      background: rgba(19, 194, 194, 0.1);
      color: #13c2c2;
      &.is-active {
        background: rgba(19, 194, 194, 0.18);
        border-color: rgba(19, 194, 194, 0.45);
        box-shadow: 0 0 0 3px rgba(19, 194, 194, 0.1);
      }
    }

    &.color-orange {
      background: rgba(250, 140, 22, 0.1);
      color: #fa8c16;
      &.is-active {
        background: rgba(250, 140, 22, 0.18);
        border-color: rgba(250, 140, 22, 0.45);
        box-shadow: 0 0 0 3px rgba(250, 140, 22, 0.1);
      }
    }

    /* disabled:空分类(count=0)灰显置灰,不可交互 */
    &.is-disabled {
      cursor: not-allowed;
      opacity: 0.45;
      filter: grayscale(0.6);
    }
  }
</style>
