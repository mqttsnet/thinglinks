<template>
  <!-- Redis：堆叠数据库立方体 + 红色品牌 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="redis-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#DC382C" />
        <stop offset="100%" stop-color="#A41E11" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#redis-bg)" />
    <!-- 三层立方体（数据库分层） -->
    <!-- 顶层 -->
    <ellipse cx="65" cy="38" rx="34" ry="9" fill="#fff" />
    <ellipse cx="65" cy="38" rx="34" ry="9" fill="none" stroke="#fff" stroke-width="1" />
    <!-- 顶层侧面 -->
    <path d="M 31 38 L 31 50 Q 31 56 65 56 Q 99 56 99 50 L 99 38" fill="#fff" opacity="0.85" />
    <!-- 中层 -->
    <ellipse cx="65" cy="56" rx="34" ry="9" fill="#fff" opacity="0.95" />
    <path d="M 31 56 L 31 68 Q 31 74 65 74 Q 99 74 99 68 L 99 56" fill="#fff" opacity="0.7" />
    <!-- 底层 -->
    <ellipse cx="65" cy="74" rx="34" ry="9" fill="#fff" opacity="0.9" />
    <path d="M 31 74 L 31 86 Q 31 92 65 92 Q 99 92 99 86 L 99 74" fill="#fff" opacity="0.55" />
    <ellipse cx="65" cy="92" rx="34" ry="9" fill="#fff" opacity="0.85" />
    <!-- 顶部高亮 -->
    <ellipse cx="50" cy="35" rx="8" ry="2" fill="#DC382C" opacity="0.4" />
    <!-- 文字 R -->
    <text x="65" y="115" font-family="Arial,sans-serif" font-size="12" font-weight="bold"
          fill="#fff" text-anchor="middle">REDIS</text>
  </svg>
</template>
