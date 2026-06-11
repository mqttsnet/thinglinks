<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('card.channel.cardChannelInfo.selectChannel')"
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
            <div
              class="card_wrap"
              :class="selectNode.id === record.id ? 'active' : ''"
              @click="handleSelectNode(record)"
            >
              <div class="left_info">
                <div class="item">
                  <div class="name">{{ record.channelName || '未命名' }}</div>
                </div>
                <a-row :gutter="[4, 0]">
                  <a-col :span="12">
                    <div class="item">
                      <div class="label">{{ t('card.channel.cardChannelInfo.operatorType') }}</div>
                      <div
                        class="content"
                        :title="getDictLabel(DictEnum.CARD_CHANNEL_OPERATOR, record.operatorType)"
                        >{{ getDictLabel(DictEnum.CARD_CHANNEL_OPERATOR, record.operatorType) }}</div
                      >
                    </div>
                  </a-col>
                  <a-col :span="12">
                    <div class="item">
                      <div class="label">{{ t('card.channel.cardChannelInfo.officialFlag') }}</div>
                      <div class="content" :title="getDictLabel(DictEnum.CARD_CHANNEL_OFFICIAL_FLAG, record.officialFlag)">{{ getDictLabel(DictEnum.CARD_CHANNEL_OFFICIAL_FLAG, record.officialFlag) }}</div>
                    </div>
                  </a-col>
                </a-row>
                <div class="item">
                  <div class="label">{{
                    t('card.channel.cardChannelInfo.channelType')
                  }}</div>
                   <div class="content" :title="getDictLabel(DictEnum.CARD_CHANNEL_TYPE, record.channelType)">{{ getDictLabel(DictEnum.CARD_CHANNEL_TYPE, record.channelType) }}</div>
                </div>
              </div>
               <img
                  class="right_img"
                  v-if="record?.operatorType === 1"
                  src="../../../../../../assets/images/card/yidong.png"
                />
                <img
                  v-else-if="record?.operatorType === 2"
                  class="right_img"
                  src="../../../../../../assets/images/card/dianxin.png"
                />
                <img
                  class="right_img"
                  v-else-if="record?.operatorType === 3"
                  src="../../../../../../assets/images/card/liantong.png"
                />
              <div class="type"
            :class="record.status == 1 ? 'error' : 'normal'">
              {{ getDictLabel(DictEnum.CARD_CHANNEL_STATUS, record.status) }}
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
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import { DictEnum } from '/@/enums/commonEnum';
import { useDict } from '/@/components/Dict';
import { page } from '/@/api/card/channel/cardChannelInfo';
import type { CardChannelInfoPageQuery } from '/@/api/card/channel/model/cardChannelInfoModel';
import { useMessage } from '/@/hooks/web/useMessage';
const props = defineProps({ value: String });
const { t } = useI18n();
const { getDictLabel } = useDict();
const { createMessage } = useMessage();
const [registerModal, { closeModal }] = useModalInner();
const emits = defineEmits(['success', 'updateSelectNode']);
const selectNode = reactive<CardChannelInfoPageQuery>({});
const current = ref<number>(1);
const pageSize = ref<number>(20);
const total = ref<number>(0);
const dataList = ref<CardChannelInfoPageQuery[]>([]);
const loading = ref<boolean>(false);
const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

// 获取列表
const getList = async (current: number, size: number, model = {}) => {
  loading.value = true;
  const res = await page({
    current,
    size,
    model,
    extra: {},
  });
  total.value = res.total;
  dataList.value = res.records;
  loading.value = false;
};

const handleSelectNode = (record: CardChannelInfoPageQuery) => {
  Object.assign(selectNode, record);
};

const handleSubmit = () => {
  if (Object.keys(selectNode).length === 0) {
    createMessage.warning(t('video.media.proxy.pleaseSelectNode'));
    return;
  }
  emits('success', selectNode);
  closeModal();
};
const handleCancel = () => {
  emits('success', {});
  emits('updateSelectNode', { channelName: '' });
  handleSelectNode({ id: '' });
  closeModal();
};
const handleChangePagination = (page: number, size: number) => {
  current.value = page;
  pageSize.value = size;
};

// 监控分页参数变化，获取新数据
watch(
  [current, pageSize],
  async ([newCurrent, newSize]) => {
    await getList(newCurrent, newSize);
  },
  { immediate: true },
);

// 监控 props.value 变化，设置 selectNode
watch(
  () => props.value,
  async (newValue) => {
    if (!newValue) {
      emits('updateSelectNode', { channelName: '' });
      handleSelectNode({ id: '' });
      return;
    }
    if (dataList.value.length === 0) {
      await getList(current.value, pageSize.value, { channelId: newValue });
    }
    const selected = dataList.value.find((item) => item.id === newValue);
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
  background-color: #fff;
  padding: 22px;

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
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: relative;
      // background-image: url('/@/assets/images/iot/link/blue-bg.png');
      height: 200px;
      border: 1px solid #e8e8e8;
      padding: 8px 12px 8px;
      border-radius: 8px;
      transition: all linear 0.2s;
      background-color: #fff;
      background-repeat: no-repeat;
      background-position: center center;
      background-size: 104% 104%;
      transition: all 0.5s;
      // min-height: 228px;
      height: 100%;

      &:hover {
        border-color: #1a66ff;
        transform: scale(1.01);
        box-shadow: 0px 4px 12px rgba(0, 26, 51, 0.08);
      }

      .left_info {
        flex: 1;

        .item {
          margin-bottom: 4px;

          .label {
            font-size: 12px;
            color: #999;
          }

          .name {
            font-weight: 500;
          }

          .content {
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
          }
        }
      }

      .right_img {
        width: 180px;
        height: 180px;
        margin-top: 24px;
        cursor: pointer;
      }

      .type {
        position: absolute;
        top: 0;
        right: 0;
        border-radius: 0 8px 0 4px;
        padding: 2px 8px;

        &.error {
          background: #fad7d9;
          color: #d43030;
        }

        &.normal {
          background-color: #dce5f5;
          color: #1a66ff;
        }
      }
    }

    .active {
      border: 1px solid #1a66ff;
    }

    .card_pagination {
      align-self: flex-end;
    }
  }

  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
}
</style>
