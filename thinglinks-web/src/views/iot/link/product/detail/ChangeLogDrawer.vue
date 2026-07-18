<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="t('iot.link.product.versionList.changeLogDrawerTitle')"
    width="720px"
    :closable="true"
    :maskClosable="false"
    class="change-log-drawer"
  >
    <!-- 版本信息条 -->
    <div class="cld-header">
      <HistoryOutlined class="cld-icon" />
      <div class="cld-info">
        <span class="cld-label">{{ t('iot.link.product.product.activeVersionNo') }}</span>
        <SnapshotIdTag :value="versionNo" />
      </div>
    </div>

    <ProductChangeLogPanel
      v-if="productIdentification && versionNo"
      :productIdentification="productIdentification"
      :versionNo="versionNo"
    />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { HistoryOutlined } from '@ant-design/icons-vue';
  import { ProductChangeLogPanel } from '/@/components/iot/ProductChangeLogPanel';
  import { SnapshotIdTag } from '/@/components/iot';

  /**
   * 版本变更记录抽屉。
   *
   * <p>发布管理列表里每个版本卡片的「变更记录」入口,右侧抽屉内嵌
   * {@link ProductChangeLogPanel},按该版本切片展示物模型变更流水。</p>
   */
  const { t } = useI18n();

  const productIdentification = ref<string>('');
  const versionNo = ref<string>('');

  const [registerDrawer] = useDrawerInner((params) => {
    productIdentification.value = params?.productIdentification ?? '';
    versionNo.value = params?.versionNo ?? '';
  });
</script>

<style lang="less" scoped>
  .change-log-drawer {
    .cld-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      margin-bottom: 14px;
      background: #f7f9fc;
      border: 1px solid #eef0f4;
      border-radius: 12px;

      .cld-icon {
        font-size: 20px;
        color: #1677ff;
      }

      .cld-info {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
      }

      .cld-label {
        font-size: 12px;
        color: #8c97a5;
      }

      .cld-version {
        font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
        font-size: 13px;
        font-weight: 600;
        color: #2a3547;
        word-break: break-all;
      }
    }
  }
</style>
