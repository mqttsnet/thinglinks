<template>
  <div class="go-project">
    <n-layout has-sider position="absolute">
      <n-space vertical>
        <project-layout-sider></project-layout-sider>
      </n-space>
      <n-layout>
        <layout-header-user></layout-header-user>
        <n-layout
          id="go-project-content-top"
          class="content-top"
          position="absolute"
          :native-scrollbar="false"
        >
          <n-layout-content>
            <layout-transition-main>
              <router-view></router-view>
            </layout-transition-main>
          </n-layout-content>
        </n-layout>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ProjectLayoutSider } from './layout/components/ProjectLayoutSider'
import { LayoutHeaderUser } from '@/layout/components/LayoutHeaderUser'
import { LayoutTransitionMain } from '@/layout/components/LayoutTransitionMain/index'
import { goDialog } from '@/utils'

// 提示
// goDialog({
//   message: '不要在官方后端上发布任何私密数据，任何人都看得到并进行删除！！！！',
//   isMaskClosable: true,
//   closeNegativeText: true,
//   transformOrigin: 'center',
//   onPositiveCallback: () => {}
// })
</script>

<style lang="scss" scoped>
@include go(project) {
  .content-top {
    top: $--header-height;
    margin-top: 1px;
  }
}
</style>
