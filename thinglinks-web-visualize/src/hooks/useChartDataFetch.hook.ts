import { onUnmounted, shallowRef, toRefs, toRaw, watch } from 'vue'
import type { WatchStopHandle } from 'vue'
import type VChart from 'vue-echarts'
import { customizeHttp } from '@/api/http'
import { useChartDataPondFetch } from '@/hooks/'
import { CreateComponentType, ChartFrameEnum } from '@/packages/index.d'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { RequestDataTypeEnum } from '@/enums/httpEnum'
import { isPreview, newFunctionHandle, intervalUnitHandle } from '@/utils'
import { setOption } from '@/packages/public/chart'

// 获取类型
type ChartEditStoreType = typeof useChartEditStore

/**
 * setdata 数据监听与更改
 * @param targetComponent
 * @param useChartEditStore 若直接引会报错，只能动态传递
 * @param updateCallback 自定义更新函数
 */
export const useChartDataFetch = (
  targetComponent: CreateComponentType,
  useChartEditStore: ChartEditStoreType,
  updateCallback?: (...args: any) => any
) => {
  const vChartRef = shallowRef<InstanceType<typeof VChart> | null>(null)
  let fetchInterval: ReturnType<typeof setInterval> | undefined
  let stopRequestWatch: WatchStopHandle | undefined

  // 数据池
  const { addGlobalDataInterface } = useChartDataPondFetch()

  // 组件类型
  const { chartFrame } = targetComponent.chartConfig

  // eCharts 组件配合 vChart 库更新方式
  const echartsUpdateHandle = (dataset: any) => {
    if (chartFrame === ChartFrameEnum.ECHARTS) {
      if (vChartRef.value) {
        setOption(vChartRef.value, { dataset: dataset })
      }
    }
  }

  const requestIntervalFn = () => {
    const chartEditStore = useChartEditStore()

    // 全局数据
    const {
      requestOriginUrl,
      requestIntervalUnit: globalUnit,
      requestInterval: globalRequestInterval
    } = toRefs(chartEditStore.getRequestGlobalConfig)

    // 目标组件
    const {
      requestDataType,
      requestUrl,
      requestIntervalUnit: targetUnit,
      requestInterval: targetInterval
    } = toRefs(targetComponent.request)

    // 非请求类型
    if (requestDataType.value !== RequestDataTypeEnum.AJAX) return

    // 处理地址
    if (requestUrl?.value) {
      // requestOriginUrl 允许为空
      const completePath = `${requestOriginUrl?.value || ''}${requestUrl.value}`
      if (!completePath) return

      if (fetchInterval) clearInterval(fetchInterval)
      stopRequestWatch?.()

      const fetchFn = async () => {
        try {
          const res = await customizeHttp(toRaw(targetComponent.request), toRaw(chartEditStore.getRequestGlobalConfig))
          if (res) {
            const filter = targetComponent.filter
            const { data } = res
            const nextData = newFunctionHandle(data, res, filter)
            echartsUpdateHandle(nextData)
            // 更新回调函数
            if (updateCallback) await updateCallback(nextData)
          }
        } catch (error) {
          // 轮询请求必须自行收口，避免产生未处理的 Promise rejection。
          console.error('获取图表数据失败', error)
        }
      }

      // 普通初始化与组件交互处理监听
      stopRequestWatch = watch(
        () => targetComponent.request,
        () => {
          void fetchFn()
        },
        {
          immediate: true,
          deep: true
        }
      )

      // 定时时间
      const targetRequestInterval = targetInterval?.value
      const time = targetRequestInterval || globalRequestInterval.value
      // 单位
      const unit = targetRequestInterval ? targetUnit.value : globalUnit.value
      // 开启轮询
      if (time) fetchInterval = setInterval(() => void fetchFn(), intervalUnitHandle(time, unit))
    }
  }

  if (isPreview()) {
    // 判断是否是数据池类型
    targetComponent.request.requestDataType === RequestDataTypeEnum.Pond
      ? addGlobalDataInterface(targetComponent, updateCallback || echartsUpdateHandle)
      : requestIntervalFn()
  }

  onUnmounted(() => {
    if (fetchInterval) clearInterval(fetchInterval)
    stopRequestWatch?.()
  })

  return { vChartRef }
}
