/**
 * 根据 isMyProject 选择对应的接口
 * isMyProject = 1 时使用 myProject.ts 中的接口
 * isMyProject = 0 时使用 projectTemplate.ts 中的接口
 */
import { saveProject, updateProject, detailsProject, updateProjectStatus } from './myProject'
import { saveProjectTemplate, updateProjectTemplate, detailsProjectTemplate, updateProjectStatusTemplate } from './projectTemplate'

/**
 * 创建项目（新建）
 */
export const createProjectApi = async (data: object, isMyProject: number = 0) => {
  if (isMyProject === 1) {
    return saveProject(data)
  } else {
    return saveProjectTemplate(data)
  }
}

/**
 * 获取项目详情
 */
export const fetchProjectApi = async (data: { identification: string }, isMyProject: number = 0) => {
  if (isMyProject === 1) {
    return detailsProject(data)
  } else {
    return detailsProjectTemplate(data)
  }
}

/**
 * 更新项目
 */
export const updateProjectApi = async (data: object, isMyProject: number = 0) => {
  if (isMyProject === 1) {
    return updateProject(data)
  } else {
    return updateProjectTemplate(data)
  }
}

/**
 * 更新项目状态（发布/取消发布）
 */
export const changeProjectReleaseApi = async (data: { id: string; status: number }, isMyProject: number = 0) => {
  if (isMyProject === 1) {
    return updateProjectStatus(data)
  } else {
    return updateProjectStatusTemplate(data)
  }
}
