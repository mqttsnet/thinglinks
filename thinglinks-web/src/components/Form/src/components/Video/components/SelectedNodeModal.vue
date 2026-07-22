<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('video.media.server.selectNode')"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :width="1200"
  >
    <div class="node-card-list">
      <div v-if="loading" class="loading">
        <a-spin />
      </div>
      <div class="card-content">
        <a-row v-if="dataList.length > 0" :gutter="[16, 16]">
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
            <div
              class="card_wrap"
              :class="{ active: selectNode.mediaIdentification === record.mediaIdentification }"
              @click="handleSelectNode(record)"
            >
              <div class="card_header">
                <span class="card_name">{{ record.name || '-' }}</span>
                <span class="card_type">{{ record?.echoMap?.type || record?.type }}</span>
              </div>
              <div class="card_body">
                <div class="info_row">
                  <span class="info_label">{{ t('video.media.server.appId') }}</span>
                  <span class="info_value" :title="record.appId">
                    {{ record.appId }}
                    <span v-if="getDictLabel(DictEnum.VIDEO_APPLICATION_SCENARIO, record.appId)" class="info_dict">
                      ({{ getDictLabel(DictEnum.VIDEO_APPLICATION_SCENARIO, record.appId) }})
                    </span>
                  </span>
                </div>
                <div class="info_row">
                  <span class="info_label">{{ t('video.media.server.host') }}</span>
                  <span class="info_value" :title="record.host">{{ record.host || record.ip || '-' }}</span>
                </div>
                <div class="info_row">
                  <span class="info_label">{{ t('video.media.server.mediaIdentification') }}</span>
                  <span class="info_value" :title="record.mediaIdentification">{{ record.mediaIdentification || '-' }}</span>
                </div>
              </div>
              <div class="card_footer">
                <span class="info_time">{{ record.createdTime || '' }}</span>
                <span v-if="record.onlineStatus" class="status_dot online"></span>
                <span v-else class="status_dot offline"></span>
              </div>
              <div v-if="selectNode.mediaIdentification === record.mediaIdentification" class="check_mark">✓</div>
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
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useDict } from '/@/components/Dict';
  import { page } from '/@/api/video/media/server';
  import type { VideoMediaServerPageQuery } from '/@/api/video/media/model/serverModel';
  import { useMessage } from '/@/hooks/web/useMessage';

  const props = defineProps({ value: String });
  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createMessage } = useMessage();
  const [registerModal, { closeModal }] = useModalInner();
  const emits = defineEmits(['success', 'updateSelectNode']);
  const selectNode = reactive<VideoMediaServerPageQuery>({});
  const current = ref<number>(1);
  const pageSize = ref<number>(20);
  const total = ref<number>(0);
  const dataList = ref<VideoMediaServerPageQuery[]>([]);
  const loading = ref<boolean>(false);
  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

  const getList = async (current: number, size: number, model = {}) => {
    loading.value = true;
    try {
      const res = await page({
        current,
        size,
        model,
        extra: {},
      });
      total.value = res.total;
      dataList.value = res.records;
    } finally {
      loading.value = false;
    }
  };

  const handleSelectNode = (record: VideoMediaServerPageQuery) => {
    Object.assign(selectNode, record);
  };

  const handleSubmit = () => {
    if (!selectNode.mediaIdentification) {
      createMessage.warning(t('video.media.proxy.pleaseSelectNode'));
      return;
    }
    emits('success', selectNode);
    closeModal();
  };

  const handleCancel = () => {
    emits('success', {});
    emits('updateSelectNode', { name: '' });
    handleSelectNode({ mediaIdentification: '' });
    closeModal();
  };

  const handleChangePagination = (page: number, size: number) => {
    current.value = page;
    pageSize.value = size;
  };

  watch(
    [current, pageSize],
    async ([newCurrent, newSize]) => {
      await getList(newCurrent, newSize);
    },
    { immediate: true },
  );

  watch(
    () => props.value,
    async (newValue) => {
      if (!newValue) {
        emits('updateSelectNode', { name: '' });
        handleSelectNode({ mediaIdentification: '' });
        return;
      }
      // 等列表加载完后再回显选中状态，不传 mediaIdentification 作为查询条件
      if (dataList.value.length === 0) {
        await getList(current.value, pageSize.value);
      }
      const selected = dataList.value.find((item) => item.mediaIdentification === newValue);
      if (selected) {
        Object.assign(selectNode, selected);
        emits('updateSelectNode', selected);
      }
    },
    { immediate: true },
  );
</script>

<style lang="less" scoped>
  .node-card-list {
    padding: 16px;

    .loading {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }

    .card-content {
      display: flex;
      flex-direction: column;

      .card_wrap {
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

        .card_header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;

          .card_name {
            font-size: 15px;
            font-weight: 600;
            color: #1a1a2e;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            margin-right: 8px;
          }

          .card_type {
            font-size: 12px;
            color: #1677ff;
            background: #e6f4ff;
            padding: 2px 8px;
            border-radius: 4px;
            flex-shrink: 0;
          }
        }

        .card_body {
          .info_row {
            display: flex;
            align-items: baseline;
            margin-bottom: 6px;
            font-size: 13px;
            line-height: 1.6;

            .info_label {
              color: #8c8c8c;
              min-width: 80px;
              flex-shrink: 0;

              &::after {
                content: '：';
              }
            }

            .info_value {
              color: #333;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              flex: 1;

              .info_dict {
                color: #8c8c8c;
                font-size: 12px;
              }
            }
          }
        }

        .card_footer {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-top: 10px;
          padding-top: 8px;
          border-top: 1px solid #f5f5f5;

          .info_time {
            font-size: 12px;
            color: #bfbfbf;
          }

          .status_dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;

            &.online {
              background: #52c41a;
              box-shadow: 0 0 4px rgba(82, 196, 26, 0.5);
            }

            &.offline {
              background: #d9d9d9;
            }
          }
        }

        .check_mark {
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

      .card_pagination {
        margin-top: 16px;
        align-self: flex-end;
      }
    }
  }
</style>
