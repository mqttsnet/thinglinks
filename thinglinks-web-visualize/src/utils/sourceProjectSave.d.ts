import type { ChartEditStorage } from '@/store/modules/chartEditStore/chartEditStore.d'

type ProjectResponse = {
  code?: number
  data?: {
    id?: string
  }
}

type SourceProjectSaveResult =
  | { ok: true; projectId: string }
  | {
      ok: false
      reason: 'invalid-identification' | 'fetch-failed' | 'invalid-project-id' | 'persist-failed'
    }

export function saveSourceProject(options: {
  detail: ChartEditStorage
  identification: string
  isMyProject: number
  fetchProject: (identification: string, isMyProject: number) => Promise<ProjectResponse>
  persistProject: (options: {
    projectId: string
    storageInfo: ChartEditStorage
    isMyProject: number
  }) => Promise<boolean>
  synchronizeWindow: (detail: ChartEditStorage) => void
}): Promise<SourceProjectSaveResult>
