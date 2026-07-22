<script lang="ts" setup>
  import { nextTick, onMounted, ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  import { BasicTree } from '/@/components/Tree';
  import MarkdownEditor from '/@/components/open/markdown-editor';

  import { findHelpTree } from '/@/api/open/doc/openApi';
  import { SopDocInfoResultVO } from '/@/api/devOperation/sop/model/sopDocInfoModel';

  const treeRef = ref();
  const value = ref('');

  const treeLoading = ref<boolean>(false);
  const docTreeDataRef = ref<SopDocInfoResultVO[]>([]);

  const loadDocTree = async () => {
    try {
      treeLoading.value = true;
      const treeList = await findHelpTree();
      docTreeDataRef.value = treeList;
      await nextTick();
      await treeRef.value?.expandAll(true);
    } finally {
      treeLoading.value = false;
    }
  };
  onMounted(async () => {
    await loadDocTree();
  });

  const handleSelect = (_, { node }: { node: any }) => {
    value.value = node.content;
  };
</script>

<template>
  <PageWrapper contentClass="flex" dense>
    <div
      class="border-border bg-card mr-2 h-full rounded-[var(--radius)] border p-2 md:w-1/3 xl:w-1/3"
    >
      <BasicTree
        ref="treeRef"
        :field-names="{ key: 'id', title: 'label' }"
        :click-row-to-expand="false"
        :loading="treeLoading"
        :tree-data="docTreeDataRef"
        default-expand-all
        check-strictly
        highlight
        search
        toolbar
        @select="handleSelect"
      />
    </div>
    <MarkdownEditor :model-value="value" readonly />
  </PageWrapper>
</template>
