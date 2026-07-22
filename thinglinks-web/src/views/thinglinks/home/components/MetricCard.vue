<template>
  <div class="metric-card" :class="{ 'is-accent': accent }">
    <div class="metric-card__head">
      <span
        class="metric-card__icon"
        :style="{ background: iconBg, color: iconColor }"
      >
        <Icon v-if="icon" :icon="icon" :size="18" />
      </span>
      <span
        v-if="showTrend"
        class="metric-card__trend"
        :class="trend! > 0 ? 'is-up' : 'is-down'"
      >
        <Icon
          :icon="trend! > 0 ? 'ant-design:arrow-up-outlined' : 'ant-design:arrow-down-outlined'"
          :size="10"
        />
        {{ Math.abs(trend!) }}%
      </span>
    </div>
    <div class="metric-card__label">{{ label }}</div>
    <div class="metric-card__value">
      <span class="metric-card__number" :class="numberSizeClass">{{ displayValue }}</span>
      <span v-if="sub" class="metric-card__sub">{{ sub }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Icon } from '/@/components/Icon';

  interface Props {
    label: string;
    value: string | number;
    sub?: string;
    trend?: number | null;
    icon?: string;
    iconColor?: string;
    iconBg?: string;
    accent?: boolean;
  }
  const props = withDefaults(defineProps<Props>(), {
    iconColor: '#5d87ff',
    iconBg: '#ecf2ff',
    accent: false,
  });

  const displayValue = computed(() => {
    if (props.value === null || props.value === undefined || props.value === '') return '-';
    return props.value;
  });

  // 值是长中文 / 长字符串时降一级字号，避免"普通用户""未分配"看起来过重
  const numberSizeClass = computed(() => {
    const v = String(displayValue.value ?? '');
    if (typeof props.value === 'number') return 'is-num';
    // 纯数字字符串走大号
    if (/^-?\d+(\.\d+)?$/.test(v)) return 'is-num';
    if (v.length >= 4) return 'is-text-sm';
    return 'is-text';
  });

  const showTrend = computed(
    () => typeof props.trend === 'number' && !Number.isNaN(props.trend),
  );
</script>

<style lang="less" scoped>
  .metric-card {
    padding: 18px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(20, 37, 66, 0.03), 0 6px 24px rgba(20, 37, 66, 0.04);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
    height: 100%;
    min-height: 108px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 6px rgba(20, 37, 66, 0.04), 0 12px 36px rgba(93, 135, 255, 0.1);
    }

    &.is-accent {
      background: linear-gradient(135deg, #ecf2ff 0%, #f5efff 100%);
    }
  }

  .metric-card__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .metric-card__icon {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .metric-card__trend {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 9999px;

    &.is-up {
      color: #0bb783;
      background: rgba(19, 222, 185, 0.12);
    }
    &.is-down {
      color: #d03b5b;
      background: rgba(250, 92, 124, 0.12);
    }
  }

  .metric-card__value {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 4px;
  }

  .metric-card__number {
    font-weight: 700;
    color: #2a3547;
    letter-spacing: -0.4px;
    line-height: 1.2;

    &.is-num {
      font-size: 28px;
    }
    &.is-text {
      font-size: 20px;
    }
    &.is-text-sm {
      font-size: 16px;
      letter-spacing: -0.1px;
    }
  }

  .metric-card__sub {
    font-size: 12px;
    color: #8c97a5;
    font-weight: 500;
  }

  .metric-card__label {
    font-size: 12px;
    color: #8c97a5;
    font-weight: 500;
    letter-spacing: 0.1px;
  }
</style>
