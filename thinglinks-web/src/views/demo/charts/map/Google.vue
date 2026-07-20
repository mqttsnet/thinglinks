<template>
  <div v-if="errorMessage" class="google-map-fallback" :style="{ height, width }">
    {{ errorMessage }}
  </div>
  <div v-else ref="wrapRef" :style="{ height, width }"></div>
</template>
<script lang="ts">
  import { defineComponent, ref, nextTick, unref, onMounted } from 'vue';

  import { useI18n } from '/@/hooks/web/useI18n';
  import { useScript } from '/@/hooks/web/useScript';

  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY?.trim();
  const MAP_URL = GOOGLE_MAPS_API_KEY
    ? `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(
        GOOGLE_MAPS_API_KEY,
      )}&signed_in=true`
    : '';

  export default defineComponent({
    name: 'GoogleMap',
    props: {
      width: {
        type: String,
        default: '100%',
      },
      height: {
        type: String,
        default: 'calc(100vh - 78px)',
      },
    },
    setup() {
      const { t } = useI18n();
      const wrapRef = ref<HTMLDivElement | null>(null);
      const errorMessage = ref('');
      const { toPromise } = useScript({ src: MAP_URL });

      async function initMap() {
        if (!GOOGLE_MAPS_API_KEY) {
          errorMessage.value = t('demo.charts.googleMap.missingApiKey');
          return;
        }

        try {
          await toPromise();
          await nextTick();
          const wrapEl = unref(wrapRef);
          const google = (window as any).google;
          if (!wrapEl || !google?.maps) {
            errorMessage.value = t('demo.charts.googleMap.loadFailed');
            return;
          }
          const latLng = { lat: 39.915, lng: 116.404 };
          const map = new google.maps.Map(wrapEl, {
            zoom: 4,
            center: latLng,
          });
          new google.maps.Marker({
            position: latLng,
            map,
            title: 'Beijing',
          });
        } catch {
          errorMessage.value = t('demo.charts.googleMap.loadFailed');
        }
      }

      onMounted(() => {
        void initMap();
      });

      return { errorMessage, wrapRef };
    },
  });
</script>

<style lang="less" scoped>
  .google-map-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    color: @text-color-secondary;
    background: @component-background;
  }
</style>
