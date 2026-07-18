<template>
  <div class="go-edit-range go-transition" @mousedown="mousedownBoxSelect($event, undefined)">
    <slot></slot>
    <!-- 水印 -->
    <edit-watermark></edit-watermark>
    <!-- 拖拽时的辅助线 -->
    <edit-align-line></edit-align-line>
    <!-- 框选时的样式框 -->
    <edit-select></edit-select>
    <!-- 拖拽时的遮罩 -->
    <div class="go-edit-range-model" :style="rangeModelStyle"></div>
  </div>
</template>

<script setup lang="ts">
import { toRefs, computed } from 'vue'
import { useSizeStyle } from '../../hooks/useStyle.hook'
import { canvasModelIndex } from '@/settings/designSetting'
import { mousedownBoxSelect } from '../../hooks/useDrag.hook'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { EditAlignLine } from '../EditAlignLine'
import { EditWatermark } from '../EditWatermark'
import { EditSelect } from '../EditSelect'

const chartEditStore = useChartEditStore()

const { getEditCanvasConfig, getEditCanvas } = toRefs(chartEditStore)

const size = computed(() => {
  return {
    w: getEditCanvasConfig.value.width,
    h: getEditCanvasConfig.value.height
  }
})

// 模态层
const rangeModelStyle = computed(() => {
  const dragStyle = getEditCanvas.value.isCreate && { 'z-index': 99999 }
  // @ts-ignore
  return { ...useSizeStyle(size.value), ...dragStyle }
})
</script>

<style lang="scss" scoped>
@include go(edit-range) {
  height: 100%;
  position: relative;
  transform-origin: left top;
  background-size: cover;
  border-radius: 10px;
  overflow: hidden;
  @include fetch-border-color('hover-border-color');
  @include fetch-bg-color('background-color2');
  @include go(edit-range-model) {
    z-index: -1;
    position: absolute;
    left: 0;
    top: 0;
  }
}
</style>
