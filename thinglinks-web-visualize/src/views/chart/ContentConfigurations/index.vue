<template>
  <n-layout has-sider sider-placement="right">
    <n-layout-content>
      <!-- 图表拖拽区域 -->
      <content-edit></content-edit>
    </n-layout-content>
    <n-layout-sider
      collapse-mode="transform"
      :collapsed-width="0"
      :width="350"
      :collapsed="collapsed"
      :native-scrollbar="false"
      show-trigger="bar"
      @collapse="collapsedHandle"
      @expand="expandHandle"
    >
      <content-box class="go-content-configurations go-boderbox" :show-top="false" :depth="2">
        <!-- 页面配置 -->
        <n-tabs v-if="!selectTarget" class="tabs-box" size="small" type="segment">
          <n-tab-pane
            v-for="item in globalTabList"
            :key="item.key"
            :name="item.key"
            size="small"
            display-directive="show:lazy"
          >
            <template #tab>
              <n-space>
                <span>{{ item.title }}</span>
                <n-icon size="16" class="icon-position">
                  <component :is="item.icon"></component>
                </n-icon>
              </n-space>
            </template>
            <component :is="item.render"></component>
          </n-tab-pane>
        </n-tabs>

        <!-- 编辑 -->
        <n-tabs v-if="selectTarget" v-model:value="tabsSelect" class="tabs-box" size="small" type="segment">
          <n-tab-pane
            v-for="item in selectTarget.isGroup ? chartsDefaultTabList : chartsTabList"
            :key="item.key"
            :name="item.key"
            size="small"
            display-directive="show:lazy"
          >
            <template #tab>
              <n-space>
                <span>{{ item.title }}</span>
                <n-icon size="16" class="icon-position">
                  <component :is="item.icon"></component>
                </n-icon>
              </n-space>
            </template>
            <component :is="item.render"></component>
          </n-tab-pane>
        </n-tabs>
      </content-box>
    </n-layout-sider>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, toRefs, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { icon } from '@/plugins'
import { loadAsyncComponent } from '@/utils'
import { ContentBox } from '../ContentBox/index'
import { TabsEnum } from './index.d'
import { useChartLayoutStore } from '@/store/modules/chartLayoutStore/chartLayoutStore'
import { ChartLayoutStoreEnum } from '@/store/modules/chartLayoutStore/chartLayoutStore.d'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'

const { t } = useI18n()
const { getDetails } = toRefs(useChartLayoutStore())
const { setItem } = useChartLayoutStore()
const chartEditStore = useChartEditStore()

const { ConstructIcon, FlashIcon, DesktopOutlineIcon, LeafIcon, RocketIcon } = icon.ionicons5

const ContentEdit = loadAsyncComponent(() => import('../ContentEdit/index.vue'))
const CanvasPage = loadAsyncComponent(() => import('./components/CanvasPage/index.vue'))
const ChartSetting = loadAsyncComponent(() => import('./components/ChartSetting/index.vue'))
const ChartData = loadAsyncComponent(() => import('./components/ChartData/index.vue'))
const ChartEvent = loadAsyncComponent(() => import('./components/ChartEvent/index.vue'))
const ChartAnimation = loadAsyncComponent(() => import('./components/ChartAnimation/index.vue'))

const collapsed = ref<boolean>(getDetails.value)
const tabsSelect = ref<TabsEnum>(TabsEnum.CHART_SETTING)

const collapsedHandle = () => {
  collapsed.value = true
  setItem(ChartLayoutStoreEnum.DETAILS, true)
}

const expandHandle = () => {
  collapsed.value = false
  setItem(ChartLayoutStoreEnum.DETAILS, false)
}

const selectTarget = computed(() => {
  const selectId = chartEditStore.getTargetChart.selectId
  // 排除多个
  if (selectId.length !== 1) return undefined
  const target = chartEditStore.componentList[chartEditStore.fetchTargetIndex()]
  if (target?.isGroup) {
    // eslint-disable-next-line vue/no-side-effects-in-computed-properties
    tabsSelect.value = TabsEnum.CHART_SETTING
  }
  return target
})

watch(getDetails, newData => {
  if (newData) {
    collapsedHandle()
  } else {
    expandHandle()
  }
})

// 页面设置
const globalTabList = computed(() => [
  {
    key: TabsEnum.PAGE_SETTING,
    title: t('project.page_config'),
    icon: DesktopOutlineIcon,
    render: CanvasPage
  }
])

const chartsDefaultTabList = computed(() => [
  {
    key: TabsEnum.CHART_SETTING,
    title: t('project.customize'),
    icon: ConstructIcon,
    render: ChartSetting
  },
  {
    key: TabsEnum.CHART_ANIMATION,
    title: t('project.animation'),
    icon: LeafIcon,
    render: ChartAnimation
  }
])

const chartsTabList = computed(() => [
  ...chartsDefaultTabList.value,
  {
    key: TabsEnum.CHART_DATA,
    title: t('project.data'),
    icon: FlashIcon,
    render: ChartData
  },
  {
    key: TabsEnum.CHART_EVENT,
    title: t('project.event'),
    icon: RocketIcon,
    render: ChartEvent
  }
])
</script>

<style lang="scss" scoped>
::v-deep .n-layout-sider {
  z-index: 9;
}
@include go(content-configurations) {
  overflow: hidden;
  .tabs-box {
    padding: 10px;
    .icon-position {
      padding-top: 2px;
    }
  }
}
</style>
