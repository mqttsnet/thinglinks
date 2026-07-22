<template>
  <BasicModal
    @register="register"
    v-bind="$attrs"
    :title="t('iot.link.ota.otaUpgrades.codeEditorDefine.title')"
    :helpMessage="t('iot.link.ota.otaUpgrades.codeEditorDefine.helpMessage')"
    @ok="okHandler"
    @cancel="cancelHandler"
  >
    <div class="code-editor">
      <codemirror
        v-model="code"
        :autofocus="true"
        :extensions="contentExtensions"
        :indent-with-tab="true"
        :style="{ height: '340px' }"
        :tab-size="2"
        :placeholder="t('iot.link.ota.otaUpgrades.codeEditorDefine.placeholder')"
      />
    </div>
  </BasicModal>
</template>

<script setup>
  import { ref } from 'vue';
  import { Codemirror } from 'vue-codemirror';
  import { json } from '@codemirror/lang-json';
  import { oneDark } from '@codemirror/theme-one-dark';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emits = defineEmits(['submitEditor']);

  const contentExtensions = [json(), oneDark];
  const code = ref('');
  const isJsonObjectValidation = ref(false);

  const [register, { closeModal }] = useModalInner(async ({ value, isJsonObject }) => {
    code.value = typeof value === 'string' ? value : JSON.stringify(value);
    isJsonObjectValidation.value = isJsonObject;
    // return data;
  });

  const okHandler = () => {
    try {
      const parsedCode = JSON.parse(code.value);
      if (isJsonObjectValidation.value && typeof parsedCode !== 'object') {
        return createMessage.error(t('iot.link.ota.otaUpgrades.codeEditorDefine.description'));
      }
      const formattedCode = JSON.stringify(parsedCode);
      emits('submitEditor', formattedCode);
      closeModal();
      code.value = '';
    } catch (error) {
      createMessage.error(t('iot.link.ota.otaUpgrades.codeEditorDefine.description'));
      console.error('Invalid JSON:', error);
    }
  };

  const cancelHandler = () => {
    closeModal();
    code.value = '';
  };
</script>

<style lang="less" scoped></style>
