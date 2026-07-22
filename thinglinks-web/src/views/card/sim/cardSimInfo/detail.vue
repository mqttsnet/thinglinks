<template>
  <PageWrapper contentFullHeight>
    <div class="detail-info">
      <a-card title="" :bordered="false">
        <div class="device_title">
          <div>
            <span>{{ cardSimInfoDetail.cardNumber }}</span>
          </div>
        </div>
        <div class="base_data">
          <div class="item">
            <span>{{ t('card.sim.cardSimInfo.imsi') }}：</span>
            <span>{{ cardSimInfoDetail.imsi }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.sim.cardSimInfo.iccid') }}：</span>
            <span>{{ cardSimInfoDetail.iccid }}</span>
          </div>
          <div class="item">
            <span>{{ t('card.sim.cardSimInfo.cardType') }}：</span>
            <span>{{ getDictLabel('CARD_SIMINFO_CARD_TYPE', cardSimInfoDetail?.cardType, '插拔卡') }}</span>
          </div>
           <div class="item">
            <span>{{ t('card.sim.cardSimInfo.status') }}：</span>
            <span class="orange" v-if="cardSimInfoDetail?.status == 1">{{
              getDictLabel('CARD_SIMINFO_STATUS', cardSimInfoDetail?.status, '待激活')
            }}</span>
            <span class="green" v-else-if="cardSimInfoDetail?.status == 2">{{
              getDictLabel('CARD_SIMINFO_STATUS', cardSimInfoDetail?.status, '已激活')
            }}</span>
            <span class="red" v-else-if="cardSimInfoDetail?.status == 3">{{
              getDictLabel('CARD_SIMINFO_STATUS', cardSimInfoDetail?.status, '停机')
            }}</span>
          </div>
        </div>
      </a-card>
    </div>
    <div class="detail-info">
      <a-card :title="t('card.sim.cardSimInfo.basicInformation')" :bordered="false">
        <a-tabs default-active-key="1">
          <a-tab-pane key="1" :tab="t('card.sim.cardSimInfo.SIMCard')">
            <a-descriptions bordered>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.imsi')">
                {{ cardSimInfoDetail.imsi }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.iccid')">
                {{ cardSimInfoDetail.iccid }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.cardNumber')">
                {{ cardSimInfoDetail.cardNumber }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.cardType')">
                 {{ getDictLabel('CARD_SIMINFO_CARD_TYPE', cardSimInfoDetail?.cardType, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.channelId')">
                {{ cardSimInfoDetail.channelId }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.flowsUsed')">
                {{ cardSimInfoDetail.flowsUsed }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.flowsTotal')">
                {{ cardSimInfoDetail.flowsTotal }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.virtualFlowsUsed')">
                {{ cardSimInfoDetail.virtualFlowsUsed }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.virtualFlowsRest')">
                {{ cardSimInfoDetail.virtualFlowsRest }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.virtualFlowsTotal')">
                {{ cardSimInfoDetail.virtualFlowsTotal }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.smsFlag')">
                {{ getDictLabel('CARD_SIMINFO_SMSFLAG', cardSimInfoDetail?.smsFlag, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.gprsFlag')">
                {{ getDictLabel('CARD_SIMINFO_GPRSFLAG', cardSimInfoDetail?.gprsFlag, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.openTime')">
                {{ cardSimInfoDetail.openTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.lastOpenTime')">
                {{ cardSimInfoDetail.lastOpenTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.startTime')">
                {{ cardSimInfoDetail.startTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.endTime')">
                {{ cardSimInfoDetail.endTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.flowsEndTime')">
                {{ cardSimInfoDetail.flowsEndTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.carrierType')">
               {{ getDictLabel('CARD_SIMINFO_CARRIER_TYPE', cardSimInfoDetail?.carrierType, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.smsCount')">
                {{ cardSimInfoDetail.smsCount }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.status')">
                {{ getDictLabel('CARD_SIMINFO_STATUS', cardSimInfoDetail?.status, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.useType')">
                {{ getDictLabel('CARD_SIMINFO_USETYPE', cardSimInfoDetail?.useType, '') }}
              </a-descriptions-item>
               <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.apnName')">
                {{ cardSimInfoDetail.apnName }}
              </a-descriptions-item>
               <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.ipAddress')">
                {{ cardSimInfoDetail.ipAddress }}
              </a-descriptions-item>
               <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.gainTime')">
                {{ cardSimInfoDetail.gainTime }}
              </a-descriptions-item>
               <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.onlineFlag')">
                 {{ getDictLabel('CARD_SIMINFO_ONLINE_FLAG', cardSimInfoDetail?.onlineFlag, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.stopCardType')">
                {{ getDictLabel('CARD_SIMINFO_STOP_CARD_TYPE', cardSimInfoDetail?.stopCardType, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.monthlyWarning')">
                {{ cardSimInfoDetail?.monthlyWarning }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.imei')">
                {{ cardSimInfoDetail?.imei }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.limitFlow')">
                {{ cardSimInfoDetail?.limitFlow }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.limitFlag')">
                {{ getDictLabel('CARD_SIMINFO_LIMIT_FLAG', cardSimInfoDetail?.limitFlag, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.limitStatus')">
                {{ getDictLabel('CARD_SIMINFO_LIMIT_STATUS', cardSimInfoDetail?.limitStatus, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.offerId')">
                {{ cardSimInfoDetail?.offerId }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.searchableStatus')">
                 {{ getDictLabel('CARD_SIMINFO_SEARCHABLE_STATUS', cardSimInfoDetail?.searchableStatus, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.createdTime')">
                {{ cardSimInfoDetail.createdTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.createdBy')">
                {{ echoMapText(cardSimInfoDetail, 'createdBy') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.updatedTime')">
                {{ cardSimInfoDetail.updatedTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.updatedBy')">
                {{ echoMapText(cardSimInfoDetail, 'updatedBy') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.remark')">
                 {{ cardSimInfoDetail?.remark }}
              </a-descriptions-item>
              <!-- <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.sim.cardSimInfo.contentData')">
                <div class="contentData" ref="textToCopy">
                  {{ cardSimInfoDetail.contentData }}
                  <Tooltip placement="top" :title="t('common.title.copy')">
                  <span class="copy_btn" @click="handleCopyText"
                    ><SvgIcon name="copy" :size="12" /></span
                ></Tooltip>
                </div>
              </a-descriptions-item> -->
            </a-descriptions>
          </a-tab-pane>
          <a-tab-pane key="2" :tab="t('card.sim.cardSimInfo.cardChannel')">
            <a-descriptions bordered>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.channelName')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.channelName }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.keyParameter')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.keyParameter }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.officialFlag')">
                {{ getDictLabel('CARD_CHANNEL_OFFICIAL_FLAG', cardSimInfoDetail.cardChannelInfoResultVO?.officialFlag, '') }}
              </a-descriptions-item>
              <!-- <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.refreshFlag')">
                {{ getDictLabel('CARD_CHANNEL_OFFICIAL_FLAG', cardSimInfoDetail.cardChannelInfoResultVO?.refreshFlag, '') }}
              </a-descriptions-item> -->
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.operatorType')">
                {{ getDictLabel('CARD_CHANNEL_OPERATOR', cardSimInfoDetail.cardChannelInfoResultVO?.operatorType, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.provinceName')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.provinceName }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.provinceCode')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.provinceCode }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.appKey')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.appKey }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.secret')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.secret }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.code')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.code }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.appId')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.appId }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.password')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.password }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.status')">
                {{ getDictLabel('CARD_CHANNEL_STATUS', cardSimInfoDetail.cardChannelInfoResultVO?.status, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.channelType')">
                {{ getDictLabel('CARD_CHANNEL_TYPE', cardSimInfoDetail.cardChannelInfoResultVO?.channelType, '') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.extendParams')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.extendParams }}
              </a-descriptions-item>
               <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.createdTime')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.createdTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.createdBy')">
                {{ echoMapText(cardSimInfoDetail.cardChannelInfoResultVO, 'createdBy') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.updatedTime')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.updatedTime }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.updatedBy')">
                {{ echoMapText(cardSimInfoDetail.cardChannelInfoResultVO, 'updatedBy') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.createdOrgId')">
                {{ echoMapText(cardSimInfoDetail.cardChannelInfoResultVO, 'createdOrgId') }}
              </a-descriptions-item>
              <a-descriptions-item :labelStyle="labelStyle" :contentStyle="contentStyle"
                :label="t('card.channel.cardChannelInfo.remark')">
                {{ cardSimInfoDetail.cardChannelInfoResultVO?.remark }}
              </a-descriptions-item>
            </a-descriptions>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
import { defineComponent, ref, reactive, onMounted, h, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { getInfoDetail } from '/@/api/card/sim/cardSimInfo';
import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
import { PageWrapper } from '/@/components/Page';
import { Card, Row, Col, Descriptions, Tag, Tabs, Button, Tooltip } from 'ant-design-vue';
import { CopyOutlined, EyeOutlined, EyeInvisibleOutlined, EditOutlined } from '@ant-design/icons-vue';
import { useDict } from '/@/components/Dict';
import EditModal from './Edit.vue';
import { useModal } from '/@/components/Modal';
const { getDictLabel } = useDict();
import { echoMapText } from '/@/utils/echo';
import { ActionEnum } from '/@/enums/commonEnum';
import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
// import { dictAllComponentProps } from '../../../../../utils/thinglinks/common';
import type { CardSimInfoResultVO } from '/@/api/card/sim/model/cardSimInfoModel';
import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';
export default defineComponent({
  name: 'SIM卡详情',
  components: {
    ACard: Card,
    ARow: Row,
    ACol: Col,
    ATag: Tag,
    [Descriptions.name]: Descriptions,
    [Descriptions.Item.name]: Descriptions.Item,
    [Tabs.name]: Tabs,
    [Tabs.TabPane.name]: Tabs.TabPane,
    CopyOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
    PageWrapper,
    EditModal,
    AButton: Button,
    EditOutlined,
    SvgIcon,
  },
  emits: ['success', 'register'],
  setup() {
    console.log('设备详情')
    // 是否显示密码明文
    const isShow = ref(false);
    const textToCopy = ref(null);
    const { t } = useI18n();
    const { createMessage, createConfirm } = useMessage();
    const { currentRoute } = useRouter();
    let cardSimInfoDetail = reactive<CardChannelInfoResultVO>({})
    const [registerModal, { openModal }] = useModal();
    let id = ref('')
    onMounted(() => {
      const { params } = currentRoute.value;
      id.value = params.id as string;
      load();
    });
    const load = async () => {
      const res = await getInfoDetail(id.value)
      cardSimInfoDetail = Object.assign(cardSimInfoDetail, res)
      await nextTick();
      console.log(textToCopy.value);
    }
    let currentKey = ref('1')
    function copyFn(text) {
      let result = copyTextToClipboard(text)
      console.log(result, 'result')
      if (result) {
        createMessage.success(t('common.tips.copySuccess'));
      } else {
        createMessage.warning(t('common.tips.copyFail'));
      }
    }

    function changeShow() {
      isShow.value = !isShow.value
    }
    function handleCopy(record: Recordable, e: Event) {
      e?.stopPropagation();
      openModal(true, {
        record,
        type: ActionEnum.COPY,
      });
    }

    const handleCopyText = async () => {
      const text = (textToCopy.value as any).innerText;
      handleCopyTextV2(text || '');
    };

    // 弹出编辑页面
    function handleEdit(e: Event) {
      e?.stopPropagation();
      openModal(true, {
        record: cardSimInfoDetail,
        type: ActionEnum.EDIT,
      });
    }

    // 新增或编辑成功回调
    function handleSuccess() {
      cardSimInfoDetail.deviceIdentification = ''
      load()
    }

    return {
      t,
      copyFn,
      cardSimInfoDetail,
      currentKey,
      labelStyle: {
        'width': '140px',
        'font-weight': '600',
        'font-size': '14px',
      },
      contentStyle: {
        'font-weight': '600',
        'font-size': '14px',
      },
      isShow,
      textToCopy,
      changeShow,
      getDictLabel,
      echoMapText,
      registerModal,
      handleEdit,
      handleCopy,
      handleSuccess,
      handleCopyText
    };
  },
});
</script>
<style lang="less" scope>
.detail-info {
  &+.detail-info {
    margin-top: 16px;
  }

  .ant-descriptions-bordered .ant-descriptions-view {
    margin-bottom: 20px;

    .contentData {
      max-height: 400px;
      overflow-y: auto;
      position: relative;
    }
  }

  .ant-descriptions-item-label contentData {
    height: 200px;
  }

  .device_title {
    font-size: 16px;
    font-family: PingFang SC-Medium, PingFang SC;
    font-weight: 600;
    color: #2E3033;
    line-height: 19px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;

    .btn {
      display: flex;

      .copy {
        margin-right: 10px;
      }
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
          color: #FA3758;
        }

        &.orange {
          color: #daae2b;
        }

        &.green {
          color: #43CF7C;
        }
      }
    }
  }
}
</style>
