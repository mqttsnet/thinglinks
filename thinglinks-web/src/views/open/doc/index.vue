<script lang="ts" setup>
  import { nextTick, ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  import { BasicTree } from '/@/components/Tree';
  import { ApiSelect } from '/@/components/Form';

  import Content from './modules/content.vue';

  import { findAppList, findDocTree } from '/@/api/open/doc/openApi';
  import { SopDocInfoResultVO } from '/@/api/devOperation/sop/model/sopDocInfoModel';

  const treeRef = ref();
  const contentRef = ref();

  const docAppIdRef = ref<string>('');
  const treeLoading = ref<boolean>(false);
  const docTreeDataRef = ref<SopDocInfoResultVO[]>([]);
  const selectedKeysRef = ref<string[]>([]);

  /**
   * 在树里深度优先找到第一个非分类（叶子）节点。
   * 加载完成后自动 select，让右侧不再是一片空白让用户摸不着头脑。
   */
  const findFirstLeaf = (
    nodes: SopDocInfoResultVO[] | undefined,
  ): SopDocInfoResultVO | undefined => {
    if (!nodes || !nodes.length) {
      return undefined;
    }
    for (const node of nodes) {
      if (node.isFolder !== 1) {
        return node;
      }
      const inChildren = findFirstLeaf(node.children);
      if (inChildren) {
        return inChildren;
      }
    }
    return undefined;
  };

  const loadDocTree = async (appId: string) => {
    try {
      treeLoading.value = true;
      // 切换应用先把右侧重置成空状态，避免上一个应用的接口详情残留
      contentRef.value?.reset();
      selectedKeysRef.value = [];

      const treeList = await findDocTree(appId);
      docTreeDataRef.value = treeList || [];
      await nextTick();
      await treeRef.value?.expandAll(true);

      // 自动选中第一个叶子节点：左侧树通常都是分类（isFolder=1）+ API 叶子（isFolder=0）的混合结构，
      // 直接显示空白页面用户会以为页面坏了。找到第一个 API 节点高亮 + 触发右侧加载。
      const firstLeaf = findFirstLeaf(docTreeDataRef.value);
      if (firstLeaf?.id) {
        selectedKeysRef.value = [firstLeaf.id];
        contentRef.value?.loadContent(firstLeaf.id);
      }
    } finally {
      treeLoading.value = false;
    }
  };
  const handleOptionsChange = async (options: any) => {
    if (options.length > 0) {
      docAppIdRef.value = options?.[0]?.value;
      await loadDocTree(options?.[0]?.value);
    }
  };

  const handleChange = async (value: any) => {
    await loadDocTree(value);
  };

  const handleSelect = (_, { node }: { node: any }) => {
    if (node.isFolder === 1) {
      // 分类节点不加载内容，但顺手把展开/折叠切一下，让用户点分类有反馈
      treeRef.value?.toggleExpand?.(node.id);
      return;
    }

    selectedKeysRef.value = [node.id];
    contentRef.value?.loadContent(node.id);
  };
</script>

<template>
  <PageWrapper contentClass="flex" dense>
    <div
      class="border-border bg-card mr-2 h-full rounded-[var(--radius)] border p-2 md:w-1/3 xl:w-1/3"
    >
      <ApiSelect
        style="width: 100%"
        v-model:value="docAppIdRef"
        :api="findAppList"
        value-field="id"
        label-field="appName"
        @options-change="handleOptionsChange"
        @change="handleChange"
      />
      <BasicTree
        ref="treeRef"
        :field-names="{ key: 'id', title: 'docTitle' }"
        :click-row-to-expand="true"
        :loading="treeLoading"
        :tree-data="docTreeDataRef"
        :selected-keys="selectedKeysRef"
        default-expand-all
        check-strictly
        highlight
        search
        toolbar
        @select="handleSelect"
      />
    </div>
    <Content class="md:w-2/3" ref="contentRef" />
  </PageWrapper>
</template>
