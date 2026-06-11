<template>
  <!-- MQTT：发布订阅星型 + IoT 节点 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="mqtt-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#660066" />
        <stop offset="100%" stop-color="#9900CC" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#mqtt-bg)" />
    <!-- 中央 broker（六边形） -->
    <polygon points="65,42 82,52 82,72 65,82 48,72 48,52" fill="#fff" />
    <text x="65" y="68" font-family="Arial,sans-serif" font-size="12" font-weight="bold"
          fill="#660066" text-anchor="middle">MQTT</text>
    <!-- 顶部 IoT 节点（设备）-->
    <circle cx="40" cy="32" r="6" fill="#fff" />
    <rect x="62" y="22" width="6" height="10" rx="1" fill="#fff" />
    <rect x="60" y="32" width="10" height="2" rx="1" fill="#fff" />
    <circle cx="90" cy="32" r="6" fill="#fff" />
    <!-- 信号波纹（连接线） -->
    <line x1="42" y1="38" x2="55" y2="50" stroke="#fff" stroke-width="1.5" opacity="0.6" stroke-dasharray="2 2" />
    <line x1="65" y1="32" x2="65" y2="42" stroke="#fff" stroke-width="1.5" opacity="0.6" stroke-dasharray="2 2" />
    <line x1="88" y1="38" x2="75" y2="50" stroke="#fff" stroke-width="1.5" opacity="0.6" stroke-dasharray="2 2" />
    <!-- 底部订阅者节点 -->
    <rect x="34" y="92" width="14" height="10" rx="2" fill="#fff" />
    <rect x="58" y="92" width="14" height="10" rx="2" fill="#fff" />
    <rect x="82" y="92" width="14" height="10" rx="2" fill="#fff" />
    <!-- broker → consumers 连接线 -->
    <line x1="55" y1="78" x2="42" y2="92" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="65" y1="82" x2="65" y2="92" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="75" y1="78" x2="88" y2="92" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <!-- 信号波纹（顶部弧线） -->
    <path d="M 30 25 Q 40 15 50 25" fill="none" stroke="#fff" stroke-width="1" opacity="0.5" />
    <path d="M 80 25 Q 90 15 100 25" fill="none" stroke="#fff" stroke-width="1" opacity="0.5" />
  </svg>
</template>
