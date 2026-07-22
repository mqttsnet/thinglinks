<template>
  <!-- Apache IoTDB：官方风格化（绿色品牌 + 风格化字母 I + 时序节点树） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="iotdb-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#0EA371" />
        <stop offset="60%" stop-color="#14B884" />
        <stop offset="100%" stop-color="#28D8A1" />
      </linearGradient>
      <radialGradient id="iotdb-glow" cx="0.5" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.4" />
        <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0" />
      </radialGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#iotdb-bg)" />
    <ellipse cx="65" cy="36" rx="60" ry="32" fill="url(#iotdb-glow)" />

    <!-- 风格化字母 I（白色，加上下两端横线） -->
    <g fill="#FFFFFF">
      <rect x="42" y="28" width="46" height="9" rx="2" />
      <rect x="59" y="37" width="12" height="56" rx="1.5" />
      <rect x="42" y="93" width="46" height="9" rx="2" />
    </g>

    <!-- 时序节点树（IoTDB 特征：root.iot.xxx 路径分支） -->
    <g stroke="#FFFFFF" stroke-width="1.5" opacity="0.7" fill="none">
      <!-- root 节点到 sub-tree -->
      <line x1="65" y1="65" x2="32" y2="80" />
      <line x1="65" y1="65" x2="65" y2="80" />
      <line x1="65" y1="65" x2="98" y2="80" />
    </g>
    <g fill="#FFFFFF" opacity="0.85">
      <circle cx="32" cy="80" r="3.5" />
      <circle cx="65" cy="80" r="3.5" />
      <circle cx="98" cy="80" r="3.5" />
    </g>
  </svg>
</template>
