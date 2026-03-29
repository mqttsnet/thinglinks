<template>
  <div class="bg-white m-4 mr-2 overflow-hidden">
    <div class="m-4">
      <a-button
        @click="handleAdd()"
        preIcon="ant-design:plus-outlined"
        class="mr-2"
        v-hasAnyPermission="['link:group:deviceGroup:add']"
      >
        {{ t('common.title.addRoot') }}
      </a-button>
      <a-button
        @click="handleBatchDelete()"
        preIcon="ant-design:delete-outlined"
        class="mr-2"
        v-hasAnyPermission="['link:group:deviceGroup:delete']"
      >
        {{ t('common.title.delete') }}
      </a-button>
    </div>
    <BasicTree
      :title="t('iot.link.group.deviceGroup.table.title')"
      toolbar
      checkable
      search
      checkStrictly
      :actionList="actionList"
      :beforeRightClick="getRightMenuList"
      :clickRowToExpand="false"
      :treeData="treeData"
      :fieldNames="{ title: 'groupName' }"
      @select="handleSelect"
      ref="treeRef"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, h, onMounted, ref, unref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    BasicTree,
    TreeItem,
    ActionItem,
    TreeActionType,
    ContextMenuItem,
  } from '/@/components/Tree';
  import { findNodeByKey, eachTree } from '/@/utils/helper/treeHelper';
  import { tree, remove } from '/@/api/iot/link/group/deviceGroup';
  import { useRoute } from 'vue-router';

  export default defineComponent({
    name: 'DeviceGroupTree',
    components: { BasicTree },

    emits: ['select', 'edit', 'add'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const route = useRoute();
      const { createMessage, createConfirm } = useMessage();
      const treeRef = ref<Nullable<TreeActionType>>(null);
      const treeData = ref<TreeItem[]>([]);

      function getTree() {
        const tree = unref(treeRef);
        if (!tree) {
          throw new Error('树结构加载失败,请刷新页面');
        }
        return tree;
      }

      onMounted(async () => {
        await fetch();
        if (treeData.value?.length) {
          const initTree = route.query.id
            ? findNodeByKey(route.query.id as string, treeData.value)
            : treeData.value[0];
          emit('select', initTree);
        }
      });

      // 加载数据
      async function fetch() {
        treeData.value = (await tree()) as unknown as TreeItem[];
        eachTree(treeData.value, (item) => {
          item.key = item.id;
          item.title = item.group_name;
          return item;
        });
        setTimeout(() => {
          getTree().filterByLevel(2);
        }, 0);
      }

      // 选择节点
      function handleSelect(keys: string[]) {
        if (keys[0]) {
          const node = findNodeByKey(keys[0], treeData.value);
          const parent = findNodeByKey(node?.parentId, treeData.value);
          emit('select', parent, node);
        }
      }

      // 悬停图标
      const actionList: ActionItem[] = [
        {
          auth: 'link:group:deviceGroup:add',
          render: (node) => {
            return h(
              'a',
              {
                class: 'ml-2',
                onClick: (e: Event) => {
                  e?.stopPropagation();
                  e?.preventDefault();
                  emit('add', findNodeByKey(node.id, treeData.value));
                },
              },
              t('common.title.add'),
            );
          },
        },
        {
          auth: 'link:group:deviceGroup:edit',
          render: (node) => {
            return h(
              'a',
              {
                class: 'ml-2',
                onClick: (e: Event) => {
                  e?.stopPropagation();
                  e?.preventDefault();
                  const current = findNodeByKey(node?.id, treeData.value);
                  const parent = findNodeByKey(node?.parentId, treeData.value);
                  emit('edit', parent, current);
                },
              },
              t('common.title.edit'),
            );
          },
        },
        {
          auth: 'link:group:deviceGroup:delete',
          render: (node) => {
            return h(
              'a',
              {
                class: 'ml-2',
                onClick: (e: Event) => {
                  e?.stopPropagation();
                  e?.preventDefault();
                  batchDelete([node.id]);
                },
              },
              t('common.title.delete'),
            );
          },
        },
      ];

      // 右键菜单
      function getRightMenuList(node: any): ContextMenuItem[] {
        return [
          {
            label: t('common.title.addChildren'),
            auth: 'link:group:deviceGroup:add',
            handler: () => {
              emit('add', findNodeByKey(unref(node).id, treeData.value));
            },
            icon: 'ant-design:plus-outlined',
          },
          {
            label: t('common.title.edit'),
            auth: 'link:group:deviceGroup:edit',
            handler: () => {
              const current = findNodeByKey(unref(node).id, treeData.value);
              const parent = findNodeByKey(unref(node)?.parentId, treeData.value);
              emit('edit', parent, current);
            },
            icon: 'ant-design:edit-outlined',
          },
          {
            auth: 'link:group:deviceGroup:delete',
            label: t('common.title.delete'),
            handler: () => {
              batchDelete([unref(node).id]);
            },
            icon: 'ant-design:delete-outlined',
          },
        ];
      }

      // 执行批量删除
      async function batchDelete(ids: string[]) {
        createConfirm({
          iconType: 'warning',
          content: t('iot.link.group.deviceGroupRel.delChildNodeTip'),
          onOk: async () => {
            try {
              await remove(ids);
              createMessage.success(t('common.tips.deleteSuccess'));
              fetch();
            } catch (e) {}
          },
        });
      }

      // 点击树外面的 新增
      function handleAdd() {
        emit('add', findNodeByKey('0', treeData.value));
      }

      // 点击树外面的 批量删除
      function handleBatchDelete() {
        const { checked } = getTree().getCheckedKeys() as {
          checked: string[];
          halfChecked: string[];
        };
        if (!checked || checked.length <= 0) {
          createMessage.warning(t('common.tips.pleaseSelectTheData'));
          return;
        }
        batchDelete(checked);
      }

      return {
        t,
        treeRef,
        treeData,
        fetch,
        handleAdd,
        handleBatchDelete,
        getRightMenuList,
        actionList,
        handleSelect,
      };
    },
  });
</script>
