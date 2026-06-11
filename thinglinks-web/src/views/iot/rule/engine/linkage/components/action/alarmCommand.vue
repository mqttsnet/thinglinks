<template>
  <div class="action-item-alarm">
    <div class="action-con">
      <div class="action-type">
        <div class="icon" @click="editModel"><PartitionOutlined /></div>
        <div class="content">
          <!-- <a-tag color="red">
            <ControlOutlined style="margin: 0 4px 0 0" />
            {{
              getName(
                actionItem.productIdentification,
                'productIdentification',
                actionItem.product,
                'productName',
              )
            }}
            <ApiOutlined />
            {{
              getName(
                actionItem.deviceIdentification,
                'deviceIdentification',
                actionItem.device,
                'deviceName',
              )
            }}
          </a-tag> -->
          {{ t('iot.link.engine.executionLog.action.triggerAlarmRule') }}
          <a-popover :title="t('iot.link.engine.alarmRecord.alarmRule')" trigger="click">
            <template #content>
              <p>
                {{ currentAlarmRule.alarmName }}
                <a-tag color="processing" @click="copyFn(currentAlarmRule.alarmIdentification)"
                  >{ t('common.title.copy') }}</a-tag
                >
              </p>
            </template>
            <a-tag color="orange">{{ currentAlarmRule.alarmName }}</a-tag>
          </a-popover>
          {{ t('iot.link.engine.executionLog.action.to') }}
          <a-popover
            :title="t('iot.link.engine.executionLog.action.contactPerson')"
            trigger="click"
          >
            <template #content>
              <p>
                {{ currentActionItem?.actionContent?.atPhone }}
                <a-tag
                  color="processing"
                  @click="copyFn(currentActionItem?.actionContent?.atPhone)"
                  >{{ t('common.title.copy') }}</a-tag
                >
              </p>
            </template>
            <a-tag color="green">{{
              t('iot.link.engine.executionLog.action.contactPerson') + `(${atPhoneArr?.length})`
            }}</a-tag>
          </a-popover>
          {{ t('iot.link.engine.executionLog.action.send') }}
          <a-popover :title="t('iot.link.engine.alarmRecord.alarmContent')" trigger="click">
            <template #content>
              <TinymceCustom
                :value="currentActionItem?.actionContent?.contentData"
                :options="{ readonly: true }"
                :showImageUpload="false"
              />
            </template>
            <a-tag color="cyan">{{ t('iot.link.engine.alarmRecord.alarmContent') }}</a-tag>
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
  import { BizConstant } from '/@/enums/biz/common';
  import { page } from '../../../../../../../api/iot/rule/alarm/alarm';
  import type { AlarmPageQuery } from '../../../../../../../api/iot/rule/alarm/model/alarmModel';
  import { TinymceCustom } from '/@/components/Tinymce/index';
  import { CloseSquareOutlined, PartitionOutlined } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictList } = useDict();

  export default defineComponent({
    name: '执行动作',
    components: {
      CloseSquareOutlined,
      PartitionOutlined,
      TinymceCustom,
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
        const { records } = await page({
          model: { alarmIdentification: props.actionItem?.actionContent?.alarmIdentification },
          size: 10,
          current: 1,
          extra: {},
        });
        Object.assign(currentAlarmRule, records[0]);
        Object.assign(currentActionItem, props.actionItem);
      });
      const atPhoneArr = computed(() => {
        return props.actionItem?.actionContent?.atPhone?.split(',');
      });

      const editModel = () => {
        emit('editModel', props.actionIndex, props.actionItem);
      };

      const getKeyLabel = (obj) => {
        return Object.keys(obj)[0];
      };
      const copyFn = (text) => {
        let result = copyTextToClipboard(text);
        console.log(result, 'result');
        console.log(text, 'text');
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      };
      const getCmdRequestName = (idx) => {
        const actionItem = props.actionItem;
        let obj = {
          params: actionItem.params,
        };
        if (actionItem?.product) {
          actionItem.product.services.forEach((item) => {
            if (item.serviceCode == actionItem.serviceCode) {
              // 从大json里获取服务
              obj.serviceName = item.serviceName; // 服务name
              obj.serviceCode = item.serviceCode; // 服务code
              item.commands.forEach((ite) => {
                if (ite.commandCode == actionItem.cmd) {
                  // 从大json里获取命令
                  obj.commandName = ite.commandName; // 命令name
                  obj.commandCode = ite.commandCode; // 命令code
                  if (idx >= 0) {
                    // 从大json里获取属性name
                    ite.requests.forEach((it) => {
                      if (it.parameterCode == obj.params[idx].key) {
                        obj.params[idx].keyName = it.parameterName; // 命令属性name
                      }
                    });
                  }
                }
              });
            }
          });
        }
        // console.log(obj)
        return obj;
      };
      const getName = (value, valueKey, obj, nameKey) => {
        if (value === BizConstant.ALL) {
          return t('iot.link.engine.linkage.allDevices');
        } else {
          if (obj) {
            // console.log(value,valueKey,obj,nameKey)
            if (obj[valueKey] == value) {
              return obj[nameKey];
            }
            return '';
          } else {
            return '';
          }
        }
      };

      return {
        t,
        getDictList,
        getKeyLabel,
        getName,
        editModel,
        copyFn,
        getCmdRequestName,
        currentAlarmRule,
        atPhoneArr,
        currentActionItem,
      };
    },
  });
</script>
<style lang="less" scope>
  .mr8 {
    margin-right: 8px;
  }

  .mt20 {
    margin-top: 12px;
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
