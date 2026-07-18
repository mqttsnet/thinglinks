<template>
  <a-spin :spinning="spinning" :tip="t('common.loadingText')">
    <div class="type-list">
      <div class="title">{{ t('iot.link.engine.executionLog.action.selectService') }}</div>
      <div class="command-item">
        <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
          <a-button>{{
            command.service.serviceCode
              ? getLabelFilter(
                  command.service.serviceCode,
                  'serviceCode',
                  'serviceName',
                  productServiceList,
                )
              : t('iot.link.engine.executionLog.action.selectService')
          }}</a-button>
          <template #overlay>
            <a-menu @click="selectService($event)">
              <a-menu-item
                :key="serviceItem.serviceCode"
                :title="serviceItem.serviceName"
                v-for="serviceItem in productServiceList"
                >{{ serviceItem.serviceName }}</a-menu-item
              >
            </a-menu>
          </template>
        </a-dropdown>

        <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
          <a-button>{{
            command.commands.commandCode
              ? getLabelFilter(
                  command.commands.commandCode,
                  'commandCode',
                  'commandName',
                  productCommandList,
                )
              : t('iot.link.engine.executionLog.action.selectCommand')
          }}</a-button>
          <template #overlay>
            <a-menu @click="selectComand($event)">
              <a-menu-item
                :key="commandItem.commandCode"
                :title="commandItem.commandName"
                :params="commandItem"
                v-for="commandItem in productCommandList"
                >{{ commandItem.commandName }}</a-menu-item
              >
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
    <div class="type-list">
      <div class="title">{{
        t('iot.link.engine.executionLog.action.selectCommandProperties')
      }}</div>
      <div class="command-item">
        <div class="params-select">
          <div class="params-list" v-for="(item, index) in command.params" :key="item">
            <a-dropdown placement="bottomLeft" class="mr8" :trigger="['click']">
              <a-button>{{
                item.key
                  ? getLabelFilter(
                      item.key,
                      'parameterCode',
                      'parameterName',
                      productCommandRequestList,
                    )
                  : t('iot.link.engine.executionLog.action.selectCommandProperties')
              }}</a-button>
              <template #overlay>
                <a-menu @click="selectComandRequest($event, index)">
                  <a-menu-item
                    :key="commandRequestItem.parameterCode"
                    :title="commandRequestItem.parameterName"
                    :params="commandRequestItem"
                    v-for="commandRequestItem in filterProductCommandRequestListOptions"
                    >{{ commandRequestItem.parameterName }}</a-menu-item
                  >
                </a-menu>
              </template>
            </a-dropdown>
            <a-dropdown
              :disabled="!item.key"
              v-if="!isEqualIgnoreCase(item.datatype, 'jsonObject')"
              placement="bottomLeft"
              class="mr8"
              :trigger="['click']"
              :visible="item.visible"
              @visibleChange="visibleChange($event, index)"
            >
              <a-button>{{ item.value ? item.value : '参数值' }}</a-button>
              <template #overlay>
                <a-card style="padding: 10px">
                  <div style="display: flex; align-items: center">
                    <BasicHelp
                      v-if="item.key"
                      :text="
                        item.key
                          ? getLabelAlertInfoFilter(
                              item.key,
                              'parameterCode',
                              productCommandRequestList,
                            )
                          : ''
                      "
                      placement="top"
                    />
                    <a-input
                      :style="{ marginLeft: item.key ? '10px' : '0' }"
                      v-model:value="item.val"
                      :placeholder="t('common.inputText')"
                    />
                    <a-button
                      type="primary"
                      @click="confirmValue(item)"
                      style="margin-left: 10px"
                      >{{ t('common.okText') }}</a-button
                    >
                  </div>
                </a-card>
              </template>
            </a-dropdown>
            <!-- <a-button v-else @click.stop="openJsonObjectModal(item)">111</a-button> -->
            <a-button v-else @click.stop="openJsonObjectModal(item)">
              <SvgIcon name="iot-link-ota-upgradeCodeEditor" />
            </a-button>
            <a-popconfirm
              :title="t('iot.link.engine.executionLog.action.deleteTips2')"
              :ok-text="t('common.okText')"
              :cancel-text="t('common.cancelText')"
              @confirm="delCommandParams($event, index)"
              @cancel="cancel"
            >
              <a-button danger shape="circle" v-if="index > 0" class="del-btn" size="small">
                <CloseSquareOutlined />
              </a-button>
            </a-popconfirm>
          </div>
          <codeEditorDefine @register="registerModalCode" @submit-editor="submitEditor" />
          <a-button
            class="add-btn"
            v-if="
              filterProductCommandRequestListOptions.length &&
              productCommandRequestList.length > command.params.length
            "
            type="dashed"
            size="small"
            shape="circle"
            @click="addCommandParams()"
          >
            <PlusOutlined />
          </a-button>
        </div>
      </div>
    </div>
  </a-spin>
</template>
<script lang="ts">
  import {
    defineComponent,
    reactive,
    toRefs,
    onMounted,
    defineExpose,
    getCurrentInstance,
    ref,
  } from 'vue';
  import {
    ApartmentOutlined,
    ControlOutlined,
    ApiOutlined,
    PlusOutlined,
    CloseCircleOutlined,
    CloseSquareOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getFullProductInfo } from '/@/api/iot/link/product/product';
  import { getLabelFilter, getLabelAlertInfoFilter } from '/@/utils/thinglinks/common';
  import { isEqualIgnoreCase } from '/@/utils/thinglinks/common.tsx';
  import { useModal } from '/@/components/Modal';
  import codeEditorDefine from '/@/views/iot/link/ota/otaUpgrades/modal/codeEditorDefine.vue';
  import BasicHelp from '/@/components/Basic/src/BasicHelp.vue';
  import { canConvertType, isWithinScope, isExceedMaxLength } from '/@/utils/index';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'ActionType',
    components: {
      PlusOutlined,
      CloseSquareOutlined,
      codeEditorDefine,
      SvgIcon,
      BasicHelp,
    },
    props: {
      productIdentification: {
        type: String,
        default: '',
      },
      actionItem: {
        type: Object,
        default: {},
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const { proxy } = getCurrentInstance();

      const state = reactive({
        typeId: null,
        spinning: true,
        command: {
          service: {},
          commands: {},
          params: [
            {
              key: '',
              value: '',
            },
          ],
        },
        productIdentification: props.productIdentification,
        productServiceList: [],
        productCommandList: [],
        productCommandRequestList: [],
        filterProductCommandRequestListOptions: [],
        visible: false,
      });

      onMounted(async () => {
        console.log(props);
        await initData();
      });
      const initData = async () => {
        await getProductInfoList(state.productIdentification);
        // state.command.service.serviceCode = props.actionItem.serviceCode;
        if (props.actionItem?.productIdentification) {
          selectService({ key: props.actionItem.serviceCode });
          selectComand({ key: props.actionItem.cmd });

          filterProductCommandRequestList();
          // selectComandRequest({key: getKeyLabel(props.actionItem.params)})
          state.command.params = props.actionItem.params;
        } else {
          // 初始化
          state.command = {
            service: {},
            commands: {},
            params: [
              {
                key: '',
                value: '',
              },
            ],
          };
        }
        state.spinning = false;
      };
      const selectService = ($event) => {
        if (!$event.key) {
          return false;
        }
        console.log($event.key);
        state.command.service = {
          serviceCode: $event.key,
        };
        state.productCommandList = state.productServiceList.filter(
          (item) => item.serviceCode == $event.key,
        )[0]?.commands;
        // 初始化
        state.command.commands = {};
        state.command.params = [
          {
            key: '',
            value: '',
          },
        ];

        console.log(state.productCommandList);
      };
      const selectComand = ($event) => {
        if (!$event.key) {
          return false;
        }
        console.log($event);
        state.command.commands = {
          commandCode: $event.key,
        };
        state.productCommandRequestList = state.productCommandList?.filter(
          (item) => item.commandCode == $event.key,
        )[0]?.requests;

        state.command.params = [
          {
            key: '',
            value: '',
          },
        ];

        filterProductCommandRequestList();
      };
      const filterProductCommandRequestList = () => {
        state.filterProductCommandRequestListOptions = state.productCommandRequestList.filter(
          (item) => {
            return state.command.params.map((ite) => ite.key).indexOf(item.parameterCode) == -1;
          },
        );
        console.log(state.filterProductCommandRequestListOptions);
      };
      const selectComandRequest = ($event, index) => {
        if (!$event.key) {
          return false;
        }
        console.log($event, $event?.item?.params.dataType);
        // state.command.commandRequest = {
        //   commandRequestCode: $event.key,
        // };
        // state.command.params = [];
        state.command.params[index].key = $event.key;
        state.command.params[index].datatype = $event?.item?.params.datatype;
        filterProductCommandRequestList();
      };
      const addCommandParams = ($event) => {
        state.command.params.push({
          key: '',
          value: '',
        });
      };
      const delCommandParams = ($event, index) => {
        state.command.params.splice(index, 1);
        filterProductCommandRequestList();
      };
      const visibleChange = ($event, index) => {
        // console.log($event);
        state.command.params[index].visible = !state.command.params[index].visible;
      };
      async function getProductInfoList(productIdentification) {
        const res = await getFullProductInfo(productIdentification);
        state.productServiceList = res.services;
        console.log(state.productServiceList);
      }

      const getcmdList = (serviceCode, services) => {
        let list = [];
        services.map((item) => {
          if (item.serviceCode == serviceCode) {
            list = item.commands;
          }
        });
        return list;
      };
      const getcmdRequestList = (commandCode, commands) => {
        let list = [];
        commands.map((item) => {
          if (item.commandCode == commandCode) {
            list = item.requests;
          }
        });
        return list;
      };

      // S jsonObject相关处理
      const [registerModalCode, { openModal: openCode }] = useModal();
      const currentJsonKey = ref('');
      const openJsonObjectModal = (item: object) => {
        currentJsonKey.value = item.key;
        openCode(true, {
          value: item.value,
          isJsonObject: true,
        });
      };
      const submitEditor = async (value) => {
        try {
          state.command.params.map((item) => {
            if (item.key === currentJsonKey.value) {
              item.value = JSON.parse(value);
            }
          });
        } catch (err) {
          console.log(err);
        }
      };
      const { createMessage } = useMessage();

      const confirmValue = (item) => {
        if (!canConvertType(item?.datatype, item?.val)) {
          console.log(typeof '123');

          createMessage.warning('参数值类型错误');
          return;
        }
        const list = state.productCommandRequestList.filter((val) => {
          return val['parameterCode'] == item.key;
        });
        if (list.length) {
          // 需要从这个里面拿min跟max item里没有
          const newItem = list[0];
          if (!isWithinScope(item?.datatype, item?.val, newItem.min, newItem.max)) {
            createMessage.warning(`参数值超出范围，取值范围为${newItem.min}到${newItem.max}`);
            return;
          }
          if (!isExceedMaxLength(item?.datatype, item?.val, newItem.maxlength)) {
            createMessage.warning(`参数值超过最大长度，最大长度为${newItem.maxlength}`);
            return;
          }
        }
        item.visible = false;
        item.value = item.val;
      };

      // E jsonObject相关处理
      return {
        t,
        ...toRefs(state),
        selectService,
        selectComand,
        selectComandRequest,
        visibleChange,
        getcmdList,
        getcmdRequestList,
        getLabelFilter,
        getLabelAlertInfoFilter,
        filterProductCommandRequestList,
        addCommandParams,
        delCommandParams,
        isEqualIgnoreCase,
        registerModalCode,
        openJsonObjectModal,
        submitEditor,
        confirmValue,
        canConvertType,
        isWithinScope,
        isExceedMaxLength,
      };
    },
  });
</script>
<style lang="less" scoped>
  .type-list {
    // display: flex;
    align-items: center;
    flex-wrap: wrap;
    background-color: #fff;
    padding: 12px;
    box-shadow: 0 0 5px #ccc;
    margin-bottom: 20px;

    .title {
      font-weight: bold;
      position: relative;
      padding-left: 8px;
      margin-bottom: 10px;

      &:before {
        content: '';
        position: absolute;
        width: 3px;
        height: 14px;
        left: 0;
        top: 50%;
        margin-top: -7px;
        background-color: @primary-color;
      }
    }

    .type-item {
      width: 120px;
      padding: 10px;
      text-align: center;
      margin: 0 10px 10px 0;
      border: 1px solid #ccc;
      border-radius: 6px;
      cursor: pointer;

      &.active {
        border-color: #1a66ff;
      }

      img {
        width: 80px;
        margin: 0 auto;
        margin-bottom: 6px;
      }
    }

    .params-select {
      display: flex;
      align-items: center;
      flex-wrap: wrap;

      .add-btn {
        font-size: 18px;
        width: 32px;
        height: 32px;
        margin-left: 10px;
      }
    }

    .params-list {
      display: flex;
      position: relative;
      padding: 8px;
      border: 1px dashed #aaa;
      border-radius: 6px;
      margin: 10px 10px 10px;
      align-items: center;
      box-shadow: 0 0 4px #ccc;

      .del-btn {
        position: absolute;
        top: -12px;
        right: -12px;
      }
    }
  }
</style>
../../../../../../../api/iot/link/product/product
