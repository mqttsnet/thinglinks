export interface ProjectResultVO {
  id: string
  status: number
  createdBy?: string
  createdOrgId?: string
  createdTime?: string
  indexImage?: string
  indexImageId?: string
  projectIdentification?: string
  projectName?: string
  templateIdentification?: string
  templateName?: string
  remark?: string
}

export interface ProjectDetailsVO {
  id: string
  status?: number
  state?: number
  content?: string
  indexImage?: string
  indexImageId?: string
  projectIdentification?: string
  projectName?: string
  templateIdentification?: string
  templateName?: string
  remark?: string
}

export interface ProjectPageResultVO {
  records: ProjectResultVO[]
  total: number
}

export interface ProjectListFilter {
  projectName?: string
  projectIdentification?: string
  status?: number
}

export interface ProjectPageParams {
  current: number
  size: number
  model: ProjectListFilter
}
