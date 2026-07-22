<template>
  <n-modal v-model:show="showRef" class="go-create-modal" @afterLeave="closeHandle">
    <n-space size="large">
      <n-card class="card-box" hoverable>
        <template #header>
          <n-text class="card-box-tite">{{ $t('project.create_tip') }}</n-text>
        </template>
        <template #header-extra>
          <n-text @click="closeHandle">
            <n-icon size="20">
              <component :is="CloseIcon"></component>
            </n-icon>
          </n-text>
        </template>
        <n-space class="card-box-content" justify="center">
          <n-button
            size="large"
            :disabled="item.disabled"
            v-for="item in typeList"
            :key="item.key"
            @click="btnHandle(item.key)"
          >
            <component :is="item.title"></component>
            <template #icon>
              <n-icon size="18">
                <component :is="item.icon"></component>
              </n-icon>
            </template>
          </n-button>
        </n-space>
        <template #action></template>
      </n-card>
    </n-space>
  </n-modal>

</template>

<script lang="ts" setup>
import { ref, watch, shallowRef } from 'vue'
import { icon } from '@/plugins'
import { PageEnum, ChartEnum } from '@/enums/pageEnum'
import { ResultEnum } from '@/enums/httpEnum'
import { routerTurnByNameWithQuery, renderLang, getUUID } from '@/utils'
import { createProjectApi } from '@/api/thinglinks/view/projectApiSelector'

const { FishIcon, CloseIcon } = icon.ionicons5
const { StoreIcon, ObjectStorageIcon } = icon.carbon
const showRef = ref(false)

const emit = defineEmits(['close'])
const props = defineProps({
  show: Boolean
})

const typeList = shallowRef([
  {
    title: renderLang('project.my_templete'),
    key: PageEnum.BASE_HOME_TEMPLATE_NAME,
    icon: FishIcon,
    disabled: false
  },
  {
    title: renderLang('project.all_project'),
    key: PageEnum.BASE_HOME_ITEMS_NAME,
    icon: ObjectStorageIcon,
    disabled: false
  },
  // {
  //   title: renderLang('project.template_market'),
  //   key: PageEnum.BASE_HOME_TEMPLATE_MARKET_NAME,
  //   icon: StoreIcon,
  //   disabled: true
  // }
])

watch(() => props.show, newValue => {
  showRef.value = newValue
})

// 关闭对话框
const closeHandle = () => {
  emit('close', false)
}

// 处理按钮点击
const btnHandle = async (key: string) => {
  // switch (key) {
  //   case ChartEnum.CHART_HOME_NAME:
  try {
    // 根据 key 判断 isMyProject 的值
    const isMyProject = key === PageEnum.BASE_HOME_TEMPLATE_NAME ? 1 : 0
    // 新增项目
    const res = await createProjectApi({
      // 项目名称
      projectName: getUUID(),
      // 备注
      remark: null,
      // 图片地址
      indexImageId: null,
      // 项目初始化状态 [1-发布,-1-未发布]
      status: -1,
      // 项目标识
      // projectIdentification: null,
      // 组织id
      // createdOrgId: null,
    }, isMyProject)
    if(res && res.code === ResultEnum.DATA_SUCCESS) {
    window['$message'].success(window['$t']('project.create_success'))
      const { projectIdentification, templateIdentification } = res.data
      routerTurnByNameWithQuery(
        ChartEnum.CHART_HOME_NAME,
        {
          identification: isMyProject ? projectIdentification : templateIdentification,
          isMyProject: isMyProject
        },
        { id: '' }, // 路由参数占位符
        undefined,
        true
      )
      closeHandle()
    }
  } catch (error) {
    window['$message'].error(window['$t']('project.create_failure'))
  }
  //     break;
  // }
}
</script>
<style lang="scss" scoped>
$cardWidth: 570px;

@include go('create-modal') {
  position: fixed;
  top: 200px;
  left: 50%;
  transform: translateX(-50%);
  .card-box {
    width: $cardWidth;
    cursor: pointer;
    border: 1px solid rgba(0, 0, 0, 0);
    @extend .go-transition;
    &:hover {
      @include hover-border-color('hover-border-color');
    }
    &-tite {
      font-size: 14px;
    }
    &-content {
      padding: 0px 10px;
      width: 100%;
    }
  }
}
</style>
