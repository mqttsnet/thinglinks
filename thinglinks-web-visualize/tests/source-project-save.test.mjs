import assert from 'node:assert/strict'
import fs from 'node:fs'
import test from 'node:test'

import { saveSourceProject } from '../src/utils/sourceProjectSave.mjs'

test('源码保存使用详情接口返回的数据库 ID 和本次解析内容', async () => {
  const editedDetail = {
    editCanvasConfig: { projectName: '本次编辑' },
    componentList: [{ id: 'chart-new' }],
    requestGlobalConfig: { requestOriginUrl: '' }
  }
  const persisted = []
  const synchronized = []

  const result = await saveSourceProject({
    detail: editedDetail,
    identification: 'public-identification',
    isMyProject: 1,
    fetchProject: async () => ({ code: 0, data: { id: 'database-project-id' } }),
    persistProject: async payload => {
      persisted.push(payload)
      return true
    },
    synchronizeWindow: detail => synchronized.push(detail)
  })

  assert.deepEqual(result, { ok: true, projectId: 'database-project-id' })
  assert.deepEqual(persisted, [
    {
      projectId: 'database-project-id',
      storageInfo: editedDetail,
      isMyProject: 1
    }
  ])
  assert.deepEqual(synchronized, [editedDetail])
})

test('后端持久化失败时返回失败且不覆盖源窗口', async () => {
  let synchronized = false

  const result = await saveSourceProject({
    detail: { componentList: [{ id: 'unsaved' }] },
    identification: 'public-identification',
    isMyProject: 0,
    fetchProject: async () => ({ code: 0, data: { id: 'database-project-id' } }),
    persistProject: async () => false,
    synchronizeWindow: () => {
      synchronized = true
    }
  })

  assert.deepEqual(result, { ok: false, reason: 'persist-failed' })
  assert.equal(synchronized, false)
})

test('详情接口未返回有效数据库 ID 时停止保存', async () => {
  let persisted = false

  const result = await saveSourceProject({
    detail: { componentList: [] },
    identification: 'public-identification',
    isMyProject: 1,
    fetchProject: async () => ({ code: 0, data: { id: '   ' } }),
    persistProject: async () => {
      persisted = true
      return true
    },
    synchronizeWindow: () => {}
  })

  assert.deepEqual(result, { ok: false, reason: 'invalid-project-id' })
  assert.equal(persisted, false)
})

test('编辑页相关定时器、键盘监听和保存节流器在卸载时释放', () => {
  const contentEditSource = fs.readFileSync(
    new URL('../src/views/chart/ContentEdit/index.vue', import.meta.url),
    'utf8'
  )
  const useSyncSource = fs.readFileSync(
    new URL('../src/views/chart/hooks/useSync.hook.ts', import.meta.url),
    'utf8'
  )
  const loginSource = fs.readFileSync(new URL('../src/views/login/index.vue', import.meta.url), 'utf8')

  assert.match(contentEditSource, /onUnmounted\(\(\)\s*=>\s*\{[\s\S]*useRemoveKeyboard\(\)/)
  assert.match(useSyncSource, /throttledDataSyncUpdate\.cancel\(\)/)
  assert.match(loginSource, /onUnmounted\(\(\)\s*=>\s*\{[\s\S]*clearInterval\([\s\S]*clearTimeout\(/)
})
