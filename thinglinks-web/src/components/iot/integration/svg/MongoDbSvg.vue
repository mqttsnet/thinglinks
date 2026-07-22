<template>
  <!-- MongoDB：官方风格化（绿色品牌 + 风格化叶子 logo） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="mongo-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#13AA52" />
        <stop offset="60%" stop-color="#1AC465" />
        <stop offset="100%" stop-color="#36D67D" />
      </linearGradient>
      <radialGradient id="mongo-glow" cx="0.5" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.42" />
        <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="mongo-leaf" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#FFFFFF" />
        <stop offset="100%" stop-color="#E8F8EF" />
      </linearGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#mongo-bg)" />
    <ellipse cx="65" cy="38" rx="60" ry="32" fill="url(#mongo-glow)" />

    <!-- MongoDB 标志性叶子（白色叶子状 + 中央竖纹） -->
    <path
      d="M65 28
         C 78 38, 90 50, 90 68
         C 90 86, 78 100, 65 105
         C 52 100, 40 86, 40 68
         C 40 50, 52 38, 65 28
         Z"
      fill="url(#mongo-leaf)"
    />
    <!-- 中央叶脉竖线 -->
    <path
      d="M65 28 L65 105"
      stroke="#13AA52"
      stroke-width="2"
      opacity="0.5"
    />
    <!-- 底部叶柄 -->
    <ellipse cx="65" cy="108" rx="3" ry="4" fill="#13AA52" opacity="0.6" />
  </svg>
</template>
