<template>
  <!-- 物联网卡渠道卡片按钮组 -->
  <div style="background-color: #fff; padding: 6px">
    <div class="card-header" v-if="!isSelect">
      <span>{{ title }}</span>
      <div>
        <a-button
          type="primary"
          preIcon="ant-design:plus-outlined"
          @click="handleAdd"
          v-hasAnyPermission="['card:channel:cardChannelInfo:add']"
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
    <div class="loading" v-if="cardLoading">
      <a-spin />
    </div>
    <!-- 卡片列表 -->
    <div style="padding: 0 23px">
      <a-row :gutter="[24, 12]" v-if="cardList.length">
        <a-col
          v-for="record in cardList"
          :key="record.id"
          :xs="isSelect ? 12 : 24"
          :sm="isSelect ? 12 : 24"
          :md="12"
          :lg="12"
          :xl="isSelect ? 12 : 8"
          :xxl="isSelect ? 12 : 6"
        >
          <div :class="isSelect ? 'isSelect' : ''">
            <!-- 容器 -->
            <div
              class="device-item"
              :class="
                record?.id == channelId && isSelect
                  ? 'isSelected'
                  : record?.id == channelId
                  ? 'active'
                  : ''
              "
              @click="selectItem(record, $event)"
            >
              <!-- 详细信息介绍 -->
              <div class="device-info">
                <!-- 这里不确定是否要判断不同的图片
                  v-hasAnyPermission="[RoleEnum.ORG_SWITCH]"
                  src/assets/images/iot/channel/dianxin.jpg
                -->
                <img
                  class=".img"
                  v-if="record?.operatorType === 1"
                  @click="handleView(record, $event)"
                  src="../../../../../../assets/images/card/yidong.png"
                />
                <img
                  v-else-if="record?.operatorType === 2"
                  class=".img"
                  @click="handleView(record, $event)"
                  src="../../../../../../assets/images/card/dianxin.png"
                />
                <img
                  class=".img"
                  v-else-if="record?.operatorType === 3"
                  @click="handleView(record, $event)"
                  src="../../../../../../assets/images/card/liantong.png"
                />
                <!-- 信息 -->
                <div class="info">
                  <a-tooltip placement="topLeft" :title="record?.channelName">
                    <div class="device-name">{{ record?.channelName }}</div>
                  </a-tooltip>
                  <!-- 运营商 -->
                  <div class="device-form">
                    <span class="label">{{ t('card.channel.cardChannelInfo.operatorType') }}</span>
                    <span class="value">{{
                      getDictLabel('CARD_CHANNEL_OPERATOR', record?.operatorType, '')
                    }}</span>
                  </div>
                  <!-- 是否官方 -->
                  <div class="device-form">
                    <span class="label">{{ t('card.channel.cardChannelInfo.officialFlag') }}</span>
                    <span class="value">{{
                      getDictLabel('CARD_CHANNEL_OFFICIAL_FLAG', record?.officialFlag, '')
                    }}</span>
                  </div>
                  <!-- 渠道类别 -->
                  <div class="device-form">
                    <span class="label">{{ t('card.channel.cardChannelInfo.channelType') }}</span>
                    <span class="value">{{
                      getDictLabel('CARD_CHANNEL_TYPE', record?.channelType, '')
                    }}</span>
                  </div>
                </div>
              </div>
              <!-- 卡片右侧 -->
              <div class="device-btns">
                <div class="device-status">
                  <!-- 渠道状态 -->
                  <!-- <img :src="record?.status == 1 ? Icon4 : Icon5" alt="" class="img" /> -->
                  <span class="label">{{ t('card.channel.cardChannelInfo.status') }}</span>

                  <!-- 如果状态为1，则表示未连接 -->
                  <span class="red" v-if="record?.status == 1">{{
                    getDictLabel('CARD_CHANNEL_STATUS', record?.status, '')
                  }}</span>
                  <!-- 如果状态为0，则表示连接 -->
                  <span v-else class="green">{{
                    getDictLabel('CARD_CHANNEL_STATUS', record?.status, '')
                  }}</span>
                </div>
                <!-- 按钮组 -->
                <div v-if="!isSelect">
                  <div
                    class="btn primary"
                    @click="handleEdit(record)"
                    v-hasAnyPermission="['card:channel:cardChannelInfo:edit']"
                    >{{ t('common.title.edit') }}</div
                  >
                  <!-- <div
                    class="btn plain"
                    v-if="record?.status != 0"
                    @click="handleStatus(record, 0)"
                    >{{ t('card.channel.cardChannelInfo.start') }}</div
                  >
                  <div class="btn danger" v-else @click="handleStatus(record, 1)">{{
                    t('card.channel.cardChannelInfo.stop')
                  }}</div> -->
                  <div
                    class="btn danger"
                    @click="handleDelete(record)"
                    v-hasAnyPermission="['card:channel:cardChannelInfo:delete']"
                    >{{ t('common.title.delete') }}</div
                  >
                </div>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
      <a-empty v-else />
      <div class="tr">
        <a-pagination
          @change="change"
          size="small"
          v-model:current="current"
          v-model:pageSize="size"
          :total="total"
          :show-total="(total) => `共 ${total} 条数据`"
          show-size-changer
          show-quick-jumper
          :page-size-options="pageSizeOptions"
        />
      </div>
    </div>
  </div>
  <EditModal @register="registerModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, ref, watch, reactive } from 'vue';
  // util
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { useModal } from '/@/components/Modal';
  import { handleFetchParams } from '/@/utils/thinglinks/common';
  // api
  import type { CardChannelInfoResultVO } from '/@/api/card/channel/model/cardChannelInfoModel';
  import { page, deleteSingle } from '/@/api/card/channel/cardChannelInfo';
  // components
  import { Card, Row, Col, Spin, Divider, Tooltip, Pagination } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import { useDict } from '/@/components/Dict';
  import { useMessage } from '/@/hooks/web/useMessage';
  import EditModal from '/@/views/card/channel/cardChannelInfo/Edit.vue';

  const { getDictLabel } = useDict();
  import Icon4 from '/@/assets/images/iot/link/device/Icon4.png';
  import Icon5 from '/@/assets/images/iot/link/device/Icon5.png';
  export default defineComponent({
    name: 'IotChannelCardList',
    components: {
      ACard: Card,
      ARow: Row,
      ACol: Col,
      ASpin: Spin,
      ADivider: Divider,
      ATooltip: Tooltip,
      EditModal,
      APagination: Pagination,
    },
    props: {
      title: {
        type: String,
        default: '列表',
      },
      isSelect: {
        type: Boolean,
        default: false,
      },
      productIdentification: {
        type: String,
        default: '',
      },
      searchData: {
        type: Object,
        default: () => {},
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();
      const { push } = useRouter();
      const { createConfirm } = useMessage();

      // 渠道卡片列表加载状态
      const cardLoading = ref<boolean>(false);
      // 卡片数据源
      const cardList = ref<CardChannelInfoResultVO[]>([]);
      // 定义查询参数
      const model = ref<any>({});
      // 分页查询
      const current = ref(1);
      const size = ref(20);
      const total = ref(0);
      function change(page: number, pageSize: number) {
        console.log(page, pageSize, size.value);
        getCardList();
      }
      /**
       * 获取卡片数据源
       */
      const getCardList = () => {
        cardLoading.value = true;
        page({
          current: current.value,
          size: size.value,
          ...handleFetchParams(model.value),
        }).then((res: any) => {
          total.value = res.total;
          cardList.value = res.records;
          cardLoading.value = false;
        });
      };
      // 渠道ID
      const channelId = ref<string>('');
      // 点击某个渠道卡片
      function selectItem(record: Recordable, e: Event) {
        e?.stopPropagation();
        channelId.value = record.id;
        // 如果选择，选中改项
        if (props.isSelect) {
          console.log(record, 'record');
          // selectChannelCard(record);
        }
      }
      // 弹出查看页面
      function handleView(record: Recordable, e: Event) {
        e?.stopPropagation();
        channelId.value = record.id;
        // 如果选择，选中改项
        if (props.isSelect) {
          // selectDeviceCard(record);
        } else {
          push({
            name: '渠道详情',
            params: { id: record.id },
          });
        }
      }
      /**
       * 顶部按钮组的动作
       * */
      // 切换视图 卡片&&列表
      const switchFlag = ref<boolean>(true);
      function switchView() {
        switchFlag.value = !switchFlag.value;
        emit('input', switchFlag.value);
      }
      // 弹出新增页面
      function handleAdd() {
        openModal(true, {
          type: ActionEnum.ADD,
        });
      }
      // 新增或编辑成功回调
      function handleSuccess() {
        getCardList();
      }

      // 弹出编辑页面
      function handleEdit(record: Recordable) {
        // e?.stopPropagation();
        openModal(true, {
          record,
          type: ActionEnum.EDIT,
        });
      }
      // // 修改设备状态
      // function handleStatus(record, status: number) {
      //   createConfirm({
      //     iconType: 'warning',
      //     content:
      //       status == 1 ? t('common.tips.confirmEnable') : t('common.tips.confirmDeactivate'),
      //     onOk: async () => {
      //       try {
      //         // await statusChange(record.id, { status: status });
      //         getCardList();
      //       } catch (e) {}
      //     },
      //   });
      // }
      // 删除
      function handleDelete(record) {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            try {
              await deleteSingle(record.id);
              getCardList();
            } catch (e) {}
          },
        });
      }
      watch(
        () => props.searchData,
        (newValue, oldValue) => {
          console.log('searchData变化了', newValue, oldValue);
          model.value = newValue;
          getCardList();
        },
        { immediate: true, deep: true },
      );
      const pageSizeOptions = ref<string[]>(['10', '20', '30', '40', '50']);
      return {
        t,
        current,
        total,
        size,
        pageSizeOptions,
        registerModal,
        handleSuccess,
        change,
        getDictLabel,
        cardList,
        getCardList,
        channelId,
        selectItem,
        cardLoading,
        switchFlag,
        switchView,
        handleAdd,
        handleEdit,
        handleView,
        // handleStatus,
        handleDelete,
        Icon4,
        Icon5,
      };
    },
  });
</script>
<style lang="less" scoped>
  .tr {
    text-align: right;
  }

  .loading {
    width: 100%;
    height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

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

  .isSelect {
    .device-item {
      .device-info {
        max-width: 100%;
        display: flex;
        flex-direction: row;
        padding-top: 40px;

        .img {
          width: 180px;
          height: 180px;
        }

        .info {
          width: 68%;
          margin-left: 2%;
        }

        .device-name {
          // position: absolute;
          // top: 30px;
          // left: 0;
          // padding: 0 16px;
        }
      }

      .device-btns {
        position: absolute;
        right: 0;
        top: 4px;
        border: 0 none;

        .device-status {
          border: 0 none;
        }
      }
    }
  }

  .device-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    padding-bottom: 16px;
    cursor: pointer;
    position: relative;
    background-color: #fff;
    transition: all 0.5s;
    border: 2px solid transparent;

    .info {
      max-width: 100%;
    }

    &.active {
      box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      border: 2px solid #1a66ff;
      margin-top: -19px;
    }

    &.isSelected {
      // box-shadow: 0px 0px 8px 0px rgba(34, 78, 166, 0.25);
      // border: 2px solid #1a66ff;
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: row;
      align-items: center;
      max-width: calc(100% - 140px);

      img {
        width: 120px;
        height: 120px;
      }

      // }

      .device-name {
        font-size: 18px;
        font-weight: 700;
        margin-bottom: 16px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
      }

      .device-form {
        display: flex;
        align-items: center;
        width: 100%;
        line-height: 20px;

        & + .device-form {
          margin-top: 5px;
        }

        .label {
          width: 50px;
          text-align: left;
          font-size: 12px;
          font-weight: 500;
          color: #b6b6b6;
        }

        .value {
          width: calc(100% - 46px);
          font-size: 14px;
          font-weight: 500;
          color: #2a2a2a;
          padding-left: 8px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .device-btns {
      width: 148px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-left: 1px dashed #d4d4d4;

      .device-status {
        padding-top: 6px;
        padding-bottom: 14px;
        border-bottom: 1px dashed #d4d4d4;
        display: flex;
        align-items: center;

        .label {
          width: 50px;
          text-align: left;
          font-size: 12px;
          font-weight: 500;
          color: #b6b6b6;
        }

        .img {
          width: 18px;
          height: 18px;
          margin-right: 2px;
        }

        span {
          color: #808080;

          &.red {
            color: #fa3758;
          }

          &.green {
            color: #43cf7c;
          }
        }
      }

      .btn {
        width: 92px;
        height: 28px;
        background: #1a66ff;
        opacity: 1;
        text-align: center;
        font-size: 14px;
        line-height: 24px;
        color: #ffffff;
        border: 2px solid #1a66ff;
        border-radius: 6px;
        margin-top: 18px;

        &.plain {
          background-color: #fff;
          color: #1a66ff;
        }

        &.danger {
          background-color: #fff;
          color: #d43030;
          border: 2px solid #d43030;
        }
      }
    }
  }
</style>
