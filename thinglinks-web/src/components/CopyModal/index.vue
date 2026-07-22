<template>
  <BasicModal
    @register="register"
    v-bind="$attrs"
    :title="title"
    @ok="cancelHandler"
    @cancel="cancelHandler"
    :maskClosable="false"
  >
    <div class="code-editor">
      <codemirror
        v-model="code"
        :read-only="true"
        :extensions="contentExtensions"
        :indent-with-tab="true"
        :line-wrapping="true"
        :style="{ height: '340px' }"
        :tab-size="2"
      />
    </div>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 12px">
        <a-button @click="cancelHandler"> {{ t('common.closeText') }} </a-button>
        <a-button type="primary" @click="copyCode">
          <template #icon><CopyOutlined /></template>
          {{ t('common.title.copy') }}
        </a-button>
      </div>
    </template>
  </BasicModal>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { Codemirror } from 'vue-codemirror';
  import { json } from '@codemirror/lang-json';
  import { oneDark } from '@codemirror/theme-one-dark';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { CopyOutlined } from '@ant-design/icons-vue';

  const { t } = useI18n();

  const title = ref('');
  const code = ref('');
  const isJson = ref(false);

  // 格式化代码：如果是 JSON 则格式化，否则保持原样
  const formatCode = (value) => {
    if (!value) return '';

    // 尝试解析为 JSON
    try {
      const parsed = JSON.parse(value);
      return JSON.stringify(parsed, null, 2);
    } catch {
      // 不是 JSON，返回原字符串
      return value;
    }
  };

  // 判断是否为 JSON 格式
  const checkIsJson = (value) => {
    if (!value) return false;
    try {
      JSON.parse(value);
      return true;
    } catch {
      return false;
    }
  };

  // 根据内容类型动态设置扩展
  const contentExtensions = computed(() => {
    const extensions = [oneDark];
    if (isJson.value) {
      extensions.push(json());
    }
    return extensions;
  });

  const [register, { closeModal }] = useModalInner(async (data) => {
    const value = typeof data.value === 'string' ? data.value : JSON.stringify(data.value, null, 2);
    isJson.value = checkIsJson(value);
    code.value = formatCode(value);
    title.value = data.title;
    return data;
  });

  const copyCode = () => {
    handleCopyTextV2(code.value);
  };

  const cancelHandler = () => {
    closeModal();
    code.value = '';
  };
</script>

<style lang="less" scoped></style>
