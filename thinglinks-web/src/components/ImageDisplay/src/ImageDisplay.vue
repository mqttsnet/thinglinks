<template>
  <div v-if="showImage" class="image-display-container" :class="containerClass">
    <div v-if="title" class="image-title">{{ title }}</div>
    <div class="image-wrapper" :style="wrapperStyle">
      <ThumbUrl
        :fileId="fileId"
        :fileUrl="fileUrl"
        :width="imageWidth"
        :height="imageHeight"
        :imageStyle="imageStyle"
        :preview="preview"
        :fallback="fallback"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, type CSSProperties, type PropType } from 'vue';
import { Image } from 'ant-design-vue';
import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
import { propTypes } from '/@/utils/propTypes';
import { errImg } from '/@/utils/file/base64Conver';

export default defineComponent({
  name: 'ImageDisplay',
  components: {
    Image,
    ThumbUrl,
  },
  props: {
    // 标题
    title: propTypes.string.def(''),
    // 图片文件ID
    fileId: propTypes.string.def(''),
    // 图片URL（与fileId二选一）
    fileUrl: propTypes.string.def(''),
    // 图片宽度
    imageWidth: propTypes.oneOfType([propTypes.number, propTypes.string]).def(200),
    // 图片高度
    imageHeight: propTypes.oneOfType([propTypes.number, propTypes.string]).def(200),
    // 图片样式
    imageStyle: {
      type: Object as PropType<CSSProperties>,
      default: () => ({ 'max-width': '200px', 'max-height': '200px' }),
    },
    // 容器样式
    wrapperStyle: {
      type: Object as PropType<CSSProperties>,
      default: () => ({}),
    },
    // 容器class
    containerClass: propTypes.string.def(''),
    // 是否显示边框
    showBorder: propTypes.bool.def(true),
    // 是否显示预览
    preview: propTypes.bool.def(true),
    // 错误图片
    fallback: propTypes.string.def(errImg),
  },
  setup(props) {
    const showImage = computed(() => {
      return props.fileId || props.fileUrl;
    });

    const wrapperStyle = computed(() => {
      const defaultStyle: CSSProperties = {
        padding: '16px',
        border: props.showBorder ? '1px solid #e8e8e8' : 'none',
        borderRadius: '4px',
        background: '#fafafa',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '200px',
      };
      return { ...defaultStyle, ...props.wrapperStyle };
    });

    return {
      showImage,
      wrapperStyle,
    };
  },
});
</script>

<style lang="less" scoped>
.image-display-container {
  .image-title {
    font-size: 14px;
    font-weight: 600;
    color: #2e3033;
    margin-bottom: 12px;
  }
}
</style>