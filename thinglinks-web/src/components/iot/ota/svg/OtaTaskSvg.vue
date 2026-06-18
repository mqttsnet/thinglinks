<template>
  <!-- OTA 升级任务:云端下推升级 ── 立体云朵 + 环形进度 + 下推箭头 + 信号点 + 下方接收芯片(进度条)。
       适配 flexy 蓝色调,突出「云端把升级任务下发到设备」场景。 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <radialGradient id="t2-halo" cx="0.5" cy="0.42" r="0.56">
        <stop offset="0" stop-color="#48d6f0" stop-opacity=".4" />
        <stop offset="55%" stop-color="#5b8cf0" stop-opacity=".16" />
        <stop offset="100%" stop-color="#5b8cf0" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="t2-cloud" x1="0" y1="0" x2="0.3" y2="1">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="55%" stop-color="#dbeaff" />
        <stop offset="100%" stop-color="#b3d0f7" />
      </linearGradient>
      <linearGradient id="t2-arrow" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#6fa0f2" />
        <stop offset="100%" stop-color="#2f6fe6" />
      </linearGradient>
      <linearGradient id="t2-chip" x1="0.1" y1="0" x2="0.9" y2="1">
        <stop offset="0" stop-color="#8fb6fb" />
        <stop offset="100%" stop-color="#3a5bc8" />
      </linearGradient>
      <radialGradient id="t2-bglow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#22cdf0" stop-opacity=".55" />
        <stop offset="100%" stop-color="#22cdf0" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="t2-sh" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#33407e" stop-opacity=".4" />
        <stop offset="100%" stop-color="#33407e" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="65" cy="60" rx="60" ry="56" fill="url(#t2-halo)" />
    <ellipse cx="65" cy="113" rx="34" ry="6.5" fill="url(#t2-sh)" />

    <path
      d="M39 50 a15 15 0 0 1 29 -5 a12 12 0 0 1 18 6.5 a13 13 0 0 1 -2.5 25.5 H43 a14 14 0 0 1 -4 -27 Z"
      fill="url(#t2-cloud)"
      stroke="#9fbef0"
      stroke-width="1"
    />
    <path
      d="M50 41 a11 11 0 0 1 20.5 -2.5"
      fill="none"
      stroke="#fff"
      stroke-width="2"
      stroke-opacity=".9"
      stroke-linecap="round"
    />
    <ellipse cx="55" cy="46" rx="6" ry="3" fill="#fff" fill-opacity=".7" />

    <path
      d="M65 60 a13 13 0 1 1 -9.2 3.8"
      fill="none"
      stroke="#cfe0f7"
      stroke-width="2.4"
      stroke-linecap="round"
    />
    <path
      d="M65 60 a13 13 0 0 1 11.5 7"
      fill="none"
      stroke="#36c6ee"
      stroke-width="2.6"
      stroke-linecap="round"
    />

    <line
      x1="65"
      y1="58"
      x2="65"
      y2="79"
      stroke="url(#t2-arrow)"
      stroke-width="6.5"
      stroke-linecap="round"
    />
    <path
      d="M55.5 71 L65 82 L74.5 71"
      fill="none"
      stroke="url(#t2-arrow)"
      stroke-width="6.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    />

    <g fill="#7fd6f2">
      <circle cx="46" cy="64" r="2" />
      <circle cx="84" cy="64" r="2" />
    </g>
    <g fill="#9fc4ff">
      <circle cx="46" cy="73" r="1.7" fill-opacity=".55" />
      <circle cx="84" cy="73" r="1.7" fill-opacity=".55" />
    </g>

    <ellipse cx="65" cy="100" rx="16" ry="8" fill="url(#t2-bglow)" />
    <polygon points="48,100 65,91 82,100 65,109" fill="url(#t2-chip)" />
    <polygon
      points="48,100 65,91 82,100 65,109"
      fill="none"
      stroke="#cfe2ff"
      stroke-opacity=".6"
      stroke-width="1"
    />
    <rect x="59.5" y="97.5" width="11" height="5" rx="1.4" fill="#0e2452" fill-opacity=".6" />
    <rect x="60.5" y="98.7" width="6" height="2.6" rx="1" fill="#5ad7f5" />
    <g stroke="#a9c7f2" stroke-width="2" stroke-linecap="round">
      <line x1="40" y1="100" x2="44" y2="100" />
      <line x1="86" y1="100" x2="90" y2="100" />
    </g>
  </svg>
</template>
