<template>
  <!-- InfluxDB：官方风格化（紫蓝渐变 + 风格化字母 I 上升箭头） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="influx-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#22ADF6" />
        <stop offset="50%" stop-color="#9B2FFF" />
        <stop offset="100%" stop-color="#D335B6" />
      </linearGradient>
      <radialGradient id="influx-glow" cx="0.3" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.35" />
        <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0" />
      </radialGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#influx-bg)" />
    <ellipse cx="50" cy="38" rx="55" ry="32" fill="url(#influx-glow)" />

    <!-- 风格化字母 I（白色，上窄下宽，模拟官方 logo） -->
    <g fill="#FFFFFF">
      <!-- 顶横 -->
      <rect x="38" y="28" width="54" height="10" rx="2" />
      <!-- 中竖（带小角度的下宽上窄） -->
      <polygon points="58,38 72,38 76,98 54,98" />
      <!-- 底横 -->
      <rect x="34" y="98" width="62" height="10" rx="2" />
    </g>

    <!-- 上升箭头（强调时序数据增长） -->
    <polyline
      points="20,90 35,75 50,82 65,60 80,68 95,45 110,52"
      stroke="#FFFFFF"
      stroke-width="2.5"
      fill="none"
      stroke-linecap="round"
      stroke-linejoin="round"
      opacity="0.55"
    />
    <polygon points="105,42 115,46 110,55" fill="#FFFFFF" opacity="0.55" />
  </svg>
</template>
