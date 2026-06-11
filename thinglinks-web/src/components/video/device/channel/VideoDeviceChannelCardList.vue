<template>
  <div class="node-card-list">
    <div v-if="loading" class="loading">
      <a-spin />
    </div>
    <div class="card-header">
      <span class="title">{{ title }}</span>
      <div class="btn_group">
        <a-button
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
          v-hasAnyPermission="['video:device:channel:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <div class="card-content">
      <a-row v-if="dataList.length > 0" :gutter="[24, 12]">
        <a-col
          v-for="record in dataList"
          :key="record.id"
          :lg="12"
          :md="12"
          :sm="24"
          :xl="8"
          :xs="24"
          :xxl="6"
        >
          <div class="card_wrap">
            <div class="left_info">
              <div class="item">
                <a-tooltip placement="top" :title="record.deviceName || '未命名'">
                  <div class="name">{{ record.deviceName || '未命名' }}</div>
                </a-tooltip>
              </div>
              <a-row :gutter="[4, 0]">
                <a-col :span="12">
                  <div class="item">
                    <div class="label">{{ t('video.device.info.manufacturer') }}</div>
                    <div class="content" :title="record.manufacturer">{{
                      record.manufacturer
                    }}</div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="item">
                    <div class="label">{{ t('video.device.info.ip') }}</div>
                    <div class="content" :title="record.ip">{{ record.ip }}</div>
                  </div>
                </a-col>
              </a-row>
              <div class="item">
                <div class="label">{{ t('video.media.push.createdTime') }}</div>
                <div class="content" :title="record.createdTime">{{ record.createdTime }}</div>
              </div>
              <div class="item last_item">
                <div class="label">{{ t('video.media.push.mediaIdentification') }}</div>
                <div class="content" :title="record.mediaIdentification">{{
                  record.mediaIdentification ?? null
                }}</div>
              </div>
              <div class="btns">
                <div class="btn" v-hasAnyPermission="['video:device:channel:delete']">
                  <a-tooltip placement="top" :title="t('common.title.delete')">
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/delete-y.png"
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn">
                  <a-tooltip
                    placement="top"
                    :title="t('common.title.edit')"
                    v-hasAnyPermission="['video:device:channel:edit']"
                  >
                    <img
                      alt=""
                      src="/@/assets/images/iot/link/device/edit-y.png"
                      @click="handleEdit(record)"
                    />
                  </a-tooltip>
                </div>
                <!-- <div class="btn">
                  <a-tooltip placement="top" :title="t('video.device.info.play')">
                    <img
                      alt=""
                      :style="{ width: '20px', height: '20px' }"
                      src="/@/assets/images/video/media/proxy/bofang.png"
                      @click="handlePlay(record, e)"
                    />
                  </a-tooltip>
                </div> -->
              </div>
            </div>
            <img
              class="right_img"
              :style="{ width: '170px', height: '140px', margin: '0px 18px 0 0' }"
              src="/@/assets/images/video/device/info/video-device-channel.svg"
              alt=""
              @click="handleView(record, e)"
            />
            <div class="type">
              {{
                getDictLabel(DictEnum.VIDEO_MEDIA_STREAM_PROXY_STATUS, String(record.onlineStatus))
              }}
            </div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="card_pagination">
        <a-pagination
          v-model:current="current"
          v-model:pageSize="pageSize"
          :page-size-options="pageSizeOptions"
          :show-total="(total) => t('component.table.total', { total })"
          :total="total"
          show-quick-jumper
          show-size-changer
          size="small"
          @change="handleChangePagination"
        />
      </div>
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
  <Player @register="registerPlayer" />
</template>
<script setup lang="ts">
  import { ref, reactive, watch, watchEffect } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useRouter } from 'vue-router';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDict } from '/@/components/Dict';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { page, remove } from '/@/api/video/device/channel';
  import EditModal from '/@/views/video/device/channel/Edit.vue';
  import type { VideoStreamProxyPageQuery } from '/@/api/video/media/model/proxyModel';
  import Player from '/@/views/video/media/proxy/Player.vue';

  const props = defineProps({ title: String, searchData: { type: Object, default: () => {} } });
  const emit = defineEmits(['input']);
  const { push } = useRouter();
  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createMessage, createConfirm } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerPlayer, { openModal: openPlayer }] = useModal();
  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

  const current = ref<number>(1);
  const pageSize = ref<number>(20);
  const total = ref<number>(0);
  const dataList = ref<VideoStreamProxyPageQuery[]>([]);
  const model = reactive<VideoStreamProxyPageQuery>({});
  const loading = ref<boolean>(false);

  // 获取列表
  const getList = async (current: number, size: number) => {
    loading.value = true;
    const res = await page({
      current,
      size,
      ...handleFetchParams(model.value ?? {}),
    });
    total.value = res.total;
    dataList.value = res.records;
    loading.value = false;
  };

  const handleChangePagination = (page: number, size: number) => {
    current.value = page;
    pageSize.value = size;
  };

  const handleAdd = () => {
    openModal(true, {
      type: ActionEnum.ADD,
    });
  };

  const handleDelete = async (record: VideoStreamProxyPageQuery) => {
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async (e) => {
        try {
          await remove([record.id]);
          createMessage.success(t('common.tips.deleteSuccess'));
          getList(current.value, pageSize.value);
        } catch (e) {
          throw new Error(e);
        }
      },
    });
  };

  const handleEdit = (record: VideoStreamProxyPageQuery) => {
    openModal(true, {
      record,
      type: ActionEnum.EDIT,
    });
  };

  const handleView = (record: VideoStreamProxyPageQuery, e: MouseEvent) => {
    e?.stopPropagation();
    push({
      name: '设备通道',
      params: {
        id: record.id,
      },
    });
  };

  const handlePlay = (record: VideoStreamProxyPageQuery, e: MouseEvent) => {
    e?.stopPropagation;
    openPlayer(true, record);
  };

  const handleSuccess = () => {
    getList(current.value, pageSize.value);
  };

  watch(
    () => props.searchData,
    () => {
      model.value = props.searchData;
      current.value = 1;
      pageSize.value = 20;
      getList(current.value, pageSize.value);
    },
    { deep: true, immediate: true },
  );

  const switchView = () => {
    emit('input', false);
  };
</script>
<style lang="less" scoped>
  @import '../../videoCardCommon.less';
</style>
