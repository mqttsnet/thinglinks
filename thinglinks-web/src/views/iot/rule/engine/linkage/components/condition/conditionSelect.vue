<template>
  <div class="linkage-condition">
    <div class="condition-con" v-if="!loading">
      <div class="condition-con-d">
        <a-tooltip placement="topLeft" overlayClassName="condition-select-uuid-tool-tip">
          <template #title>
            <span class="uuid-tool-tip">
              UUID: {{ parentConditionsUUID }}
              <CopyOutlined @click="copyUUid(parentConditionsUUID)" />
            </span>
          </template>
          <i v-if="parentConditionsUUID" class="iconfont icon-id parent-conditions-id"></i>
        </a-tooltip>
        <div style="width: 100%">
          <div class="conditions-terms-con">
            <!-- <div class="conditions-terms-con-ite-list"> -->
            <div class="conditions-terms-con-ite" v-for="(item, index) in conditions" :key="index">
              <a-dropdown class="mr8" v-if="index > 0" placement="bottomLeft">
                <a-button type="primary" shape="round">{{
                  getLabelFilter(item.logicalOperator, 'name', 'desc', connectList)
                }}</a-button>
                <template #overlay>
                  <a-menu @click="selectConnect($event, index)">
                    <a-menu-item
                      v-for="connectItem in connectList"
                      :key="connectItem.name"
                      :value="connectItem.value"
                      :title="connectItem.desc"
                      >{{ connectItem.desc }}</a-menu-item
                    >
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
                    <a-button class="ml8" type="primary" size="small">
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
                          >{{ connectChildrenItem.desc }}</a-menu-item
                        >
                      </a-menu>
                    </template>
                  </a-dropdown>
                  <div class="content-item">
                    <a-tooltip
                      placement="topLeft"
                      overlayClassName="condition-select-uuid-tool-tip"
                    >
                      <template #title>
                        <span class="uuid-tool-tip">
                          UUID:{{ childrenItem.uuid }}
                          <CopyOutlined @click="copyUUid(childrenItem.uuid)" />
                        </span>
                      </template>
                      <i v-if="childrenItem.uuid" class="iconfont icon-id mr8"></i>
                    </a-tooltip>
                    <a-button
                      class="mr8 type ant-tag-red"
                      @click="handleSelectRule(index, childrenIndex)"
                    >
                      <span v-if="!childrenItem.leftParam.productIdentification">{{
                        t('iot.link.engine.executionLog.triggerTips')
                      }}</span>
                      <span class="condition-item" v-else>
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
                        {{ getDeviceScopeName(childrenItem) }}
                        <!-- {{ childrenItem.triggerTypeObj.selectedType.name }} -->
                      </span>
                    </a-button>
                    <template v-if="childrenItem.leftParam.productIdentification">
                      <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
                        <a-button class="ant-tag-orange">{{
                          childrenItem.leftParam.serviceCode
                            ? getLabelFilter(
                                childrenItem.leftParam.serviceCode,
                                'serviceCode',
                                'serviceName',
                                childrenItem.product.services,
                              )
                            : '请选择服务'
                        }}</a-button>
                        <template #overlay>
                          <a-menu @click="selectService($event, index, childrenIndex)">
                            <a-menu-item
                              :key="serviceItem.serviceCode"
                              :title="serviceItem.serviceName"
                              :selectedKeys="[childrenItem.leftParam.serviceCode]"
                              v-for="serviceItem in childrenItem.product.services"
                              >{{ serviceItem.serviceName }}</a-menu-item
                            >
                          </a-menu>
                        </template>
                      </a-dropdown>
                      <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
                        <a-button class="ant-tag-green">{{
                          childrenItem.leftParam.field
                            ? getLabelFilter(
                                childrenItem.leftParam.field,
                                'propertyCode',
                                'propertyName',
                                getPropertyList(
                                  childrenItem.leftParam.serviceCode,
                                  childrenItem.product.services,
                                ),
                              )
                            : '请选择参数'
                        }}</a-button>
                        <template #overlay>
                          <a-menu @click="selectProperty($event, index, childrenIndex)">
                            <a-menu-item
                              :key="propertyItem.propertyCode"
                              :title="propertyItem.propertyName"
                              :params="propertyItem"
                              v-for="propertyItem in getPropertyList(
                                childrenItem.leftParam.serviceCode,
                                childrenItem.product.services,
                              )"
                              >{{ propertyItem.propertyName }}</a-menu-item
                            >
                          </a-menu>
                        </template>
                      </a-dropdown>
                      <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
                        <a-button class="ant-tag-cyan">{{
                          childrenItem.operator.desc ? childrenItem.operator.desc : '操作符'
                        }}</a-button>
                        <template #overlay>
                          <a-menu @click="selectOperate($event, index, childrenIndex)">
                            <a-menu-item
                              v-for="operateItem in getOperateList(childrenItem.leftParam.dataType)"
                              :key="operateItem.name"
                              :value="operateItem.value"
                              :title="operateItem.desc"
                              >{{ operateItem.desc }}</a-menu-item
                            >
                          </a-menu>
                        </template>
                      </a-dropdown>
                      <a-dropdown
                        placement="bottomLeft"
                        class="mr8"
                        :trigger="['click']"
                        :visible="childrenItem.visiable"
                        @visibleChange="visibleChange($event, index, childrenIndex)"
                      >
                        <a-button class="ant-tag-purple">
                          <div>
                            <span
                              v-for="(paramsItem, paramsIndex) in childrenItem.rightParams"
                              :key="paramsIndex"
                            >
                              {{ paramsItem.value ? paramsItem.value : '参数值' }}</span
                            >
                          </div>
                        </a-button>
                        <template #overlay>
                          <a-card style="padding: 10px">
                            <!-- TODO 需要根据属性值及操作符匹配输入框内容以及处理多个参数的问题 -->
                            <div
                              v-for="(paramsItem, paramsIndex) in childrenItem.rightParams"
                              :key="paramsIndex"
                            >
                              <input-item v-model:value="paramsItem.value" type="input" />
                              <!-- <a-input v-model:value="paramsItem.value" placeholder="请输入" /> -->
                            </div>
                            <!-- <div>
                              <a-textarea v-model:value="value" placeholder="请输入" :rows="4" />
                            </div>
                            <div>
                              <a-input-number id="inputNumber" v-model:value="value" :min="1" :max="10" />
                            </div>
                            <div>
                              <a-time-picker v-model:value="strValue" valueFormat="HH:mm:ss" />
                            </div>
                            <div>
                              <a-radio-group v-model:value="value">
                                <a-radio :value="1">Option A</a-radio>
                                <a-radio :value="2">Option B</a-radio>
                                <a-radio :value="3">Option C</a-radio>
                                <a-radio  :value="4">
                                  More...
                                  <a-input v-if="value === 4" style="width: 100px; margin-left: 10px" />
                                </a-radio>
                              </a-radio-group>
                            </div> -->
                          </a-card>
                        </template>
                      </a-dropdown>
                    </template>
                  </div>
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
            <!-- </div> -->
            <a-button type="primary" @click="addConditionsGroup">
              <PlusOutlined />{{ t('iot.link.engine.executionLog.grouping') }}</a-button
            >
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
  import { defineComponent, ref, toRefs, reactive, onMounted, watch, defineProps } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicForm } from '/@/components/Form/index';
  import InputItem from '/@/components/InputItem/index.vue';
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
  import { BizConstant } from '/@/enums/biz/common';
  import { ActionEnum } from '/@/enums/commonEnum';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import { detailBydeviceIdentification } from '/@/api/iot/link/device/device';
  import { operate, connect } from '/@/api/iot/link/operator/operator';
  import triggerRule from '../modal/triggerRule.vue';
  import { getLabelFilter } from '/@/utils/thinglinks/common';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';

  import {
    ApartmentOutlined,
    ControlOutlined,
    ApiOutlined,
    PlusOutlined,
    CloseCircleOutlined,
    CloseSquareOutlined,
    DeleteOutlined,
    CopyOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  const { getDictList } = useDict();

  export default defineComponent({
    name: '条件配置',
    components: {
      ApartmentOutlined,
      ControlOutlined,
      ApiOutlined,
      PlusOutlined,
      CloseCircleOutlined,
      CloseSquareOutlined,
      DeleteOutlined,
      InputItem,
      triggerRule,
      CopyOutlined,
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
      const type = ref<ActionEnum>(ActionEnum.ADD);
      const [registerModal, { openModal }] = useModal();
      const { createMessage } = useMessage();

      const state = reactive({
        loading: false,
        parentConditionsUUID: '',
        conditions: [
          {
            type: 'GROUP',
            // 第一条传固定值
            logicalOperator: 'AND',
            conditions: [
              {
                type: 'CONDITION',
                leftParam: {
                  dataType: '',
                  desc: '',
                  deviceIdentification: '',
                  field: '',
                  id: '',
                  multiSelect: false,
                  productIdentification: '',
                  serviceCode: '',
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
                // 第一条传固定值
                logicalOperator: 'AND',

                visiable: false,
                device: {
                  // 设备
                },
                product: {
                  // 产品
                  services: [],
                },
              },
            ],
          },
        ],
        operateList: [],
        connectList: [],
      });

      onMounted(() => {
        load();
        // initData();
      });
      const initData = () => {
        if (props.conditionType === 0) {
          state.conditions = props.conditionScheme;
          state.conditions.forEach((item, index) => {
            state.parentConditionsUUID = item.uuid;
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
        // 操作符
        const resOperate = await operate({});
        state.operateList = resOperate;
        // 连接符
        const resConnect = await connect({});
        state.connectList = resConnect;
      };

      const getPropertyList = (serviceCode, services) => {
        let list = [];
        services.map((item) => {
          if (item.serviceCode == serviceCode) {
            list = item.properties;
          }
        });
        return list;
      };

      const selectService = async ($event, index, childrenIndex) => {
        // 设置该服务下的属性列表
        // const productPropertyList = state.conditions[index].conditions[childrenIndex].productServiceList.filter(
        //   (item) => item.serviceCode == $event.key,
        // )[0].properties;

        state.conditions[index].conditions[childrenIndex].leftParam = {
          ...state.conditions[index].conditions[childrenIndex].leftParam,
          serviceCode: $event.key,
        };
        // 重置属性值为空对象
        state.conditions[index].conditions[childrenIndex].leftParam.field = '';
        state.conditions[index].conditions[childrenIndex].rightParams.operator = {
          desc: '',
          value: '',
        };
        state.conditions[index].conditions[childrenIndex].rightParams = [
          {
            type: 'CONSTANT',
            value: '',
            desc: '',
          },
        ];
      };
      // 选择属性
      const selectProperty = async ($event, index, childrenIndex) => {
        // 设置属性json
        state.conditions[index].conditions[childrenIndex].leftParam = {
          ...state.conditions[index].conditions[childrenIndex].leftParam,
          desc: $event.item.title,
          field: $event.key,
          dataType: $event.item.params.datatype,
        };
        state.conditions[index].conditions[childrenIndex].rightParams.operator = {
          desc: '',
          value: '',
        };
        state.conditions[index].conditions[childrenIndex].rightParams = [
          {
            type: 'CONSTANT',
            value: '',
            desc: '',
          },
        ];

        // 筛选出对应的操作符列表 放到json里
        // state.conditions[index].conditions[childrenIndex].operateList = getOperateList($event.item.params.datatype)
      };

      // 筛选出对应的操作符列表 放到json里
      const visibleChange = ($event, index, childrenIndex) => {
        state.conditions[index].conditions[childrenIndex].visiable =
          !state.conditions[index].conditions[childrenIndex].visiable;
      };

      // 筛选操作符列表
      const getOperateList = (datatype) => {
        if (datatype) {
          const operateList = state.operateList.filter((item) => {
            return item.supportedDataTypes.indexOf(datatype) > -1;
          });
          return operateList;
        }
      };

      // 选择操作符
      const selectOperate = async ($event, index, childrenIndex) => {
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
      // 选择组连接符
      const selectConnect = async ($event, index) => {
        state.conditions[index].logicalOperator = $event.key;
      };
      // 选择单条条件连接符
      const selectConnectChildren = async ($event, index, childrenIndex) => {
        state.conditions[index].conditions[childrenIndex].logicalOperator = $event.key;
      };
      const addConditionsGroup = () => {
        state.conditions.push({
          type: 'GROUP',
          conditions: [
            {
              type: 'CONDITION',
              leftParam: {
                dataType: '',
                desc: '',
                deviceIdentification: '',
                field: '',
                id: '',
                multiSelect: false,
                productIdentification: '',
                serviceCode: '',
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
              // 第一条传固定值
              logicalOperator: 'AND',

              visiable: false,
              device: {
                // 设备
              },
              product: {
                // 产品
                services: [],
              },
            },
          ],
          logicalOperator: state.connectList[0].name,
        });
      };
      const addConditionsChildren = (index) => {
        state.conditions[index].conditions.push({
          type: 'CONDITION',
          leftParam: {
            dataType: '',
            desc: '',
            deviceIdentification: '',
            field: '',
            id: '',
            multiSelect: false,
            productIdentification: '',
            serviceCode: '',
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
          // 第一条传固定值
          logicalOperator: 'AND',
          visiable: false,
          device: {},
          product: {
            services: [],
          },
        });
      };
      const delConditions = (index: number) => {
        state.conditions.splice(index, 1);
      };
      const delConditionsChildren = (index: number, childrenIndex: number) => {
        state.conditions[index].conditions.splice(childrenIndex, 1);
      };

      const cancel = (e: MouseEvent) => {
        return e;
      };
      const saveTriggerRule = (res) => {
        const conditionItem = state.conditions[res.index].conditions[res.childrenIndex];
        const nextDeviceIdentification = normalizeAllDevice(
          res.selectedDevice?.deviceIdentification,
        );
        const nextProductIdentification = res.selectedProduct?.productIdentification || '';

        if (nextDeviceIdentification !== conditionItem.leftParam.deviceIdentification) {
          conditionItem.leftParam.deviceIdentification = nextDeviceIdentification;
          getDeviceInfoList(nextDeviceIdentification, res.index, res.childrenIndex);
        }

        if (nextProductIdentification !== conditionItem.leftParam.productIdentification) {
          conditionItem.leftParam = {
            productIdentification: nextProductIdentification,
            deviceIdentification: nextDeviceIdentification,
          };
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

          getProductInfoList(nextProductIdentification, res.index, res.childrenIndex);
        }
      };
      // 获取产品详情
      async function getProductInfoList(productIdentification, index, childrenIndex) {
        if (!productIdentification) {
          return false;
        }
        const res = await getFullProductInfo(productIdentification);
        state.conditions[index].conditions[childrenIndex].product = res;
      }
      // 获取设备
      async function getDeviceInfoList(deviceIdentification, index, childrenIndex) {
        if (!deviceIdentification) {
          state.conditions[index].conditions[childrenIndex].device = {};
          return false;
        }
        if (isAllDevice(deviceIdentification)) {
          state.conditions[index].conditions[childrenIndex].device = {};
          return false;
        }
        const res = await detailBydeviceIdentification(deviceIdentification);
        state.conditions[index].conditions[childrenIndex].device = res;
      }
      // 弹出选择页面
      const handleSelectRule = (index: number, childrenIndex: number) => {
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
      function handleSuccess() {
        load();
      }
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

      const isAllDevice = (value) => value === BizConstant.ALL;

      const normalizeAllDevice = (value) => (isAllDevice(value) ? BizConstant.ALL : value || '');

      const getDeviceScopeName = (conditionItem) => {
        const deviceIdentification = conditionItem?.leftParam?.deviceIdentification;
        if (isAllDevice(deviceIdentification)) {
          return t('iot.link.engine.linkage.allDeviceScope');
        }
        return deviceIdentification
          ? getName(
              deviceIdentification,
              'deviceIdentification',
              conditionItem.device,
              'deviceName',
            ) || deviceIdentification
          : '';
      };

      const copyUUid = (uuid) => {
        let result = copyTextToClipboard(uuid);
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      };

      return {
        t,
        registerModal,
        getName,
        getDeviceScopeName,
        saveTriggerRule,
        ...toRefs(state),
        getDictList,
        selectService,
        selectProperty,
        selectOperate,
        selectConnect,
        selectConnectChildren,
        delConditions,
        delConditionsChildren,
        addConditionsGroup,
        addConditionsChildren,
        cancel,
        visibleChange,
        handleSelectRule,
        getLabelFilter,
        getPropertyList,
        getOperateList,
        copyUUid,
      };
    },
  });
</script>
<style lang="less" scope>
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

        .parent-conditions-id {
          position: absolute;
          left: -10px;
          top: -10px;
          font-size: 20px;
        }

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

            .content-item {
              background: #f0f0f0;
              padding: 5px 8px;
            }

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

  .ml8 {
    margin-left: 8px;
  }

  .mt20 {
    margin-top: 12px;
  }

  .ts-img {
    width: 100%;
  }

  .condition-select-uuid-tool-tip {
    max-width: 100%;
  }
</style>
