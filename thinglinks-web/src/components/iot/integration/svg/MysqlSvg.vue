<template>
  <!-- MySQL：海豚 + 数据库圆柱 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="mysql-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#00758F" />
        <stop offset="100%" stop-color="#003545" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#mysql-bg)" />
    <!-- 数据库圆柱 -->
    <ellipse cx="65" cy="36" rx="30" ry="7" fill="#fff" />
    <path d="M 35 36 L 35 62 Q 35 68 65 68 Q 95 68 95 62 L 95 36" fill="#fff" opacity="0.95" />
    <ellipse cx="65" cy="62" rx="30" ry="7" fill="#fff" opacity="0.4" />
    <!-- 中部分隔线 -->
    <ellipse cx="65" cy="49" rx="30" ry="7" fill="none" stroke="#00758F" stroke-width="0.8" opacity="0.5" />
    <!-- 海豚（橙色） -->
    <path
      d="M 50 78
         Q 60 70 75 75
         Q 85 78 88 88
         Q 90 96 82 100
         L 78 96
         Q 78 92 75 90
         Q 70 88 65 90
         L 60 95
         Q 56 92 54 88
         Q 50 84 50 78 Z"
      fill="#F29111"
    />
    <!-- 海豚眼 -->
    <circle cx="78" cy="84" r="1.8" fill="#fff" />
    <!-- 海豚尾鳍 -->
    <path d="M 50 78 L 42 73 L 45 80 L 38 82 L 48 84 Z" fill="#F29111" />
    <!-- 文字 -->
    <text x="65" y="118" font-family="Arial,sans-serif" font-size="11" font-weight="bold"
          fill="#fff" text-anchor="middle">MySQL</text>
  </svg>
</template>
