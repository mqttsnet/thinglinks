<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import Axios from 'axios';
  import MarkdownEditor from '/@/components/open/markdown-editor';

  const value = ref('');
  onMounted(() => {
    Axios.get('/static/md/sign.md').then((resp) => {
      value.value = resp.data;
    });
  });
</script>
<template>
  <MarkdownEditor :model-value="value" readonly />
</template>
