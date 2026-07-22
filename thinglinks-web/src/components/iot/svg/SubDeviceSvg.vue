<template>
  <!-- 子设备(SUBDEVICE):3D 玻璃质感 ── 小型从属设备 + 父链接虚线,呼应"挂载在网关下" -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="sd-body-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#d3e3ff" />
        <stop offset="100%" stop-color="#9bbef0" />
      </linearGradient>
      <linearGradient id="sd-body-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#92b6ec" />
        <stop offset="100%" stop-color="#5a83cd" />
      </linearGradient>
      <linearGradient id="sd-body-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#79a1e0" />
        <stop offset="100%" stop-color="#456fb8" />
      </linearGradient>
      <linearGradient id="sd-screen" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#fbfdff" />
        <stop offset="100%" stop-color="#dde9fa" />
      </linearGradient>
      <radialGradient id="sd-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#8ba2cf" stop-opacity="0.45" />
        <stop offset="100%" stop-color="#8ba2cf" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="sd-parent" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#9b75e6" stop-opacity="0.6" />
        <stop offset="100%" stop-color="#9b75e6" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 接地柔影 -->
    <ellipse cx="65" cy="115" rx="32" ry="6" fill="url(#sd-shadow)" />

    <!-- 顶部父网关节点(紫色,代表 parent)+ 连接虚线 -->
    <ellipse cx="65" cy="22" rx="14" ry="6" fill="url(#sd-parent)" />
    <circle cx="65" cy="22" r="5.5" fill="#9b75e6" />
    <circle cx="63.5" cy="20.5" r="1.4" fill="#ffffff" fill-opacity="0.9" />
    <!-- "GW" 文字提示父级身份 -->
    <text x="65" y="25" text-anchor="middle" fill="#ffffff" font-size="6" font-family="ui-monospace, Menlo, monospace" font-weight="700">GW</text>

    <!-- 父子虚线连接 -->
    <line x1="65" y1="29" x2="65" y2="48" stroke="#9b75e6" stroke-width="1.5" stroke-opacity="0.55" stroke-dasharray="3 2" stroke-linecap="round" />
    <!-- 中段小箭头 -->
    <polygon points="62,40 68,40 65,44" fill="#9b75e6" fill-opacity="0.7" />

    <!-- 子设备主体(小型,蓝色立方) -->
    <polygon points="44,68 65,79 86,68 65,57" fill="url(#sd-body-top)" />
    <polygon points="44,68 65,79 65,103 44,92" fill="url(#sd-body-l)" />
    <polygon points="86,68 65,79 65,103 86,92" fill="url(#sd-body-r)" />

    <!-- 顶面顶部边缘高光 -->
    <polyline points="44,68 65,57 86,68" fill="none" stroke="#ffffff" stroke-width="1.4" stroke-opacity="0.75" stroke-linejoin="round" />

    <!-- 左面小屏 + 两点状态灯 -->
    <polygon points="49,75 60,81 60,87.5 49,81.5" fill="url(#sd-screen)" />
    <polygon points="49,75 60,81 60,82 49,76" fill="#ffffff" fill-opacity="0.4" />
    <circle cx="52" cy="80" r="1" fill="#13deb9" />
    <circle cx="55.5" cy="82" r="1" fill="#ffae1f" />

    <!-- 右面 2 条散热栅 -->
    <line x1="70" y1="80" x2="82" y2="74" stroke="#ffffff" stroke-width="0.8" stroke-opacity="0.5" />
    <line x1="70" y1="86" x2="82" y2="80" stroke="#ffffff" stroke-width="0.8" stroke-opacity="0.45" />

    <!-- 中心垂直棱高光 -->
    <line x1="65" y1="79" x2="65" y2="103" stroke="#ffffff" stroke-width="1.1" stroke-opacity="0.45" stroke-linecap="round" />
    <!-- 顶面光斑 -->
    <polygon points="63,59 67,59 67,62 63,62" fill="#ffffff" fill-opacity="0.5" />
  </svg>
</template>
