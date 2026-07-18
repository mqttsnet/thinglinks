<template>
  <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="alarm-channel-card" x1="23" y1="27" x2="86" y2="92">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="1" stop-color="#effbf8" />
      </linearGradient>
      <linearGradient id="alarm-channel-horn" x1="69" y1="38" x2="103" y2="70">
        <stop offset="0" stop-color="#83a8ff" />
        <stop offset="1" stop-color="#4f77ec" />
      </linearGradient>
      <radialGradient id="alarm-channel-shadow" cx="0.5" cy="0.5" r="0.5">
        <stop offset="0" stop-color="#8bb6b2" stop-opacity="0.35" />
        <stop offset="1" stop-color="#8bb6b2" stop-opacity="0" />
      </radialGradient>
    </defs>

    <ellipse cx="60" cy="103" rx="42" ry="8" fill="url(#alarm-channel-shadow)" />
    <rect x="18" y="27" width="73" height="58" rx="14" fill="#e9fbf7" />
    <rect
      x="25"
      y="34"
      width="56"
      height="42"
      rx="11"
      fill="url(#alarm-channel-card)"
      stroke="#87ded3"
      stroke-width="1.7"
    />

    <!-- Message envelope. -->
    <rect
      x="35"
      y="43"
      width="33"
      height="22"
      rx="6"
      fill="#ffffff"
      stroke="#35c2b2"
      stroke-width="1.8"
    />
    <path
      d="M37 48l14.5 10L66 48"
      stroke="#35c2b2"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <path d="M40 68h23" stroke="#b9deda" stroke-width="2.4" stroke-linecap="round" />

    <!-- Broadcast channel cone. -->
    <path d="M76 53l18-11v30L76 61v-8z" fill="url(#alarm-channel-horn)" />
    <rect x="69" y="50" width="9" height="14" rx="3" fill="#2dd4bf" />
    <path
      d="M95 47q7 8 0 18M100 41q13 15 0 30"
      stroke="#8fb1ff"
      stroke-width="2.2"
      stroke-linecap="round"
      opacity="0.85"
    />
    <path d="M73 63l7 15" stroke="#3b5edb" stroke-width="3" stroke-linecap="round" />

    <!-- Channel endpoints: webhook / app / user group. -->
    <circle cx="32" cy="88" r="7" fill="#ecfff9" stroke="#25b9aa" stroke-width="1.7" />
    <path d="M29 88h6M32 85v6" stroke="#25b9aa" stroke-width="1.6" stroke-linecap="round" />
    <circle cx="56" cy="91" r="7" fill="#eef4ff" stroke="#5d87ff" stroke-width="1.7" />
    <path d="M53 91h6M56 88v6" stroke="#5d87ff" stroke-width="1.6" stroke-linecap="round" />
    <circle cx="81" cy="86" r="7" fill="#fff7e6" stroke="#ffb020" stroke-width="1.7" />
    <path d="M78 86h6M81 83v6" stroke="#ffb020" stroke-width="1.6" stroke-linecap="round" />
    <path
      d="M38 86c8-7 18-7 28 0"
      stroke="#b9deda"
      stroke-width="1.7"
      stroke-dasharray="3 3"
      stroke-linecap="round"
    />
    <path
      d="M63 88c6-5 11-6 18-3"
      stroke="#b9deda"
      stroke-width="1.7"
      stroke-dasharray="3 3"
      stroke-linecap="round"
    />

    <!-- Delivery status ticks. -->
    <circle cx="78" cy="35" r="6" fill="#2dd4bf" />
    <path
      d="M75 35l2 2 4-5"
      stroke="#ffffff"
      stroke-width="1.8"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <circle cx="88" cy="30" r="3" fill="#ffb020" />
  </svg>
</template>
