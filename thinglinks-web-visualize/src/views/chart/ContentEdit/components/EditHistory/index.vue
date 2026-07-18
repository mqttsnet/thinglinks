<template>
  <div class="go-flex-items-center">
    <n-popover class="edit-history-popover" :show-arrow="false" size="small" trigger="click" placement="top-start">
      <template #trigger>
        <n-button class="mr-10" secondary size="small" :disabled="options.length === 0">
          <span class="btn-text">{{ $t('project.history_record') }}</span>
        </n-button>
      </template>

      <div class="history-list-box">
        <n-scrollbar style="max-height: 500px">
          <div
            class="list-item go-flex-items-center go-ellipsis-1"
            v-for="(item, index) in options"
            :key="index"
            :title="item.label"
          >
            <n-icon class="item-icon" size="16" :depth="2" :component="item.icon" />
            <n-text depth="2">{{ item.label }}</n-text>
          </div>
        </n-scrollbar>
        <div class="popover-modal"></div>
      </div>
    </n-popover>

    <n-tooltip trigger="hover">
      <template #trigger>
        <n-icon size="21" :depth="3">
          <help-outline-icon></help-outline-icon>
        </n-icon>
      </template>
      <span>{{ $t('project.history_max_records', { count: editHistoryMax }) }}</span>
    </n-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { icon } from '@/plugins'
import { useChartHistoryStore } from '@/store/modules/chartHistoryStore/chartHistoryStore'
import { historyActionTypeName } from '@/store/modules/chartHistoryStore/chartHistoryDefine'
import { CreateComponentType } from '@/packages/index.d'
import { editHistoryMax } from '@/settings/designSetting'
import reverse from 'lodash/reverse'
import {
  HistoryItemType,
  HistoryTargetTypeEnum,
  HistoryActionTypeEnum
} from '@/store/modules/chartHistoryStore/chartHistoryStore.d'

const { t } = useI18n()

const {
  DesktopOutlineIcon,
  PencilIcon,
  TrashIcon,
  CopyIcon,
  LayersIcon,
  DuplicateIcon,
  HelpOutlineIcon,
  LockClosedOutlineIcon,
  LockOpenOutlineIcon,
  EyeOffOutlineIcon,
  EyeOutlineIcon
} = icon.ionicons5
const { StackedMoveIcon, Carbon3DCursorIcon, Carbon3DSoftwareIcon } = icon.carbon

const chartHistoryStoreStore = useChartHistoryStore()

// 设置类型对应图标
const iconHandle = (e: HistoryItemType) => {
  // 画布编辑
  if (e.targetType === HistoryTargetTypeEnum.CANVAS) {
    return DesktopOutlineIcon
  }
  switch (e.actionType) {
    case HistoryActionTypeEnum.UPDATE:
      return PencilIcon
    case HistoryActionTypeEnum.DELETE:
      return TrashIcon
    case HistoryActionTypeEnum.PASTE:
      return CopyIcon
    case HistoryActionTypeEnum.TOP:
      return LayersIcon
    case HistoryActionTypeEnum.BOTTOM:
      return LayersIcon
    case HistoryActionTypeEnum.UP:
      return LayersIcon
    case HistoryActionTypeEnum.DOWN:
      return LayersIcon
    case HistoryActionTypeEnum.MOVE:
      return StackedMoveIcon
    case HistoryActionTypeEnum.ADD:
      return DuplicateIcon
    case HistoryActionTypeEnum.GROUP:
      return Carbon3DCursorIcon
    case HistoryActionTypeEnum.UN_GROUP:
      return Carbon3DSoftwareIcon
    case HistoryActionTypeEnum.LOCK:
      return LockClosedOutlineIcon
    case HistoryActionTypeEnum.UNLOCK:
      return LockOpenOutlineIcon
    case HistoryActionTypeEnum.HIDE:
      return EyeOffOutlineIcon
    case HistoryActionTypeEnum.SHOW:
      return EyeOutlineIcon
    default:
      return PencilIcon
  }
}

// 获取翻译后的历史操作名称
const getHistoryActionName = (actionType: HistoryActionTypeEnum | HistoryTargetTypeEnum): string => {
  const actionNameMap: Record<string, string> = {
    [HistoryActionTypeEnum.ADD]: t('project.history_add'),
    [HistoryActionTypeEnum.DELETE]: t('project.delete'),
    [HistoryActionTypeEnum.UPDATE]: t('project.history_update'),
    [HistoryActionTypeEnum.MOVE]: t('project.history_move'),
    [HistoryActionTypeEnum.PASTE]: t('project.paste'),
    [HistoryActionTypeEnum.COPY]: t('project.copy'),
    [HistoryActionTypeEnum.CUT]: t('project.cut'),
    [HistoryActionTypeEnum.TOP]: t('project.to_top'),
    [HistoryActionTypeEnum.BOTTOM]: t('project.to_bottom'),
    [HistoryActionTypeEnum.UP]: t('project.move_up'),
    [HistoryActionTypeEnum.DOWN]: t('project.move_down'),
    [HistoryActionTypeEnum.GROUP]: t('project.history_group'),
    [HistoryActionTypeEnum.UN_GROUP]: t('project.history_un_group'),
    [HistoryActionTypeEnum.LOCK]: t('project.lock'),
    [HistoryActionTypeEnum.UNLOCK]: t('project.unlock'),
    [HistoryActionTypeEnum.HIDE]: t('project.hide'),
    [HistoryActionTypeEnum.SHOW]: t('project.show'),
    [HistoryTargetTypeEnum.CANVAS]: t('project.history_canvas_init')
  }
  const fallbackActionNameMap: Partial<Record<HistoryActionTypeEnum | HistoryTargetTypeEnum, string>> = historyActionTypeName
  return actionNameMap[actionType] || fallbackActionNameMap[actionType] || ''
}

// 设置类型对应文本
const labelHandle = (e: HistoryItemType) => {
  // 画布编辑
  if (e.targetType === HistoryTargetTypeEnum.CANVAS) {
    return getHistoryActionName(HistoryTargetTypeEnum.CANVAS)
  } else if (e.actionType === HistoryActionTypeEnum.GROUP || e.actionType === HistoryActionTypeEnum.UN_GROUP) {
    return getHistoryActionName(e.actionType)
  } else if (e.historyData.length) {
    return `${getHistoryActionName(e.actionType)} - ${(e.historyData[0] as CreateComponentType).chartConfig.title}`
  }
}

const options = computed(() => {
  const backStack: HistoryItemType[] = chartHistoryStoreStore.getBackStack
  const options = backStack.map((e: HistoryItemType) => {
    return {
      label: labelHandle(e),
      icon: iconHandle(e)
    }
  })

  return reverse(options.filter(item => item.label))
})
</script>

<style lang="scss" scoped>
.edit-history-popover {
  .btn-text {
    font-size: 12px;
    margin-right: 3px;
  }
  .history-list-box {
    width: 100%;
    .list-item {
      z-index: 2;
      position: relative;
      cursor: default;
      padding: 2px;
      .item-icon {
        margin-right: 10px;
      }
    }
  }
}
</style>
