<template>
  <div class="go-items-list">
    <!-- 筛选区域 -->
    <div class="list-filter">
      <n-space>
        <n-input
          v-model:value="filterParams.projectName"
          :placeholder="namePlaceholder"
          clearable
          class="filter-input"
          @keyup.enter="handleFilter"
        />
        <n-input
          v-model:value="filterParams.projectIdentification"
          :placeholder="identificationPlaceholder"
          clearable
          class="filter-input"
          @keyup.enter="handleFilter"
        />
        <n-select
          v-if="isMyProject" 
          v-model:value="filterParams.status"
          :placeholder="$t('project.status')"
          clearable
          class="filter-select"
          :options="statusOptions"
          @update:value="handleFilter"
        />
        <n-button type="primary" @click="handleFilter">{{ $t('project.filter') }}</n-button>
        <n-button @click="handleReset">{{ $t('project.reset') }}</n-button>
      </n-space>
    </div>

    <!-- 内容区域（可滚动） -->
    <div class="list-content-wrapper">
      <!-- 加载 -->
      <div v-show="loading" class="list-loading">
        <go-loading></go-loading>
      </div>
      <!-- 列表 -->
      <div v-show="!loading" class="list-content">
        <div class="list-cards">
          <project-items-card
            v-for="(item, index) in list"
            :key="item.id"
            :cardData="(item as ProjectResultVO)"
            :isMyProject="isMyProject"
            @preview="previewHandle"
            @resize="resizeHandle"
            @delete="deleteHandle(item)"
            @release="releaseHandle(item, index)"
            @edit="editHandle"
          ></project-items-card>
        </div>
        <n-empty class="no-data" v-if="!list.length" :description="$t('project.no_project')"></n-empty>
      </div>
    </div>

    <!-- 分页（固定在底部） -->
    <div class="list-pagination">
      <n-pagination
        :page="paginat.page"
        :page-size="paginat.limit"
        :item-count="paginat.count"
        :page-sizes="[12, 24, 36, 48]"
        @update:page="changePage"
        @update:page-size="changeSize"
        show-size-picker
      />
    </div>
  </div>

  <!-- model -->
  <project-items-modal-card
    v-if="modalData"
    :modalShow="modalShow"
    :cardData="modalData"
    @close="closeModal"
    @edit="editHandle"
  ></project-items-modal-card>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { PageEnum } from '@/enums/pageEnum'
import { ProjectItemsCard } from '../ProjectItemsCard/index'
import { ProjectItemsModalCard } from '../ProjectItemsModalCard/index'
import { icon } from '@/plugins'
import { useModalDataInit } from './hooks/useModal.hook'
import { useDataListInit, type ProjectType } from './hooks/useData.hook'
import { ProjectResultVO } from '../../index.d'

const { t } = useI18n()
const route = useRoute()

const props = defineProps({
  isMyProject: {
    type: Boolean,
    default: false
  }
})

const { CopyIcon, EllipsisHorizontalCircleSharpIcon } = icon.ionicons5
const { modalData, modalShow, closeModal, previewHandle, resizeHandle, editHandle } = useModalDataInit()

// 根据 isMyProject 确定项目类型
const projectType: ProjectType = props.isMyProject ? 'myProject' : 'projectTemplate'

// 筛选参数
const filterParams = reactive<{
  projectName: string
  projectIdentification: string
  status: number | null | undefined
}>({
  projectName: '',
  projectIdentification: '',
  status: !props.isMyProject ? -1 : undefined
})

// 根据路由动态设置 placeholder
const isItemsRoute = computed(() => route.path === PageEnum.BASE_HOME_ITEMS)
const namePlaceholder = computed(() => 
  isItemsRoute.value ? t('project.template_name') : t('project.project_name')
)
const identificationPlaceholder = computed(() => 
  isItemsRoute.value ? t('project.template_identification') : t('project.project_identification')
)

// 状态选项
const statusOptions = computed(() => [
  { label: t('project.release'), value: 1 },
  { label: t('project.unreleased'), value: -1 }
])

const { loading, paginat, list, changeSize, changePage, releaseHandle, deleteHandle, getList, setFilterParams } = useDataListInit(projectType)

// 筛选处理
const handleFilter = () => {
  setFilterParams({
    projectName: filterParams.projectName || undefined,
    projectIdentification: filterParams.projectIdentification || undefined,
    status: filterParams.status !== null ? filterParams.status : undefined
  })
  paginat.page = 1
  getList()
}

// 重置筛选
const handleReset = () => {
  filterParams.projectName = ''
  filterParams.projectIdentification = ''
  filterParams.status = null
  setFilterParams({})
  paginat.page = 1
  getList()
}

onMounted(() => {
  handleFilter()
})
</script>

<style lang="scss" scoped>
@include go('items-list') {
  display: flex;
  flex-direction: column;
  height: calc(100vh - #{$--header-height} - 2.5rem);
  overflow: hidden;
  
  .list-filter {
    flex-shrink: 0;
    margin-bottom: 1.25rem;
    padding-bottom: 0.625rem;
    
    .filter-input {
      width: 12.5rem;
      min-width: 8rem;
      max-width: 100%;
    }
    
    .filter-select {
      width: 9.375rem;
      min-width: 7rem;
      max-width: 100%;
    }
    
    // 响应式调整输入框宽度
    @media (max-width: 48rem) {
      .filter-input,
      .filter-select {
        width: 100%;
        max-width: 100%;
      }
    }
  }
  
  .list-content-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
    
    .list-loading {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 25rem;
    }
    
    .list-content {
      padding-bottom: 1.25rem;
      
      .list-cards {
        display: grid;
        gap: 1.25rem;
        justify-items: start;
        
        // 响应式网格布局
        // 最小值：17.5rem，最大值：22rem
        // 理想值基于容器宽度动态计算，自动调整列数
        grid-template-columns: repeat(
          auto-fit,
          minmax(
            clamp(
              17.5rem,
              calc((100% - 3 * 1.25rem) / 4),
              22rem
            ),
            1fr
          )
        );
        
        // 确保卡片正确显示，避免边距重叠
        > * {
          min-width: 0;
          max-width: min(100%, 30rem);
          margin: 0;
        }
        
        // 清除 n-card 组件的默认 margin
        @include deep() {
          .n-card {
            margin: 0;
          }
        }
      }
      
      .no-data {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 25rem;
      }
    }
  }
  
  .list-pagination {
    flex-shrink: 0;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 1rem 1.25rem;
    background-color: var(--n-color);
    border-top: 1px solid var(--n-border-color);
    margin-top: auto;
    position: sticky;
    bottom: 0;
    z-index: 10;
  }
}
</style>
