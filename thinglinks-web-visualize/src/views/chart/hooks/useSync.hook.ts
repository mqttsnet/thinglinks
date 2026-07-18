import { getCurrentInstance, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import html2canvas from 'html2canvas'
import {
  dataURLToBlob,
  fetchRouteParamsLocation,
  getUUID,
  httpErrorHandle,
  JSONParse,
  JSONStringify
} from '@/utils'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { EditCanvasTypeEnum, ChartEditStoreEnum, ProjectInfoEnum, ChartEditStorage } from '@/store/modules/chartEditStore/chartEditStore.d'
import { EditCanvasConfigEnum } from '@/store/modules/chartEditStore/chartEditStore.d'
import { useChartHistoryStore } from '@/store/modules/chartHistoryStore/chartHistoryStore'
import { useChartLayoutStore } from '@/store/modules/chartLayoutStore/chartLayoutStore'
import { ChartLayoutStoreEnum } from '@/store/modules/chartLayoutStore/chartLayoutStore.d'
import { fetchChartComponent, fetchConfigComponent, createComponent } from '@/packages/index'
import { saveInterval } from '@/settings/designSetting'
import throttle from 'lodash/throttle'
// 接口状态
import { ResultEnum } from '@/enums/httpEnum'
// 接口
import { uploadFile, getIndexImage } from '@/api/path'
import { fetchProjectApi, updateProjectApi } from '@/api/thinglinks/view/projectApiSelector'
// 画布枚举
import { SyncEnum } from '@/enums/editPageEnum'
import { CreateComponentType, CreateComponentGroupType } from '@/packages/index.d'
import { BaseEvent, EventLife } from '@/enums/eventEnum'
import { PublicGroupConfigClass } from '@/packages/public/publicConfig'
import merge from 'lodash/merge'
import type { ProjectDetailsVO } from '@/api/thinglinks/view/projectModel'

/**
 * * 画布-版本升级对旧数据无法兼容的补丁
 * @param object
 */
const canvasVersionUpdatePolyfill = (object: any) => {
  return object
}

/**
 * * 组件-版本升级对旧数据无法兼容的补丁
 * @param newObject
 * @param sources
 */
const componentVersionUpdatePolyfill = (newObject: any, sources: any) => {
  try {
    // 判断是否是组件
    if (sources.id) {
      // 处理事件补丁
      const hasVnodeBeforeMount = 'vnodeBeforeMount' in sources.events
      const hasVnodeMounted = 'vnodeMounted' in sources.events

      if (hasVnodeBeforeMount) {
        newObject.events.advancedEvents.vnodeBeforeMount = sources?.events.vnodeBeforeMount
      }
      if (hasVnodeMounted) {
        newObject.events.advancedEvents.vnodeMounted = sources?.events.vnodeMounted
      }
      if (hasVnodeBeforeMount || hasVnodeMounted) {
        sources.events = {
          baseEvent: {
            [BaseEvent.ON_CLICK]: undefined,
            [BaseEvent.ON_DBL_CLICK]: undefined,
            [BaseEvent.ON_MOUSE_ENTER]: undefined,
            [BaseEvent.ON_MOUSE_LEAVE]: undefined
          },
          advancedEvents: {
            [EventLife.VNODE_MOUNTED]: undefined,
            [EventLife.VNODE_BEFORE_MOUNT]: undefined
          },
          interactEvents: []
        }
      }
      return newObject
    }
  } catch (error) {
    return newObject
  }
}

/**
 * * 合并处理
 * @param newObject 新的模板数据
 * @param sources 新拿到的数据
 * @returns object
 */
const componentMerge = (newObject: any, sources: any, notComponent = false) => {
  // 处理组件补丁
  componentVersionUpdatePolyfill(newObject, sources)

  // 非组件不处理
  if (notComponent) return merge(newObject, sources)
  // 组件排除 newObject
  const option = sources.option
  if (!option) return merge(newObject, sources)

  // 为 undefined 的 sources 来源对象属性将被跳过详见 https://www.lodashjs.com/docs/lodash.merge
  sources.option = undefined
  if (option) {
    return {
      ...merge(newObject, sources),
      option: option
    }
  }
}

// 请求处理
export const useSync = () => {
  const route = useRoute()
  const chartEditStore = useChartEditStore()
  const chartHistoryStore = useChartHistoryStore()
  const chartLayoutStore = useChartLayoutStore()
  /**
   * * 组件动态注册
   * @param projectData 项目数据
   * @param isReplace 是否替换数据
   * @returns
   */
  const updateComponent = async (projectData: ChartEditStorage, isReplace = false, changeId = false) => {
    if (isReplace) {
      // 清除列表
      chartEditStore.componentList = []
      // 清除历史记录
      chartHistoryStore.clearBackStack()
      chartHistoryStore.clearForwardStack()
    }
    // 画布补丁处理
    projectData.editCanvasConfig = canvasVersionUpdatePolyfill(projectData.editCanvasConfig)

    // 列表组件注册
    projectData.componentList.forEach(async (e: CreateComponentType | CreateComponentGroupType) => {
      const intComponent = (target: CreateComponentType) => {
        if (!window['$vue'].component(target.chartConfig.chartKey)) {
          window['$vue'].component(target.chartConfig.chartKey, fetchChartComponent(target.chartConfig))
          window['$vue'].component(target.chartConfig.conKey, fetchConfigComponent(target.chartConfig))
        }
      }

      if (e.isGroup) {
        (e as CreateComponentGroupType).groupList.forEach(groupItem => {
          intComponent(groupItem)
        })
      } else {
        intComponent(e as CreateComponentType)
      }
    })

    // 创建函数-重新创建是为了处理类种方法消失的问题
    const create = async (
      _componentInstance: CreateComponentType,
      callBack?: (componentInstance: CreateComponentType) => void
    ) => {
      // 补充 class 上的方法
      let newComponent: CreateComponentType = await createComponent(_componentInstance.chartConfig)
      if (_componentInstance.chartConfig.redirectComponent) {
        _componentInstance.chartConfig.dataset && (newComponent.option.dataset = _componentInstance.chartConfig.dataset)
        newComponent.chartConfig.title = _componentInstance.chartConfig.title
        newComponent.chartConfig.chartFrame = _componentInstance.chartConfig.chartFrame
      }
      if (callBack) {
        if (changeId) {
          callBack(componentMerge(newComponent, { ..._componentInstance, id: getUUID() }))
        } else {
          callBack(componentMerge(newComponent, _componentInstance))
        }
      } else {
        if (changeId) {
          chartEditStore.addComponentList(
            componentMerge(newComponent, { ..._componentInstance, id: getUUID() }),
            false,
            true
          )
        } else {
          chartEditStore.addComponentList(componentMerge(newComponent, _componentInstance), false, true)
        }
      }
    }

    // 数据赋值
    for (const key in projectData) {
      // 组件
      if (key === ChartEditStoreEnum.COMPONENT_LIST) {
        let loadIndex = 0
        const listLength = projectData[key].length
        for (const comItem of projectData[key]) {
          // 设置加载数量
          let percentage = parseInt((parseFloat(`${++loadIndex / listLength}`) * 100).toString())
          chartLayoutStore.setItemUnHandle(ChartLayoutStoreEnum.PERCENTAGE, percentage)
          // 判断类型
          if (comItem.isGroup) {
            // 创建分组
            let groupClass = new PublicGroupConfigClass()
            if (changeId) {
              groupClass = componentMerge(groupClass, { ...comItem, id: getUUID() })
            } else {
              groupClass = componentMerge(groupClass, comItem)
            }

            // 异步注册子应用
            const targetList: CreateComponentType[] = []
            for (const groupItem of (comItem as CreateComponentGroupType).groupList) {
              await create(groupItem, e => {
                targetList.push(e)
              })
            }
            groupClass.groupList = targetList

            // 分组插入到列表
            chartEditStore.addComponentList(groupClass, false, true)
          } else {
            await create(comItem as CreateComponentType)
          }
          if (percentage === 100) {
            // 清除历史记录
            chartHistoryStore.clearBackStack()
            chartHistoryStore.clearForwardStack()
          }
        }
      } else {
        // 非组件(顺便排除脏数据)
        if (key !== 'editCanvasConfig' && key !== 'requestGlobalConfig') return
        componentMerge(chartEditStore[key], projectData[key], true)
      }
    }

    // 清除数量
    chartLayoutStore.setItemUnHandle(ChartLayoutStoreEnum.PERCENTAGE, 0)
  }

  /**
   * * 赋值全局数据
   * @param projectData 项目数据
   * @returns
   */
  const updateStoreInfo = async (projectData: ProjectDetailsVO) => {
    const {
      id,
      projectName = '',
      templateName = '',
      templateIdentification = '',
      remark = '',
      indexImage = '',
      indexImageId = '',
      status = projectData.state ?? -1,
      projectIdentification = ''
    } = projectData

    // ID
    chartEditStore.setProjectInfo(ProjectInfoEnum.PROJECT_ID, id)
    // 名称
    chartEditStore.setProjectInfo(ProjectInfoEnum.PROJECT_NAME, projectName)
    // 模板名称
    chartEditStore.setProjectInfo(ProjectInfoEnum.TEMPLATE_NAME, templateName)
    // 项目标识
    chartEditStore.setProjectInfo(ProjectInfoEnum.PROJECT_IDENTIFICATION, projectIdentification)
    // 模板标识
    chartEditStore.setProjectInfo(ProjectInfoEnum.TEMPLATE_IDENTIFICATION, templateIdentification)
    // 描述
    chartEditStore.setProjectInfo(ProjectInfoEnum.REMARKS, remark)
    // 缩略图
    chartEditStore.setProjectInfo(ProjectInfoEnum.THUMBNAIL, indexImage)
    // 发布
    chartEditStore.setProjectInfo(ProjectInfoEnum.RELEASE, status === 1)
    // 背景图片
    // 因接口原因首先根据背景图片Id获取背景图片地址
    if (indexImageId) {
      try {
        const getIndexImageRes = await getIndexImage([indexImageId])
        if (getIndexImageRes?.code === ResultEnum.DATA_SUCCESS) {
          chartEditStore.setEditCanvasConfig(
            EditCanvasConfigEnum.BACKGROUND_IMAGE,
            getIndexImageRes.data[indexImageId]
          )
        }
      } catch (error) {
        console.error('获取项目背景图失败', error)
        window['$message'].warning('项目已加载，但背景图获取失败')
      }
    }
  }

  // * 数据获取
  const dataSyncFetch = async () => {
    // FIX:重新执行dataSyncFetch需清空chartEditStore.componentList,否则会导致图层重复
    // 切换语言等操作会导致重新执行 dataSyncFetch,此时pinia中并未清空chartEditStore.componentLis
    // t，导致图层重复
    chartEditStore.componentList = []
    chartEditStore.setEditCanvas(EditCanvasTypeEnum.SAVE_STATUS, SyncEnum.START)
    let succeeded = false

    try {
      // 优先从 query 参数中获取 identification 和 isMyProject
      const identification = (route.query?.identification as string) || fetchRouteParamsLocation()
      const isMyProject = route.query?.isMyProject ? Number(route.query.isMyProject) : 0
      const res = await fetchProjectApi({ identification }, isMyProject)
      if (res && res.code === ResultEnum.DATA_SUCCESS) {
        await updateStoreInfo(res.data)
        // 更新全局数据
        res.data.content && (await updateComponent(JSONParse(res.data.content)))
        succeeded = true
        return
      }
      httpErrorHandle()
    } catch (error) {
      console.error('获取项目数据失败', error)
      httpErrorHandle()
    } finally {
      chartEditStore.setEditCanvas(
        EditCanvasTypeEnum.SAVE_STATUS,
        succeeded ? SyncEnum.SUCCESS : SyncEnum.FAILURE
      )
    }
  }

  // * 数据保存
  const persistData = async (
    updateImg = true,
    storageInfo: ChartEditStorage = chartEditStore.getStorageInfo,
    projectId = chartEditStore.getProjectInfo[ProjectInfoEnum.PROJECT_ID],
    isMyProjectOverride?: number,
    notifyFailure = true
  ): Promise<boolean> => {
    const isMyProject =
      isMyProjectOverride ?? (route.query?.isMyProject ? Number(route.query.isMyProject) : 0)

    if (!projectId) {
      chartEditStore.setEditCanvas(EditCanvasTypeEnum.SAVE_STATUS, SyncEnum.FAILURE)
      if (notifyFailure) window['$message'].error('项目数据未初始化成功，请刷新页面')
      return false
    }

    chartEditStore.setEditCanvas(EditCanvasTypeEnum.SAVE_STATUS, SyncEnum.START)

    // 缩略图上传失败不影响项目 JSON 保存。
    try {
      let indexImageId: string | undefined
      if (updateImg) {
        try {
          const range = document.querySelector<HTMLElement>('.go-edit-range')
          if (!range) throw new Error('未找到可视化编辑区域')

          const canvasImage = await html2canvas(range, {
            backgroundColor: null,
            allowTaint: true,
            useCORS: true
          })
          const file = new File(
            [dataURLToBlob(canvasImage.toDataURL('image/png'))],
            `${getUUID()}.png`,
            { type: 'image/png' }
          )
          const uploadRes = await uploadFile({
            file,
            bizType: 'DEF__VIEW__AVATAR'
          })
          if (uploadRes?.code !== ResultEnum.DATA_SUCCESS || !uploadRes.data?.id) {
            throw new Error(uploadRes?.msg || '缩略图上传接口返回失败')
          }
          indexImageId = uploadRes.data.id
        } catch (error) {
          console.error('更新项目缩略图失败', error)
          window['$message'].warning('缩略图更新失败，项目数据仍会继续保存')
        }
      }

      const params = new FormData()
      params.append('id', projectId)
      if (indexImageId) params.append('indexImageId', indexImageId)
      params.append('content', JSONStringify(storageInfo))
      params.append('projectName', storageInfo.editCanvasConfig?.projectName || '')

      const res = await updateProjectApi(params, isMyProject)
      if (!res || res.code !== ResultEnum.DATA_SUCCESS) {
        throw new Error(res?.msg || '保存接口返回失败')
      }

      chartEditStore.setEditCanvas(EditCanvasTypeEnum.SAVE_STATUS, SyncEnum.SUCCESS)
      return true
    } catch (error) {
      console.error('保存项目数据失败', error)
      chartEditStore.setEditCanvas(EditCanvasTypeEnum.SAVE_STATUS, SyncEnum.FAILURE)
      if (notifyFailure) window['$message'].error('项目数据保存失败，请稍后重试')
      return false
    }
  }

  const throttledDataSyncUpdate = throttle(persistData, 3000)

  if (getCurrentInstance()) {
    onUnmounted(() => {
      throttledDataSyncUpdate.cancel()
    })
  }

  // JSON 编辑器保存需要立即完成并可被 await，不进入缩略图自动保存的节流窗口。
  const dataSyncUpdate = (
    updateImg = true,
    storageInfo: ChartEditStorage = chartEditStore.getStorageInfo,
    projectId = chartEditStore.getProjectInfo[ProjectInfoEnum.PROJECT_ID],
    isMyProjectOverride?: number,
    notifyFailure = true
  ) =>
    updateImg
      ? throttledDataSyncUpdate(
          true,
          storageInfo,
          projectId,
          isMyProjectOverride,
          notifyFailure
        )
      : persistData(false, storageInfo, projectId, isMyProjectOverride, notifyFailure)

  // * 定时处理
  const intervalDataSyncUpdate = () => {
    // 定时获取数据
    const syncTiming = setInterval(() => {
      dataSyncUpdate()
    }, saveInterval * 1000)

    // 销毁
    onUnmounted(() => {
      clearInterval(syncTiming)
    })
  }

  return {
    updateComponent,
    updateStoreInfo,
    dataSyncFetch,
    dataSyncUpdate,
    intervalDataSyncUpdate
  }
}
