<template>
  <component :is="iconMap[code]" v-if="iconMap[code]" />
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import type { Component } from 'vue';
  import type { WeatherCode } from '../../hooks/useWeather';
  import SunnyIcon from './icons/SunnyIcon.vue';
  import CloudyIcon from './icons/CloudyIcon.vue';
  import OvercastIcon from './icons/OvercastIcon.vue';
  import RainIcon from './icons/RainIcon.vue';
  import SnowIcon from './icons/SnowIcon.vue';
  import ThunderIcon from './icons/ThunderIcon.vue';
  import FogIcon from './icons/FogIcon.vue';

  interface Props {
    code: WeatherCode;
  }
  const props = defineProps<Props>();

  const iconMap: Record<WeatherCode, Component> = {
    sunny: SunnyIcon,
    cloudy: CloudyIcon,
    overcast: OvercastIcon,
    rain: RainIcon,
    snow: SnowIcon,
    thunder: ThunderIcon,
    fog: FogIcon,
  };

  // 显式引用以让 linter 不抱怨
  const _ = computed(() => props.code);
</script>
