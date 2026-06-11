<template>
  <!-- 直接裸 div ── 父级 panel-card 已给固定高度 + flex,不用 PageWrapper(它内部多层 wrapper 会断开 height 链) -->
  <div class="change-log-page">
    <div class="cl-wrap">
      <!-- ─────── 顶部工具栏:版本选择 + 刷新 ─────── -->
      <div class="cl-toolbar">
        <div class="cl-toolbar-left">
          <span class="cl-label">
            <HistoryOutlined class="cl-label-icon" />
            {{ t('iot.link.product.changeLog.selectVersion') }}
          </span>
          <a-select
            v-model:value="selectedVersionNo"
            :loading="loadingVersions"
            :options="versionOptions"
            :placeholder="t('iot.link.product.changeLog.versionPlaceholder')"
            :disabled="!versions.length"
            style="width: 360px"
            size="middle"
            optionLabelProp="label"
            show-search
            option-filter-prop="label"
            :getPopupContainer="(triggerNode: any) => triggerNode.parentNode"
          >
            <template #option="{ value, label, status, time }">
              <div class="ver-opt">
                <span :class="['ver-status', `vs-${status}`]">{{ statusLabel(status) }}</span>
                <span class="ver-no" :title="value">{{ shortVersion(label) }}</span>
                <span v-if="time" class="ver-time">{{ time }}</span>
              </div>
            </template>
          </a-select>

          <!-- 当前选中的版本状态徽章(展示在 select 旁) -->
          <span v-if="selectedVersion" :class="['cl-current-status', `vs-${selectedVersion.versionStatus ?? 0}`]">
            {{ statusLabel(selectedVersion.versionStatus ?? 0) }}
          </span>
        </div>
        <a-tooltip :title="t('common.title.refresh')">
          <a-button shape="circle" @click="reload">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>

      <!-- ─────── 提示条 ─────── -->
      <div class="cl-hint-bar">
        <InfoCircleOutlined class="hint-icon" />
        <span>{{ t('iot.link.product.changeLog.hint') }}</span>
      </div>

      <!-- ─────── 主体 ─────── -->
      <div class="cl-body">
        <div v-if="loadingVersions" class="cl-loading">
          <a-spin />
        </div>

        <!-- 无任何版本:空态 -->
        <a-empty
          v-else-if="!versions.length"
          class="cl-empty"
          :description="t('iot.link.product.changeLog.noVersion')"
        />

        <!-- 选中版本的变更记录 -->
        <ProductChangeLogPanel
          v-else-if="selectedVersionNo"
          ref="panelRef"
          :productIdentification="props.productIdentification"
          :versionNo="selectedVersionNo"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { HistoryOutlined, InfoCircleOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { ProductChangeLogPanel } from '/@/components/iot/ProductChangeLogPanel';
  import { listByProduct } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionResultVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { formatSnapshotId } from '/@/utils/iot/version';

  const props = defineProps<{
    productIdentification: string;
  }>();

  const { t } = useI18n();

  /** 该产品全部版本列表(草稿 + 已发布 + 灰度 + 已归档...);默认按 createdTime 倒序。 */
  const versions = ref<ProductVersionResultVO[]>([]);
  const loadingVersions = ref(false);
  /** 当前下拉选中的版本号。 */
  const selectedVersionNo = ref<string>('');
  const panelRef = ref<InstanceType<typeof ProductChangeLogPanel> | null>(null);

  /**
   * select 选项:展示"状态徽章 + 短版本号 + 时间"。
   *
   * <p>option 的 label 留完整 versionNo(供搜索/复制),实际显示由 #option 插槽自定义。</p>
   */
  const versionOptions = computed(() =>
    versions.value.map((v) => ({
      value: v.versionNo,
      label: v.versionNo,
      status: v.versionStatus ?? 0,
      time: formatTime(v.publishTime || v.createdTime),
    })),
  );

  /** 当前选中版本的完整 entity(用于侧标徽章 + 信息展示)。 */
  const selectedVersion = computed(() =>
    versions.value.find((v) => v.versionNo === selectedVersionNo.value),
  );

  /** 版本状态 → 字典 label,复用版本列表已有 i18n。 */
  function statusLabel(status: number): string {
    return t(`iot.link.product.versionList.status.${status}`);
  }

  /** 版本号短展示:16 位雪花太长,截断成前 4 + ... + 后 4。 */
  function shortVersion(versionNo: string): string {
    return formatSnapshotId(versionNo) || versionNo;
  }

  /** 时间格式化:取 "MM-DD HH:mm" 这种简洁形式。 */
  function formatTime(time?: string): string {
    if (!time) return '';
    const s = time.replace('T', ' ');
    return s.length >= 16 ? s.slice(5, 16) : s;
  }

  /**
   * 拉取该产品所有版本 + 设默认选中。
   *
   * <p>默认选中规则:</p>
   * <ol>
   *   <li>有草稿(DRAFT,status=0)→ 选草稿(用户最常想看的"我刚改了什么")</li>
   *   <li>无草稿但有历史版本 → 选最新一条(列表第一项)</li>
   *   <li>完全没有版本 → 空态</li>
   * </ol>
   */
  async function loadVersions() {
    if (!props.productIdentification) return;
    loadingVersions.value = true;
    try {
      const res = await listByProduct(props.productIdentification);
      const list: ProductVersionResultVO[] = Array.isArray(res) ? res : [];
      versions.value = list;
      // 仅首次 / 上次选中已失效时,才覆盖 selectedVersionNo(用户手动切换的版本不强制重置)
      const stillExists =
        selectedVersionNo.value && list.some((v) => v.versionNo === selectedVersionNo.value);
      if (!stillExists) {
        const draft = list.find((v) => v.versionStatus === 0);
        selectedVersionNo.value = draft?.versionNo ?? list[0]?.versionNo ?? '';
      }
    } finally {
      loadingVersions.value = false;
    }
  }

  /** 刷新:重拉版本列表 + 当前 panel 重拉记录。 */
  function reload() {
    loadVersions().then(() => panelRef.value?.reload?.());
  }

  onMounted(() => loadVersions());
</script>

<style lang="less" scoped>
  /* tab 内独立滚动 ── 裸 div + flex 链,父级 panel-card 已给固定 height(Pattern B) */
  .change-log-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
  }

  .cl-wrap {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    overflow: hidden;
  }

  /* ─── 工具栏:版本下拉 + 刷新 ─── */
  .cl-toolbar {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 8px 4px 8px;

    .cl-toolbar-left {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;
      min-width: 0;
    }

    .cl-label {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;

      .cl-label-icon {
        color: #5d87ff;
        font-size: 15px;
      }
    }

    .cl-current-status {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 10px;
      font-size: 12px;
      font-weight: 600;
    }
  }

  /* ─── 状态徽章(下拉选项 + 当前选中两处共用) ─── */
  .vs-0 {
    color: #5a6a85;
    background: #eef0f4;
  }
  .vs-1 {
    color: #0fb094;
    background: #ebfaf2;
  }
  .vs-2 {
    color: #5d87ff;
    background: #eef5ff;
  }
  .vs-3 {
    color: #9b75e6;
    background: #f5f0ff;
  }
  .vs-4 {
    color: #ffae1f;
    background: #fff7e6;
  }
  .vs-5 {
    color: #97a1b0;
    background: #f5f7fa;
  }

  /* ─── 下拉选项内布局 ─── */
  .ver-opt {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 2px 0;

    .ver-status {
      flex-shrink: 0;
      padding: 2px 8px;
      border-radius: 8px;
      font-size: 11px;
      font-weight: 600;
    }

    .ver-no {
      flex: 1;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 12px;
      color: #2a3547;
      letter-spacing: 0.2px;
    }

    .ver-time {
      flex-shrink: 0;
      font-size: 11px;
      color: #97a1b0;
      font-variant-numeric: tabular-nums;
    }
  }

  /* ─── 提示条 ─── */
  .cl-hint-bar {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    margin-bottom: 12px;
    border-radius: 8px;
    background: #eef5ff;
    font-size: 12px;
    color: #5a6a85;
    line-height: 1.5;

    .hint-icon {
      color: #5d87ff;
      font-size: 13px;
      flex-shrink: 0;
    }
  }

  .cl-body {
    flex: 1;
    min-height: 0;
  }

  .cl-loading {
    display: flex;
    justify-content: center;
    padding: 64px 0;
  }

  .cl-empty {
    padding: 64px 0;
  }
</style>
