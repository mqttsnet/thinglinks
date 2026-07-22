<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 240 160"
    preserveAspectRatio="xMidYMid meet"
    class="acl-rule-svg"
  >
    <defs>
      <linearGradient id="aclBg" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#1890ff" />
        <stop offset="100%" stop-color="#722ed1" />
      </linearGradient>
      <linearGradient id="aclShield" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#fff" stop-opacity="0.95" />
        <stop offset="100%" stop-color="#fff" stop-opacity="0.7" />
      </linearGradient>
    </defs>

    <!-- 背景 -->
    <rect width="240" height="160" fill="url(#aclBg)" />

    <!-- 装饰圆点 -->
    <circle cx="40" cy="30" r="3" fill="#fff" opacity="0.3" />
    <circle cx="60" cy="20" r="2" fill="#fff" opacity="0.25" />
    <circle cx="200" cy="40" r="4" fill="#fff" opacity="0.2" />
    <circle cx="190" cy="120" r="2.5" fill="#fff" opacity="0.3" />
    <circle cx="30" cy="130" r="3.5" fill="#fff" opacity="0.25" />

    <!-- 中央盾牌 -->
    <g transform="translate(120 82)">
      <path
        d="M 0 -42 L 30 -32 L 30 5 Q 30 25 0 38 Q -30 25 -30 5 L -30 -32 Z"
        fill="url(#aclShield)"
        stroke="#fff"
        stroke-width="1.5"
      />
      <!-- ACL 字样 -->
      <text
        x="0"
        y="-2"
        text-anchor="middle"
        font-family="Arial, sans-serif"
        font-size="14"
        font-weight="700"
        fill="#1890ff"
      >ACL</text>
      <!-- 锁标 -->
      <g transform="translate(0 14)" opacity="0.8">
        <rect x="-7" y="0" width="14" height="11" rx="2" fill="#1890ff" />
        <path d="M -4 0 V -3 Q -4 -7 0 -7 Q 4 -7 4 -3 V 0" fill="none" stroke="#1890ff" stroke-width="1.6" />
      </g>
    </g>

    <!-- 左侧规则虚线 -->
    <g stroke="#fff" stroke-width="1.2" opacity="0.5" stroke-dasharray="4 3" fill="none">
      <path d="M 0 78 L 60 78" />
      <path d="M 0 95 L 50 95" />
    </g>
    <!-- 右侧规则虚线 -->
    <g stroke="#fff" stroke-width="1.2" opacity="0.5" stroke-dasharray="4 3" fill="none">
      <path d="M 180 78 L 240 78" />
      <path d="M 190 95 L 240 95" />
    </g>
  </svg>
</template>

<script lang="ts" setup>
  defineOptions({ name: 'DeviceAclRuleSvg' });
</script>

<style lang="less" scoped>
  .acl-rule-svg {
    width: 100%;
    height: 100%;
    display: block;
  }
</style>
