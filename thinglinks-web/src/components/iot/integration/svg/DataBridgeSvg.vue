<template>
  <!-- 数据桥接：双向流箭头 + 桥梁意象（用于桥接规则卡片/空状态图） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="bridge-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#13C2C2" />
        <stop offset="100%" stop-color="#006D75" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#bridge-bg)" />
    <!-- 左侧：平台（设备图标）-->
    <rect x="22" y="48" width="22" height="34" rx="3" fill="#fff" />
    <rect x="26" y="52" width="14" height="3" rx="1" fill="#13C2C2" />
    <rect x="26" y="58" width="14" height="3" rx="1" fill="#13C2C2" opacity="0.6" />
    <rect x="26" y="64" width="14" height="3" rx="1" fill="#13C2C2" opacity="0.6" />
    <circle cx="33" cy="76" r="2" fill="#52C41A" />
    <!-- 中间桥梁（双向箭头） -->
    <line x1="48" y1="58" x2="80" y2="58" stroke="#fff" stroke-width="2" stroke-linecap="round" />
    <polygon points="80,58 76,55 76,61" fill="#fff" />
    <line x1="80" y1="72" x2="48" y2="72" stroke="#fff" stroke-width="2" stroke-linecap="round" />
    <polygon points="48,72 52,69 52,75" fill="#fff" />
    <!-- 右侧：第三方系统（云图标）-->
    <path
      d="M 86 60
         Q 86 52 94 52
         Q 96 46 102 46
         Q 110 46 110 54
         Q 116 54 116 62
         Q 116 70 108 70
         L 92 70
         Q 84 70 84 62 Z"
      fill="#fff"
    />
    <!-- 桥墩 -->
    <rect x="58" y="82" width="3" height="14" fill="#fff" opacity="0.7" />
    <rect x="68" y="82" width="3" height="14" fill="#fff" opacity="0.7" />
    <!-- 标题文字 -->
    <text x="65" y="115" font-family="Arial,sans-serif" font-size="10" font-weight="bold"
          fill="#fff" text-anchor="middle">DATA BRIDGE</text>
  </svg>
</template>
