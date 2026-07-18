<template>
  <svg
    class="product-model-card-svg"
    :class="`product-model-card-svg--${variant}`"
    viewBox="0 0 64 64"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <rect class="model-svg__surface" x="8" y="8" width="48" height="48" rx="12" />

    <g v-if="variant === 'property'">
      <path class="model-svg__muted-line" d="M20 23H44M20 32H44M20 41H44" />
      <circle class="model-svg__node" cx="29" cy="23" r="4" />
      <circle class="model-svg__node" cx="38" cy="32" r="4" />
      <circle class="model-svg__node" cx="25" cy="41" r="4" />
    </g>

    <g v-else>
      <path class="model-svg__line" d="M21 22L30 31L21 40" />
      <path class="model-svg__muted-line" d="M34 40H45" />
      <path class="model-svg__muted-line" d="M35 22H45" />
      <rect class="model-svg__cursor" x="34" y="29" width="12" height="5" rx="2.5" />
    </g>
  </svg>
</template>

<script lang="ts" setup>
  withDefaults(
    defineProps<{
      variant?: 'property' | 'command';
    }>(),
    {
      variant: 'property',
    },
  );
</script>

<style lang="less" scoped>
  .product-model-card-svg {
    display: block;
    width: 100%;
    height: 100%;
    color: inherit;
  }

  .model-svg__surface {
    fill: fade(@primary-color, 8%);
    stroke: fade(@primary-color, 18%);
    stroke-width: 1.5;
  }

  .model-svg__line {
    stroke: currentColor;
    stroke-width: 3.2;
    stroke-linecap: round;
    stroke-linejoin: round;
  }

  .model-svg__muted-line {
    stroke: fade(@primary-color, 42%);
    stroke-width: 2.4;
    stroke-linecap: round;
    stroke-linejoin: round;
  }

  .model-svg__node {
    fill: @component-background;
    stroke: currentColor;
    stroke-width: 2.4;
  }

  .model-svg__cursor {
    fill: currentColor;
  }
</style>
