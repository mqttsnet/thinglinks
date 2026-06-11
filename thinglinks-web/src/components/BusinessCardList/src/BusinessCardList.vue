<template>
  <div class="biz-card-list">
    <div v-if="loading" class="biz-card-list__loading">
      <a-spin />
    </div>

    <!-- 头部：标题 + 按钮 -->
    <div class="biz-card-list__header">
      <span class="biz-card-list__title">{{ title }}</span>
      <div class="biz-card-list__toolbar">
        <slot name="headerExtra" />
        <a-button
          v-if="permissions?.add"
          v-hasAnyPermission="[permissions.add]"
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">
          {{ t('common.switchView') }}
        </a-button>
      </div>
    </div>

    <!-- 卡片网格 -->
    <div class="biz-card-list__content">
      <a-row v-if="dataList.length > 0" :gutter="[24, 12]">
        <a-col
          v-for="record in dataList"
          :key="record.id"
          :xs="24"
          :sm="24"
          :md="12"
          :lg="12"
          :xl="8"
          :xxl="6"
        >
          <div class="biz-card" @click="handleCardClick(record, $event)">
            <!-- 左侧信息 -->
            <div class="biz-card__info">
              <!-- 名称 -->
              <div class="biz-card__name-row">
                <a-tooltip placement="top" :title="getRecordName(record)">
                  <div class="biz-card__name">{{ getRecordName(record) }}</div>
                </a-tooltip>
              </div>

              <!-- 动态字段 -->
              <a-row :gutter="[4, 0]">
                <a-col
                  v-for="(field, idx) in normalizedFields"
                  :key="idx"
                  :span="field.span || 24"
                >
                  <div class="biz-card__field">
                    <div class="biz-card__label">{{ field.label }}</div>
                    <div class="biz-card__value" :title="getFieldValue(record, field)">
                      {{ getFieldValue(record, field) }}
                    </div>
                  </div>
                </a-col>
              </a-row>

              <!-- 操作按钮 -->
              <div class="biz-card__actions">
                <!-- 详情按钮：仅当有详情路由时显示，避免用户找不到入口 -->
                <div
                  v-if="effectiveDetailRouteName"
                  v-hasAnyPermission="permissions?.view ? [permissions.view] : []"
                  class="biz-card__action-btn"
                >
                  <a-tooltip placement="top" :title="t('common.title.view')">
                    <Icon
                      icon="ant-design:search-outlined"
                      :size="16"
                      @click.stop="handleView(record, $event)"
                    />
                  </a-tooltip>
                </div>
                <div
                  v-if="permissions?.delete"
                  v-hasAnyPermission="[permissions.delete]"
                  class="biz-card__action-btn"
                >
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <Icon icon="ant-design:delete-outlined" :size="16" @click.stop="handleDelete(record)" />
                  </a-tooltip>
                </div>
                <div
                  v-if="permissions?.edit"
                  v-hasAnyPermission="[permissions.edit]"
                  class="biz-card__action-btn"
                >
                  <a-tooltip placement="top" :title="t('common.title.edit')">
                    <Icon icon="ant-design:edit-outlined" :size="16" @click.stop="handleEdit(record)" />
                  </a-tooltip>
                </div>
                <!-- 额外操作 -->
                <template v-for="action in extraActions" :key="action.event">
                  <div
                    v-if="!action.permission || hasPermission(action.permission)"
                    :class="[
                      'biz-card__action-btn',
                      action.disabled && action.disabled(record) ? 'is-disabled' : '',
                    ]"
                  >
                    <a-tooltip placement="top" :title="action.tooltip">
                      <Icon
                        :icon="action.icon"
                        :size="action.iconSize || 16"
                        @click.stop="
                          !(action.disabled && action.disabled(record)) &&
                            handleExtraAction(action.event, record, $event)
                        "
                      />
                    </a-tooltip>
                  </div>
                </template>
                <slot name="cardActions" :record="record" />
              </div>
            </div>

            <!-- 右侧图片 -->
            <div class="biz-card__image" @click.stop="handleView(record, $event)">
              <slot name="cardImage" :record="record">
                <DefaultCardSvg />
              </slot>
            </div>

            <!-- 右上角徽章 -->
            <div v-if="badgeField && record[badgeField] != null" class="biz-card__badge">
              {{ getBadgeText(record) }}
            </div>

            <!-- 右下角状态标签 -->
            <div
              v-if="statusField"
              class="biz-card__status"
              :class="resolveCardStatus(record).cls"
            >
              <span class="biz-card__status-dot" />
              <span class="biz-card__status-text">{{ resolveCardStatus(record).label }}</span>
            </div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />

      <!-- 分页 -->
      <div class="biz-card-list__pagination">
        <a-pagination
          v-model:current="current"
          v-model:pageSize="pageSize"
          :page-size-options="pageSizeOptions"
          :show-total="(total) => t('component.table.total', { total })"
          :total="total"
          show-quick-jumper
          show-size-changer
          size="small"
          @change="handleChangePagination"
        />
      </div>
    </div>

    <!-- 编辑弹窗（动态组件） -->
    <component
      v-if="editModal"
      :is="editModal"
      @register="registerModal"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import { useRouter } from 'vue-router';
  import { useDetailRoute } from '/@/hooks/web/usePage';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { Icon } from '/@/components/Icon';
  import DefaultCardSvg from './DefaultCardSvg.vue';
  import type { Component, PropType } from 'vue';
  import type { CardField, CardAction, CardPermissions } from './types';

  const props = defineProps({
    pageApi: { type: Function as PropType<(...args: any[]) => Promise<any>>, required: true },
    deleteApi: { type: Function as PropType<(id: any) => Promise<any>>, default: undefined },
    title: { type: String, default: '' },
    searchData: { type: Object, default: () => ({}) },
    nameField: { type: String, default: 'name' },
    nameFallback: { type: String, default: undefined },
    fields: { type: Array as PropType<CardField[]>, default: () => [] },
    statusField: { type: String, default: undefined },
    statusOnlineValue: { type: [Boolean, String, Number], default: true },
    statusOnlineLabel: { type: String, default: undefined },
    statusOfflineLabel: { type: String, default: undefined },
    // 多态状态解析器(可选):传入则按其返回 { label, cls } 渲染,支持在线/离线/未连接等多态;
    // 不传则回退到 statusOnlineValue 二态判定,保持既有调用方兼容。
    statusResolver: {
      type: Function as PropType<(record: any) => { label: string; cls: string }>,
      default: undefined,
    },
    badgeField: { type: String, default: undefined },
    badgeDictType: { type: String, default: undefined },
    permissions: { type: Object as PropType<CardPermissions>, default: () => ({}) },
    detailRouteName: { type: String, default: undefined },
    editModal: { type: Object as PropType<Component>, default: undefined },
    extraActions: { type: Array as PropType<CardAction[]>, default: () => [] },
  });

  const emit = defineEmits(['input', 'add', 'edit', 'view', 'delete', 'extraAction']);

  const { t } = useI18n();
  const { push } = useRouter();
  const { detailRouteName: autoDetailRouteName, goDetail } = useDetailRoute();
  const { getDictLabel } = useDict();
  const { createMessage, createConfirm } = useMessage();
  const { hasPermission: checkPermission } = usePermission();
  const [registerModal, { openModal }] = useModal();

  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);
  const current = ref<number>(1);
  const pageSize = ref<number>(20);
  const total = ref<number>(0);
  const dataList = ref<any[]>([]);
  const model = reactive<any>({});
  const loading = ref<boolean>(false);

  const normalizedFields = computed(() => props.fields);

  /**
   * 实际生效的详情路由名：优先用外部 detailRouteName，回退到自动查找。
   * 用于决定是否在卡片操作区显示"详情"按钮（无路由时隐藏）。
   */
  const effectiveDetailRouteName = computed(
    () => props.detailRouteName || autoDetailRouteName.value,
  );

  const getRecordName = (record: any): string => {
    return record[props.nameField] || props.nameFallback || t('common.undefinedText');
  };

  /**
   * 取嵌套路径的值，支持 'echoMap.dataSourceId' 之类的点号路径。
   *
   * @param record 数据记录
   * @param path 字段路径（如 'name' 或 'echoMap.dataSourceId'）
   */
  const getNestedValue = (record: any, path: string): any => {
    if (!record || !path) return undefined;
    if (!path.includes('.')) return record[path];
    return path.split('.').reduce((obj, key) => (obj == null ? undefined : obj[key]), record);
  };

  const getFieldValue = (record: any, field: CardField): string => {
    const val = getNestedValue(record, field.field);
    if (val == null) return '';
    if (field.dictType) {
      return getDictLabel(field.dictType, String(val)) || String(val);
    }
    return String(val);
  };

  const getBadgeText = (record: any): string => {
    const val = record[props.badgeField!];
    if (val == null) return '';
    if (props.badgeDictType) {
      return getDictLabel(props.badgeDictType, String(val)) || String(val);
    }
    return String(val);
  };

  const hasPermission = (code: string): boolean => {
    return checkPermission([code]);
  };

  /**
   * 把任意类型的状态值标准化成 boolean。
   * 后端可能返回 boolean / number(1|0) / string("true"|"false"|"1"|"0"|"yes"|"no")
   * 直接 `===` 比较容易在类型不一致时误判为 offline，统一规整后再比。
   */
  const normalizeBool = (v: any): boolean => {
    if (v == null) return false;
    if (typeof v === 'boolean') return v;
    if (typeof v === 'number') return v !== 0;
    if (typeof v === 'string') {
      const s = v.toLowerCase().trim();
      return s === 'true' || s === '1' || s === 'yes' || s === 'y' || s === 'online';
    }
    return Boolean(v);
  };

  const isOnlineStatus = (record: any): boolean => {
    if (!props.statusField) return false;
    return normalizeBool(record[props.statusField]) === normalizeBool(props.statusOnlineValue);
  };

  // 状态标签渲染:优先用 statusResolver(多态),否则回退二态在线/离线。
  const resolveCardStatus = (record: any): { label: string; cls: string } => {
    if (props.statusResolver) {
      return props.statusResolver(record);
    }
    return isOnlineStatus(record)
      ? { label: props.statusOnlineLabel || t('thinglinks.common.yes'), cls: 'online' }
      : { label: props.statusOfflineLabel || t('thinglinks.common.no'), cls: 'offline' };
  };

  // 数据获取：model 是 reactive 容器，通过 Object.assign 整段替换搜索参数
  const getList = async (p: number, size: number) => {
    loading.value = true;
    try {
      const res = await props.pageApi({
        current: p,
        size,
        ...handleFetchParams(model ?? {}),
      });
      total.value = res.total;
      dataList.value = res.records;
    } finally {
      loading.value = false;
    }
  };

  const handleChangePagination = (p: number, size: number) => {
    current.value = p;
    pageSize.value = size;
    // 翻页 / 改每页条数都要重新拉数据，否则视图卡住不刷新
    getList(p, size);
  };

  // CRUD
  const handleAdd = () => {
    if (props.editModal) {
      openModal(true, { type: ActionEnum.ADD });
    }
    emit('add');
  };

  const handleEdit = (record: any) => {
    if (props.editModal) {
      openModal(true, { record, type: ActionEnum.EDIT });
    }
    emit('edit', record);
  };

  const handleView = (record: any, e: MouseEvent) => {
    e?.stopPropagation();
    // 优先使用外部传入的路由名，否则自动查找
    const routeName = props.detailRouteName || autoDetailRouteName.value;
    if (routeName) {
      push({ name: routeName, params: { id: record.id } });
    }
    emit('view', record);
  };

  const handleCardClick = (record: any, e: MouseEvent) => {
    // 仅在有详情路由时整卡片可点击
  };

  const handleDelete = (record: any) => {
    if (!props.deleteApi) {
      emit('delete', record);
      return;
    }
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async () => {
        try {
          await props.deleteApi!(record.id);
          createMessage.success(t('common.tips.deleteSuccess'));
          getList(current.value, pageSize.value);
        } catch (e) {
          throw new Error(e as string);
        }
      },
    });
  };

  const handleExtraAction = (event: string, record: any, e: MouseEvent) => {
    e?.stopPropagation();
    emit('extraAction', { event, record });
  };

  const handleSuccess = () => {
    getList(current.value, pageSize.value);
  };

  const switchView = () => {
    emit('input', false);
  };

  // 监听搜索条件变化：用 reactive 容器 model 同步搜索参数（先清旧、再合并新），
  // 避免直接 `model.value = X` 这种把 reactive 当 ref 用的反模式导致 getList 拿不到最新值。
  watch(
    () => props.searchData,
    () => {
      Object.keys(model).forEach((k) => delete model[k]);
      Object.assign(model, props.searchData ?? {});
      current.value = 1;
      pageSize.value = 20;
      getList(current.value, pageSize.value);
    },
    { deep: true, immediate: true },
  );

  // 暴露刷新方法
  defineExpose({ reload: () => getList(current.value, pageSize.value) });
</script>

<style lang="less" scoped>
  .biz-card-list {
    background-color: #f5f7fa;
    padding: 24px;
    min-height: 100%;

    &__loading {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 10;
    }

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
      flex-wrap: wrap;
      gap: 8px;
    }

    &__title {
      font-weight: 600;
      font-size: 17px;
      color: #2a3547;
    }

    &__toolbar {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    &__content {
      display: flex;
      flex-direction: column;
    }

    &__pagination {
      align-self: flex-end;
      margin-top: 20px;
    }
  }

  .biz-card {
    display: flex;
    position: relative;
    background: #fff;
    border: none;
    padding: 20px;
    border-radius: 16px;
    min-height: 200px;
    height: 100%;
    overflow: hidden;
    transition: all 0.25s ease;
    cursor: default;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08), 0 8px 24px rgba(0, 0, 0, 0.04);
    }

    &__info {
      flex: 1;
      min-width: 0;
      max-width: calc(100% - 110px);
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    &__name-row {
      margin-bottom: 12px;
    }

    &__name {
      font-weight: 600;
      font-size: 15px;
      color: #2a3547;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      line-height: 1.4;
    }

    &__field {
      margin-bottom: 6px;
      overflow: hidden;

      .biz-card__label {
        font-size: 11px;
        color: #a0aec0;
        white-space: nowrap;
        text-transform: uppercase;
        letter-spacing: 0.3px;
        margin-bottom: 1px;
      }

      .biz-card__value {
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
        font-size: 13px;
        color: #4a5568;
        max-width: 100%;
        font-weight: 500;
      }
    }

    &__actions {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: auto;
      padding-top: 12px;
    }

    &__action-btn {
      cursor: pointer;
      color: #718096;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      width: 30px;
      height: 30px;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        color: #5d87ff;
        background-color: rgba(93, 135, 255, 0.08);
      }

      &.is-disabled {
        cursor: not-allowed;
        color: #c2cfe0;
        opacity: 0.5;
        pointer-events: none;

        &:hover {
          color: #c2cfe0;
          background-color: transparent;
        }
      }
    }

    &__image {
      width: 90px;
      height: 90px;
      flex-shrink: 0;
      cursor: pointer;
      position: absolute;
      right: 20px;
      top: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      background: linear-gradient(135deg, #eef2ff 0%, #e8f4fd 100%);
      border-radius: 14px;
      padding: 16px;

      :deep(svg) {
        width: 100%;
        height: 100%;
        opacity: 0.7;
      }

      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    &__badge {
      position: absolute;
      top: 0;
      right: 0;
      border-radius: 0 16px 0 12px;
      background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
      color: #5d87ff;
      padding: 3px 10px;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.2px;
    }

    &__status {
      position: absolute;
      bottom: 16px;
      right: 16px;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      padding: 3px 10px;
      border-radius: 20px;
      font-size: 11px;
      font-weight: 600;
      line-height: 16px;
      white-space: nowrap;

      &.online {
        background-color: #ebfaf2;
        color: #13deb9;
      }

      &.offline {
        background-color: #fef5e5;
        color: #ffae1f;
      }

      &.unconnected {
        background-color: #f2f4f7;
        color: #8c97a5;
      }
    }

    &__status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      flex-shrink: 0;

      .online & {
        background-color: #13deb9;
      }

      .offline & {
        background-color: #ffae1f;
      }

      .unconnected & {
        background-color: #8c97a5;
      }
    }

    &__status-text {
      flex-shrink: 0;
    }
  }

  // H5 小屏适配
  @media screen and (max-width: 768px) {
    .biz-card-list {
      padding: 12px;

      &__header {
        flex-direction: column;
        align-items: flex-start;
      }

      &__toolbar {
        width: 100%;
        justify-content: flex-end;
      }

      &__pagination {
        align-self: center;

        :deep(.ant-pagination) {
          flex-wrap: wrap;
          justify-content: center;
        }
      }
    }

    .biz-card {
      min-height: 170px;
      padding: 16px;
      border-radius: 12px;

      &__info {
        max-width: calc(100% - 90px);
      }

      &__image {
        width: 70px;
        height: 70px;
        right: 14px;
        top: 16px;
        padding: 12px;
        border-radius: 12px;
      }

      &__actions {
        gap: 2px;
      }

      &__action-btn {
        width: 28px;
        height: 28px;
      }

      &__name {
        font-size: 14px;
      }

      &__field {
        .biz-card__label {
          font-size: 10px;
        }

        .biz-card__value {
          font-size: 12px;
        }
      }
    }
  }

  // 超小屏（<480px）
  @media screen and (max-width: 480px) {
    .biz-card {
      min-height: 150px;

      &__info {
        max-width: calc(100% - 70px);
      }

      &__image {
        width: 56px;
        height: 56px;
        right: 10px;
        top: 12px;
        padding: 10px;
        border-radius: 10px;
      }

      &__status {
        bottom: 12px;
        right: 12px;
        font-size: 10px;
        padding: 2px 8px;
      }
    }
  }
</style>
