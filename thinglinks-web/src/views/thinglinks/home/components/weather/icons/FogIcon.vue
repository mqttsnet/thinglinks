<template>
  <svg viewBox="0 0 40 40" class="wi-fog">
    <line class="line l1" x1="8" y1="14" x2="32" y2="14" />
    <line class="line l2" x1="6" y1="20" x2="34" y2="20" />
    <line class="line l3" x1="10" y1="26" x2="30" y2="26" />
    <line class="line l4" x1="8" y1="32" x2="32" y2="32" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-fog {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .line {
    stroke: #6b7a92;
    stroke-width: 2.3;
    stroke-linecap: round;
    opacity: 0.75;
    animation: fog-drift 4s ease-in-out infinite;
    transform-origin: center;
  }
  .l1 { animation-delay: 0s; }
  .l2 { animation-delay: 0.5s; }
  .l3 { animation-delay: 1s; }
  .l4 { animation-delay: 1.5s; }

  @keyframes fog-drift {
    0%, 100% { transform: translateX(-2px); opacity: 0.5; }
    50% { transform: translateX(2px); opacity: 0.9; }
  }
</style>
