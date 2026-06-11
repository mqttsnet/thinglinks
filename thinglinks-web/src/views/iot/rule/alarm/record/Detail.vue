<template>
  <PageWrapper contentFullHeight>
    <div class="detail-info">
      <a-card title="" :bordered="false">
        <div class="device_title">
          <div>
            <span>{{ alarmRecordDetail.id }}</span>
          </div>
          <a-button
            type="primary"
            :disabled="alarmRecordDetail.handledStatus == 2"
            danger
            @click="handleEdit"
          >
            <template #icon><EditOutlined /></template>
            <!-- {{getDictLabel('RULE_ALARM_RECORD_HANDLED_STATUE', alarmRecordDetail?.handledStatus, '处理  ') }} -->
            {{
              t(
                alarmRecordDetail.handledStatus == 0
                  ? 'common.title.handle'
                  : alarmRecordDetail.handledStatus == 1
                  ? 'common.title.goResolved'
                  : 'common.title.resolved',
              )
            }}
          </a-button>
        </div>
        <div class="base_data">
          <div class="item">
            <span>{{ t('iot.link.engine.alarmRecord.appId') }}:</span>
            <span>{{ alarmRecordDetail.appId }}</span>
          </div>
          <div class="item">
            <span>{{ t('iot.link.engine.alarmRecord.alarmIdentification') }}：</span>
            <span>{{ alarmRecordDetail.alarmIdentification }}</span>
          </div>
          <div class="item">
            <span>{{ t('iot.link.engine.alarmRecord.handledStatus') }}：</span>
            <span class="red" v-if="alarmRecordDetail?.handledStatus == 0">{{
              getDictLabel(
                'RULE_ALARM_RECORD_HANDLED_STATUE',
                alarmRecordDetail?.handledStatus,
                '待处理',
              )
            }}</span>
            <span class="orange" v-else-if="alarmRecordDetail?.handledStatus == 1">{{
              getDictLabel(
                'RULE_ALARM_RECORD_HANDLED_STATUE',
                alarmRecordDetail?.handledStatus,
                '处理中',
              )
            }}</span>
            <span class="green" v-else-if="alarmRecordDetail?.handledStatus == 2">{{
              getDictLabel(
                'RULE_ALARM_RECORD_HANDLED_STATUE',
                alarmRecordDetail?.handledStatus,
                '已解决',
              )
            }}</span>
          </div>
        </div>
      </a-card>
    </div>
    <div class="detail-info">
      <a-card :title="t('iot.link.engine.alarmRecord.basicInfo')" :bordered="false">
        <a-tabs default-active-key="1">
          <a-tab-pane key="1" :tab="t('iot.link.engine.alarmRecord.alarmRecord')">
            <a-descriptions bordered>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.occurredTime')"
              >
                {{ alarmRecordDetail.occurredTime }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.handledTime')"
              >
                {{ alarmRecordDetail.handledTime }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.handlingNotes')"
              >
                {{ alarmRecordDetail.handlingNotes }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.resolvedTime')"
              >
                {{ alarmRecordDetail.resolvedTime }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.resolutionNotes')"
              >
                {{ alarmRecordDetail.resolutionNotes }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.remark')"
              >
                {{ alarmRecordDetail.remark }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.contentData')"
              >
                <div class="contentData" ref="textToCopy">
                  {{ alarmRecordDetail.contentData }}
                  <Tooltip placement="top" :title="t('common.title.copy')">
                    <span class="copy_btn" @click="handleCopyText"
                      ><SvgIcon name="copy" :size="12" /></span
                  ></Tooltip>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </a-tab-pane>
          <a-tab-pane key="2" :tab="t('iot.link.engine.alarmRecord.alarmRule')">
            <a-descriptions bordered>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.appId')"
              >
                {{ alarmRecordDetail?.ruleAlarmDetailsResultVO.appId }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.alarmName')"
              >
                {{ alarmRecordDetail.ruleAlarmDetailsResultVO?.alarmName }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.alarmScene')"
              >
                {{
                  getDictLabel(
                    'RULE_ALARM_SCENE',
                    alarmRecordDetail.ruleAlarmDetailsResultVO?.alarmScene,
                    '场景联动',
                  )
                }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.alarmIdentification')"
              >
                {{ alarmRecordDetail.ruleAlarmDetailsResultVO?.alarmIdentification }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.alarmChannelIds')"
              >
                {{ alarmRecordDetail.ruleAlarmDetailsResultVO?.alarmChannelIds }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.alarmLevel')"
              >
                {{
                  getDictLabel(
                    'RULE_ALARM_LEVEL',
                    alarmRecordDetail.ruleAlarmDetailsResultVO?.level,
                    '低级',
                  )
                }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.status')"
              >
                {{
                  getDictLabel(
                    'RULE_ALARM_STATUS',
                    alarmRecordDetail.ruleAlarmDetailsResultVO?.status,
                    '未启用',
                  )
                }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.alarmRecord.remark')"
              >
                {{ alarmRecordDetail.ruleAlarmDetailsResultVO?.remark }}
              </a-descriptions-item>
            </a-descriptions>
          </a-tab-pane>
          <a-tab-pane key="3" :tab="t('iot.link.engine.alarmRecord.alarmChannel')">
            <a-descriptions
              bordered
              v-for="item in alarmRecordDetail.ruleAlarmDetailsResultVO
                .ruleAlarmChannelDetailsResultVOList"
            >
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.channelName')"
              >
                <div style="min-width: 200px">
                  {{ item?.channelName }}
                </div>
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.channelType')"
              >
                <div style="min-width: 200px">{{
                  getDictLabel('RULE_ALARM_CHANNEL_TYPE', item?.channelType, '钉钉')
                }}</div>
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.status')"
              >
                <div style="min-width: 200px">{{
                  getDictLabel('RULE_ALARM_STATUS', item?.status, '未启用')
                }}</div>
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.createdTime')"
              >
                {{ item?.createdTime }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.createdBy')"
              >
                <div style="min-width: 200px">
                  {{ echoMapText(item, 'createdBy') }}
                </div>
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.updatedTime')"
              >
                {{ item?.updatedTime }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.updatedBy')"
              >
                {{ echoMapText(item, 'updatedBy') }}
              </a-descriptions-item>
              <a-descriptions-item
                :labelStyle="labelStyle"
                :contentStyle="contentStyle"
                :label="t('iot.link.engine.channel.remark')"
              >
                {{ item?.remark }}
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
  import { detail } from '../../../../../api/iot/rule/alarm/record';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col, Descriptions, Tag, Tabs, Button, Tooltip } from 'ant-design-vue';
  import {
    CopyOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
    EditOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import EditModal from './Edit.vue';
  import { useModal } from '/@/components/Modal';
  const { getDictLabel } = useDict();
  import { ActionEnum } from '/@/enums/commonEnum';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import type { AlarmRecordResultVO } from '../../../../../api/iot/rule/alarm/model/recordModel';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';
  import { echoMapText } from '/@/utils/echo';
  export default defineComponent({
    name: '告警记录详情',
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
      console.log('设备详情');
      // 是否显示密码明文
      const isShow = ref(false);
      const textToCopy = ref(null);
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const { currentRoute } = useRouter();
      let alarmRecordDetail = reactive<AlarmRecordResultVO>({});
      const [registerModal, { openModal }] = useModal();
      let id = ref('');
      onMounted(() => {
        const { params } = currentRoute.value;
        id.value = params.id as string;
        load();
      });
      const load = async () => {
        const res = await detail(id.value);
        alarmRecordDetail = Object.assign(alarmRecordDetail, res);
        await nextTick();
        console.log(textToCopy.value);
      };
      let currentKey = ref('1');
      function copyFn(text) {
        let result = copyTextToClipboard(text);
        console.log(result, 'result');
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      }

      function changeShow() {
        isShow.value = !isShow.value;
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
          record: alarmRecordDetail,
          type: ActionEnum.EDIT,
        });
      }

      // 新增或编辑成功回调
      function handleSuccess() {
        alarmRecordDetail.deviceIdentification = '';
        load();
      }

      return {
        t,
        echoMapText,
        copyFn,
        alarmRecordDetail,
        currentKey,
        labelStyle: {
          'min-width': '140px',
          'font-weight': '600',
          'font-size': '14px',
        },
        contentStyle: {
          'max-width': '200px',
          'font-weight': '600',
          'font-size': '14px',
        },
        isShow,
        textToCopy,
        changeShow,
        getDictLabel,
        registerModal,
        handleEdit,
        handleCopy,
        handleSuccess,
        handleCopyText,
      };
    },
  });
</script>
<style lang="less" scope>
  .detail-info {
    & + .detail-info {
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
      color: #2e3033;
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

        & + .item {
          padding-left: 12px;
          border-left: 1px solid #e0e0e0;
        }

        span {
          &.red {
            color: #fa3758;
          }

          &.orange {
            color: #daae2b;
          }

          &.green {
            color: #43cf7c;
          }
        }
      }
    }
  }</style
>../../../../../api/iot/link/alarmRecord/record../../../../../api/iot/link/alarmRecord/model/recordModel
