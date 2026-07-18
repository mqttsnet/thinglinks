<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="t('iot.link.product.publishRecord.diff.drawerTitle')"
    width="920px"
    :closable="true"
    :maskClosable="false"
    class="diff-drawer"
  >
    <template v-if="loading">
      <div class="loading-wrap">
        <a-spin :tip="t('common.title.loading')" />
      </div>
    </template>

    <template v-else-if="diffData">
      <!-- 顶部信息卡:源 → 目标版本 + 变更统计 chip -->
      <div class="header-card">
        <div class="versions">
          <span class="version-chip from">
            <ClockCircleOutlined class="chip-icon" />
            {{ diffData.sourceVersion || t('iot.link.product.publishRecord.diff.empty') }}
          </span>
          <ArrowRightOutlined class="version-arrow" />
          <span class="version-chip to">
            <RocketOutlined class="chip-icon" />
            {{ diffData.targetVersion }}
          </span>
        </div>

        <div class="summary-chips">
          <SummaryChip
            v-if="(diffData.summary?.productInfoChanged ?? 0) > 0"
            color="processing"
            :label="t('iot.link.product.publishRecord.diff.productInfo')"
            :count="diffData.summary!.productInfoChanged!"
          />
          <SummaryChip
            v-if="totalServiceChange > 0"
            color="purple"
            :label="t('iot.link.product.publishRecord.diff.services')"
            :count="totalServiceChange"
          />
          <SummaryChip
            v-if="totalPropertyChange > 0"
            color="cyan"
            :label="t('iot.link.product.publishRecord.diff.properties')"
            :count="totalPropertyChange"
          />
          <SummaryChip
            v-if="totalCommandChange > 0"
            color="orange"
            :label="t('iot.link.product.publishRecord.diff.commands')"
            :count="totalCommandChange"
          />
          <div v-if="totalChange === 0" class="no-changes">
            {{ t('iot.link.product.publishRecord.diff.noChanges') }}
          </div>
        </div>
      </div>

      <!-- 层级差异树 -->
      <ProductVersionDiffViewer :nodes="diffData.nodes ?? []" />
    </template>

    <template v-else>
      <a-empty :description="t('iot.link.product.publishRecord.diff.empty')" />
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { ArrowRightOutlined, ClockCircleOutlined, RocketOutlined } from '@ant-design/icons-vue';
  import { diffVersion } from '/@/api/iot/link/productVersion/productVersion';
  import type { ProductVersionDiffVO } from '/@/api/iot/link/productVersion/model/productVersionModel';
  import { ProductVersionDiffViewer } from '/@/components/iot/ProductVersionDiffViewer';
  import SummaryChip from './SummaryChip.vue';

  const { t } = useI18n();

  const loading = ref(false);
  const diffData = ref<ProductVersionDiffVO | null>(null);

  /**
   * 请求序号守卫 ── 抽屉关闭再快速重开不同版本时,旧请求响应可能晚于新请求回来,
   * 会把新版本的 diffData 覆盖成老版本的结果(用户看到的对比和点的版本对不上)。
   * 每次入口 `++currentSeq`,响应回写前比对自己的序号,过期请求直接丢弃。
   */
  let currentSeq = 0;

  const [registerDrawer] = useDrawerInner(async (params) => {
    diffData.value = null;
    const mySeq = ++currentSeq;
    loading.value = true;
    try {
      const { productIdentification, sourceVersion, targetVersion } = params;
      const res = await diffVersion(productIdentification, targetVersion, sourceVersion);
      if (mySeq === currentSeq) {
        diffData.value = res;
      }
    } catch (e) {
      // 过期请求的报错也吞掉,不污染当前抽屉的 toast / loading
      if (mySeq === currentSeq) {
        throw e;
      }
    } finally {
      if (mySeq === currentSeq) {
        loading.value = false;
      }
    }
  });

  /** 总服务变更数。 */
  const totalServiceChange = computed(
    () =>
      (diffData.value?.summary?.serviceAdded ?? 0) +
      (diffData.value?.summary?.serviceRemoved ?? 0) +
      (diffData.value?.summary?.serviceModified ?? 0),
  );

  /** 总属性变更数。 */
  const totalPropertyChange = computed(
    () =>
      (diffData.value?.summary?.propertyAdded ?? 0) +
      (diffData.value?.summary?.propertyRemoved ?? 0) +
      (diffData.value?.summary?.propertyModified ?? 0),
  );

  /** 总命令变更数。 */
  const totalCommandChange = computed(
    () =>
      (diffData.value?.summary?.commandAdded ?? 0) +
      (diffData.value?.summary?.commandRemoved ?? 0) +
      (diffData.value?.summary?.commandModified ?? 0),
  );

  /** 全部变更数。 */
  const totalChange = computed(
    () =>
      (diffData.value?.summary?.productInfoChanged ?? 0) +
      totalServiceChange.value +
      totalPropertyChange.value +
      totalCommandChange.value,
  );
</script>

<style lang="less" scoped>
  .diff-drawer {
    .loading-wrap {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 240px;
    }

    .header-card {
      padding: 16px 18px;
      border-radius: 12px;
      background: #f7f9fc;
      border: 1px solid #eef0f4;
      margin-bottom: 16px;
    }

    .versions {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 12px;

      .version-chip {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 5px 12px;
        border-radius: 10px;
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        font-size: 13px;
        font-weight: 500;
        word-break: break-all;
        background: #fff;
        border: 1px solid #e8ecf2;

        .chip-icon {
          font-size: 13px;
        }

        &.from {
          color: #5b6b82;
        }

        &.to {
          color: #1677ff;
          background: #eef4ff;
          border-color: #cfe2ff;
        }
      }

      .version-arrow {
        font-size: 16px;
        color: #c2c9d3;
      }
    }

    .summary-chips {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;

      .no-changes {
        font-size: 13px;
        color: #97a1b0;
      }
    }
  }
</style>
