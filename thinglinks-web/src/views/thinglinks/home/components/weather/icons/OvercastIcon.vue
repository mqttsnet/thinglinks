<template>
  <svg viewBox="0 0 40 40" class="wi-overcast">
    <path class="cloud" d="M28 19a7 7 0 0 0-13.6-2 5 5 0 1 0-1.4 9.9H28a4 4 0 0 0 0-8Z" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-overcast {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .cloud {
    fill: #eef1f7;
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
