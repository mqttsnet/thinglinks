<template>
  <!-- 普通设备(COMMON):3D 玻璃质感,与产品 SVG 同色系但更"实体化" ── 单设备 + 信号波 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!-- 设备主体顶面(蓝青) -->
      <linearGradient id="cd-body-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#a3cbff" />
        <stop offset="100%" stop-color="#6ea4f3" />
      </linearGradient>
      <!-- 设备主体左面 -->
      <linearGradient id="cd-body-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#6ea4f3" />
        <stop offset="100%" stop-color="#3d77df" />
      </linearGradient>
      <!-- 设备主体右面 -->
      <linearGradient id="cd-body-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#5d92ea" />
        <stop offset="100%" stop-color="#2d63c9" />
      </linearGradient>
      <!-- 顶部指示屏 -->
      <linearGradient id="cd-screen" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#fbfdff" />
        <stop offset="100%" stop-color="#cfe1ff" />
      </linearGradient>
      <!-- 接地阴影 -->
      <radialGradient id="cd-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#8ba2cf" stop-opacity="0.5" />
        <stop offset="100%" stop-color="#8ba2cf" stop-opacity="0" />
      </radialGradient>
      <!-- 信号波光晕 -->
      <radialGradient id="cd-wave" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#1677ff" stop-opacity="0.5" />
        <stop offset="100%" stop-color="#1677ff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 接地柔影 -->
    <ellipse cx="65" cy="113" rx="36" ry="7" fill="url(#cd-shadow)" />

    <!-- 信号波(同心扇形,呼应"传感器/数据上报") -->
    <ellipse cx="65" cy="34" rx="22" ry="6" fill="url(#cd-wave)" />
    <path d="M 47 30 Q 65 22 83 30" stroke="#5d87ff" stroke-width="1.5" stroke-opacity="0.6" fill="none" stroke-linecap="round" />
    <path d="M 53 26 Q 65 20 77 26" stroke="#5d87ff" stroke-width="1.4" stroke-opacity="0.45" fill="none" stroke-linecap="round" />
    <path d="M 58 22 Q 65 18 72 22" stroke="#5d87ff" stroke-width="1.3" stroke-opacity="0.3" fill="none" stroke-linecap="round" />

    <!-- 顶部信号小球 -->
    <circle cx="65" cy="36" r="2.5" fill="#1677ff" />
    <circle cx="64" cy="35" r="0.8" fill="#ffffff" fill-opacity="0.9" />

    <!-- 设备主体:等距 3D ── 上面 + 左面 + 右面 -->
    <polygon points="38,58 65,72 92,58 65,44" fill="url(#cd-body-top)" />
    <polygon points="38,58 65,72 65,104 38,90" fill="url(#cd-body-l)" />
    <polygon points="92,58 65,72 65,104 92,90" fill="url(#cd-body-r)" />

    <!-- 顶面顶部边缘高光 -->
    <polyline points="38,58 65,44 92,58" fill="none" stroke="#ffffff" stroke-width="1.5" stroke-opacity="0.75" stroke-linejoin="round" />

    <!-- 顶面装饰屏(指示灯组,左面) -->
    <polygon points="45,67 60,74.5 60,82 45,74.5" fill="url(#cd-screen)" />
    <polygon points="45,67 60,74.5 60,76 45,68.5" fill="#ffffff" fill-opacity="0.4" />
    <!-- 屏内 3 颗状态点 -->
    <circle cx="49" cy="73" r="1.2" fill="#13deb9" />
    <circle cx="52.5" cy="74.5" r="1.2" fill="#ffae1f" />
    <circle cx="56" cy="76" r="1.2" fill="#5d87ff" />

    <!-- 右面散热栅条 -->
    <line x1="70" y1="75" x2="86" y2="67" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.55" />
    <line x1="70" y1="80" x2="86" y2="72" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.5" />
    <line x1="70" y1="85" x2="86" y2="77" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.45" />

    <!-- 棱边高光(中心垂直 + 顶面斜边) -->
    <line x1="65" y1="72" x2="65" y2="104" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.45" stroke-linecap="round" />
    <polygon points="63,46 67,46 67,49 63,49" fill="#ffffff" fill-opacity="0.5" />
  </svg>
</template>
