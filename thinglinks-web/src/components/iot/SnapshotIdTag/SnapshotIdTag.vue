<template>
  <a-tag v-if="hasSnapshotId(value)" :color="color" class="snapshot-id">
    <span class="snapshot-id__text" :title="String(value)">{{ formatSnapshotId(value) }}</span>
    <a-tooltip :title="t('common.title.copy')">
      <CopyOutlined class="snapshot-id__copy" @click.stop="onCopy" />
    </a-tooltip>
  </a-tag>
  <span v-else class="snapshot-id-empty">{{ fallback }}</span>
</template>

<script lang="ts" setup>
  import { CopyOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { formatSnapshotId, hasSnapshotId } from '/@/utils/iot/version';

  /**
   * 版本快照序号轻量徽章 ── 统一展示 + 一键复制。
   *
   * <p>所有展示"版本序号"(产品当前版本 / 上一全量版本 / 历史版本号)的位置都用本组件,
   * 避免出现"有的截短有的不截短 / 有的能复制有的不能"的不一致。空值场景静默 fallback,
   * 不渲染徽章,只显示 fallback 文本(默认 "-")。</p>
   *
   * <p>颜色直接透传 antd {@code <a-tag :color>} 原生预设
   * (default / blue / purple / gold / processing / success / warning / error / 等),
   * 不再自定义语义色调,与项目里其他 {@code <a-tag>} 自然一致。</p>
   */
  defineOptions({ name: 'SnapshotIdTag' });

  const props = withDefaults(
    defineProps<{
      /** 版本序号(雪花字符串);空值时只展示 fallback。 */
      value?: string | null;
      /** antd {@code <a-tag>} 原生 color 预设,不传走默认中性灰。 */
      color?: string;
      /** 空值占位文本,默认 '-'。 */
      fallback?: string;
    }>(),
    {
      fallback: '-',
    },
  );

  const { t } = useI18n();
  const { createMessage } = useMessage();

  function onCopy() {
    if (!props.value) return;
    const ok = copyTextToClipboard(String(props.value));
    if (ok) {
      createMessage.success(t('common.tips.copySuccess'));
    } else {
      createMessage.warning(t('common.tips.copyFail'));
    }
  }
</script>

<style lang="less" scoped>
  /* 版本快照徽章:复用 antd <a-tag> 颜色体系,只覆盖等宽字体 + 复制图标交互 */
  .snapshot-id {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;

    &__text {
      letter-spacing: 0.2px;
      user-select: text;
    }

    &__copy {
      cursor: pointer;
      font-size: 12px;
      opacity: 0.55;
      transition: opacity 0.15s ease;

      &:hover {
        opacity: 1;
      }
    }
  }

  .snapshot-id-empty {
    color: var(--text-tertiary, #bfbfbf);
  }
</style>
