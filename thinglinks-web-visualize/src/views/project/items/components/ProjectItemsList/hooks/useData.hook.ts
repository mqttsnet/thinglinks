import { ref, reactive } from 'vue'
import { goDialog, httpErrorHandle } from '@/utils'
import { DialogEnum } from '@/enums/pluginEnum'
import { Chartype, ChartList } from '../../../index.d'
import type { ProjectListFilter, ProjectPageParams } from '@/api/thinglinks/view/projectModel'
import { ResultEnum } from '@/enums/httpEnum'
// api
import { page, delProject, updateProjectStatus } from '@/api/thinglinks/view/myProject'
import { pageTemplate, delProjectTemplate, updateProjectStatusTemplate } from '@/api/thinglinks/view/projectTemplate'
import { getIndexImage } from '@/api/path'

// 项目类型枚举
export type ProjectType = 'myProject' | 'projectTemplate'

// API 接口映射类型
interface ProjectApiMap {
  page: (data: ProjectPageParams) => ReturnType<typeof page>
  delete: (data: { id: string }) => ReturnType<typeof delProject>
  updateStatus: (data: { id: string; status: number }) => ReturnType<typeof updateProjectStatus>
}

// 根据项目类型获取对应的 API 接口
const getProjectApi = (type: ProjectType): ProjectApiMap => {
  // 这里可以根据 type 返回不同的 API 接口
  // 目前接口相同，但预留扩展空间
  if (type === 'myProject') {
    // 我的项目接口
    return {
      page: data => page(data),
      delete: (data: { id: string }) => delProject(data),
      updateStatus: (data: { id: string; status: number }) => updateProjectStatus(data)
    }
  } else {
    // 项目模版接口
    return {
      page: data => pageTemplate(data),
      delete: (data: { id: string }) => delProjectTemplate(data),
      updateStatus: (data: { id: string; status: number }) => updateProjectStatusTemplate(data)
    }
  }
}

// 数据初始化
export const useDataListInit = (projectType: ProjectType = 'myProject') => {
  // 根据项目类型获取对应的 API
  const api = getProjectApi(projectType)
  const loading = ref(true)

  const paginat = reactive({
    // 当前页数
    page: 1,
    // 每页值
    limit: 12,
    // 总数
    count: 10
  })

  // TODO: 接口联调  待封装
  const list = ref<ChartList>([])

  // 筛选参数
  const filterParams = ref<ProjectListFilter>({})

  // 设置筛选参数
  const setFilterParams = (params: ProjectListFilter) => {
    filterParams.value = params
  }

  // 数据请求
  const getList = async () => {
    loading.value = true
    try {
      const { data } = await api.page({
        current: paginat.page,
        size: paginat.limit,
        model: {
          ...filterParams.value
        }
      })

      if (!data?.records) {
        list.value = []
        paginat.count = 0
        return
      }

      paginat.count = data.total
      list.value = data.records

      const ids: string[] = []
      data.records.forEach(item => {
        if (item.indexImageId) {
          ids.push(item.indexImageId)
        }
      })

      if (ids.length) {
        const indexImageList = await getIndexImage(ids)
        list.value = data.records.map(item => {
          const indexImage = item.indexImageId ? indexImageList?.data[item.indexImageId] : undefined
          if (indexImageList?.code === ResultEnum.DATA_SUCCESS && indexImage) {
            return { ...item, indexImage }
          }
          return item
        })
      }
    } catch (error) {
      console.error('获取项目列表失败', error)
      httpErrorHandle()
    } finally {
      loading.value = false
    }
  }

  // 修改页数
  const changePage = (_page: number) => {
    paginat.page = _page
    void getList()
  }

  // 修改大小
  const changeSize = (_size: number) => {
    paginat.limit = _size
    void getList()
  }
  
  // 删除处理
  const deleteHandle = (cardData: Chartype) => {
    goDialog({
      type: DialogEnum.DELETE,
      promise: true,
      onPositiveCallback: () => api.delete({ id: cardData.id }),
      promiseResCallback: (res: any) => {
        if (res.code === ResultEnum.DATA_SUCCESS) {
          window['$message'].success(window['$t']('global.r_delete_success'))
          void getList()
          return
        }
        httpErrorHandle()
      },
      promiseRejCallback: (error: unknown) => {
        console.error('删除项目失败', error)
        httpErrorHandle()
      }
    })
  }

  // 发布处理
  const releaseHandle = async (cardData: Chartype, _index: number) => {
    const { id, status } = cardData
    try {
      const res = await api.updateStatus({
        id,
        // [-1未发布, 1发布]
        status: status > 0 ? -1 : 1
      })
      if (res && res.code === ResultEnum.DATA_SUCCESS) {
        list.value = []
        await getList()
        // 发布 -> 未发布
        if (status > 0) {
          window['$message'].success(window['$t']('global.r_unpublish_success'))
          return
        }
        // 未发布 -> 发布
        window['$message'].success(window['$t']('global.r_publish_success'))
        return
      }
      httpErrorHandle()
    } catch (error) {
      console.error('更新项目发布状态失败', error)
      httpErrorHandle()
    }
  }


  return {
    loading,
    paginat,
    list,
    getList,
    releaseHandle,
    changeSize,
    changePage,
    deleteHandle,
    setFilterParams
  }
}
