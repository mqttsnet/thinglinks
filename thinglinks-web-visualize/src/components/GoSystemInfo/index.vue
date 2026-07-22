<template>
  <n-modal v-model:show="modelShowRef" @afterLeave="closeHandle">
    <n-list bordered class="go-system-info">
      <template #header>
        <n-space justify="space-between">
          <n-h3 class="go-mb-0">{{ t('global.about_us') }}</n-h3>
          <n-icon size="20" class="go-cursor-pointer" @click="closeHandle">
            <close-icon></close-icon>
          </n-icon>
        </n-space>
      </template>

      <n-list-item>
        <n-space class="go-my-2" :size="20">
          <n-text class="item-left">{{ t('global.product_name') }}</n-text>
          <n-text>{{ productName }}</n-text>
        </n-space>
      </n-list-item>

      <n-list-item>
        <n-space class="go-my-2" :size="20">
          <n-text class="item-left">{{ t('global.component_name') }}</n-text>
          <n-text>{{ productInfo.componentName }}</n-text>
        </n-space>
      </n-list-item>

      <n-list-item>
        <n-space class="go-my-2" :size="20">
          <n-text class="item-left">{{ t('global.component_version') }}</n-text>
          <n-text>{{ productInfo.componentVersion }}</n-text>
        </n-space>
      </n-list-item>

      <n-list-item>
        <n-space class="go-my-2" :size="20">
          <n-text class="item-left">{{ t('global.product_edition') }}</n-text>
          <n-text>{{ editionName }}</n-text>
        </n-space>
      </n-list-item>

      <n-list-item>
        <n-space class="go-mt-2" :size="20">
          <n-text class="item-left">{{ t('global.license_file') }}</n-text>
          <n-text>{{ productInfo.licenseFile }}</n-text>
        </n-space>
      </n-list-item>
    </n-list>
  </n-modal>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { icon } from '@/plugins'
import { useLangStore } from '@/store/modules/langStore/langStore'
import { getProductEditionName, getProductName, productInfo } from '@/settings/productSetting'

const props = defineProps({
  modelShow: Boolean
})

const emit = defineEmits(['update:modelShow'])
const { HelpOutlineIcon, CloseIcon } = icon.ionicons5
const modelShowRef = ref(false)

const t = window['$t']
const langStore = useLangStore()
const productName = computed(() => getProductName(langStore.getLang))
const editionName = computed(() => getProductEditionName(langStore.getLang))

watch(() => props.modelShow, (newValue) => {
  modelShowRef.value = newValue
})

const closeHandle = () => {
  emit('update:modelShow', false)
}
</script>

<style lang="scss" scoped>
@include go('system-info') {
  @extend .go-background-filter;
  min-width: 100px;
  max-width: 60vw;
  padding-bottom: 20px;
  .item-left {
    width: 200px;
  }
  @include deep() {
    .n-list-item:not(:last-child) {
      border-bottom: 0;
    }
  }
}
</style>
