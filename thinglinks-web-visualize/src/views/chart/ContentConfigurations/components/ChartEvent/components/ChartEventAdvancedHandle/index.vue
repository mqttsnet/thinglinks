<template>
  <n-collapse-item title="高级事件配置" name="3">
    <template #header-extra>
      <n-button type="primary" tertiary size="small" @click.stop="showModal = true">
        <template #icon>
          <n-icon>
            <pencil-icon />
          </n-icon>
        </template>
        编辑
      </n-button>
    </template>
    <n-card class="collapse-show-box">
      <!-- 函数体 -->
      <div v-for="eventName in EventLife" :key="eventName">
        <p>
          <span class="func-annotate">// {{ EventLifeName[eventName] }}</span>
          <br />
          <span class="func-keyword">async {{ eventName }}</span> (e, components, echarts, node_modules) {
        </p>
        <p class="go-ml-4">
          <n-code :code="(targetData.events.advancedEvents || {})[eventName] || ''" language="typescript"></n-code>
        </p>
        <p>}<span>,</span></p>
      </div>
    </n-card>
  </n-collapse-item>

  <!-- 弹窗 -->
  <n-modal class="go-chart-data-monaco-editor" v-model:show="showModal" :mask-closable="false">
    <n-card :bordered="false" role="dialog" size="small" aria-modal="true" style="width: 1200px; height: 700px">
      <template #header>
        <n-space>
          <n-text>高级事件编辑器（配合源码使用）</n-text>
        </n-space>
      </template>

      <template #header-extra> </template>

      <n-layout has-sider sider-placement="right">
        <n-layout style="height: 580px; padding-right: 20px">
          <n-tabs v-model:value="editTab" type="card" tab-style="min-width: 100px;">
            <!-- 提示 -->
            <template #suffix>
              <n-text class="tab-tip" type="warning">提示: {{ EventLifeTip[editTab] }}</n-text>
            </template>
            <n-tab-pane
              v-for="(eventName, index) in EventLife"
              :key="index"
              :tab="`${EventLifeName[eventName]}-${eventName}`"
              :name="eventName"
            >
              <!-- 函数名称 -->
              <p class="go-pl-3">
                <span class="func-keyword">async function &nbsp;&nbsp;</span>
                <span class="func-keyNameWord">{{ eventName }}(e, components, echarts, node_modules)&nbsp;&nbsp;{</span>
              </p>
              <!-- 编辑主体 -->
              <monaco-editor v-model:modelValue="advancedEvents[eventName]" height="480px" language="javascript" />
              <!-- 函数结束 -->
              <p class="go-pl-3 func-keyNameWord">}</p>
            </n-tab-pane>
          </n-tabs>
        </n-layout>
        <n-layout-sider
          :collapsed-width="14"
          :width="340"
          show-trigger="bar"
          collapse-mode="transform"
          content-style="padding: 12px 12px 0px 12px;margin-left: 3px;"
        >
          <n-tabs default-value="1" justify-content="space-evenly" type="segment">
            <!-- 验证结果 -->
            <n-tab-pane tab="验证结果" name="1" size="small">
              <n-scrollbar trigger="none" style="max-height: 505px">
                <n-collapse class="go-px-3" arrow-placement="right" :default-expanded-names="[1, 2, 3]">
                  <template v-for="error in [validEvents()]" :key="error">
                    <n-collapse-item title="错误函数" :name="1">
                      <n-text depth="3">{{ error.errorFn || '暂无' }}</n-text>
                    </n-collapse-item>
                    <n-collapse-item title="错误信息" :name="2">
                      <n-text depth="3">{{ error.name || '暂无' }}</n-text>
                    </n-collapse-item>
                    <n-collapse-item title="堆栈信息" :name="3">
                      <n-text depth="3">{{ error.message || '暂无' }}</n-text>
                    </n-collapse-item>
                  </template>
                </n-collapse>
              </n-scrollbar>
            </n-tab-pane>
            <!-- 辅助说明 -->
            <n-tab-pane tab="变量说明" name="2">
              <n-scrollbar trigger="none" style="max-height: 505px">
                <n-collapse class="go-px-3" arrow-placement="right" :default-expanded-names="[1, 2, 3, 4]">
                  <n-collapse-item title="e" :name="1">
                    <n-text depth="3">触发对应生命周期事件时接收的参数</n-text>
                  </n-collapse-item>
                  <n-collapse-item title="this" :name="2">
                    <n-text depth="3">图表组件实例</n-text>
                    <br />
                    <n-tag class="go-m-1" v-for="prop in ['refs', 'setupState', 'ctx', 'props', '...']" :key="prop">{{
                      prop
                    }}</n-tag>
                  </n-collapse-item>
                  <n-collapse-item title="components" :name="3">
                    <n-text depth="3"
                      >当前大屏内所有组件的集合id 图表组件中的配置id，可以获取其他图表组件进行控制</n-text
                    >
                    <n-code :code="`{\n  [id]: component\n}`" language="typescript"></n-code>
                  </n-collapse-item>
                  <n-collapse-item title="node_modules" :name="4">
                    <n-text depth="3">以下是内置在代码环境中可用的包变量</n-text>
                    <br />
                    <n-tag class="go-m-1" v-for="pkg in Object.keys(npmPkgs || {})" :key="pkg">{{ pkg }}</n-tag>
                  </n-collapse-item>
                </n-collapse>
              </n-scrollbar>
            </n-tab-pane>
            <!-- 介绍案例 -->
            <n-tab-pane tab="介绍案例" name="3">
              <n-scrollbar trigger="none" style="max-height: 505px">
                <n-collapse arrow-placement="right">
                  <n-collapse-item
                    v-for="(item, index) in templateList"
                    :key="index"
                    :title="`案例${index + 1}：${item.description}`"
                    :name="index"
                  >
                    <n-code :code="item.code" language="typescript"></n-code>
                  </n-collapse-item>
                </n-collapse>
              </n-scrollbar>
            </n-tab-pane>
          </n-tabs>
        </n-layout-sider>
      </n-layout>

      <template #action>
        <n-space justify="space-between">
          <div class="go-flex-items-center">
            <n-tag :bordered="false" type="primary">
              <template #icon>
                <n-icon :component="DocumentTextIcon" />
              </template>
              说明
            </n-tag>
            <n-text class="go-ml-2" depth="2">通过提供的参数可为图表增加定制化的tooltip、交互事件等等</n-text>
          </div>

          <n-space>
            <n-button size="medium" @click="closeEvents">取消</n-button>
            <n-button size="medium" type="primary" @click="saveEvents">保存</n-button>
          </n-space>
        </n-space>
      </template>
    </n-card>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch, toRefs, toRaw } from 'vue'
import MonacoEditor from '@/components/Pages/MonacoEditor/index.vue'
import { useTargetData } from '../../../hooks/useTargetData.hook'
import { templateList } from './importTemplate'
import { npmPkgs } from '@/hooks'
import { icon } from '@/plugins'
import { CreateComponentType } from '@/packages/index.d'
import { EventLife } from '@/enums/eventEnum'

const { targetData, chartEditStore } = useTargetData()
const { DocumentTextIcon, ChevronDownIcon, PencilIcon } = icon.ionicons5

const EventLifeName = {
  [EventLife.VNODE_BEFORE_MOUNT]: '渲染之前',
  [EventLife.VNODE_MOUNTED]: '渲染之后'
}

const EventLifeTip = {
  [EventLife.VNODE_BEFORE_MOUNT]: '此时组件 DOM 还未存在',
  [EventLife.VNODE_MOUNTED]: '此时组件 DOM 已经存在'
}

// 受控弹窗
const showModal = ref(false)
// 编辑区域控制
const editTab = ref(EventLife.VNODE_MOUNTED)
// events 函数模板
let advancedEvents = ref({ ...targetData.value.events.advancedEvents })
// 事件错误标识
const errorFlag = ref(false)

// 验证语法
const validEvents = () => {
  let errorFn = ''
  let message = ''
  let name = ''

  errorFlag.value = Object.entries(advancedEvents.value).every(([eventName, str]) => {
    try {
      // 支持await，验证语法
      const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor
      new AsyncFunction(str)
      return true
    } catch (error: any) {
      message = error.message
      name = error.name
      errorFn = eventName
      return false
    }
  })
  return {
    errorFn,
    message,
    name
  }
}

// 关闭事件
const closeEvents = () => {
  showModal.value = false
}

// 新增事件
const saveEvents = () => {
  if (validEvents().errorFn) {
    window['$message'].error('事件函数错误，无法进行保存')
    return
  }
  if (Object.values(advancedEvents.value).join('').trim() === '') {
    // 清空事件
    targetData.value.events.advancedEvents = {
      vnodeBeforeMount: undefined,
      vnodeMounted: undefined
    }
  } else {
    targetData.value.events.advancedEvents = { ...advancedEvents.value }
  }
  closeEvents()
}

watch(
  () => showModal.value,
  (newData: boolean) => {
    if (newData) {
      advancedEvents.value = { ...targetData.value.events.advancedEvents }
    }
  }
)
</script>

<style lang="scss" scoped>
@import '../index.scss';
</style>
