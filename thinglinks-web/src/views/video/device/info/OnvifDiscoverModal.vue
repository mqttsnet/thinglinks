<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModel"
    :title="t('video.onvif.discoverTitle')"
    width="900px"
    :maskClosable="false"
    :keyboard="true"
    :showOkBtn="false"
    :cancelText="t('common.closeText')"
  >
    <a-row :gutter="12" class="mb-2">
      <a-col :span="6">
        <a-input
          v-model:value="state.username"
          :placeholder="t('video.onvif.username')"
          allow-clear
        />
      </a-col>
      <a-col :span="6">
        <a-input-password
          v-model:value="state.password"
          :placeholder="t('video.onvif.password')"
          allow-clear
        />
      </a-col>
      <a-col :span="8">
        <ApiVideoSelectNodeCard
          v-model:value="state.mediaIdentification"
          :placeholder="t('video.onvif.mediaIdentification')"
        />
      </a-col>
      <a-col :span="4">
        <a-button
          type="primary"
          block
          :loading="state.scanning"
          @click="handleScan"
        >
          {{ t('video.onvif.scan') }}
        </a-button>
      </a-col>
    </a-row>

    <a-table
      size="small"
      :columns="deviceColumns"
      :data-source="state.devices"
      :pagination="false"
      :loading="state.scanning"
      :rowKey="(r) => r.endpointReference || r.xaddr"
      :expandedRowKeys="state.expandedKeys"
      @expand="handleExpand"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-button size="small" type="link" @click="handleLoadProfiles(record)">
            {{ t('video.onvif.loadProfiles') }}
          </a-button>
        </template>
      </template>

      <template #expandedRowRender="{ record }">
        <a-table
          size="small"
          :columns="profileColumns"
          :data-source="state.profilesByXaddr[record.xaddr] || []"
          :pagination="false"
          :rowKey="(r) => r.token"
          :loading="state.loadingProfiles === record.xaddr"
        >
          <template #bodyCell="{ column: pc, record: profile }">
            <template v-if="pc.key === 'action'">
              <a-button
                size="small"
                type="primary"
                :loading="state.importing === profile.token"
                :disabled="!state.mediaIdentification"
                @click="handleImport(record, profile)"
              >
                {{ t('video.onvif.import') }}
              </a-button>
            </template>
          </template>
        </a-table>
      </template>
    </a-table>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    discoverOnvif,
    fetchOnvifProfiles,
    importOnvifDevice,
    type OnvifDevice,
    type OnvifProfile,
  } from '/@/api/video/onvif';
  import ApiVideoSelectNodeCard from '/@/components/Form/src/components/Video/ApiSelectNodeCard.vue';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const emit = defineEmits(['success', 'register']);

  const state = reactive<{
    scanning: boolean;
    devices: OnvifDevice[];
    expandedKeys: string[];
    profilesByXaddr: Record<string, OnvifProfile[]>;
    loadingProfiles: string | null;
    importing: string | null;
    username: string;
    password: string;
    mediaIdentification: string;
  }>({
    scanning: false,
    devices: [],
    expandedKeys: [],
    profilesByXaddr: {},
    loadingProfiles: null,
    importing: null,
    username: '',
    password: '',
    mediaIdentification: '',
  });

  const [registerModel] = useModalInner(() => {
    state.devices = [];
    state.expandedKeys = [];
    state.profilesByXaddr = {};
  });

  const deviceColumns = [
    { title: t('video.onvif.xaddr'), dataIndex: 'xaddr', ellipsis: true },
    { title: t('video.onvif.types'), dataIndex: 'types', ellipsis: true },
    { title: t('video.onvif.endpointReference'), dataIndex: 'endpointReference', ellipsis: true },
    { title: t('common.title.action'), key: 'action', width: 120 },
  ];

  const profileColumns = [
    { title: t('video.onvif.profileName'), dataIndex: 'name' },
    { title: t('video.onvif.encoding'), dataIndex: 'videoEncoding', width: 80 },
    {
      title: t('video.onvif.resolution'),
      customRender: ({ record }) => `${record.width || '-'}×${record.height || '-'}`,
      width: 120,
    },
    { title: t('video.onvif.frameRate'), dataIndex: 'frameRate', width: 80 },
    { title: t('video.onvif.bitrate'), dataIndex: 'bitrate', width: 100 },
    { title: t('common.title.action'), key: 'action', width: 100 },
  ];

  async function handleScan() {
    state.scanning = true;
    try {
      const list = await discoverOnvif(4);
      state.devices = list || [];
      if (state.devices.length === 0) {
        createMessage.warning(t('video.onvif.noDeviceFound'));
      }
    } catch (e: any) {
      createMessage.error(t('video.onvif.scanFailed') + ': ' + (e?.message || e));
    } finally {
      state.scanning = false;
    }
  }

  function handleExpand(expanded: boolean, record: OnvifDevice) {
    const key = record.endpointReference || record.xaddr;
    if (expanded) {
      state.expandedKeys = [...state.expandedKeys, key];
      if (!state.profilesByXaddr[record.xaddr]) {
        handleLoadProfiles(record);
      }
    } else {
      state.expandedKeys = state.expandedKeys.filter((k) => k !== key);
    }
  }

  async function handleLoadProfiles(record: OnvifDevice) {
    state.loadingProfiles = record.xaddr;
    try {
      const list = await fetchOnvifProfiles(record.xaddr, state.username, state.password);
      state.profilesByXaddr[record.xaddr] = list || [];
      const key = record.endpointReference || record.xaddr;
      if (!state.expandedKeys.includes(key)) {
        state.expandedKeys = [...state.expandedKeys, key];
      }
    } catch (e: any) {
      createMessage.error(t('video.onvif.profilesFailed') + ': ' + (e?.message || e));
    } finally {
      state.loadingProfiles = null;
    }
  }

  async function handleImport(device: OnvifDevice, profile: OnvifProfile) {
    if (!state.mediaIdentification) {
      createMessage.warning(t('video.onvif.mediaIdentificationRequired'));
      return;
    }
    state.importing = profile.token;
    try {
      const deviceId = await importOnvifDevice({
        xaddr: device.xaddr,
        username: state.username || undefined,
        password: state.password || undefined,
        profileToken: profile.token,
        mediaIdentification: state.mediaIdentification,
        customName: profile.name,
      });
      createMessage.success(t('video.onvif.importSuccess') + ': ' + deviceId);
      emit('success');
    } catch (e: any) {
      createMessage.error(t('video.onvif.importFailed') + ': ' + (e?.message || e));
    } finally {
      state.importing = null;
    }
  }
</script>

<style lang="less" scoped>
  .mb-2 {
    margin-bottom: 12px;
  }
</style>
