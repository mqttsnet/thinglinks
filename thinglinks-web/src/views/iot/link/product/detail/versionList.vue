<template>
  <!-- 裸 div ── 父级 panel-card 已给固定高度 + flex,无需 PageWrapper -->
  <div class="version-list-page">
    <!-- 顶部状态 tab -->
    <a-tabs v-model:activeKey="activeStatusKey" @change="onTabChange" class="status-tabs">
      <a-tab-pane v-for="tab in statusTabs" :key="tab.key">
        <template #tab>
          <span>
            {{ tab.label }}
            <a-badge
              v-if="tab.count > 0"
              :count="tab.count"
              :number-style="tab.key === activeStatusKey ? activeBadgeStyle : inactiveBadgeStyle"
              :overflow-count="999"
            />
          </span>
        </template>
      </a-tab-pane>
    </a-tabs>

    <!-- 工具栏:提示 + 版本号筛选 + 刷新(icon) -->
    <div class="version-toolbar">
      <span class="hint">{{ t('iot.link.product.versionList.hint') }}</span>
      <div class="toolbar-right">
        <a-select
          v-model:value="versionFilter"
          :options="versionFilterOptions"
          :placeholder="t('iot.link.product.versionList.versionFilterPlaceholder')"
          allow-clear
          size="middle"
          style="width: 260px"
          :getPopupContainer="(triggerNode: any) => triggerNode.parentNode"
        />
        <a-tooltip :title="t('common.title.refresh')">
          <a-button shape="circle" size="middle" @click="reload">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <!-- 卡片列表 -->
    <div class="version-card-list">
      <div v-if="loading" class="loading-wrap">
        <a-spin />
      </div>

      <template v-else-if="filteredRecords.length">
        <div
          v-for="rec in filteredRecords"
          :key="rec.id"
          class="version-card"
          :class="[
            'status-' + rec.versionStatus,
            { 'is-current': isCurrentEffective(rec) },
          ]"
        >
          <!-- 左色条 + 图标 -->
          <div class="card-rail">
            <component :is="getStatusIcon(rec.versionStatus)" class="rail-icon" />
          </div>

          <!-- 主体 -->
          <div class="card-body">
            <!-- 第一行:状态徽章 + 策略 / 生效标签 -->
            <div class="card-head">
              <a-badge
                :status="getStatusBadge(rec.versionStatus)"
                :text="getStatusLabel(rec.versionStatus)"
              />
              <a-tag
                v-if="rec.versionStatus !== 0 && rec.publishStrategy != null"
                :color="getStrategyColor(rec.publishStrategy)"
              >
                {{ getStrategyLabel(rec.publishStrategy) }}
              </a-tag>
              <a-tag v-if="isCurrentEffective(rec)" color="blue" class="effective-tag">
                <CheckCircleFilled />
                {{ t('iot.link.product.versionList.currentEffective') }}
              </a-tag>
              <a-tag v-if="isPreviousFull(rec)" color="gold" class="previous-tag">
                {{ t('iot.link.product.versionList.previousFull') }}
              </a-tag>
            </div>

            <!-- 第二行:完整版本序号 + 复制(SnapshotIdTag 自带复制按钮) -->
            <div class="version-block">
              <span class="version-key">{{ t('iot.link.product.product.activeVersionNo') }}</span>
              <SnapshotIdTag :value="rec.versionNo" />
            </div>

            <div v-if="rec.remark" class="card-meta">
              <FileTextOutlined class="meta-icon" />
              <span class="remark">{{ rec.remark }}</span>
            </div>

            <div class="card-foot">
              <ClockCircleOutlined class="foot-icon" />
              <span class="time">{{ formatTime(rec.publishTime || rec.createdTime) }}</span>
              <span class="dot">·</span>
              <UserOutlined class="foot-icon" />
              <span class="operator">{{ echoMapText(rec, 'createdBy') }}</span>

              <div class="actions">
                <!-- 主操作:草稿可发布 -->
                <a-button
                  v-if="rec.versionStatus === 0"
                  type="primary"
                  size="small"
                  @click="handlePublish(rec)"
                >
                  <RocketOutlined />
                  {{ t('iot.link.product.versionList.action.publish') }}
                </a-button>

                <!-- 主操作:变更记录 -->
                <a-button type="link" size="small" @click="handleChangeLog(rec)">
                  <HistoryOutlined />
                  {{ t('iot.link.product.versionList.action.changeLog') }}
                </a-button>

                <!-- 主操作:预览快照 -->
                <a-button type="link" size="small" @click="handlePreview(rec)">
                  <EyeOutlined />
                  {{ t('iot.link.product.versionList.preview.entry') }}
                </a-button>

                <!-- 次级操作收进「更多」下拉 -->
                <a-dropdown v-if="moreActions(rec).length" :trigger="['click']">
                  <a-button type="link" size="small" class="more-btn">
                    {{ t('iot.link.product.versionList.action.more') }}
                    <DownOutlined class="more-caret" />
                  </a-button>
                  <template #overlay>
                    <a-menu @click="({ key }) => onMoreClick(key as string, rec)">
                      <a-menu-item
                        v-for="act in moreActions(rec)"
                        :key="act.key"
                        :danger="act.danger"
                      >
                        <component :is="act.icon" class="menu-icon" />
                        {{ act.label }}
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </div>
          </div>
        </div>
      </template>

      <a-empty v-else :description="t('iot.link.product.versionList.empty')" />
    </div>

    <PublishModal @register="registerPublishModal" @success="reload" />
    <DiffDrawer @register="registerDiffDrawer" />
    <SnapshotPreviewModal @register="registerPreviewModal" />
    <ChangeLogDrawer @register="registerChangeLogDrawer" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import {
    EditOutlined,
    CheckCircleOutlined,
    CheckCircleFilled,
    BranchesOutlined,
    EyeOutlined,
    RollbackOutlined,
    InboxOutlined,
    RocketOutlined,
    DiffOutlined,
    DeleteOutlined,
    FileTextOutlined,
    ClockCircleOutlined,
    UserOutlined,
    HistoryOutlined,
    DownOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';
  import type { Component } from 'vue';
  import { echoMapText } from '/@/utils/echo';
  import { formatSnapshotId } from '/@/utils/iot/version';
  import { SnapshotIdTag } from '/@/components/iot';
  import { listByProduct, rollbackVersion, purgeHistoryVersion } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionResultVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import type { ProductResultVO } from '/@/api/iot/link/product/model/productModel';
  import PublishModal from './PublishModal.vue';
  import DiffDrawer from './DiffDrawer.vue';
  import SnapshotPreviewModal from './SnapshotPreviewModal.vue';
  import ChangeLogDrawer from './ChangeLogDrawer.vue';

  const props = defineProps<{
    /** 由父级 detail/index.vue 透传 ── 包含 productIdentification / activeVersionNo / previousFullVersionNo */
    productDetail: ProductResultVO;
  }>();

  /** 发布 / 回滚 / 归档会改动 product.activeVersionNo,需通知父级详情页刷新。 */
  const emit = defineEmits(['refresh']);

  /** 兼容旧调用方:从 productDetail 提取 productIdentification。 */
  const productIdentification = computed(() => props.productDetail?.productIdentification ?? '');

  const { t } = useI18n();
  const { createMessage, createConfirm } = useMessage();

  // ──────────── 状态 ────────────

  /** 状态枚举:DRAFT=0, PUBLISHED=1, CANARY=2, SHADOW=3, ROLLED_BACK=4, ARCHIVED=5。 */
  const STATUS = {
    DRAFT: 0,
    PUBLISHED: 1,
    CANARY: 2,
    SHADOW: 3,
    ROLLED_BACK: 4,
    ARCHIVED: 5,
  } as const;

  const activeStatusKey = ref<string>('ALL');
  const records = ref<ProductVersionResultVO[]>([]);
  const loading = ref(false);
  /** 产品当前正式版本号(取自 product.activeVersionNo,用于标记"最新生效")。 */
  const currentProductVersion = computed<string | undefined>(() => (props.productDetail as any)?.activeVersionNo);
  /** 灰度发布时,被切换前的全量版本(product.previousFullVersionNo)。 */
  const previousFullVersionNo = computed<string | undefined>(() => (props.productDetail as any)?.previousFullVersionNo);
  /** 产品名称(供 PublishModal 头部展示)。 */
  const productName = computed<string | undefined>(() => props.productDetail?.productName);

  // ──────────── 列表加载 ────────────

  async function loadAll() {
    if (!productIdentification.value) return;
    loading.value = true;
    try {
      const versionRes: any = await listByProduct(productIdentification.value);
      records.value = Array.isArray(versionRes) ? versionRes : [];
    } finally {
      loading.value = false;
    }
  }

  function reload() {
    loadAll();
    // 列表数据可能由发布 / 回滚 / 归档触发变化,顺带通知父级刷新产品详情(当前版本号等)
    emit('refresh');
  }

  onMounted(() => {
    loadAll();
  });

  // ──────────── tab 筛选 ────────────

  const statusTabs = computed(() => {
    const counts = countByStatus(records.value);
    return [
      { key: 'ALL', label: t('iot.link.product.versionList.tab.all'), count: records.value.length },
      { key: String(STATUS.DRAFT), label: t('iot.link.product.versionList.tab.draft'), count: counts[STATUS.DRAFT] },
      { key: String(STATUS.PUBLISHED), label: t('iot.link.product.versionList.tab.published'), count: counts[STATUS.PUBLISHED] },
      { key: String(STATUS.CANARY), label: t('iot.link.product.versionList.tab.canary'), count: counts[STATUS.CANARY] },
      { key: String(STATUS.SHADOW), label: t('iot.link.product.versionList.tab.shadow'), count: counts[STATUS.SHADOW] },
      { key: String(STATUS.ROLLED_BACK), label: t('iot.link.product.versionList.tab.rolledBack'), count: counts[STATUS.ROLLED_BACK] },
      { key: String(STATUS.ARCHIVED), label: t('iot.link.product.versionList.tab.archived'), count: counts[STATUS.ARCHIVED] },
    ];
  });

  function countByStatus(list: ProductVersionResultVO[]): Record<number, number> {
    const map: Record<number, number> = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    list.forEach((r) => {
      if (r.versionStatus != null) map[r.versionStatus] = (map[r.versionStatus] || 0) + 1;
    });
    return map;
  }

  /** 用户在工具栏选中的版本号(空 = 不过滤);跟 status tab 是 AND 关系。 */
  const versionFilter = ref<string | undefined>(undefined);

  /** 版本号下拉选项 ── 全部已加载版本,展示"短版本号"。 */
  const versionFilterOptions = computed(() =>
    records.value
      .filter((r) => !!r.versionNo)
      .map((r) => ({
        value: r.versionNo,
        label: formatSnapshotId(r.versionNo!) || r.versionNo,
      })),
  );

  const filteredRecords = computed(() => {
    let list = records.value;
    if (activeStatusKey.value !== 'ALL') {
      const target = Number(activeStatusKey.value);
      list = list.filter((r) => r.versionStatus === target);
    }
    if (versionFilter.value) {
      list = list.filter((r) => r.versionNo === versionFilter.value);
    }
    return list;
  });

  function onTabChange(_key: string | number) {
    // a-tabs change 仅切换视图,数据已在内存,不重新拉
  }

  /** active 时(选中 tab)的徽章样式:深色背景。 */
  const activeBadgeStyle = { backgroundColor: '#1677ff', color: '#fff' };
  /** 未选中 tab 的徽章样式:浅灰背景。 */
  const inactiveBadgeStyle = { backgroundColor: '#f0f0f0', color: '#595959' };

  // ──────────── 文案 / 颜色 / 图标 ────────────

  function getStatusLabel(s?: number): string {
    return t(`iot.link.product.versionList.status.${s}`);
  }

  function getStatusBadge(s?: number): 'default' | 'processing' | 'success' | 'warning' | 'error' {
    switch (s) {
      case STATUS.DRAFT: return 'default';
      case STATUS.PUBLISHED: return 'success';
      case STATUS.CANARY: return 'warning';
      case STATUS.SHADOW: return 'processing';
      case STATUS.ROLLED_BACK: return 'default';
      case STATUS.ARCHIVED: return 'default';
      default: return 'default';
    }
  }

  function getStatusIcon(s?: number) {
    switch (s) {
      case STATUS.DRAFT: return EditOutlined;
      case STATUS.PUBLISHED: return CheckCircleOutlined;
      case STATUS.CANARY: return BranchesOutlined;
      case STATUS.SHADOW: return EyeOutlined;
      case STATUS.ROLLED_BACK: return RollbackOutlined;
      case STATUS.ARCHIVED: return InboxOutlined;
      default: return EditOutlined;
    }
  }

  function getStrategyLabel(strategy?: number): string {
    if (strategy == null) return '';
    return t(`iot.link.product.versionList.strategy.${strategy}`);
  }

  function getStrategyColor(strategy?: number): string {
    switch (strategy) {
      case 0: return 'green';   // FULL
      case 1: return 'orange';  // CANARY
      case 2: return 'purple';  // SHADOW
      default: return 'default';
    }
  }


  function formatTime(t?: string): string {
    if (!t) return '—';
    return t.replace('T', ' ').slice(0, 19);
  }

  function isCurrentEffective(rec: ProductVersionResultVO): boolean {
    return !!currentProductVersion.value && rec.versionNo === currentProductVersion.value;
  }

  function isPreviousFull(rec: ProductVersionResultVO): boolean {
    return !!previousFullVersionNo.value && rec.versionNo === previousFullVersionNo.value;
  }

  /** 仅历史已发布 / 灰度 / 影子 才允许回滚;DRAFT / ARCHIVED / ROLLED_BACK / 最新生效不允许。 */
  function canRollback(rec: ProductVersionResultVO): boolean {
    if (rec.versionStatus === STATUS.DRAFT) return false;
    if (rec.versionStatus === STATUS.ARCHIVED) return false;
    if (rec.versionStatus === STATUS.ROLLED_BACK) return false;
    return !isCurrentEffective(rec);
  }

  /** 仅非最新生效 / 非草稿 / 非已归档 的版本才能归档(清理 TD 资源)。 */
  function canPurge(rec: ProductVersionResultVO): boolean {
    if (rec.versionStatus === STATUS.DRAFT) return false;
    if (rec.versionStatus === STATUS.ARCHIVED) return false;
    if (isCurrentEffective(rec)) return false;
    if (isPreviousFull(rec)) return false;
    return true;
  }

  // ──────────── 操作 ────────────

  const [registerPublishModal, { openModal: openPublishModal }] = useModal();
  const [registerDiffDrawer, { openDrawer: openDiffDrawer }] = useDrawer();
  const [registerPreviewModal, { openModal: openPreviewModal }] = useModal();
  const [registerChangeLogDrawer, { openDrawer: openChangeLogDrawer }] = useDrawer();

  /** 次级操作菜单项类型。 */
  interface MoreAction {
    key: 'diffVsCurrent' | 'diff' | 'rollback' | 'archive';
    label: string;
    icon: Component;
    danger?: boolean;
  }

  /**
   * 卡片次级操作集合(收进「更多」下拉)。
   *
   * <p>主操作(发布 / 变更记录 / 预览快照)显性展示;对比最新生效版本 / 对比上一发布 /
   * 回滚 / 归档按版本状态条件收进此处。</p>
   */
  function moreActions(rec: ProductVersionResultVO): MoreAction[] {
    const list: MoreAction[] = [];
    // 草稿专属:对比最新生效版本(产品已发布过才有意义)
    if (rec.versionStatus === STATUS.DRAFT && currentProductVersion.value) {
      list.push({
        key: 'diffVsCurrent',
        label: t('iot.link.product.versionList.diffVsCurrent'),
        icon: DiffOutlined,
      });
    }
    // 非草稿:对比上一发布
    if (rec.versionStatus !== STATUS.DRAFT) {
      list.push({
        key: 'diff',
        label: t('iot.link.product.versionList.action.diff'),
        icon: DiffOutlined,
      });
    }
    // 历史已发布 / 灰度 / 影子:可回滚
    if (canRollback(rec)) {
      list.push({
        key: 'rollback',
        label: t('iot.link.product.versionList.action.rollback'),
        icon: RollbackOutlined,
      });
    }
    // 已下线 / 历史版本:归档
    if (canPurge(rec)) {
      list.push({
        key: 'archive',
        label: t('iot.link.product.versionList.action.archive'),
        icon: DeleteOutlined,
        danger: true,
      });
    }
    return list;
  }

  /** 「更多」下拉菜单项点击分发。 */
  function onMoreClick(key: string, rec: ProductVersionResultVO) {
    if (key === 'diffVsCurrent') handleDiffVsCurrent(rec);
    else if (key === 'diff') handleDiff(rec);
    else if (key === 'rollback') handleRollback(rec);
    else if (key === 'archive') handlePurge(rec);
  }

  /** 打开变更记录抽屉(按该版本切片)。 */
  function handleChangeLog(rec: ProductVersionResultVO) {
    openChangeLogDrawer(true, {
      productIdentification: rec.productIdentification,
      versionNo: rec.versionNo,
    });
  }

  function handlePublish(rec: ProductVersionResultVO) {
    openPublishModal(true, {
      productIdentification: rec.productIdentification,
      productName: productName.value,
      currentVersion: currentProductVersion.value,
      draftVersion: rec.versionNo,
    });
  }

  function handleRollback(rec: ProductVersionResultVO) {
    createConfirm({
      iconType: 'warning',
      title: t('iot.link.product.versionList.confirm.rollbackTitle'),
      content: t('iot.link.product.versionList.confirm.rollbackContent', {
        version: formatSnapshotId(rec.versionNo),
      }),
      okText: t('iot.link.product.versionList.action.rollback'),
      onOk: async () => {
        try {
          await rollbackVersion({
            productIdentification: rec.productIdentification!,
            targetVersion: rec.versionNo!,
          });
          createMessage.success(t('iot.link.product.versionList.tip.rollbackOk'));
          reload();
        } catch (e: any) {
          createMessage.error(e?.message ?? t('iot.link.product.versionList.tip.rollbackFail'));
        }
      },
    });
  }

  function handlePurge(rec: ProductVersionResultVO) {
    createConfirm({
      iconType: 'warning',
      title: t('iot.link.product.versionList.confirm.purgeTitle'),
      content: t('iot.link.product.versionList.confirm.purgeContent', {
        version: formatSnapshotId(rec.versionNo),
      }),
      okText: t('iot.link.product.versionList.action.archive'),
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await purgeHistoryVersion({
            productIdentification: rec.productIdentification!,
            versionNo: rec.versionNo!,
          });
          createMessage.success(t('iot.link.product.versionList.tip.archiveOk'));
          reload();
        } catch (e: any) {
          createMessage.error(e?.message ?? t('iot.link.product.versionList.tip.archiveFail'));
        }
      },
    });
  }

  /** diff:当前版本 vs 上一个发布版本(按 publishTime 取更早一档)。 */
  function handleDiff(rec: ProductVersionResultVO) {
    const sourceVersion = pickPreviousPublishedVersion(rec);
    if (!sourceVersion) {
      // 已是最早一次发布 / 极端 race(rec 不在 records 里)── 给用户明确反馈而非"啥都没发生"
      createMessage.info(t('iot.link.product.versionList.tip.noPreviousPublished'));
      return;
    }
    openDiffDrawer(true, {
      productIdentification: rec.productIdentification,
      sourceVersion,
      targetVersion: rec.versionNo,
    });
  }

  /** diff:草稿 vs 最新生效版本(草稿专属快捷入口)。 */
  function handleDiffVsCurrent(rec: ProductVersionResultVO) {
    if (!currentProductVersion.value) {
      // 菜单项虽已按此条件隐藏,但极端 race(列表刷新中)下仍可能进来,显式提示而非静默 return
      createMessage.info(t('iot.link.product.versionList.tip.noLiveVersion'));
      return;
    }
    openDiffDrawer(true, {
      productIdentification: rec.productIdentification,
      sourceVersion: currentProductVersion.value,
      targetVersion: rec.versionNo,
    });
  }

  /** 预览快照:打开 SnapshotPreviewModal,直接复用 record.productSnapshotJson。 */
  function handlePreview(rec: ProductVersionResultVO) {
    openPreviewModal(true, {
      record: rec,
      productIdentification: rec.productIdentification,
      versionNo: rec.versionNo,
    });
  }

  /**
   * 取 rec 在"发布历史"上的上一档版本号。
   *
   * <p><b>排序按 publishTime DESC,而非 createdTime</b>:回滚 / 灰度 / 影子等场景下,
   * 版本的"创建时间"和"实际上线时间"可能错位 ── 例如回滚把更早创建的版本变成 live,
   * 按 createdTime 选会把"晚创建但被回滚下线"的版本当成上一发布,语义错。
   * publishTime 为空(理论上发布过的都有)的回退到 createdTime 兜底。</p>
   *
   * <p>草稿(DRAFT)无 publishTime,不在候选;其余状态(已发布 / 灰度 / 影子 / 已回滚 /
   * 已归档)都曾经"上线过",都是合法对比候选。</p>
   */
  function pickPreviousPublishedVersion(rec: ProductVersionResultVO): string | undefined {
    const published = records.value.filter((r) => r.versionStatus !== STATUS.DRAFT);
    const sorted = [...published].sort((a, b) => {
      const ta = a.publishTime || a.createdTime || '';
      const tb = b.publishTime || b.createdTime || '';
      return tb.localeCompare(ta);
    });
    const idx = sorted.findIndex((r) => r.id === rec.id);
    if (idx < 0) return undefined;
    return sorted[idx + 1]?.versionNo;
  }
</script>

<style lang="less" scoped>
  /* tab 内独立滚动 ── 父级 panel-card 已固定高度,这里 height:100% + flex 链让卡片列表自己滚 */
  /* 裸 div + flex 链:status-tabs / toolbar 自然高度,version-card-list 占余下高度内部滚 */
  .version-list-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;

    .status-tabs {
      flex-shrink: 0;
      padding: 0 16px;
      margin-bottom: 8px;

      :deep(.ant-tabs-tab) {
        padding: 12px 16px;
      }

      :deep(.ant-badge) {
        margin-left: 6px;
      }
    }

    .version-toolbar {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 4px 16px 12px;

      .hint {
        flex: 1;
        min-width: 0;
        font-size: 12px;
        color: var(--text-tertiary, #8c8c8c);
      }

      .toolbar-right {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;
      }
    }

    /* 卡片列表占余下空间内部滚 ── 必须嵌入 .version-list-page 内才能 flex:1 生效 */
    .version-card-list {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      padding: 0 16px 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .loading-wrap {
        display: flex;
        justify-content: center;
        padding: 60px 0;
      }
    }
  }

  .version-card {
    display: flex;
    /* 关键:flex-shrink:0 防止在父级 flex column 容器(.version-card-list)空间不足时被等比压扁
     * (默认 flex-shrink:1 会让所有卡片均分父高度,版本号 / 备注 / 时间 / 操作按钮全被裁掉,
     * 用户只看到 head 一行)。父级 .version-card-list 有 overflow-y:auto,总高超出时滚动。 */
    flex-shrink: 0;
    background: var(--component-background, #fff);
    border: 1px solid var(--border-color-secondary, #f0f0f0);
    border-radius: 12px;
    /* overflow:hidden 保留:卡片左侧 card-rail 渐变背景需要 + 圆角裁切 */
    overflow: hidden;
    transition: all 0.2s;

    &:hover {
      border-color: var(--border-color, #d9d9d9);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      transform: translateY(-1px);
    }

    // 最新生效:左侧蓝色加粗高亮
    &.is-current {
      border-color: rgba(22, 119, 255, 0.4);
      background: rgba(22, 119, 255, 0.02);

      .card-rail {
        border-right-width: 4px;
      }
    }

    .card-rail {
      width: 44px;
      flex-shrink: 0;
      display: flex;
      align-items: flex-start;
      justify-content: center;
      padding-top: 18px;
      border-right: 3px solid transparent;

      .rail-icon {
        font-size: 18px;
      }
    }

    // 状态色条
    &.status-0 .card-rail { // DRAFT
      background: linear-gradient(180deg, rgba(140, 140, 140, 0.1), rgba(140, 140, 140, 0.02));
      border-right-color: #8c8c8c;
      .rail-icon { color: #8c8c8c; }
    }
    &.status-1 .card-rail { // PUBLISHED
      background: linear-gradient(180deg, rgba(82, 196, 26, 0.12), rgba(82, 196, 26, 0.02));
      border-right-color: #52c41a;
      .rail-icon { color: #52c41a; }
    }
    &.status-2 .card-rail { // CANARY
      background: linear-gradient(180deg, rgba(250, 140, 22, 0.12), rgba(250, 140, 22, 0.02));
      border-right-color: #fa8c16;
      .rail-icon { color: #fa8c16; }
    }
    &.status-3 .card-rail { // SHADOW
      background: linear-gradient(180deg, rgba(114, 46, 209, 0.12), rgba(114, 46, 209, 0.02));
      border-right-color: #722ed1;
      .rail-icon { color: #722ed1; }
    }
    &.status-4 .card-rail { // ROLLED_BACK
      background: linear-gradient(180deg, rgba(191, 191, 191, 0.1), rgba(191, 191, 191, 0.02));
      border-right-color: #bfbfbf;
      .rail-icon { color: #bfbfbf; }
    }
    &.status-5 .card-rail { // ARCHIVED
      background: linear-gradient(180deg, rgba(217, 217, 217, 0.15), rgba(217, 217, 217, 0.02));
      border-right-color: #d9d9d9;
      .rail-icon { color: #bfbfbf; }
    }

    .card-body {
      flex: 1;
      padding: 16px 20px;
      min-width: 0;
    }

    .card-head {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 10px;
    }

    /* 完整版本号块:标签 + 等宽完整版本号 + 复制按钮 */
    .version-block {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 12px;
      border-radius: 8px;
      background: var(--background-secondary, #f5f7fa);
      border: 1px solid var(--border-color, #e8ecf2);
      margin-bottom: 10px;
      max-width: 100%;

      .version-key {
        flex-shrink: 0;
        font-size: 11px;
        color: #8c97a5;
        padding-right: 8px;
        border-right: 1px solid #e0e4ea;
      }

      .version-text {
        flex: 1;
        min-width: 0;
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        word-break: break-all;
      }

      .copy-btn {
        flex-shrink: 0;
        cursor: pointer;
        color: #8c97a5;
        font-size: 14px;
        transition: color 0.15s ease;

        &:hover {
          color: #5d87ff;
        }
      }
    }

    .effective-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .card-meta {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      color: var(--text-secondary, #595959);
      font-size: 13px;
      margin-bottom: 8px;

      .meta-icon {
        margin-top: 3px;
        color: var(--text-tertiary, #8c8c8c);
      }

      .remark {
        flex: 1;
        line-height: 1.5;
      }
    }

    .card-foot {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: var(--text-tertiary, #8c8c8c);

      .foot-icon {
        font-size: 12px;
      }

      .dot {
        color: var(--text-quaternary, #bfbfbf);
      }

      .actions {
        margin-left: auto;
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .more-btn {
        display: inline-flex;
        align-items: center;
        gap: 3px;

        .more-caret {
          font-size: 10px;
        }
      }
    }
  }

  .menu-icon {
    margin-right: 6px;
    color: #8c97a5;
  }
</style>
