import { onUnmounted, toRaw } from 'vue'
import { customizeHttp } from '@/api/http'
import { CreateComponentType } from '@/packages/index.d'
import { RequestGlobalConfigType, RequestDataPondItemType } from '@/store/modules/chartEditStore/chartEditStore.d'
import { newFunctionHandle, intervalUnitHandle } from '@/utils'

// 数据池存储的数据类型
type DataPondMapType = {
  updateCallback: (...args: any) => any
  filter?: string | undefined
}

// 数据池 Map 中请求对应 callback
const mittDataPondMap = new Map<string, DataPondMapType[]>()

// 创建单个数据项轮询接口
const newPondItemInterval = (
  requestGlobalConfig: RequestGlobalConfigType,
  requestDataPondItem: RequestDataPondItemType,
  dataPondMapItem?: DataPondMapType[]
) => {
  if (!dataPondMapItem) return
  let fetchInterval: ReturnType<typeof setInterval> | undefined

  // 请求
  const fetchFn = async () => {
    try {
      const res = await customizeHttp(toRaw(requestDataPondItem.dataPondRequestConfig), toRaw(requestGlobalConfig))

      if (res) {
        // 同步和异步回调都纳入统一异常处理。
        await Promise.all(
          dataPondMapItem.map(item =>
            Promise.resolve(item.updateCallback(newFunctionHandle(res.data, res, item.filter)))
          )
        )
      }
    } catch (error) {
      console.error('获取数据池数据失败', error)
    }
  }

  // 立即调用
  void fetchFn()

  const targetInterval = requestDataPondItem.dataPondRequestConfig.requestInterval
  const targetUnit = requestDataPondItem.dataPondRequestConfig.requestIntervalUnit

  const globalRequestInterval = requestGlobalConfig.requestInterval
  const globalUnit = requestGlobalConfig.requestIntervalUnit

  // 定时时间
  const time = targetInterval  ? targetInterval : globalRequestInterval
  // 单位
  const unit = targetInterval  ? targetUnit : globalUnit
  // 开启轮询
  if (time) fetchInterval = setInterval(() => void fetchFn(), intervalUnitHandle(time, unit))

  return () => {
    if (fetchInterval) clearInterval(fetchInterval)
  }
}

/**
 * 数据池接口处理
 */
export const useChartDataPondFetch = () => {
  const stopIntervals: Array<() => void> = []

  const stopAllIntervals = () => {
    stopIntervals.splice(0).forEach(stop => stop())
  }

  // 新增全局接口
  const addGlobalDataInterface = (
    targetComponent: CreateComponentType,
    updateCallback: (...args: any) => any
  ) => {
    // 组件对应的数据池 Id
    const requestDataPondId = targetComponent.request.requestDataPondId as string
    // 新增数据项
    const mittPondIdArr = mittDataPondMap.get(requestDataPondId) || []
    mittPondIdArr.push({
      updateCallback: updateCallback,
      filter: targetComponent.filter
    })
    mittDataPondMap.set(requestDataPondId, mittPondIdArr)
  }

  // 清除旧数据
  const clearMittDataPondMap = () => {
    stopAllIntervals()
    mittDataPondMap.clear()
  }

  // 初始化数据池
  const initDataPond = (requestGlobalConfig: RequestGlobalConfigType) => {
    stopAllIntervals()
    const { requestDataPond } = requestGlobalConfig
    // 根据 mapId 查找对应的数据池配置
    for (let pondKey of mittDataPondMap.keys()) {
      const requestDataPondItem = requestDataPond.find(item => item.dataPondId === pondKey)
      if (requestDataPondItem) {
        const stop = newPondItemInterval(requestGlobalConfig, requestDataPondItem, mittDataPondMap.get(pondKey))
        if (stop) stopIntervals.push(stop)
      }
    }
  }

  onUnmounted(stopAllIntervals)

  return {
    addGlobalDataInterface,
    clearMittDataPondMap,
    initDataPond
  }
}
