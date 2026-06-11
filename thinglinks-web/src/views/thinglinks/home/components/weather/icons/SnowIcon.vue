<template>
  <svg viewBox="0 0 40 40" class="wi-snow">
    <path class="cloud" d="M28 18a7 7 0 0 0-13.6-2 5 5 0 1 0-1.4 9.9H28a4 4 0 0 0 0-8Z" />
    <g class="flake flake-1" transform="translate(14 31)"><circle r="1.2" /></g>
    <g class="flake flake-2" transform="translate(20 31)"><circle r="1.2" /></g>
    <g class="flake flake-3" transform="translate(26 31)"><circle r="1.2" /></g>
  </svg>
</template>

<style lang="less" scoped>
  .wi-snow {
    width: 100%;
    height: 100%;
    overflow: visible;
  }

  .cloud {
    fill: #f0f4fa;
    stroke: rgba(0, 0, 0, 0.08);
    stroke-width: 1;
  }

  .flake {
    fill: #fff;
    animation: flake-fall 2.4s ease-in-out infinite;
  }
  .flake-1 { animation-delay: 0s; }
  .flake-2 { animation-delay: 0.6s; }
  .flake-3 { animation-delay: 1.2s; }

  @keyframes flake-fall {
    0% { transform: translateY(-4px) scale(1); opacity: 0; }
    30% { opacity: 1; }
    100% { transform: translateY(6px) scale(0.6); opacity: 0; }
  }
</style>
