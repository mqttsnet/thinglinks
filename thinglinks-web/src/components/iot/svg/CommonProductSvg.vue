<template>
  <!-- 普通产品(COMMON):等距 3D 玻璃质感 ── 分层底座 + 直连设备模块,参考专业 UI 插画风格,适配 flexy 蓝色调 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!-- 下层平台顶面渐变(浅) -->
      <linearGradient id="cp-low-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#eef3fc" />
        <stop offset="100%" stop-color="#dae6f7" />
      </linearGradient>
      <!-- 上层底座顶面渐变(蓝) -->
      <linearGradient id="cp-up-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#7eadf7" />
        <stop offset="100%" stop-color="#5b91ec" />
      </linearGradient>
      <!-- 设备玻璃顶面 -->
      <linearGradient id="cp-glass-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#fbfdff" />
        <stop offset="100%" stop-color="#e6f0ff" />
      </linearGradient>
      <!-- 设备玻璃左面 -->
      <linearGradient id="cp-glass-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#dde9fd" />
        <stop offset="100%" stop-color="#bcd2f3" />
      </linearGradient>
      <!-- 设备玻璃右面 -->
      <linearGradient id="cp-glass-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#c6dbfa" />
        <stop offset="100%" stop-color="#9cbef0" />
      </linearGradient>
      <!-- 设备屏幕(发光) -->
      <linearGradient id="cp-screen" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#6ba6f8" />
        <stop offset="100%" stop-color="#2f6fe6" />
      </linearGradient>
      <!-- 接地柔影 -->
      <radialGradient id="cp-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#8ba2cf" stop-opacity="0.5" />
        <stop offset="100%" stop-color="#8ba2cf" stop-opacity="0" />
      </radialGradient>
      <!-- 天线节点光晕 -->
      <radialGradient id="cp-accent" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#1677ff" stop-opacity="0.55" />
        <stop offset="100%" stop-color="#1677ff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 接地柔影 -->
    <ellipse cx="65" cy="116" rx="42" ry="9" fill="url(#cp-shadow)" />

    <!-- 下层平台(瓷砖感):左面 / 右面 / 顶面 + 分格 + 顶棱高光 -->
    <polygon points="25,88 65,108 65,118 25,98" fill="#b6c9ec" />
    <polygon points="105,88 65,108 65,118 105,98" fill="#cedcf4" />
    <polygon points="65,68 105,88 65,108 25,88" fill="url(#cp-low-top)" />
    <line x1="85" y1="78" x2="45" y2="98" stroke="#a7b9dd" stroke-width="1" stroke-opacity="0.5" />
    <line x1="45" y1="78" x2="85" y2="98" stroke="#a7b9dd" stroke-width="1" stroke-opacity="0.5" />
    <polyline points="25,88 65,68 105,88" fill="none" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.5" stroke-linejoin="round" />

    <!-- 上层底座(蓝色台):左面 / 右面 / 顶面 + 顶棱高光 -->
    <polygon points="39,79 65,92 65,101 39,88" fill="#3f74dd" />
    <polygon points="91,79 65,92 65,101 91,88" fill="#5285e6" />
    <polygon points="65,66 91,79 65,92 39,79" fill="url(#cp-up-top)" />
    <polyline points="39,79 65,66 91,79" fill="none" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.55" stroke-linejoin="round" />

    <!-- 天线 + 发光信号节点 -->
    <line x1="65" y1="41" x2="65" y2="25" stroke="#a7c4f0" stroke-width="2" stroke-linecap="round" />
    <ellipse cx="65" cy="24" rx="7" ry="7" fill="url(#cp-accent)" />
    <circle cx="65" cy="24" r="3" fill="#1677ff" />
    <circle cx="63.8" cy="22.8" r="1" fill="#ffffff" fill-opacity="0.9" />

    <!-- 设备:玻璃模块三面 -->
    <polygon points="47,50 65,59 65,88 47,79" fill="url(#cp-glass-l)" />
    <polygon points="83,50 65,59 65,88 83,79" fill="url(#cp-glass-r)" />
    <polygon points="65,41 83,50 65,59 47,50" fill="url(#cp-glass-top)" />

    <!-- 左面顶部高光薄片 -->
    <polygon points="47,50 65,59 65,62.5 47,53.5" fill="#ffffff" fill-opacity="0.3" />

    <!-- 屏幕 + 顶棱反光 + 角落反光 -->
    <polygon points="68,61 80,55 80,71 68,77" fill="url(#cp-screen)" />
    <line x1="68" y1="61" x2="80" y2="55" stroke="#ffffff" stroke-width="1.4" stroke-opacity="0.6" />
    <polygon points="68,61 73,58.5 68,67.7" fill="#ffffff" fill-opacity="0.2" />

    <!-- 设备棱边高光 + 顶面光斑 -->
    <line x1="65" y1="59" x2="65" y2="88" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.5" stroke-linecap="round" />
    <polyline points="47,50 65,41 83,50" fill="none" stroke="#ffffff" stroke-width="1.5" stroke-opacity="0.8" stroke-linejoin="round" />
    <polygon points="65,43 71,46.5 65,50 59,46.5" fill="#ffffff" fill-opacity="0.5" />
  </svg>
</template>
