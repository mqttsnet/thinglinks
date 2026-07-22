<template>
  <n-space class="go-mt-0" :wrap="false">
    <n-button v-for="item in comBtnList" :key="item.key" :type="item.type()" ghost @click="item.event">
      <template #icon>
        <component :is="item.icon"></component>
      </template>
      <span>{{ item.title() }}</span>
    </n-button>
  </n-space>

  <!-- 发布管理弹窗 -->
  <n-modal v-model:show="modelShow" @afterLeave="closeHandle">
    <n-list bordered class="go-system-setting">
      <template #header>
        <n-space justify="space-between">
          <n-h3 class="go-mb-0">{{ $t('project.release_management') }}</n-h3>
          <n-icon size="20" class="go-cursor-pointer" @click="closeHandle">
            <close-icon></close-icon>
          </n-icon>
        </n-space>
      </template>

      <n-list-item>
        <n-space :size="10">
          <n-alert :show-icon="false" :title="$t('project.preview_address')" type="success">
            {{ previewPath() }}
          </n-alert>
          <n-space vertical>
            <n-button tertiary type="primary" @click="copyPreviewPath()"> {{ $t('project.copy_address') }} </n-button>
            <n-button v-if="isMyProject === 1" :type="release ? 'warning' : 'primary'" @click="sendHandle">
              {{ release ? $t('project.cancel_release') : $t('project.release_screen') }}
            </n-button>
          </n-space>
        </n-space>
      </n-list-item>

      <n-list-item>
        <n-space :size="10">
          <n-button @click="modelShowHandle">{{ $t('project.close_modal') }}</n-button>
        </n-space>
      </n-list-item>
    </n-list>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useClipboard } from '@vueuse/core'
import { PreviewEnum } from '@/enums/pageEnum'
import { StorageEnum } from '@/enums/storageEnum'
import { ResultEnum } from '@/enums/httpEnum'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { syncData } from '../../ContentEdit/components/EditTools/hooks/useSyncUpdate.hook'
import { ProjectInfoEnum } from '@/store/modules/chartEditStore/chartEditStore.d'
import { changeProjectReleaseApi } from '@/api/thinglinks/view/projectApiSelector'
import {
  previewPath,
  renderIcon,
  fetchPathByName,
  routerTurnByNameWithQuery,
  setSessionStorage,
  getLocalStorage,
  httpErrorHandle,
  fetchRouteParamsLocation
} from '@/utils'
import { icon } from '@/plugins'
import { cloneDeep } from 'lodash'

const { t } = useI18n()
const { BrowsersOutlineIcon, SendIcon, AnalyticsIcon, CloseIcon } = icon.ionicons5
const chartEditStore = useChartEditStore()

const previewPathRef = ref(previewPath())
const { copy, isSupported } = useClipboard({ source: previewPathRef })

const routerParamsInfo = useRoute()

const modelShow = ref<boolean>(false)
const release = ref<boolean>(false)

// 获取 isMyProject 参数
const isMyProject = computed(() => {
  return routerParamsInfo.query?.isMyProject ? Number(routerParamsInfo.query.isMyProject) : 0
})

watchEffect(() => {
  release.value = chartEditStore.getProjectInfo.release || false
})

// 关闭弹窗
const closeHandle = () => {
  modelShow.value = false
}

// 预览
const previewHandle = () => {
  const path = fetchPathByName(PreviewEnum.CHART_PREVIEW_NAME, 'href')
  if (!path) return
  const { identification } = routerParamsInfo.query
  // id 标识
  const previewId = Array.isArray(identification)
    ? identification.find((value): value is string => typeof value === 'string')
    : identification
  if (!previewId) return
  const storageInfo = chartEditStore.getStorageInfo
  const sessionStorageInfo = getLocalStorage(StorageEnum.GO_CHART_STORAGE_LIST) || []

  if (sessionStorageInfo?.length) {
    const repeateIndex = sessionStorageInfo.findIndex((e: { id: string }) => e.id === previewId)
    // 重复替换
    if (repeateIndex !== -1) {
      sessionStorageInfo.splice(repeateIndex, 1, {
        id: previewId,
        ...storageInfo
      })
      setSessionStorage(StorageEnum.GO_CHART_STORAGE_LIST, sessionStorageInfo)
    } else {
      sessionStorageInfo.push({
        id: previewId,
        ...storageInfo
      })
      setSessionStorage(StorageEnum.GO_CHART_STORAGE_LIST, sessionStorageInfo)
    }
  } else {
    setSessionStorage(StorageEnum.GO_CHART_STORAGE_LIST, [{ id: previewId, ...storageInfo }])
  }
  // 跳转
  // routerTurnByPath(path, [previewId], undefined, true)
  routerTurnByNameWithQuery(
    PreviewEnum.CHART_PREVIEW_NAME,
    {
      identification: previewId,
      isMyProject: isMyProject.value
    },
    {}, // 路由参数
    undefined,
    true
  )
}

// 模态弹窗
const modelShowHandle = () => {
  modelShow.value = !modelShow.value
}

// 复制预览地址
const copyPreviewPath = (successText?: string, failureText?: string) => {
  if (isSupported) {
    copy()
    window['$message'].success(successText || t('project.copy_success'))
  } else {
    window['$message'].error(failureText || t('project.copy_failure'))
  }
}

// 发布
const sendHandle = async () => {
  let projectId = chartEditStore.getProjectInfo[ProjectInfoEnum.PROJECT_ID];
  // 获取 isMyProject 参数
  const isMyProject = routerParamsInfo.query?.isMyProject ? Number(routerParamsInfo.query.isMyProject) : 0
  const res = await changeProjectReleaseApi({
    id: projectId,
    // 反过来
    status: release.value ? -1 : 1
  }, isMyProject)

  if (res && res.code === ResultEnum.DATA_SUCCESS) {
    modelShowHandle()
    if (!release.value) {
      copyPreviewPath(t('project.release_success_copied'), t('project.release_success'))
    } else {
      window['$message'].success(t('project.release_cancelled'))
    }
    chartEditStore.setProjectInfo(ProjectInfoEnum.RELEASE, !release.value)
  } else {
    httpErrorHandle()
  }
}

const btnList = [
  {
    select: true,
    title: () => t('project.sync_content'),
    type: () => 'primary',
    icon: renderIcon(AnalyticsIcon),
    event: syncData
  },
  {
    key: 'preview',
    title: () => t('project.preview'),
    type: () => 'default',
    icon: renderIcon(BrowsersOutlineIcon),
    event: previewHandle
  },
  {
    key: 'release',
    title: () => (release.value ? t('project.released') : t('project.release_action')),
    icon: renderIcon(SendIcon),
    type: () => (release.value ? 'primary' : 'default'),
    event: modelShowHandle
  }
]

const comBtnList = computed(() => {
  let result = btnList
  
  // 如果 isMyProject === 0，过滤掉发布按钮
  if (isMyProject.value === 0) {
    result = result.filter(item => item.key !== 'release')
  }
  
  if (chartEditStore.getEditCanvas.isCodeEdit) {
    return result
  }
  const cloneList = cloneDeep(result)
  cloneList.shift()
  return cloneList
})
</script>

<style lang="scss" scoped>
@include go('system-setting') {
  @extend .go-background-filter;
  min-width: 100px;
  max-width: 60vw;
  padding-bottom: 20px;
  @include deep() {
    .n-list-item:not(:last-child) {
      border-bottom: 0;
    }
  }
}
</style>
