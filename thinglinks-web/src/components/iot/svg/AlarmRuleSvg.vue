<template>
  <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="alarm-rule-panel" x1="25" y1="22" x2="94" y2="91">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="1" stop-color="#edf7ff" />
      </linearGradient>
      <linearGradient id="alarm-rule-device" x1="27" y1="64" x2="61" y2="97">
        <stop offset="0" stop-color="#9ed8ff" />
        <stop offset="1" stop-color="#4f8ff0" />
      </linearGradient>
      <linearGradient id="alarm-rule-bell" x1="72" y1="24" x2="99" y2="57">
        <stop offset="0" stop-color="#ffd78a" />
        <stop offset="1" stop-color="#ff9f1a" />
      </linearGradient>
      <radialGradient id="alarm-rule-glow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#2dd4bf" stop-opacity="0.45" />
        <stop offset="1" stop-color="#2dd4bf" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="60" cy="104" rx="42" ry="8" fill="#8aa6c8" opacity="0.16" />
    <rect x="17" y="20" width="82" height="72" rx="14" fill="#e8f4ff" />
    <rect
      x="23"
      y="26"
      width="70"
      height="60"
      rx="12"
      fill="url(#alarm-rule-panel)"
      stroke="#9ed8ff"
      stroke-width="1.6"
    />

    <!-- Condition builder: left input node -> rule branch -> alert node. -->
    <circle cx="38" cy="49" r="8" fill="#dffaf5" stroke="#25b9aa" stroke-width="2" />
    <circle cx="38" cy="49" r="3.2" fill="#25b9aa" />
    <path d="M46 49h22" stroke="#4f8ff0" stroke-width="3" stroke-linecap="round" />
    <path d="M58 39v20" stroke="#b9c9db" stroke-width="2.4" stroke-linecap="round" />
    <circle cx="68" cy="39" r="5" fill="#eef4ff" stroke="#7aa8ff" stroke-width="1.7" />
    <circle cx="68" cy="59" r="5" fill="#fff7e3" stroke="#ffb020" stroke-width="1.7" />
    <path d="M73 39h11M73 59h11" stroke="#95a3b8" stroke-width="2.2" stroke-linecap="round" />

    <!-- Device signal cube, representing the monitored product/device source. -->
    <polygon points="28,74 43,82 43,98 28,90" fill="#2f80ed" opacity="0.92" />
    <polygon points="58,74 43,82 43,98 58,90" fill="url(#alarm-rule-device)" />
    <polygon points="43,66 58,74 43,82 28,74" fill="#b7e3ff" />
    <polyline
      points="28,74 43,66 58,74"
      stroke="#ffffff"
      stroke-width="1.4"
      stroke-opacity="0.75"
      stroke-linejoin="round"
    />
    <path
      d="M36 85h7M36 90h11"
      stroke="#ffffff"
      stroke-width="1.5"
      stroke-linecap="round"
      opacity="0.85"
    />
    <path
      d="M36 62q7-5 14 0M39 57q4-3 8 0"
      stroke="#5d87ff"
      stroke-width="1.8"
      stroke-linecap="round"
      opacity="0.55"
    />

    <!-- Alarm bell and status mark. -->
    <ellipse cx="86" cy="42" rx="21" ry="19" fill="url(#alarm-rule-glow)" />
    <path
      d="M79 47c0-8 4-13 10-13s10 5 10 13l3 7H76l3-7z"
      fill="url(#alarm-rule-bell)"
      stroke="#f59e0b"
      stroke-width="1.6"
      stroke-linejoin="round"
    />
    <path
      d="M85 34c0-2 1.6-3.5 4-3.5s4 1.5 4 3.5"
      stroke="#f59e0b"
      stroke-width="1.8"
      stroke-linecap="round"
    />
    <path
      d="M85 56c.8 2.3 2.2 3.6 4 3.6s3.2-1.3 4-3.6"
      stroke="#9a5b00"
      stroke-width="1.7"
      stroke-linecap="round"
    />
    <circle cx="99" cy="31" r="7" fill="#ff5f57" />
    <path d="M99 27v5M99 35h.1" stroke="#ffffff" stroke-width="2" stroke-linecap="round" />

    <!-- Rule footer chips. -->
    <rect
      x="58"
      y="72"
      width="25"
      height="7"
      rx="3.5"
      fill="#e8fff8"
      stroke="#8ee6d6"
      stroke-width="1.2"
    />
    <rect x="58" y="82" width="17" height="6" rx="3" fill="#eef4ff" />
    <circle cx="87" cy="76" r="3" fill="#25b9aa" />
    <circle cx="96" cy="76" r="3" fill="#ffb020" />
  </svg>
</template>
