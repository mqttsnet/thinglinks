<template>
  <div class="json-viewer">
    <div class="json-toolbar">
      <a-space>
        <a-tooltip
          v-if="masked"
          :title="t('iot.rule.integration.datasource.detail.rawJsonHint')"
        >
          <a-switch
            v-model:checked="revealed"
            size="small"
            :checked-children="t('iot.rule.integration.datasource.detail.rawJson')"
            :un-checked-children="t('iot.rule.integration.datasource.detail.rawJson')"
          />
        </a-tooltip>
        <a-tooltip :title="t('common.title.copy')">
          <a-button size="small" @click="handleCopy">
            <template #icon><CopyOutlined /></template>
          </a-button>
        </a-tooltip>
      </a-space>
    </div>
    <pre class="json-body" :class="{ 'is-empty': !rendered.trim() }">{{ rendered || '-' }}</pre>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { CopyOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';

  /**
   * Flexy 风格 JSON 查看器
   * <p>支持 masked 模式：凭证类敏感字段默认遮罩，需手动展开。
   */
  const props = defineProps({
    value: { type: String, default: '' },
    /**
     * 敏感模式：true 时按字段名启发式（password/secret/token/key/credential）
     * 自动把字段值替换为 "***"，用户点开关解除遮罩。
     */
    masked: { type: Boolean, default: false },
  });

  const { t } = useI18n();
  const { createMessage } = useMessage();

  const revealed = ref(false);

  /** 敏感字段关键词（不区分大小写） */
  const SENSITIVE_KEYS = [
    'password',
    'pwd',
    'secret',
    'token',
    'apikey',
    'api_key',
    'accesskey',
    'access_key',
    'authorization',
    'credential',
    'privatekey',
    'private_key',
  ];

  /**
   * 解析 + 美化 JSON。失败时返回原始字符串（不抛错）。
   */
  function prettify(raw: string): string {
    if (!raw) return '';
    try {
      const obj = JSON.parse(raw);
      return JSON.stringify(obj, null, 2);
    } catch {
      return raw;
    }
  }

  /**
   * 按字段名启发式遮罩敏感值。
   * 仅在 masked=true && revealed=false 时生效。
   */
  function maskSensitive(raw: string): string {
    if (!raw) return '';
    let parsed: any;
    try {
      parsed = JSON.parse(raw);
    } catch {
      return '***';
    }
    const masked = recursiveMask(parsed);
    return JSON.stringify(masked, null, 2);
  }

  function recursiveMask(node: any): any {
    if (node == null) return node;
    if (Array.isArray(node)) return node.map(recursiveMask);
    if (typeof node === 'object') {
      const out: Record<string, any> = {};
      for (const k of Object.keys(node)) {
        const lower = k.toLowerCase();
        if (SENSITIVE_KEYS.some((s) => lower.includes(s))) {
          out[k] = '***';
        } else {
          out[k] = recursiveMask(node[k]);
        }
      }
      return out;
    }
    return node;
  }

  const rendered = computed(() => {
    if (props.masked && !revealed.value) {
      return maskSensitive(props.value);
    }
    return prettify(props.value);
  });

  function handleCopy() {
    if (copyTextToClipboard(rendered.value)) {
      createMessage.success(t('common.tips.copySuccess'));
    }
  }
</script>

<style lang="less" scoped>
  .json-viewer {
    background: #f6f8fb;
    border: 1px solid #e8eaf0;
    border-radius: 8px;
    overflow: hidden;
  }

  .json-toolbar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 6px 12px;
    background: #fff;
    border-bottom: 1px solid #e8eaf0;
  }

  .json-body {
    margin: 0;
    padding: 12px 16px;
    font-family: 'JetBrains Mono', Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    color: #2a3547;
    max-height: 480px;
    overflow: auto;
    white-space: pre-wrap;
    word-break: break-word;

    &.is-empty {
      color: #a0aec0;
    }
  }
</style>
