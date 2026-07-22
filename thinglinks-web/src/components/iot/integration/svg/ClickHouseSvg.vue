<template>
  <!-- ClickHouse：官方风格化（黄色品牌色 + 风格化柱状立柱） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="ch-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FFEB3B" />
        <stop offset="60%" stop-color="#FFD600" />
        <stop offset="100%" stop-color="#FFAB00" />
      </linearGradient>
      <radialGradient id="ch-glow" cx="0.5" cy="0.3" r="0.8">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.45" />
        <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0" />
      </radialGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#ch-bg)" />
    <ellipse cx="65" cy="38" rx="60" ry="32" fill="url(#ch-glow)" />

    <!-- ClickHouse 标志性柱状（5 根白色立柱 + 中间 1 根细立柱，模拟官方 logo） -->
    <g fill="#262626">
      <rect x="32" y="34" width="9" height="62" rx="1.5" />
      <rect x="46" y="34" width="9" height="62" rx="1.5" />
      <rect x="60" y="34" width="9" height="62" rx="1.5" />
      <rect x="74" y="34" width="9" height="62" rx="1.5" />
      <rect x="88" y="34" width="9" height="62" rx="1.5" />
      <!-- 中间细横条（官方 logo 特征） -->
      <rect x="32" y="62" width="65" height="6" />
    </g>

    <!-- 闪电符号（强调"快"特性） -->
    <path
      d="M105 30 L96 50 L102 50 L94 70 L108 46 L102 46 Z"
      fill="#262626"
      opacity="0.85"
    />
  </svg>
</template>
