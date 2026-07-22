<template>
  <!-- HTTP：浏览器窗口 + 大括号 + 网络节点 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="http-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#1890FF" />
        <stop offset="100%" stop-color="#0050B3" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#http-bg)" />
    <!-- 浏览器窗口 -->
    <rect x="22" y="28" width="86" height="74" rx="6" fill="#fff" />
    <!-- 标题栏 -->
    <rect x="22" y="28" width="86" height="14" rx="6" fill="#E6F4FF" />
    <rect x="22" y="36" width="86" height="6" fill="#E6F4FF" />
    <circle cx="30" cy="35" r="2" fill="#FF5F56" />
    <circle cx="38" cy="35" r="2" fill="#FFBD2E" />
    <circle cx="46" cy="35" r="2" fill="#27C93F" />
    <!-- URL 栏 -->
    <rect x="56" y="32" width="46" height="6" rx="3" fill="#fff" stroke="#1890FF" stroke-width="0.5" />
    <!-- 大括号 + JSON 内容 -->
    <text x="32" y="65" font-family="Monaco,monospace" font-size="12" font-weight="bold" fill="#1890FF">{</text>
    <line x1="38" y1="58" x2="80" y2="58" stroke="#1890FF" stroke-width="1.5" stroke-linecap="round" />
    <line x1="38" y1="66" x2="68" y2="66" stroke="#0050B3" stroke-width="1.5" stroke-linecap="round" opacity="0.6" />
    <line x1="38" y1="74" x2="76" y2="74" stroke="#0050B3" stroke-width="1.5" stroke-linecap="round" opacity="0.6" />
    <line x1="38" y1="82" x2="60" y2="82" stroke="#0050B3" stroke-width="1.5" stroke-linecap="round" opacity="0.4" />
    <text x="86" y="92" font-family="Monaco,monospace" font-size="12" font-weight="bold" fill="#1890FF">}</text>
    <!-- HTTP 文字 -->
    <text x="65" y="120" font-family="Arial,sans-serif" font-size="10" font-weight="bold"
          fill="#fff" text-anchor="middle">HTTP API</text>
  </svg>
</template>
