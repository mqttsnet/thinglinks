<template>
  <!-- 改为 overflow-x:auto, 避免深层级菜单被左侧窄面板裁掉,允许横向滚动 -->
  <div class="def-resource-tree bg-white m-4 mr-2">
    <div class="m-4">
      <Select
        v-model:value="applicationRef"
        :disabled="data.appDisabled"
        :options="data.applicationList"
        labelInValue
        placeholder="选择应用"
        showSearch
        style="width: 100%; margin-bottom: 1rem"
        @change="handleChange"
      />
      <a-button
        v-hasAnyPermission="[RoleEnum.RESOURCE_ADD, RoleEnum.APPLICATION_RESOURCE_ADD]"
        class="mr-2"
        preIcon="ant-design:plus-outlined"
        @click="handleAdd()"
      >
        {{ t('common.title.addRoot') }}
      </a-button>
      <a-button
        v-hasAnyPermission="[RoleEnum.RESOURCE_DELETE, RoleEnum.APPLICATION_RESOURCE_DELETE]"
        class="mr-2"
        preIcon="ant-design:delete-outlined"
        @click="handleBatchDelete()"
      >
        {{ t('common.title.delete') }}
      </a-button>
      <a-button class="mr-2" preIcon="ant-design:reload-outlined" @click="fetch()">
        {{ t('common.redo') }}
      </a-button>
      <a-button class="mr-2" preIcon="ant-design:clear-outlined" @click="handleClearCache()">
        刷新缓存
      </a-button>
    </div>
    <BasicTree
      ref="treeRef"
      :actionList="actionList"
      :beforeRightClick="getRightMenuList"
      :clickRowToExpand="false"
      :loading="treeLoading"
      :title="t('devOperation.application.defResource.table.title')"
      :treeData="treeData"
      checkStrictly
      checkable
      highlight
      search
      toolbar
      @select="handleSelect"
    >
      <template #titleBefore="item">
        <template v-if="item.echoMap?.resourceType">
          <Tag
            :color="getResourceTagColor(item?.resourceType, item.isHidden)"
            :title="item.treePath"
          >
            {{ item.echoMap?.resourceType }}
          </Tag>
        </template>
      </template>
    </BasicTree>
    <MoveModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<style lang="less" scoped>
  // 深层级树 + 长名称场景:横向滚动兜底,操作按钮不再遮挡名称
  .def-resource-tree {
    overflow-x: auto;
    overflow-y: hidden;
    // 高度让外层 PageWrapper 撑满
    height: 100%;
  }
</style>
<script lang="ts">
  import { defineComponent, h, onMounted, reactive, ref, unref } from 'vue';
  import { Select, Tag } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PermModeEnum, RoleEnum } from '/@/enums/roleEnum';
  import {
    BasicTree,
    ContextMenuItem,
    TreeActionItem,
    TreeActionType,
    TreeItem,
  } from '/@/components/Tree';
  import {
    DeleteOutlined,
    DragOutlined,
    EditOutlined,
    PlusSquareOutlined,
  } from '@ant-design/icons-vue';
  import { eachTree, findNodeByKey } from '/@/utils/helper/treeHelper';
  import { getResourceTagColor } from '/@/utils/color';
  import { query } from '/@/api/devOperation/application/defApplication';
  import { remove, tree, clearCache } from '/@/api/devOperation/application/defResource';
  import { ResourceTypeEnum } from '/@/enums/biz/tenant';
  import { useModal } from '/@/components/Modal';
  import MoveModal from './Move.vue';

  export default defineComponent({
    name: 'DefResourceManagement',
    components: { BasicTree, Select, Tag, MoveModal },
    emits: ['select', 'add', 'edit', 'change'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const { currentRoute } = useRouter();
      const treeRef = ref<Nullable<TreeActionType>>(null);
      const treeData = ref<TreeItem[]>([]);
      const treeLoading = ref<boolean>(false);
      const data = reactive<Recordable>({
        applicationList: [],
        appDisabled: false,
      });
      const applicationRef = reactive<Recordable>({ value: '', label: '' });
      const [registerModal, { openModal }] = useModal();

      function getTree() {
        const tree = unref(treeRef);
        if (!tree) {
          throw new Error('树结构加载失败,请刷新页面');
        }
        return tree;
      }

      onMounted(async () => {
        const params = currentRoute.value?.params;
        applicationRef.value = params?.id as string;
        const applications = await query({ id: params?.id as string });
        data.appDisabled = params?.id ? true : false;

        data.applicationList = applications.map((item) => ({
          label: item.name,
          value: item.id,
          key: item.id,
        }));
        if (applications && applications.length > 0) {
          applicationRef.value = applications[0]?.id;
          applicationRef.label = applications[0]?.name;
          await fetch(applicationRef.value);
        }
      });

      // 加载数据
      async function fetch(applicationId?: string) {
        try {
          treeLoading.value = true;

          applicationId = applicationId || applicationRef.value;
          if (!!applicationId) {
            treeData.value = (await tree({ applicationId })) as unknown as TreeItem[];

            eachTree(
              treeData.value,
              (item, parent) => {
                item.key = item.id;
                item.title = item.name;
                item.keyLinks = [...(parent.keyLinks || []), item.id];
                item.slots = { titleBefore: 'titleBefore' };
                return item;
              },
              {},
            );
            setTimeout(() => {
              // getTree().filterByLevel(2);
              getTree().setCheckedKeys({ checked: [], halfChecked: [] });
            }, 0);
          } else {
            createMessage.warn('请先选择应用');
          }
        } finally {
          treeLoading.value = false;
        }
      }

      // 选择节点
      function handleSelect(keys: string[]) {
        if (keys[0]) {
          const node = findNodeByKey(keys[0], treeData.value);
          const parent = findNodeByKey(node?.parentId, treeData.value);
          node.applicationId = applicationRef.value;
          node.applicationName = applicationRef.label;
          emit('select', parent, node);
        }
      }

      function handleMove(current = {}) {
        openModal(true, {
          applicationId: applicationRef.value,
          current,
        });
      }

      // 悬停图标
      const actionList: TreeActionItem[] = [
        {
          auth: [RoleEnum.RESOURCE_MOVE, RoleEnum.APPLICATION_RESOURCE_MOVE],
          authMode: PermModeEnum.HasAny,
          render: (node) => {
            return h(DragOutlined, {
              class: 'ml-2',
              title: t('common.title.move'),
              onClick: (e: Event) => {
                e?.stopPropagation();
                e?.preventDefault();
                handleMove(findNodeByKey(node.id, treeData.value));
              },
            });
          },
        },
        {
          auth: [RoleEnum.RESOURCE_ADD, RoleEnum.APPLICATION_RESOURCE_ADD],
          authMode: PermModeEnum.HasAny,
          show: (node) => {
            return [ResourceTypeEnum.MENU, ResourceTypeEnum.FUNCTION].includes(node.resourceType);
          },
          render: (node) => {
            return h(PlusSquareOutlined, {
              class: 'ml-2',
              title: t('common.title.add'),
              onClick: (e: Event) => {
                e?.stopPropagation();
                e?.preventDefault();
                emit('add', findNodeByKey(node.id, treeData.value), {
                  applicationId: applicationRef.value,
                  applicationName: applicationRef.label,
                });
              },
            });
          },
        },
        {
          auth: [RoleEnum.RESOURCE_EDIT, RoleEnum.APPLICATION_RESOURCE_EDIT],
          authMode: PermModeEnum.HasAny,
          render: (node) => {
            return h(EditOutlined, {
              class: 'ml-2',
              title: t('common.title.edit'),
              onClick: (e: Event) => {
                e?.stopPropagation();
                e?.preventDefault();
                const current = findNodeByKey(node?.id, treeData.value);
                const parent = findNodeByKey(node?.parentId, treeData.value);
                current.applicationName = applicationRef.label;
                emit('edit', parent, current);
              },
            });
          },
        },
        {
          auth: [RoleEnum.RESOURCE_DELETE, RoleEnum.APPLICATION_RESOURCE_DELETE],
          authMode: PermModeEnum.HasAny,
          render: (node) => {
            return h(DeleteOutlined, {
              class: 'ml-2',
              title: t('common.title.delete'),
              style: { color: '#ED6F6F' },
              onClick: (e: Event) => {
                e?.stopPropagation();
                e?.preventDefault();
                batchDelete([node.id]);
              },
            });
          },
        },
      ];

      // 右键菜单
      function getRightMenuList(node: any): ContextMenuItem[] {
        return [
          {
            auth: [RoleEnum.RESOURCE_ADD, RoleEnum.APPLICATION_RESOURCE_ADD],
            authMode: PermModeEnum.HasAny,
            label: t('common.title.addChildren'),
            handler: () => {
              emit('add', findNodeByKey(unref(node).id, treeData.value), {
                applicationId: applicationRef.value,
                applicationName: applicationRef.label,
              });
            },
            icon: 'ant-design:plus-square-outlined',
          },
          {
            auth: [RoleEnum.RESOURCE_MOVE, RoleEnum.APPLICATION_RESOURCE_MOVE],
            authMode: PermModeEnum.HasAny,
            label: t('common.title.move'),
            handler: () => {
              handleMove(findNodeByKey(unref(node).id, treeData.value));
            },
            icon: 'ant-design:drag-outlined',
          },
          {
            label: t('common.title.edit'),
            auth: [RoleEnum.RESOURCE_EDIT, RoleEnum.APPLICATION_RESOURCE_EDIT],
            handler: () => {
              const current = findNodeByKey(unref(node)?.id, treeData.value);
              const parent = findNodeByKey(unref(node)?.parentId, treeData.value);
              current.applicationName = applicationRef.label;
              emit('edit', parent, current);
            },
            icon: 'ant-design:edit-outlined',
          },
          {
            label: t('common.title.delete'),
            handler: () => {
              batchDelete([unref(node).id]);
            },
            icon: 'ant-design:delete-outlined',
            auth: [RoleEnum.RESOURCE_DELETE, RoleEnum.APPLICATION_RESOURCE_DELETE],
            authMode: PermModeEnum.HasAny,
          },
        ];
      }

      // 执行批量删除
      async function batchDelete(ids: string[]) {
        createConfirm({
          iconType: 'warning',
          content: '选中节点及其子结点将被永久删除, 是否确定删除？',
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
        emit('add', findNodeByKey('0', treeData.value), {
          applicationId: applicationRef.value,
          applicationName: applicationRef.label,
        });
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

      async function handleChange({ value, label }) {
        applicationRef.label = label;
        applicationRef.value = value;
        await fetch(value);
        emit('change', value, label);
      }

      async function handleSuccess() {
        await fetch();
      }

      async function handleClearCache() {
        const applicationId = applicationRef.value;
        if (!applicationId) {
          createMessage.warn('请先选择应用');
          return;
        }
        try {
          await clearCache(applicationId);
          createMessage.success('缓存刷新成功');
          await fetch(applicationId);
        } catch (e) {
          createMessage.error('缓存刷新失败');
        }
      }

      return {
        t,
        RoleEnum,
        getResourceTagColor,
        treeRef,
        treeData,
        fetch,
        handleAdd,
        handleBatchDelete,
        getRightMenuList,
        actionList,
        handleSelect,
        query,
        handleChange,
        data,
        applicationRef,
        treeLoading,
        registerModal,
        handleSuccess,
        handleClearCache,
      };
    },
  });
</script>
