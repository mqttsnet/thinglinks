<template>
  <PageWrapper contentFullHeight>
    <div class="detail-info">
      <a-card title="" :bordered="false">
        <div class="device_title">
          <div>
            <span>{{ cardInfoDetail.channelName }}</span>
          </div>
        </div>
        <div class="base_data">
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.status') }}：</span>
            <span class="green" v-if="cardInfoDetail?.status == 0">{{
              getDictLabel('CARD_CHANNEL_STATUS', cardInfoDetail?.status, '')
            }}</span>

            <span class="red" v-else-if="cardInfoDetail?.status == 1">{{
              getDictLabel('CARD_CHANNEL_STATUS', cardInfoDetail?.status, '')
            }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.appId') }}:</span>
            <span>{{ cardInfoDetail.appId }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.operatorType') }}：</span>
            <span>{{
              getDictLabel('CARD_CHANNEL_OPERATOR', cardInfoDetail?.operatorType, '')
            }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.channelType') }}：</span>
            <span>{{ getDictLabel('CARD_CHANNEL_TYPE', cardInfoDetail?.channelType, '') }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.officialFlag') }}：</span>
            <span>{{
              getDictLabel('CARD_CHANNEL_OFFICIAL_FLAG', cardInfoDetail?.officialFlag, '')
            }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.channel.cardChannelInfo.refreshFlag') }}：</span>
            <span class="green" v-if="cardInfoDetail?.refreshFlag == 1">是 </span>
            <span class="red" v-else-if="cardInfoDetail?.status == 0">否</span>
          </div>
        </div>
      </a-card>
    </div>
    <div class="detail-info">
      <a-card title="" :bordered="false">
        <a-descriptions :title="t('card.channel.cardChannelInfo.basicInformation')" bordered>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.provinceName')"
          >
            {{ cardInfoDetail.provinceName }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.provinceCode')"
          >
            {{ cardInfoDetail.provinceCode }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.appKey')"
          >
            {{ cardInfoDetail.appKey }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.code')"
          >
            {{ cardInfoDetail.code }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.extendParams')"
          >
            {{ cardInfoDetail.extendParams }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.remark')"
          >
            {{ cardInfoDetail.remark }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.createdBy')"
          >
            {{ echoMapText(cardInfoDetail, 'createdBy') }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.createdTime')"
          >
            {{ cardInfoDetail.createdTime }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.updatedBy')"
          >
            {{ echoMapText(cardInfoDetail, 'updatedBy') }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.updatedTime')"
          >
            {{ cardInfoDetail.updatedTime }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('card.channel.cardChannelInfo.createdOrgId')"
          >
            {{ echoMapText(cardInfoDetail, 'createdOrgId') }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
    <div class="detail-info" v-hasAnyPermission="['card:channel:cardChannelInfo:detail:channelEmpowerment']">
      <a-card title="" :bordered="false">
        <a-tabs default-active-key="1" v-model:activeKey="currentKey">
          <a-tab-pane key="1" :tab="t('card.channel.cardChannelInfo.channelEmpowerment')">
             <div class="detail-info">
            <BasicTable @register="registerTable">
              <template #toolbar>
                <a-button
                  type="primary"
                  color="error"
                  preIcon="ant-design:delete-outlined"
                  @click="handleBatchDelete"
                >
                  {{ t('common.title.delete') }}
                </a-button>
                <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">
                  {{ t('common.title.add') }}
                </a-button>
              </template>
               <template #requestTypeCode="{ record }">
                  {{ getDictLabel('CARD_CHANNEL_CONFIG_REQUEST_TYPE_CODE', record?.requestTypeCode, '') }}
                </template>
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'action'">
                  <TableAction
                    :actions="[
                      {
                        tooltip: t('common.title.view'),
                        icon: 'ant-design:search-outlined',
                        onClick: handleView.bind(null, record),
                      },
                      {
                        tooltip: t('common.title.edit'),
                        icon: 'ant-design:edit-outlined',
                        onClick: handleEdit.bind(null, record),
                      },
                      {
                        tooltip: t('common.title.copy'),
                        icon: 'ant-design:copy-outlined',
                        onClick: handleCopy.bind(null, record),
                      },
                      {
                        tooltip: t('common.title.delete'),
                        icon: 'ant-design:delete-outlined',
                        color: 'error',
                        popConfirm: {
                          title: t('common.tips.confirmDelete'),
                          confirm: handleDelete.bind(null, record),
                        },
                      },
                    ]"
                    :stopButtonPropagation="true"
                  />
                </template>
              </template>
            </BasicTable>
      <EditModal @register="registerModal" @success="handleSuccess" />
    </div>
          </a-tab-pane>
        </a-tabs>
        <!-- <config
          v-if="currentKey == '1'"
        /> -->
      </a-card>
    </div>

  </PageWrapper>
</template>
<script lang="ts">
import { defineComponent, onMounted, ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { PageWrapper } from '/@/components/Page';
import { useModal } from '/@/components/Modal';
import { handleFetchParams } from '/@/utils/thinglinks/common';
import { ActionEnum } from '/@/enums/commonEnum';
import { Card, Row, Col, Descriptions, Tag, Tabs, Tooltip } from 'ant-design-vue';
import { getInfoDetail } from '/@/api/card/channel/cardChannelInfo';
import { page, remove } from '/@/api/card/channel/cardChannelInfoConfig';
import { columns, searchFormSchema } from '../cardChannelInfoConfig/cardChannelInfoConfig.data';
import EditModal from '../cardChannelInfoConfig/ConfigEdit.vue';
import type { CardChannelInfoResultVO } from '/@/api/card/channel/model/cardChannelInfoModel';
import type { DevicePageQuery } from '/@/api/iot/link/device/model/deviceModel';
import { useDict } from '/@/components/Dict';
import { echoMapText } from '/@/utils/echo';
const { getDictLabel } = useDict();
export default defineComponent({
  // 若需要开启页面缓存，请将此参数跟菜单名保持一致
  name: '渠道详情',
  components: {
    ACard: Card,
    BasicTable,
    PageWrapper,
    TableAction,
    EditModal,
    ARow: Row,
    ACol: Col,
    ATag: Tag,
    [Descriptions.name]: Descriptions,
    [Descriptions.Item.name]: Descriptions.Item,
    [Tabs.name]: Tabs,
    [Tabs.TabPane.name]: Tabs.TabPane,
    Tooltip,
  },
  setup() {
    const { t } = useI18n();
    const { currentRoute } = useRouter();
    const { createMessage, createConfirm } = useMessage();
    const [registerModal, { openModal }] = useModal();
    const infoId = ref<string>('');
    let deviceDetail = reactive<DevicePageQuery>({});
    let cardInfoDetail = ref<CardChannelInfoResultVO>({});
    let currentKey = ref('1');
    // 表格
    const [registerTable, { reload, getSelectRowKeys }] = useTable({
      title: t('card.channel.cardChannelInfoConfig.table.title'),
      api: page,
      columns: columns(),
      formConfig: {
        name: 'CardChannelInfoConfigSearch',
        labelWidth: 120,
        schemas: searchFormSchema(),
        autoSubmitOnEnter: true,
        resetButtonOptions: {
          preIcon: 'ant-design:rest-outlined',
        },
        submitButtonOptions: {
          preIcon: 'ant-design:search-outlined',
        },
      },
      beforeFetch: handleFetchParams,
      useSearchForm: true,
      showTableSetting: true,
      bordered: true,
      rowKey: 'id',
      rowSelection: {
        type: 'checkbox',
        columnWidth: 40,
      },
      actionColumn: {
        width: 200,
        title: t('common.column.action'),
        dataIndex: 'action',
      },
    });

    // 弹出复制页面
    function handleCopy(record: Recordable, e: Event) {
      e?.stopPropagation();
      openModal(true, {
        record,
        type: ActionEnum.COPY,
      });
    }
    // 弹出新增页面
    function handleAdd(record: Recordable, e: Event) {
      e?.stopPropagation();
      openModal(true, {
        infoId: infoId.value,
        type: ActionEnum.ADD,
      });
    }

    // 弹出查看页面
    function handleView(record: Recordable, e: Event) {
      e?.stopPropagation();
      openModal(true, {
        record,
        type: ActionEnum.VIEW,
      });
    }

    // 弹出编辑页面
    function handleEdit(record: Recordable, e: Event) {
      e?.stopPropagation();
      openModal(true, {
        record,
        type: ActionEnum.EDIT,
      });
    }

    // 新增或编辑成功回调
    function handleSuccess() {
      reload();
    }

    async function batchDelete(ids: string[]) {
      await remove(ids);
      createMessage.success(t('common.tips.deleteSuccess'));
      handleSuccess();
    }

    // 点击单行删除
    function handleDelete(record: Recordable, e: Event) {
      e?.stopPropagation();
      if (record?.id) {
        batchDelete([record.id]);
      }
    }

    // 点击批量删除
    function handleBatchDelete() {
      const ids = getSelectRowKeys();
      if (!ids || ids.length <= 0) {
        createMessage.warning(t('common.tips.pleaseSelectTheData'));
        return;
      }
      createConfirm({
        iconType: 'warning',
        content: t('common.tips.confirmDelete'),
        onOk: async () => {
          try {
            await batchDelete(ids);
          } catch (e) { }
        },
      });
    }
    const load = async () => {
      const res = await getInfoDetail(infoId.value);
      // console.log('res', res);
      cardInfoDetail.value = res;
    };

    // 初始化
    onMounted(() => {
      const { params } = currentRoute.value;
      console.log(params);
      infoId.value = params.id as string;
      load();
    });

    return {
      t,
      currentKey,
      registerTable,
      registerModal,
      handleView,
      handleCopy,
      handleAdd,
      handleEdit,
      handleDelete,
      handleBatchDelete,
      handleSuccess,
      cardInfoDetail,
      deviceDetail,
      getDictLabel,
      echoMapText,
      labelStyle: {
        width: '140px',
        'font-weight': '600',
        'font-size': '14px',
      },
      contentStyle: {
        'font-weight': '600',
        'font-size': '14px',
      },
    };
  },
});
</script>
<style lang="less" scope>
.detail-info {
  &+.detail-info {
    margin-top: 16px;
  }

  .device_title {
    font-size: 16px;
    font-family: PingFang SC-Medium, PingFang SC;
    font-weight: 600;
    color: #2e3033;
    line-height: 19px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;

    .gateWay {
      font-weight: 400;
      color: #999;
      font-size: 12px;
      margin-left: 12px;
    }

    .copy_btn {
      margin-left: 4px;
    }
  }

  .base_data {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #a6a6a6;
    line-height: 17px;

    .item {
      padding-right: 12px;

      &+.item {
        padding-left: 12px;
        border-left: 1px solid #e0e0e0;
      }

      span {
        &.red {
          color: #fa3758;
        }

        &.green {
          color: #43cf7c;
        }
      }
    }
  }
}
</style>
../../../../api/iot/link/device/model/deviceModel
