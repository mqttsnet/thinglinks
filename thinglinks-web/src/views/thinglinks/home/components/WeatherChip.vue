<template>
  <!-- 加载中：骨架屏 -->
  <div
    v-if="loading"
    class="weather-chip weather-chip--loading"
  >
    <div class="weather-chip__skeleton-icon" />
    <div class="weather-chip__skeleton-text" />
  </div>
  <!-- 成功：天气 chip。失败 / 未配置均不渲染（console.warn 有提示） -->
  <div
    v-else-if="!error && city && temp"
    :class="['weather-chip', `weather-chip--${code}`, { 'is-refreshing': refreshing }]"
    @click="onClick"
  >
    <div class="weather-chip__icon">
      <WeatherIcon :code="code" />
    </div>
    <div class="weather-chip__text">
      <span class="weather-chip__city">{{ city }}</span>
      <span class="weather-chip__temp">{{ temp }}°</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useWeather } from '../hooks/useWeather';
  import WeatherIcon from './weather/WeatherIcon.vue';

  const { loading, error, city, temp, code, refresh } = useWeather();
  const refreshing = ref(false);

  async function onClick() {
    if (refreshing.value) return;
    refreshing.value = true;
    await refresh();
    setTimeout(() => (refreshing.value = false), 700);
  }
</script>

<style lang="less" scoped>
  .weather-chip {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px 6px 8px;
    border-radius: 999px;
    background: linear-gradient(135deg, #ffe7a8 0%, #ffbd6b 100%);
    box-shadow: 0 4px 12px rgba(255, 174, 31, 0.22);
    color: #5b3a0b;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    user-select: none;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(255, 174, 31, 0.3);
    }
    &:active {
      transform: scale(0.96);
    }
    &.is-refreshing .weather-chip__icon {
      animation: chip-spin 0.7s ease;
    }
  }

  @keyframes chip-spin {
    0% { transform: rotate(0); }
    100% { transform: rotate(360deg); }
  }

  .weather-chip--cloudy {
    background: linear-gradient(135deg, #fde9a9 0%, #b3cdfa 100%);
    color: #2d3e5c;
    box-shadow: 0 4px 12px rgba(93, 135, 255, 0.22);
  }
  .weather-chip--overcast {
    background: linear-gradient(135deg, #d5dde8 0%, #b0bac9 100%);
    color: #2a3547;
    box-shadow: 0 4px 12px rgba(120, 140, 170, 0.25);
  }
  .weather-chip--rain {
    background: linear-gradient(135deg, #8fb8f5 0%, #577ec2 100%);
    color: #fff;
    box-shadow: 0 4px 14px rgba(87, 126, 194, 0.35);
  }
  .weather-chip--snow {
    background: linear-gradient(135deg, #e8f3ff 0%, #b9d4f0 100%);
    color: #2a4370;
    box-shadow: 0 4px 14px rgba(180, 208, 235, 0.45);
  }
  .weather-chip--thunder {
    background: linear-gradient(135deg, #5b4a7e 0%, #322551 100%);
    color: #fff;
    box-shadow: 0 4px 14px rgba(51, 37, 81, 0.4);
  }
  .weather-chip--fog {
    background: linear-gradient(135deg, #e0e4ec 0%, #b6bcc6 100%);
    color: #2a3547;
    box-shadow: 0 4px 12px rgba(120, 128, 145, 0.25);
  }

  .weather-chip__icon {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
  }

  .weather-chip__text {
    display: flex;
    flex-direction: column;
    line-height: 1.1;
  }
  .weather-chip__city {
    font-size: 10px;
    opacity: 0.85;
    letter-spacing: 0.4px;
  }
  .weather-chip__temp {
    font-size: 14px;
    font-weight: 700;
  }

  /* === Loading skeleton === */
  .weather-chip--loading {
    background: linear-gradient(135deg, #eef1f7 0%, #dde3ed 100%);
    color: #8c97a5;
    cursor: default;
    box-shadow: none;
    &:hover { transform: none; box-shadow: none; }
    &:active { transform: none; }
  }
  .weather-chip__skeleton-icon,
  .weather-chip__skeleton-text {
    background: linear-gradient(90deg, #e2e6ef 0%, #eef1f7 50%, #e2e6ef 100%);
    background-size: 200% 100%;
    border-radius: 6px;
    animation: skeleton-shimmer 1.4s ease-in-out infinite;
  }
  .weather-chip__skeleton-icon {
    width: 22px;
    height: 22px;
    border-radius: 50%;
  }
  .weather-chip__skeleton-text {
    width: 56px;
    height: 14px;
  }
  @keyframes skeleton-shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
  }

</style>
