<template>
  <div class="record-timeline" ref="containerRef">
    <div class="timeline-header">
      <span class="timeline-title">{{ t('video.record.playback.timeline') }}</span>
      <div class="timeline-zoom">
        <a-tooltip :title="t('video.record.playback.timelineZoomOut')">
          <a-button size="small" :icon="h(ZoomOutOutlined)" @click="zoomOut" :disabled="zoomLevel >= zoomLevels.length - 1" />
        </a-tooltip>
        <span class="zoom-label">{{ zoomLevels[zoomLevel].label }}</span>
        <a-tooltip :title="t('video.record.playback.timelineZoomIn')">
          <a-button size="small" :icon="h(ZoomInOutlined)" @click="zoomIn" :disabled="zoomLevel <= 0" />
        </a-tooltip>
      </div>
    </div>
    <div
      class="timeline-canvas-wrapper"
      ref="canvasWrapperRef"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
      @wheel.prevent="handleWheel"
    >
      <canvas ref="canvasRef" />
      <div v-if="hoverTime" class="timeline-tooltip" :style="{ left: tooltipLeft + 'px' }">
        {{ hoverTime }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted, nextTick, h, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons-vue';

  interface TimeSegment {
    startTime: number; // seconds from 00:00
    endTime: number;
  }

  const props = withDefaults(
    defineProps<{
      segments: TimeSegment[];
      currentTime?: number; // seconds from 00:00
      totalSeconds?: number; // default 86400 (24h)
    }>(),
    {
      currentTime: 0,
      totalSeconds: 86400,
    },
  );

  const emit = defineEmits<{
    (e: 'seek', time: number): void;
  }>();

  const { t } = useI18n();
  const containerRef = ref<HTMLDivElement>();
  const canvasWrapperRef = ref<HTMLDivElement>();
  const canvasRef = ref<HTMLCanvasElement>();
  const hoverTime = ref('');
  const tooltipLeft = ref(0);

  const zoomLevels = [
    { hours: 1, label: '1h' },
    { hours: 2, label: '2h' },
    { hours: 4, label: '4h' },
    { hours: 8, label: '8h' },
    { hours: 12, label: '12h' },
    { hours: 24, label: '24h' },
  ];
  const zoomLevel = ref(5); // default 24h view
  const panOffset = ref(0); // seconds offset from 00:00

  // Canvas dimensions
  const CANVAS_HEIGHT = 56;
  const TRACK_Y = 16;
  const TRACK_HEIGHT = 24;

  // Colors (Flexy Dashboard style)
  const COLOR_BG = '#f8fafc';
  const COLOR_TRACK = '#e8ecf1';
  const COLOR_SEGMENT = '#5d87ff';
  const COLOR_SEGMENT_HOVER = '#7c9fff';
  const COLOR_POINTER = '#fa896b';
  const COLOR_TICK = '#c5cdd8';
  const COLOR_TICK_TEXT = '#8c97a5';
  const COLOR_GRID = '#f0f2f5';

  let animationId: number | null = null;
  let isDragging = false;
  let dragStartX = 0;
  let dragStartOffset = 0;

  const visibleSeconds = computed(() => zoomLevels[zoomLevel.value].hours * 3600);

  function zoomIn() {
    if (zoomLevel.value > 0) {
      const centerTime = panOffset.value + visibleSeconds.value / 2;
      zoomLevel.value--;
      panOffset.value = Math.max(0, centerTime - visibleSeconds.value / 2);
      clampPanOffset();
      draw();
    }
  }

  function zoomOut() {
    if (zoomLevel.value < zoomLevels.length - 1) {
      const centerTime = panOffset.value + visibleSeconds.value / 2;
      zoomLevel.value++;
      panOffset.value = Math.max(0, centerTime - visibleSeconds.value / 2);
      clampPanOffset();
      draw();
    }
  }

  function clampPanOffset() {
    const max = Math.max(0, props.totalSeconds - visibleSeconds.value);
    panOffset.value = Math.min(Math.max(0, panOffset.value), max);
  }

  function secondsToHMS(totalSec: number): string {
    const h = Math.floor(totalSec / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = Math.floor(totalSec % 60);
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  function xToTime(x: number): number {
    const canvas = canvasRef.value;
    if (!canvas) return 0;
    const ratio = x / canvas.width;
    return panOffset.value + ratio * visibleSeconds.value;
  }

  function timeToX(time: number): number {
    const canvas = canvasRef.value;
    if (!canvas) return 0;
    return ((time - panOffset.value) / visibleSeconds.value) * canvas.width;
  }

  function draw() {
    const canvas = canvasRef.value;
    const wrapper = canvasWrapperRef.value;
    if (!canvas || !wrapper) return;

    const dpr = window.devicePixelRatio || 1;
    const rect = wrapper.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = CANVAS_HEIGHT * dpr;
    canvas.style.width = rect.width + 'px';
    canvas.style.height = CANVAS_HEIGHT + 'px';

    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.scale(dpr, dpr);
    const W = rect.width;
    const H = CANVAS_HEIGHT;

    // Background
    ctx.fillStyle = COLOR_BG;
    ctx.fillRect(0, 0, W, H);

    // Track background
    ctx.fillStyle = COLOR_TRACK;
    ctx.beginPath();
    ctx.roundRect(0, TRACK_Y, W, TRACK_HEIGHT, 4);
    ctx.fill();

    // Grid lines & time ticks
    const vs = visibleSeconds.value;
    let tickInterval: number;
    if (vs <= 3600) tickInterval = 300; // 5min
    else if (vs <= 7200) tickInterval = 600; // 10min
    else if (vs <= 14400) tickInterval = 1800; // 30min
    else if (vs <= 28800) tickInterval = 3600; // 1h
    else tickInterval = 7200; // 2h

    const startTick = Math.ceil(panOffset.value / tickInterval) * tickInterval;
    for (let t = startTick; t <= panOffset.value + vs; t += tickInterval) {
      const x = timeToX(t);
      if (x < 0 || x > W) continue;

      // Grid line
      ctx.strokeStyle = COLOR_GRID;
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      ctx.moveTo(x, TRACK_Y);
      ctx.lineTo(x, TRACK_Y + TRACK_HEIGHT);
      ctx.stroke();

      // Tick line
      ctx.strokeStyle = COLOR_TICK;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(x, TRACK_Y + TRACK_HEIGHT);
      ctx.lineTo(x, TRACK_Y + TRACK_HEIGHT + 6);
      ctx.stroke();

      // Tick label
      ctx.fillStyle = COLOR_TICK_TEXT;
      ctx.font = '10px -apple-system, BlinkMacSystemFont, sans-serif';
      ctx.textAlign = 'center';
      const label = secondsToHMS(t).substring(0, 5); // HH:MM
      ctx.fillText(label, x, H - 2);
    }

    // Recording segments
    for (const seg of props.segments) {
      const x1 = Math.max(0, timeToX(seg.startTime));
      const x2 = Math.min(W, timeToX(seg.endTime));
      if (x2 <= 0 || x1 >= W) continue;

      ctx.fillStyle = COLOR_SEGMENT;
      ctx.beginPath();
      ctx.roundRect(x1, TRACK_Y + 2, Math.max(2, x2 - x1), TRACK_HEIGHT - 4, 3);
      ctx.fill();
    }

    // Current time pointer
    if (props.currentTime >= panOffset.value && props.currentTime <= panOffset.value + vs) {
      const px = timeToX(props.currentTime);

      // Pointer line
      ctx.strokeStyle = COLOR_POINTER;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(px, TRACK_Y - 2);
      ctx.lineTo(px, TRACK_Y + TRACK_HEIGHT + 2);
      ctx.stroke();

      // Pointer triangle
      ctx.fillStyle = COLOR_POINTER;
      ctx.beginPath();
      ctx.moveTo(px - 5, TRACK_Y - 2);
      ctx.lineTo(px + 5, TRACK_Y - 2);
      ctx.lineTo(px, TRACK_Y + 4);
      ctx.closePath();
      ctx.fill();
    }
  }

  function handleMouseDown(e: MouseEvent) {
    const rect = canvasWrapperRef.value?.getBoundingClientRect();
    if (!rect) return;
    const x = e.clientX - rect.left;

    // Check if clicking on the timeline track area for seeking
    if (e.clientY - rect.top >= TRACK_Y && e.clientY - rect.top <= TRACK_Y + TRACK_HEIGHT) {
      const time = xToTime(x);
      emit('seek', Math.floor(time));
      return;
    }

    // Otherwise start panning
    isDragging = true;
    dragStartX = e.clientX;
    dragStartOffset = panOffset.value;
  }

  function handleMouseMove(e: MouseEvent) {
    const rect = canvasWrapperRef.value?.getBoundingClientRect();
    if (!rect) return;

    if (isDragging) {
      const dx = e.clientX - dragStartX;
      const timeDelta = (dx / rect.width) * visibleSeconds.value;
      panOffset.value = dragStartOffset - timeDelta;
      clampPanOffset();
      draw();
      return;
    }

    const x = e.clientX - rect.left;
    const time = xToTime(x);
    if (time >= 0 && time <= props.totalSeconds) {
      hoverTime.value = secondsToHMS(time);
      tooltipLeft.value = Math.min(Math.max(30, x), rect.width - 30);
    } else {
      hoverTime.value = '';
    }
  }

  function handleMouseLeave() {
    isDragging = false;
    hoverTime.value = '';
  }

  function handleWheel(e: WheelEvent) {
    if (e.deltaY < 0) {
      zoomIn();
    } else {
      zoomOut();
    }
  }

  // Auto-pan to keep current time in view
  watch(
    () => props.currentTime,
    (val) => {
      if (val < panOffset.value || val > panOffset.value + visibleSeconds.value) {
        panOffset.value = Math.max(0, val - visibleSeconds.value * 0.2);
        clampPanOffset();
      }
      draw();
    },
  );

  watch(() => props.segments, draw, { deep: true });

  // Resize observer
  let resizeObserver: ResizeObserver | null = null;

  onMounted(async () => {
    await nextTick();
    draw();

    // Listen for global mouseup
    const onMouseUp = () => {
      isDragging = false;
    };
    window.addEventListener('mouseup', onMouseUp);

    resizeObserver = new ResizeObserver(() => draw());
    if (canvasWrapperRef.value) {
      resizeObserver.observe(canvasWrapperRef.value);
    }

    // Cleanup
    onUnmounted(() => {
      window.removeEventListener('mouseup', onMouseUp);
      resizeObserver?.disconnect();
      if (animationId) cancelAnimationFrame(animationId);
    });
  });

  defineExpose({ draw, zoomIn, zoomOut });
</script>

<style lang="less" scoped>
  .record-timeline {
    user-select: none;
  }

  .timeline-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .timeline-title {
    font-size: 13px;
    font-weight: 600;
    color: #2a3547;
  }

  .timeline-zoom {
    display: flex;
    align-items: center;
    gap: 8px;

    .zoom-label {
      font-size: 12px;
      color: #8c97a5;
      min-width: 28px;
      text-align: center;
    }
  }

  .timeline-canvas-wrapper {
    position: relative;
    cursor: crosshair;
    border-radius: 8px;
    overflow: hidden;

    canvas {
      display: block;
      width: 100%;
    }
  }

  .timeline-tooltip {
    position: absolute;
    top: -28px;
    transform: translateX(-50%);
    background: #2a3547;
    color: #fff;
    font-size: 11px;
    padding: 3px 8px;
    border-radius: 4px;
    pointer-events: none;
    white-space: nowrap;

    &::after {
      content: '';
      position: absolute;
      bottom: -4px;
      left: 50%;
      transform: translateX(-50%);
      border-left: 4px solid transparent;
      border-right: 4px solid transparent;
      border-top: 4px solid #2a3547;
    }
  }
</style>
