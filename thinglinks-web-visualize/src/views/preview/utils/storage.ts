import { getSessionStorage, fetchRouteParamsLocation, httpErrorHandle, JSONParse } from '@/utils'
import { ResultEnum } from '@/enums/httpEnum'
import { StorageEnum } from '@/enums/storageEnum'
import { ChartEditStorage } from '@/store/modules/chartEditStore/chartEditStore.d'
import { fetchProjectApi } from '@/api/thinglinks/view/projectApiSelector'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { useRoute } from 'vue-router'

const chartEditStore = useChartEditStore()

export interface ChartEditStorageType extends ChartEditStorage {
  id: string
}

// 根据路由 id 获取存储数据的信息
export const getSessionStorageInfo = async () => {
  const route = useRoute()
  const id = fetchRouteParamsLocation()
  const storageList: ChartEditStorageType[] = getSessionStorage(StorageEnum.GO_CHART_STORAGE_LIST)

  // 是否本地预览
  if (!storageList || storageList.findIndex(e => e.id === id.toString()) === -1) {
    // 获取 isMyProject 参数，如果没有则默认为 0
    const isMyProject = route.query?.isMyProject ? Number(route.query.isMyProject) : 0
    // 接口调用
    const res = await fetchProjectApi({ identification: id }, isMyProject)
    if (res && res.code === ResultEnum.DATA_SUCCESS) {
      const { content, status, state } = res.data
      if ((status ?? state) === -1) {
        // 跳转未发布页
        return { isRelease: false }
      }
      if (!content) {
        httpErrorHandle()
        return
      }
      const parseData = { ...JSONParse(content), id }
      const { editCanvasConfig, requestGlobalConfig, componentList } = parseData
      chartEditStore.editCanvasConfig = editCanvasConfig
      chartEditStore.requestGlobalConfig = requestGlobalConfig
      chartEditStore.componentList = componentList
      return parseData
    } else {
      httpErrorHandle()
    }
  } else {
    // 本地读取
    for (let i = 0; i < storageList.length; i++) {
      if (id.toString() === storageList[i]['id']) {
        const { editCanvasConfig, requestGlobalConfig, componentList } = storageList[i]
        chartEditStore.editCanvasConfig = editCanvasConfig
        chartEditStore.requestGlobalConfig = requestGlobalConfig
        chartEditStore.componentList = componentList
        return storageList[i]
      }
    }
  }
}
