<template>
  <svg viewBox="0 0 40 40" class="wi-cloudy">
    <circle cx="14" cy="14" r="5.5" class="sun" />
    <path class="cloud" d="M26 22a5 5 0 0 0-9.6-1.4A4 4 0 1 0 14 28h12a3.5 3.5 0 0 0 0-7 3 3 0 0 0 0 1Z" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-cloudy {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .sun {
    fill: #ffd14a;
  }

  .cloud {
    fill: #fff;
    stroke: rgba(0, 0, 0, 0.08);
    stroke-width: 1;
    animation: cloud-drift 4s ease-in-out infinite;
    transform-origin: center;
  }

  @keyframes cloud-drift {
    0%, 100% { transform: translateX(0); }
    50% { transform: translateX(1.5px); }
  }
</style>
