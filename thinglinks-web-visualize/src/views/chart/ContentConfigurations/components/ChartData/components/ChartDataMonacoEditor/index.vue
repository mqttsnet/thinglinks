<template>
  <template v-if="targetData.filter">
    <n-card>
      <p><span class="func-keyword">function</span>&nbsp;&nbsp;filter(data, res)&nbsp;&nbsp;{</p>
      <!-- 函数体 -->
      <div class="go-ml-4">
        <n-code :code="targetData.filter" language="typescript"></n-code>
      </div>
      <p>}</p>
      <template #footer>
        <n-space justify="end">
          <n-button type="primary" tertiary size="small" @click="addFilter">
            <template #icon>
              <n-icon>
                <filter-edit-icon />
              </n-icon>
            </template>
            {{ $t('global.r_edit') }}
          </n-button>
          <n-button tertiary size="small" @click="delFilter"> {{ $t('project.delete') }} </n-button>
        </n-space>
      </template>
    </n-card>
  </template>
  <template v-else>
    <n-button class="go-ml-3" @click="addFilter">
      <template #icon>
        <n-icon>
          <filter-icon />
        </n-icon>
      </template>
      {{ $t('project.add_filter') }}
    </n-button>
  </template>

  <!-- 弹窗 -->
  <n-modal class="go-chart-data-monaco-editor" v-model:show="showModal" :mask-closable="false" :closeOnEsc="false">
    <n-card :bordered="false" role="dialog" size="small" aria-modal="true" style="width: 1000px; height: 600px">
      <template #header>
        <n-space>
          <n-text>{{ $t('project.filter_function_editor') }}</n-text>
        </n-space>
      </template>
      <template #header-extra> </template>
      <n-space size="small" vertical>
        <n-space justify="space-between">
          <div>
            <n-space vertical>
              <n-tag type="info">
                <span class="func-keyword">function</span>&nbsp;&nbsp;filter(data, res)&nbsp;&nbsp;{
              </n-tag>
              <monaco-editor v-model:modelValue="filter" width="460px" height="380px" language="javascript" />
              <n-tag type="info">}</n-tag>
            </n-space>
          </div>
          <n-divider vertical style="height: 480px" />
          <n-scrollbar style="max-height: 480px">
            <n-space :size="15" vertical>
              <div class="editor-data-show">
                <n-space>
                  <n-text depth="3">{{ $t('project.default_filter_data') }}</n-text>
                  <n-code :code="toString(sourceData?.data) || $t('project.create_color_no_data')" language="json" :word-wrap="true"></n-code>
                </n-space>
              </div>
              <div class="editor-data-show">
                <n-space>
                  <n-text depth="3">{{ $t('project.api_return_data') }}</n-text>
                  <n-code :code="toString(sourceData) || $t('project.create_color_no_data')" language="json" :word-wrap="true"></n-code>
                </n-space>
              </div>
              <div class="editor-data-show">
                <n-space>
                  <n-text depth="3">{{ $t('project.filter_result') }}</n-text>
                  <n-code :code="filterRes || $t('project.create_color_no_data')" language="json" :word-wrap="true"></n-code>
                </n-space>
              </div>
            </n-space>
          </n-scrollbar>
        </n-space>
      </n-space>
      <template #action>
        <n-space justify="space-between">
          <div class="go-flex-items-center">
            <n-tag :bordered="false" type="primary">
              <template #icon>
                <n-icon :component="DocumentTextIcon" />
              </template>
              {{ $t('project.rule') }}
            </n-tag>
            <n-text class="go-ml-2" depth="2">{{ $t('project.filter_default_process_tip') }}</n-text>
          </div>

          <n-space>
            <n-button size="medium" @click="closeFilter">{{ $t('global.cancel') }}</n-button>
            <n-button size="medium" type="primary" @click="saveFilter">{{ $t('project.save') }}</n-button>
          </n-space>
        </n-space>
      </template>
    </n-card>
  </n-modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch, toRef, toRefs, toRaw, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTargetData } from '../../../hooks/useTargetData.hook'
import MonacoEditor from '@/components/Pages/MonacoEditor/index.vue'
import { icon } from '@/plugins'
import { goDialog, toString } from '@/utils'
import { customizeHttp } from '@/api/http'
import cloneDeep from 'lodash/cloneDeep'

const { t } = useI18n()

const { DocumentTextIcon } = icon.ionicons5
const { FilterIcon, FilterEditIcon } = icon.carbon
const { targetData, chartEditStore } = useTargetData()
const { requestDataType } = toRefs(targetData.value.request)
const { requestOriginUrl } = toRefs(chartEditStore.getRequestGlobalConfig)

// 受控弹窗
const showModal = ref(false)
// filter 函数模板
const filter = ref(targetData.value.filter || `return data`)
// 过滤错误标识
const errorFlag = ref(false)
// 目标静态/接口数据
const sourceData = ref<any>('')

// 动态获取数据
const fetchTargetData = async () => {
  try {
    const res = await customizeHttp(toRaw(targetData.value.request), toRaw(chartEditStore.getRequestGlobalConfig))
    if (res) {
      sourceData.value = res
      return
    }
    window['$message'].warning(t('project.no_return_value'))
  } catch (error) {
    console.error(error);
    window['$message'].warning(t('project.data_exception'))
  }
}

// 过滤结果
const filterRes = computed(() => {
  try {
    const fn = new Function('data', 'res', filter.value)
    const response = cloneDeep(sourceData.value)
    const res = fn(response?.data, response)
    // eslint-disable-next-line vue/no-side-effects-in-computed-properties
    errorFlag.value = false
    return toString(res)
  } catch (error) {
    // eslint-disable-next-line vue/no-side-effects-in-computed-properties
    errorFlag.value = true
    return `${t('project.filter_function_error')}${error}`
  }
})

// 新增过滤器
const addFilter = () => {
  showModal.value = true
}

// 删除过滤器
const delFilter = () => {
  goDialog({
    message: t('project.delete_filter_confirm'),
    onPositiveCallback: () => {
      targetData.value.filter = undefined
    }
  })
}

// 关闭过滤器
const closeFilter = () => {
  showModal.value = false
}

// 新增过滤器
const saveFilter = () => {
  if (errorFlag.value) {
    window['$message'].error(t('project.filter_function_error_cannot_save'))
    return
  }
  targetData.value.filter = filter.value
  closeFilter()
}

watch(
  () => showModal.value,
  (newData: boolean) => {
    if (newData) {
      fetchTargetData()
      filter.value = targetData.value.filter || `return data`
    }
  }
)
</script>

<style lang="scss" scoped>
.func-keyword {
  color: #b478cf;
}
@include go('chart-data-monaco-editor') {
  &.n-card.n-modal,
  .n-card {
    @extend .go-background-filter;
  }
  .editor-data-show {
    @include fetch-bg-color('filter-color');
    width: 420px;
    padding: 20px;
    border-radius: 5px;
  }
}
</style>
