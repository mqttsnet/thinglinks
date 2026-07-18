<template>
  <v-chart ref="vChartRef" :init-options="initOptions" :theme="themeColor" :option="option.value" :manual-update="isPreview()" autoresize>
  </v-chart>
</template>

<script setup lang="ts">
import { PropType, reactive, watch, shallowRef, nextTick } from 'vue'
import config, { includes } from './config'
import VChart from 'vue-echarts'
import { useCanvasInitOptions } from '@/hooks/useCanvasInitOptions.hook'
import { use, registerMap } from 'echarts/core'
import { EffectScatterChart, MapChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { useChartDataFetch } from '@/hooks'
import { mergeTheme, setOption } from '@/packages/public/chart'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { isPreview } from '@/utils'
import mapJsonWithoutHainanIsLands from './mapWithoutHainanIsLands.json'
import { DatasetComponent, GridComponent, TooltipComponent, GeoComponent, VisualMapComponent } from 'echarts/components'

const props = defineProps({
  themeSetting: {
    type: Object,
    required: true
  },
  themeColor: {
    type: Object,
    required: true
  },
  chartConfig: {
    type: Object as PropType<config>,
    required: true
  }
})

const initOptions = useCanvasInitOptions(props.chartConfig.option, props.themeSetting)

use([
  MapChart,
  DatasetComponent,
  CanvasRenderer,
  GridComponent,
  TooltipComponent,
  GeoComponent,
  EffectScatterChart,
  VisualMapComponent
])

const option = reactive({
  value: mergeTheme(props.chartConfig.option, props.themeSetting, includes)
})
const vChartRef = shallowRef<InstanceType<typeof VChart>>()

//动态获取json注册地图
const getGeojson = (regionId: string) => {
  return new Promise<boolean>(resolve => {
    import(`./mapGeojson/${regionId}.json`).then(data => {
      registerMap(regionId, { geoJSON: data.default as any, specialAreas: {} })
      resolve(true)
    })
  })
}

//异步时先注册空的 保证初始化不报错
registerMap(`${props.chartConfig.option.mapRegion.adcode}`, { geoJSON: {} as any, specialAreas: {} })

// 进行更换初始化地图 如果为china 单独处理
const registerMapInitAsync = async () => {
  await nextTick()
  const adCode = `${props.chartConfig.option.mapRegion.adcode}`;
  if (adCode !== 'china') {
    await getGeojson(adCode)
  } else {
    await hainanLandsHandle(props.chartConfig.option.mapRegion.showHainanIsLands)
  }
  vEchartsSetOption()
}
registerMapInitAsync()

// 手动触发渲染
const vEchartsSetOption = () => {
  option.value = props.chartConfig.option
  setOption(vChartRef.value, props.chartConfig.option)
}

// 更新数据处理
const dataSetHandle = async (dataset: any) => {
  props.chartConfig.option.series.forEach((item: any) => {
    if (item.type === 'effectScatter' && dataset.point) item.data = dataset.point
    else if (item.type === 'map' && dataset.map) item.data = dataset.map
  })
  if (dataset.pieces) props.chartConfig.option.visualMap.pieces = dataset.pieces

  isPreview() && vEchartsSetOption()
}
// 处理海南群岛
const hainanLandsHandle = async (newData: boolean) => {
  if (newData) {
    await getGeojson('china')
  } else {
    registerMap('china', { geoJSON: mapJsonWithoutHainanIsLands as any, specialAreas: {} })
  }
}
//监听 dataset 数据发生变化
watch(
  () => props.chartConfig.option.dataset,
  newData => {
    dataSetHandle(newData)
  },
  {
    immediate: true,
    deep: false
  }
)

//监听是否显示南海群岛
watch(
  () => props.chartConfig.option.mapRegion.showHainanIsLands,
  async newData => {
    try {
      await hainanLandsHandle(newData)
      vEchartsSetOption()
    } catch (error) {
      console.log(error)
    }
  },
  {
    deep: false
  }
)

//监听地图展示区域发生变化
watch(
  () => `${props.chartConfig.option.mapRegion.adcode}`,
  async newData => {
    try {
      await getGeojson(newData)
      props.chartConfig.option.geo.map = newData
      props.chartConfig.option.series.forEach((item: any) => {
        if (item.type === 'map') item.map = newData
      })
      vEchartsSetOption()
    } catch (error) {
      console.log(error)
    }
  },
  {
    deep: false
  }
)

// 预览
useChartDataFetch(props.chartConfig, useChartEditStore, (newData: any) => {
  dataSetHandle(newData)
})
</script>
