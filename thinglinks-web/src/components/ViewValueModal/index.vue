<template>
  <BasicModal
    @register="register"
    v-bind="$attrs"
    :title="title"
    :width="width"
    :showOkBtn="false"
    @cancel="handleCancel"
  >
    <div class="code-editor">
      <codemirror
        v-model="displayValue"
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
        <a-button @click="handleCancel"> {{ t('common.closeText') }} </a-button>
        <a-button type="primary" @click="handleCopy" :disabled="!value">
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
  const value = ref('');
  const displayValue = ref('');
  const width = ref(520);
  const isJson = ref(false);

  const formatCode = (val) => {
    if (!val) return '';
    try {
      const parsed = JSON.parse(val);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return val;
    }
  };

  const checkIsJson = (val) => {
    if (!val) return false;
    try {
      JSON.parse(val);
      return true;
    } catch {
      return false;
    }
  };

  const contentExtensions = computed(() => {
    const extensions = [oneDark];
    if (isJson.value) {
      extensions.push(json());
    }
    return extensions;
  });

  const [register, { closeModal }] = useModalInner(async (data) => {
    const raw = typeof data.value === 'string' ? data.value : JSON.stringify(data.value, null, 2);
    value.value = raw;
    isJson.value = checkIsJson(raw);
    displayValue.value = formatCode(raw);
    title.value = data.title || '';
    width.value = data.width || 520;
    return data;
  });

  const handleCopy = () => {
    handleCopyTextV2(displayValue.value);
  };

  const handleCancel = () => {
    closeModal();
    value.value = '';
    displayValue.value = '';
  };
</script>
