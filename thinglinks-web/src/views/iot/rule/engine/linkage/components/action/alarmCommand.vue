<template>
  <div class="action-item-alarm">
    <div class="action-con">
      <div class="action-type">
        <div class="icon" @click="editModel"><PartitionOutlined /></div>
        <div class="content">
          {{ t('iot.link.engine.executionLog.action.triggerAlarmRule') }}
          <a-popover :title="t('iot.link.engine.alarmRecord.alarmRule')" trigger="click">
            <template #content>
              <p>
                {{
                  currentAlarmRule.alarmName || currentActionItem?.actionContent?.alarmName || '-'
                }}
                <a-tag color="processing" @click="copyFn(currentAlarmRule.alarmIdentification)">{{
                  t('common.title.copy')
                }}</a-tag>
              </p>
            </template>
            <a-tag color="orange">{{
              currentAlarmRule.alarmName || currentActionItem?.actionContent?.alarmName || '-'
            }}</a-tag>
          </a-popover>
          {{ t('iot.link.engine.executionLog.action.to') }}
          <a-popover
            :title="t('iot.link.engine.executionLog.action.contactPerson')"
            trigger="click"
          >
            <template #content>
              <div class="alarm-summary-pop">
                <div v-for="item in recipientSummary" :key="`${item.type}:${item.value}`">
                  {{ item.type }}: {{ item.label || item.value }}
                </div>
              </div>
            </template>
            <a-tag color="green">{{
              t('iot.link.engine.executionLog.action.contactPerson') + `(${recipientCount})`
            }}</a-tag>
          </a-popover>
          {{ t('iot.link.engine.executionLog.action.send') }}
          <a-popover :title="t('iot.link.engine.linkage.channelTemplate')" trigger="click">
            <template #content>
              <div class="alarm-template-pop">
                <div
                  v-for="item in templateSummary"
                  :key="item.channelType"
                  class="template-pop-item"
                >
                  <div class="template-pop-title">{{ channelLabel(item.channelType) }}</div>
                  <pre>{{ item.contentTemplate || '-' }}</pre>
                </div>
              </div>
            </template>
            <a-tag color="cyan">{{
              t('iot.link.engine.linkage.channelTemplate') + `(${channelCount})`
            }}</a-tag>
          </a-popover>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, watchEffect, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { page } from '../../../../../../../api/iot/rule/alarm/alarm';
  import type { AlarmPageQuery } from '../../../../../../../api/iot/rule/alarm/model/alarmModel';
  import { PartitionOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: '执行动作',
    components: {
      PartitionOutlined,
    },
    props: {
      actionChildrenIndex: {
        type: Number,
        default: 0,
      },
      actionIndex: {
        type: Number,
        default: 0,
      },
      type: {
        type: Number,
        default: 1,
      },
      actionItem: {
        type: Object,
        default: () => {},
      },
    },
    emits: ['editModel'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const { t } = useI18n();
      const currentActionItem = reactive<any>({});
      const currentAlarmRule = reactive<AlarmPageQuery>({});
      watchEffect(async () => {
        Object.assign(currentActionItem, props.actionItem || {});
        const alarmIdentification = props.actionItem?.actionContent?.alarmIdentification;
        Object.assign(currentAlarmRule, {
          alarmIdentification,
          alarmName: props.actionItem?.actionContent?.alarmName,
        });
        if (!alarmIdentification) {
          return;
        }
        const result = await page({
          model: { alarmIdentification: props.actionItem?.actionContent?.alarmIdentification },
          size: 10,
          current: 1,
          extra: {},
        }).catch(() => null);
        const record = result?.records?.[0];
        if (record) {
          Object.assign(currentAlarmRule, record);
        }
      });
      const recipientSummary = computed(() => {
        const actionContent = props.actionItem?.actionContent || {};
        if (Array.isArray(actionContent.recipients)) {
          return actionContent.recipients;
        }
        return String(actionContent.atPhone || '')
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean)
          .map((phone) => ({ type: 'PHONE', value: phone, label: phone }));
      });
      const templateSummary = computed(() => {
        const actionContent = props.actionItem?.actionContent || {};
        if (
          Array.isArray(actionContent.channelTemplates) &&
          actionContent.channelTemplates.length
        ) {
          return actionContent.channelTemplates;
        }
        return [
          {
            channelType: 0,
            contentTemplate: actionContent.contentData,
          },
        ];
      });
      const recipientCount = computed(() => recipientSummary.value.length);
      const channelCount = computed(() => templateSummary.value.length);
      const channelLabel = (channelType: number | string) => {
        const labels: Record<number, string> = {
          0: t('iot.link.engine.linkage.dingTalk'),
          1: t('iot.link.engine.linkage.enterpriseWechat'),
          2: t('iot.link.engine.linkage.feishu'),
          3: t('iot.link.engine.linkage.siteMessage'),
        };
        return labels[Number(channelType)] || t('iot.link.engine.linkage.unknownChannel');
      };

      const editModel = () => {
        emit('editModel', props.actionIndex, props.actionItem);
      };

      const copyFn = (text) => {
        let result = copyTextToClipboard(text);
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      };
      return {
        t,
        editModel,
        copyFn,
        currentAlarmRule,
        recipientSummary,
        templateSummary,
        recipientCount,
        channelCount,
        channelLabel,
        currentActionItem,
      };
    },
  });
</script>
<style lang="less" scoped>
  .mr8 {
    margin-right: 8px;
  }

  .mt20 {
    margin-top: 12px;
  }

  .alarm-summary-pop {
    min-width: 220px;
    max-width: 360px;
  }

  .alarm-template-pop {
    width: 420px;
    max-height: 360px;
    overflow: auto;

    .template-pop-item + .template-pop-item {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed @border-color-base;
    }

    .template-pop-title {
      margin-bottom: 6px;
      font-weight: 600;
    }

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-word;
      font-family: inherit;
      color: @text-color-secondary;
    }
  }

  .action-list {
    .action-item-alarm {
      display: flex;
      margin-bottom: 0;
      align-items: center;

      .action-con {
        display: flex;
        margin-left: 16px;
        background-color: #f5f5f5;
        min-width: 200px;
        padding: 6px 18px;
        height: 40px;
        border-radius: 6px;
        // overflow: hidden;
        position: relative;
        padding-left: 52px;

        .action-type {
          display: flex;
          align-items: center;
        }

        .content {
          display: flex;
          align-items: center;

          .ant-tag {
            margin-left: 4px;
          }
        }

        .icon {
          background-color: #f0f0f0;
          font-size: 18px;
          position: absolute;
          left: 0;
          top: 0;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 6px 0 0 6px;
        }
      }

      .del-btn {
        position: absolute;
        top: -8px;
        right: 0;
      }
    }
  }
</style>
../../../../../../../api/iot/link/alarm/alarm../../../../../../../api/iot/link/alarm/model/alarmModel
