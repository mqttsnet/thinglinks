<template>
  <!-- PostgreSQL：官方风格化（蓝色品牌 + 风格化大象头 + DB 圆柱） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="pg-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#336791" />
        <stop offset="60%" stop-color="#2E5C7E" />
        <stop offset="100%" stop-color="#1F4054" />
      </linearGradient>
      <radialGradient id="pg-glow" cx="0.4" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#7CB6E8" stop-opacity="0.4" />
        <stop offset="100%" stop-color="#7CB6E8" stop-opacity="0" />
      </radialGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#pg-bg)" />
    <ellipse cx="55" cy="38" rx="55" ry="32" fill="url(#pg-glow)" />

    <!-- 风格化大象头（PostgreSQL 标志性吉祥物简化） -->
    <g fill="#FFFFFF">
      <!-- 头部 -->
      <ellipse cx="65" cy="55" rx="26" ry="22" />
      <!-- 鼻子 -->
      <path d="M55 70 Q50 86, 58 92 Q66 96, 64 88 Q60 82, 65 78 Z" />
      <!-- 耳朵 -->
      <ellipse cx="44" cy="46" rx="6" ry="8" transform="rotate(-30 44 46)" />
      <ellipse cx="86" cy="46" rx="6" ry="8" transform="rotate(30 86 46)" />
    </g>
    <!-- 眼睛 -->
    <circle cx="58" cy="50" r="2.2" fill="#1F4054" />
    <circle cx="74" cy="50" r="2.2" fill="#1F4054" />

    <!-- DB 圆柱形（强调"数据库"属性） -->
    <g stroke="#FFFFFF" stroke-width="1.8" fill="none" opacity="0.85">
      <ellipse cx="65" cy="100" rx="22" ry="5" />
      <line x1="43" y1="100" x2="43" y2="108" />
      <line x1="87" y1="100" x2="87" y2="108" />
      <ellipse cx="65" cy="108" rx="22" ry="5" />
    </g>
  </svg>
</template>
