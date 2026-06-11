<template>
  <!-- Apache Pulsar：官方风格化（紫色品牌 + 黄色光球 + 辐射波） -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="pulsar-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#1A0B40" />
        <stop offset="50%" stop-color="#3D1A78" />
        <stop offset="100%" stop-color="#6028B5" />
      </linearGradient>
      <radialGradient id="pulsar-core" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#FFE066" />
        <stop offset="60%" stop-color="#F2A413" />
        <stop offset="100%" stop-color="#F2A413" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="pulsar-halo" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#FFD700" stop-opacity="0.4" />
        <stop offset="100%" stop-color="#FFD700" stop-opacity="0" />
      </radialGradient>
    </defs>
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#pulsar-bg)" />

    <!-- 外层光晕 -->
    <circle cx="65" cy="65" r="50" fill="url(#pulsar-halo)" />
    <!-- 核心光球（脉冲星） -->
    <circle cx="65" cy="65" r="22" fill="url(#pulsar-core)" />
    <!-- 中心高亮点 -->
    <circle cx="65" cy="65" r="8" fill="#FFFFFF" />

    <!-- 辐射波（4 条对称弧） -->
    <g stroke="#FFD700" stroke-width="2" fill="none" stroke-linecap="round" opacity="0.7">
      <path d="M65 30 Q35 50, 35 65" />
      <path d="M65 30 Q95 50, 95 65" />
      <path d="M65 100 Q35 80, 35 65" />
      <path d="M65 100 Q95 80, 95 65" />
    </g>

    <!-- 闪光线条（强调"快"特性） -->
    <g stroke="#FFFFFF" stroke-width="1.2" fill="none" opacity="0.5">
      <line x1="65" y1="20" x2="65" y2="32" />
      <line x1="65" y1="98" x2="65" y2="110" />
      <line x1="20" y1="65" x2="32" y2="65" />
      <line x1="98" y1="65" x2="110" y2="65" />
    </g>
  </svg>
</template>
