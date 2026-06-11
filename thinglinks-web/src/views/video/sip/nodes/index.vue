<template>
  <PageWrapper dense contentFullHeight>
    <div class="sip-nodes-page">
      <!-- 服务信息概览 -->
      <a-row :gutter="16" class="info-cards">
        <a-col :span="8">
          <a-card hoverable>
            <a-statistic :title="t('video.sip.nodes.listenPort')" :value="serverInfo.port || '-'" :value-style="{ color: '#1890ff' }">
              <template #prefix>
                <ApiOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card hoverable>
            <a-statistic :title="t('video.sip.nodes.commandTimeout')" :value-style="{ color: '#52c41a' }">
              <template #prefix>
                <ClockCircleOutlined />
              </template>
              <template #formatter>
                {{ serverInfo.timeout ? serverInfo.timeout + ' ms' : '-' }}
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card hoverable>
            <a-statistic :title="t('video.sip.nodes.monitorNicCount')" :value="serverInfo.monitorIps?.length || 0" :value-style="{ color: '#722ed1' }">
              <template #prefix>
                <ClusterOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 监听网卡列表 -->
      <a-card :title="t('video.sip.nodes.monitorNic')" class="nic-card" :loading="loading">
        <template #extra>
          <a-button size="small" preIcon="ant-design:reload-outlined" @click="loadData">
            {{ t('common.refresh') }}
          </a-button>
        </template>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(ip, idx) in serverInfo.monitorIps" :key="ip">
            <a-card size="small" hoverable class="nic-item">
              <div class="nic-content">
                <div class="nic-icon">
                  <WifiOutlined style="font-size: 28px; color: #1890ff" />
                </div>
                <div class="nic-info">
                  <div class="nic-ip">{{ ip }}</div>
                  <div class="nic-label">{{ t('video.sip.nodes.monitorNic') }} #{{ idx + 1 }}</div>
                </div>
                <div class="nic-status">
                  <a-badge status="success" :text="t('video.sip.nodes.monitoring')" />
                </div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="24" v-if="!loading && !serverInfo.monitorIps?.length">
            <a-empty :description="t('video.sip.nodes.noMonitorNic')" />
          </a-col>
        </a-row>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { reactive, ref, onMounted } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { ApiOutlined, ClockCircleOutlined, ClusterOutlined, WifiOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getServerInfo } from '/@/api/video/sip/sipConfig';
  import type { SipServerInfo } from '/@/api/video/sip/model/sipConfigModel';

  const { t } = useI18n();

  const serverInfo = reactive<SipServerInfo>({});
  const loading = ref(false);

  onMounted(loadData);

  async function loadData() {
    loading.value = true;
    try {
      const data = await getServerInfo();
      Object.assign(serverInfo, data);
    } catch (_) {
      // ignore
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .sip-nodes-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .info-cards {
    .ant-card {
      border-radius: 8px;
    }
  }

  .nic-card {
    border-radius: 8px;
  }

  .nic-item {
    border-radius: 8px;

    .nic-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .nic-icon {
      flex-shrink: 0;
    }

    .nic-info {
      flex: 1;
      min-width: 0;

      .nic-ip {
        font-size: 15px;
        font-weight: 500;
        color: rgba(0, 0, 0, 0.85);
        word-break: break-all;
      }

      .nic-label {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
        margin-top: 2px;
      }
    }

    .nic-status {
      flex-shrink: 0;
    }
  }
</style>
