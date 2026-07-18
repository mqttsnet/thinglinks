<template>
  <div class="go-edit">
    <n-layout>
      <n-layout-header class="go-edit-header go-px-5 go-flex-items-center" bordered>
        <div>
          <n-text class="go-edit-title go-mr-4">{{ $t('global.editor_title') }}</n-text>
          <n-button v-if="showOpenFilePicker" class="go-mr-3" size="medium" @click="importJSON">
            <template #icon>
              <n-icon>
                <download-icon></download-icon>
              </n-icon>
            </template>
            {{ $t('global.import') }}
          </n-button>
        </div>
        <n-space>
          <!-- 暂时关闭 -->
          <!-- <n-tag :bordered="false" type="warning"> 「页面失焦保存」 </n-tag> -->
          <n-tag :bordered="false" type="warning"> {{ $t('global.ctrl_s_update_view') }} </n-tag>
          <n-button v-if="showOpenFilePicker" class="go-mr-3" size="medium" @click="updateSync">
            <template #icon>
              <n-icon>
                <analytics-icon></analytics-icon>
              </n-icon>
            </template>
            {{ $t('global.save') }}
          </n-button>
        </n-space>
      </n-layout-header>
      <n-layout-content>
        <monaco-editor
          v-model:modelValue="content"
          language="json"
          :editorOptions="{
            lineNumbers: 'on',
            minimap: { enabled: true }
          }"
          />
      </n-layout-content>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import MonacoEditor from '@/components/Pages/MonacoEditor/index.vue'
import { SavePageEnum } from '@/enums/editPageEnum'
import { getSessionStorageInfo } from '../preview/utils'
import {
  fetchRouteParamsLocation,
  goDialog,
  JSONParse,
  JSONStringify,
  saveSourceProject,
  setSessionStorage,
  setTitle
} from '@/utils'
import { StorageEnum } from '@/enums/storageEnum'
import { icon } from '@/plugins'
import { useSync } from '@/views/chart/hooks/useSync.hook'
import { fetchProjectApi } from '@/api/thinglinks/view/projectApiSelector'
import type { ChartEditStorage } from '@/store/modules/chartEditStore/chartEditStore.d'
import type { ChartEditStorageType } from '../preview/index.d'

const { t } = useI18n()
const route = useRoute()
const { dataSyncUpdate } = useSync()

const { ChevronBackOutlineIcon, DownloadIcon, AnalyticsIcon } = icon.ionicons5
const showOpenFilePicker: Function = (window as any).showOpenFilePicker
const content = ref('')

window['$message'].warning(t('global.do_not_refresh_window'))

// 从sessionStorage 获取数据
async function getDataBySession() {
  const localStorageInfo: ChartEditStorageType = (await getSessionStorageInfo()) as unknown as ChartEditStorageType
  setTitle(`${t('global.r_edit')}-${localStorageInfo.editCanvasConfig.projectName}`)
  content.value = JSONStringify(localStorageInfo)
}
const initialDataTimer = setTimeout(getDataBySession)

// 返回父窗口
function back() {
  window.opener.name = Date.now()
  window.open(window.opener.location.href, window.opener.name)
}

// 导入json文本
function importJSON() {
  goDialog({
    message: t('global.import_data_confirm'),
    isMaskClosable: true,
    transformOrigin: 'center',
    onPositiveCallback: async () => {
      try {
        const files = await showOpenFilePicker()
        const file = await files[0].getFile()
        const fr = new FileReader()
        fr.readAsText(file)
        fr.onloadend = () => {
          content.value = (fr.result || '').toString()
        }
        window['$message'].success(t('global.import_success'))
      } catch (error) {
        window['$message'].error(t('global.import_failed'))
        console.log(error)
      }
    }
  })
}

// 同步数据编辑页
const sourceUpdateHandler = (event: Event) => {
  const detail = (event as CustomEvent<ChartEditStorage>).detail
  window['$message'].success(t('global.updating'))
  setSessionStorage(StorageEnum.GO_CHART_STORAGE_LIST, [detail])
  content.value = JSONStringify(detail)
}
window.opener?.addEventListener(SavePageEnum.CHART, sourceUpdateHandler)

// 保存按钮同步数据
const saveShortcutHandler = (e: KeyboardEvent) => {
  if (e.keyCode == 83 && (navigator.platform.match('Mac') ? e.metaKey : e.ctrlKey)) {
    e.preventDefault()
    updateSync()
  }
}
document.addEventListener('keydown', saveShortcutHandler)

// 失焦保存（暂时关闭）
// addEventListener('blur', updateSync)

// 同步更新
async function updateSync() {
  if (!window.opener) {
    return window['$message'].error(t('global.source_window_closed'))
  }
  goDialog({
    message: t('global.overwrite_source_confirm'),
    isMaskClosable: true,
    transformOrigin: 'center',
    onPositiveCallback: async () => {
      let detail: ChartEditStorage
      try {
        const parsedDetail = JSONParse(content.value)
        if (!parsedDetail || typeof parsedDetail !== 'object' || Array.isArray(parsedDetail)) {
          throw new Error('源码内容不是项目对象')
        }
        const { id: _sessionId, ...projectStorage } = parsedDetail
        detail = projectStorage as ChartEditStorage
      } catch (error) {
        window['$message'].error(t('global.content_format_error'))
        console.error(error)
        return
      }

      try {
        const identification =
          (route.query?.identification as string) || fetchRouteParamsLocation()
        const isMyProject = route.query?.isMyProject ? Number(route.query.isMyProject) : 0
        const result = await saveSourceProject({
          detail,
          identification,
          isMyProject,
          fetchProject: (currentIdentification, currentIsMyProject) =>
            fetchProjectApi({ identification: currentIdentification }, currentIsMyProject),
          persistProject: async ({ projectId, storageInfo, isMyProject: currentIsMyProject }) =>
            (await dataSyncUpdate(
              false,
              storageInfo,
              projectId,
              currentIsMyProject,
              false
            )) === true,
          synchronizeWindow: savedDetail => {
            window.opener?.dispatchEvent(
              new CustomEvent(SavePageEnum.JSON, { detail: savedDetail })
            )
          }
        })

        if (!result.ok) {
          const messageKey = result.reason.includes('invalid')
            ? 'global.source_project_invalid'
            : 'global.source_save_failed'
          window['$message'].error(t(messageKey))
          return
        }
        window['$message'].success(t('global.source_save_success'))
      } catch (error) {
        console.error('保存源码编辑内容失败', error)
        window['$message'].error(t('global.source_save_failed'))
      }
    }
  })
}

// 关闭页面发送关闭事件
const closeHandler = () => {
  if (window.opener) {
    window.opener.dispatchEvent(new CustomEvent(SavePageEnum.CLOSE))
  }
}
window.addEventListener('beforeunload', closeHandler)

onUnmounted(() => {
  clearTimeout(initialDataTimer)
  document.removeEventListener('keydown', saveShortcutHandler)
  window.removeEventListener('beforeunload', closeHandler)
  window.opener?.removeEventListener(SavePageEnum.CHART, sourceUpdateHandler)
})
</script>

<style lang="scss" scoped>
.go-edit {
  display: flex;
  flex-direction: column;
  height: 100vh;
  .go-edit-header {
    display: flex;
    align-items: center;
    height: 60px;
    justify-content: space-between;
    .go-edit-title {
      position: relative;
      bottom: 3px;
      font-size: 18px;
      font-weight: bold;
    }
  }
  @include deep() {
    .go-editor-area {
      height: calc(100vh - 60px) !important;
    }
  }
}
</style>
