<template>
  <!-- CA 许可证证书:盾牌 + 证书勾选 (用于 CA 许可证证书卡片/详情头) -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="cacert-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#52C41A" />
        <stop offset="100%" stop-color="#237804" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#cacert-bg)" />

    <!-- 证书本体 -->
    <rect x="30" y="30" width="64" height="58" rx="4" fill="#fff" />
    <rect x="36" y="38" width="36" height="3" rx="1" fill="#237804" />
    <rect x="36" y="46" width="48" height="3" rx="1" fill="#237804" opacity="0.7" />
    <rect x="36" y="54" width="40" height="3" rx="1" fill="#237804" opacity="0.5" />
    <rect x="36" y="62" width="28" height="3" rx="1" fill="#237804" opacity="0.5" />

    <!-- 右下角盾牌 + 勾选 -->
    <path
      d="M 88 62
         L 110 62
         L 110 80
         Q 110 92 99 96
         Q 88 92 88 80 Z"
      fill="#FAAD14"
      stroke="#fff"
      stroke-width="2"
    />
    <path
      d="M 93 79 L 98 84 L 105 75"
      stroke="#fff"
      stroke-width="2.5"
      stroke-linecap="round"
      stroke-linejoin="round"
      fill="none"
    />

    <!-- 标题文字 -->
    <text x="65" y="115" font-family="Arial,sans-serif" font-size="10" font-weight="bold"
          fill="#fff" text-anchor="middle">CA CERT</text>
  </svg>
</template>
