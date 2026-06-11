<template>
  <div class="video-player-wrapper">
    <JessibucaPlayer
      v-if="currentPlayer === 'jessibuca'"
      ref="playerRef"
      :url="url"
      :autoplay="autoplay"
      :muted="muted"
      :width="width"
      :height="height"
      :hasAudio="hasAudio"
      :auto-aspect="autoAspect"
      :fill-mode="fillMode"
      @ready="$emit('ready', $event)"
      @play="$emit('play')"
      @pause="$emit('pause')"
      @error="$emit('error', $event)"
      @close="$emit('close')"
    />
    <FlvPlayer
      v-else-if="currentPlayer === 'flv'"
      ref="playerRef"
      :url="url"
      :autoplay="autoplay"
      :muted="muted"
      :width="width"
      :height="height"
      :auto-aspect="autoAspect"
      :fill-mode="fillMode"
      @ready="$emit('ready', $event)"
      @play="$emit('play')"
      @pause="$emit('pause')"
      @error="$emit('error', $event)"
      @close="$emit('close')"
    />
    <HlsPlayer
      v-else-if="currentPlayer === 'hls'"
      ref="playerRef"
      :url="url"
      :autoplay="autoplay"
      :muted="muted"
      :width="width"
      :height="height"
      :auto-aspect="autoAspect"
      :fill-mode="fillMode"
      @ready="$emit('ready', $event)"
      @play="$emit('play')"
      @pause="$emit('pause')"
      @error="$emit('error', $event)"
      @close="$emit('close')"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watch, nextTick } from 'vue';
  import JessibucaPlayer from './JessibucaPlayer.vue';
  import FlvPlayer from './FlvPlayer.vue';
  import HlsPlayer from './HlsPlayer.vue';
  import type { PlayerType } from './types';

  export default defineComponent({
    name: 'VideoPlayer',
    components: { JessibucaPlayer, FlvPlayer, HlsPlayer },
    props: {
      url: { type: String, default: '' },
      playerType: { type: String as () => PlayerType, default: 'jessibuca' },
      autoplay: { type: Boolean, default: true },
      muted: { type: Boolean, default: false },
      width: { type: [String, Number], default: '100%' },
      height: { type: [String, Number], default: 400 },
      hasAudio: { type: Boolean, default: true },
      // 自适应模式：容器宽高比跟随视频实际比例（detail 单画面用），关闭保持固定 height（分屏用）
      autoAspect: { type: Boolean, default: false },
      // 填充模式：分屏 cover 等比裁剪填满（推荐）；contain 兼容旧 letterbox 行为；fill 拉伸（变形，慎用）
      fillMode: { type: String as () => 'contain' | 'cover' | 'fill', default: 'contain' },
    },
    emits: ['ready', 'play', 'pause', 'error', 'close', 'playerChange'],
    setup(props, { emit, expose }) {
      const playerRef = ref<any>(null);
      const currentPlayer = ref<PlayerType>(props.playerType);

      async function switchPlayer(type: PlayerType) {
        if (type === currentPlayer.value) return;
        // 切换 currentPlayer 时，Vue 会卸载旧组件（触发 onBeforeUnmount → destroy），
        // 无需手动调用 destroy，避免双重销毁
        currentPlayer.value = type;
        emit('playerChange', type);
        await nextTick();
      }

      function play(url?: string) {
        playerRef.value?.play?.(url);
      }

      function pause() {
        playerRef.value?.pause?.();
      }

      function destroy() {
        // 仅在手动调用时触发，子组件的 onBeforeUnmount 会自行处理
        playerRef.value?.destroy?.();
      }

      function setVolume(volume: number) {
        playerRef.value?.setVolume?.(volume);
      }

      function fullscreen() {
        playerRef.value?.fullscreen?.();
      }

      function screenshot(filename?: string) {
        playerRef.value?.screenshot?.(filename);
      }

      watch(
        () => props.playerType,
        (newType) => {
          if (newType !== currentPlayer.value) {
            switchPlayer(newType);
          }
        },
      );

      expose({
        play,
        pause,
        destroy,
        setVolume,
        fullscreen,
        screenshot,
        switchPlayer,
        getCurrentPlayer: () => currentPlayer.value,
      });

      return {
        playerRef,
        currentPlayer,
      };
    },
  });
</script>
<style lang="less" scoped>
  .video-player-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
  }
</style>
