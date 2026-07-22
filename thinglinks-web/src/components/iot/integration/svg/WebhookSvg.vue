<template>
  <!-- WebHook：钩子 + 闪电（含 HMAC 签名意象） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="hook-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#722ED1" />
        <stop offset="100%" stop-color="#391085" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#hook-bg)" />
    <!-- WebHook 三链节点 -->
    <!-- 节点 1（左）-->
    <circle cx="32" cy="78" r="14" fill="none" stroke="#fff" stroke-width="3" />
    <circle cx="32" cy="78" r="6" fill="#fff" />
    <!-- 节点 2（顶部）-->
    <circle cx="65" cy="38" r="14" fill="none" stroke="#fff" stroke-width="3" />
    <circle cx="65" cy="38" r="6" fill="#fff" />
    <!-- 节点 3（右下）-->
    <circle cx="98" cy="78" r="14" fill="none" stroke="#fff" stroke-width="3" />
    <circle cx="98" cy="78" r="6" fill="#fff" />
    <!-- 连接线段（钩子链路）-->
    <path d="M 42 70 L 56 46" stroke="#fff" stroke-width="2.5" stroke-linecap="round" />
    <path d="M 74 46 L 88 70" stroke="#fff" stroke-width="2.5" stroke-linecap="round" />
    <!-- 闪电（签名意象） -->
    <path
      d="M 60 92 L 67 92 L 64 102 L 72 102 L 60 118 L 64 105 L 56 105 Z"
      fill="#FFD700"
    />
  </svg>
</template>
