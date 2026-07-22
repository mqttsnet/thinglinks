<template>
  <Empty
    v-if="!entries || !entries.length"
    :image="simpleImage"
    :description="t('iot.rule.groovy.ruleGroovyScript.details.debug.emptyObject')"
    class="kv-empty"
  />
  <div v-else class="kv-table">
    <div class="kv-head">
      <span class="kv-col-key">{{
        t('iot.rule.groovy.ruleGroovyScript.details.debug.colKey')
      }}</span>
      <span class="kv-col-val">{{
        t('iot.rule.groovy.ruleGroovyScript.details.debug.colValue')
      }}</span>
    </div>
    <div v-for="item in entries" :key="item.key" class="kv-row">
      <span class="kv-key" :title="prefix + item.key">{{ prefix }}{{ item.key }}</span>
      <span class="kv-val">
        <a-tooltip v-if="isLong(item.display)" :title="item.display" placement="topLeft">
          <span class="kv-val-text ellipsis">{{ item.display }}</span>
        </a-tooltip>
        <span v-else class="kv-val-text">{{ item.display }}</span>
        <a-tooltip :title="t('iot.rule.groovy.ruleGroovyScript.details.debug.copyValue')">
          <CopyOutlined class="kv-copy" @click="copy(item.display)" />
        </a-tooltip>
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  /**
   * BindingKvTable ── 数据驱动的「字段名 → 实际值」列表。
   *
   * <p>遍历传入的 entries 渲染,值过长则省略 + 悬浮全文 + 可复制;后端 binding 加字段无需改 UI。
   *
   * @author mqttsnet
   */
  import { computed } from 'vue';
  import { Empty } from 'ant-design-vue';
  import { CopyOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';

  defineOptions({ name: 'BindingKvTable' });

  interface KvEntry {
    key: string;
    display: string;
  }

  const props = withDefaults(
    defineProps<{
      entries?: KvEntry[];
      /** 变量前缀,如 device. / product.,纯展示用 */
      prefix?: string;
    }>(),
    {
      prefix: '',
    },
  );

  // 显式引用,避免 setup 中未使用告警
  void props;

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const simpleImage = computed(() => Empty.PRESENTED_IMAGE_SIMPLE);

  function isLong(v: string): boolean {
    return !!v && v.length > 48;
  }

  function copy(v: string) {
    const ok = copyTextToClipboard(v ?? '');
    if (ok) createMessage.success(t('iot.rule.groovy.ruleGroovyScript.details.debug.copied'));
  }
</script>

<style lang="less" scoped>
  .kv-empty {
    padding: 24px 0;
  }

  .kv-table {
    border: 1px solid #f0f2f5;
    border-radius: 8px;
    overflow: hidden;
  }

  .kv-head,
  .kv-row {
    display: flex;
    align-items: center;
  }

  .kv-head {
    background: #fafbfc;
    color: #6b7280;
    font-size: 12px;
    font-weight: 600;
    border-bottom: 1px solid #f0f2f5;
  }

  .kv-row {
    border-bottom: 1px solid #f5f6f8;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f9fbff;
    }
  }

  .kv-col-key,
  .kv-key {
    width: 42%;
    flex-shrink: 0;
    padding: 8px 12px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #2a3547;
    word-break: break-all;
  }

  .kv-col-val,
  .kv-val {
    flex: 1;
    min-width: 0;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .kv-val-text {
    flex: 1;
    min-width: 0;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    color: #2a3547;

    &.ellipsis {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .kv-copy {
    color: #9aa4b2;
    cursor: pointer;
    flex-shrink: 0;

    &:hover {
      color: @primary-color;
    }
  }
</style>
