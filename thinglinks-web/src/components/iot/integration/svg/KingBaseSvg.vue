<template>
  <!-- 人大金仓 KingBase：深蓝 + 金黄"K + 仓"图腾，致敬官方"金仓"意象 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="kb-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#1A4480" />
        <stop offset="60%" stop-color="#143560" />
        <stop offset="100%" stop-color="#0B2243" />
      </linearGradient>
      <radialGradient id="kb-glow" cx="0.4" cy="0.3" r="0.7">
        <stop offset="0%" stop-color="#FFD580" stop-opacity="0.35" />
        <stop offset="100%" stop-color="#FFD580" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="kb-gold" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#FFD54A" />
        <stop offset="100%" stop-color="#E59A12" />
      </linearGradient>
    </defs>

    <!-- 背景 -->
    <rect x="8" y="8" width="114" height="114" rx="22" fill="url(#kb-bg)" />
    <ellipse cx="55" cy="38" rx="55" ry="32" fill="url(#kb-glow)" />

    <!-- 仓顶（屋脊三角，呼应"金仓" granary 意象） -->
    <path
      d="M 28 50
         L 65 28
         L 102 50
         L 96 50
         L 65 36
         L 34 50 Z"
      fill="url(#kb-gold)"
    />

    <!-- 仓体（金色，承载"金仓"色） -->
    <rect x="36" y="50" width="58" height="42" rx="2" fill="url(#kb-gold)" opacity="0.92" />

    <!-- 仓门（深蓝凹陷，营造立体） -->
    <rect x="42" y="62" width="14" height="30" rx="1" fill="#1A4480" />

    <!-- "K"字标（深蓝压印在仓体右侧，致敬 KingBase 的 K） -->
    <path
      d="M 64 60
         L 64 88
         L 70 88
         L 70 76
         L 76 88
         L 84 88
         L 75 73
         L 84 60
         L 76 60
         L 70 71
         L 70 60 Z"
      fill="#1A4480"
    />

    <!-- DB 圆柱（强调数据库属性，金色描边） -->
    <g stroke="url(#kb-gold)" stroke-width="1.8" fill="none" opacity="0.85">
      <ellipse cx="65" cy="100" rx="30" ry="5" />
      <line x1="35" y1="100" x2="35" y2="110" />
      <line x1="95" y1="100" x2="95" y2="110" />
      <ellipse cx="65" cy="110" rx="30" ry="5" />
    </g>
  </svg>
</template>
