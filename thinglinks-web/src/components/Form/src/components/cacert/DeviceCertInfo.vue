<template>
  <span class="device-cert-inline">
    <!-- 加载中 -->
    <a-spin v-if="loading" size="small" />

    <!-- 找不到证书 -->
    <template v-else-if="!cert">
      <span class="serial-fallback" :title="serialNumber">{{ serialNumber || '—' }}</span>
      <span v-if="serialNumber" class="missing">
        ({{ t('iot.link.device.device.cert.notFound') }})
      </span>
    </template>

    <!-- 找到证书 → 富文本展示 -->
    <template v-else>
      <span class="cert-name" :title="cert.certName">
        {{ cert.certName || t('iot.link.device.device.cert.unnamed') }}
      </span>
      <a-tag :color="stateColor">
        {{ getDictLabel(DictEnum.LINK_CA_CERT_STATUS, cert.state, '—') }}
      </a-tag>
      <a-tag color="blue">
        {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, cert.algorithm, '—') }}
      </a-tag>
      <a-tag :color="daysLeftColor">{{ daysLeftLabel }}</a-tag>
      <a-button type="link" size="small" @click="goToDetail">
        {{ t('iot.link.device.device.cert.viewDetail') }}
      </a-button>
    </template>
  </span>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue';
  import dayjs from 'dayjs';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDict } from '/@/components/Dict';
  import { DictEnum } from '/@/enums/commonEnum';
  import { page } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';

  const props = defineProps<{ serialNumber?: string | number | null }>();

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const router = useRouter();

  const loading = ref(false);
  const cert = ref<any | null>(null);

  const fetchCert = async (sn?: string) => {
    if (!sn) {
      cert.value = null;
      return;
    }
    loading.value = true;
    try {
      const res = await page({
        current: 1,
        size: 1,
        model: { serialNumber: sn },
        extra: {},
      });
      cert.value = res?.records?.[0] || null;
    } finally {
      loading.value = false;
    }
  };

  watch(
    () => props.serialNumber,
    (sn) => fetchCert(sn ? String(sn) : undefined),
    { immediate: true },
  );

  // ===== 计算 =====
  const stateColor = computed(() => {
    switch (cert.value?.state) {
      case 1:
        return 'success';
      case 2:
        return 'error';
      default:
        return 'warning';
    }
  });

  const daysLeft = computed<number | null>(() => {
    if (!cert.value?.notAfter) return null;
    return dayjs(cert.value.notAfter).diff(dayjs(), 'day');
  });

  const daysLeftLabel = computed(() => {
    const d = daysLeft.value;
    if (d === null) return '—';
    if (d < 0) return t('iot.link.device.device.cert.expired');
    return t('iot.link.device.device.cert.daysRemaining', { days: d });
  });

  const daysLeftColor = computed(() => {
    const d = daysLeft.value;
    if (d === null) return 'default';
    if (d < 0) return 'error';
    if (d <= 30) return 'warning';
    return 'success';
  });

  // 跳转 CA 许可证证书 详情(path-based,跨模块导航统一约定).
  const goToDetail = () => {
    if (!cert.value?.id) return;
    router.push({ path: `/cacert/caCertLicense/detail/${cert.value.id}` });
  };
</script>

<style scoped lang="less">
  .device-cert-inline {
    display: inline-flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;

    .cert-name {
      font-weight: 500;
      max-width: 220px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .serial-fallback {
      font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
      color: #555;
    }

    .missing {
      color: #d43030;
      font-size: 12px;
    }
  }
</style>
