<template>
  <a-spin :spinning="spinning" :tip="t('common.loadingText')">
    <PageWrapper contentFullHeight>
      <div class="detail-info" v-if="!spinning">
        <a-card title="" :bordered="false">
          <div class="linkage-title">
            <div
              ><span> {{ conditionDetails.ruleName }} </span>
              <a-button type="dashed" class="type" @click="selectConditionType">
                <ApartmentOutlined />{{
                  getLabelFilter(conditionType, 'value', 'label', conditionTypeList)
                }}
              </a-button>
              <a-tooltip placement="topLeft" overlayClassName="condition-select-uuid-tool-tip">
                <template #title>
                  <span class="uuid-tool-tip">
                    {{ t('iot.link.engine.executionLog.ruleIdentification') }}:
                    {{ conditionDetails.ruleIdentification }}
                    <CopyOutlined @click="copyText(conditionDetails.ruleIdentification)" />
                  </span>
                </template>
                <i v-if="conditionDetails.ruleIdentification" class="iconfont icon-id mrl-10"></i>
              </a-tooltip>
              <a-tooltip placement="topLeft" overlayClassName="condition-select-uuid-tool-tip">
                <template #title>
                  <span class="uuid-tool-tip">
                    {{ t('iot.link.engine.executionLog.remark') }}:
                    {{ conditionDetails.parentConditionRemark }}
                    <CopyOutlined @click="copyText(conditionDetails.parentConditionRemark)" />
                  </span>
                </template>
                <InfoCircleOutlined
                  v-if="conditionDetails.parentConditionRemark"
                  class="mrl-10"
                /> </a-tooltip
            ></div>
            <a-button
              style="float: right"
              type="primary"
              color="error"
              preIcon="ant-design:switcher-outlined"
              @click="openDetailDrawer"
              v-hasAnyPermission="['rule:engine:linkage:executionLogDetail']"
            >
              {{ t('iot.link.engine.executionLog.executionLog') }}
            </a-button>
          </div>
          <a-card title="" :bordered="false">
            <div class="detail-title">
              <span>{{ t('iot.link.engine.executionLog.triggerCondition') }}</span>
              <DebounceSetting
                :disabled="conditionType === 1"
                v-model:antiShake="antiShake"
                v-model:frequency="frequency"
                v-model:occurrenceRadio="occurrenceRadio"
              />
            </div>
            <ConditionByStatus
              ref="conditionByStatus"
              v-show="conditionType === 2"
              :conditionScheme="conditionDetails.conditionScheme"
              :productInfos="productInfos"
              :deviceInfos="deviceInfos"
              :conditionType="conditionDetails.conditionType"
            />
            <conditionSelect
              ref="conditionSelect"
              :conditionScheme="conditionDetails.conditionScheme"
              :productInfos="productInfos"
              :deviceInfos="deviceInfos"
              :conditionType="conditionDetails.conditionType"
              v-show="conditionType === 0"
            />
            <SetIntervalContent
              v-show="conditionType === 1"
              :appointContent="setIntervalParams.appointContent"
              :effectiveType="setIntervalParams.effectiveType"
              :disabled="true"
            />
          </a-card>
          <a-card title="" :bordered="false">
            <div class="detail-title">
              <span>{{ t('iot.link.engine.executionLog.performAnAction') }}</span>
            </div>
            <actionSelect
              ref="triggerAction"
              :actions="conditionDetails.conditionActionDetailsResultVOS"
              :productInfos="productInfos"
              :deviceInfos="deviceInfos"
            />
          </a-card>
          <a-card title="" :bordered="false">
            <div class="detail-title">{{ t('iot.link.engine.executionLog.explanation') }}</div>
            <div class="linkage-rule">
              <a-textarea
                v-model:value="remark"
                placeholder="请输入说明"
                :auto-size="{ minRows: 2, maxRows: 5 }"
              />
            </div>
          </a-card>
          <div style="text-align: center">
            <a-button type="primary" class="type" @click="submitHandle">{{
              t('common.saveText')
            }}</a-button>
          </div>
        </a-card>
      </div>
      <ConditionType
        v-if="conditionTypeList.length"
        :conditionType="conditionType"
        :options="conditionTypeList"
        @register="registerModal"
        @success="handleSuccess"
        @select-type-card="selectTypeCard"
      />
    </PageWrapper>
    <executionLogDetail @register="executionLogDetailRegister" />
  </a-spin>
</template>
<script lang="ts">
  import { defineComponent, reactive, onMounted, toRefs, getCurrentInstance } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BizConstant } from '/@/enums/biz/common';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { PageWrapper } from '/@/components/Page';
  import conditionSelect from './components/condition/conditionSelect.vue';
  import ConditionType from './components/condition/conditionType.vue';
  import actionSelect from './components/action/actionSelect.vue';
  import { useRouter } from 'vue-router';
  import { Card, Button } from 'ant-design-vue';
  import { getRuleDetails } from '../../../../../api/iot/rule/engine/linkage/linkage';
  import { detailBydeviceIdentifications } from '/@/api/iot/link/device/device';
  import { getFullProductInfos } from '/@/api/iot/link/product/product';
  import {
    saveRuleConditionAction,
    updateRuleConditionAction,
  } from '../../../../../api/iot/rule/engine/linkage/conditionAction';
  import { randomNum } from '/@/utils';
  import { getLabelFilter } from '/@/utils/thinglinks/common';
  import { ApartmentOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import DebounceSetting from './components/condition/debounceSetting.vue';
  import SetIntervalContent from './components/condition/setIntervalContent.vue';
  import ConditionByStatus from './components/condition/conditionByStatus.vue';
  import { CopyOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';
  import executionLogDetail from './executionLog/detail.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useDictStore } from '/@/store/modules/dict';

  export default defineComponent({
    name: 'LinkageDetail',
    components: {
      ACard: Card,
      ConditionType,
      conditionSelect,
      actionSelect,
      ApartmentOutlined,
      PageWrapper,
      AButton: Button,
      DebounceSetting,
      SetIntervalContent,
      ConditionByStatus,
      CopyOutlined,
      InfoCircleOutlined,
      executionLogDetail,
    },
    emits: ['success', 'register'],
    setup() {
      // 是否显示密码明文
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const { currentRoute, go } = useRouter();
      const [registerModal, { openModal }] = useModal();
      const [executionLogDetailRegister, { openDrawer: openDetail }] = useDrawer();
      const { proxy } = getCurrentInstance();

      const state = reactive<any>({
        spinning: true,
        occurrenceRadio: 1,
        antiShake: 1,
        frequency: {
          timeValue: 1,
          count: 1,
        },
        remark: '',
        activeKey: ['1'],
        actions: [
          {
            children: [{ service: {}, property: {}, operate: {} }],
          },
        ],
        triggerTypeObj: {
          activeStep: 0,
          selectedProduct: {},
          selectedDevice: {},
          selectedType: {},
        },
        ruleId: null, // rule主键ID
        conditionType: 0, // 0设备属性触发 1定时触发 2设备状态触发
        conditionTypeList: [],
        conditionDetails: {},
        productInfos: [], // 拍平去重后的产品列表
        deviceInfos: [], // 拍平去重后的设备列表
        setIntervalParams: {}, // 定时触发参数
      });

      onMounted(() => {
        const { query } = currentRoute.value;
        state.ruleId = query.id;
        console.log(query);

        getDetails(state.ruleId);
        const dictStore = useDictStore();
        state.conditionTypeList = dictStore.getDictItemOptionList(DictEnum.RULE_EVENT_TYPE);
        console.log('state.conditionTypeList', state.conditionTypeList);
        load();
      });

      const getDetails = async (id) => {
        const detail = await getRuleDetails(id);
        state.conditionDetails = {
          ...detail,
        };
        if (detail.conditionDetailsResultVOS.length) {
          // 数据解构
          let conditionDetailsResult = { ...detail.conditionDetailsResultVOS[0] };
          conditionDetailsResult.antiShakeScheme = JSON.parse(
            conditionDetailsResult.antiShakeScheme,
          );
          conditionDetailsResult.conditionScheme = JSON.parse(
            conditionDetailsResult.conditionScheme,
          );
          conditionDetailsResult.conditionActionDetailsResultVOS.forEach((item) => {
            item.actionContent = JSON.parse(item.actionContent);
          });

          // console.log(conditionDetailsResult)
          state.conditionDetails = {
            ...state.conditionDetails,
            ...conditionDetailsResult,
            parentConditionRemark: state.conditionDetails.remark,
          };

          // 防抖状态
          state.antiShake = conditionDetailsResult.antiShake;
          // 防抖策略
          state.occurrenceRadio =
            conditionDetailsResult.antiShakeScheme.occurrence.first == true ? 1 : 2;
          state.frequency = conditionDetailsResult.antiShakeScheme.frequency;
          console.log(state.conditionDetails);
          // console.log(state.conditionDetails)
          // 获取拍平后的结果
          let flattenItem = flatten([
            state.conditionDetails.conditionScheme?.map((item) => item.conditions),
            state.conditionDetails.conditionActionDetailsResultVOS.map((item) => {
              if (item.actionType === 1) {
                return item;
              }
              return item.actionContent.parallel.concat(item.actionContent.serial);
            }),
          ]);

          // console.log(flattenItem)
          // 筛选出获取productIdentification有值的
          let productIdentifications = flattenItem.filter((item) => {
            return item.leftParam?.productIdentification || item.productIdentification;
          });
          // 筛选出获取deviceIdentification有值的
          let deviceIdentifications = flattenItem.filter((item) => {
            return (
              item.leftParam?.deviceIdentification ||
              (item.deviceIdentification && item.deviceIdentification != BizConstant.ALL)
            );
          });

          // 获取productIdentification合集
          productIdentifications = productIdentifications.map((item) => {
            return item.leftParam?.productIdentification || item.productIdentification;
          });
          // 获取deviceIdentification合集
          deviceIdentifications = deviceIdentifications.map((item) => {
            return item.leftParam?.deviceIdentification || item.deviceIdentification;
          });
          // return false;
          //去重后批量请求product
          const productInfos = await getFullProductInfos({
            productIdentifications: noRepeat(productIdentifications).join(','),
          });
          //去重后批量请求device  考虑deviceIdentifications == all的情况
          const deviceInfos = await detailBydeviceIdentifications({
            deviceIdentifications: noRepeat(deviceIdentifications).join(','),
          });
          state.productInfos = productInfos;
          state.deviceInfos = deviceInfos;
          state.conditionType = conditionDetailsResult.conditionType;
        }
        state.setIntervalParams.effectiveType = detail.effectiveType;
        state.setIntervalParams.appointContent = JSON.parse(detail.appointContent as string);
        state.spinning = false;
      };

      // 数组拍平及去重
      // const flattenAndNoRepeat = (arr) => {
      //   return Array.from(new Set(arr.flat(Infinity)));
      // }

      // 数组拍平
      const flatten = (arr) => {
        return arr.flat(Infinity);
      };
      // 数组去重
      const noRepeat = (arr) => {
        return Array.from(new Set(arr));
      };

      const load = async () => {
        // 操作符
        // const resOperate = await operate({})
        // state.operateList = resOperate
        // // 连接符
        // const resConnect = await connect({})
        // state.connectList = resConnect
      };
      function copyFn(text) {
        let result = copyTextToClipboard(text);
        console.log(result, 'result');
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      }
      const selectConditionType = () => {
        openModal(true, {
          type: ActionEnum.EDIT,
        });
      };
      // 新增或编辑成功回调
      function handleSuccess() {
        load();
      }

      const selectTypeCard = (res) => {
        state.conditionType = res;
      };

      const getParamsObject = (arr) => {
        let jsonData = {};
        arr.map((item) => {
          jsonData[item.key] = item.value;
        });
        return jsonData;
      };
      const validateCondition = async (conditionType: number, conditions: any) => {
        // 设备属性触发验证函数
        const validate = (conditions) => {
          let errors: any = [];

          const validateItem = (condition, parentIndex, childIndex) => {
            // 校验operator.value是否为空
            if (!condition.operator.value) {
              errors.push({
                parentIndex,
                childIndex,
                field: 'operator.value',
              });
            }

            // 校验rightParams中的value是否为空 - 暂时先不验证rightParams.value
            // condition.rightParams.forEach((param, index) => {
            //   if (!param.value) {
            //     errors.push({
            //       parentIndex,
            //       childIndex,
            //       rightParamIndex: index,
            //       field: 'rightParams.value'
            //     });
            //   }
            // });
          };

          conditions.forEach((group, parentIndex) => {
            group.conditions.forEach((condition, childIndex) => {
              validateItem(condition, parentIndex, childIndex);
            });
          });

          return errors;
        };
        switch (conditionType) {
          case 0:
            const errorAttribute = validate(conditions);
            if (errorAttribute.length) {
              const { parentIndex, childIndex } = errorAttribute[0];
              createMessage.warning(`第${parentIndex + 1}条件组中第${childIndex + 1}项条件填写不完全`);
              return false;
            }
            return true;

          case 2:
            const errorStatus = validate(conditions);
            if (errorStatus.length) {
              const { parentIndex, childIndex } = errorStatus[0];
              createMessage.warning(`第${parentIndex + 1}条件组中第${childIndex + 1}项条件填写不完全`);
              return false;
            }
            return true;
          default:
            return true;
        }
      };
      const handleSaveRuleConditionAction = async () => {
        const conditions = proxy.$refs.conditionSelect?.conditions;
        const conditionsByStatus = proxy.$refs.conditionByStatus?.conditions;
        const triggerAction = proxy.$refs.triggerAction?.actions;
        const validataRes = await validateCondition(
          state.conditionType,
          state.conditionType === 0 ? conditions : conditionsByStatus,
        );
        if (!validataRes) {
          return 'error';
        }
        const antiShakeScheme = {
          unit: 'second',
          frequency: state.frequency,
          occurrence: {
            first: state.occurrenceRadio == 1,
            last: state.occurrenceRadio == 2,
          },
        };

        const cond =
          state.conditionType === 1
            ? []
            : (state.conditionType === 0 ? conditions : conditionsByStatus).map((item) => {
                return {
                  type: item.type,
                  uuid: randomNum(),
                  logicalOperator: item.logicalOperator,
                  conditions: item.conditions.map((ite) => {
                    return {
                      uuid: randomNum(),
                      type: ite.type,
                      logicalOperator: ite.logicalOperator,
                      leftParam: {
                        id: '',
                        productIdentification: ite.leftParam.productIdentification,
                        deviceIdentification: ite.leftParam.deviceIdentification,
                        dataType: ite.leftParam.datatype || 'string',
                        multiSelect: false,
                        ...(state.conditionType === 0
                          ? {
                              serviceCode: ite.leftParam.serviceCode,
                              field: ite.leftParam.field,
                              desc: ite.leftParam.field,
                            }
                          : {}),
                      },
                      operator: ite.operator,
                      rightParams: ite.rightParams,
                    };
                  }),
                };
              });
        console.log(cond, '条件');
        // 动作判空规则
        const filterParams = (arr) => {
          return arr.filter((item) => {
            return (
              (item.actionType === 0 &&
                (item.actionContent.serial.length > 0 || item.actionContent.parallel.length > 0)) ||
              (item.actionType === 1 && Object.keys(item.actionContent).length > 0)
            );
          });
        };
        const conditionActionSaveVOS = filterParams(triggerAction).map((item) => {
          return {
            ruleConditionId: state.conditionDetails.id || null,
            id: item.id || null,
            actionType: item.actionType,
            actionContent:
              item.actionType === 1
                ? JSON.stringify(item.actionContent)
                : JSON.stringify({
                    serial: item.actionContent.serial.map((actionItem) => {
                      return {
                        msgType: actionItem.msgType,
                        cmd: actionItem.cmd,
                        params: getParamsObject(actionItem.params),
                        serviceCode: actionItem.serviceCode,
                        deviceIdentification: actionItem.deviceIdentification,
                        productIdentification: actionItem.productIdentification,
                      };
                    }),
                    parallel: item.actionContent.parallel.map((actionItem) => {
                      return {
                        msgType: actionItem.msgType,
                        cmd: actionItem.cmd,
                        params: getParamsObject(actionItem.params),
                        serviceCode: actionItem.serviceCode,
                        deviceIdentification: actionItem.deviceIdentification,
                        productIdentification: actionItem.productIdentification,
                      };
                    }),
                  }),
          };
        });

        console.log(conditionActionSaveVOS, '动作');

        if (!state.conditionDetails.conditionDetailsResultVOS?.length) {
          let params = {
            antiShake: state.antiShake,
            antiShakeScheme: JSON.stringify(antiShakeScheme),
            conditionActionSaveVOS,
            conditionScheme: JSON.stringify(cond),
            ruleId: state.ruleId,
            conditionType: state.conditionType,
            status: 0,
          };
          console.log('params', params);
          const res = await saveRuleConditionAction(params);
          go(-1);
        } else {
          let params = {
            antiShake: state.antiShake,
            antiShakeScheme: JSON.stringify(antiShakeScheme),
            conditionActionUpdateVOS: conditionActionSaveVOS,
            conditionScheme: JSON.stringify(cond),
            ruleId: state.ruleId,
            conditionType: state.conditionType,
            status: 0,
            id: state.conditionDetails.id,
          };
          console.log('params1', params, state);
          const res = await updateRuleConditionAction(params);
          go(-1);
        }
      };
      const submitHandle = async () => {
        createConfirm({
          iconType: 'info',
          content: `当前触发类型为：⌈${
            state.conditionTypeList?.find((item: any) => item.value === String(state.conditionType))
              ?.name
          }⌋， 确认保存吗?`,
          onOk: async (e) => {
            try {
              const res = await handleSaveRuleConditionAction();
              if (res !== 'error') {
                createMessage.success(t('common.tips.saveSuccess'));
              }
            } catch (error) {
              throw new Error('保存失败');
            }
          },
        });
      };

      const copyText = (text) => {
        let result = copyTextToClipboard(text);
        console.log(result, 'result');
        if (result) {
          createMessage.success(t('common.tips.copySuccess'));
        } else {
          createMessage.warning(t('common.tips.copyFail'));
        }
      };
      // 查看执行日志
      const openDetailDrawer = () => {
        openDetail(true, {
          ruleIdentification: state.conditionDetails.ruleIdentification,
        });
      };

      return {
        t,
        copyFn,
        labelStyle: {
          width: '120px',
          'font-weight': '600',
          'font-size': '14px',
        },
        selectTypeCard,
        handleSuccess,
        handleSaveRuleConditionAction,
        registerModal,
        ...(toRefs(state) as any),
        getLabelFilter,
        selectConditionType,
        submitHandle,
        copyText,
        executionLogDetailRegister,
        openDetailDrawer,
      };
    },
  });
</script>
<style lang="less" scope>
  .mrl-10 {
    margin-left: 10px;
  }

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
      justify-content: space-between;

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

    .actions-terms-title {
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
  }

  .mt20 {
    margin-top: 12px;
  }

  .ts-img {
    width: 100%;
  }
</style>
