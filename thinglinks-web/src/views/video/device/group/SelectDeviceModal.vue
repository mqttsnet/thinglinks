<template>
  <BasicModal
    @register="registerModal"
    :title="t('video.device.group.bindDevice')"
    :maskClosable="false"
    :destroyOnClose="true"
    @ok="handleSubmit"
    :width="1200"
  >
    <!-- 搜索区域 -->
    <div class="search-bar">
      <a-input
        v-model:value="searchForm.deviceIdentification"
        :placeholder="t('video.device.info.deviceIdentification')"
        allow-clear
        style="width: 180px"
        @press-enter="handleSearch"
      />
      <a-input
        v-model:value="searchForm.deviceName"
        :placeholder="t('video.device.info.deviceName')"
        allow-clear
        style="width: 180px"
        @press-enter="handleSearch"
      />
      <a-input
        v-model:value="searchForm.host"
        :placeholder="t('video.device.info.host')"
        allow-clear
        style="width: 160px"
        @press-enter="handleSearch"
      />
      <a-button type="primary" @click="handleSearch">
        <template #icon><SearchOutlined /></template>
        {{ t('common.queryText') }}
      </a-button>
      <a-button @click="handleReset">
        <template #icon><ReloadOutlined /></template>
        {{ t('common.resetText') }}
      </a-button>
    </div>

    <!-- 设备卡片列表 -->
    <div class="device-card-list">
      <a-spin :spinning="loading">
        <a-row v-if="dataList.length > 0" :gutter="[16, 16]">
          <a-col
            v-for="record in dataList"
            :key="record.id"
            :lg="12" :md="12" :sm="24" :xl="8" :xs="24" :xxl="6"
          >
            <div
              class="device-card"
              :class="{ active: selectedDevice?.id === record.id }"
              @click="handleSelect(record)"
            >
              <div class="device-card__header">
                <span class="device-card__name">{{ record.deviceName || record.customName || '-' }}</span>
                <span class="device-card__status" :class="isTruthyStatus(record.onlineStatus) ? 'online' : 'offline'">
                  {{ isTruthyStatus(record.onlineStatus) ? t('video.device.live.online') : t('video.device.live.offline') }}
                </span>
              </div>
              <div class="device-card__body">
                <div class="info-row">
                  <span class="info-label">{{ t('video.device.info.deviceIdentification') }}</span>
                  <span class="info-value" :title="record.deviceIdentification">{{ record.deviceIdentification || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">{{ t('video.device.info.host') }}</span>
                  <span class="info-value">{{ record.host || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">{{ t('video.device.info.manufacturer') }}</span>
                  <span class="info-value">{{ record.manufacturer || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">{{ t('video.device.info.channelCount') }}</span>
                  <span class="info-value">{{ record.channelCount ?? '-' }}</span>
                </div>
              </div>
              <div class="device-card__footer">
                <span class="info-time">{{ record.createdTime || '' }}</span>
              </div>
              <div v-if="selectedDevice?.id === record.id" class="check-mark">✓</div>
            </div>
          </a-col>
        </a-row>
        <a-empty v-else />
        <div class="card-pagination">
          <a-pagination
            v-model:current="current"
            v-model:pageSize="pageSize"
            :page-size-options="['10', '20', '30', '40']"
            :show-total="(total) => t('component.table.total', { total })"
            :total="total"
            show-quick-jumper
            show-size-changer
            size="small"
            @change="handlePageChange"
          />
        </div>
      </a-spin>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { page } from '/@/api/video/device/info';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';
  import type { VideoDeviceInfoResultVO } from '/@/api/video/device/model/infoModel';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emits = defineEmits(['success']);

  const [registerModal, { closeModal }] = useModalInner();

  const loading = ref(false);
  const current = ref(1);
  const pageSize = ref(20);
  const total = ref(0);
  const dataList = ref<VideoDeviceInfoResultVO[]>([]);
  const selectedDevice = ref<VideoDeviceInfoResultVO | null>(null);

  const searchForm = reactive({
    deviceIdentification: '',
    deviceName: '',
    host: '',
  });

  async function loadData() {
    loading.value = true;
    try {
      const model: Record<string, any> = {};
      if (searchForm.deviceIdentification) model.deviceIdentification = searchForm.deviceIdentification;
      if (searchForm.deviceName) model.deviceName = searchForm.deviceName;
      if (searchForm.host) model.host = searchForm.host;

      const res = await page({
        current: current.value,
        size: pageSize.value,
        model,
        extra: {},
      });
      total.value = res.total;
      dataList.value = res.records;
    } catch (e: any) {
      console.warn('Load device list error:', e);
      dataList.value = [];
      total.value = 0;
    } finally {
      loading.value = false;
    }
  }

  function handleSelect(record: VideoDeviceInfoResultVO) {
    selectedDevice.value = record;
  }

  function handleSearch() {
    current.value = 1;
    loadData();
  }

  function handleReset() {
    searchForm.deviceIdentification = '';
    searchForm.deviceName = '';
    searchForm.host = '';
    current.value = 1;
    loadData();
  }

  function handlePageChange(p: number, s: number) {
    current.value = p;
    pageSize.value = s;
  }

  function handleSubmit() {
    if (!selectedDevice.value) {
      createMessage.warning(t('video.device.group.selectGroup'));
      return;
    }
    emits('success', selectedDevice.value);
    selectedDevice.value = null;
    closeModal();
  }

  // 每次翻页/改变 pageSize 时重新加载
  watch([current, pageSize], () => {
    loadData();
  }, { immediate: true });
</script>

<style lang="less" scoped>
  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
  }

  .device-card-list {
    .card-pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .device-card {
    position: relative;
    border: 2px solid #f0f0f0;
    border-radius: 12px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.25s ease;
    background: #fff;
    height: 100%;

    &:hover {
      border-color: #91caff;
      box-shadow: 0 4px 16px rgba(24, 144, 255, 0.1);
      transform: translateY(-2px);
    }

    &.active {
      border-color: #1677ff;
      background: #f0f7ff;
      box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15);
    }

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }

    &__name {
      font-size: 15px;
      font-weight: 600;
      color: #1a1a2e;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      margin-right: 8px;
    }

    &__status {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      flex-shrink: 0;

      &.online {
        color: #52c41a;
        background: #f6ffed;
      }

      &.offline {
        color: #999;
        background: #f5f5f5;
      }
    }

    &__body {
      .info-row {
        display: flex;
        align-items: baseline;
        margin-bottom: 6px;
        font-size: 13px;
        line-height: 1.6;

        .info-label {
          color: #8c8c8c;
          min-width: 72px;
          flex-shrink: 0;

          &::after {
            content: '：';
          }
        }

        .info-value {
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          flex: 1;
        }
      }
    }

    &__footer {
      margin-top: 10px;
      padding-top: 8px;
      border-top: 1px solid #f5f5f5;

      .info-time {
        font-size: 12px;
        color: #bfbfbf;
      }
    }

    .check-mark {
      position: absolute;
      top: 8px;
      right: 12px;
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: #1677ff;
      color: #fff;
      font-size: 14px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
</style>
