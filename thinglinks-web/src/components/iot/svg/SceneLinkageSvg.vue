<template>
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="scene-linkage-platform" x1="20" y1="63" x2="110" y2="110">
        <stop offset="0" stop-color="#f4f2ff" />
        <stop offset="1" stop-color="#dfe7ff" />
      </linearGradient>
      <linearGradient id="scene-linkage-device" x1="26" y1="67" x2="53" y2="96">
        <stop offset="0" stop-color="#eef7ff" />
        <stop offset="1" stop-color="#83b5ff" />
      </linearGradient>
      <linearGradient id="scene-linkage-hub" x1="49" y1="29" x2="82" y2="72">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="1" stop-color="#d9ceff" />
      </linearGradient>
      <linearGradient id="scene-linkage-action" x1="75" y1="55" x2="102" y2="92">
        <stop offset="0" stop-color="#c9fff3" />
        <stop offset="1" stop-color="#2dd4bf" />
      </linearGradient>
      <radialGradient id="scene-linkage-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#7f70c8" stop-opacity="0.35" />
        <stop offset="1" stop-color="#7f70c8" stop-opacity="0" />
      </radialGradient>
      <radialGradient id="scene-linkage-pulse" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#7c5cff" stop-opacity="0.55" />
        <stop offset="1" stop-color="#7c5cff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="65" cy="115" rx="48" ry="9" fill="url(#scene-linkage-shadow)" />

    <!-- Workbench platform. -->
    <polygon points="18,86 65,110 65,120 18,96" fill="#bdc9f0" />
    <polygon points="112,86 65,110 65,120 112,96" fill="#d4dcfa" />
    <polygon points="65,62 112,86 65,110 18,86" fill="url(#scene-linkage-platform)" />
    <path
      d="M42 74l47 24M88 74L41 98"
      stroke="#aab5df"
      stroke-width="1"
      stroke-dasharray="3 4"
      opacity="0.55"
    />
    <polyline
      points="18,86 65,62 112,86"
      stroke="#ffffff"
      stroke-width="1.3"
      stroke-opacity="0.75"
      stroke-linejoin="round"
    />

    <!-- Trigger device. -->
    <polygon points="24,73 39,81 39,98 24,90" fill="#4f8ff0" opacity="0.95" />
    <polygon points="54,73 39,81 39,98 54,90" fill="url(#scene-linkage-device)" />
    <polygon points="39,65 54,73 39,81 24,73" fill="#dcedff" />
    <path
      d="M31 86h8M31 91h12"
      stroke="#ffffff"
      stroke-width="1.5"
      stroke-linecap="round"
      opacity="0.8"
    />
    <path
      d="M31 61q8-5 16 0M35 56q4-3 8 0"
      stroke="#5d87ff"
      stroke-width="1.7"
      stroke-linecap="round"
      opacity="0.6"
    />
    <circle cx="29" cy="70" r="4" fill="#2dd4bf" stroke="#ffffff" stroke-width="1.5" />

    <!-- Rule hub. -->
    <polygon points="48,40 65,49 65,75 48,66" fill="#c8bcf3" />
    <polygon points="82,40 65,49 65,75 82,66" fill="#aa95ec" />
    <polygon points="65,31 82,40 65,49 48,40" fill="url(#scene-linkage-hub)" />
    <polyline
      points="48,40 65,31 82,40"
      stroke="#ffffff"
      stroke-width="1.5"
      stroke-opacity="0.82"
      stroke-linejoin="round"
    />
    <line
      x1="65"
      y1="49"
      x2="65"
      y2="75"
      stroke="#ffffff"
      stroke-width="1.1"
      stroke-opacity="0.48"
    />
    <polygon points="69,51 78,46 78,62 69,67" fill="#7658e8" />
    <polygon points="75,49 71.5,55.5 74,56 71.8,62.5 77,54 74.5,53.6" fill="#ffffff" />

    <!-- Condition chips orbiting the hub. -->
    <rect
      x="33"
      y="34"
      width="20"
      height="10"
      rx="5"
      fill="#edf7ff"
      stroke="#9ed8ff"
      stroke-width="1.2"
    />
    <circle cx="40" cy="39" r="2" fill="#5d87ff" />
    <path d="M45 39h4" stroke="#8ca0bd" stroke-width="1.5" stroke-linecap="round" />
    <rect
      x="77"
      y="26"
      width="21"
      height="10"
      rx="5"
      fill="#fff7e6"
      stroke="#ffcf73"
      stroke-width="1.2"
    />
    <path d="M84 31h7" stroke="#ffb020" stroke-width="1.7" stroke-linecap="round" />

    <!-- Action device. -->
    <polygon points="78,73 93,81 93,98 78,90" fill="#1ba99b" />
    <polygon points="108,73 93,81 93,98 108,90" fill="url(#scene-linkage-action)" />
    <polygon points="93,65 108,73 93,81 78,73" fill="#c9fff3" />
    <path
      d="M85 86h9M85 91h13"
      stroke="#ffffff"
      stroke-width="1.5"
      stroke-linecap="round"
      opacity="0.85"
    />
    <circle cx="104" cy="70" r="4" fill="#ffb020" stroke="#ffffff" stroke-width="1.5" />

    <!-- Trigger -> rule -> action path. -->
    <path
      d="M45 66Q50 56 56 51"
      stroke="#7c5cff"
      stroke-width="2"
      stroke-dasharray="4 4"
      stroke-linecap="round"
    />
    <path
      d="M74 52Q82 59 88 68"
      stroke="#7c5cff"
      stroke-width="2"
      stroke-dasharray="4 4"
      stroke-linecap="round"
    />
    <circle cx="51" cy="58" r="2.4" fill="#7c5cff" />
    <circle cx="82" cy="60" r="2.4" fill="#7c5cff" />
    <ellipse cx="65" cy="24" rx="10" ry="8" fill="url(#scene-linkage-pulse)" />
    <circle cx="65" cy="24" r="4" fill="#7c5cff" />
    <path d="M65 28v8" stroke="#b9adf4" stroke-width="2" stroke-linecap="round" />

    <!-- Completion mark for the generated action. -->
    <circle cx="101" cy="44" r="9" fill="#2dd4bf" />
    <path
      d="M97 44l3 3 6-7"
      stroke="#ffffff"
      stroke-width="2.2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>
