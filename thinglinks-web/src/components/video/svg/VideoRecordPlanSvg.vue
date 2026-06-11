<template>
  <!-- 录像计划：时钟 + 录像胶卷 + 日历 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 时钟 -->
    <circle cx="42" cy="42" r="24" fill="#f5f0ff" stroke="#7c5cfc" stroke-width="1.5" />
    <circle cx="42" cy="42" r="2" fill="#7c5cfc" />
    <line x1="42" y1="42" x2="42" y2="28" stroke="#7c5cfc" stroke-width="2" stroke-linecap="round" />
    <line x1="42" y1="42" x2="52" y2="46" stroke="#7c5cfc" stroke-width="2" stroke-linecap="round" />
    <!-- 时钟刻度 -->
    <line x1="42" y1="20" x2="42" y2="23" stroke="#c5b3f5" stroke-width="1.5" />
    <line x1="42" y1="61" x2="42" y2="64" stroke="#c5b3f5" stroke-width="1.5" />
    <line x1="20" y1="42" x2="23" y2="42" stroke="#c5b3f5" stroke-width="1.5" />
    <line x1="61" y1="42" x2="64" y2="42" stroke="#c5b3f5" stroke-width="1.5" />
    <!-- 录像图标(REC) -->
    <rect x="72" y="22" width="44" height="30" rx="6" fill="#fff0f0" stroke="#fa896b" stroke-width="1.5" />
    <circle cx="84" cy="37" r="4" fill="#fa896b" />
    <rect x="92" y="33" width="18" height="3" rx="1" fill="#fa896b" opacity="0.6" />
    <rect x="92" y="39" width="12" height="3" rx="1" fill="#fa896b" opacity="0.4" />
    <text x="84" y="30" text-anchor="middle" fill="#fa896b" font-size="7" font-weight="700" font-family="sans-serif">REC</text>
    <!-- 日历 -->
    <rect x="18" y="78" width="44" height="36" rx="5" fill="#e8f0fe" stroke="#5d87ff" stroke-width="1" />
    <rect x="18" y="78" width="44" height="12" rx="5" fill="#5d87ff" />
    <text x="40" y="88" text-anchor="middle" fill="#fff" font-size="8" font-weight="600" font-family="sans-serif">PLAN</text>
    <rect x="24" y="96" width="6" height="5" rx="1" fill="#d4e7fe" />
    <rect x="33" y="96" width="6" height="5" rx="1" fill="#5d87ff" opacity="0.7" />
    <rect x="42" y="96" width="6" height="5" rx="1" fill="#d4e7fe" />
    <rect x="51" y="96" width="6" height="5" rx="1" fill="#d4e7fe" />
    <rect x="24" y="104" width="6" height="5" rx="1" fill="#d4e7fe" />
    <rect x="33" y="104" width="6" height="5" rx="1" fill="#d4e7fe" />
    <rect x="42" y="104" width="6" height="5" rx="1" fill="#5d87ff" opacity="0.7" />
    <rect x="51" y="104" width="6" height="5" rx="1" fill="#d4e7fe" />
    <!-- 存储 -->
    <rect x="72" y="68" width="44" height="46" rx="5" fill="#e6faf4" stroke="#13deb9" stroke-width="1" />
    <path d="M84 80 L84 104 L106 104 L106 86 L100 80 Z" fill="#c3f5e5" stroke="#13deb9" stroke-width="1" />
    <polyline points="100,80 100,86 106,86" fill="none" stroke="#13deb9" stroke-width="1" />
    <rect x="88" y="90" width="14" height="2" rx="1" fill="#13deb9" opacity="0.6" />
    <rect x="88" y="95" width="10" height="2" rx="1" fill="#13deb9" opacity="0.4" />
  </svg>
</template>
