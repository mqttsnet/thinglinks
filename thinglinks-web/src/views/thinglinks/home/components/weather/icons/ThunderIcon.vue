<template>
  <svg viewBox="0 0 40 40" class="wi-thunder">
    <path class="cloud" d="M28 17a7 7 0 0 0-13.6-2 5 5 0 1 0-1.4 9.9H28a4 4 0 0 0 0-8Z" />
    <polygon class="bolt" points="19,25 24,25 21,31 26,31 18,37 20,31 16,31" />
  </svg>
</template>

<style lang="less" scoped>
  .wi-thunder {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .cloud {
    fill: #f0f4fa;
    stroke: rgba(0, 0, 0, 0.08);
    stroke-width: 1;
  }

  .bolt {
    fill: #ffd14a;
    filter: drop-shadow(0 0 4px rgba(255, 209, 74, 0.9));
    animation: bolt-flash 1.8s ease-in-out infinite;
    transform-origin: 21px 31px;
  }

  @keyframes bolt-flash {
    0%, 20%, 40%, 100% { opacity: 0; transform: scale(0.9); }
    10%, 30% { opacity: 1; transform: scale(1); }
  }
</style>
