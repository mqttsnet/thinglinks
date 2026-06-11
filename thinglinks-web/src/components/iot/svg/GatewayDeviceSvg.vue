<template>
  <!-- 网关设备(GATEWAY):3D 玻璃质感 ── 主网关枢纽 + 左右子设备 + 双信号波,表达"挂载多个子设备" -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="gd-body-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#bcd8ff" />
        <stop offset="100%" stop-color="#7ba6f0" />
      </linearGradient>
      <linearGradient id="gd-body-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#6394ec" />
        <stop offset="100%" stop-color="#2f5fbb" />
      </linearGradient>
      <linearGradient id="gd-body-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#4d80df" />
        <stop offset="100%" stop-color="#1f4ea9" />
      </linearGradient>
      <linearGradient id="gd-sub-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#fbfdff" />
        <stop offset="100%" stop-color="#dde9fa" />
      </linearGradient>
      <linearGradient id="gd-sub-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#cedcf4" />
        <stop offset="100%" stop-color="#9eb7e0" />
      </linearGradient>
      <linearGradient id="gd-sub-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#a9c0eb" />
        <stop offset="100%" stop-color="#7693c8" />
      </linearGradient>
      <linearGradient id="gd-screen" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#8cc4ff" />
        <stop offset="100%" stop-color="#3f86f0" />
      </linearGradient>
      <radialGradient id="gd-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#8ba2cf" stop-opacity="0.55" />
        <stop offset="100%" stop-color="#8ba2cf" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="gd-wave" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#1677ff" stop-opacity="0.5" />
        <stop offset="100%" stop-color="#1677ff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 接地柔影(更宽,因为有 3 个设备) -->
    <ellipse cx="65" cy="115" rx="48" ry="8" fill="url(#gd-shadow)" />

    <!-- 顶部双信号波(网关广播 + 上行) -->
    <ellipse cx="65" cy="25" rx="26" ry="6" fill="url(#gd-wave)" />
    <path d="M 42 22 Q 65 12 88 22" stroke="#5d87ff" stroke-width="1.6" stroke-opacity="0.55" fill="none" stroke-linecap="round" />
    <path d="M 49 17 Q 65 8 81 17" stroke="#5d87ff" stroke-width="1.4" stroke-opacity="0.38" fill="none" stroke-linecap="round" />
    <!-- 上行小球 -->
    <circle cx="65" cy="20" r="2.6" fill="#1677ff" />
    <circle cx="64" cy="19" r="0.8" fill="#ffffff" fill-opacity="0.9" />

    <!-- 子设备 1(左下,小立方) -->
    <polygon points="20,82 32,88 32,98 20,92" fill="url(#gd-sub-l)" />
    <polygon points="44,82 32,88 32,98 44,92" fill="url(#gd-sub-r)" />
    <polygon points="32,76 44,82 32,88 20,82" fill="url(#gd-sub-top)" />
    <circle cx="32" cy="91.5" r="1.5" fill="#13deb9" />
    <!-- 连接线到主网关 -->
    <line x1="32" y1="92" x2="48" y2="80" stroke="#5d87ff" stroke-width="1.2" stroke-opacity="0.55" stroke-dasharray="2 2" />

    <!-- 子设备 2(右下,小立方) -->
    <polygon points="86,82 98,88 98,98 86,92" fill="url(#gd-sub-l)" />
    <polygon points="110,82 98,88 98,98 110,92" fill="url(#gd-sub-r)" />
    <polygon points="98,76 110,82 98,88 86,82" fill="url(#gd-sub-top)" />
    <circle cx="98" cy="91.5" r="1.5" fill="#ffae1f" />
    <line x1="98" y1="92" x2="82" y2="80" stroke="#5d87ff" stroke-width="1.2" stroke-opacity="0.55" stroke-dasharray="2 2" />

    <!-- 主网关枢纽(更大,中心) -->
    <polygon points="42,58 65,70 88,58 65,46" fill="url(#gd-body-top)" />
    <polygon points="42,58 65,70 65,98 42,86" fill="url(#gd-body-l)" />
    <polygon points="88,58 65,70 65,98 88,86" fill="url(#gd-body-r)" />

    <!-- 顶面顶部边缘高光 -->
    <polyline points="42,58 65,46 88,58" fill="none" stroke="#ffffff" stroke-width="1.6" stroke-opacity="0.8" stroke-linejoin="round" />

    <!-- 主网关大屏(发光) -->
    <polygon points="46,66 60,73 60,82 46,75" fill="url(#gd-screen)" />
    <polygon points="46,66 60,73 60,74.5 46,67.5" fill="#ffffff" fill-opacity="0.5" />
    <!-- 屏内活动条 -->
    <line x1="48" y1="70" x2="58" y2="75" stroke="#ffffff" stroke-width="0.7" stroke-opacity="0.7" />
    <line x1="48" y1="73" x2="58" y2="78" stroke="#ffffff" stroke-width="0.7" stroke-opacity="0.55" />

    <!-- 右面散热栅条 -->
    <line x1="70" y1="74" x2="84" y2="66" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.5" />
    <line x1="70" y1="80" x2="84" y2="72" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.45" />
    <line x1="70" y1="86" x2="84" y2="78" stroke="#ffffff" stroke-width="0.9" stroke-opacity="0.4" />

    <!-- 棱边高光 -->
    <line x1="65" y1="70" x2="65" y2="98" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.45" stroke-linecap="round" />
    <polygon points="63,48 67,48 67,51 63,51" fill="#ffffff" fill-opacity="0.55" />
  </svg>
</template>
