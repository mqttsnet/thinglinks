<template>
  <PageWrapper dense contentClass="flex h-full">
    <DefResourceTree
      ref="treeRef"
      class="resource-tree-pane"
      :style="{ width: leftWidth + 'px' }"
      @select="handleTreeSelect"
      @add="handleTreeAdd"
      @edit="handleTreeEdit"
      @change="handlerApplicationChange"
    />
    <!-- 拖拽分隔条:调整左侧资源树面板宽度 -->
    <div
      ref="resizerRef"
      class="resource-resizer"
      :class="{ 'is-resizing': resizing }"
      title="拖拽调整资源树宽度"
      @mousedown="onResizeStart"
    />
    <Edit ref="editRef" class="resource-edit-pane overflow-y-auto" @success="handleEditSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, onBeforeUnmount, onMounted, ref, unref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { ActionEnum } from '/@/enums/commonEnum';
  import DefResourceTree from './Tree.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import Edit from './Edit.vue';
  import { ResourceTypeEnum } from '/@/enums/biz/tenant';

  /** 资源树面板宽度本地缓存键。 */
  const WIDTH_KEY = 'def-resource-tree-width';
  /** 资源树面板最小宽度(px)。 */
  const MIN_WIDTH = 240;
  /** 右侧编辑区至少保留宽度(px)。 */
  const RIGHT_MIN = 420;

  export default defineComponent({
    name: '资源管理',
    components: { Edit, DefResourceTree, PageWrapper },
    setup() {
      const editRef = ref<any>(null);
      const treeRef = ref<any>(null);
      const { createMessage } = useMessage();

      // ── 左侧资源树面板宽度:中间分隔条可拖拽调整,记忆到 localStorage ──
      const leftWidth = ref<number>(340);
      const resizerRef = ref<Nullable<HTMLElement>>(null);
      const resizing = ref<boolean>(false);
      let startX = 0;
      let startWidth = 0;
      let containerWidth = 0;

      /** 把宽度夹在 [MIN_WIDTH, 容器宽 - RIGHT_MIN] 区间内。 */
      function clampWidth(w: number): number {
        const max = containerWidth > 0 ? Math.max(MIN_WIDTH, containerWidth - RIGHT_MIN) : w;
        return Math.min(Math.max(w, MIN_WIDTH), max);
      }

      function onResizeMove(e: MouseEvent) {
        if (!resizing.value) return;
        leftWidth.value = clampWidth(startWidth + (e.clientX - startX));
      }

      function onResizeEnd() {
        if (!resizing.value) return;
        resizing.value = false;
        document.body.style.cursor = '';
        document.body.style.userSelect = '';
        window.removeEventListener('mousemove', onResizeMove);
        window.removeEventListener('mouseup', onResizeEnd);
        localStorage.setItem(WIDTH_KEY, String(leftWidth.value));
      }

      // 仅左键起拖
      function onResizeStart(e: MouseEvent) {
        if (e.button !== 0) return;
        containerWidth = unref(resizerRef)?.parentElement?.clientWidth ?? 0;
        startX = e.clientX;
        startWidth = leftWidth.value;
        resizing.value = true;
        document.body.style.cursor = 'col-resize';
        document.body.style.userSelect = 'none';
        window.addEventListener('mousemove', onResizeMove);
        window.addEventListener('mouseup', onResizeEnd);
        e.preventDefault();
      }

      onMounted(() => {
        containerWidth = unref(resizerRef)?.parentElement?.clientWidth ?? 0;
        const saved = Number(localStorage.getItem(WIDTH_KEY));
        if (saved > 0) {
          leftWidth.value = clampWidth(saved);
        } else if (containerWidth > 0) {
          // 初始宽度取容器约 30%(比原固定 25% 略宽)
          leftWidth.value = clampWidth(Math.round(containerWidth * 0.3));
        }
      });

      onBeforeUnmount(() => {
        window.removeEventListener('mousemove', onResizeMove);
        window.removeEventListener('mouseup', onResizeEnd);
      });
      // ──────────────────────────────────────────────────────────────

      // 获取编辑表单
      function getEditRef() {
        return unref(editRef);
      }
      // 获取树
      function getTreeRef() {
        return unref(treeRef);
      }

      // 编辑成功回调
      function handleEditSuccess(applicationId: string) {
        getTreeRef().fetch(applicationId);
      }

      // 选中树的节点
      function handleTreeSelect(parent = {}, record = {}) {
        getEditRef().setData({ type: ActionEnum.VIEW, parent, record });
      }

      // 编辑
      function handleTreeEdit(parent = {}, record = {}) {
        getEditRef().setData({ type: ActionEnum.EDIT, parent, record });
      }

      // 点击树的新增按钮
      function handleTreeAdd(parent = {} as { resourceType: string }, record = {}) {
        if (parent?.resourceType === ResourceTypeEnum.FIELD) {
          createMessage.warn('字段下不能添加子资源');
          getEditRef().resetForm(record);
        } else {
          getEditRef().setData({ type: ActionEnum.ADD, parent, record });
        }
      }

      function handlerApplicationChange(applicationId: string, applicationName: string) {
        getEditRef().resetForm({ applicationId, applicationName });
      }
      return {
        editRef,
        treeRef,
        leftWidth,
        resizerRef,
        resizing,
        onResizeStart,
        handleEditSuccess,
        handleTreeSelect,
        handleTreeAdd,
        handleTreeEdit,
        handlerApplicationChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  // 左侧资源树面板:宽度受 leftWidth 控制,不参与 flex 伸缩
  .resource-tree-pane {
    flex: 0 0 auto;
  }

  // 中间拖拽分隔条:调整资源树面板宽度
  .resource-resizer {
    position: relative;
    flex: 0 0 8px;
    cursor: col-resize;

    &::after {
      content: '';
      position: absolute;
      top: 0;
      bottom: 0;
      left: 50%;
      width: 2px;
      background: var(--border-color, #f0f0f0);
      transform: translateX(-50%);
      transition: background 0.2s;
    }

    &:hover::after,
    &.is-resizing::after {
      width: 3px;
      background: var(--primary-color, #1677ff);
    }
  }

  // 右侧编辑区:占据剩余空间
  .resource-edit-pane {
    flex: 1 1 0;
    min-width: 0;
  }
</style>
