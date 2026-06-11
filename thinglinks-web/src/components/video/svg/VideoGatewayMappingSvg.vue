<template>
  <!-- 网关映射：双向箭头 + 协议转换 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 左设备 -->
    <rect x="10" y="38" width="36" height="28" rx="5" fill="#f5f0ff" stroke="#7c5cfc" stroke-width="1.5" />
    <circle cx="22" cy="48" r="3" fill="#7c5cfc" />
    <rect x="28" y="46" width="12" height="3" rx="1" fill="#c5b3f5" />
    <rect x="16" y="56" width="24" height="3" rx="1" fill="#e0d6f8" />
    <!-- 右设备 -->
    <rect x="84" y="38" width="36" height="28" rx="5" fill="#e8f0fe" stroke="#5d87ff" stroke-width="1.5" />
    <circle cx="96" cy="48" r="3" fill="#5d87ff" />
    <rect x="102" y="46" width="12" height="3" rx="1" fill="#a6c8ff" />
    <rect x="90" y="56" width="24" height="3" rx="1" fill="#d4e7fe" />
    <!-- 双向箭头 -->
    <rect x="50" y="49" width="30" height="5" rx="2" fill="#13deb9" />
    <polygon points="48,51.5 55,46 55,57" fill="#13deb9" />
    <polygon points="82,51.5 75,46 75,57" fill="#13deb9" />
    <!-- GB28181 标签 -->
    <rect x="38" y="80" width="54" height="20" rx="10" fill="#f5f8ff" stroke="#5d87ff" stroke-width="1" />
    <text x="65" y="94" text-anchor="middle" fill="#5d87ff" font-size="10" font-weight="600" font-family="sans-serif">GB28181</text>
    <!-- 连线 -->
    <line x1="28" y1="66" x2="28" y2="82" stroke="#7c5cfc" stroke-width="1" stroke-dasharray="3,2" />
    <line x1="102" y1="66" x2="102" y2="82" stroke="#5d87ff" stroke-width="1" stroke-dasharray="3,2" />
    <line x1="28" y1="82" x2="38" y2="90" stroke="#7c5cfc" stroke-width="1" stroke-dasharray="3,2" />
    <line x1="102" y1="82" x2="92" y2="90" stroke="#5d87ff" stroke-width="1" stroke-dasharray="3,2" />
  </svg>
</template>
