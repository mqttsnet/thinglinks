<template>
  <div class="cacert-select-wrap">
    <!-- 选择按钮 + 详情链接 -->
    <div class="action-row">
      <a-button type="primary" @click="handleOpenModal" :disabled="$attrs.disabled">
        {{
          selectNode?.serialNumber
            ? t('iot.link.device.device.cert.changeSelection')
            : t('iot.link.device.device.select')
        }}
      </a-button>
      <a-button
        v-if="selectNode?.id"
        type="link"
        size="small"
        @click="goToCertDetail"
      >
        {{ t('iot.link.device.device.cert.viewDetail') }}
      </a-button>
    </div>

    <!-- 已吊销 警告 (回显时如果用户先前选了已被吊销的证书) -->
    <a-alert
      v-if="selectNode?.state === 2"
      type="error"
      show-icon
      class="mt-2"
      :message="t('iot.link.device.device.cert.revokedWarning')"
    />

    <!-- 已选择的证书 预览卡 -->
    <div v-if="selectNode?.serialNumber" class="preview-card">
      <div class="row">
        <span class="cert-name" :title="selectNode.certName">
          {{ selectNode.certName || t('iot.link.device.device.cert.unnamed') }}
        </span>
        <a-tag :color="stateColor">
          {{ getDictLabel(DictEnum.LINK_CA_CERT_STATUS, selectNode.state, '—') }}
        </a-tag>
        <a-tag color="blue">
          {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, selectNode.algorithm, '—') }}
        </a-tag>
        <a-tag :color="daysLeftColor">{{ daysLeftLabel }}</a-tag>
      </div>
      <div class="row meta">
        <span class="label">{{
          t('iot.link.operationMaintenance.cacert.caCertLicense.serialNumber')
        }}</span>
        <span class="serial" :title="selectNode.serialNumber">
          {{ selectNode.serialNumber }}
        </span>
        <span class="dot">·</span>
        <span class="label">{{ t('iot.link.device.device.cert.validity') }}</span>
        <span>{{ validityRange }}</span>
      </div>
    </div>

    <SelectedNodeModal
      @register="registerModal"
      @success="handleSuccess"
      :value="props.value"
      @update-select-node="handleUpdateSelectNode"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
  import { reactive, watch, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import dayjs from 'dayjs';
  import { useModal } from '/@/components/Modal';
  import SelectedNodeModal from './components/SelectedNodeModal.vue';
  import type { CaCertLicensePageQuery } from '/@/api/iot/link/operationMaintenance/cacert/model/caCertLicenseModel';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';

  const props = defineProps({ type: String, value: String });
  const emit = defineEmits(['select']);

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const router = useRouter();

  const selectNode = reactive<CaCertLicensePageQuery>({
    certName: '',
    serialNumber: '',
    algorithm: undefined,
    state: undefined,
    notBefore: undefined,
    notAfter: undefined,
  } as any);

  const [registerModal, { openModal }] = useModal();

  const handleOpenModal = () => openModal();

  const handleSuccess = (data: CaCertLicensePageQuery) => {
    Object.assign(selectNode, data);
    emit('select', data);
  };

  const handleUpdateSelectNode = (data: CaCertLicensePageQuery) => {
    Object.assign(selectNode, data);
  };

  // 跳转 CA 许可证证书 详情(path-based,跨模块导航统一约定;
  // 与 views/iot/rule/integration/bridge/detail.vue:511 一致风格).
  const goToCertDetail = () => {
    const id = (selectNode as any)?.id;
    if (!id) return;
    router.push({ path: `/cacert/caCertLicense/detail/${id}` });
  };

  // ===== 派生计算属性 =====
  const stateColor = computed(() => {
    switch (selectNode.state) {
      case 1:
        return 'success';
      case 2:
        return 'error';
      default:
        return 'warning';
    }
  });

  const daysLeftValue = computed<number | null>(() => {
    if (!selectNode.notAfter) return null;
    return dayjs(selectNode.notAfter).diff(dayjs(), 'day');
  });

  const daysLeftLabel = computed(() => {
    const d = daysLeftValue.value;
    if (d === null) return '—';
    if (d < 0) return t('iot.link.device.device.cert.expired');
    return t('iot.link.device.device.cert.daysRemaining', { days: d });
  });

  const daysLeftColor = computed(() => {
    const d = daysLeftValue.value;
    if (d === null) return 'default';
    if (d < 0) return 'error';
    if (d <= 30) return 'warning';
    return 'success';
  });

  const validityRange = computed(() => {
    const s = selectNode.notBefore ? dayjs(selectNode.notBefore).format('YYYY-MM-DD') : '?';
    const e = selectNode.notAfter ? dayjs(selectNode.notAfter).format('YYYY-MM-DD') : '?';
    return `${s} ~ ${e}`;
  });

  // 外部 value 被清空 → 清空预览
  watch(
    () => props.value,
    (newValue) => {
      if (!newValue) {
        Object.assign(selectNode, {
          certName: '',
          serialNumber: '',
          algorithm: undefined,
          state: undefined,
          notBefore: undefined,
          notAfter: undefined,
        });
      }
    },
  );
</script>

<style scoped lang="less">
  .cacert-select-wrap {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .action-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .preview-card {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    padding: 10px 12px;
    background: #fafbfc;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 8px;

      &.meta {
        font-size: 12px;
        color: #888;
      }

      .cert-name {
        font-size: 14px;
        font-weight: 600;
        color: #1f2933;
        max-width: 260px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .label {
        color: #999;
      }

      .serial {
        color: #555;
        font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
        max-width: 320px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .dot {
        color: #ccc;
      }
    }
  }

  .mt-2 {
    margin-top: 8px;
  }
</style>
