<template>
  <PageWrapper dense contentFullHeight class="version-overview-page">
    <!-- 顶部 5 个统计卡片 -->
    <div class="stats-row">
      <a-card class="stat-card stat-total" :bordered="false" size="small">
        <a-statistic
          :title="t('iot.link.product.versionOverview.stats.productTotal')"
          :value="stats.productTotal ?? 0"
          :loading="statsLoading"
        >
          <template #prefix><AppstoreOutlined class="stat-icon" /></template>
        </a-statistic>
      </a-card>
      <a-card class="stat-card stat-published" :bordered="false" size="small">
        <a-statistic
          :title="t('iot.link.product.versionOverview.stats.published')"
          :value="stats.publishedProductCount ?? 0"
          :loading="statsLoading"
          value-style="color:#52c41a"
        >
          <template #prefix><CheckCircleFilled class="stat-icon" /></template>
        </a-statistic>
      </a-card>
      <a-card class="stat-card stat-canary" :bordered="false" size="small">
        <a-statistic
          :title="t('iot.link.product.versionOverview.stats.canary')"
          :value="stats.canaryProductCount ?? 0"
          :loading="statsLoading"
          value-style="color:#fa8c16"
        >
          <template #prefix><BranchesOutlined class="stat-icon" /></template>
        </a-statistic>
      </a-card>
      <a-card class="stat-card stat-unpublished" :bordered="false" size="small">
        <a-statistic
          :title="t('iot.link.product.versionOverview.stats.unpublished')"
          :value="stats.unpublishedProductCount ?? 0"
          :loading="statsLoading"
          value-style="color:#8c8c8c"
        >
          <template #prefix><InboxOutlined class="stat-icon" /></template>
        </a-statistic>
      </a-card>
      <a-card class="stat-card stat-recent" :bordered="false" size="small">
        <a-statistic
          :title="t('iot.link.product.versionOverview.stats.recent7d')"
          :value="stats.recentPublishCount7d ?? 0"
          :loading="statsLoading"
          value-style="color:#1677ff"
        >
          <template #prefix><RocketOutlined class="stat-icon" /></template>
          <template #suffix>
            <span class="suffix-hint">{{ t('iot.link.product.versionOverview.stats.recent7dSuffix') }}</span>
          </template>
        </a-statistic>
      </a-card>
    </div>

    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button preIcon="ant-design:reload-outlined" @click="reloadAll">
          {{ t('common.title.refresh') }}
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'productName'">
          <span class="product-name-cell">
            <ThumbUrl
              v-if="record.icon"
              :fileId="record.icon"
              :imageStyle="{ 'max-width': '32px', 'max-height': '32px' }"
            />
            <strong>{{ record.productName }}</strong>
          </span>
        </template>

        <template v-if="column.dataIndex === 'productIdentification'">
          <code class="ident-code">{{ record.productIdentification }}</code>
        </template>

        <template v-if="column.dataIndex === 'currentVersion'">
          <SnapshotIdTag
            v-if="record.activeVersionNo"
            :value="record.activeVersionNo"
            color="green"
          />
          <a-tag v-else color="default">
            {{ t('iot.link.product.versionOverview.neverPublished') }}
          </a-tag>
        </template>

        <template v-if="column.dataIndex === 'previousFullVersionNo'">
          <SnapshotIdTag
            :value="record.previousFullVersionNo"
            color="gold"
            fallback="—"
          />
        </template>

        <template v-if="column.dataIndex === 'productStatus'">
          <a-badge
            :status="record.productStatus == 0 ? 'success' : 'default'"
            :text="getDictLabel('LINK_PRODUCT_STATUS', record.productStatus, '—')"
          />
        </template>

        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="goToVersionTab(record)">
            <SettingOutlined />
            {{ t('iot.link.product.versionOverview.gotoDetail') }}
          </a-button>
        </template>
      </template>
    </BasicTable>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, BasicColumn, useTable } from '/@/components/Table';
  import {
    CheckCircleOutlined,
    CheckCircleFilled,
    SettingOutlined,
    AppstoreOutlined,
    BranchesOutlined,
    InboxOutlined,
    RocketOutlined,
  } from '@ant-design/icons-vue';
  import ThumbUrl from '/@/components/Upload/src/ThumbUrl.vue';
  import { SnapshotIdTag } from '/@/components/iot';
  import { useDict } from '/@/components/Dict';
  import { page as getProductPage } from '/@/api/iot/link/product/product';
  import { versionStatistics } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionStatisticsResultVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { handleFetchParams } from '/@/utils/thinglinks/common';

  const { t } = useI18n();
  const router = useRouter();
  const { getDictLabel } = useDict();

  // ── 顶部统计 ──
  const statsLoading = ref(false);
  const stats = reactive<ProductVersionStatisticsResultVO>({});

  async function loadStats() {
    statsLoading.value = true;
    try {
      const res: any = await versionStatistics();
      Object.assign(stats, res ?? {});
    } finally {
      statsLoading.value = false;
    }
  }

  onMounted(() => {
    loadStats();
  });

  function columns(): BasicColumn[] {
    return [
      { title: t('iot.link.product.versionOverview.col.productName'), dataIndex: 'productName', width: 220 },
      { title: t('iot.link.product.versionOverview.col.productIdentification'), dataIndex: 'productIdentification', width: 200 },
      { title: t('iot.link.product.versionOverview.col.currentVersion'), dataIndex: 'currentVersion', width: 180 },
      { title: t('iot.link.product.versionOverview.col.previousFullVersionNo'), dataIndex: 'previousFullVersionNo', width: 160 },
      { title: t('iot.link.product.versionOverview.col.productStatus'), dataIndex: 'productStatus', width: 100 },
      { title: t('iot.link.product.product.appId'), dataIndex: 'appId', width: 120 },
      { title: t('iot.link.product.product.manufacturerName'), dataIndex: 'manufacturerName', ellipsis: true },
      { title: t('common.column.action'), dataIndex: 'action', width: 140, fixed: 'right' },
    ];
  }

  const searchFormSchema = [
    {
      field: 'productName',
      label: t('iot.link.product.product.productName'),
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'productIdentification',
      label: t('iot.link.product.product.productIdentification'),
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'productStatus',
      label: t('iot.link.product.product.productStatus'),
      component: 'Select',
      colProps: { span: 6 },
      componentProps: { dictType: 'LINK_PRODUCT_STATUS' },
    },
  ];

  const [registerTable, { reload }] = useTable({
    api: getProductPage,
    immediate: true,
    rowKey: 'id',
    columns: columns(),
    formConfig: {
      labelWidth: 100,
      schemas: searchFormSchema,
    },
    useSearchForm: true,
    beforeFetch: handleFetchParams,
    showIndexColumn: false,
    showTableSetting: true,
    bordered: false,
    canResize: false,
  });

  /** 跳到产品详情页 tab6 = 发布管理(走路由名,与产品列表进详情保持一致)。 */
  function goToVersionTab(record: any) {
    router.push({
      name: '产品详情',
      params: { id: record.id },
      query: { tab: '6' },
    });
  }

  /** 工具栏刷新按钮同时刷统计 + 表格。 */
  function reloadAll() {
    loadStats();
    reload();
  }
</script>

<style lang="less" scoped>
  .version-overview-page {
    .stats-row {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 12px;
      padding: 12px 16px 0;

      @media (max-width: 1200px) {
        grid-template-columns: repeat(3, 1fr);
      }

      @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    .stat-card {
      background: #fff;
      border: 1px solid #eef0f4;
      border-radius: 12px;
      transition: box-shadow 0.2s ease, border-color 0.2s ease, transform 0.2s ease;

      &:hover {
        border-color: #e2e6ee;
        box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
        transform: translateY(-2px);
      }

      &.stat-total      { border-left: 3px solid #1677ff; }
      &.stat-published  { border-left: 3px solid #52c41a; }
      &.stat-canary     { border-left: 3px solid #fa8c16; }
      &.stat-unpublished{ border-left: 3px solid #8c8c8c; }
      &.stat-recent     { border-left: 3px solid #722ed1; }

      .stat-icon {
        margin-right: 6px;
        font-size: 20px;
      }

      :deep(.ant-statistic-title) {
        font-size: 13px;
        color: var(--text-secondary, #595959);
        margin-bottom: 6px;
      }

      :deep(.ant-statistic-content) {
        font-size: 22px;
        font-weight: 600;
      }

      .suffix-hint {
        font-size: 12px;
        color: var(--text-tertiary, #8c8c8c);
        margin-left: 6px;
      }
    }

    .product-name-cell {
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }

    .ident-code {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 12px;
      padding: 2px 8px;
      background: var(--background-secondary, #f5f5f5);
      border-radius: 4px;
      color: var(--text-secondary, #595959);
    }
  }
</style>
