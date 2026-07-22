<template>
  <div class="root">
    <div class="container-shell">
      <div class="performance">
        <div class="k-bps">{{ kBps }}kb/s</div>
        <div class="performance-content" :style="{ background: performance }"></div>
      </div>
      <div class="vide-main">
        <div id="container" ref="container" @click="handleChangeStatus"></div>
        <div class="pause-overlay" v-if="isShowPaused">
          <div class="pause-icon">
            <div></div>
            <div></div>
          </div>
        </div>
      </div>
      <div class="bottom-operate">
        <div class="operate-left">
          <SvgIcon name="bofang" @click="handleChangeStatus" v-if="!playing" class="operate-icon" />
          <SvgIcon name="zanting" @click="handleChangeStatus" v-else class="operate-icon" />
        </div>
        <div class="operate-right">
          <VolumeControl
            :volume="volume"
            :muted="quieting"
            @update:volume="updateVolume"
            @update:muted="updateMuted"
          />
          <SvgIcon name="shipinluzhi" @click="startRecord" v-if="!recording" class="operate-icon" />
          <SvgIcon name="tingzhiluzhi" @click="stopAndSaveRecord" v-else class="operate-icon" />
          <SvgIcon name="xiangji" @click="screenShot" class="operate-icon" />
          <SvgIcon name="xuanzhuan" @click="rotateChange" class="operate-icon" />
          <a-popover title="设置" trigger="click">
            <template #content>
              <div class="popover-container">
                <div class="popover-item">
                  <div class="label">{{ t('video.media.proxy.buffer') }}: </div>
                  <a-input-number v-model:value="buffer" @change="changeBuffer" :step="0.1" />
                </div>
                <div class="popover-item">
                  <div class="label"
                    >{{ t('video.media.proxy.enable') }}MediaSource:
                  </div>
                  <a-switch v-model:checked="useMSE" @change="restartPlay('mse')" />
                </div>
                <div class="popover-item">
                  <div class="label"
                    >{{ t('video.media.proxy.enable') }}开启Webcodecs:
                  </div>
                  <a-switch v-model:checked="useWCS" @change="restartPlay('wcs')" />
                </div>
                <div class="popover-item">
                  <div class="label">{{ t('video.media.proxy.zoomSetting') }}: </div>
                  <a-select v-model:value="scale" @change="scaleChange" size="small">
                    <a-select-option :value="0">{{
                      t('video.media.proxy.fullFill1')
                    }}</a-select-option>
                    <a-select-option :value="1">{{
                      t('video.media.proxy.proportionalScale')
                    }}</a-select-option>
                    <a-select-option :value="2">{{
                      t('video.media.proxy.fullFill2')
                    }}</a-select-option>
                  </a-select>
                </div>
                <div class="popover-item">
                  <div class="label"
                    >{{ t('video.media.proxy.videoRecodingFormat') }}:
                  </div>
                  <a-select v-model:value="recordType" size="small">
                    <a-select-option value="webm">webm</a-select-option>
                    <a-select-option value="mp4">mp4</a-select-option>
                  </a-select>
                </div>
              </div>
            </template>

            <SvgIcon name="shezhi" class="operate-icon" />
          </a-popover>
          <SvgIcon name="quanping" class="operate-icon" @click="fullscreen" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import VolumeControl from '../components/VolumeControl.vue';
  const props = defineProps({ videoUrl: String });

  const jessibuca = ref(null);
  const buffer = ref(0.2);
  const useMSE = ref(true);
  const useWCS = ref(false);
  const playing = ref(true);
  const quieting = ref(true);
  const loaded = ref(false);
  const showOperateBtns = ref(false);
  const showBandwidth = ref(false);
  const performance = ref('');
  const volume = ref(1);
  const rotate = ref(0);
  const useOffscreen = ref(false);
  const recording = ref(false);
  const recordType = ref('webm');
  const scale = ref(0);
  const container = ref(null);
  const kBps = ref(0);
  const isShowPaused = ref(false);

  onMounted(() => {
    create();
    play();
    window.onerror = (msg) => {
      console.error(msg);
    };
  });

  onBeforeUnmount(async () => {
    if (jessibuca.value) {
      await jessibuca.value.destroy();
      jessibuca.value = null;
    }
  });

  const create = (options = {}) => {
    jessibuca.value = new window.Jessibuca(
      Object.assign(
        {
          container: container.value,
          decoder: '/jessibuca/decoder.js',
          videoBuffer: Number(buffer.value),
          isResize: false,
          useWCS: useWCS.value,
          useMSE: useMSE.value,
          text: '',
          loadingText: '疯狂加载中...',
          debug: true,
          supportDblclickFullscreen: true,
          showBandwidth: showBandwidth.value,
          operateBtns: {
            fullscreen: showOperateBtns.value,
            screenshot: showOperateBtns.value,
            play: showOperateBtns.value,
            audio: showOperateBtns.value,
          },
          vod: false,
          forceNoOffscreen: !useOffscreen.value,
          isNotMute: true,
          timeout: 10,
        },
        options,
      ),
    );

    jessibuca.value.on('load', () => console.log('on load'));
    jessibuca.value.on('log', (msg) => console.log('on log', msg));
    jessibuca.value.on('record', (msg) => console.log('on record:', msg));
    jessibuca.value.on('pause', () => {
      console.log('on pause');
      playing.value = false;
    });
    jessibuca.value.on('fullscreen', (msg) => console.log('on fullscreen', msg));
    jessibuca.value.on('mute', (msg) => {
      console.log('on mute', msg);
      quieting.value = msg;
    });
    jessibuca.value.on('audioInfo', (msg) => console.log('audioInfo', msg));
    jessibuca.value.on('videoInfo', (info) => console.log('videoInfo', info));
    jessibuca.value.on('error', (error) => console.log('error', error));
    jessibuca.value.on('timeout', () => console.log('timeout'));
    jessibuca.value.on('start', () => console.log('frame start'));
    jessibuca.value.on('performance', (res) => {
      let show = 'red';
      if (res === 2) {
        show = 'green';
      } else if (res === 1) {
        show = 'orange';
      }
      performance.value = show;
    });

    jessibuca.value.on('play', () => {
      playing.value = true;
      loaded.value = true;
      quieting.value = jessibuca.value.isMute();
    });
    jessibuca.value.on('kBps', (res) => {
      kBps.value = Math.round(res);
    });
  };

  const play = () => {
    if (props.videoUrl) {
      jessibuca.value.play(props.videoUrl);
    }
  };

  const mute = () => {
    jessibuca.value.mute();
  };

  const cancelMute = () => {
    jessibuca.value.cancelMute();
  };
  const updateMuted = (newMuted) => {
    quieting.value = newMuted;
    if (newMuted) {
      mute();
    } else {
      cancelMute();
    }
  };

  const pause = () => {
    jessibuca.value.pause();
    playing.value = false;
    performance.value = '';
  };

  const updateVolume = (newVolume) => {
    volume.value = newVolume;
    jessibuca.value.setVolume(volume.value);
  };

  const rotateChange = () => {
    if (rotate.value === 270) {
      rotate.value = 0;
    } else {
      rotate.value = rotate.value + 90;
    }
    jessibuca.value.setRotate(rotate.value);
  };

  const destroy = async () => {
    if (jessibuca.value) {
      await jessibuca.value.destroy();
    }
    create();
    playing.value = false;
    loaded.value = false;
    performance.value = '';
  };

  const fullscreen = () => {
    jessibuca.value.setFullscreen(true);
  };

  const startRecord = () => {
    recording.value = true;
    const time = new Date().getTime();
    jessibuca.value.startRecord(time, recordType.value);
  };

  const stopAndSaveRecord = () => {
    recording.value = false;
    jessibuca.value.stopRecordAndSave();
  };

  const screenShot = () => {
    jessibuca.value.screenshot();
  };

  const restartPlay = async (type) => {
    if (type === 'mse') {
      useWCS.value = false;
      useOffscreen.value = false;
    } else if (type === 'wcs') {
      useMSE.value = false;
    } else if (type === 'offscreen') {
      //由于目前浏览器兼容性太差，会出现慕名奇妙的错误问题，目前播放器内部禁用离屏渲染。
      useMSE.value = false;
    }
    await destroy();
    setTimeout(() => {
      play();
    }, 100);
  };

  const changeBuffer = () => {
    jessibuca.value.setBufferTime(Number(buffer.value));
  };

  const scaleChange = () => {
    jessibuca.value.setScaleMode(scale.value);
  };
  const handleChangeStatus = async () => {
    if (playing.value) {
      isShowPaused.value = true;
      pause();
    } else {
      isShowPaused.value = false;
      play();
    }
  };

  watch(
    () => props.videoUrl,
    async () => {
      play();
    },
  );
</script>

<style scoped lang="less">
  .root {
    display: flex;
  }

  .container-shell {
    width: 100%;
    position: relative;
    background: #000;
    padding: 30px 0 0 0;
    border-radius: 4px;
    .performance {
      position: absolute;
      top: 10px;
      right: 14px;
      display: flex;
      align-items: center;
      gap: 12px;
      .k-bps {
        color: #fff;
        font-size: 12px;
      }
      &-content {
        width: 12px;
        height: 12px;
        border-radius: 50%;
      }
    }
    .vide-main {
      position: relative;
      .pause-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        background: rgba(0, 0, 0, 0.5);
        pointer-events: none;
        transition: opacity 0.3s ease;
      }

      .pause-overlay.active {
        opacity: 1;
        pointer-events: auto;
      }

      .pause-icon {
        width: 50px;
        height: 40px;
        display: flex;
        place-content: center space-around;
        div {
          width: 14px;
          height: 40px;
          background-color: rgba(255, 255, 255, 0.5);
          border-radius: 4px;
        }
      }
    }
    .bottom-operate {
      background-color: rgba(255, 255, 255, 0.5);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px;
      .operate-right {
        display: flex;
        align-items: center;
        gap: 10px;
      }
      .operate-icon {
        transition: all linear 0.2s;
        cursor: pointer;
        &:hover {
          transform: scale(1.1);
        }
      }
    }
  }

  .container-shell-title {
    position: absolute;
    color: darkgray;
    top: 4px;
    left: 10px;
    text-shadow: 1px 1px black;
  }

  #container {
    background: rgba(13, 14, 27, 0.7);
    height: 350px;
  }

  .input {
    display: flex;
    align-items: center;
    margin-top: 10px;
    color: white;
    place-content: stretch;
  }

  .input input[type='input'] {
    flex: auto;
  }

  .option {
    position: absolute;
    top: 4px;
    right: 10px;
    display: flex;
    place-content: center;
    font-size: 12px;
  }

  .option span {
    color: white;
  }
  .popover-container {
    .popover-item {
      display: flex;
      font-size: 12px;
      color: #999;
      margin-bottom: 6px;
      .label {
        width: 102px;
        margin-right: 4px;
        text-align: right;
      }
      :deep(.ant-input-number) {
        width: 100px !important;
      }
      :deep(.ant-input-number-input) {
        height: 22px;
      }
      :deep(.ant-select) {
        font-size: 12px;
      }
    }
  }
</style>
