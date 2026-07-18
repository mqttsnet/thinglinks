<template>
  <div class="action-item">
    <div class="action-index">
      <a-button type="primary" shape="circle">
        {{ actionIndex + 1 }}
      </a-button>
    </div>
    <div class="action-con">
      <div class="action-type">
        <div class="icon" @click="editModel(index, actionIndex, 2, actionItem)"
          ><PartitionOutlined
        /></div>
        <div class="content">
          <a-tag color="red">
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
          </a-tag>
          {{ t('iot.link.engine.executionLog.action.equipmentDistribution') }}
          <a-popover :title="t('iot.link.engine.executionLog.action.service')" trigger="click">
            <template #content>
              <p>
                {{ getCmdRequestName().serviceCode }}
                <a-tag color="processing" @click="copyFn(getCmdRequestName().serviceCode)">{{
                  t('common.title.copy')
                }}</a-tag>
              </p>
            </template>
            <a-tag color="orange">{{ getCmdRequestName().serviceName }}</a-tag>
          </a-popover>
          {{ t('iot.link.engine.executionLog.action.underTheService') }}
          <a-popover :title="t('iot.link.engine.executionLog.action.command')" trigger="click">
            <template #content>
              <p>
                {{ getCmdRequestName().commandCode }}
                <a-tag color="processing" @click="copyFn(getCmdRequestName().commandCode)">{{
                  t('common.title.copy')
                }}</a-tag>
              </p>
            </template>
            <a-tag color="green">{{ getCmdRequestName().commandName }}</a-tag>
          </a-popover>
          命令

          <a-popover :title="t('iot.link.engine.executionLog.action.action')" trigger="click">
            <template #content>
              <p v-for="(ite, idx) in actionItem.params" :key="idx">
                <a-popover
                  :title="t('iot.link.engine.executionLog.action.commandProperties')"
                  trigger="click"
                >
                  <template #content>
                    <p>
                      {{ ite.key }}
                      <a-tag color="processing" @click="copyFn(ite.key)">{{
                        t('common.title.copy')
                      }}</a-tag>
                    </p>
                  </template>
                  <a-tag color="cyan">{{ getCmdRequestName(idx).params[idx].keyName }} </a-tag>
                </a-popover>

                {{ t('iot.link.engine.executionLog.action.setting') }}
                <a-tag color="purple">{{ ite.value }}</a-tag>

                <!-- <a-tag color="processing" @click="copyFn(getCmdRequestName(actionItem).commandCode)">复制</a-tag> -->
              </p>
            </template>
            <a-tag color="cyan"
              >{{ t('common.title.view')
              }}{{ t('iot.link.engine.executionLog.action.command') }}（{{
                actionItem.params.length
              }}个）</a-tag
            >
          </a-popover>
          <!-- <a-tag color="cyan">{{ getCmdRequestName(actionItem).parameterName }} </a-tag>
          设置为
          <a-tag color="purple">{{ actionItem.params[getKeyLabel(actionItem.params)] }}</a-tag>
          的动作 -->
        </div>
      </div>
      <a-popconfirm
        :title="t('iot.link.engine.executionLog.action.deleteTips')"
        :ok-text="t('common.okText')"
        :cancel-text="t('common.cancelText')"
        @confirm="delActionsChildren"
        @cancel="cancel"
      >
        <a-button danger shape="circle" class="del-btn" size="small">
          <CloseSquareOutlined />
        </a-button>
      </a-popconfirm>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs, reactive, watch, getCurrentInstance } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicForm } from '/@/components/Form/index';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useModal } from '/@/components/Modal';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import {
    Steps,
    Form,
    Row,
    Col,
    Dropdown,
    Menu,
    Radio,
    Checkbox,
    TimePicker,
    Card,
    Select,
  } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { BizConstant } from '/@/enums/biz/common';
  import { operate, connect } from '/@/api/iot/link/operator/operator';

  import {
    ApartmentOutlined,
    ControlOutlined,
    ApiOutlined,
    PlusOutlined,
    CloseCircleOutlined,
    CloseSquareOutlined,
    DeleteOutlined,
    PartitionOutlined,
    AlertOutlined,
    PullRequestOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictList } = useDict();

  export default defineComponent({
    name: '执行动作',
    components: {
      ApartmentOutlined,
      ControlOutlined,
      ApiOutlined,
      PlusOutlined,
      CloseCircleOutlined,
      CloseSquareOutlined,
      DeleteOutlined,
      PartitionOutlined,
      AlertOutlined,
      PullRequestOutlined,
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
        type: Number,
        default: {},
      },
    },
    emits: ['editModel', 'delActionsChildren'],
    setup(props, { emit }) {
      const { proxy } = getCurrentInstance();
      const { createMessage } = useMessage();
      const { t } = useI18n();

      const state = reactive({
        actionIndex: props.actionChildrenIndex,
      });

      // 删除单个
      const delActionsChildren = (index, actionIndex, actionsAddType) => {
        emit('delActionsChildren', props.actionIndex, props.actionChildrenIndex, props.type);
      };

      const editModel = (actionIndex, actionChildrenIndex, actionsAddType, actionItem) => {
        emit(
          'editModel',
          props.actionIndex,
          props.actionChildrenIndex,
          props.type,
          props.actionItem,
        );
        // state.actionItem = actionItem;
        // openModal(true, {
        //   type: ActionEnum.EDIT,
        //   actionIndex,
        //   actionChildrenIndex,
        //   actionsAddType,
        //   actionItem,
        // });
      };

      const getKeyLabel = (obj) => {
        return Object.keys(obj)[0];
      };
      const copyFn = (text) => {
        let result = copyTextToClipboard(text);
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warn(t('common.tips.copyFail'));
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
        ...toRefs(state),
        getDictList,
        delActionsChildren,
        getKeyLabel,
        getName,
        editModel,
        copyFn,
        getCmdRequestName,
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
    .action-item {
      display: flex;
      margin-bottom: 12px;
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
../../../../../../../api/iot/link/product/product../../../../../../../api/iot/link/device/device../../../../../../../api/iot/link/operator/operator
