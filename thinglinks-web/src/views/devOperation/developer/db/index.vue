<template>
  <div style="padding: 30px">
    <div v-for="(path, name) in services" :key="name" style="margin-bottom: 10px">
      {{ name }}:
      <a :href="buildUrl(path)" target="_blank"> 点击访问druid </a>
    </div>
  </div>
</template>

<script>
  import { buildDruidUrl } from './url.mjs';

  export default {
    data() {
      return {
        services: {
          'thinglinks-oauth-server': 'oauth',
          'thinglinks-generator-server': 'generator',
          'thinglinks-view-server': 'view',
          'thinglinks-system-server': 'system',
          'thinglinks-base-executor': 'base-executor',
          'thinglinks-mqs-server': 'mqs',
          'thinglinks-broker-server': 'broker',
          'thinglinks-iot-executor': 'iot-executor',
          'thinglinks-link-server': 'link',
          'thinglinks-tds-server': 'tds',
          'thinglinks-monitor-server': 'monitor',
          'thinglinks-rule-server': 'rule',
          'thinglinks-base-server': 'base',
          // ... 其他服务
        },
      };
    },
    methods: {
      buildUrl(path) {
        return buildDruidUrl(path, import.meta.env.VITE_PROXY, window.location.origin);
      },
    },
  };
</script>
