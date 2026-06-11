<template>
  <!-- RabbitMQ：兔子 + 信箱 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="rabbit-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FF6600" />
        <stop offset="100%" stop-color="#E84D1F" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#rabbit-bg)" />
    <!-- 左耳 -->
    <rect x="36" y="28" width="14" height="36" rx="6" fill="#fff" />
    <rect x="40" y="34" width="6" height="20" rx="3" fill="#FF6600" />
    <!-- 右耳 -->
    <rect x="80" y="28" width="14" height="36" rx="6" fill="#fff" />
    <rect x="84" y="34" width="6" height="20" rx="3" fill="#FF6600" />
    <!-- 头部 -->
    <ellipse cx="65" cy="78" rx="32" ry="22" fill="#fff" />
    <!-- 眼睛 -->
    <circle cx="54" cy="74" r="3.5" fill="#FF6600" />
    <circle cx="76" cy="74" r="3.5" fill="#FF6600" />
    <circle cx="54.5" cy="73" r="1.2" fill="#fff" />
    <circle cx="76.5" cy="73" r="1.2" fill="#fff" />
    <!-- 鼻子 -->
    <ellipse cx="65" cy="82" rx="3" ry="2" fill="#FF6600" />
    <!-- 嘴 -->
    <path d="M 65 85 L 65 90 M 60 90 Q 65 94 70 90" stroke="#FF6600" stroke-width="1.5" fill="none" stroke-linecap="round" />
    <!-- 腮红 -->
    <ellipse cx="48" cy="84" rx="4" ry="2.5" fill="#FFCCAA" opacity="0.8" />
    <ellipse cx="82" cy="84" rx="4" ry="2.5" fill="#FFCCAA" opacity="0.8" />
    <!-- 胡须 -->
    <line x1="48" y1="89" x2="40" y2="87" stroke="#FF6600" stroke-width="1" />
    <line x1="48" y1="91" x2="40" y2="92" stroke="#FF6600" stroke-width="1" />
    <line x1="82" y1="89" x2="90" y2="87" stroke="#FF6600" stroke-width="1" />
    <line x1="82" y1="91" x2="90" y2="92" stroke="#FF6600" stroke-width="1" />
  </svg>
</template>
