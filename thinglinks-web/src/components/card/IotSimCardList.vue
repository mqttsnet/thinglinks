<template>
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header" v-if="!isSelect">
      <span>{{ title }}</span>
      <div>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['card:sim:cardSimInfo:add']"
        >
          {{ t('common.title.add') }}
        </a-button>
        <!-- <a-button preIcon="ant-design:download-outlined" @click="handleImport">{{
          t('common.title.import')
        }}</a-button> -->
        <a-button preIcon="ant-design:swap-outlined" @click="switchView">{{
          t('common.switchView')
        }}</a-button>
      </div>
    </div>
    <!-- loading效果 -->
    <div class="loading" v-if="loading">
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
          <div class="card_wrap">
            <div class="left_info">
              <div class="item">
                <a-tooltip placement="top" :title="record.cardNumber || '未命名'">
                  <div class="name">{{ record.cardNumber || '未命名' }}</div>
                </a-tooltip>
              </div>
              <a-row :gutter="[4, 0]">
                <a-col :span="12">
                  <div class="item">
                    <div class="label">{{ t('card.sim.cardSimInfo.flowsUsed') }}</div>
                    <div class="content">{{ record.flowsUsed }}</div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="item">
                    <div class="label">{{ t('card.sim.cardSimInfo.flowsRest') }}</div>
                    <div class="content">{{ record.flowsRest }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-row :gutter="[4, 0]">
                <a-col :span="12">
                  <div class="item">
                    <div class="label">{{ t('card.sim.cardSimInfo.createdTime') }}</div>
                    <div class="content" :title="record.createdTime">{{ record.createdTime }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-row :gutter="[4, 0]">
                <a-col :span="20">
                  <div class="item">
                    <div class="label">{{ t('card.sim.cardSimInfo.flowsUsedScale') }}</div>
                    <div class="content" :title="record.flowsUsedScale"
                      ><a-progress
                        :percent="(record.flowsUsed / record.flowsTotal) * 100"
                        size="small"
                    /></div>
                  </div>
                </a-col>
              </a-row>
              <div class="item last_item">
                <!-- <div class="label">{{ t('video.media.videoStreamProxy.mediaIdentification') }}</div>
                <div class="content" :title="record.mediaIdentification">{{
                  record.mediaIdentification ?? null
                }}</div> -->
              </div>
              <div class="btns">
                <div class="btn" v-hasAnyPermission="['card:sim:cardSimInfo:delete']">
                  <a-tooltip
                    placement="top"
                    :title="t('common.title.delete')"
                    v-hasAnyPermission="['card:sim:cardSimInfo:delete']"
                  >
                    <img
                      alt=""
                      src="../../../../../../assets/images/iot/link/device/delete-y.png"
                      @click="handleDelete(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasAnyPermission="['card:sim:cardSimInfo:copy']">
                  <a-tooltip
                    placement="top"
                    :title="t('common.title.copy')"
                    v-hasAnyPermission="['card:sim:cardSimInfo:copy']"
                  >
                    <img
                      alt=""
                      src="../../../../../../assets/images/iot/link/device/copy-y.png"
                      @click="handleCopy(record)"
                    />
                  </a-tooltip>
                </div>
                <div class="btn" v-hasAnyPermission="['card:sim:cardSimInfo:edit']">
                  <a-tooltip
                    placement="top"
                    :title="t('common.title.edit')"
                    v-hasAnyPermission="['card:sim:cardSimInfo:edit']"
                  >
                    <img
                      alt=""
                      src="../../../../../../assets/images/iot/link/device/edit-y.png"
                      @click="handleEdit(record)"
                    />
                  </a-tooltip>
                </div>
              </div>
            </div>
            <img
              v-if="record.onlineFlag == 1"
              class="right_img"
              :style="{ width: '140px', height: '140px', margin: '0px 18px 0 0' }"
              src="../../../../../../assets/images/card/sim.png"
              alt=""
              @click="handleView(record, e)"
            />
            <img
              v-else
              class="right_img"
              :style="{ width: '140px', height: '140px', margin: '0px 18px 0 0' }"
              src="../../../../../../assets/images/card/no-sim.png"
              alt=""
              @click="handleView(record, e)"
            />
            <div class="type" :class="record.onlineFlag == 0 ? 'error' : 'normal'">
              {{ getDictLabel('CARD_SIMINFO_ONLINE_FLAG', record.onlineFlag, '') }}
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
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  import { useDict } from '/@/components/Dict';
  import { useRouter } from 'vue-router';
  import { page, deleteSingle } from '/@/api/card/sim/cardSimInfo';
  import EditModal from '/@/views/card/sim/cardSimInfo/Edit.vue';
  import type { CardSimInfoPageQuery } from '/@/api/card/sim/model/cardSimInfoModel';
  import Player from '/@/views/video/media/videoStreamProxy/Player.vue';

  const props = defineProps({ title: String, searchData: { type: Object, default: () => {} } });
  const emit = defineEmits(['input']);

  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createConfirm } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerPlayer, { openModal: openPlayer }] = useModal();
  const { push } = useRouter();
  const pageSizeOptions = ref(['10', '20', '30', '40', '50']);

  const current = ref<number>(1);
  const pageSize = ref<number>(20);
  const total = ref<number>(0);
  const dataList = ref<CardSimInfoPageQuery[]>([]);
  const model = reactive<CardSimInfoPageQuery>({});
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
  // 弹出编辑页面
  const handleEdit = (record: Recordable, e: Event) => {
    e?.stopPropagation();
    openModal(true, {
      record,
      type: ActionEnum.EDIT,
    });
  };
  // 点击单行删除
  const handleDelete = (record) => {
    createConfirm({
      iconType: 'warning',
      content: t('common.tips.confirmDelete'),
      onOk: async () => {
        try {
          await deleteSingle(record.id);
          handleSuccess();
        } catch (e) {}
      },
    });
  };
  // 弹出复制页面
  const handleCopy = (record: Recordable, e: Event) => {
    e?.stopPropagation();
    openModal(true, {
      record,
      type: ActionEnum.COPY,
    });
  };

  // 弹出查看页面
  const handleView = (record: Recordable, e: Event) => {
    e?.stopPropagation();
    push({
      name: 'SIM卡详情',
      params: { id: record.id },
    });
  };

  const handlePlay = (record: CardSimInfoPageQuery, e: MouseEvent) => {
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
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 33px;

    span {
      margin-left: 7px;
    }

    div {
      .ant-btn {
        margin: 0 8px 8px 0;
      }
    }
  }

  .loading {
    width: 100%;
    height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .card-content {
    display: flex;
    flex-direction: column;

    .card_wrap {
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: relative;
      background-image: url('../../../../../../../assets/images/link/blue-bg.png');
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

        .last_item {
          margin-bottom: 36px;
        }

        .btns {
          display: flex;
          align-items: center;
          justify-content: space-around;
          position: absolute;
          left: 12px;
          bottom: 8px;
          width: 130px;
          height: 28px;
          border-radius: 45px;
          padding: 0 10px;
          border: 2px solid #1a66ff;

          .btn {
            img {
              width: 16px;
              height: 16px;
            }
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

    .card_pagination {
      align-self: flex-end;
    }
  }

  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
</style>
