<template>
  <svg viewBox="0 0 40 40" class="wi-rain">
    <path class="cloud" d="M28 18a7 7 0 0 0-13.6-2 5 5 0 1 0-1.4 9.9H28a4 4 0 0 0 0-8Z" />
    <line class="drop drop-1" x1="15" y1="28" x2="13" y2="35" />
    <line class="drop drop-2" x1="20" y1="28" x2="18" y2="35" />
    <line class="drop drop-3" x1="25" y1="28" x2="23" y2="35" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-rain {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .cloud {
    fill: #f0f4fa;
    stroke: rgba(0, 0, 0, 0.08);
    stroke-width: 1;
  }

  .drop {
    stroke: #2f6fd1;
    stroke-width: 2;
    stroke-linecap: round;
    opacity: 0.9;
    animation: drop-fall 1.1s ease-in infinite;
  }
  .drop-1 { animation-delay: 0s; }
  .drop-2 { animation-delay: 0.35s; }
  .drop-3 { animation-delay: 0.7s; }

  @keyframes drop-fall {
    0% { transform: translateY(-6px); opacity: 0; }
    30% { opacity: 1; }
    100% { transform: translateY(4px); opacity: 0; }
  }
</style>
