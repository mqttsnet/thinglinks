<template>
  <svg viewBox="0 0 40 40" class="wi-sunny">
    <g class="rays">
      <line v-for="i in 8" :key="i" :transform="`rotate(${i * 45} 20 20)`" x1="20" y1="3" x2="20" y2="8" />
    </g>
    <circle cx="20" cy="20" r="7" class="core" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-sunny {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .core {
    fill: #ffd14a;
    filter: drop-shadow(0 0 3px rgba(255, 209, 74, 0.7));
  }

  .rays {
    stroke: #ff9f2b;
    stroke-width: 2.2;
    stroke-linecap: round;
    transform-origin: 20px 20px;
    animation: rays-rotate 6s linear infinite;
  }

  @keyframes rays-rotate {
    0% { transform: rotate(0) scale(1); opacity: 0.85; }
    50% { transform: rotate(180deg) scale(1.08); opacity: 1; }
    100% { transform: rotate(360deg) scale(1); opacity: 0.85; }
  }
</style>
