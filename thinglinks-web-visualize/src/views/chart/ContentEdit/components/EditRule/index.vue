<template>
  <div class="go-sketch-rule">
    <sketch-rule
      v-if="sketchRuleReDraw"
      ref="sketchRuleRef"
      :thick="thick"
      :scale="scale"
      :width="canvasBox().width"
      :height="canvasBox().height"
      :canvasWidth="width"
      :canvasHeight="height"
      :lines="lines"
      :palette="paletteStyle"
      :isShowReferLine="true"
      :shadow="shadow"
      @zoomchange="handleZoomChange"
    >
      <template #default>
        <div ref="refSketchRuleBox" class="canvas" :style="canvasStyle" @mousedown="dragCanvas">
          <slot></slot>
        </div>
      </template>
    </sketch-rule>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, toRefs, watch, onUnmounted, computed } from 'vue'
import type { CSSProperties } from 'vue'
import { listen } from 'dom-helpers'
import { useDesignStore } from '@/store/modules/designStore/designStore'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { CreateComponentGroupType } from '@/packages/index.d'
import throttle from 'lodash/throttle'

const chartEditStore = useChartEditStore()
const designStore = useDesignStore()

const thick = 20

const sketchRuleRef = ref()
const sketchRuleReDraw = ref(false)
const isPressSpace = ref(false)
const cursorStyle = ref('auto')
const { width, height } = toRefs(chartEditStore.getEditCanvasConfig)
const lines = reactive({ h: [], v: [] })

const scale = computed(() => chartEditStore.getEditCanvas.scale)

// 防止 panzoom 与 store 互相触发造成循环
let isUpdatingFromPanzoom = false
watch(
  () => chartEditStore.getEditCanvas.scale,
  newScale => {
    if (isUpdatingFromPanzoom) {
      isUpdatingFromPanzoom = false
      return
    }
    const panzoom = sketchRuleRef.value?.panzoomInstance
    if (panzoom && Math.abs(panzoom.getScale() - newScale) > 0.001) {
      panzoom.zoom(newScale)
    }
  }
)

// 主题
const paletteStyle = computed(() => {
  const isDarkTheme = designStore.getDarkTheme
  return isDarkTheme
    ? {
        bgColor: 'transparent',
        longfgColor: '#4d4d4d',
        shortfgColor: '#4d4d4d',
        fontColor: '#4d4d4d',
        shadowColor: '#232324',
        borderColor: '#18181c',
        cornerActiveColor: '#18181c'
      }
    : { bgColor: 'transparent' }
})

// 颜色
const themeColor = computed(() => {
  return designStore.getAppTheme
})
const canvasStyle = computed((): CSSProperties => {
  return {
    pointerEvents: isPressSpace.value ? ('none' as const) : ('auto' as const),
    width: `${width.value}px`,
    height: `${height.value}px`
  }
})
// 根据 id 查找组件（含分组内子组件）
const findComponentById = (id: string) => {
  for (const item of chartEditStore.getComponentList) {
    if (item.id === id) return item
    if (item.isGroup) {
      const child = (item as CreateComponentGroupType).groupList.find(c => c.id === id)
      if (child) return child
    }
  }
  return null
}

// 阴影（画布在标尺坐标系中的位置，从 thick 偏移开始）
const shadow = computed(() => {
  const selectIds = chartEditStore.getTargetChart.selectId
  if (!selectIds.length) {
    return { x: 0, y: 0, width: 0, height: 0 }
  }

  let minX = Infinity
  let minY = Infinity
  let maxX = -Infinity
  let maxY = -Infinity

  for (const id of selectIds) {
    const comp = findComponentById(id)
    if (comp) {
      const { x, y, w, h } = comp.attr
      minX = Math.min(minX, x)
      minY = Math.min(minY, y)
      maxX = Math.max(maxX, x + w)
      maxY = Math.max(maxY, y + h)
    }
  }

  if (minX === Infinity) {
    return { x: 0, y: 0, width: 0, height: 0 }
  }

  return {
    x: minX,
    y: minY,
    width: maxX - minX,
    height: maxY - minY
  }
})

// 拖拽处理
const dragCanvas = (e: any) => {
  e.preventDefault()
  e.stopPropagation()

  if (e.which == 2) isPressSpace.value = true
  else if (!window.$KeyboardActive?.space) return
  // @ts-ignore
  document.activeElement?.blur()

  const listenMouseup = listen(window, 'mouseup', () => {
    listenMouseup()
    isPressSpace.value = false
  })
}

// 计算画布大小
const canvasBox = () => {
  const layoutDom = document.getElementById('go-chart-edit-layout')
  if (layoutDom) {
    // 此处减去滚动条的宽度和高度
    const scrollW = 20
    return {
      height: layoutDom.clientHeight - scrollW,
      width: layoutDom.clientWidth
    }
  }
  return {
    width: width.value,
    height: height.value
  }
}

// 重绘标尺
const reDraw = throttle(() => {
  sketchRuleReDraw.value = false
  setTimeout(() => {
    sketchRuleReDraw.value = true
  }, 10)
}, 20)

// 处理主题变化
watch(
  () => designStore.getDarkTheme,
  () => {
    reDraw()
  }
)

// 处理鼠标样式
watch(
  () => isPressSpace.value,
  newValue => {
    cursorStyle.value = newValue ? 'grab' : 'auto'
  }
)

const handleZoomChange = (detail: any) => {
  isUpdatingFromPanzoom = true
  chartEditStore.setScale(detail.scale)
}

onMounted(() => {
  // 防止 canvasBox() 拿不准尺寸
  sketchRuleReDraw.value = true
  window.onKeySpacePressHold = (isHold: boolean) => {
    isPressSpace.value = isHold
  }
})

onUnmounted(() => {
  window.onKeySpacePressHold = undefined
})

const reset = () => sketchRuleRef.value?.reset?.()
const zoomIn = () => sketchRuleRef.value?.zoomIn?.()
const zoomOut = () => sketchRuleRef.value?.zoomOut?.()

defineExpose({
  reset,
  zoomIn,
  zoomOut
})
</script>

<style>
/* 横线 */
.sketch-ruler .v-container .lines .line {
  /* 最大缩放 200% */
  width: 200vw !important;
  border-top: 1px dashed v-bind('themeColor') !important;
}

.sketch-ruler .v-container .indicator {
  border-bottom: 1px dashed v-bind('themeColor') !important;
}

/* 竖线 */
.sketch-ruler .h-container .lines .line {
  /* 最大缩放 200% */
  height: 200vh !important;
  border-left: 1px dashed v-bind('themeColor') !important;
}

.sketch-ruler .h-container .indicator {
  border-left: 1px dashed v-bind('themeColor') !important;
}

/* 坐标数值背景颜色 */
.sketch-ruler .indicator .value {
  background-color: rgba(0, 0, 0, 0);
}

/* 删除按钮 */
.sketch-ruler .line .del {
  padding: 0;
  color: v-bind('themeColor');
  font-size: 26px;
  font-weight: bolder;
}

.sketch-ruler .corner {
  border-width: 0 !important;
}
</style>

<style lang="scss" scoped>
@include go('sketch-rule') {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 100%;

  .canvas {
    position: absolute;
    top: 0;
    left: 0;

    &:hover {
      cursor: v-bind('cursorStyle');
    }

    &:active {
      cursor: crosshair;
    }
  }
}
</style>
