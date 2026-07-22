<template>
  <!-- ─────── 模型定义 tab(Flexy 风格,左右两栏独立滚动) ─────── -->
  <div :class="['md-wrap', { 'is-side-collapsed': state.serviceCollapsed }]">
    <!-- ============ 左侧:服务列表卡片 ============ -->
    <div class="md-side">
      <div class="md-side-head">
        <span v-if="!state.serviceCollapsed" class="md-side-title">
          <ApiOutlined class="title-icon" />
          {{ t('iot.link.productService.productService.serviceList') }}
        </span>
        <span v-else class="md-side-icon">
          <ApiOutlined />
        </span>
        <div class="md-side-actions">
          <a-tooltip v-if="!state.serviceCollapsed" :title="t('common.title.add')">
            <a-button type="primary" size="small" shape="circle" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
            </a-button>
          </a-tooltip>
          <a-tooltip v-if="!state.serviceCollapsed" :title="t('common.title.refresh')">
            <a-button size="small" shape="circle" @click="handleList">
              <template #icon><RedoOutlined /></template>
            </a-button>
          </a-tooltip>
          <a-tooltip
            :title="
              state.serviceCollapsed
                ? t('iot.link.productService.productService.expandServicePanel')
                : t('iot.link.productService.productService.collapseServicePanel')
            "
          >
            <a-button
              size="small"
              shape="circle"
              :aria-label="
                state.serviceCollapsed
                  ? t('iot.link.productService.productService.expandServicePanel')
                  : t('iot.link.productService.productService.collapseServicePanel')
              "
              @click="toggleServiceCollapsed"
            >
              <template #icon>
                <MenuUnfoldOutlined
                  v-if="state.serviceCollapsed"
                  :aria-label="t('iot.link.productService.productService.expandServicePanel')"
                />
                <MenuFoldOutlined
                  v-else
                  :aria-label="t('iot.link.productService.productService.collapseServicePanel')"
                />
              </template>
            </a-button>
          </a-tooltip>
        </div>
      </div>

      <div v-if="!state.serviceCollapsed" class="md-side-list">
        <a-empty
          v-if="!state.list.length"
          class="md-empty"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
          :description="t('iot.link.productService.productService.emptyService')"
        />
        <div
          v-for="item in state.list"
          :key="item.id"
          :class="['svc-card', { active: item.id === state.serviceId }]"
          @click="changeService(item.id)"
        >
          <div class="svc-card-head">
            <span class="svc-name" :title="item.serviceName">{{ item.serviceName }}</span>
            <!-- 选中态由右上角 svc-card-actions(编辑/删除)占用,status icon 让位避免重叠 -->
            <span
              v-if="item.id !== state.serviceId"
              :class="['svc-status', item.serviceStatus == 0 ? 'on' : 'off']"
            >
              <LinkOutlined v-if="item.serviceStatus == 0" />
              <DisconnectOutlined v-else />
            </span>
          </div>
          <div class="svc-card-meta">
            <span class="svc-type-chip">{{
              getDictLabel('LINK_PRODUCT_SERVICE_TYPE', item.serviceType, '—')
            }}</span>
          </div>
          <a-tooltip v-if="item.description" :title="item.description" placement="right">
            <p class="svc-desc">{{ item.description }}</p>
          </a-tooltip>

          <!-- 选中时浮现编辑 / 删除小按钮 -->
          <div v-if="item.id === state.serviceId" class="svc-card-actions" @click.stop>
            <a-tooltip :title="t('common.title.edit')">
              <a-button type="text" size="small" @click="handleEdit($event)">
                <template #icon><EditOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip :title="t('common.title.delete')">
              <a-button type="text" size="small" danger @click="handleDelete($event)">
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-tooltip>
          </div>
        </div>
      </div>

      <div v-else class="md-side-rail">
        <a-empty
          v-if="!state.list.length"
          class="md-empty md-empty-rail"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
          :description="false"
        />
        <template v-else>
          <a-tooltip
            v-for="item in state.list"
            :key="item.id"
            :title="item.serviceName"
            placement="right"
          >
            <button
              type="button"
              :class="['svc-rail-item', { active: item.id === state.serviceId }]"
              @click="changeService(item.id)"
            >
              <span :class="['svc-rail-dot', item.serviceStatus == 0 ? 'on' : 'off']"></span>
              <span class="svc-rail-text">{{ getServiceInitial(item.serviceName) }}</span>
            </button>
          </a-tooltip>
        </template>
      </div>
    </div>

    <!-- ============ 右侧:属性 / 命令 内容区 ============ -->
    <div class="md-main">
      <template v-if="state.serviceId">
        <a-tabs
          v-model:activeKey="state.type"
          class="md-tabs"
          size="middle"
          tabBarStyle="margin-bottom: 12px"
        >
          <a-tab-pane :key="1">
            <template #tab>
              <span class="md-tab-label">
                <UnorderedListOutlined />
                {{ t('iot.link.productService.productService.attributeList') }}
              </span>
            </template>
          </a-tab-pane>
          <a-tab-pane :key="2">
            <template #tab>
              <span class="md-tab-label">
                <CodeOutlined />
                {{ t('iot.link.productService.productService.commandList') }}
              </span>
            </template>
          </a-tab-pane>
        </a-tabs>

        <div class="md-main-body">
          <property v-if="state.type === 1" :serviceId="state.serviceId" />
          <command v-else-if="state.type === 2" :serviceId="state.serviceId" />
        </div>
      </template>

      <a-empty
        v-else
        class="md-empty md-empty-pick"
        :description="t('iot.link.productService.productService.pickServiceHint')"
      />
    </div>
  </div>

  <EditModal @register="registerModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, reactive, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { Empty, Tooltip, Tabs } from 'ant-design-vue';
  import { query, remove } from '/@/api/iot/link/productService/productService';
  import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    RedoOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    LinkOutlined,
    DisconnectOutlined,
    ApiOutlined,
    UnorderedListOutlined,
    CodeOutlined,
  } from '@ant-design/icons-vue';
  import property from '/@/views/iot/link/productProperty/productProperty/index.vue';
  import command from '/@/views/iot/link/productCommand/productCommand/index.vue';
  import { ActionEnum } from '/@/enums/commonEnum';
  import EditModal from '/@/views/iot/link/product/service/Edit.vue';
  import { useDict } from '/@/components/Dict';

  const { getDictLabel } = useDict();

  export default defineComponent({
    name: 'ModelDefinition',
    components: {
      ATooltip: Tooltip,
      ATabs: Tabs,
      ATabPane: Tabs.TabPane,
      PlusOutlined,
      EditOutlined,
      DeleteOutlined,
      RedoOutlined,
      MenuFoldOutlined,
      MenuUnfoldOutlined,
      LinkOutlined,
      DisconnectOutlined,
      ApiOutlined,
      UnorderedListOutlined,
      CodeOutlined,
      command,
      property,
      EditModal,
    },
    props: {
      id: { type: String, default: '' },
    },
    emits: ['success'],
    setup(props) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();
      const { createMessage, createConfirm } = useMessage();

      const state = reactive({
        list: [] as any[],
        productId: props.id,
        serviceId: '',
        type: 1,
        serviceCollapsed: false,
      });

      async function handleList() {
        state.list = (await query({ productId: state.productId })) as any[];
        if (state.list.length > 0) {
          // 仅当当前选中的 serviceId 仍在列表里时保留;否则回退到第一项
          const stillExists = state.list.some((s) => s.id === state.serviceId);
          if (!stillExists) state.serviceId = state.list[0].id;
        } else {
          state.serviceId = '';
        }
      }

      function changeService(serviceId: string) {
        state.serviceId = serviceId;
      }

      function toggleServiceCollapsed() {
        state.serviceCollapsed = !state.serviceCollapsed;
      }

      function getServiceInitial(serviceName?: string) {
        const name = String(serviceName || '').trim();
        return name ? name.slice(0, 2) : '--';
      }

      function handleAdd() {
        openModal(true, { productId: state.productId, type: ActionEnum.ADD });
      }

      function handleEdit(e: Event) {
        e?.stopPropagation();
        if (!state.serviceId) return;
        openModal(true, {
          serviceId: state.serviceId,
          productId: state.productId,
          type: ActionEnum.EDIT,
        });
      }

      function handleSuccess() {
        handleList();
      }

      function handleDelete(e: Event) {
        e?.stopPropagation();
        if (!state.serviceId) return;
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await remove([state.serviceId]);
              createMessage.success(t('common.tips.deleteSuccess'));
              state.serviceId = '';
              handleSuccess();
            } catch (err) {
              console.error(err);
            }
          },
        });
      }

      onMounted(() => handleList());

      return {
        t,
        state,
        Empty,
        registerModal,
        handleAdd,
        handleEdit,
        handleDelete,
        handleSuccess,
        changeService,
        toggleServiceCollapsed,
        getServiceInitial,
        handleList,
        getDictLabel,
      };
    },
  });
</script>

<style lang="less" scoped>
  /* ─── 主容器:左右两栏 + 各自独立滚动 ── 父级 panel-card 已固定高度,这里 100% 撑满 ─── */
  .md-wrap {
    display: flex;
    gap: 14px;
    height: 100%;
    background: #f5f7fa;
    border-radius: 12px;
    padding: 12px;

    &.is-side-collapsed {
      gap: 10px;
    }
  }

  /* ============ 左侧服务列表 ============ */
  .md-side {
    flex: 0 0 240px;
    display: flex;
    flex-direction: column;
    min-width: 0;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
    overflow: hidden;
    transition: flex-basis 0.18s ease, width 0.18s ease;

    .is-side-collapsed & {
      flex-basis: 58px;
      width: 58px;
    }
  }

  .md-side-head {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-bottom: 1px solid #f0f2f5;

    .is-side-collapsed & {
      flex-direction: column;
      justify-content: flex-start;
      gap: 8px;
      padding: 10px 8px;
    }

    .md-side-title {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 700;
      color: #2a3547;

      .title-icon {
        color: @primary-color;
        font-size: 15px;
      }
    }

    .md-side-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 30px;
      color: @primary-color;
      background: fade(@primary-color, 8%);
      border-radius: 8px;
    }

    .md-side-actions {
      display: inline-flex;
      gap: 4px;

      .is-side-collapsed & {
        justify-content: center;
      }
    }
  }

  .md-side-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .md-side-rail {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 10px 8px;
    overflow-y: auto;
  }

  .md-empty-rail {
    margin: 10px auto;
  }

  .svc-rail-item {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    padding: 0;
    color: @text-color-secondary;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    background: #f8fafc;
    border: 1px solid @border-color-base;
    border-radius: 9px;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover,
    &:focus-visible {
      color: @primary-color;
      background: fade(@primary-color, 6%);
      border-color: fade(@primary-color, 45%);
      outline: none;
    }

    &.active {
      color: @primary-color;
      background: fade(@primary-color, 8%);
      border-color: @primary-color;
      box-shadow: 0 2px 8px fade(@primary-color, 16%);
    }
  }

  .svc-rail-dot {
    position: absolute;
    top: 5px;
    right: 5px;
    width: 6px;
    height: 6px;
    border-radius: 50%;

    &.on {
      background-color: @button-success-color;
    }

    &.off {
      background-color: @button-warn-color;
    }
  }

  .svc-rail-text {
    max-width: 24px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* ─── 服务卡片(Flexy 风格 + 选中态突出) ─── */
  .svc-card {
    position: relative;
    /* flex-shrink:0 防止父级 .md-side-list(flex column + overflow-y:auto)空间不足时
     * 把多张服务卡片"等比压扁",必须让它们保持自然高度由 overflow:auto 触发滚动 */
    flex-shrink: 0;
    padding: 12px 14px;
    border-radius: 10px;
    border: 1.5px solid transparent;
    background: #f8fafc;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
      background: #f5f7fa;
      transform: translateY(-1px);
    }

    &.active {
      background: #f5f7fa;
      border-color: @primary-color;
      box-shadow: 0 4px 12px rgb(15 23 42 / 8%);

      .svc-name {
        color: @primary-color;
      }
    }
  }

  .svc-card-head {
    display: flex;
    align-items: center;
    gap: 8px;

    .svc-name {
      flex: 1;
      font-size: 13.5px;
      font-weight: 700;
      color: #2a3547;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .svc-status {
      flex-shrink: 0;
      font-size: 13px;

      &.on {
        color: #13deb9;
      }
      &.off {
        color: #fa896b;
      }
    }
  }

  .svc-card-meta {
    margin-top: 6px;

    .svc-type-chip {
      display: inline-flex;
      padding: 1px 8px;
      border-radius: 6px;
      font-size: 11px;
      color: #5b6b82;
      background: #edf2f7;
    }
  }

  .svc-desc {
    margin: 8px 0 0;
    font-size: 11.5px;
    color: #97a1b0;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  /* 选中时浮现的编辑/删除按钮 */
  .svc-card-actions {
    position: absolute;
    top: 4px;
    right: 4px;
    display: inline-flex;
    gap: 0;

    :deep(.ant-btn) {
      width: 24px;
      height: 24px;
      padding: 0;
    }
  }

  /* ============ 右侧主内容区 ============ */
  .md-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
    padding: 12px 16px;
    overflow: hidden;
  }

  .md-tabs {
    flex-shrink: 0;
  }

  .md-tab-label {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .md-main-body {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  /* ─── 空态 ─── */
  .md-empty {
    margin: 40px auto;
  }

  .md-empty-pick {
    margin: auto;
  }

  /* H5 适配:左侧栏变成水平横向滚动 */
  @media screen and (max-width: 768px) {
    .md-wrap {
      flex-direction: column;
      height: auto;
    }
    .md-side {
      flex: 0 0 auto;
      max-height: 220px;

      .is-side-collapsed & {
        flex-basis: auto;
        width: 100%;
      }
    }
    .md-side-list {
      flex-direction: row;
      overflow-x: auto;
      overflow-y: hidden;

      .svc-card {
        flex: 0 0 200px;
      }
    }

    .md-side-rail {
      flex-direction: row;
      justify-content: flex-start;
      overflow-x: auto;
      overflow-y: hidden;
    }
  }
</style>
