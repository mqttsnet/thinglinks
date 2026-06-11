<template>
  <div class="api-select-node-card">
    <a-button
      type="primary"
      size="small"
      @click="handleOpenModal"
      :disabled="isDisabled"
    >
      {{ t('video.media.server.select') }}
    </a-button>
    <span v-if="displayName" class="show_name">{{ displayName }}</span>
    <span v-else-if="innerValue" class="show_name show_name--raw">{{ innerValue }}</span>
    <SelectedNodeModal
      @register="registerModal"
      @success="handleSuccess"
      :value="innerValue"
      @update-select-node="handleUpdateSelectNode"
    />
  </div>
</template>

<script setup lang="ts">
  import { watch, ref, onMounted, computed } from 'vue';
  import { useModal } from '/@/components/Modal';
  import SelectedNodeModal from './components/SelectedNodeModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { query as queryServer } from '/@/api/video/media/server';

  const props = defineProps({
    /** v-model 绑定值（mediaIdentification） */
    value: { type: [String, Number], default: '' },
    /** 是否禁用 */
    disabled: { type: Boolean, default: false },
    /** 是否只读 */
    readonly: { type: Boolean, default: false },
  });

  const emit = defineEmits(['change', 'update:value']);

  const { t } = useI18n();
  const innerValue = ref(String(props.value || ''));
  const displayName = ref('');
  const resolving = ref(false);
  const [registerModal, { openModal }] = useModal();

  const isDisabled = computed(() => props.disabled || props.readonly);

  const handleOpenModal = () => {
    if (isDisabled.value) return;
    openModal();
  };

  const handleSuccess = (data: Record<string, any>) => {
    if (data && data.mediaIdentification) {
      displayName.value = data.name || '';
      innerValue.value = data.mediaIdentification;
      emit('change', data.mediaIdentification);
      emit('update:value', data.mediaIdentification);
    }
  };

  const handleUpdateSelectNode = (data: Record<string, any>) => {
    displayName.value = data.name || '';
  };

  /** 根据 mediaIdentification 查询服务器名称 */
  async function resolveNodeName(mediaIdentification: string) {
    if (!mediaIdentification || resolving.value) return;
    resolving.value = true;
    try {
      const list = await queryServer({ mediaIdentification });
      if (list && list.length > 0) {
        displayName.value = list[0].name || '';
      }
    } catch (e) {
      console.warn('Failed to resolve media server name:', e);
    } finally {
      resolving.value = false;
    }
  }

  // 同步外部 value 变化（Form 框架赋值）
  watch(
    () => props.value,
    (newValue) => {
      const strVal = String(newValue || '');
      innerValue.value = strVal;
      if (!strVal) {
        displayName.value = '';
      } else if (!displayName.value) {
        resolveNodeName(strVal);
      }
    },
    { immediate: true },
  );
</script>

<style scoped lang="less">
  .api-select-node-card {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    width: 100%;
  }

  .show_name {
    font-size: 13px;
    color: #333;
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &--raw {
      color: #999;
      font-style: italic;
    }
  }
</style>
