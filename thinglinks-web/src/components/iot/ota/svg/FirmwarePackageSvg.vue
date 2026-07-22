<template>
  <!-- 固件包(FIRMWARE):等距 3D 芯片 ── 金属引脚 + 顶面电路走线/发光节点 + 青色晶核 + 玻璃高光 + 顶部升级徽标。
       适配 flexy 蓝色调,突出「固件刷写 / OTA 升级」场景。 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <radialGradient id="f2-halo" cx="0.5" cy="0.46" r="0.55">
        <stop offset="0" stop-color="#48d6f0" stop-opacity=".42" />
        <stop offset="55%" stop-color="#5b8cf0" stop-opacity=".18" />
        <stop offset="100%" stop-color="#5b8cf0" stop-opacity="0" />
      </radialGradient>
      <linearGradient id="f2-top" x1="0.1" y1="0" x2="0.9" y2="1">
        <stop offset="0" stop-color="#8fb6fb" />
        <stop offset="45%" stop-color="#5a82e8" />
        <stop offset="100%" stop-color="#3a5bc8" />
      </linearGradient>
      <linearGradient id="f2-l" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#33489f" />
        <stop offset="100%" stop-color="#202c66" />
      </linearGradient>
      <linearGradient id="f2-r" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#4662c4" />
        <stop offset="100%" stop-color="#2c3d86" />
      </linearGradient>
      <linearGradient id="f2-pin" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#ffeab0" />
        <stop offset="50%" stop-color="#f6c453" />
        <stop offset="100%" stop-color="#d99524" />
      </linearGradient>
      <linearGradient id="f2-gloss" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#ffffff" stop-opacity=".55" />
        <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
      </linearGradient>
      <radialGradient id="f2-die" cx="0.5" cy="0.4" r="0.62">
        <stop offset="0" stop-color="#eafcff" />
        <stop offset="45%" stop-color="#5ad7f5" />
        <stop offset="100%" stop-color="#2a8de0" />
      </radialGradient>
      <radialGradient id="f2-node" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#9af1ff" />
        <stop offset="100%" stop-color="#33b6ef" />
      </radialGradient>
      <radialGradient id="f2-bglow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#22cdf0" stop-opacity=".7" />
        <stop offset="100%" stop-color="#22cdf0" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="f2-sh" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#33407e" stop-opacity=".42" />
        <stop offset="100%" stop-color="#33407e" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="65" cy="63" rx="62" ry="58" fill="url(#f2-halo)" />
    <ellipse cx="65" cy="110" rx="38" ry="7.5" fill="url(#f2-sh)" />

    <g stroke="url(#f2-pin)" stroke-width="3.2" stroke-linecap="round">
      <line x1="41" y1="74" x2="33" y2="77.5" />
      <line x1="50" y1="79" x2="42" y2="82.5" />
      <line x1="59" y1="84" x2="51" y2="87.5" />
      <line x1="89" y1="74" x2="97" y2="77.5" />
      <line x1="80" y1="79" x2="88" y2="82.5" />
      <line x1="71" y1="84" x2="79" y2="87.5" />
      <line x1="41" y1="60" x2="34" y2="56.5" stroke-opacity=".55" />
      <line x1="89" y1="60" x2="96" y2="56.5" stroke-opacity=".55" />
    </g>

    <polygon points="27,62 65,84 65,96 27,74" fill="url(#f2-l)" />
    <polygon points="103,62 65,84 65,96 103,74" fill="url(#f2-r)" />
    <polygon points="65,40 103,62 65,84 27,62" fill="url(#f2-top)" />
    <polygon points="65,40 47,51 65,62 83,51" fill="url(#f2-gloss)" />

    <g stroke="#7fd6f2" stroke-width="1.1" stroke-opacity=".9" stroke-linecap="round">
      <line x1="65" y1="46" x2="65" y2="56" />
      <line x1="48" y1="50" x2="58" y2="56" />
      <line x1="82" y1="50" x2="72" y2="56" />
      <line x1="42" y1="60" x2="56" y2="60" />
      <line x1="88" y1="60" x2="74" y2="60" />
      <line x1="52" y1="68" x2="62" y2="68" />
      <line x1="78" y1="68" x2="68" y2="68" />
      <line x1="65" y1="78" x2="65" y2="70" />
    </g>
    <g fill="url(#f2-node)">
      <circle cx="48" cy="50" r="1.9" />
      <circle cx="82" cy="50" r="1.9" />
      <circle cx="42" cy="60" r="1.9" />
      <circle cx="88" cy="60" r="1.9" />
      <circle cx="65" cy="46" r="1.9" />
      <circle cx="52" cy="68" r="1.7" />
      <circle cx="78" cy="68" r="1.7" />
    </g>

    <ellipse cx="65" cy="62" rx="16" ry="10" fill="url(#f2-bglow)" />
    <polygon points="65,54 79,62 65,70 51,62" fill="url(#f2-die)" />
    <polygon points="65,56.5 74,62 65,67.5 56,62" fill="#082a4a" fill-opacity=".18" />
    <polygon points="65,56 71,59.4 65,62 59,59.4" fill="#fff" fill-opacity=".5" />

    <polyline
      points="27,62 65,40 103,62"
      fill="none"
      stroke="#cfe2ff"
      stroke-width="1.5"
      stroke-opacity=".85"
      stroke-linejoin="round"
    />
    <line x1="65" y1="84" x2="65" y2="96" stroke="#8fd6f2" stroke-width="1" stroke-opacity=".5" />

    <ellipse cx="65" cy="22" rx="13" ry="13" fill="url(#f2-bglow)" />
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
