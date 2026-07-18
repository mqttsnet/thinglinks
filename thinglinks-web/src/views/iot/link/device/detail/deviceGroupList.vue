<template>
  <div class="group-panel">
    <div class="group-toolbar">
      <div class="toolbar-title">
        <div class="title-icon"><ApartmentOutlined /></div>
        <div>
          <div class="title-text">{{ Tg('title') }}</div>
          <div class="title-sub">{{ Tg('subtitle') }}</div>
        </div>
      </div>

      <div class="toolbar-actions">
        <AInput
          v-model:value="keyword"
          allow-clear
          size="small"
          class="group-keyword"
          :placeholder="Tg('keywordPh')"
          @press-enter="handleSearch"
          @change="handleKeywordChange"
        >
          <template #prefix>
            <span class="filter-prefix">{{ Tg('keywordPrefix') }}</span>
          </template>
        </AInput>
        <AButton size="small" type="primary" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          {{ Tg('search') }}
        </AButton>
        <AButton size="small" :loading="loading" @click="loadGroups">
          <template #icon><ReloadOutlined /></template>
          {{ Tg('refresh') }}
        </AButton>
        <AButton size="small" type="primary" class="add-btn" @click="addDeviceToGroup">
          <template #icon><PlusOutlined /></template>
          {{ t('iot.link.device.device.addGroup') }}
        </AButton>
      </div>
    </div>

    <div class="group-summary">
      <div v-for="item in summaryCards" :key="item.key" class="summary-item" :class="item.key">
        <div class="summary-icon">
          <ApartmentOutlined v-if="item.key === 'total'" />
          <SearchOutlined v-else-if="item.key === 'visible'" />
          <CheckCircleOutlined v-else-if="item.key === 'enabled'" />
          <ClockCircleOutlined v-else />
        </div>
        <div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <ASpin :spinning="loading">
      <div v-if="displayRows.length === 0" class="group-empty">
        <AEmpty :description="hasActiveFilter ? Tg('emptyFiltered') : Tg('empty')">
          <template #image>
            <ApartmentOutlined class="empty-icon" />
          </template>
          <AButton v-if="!hasActiveFilter" type="primary" size="small" @click="addDeviceToGroup">
            <template #icon><PlusOutlined /></template>
            {{ t('iot.link.device.device.addGroup') }}
          </AButton>
        </AEmpty>
      </div>

      <div v-else class="group-list">
        <div
          v-for="record in displayRows"
          :key="record.id || record.groupId"
          class="group-card"
          :class="{ disabled: isGroupDisabled(record) }"
        >
          <div class="card-main">
            <div class="card-avatar">
              <ApartmentOutlined />
            </div>

            <div class="card-content">
              <div class="card-head">
                <div class="group-title">
                  <span class="group-name">{{ groupName(record) }}</span>
                  <ATag :color="isGroupDisabled(record) ? 'default' : 'success'">
                    {{ groupStatusText(record) }}
                  </ATag>
                  <ATag color="blue">{{ groupTypeText(record) }}</ATag>
                </div>
                <span class="join-time">{{ record.updatedTime || record.createdTime || '-' }}</span>
              </div>

              <div class="group-meta">
                <span>
                  {{ Tg('groupId') }}:
                  <b>{{ record.groupId || '-' }}</b>
                </span>
                <span v-if="groupInfo(record).parentId">
                  {{ Tg('parentId') }}: {{ groupInfo(record).parentId }}
                </span>
                <span v-if="groupInfo(record).sortValue != null">
                  {{ Tg('sortValue') }}: {{ groupInfo(record).sortValue }}
                </span>
              </div>

              <div class="group-desc">
                {{ groupInfo(record).description || record.remark || Tg('noDescription') }}
              </div>
            </div>
          </div>

          <div class="card-actions">
            <AButton type="link" size="small" @click="openDetail(record)">
              <template #icon><EyeOutlined /></template>
              {{ Tg('viewDetail') }}
            </AButton>
            <AButton type="link" size="small" @click="goGroupView(record)">
              <template #icon><BranchesOutlined /></template>
              {{ Tg('viewGroup') }}
            </AButton>
          </div>
        </div>
      </div>
    </ASpin>

    <div v-if="pagination.total > 0" class="group-pagination">
      <APagination
        v-model:current="pagination.current"
        v-model:pageSize="pagination.size"
        :total="pagination.total"
        show-size-changer
        show-quick-jumper
        size="small"
        :show-total="(total) => Tg('totalWithCount', { total })"
        @change="loadGroups"
        @show-size-change="loadGroups"
      />
    </div>

    <ADrawer
      v-model:visible="detailVisible"
      :title="Tg('detailTitle')"
      :width="680"
      class="group-detail-drawer"
      destroy-on-close
    >
      <template v-if="activeRecord">
        <div class="detail-section">
          <div class="detail-title">{{ Tg('groupInfo') }}</div>
          <div class="detail-grid">
            <div class="detail-item wide">
              <span>{{ Tg('groupName') }}</span>
              <b>{{ groupName(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('groupStatus') }}</span>
              <b>{{ groupStatusText(activeRecord) }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('groupType') }}</span>
              <b>{{ groupTypeText(activeRecord) }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ Tg('groupId') }}</span>
              <b>{{ activeRecord.groupId || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('parentId') }}</span>
              <b>{{ groupInfo(activeRecord).parentId || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('sortValue') }}</span>
              <b>{{ groupInfo(activeRecord).sortValue ?? '-' }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ Tg('description') }}</span>
              <b>{{ groupInfo(activeRecord).description || '-' }}</b>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-title">{{ Tg('relationshipInfo') }}</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span>{{ Tg('relationId') }}</span>
              <b>{{ activeRecord.id || '-' }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ t('iot.link.device.device.deviceIdentification') }}</span>
              <b>{{ activeRecord.deviceIdentification || deviceIdentification || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('joinedAt') }}</span>
              <b>{{ activeRecord.createdTime || '-' }}</b>
            </div>
            <div class="detail-item">
              <span>{{ Tg('updatedAt') }}</span>
              <b>{{ activeRecord.updatedTime || '-' }}</b>
            </div>
            <div class="detail-item wide">
              <span>{{ Tg('relationRemark') }}</span>
              <b>{{ activeRecord.remark || '-' }}</b>
            </div>
          </div>
        </div>

        <div class="drawer-actions">
          <AButton @click="copyGroupId(activeRecord)">
            <template #icon><CopyOutlined /></template>
            {{ Tg('copyGroupId') }}
          </AButton>
          <AButton type="primary" @click="goGroupView(activeRecord)">
            <template #icon><BranchesOutlined /></template>
            {{ Tg('viewGroup') }}
          </AButton>
        </div>
      </template>
    </ADrawer>

    <DeviceToGroup @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    Button as AButton,
    Drawer as ADrawer,
    Empty as AEmpty,
    Input as AInput,
    Pagination as APagination,
    Spin as ASpin,
    Tag as ATag,
  } from 'ant-design-vue';
  import {
    ApartmentOutlined,
    BranchesOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    CopyOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined,
  } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { page } from '/@/api/iot/link/group/deviceGroupRel';
  import { detail as groupDetail } from '/@/api/iot/link/group/deviceGroup';
  import type { DeviceGroupRelResultVO } from '/@/api/iot/link/group/model/deviceGroupRelModel';
  import type { DeviceGroupResultVO } from '/@/api/iot/link/group/model/deviceGroupModel';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import DeviceToGroup from '/@/views/iot/link/device/deviceGroup/deviceToGroup.vue';

  defineOptions({ name: 'DeviceGroupList' });

  interface GroupRelRecord extends DeviceGroupRelResultVO {
    groupName?: string;
    parentId?: string;
    sortValue?: number;
    type?: number;
    state?: boolean;
    description?: string;
  }

  const props = defineProps({
    deviceIdentification: {
      type: String,
      default: '',
    },
  });

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const router = useRouter();
  const [registerModal, { openModal }] = useModal();
  const Tg = (key: string, params?: Recordable) =>
    t(`iot.link.device.device.deviceGroupRecord.${key}`, params);

  const loading = ref(false);
  const records = ref<GroupRelRecord[]>([]);
  const keyword = ref('');
  const detailVisible = ref(false);
  const activeRecord = ref<GroupRelRecord | null>(null);
  const groupCache = reactive<Record<string, DeviceGroupResultVO>>({});
  const pagination = reactive({
    current: 1,
    size: 12,
    total: 0,
  });

  const deviceIdentification = computed(() => props.deviceIdentification);
  const hasActiveFilter = computed(() => !!keyword.value.trim());

  const displayRows = computed(() => {
    const key = keyword.value.trim().toLowerCase();
    if (!key) return records.value;
    return records.value.filter((record) =>
      [
        groupName(record),
        record.groupId,
        record.remark,
        groupInfo(record).description,
        groupInfo(record).parentId,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(key)),
    );
  });

  const summaryCards = computed(() => {
    const rows = displayRows.value;
    const enabled = rows.filter((record) => !isGroupDisabled(record)).length;
    const latest = rows[0];
    return [
      { key: 'total', label: Tg('total'), value: pagination.total },
      { key: 'visible', label: Tg('visible'), value: rows.length },
      { key: 'enabled', label: Tg('enabled'), value: enabled },
      { key: 'latest', label: Tg('latest'), value: latest ? groupName(latest) : '-' },
    ];
  });

  watch(
    () => props.deviceIdentification,
    () => {
      pagination.current = 1;
      loadGroups();
    },
  );

  onMounted(loadGroups);

  async function loadGroups(): Promise<void> {
    if (!props.deviceIdentification) return;
    loading.value = true;
    try {
      const result: any = await page({
        current: pagination.current,
        size: pagination.size,
        sort: 'updatedTime',
        order: 'descend',
        model: {
          deviceIdentification: props.deviceIdentification,
        },
      } as any);
      records.value = (result?.records || []) as GroupRelRecord[];
      pagination.total = Number(result?.total || 0);
      await hydrateGroupInfo(records.value);
    } catch (e: any) {
      records.value = [];
      pagination.total = 0;
      createMessage.error(e?.message || Tg('loadFailed'));
    } finally {
      loading.value = false;
    }
  }

  async function hydrateGroupInfo(list: GroupRelRecord[]): Promise<void> {
    const ids = Array.from(
      new Set(
        list.map((record) => String(record.groupId || '')).filter((id) => id && !groupCache[id]),
      ),
    );
    if (!ids.length) return;
    await Promise.allSettled(
      ids.map(async (id) => {
        const detail = await groupDetail(id);
        if (detail) groupCache[id] = detail;
      }),
    );
  }

  function groupInfo(record: GroupRelRecord | null): Partial<DeviceGroupResultVO> {
    if (!record) return {};
    const id = String(record.groupId || '');
    return {
      ...groupCache[id],
      ...record,
      ...pickEchoInfo(record),
    };
  }

  function pickEchoInfo(record: GroupRelRecord): Partial<DeviceGroupResultVO> {
    const echoMap = record.echoMap || {};
    return {
      groupName: echoMap.groupName || record.groupName || echoMap.groupId,
      parentId: echoMap.parentId || record.parentId,
      sortValue: echoMap.sortValue ?? record.sortValue,
      type: echoMap.type ?? record.type,
      state: echoMap.state ?? record.state,
      description: echoMap.description || record.description,
    };
  }

  function groupName(record: GroupRelRecord | null): string {
    if (!record) return '-';
    const info = groupInfo(record);
    return String(info.groupName || record.groupId || Tg('unknownGroup'));
  }

  function groupStatusText(record: GroupRelRecord): string {
    return isGroupDisabled(record) ? Tg('disabledStatus') : Tg('enabledStatus');
  }

  function isGroupDisabled(record: GroupRelRecord): boolean {
    const state = groupInfo(record).state as any;
    return state === false || state === 0 || state === '0' || state === 'false';
  }

  function groupTypeText(record: GroupRelRecord): string {
    const type = groupInfo(record).type;
    if (type == null || Number(type) === 0)
      return t('iot.link.group.deviceGroup.typeOptions.normalGroup');
    return String(type);
  }

  function handleKeywordChange(): void {
    if (!keyword.value) handleSearch();
  }

  function handleSearch(): void {
    // 当前接口只按设备标识查关系，分组名/描述来自回显或详情，因此关键字在当前页本地过滤。
  }

  function addDeviceToGroup(): void {
    openModal(true, {
      deviceIdentification: props.deviceIdentification,
    });
  }

  function goGroupView(record: GroupRelRecord): void {
    router.push({
      name: '设备分组',
      query: { id: record.groupId },
    });
  }

  function openDetail(record: GroupRelRecord): void {
    activeRecord.value = record;
    detailVisible.value = true;
  }

  function copyGroupId(record: GroupRelRecord): void {
    if (!record.groupId) return;
    handleCopyTextV2(record.groupId);
  }

  function handleSuccess(): void {
    pagination.current = 1;
    loadGroups();
  }
</script>

<style lang="less" scoped>
  .group-panel {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 420px;
  }

  .group-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    border: 1px solid #e5eaef;
    border-radius: 8px;
    background: #fff;
  }

  .toolbar-title {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 240px;
  }

  .title-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    border-radius: 8px;
    color: @primary-color;
    background: #ecf2ff;
    font-size: 17px;
  }

  .title-text {
    color: #2a3547;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.4;
  }

  .title-sub {
    margin-top: 2px;
    color: #5a6a85;
    font-size: 12px;
  }

  .toolbar-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .group-keyword {
    width: min(360px, 42vw);
    border-color: #dfe5ef;
    border-radius: 8px;
    background: #fff;
    box-shadow: none;

    &:hover,
    &:focus,
    &:focus-within {
      border-color: @primary-color;
      box-shadow: none;
    }

    :deep(.ant-input) {
      color: #2a3547;
      font-size: 13px;
    }

    :deep(.ant-input-clear-icon) {
      color: #8c97a5;
    }
  }

  .filter-prefix {
    color: @primary-color;
    font-size: 12px;
    font-weight: 600;
  }

  .add-btn {
    background: @primary-color;
    border-color: @primary-color;

    &:hover,
    &:focus {
      background: @primary-color;
      border-color: @primary-color;
    }
  }

  .group-summary {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-item {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
    padding: 14px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
  }

  .summary-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    flex-shrink: 0;
    border-radius: 8px;
    color: @primary-color;
    background: #ecf2ff;
  }

  .summary-item.visible .summary-icon {
    color: #49beff;
    background: #e8f7ff;
  }

  .summary-item.enabled .summary-icon {
    color: #13deb9;
    background: #e6fffa;
  }

  .summary-item.latest .summary-icon {
    color: #ffae1f;
    background: #fff8e6;
  }

  .summary-value {
    max-width: 180px;
    overflow: hidden;
    color: #2a3547;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.25;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .summary-label {
    margin-top: 2px;
    color: #5a6a85;
    font-size: 12px;
  }

  .group-empty {
    padding: 48px 0;
    border: 1px dashed #dfe5ef;
    border-radius: 8px;
    background: #fff;
  }

  .empty-icon {
    color: #b8c2d1;
    font-size: 44px;
  }

  .group-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .group-card {
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    gap: 12px;
    min-width: 0;
    padding: 14px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
    transition: border-color 0.18s ease, background 0.18s ease;

    &:hover {
      border-color: #d8e2f0;
      background: #fbfcfe;
    }

    &.disabled {
      background: #fbfcfe;
    }
  }

  .card-main {
    display: flex;
    gap: 12px;
    min-width: 0;
    flex: 1;
  }

  .card-avatar {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    border-radius: 8px;
    color: @primary-color;
    background: #ecf2ff;
    font-size: 17px;
  }

  .card-content {
    min-width: 0;
    flex: 1;
  }

  .card-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .group-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    min-width: 0;
  }

  .group-name {
    max-width: 260px;
    overflow: hidden;
    color: #2a3547;
    font-size: 15px;
    font-weight: 700;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .join-time {
    flex-shrink: 0;
    color: #8c97a5;
    font-size: 12px;
    white-space: nowrap;
  }

  .group-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 14px;
    margin-top: 10px;
    color: #5a6a85;
    font-size: 12px;

    b {
      color: #2a3547;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-weight: 600;
      word-break: break-all;
    }
  }

  .group-desc {
    display: -webkit-box;
    margin-top: 8px;
    overflow: hidden;
    color: #8c97a5;
    font-size: 12px;
    line-height: 1.5;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .card-actions {
    display: flex;
    align-items: flex-end;
    justify-content: center;
    flex-direction: column;
    gap: 4px;
    flex-shrink: 0;
  }

  .group-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 2px;
  }

  .detail-section {
    padding: 14px;
    margin-bottom: 14px;
    border: 1px solid #edf2f7;
    border-radius: 8px;
    background: #fff;
  }

  .detail-title {
    margin-bottom: 12px;
    color: #2a3547;
    font-size: 14px;
    font-weight: 700;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .detail-item {
    min-width: 0;
    padding: 10px 12px;
    border-radius: 8px;
    background: #f6f9fc;

    &.wide {
      grid-column: span 2;
    }

    span {
      display: block;
      color: #8c97a5;
      font-size: 12px;
      line-height: 1.4;
    }

    b {
      display: block;
      margin-top: 4px;
      overflow-wrap: anywhere;
      color: #2a3547;
      font-size: 13px;
      font-weight: 600;
      line-height: 1.5;
    }
  }

  .drawer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  @media (max-width: 1180px) {
    .group-toolbar {
      flex-direction: column;
    }

    .toolbar-actions {
      justify-content: flex-start;
      width: 100%;
    }

    .group-keyword {
      width: min(100%, 420px);
    }

    .group-list {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 768px) {
    .group-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .group-card {
      flex-direction: column;
    }

    .card-actions {
      align-items: flex-start;
      flex-direction: row;
      flex-wrap: wrap;
    }

    .join-time {
      white-space: normal;
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }

    .detail-item.wide {
      grid-column: span 1;
    }
  }

  @media (max-width: 560px) {
    .group-summary {
      grid-template-columns: 1fr;
    }

    .toolbar-actions {
      align-items: stretch;
      flex-direction: column;
    }

    .group-keyword {
      width: 100%;
    }
  }
</style>
