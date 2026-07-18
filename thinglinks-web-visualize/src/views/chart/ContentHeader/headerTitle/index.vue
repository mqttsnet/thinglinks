<template>
  <n-space>
    <n-icon size="20" :depth="3">
      <fish-icon></fish-icon>
    </n-icon>
    <n-text @click="handleFocus">
      {{ $t('project.workspace') }}
      <n-button v-show="!focus" secondary size="tiny">
        <span class="title">
          {{ comTitle }}
        </span>
      </n-button>
    </n-text>

    <n-input
      v-show="focus"
      ref="inputInstRef"
      size="small"
      type="text"
      maxlength="16"
      show-count
      :placeholder="$t('project.enter_project_name')"
      v-model:value.trim="title"
      @keyup.enter="handleBlur"
      @blur="handleBlur"
    ></n-input>
  </n-space>
</template>

<script setup lang="ts">
import { ref, nextTick, computed, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ResultEnum } from '@/enums/httpEnum'
import { fetchRouteParamsLocation, httpErrorHandle, setTitle } from '@/utils'
import { useChartEditStore } from '@/store/modules/chartEditStore/chartEditStore'
import { ProjectInfoEnum, EditCanvasConfigEnum } from '@/store/modules/chartEditStore/chartEditStore.d'
import { updateProjectApi } from '@/api/thinglinks/view/projectApiSelector'
import { useSync } from '../../hooks/useSync.hook'
import { icon } from '@/plugins'

const { t } = useI18n()
const route = useRoute()
const chartEditStore = useChartEditStore()
const { dataSyncUpdate } = useSync()
const { FishIcon } = icon.ionicons5

const focus = ref<boolean>(false)
const inputInstRef = ref(null)

const title = ref<string>(fetchRouteParamsLocation())


// 获取 isMyProject 参数
const isMyProject = route.query?.isMyProject ? Number(route.query.isMyProject) : 0

watchEffect(() => {
  title.value = isMyProject === 1 ? chartEditStore.getProjectInfo.projectName : chartEditStore.getProjectInfo.templateName || ''
})

const comTitle = computed(() => {
  // eslint-disable-next-line vue/no-side-effects-in-computed-properties
  title.value = title.value.replace(/\s/g, '')
  const newTitle = title.value.length ? title.value : t('project.new_project')
  setTitle(`${t('project.workspace')}${newTitle}`)
  chartEditStore.setEditCanvasConfig(EditCanvasConfigEnum.PROJECT_NAME, newTitle)
  return newTitle
})

const handleFocus = () => {
  focus.value = true
  nextTick(() => {
    inputInstRef.value && (inputInstRef.value as any).focus()
  })
}

const handleBlur = async () => {
  focus.value = false
  const titleEnum = isMyProject === 1 ? ProjectInfoEnum.PROJECT_NAME : ProjectInfoEnum.TEMPLATE_NAME
  chartEditStore.setProjectInfo(titleEnum, title.value || '')
  let id = chartEditStore.getProjectInfo[ProjectInfoEnum.PROJECT_ID];
  const params = {
    id,
    [titleEnum]: title.value
  }
  const res = (await updateProjectApi(params, isMyProject))
  if (res && res.code === ResultEnum.DATA_SUCCESS) {
    dataSyncUpdate()
  } else {
    httpErrorHandle()
  }
}
</script>
<style lang="scss" scoped>
.title {
  padding-left: 5px;
  padding-right: 5px;
  font-size: 15px;
}
</style>
