<template>
  <video
    ref="videoRef"
    :style="containerStyle"
    class="flv-player"
    :muted="muted"
    :controls="true"
    @loadedmetadata="onLoadedMetadata"
  ></video>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';

  // 缓存脚本加载 Promise，防止重复加载
  let flvLoadPromise: Promise<any> | null = null;

  export default defineComponent({
    name: 'FlvPlayer',
    props: {
      url: { type: String, default: '' },
      autoplay: { type: Boolean, default: true },
      muted: { type: Boolean, default: false },
      width: { type: [String, Number], default: '100%' },
      height: { type: [String, Number], default: 400 },
      // 自适应模式：容器宽高比跟随视频实际比例（detail 用），关闭保持固定 height（分屏用）
      autoAspect: { type: Boolean, default: false },
      // 填充模式（仅 autoAspect=false 时生效）：见 JessibucaPlayer.fillMode 同名说明
      fillMode: { type: String as () => 'contain' | 'cover' | 'fill', default: 'contain' },
    },
    emits: ['ready', 'play', 'pause', 'error', 'close'],
    setup(props, { emit, expose }) {
      const videoRef = ref<HTMLVideoElement>();
      let flvPlayer: any = null;
      let destroyed = false;

      // 视频实际宽高比，loadedmetadata 后回填
      const aspectRatio = ref<string>('16 / 9');

      const containerStyle = computed(() => {
        const widthStyle = typeof props.width === 'number' ? `${props.width}px` : props.width;
        const heightStyle = typeof props.height === 'number' ? `${props.height}px` : props.height;
        // fill 在 video 元素上即 fill；其他直传 object-fit。
        // fill='cover' 是分屏场景的关键：容器固定 100%/100% 也能无黑边。
        const objectFit = props.fillMode;
        if (props.autoAspect) {
          return {
            width: widthStyle,
            aspectRatio: aspectRatio.value,
            objectFit,
            ...(heightStyle && heightStyle !== 'auto' ? { minHeight: heightStyle } : {}),
          };
        }
        return {
          width: widthStyle,
          height: heightStyle,
          objectFit,
        };
      });

      function onLoadedMetadata() {
        if (videoRef.value && videoRef.value.videoWidth > 0 && videoRef.value.videoHeight > 0) {
          aspectRatio.value = `${videoRef.value.videoWidth} / ${videoRef.value.videoHeight}`;
        }
      }

      function loadFlvJs(): Promise<any> {
        if ((window as any).flvjs) {
          return Promise.resolve((window as any).flvjs);
        }
        if (flvLoadPromise) {
          return flvLoadPromise;
        }
        flvLoadPromise = new Promise((resolve, reject) => {
          const script = document.createElement('script');
          script.src = '/resource/flv.min.js';
          script.onload = () => {
            if ((window as any).flvjs) {
              resolve((window as any).flvjs);
            } else {
              flvLoadPromise = null;
              reject(new Error('flv.js not found'));
            }
          };
          script.onerror = () => {
            flvLoadPromise = null;
            reject(new Error('Failed to load flv.js'));
          };
          document.head.appendChild(script);
        });
        return flvLoadPromise;
      }

      async function initPlayer() {
        if (!videoRef.value || destroyed) return;
        try {
          const flvjs = await loadFlvJs();
          if (destroyed) return;
          if (!flvjs.isSupported()) {
            emit('error', new Error('flv.js is not supported in this browser'));
            return;
          }
          emit('ready');
          if (props.url && props.autoplay) {
            play(props.url);
          }
        } catch (err) {
          emit('error', err);
        }
      }

      async function play(url?: string) {
        const playUrl = url || props.url;
        if (!playUrl || !videoRef.value || destroyed) return;
        destroyFlvPlayer();
        try {
          const flvjs = await loadFlvJs();
          if (destroyed) return;
          flvPlayer = flvjs.createPlayer({
            type: 'flv',
            url: playUrl,
            isLive: true,
          });
          flvPlayer.attachMediaElement(videoRef.value);
          flvPlayer.load();
          // video.play() 返回 Promise，需 catch 处理浏览器自动播放策略拒绝
          videoRef.value.play().catch((e: any) => {
            console.warn('FlvPlayer autoplay rejected:', e);
          });
          flvPlayer.on(flvjs.Events.ERROR, (e: any) => emit('error', e));
          emit('play');
        } catch (err) {
          emit('error', err);
        }
      }

      function pause() {
        videoRef.value?.pause();
      }

      /** 仅销毁 flv.js 实例，不标记组件已销毁 */
      function destroyFlvPlayer() {
        if (flvPlayer) {
          try {
            flvPlayer.pause();
            flvPlayer.unload();
            flvPlayer.detachMediaElement();
            flvPlayer.destroy();
          } catch (e) {
            console.warn('FlvPlayer destroy error:', e);
          }
          flvPlayer = null;
        }
      }

      function destroy() {
        if (destroyed) return;
        destroyed = true;
        destroyFlvPlayer();
      }

      function setVolume(volume: number) {
        if (videoRef.value) {
          videoRef.value.volume = Math.max(0, Math.min(1, volume));
        }
      }

      function fullscreen() {
        videoRef.value?.requestFullscreen();
      }

      function screenshot(filename?: string) {
        if (!videoRef.value) return;
        const canvas = document.createElement('canvas');
        canvas.width = videoRef.value.videoWidth;
        canvas.height = videoRef.value.videoHeight;
        canvas.getContext('2d')?.drawImage(videoRef.value, 0, 0);
        const link = document.createElement('a');
        link.download = `${filename || 'snapshot'}.png`;
        link.href = canvas.toDataURL('image/png');
        link.click();
      }

      watch(
        () => props.url,
        (newUrl) => {
          if (newUrl && !destroyed) play(newUrl);
        },
      );

      onMounted(() => initPlayer());
      onBeforeUnmount(() => destroy());

      expose({ play, pause, destroy, setVolume, fullscreen, screenshot });

      return { videoRef, containerStyle, onLoadedMetadata };
    },
  });
</script>
<style lang="less" scoped>
  .flv-player {
    background: #000;
    border-radius: 4px;
    /* object-fit 由 fillMode prop 通过 inline style 控制（contain/cover/fill），不在此处硬编码 */
  }
</style>
