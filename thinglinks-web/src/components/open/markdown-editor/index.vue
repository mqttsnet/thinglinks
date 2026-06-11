<script setup lang="ts">
  import { computed, ref } from 'vue';

  import gemoji from '@bytemd/plugin-gemoji';
  import gfm from '@bytemd/plugin-gfm';
  import highlight from '@bytemd/plugin-highlight';
  // bytemd https://github.com/bytedance/bytemd.git
  import { Editor, Viewer } from '@bytemd/vue-next';

  import 'bytemd/dist/index.css';
  import 'highlight.js/styles/idea.css';
  import 'github-markdown-css/github-markdown.css';

  const props = defineProps({
    modelValue: {
      type: String,
      default: '',
      required: true,
    },
    placeholder: {
      type: String,
      default: '请输入内容，支持Markdown语法',
      required: false,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  });
  const emit = defineEmits<{
    'update:modelValue': [value: string];
  }>();

  const plugins = [gfm(), highlight(), gemoji()];

  const updateValue = ref('');

  const content = computed(() => {
    return props.modelValue;
  });

  const isEdit = computed(() => {
    return !props.readonly;
  });

  function handleChange(v: any) {
    updateValue.value = v;
    emit('update:modelValue', v);
  }
</script>
<template>
  <div>
    <Editor
      v-if="isEdit"
      :value="content"
      :placeholder="placeholder"
      :plugins="plugins"
      @change="handleChange"
    />
    <Viewer v-else :value="content" :plugins="plugins" />
  </div>
</template>
<style lang="scss">
  .bytemd {
    height: calc(100vh - 100px);
  }

  .markdown-body {
    box-sizing: border-box;
    min-width: 200px;
    padding: 25px;
    margin: 0 auto;

    ol {
      padding-left: 1em;
      margin: 0;
      list-style: decimal !important;
    }

    ul {
      padding-left: 1em;
      margin: 0;
      list-style: disc !important;
    }
  }

  .markdown-body p,
  .markdown-body blockquote,
  .markdown-body ul,
  .markdown-body ol,
  .markdown-body dl,
  .markdown-body table,
  .markdown-body pre,
  .markdown-body details {
    margin-top: 0;
    margin-bottom: 16px;
  }
</style>
