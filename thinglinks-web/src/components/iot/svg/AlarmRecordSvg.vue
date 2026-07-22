<template>
  <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="alarm-record-sheet" x1="29" y1="18" x2="91" y2="97">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="1" stop-color="#f3f7ff" />
      </linearGradient>
      <linearGradient id="alarm-record-timeline" x1="35" y1="38" x2="35" y2="86">
        <stop offset="0" stop-color="#5d87ff" />
        <stop offset="1" stop-color="#2dd4bf" />
      </linearGradient>
      <radialGradient id="alarm-record-glow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#5d87ff" stop-opacity="0.32" />
        <stop offset="1" stop-color="#5d87ff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="60" cy="105" rx="39" ry="8" fill="#8ea0c8" opacity="0.16" />
    <rect
      x="27"
      y="18"
      width="60"
      height="78"
      rx="12"
      fill="url(#alarm-record-sheet)"
      stroke="#a9c0ff"
      stroke-width="1.7"
    />
    <path d="M72 18v21h15" fill="#e8eeff" />
    <path d="M72 18v21h15" stroke="#a9c0ff" stroke-width="1.7" stroke-linejoin="round" />
    <path d="M41 31h20M41 38h16" stroke="#7f8da3" stroke-width="2.4" stroke-linecap="round" />

    <!-- Alarm timeline records. -->
    <path
      d="M36 50v35"
      stroke="url(#alarm-record-timeline)"
      stroke-width="3"
      stroke-linecap="round"
    />
    <circle cx="36" cy="52" r="5" fill="#5d87ff" stroke="#ffffff" stroke-width="2" />
    <circle cx="36" cy="68" r="5" fill="#ffb020" stroke="#ffffff" stroke-width="2" />
    <circle cx="36" cy="84" r="5" fill="#2dd4bf" stroke="#ffffff" stroke-width="2" />
    <rect x="47" y="47" width="27" height="8" rx="4" fill="#edf4ff" />
    <rect x="47" y="63" width="22" height="8" rx="4" fill="#fff7e6" />
    <rect x="47" y="79" width="30" height="8" rx="4" fill="#eafff9" />
    <path d="M79 51h4M74 67h9M81 83h2" stroke="#9aa9bd" stroke-width="2" stroke-linecap="round" />

    <!-- Detail magnifier and handled status. -->
    <ellipse cx="84" cy="80" rx="22" ry="21" fill="url(#alarm-record-glow)" />
    <circle cx="82" cy="78" r="15" fill="#ffffff" stroke="#2dd4bf" stroke-width="2.4" />
    <path d="M91 89l9 9" stroke="#2dd4bf" stroke-width="3" stroke-linecap="round" />
    <path
      d="M75 78l5 5 10-12"
      stroke="#2dd4bf"
      stroke-width="3.2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <circle cx="93" cy="58" r="8" fill="#ff5f57" />
    <path d="M93 54v5M93 62h.1" stroke="#ffffff" stroke-width="2" stroke-linecap="round" />
  </svg>
</template>
