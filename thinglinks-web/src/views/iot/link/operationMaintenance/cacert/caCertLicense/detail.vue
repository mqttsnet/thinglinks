<template>
  <PageWrapper contentFullHeight class="ca-detail">
    <!-- ===== 顶部 Header ===== -->
    <Card :bordered="false" class="header-card">
      <div class="header-row">
        <div class="header-left">
          <div class="ca-icon">
            <CaCertLicenseSvg />
          </div>
          <div class="ca-meta">
            <div class="ca-title">
              <span class="name-text">{{ detailData.certName || '-' }}</span>
              <Tag :color="getStateColor(detailData.state)">
                {{ getDictLabel(DictEnum.LINK_CA_CERT_STATUS, detailData.state, '-') }}
              </Tag>
              <Tag color="blue">
                {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, detailData.algorithm, '-') }}
              </Tag>
              <Tag :color="validityColor">{{ validityLabel }}</Tag>
            </div>
            <div class="meta-line">
              <span>
                <NumberOutlined />
                {{ t(`${tNs}.serialNumber`) }}:
                <CopyableText :text="detailData.serialNumber || '-'" />
              </span>
              <a-divider type="vertical" />
              <span>
                <ClockCircleOutlined />
                {{ t('thinglinks.common.createdTime') }}: {{ detailData.createdTime || '-' }}
              </span>
            </div>
          </div>
        </div>

        <a-space>
          <a-button @click="load">
            <template #icon><ReloadOutlined /></template>
            {{ t(`${tNs}.detail.refresh`) }}
          </a-button>
          <a-button
            @click="handleEdit"
            v-hasAnyPermission="['link:cacert:caCertLicense:edit']"
          >
            <template #icon><EditOutlined /></template>
            {{ t('common.title.edit') }}
          </a-button>
          <a-button
            :disabled="detailData.state !== 1"
            @click="handleDownloadPack"
            v-hasAnyPermission="['link:cacert:caCertLicense:downloadPack']"
          >
            <template #icon><DownloadOutlined /></template>
            {{ t(`${tNs}.action.downloadPack`) }}
          </a-button>
          <a-button
            @click="handleSslTest"
            v-hasAnyPermission="['link:cacert:caCertLicense:sslTest']"
          >
            <template #icon><ThunderboltOutlined /></template>
            {{ t(`${tNs}.action.testSsl`) }}
          </a-button>
          <a-button
            danger
            :disabled="detailData.state !== 1"
            @click="handleRevoke"
            v-hasAnyPermission="['link:cacert:caCertLicense:revoke']"
          >
            <template #icon><StopOutlined /></template>
            {{ t(`${tNs}.action.revoke`) }}
          </a-button>
        </a-space>
      </div>
    </Card>

    <!-- ===== 4 指标 ===== -->
    <Row :gutter="16" class="metric-row">
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon algo"><ApiOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t(`${tNs}.algorithm`) }}</div>
            <div class="metric-value">
              {{ getDictLabel(DictEnum.LINK_CA_CERT_ALGORITHM, detailData.algorithm, '-') }}
            </div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon validity" :class="validityKey">
            <SafetyCertificateOutlined />
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ t(`${tNs}.validityRemaining`, { days: '' }) }}</div>
            <div class="metric-value">{{ validityLabel }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon devices"><ClusterOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t(`${tNs}.boundDeviceCount`) }}</div>
            <div class="metric-value">{{ impactData?.boundDeviceCount ?? '-' }}</div>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :sm="12" :lg="6">
        <Card :bordered="false" class="metric-card">
          <div class="metric-icon online"><WifiOutlined /></div>
          <div class="metric-body">
            <div class="metric-label">{{ t(`${tNs}.onlineDeviceCount`) }}</div>
            <div class="metric-value">{{ impactData?.onlineDeviceCount ?? '-' }}</div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- ===== 主体 Tabs ===== -->
    <Card :bordered="false" class="panel-card">
      <a-tabs v-model:activeKey="activeTab" size="small">
        <!-- Tab 1 基本信息 -->
        <a-tab-pane key="basic" :tab="t(`${tNs}.tabs.basic`)">
          <a-descriptions bordered :column="{ xs: 1, sm: 2, lg: 3 }" size="small">
            <a-descriptions-item :label="t(`${tNs}.commonName`)">
              {{ detailData.commonName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.organization`)">
              {{ detailData.organization || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.organizationalUnit`)">
              {{ detailData.organizationalUnit || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.countryName`)">
              {{ detailData.countryName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.provinceName`)">
              {{ detailData.provinceName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.localityName`)">
              {{ detailData.localityName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.email`)">
              {{ detailData.email || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.notBefore`)">
              {{ detailData.notBefore || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.notAfter`)">
              {{ detailData.notAfter || '-' }}
            </a-descriptions-item>
            <a-descriptions-item v-if="detailData.state === 2" :label="t(`${tNs}.revokeTime`)">
              {{ detailData.revokeTime || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="t(`${tNs}.remark`)" :span="3">
              {{ detailData.remark || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <!-- Tab 2 证书内容 -->
        <a-tab-pane key="content" :tab="t(`${tNs}.tabs.content`)">
          <a-alert
            type="info"
            show-icon
            :message="t(`${tNs}.contentTip`)"
            style="margin-bottom: 12px"
          />
          <div class="pem-block">
            <div class="pem-toolbar">
              <a-button size="small" @click="handleCopyPem">
                <template #icon><CopyOutlined /></template>
                {{ t('common.title.copy') }}
              </a-button>
            </div>
            <pre class="pem-content">{{ formattedPem || '-' }}</pre>
          </div>
        </a-tab-pane>

        <!-- Tab 3 关联设备 -->
        <a-tab-pane key="devices" :tab="t(`${tNs}.tabs.devices`)">
          <a-alert
            v-if="(impactData?.boundDeviceCount ?? 0) > 50"
            type="info"
            show-icon
            :message="t(`${tNs}.impact.top50Tip`, { count: impactData.boundDeviceCount })"
            style="margin-bottom: 12px"
          />
          <a-table
            :columns="deviceColumns"
            :data-source="impactData?.topDevices || []"
            :pagination="false"
            :loading="impactLoading"
            size="small"
            row-key="deviceIdentification"
          >
            <template #emptyText>
              <a-empty :description="t('thinglinks.common.noData')" />
            </template>
          </a-table>
        </a-tab-pane>

        <!-- Tab 4 审计日志 -->
        <a-tab-pane key="audit" :tab="t(`${tNs}.tabs.audit`)">
          <a-spin :spinning="auditLoading">
            <a-timeline v-if="auditList.length" mode="left">
              <a-timeline-item
                v-for="row in auditList"
                :key="row.id"
                :color="getAuditColor(row.type)"
              >
                <template #dot>
                  <component :is="getAuditIcon(row.type)" />
                </template>
                <div class="audit-row">
                  <a-tag :color="getAuditColor(row.type)">
                    {{ getDictLabel(DictEnum.LINK_CA_CERT_AUDIT_TYPE, row.type, row.type || '-') }}
                  </a-tag>
                  <span class="audit-time">{{ row.createdTime }}</span>
                </div>
                <div class="audit-detail">{{ row.detail || '-' }}</div>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else :description="t(`${tNs}.audit.empty`)" />
          </a-spin>
        </a-tab-pane>
      </a-tabs>
    </Card>

    <RevokeImpactModal @register="registerRevokeModal" @success="handleRevokeSuccess" />
    <DownloadPackModal @register="registerDownloadModal" />
    <EditMetadataModal @register="registerEditModal" @success="load" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import dayjs from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Card, Row, Col, Tag } from 'ant-design-vue';
  import {
    ApiOutlined,
    ClockCircleOutlined,
    ClusterOutlined,
    CopyOutlined,
    DownloadOutlined,
    EditOutlined,
    NumberOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    StopOutlined,
    ThunderboltOutlined,
    WifiOutlined,
    PlusCircleOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    DownloadOutlined as DownloadIcon,
    ExperimentOutlined,
  } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useDict } from '/@/components/Dict';
  import { useModal } from '/@/components/Modal';
  import { DictEnum } from '/@/enums/commonEnum';
  import { CaCertLicenseSvg } from '/@/components/iot/integration/svg';
  import CopyableText from '/@/components/CopyableText';
  import {
    detail,
    getImpact,
    listAudit,
    type CaCertAuditLogVO,
  } from '/@/api/iot/link/operationMaintenance/cacert/caCertLicense';
  import RevokeImpactModal from './RevokeImpactModal.vue';
  import DownloadPackModal from './DownloadPackModal.vue';
  import EditMetadataModal from './EditMetadataModal.vue';

  defineOptions({ name: 'CA许可证证书详情' });

  const tNs = 'iot.link.operationMaintenance.cacert.caCertLicense';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const { getDictLabel } = useDict();
  const { currentRoute, push } = useRouter();

  const id = ref('');
  const detailData = reactive<any>({});
  const impactData = ref<any | null>(null);
  const impactLoading = ref(false);
  const auditList = ref<CaCertAuditLogVO[]>([]);
  const auditLoading = ref(false);
  const activeTab = ref<'basic' | 'content' | 'devices' | 'audit'>('basic');

  const [registerRevokeModal, { openModal: openRevokeModal }] = useModal();
  const [registerDownloadModal, { openModal: openDownloadModal }] = useModal();
  const [registerEditModal, { openModal: openEditModal }] = useModal();

  const deviceColumns = [
    { title: t(`${tNs}.impact.deviceIdentification`), dataIndex: 'deviceIdentification' },
    { title: t(`${tNs}.impact.deviceName`), dataIndex: 'deviceName' },
    {
      title: t(`${tNs}.impact.online`),
      dataIndex: 'connectStatus',
      width: 90,
      customRender: ({ text }: { text: number }) =>
        text === 1
          ? t(`${tNs}.impact.connectStatusOnline`)
          : t(`${tNs}.impact.connectStatusOffline`),
    },
  ];

  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = String(params?.id ?? '');
    if (id.value) load();
  });

  /**
   * 详情 / 影响面 / 审计日志 三个接口**并行**触发,
   * 不让影响面等到详情 resolve 才发请求 ── 否则 metric 卡片要等两个串联 RTT 才出数,
   * 用户进页面瞬间看到的是"-",等点了 Tab 3 才发现数字已经填上(其实是后端响应慢了).
   */
  async function load() {
    if (!id.value) return;
    await Promise.allSettled([loadDetail(), loadImpact(), loadAudit()]);
  }

  async function loadDetail() {
    try {
      const res = await detail(id.value);
      Object.keys(detailData).forEach((k) => delete detailData[k]);
      Object.assign(detailData, res || {});
    } catch (e: any) {
      createMessage.error(e?.message || t(`${tNs}.detail.loadFailed`));
    }
  }

  async function loadImpact() {
    if (!id.value) return;
    impactLoading.value = true;
    try {
      impactData.value = await getImpact(id.value);
    } catch {
      impactData.value = null;
    } finally {
      impactLoading.value = false;
    }
  }

  async function loadAudit() {
    auditLoading.value = true;
    try {
      const res = await listAudit(id.value);
      auditList.value = res || [];
    } catch {
      auditList.value = [];
    } finally {
      auditLoading.value = false;
    }
  }

  // ===== 操作 =====
  function handleEdit() {
    openEditModal(true, { record: detailData });
  }

  function handleRevoke() {
    openRevokeModal(true, { record: detailData });
  }

  function handleDownloadPack() {
    openDownloadModal(true, { record: detailData });
  }

  function handleSslTest() {
    push({
      path: '/link/cacert/sslTester',
      query: { caSerialNumber: detailData.serialNumber },
    });
  }

  function handleRevokeSuccess() {
    load();
  }

  function handleCopyPem() {
    const pem = formattedPem.value || '';
    if (!pem || pem === '-') return;
    navigator.clipboard
      ?.writeText(pem)
      .then(() => createMessage.success(t('common.tips.copySuccess')))
      .catch(() => createMessage.error(t('common.tips.copyFail')));
  }

  // ===== 派生 =====
  /**
   * 把后端存的 Base64 单行内容包成标准 PEM 边界(每 64 字符换行).
   * 若后端已是完整 PEM 直接返回.
   */
  const formattedPem = computed(() => {
    const raw = detailData.licenseBase64;
    if (!raw) return '';
    if (raw.startsWith('-----BEGIN')) return raw;
    const lines = raw.match(/.{1,64}/g) || [];
    return ['-----BEGIN CERTIFICATE-----', ...lines, '-----END CERTIFICATE-----'].join('\n');
  });

  const daysLeft = computed<number | null>(() => {
    if (!detailData.notAfter) return null;
    return dayjs(detailData.notAfter).diff(dayjs(), 'day');
  });

  const validityLabel = computed(() => {
    const d = daysLeft.value;
    if (d === null) return '-';
    if (d < 0) return t(`${tNs}.expired`);
    return t(`${tNs}.validityRemaining`, { days: d });
  });

  const validityKey = computed<'ok' | 'warn' | 'down' | 'unknown'>(() => {
    const d = daysLeft.value;
    if (d === null) return 'unknown';
    if (d < 0) return 'down';
    if (d <= 30) return 'warn';
    return 'ok';
  });

  const validityColor = computed(() => {
    switch (validityKey.value) {
      case 'ok':
        return 'success';
      case 'warn':
        return 'warning';
      case 'down':
        return 'error';
      default:
        return 'default';
    }
  });

  function getStateColor(state?: number): string {
    switch (state) {
      case 0:
        return 'warning';
      case 1:
        return 'success';
      case 2:
        return 'error';
      default:
        return 'default';
    }
  }

  function getAuditColor(type?: string): string {
    switch (type) {
      case 'IMPORT':
        return 'blue';
      case 'ISSUE':
        return 'green';
      case 'REVOKE':
        return 'red';
      case 'DOWNLOAD_PACK':
        return 'cyan';
      case 'SSL_TEST':
        return 'purple';
      default:
        return 'gray';
    }
  }

  function getAuditIcon(type?: string) {
    switch (type) {
      case 'IMPORT':
        return PlusCircleOutlined;
      case 'ISSUE':
        return CheckCircleOutlined;
      case 'REVOKE':
        return CloseCircleOutlined;
      case 'DOWNLOAD_PACK':
        return DownloadIcon;
      case 'SSL_TEST':
        return ExperimentOutlined;
      default:
        return ClockCircleOutlined;
    }
  }
</script>

<style lang="less" scoped>
  .ca-detail {
    .header-card {
      margin-bottom: 16px;

      .header-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        flex-wrap: wrap;
      }

      .header-left {
        display: flex;
        align-items: center;
        gap: 16px;
        min-width: 0;
      }

      .ca-icon {
        width: 80px;
        height: 80px;
        flex-shrink: 0;
      }

      .ca-title {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;

        .name-text {
          font-size: 18px;
          font-weight: 600;
          color: #1f2933;
        }
      }

      .meta-line {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-top: 8px;
        font-size: 12px;
        color: #888;

        .anticon {
          margin-right: 4px;
        }
      }
    }

    .metric-row {
      margin-bottom: 16px;
    }

    .metric-card {
      display: flex;
      align-items: center;
      padding: 16px;

      :deep(.ant-card-body) {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 0;
        width: 100%;
      }

      .metric-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        flex-shrink: 0;

        &.algo {
          background: linear-gradient(135deg, #1890ff, #0050b3);
        }

        &.validity.ok {
          background: linear-gradient(135deg, #52c41a, #237804);
        }

        &.validity.warn {
          background: linear-gradient(135deg, #faad14, #d48806);
        }

        &.validity.down {
          background: linear-gradient(135deg, #f5222d, #a8071a);
        }

        &.validity.unknown {
          background: linear-gradient(135deg, #bfbfbf, #8c8c8c);
        }

        &.devices {
          background: linear-gradient(135deg, #722ed1, #391085);
        }

        &.online {
          background: linear-gradient(135deg, #13c2c2, #006d75);
        }
      }

      .metric-label {
        font-size: 12px;
        color: #888;
      }

      .metric-value {
        font-size: 20px;
        font-weight: 600;
        color: #1f2933;
        margin-top: 2px;
      }
    }

    .panel-card {
      :deep(.ant-card-body) {
        padding: 16px 24px;
      }
    }

    .pem-block {
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      background: #fafafa;

      .pem-toolbar {
        display: flex;
        justify-content: flex-end;
        padding: 8px;
        border-bottom: 1px solid #f0f0f0;
        background: #fff;
      }

      .pem-content {
        margin: 0;
        padding: 12px 16px;
        font-family: ui-monospace, SFMono-Regular, 'Roboto Mono', monospace;
        font-size: 12px;
        line-height: 1.6;
        color: #555;
        white-space: pre-wrap;
        word-break: break-all;
        max-height: 480px;
        overflow: auto;
      }
    }

    .audit-row {
      display: flex;
      align-items: center;
      gap: 8px;

      .audit-time {
        font-size: 12px;
        color: #999;
      }
    }

    .audit-detail {
      font-size: 12px;
      color: #555;
      margin-top: 4px;
      word-break: break-all;
    }
  }
</style>
