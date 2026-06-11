<template>
  <div ref="containerRef" :style="containerStyle" class="jessibuca-player"></div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';

  // 缓存脚本加载 Promise，防止多次并发加载创建重复 script 标签
  let jessibucaLoadPromise: Promise<any> | null = null;

  export default defineComponent({
    name: 'JessibucaPlayer',
    props: {
      url: { type: String, default: '' },
      autoplay: { type: Boolean, default: true },
      muted: { type: Boolean, default: false },
      width: { type: [String, Number], default: '100%' },
      height: { type: [String, Number], default: 400 },
      hasAudio: { type: Boolean, default: true },
      showOperationBar: { type: Boolean, default: true },
      // 自适应模式：容器宽高比跟随视频实际比例（detail / 单画面用），关闭则保持固定 height（分屏网格用）
      autoAspect: { type: Boolean, default: false },
      // 填充模式（仅 autoAspect=false 时生效）：
      //   contain - 保留完整画面，宽高比不匹配时留黑边（旧行为）
      //   cover   - 等比裁剪填满容器，无黑边（分屏推荐：单元格大小固定不会跳动，iPad 也表现稳定）
      //   fill    - 直接拉伸（会变形，慎用）
      fillMode: { type: String as () => 'contain' | 'cover' | 'fill', default: 'contain' },
    },
    emits: ['ready', 'play', 'pause', 'error', 'close'],
    setup(props, { emit, expose }) {
      const containerRef = ref<HTMLDivElement>();
      let jessibuca: any = null;
      let destroyed = false;

      // 视频实际宽高比，从 Jessibuca 的 videoInfo 事件回填；拿到前用 16:9 兜底
      const aspectRatio = ref<string>('16 / 9');

      const containerStyle = computed(() => {
        const widthStyle = typeof props.width === 'number' ? `${props.width}px` : props.width;
        const heightStyle = typeof props.height === 'number' ? `${props.height}px` : props.height;
        if (props.autoAspect) {
          // 自适应模式：容器宽高比 = 视频比例（拿到 videoInfo 后），消除黑边；
          // height 退化为 minHeight 兜底，避免视频流尚未到达时容器塌成 0px。
          return {
            width: widthStyle,
            aspectRatio: aspectRatio.value,
            ...(heightStyle && heightStyle !== 'auto' ? { minHeight: heightStyle } : {}),
          };
        }
        // 固定模式（分屏网格等场景）：严格按外部尺寸约束，不影响布局
        return {
          width: widthStyle,
          height: heightStyle,
        };
      });

      function loadScript(src: string): Promise<void> {
        return new Promise((resolve, reject) => {
          const script = document.createElement('script');
          script.src = src;
          script.onload = () => resolve();
          script.onerror = () => reject(new Error(`Failed to load script: ${src}`));
          document.head.appendChild(script);
        });
      }

      function loadJessibuca(): Promise<any> {
        // 已加载直接返回（优先 Pro）
        if ((window as any).JessibucaPro) {
          return Promise.resolve((window as any).JessibucaPro);
        }
        if ((window as any).Jessibuca) {
          return Promise.resolve((window as any).Jessibuca);
        }
        if (jessibucaLoadPromise) {
          return jessibucaLoadPromise;
        }
        // 先加载标准版，再加载 Pro 适配层
        jessibucaLoadPromise = loadScript('/jessibuca/jessibuca.js')
          .then(() => {
            // 标准版已就位，尝试加载 Pro 适配层
            return loadScript('/resource/jessibuca-pro/jessibuca-pro.js')
              .then(() => {
                if ((window as any).JessibucaPro) return (window as any).JessibucaPro;
                return (window as any).Jessibuca;
              })
              .catch(() => {
                // Pro 适配层不存在，直接用标准版
                return (window as any).Jessibuca;
              });
          })
          .catch((err) => {
            jessibucaLoadPromise = null;
            throw err;
          });
        return jessibucaLoadPromise;
      }

      async function initPlayer() {
        if (!containerRef.value || destroyed) return;
        try {
          const JessibucaClass = await loadJessibuca();
          if (destroyed) return; // 组件可能在 await 期间已销毁

          const isPro = !!(window as any).JessibucaPro;
          const baseConfig: Record<string, any> = {
            container: containerRef.value,
            videoBuffer: 0.2,
            isResize: true,
            loadingText: '',
            debug: false,
            supportDblclickFullscreen: true,
            showBandwidth: false,
            hasAudio: props.hasAudio,
            useMSE: true,
            useWCS: isPro,
            timeout: 10,
            heartTimeout: 10,
            // 超时自动重连（不稳定流源容错）
            heartTimeoutReplay: true,
            heartTimeoutReplayTimes: 3,
            loadingTimeoutReplay: true,
            loadingTimeoutReplayTimes: 3,
            operateBtns: props.showOperationBar
              ? {
                  fullscreen: true,
                  screenshot: true,
                  play: true,
                  audio: props.hasAudio,
                }
              : {},
            forceNoOffscreen: true,
            isNotMute: !props.muted,
            decoder: '/jessibuca/decoder.js',
          };
          // Pro 版本专有配置
          if (isPro) {
            baseConfig.videoBufferDelay = 1;
            baseConfig.isFullResize = true;
            baseConfig.audioEngine = 'worklet';
            baseConfig.heartTimeout = 15;
            baseConfig.heartTimeoutReplayUseLastFrameShow = true;
            baseConfig.decoder = '/resource/jessibuca-pro/decoder-pro.js';
          }
          // fillMode → Jessibuca 的 scaleMode：0=contain（letterbox），1=fill（拉伸），2=cover（等比裁剪填满）
          // 仅 autoAspect=false 时这个配置才有意义；autoAspect 模式下容器自身就是视频比例，contain 已无黑边
          const scaleModeMap: Record<string, number> = { contain: 0, fill: 1, cover: 2 };
          baseConfig.scaleMode = scaleModeMap[props.fillMode] ?? 0;
          jessibuca = new JessibucaClass(baseConfig);

          jessibuca.on('play', () => emit('play'));
          jessibuca.on('pause', () => emit('pause'));
          jessibuca.on('error', (err: any) => emit('error', err));
          jessibuca.on('close', () => {
            // 流关闭后重置回 16:9 兜底，避免下一路流复用上一路的比例
            aspectRatio.value = '16 / 9';
            emit('close');
          });

          // 视频信息事件：拿到分辨率后让容器宽高比对齐视频，彻底消除黑边
          // Jessibuca / Jessibuca-Pro 的事件名都是 'videoInfo'
          jessibuca.on('videoInfo', (info: { width?: number; height?: number }) => {
            if (info?.width && info.height && info.width > 0 && info.height > 0) {
              aspectRatio.value = `${info.width} / ${info.height}`;
            }
          });

          // jessibuca-pro 特有事件：播放失败并暂停
          if (JessibucaClass.EVENTS?.playFailedAndPaused) {
            jessibuca.on(JessibucaClass.EVENTS.playFailedAndPaused, (error: any) => {
              console.warn('Jessibuca playFailedAndPaused:', error);
              emit('error', error);
            });
          }

          emit('ready', jessibuca);

          if (props.url && props.autoplay) {
            await nextTick();
            play(props.url);
          }
        } catch (err) {
          console.error('Failed to init jessibuca player:', err);
          emit('error', err);
        }
      }

      function play(url?: string) {
        const playUrl = url || props.url;
        if (!jessibuca || !playUrl || destroyed) return;
        // 切换流地址时先 close 再 play，避免流残留
        jessibuca.close?.();
        jessibuca.play(playUrl);
      }

      function pause() {
        jessibuca?.pause();
      }

      async function destroy() {
        if (destroyed) return;
        destroyed = true;
        if (jessibuca) {
          try {
            // jessibuca.destroy() 返回 Promise
            await jessibuca.destroy();
          } catch (e) {
            console.warn('Jessibuca destroy error:', e);
          }
          jessibuca = null;
        }
      }

      function setVolume(volume: number) {
        jessibuca?.setVolume(volume);
      }

      function fullscreen() {
        jessibuca?.setFullscreen(true);
      }

      function screenshot(filename?: string) {
        jessibuca?.screenshot(filename || 'snapshot', 'png');
      }

      function isMute(): boolean {
        return jessibuca?.isMute() ?? true;
      }

      function toggleMute() {
        if (jessibuca) {
          if (jessibuca.isMute()) {
            jessibuca.cancelMute();
          } else {
            jessibuca.mute();
          }
        }
      }

      watch(
        () => props.url,
        (newUrl) => {
          if (newUrl && jessibuca && !destroyed) {
            play(newUrl);
          }
        },
      );

      onMounted(() => {
        initPlayer();
      });

      onBeforeUnmount(() => {
        destroy();
      });

      function clearView() { jessibuca?.clearView(); }
      function setScaleMode(m: number) { jessibuca?.setScaleMode(m); }
      function setRotate(d: number) { jessibuca?.setRotate(d); }
      function startRecord(f?: string, t?: string) { jessibuca?.startRecord(f || `rec_${Date.now()}`, t || 'mp4'); }
      function stopRecordAndSave() { jessibuca?.stopRecordAndSave(); }
      function isRecording(): boolean { return jessibuca?.isRecording() ?? false; }
      function isPlaying(): boolean { return jessibuca?.isPlaying() ?? false; }

      expose({
        play,
        pause,
        destroy,
        setVolume,
        fullscreen,
        screenshot,
        isMute,
        toggleMute,
        clearView,
        setScaleMode,
        setRotate,
        startRecord,
        stopRecordAndSave,
        isRecording,
        isPlaying,
        getInstance: () => jessibuca,
      });

      return {
        containerRef,
        containerStyle,
      };
    },
  });
</script>
<style lang="less" scoped>
  .jessibuca-player {
    background: #000;
    border-radius: 4px;
    overflow: hidden;
  }
</style>
