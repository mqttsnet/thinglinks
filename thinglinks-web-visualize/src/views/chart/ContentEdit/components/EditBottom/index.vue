<template>
  <div class="go-edit-bottom">
    <n-space>
      <!-- 历史记录 -->
      <edit-history></edit-history>
      <!-- CTRL按键触发展示 -->
      <n-text id="keyboard-dress-show" depth="3"></n-text>
      <n-divider vertical />
      <edit-data-sync></edit-data-sync>
    </n-space>

    <n-space class="bottom-ri">
      <!-- 快捷键提示 -->
      <edit-shortcut-key />

      <!-- 缩放比例 -->
      <n-select
        ref="selectInstRef"
        class="scale-btn"
        v-model:value="filterValue"
        size="mini"
        :disabled="lockScale"
        :options="filterOptions"
        @update:value="selectHandle"
      ></n-select>

      <!-- 锁定缩放 -->
      <n-tooltip trigger="hover">
        <template #trigger>
          <n-button @click="lockHandle" text>
            <n-icon class="lock-icon" :class="{ color: lockScale }" size="18" :depth="2">
              <lock-closed-outline-icon v-if="lockScale"></lock-closed-outline-icon>
              <lock-open-outline-icon v-else></lock-open-outline-icon>
            </n-icon>
          </n-button>
        </template>
        <span>{{ lockScale ? '解锁' : '锁定' }}当前比例</span>
      </n-tooltip>

      <!-- 拖动 -->
      <n-slider
        class="scale-slider"
        v-model:value="sliderValue"
        :default-value="50"
        :min="10"
        :max="200"
        :step="5"
        :format-tooltip="sliderFormatTooltip"
        :disabled="lockScale"
        :marks="sliderMaks"
        @update:value="sliderHandle"
      ></n-slider>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { SelectInst } from 'naive-ui'
import { reactive, ref, toRefs, watchEffect } from 'vue'
import { icon } from '@/plugins'
import { EditHistory } from '../EditHistory/index'
import { EditShortcutKey } from '../EditShortcutKey/index'
import { EditDataSync } from '../EditDataSync/index'
import { useDesignStore } from '@/store/modules/designStore/designStore'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { EditCanvasTypeEnum } from '@/store/modules/chartEditStore/chartEditStore.d'
const { LockClosedOutlineIcon, LockOpenOutlineIcon } = icon.ionicons5

const props = defineProps<{
  reset?: () => void
  zoomIn?: () => void
  zoomOut?: () => void
}>()

// 全局颜色
const designStore = useDesignStore()
const themeColor = ref(designStore.getAppTheme)
const chartEditStore = useChartEditStore()
const { lockScale, scale } = toRefs(chartEditStore.getEditCanvas)
const selectInstRef = ref<SelectInst | null>(null)

// 缩放选项
let filterOptions = [
  {
    label: '200%',
    value: 200
  },
  {
    label: '150%',
    value: 150
  },
  {
    label: '100%',
    value: 100
  },
  {
    label: '50%',
    value: 50
  },
  {
    label: '自适应',
    value: 0
  }
]

// 选择值
const filterValue = ref('')

// 用户自选择
const selectHandle = (v: number) => {
  selectInstRef.value?.blur()
  if (v === 0) {
    props.reset?.()
    return
  }
  chartEditStore.setScale(v / 100)
}

// 点击锁处理
const lockHandle = () => {
  chartEditStore.setEditCanvas(EditCanvasTypeEnum.LOCK_SCALE, !lockScale.value)
}

// 拖动
const sliderValue = ref(100)

// 拖动格式化
const sliderFormatTooltip = (v: string) => `${v}%`

// 拖动处理
const sliderHandle = (v: number) => {
  chartEditStore.setScale(v / 100)
}

const sliderMaks = reactive({
  100: ''
})

// 监听 scale 变化
watchEffect(() => {
  const value = (scale.value * 100).toFixed(0)
  filterValue.value = `${value}%`
  sliderValue.value = parseInt(value)
})
</script>

<style lang="scss" scoped>
$min-width: 500px;
$max-width: 670px;
@include go('edit-bottom') {
  width: 100%;
  min-width: $min-width;
  min-width: $max-width;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  width: 100%;
  min-width: $min-width;
  height: 40px;
  .bottom-ri {
    position: relative;
    top: 15px;
    .lock-icon {
      padding-top: 4px;
      &.color {
        color: v-bind('themeColor');
      }
    }
    .scale-btn {
      width: 90px;
      font-size: 12px;
      @include deep() {
        .n-base-selection-label {
          padding: 3px;
        }
      }
    }
    .scale-slider {
      position: relative;
      top: -4px;
      width: 100px;
    }
  }
}
</style>
