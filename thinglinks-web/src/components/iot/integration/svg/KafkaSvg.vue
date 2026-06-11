<template>
  <!-- Apache Kafka：消息队列 + 流式管道 -->
  <svg viewBox="0 0 130 130" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="kafka-bg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#231F20" />
        <stop offset="100%" stop-color="#3a3a3a" />
      </linearGradient>
    </defs>
    <!-- 背景圆角 -->
    <rect x="10" y="10" width="110" height="110" rx="14" fill="url(#kafka-bg)" />
    <!-- 顶部节点（生产者） -->
    <circle cx="40" cy="35" r="6" fill="#fff" />
    <circle cx="65" cy="30" r="6" fill="#fff" />
    <circle cx="90" cy="35" r="6" fill="#fff" />
    <!-- 中央 broker 节点 -->
    <circle cx="65" cy="65" r="14" fill="#fff" />
    <circle cx="65" cy="65" r="6" fill="#231F20" />
    <!-- 底部节点（消费者） -->
    <circle cx="40" cy="95" r="6" fill="#fff" />
    <circle cx="65" cy="100" r="6" fill="#fff" />
    <circle cx="90" cy="95" r="6" fill="#fff" />
    <!-- 连接线 producers → broker -->
    <line x1="40" y1="35" x2="55" y2="60" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="65" y1="30" x2="65" y2="50" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="90" y1="35" x2="75" y2="60" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <!-- 连接线 broker → consumers -->
    <line x1="55" y1="70" x2="40" y2="95" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="65" y1="80" x2="65" y2="100" stroke="#fff" stroke-width="1.5" opacity="0.6" />
    <line x1="75" y1="70" x2="90" y2="95" stroke="#fff" stroke-width="1.5" opacity="0.6" />
  </svg>
</template>
