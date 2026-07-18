<template>
  <div v-if="cardData" class="go-items-list-card">
    <n-card hoverable size="small">
      <div class="list-content">
        <!-- 顶部按钮 -->
        <div class="list-content-top">
          <mac-os-control-btn
            class="top-btn"
            :hidden="['remove']"
            @close="deleteHandle"
            @resize="resizeHandle"
          ></mac-os-control-btn>
        </div>
        <!-- 中间 -->
        <div class="list-content-img" @click="resizeHandle">
          <n-image
            object-fit="contain"
            height="180"
            preview-disabled
            :src="cardImage"
            :alt="cardData.projectName || cardData.templateName"
            :fallback-src="requireErrorImg()"
          ></n-image>
        </div>
      </div>
      <template #action>
        <div class="go-flex-items-center list-footer" justify="space-between">
          <n-text class="go-ellipsis-1" :title="cardData.projectName || cardData.templateName || cardData.id">
            {{ cardTitle }}
          </n-text>
          <!-- 工具 -->
          <div class="go-flex-items-center list-footer-ri">
            <n-space>
              <n-text v-if="props.isMyProject">
                <n-badge class="go-animation-twinkle" :color="isReleased ? '#34c749' : '#fcbc40'"></n-badge>
                {{  isReleased ? $t('project.release') : $t('project.unreleased') }}
              </n-text>

              <template v-for="item in fnBtnList" :key="item.key">
                <template v-if="item.key === 'select'">
                  <n-dropdown
                    trigger="hover"
                    placement="bottom"
                    :options="selectOptions"
                    :show-arrow="true"
                    @select="handleSelect"
                  >
                    <n-button size="small">
                      <template #icon>
                        <component :is="item.icon"></component>
                      </template>
                    </n-button>
                  </n-dropdown>
                </template>

                <n-tooltip v-else placement="bottom" trigger="hover">
                  <template #trigger>
                    <n-button size="small" @click="handleSelect(item.key)">
                      <template #icon>
                        <component :is="item.icon"></component>
                      </template>
                    </n-button>
                  </template>
                  <component :is="item.label"></component>
                </n-tooltip>
              </template>
            </n-space>
            <!-- end -->
          </div>
        </div>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { renderIcon, renderLang, requireErrorImg } from '@/utils'
import { icon } from '@/plugins'
import { MacOsControlBtn } from '@/components/Tips/MacOsControlBtn'
import { ProjectResultVO } from '../../index.d'

const {
  EllipsisHorizontalCircleSharpIcon,
  TrashIcon,
  BrowsersOutlineIcon,
  HammerIcon,
  SendIcon
} = icon.ionicons5

const emit = defineEmits(['preview', 'delete', 'resize', 'edit', 'release'])

const props = defineProps({
  cardData: Object as PropType<ProjectResultVO>,
  isMyProject: {
    type: Boolean,
    default: false
  }
})

// 判断项目是否已发布
const isReleased = computed(() => {
  return (props.cardData?.status ?? 0) > 0
})

// 判断是否可以编辑
const canEdit = computed(() => {
  // 当 isMyProject 为 false 时，始终可以编辑
  // 当 isMyProject 为 true 时，只有未发布的项目可以编辑
  return !props.isMyProject || !isReleased.value
})

// 判断是否可以删除
const canDelete = computed(() => {
  // 当 isMyProject 为 true 且项目已发布时，不能删除
  // 其他情况都可以删除
  return !(props.isMyProject && isReleased.value)
})

// title
const cardTitle = computed(() => {
  const cardData = props.cardData
  return cardData?.projectName || cardData?.projectIdentification || cardData?.templateName || cardData?.templateIdentification || ''
})

const cardImage = computed(() => {
  const cardData = props.cardData
  if (cardData?.indexImage) return cardData.indexImage
  if (cardData?.indexImageId?.startsWith('http')) return cardData.indexImageId
  return requireUrl('project/moke-20211219181327.png')
})

// 按钮列表：根据 isMyProject 和 status 控制编辑按钮显示
const fnBtnList = computed(() => {
  const buttons = []
  
  if (canEdit.value) {
    buttons.push({
      label: renderLang('global.r_edit'),
      key: 'edit',
      icon: renderIcon(HammerIcon)
    })
  }
  
  buttons.push({
    label: renderLang('global.r_more'),
    key: 'select',
    icon: renderIcon(EllipsisHorizontalCircleSharpIcon)
  })
  
  return buttons
})

const selectOptions = computed(() => {
  const options = [
    {
      label: renderLang('global.r_preview'),
      key: 'preview',
      icon: renderIcon(BrowsersOutlineIcon)
    }
  ]
  
  // 只有在 isMyProject 为 true 时才显示发布/取消发布选项
  if (props.isMyProject) {
    options.push({
      label: isReleased.value ? renderLang('global.r_unpublish') : renderLang('global.r_publish'),
      key: 'release',
      icon: renderIcon(SendIcon)
    })
  }
  
  // 删除选项：根据 canDelete 判断
  if (canDelete.value) {
    options.push({
      label: renderLang('global.r_delete'),
      key: 'delete',
      icon: renderIcon(TrashIcon)
    })
  }
  
  return options
})

const handleSelect = (key: string) => {
  switch (key) {
    case 'preview':
      previewHandle()
      break
    case 'delete':
      deleteHandle()
      break
    case 'release':
      releaseHandle()
      break
    case 'edit':
      editHandle()
      break
  }
}
// 处理url获取
const requireUrl = (name: string) => {
  return new URL(`../../../../../assets/images/${name}`, import.meta.url).href
}

// 预览处理
const previewHandle = () => {
  emit('preview', props.cardData)
}

// 删除处理
const deleteHandle = () => {
  emit('delete', props.cardData)
}

// 编辑处理
const editHandle = () => {
  emit('edit', props.cardData)
}

// 发布/取消发布处理
const releaseHandle = () => {
  emit('release', props.cardData)
}

// 放大处理
const resizeHandle = () => {
  emit('resize', props.cardData)
}
</script>

<style lang="scss" scoped>
$contentHeight: 180px;
@include go('items-list-card') {
  position: relative;
  border-radius: $--border-radius-base;
  border: 1px solid rgba(0, 0, 0, 0);
  @extend .go-transition;
  &:hover {
    @include hover-border-color('hover-border-color');
  }
  .list-content {
    margin-top: 20px;
    margin-bottom: 5px;
    cursor: pointer;
    border-radius: $--border-radius-base;
    @include background-image('background-point');
    @extend .go-point-bg;
    &-top {
      position: absolute;
      top: 10px;
      left: 10px;
      height: 22px;
    }
    &-img {
      height: $contentHeight;
      @extend .go-flex-center;
      @extend .go-border-radius;
      @include deep() {
        img {
          @extend .go-border-radius;
        }
      }
    }
  }
  .list-footer {
    flex-wrap: nowrap;
    justify-content: space-between;
    line-height: 30px;
    &-ri {
      justify-content: flex-end;
      min-width: 180px;
    }
  }
}
</style>
