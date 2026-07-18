<template>
  <content-box
    id="go-chart-edit-layout"
    :flex="true"
    :showTop="false"
    :showBottom="true"
    :depth="1"
    :xScroll="true"
    :disabledScroll="true"
    @mousedown="mousedownHandleUnStop"
    @drop="dragHandle"
    @dragover="dragoverHandle"
    @dragenter="dragoverHandle"
  >
    <edit-rule ref="editRuleRef">
      <!-- 画布主体 -->
      <div id="go-chart-edit-content" @contextmenu="handleContextMenu">
        <!-- 展示 -->
        <edit-range>
          <!-- 滤镜预览 -->
          <div
            :style="{
              ...getFilterStyle(chartEditStore.getEditCanvasConfig),
              ...rangeStyle
            }"
          >
            <!-- 图表 -->
            <div v-for="(item, index) in chartEditStore.getComponentList" :key="item.id">
              <!-- 分组 -->
              <edit-group
                v-if="item.isGroup"
                :groupData="item as CreateComponentGroupType"
                :groupIndex="index"
              ></edit-group>

              <!-- 单组件 -->
              <edit-shape-box
                v-else
                :data-id="item.id"
                :index="index"
                :style="{
                  ...useComponentStyle(item.attr, index),
                  ...(getBlendModeStyle(item.styles) as any),
                }"
                :item="item"
                @click="mouseClickHandle($event, item)"
                @mousedown="mousedownHandle($event, item)"
                @mouseenter="mouseenterHandle($event, item)"
                @mouseleave="mouseleaveHandle($event, item)"
                @contextmenu="handleContextMenu($event, item, optionsHandle)"
              >
                <component
                  class="edit-content-chart"
                  :class="animationsClass(item.styles.animations)"
                  :is="item.chartConfig.chartKey"
                  :chartConfig="item"
                  :themeSetting="themeSetting"
                  :themeColor="themeColor"
                  :style="{
                    ...useSizeStyle(item.attr),
                    ...getFilterStyle(item.styles),
                    ...getTransformStyle(item.styles)
                  }"
                ></component>
              </edit-shape-box>
            </div>
          </div>
        </edit-range>
      </div>
    </edit-rule>

    <!-- 工具栏 -->
    <template #aside>
      <edit-tools></edit-tools>
    </template>

    <!-- 底部控制 -->
    <template #bottom>
      <EditBottom
        :reset="editRuleMethods.reset"
        :zoom-in="editRuleMethods.zoomIn"
        :zoom-out="editRuleMethods.zoomOut"
      ></EditBottom>
    </template>
  </content-box>
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted, computed, provide, ref } from 'vue'
import { chartColors } from '@/settings/chartThemes/index'
import { MenuEnum } from '@/enums/editPageEnum'
import { CreateComponentType, CreateComponentGroupType } from '@/packages/index.d'
import { animationsClass, getFilterStyle, getTransformStyle, getBlendModeStyle, colorCustomMerge } from '@/utils'
import { useContextMenu } from '@/views/chart/hooks/useContextMenu.hook'
import { MenuOptionsItemType } from '@/views/chart/hooks/useContextMenu.hook.d'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { SCALE_KEY } from '@/views/preview/hooks/useScale.hook'
import { useLayout } from './hooks/useLayout.hook'
import { useAddKeyboard, useRemoveKeyboard } from '../hooks/useKeyboard.hook'
import { useSync } from '../hooks/useSync.hook'
import { dragHandle, dragoverHandle, mousedownHandleUnStop, useMouseHandle } from './hooks/useDrag.hook'
import { useComponentStyle, useSizeStyle } from './hooks/useStyle.hook'

import { ContentBox } from '../ContentBox/index'
import { EditGroup } from './components/EditGroup'
import { EditRange } from './components/EditRange'
import { EditRule } from './components/EditRule'
import { EditBottom } from './components/EditBottom'
import { EditShapeBox } from './components/EditShapeBox'
import { EditTools } from './components/EditTools'

const chartEditStore = useChartEditStore()
const { handleContextMenu } = useContextMenu()
const { dataSyncFetch, dataSyncUpdate, intervalDataSyncUpdate } = useSync()

// 编辑时注入scale变量，消除警告
provide(SCALE_KEY, null)

const editRuleRef = ref()
const editRuleMethods = {
  reset: () => editRuleRef.value?.reset?.(),
  zoomIn: () => editRuleRef.value?.zoomIn?.(),
  zoomOut: () => editRuleRef.value?.zoomOut?.()
}

// 布局处理
useLayout(async () => {})

// 点击事件
const { mouseenterHandle, mouseleaveHandle, mousedownHandle, mouseClickHandle } = useMouseHandle()

// 右键事件
const optionsHandle = (
  targetList: MenuOptionsItemType[],
  allList: MenuOptionsItemType[],
  targetInstance: CreateComponentType
) => {
  // 多选处理
  if (chartEditStore.getTargetChart.selectId.length > 1) {
    return allList.filter(i => [MenuEnum.GROUP, MenuEnum.DELETE].includes(i.key as MenuEnum))
  }
  const statusMenuEnums: MenuEnum[] = []
  if (targetInstance.status.lock) {
    statusMenuEnums.push(MenuEnum.LOCK)
  } else {
    statusMenuEnums.push(MenuEnum.UNLOCK)
  }
  if (targetInstance.status.hide) {
    statusMenuEnums.push(MenuEnum.HIDE)
  } else {
    statusMenuEnums.push(MenuEnum.SHOW)
  }
  return targetList.filter(i => !statusMenuEnums.includes(i.key as MenuEnum))
}

// 主题色
const themeSetting = computed(() => {
  const chartThemeSetting = chartEditStore.getEditCanvasConfig.chartThemeSetting
  return chartThemeSetting
})

// 配置项
const themeColor = computed(() => {
  const colorCustomMergeData = colorCustomMerge(chartEditStore.getEditCanvasConfig.chartCustomThemeColorInfo)
  return colorCustomMergeData[chartEditStore.getEditCanvasConfig.chartThemeColor]
})

// 是否展示渲染
const filterShow = computed(() => {
  return chartEditStore.getEditCanvasConfig.filterShow
})

// 背景
const rangeStyle = computed(() => {
  // 设置背景色和图片背景
  const background = chartEditStore.getEditCanvasConfig.background
  const backgroundImage = chartEditStore.getEditCanvasConfig.backgroundImage
  const selectColor = chartEditStore.getEditCanvasConfig.selectColor
  const backgroundColor = background ? background : undefined

  const computedBackground = selectColor
    ? { background: backgroundColor }
    : { background: `url(${backgroundImage}) no-repeat center center / cover !important` }

  // @ts-ignore
  return {
    ...computedBackground,
    width: 'inherit',
    height: 'inherit'
  }
})

onMounted(() => {
  // 键盘事件
  useAddKeyboard(() => dataSyncUpdate())
  // 获取数据
  dataSyncFetch()
  // 定时更新数据
  intervalDataSyncUpdate()
})

onUnmounted(() => {
  useRemoveKeyboard()
})
</script>

<style lang="scss" scoped>
@include goId('chart-edit-layout') {
  position: relative;
  width: 100%;
  overflow: hidden;
  @extend .go-point-bg;
  @include background-image('background-point');

  @include goId('chart-edit-content') {
    overflow: hidden;
    height: 100%;
    @extend .go-transition;
    @include fetch-theme('box-shadow');

    .edit-content-chart {
      position: absolute;
      overflow: hidden;
    }
  }
}
</style>
