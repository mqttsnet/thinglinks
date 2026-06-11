<template>
  <!-- 达梦 DM 数据库：品牌深红 + 风格化"DM"字标 + DB 圆柱 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="dm-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#C8102E" />
        <stop offset="60%" stop-color="#A60D24" />
        <stop offset="100%" stop-color="#7A0919" />
      </linearGradient>
      <radialGradient id="dm-glow" cx="0.3" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#FFB6B6" stop-opacity="0.35" />
        <stop offset="100%" stop-color="#FFB6B6" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="dm-letter" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#FFFFFF" />
        <stop offset="100%" stop-color="#F5E6E8" />
      </linearGradient>
    </defs>

    <!-- 背景 -->
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#dm-bg)" />
    <ellipse cx="50" cy="36" rx="55" ry="32" fill="url(#dm-glow)" />

    <!-- "DM" 字标（D 圆角矩形 + M 三角形山峰，达梦官方风格化） -->
    <!-- D -->
    <path
      d="M 28 36
         L 28 80
         L 50 80
         Q 64 80 64 58
         Q 64 36 50 36
         Z
         M 38 46
         L 38 70
         L 49 70
         Q 54 70 54 58
         Q 54 46 49 46
         Z"
      fill="url(#dm-letter)"
      fill-rule="evenodd"
    />

    <!-- M（山峰风格，呼应"达梦"的"梦"意境） -->
    <path
      d="M 70 80
         L 70 36
         L 78 36
         L 86 56
         L 94 36
         L 102 36
         L 102 80
         L 95 80
         L 95 50
         L 89 64
         L 83 64
         L 77 50
         L 77 80 Z"
      fill="url(#dm-letter)"
    />

    <!-- DB 圆柱形（强调数据库属性） -->
    <g stroke="#FFFFFF" stroke-width="1.8" fill="none" opacity="0.75">
      <ellipse cx="65" cy="98" rx="28" ry="5" />
      <line x1="37" y1="98" x2="37" y2="108" />
      <line x1="93" y1="98" x2="93" y2="108" />
      <ellipse cx="65" cy="108" rx="28" ry="5" />
    </g>
  </svg>
</template>
