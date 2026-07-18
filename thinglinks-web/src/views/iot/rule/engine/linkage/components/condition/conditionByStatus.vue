<template>
  <div class="linkage-condition">
    <div class="condition-con" v-if="!loading">
      <div class="condition-con-d">
        <div style="width: 100%">
          <div class="conditions-terms-con">
            <div class="conditions-terms-con-ite" v-for="(item, index) in conditions" :key="index">
              <a-dropdown class="mr8" v-if="index > 0" placement="bottomLeft">
                <a-button type="primary" shape="round">
                  {{ getLabelFilter(item.logicalOperator, 'name', 'desc', connectList) }}
                </a-button>
                <template #overlay>
                  <a-menu @click="selectConnect($event, index)">
                    <a-menu-item
                      v-for="connectItem in connectList"
                      :key="connectItem.name"
                      :value="connectItem.value"
                      :title="connectItem.desc"
                    >
                      {{ connectItem.desc }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
              <div class="conditions-terms-item mr8">
                <div
                  class="conditions-terms-list-item"
                  v-for="(childrenItem, childrenIndex) in item.conditions"
                  :key="childrenIndex"
                >
                  <a-dropdown class="mr8" v-if="childrenIndex > 0" placement="bottomLeft">
                    <a-button type="primary" size="small">
                      {{
                        getLabelFilter(childrenItem.logicalOperator, 'name', 'desc', connectList)
                      }}
                    </a-button>
                    <template #overlay>
                      <a-menu @click="selectConnectChildren($event, index, childrenIndex)">
                        <a-menu-item
                          v-for="connectChildrenItem in connectList"
                          :key="connectChildrenItem.name"
                          :value="connectChildrenItem.value"
                          :title="connectChildrenItem.desc"
                        >
                          {{ connectChildrenItem.desc }}
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                  <a-button
                    class="mr8 type"
                    type="primary"
                    @click="handleSelectRule(index, childrenIndex)"
                  >
                    <span v-if="!childrenItem.leftParam.deviceIdentification">{{
                      t('iot.link.engine.executionLog.triggerTips')
                    }}</span>
                    <span v-else>
                      <ControlOutlined />
                      {{
                        getName(
                          childrenItem.leftParam.productIdentification,
                          'productIdentification',
                          childrenItem.product,
                          'productName',
                        )
                      }}
                      <ApiOutlined />
                      {{
                        getName(
                          childrenItem.leftParam.deviceIdentification,
                          'deviceIdentification',
                          childrenItem.device,
                          'deviceName',
                        )
                      }}
                    </span>
                  </a-button>
                  <template v-if="childrenItem.leftParam.deviceIdentification">
                    <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
                      <a-button>
                        {{ childrenItem.operator.desc ? childrenItem.operator.desc : '操作符' }}
                      </a-button>
                      <template #overlay>
                        <a-menu @click="selectOperate($event, index, childrenIndex)">
                          <a-menu-item
                            v-for="operateItem in getOperateList(childrenItem.leftParam.dataType)"
                            :key="operateItem.name"
                            :value="operateItem.value"
                            :title="operateItem.desc"
                          >
                            {{ operateItem.desc }}
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                    <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
                      <a-button>
                        <a-tooltip>
                          <template #title>{{
                            findRemarkFromStatusList(childrenItem.rightParams[0]?.value)
                          }}</template>
                          <ExclamationCircleOutlined
                            v-if="childrenItem.rightParams[0].desc"
                          /> </a-tooltip
                        >{{ childrenItem.rightParams[0].desc || '设备状态' }}
                      </a-button>
                      <template #overlay>
                        <a-menu @click="selectDeviceStatus($event, index, childrenIndex)">
                          <a-menu-item
                            v-for="statusItem in statusList"
                            :key="statusItem.value"
                            :value="statusItem.value"
                            :title="statusItem.text"
                            :desc="statusItem.remark"
                          >
                            <a-tooltip>
                              <template #title>{{ statusItem.remark }}</template>
                              <ExclamationCircleOutlined />
                            </a-tooltip>
                            {{ statusItem.text }}
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </template>
                  <a-popconfirm
                    :title="t('iot.link.engine.executionLog.deleteTips1')"
                    :ok-text="t('common.okText')"
                    :cancel-text="t('common.cancelText')"
                    @confirm="delConditionsChildren(index, childrenIndex)"
                    @cancel="cancel"
                  >
                    <a-button
                      danger
                      shape="circle"
                      v-if="childrenIndex > 0"
                      class="del-btn"
                      size="small"
                    >
                      <CloseSquareOutlined />
                    </a-button>
                  </a-popconfirm>
                </div>
                <a-button
                  type="dashed"
                  size="small"
                  shape="circle"
                  @click="addConditionsChildren(index)"
                >
                  <PlusOutlined />
                </a-button>
                <a-popconfirm
                  :title="t('iot.link.engine.executionLog.deleteTips2')"
                  :ok-text="t('common.okText')"
                  :cancel-text="t('common.cancelText')"
                  @confirm="delConditions(index)"
                  @cancel="cancel"
                >
                  <a-button danger shape="circle" v-if="index > 0" class="del-btn" size="small">
                    <CloseCircleOutlined />
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
            <a-button type="primary" @click="addConditionsGroup">
              <PlusOutlined /> {{ t('iot.link.engine.executionLog.grouping') }}
            </a-button>
          </div>
        </div>
      </div>
    </div>
    <triggerRule
      :triggerType="1"
      @register="registerModal"
      @success="handleSuccess"
      @saveTriggerRule="saveTriggerRule"
    />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, onMounted, watch, toRefs } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import { detailBydeviceIdentification } from '/@/api/iot/link/device/device';
  import { operate, connect } from '/@/api/iot/link/operator/operator';
  import triggerRule from '../modal/triggerRule.vue';
  import { getLabelFilter } from '/@/utils/thinglinks/common';
  import { DictEnum } from '/@/enums/commonEnum';

  import {
    ControlOutlined,
    ApiOutlined,
    PlusOutlined,
    CloseCircleOutlined,
    CloseSquareOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';

  import { useDict } from '/@/components/Dict';

  export default defineComponent({
    name: 'LinkageCondition',
    components: {
      triggerRule,
      ControlOutlined,
      ApiOutlined,
      PlusOutlined,
      CloseCircleOutlined,
      CloseSquareOutlined,
      ExclamationCircleOutlined,
    },
    props: {
      conditionScheme: {
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
      conditionType: {
        type: Number,
        default: 0,
      },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const { getDicListUpgrade } = useDict();
      const [registerModal, { openModal }] = useModal();

      const state = reactive({
        loading: false,
        conditions: [
          {
            type: 'GROUP',
            logicalOperator: 'AND',
            conditions: [
              {
                type: 'CONDITION',
                leftParam: {
                  dataType: 'string',
                  deviceIdentification: '',
                  id: '',
                  productIdentification: '',
                },
                operator: {
                  desc: '',
                  value: '',
                },
                rightParams: [
                  {
                    type: 'CONSTANT',
                    value: '',
                    desc: '',
                  },
                ],
                logicalOperator: 'AND',
                visiable: false,
                device: {},
                product: {
                  services: [],
                },
              },
            ],
          },
        ],
        operateList: [],
        connectList: [],
        statusList: [],
      });

      const { loading, conditions, connectList, statusList } = toRefs(state);

      onMounted(() => {
        load();
        // initData();
      });

      const initData = () => {
        if (props.conditionType === 2) {
          state.conditions = props.conditionScheme;
          state.conditions.forEach((item, index) => {
            item.conditions.forEach((ite, childrenIndex) => {
              const productInfo = props.productInfos.filter(
                (it) => ite.leftParam.productIdentification === it.productIdentification,
              );
              state.conditions[index].conditions[childrenIndex].product = productInfo
                ? productInfo[0]
                : {};
              const deviceInfo = props.deviceInfos.filter(
                (it) => ite.leftParam.deviceIdentification === it.deviceIdentification,
              );
              state.conditions[index].conditions[childrenIndex].device = deviceInfo
                ? deviceInfo[0]
                : {};
            });
          });
        }
        state.loading = false;
      };

      watch(
        () => props.conditionType,
        (newVal: number) => {
          initData();
        },
        { immediate: true },
      );

      const load = async () => {
        const resOperate = await operate({});
        state.operateList = resOperate;
        const resConnect = await connect({});
        state.connectList = resConnect;
        const resStatusList = await getDicListUpgrade(
          DictEnum.RULE_CONDITION_DEVICE_ACTION_TIRGGER_TYPE,
        );
        state.statusList = resStatusList;
      };

      const getOperateList = (datatype) => {
        if (datatype) {
          return state.operateList.filter((item) => item.supportedDataTypes.includes(datatype));
        }
        return [];
      };

      const selectOperate = ($event, index, childrenIndex) => {
        state.conditions[index].conditions[childrenIndex].operator = {
          desc: $event.item.title,
          value: $event.key,
        };
        state.conditions[index].conditions[childrenIndex].rightParams = [
          {
            type: 'CONSTANT',
            value: '',
            desc: '',
          },
        ];
      };

      const selectDeviceStatus = ($event, index, childrenIndex) => {
        state.conditions[index].conditions[childrenIndex].rightParams = [
          {
            type: 'CONSTANT',
            value: $event.key,
            desc: $event.item.title,
          },
        ];
      };

      const findRemarkFromStatusList = (value) => {
        const res = state.statusList.find((item) => item.value === value);
        return res ? res.remark : '';
      };

      const selectConnect = ($event, index) => {
        state.conditions[index].logicalOperator = $event.key;
      };

      const selectConnectChildren = ($event, index, childrenIndex) => {
        state.conditions[index].conditions[childrenIndex].logicalOperator = $event.key;
      };

      const addConditionsGroup = () => {
        state.conditions.push({
          type: 'GROUP',
          logicalOperator: 'AND',
          conditions: [
            {
              type: 'CONDITION',
              leftParam: {
                dataType: 'string',
                deviceIdentification: '',
                id: '',
                productIdentification: '',
              },
              operator: {
                desc: '',
                value: '',
              },
              rightParams: [
                {
                  type: 'CONSTANT',
                  value: '',
                  desc: '',
                },
              ],
              logicalOperator: 'AND',
              visiable: false,
              device: {},
              product: {
                services: [],
              },
            },
          ],
        });
      };

      const addConditionsChildren = (index) => {
        state.conditions[index].conditions.push({
          type: 'CONDITION',
          leftParam: {
            dataType: 'string',
            deviceIdentification: '',
            id: '',
            productIdentification: '',
          },
          operator: {
            desc: '',
            value: '',
          },
          rightParams: [
            {
              type: 'CONSTANT',
              value: '',
              desc: '',
            },
          ],
          logicalOperator: 'AND',
          visiable: false,
          device: {},
          product: {
            services: [],
          },
        });
      };

      const delConditions = (index) => {
        state.conditions.splice(index, 1);
      };

      const delConditionsChildren = (index, childrenIndex) => {
        state.conditions[index].conditions.splice(childrenIndex, 1);
      };

      const cancel = (e) => {
        console.log(e);
      };

      const saveTriggerRule = (res) => {
        if (
          res.selectedProduct.deviceIdentification ===
          state.conditions[res.index].conditions[res.childrenIndex].leftParam.deviceIdentification
        ) {
          return false;
        } else {
          state.conditions[res.index].conditions[res.childrenIndex].leftParam.deviceIdentification =
            res.selectedDevice.deviceIdentification;
          getDeviceInfoList(res.selectedDevice.deviceIdentification, res.index, res.childrenIndex);
        }

        if (
          res.selectedProduct.productIdentification ===
          state.conditions[res.index].conditions[res.childrenIndex].leftParam.productIdentification
        ) {
          return false;
        } else {
          state.conditions[res.index].conditions[
            res.childrenIndex
          ].leftParam.productIdentification = res.selectedProduct.productIdentification;
          state.conditions[res.index].conditions[res.childrenIndex].operator = {
            desc: '',
            value: '',
          };
          state.conditions[res.index].conditions[res.childrenIndex].rightParams = [
            {
              type: 'CONSTANT',
              value: '',
              desc: '',
            },
          ];

          getProductInfoList(
            res.selectedProduct.productIdentification,
            res.index,
            res.childrenIndex,
          );
        }
      };

      const getProductInfoList = async (productIdentification, index, childrenIndex) => {
        if (!productIdentification) {
          return false;
        }
        const res = await getFullProductInfo(productIdentification);
        state.conditions[index].conditions[childrenIndex].product = res;
      };

      const getDeviceInfoList = async (deviceIdentification, index, childrenIndex) => {
        if (!deviceIdentification || deviceIdentification === 'all') {
          return false;
        }
        const res = await detailBydeviceIdentification(deviceIdentification);
        state.conditions[index].conditions[childrenIndex].device = res;
      };

      const handleSelectRule = (index, childrenIndex) => {
        let leftParam = state.conditions[index].conditions[childrenIndex].leftParam;
        if (leftParam.deviceIdentification && leftParam.productIdentification) {
          openModal(true, {
            type: ActionEnum.EDIT,
            index,
            childrenIndex,
            conditionItem: {
              leftParam,
            },
          });
        } else {
          openModal(true, {
            type: ActionEnum.ADD,
            index,
            childrenIndex,
          });
        }
      };

      const handleSuccess = () => {
        load();
      };

      const getName = (value, valueKey, obj, nameKey) => {
        if (obj) {
          if (obj[valueKey] === value) {
            return obj[nameKey];
          }
          return '';
        } else {
          return '';
        }
      };

      return {
        t,
        registerModal,
        openModal,
        loading,
        conditions,
        connectList,
        statusList,
        getLabelFilter,
        getOperateList,
        selectOperate,
        selectDeviceStatus,
        findRemarkFromStatusList,
        selectConnect,
        selectConnectChildren,
        addConditionsGroup,
        addConditionsChildren,
        delConditions,
        delConditionsChildren,
        cancel,
        saveTriggerRule,
        getProductInfoList,
        getDeviceInfoList,
        handleSelectRule,
        handleSuccess,
        getName,
      };
    },
  });
</script>

<style lang="less" scoped>
  .detail-info {
    padding: 16px 16px 0;

    &:nth-last-child(1) {
      padding-bottom: 16px;
    }

    .linkage-title {
      font-size: 16px;
      font-family: PingFang SC-Medium, PingFang SC;
      font-weight: 600;
      color: #2e3033;
      line-height: 19px;
      margin-bottom: 10px;
      display: flex;
      align-items: center;

      .type {
        margin-left: 10px;
      }
    }

    .detail-title {
      position: relative;
      padding: 0 0 0 10px;
      color: #000c;
      font-weight: 600;
      line-height: 20px;
      min-height: 48px;
      border-bottom: 1px dotted #ccc;
      margin-bottom: 16px;
      display: flex;
      align-items: center;

      &.hideIcon {
        border-bottom: 0 none;

        &:before {
          display: none;
        }
      }

      .detail-title-info {
        margin-left: 10px;
        height: 40px;
        display: flex;
        align-items: center;
      }

      &:before {
        position: absolute;
        top: 14px;
        left: 0;
        width: 4px;
        height: 20px;
        background-color: @primary-color;
        border-radius: 0 3px 3px 0;
        content: ' ';
      }
    }
  }

  .linkage-condition {
    display: flex;

    .conditions-terms-title {
      width: 50px;
      height: 30px;
      line-height: 30px;
      text-align: center;
      margin-right: 20px;
      color: #fff;
      font-weight: 800;
      font-size: 14px;
      background-color: @primary-color;
      border-radius: 20px;
    }

    .condition-con {
      width: 100%;

      .condition-con-d {
        display: flex;
        position: relative;
        margin-top: 0;

        .conditions-terms-con-ite {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          position: relative;
          padding-left: 60px;

          &:nth-child(1) {
            padding-left: 0;
          }

          & > .ant-dropdown-trigger {
            position: absolute;
            left: 0;
            font-weight: bold;
          }
        }

        .conditions-terms-item {
          display: flex;
          flex-wrap: wrap;
          position: relative;
          padding: 8px;
          border: 1px dashed #aaa;
          border-radius: 6px;
          margin-bottom: 10px;
          align-items: center;
          box-shadow: 0 0 4px #ccc;

          .del-btn {
            position: absolute;
            top: -12px;
            right: -12px;
          }

          .conditions-terms-list-item {
            position: relative;
            display: flex;
            align-items: center;
            margin: 5px 0;

            .del-btn {
              top: -12px;
              right: -4px;
            }
          }
        }

        .conditions-terms-con {
          padding-top: 4px;
          display: block;
          align-items: center;
          flex-wrap: wrap;
          overflow: auto;
        }
      }
    }

    .else-box {
      position: relative;

      .del-btn {
        position: absolute;
        top: -12px;
        right: -12px;
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

  .ts-img {
    width: 100%;
  }
</style>
../../../../../../../api/iot/link/product/product../../../../../../../api/iot/link/device/device../../../../../../../api/iot/link/operator/operator
