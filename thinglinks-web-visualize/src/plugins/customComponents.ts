import type { App } from 'vue'
import { GoSkeleton } from '@/components/GoSkeleton'
import { GoLoading } from '@/components/GoLoading'
import SketchRule from 'vue3-sketch-ruler'

/**
 * 全局注册自定义组件
 * @param app
 */
export function setupCustomComponents(app: App) {
  // 骨架屏
  app.component('GoSkeleton', GoSkeleton)
  // 加载
  app.component('GoLoading', GoLoading)
  // 标尺
  app.component('SketchRule', SketchRule)
}
