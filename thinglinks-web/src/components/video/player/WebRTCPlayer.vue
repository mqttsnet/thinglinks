<template>
  <div class="webrtc-player" :style="{ width, height }">
    <video
      ref="videoRef"
      autoplay
      muted
      playsinline
      :style="{ width: '100%', height: '100%', objectFit: 'contain', background: '#000' }"
    />
    <div v-if="loading" class="webrtc-player__loading">
      <a-spin :tip="t('video.player.webrtc.connecting')" />
    </div>
    <div v-if="error" class="webrtc-player__error">
      <Icon icon="ant-design:warning-outlined" :size="24" color="#ff4d4f" />
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { t } = useI18n();

  const props = defineProps({
    url: { type: String, default: '' },
    width: { type: String, default: '100%' },
    height: { type: String, default: '100%' },
    autoplay: { type: Boolean, default: true },
  });

  const emit = defineEmits(['ready', 'error', 'close']);

  const videoRef = ref<HTMLVideoElement>();
  const loading = ref(false);
  const error = ref('');
  let pc: RTCPeerConnection | null = null;

  watch(
    () => props.url,
    (newUrl) => {
      if (newUrl) {
        play(newUrl);
      } else {
        destroy();
      }
    },
  );

  onMounted(() => {
    if (props.url && props.autoplay) {
      play(props.url);
    }
  });

  onBeforeUnmount(() => {
    destroy();
  });

  async function play(url: string) {
    if (!url) return;
    destroy();
    loading.value = true;
    error.value = '';

    try {
      pc = new RTCPeerConnection();

      pc.addTransceiver('video', { direction: 'recvonly' });
      pc.addTransceiver('audio', { direction: 'recvonly' });

      pc.ontrack = (event) => {
        if (videoRef.value && event.streams?.[0]) {
          videoRef.value.srcObject = event.streams[0];
        }
      };

      pc.oniceconnectionstatechange = () => {
        if (pc?.iceConnectionState === 'connected') {
          loading.value = false;
          emit('ready');
        } else if (pc?.iceConnectionState === 'failed' || pc?.iceConnectionState === 'disconnected') {
          error.value = t('video.player.webrtc.disconnected');
          loading.value = false;
          emit('error', error.value);
        }
      };

      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);

      // 等待 ICE gathering 完成
      await new Promise<void>((resolve) => {
        if (pc?.iceGatheringState === 'complete') {
          resolve();
        } else {
          pc!.onicegatheringstatechange = () => {
            if (pc?.iceGatheringState === 'complete') resolve();
          };
          // 超时兜底
          setTimeout(resolve, 3000);
        }
      });

      // POST SDP offer to ZLMediaKit WebRTC API
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/sdp' },
        body: pc.localDescription?.sdp,
      });

      if (!response.ok) {
        throw new Error(`${t('video.player.webrtc.sdpExchangeFailed')}: ${response.status}`);
      }

      const answerSdp = await response.text();
      await pc.setRemoteDescription({ type: 'answer', sdp: answerSdp });
    } catch (e: any) {
      error.value = e.message || t('video.player.webrtc.connectFailed');
      loading.value = false;
      emit('error', error.value);
    }
  }

  function destroy() {
    if (pc) {
      pc.close();
      pc = null;
    }
    if (videoRef.value) {
      videoRef.value.srcObject = null;
    }
    loading.value = false;
    error.value = '';
    emit('close');
  }

  defineExpose({ play, destroy });
</script>

<style lang="less" scoped>
  .webrtc-player {
    position: relative;
    background: #000;

    &__loading,
    &__error {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-size: 13px;
    }
  }
</style>
