<template>
  <div class="linkage-condition">
    <div class="condition-con">
      <div class="condition-con-d2" v-if="!loading">
        <!-- <Row :gutter="16"> -->
        <div
          class="gutter-row condition-con-item"
          :span="12"
          v-for="(item, index) in actions"
          :key="index"
        >
          <a-collapse
            style="width: 100%"
            :activeKey="item.activeKey"
            v-if="item.actionType === 0 || type === 3"
            @change="activeKeyChange($event, index)"
          >
            <a-collapse-panel key="1" :header="t('iot.link.engine.executionLog.action.header1')">
              <div class="action-list">
                <action-command
                  v-for="(actionItem, actionIndex) in item.actionContent.serial"
                  :key="actionIndex"
                  :actionIndex="index"
                  :actionChildrenIndex="actionIndex"
                  :actionItem="actionItem"
                  :type="1"
                  @editModel="editModel"
                  @delActionsChildren="delActionsChildren"
                />
              </div>
              <a-button type="primary" style="width: 100%" @click="selectActionCard(index, 1)">
                <PlusOutlined /> {{ t('iot.link.engine.executionLog.action.addPerformAnAction') }}
              </a-button>
            </a-collapse-panel>
            <a-collapse-panel key="2" :header="t('iot.link.engine.executionLog.action.header2')">
              <div class="action-list">
                <action-command
                  v-for="(actionItem, actionIndex) in item.actionContent.parallel"
                  :key="actionIndex"
                  :actionIndex="index"
                  :actionChildrenIndex="actionIndex"
                  :actionItem="actionItem"
                  :type="2"
                  @editModel="editModel"
                />
              </div>
              <a-button type="primary" style="width: 100%" @click="selectActionCard(index, 2)">
                <PlusOutlined /> {{ t('iot.link.engine.executionLog.action.addPerformAnAction') }}
              </a-button>
            </a-collapse-panel>
          </a-collapse>
          <div class="trigger-alarm action-list" v-if="item.actionType === 1">
            <alarm-command
              :actionIndex="index"
              :actionItem="item"
              @edit-model="editAlarmHandle"
              v-if="Object.keys(item.actionContent).length > 0"
            />
            <a-button type="primary" style="width: 100%" @click="triggerAlarmHandle(index)" v-else>
              <PlusOutlined /> {{ t('iot.link.engine.executionLog.action.addAlarmAction') }}
            </a-button>
          </div>
          <a-popconfirm
            v-if="type != 3"
            :title="t('iot.link.engine.executionLog.action.deleteTips1')"
            :ok-text="t('common.okText')"
            :cancel-text="t('common.cancelText')"
            @confirm="delActions(index)"
            @cancel="cancel"
          >
            <a-button danger shape="circle" class="del-btn" size="small">
              <CloseCircleOutlined />
            </a-button>
          </a-popconfirm>
        </div>
        <!-- </Row> -->
      </div>

      <div class="linkage-rule" v-if="type != 3">
        <a-dropdown
          placement="bottomLeft"
          :trigger="['click']"
          :getPopupContainer="(triggerNode:HTMLElement) => triggerNode.parentNode"
        >
          <!-- <a-button type="primary" style="width: 100%">
            <PlusOutlined /> 添加执行动作
          </a-button> -->
          <a-button type="primary" class="type">
            <span v-if="true"
              ><PlusOutlined />
              {{
                t('common.chooseText') + t('iot.link.engine.executionLog.action.actionType')
              }}</span
            >
            <span v-else> <ControlOutlined /> {{ 'aaaaa' }} </span>
          </a-button>
          <template #overlay>
            <actionType @selectTypeCard="addActions" />
          </template>
        </a-dropdown>
      </div>
    </div>
    <triggerRule
      :triggerType="type ? type : 2"
      @register="registerModal"
      @success="handleSuccess"
      @saveTriggerAction="saveTriggerAction"
    />
    <triggerAlarm
      @register="triggerModal"
      @success="handleSuccess"
      @saveTriggerAlarm="handleSaveTriggerAlarm"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, toRefs, reactive, onMounted, getCurrentInstance } from 'vue';
  import actionCommand from './actionCommand.vue';
  import alarmCommand from './alarmCommand.vue';
  import actionType from './actionType.vue';
  import triggerRule from '../modal/triggerRule.vue';
  import triggerAlarm from '../modal/TriggerAlarm.vue';
  import { useModal } from '/@/components/Modal';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import { Row, Col } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';

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
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
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
      actionType,
      actionCommand,
      triggerRule,
      Row,
      Col,
      triggerAlarm,
      alarmCommand,
      SvgIcon,
    },
    props: {
      actions: {
        type: Array,
        default: () => [],
      },
      productInfos: {
        type: Array,
        default: () => [],
      },
      deviceInfos: {
        type: Array,
        default: () => [],
      },
      type: {
        type: Number,
        default: 0,
      },
      productIdentification: {
        type: String,
        default: '',
      },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { proxy } = getCurrentInstance();
      const { t } = useI18n();
      // const type = ref<ActionEnum>(ActionEnum.ADD);
      const [registerModal, { openModal, closeModal, getVisible }] = useModal();
      const [triggerModal, { openModal: openTriggerAlarmModal }] = useModal();

      const state = reactive<any>({
        activeKey: ['1'],
        actions: [],
        actionsAddType: 1, // 1串行  2 并行
        loading: false,
        actionItem: {},
        type: props.type,
      });
      onMounted(() => {
        load();

        if (state.type == 3) {
          addActionsType3();
        }
      });
      const load = async () => {
        state.actions = props.actions;
        // 有数据的展开collapse
        state.actions.forEach((item) => {
          let activeKey = [];
          if (item.actionContent?.serial?.length) {
            activeKey.push('1');
          }
          if (item.actionContent?.parallel?.length) {
            activeKey.push('2');
          }
          if (item.actionType !== 1) {
            item.activeKey = activeKey;
          }
        });
        state.actions?.forEach((item, index) => {
          item.actionContent?.serial?.forEach((ite, childrenIndex) => {
            let actionContentSerialItem =
              state.actions[index].actionContent.serial?.[childrenIndex];
            const productInfo = props.productInfos.filter((it) => {
              return ite.productIdentification == it.productIdentification;
            });
            const deviceInfo = props.deviceInfos.filter((it) => {
              return ite.deviceIdentification == it.deviceIdentification;
            });
            actionContentSerialItem.product = productInfo ? productInfo[0] : {};
            actionContentSerialItem.device = deviceInfo ? deviceInfo[0] : {};
            actionContentSerialItem.params = getParamsList(actionContentSerialItem.params);
          });
          item.actionContent?.parallel?.forEach((ite, childrenIndex) => {
            let actionContentParallelItem =
              state.actions[index].actionContent.parallel?.[childrenIndex];
            const productInfo = props.productInfos.filter((it) => {
              return ite.productIdentification == it.productIdentification;
            });
            const deviceInfo = props.deviceInfos.filter((it) => {
              return ite.deviceIdentification == it.deviceIdentification;
            });
            actionContentParallelItem.product = productInfo ? productInfo[0] : {};
            actionContentParallelItem.device = deviceInfo ? deviceInfo[0] : {};
            actionContentParallelItem.params = getParamsList(actionContentParallelItem.params);
          });
        });
      };

      const getParamsList = (obj) => {
        // if(obj && Object.keys(obj) && Object.keys(obj).length){
        //   return Object.keys(obj)[0]
        // }else{
        //   return ''
        // }
        let list = [];
        if (obj) {
          Object.keys(obj).map((item) => {
            list.push({
              key: item,
              value: obj[item],
              visible: false,
            });
          });
        }
        return list;
      };

      // 删除单个
      const delActionsChildren = (index, actionIndex, actionsAddType) => {
        if (actionsAddType == 1) {
          // 删除串行
          state.actions[index].actionContent.serial?.splice(actionIndex, 1);
        } else {
          // 删除并行
          state.actions[index].actionContent.parallel?.splice(actionIndex, 1);
        }
      };
      // 删除组
      const delActions = (index) => {
        state.actions.splice(index, 1);
      };

      // 选择产品及设备及服务命令命令属性
      const selectActionCard = (actionIndex, actionsAddType) => {
        openModal(true, {
          type: ActionEnum.ADD,
          actionIndex,
          actionsAddType,
          productIdentification: props.productIdentification || '',
        });
      };

      // 添加触发告警动作
      const triggerAlarmHandle = (index: number) => {
        openTriggerAlarmModal(true, { type: ActionEnum.ADD, actionIndex: index });
      };

      const handleSaveTriggerAlarm = (res: any) => {
        state.actions[res.actionIndex].actionContent = res;
      };

      const editAlarmHandle = (index: number, actionItem: any) => {
        openTriggerAlarmModal(true, { type: ActionEnum.EDIT, actionIndex: index, actionItem });
      };

      const editModel = (actionIndex, actionChildrenIndex, actionsAddType, actionItem) => {
        state.actionItem = actionItem;
        openModal(true, {
          type: ActionEnum.EDIT,
          actionIndex,
          actionChildrenIndex,
          actionsAddType,
          actionItem,
        });
      };

      const getKeyLabel = (obj) => {
        return Object.keys(obj)[0];
      };

      // 保存提交单个动作到页面
      const saveTriggerAction = async (res) => {
        const product = await getFullProductInfo(res.selectedProduct.productIdentification);
        if (res.actionsAddType == 1) {
          // 添加串行
          if (res.actionChildrenIndex != null) {
            state.actions[res.actionIndex].actionContent.serial[res.actionChildrenIndex] = {
              msgType: 'cloudReq',
              device: res.selectedDevice,
              product,
              cmd: res.commands.commandCode,
              params: res.params,
              serviceCode: res.service.serviceCode,
              deviceIdentification: res.selectedDevice.deviceIdentification,
              productIdentification: res.selectedProduct.productIdentification,
            };
          } else {
            state.actions[res.actionIndex].actionContent.serial?.push({
              msgType: 'cloudReq',
              device: res.selectedDevice,
              product,
              cmd: res.commands.commandCode,
              params: res.params,
              serviceCode: res.service.serviceCode,
              deviceIdentification: res.selectedDevice.deviceIdentification,
              productIdentification: res.selectedProduct.productIdentification,
            });
          }
        } else {
          // 添加并行
          if (res.actionChildrenIndex != null) {
            state.actions[res.actionIndex].actionContent.parallel[res.actionChildrenIndex] = {
              msgType: 'cloudReq',
              device: res.selectedDevice,
              product,
              cmd: res.commands.commandCode,
              params: res.params,
              serviceCode: res.service.serviceCode,
              deviceIdentification: res.selectedDevice.deviceIdentification,
              productIdentification: res.selectedProduct.productIdentification,
            };
          } else {
            state.actions[res.actionIndex].actionContent.parallel?.push({
              msgType: 'cloudReq',
              device: res.selectedDevice,
              product,
              cmd: res.commands.commandCode,
              params: res.params,
              serviceCode: res.service.serviceCode,
              deviceIdentification: res.selectedDevice.deviceIdentification,
              productIdentification: res.selectedProduct.productIdentification,
            });
          }
        }

        // if(state.actionsAddType == 1){
        //   addSeriesActions(0,res)
        // }else{
        //   addParallelActions(0,res)
        // }
        // emit('saveTriggerAction', { ...state })
      };

      const handleSuccess = () => {
        load();
      };

      // 新增组
      const addActions = (res) => {
        switch (res.id) {
          case 0:
            state.actions.push({
              activeKey: ['1'],
              actionContent: {
                serial: [],
                parallel: [],
              },
              actionType: res.id,
            });
            break;
          case 1:
            state.actions.push({
              actionType: res.id,
              actionContent: {},
            });
        }
      };
      // 设备调试新增组
      const addActionsType3 = () => {
        state.actions = [
          {
            activeKey: ['1'],
            actionContent: {
              serial: [],
              parallel: [],
            },
          },
        ];
      };
      const activeKeyChange = ($event, index) => {
        state.actions[index].activeKey = $event;
      };
      const getName = (value, valueKey, obj, nameKey) => {
        if (obj) {
          // console.log(value,valueKey,obj,nameKey)
          if (obj[valueKey] == value) {
            return obj[nameKey];
          }
          return '';
        } else {
          return '';
        }
      };

      const getCmdRequestName = (actionItem) => {
        // console.log(actionItem)
        let obj = {};
        if (actionItem?.product) {
          actionItem.product.services.forEach((item) => {
            if (item.serviceCode == actionItem.serviceCode) {
              obj.serviceName = item.serviceName; // 服务name
              item.commands.forEach((ite) => {
                if (ite.commandCode == actionItem.cmd) {
                  obj.commandName = ite.commandName; // 命令name
                  ite.requests.forEach((it) => {
                    if (it.parameterCode == getKeyLabel(actionItem.params)) {
                      obj.parameterName = it.parameterName; // 命令属性name
                    }
                  });
                }
              });
            }
          });
        }
        // console.log(obj)
        return obj;
      };

      return {
        t,
        registerModal,
        handleSuccess,
        ...toRefs(state),
        getDictList,
        delActionsChildren,
        selectActionCard,
        saveTriggerAction,
        addActions,
        addActionsType3,
        delActions,
        getKeyLabel,
        activeKeyChange,
        getName,
        editModel,
        getVisible,
        getCmdRequestName,
        triggerAlarmHandle,
        triggerModal,
        handleSaveTriggerAlarm,
        editAlarmHandle,
      };
    },
  });
</script>
<style lang="less" scope>
  .linkage-condition {
    display: flex;

    .condition-con {
      width: 100%;

      .condition-con-d2 {
        position: relative;
      }

      .linkage-rule {
        position: relative;
        z-index: 10;
      }

      .condition-con-item {
        position: relative;
        margin-bottom: 16px;

        .del-btn {
          position: absolute;
          top: -10px;
          right: -10px;
        }
      }
    }
  }

  .fdNumber {
    width: 50px;
  }

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
        height: auto;
        border-radius: 6px;
        // overflow: hidden;
        position: relative;
        padding-left: 52px;

        .action-type {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
        }

        .content {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          line-height: 30px;

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
          height: 100%;
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

  .trigger-alarm {
    border: 1px solid #d9d9d9;
    padding: 16px;
  }
</style>
../../../../../../../api/iot/link/product/product
