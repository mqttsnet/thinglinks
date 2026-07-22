<template>
  <div class="real-time-info">
    <div>
      <div class="label">{{ t('video.media.proxy.playUrl') }}：</div>
      <a-input v-model:value="getSharedUrl.sharedUrl" disabled>
        <template #addonAfter
          ><SvgIcon name="copy" @click="handleCopyText(getSharedUrl.sharedUrl)" />
        </template>
      </a-input>
    </div>
    <div>
      <div class="label">iframe：</div>
      <a-input v-model:value="getSharedUrl.sharedIframe" disabled>
        <template #addonAfter
          ><SvgIcon name="copy" @click="handleCopyText(getSharedUrl.sharedIframe)" />
        </template>
      </a-input>
    </div>
    <div>
      <div class="label">{{ t('video.media.proxy.resourceUrl') }}：</div>
      <a-input v-model:value="getSharedUrl.sharedRtmp" disabled>
        <template #addonBefore>
          <a-select
            :value="t('video.media.proxy.moreUrl')"
            :dropdownMatchSelectWidth="false"
          >
            <a-select-option
              v-for="item in getResourceLabelList"
              :value="urlMap[item]"
              :key="item"
              @click="handleCopyText(urlMap[item])"
              >{{ urlMap[item] }}</a-select-option
            >
          </a-select>
        </template>

        <template #addonAfter><SvgIcon name="copy" /> </template>
      </a-input>
    </div>
  </div>
</template>
<script setup lang="ts">
  import { ref, computed, watchEffect } from 'vue';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';

  const props = defineProps({
    videoUrl: { type: String, required: true },
    urlMap: { type: Object, required: true },
  });
  const { t } = useI18n();
  const { createMessage } = useMessage();
  const videoUrl = ref(props.videoUrl);
  const urlMap = ref(props.urlMap);
  const getSharedUrl = computed(() => {
    return {
      sharedUrl: window.location.origin + '/#/play/wasm/' + encodeURIComponent(videoUrl.value),
      sharedIframe:
        '<iframe src="' +
        window.location.origin +
        '/#/play/wasm/' +
        encodeURIComponent(videoUrl.value) +
        '"></iframe>',
      sharedRtmp: videoUrl,
    };
  });
  const getResourceLabelList = computed(() => {
    return Object.keys(urlMap.value);
  });
  const handleCopyText = async (text: string) => {
    handleCopyTextV2(text || '');
  };
  watchEffect(() => {
    urlMap.value = props.urlMap;
    videoUrl.value = props.videoUrl;
  });
</script>
<style scoped lang="less">
  .real-time-info {
    .label {
      margin: 8px 0;
    }
  }
</style>
