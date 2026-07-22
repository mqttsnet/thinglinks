<template>
  <a-spin :spinning="spinning" :tip="t('common.loadingText')">
    <PageWrapper contentFullHeight>
      <div class="linkage-detail-flexy" v-if="!spinning">
        <!-- ─────── 顶部规则信息条 ─────── -->
        <div class="flexy-header">
          <div class="header-icon-wrap">
            <ApartmentOutlined class="header-icon" />
          </div>
          <div class="header-content">
            <div class="header-line">
              <span class="rule-name">{{ conditionDetails.ruleName }}</span>
              <a-button
                type="dashed"
                class="type-chip"
                :disabled="readonlyMode"
                @click="selectConditionType"
              >
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
                <InfoCircleOutlined v-if="conditionDetails.parentConditionRemark" class="mrl-10" />
              </a-tooltip>
            </div>
            <div class="header-hint">{{ t('iot.link.engine.linkage.detailTitle') }}</div>
            <div class="header-meta">
              <span class="meta-item">
                <span class="meta-label">规则标识</span>
                <span class="meta-value">{{ conditionDetails.ruleIdentification || '-' }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">条件组</span>
                <span class="meta-value">{{ conditionGroupCount }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">动作数</span>
                <span class="meta-value">{{ actionCount }}</span>
              </span>
              <span class="meta-item" :class="{ muted: conditionType === 1 }">
                <span class="meta-label">防抖</span>
                <span class="meta-value">{{ antiShake === 0 ? '开启' : '关闭' }}</span>
              </span>
            </div>
          </div>
          <a-button
            type="primary"
            class="execution-log-btn"
            preIcon="ant-design:switcher-outlined"
            @click="openDetailDrawer"
            v-hasAnyPermission="['rule:engine:linkage:executionLogDetail']"
          >
            {{ t('iot.link.engine.executionLog.executionLog') }}
          </a-button>
        </div>

        <!-- ─────── 1:触发条件 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <div class="step-title">
              <span class="step-no">1</span>
              <span class="step-title-text">{{
                t('iot.link.engine.executionLog.triggerCondition')
              }}</span>
            </div>
            <div class="step-extra">
              <DebounceSetting
                :disabled="readonlyMode || conditionType === 1"
                v-model:antiShake="antiShake"
                v-model:frequency="frequency"
                v-model:occurrenceRadio="occurrenceRadio"
              />
            </div>
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
        </div>

        <!-- ─────── 2:执行动作 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <div class="step-title">
              <span class="step-no">2</span>
              <span class="step-title-text">{{
                t('iot.link.engine.executionLog.performAnAction')
              }}</span>
            </div>
          </div>
          <actionSelect
            ref="triggerAction"
            :actions="conditionDetails.conditionActionDetailsResultVOS"
            :productInfos="productInfos"
            :deviceInfos="deviceInfos"
          />
        </div>

        <!-- ─────── 3:说明 ─────── -->
        <div class="flexy-card">
          <div class="step-header">
            <div class="step-title">
              <span class="step-no">3</span>
              <span class="step-title-text">{{
                t('iot.link.engine.executionLog.explanation')
              }}</span>
            </div>
          </div>
          <a-textarea
            v-model:value="remark"
            placeholder="请输入说明"
            :disabled="readonlyMode"
            :auto-size="{ minRows: 2, maxRows: 5 }"
          />
        </div>

        <div v-if="!readonlyMode" class="flexy-footer">
          <a-button type="primary" @click="submitHandle">{{ t('common.saveText') }}</a-button>
        </div>
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
  import { computed, defineComponent, reactive, onMounted, toRefs, getCurrentInstance } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BizConstant } from '/@/enums/biz/common';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { PageWrapper } from '/@/components/Page';
  import conditionSelect from './components/condition/conditionSelect.vue';
  import ConditionType from './components/condition/conditionType.vue';
  import actionSelect from './components/action/actionSelect.vue';
  import { useRouter } from 'vue-router';
  import { Button } from 'ant-design-vue';
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
      const readonlyMode = computed(() => currentRoute.value.query.type === 'handleView');
      const conditionGroupCount = computed(() => {
        const scheme = state.conditionDetails?.conditionScheme;
        return Array.isArray(scheme) ? scheme.length : 0;
      });
      const actionCount = computed(() => {
        const actions = state.conditionDetails?.conditionActionDetailsResultVOS;
        return Array.isArray(actions) ? actions.length : 0;
      });

      function safeParseJson<T>(value: unknown, fallback: T): T {
        if (value === null || value === undefined || value === '') {
          return fallback;
        }
        if (typeof value !== 'string') {
          return value as T;
        }
        try {
          return JSON.parse(value) as T;
        } catch (error) {
          return fallback;
        }
      }

      onMounted(() => {
        const { query } = currentRoute.value;
        state.ruleId = query.id;

        getDetails(state.ruleId);
        const dictStore = useDictStore();
        state.conditionTypeList = dictStore.getDictItemOptionList(DictEnum.RULE_EVENT_TYPE);
        load();
      });

      const getDetails = async (id) => {
        const detail = await getRuleDetails(id);
        state.conditionDetails = {
          ...detail,
        };
        if (detail.conditionDetailsResultVOS?.length) {
          // 数据解构
          let conditionDetailsResult = { ...detail.conditionDetailsResultVOS[0] };
          conditionDetailsResult.antiShakeScheme = safeParseJson(
            conditionDetailsResult.antiShakeScheme,
            {
              occurrence: { first: true, last: false },
              frequency: { timeValue: 1, count: 1 },
            },
          );
          conditionDetailsResult.conditionScheme = safeParseJson(
            conditionDetailsResult.conditionScheme,
            [],
          );
          conditionDetailsResult.conditionActionDetailsResultVOS?.forEach((item) => {
            item.actionContent = safeParseJson(
              item.actionContent,
              item.actionType === 1 ? {} : { serial: [], parallel: [] },
            );
          });

          // console.log(conditionDetailsResult)
          state.conditionDetails = {
            ...state.conditionDetails,
            ...conditionDetailsResult,
            parentConditionRemark: state.conditionDetails.remark,
          };

          // 防抖状态
          state.antiShake = Number(conditionDetailsResult.antiShake ?? 1);
          // 防抖策略
          state.occurrenceRadio =
            conditionDetailsResult.antiShakeScheme?.occurrence?.first === true ? 1 : 2;
          state.frequency = conditionDetailsResult.antiShakeScheme?.frequency || {
            timeValue: 1,
            count: 1,
          };
          // console.log(state.conditionDetails)
          // 获取拍平后的结果
          let flattenItem = flatten([
            state.conditionDetails.conditionScheme?.map((item) => item.conditions),
            (state.conditionDetails.conditionActionDetailsResultVOS || []).map((item) => {
              if (item.actionType === 1) {
                return item;
              }
              return (item.actionContent?.parallel || []).concat(item.actionContent?.serial || []);
            }),
          ]).filter(Boolean);

          // console.log(flattenItem)
          // 筛选出获取productIdentification有值的
          let productIdentifications = flattenItem.filter((item) => {
            return item.leftParam?.productIdentification || item.productIdentification;
          });
          // 筛选出获取deviceIdentification有值的
          let deviceIdentifications = flattenItem.filter((item) => {
            return (
              item.leftParam?.deviceIdentification ||
              (item.deviceIdentification && item.deviceIdentification !== BizConstant.ALL)
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
          const uniqueProductIdentifications = noRepeat(productIdentifications).filter(Boolean);
          const uniqueDeviceIdentifications = noRepeat(deviceIdentifications)
            .filter(Boolean)
            .filter((item) => item !== BizConstant.ALL);
          const productInfos = uniqueProductIdentifications.length
            ? await getFullProductInfos({
                productIdentifications: uniqueProductIdentifications.join(','),
              })
            : [];
          //去重后批量请求device  考虑deviceIdentifications == all的情况
          const deviceInfos = uniqueDeviceIdentifications.length
            ? await detailBydeviceIdentifications({
                deviceIdentifications: uniqueDeviceIdentifications.join(','),
              })
            : [];
          state.productInfos = productInfos;
          state.deviceInfos = deviceInfos;
          state.conditionType = conditionDetailsResult.conditionType;
        }
        state.setIntervalParams.effectiveType = detail.effectiveType;
        state.setIntervalParams.appointContent = safeParseJson(detail.appointContent, {});
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
              createMessage.warning(
                `第${parentIndex + 1}条件组中第${childIndex + 1}项条件填写不完全`,
              );
              return false;
            }
            return true;

          case 2:
            const errorStatus = validate(conditions);
            if (errorStatus.length) {
              const { parentIndex, childIndex } = errorStatus[0];
              createMessage.warning(
                `第${parentIndex + 1}条件组中第${childIndex + 1}项条件填写不完全`,
              );
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
          await saveRuleConditionAction(params);
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
          await updateRuleConditionAction(params);
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
          onOk: async () => {
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
        readonlyMode,
        conditionGroupCount,
        actionCount,
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

  // 本页壳层样式;旧类保留给条件/动作子组件继续使用
  .linkage-detail-flexy {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 100%;
    padding: 16px 18px 24px;
    background: #f5f7fa;

    .flexy-header {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px 18px;
      background: #fff;
      border: 1px solid #e6edf3;
      border-radius: 8px;
      box-shadow: 0 8px 20px rgba(20, 31, 53, 0.04);

      .header-icon-wrap {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        border: 1px solid fade(@primary-color, 16%);
        border-radius: 8px;
        background: fade(@primary-color, 8%);

        .header-icon {
          font-size: 20px;
          color: @primary-color;
        }
      }

      .header-content {
        flex: 1;
        min-width: 0;
      }

      .header-line {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;

        .rule-name {
          max-width: 460px;
          font-size: 17px;
          font-weight: 600;
          color: @text-color-base;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .type-chip {
          flex-shrink: 0;
          height: 32px;
          border-color: #d8e3ec;
          background: #fbfcfd;
          color: #344054;
        }
      }

      .header-hint {
        margin-top: 4px;
        font-size: 12px;
        color: @text-color-secondary;
      }

      .header-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 12px;

        .meta-item {
          display: inline-flex;
          align-items: center;
          min-height: 28px;
          padding: 4px 10px;
          color: #344054;
          background: #f8fafc;
          border: 1px solid #e6edf3;
          border-radius: 6px;

          &.muted {
            color: #667085;
          }
        }

        .meta-label {
          margin-right: 6px;
          color: #8a96a8;
          font-size: 12px;
        }

        .meta-value {
          max-width: 280px;
          overflow: hidden;
          font-size: 12px;
          font-weight: 600;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .execution-log-btn {
        flex-shrink: 0;
        margin-left: auto;
        border-radius: 4px;
        box-shadow: none;
      }
    }

    .flexy-card {
      padding: 18px 20px;
      background: #fff;
      border: 1px solid #e6edf3;
      border-radius: 8px;
      box-shadow: 0 8px 20px rgba(20, 31, 53, 0.03);

      .step-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        margin-bottom: 14px;

        .step-title {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          min-width: 0;
        }

        .step-no {
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          width: 20px;
          height: 20px;
          border-radius: 6px;
          background: fade(@primary-color, 9%);
          color: @primary-color;
          font-size: 12px;
          font-weight: 700;
        }

        .step-title-text {
          font-size: 14px;
          font-weight: 600;
          color: @text-color-base;
        }

        .step-extra {
          margin-left: auto;
        }
      }

      .ant-input,
      .ant-input-affix-wrapper,
      .ant-select-selector,
      textarea.ant-input {
        border-radius: 6px;
      }
    }

    .flexy-footer {
      display: flex;
      justify-content: center;
      padding: 4px 0;

      .ant-btn {
        min-width: 96px;
        border-radius: 4px;
      }
    }

    @media (max-width: 768px) {
      padding: 12px;

      .flexy-header {
        flex-wrap: wrap;

        .execution-log-btn {
          width: 100%;
          margin-left: 0;
        }
      }

      .flexy-card .step-header {
        align-items: flex-start;
        flex-direction: column;

        .step-extra {
          width: 100%;
          margin-left: 0;
        }
      }
    }
  }
</style>
