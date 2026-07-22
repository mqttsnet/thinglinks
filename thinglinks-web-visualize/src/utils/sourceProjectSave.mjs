const DATA_SUCCESS = 0

/**
 * 保存源码编辑器本次解析的数据，并在持久化成功后同步编辑窗口。
 */
export async function saveSourceProject({
  detail,
  identification,
  isMyProject,
  fetchProject,
  persistProject,
  synchronizeWindow
}) {
  if (typeof identification !== 'string' || !identification.trim()) {
    return { ok: false, reason: 'invalid-identification' }
  }

  const projectResponse = await fetchProject(identification, isMyProject)
  if (projectResponse?.code !== DATA_SUCCESS) {
    return { ok: false, reason: 'fetch-failed' }
  }

  const projectId = projectResponse?.data?.id
  if (typeof projectId !== 'string' || !projectId.trim()) {
    return { ok: false, reason: 'invalid-project-id' }
  }

  const persisted = await persistProject({
    projectId,
    storageInfo: detail,
    isMyProject
  })
  if (!persisted) {
    return { ok: false, reason: 'persist-failed' }
  }

  synchronizeWindow(detail)
  return { ok: true, projectId }
}
