<template>
  <!-- 网关产品(GATEWAY):等距 3D 玻璃质感 ── 分层底座 + 网关枢纽(大发光屏 + 双天线)+ 子设备节点,与普通产品成套 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!-- 下层平台顶面渐变(浅) -->
      <linearGradient id="gw-low-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#eef3fc" />
        <stop offset="100%" stop-color="#dae6f7" />
      </linearGradient>
      <!-- 上层底座顶面渐变(蓝) -->
      <linearGradient id="gw-up-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#7eadf7" />
        <stop offset="100%" stop-color="#5b91ec" />
      </linearGradient>
      <!-- 网关玻璃顶面 -->
      <linearGradient id="gw-glass-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#fbfdff" />
        <stop offset="100%" stop-color="#e6f0ff" />
      </linearGradient>
      <!-- 网关玻璃左面 -->
      <linearGradient id="gw-glass-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#dde9fd" />
        <stop offset="100%" stop-color="#bcd2f3" />
      </linearGradient>
      <!-- 网关玻璃右面 -->
      <linearGradient id="gw-glass-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#c6dbfa" />
        <stop offset="100%" stop-color="#9cbef0" />
      </linearGradient>
      <!-- 网关屏幕(发光,比普通产品更亮) -->
      <linearGradient id="gw-screen" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#8cc4ff" />
        <stop offset="100%" stop-color="#3f86f0" />
      </linearGradient>
      <!-- 子设备节点顶面 -->
      <linearGradient id="gw-node-top" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#f1f6fe" />
        <stop offset="100%" stop-color="#dde8f8" />
      </linearGradient>
      <!-- 接地柔影 -->
      <radialGradient id="gw-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#8ba2cf" stop-opacity="0.5" />
        <stop offset="100%" stop-color="#8ba2cf" stop-opacity="0" />
      </radialGradient>
      <!-- 天线节点光晕 -->
      <radialGradient id="gw-accent" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0%" stop-color="#1677ff" stop-opacity="0.55" />
        <stop offset="100%" stop-color="#1677ff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 接地柔影 -->
    <ellipse cx="65" cy="116" rx="42" ry="9" fill="url(#gw-shadow)" />

    <!-- 下层平台(瓷砖感) -->
    <polygon points="25,88 65,108 65,118 25,98" fill="#b6c9ec" />
    <polygon points="105,88 65,108 65,118 105,98" fill="#cedcf4" />
    <polygon points="65,68 105,88 65,108 25,88" fill="url(#gw-low-top)" />
    <line x1="85" y1="78" x2="45" y2="98" stroke="#a7b9dd" stroke-width="1" stroke-opacity="0.5" />
    <line x1="45" y1="78" x2="85" y2="98" stroke="#a7b9dd" stroke-width="1" stroke-opacity="0.5" />
    <polyline points="25,88 65,68 105,88" fill="none" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.5" stroke-linejoin="round" />

    <!-- 上层底座(蓝色台) -->
    <polygon points="39,79 65,92 65,101 39,88" fill="#3f74dd" />
    <polygon points="91,79 65,92 65,101 91,88" fill="#5285e6" />
    <polygon points="65,66 91,79 65,92 39,79" fill="url(#gw-up-top)" />
    <polyline points="39,79 65,66 91,79" fill="none" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.55" stroke-linejoin="round" />

    <!-- 子设备节点(左 / 右各一,带状态灯) -->
    <polygon points="44,89 50,92 50,100 44,97" fill="#bcd0f0" />
    <polygon points="56,89 50,92 50,100 56,97" fill="#cfdef5" />
    <polygon points="50,86 56,89 50,92 44,89" fill="url(#gw-node-top)" />
    <circle cx="53" cy="94.3" r="1.5" fill="#1677ff" />
    <polygon points="74,89 80,92 80,100 74,97" fill="#bcd0f0" />
    <polygon points="86,89 80,92 80,100 86,97" fill="#cfdef5" />
    <polygon points="80,86 86,89 80,92 74,89" fill="url(#gw-node-top)" />
    <circle cx="83" cy="94.3" r="1.5" fill="#1677ff" />

    <!-- 双天线 + 发光信号节点 -->
    <line x1="60" y1="48" x2="55" y2="37" stroke="#a7c4f0" stroke-width="2" stroke-linecap="round" />
    <ellipse cx="55" cy="36" rx="6" ry="6" fill="url(#gw-accent)" />
    <circle cx="55" cy="36" r="2.6" fill="#1677ff" />
    <line x1="70" y1="48" x2="75" y2="37" stroke="#a7c4f0" stroke-width="2" stroke-linecap="round" />
    <ellipse cx="75" cy="36" rx="6" ry="6" fill="url(#gw-accent)" />
    <circle cx="75" cy="36" r="2.6" fill="#1677ff" />

    <!-- 网关枢纽:玻璃模块三面 -->
    <polygon points="45,53 65,63 65,89 45,79" fill="url(#gw-glass-l)" />
    <polygon points="85,53 65,63 65,89 85,79" fill="url(#gw-glass-r)" />
    <polygon points="65,43 85,53 65,63 45,53" fill="url(#gw-glass-top)" />

    <!-- 左面顶部高光薄片 -->
    <polygon points="45,53 65,63 65,66.5 45,56.5" fill="#ffffff" fill-opacity="0.3" />

    <!-- 大屏 + 顶棱反光 + 角落反光 -->
    <polygon points="68,64 82,57 82,72 68,80" fill="url(#gw-screen)" />
    <line x1="68" y1="64" x2="82" y2="57" stroke="#ffffff" stroke-width="1.4" stroke-opacity="0.65" />
    <polygon points="68,64 73,61.5 68,71" fill="#ffffff" fill-opacity="0.22" />

    <!-- 网关棱边高光 + 顶面光斑 -->
    <line x1="65" y1="63" x2="65" y2="89" stroke="#ffffff" stroke-width="1.2" stroke-opacity="0.5" stroke-linecap="round" />
    <polyline points="45,53 65,43 85,53" fill="none" stroke="#ffffff" stroke-width="1.5" stroke-opacity="0.8" stroke-linejoin="round" />
    <polygon points="65,45 71,48.5 65,52 59,48.5" fill="#ffffff" fill-opacity="0.5" />
  </svg>
</template>
