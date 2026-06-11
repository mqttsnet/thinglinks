<template>
  <!-- Apache RocketMQ：火箭 + 消息流 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="rmq-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#FF6B35" />
        <stop offset="100%" stop-color="#D71618" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#rmq-bg)" />
    <!-- 火箭主体 -->
    <path
      d="M 65 25
         L 75 50
         L 75 85
         L 55 85
         L 55 50
         Z"
      fill="#fff"
    />
    <!-- 火箭头 -->
    <path d="M 65 25 L 75 50 L 55 50 Z" fill="#fff" stroke="#D71618" stroke-width="1.5" />
    <!-- 圆窗 -->
    <circle cx="65" cy="60" r="6" fill="#D71618" />
    <circle cx="65" cy="60" r="3" fill="#fff" />
    <!-- 左翼 -->
    <path d="M 55 75 L 42 95 L 55 90 Z" fill="#fff" />
    <!-- 右翼 -->
    <path d="M 75 75 L 88 95 L 75 90 Z" fill="#fff" />
    <!-- 底部尾焰 -->
    <path d="M 55 85 L 60 105 L 65 95 L 70 105 L 75 85 Z" fill="#FFD700" />
    <path d="M 60 105 L 62 110 L 64 105" fill="none" stroke="#FFD700" stroke-width="1.5" />
    <path d="M 68 105 L 70 110 L 72 105" fill="none" stroke="#FFD700" stroke-width="1.5" />
    <!-- 文字 -->
    <text x="65" y="120" font-family="Arial,sans-serif" font-size="9" font-weight="bold"
          fill="#fff" text-anchor="middle">RocketMQ</text>
  </svg>
</template>
