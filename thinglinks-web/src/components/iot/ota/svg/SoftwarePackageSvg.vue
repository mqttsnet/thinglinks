<template>
  <!-- 软件包(SOFTWARE):等距 3D 分层包裹 ── 侧面分层(多版本)+ 正面两个代码窗(语法高亮)+ 顶面玻璃高光
       + 顶部升级徽标。适配 flexy 蓝色调,突出「软件 / 代码升级」场景。 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <radialGradient id="s2-halo" cx="0.5" cy="0.46" r="0.55">
        <stop offset="0" stop-color="#6fb6f5" stop-opacity=".4" />
        <stop offset="55%" stop-color="#5b8cf0" stop-opacity=".16" />
        <stop offset="100%" stop-color="#5b8cf0" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="s2-top" x1="0.1" y1="0" x2="0.9" y2="1">
        <stop offset="0" stop-color="#a6cdf6" />
        <stop offset="50%" stop-color="#6ea0ec" />
        <stop offset="100%" stop-color="#4f7fd8" />
      </linearGradient>
      <linearGradient id="s2-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#4d77cf" />
        <stop offset="100%" stop-color="#324f9e" />
      </linearGradient>
      <linearGradient id="s2-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#3a5ebb" />
        <stop offset="100%" stop-color="#243a80" />
      </linearGradient>
      <linearGradient id="s2-win" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#0e2452" />
        <stop offset="100%" stop-color="#16336e" />
      </linearGradient>
      <linearGradient id="s2-gloss" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#ffffff" stop-opacity=".5" />
        <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
      </linearGradient>
      <radialGradient id="s2-sh" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#33407e" stop-opacity=".42" />
        <stop offset="100%" stop-color="#33407e" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="65" cy="64" rx="60" ry="56" fill="url(#s2-halo)" />
    <ellipse cx="65" cy="112" rx="38" ry="7.5" fill="url(#s2-sh)" />

    <polygon points="33,86 33,58 30,59.5 30,87.5" fill="#2c4790" fill-opacity=".55" />
    <polygon points="27,55 62,38 97,55 62,72" fill="#2c4790" fill-opacity=".4" />
    <polygon points="30,58 65,76 65,103 30,85" fill="url(#s2-l)" />
    <polygon points="100,58 65,76 65,103 100,85" fill="url(#s2-r)" />
    <polygon points="65,40 100,58 65,76 30,58" fill="url(#s2-top)" />
    <polygon points="65,40 47,49 65,58 83,49" fill="url(#s2-gloss)" />
    <line
      x1="65"
      y1="76"
      x2="65"
      y2="103"
      stroke="#9fc4ff"
      stroke-width="1.1"
      stroke-opacity=".5"
    />
    <polyline
      points="30,58 65,40 100,58"
      fill="none"
      stroke="#cfe2ff"
      stroke-width="1.5"
      stroke-opacity=".85"
      stroke-linejoin="round"
    />

    <polygon
      points="38,84 60,95.5 60,99.5 38,88"
      fill="url(#s2-win)"
      stroke="#1a3a72"
      stroke-width="0.6"
    />
    <g
      stroke="#7fe0ff"
      stroke-width="1.7"
      stroke-linecap="round"
      stroke-linejoin="round"
      fill="none"
    >
      <polyline points="46.5,88.5 43.5,89.7 46.5,92.4" />
      <polyline points="53,90.6 56,93.5 53,94.5" />
    </g>
    <polygon
      points="70,87.5 92,76 92,80 70,91.5"
      fill="url(#s2-win)"
      stroke="#1a3a72"
      stroke-width="0.6"
    />
    <g stroke="#9af0c0" stroke-width="1.6" stroke-linecap="round" fill="none">
      <line x1="75" y1="84.5" x2="82" y2="81" />
      <line x1="78" y1="86" x2="87" y2="81.5" />
    </g>

    <circle cx="65" cy="49" r="2.4" fill="#fff" fill-opacity=".55" />

    <ellipse cx="65" cy="22" rx="13" ry="13" fill="url(#s2-halo)" />
    <circle cx="65" cy="22" r="11.5" fill="#2f6fe6" />
    <circle
      cx="65"
      cy="22"
      r="11.5"
      fill="none"
      stroke="#bfe0ff"
      stroke-width="1.3"
      stroke-opacity=".8"
    />
    <path d="M65 14.5 L73 24 H68.5 V31 H61.5 V24 H57 Z" fill="#fff" />
    <ellipse cx="61" cy="18.5" rx="3.4" ry="2.2" fill="#fff" fill-opacity=".4" />
  </svg>
</template>
