<template>
  <PageWrapper dense contentFullHeight>
    <div class="group-container">
      <!-- 左侧：分组树 -->
      <div class="group-tree">
        <div class="group-tree__header">
          <span class="group-tree__title">{{ t('video.device.group.groupTree') }}</span>
          <a-button
            type="link"
            size="small"
            @click="handleAddRoot"
            v-hasAnyPermission="['video:device:group:add']"
          >
            <template #icon><PlusOutlined /></template>
          </a-button>
        </div>
        <a-spin :spinning="treeLoading">
          <a-input-search
            v-model:value="searchValue"
            :placeholder="t('common.searchText')"
            style="margin-bottom: 8px"
            allow-clear
          />
          <a-tree
            v-if="treeData.length > 0"
            :tree-data="filteredTreeData"
            :field-names="{ title: 'groupName', key: 'id', children: 'children' }"
            :selected-keys="selectedKeys"
            default-expand-all
            @select="handleTreeSelect"
          >
            <template #title="{ groupName, enable }">
              <span :style="{ color: enable ? '' : '#999' }">{{ groupName }}</span>
            </template>
          </a-tree>
          <a-empty v-else :description="false" />
        </a-spin>
      </div>

      <!-- 右侧：分组详情 + 关联设备 -->
      <div class="group-content">
        <template v-if="selectedGroup">
          <!-- 分组信息卡片 -->
          <div class="group-info-card">
            <div class="group-info-card__header">
              <h3>{{ selectedGroup.groupName }}</h3>
              <div class="group-info-card__actions">
                <a-button
                  size="small"
                  @click="handleAddSub"
                  v-hasAnyPermission="['video:device:group:add']"
                >
                  <template #icon><PlusOutlined /></template>
                  {{ t('video.device.group.addSubGroup') }}
                </a-button>
                <a-button
                  size="small"
                  @click="handleEdit(selectedGroup)"
                  v-hasAnyPermission="['video:device:group:edit']"
                >
                  <template #icon><EditOutlined /></template>
                  {{ t('video.device.group.editGroup') }}
                </a-button>
                <a-popconfirm
                  :title="t('common.tips.confirmDelete')"
                  @confirm="handleDelete(selectedGroup)"
                >
                  <a-button
                    size="small"
                    danger
                    v-hasAnyPermission="['video:device:group:delete']"
                  >
                    <template #icon><DeleteOutlined /></template>
                    {{ t('video.device.group.deleteGroup') }}
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
            <a-descriptions :column="3" size="small" bordered>
              <a-descriptions-item :label="t('video.device.group.groupType')">
                {{ selectedGroup.echoMap?.groupType || '-' }}
              </a-descriptions-item>
              <a-descriptions-item :label="t('video.device.group.sortOrder')">
                {{ selectedGroup.sortOrder ?? '-' }}
              </a-descriptions-item>
              <a-descriptions-item :label="t('video.device.group.enable')">
                <a-tag :color="selectedGroup.enable ? 'success' : 'default'">
                  {{ selectedGroup.enable ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item :label="t('video.device.group.remark')" :span="3">
                {{ selectedGroup.remark || '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- 关联设备列表 -->
          <div class="group-device-list">
            <div class="group-device-list__header">
              <h4>{{ t('video.device.group.deviceList') }}</h4>
              <a-button
                type="primary"
                size="small"
                @click="handleBindDevice"
                v-hasAnyPermission="['video:device:group:bind']"
              >
                <template #icon><LinkOutlined /></template>
                {{ t('video.device.group.bindDevice') }}
              </a-button>
            </div>
            <a-spin :spinning="deviceLoading">
              <a-table
                v-if="deviceList.length > 0"
                :columns="deviceColumns"
                :data-source="deviceList"
                :pagination="false"
                size="small"
                row-key="id"
                bordered
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'action'">
                    <a-popconfirm
                      :title="t('video.device.group.confirmUnbind')"
                      @confirm="handleUnbind(record)"
                    >
                      <a-button
                        type="link"
                        danger
                        size="small"
                        v-hasAnyPermission="['video:device:group:unbind']"
                      >
                        {{ t('video.device.group.unbindDevice') }}
                      </a-button>
                    </a-popconfirm>
                  </template>
                </template>
              </a-table>
              <a-empty v-else :description="t('video.device.group.noDeviceBound')" />
            </a-spin>
          </div>
        </template>

        <div v-else class="group-empty">
          <a-empty :description="t('video.device.group.selectGroup')" />
        </div>
      </div>
    </div>

    <!-- 编辑分组弹窗 -->
    <EditModal @register="registerEditModal" @success="handleSuccess" />

    <!-- 绑定设备弹窗 -->
    <SelectDeviceModal @register="registerBindModal" @success="handleDeviceSelected" />
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    LinkOutlined,
  } from '@ant-design/icons-vue';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { tree, remove, detail } from '/@/api/video/device/group';
  import { listByGroup, bind, unbind } from '/@/api/video/device/groupRelation';
  import EditModal from './Edit.vue';
  import SelectDeviceModal from './SelectDeviceModal.vue';

  export default defineComponent({
    name: 'VideoDeviceGroup',
    components: {
      PageWrapper,
      EditModal,
      SelectDeviceModal,
      PlusOutlined,
      EditOutlined,
      DeleteOutlined,
      LinkOutlined,
    },
    setup() {
      const { t } = useI18n();
      const { createMessage } = useMessage();

      const treeData = ref<any[]>([]);
      const treeLoading = ref(false);
      const searchValue = ref('');
      const selectedKeys = ref<string[]>([]);
      const selectedGroup = ref<any>(null);
      const deviceList = ref<any[]>([]);
      const deviceLoading = ref(false);

      const [registerEditModal, { openModal: openEditModal }] = useModal();
      const [registerBindModal, { openModal: openBindModal }] = useModal();

      const deviceColumns = [
        {
          title: t('video.device.group.deviceIdentification'),
          dataIndex: 'deviceIdentification',
          width: 220,
        },
        {
          title: t('video.device.group.channelIdentification'),
          dataIndex: 'channelIdentification',
          width: 220,
        },
        {
          title: t('thinglinks.common.createdTime'),
          dataIndex: 'createdTime',
          width: 180,
        },
        {
          title: t('common.column.action'),
          dataIndex: 'action',
          width: 100,
          fixed: 'right' as const,
        },
      ];

      // 过滤树节点
      const filteredTreeData = computed(() => {
        if (!searchValue.value) return treeData.value;
        return filterTree(treeData.value, searchValue.value);
      });

      function filterTree(data: any[], keyword: string): any[] {
        return data
          .map((node) => {
            const children = node.children ? filterTree(node.children, keyword) : [];
            if (
              node.groupName?.includes(keyword) ||
              children.length > 0
            ) {
              return { ...node, children };
            }
            return null;
          })
          .filter(Boolean) as any[];
      }

      // 加载分组树
      async function loadTree() {
        treeLoading.value = true;
        try {
          treeData.value = await tree();
        } finally {
          treeLoading.value = false;
        }
      }

      // 加载关联设备
      async function loadDevices(groupId: string) {
        deviceLoading.value = true;
        try {
          deviceList.value = await listByGroup(groupId);
        } finally {
          deviceLoading.value = false;
        }
      }

      // 选择树节点
      async function handleTreeSelect(keys: string[]) {
        if (keys.length === 0) return;
        selectedKeys.value = keys;
        const groupDetail = await detail(keys[0]);
        selectedGroup.value = groupDetail;
        loadDevices(keys[0]);
      }

      // 新增根分组
      function handleAddRoot() {
        openEditModal(true, { type: ActionEnum.ADD });
      }

      // 新增子分组
      function handleAddSub() {
        openEditModal(true, {
          type: ActionEnum.ADD,
          parentId: selectedGroup.value?.id,
        });
      }

      // 编辑分组
      function handleEdit(record: any) {
        openEditModal(true, { record, type: ActionEnum.EDIT });
      }

      // 删除分组
      async function handleDelete(record: any) {
        await remove([record.id]);
        createMessage.success(t('common.tips.deleteSuccess'));
        selectedGroup.value = null;
        selectedKeys.value = [];
        deviceList.value = [];
        loadTree();
      }

      // 绑定设备
      function handleBindDevice() {
        openBindModal(true);
      }

      async function handleDeviceSelected(device: any) {
        try {
          await bind({
            groupId: selectedGroup.value.id,
            deviceIdentification: device.deviceIdentification,
          });
          createMessage.success(t('video.device.group.bindSuccess'));
          loadDevices(selectedGroup.value.id);
        } catch (e: any) {
          createMessage.error(e?.message || t('common.tips.operationFailed'));
        }
      }

      // 解绑设备
      async function handleUnbind(record: any) {
        await unbind(record.id);
        createMessage.success(t('video.device.group.unbindSuccess'));
        loadDevices(selectedGroup.value.id);
      }

      // 成功回调
      function handleSuccess() {
        loadTree();
        if (selectedGroup.value?.id) {
          detail(selectedGroup.value.id).then((res) => {
            selectedGroup.value = res;
          });
        }
      }

      onMounted(() => {
        loadTree();
      });

      return {
        t,
        treeData,
        treeLoading,
        searchValue,
        filteredTreeData,
        selectedKeys,
        selectedGroup,
        deviceList,
        deviceLoading,
        deviceColumns,
        registerEditModal,
        registerBindModal,
        handleTreeSelect,
        handleAddRoot,
        handleAddSub,
        handleEdit,
        handleDelete,
        handleBindDevice,
        handleDeviceSelected,
        handleUnbind,
        handleSuccess,
      };
    },
  });
</script>

<style lang="less" scoped>
  .group-container {
    display: flex;
    gap: 16px;
    height: 100%;
    min-height: 0;
  }

  .group-tree {
    width: 280px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
    overflow-y: auto;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }

    &__title {
      font-weight: 600;
      font-size: 15px;
      color: #2a3547;
    }
  }

  .group-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .group-info-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 8px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #2a3547;
      }
    }

    &__actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .group-device-list {
    flex: 1;
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
    overflow: auto;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        font-size: 15px;
        font-weight: 600;
        color: #2a3547;
      }
    }
  }

  .group-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
  }

  @media screen and (max-width: 768px) {
    .group-container {
      flex-direction: column;
    }

    .group-tree {
      width: 100%;
    }
  }
</style>
