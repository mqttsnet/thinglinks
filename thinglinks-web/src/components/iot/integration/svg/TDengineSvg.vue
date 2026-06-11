<template>
  <!-- TDengine：参照官方 logo 风格化（橙色品牌 + 风格化字母 T 含时序波形） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="td-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FF6F1F" />
        <stop offset="60%" stop-color="#FF8024" />
        <stop offset="100%" stop-color="#FFA13B" />
      </linearGradient>
      <radialGradient id="td-glow" cx="0.5" cy="0.4" r="0.7">
        <stop offset="0%" stop-color="#FFD18C" stop-opacity="0.55" />
        <stop offset="100%" stop-color="#FFA13B" stop-opacity="0" />
      </radialGradient>
    </defs>
    <!-- 圆角背景 + 高光 -->
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#td-bg)" />
    <ellipse cx="58" cy="40" rx="60" ry="36" fill="url(#td-glow)" />

    <!-- 品牌字符 T（白色实心 + 微阴影） -->
    <path
      d="M30 35 H100 L100 50 H75 V100 H55 V50 H30 Z"
      fill="#FFFFFF"
      filter="url(#td-soft)"
    />
    <filter id="td-soft" x="-10%" y="-10%" width="120%" height="120%">
      <feGaussianBlur stdDeviation="0.5" />
    </filter>

    <!-- T 横线下方时序波形（白色半透明，强调时序属性） -->
    <polyline
      points="20,82 32,72 44,80 56,66 68,76 80,62 92,72 104,68"
      stroke="#FFFFFF"
      stroke-width="2"
      fill="none"
      stroke-linecap="round"
      stroke-linejoin="round"
      opacity="0.85"
    />
    <!-- 时序数据点 -->
    <circle cx="44" cy="80" r="2.5" fill="#FFFFFF" />
    <circle cx="68" cy="76" r="2.5" fill="#FFFFFF" />
    <circle cx="92" cy="72" r="2.5" fill="#FFFFFF" />
    <!-- 时间轴 -->
    <line x1="20" y1="108" x2="110" y2="108" stroke="#FFFFFF" stroke-width="1.2" opacity="0.4" />
  </svg>
</template>
